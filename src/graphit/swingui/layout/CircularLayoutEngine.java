package graphit.swingui.layout;

import graphit.interfaces.AbstractGraph;
import graphit.interfaces.LayoutEngine;

import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

/**
 * Places nodes evenly spaced on a circle.
 */
public class CircularLayoutEngine implements LayoutEngine {
    @Override
    public List<Point> layout(AbstractGraph graph, Dimension area) {
        double[][] m = graph.deepClone();
        int n = (m == null) ? 0 : m.length;
        List<Point> positions = new ArrayList<>(n);
        if (n == 0)
            return positions;

        int width = Math.max(1, area.width);
        int height = Math.max(1, area.height);
        int cx = width / 2;
        int cy = height / 2;
        int radius = Math.max(1, Math.min(width, height) / 2 - 40);

        for (int i = 0; i < n; i++) {
            double angle = 2 * Math.PI * i / n;
            int x = cx + (int) Math.round(radius * Math.cos(angle));
            int y = cy + (int) Math.round(radius * Math.sin(angle));
            positions.add(new Point(x, y));
        }
        return positions;
    }
}
