package com.metalbenders.hardware;

import android.content.Context;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;

import com.qualcomm.robotcore.hardware.HardwareMap;

import java.util.Arrays;
import java.util.HashMap;

/**
 * 2026-27 FTC Season BIOBUZZ
 * <br/>
 * Hardware wrapper for Studica (YDLidar) T-Mini Lidar unit.
 * Provides access to hardware via USP -> Serial adapter.
 *
 * @author Team 14739 Metal Benders
 * @version 0.1
 * @since 2026-09-20
 */

public class StudicaLidar {

    private LidarThread lidarThread;
    private boolean isConnected = false;

    /**
     * Initializes and connects to the Lidar using the HardwareMap context.
     * Creates thread to perform non-blocking reads.
     * Call this inside your OpMode's init() before waitForStart().
     *
     * @param hardwareMap FTC HardwareMap from the OpMode
     * @return true if connection to USB LiDAR succeeded, false otherwise
     */
    public boolean init(HardwareMap hardwareMap) {
        Context context = hardwareMap.appContext;
        lidarThread = new LidarThread(context);

        if (!lidarThread.connectToUsbLidar()) {
            isConnected = false;
            return false;
        }

        isConnected = true;
        return true;
    }

    /**
     * Starts the background processing thread.
     * Call this after waitForStart() in OpMode.
     */
    public void start() {
        if (lidarThread != null && isConnected && !lidarThread.isAlive()) {
            lidarThread.start();
        }
    }

    /**
     * Returns a 360 element array representing distance readings in mm for degrees 0 through 359 degrees.
     *
     * @return double array of length 360 (values in millimeters)
     */
    public double[] getRanges(boolean inverted) {
        if (lidarThread != null) {
            return lidarThread.getRanges(inverted);
        }
        return new double[360];
    }

    /**
     * Gets distance reading at a specific angle (0° to 359°).
     *
     * @param degree Degree index (0 = Forward, 90 = Right, 180 = Rear, 270 = Left)
     * @return Distance in millimeters, or 0.0 if invalid/out of range
     */
    public double getDistanceAt(int degree, boolean inverted) {
        int index = Math.floorMod(degree, 360);
        double[] ranges = getRanges(inverted);
        return ranges[index];
    }

    /**
     * @return Total USB packet count processed by the driver
     */
    public long getPacketCount() {
        return lidarThread != null ? lidarThread.getPacketCount() : 0;
    }

    /**
     * @return True if USB initialization and connection succeeded
     */
    public boolean isConnected() {
        return isConnected;
    }

    /**
     * Stops the background thread and safely closes the USB connection.
     * Call this when the OpMode stops or in a cleanup block.
     */
    public void stop() {
        if (lidarThread != null) {
            lidarThread.close();
            isConnected = false;
        }
    }

    // ----------------------------------------------------------------------------------
    // Background Worker Thread
    // ----------------------------------------------------------------------------------
    private static class LidarThread extends Thread {
        private final Context context;
        private UsbDeviceConnection usbConnection;
        private UsbInterface usbInterface;
        private UsbEndpoint endpointIn;
        private UsbEndpoint endpointOut;
        private volatile boolean running = true;

        private final double[] ranges360 = new double[360];
        private static final int TIMEOUT_MS = 500;
        private final byte[] streamRingBuffer = new byte[65536];
        private int ringBufferLength = 0;
        private long totalPacketsParsed = 0;
        private final double[][] filterHistory = new double[360][4];

        public LidarThread(Context context) {
            this.context = context;
            Arrays.fill(ranges360, 0.0);
        }

        public boolean connectToUsbLidar() {
            UsbManager manager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
            if (manager == null) return false;

            HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
            if (deviceList.isEmpty()) return false;

            UsbDevice targetDevice = null;
            for (UsbDevice device : deviceList.values()) {
                int vendorId = device.getVendorId();
                if (vendorId == 0x10C4 || vendorId == 0x0403 || vendorId == 0x1A86) {
                    targetDevice = device;
                    break;
                }
            }

            if (targetDevice == null) return false;

            usbInterface = targetDevice.getInterface(0);
            for (int i = 0; i < usbInterface.getEndpointCount(); i++) {
                UsbEndpoint ep = usbInterface.getEndpoint(i);
                if (ep.getType() == UsbConstants.USB_ENDPOINT_XFER_BULK) {
                    if (ep.getDirection() == UsbConstants.USB_DIR_IN) {
                        endpointIn = ep;
                    } else {
                        endpointOut = ep;
                    }
                }
            }

            usbConnection = manager.openDevice(targetDevice);
            if (usbConnection == null) return false;

            usbConnection.claimInterface(usbInterface, true);

            // CP210x / FTDI / CH340 USB-Serial bridge baud rate configuration
            usbConnection.controlTransfer(0x41, 0x00, 0x0001, 0, null, 0, TIMEOUT_MS);
            byte[] baudData = new byte[]{(byte) 0x00, (byte) 0x84, (byte) 0x03, (byte) 0x00};
            usbConnection.controlTransfer(0x41, 0x1E, 0, 0, baudData, baudData.length, TIMEOUT_MS);
            usbConnection.controlTransfer(0x41, 0x03, 0x0800, 0, null, 0, TIMEOUT_MS);

            // Send active scan command [0xA5, 0x60]
            byte[] startCmd = new byte[]{(byte) 0xA5, (byte) 0x60};
            usbConnection.bulkTransfer(endpointOut, startCmd, startCmd.length, TIMEOUT_MS);

            return true;
        }

