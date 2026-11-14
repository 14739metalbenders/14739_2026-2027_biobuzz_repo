import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageOutputStream;

/**
 * Generates nominal autonomous planning maps from the current field locations.
 * Run from the repository root: java autonomous_field_maps\GenerateMaps.java
 */
public class GenerateMaps {
    private static final Path OUTPUT = Path.of("autonomous_field_maps", "new_images");
    private static final Path OLD_IMAGES = Path.of("autonomous_field_maps", "old_images");
    private static final int OLD_FIELD_SIZE = 939;
    private static final int OLD_FIELD_TOP = 87;
    private static final int[] OLD_FIELD_LEFT = {123, 1139};
    private static final int WIDTH = 2550;
    private static final int HEIGHT = 3300;
    private static final int FIELD_SIZE = 1000;
    private static final int FIELD_TOP = 90;
    private static final int[] FIELD_LEFT = {160, 1400};
    private static final int[] ROW_TOP = {100, 1670};
    private static final Color RED = new Color(195, 25, 30);
    private static final Color BLUE = new Color(22, 65, 200);
    private static final Color ORANGE = new Color(187, 72, 0);
    private static final Font TITLE = new Font("SansSerif", Font.BOLD, 32);
    private static final Font SUBTITLE = new Font("SansSerif", Font.PLAIN, 25);
    private static final Font LABEL = new Font("SansSerif", Font.BOLD, 21);
    private static final Font LIST = new Font("SansSerif", Font.PLAIN, 26);
    private static final String[] COLLECTIONS = {"GARDEN", "PARK_SIDE_FLOWER", "HIVE_SIDE_FLOWER"};
    private static BufferedImage fieldArtwork;

    private record Point(double x, double y) {}
    private record Step(Point point, String description, boolean shoot) {}

    private static class Route {
        final List<Step> steps = new ArrayList<>();
        final boolean red;
        final boolean inner;
        int side; // 0 = audience, 1 = scoring
        int hiveSide;
        int roundsOnSide;
        int crossings;

        Route(boolean red, boolean active, boolean inner) {
            this.red = red;
            this.inner = inner;
            int gardenSide = red ? 0 : 1;
            side = active ? gardenSide : 1 - gardenSide;
            // An inactive start assumes the partner's preload tips the hive to our side.
            hiveSide = side;
            add(point(57.75, side == 0 ? 7 : 137, false),
                    "Start: " + sideName(side) + (active ? " (active)" : " (inactive)"), false);
        }

        Point point(double x, double y, boolean rotate) {
            if (red) return new Point(x, y);
            return new Point(144 - x, rotate ? 144 - y : y);
        }

        String sideName(int s) {
            return s == 0 ? "audience side" : "scoring side";
        }

        void add(Point point, String text, boolean shoot) {
            steps.add(new Step(point, text, shoot));
        }

        Point launch(int s) {
            return point(57.75, s == 0 ? 30 : 114, false);
        }

        Point waypoint(int s) {
            return point(inner ? 60 : 30, s == 0 ? 34 : 110, false);
        }

        void travelToSide(int target) {
            if (side == target) return;
            add(waypoint(side), "Waypoint: leave " + sideName(side), false);
            add(waypoint(target), "Waypoint: enter " + sideName(target), false);
            side = target;
            crossings++;
        }

        double distanceInches() {
            double distance = 0;
            for (int i = 1; i < steps.size(); i++) {
                Point from = steps.get(i - 1).point;
                Point to = steps.get(i).point;
                distance += Math.hypot(to.x - from.x, to.y - from.y);
            }
            return distance;
        }

        void shoot(String challenge, boolean activeStart, boolean preload) {
            // DriveToLaunchAndShoot first visits the launch point on the current side.
            if (side != hiveSide) {
                add(launch(side), "Launch position: check hive, do not shoot", false);
                travelToSide(hiveSide);
            }
            String note = preload && !activeStart ? " (after partner tips hive)" : "";
            roundsOnSide++;
            boolean tips = activeStart && preload || roundsOnSide == 2;
            add(launch(side), "SHOOT " + challenge + note + (tips ? "; hive tips after shot" : ""), true);
            if (tips) {
                hiveSide = 1 - hiveSide;
                roundsOnSide = 0;
            }
        }

