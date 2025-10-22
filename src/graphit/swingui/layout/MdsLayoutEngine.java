package graphit.swingui.layout;

import graphit.interfaces.AbstractGraph;
import graphit.interfaces.LayoutEngine;

import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Multidimensional Scaling (MDS) layout using classical MDS on graph geodesic
 * distances.
 * - Distances use weighted shortest-paths based on the adjacency matrix entries
 * (>0 as edge length).
 * - Embeds into 2D by taking the top-2 eigenpairs of the double-centered
 * matrix.
 * Only uses Java standard library.
 */
public class MdsLayoutEngine implements LayoutEngine {

    private final double padding;
    private final int eigenIterations;

    public MdsLayoutEngine() {
        this(40.0, 200);
    }

    public MdsLayoutEngine(double padding, int eigenIterations) {
        this.padding = Math.max(0, padding);
        this.eigenIterations = Math.max(20, eigenIterations);
    }

    @Override
    public List<Point> layout(AbstractGraph graph, Dimension area) {
        int[][] matrix = graph.exportMatrix();
        double[][] m = null;
        m = new double[matrix.length][];
        for (int i = 0; i < m.length; i++) {
            int[] row = matrix[i];
            if (row != null) {
                m[i] = new double[row.length];
                for (int j = 0; j < row.length; j++) {
                    m[i][j] = (double) row[j];
                }
            } else {
                m[i] = null;
            }
        }

        // double[][] m = graph.deepClone();

        int n = (m == null) ? 0 : m.length;
        List<Point> out = new ArrayList<>(n);
        if (n == 0)
            return out;

        // 1) All-pairs weighted shortest path distances
        double[][] D = allPairsWeightedShortestPaths(m);
        // Symmetrize distances to make them suitable for MDS if graph is directed
        symmetrize(D);

        // 2) Classical MDS: B = -1/2 * J * (D^2) * J
        double[][] B = buildDoubleCenteredMatrix(D);

        // 3) Top-2 eigenpairs of B via power iteration with deflation
        EigenPair e1 = powerIteration(B, eigenIterations);
        double[][] Bdef = deflate(B, e1);
        EigenPair e2 = powerIteration(Bdef, eigenIterations);

        // 4) Coordinates: x_i = sqrt(lambda) * v_i
        double[] X = new double[n];
        double[] Y = new double[n];
        if (e1.lambda > 1e-9 && e1.v != null) {
            double s = Math.sqrt(e1.lambda);
            for (int i = 0; i < n; i++)
                X[i] = e1.v[i] * s;
        }
        if (e2.lambda > 1e-9 && e2.v != null) {
            double s = Math.sqrt(e2.lambda);
            for (int i = 0; i < n; i++)
                Y[i] = e2.v[i] * s;
        }

        // 5) Normalize to area with padding
        int w = Math.max(1, area.width);
        int h = Math.max(1, area.height);
        double minX = Double.POSITIVE_INFINITY, maxX = Double.NEGATIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY, maxY = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < n; i++) {
            if (X[i] < minX)
                minX = X[i];
            if (X[i] > maxX)
                maxX = X[i];
            if (Y[i] < minY)
                minY = Y[i];
            if (Y[i] > maxY)
                maxY = Y[i];
        }
        if (!(maxX > minX)) { // degenerate
            for (int i = 0; i < n; i++)
                X[i] = i;
            minX = 0;
            maxX = Math.max(1, n - 1);
        }
        if (!(maxY > minY)) {
            for (int i = 0; i < n; i++)
                Y[i] = 0;
            minY = 0;
            maxY = 1;
        }

        double pad = Math.min(Math.min(w, h) * 0.2, padding);
        double targetMinX = pad, targetMaxX = w - pad;
        double targetMinY = pad, targetMaxY = h - pad;

