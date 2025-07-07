package grapher.swingui;

import javax.swing.*;
import javax.swing.Timer;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

import grapher.interfaces.adj.AbstractGraph;
import grapher.interfaces.adj.IGraphVisualizer;
import grapher.interfaces.adj.IStepable;
import grapher.logic.graph.VEdge;
import grapher.logic.graph.VGraph;
import grapher.logic.graph.VNode;
import grapher.logic.graph.visualizer.ForceSimulator;

public class GraphPanel extends JPanel {
    private VGraph vGraph;
    private VNode dragged = null;
    private double offsetX, offsetY;
    private double centerX = 0;
    private double centerY = 0;

    // private java.util.List<int[]> edges = new ArrayList<>();
    private IGraphVisualizer visualizer;

    public GraphPanel(AbstractGraph graph) {
        this.visualizer = new ForceSimulator(graph);
        this.vGraph = this.visualizer.buildVGraph(graph);

        setBackground(Color.WHITE);

        // Mouse listeners for dragging
        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                for (VNode node : vGraph.nodes) {
                    if (node.contains(e.getX() - centerX, e.getY() - centerY)) {
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

        if (this.visualizer instanceof IStepable) {
            IStepable simulator = (IStepable) this.visualizer;
            System.out.println("is a simulator");
            Timer timer = new Timer(10, null);
            final int[] i = { 0 };

            timer.addActionListener(e -> {
                simulator.step();
                for (VNode n : this.vGraph.nodes) {
                    System.out.print("x:");
                    System.out.print(n.x);
                    System.out.print("\tvx:");
                    System.out.print(n.vx);
                    System.out.print("\ty:");
                    System.out.print(n.y);
                    System.out.print("\tvy:");
                    System.out.print(n.vy);
                    System.out.println();
                }
                System.out.println(i[0]);
                repaint();

                if (++i[0] >= 100) {
                    timer.stop();
                }
            });

            timer.start();
        }

    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        centerX = getBounds().getCenterX();
        centerY = getBounds().getCenterY();

        // Draw edges
        g.setColor(Color.GRAY);
        List<VNode> nodes = vGraph.nodes;
        List<VEdge> edges = vGraph.getAllEdges();

        for (VEdge edge : edges) {
            VNode a = edge.from;
            VNode b = edge.to;
            g.drawLine((int) (a.x + centerX), (int) (a.y + centerY), (int) (b.x + centerX), (int) (b.y + centerY));
        }
        // Draw nodes
        for (VNode node : nodes) {
            g.setColor(Color.BLUE);
            int x = (int) (node.x + centerX);
            int y = (int) (node.y + centerY);
            g.fillOval(x - node.radius, y - node.radius, node.radius * 2, node.radius * 2);
            g.setColor(Color.WHITE);
            g.drawString(node.name, x - 5, y + 5);
        }
    }

}