        void build(List<String> strategy, boolean active) {
            for (String challenge : strategy) {
                switch (challenge) {
                    case "PRELOAD" -> shoot("preload", active, true);
                    case "GARDEN" -> {
                        travelToSide(red ? 0 : 1);
                        add(point(22, 7, true), "Garden start", false);
                        add(point(7, 7, true), "Garden end; collect", false);
                        shoot("garden", active, false);
                    }
                    case "PARK_SIDE_FLOWER" -> {
                        travelToSide(red ? 0 : 1);
                        add(point(10, 47, true), "Park-side flower; collect", false);
                        add(point(15, 47, true), "Park-side flower backup (~0.42 ft)", false);
                        shoot("park-side flower", active, false);
                    }
                    case "HIVE_SIDE_FLOWER" -> {
                        travelToSide(red ? 1 : 0);
                        add(point(47, 131.5, true), "Hive-side flower; collect", false);
                        add(point(47, 126.5, true), "Hive-side flower backup (~0.42 ft)", false);
                        shoot("hive-side flower", active, false);
                    }
                    case "PARK" -> {
                        travelToSide(red ? 1 : 0);
                        add(point(10, 92, true), "Park", false);
                    }
                    default -> throw new IllegalArgumentException(challenge);
                }
            }
        }
    }

    private static int px(int left, double x) {
        return left + (int) Math.round(x * FIELD_SIZE / 144.0);
    }

    private static int py(double y) {
        return FIELD_TOP + FIELD_SIZE - (int) Math.round(y * FIELD_SIZE / 144.0);
    }

    private static int brightness(int rgb) {
        return ((rgb >>> 16) & 255) + ((rgb >>> 8) & 255) + (rgb & 255);
    }

    private static BufferedImage extractFieldArtwork() throws IOException {
        List<Path> paths;
        try (Stream<Path> files = Files.list(OLD_IMAGES)) {
            paths = files.filter(path -> path.getFileName().toString().endsWith(".png")).sorted().toList();
        }
        if (paths.size() != 12) {
            throw new IOException("Expected the 12 original maps in " + OLD_IMAGES + "; found " + paths.size());
        }
        List<int[]> panels = new ArrayList<>();
        for (Path path : paths) {
            BufferedImage image = ImageIO.read(path.toFile());
            if (image == null || image.getWidth() != 2200 || image.getHeight() != 1700) {
                throw new IOException("Unexpected original map dimensions: " + path);
            }
            for (int left : OLD_FIELD_LEFT) {
                panels.add(image.getRGB(left, OLD_FIELD_TOP, OLD_FIELD_SIZE, OLD_FIELD_SIZE, null, 0, OLD_FIELD_SIZE));
            }
        }
        int[] pixels = new int[OLD_FIELD_SIZE * OLD_FIELD_SIZE];
        int[] colors = new int[panels.size()];
        // The shared field pixel is normally the most frequent across the 24 overlaid panels.
        for (int i = 0; i < pixels.length; i++) {
            for (int j = 0; j < panels.size(); j++) colors[j] = panels.get(j)[i];
            Arrays.sort(colors);
            int best = colors[0], bestCount = 0;
            for (int start = 0; start < colors.length;) {
                int end = start + 1;
                while (end < colors.length && colors[end] == colors[start]) end++;
                int count = end - start;
                if (count > bestCount || count == bestCount && brightness(colors[start]) > brightness(best)) {
                    best = colors[start];
                    bestCount = count;
                }
                start = end;
            }
            pixels[i] = best;
        }
        BufferedImage artwork = new BufferedImage(OLD_FIELD_SIZE, OLD_FIELD_SIZE, BufferedImage.TYPE_INT_RGB);
        artwork.setRGB(0, 0, OLD_FIELD_SIZE, OLD_FIELD_SIZE, pixels, 0, OLD_FIELD_SIZE);
        return artwork;
    }

    private static void field(Graphics2D g, int left, boolean red) {
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(fieldArtwork, left, FIELD_TOP, FIELD_SIZE, FIELD_SIZE, null);
        // The original diagrams show only the corresponding alliance's park zone.
        g.setColor(new Color(0, 125, 62));
        g.setStroke(new BasicStroke(3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10, new float[]{9, 6}, 0));
        int x1 = red ? 4 : 124;
        int y2 = red ? 100 : 60;
        g.drawRect(px(left, x1), py(y2), px(left, x1 + 16) - px(left, x1), py(y2 - 16) - py(y2));
        g.setFont(LABEL);
        g.drawString("PARK", px(left, x1 + 5), py(y2 - 16) + 24);
        g.setStroke(new BasicStroke(1));
        g.setFont(LABEL);
        g.setColor(new Color(100, 100, 100));
        for (int i = 0; i <= 144; i += 24) {
            g.drawString(Integer.toString(i / 12), px(left, i) - 9, FIELD_TOP + FIELD_SIZE + 24);
            g.drawString(Integer.toString(i / 12), left - 48, py(i) + 7);
        }
    }