        @Override
        public void run() {
            byte[] usbTransferChunk = new byte[4096];

            while (running && !isInterrupted()) {
                if (usbConnection == null || endpointIn == null) break;

                int bytesRead = usbConnection.bulkTransfer(endpointIn, usbTransferChunk, usbTransferChunk.length, TIMEOUT_MS);
                if (bytesRead > 0) {
                    if (ringBufferLength + bytesRead > streamRingBuffer.length) {
                        ringBufferLength = 0; // Buffer overrun safety reset
                    }
                    System.arraycopy(usbTransferChunk, 0, streamRingBuffer, ringBufferLength, bytesRead);
                    ringBufferLength += bytesRead;

                    parseRingBuffer();
                }
            }
        }

        private void parseRingBuffer() {
            int cursor = 0;

            while (cursor <= ringBufferLength - 10) {
                // Point Cloud sync header check [0xAA, 0x55]
                if ((streamRingBuffer[cursor] & 0xFF) == 0xAA && (streamRingBuffer[cursor + 1] & 0xFF) == 0x55) {

                    int sampleCount = streamRingBuffer[cursor + 3] & 0xFF;
                    int expectedPacketSize = 10 + (sampleCount * 3);

                    if (cursor + expectedPacketSize > ringBufferLength) {
                        break; // Incomplete packet, wait for next transfer
                    }

                    int fsaRaw = ((streamRingBuffer[cursor + 5] & 0xFF) << 8) | (streamRingBuffer[cursor + 4] & 0xFF);
                    int lsaRaw = ((streamRingBuffer[cursor + 7] & 0xFF) << 8) | (streamRingBuffer[cursor + 6] & 0xFF);

                    double startAngle = (fsaRaw >> 1) / 64.0;
                    double endAngle = (lsaRaw >> 1) / 64.0;

                    double angleDiff = endAngle - startAngle;
                    if (angleDiff < 0) angleDiff += 360;

                    int dataOffset = cursor + 10;
                    for (int s = 0; s < sampleCount; s++) {
                        int byteIdx = dataOffset + (s * 3);

                        int quality = streamRingBuffer[byteIdx] & 0xFF;
                        int lowDist = streamRingBuffer[byteIdx + 1] & 0xFF;
                        int highDist = streamRingBuffer[byteIdx + 2] & 0xFF;

                        double distanceMm = ((highDist << 8) | lowDist) / 4.0;

                        if (distanceMm > 0 && quality > 0) {
                            double finalAngle = startAngle + ((angleDiff / Math.max(sampleCount - 1, 1)) * s);
                            int degreeIndex = (int) Math.round(finalAngle) % 360;
                            if (degreeIndex < 0) degreeIndex += 360;

                            // Moving Average Filter
                            double[] history = filterHistory[degreeIndex];
                            history[3] = history[2];
                            history[2] = history[1];
                            history[1] = history[0];
                            history[0] = distanceMm;

                            double filteredVal = (history[0] + history[1] + history[2] + history[3]) / 4.0;

                            synchronized (ranges360) {
                                ranges360[degreeIndex] = filteredVal;
                            }
                        }
                    }

                    totalPacketsParsed++;
                    cursor += expectedPacketSize;
                } else {
                    cursor++;
                }
            }

            if (cursor > 0) {
                int remainingBytes = ringBufferLength - cursor;
                if (remainingBytes > 0) {
                    System.arraycopy(streamRingBuffer, cursor, streamRingBuffer, 0, remainingBytes);
                }
                ringBufferLength = remainingBytes;
            }
        }

        public double[] getRanges(boolean inverted) {
            double[] snapshot = new double[360];
            synchronized (ranges360) {
                if (inverted)
                {
                    System.arraycopy(ranges360, 0, snapshot, 0, 360);
                    snapshot = reverseSensorArray(snapshot);
                } else {
                    System.arraycopy(ranges360, 0, snapshot, 0, 360);
                }

            }
            return snapshot;
        }

        private double[] reverseSensorArray(double[] array) {
            int left = 0;
            int right = array.length - 1;
            while (left < right) {
                double temp = array[left];
                array[left] = array[right];
                array[right] = temp;
                left++;
                right--;
            }
            return array;
        }

        public long getPacketCount() {
            return totalPacketsParsed;
        }

        public void close() {
            running = false;
            this.interrupt();
            if (usbConnection != null) {
                try {
                    byte[] stopCmd = new byte[]{(byte) 0xA5, (byte) 0x65};
                    usbConnection.bulkTransfer(endpointOut, stopCmd, stopCmd.length, TIMEOUT_MS);
                    usbConnection.releaseInterface(usbInterface);
                    usbConnection.close();
                } catch (Exception ignored) {
                }
            }
        }
    }
}