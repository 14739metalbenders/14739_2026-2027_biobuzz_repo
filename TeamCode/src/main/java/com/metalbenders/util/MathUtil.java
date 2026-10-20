package com.metalbenders.util;

import com.qualcomm.hardware.limelightvision.LLResultTypes;

import java.util.List;

public class MathUtil {

    public static double calculateAverageTy(List<LLResultTypes.FiducialResult> fiducialResults) {
        double sumTy = 0;
        for (LLResultTypes.FiducialResult result : fiducialResults) {
            sumTy += result.getCameraPoseTargetSpace().getPosition().y;
        }
        return sumTy / fiducialResults.size();
    }

    public static double calculateAverageTx(List<LLResultTypes.FiducialResult> fiducialResults) {
        double sumTx = 0;
        for (LLResultTypes.FiducialResult result : fiducialResults) {
            sumTx += result.getCameraPoseTargetSpace().getPosition().x;
        }
        return sumTx / fiducialResults.size();
    }
}
