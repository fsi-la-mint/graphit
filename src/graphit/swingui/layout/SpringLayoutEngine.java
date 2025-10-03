package graphit.swingui.layout;

import graphit.interfaces.AbstractGraph;
import graphit.interfaces.LayoutEngine;

import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * A simple force-directed (spring) layout engine (Fruchterman-Reingold style).
 * Not optimized, but fine for small graphs in a teaching/demo context.
 */
public class SpringLayoutEngine implements LayoutEngine {

    private final int iterations;
    private final double areaPadding;

    public SpringLayoutEngine() {
        this(500, 40); // reasonable defaults
    }

    public SpringLayoutEngine(int iterations, double areaPadding) {
        this.iterations = Math.max(1, iterations);
        this.areaPadding = Math.max(0, areaPadding);
    }

    @Override
    public List<Point> layout(AbstractGraph graph, Dimension area) {
        int[][] m = graph.exportMatrix();
        int n = (m == null) ? 0 : m.length;
        List<Point> positions = new ArrayList<>(n);
        if (n == 0) return positions;

        int width = Math.max(1, area.width);
        int height = Math.max(1, area.height);
        double pad = areaPadding;
        double minX = pad, minY = pad, maxX = width - pad, maxY = height - pad;

        // Compute average absolute edge weight for normalization
        double sumW = 0.0; int cntW = 0;
        if (m != null) {
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    int wij = m[i][j];
                    if (wij != 0) { sumW += Math.abs((double) wij); cntW++; }
                }
            }
        }
        final double avgW = (cntW > 0) ? (sumW / cntW) : 1.0;

        // Initialize positions randomly
        Random rnd = new Random(42);
        double[] x = new double[n];
        double[] y = new double[n];
        for (int i = 0; i < n; i++) {
            x[i] = minX + rnd.nextDouble() * (maxX - minX);
            y[i] = minY + rnd.nextDouble() * (maxY - minY);
        }

        // Forces arrays
        double[] dx = new double[n];
        double[] dy = new double[n];

        // Fruchterman-Reingold constants
        double areaSize = width * height;
        double k = Math.sqrt(areaSize / (double) n); // optimal distance between nodes
        double temperature = Math.min(width, height) / 10.0;

        for (int iter = 0; iter < iterations; iter++) {
            // reset
            for (int i = 0; i < n; i++) { dx[i] = 0; dy[i] = 0; }

            // repulsive forces between all pairs
            for (int i = 0; i < n; i++) {
                for (int j = i + 1; j < n; j++) {
                    double dxij = x[i] - x[j];
                    double dyij = y[i] - y[j];
                    double dist2 = dxij * dxij + dyij * dyij + 0.01; // avoid div by zero
                    double dist = Math.sqrt(dist2);
                    double force = (k * k) / dist; // repulsive
                    double fx = force * dxij / dist;
                    double fy = force * dyij / dist;
                    dx[i] += fx; dy[i] += fy;
                    dx[j] -= fx; dy[j] -= fy;
                }
            }

            // attractive forces for edges (weighted)
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    if (m[i][j] != 0) { // edge present
                        // Normalize weight so typical edges are ~1.0 influence
                        double w = Math.abs((double) m[i][j]) / avgW;
                        // Clamp to keep the system stable
                        if (w < 0.1) w = 0.1; else if (w > 10.0) w = 10.0;
                        double dxij = x[i] - x[j];
                        double dyij = y[i] - y[j];
                        double dist = Math.sqrt(dxij * dxij + dyij * dyij) + 0.01;
                        double force = ((dist * dist) / k) * w; // weighted attractive
                        double fx = force * dxij / dist;
                        double fy = force * dyij / dist;
                        // i pulls towards j (negative of dxij sign)
                        dx[i] -= fx; dy[i] -= fy;
                        dx[j] += fx; dy[j] += fy;
                    }
                }
            }

            // limit maximum displacement by current temperature; update positions
            for (int i = 0; i < n; i++) {
                double disp = Math.sqrt(dx[i] * dx[i] + dy[i] * dy[i]);
                if (disp > 0) {
                    double limited = Math.min(temperature, disp);
                    x[i] += (dx[i] / disp) * limited;
                    y[i] += (dy[i] / disp) * limited;
                }

                // keep within bounds
                x[i] = Math.max(minX, Math.min(maxX, x[i]));
                y[i] = Math.max(minY, Math.min(maxY, y[i]));
            }

            // cool
            temperature *= 0.95;
            if (temperature < 0.1) break;
        }

        for (int i = 0; i < n; i++) {
            positions.add(new Point((int) Math.round(x[i]), (int) Math.round(y[i])));
        }
        return positions;
    }
}
