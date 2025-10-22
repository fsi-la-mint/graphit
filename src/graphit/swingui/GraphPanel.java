package graphit.swingui;

import javax.swing.*;

import graphit.interfaces.AbstractGraph;
import graphit.interfaces.LayoutEngine;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class GraphPanel extends JPanel {
    private final AbstractGraph graph;
    private final java.util.List<VNode> nodes = new ArrayList<>();
    private VNode dragged = null;
    private int offsetX, offsetY;
    private LayoutEngine layoutEngine;
    // Traversal visualization state
    private final java.util.Set<Integer> visited = new java.util.HashSet<>();
    private Integer currentNodeIndex = null;
    // private java.util.List<int[]> edges = new ArrayList<>();

    public GraphPanel(AbstractGraph graph) {
        this(graph, null);
    }

    public GraphPanel(AbstractGraph graph, LayoutEngine engine) {
        this.graph = graph;
        this.layoutEngine = engine;
        setBackground(Color.WHITE);

        int n = graph.exportMatrix() == null ? 0 : graph.exportMatrix().length;
        for (int i = 0; i < n; i++) {
            // temporary positions; will be replaced by layout engine if provided
            nodes.add(new VNode(Integer.toString(i), (i + 1) * 60, 100));
        }

        // Apply initial layout after component is realized, so we have a size
        SwingUtilities.invokeLater(() -> applyLayoutIfPossible());

        // Mouse listeners for dragging
        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                for (VNode node : nodes) {
                    if (node.contains(e.getX(), e.getY())) {
                        dragged = node;
                        offsetX = e.getX() - node.x;
                        offsetY = e.getY() - node.y;
                        break;
                    }
                }
            }

            public void mouseReleased(MouseEvent e) {
                dragged = null;
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                if (dragged != null) {
                    dragged.x = e.getX() - offsetX;
                    dragged.y = e.getY() - offsetY;
                    repaint();
                }
            }
        });

        // Re-layout when the panel is resized (optional)
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                // Only auto-reflow if we have an engine configured
                if (layoutEngine != null) {
                    applyLayout();
                }
            }
        });

    }

    public void setLayoutEngine(LayoutEngine engine, boolean relayout) {
        this.layoutEngine = engine;
        if (relayout)
            applyLayout();
    }

    public void applyLayout() {
        if (layoutEngine == null || graph == null)
            return;
        Dimension area = getSize();
        if (area == null || area.width < 10 || area.height < 10) {
            Container p = getParent();
            if (p != null) {
                area = p.getSize();
            }
            if (area == null || area.width < 10 || area.height < 10) {
                area = new Dimension(500, 400);
            }
        }
        java.util.List<Point> pts = layoutEngine.layout(graph, area);
        for (int i = 0; i < nodes.size() && i < pts.size(); i++) {
            Point pt = pts.get(i);
            nodes.get(i).x = pt.x;
            nodes.get(i).y = pt.y;
        }
        repaint();
    }

    private void applyLayoutIfPossible() {
        if (layoutEngine != null) {
            applyLayout();
        }
    }

    // Traversal controls
    public void setTraversalState(java.util.List<Integer> track, int timeIndex) {
        visited.clear();
        currentNodeIndex = null;
        if (track != null) {
            for (int i = 0; i < track.size(); i++) {
                Integer idx = track.get(i);
                if (idx == null)
                    continue;
                if (i < timeIndex)
                    visited.add(idx);
                else if (i == timeIndex)
                    currentNodeIndex = idx;
            }
        }
        repaint();
    }

    public void clearTraversalState() {
        visited.clear();
        currentNodeIndex = null;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int R = 12; // visual node radius; adjust to your node size

            int[][] matrix = graph.exportMatrix();
            if (matrix == null || matrix.length != nodes.size()) {
                return;
            }
            g2.setColor(new Color(0x66, 0x66, 0x66));
            g2.setStroke(new BasicStroke(1.2f));

            for (int i = 0; i < matrix.length; i++) {
                for (int j = 0; j < matrix[i].length; j++) {
                    if (matrix[i][j] == 0 || i == j)
                        continue;

                    VNode a = nodes.get(i);
                    VNode b = nodes.get(j);

                    double dx = b.x - a.x, dy = b.y - a.y;
                    double dist = Math.hypot(dx, dy);
                    if (dist < 1e-6)
                        continue;

                    double ux = dx / dist, uy = dy / dist;

                    // start at source outline, end at target outline
                    int sx = (int) Math.round(a.x + ux * a.radius);
                    int sy = (int) Math.round(a.y + uy * a.radius);
                    int tx = (int) Math.round(b.x - ux * b.radius);
                    int ty = (int) Math.round(b.y - uy * b.radius);

                    // also pull the arrowhead back a bit so its tips don't intrude
                    int arrowPad = 2; // tweak as needed
                    tx = (int) Math.round(tx - ux * arrowPad);
                    ty = (int) Math.round(ty - uy * arrowPad);

                    drawArrow(g2, sx, sy, tx, ty);
                }
            }

            // Draw nodes with traversal coloring
            for (int idx = 0; idx < nodes.size(); idx++) {
                VNode node = nodes.get(idx);
                Color fill = Color.BLUE;
                if (currentNodeIndex != null && idx == currentNodeIndex.intValue()) {
                    fill = new Color(0x80, 0x00, 0x80); // purple for current
                } else if (visited.contains(idx)) {
                    fill = new Color(0x00, 0x64, 0x00); // dark green for visited
                }
                g2.setColor(fill);
                g2.fillOval(node.x - node.radius, node.y - node.radius, node.radius * 2, node.radius * 2);
                g2.setColor(Color.WHITE);
                g2.drawString(node.name, node.x - 5, node.y + 5);
            }
        } finally {
            g2.dispose();
        }

    }

    private static void drawArrow(Graphics2D g2, int x1, int y1, int x2, int y2) {
        g2.drawLine(x1, y1, x2, y2);

        double phi = Math.toRadians(25);
        int barb = 10;

        double dx = x2 - x1, dy = y2 - y1;
        double theta = Math.atan2(dy, dx);

        for (int s : new int[] { 1, -1 }) {
            double rho = theta + Math.PI + s * phi;
            int ax = (int) Math.round(x2 + barb * Math.cos(rho));
            int ay = (int) Math.round(y2 + barb * Math.sin(rho));
            g2.drawLine(x2, y2, ax, ay);
        }
    }

}

class VNode {
    public int x, y, radius = 20;
    public String name;

    public VNode(String name, int x, int y) {
        this.name = name;
        this.x = x;
        this.y = y;
    }

    public boolean contains(int mx, int my) {
        int dx = x - mx;
        int dy = y - my;
        return dx * dx + dy * dy <= radius * radius;
    }
}