    private static void arrow(Graphics2D g, int left, Point from, Point to, Color color, int index) {
        double x1 = px(left, from.x), y1 = py(from.y);
        double x2 = px(left, to.x), y2 = py(to.y);
        double dx = x2 - x1, dy = y2 - y1, len = Math.hypot(dx, dy);
        if (len < 1) return;
        // Slight offsets keep successive outbound and return legs distinguishable.
        double offset = (index % 5 - 2) * 3;
        double ox = -dy / len * offset, oy = dx / len * offset;
        x1 += ox; y1 += oy; x2 += ox; y2 += oy;
        g.setColor(color);
        g.setStroke(new BasicStroke(3.5f));
        g.drawLine((int) x1, (int) y1, (int) x2, (int) y2);
        double t = 0.60, ax = x1 + t * dx, ay = y1 + t * dy;
        double ux = dx / len, uy = dy / len;
        Path2D head = new Path2D.Double();
        head.moveTo(ax + 11 * ux, ay + 11 * uy);
        head.lineTo(ax - 9 * ux - 7 * uy, ay - 9 * uy + 7 * ux);
        head.lineTo(ax - 9 * ux + 7 * uy, ay - 9 * uy - 7 * ux);
        head.closePath();
        g.fill(head);
    }

    private static void drawRoute(Graphics2D g, int left, Route route, Color color) {
        for (int i = 1; i < route.steps.size(); i++) {
            arrow(g, left, route.steps.get(i - 1).point, route.steps.get(i).point, color, i);
        }
        Map<Point, List<Integer>> markers = new LinkedHashMap<>();
        for (int i = 0; i < route.steps.size(); i++) {
            markers.computeIfAbsent(route.steps.get(i).point, key -> new ArrayList<>()).add(i);
        }
        g.setFont(LABEL);
        for (Map.Entry<Point, List<Integer>> entry : markers.entrySet()) {
            String label = String.join(",", entry.getValue().stream()
                    .map(i -> i == 0 ? "S" : Integer.toString(i)).toList());
            int x = px(left, entry.getKey().x), y = py(entry.getKey().y);
            int w = Math.max(38, g.getFontMetrics().stringWidth(label) + 16);
            boolean shoot = entry.getValue().stream().anyMatch(i -> route.steps.get(i).shoot);
            if (shoot) {
                g.setColor(ORANGE);
                g.setStroke(new BasicStroke(4));
                g.draw(new Ellipse2D.Double(x - w / 2.0 - 5, y - 24, w + 10, 48));
            }
            g.setColor(Color.WHITE);
            g.fill(new Ellipse2D.Double(x - w / 2.0, y - 18, w, 36));
            g.setColor(color);
            g.setStroke(new BasicStroke(3));
            g.draw(new Ellipse2D.Double(x - w / 2.0, y - 18, w, 36));
            g.setColor(Color.BLACK);
            g.drawString(label, x - g.getFontMetrics().stringWidth(label) / 2, y + 7);
        }
    }

    private static String sharedDescription(String red, String blue) {
        if (red.equals(blue)) return red;
        String normalizedRed = red.replace("audience side", "<side>").replace("scoring side", "<side>");
        String normalizedBlue = blue.replace("audience side", "<side>").replace("scoring side", "<side>");
        if (!normalizedRed.equals(normalizedBlue)) {
            throw new IllegalStateException("Alliance routes do not share a legend: " + red + " / " + blue);
        }
        String sides = red.contains("audience side") ? "audience/scoring" : "scoring/audience";
        return red.replace("audience side", sides).replace("scoring side", sides);
    }

    private static void legend(Graphics2D g, Route red, Route blue) {
        if (red.steps.size() != blue.steps.size()) throw new IllegalStateException("Alliance route lengths differ");
        int rows = (red.steps.size() + 1) / 2;
        int firstY = FIELD_TOP + FIELD_SIZE + 67;
        if (firstY + (rows - 1) * 31 > 1550) {
            throw new IllegalStateException("Route legend does not fit: " + red.steps.size() + " stops");
        }
        for (int i = 0; i < red.steps.size(); i++) {
            Step step = red.steps.get(i);
            if (step.shoot != blue.steps.get(i).shoot) throw new IllegalStateException("Alliance shots differ");
            String text = (i == 0 ? "S" : Integer.toString(i)) + ". "
                    + sharedDescription(step.description, blue.steps.get(i).description);
            int x = i < rows ? 100 : 1300;
            int y = firstY + (i % rows) * 31;
            g.setFont(LIST);
            if (g.getFontMetrics().stringWidth(text) > 1150) {
                throw new IllegalStateException("Legend entry too wide: " + text);
            }
            g.setColor(step.shoot ? ORANGE : Color.BLACK);
            g.drawString(text, x, y);
        }
    }

