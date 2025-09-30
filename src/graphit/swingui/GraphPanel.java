package graphit.swingui;

import javax.swing.*;

import graphit.interfaces.AbstractGraph;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class GraphPanel extends JPanel {
    private java.util.List<VNode> nodes = new ArrayList<>();
    private VNode dragged = null;
    private int offsetX, offsetY;
    // private java.util.List<int[]> edges = new ArrayList<>();

    public GraphPanel(AbstractGraph graph) {
        setBackground(Color.WHITE);

        for (int i = 0; i < graph.exportMatrix().length; i++) {
            nodes.add(new VNode(Integer.toString(i), (i + 1) * 100, 100));
        }

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

    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Draw edges
        g.setColor(Color.GRAY);
        // for (int[] edge : edges) {
        // Node a = nodes.get(edge[0]);
        // Node b = nodes.get(edge[1]);
        // g.drawLine(a.x, a.y, b.x, b.y);
        // }

        // Draw nodes
        for (VNode node : nodes) {
            g.setColor(Color.BLUE);
            g.fillOval(node.x - node.radius, node.y - node.radius, node.radius * 2, node.radius * 2);
            g.setColor(Color.WHITE);
            g.drawString(node.name, node.x - 5, node.y + 5);
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
