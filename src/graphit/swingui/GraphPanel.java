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
        if (relayout) applyLayout();
    }

    public void applyLayout() {
        if (layoutEngine == null || graph == null) return;
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

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Draw edges from adjacency matrix
            int[][] matrix = graph.exportMatrix();
            if (matrix != null && matrix.length == nodes.size()) {
                g2.setColor(new Color(0x66, 0x66, 0x66));
                g2.setStroke(new BasicStroke(1.2f));
                for (int i = 0; i < matrix.length; i++) {
                    for (int j = 0; j < matrix[i].length; j++) {
                        if (matrix[i][j] != 0) {
                            VNode a = nodes.get(i);
                            VNode b = nodes.get(j);
                            g2.drawLine(a.x, a.y, b.x, b.y);
                        }
                    }
                }
            }

            // Draw nodes
            for (VNode node : nodes) {
                g2.setColor(Color.BLUE);
                g2.fillOval(node.x - node.radius, node.y - node.radius, node.radius * 2, node.radius * 2);
                g2.setColor(Color.WHITE);
                g2.drawString(node.name, node.x - 5, node.y + 5);
            }
        } finally {
            g2.dispose();
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