    private static void writePng(BufferedImage image, Path output) throws IOException {
        ImageWriter writer = ImageIO.getImageWritersByFormatName("png").next();
        try (ImageOutputStream stream = ImageIO.createImageOutputStream(output.toFile())) {
            writer.setOutput(stream);
            IIOMetadata metadata = writer.getDefaultImageMetadata(
                    javax.imageio.ImageTypeSpecifier.createFromRenderedImage(image), writer.getDefaultWriteParam());
            String format = "javax_imageio_png_1.0";
            IIOMetadataNode root = (IIOMetadataNode) metadata.getAsTree(format);
            IIOMetadataNode density = new IIOMetadataNode("pHYs");
            density.setAttribute("pixelsPerUnitXAxis", "11811");
            density.setAttribute("pixelsPerUnitYAxis", "11811");
            density.setAttribute("unitSpecifier", "meter");
            root.appendChild(density);
            metadata.setFromTree(format, root);
            writer.write(null, new javax.imageio.IIOImage(image, null, metadata), writer.getDefaultWriteParam());
        } finally {
            writer.dispose();
        }
    }

    private static void render(List<String> strategy, boolean inner) throws IOException {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, WIDTH, HEIGHT);
        String sequence = String.join(" > ", strategy);
        g.setFont(TITLE);
        g.setColor(Color.BLACK);
        g.drawString(sequence + "  |  " + (inner ? "INNER" : "OUTER") + " waypoints", 100, 55);
        g.setFont(SUBTITLE);
        for (int row = 0; row < 2; row++) {
            boolean active = row == 0;
            Graphics2D section = (Graphics2D) g.create();
            section.translate(0, ROW_TOP[row]);
            section.setFont(TITLE);
            section.setColor(Color.BLACK);
            section.drawString(active ? "ACTIVE START" : "INACTIVE START", 100, 5);
            section.setFont(SUBTITLE);
            section.drawString(active
                    ? "Our preload tips the hive; then two shooting rounds per tip."
                    : "Partner preload tips hive to us; then two of our shooting rounds per tip.", 100, 35);
            Route red = new Route(true, active, inner);
            Route blue = new Route(false, active, inner);
            red.build(strategy, active);
            blue.build(strategy, active);
            double distance = red.distanceInches();
            if (red.crossings != blue.crossings || Math.abs(distance - blue.distanceInches()) > 1e-6) {
                throw new IllegalStateException("Alliance route metrics differ");
            }
            section.setFont(TITLE);
            section.setColor(Color.BLACK);
            section.drawString(String.format(Locale.ROOT, "Side crossings: %d    Distance: ~%.1f ft",
                    red.crossings, distance / 12.0), 1100, 5);
            for (int i = 0; i < 2; i++) {
                boolean isRed = i == 0;
                section.setFont(TITLE);
                section.setColor(isRed ? RED : BLUE);
                section.drawString(isRed ? "RED" : "BLUE", FIELD_LEFT[i], 76);
                field(section, FIELD_LEFT[i], isRed);
                drawRoute(section, FIELD_LEFT[i], isRed ? red : blue, isRed ? RED : BLUE);
            }
            legend(section, red, blue);
            section.dispose();
        }
        g.setFont(new Font("SansSerif", Font.PLAIN, 22));
        g.setColor(Color.DARK_GRAY);
        g.drawString("Field axes in feet. Side names are red/blue. Orange ring = shoot; shared numbers = revisits.", 100, HEIGHT - 62);
        g.drawString("Nominal straight-line routes only; live detection, timeouts and the 25-second park override can change execution.", 100, HEIGHT - 33);
        g.dispose();
        String name = String.join("__", strategy);
        name += "__" + (inner ? "INNER" : "OUTER") + ".png";
        writePng(image, OUTPUT.resolve(name));
    }

    private static void addPermutations(List<List<String>> permutations, List<String> current, List<String> remaining) {
        permutations.add(new ArrayList<>(current));
        for (String item : remaining) {
            List<String> next = new ArrayList<>(remaining);
            next.remove(item);
            current.add(item);
            addPermutations(permutations, current, next);
            current.remove(current.size() - 1);
        }
    }

    public static void main(String[] args) throws IOException {
        Files.createDirectories(OUTPUT);
        fieldArtwork = extractFieldArtwork();
        List<List<String>> collections = new ArrayList<>();
        addPermutations(collections, new ArrayList<>(), Arrays.asList(COLLECTIONS));
        List<List<String>> strategies = new ArrayList<>();
        for (List<String> selection : collections) {
            List<String> strategy = new ArrayList<>();
            strategy.add("PRELOAD");
            strategy.addAll(selection);
            strategy.add("PARK");
            strategies.add(strategy);
        }
        if (strategies.size() != 16) throw new IllegalStateException("Expected 16 strategies");
        int count = 0;
        for (List<String> strategy : strategies) {
            for (boolean inner : new boolean[]{true, false}) {
                render(strategy, inner);
                count++;
            }
        }
        System.out.println("Generated " + count + " maps in " + OUTPUT);
    }
}