        for (int i = 0; i < n; i++) {
            double nx = (X[i] - minX) / (maxX - minX);
            double ny = (Y[i] - minY) / (maxY - minY);
            int px = (int) Math.round(targetMinX + nx * (targetMaxX - targetMinX));
            int py = (int) Math.round(targetMinY + ny * (targetMaxY - targetMinY));
            out.add(new Point(px, py));
        }
        return out;
    }

    private static double[][] allPairsWeightedShortestPaths(double[][] m) {
        int n = m.length;
        double INF = 1e12;
        double[][] dist = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i == j) {
                    dist[i][j] = 0.0;
                } else if (m[i][j] > 0) {
                    dist[i][j] = (double) m[i][j]; // use weight as length
                } else {
                    dist[i][j] = INF; // no edge
                }
            }
        }

        // Floyd-Warshall for weighted graphs (assuming non-negative weights)
        for (int k = 0; k < n; k++) {
            for (int i = 0; i < n; i++) {
                double dik = dist[i][k];
                if (dik == INF)
                    continue;
                for (int j = 0; j < n; j++) {
                    double v = dik + dist[k][j];
                    if (v < dist[i][j])
                        dist[i][j] = v;
                }
            }
        }

        // Replace unreachable distances with a large finite value (slightly above max)
        double max = 0;
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                if (dist[i][j] < INF && dist[i][j] > max)
                    max = dist[i][j];
        double far = (max <= 0) ? 1.0 : (max * 1.5 + 1.0);
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                if (dist[i][j] >= INF)
                    dist[i][j] = far;
        return dist;
    }

    private static void symmetrize(double[][] D) {
        int n = D.length;
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                double a = D[i][j];
                double b = D[j][i];
                double s;
                if (Double.isFinite(a) && Double.isFinite(b))
                    s = 0.5 * (a + b);
                else if (Double.isFinite(a))
                    s = a;
                else if (Double.isFinite(b))
                    s = b;
                else
                    s = a; // both non-finite, keep as-is
                D[i][j] = D[j][i] = s;
            }
        }
    }

    private static double[][] buildDoubleCenteredMatrix(double[][] D) {
        int n = D.length;
        double[][] D2 = new double[n][n];
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                D2[i][j] = D[i][j] * D[i][j];

        double[] rowMean = new double[n];
        double[] colMean = new double[n];
        double totalMean = 0.0;

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                rowMean[i] += D2[i][j];
                colMean[j] += D2[i][j];
                totalMean += D2[i][j];
            }
        }
        for (int i = 0; i < n; i++)
            rowMean[i] /= n;
        for (int j = 0; j < n; j++)
            colMean[j] /= n;
        totalMean /= (n * n);

        double[][] B = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                B[i][j] = -0.5 * (D2[i][j] - rowMean[i] - colMean[j] + totalMean);
            }
        }
        return B;
    }

    private static class EigenPair {
        double lambda;
        double[] v;

        EigenPair(double lambda, double[] v) {
            this.lambda = lambda;
            this.v = v;
        }
    }

    private static EigenPair powerIteration(double[][] A, int iterations) {
        int n = A.length;
        double[] v = new double[n];
        Random rnd = new Random(123);
        for (int i = 0; i < n; i++)
            v[i] = rnd.nextDouble();
        normalize(v);

        double lambda = 0;
        for (int it = 0; it < iterations; it++) {
            double[] Av = multiply(A, v);
            double norm = norm(Av);
            if (norm < 1e-12)
                break;
            for (int i = 0; i < n; i++)
                v[i] = Av[i] / norm;
            lambda = rayleighQuotient(A, v);
        }
        return new EigenPair(lambda, v);
    }

    private static double[][] deflate(double[][] A, EigenPair e) {
        int n = A.length;
        double[][] B = new double[n][n];
        double lambda = e.lambda;
        double[] v = e.v;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                B[i][j] = A[i][j] - lambda * v[i] * v[j];
            }
        }
        return B;
    }

    private static double[] multiply(double[][] A, double[] x) {
        int n = A.length;
        double[] y = new double[n];
        for (int i = 0; i < n; i++) {
            double s = 0;
            for (int j = 0; j < n; j++)
                s += A[i][j] * x[j];
            y[i] = s;
        }
        return y;
    }

    private static void normalize(double[] v) {
        double n = norm(v);
        if (n < 1e-12)
            return;
        for (int i = 0; i < v.length; i++)
            v[i] /= n;
    }

    private static double norm(double[] v) {
        double s = 0;
        for (double a : v)
            s += a * a;
        return Math.sqrt(s);
    }

    private static double rayleighQuotient(double[][] A, double[] v) {
        double[] Av = multiply(A, v);
        double num = 0, den = 0;
        for (int i = 0; i < v.length; i++) {
            num += v[i] * Av[i];
            den += v[i] * v[i];
        }
        if (den < 1e-12)
            return 0;
        return num / den;
    }
}
