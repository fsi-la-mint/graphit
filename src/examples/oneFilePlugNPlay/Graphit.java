
import java.awt.*;

import java.awt.Dimension;

import javax.swing.*;
import java.awt.Point;
import java.awt.event.*;
import java.util.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.swing.*;

public class Graphit extends JFrame {

    private JTabbedPane tabbedPane;
    private LayoutEngine currentEngine = null;
    private GraphPanel currentGraphPanel = null;

    public Graphit() {
        super("Graphit | Learning Graphs");
        setSize(500, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        tabbedPane = new JTabbedPane();
        getContentPane().add(tabbedPane);

        // Optionally, add an initial tab
        JPanel home = new JPanel();
        home.add(new JLabel("Welcome to Graphit!"));
        tabbedPane.addTab("Home", home);

        setSpring(); // Default layout engine
        setVisible(true);
    }

    public void showAdjazenzmatrix(AbstractGraph graph) {
        AdjazenzmatrixPanel matrixPanel = new AdjazenzmatrixPanel(graph);
        tabbedPane.addTab("Adjazenzmatrix", matrixPanel);
        tabbedPane.setSelectedComponent(matrixPanel);
    }

    public void showGraph(AbstractGraph graph) {
        GraphPanel graphPanel = new GraphPanel(graph, currentEngine);
        this.currentGraphPanel = graphPanel;
        tabbedPane.addTab("Graph", graphPanel);
        tabbedPane.setSelectedComponent(graphPanel);
    }

    // API: select engines
    public void setSpring() {
        this.currentEngine = new SpringLayoutEngine();
        if (currentGraphPanel != null)
            currentGraphPanel.setLayoutEngine(currentEngine, true);
    }

    public void setMethod2() {
        this.currentEngine = new CircularLayoutEngine();
        if (currentGraphPanel != null)
            currentGraphPanel.setLayoutEngine(currentEngine, true);
    }
}

abstract class AbstractGraph {
    private java.util.List<Integer> trackList = new java.util.ArrayList<>();

    public abstract int[][] exportMatrix();

    public void bfs(int start) {
        bfs(start, -1);
    }

    public void track(int current) {
        trackList.add(current);
    }

    public void bfs(int start, int end) {

    }

    public final java.util.List<Integer> exportTrack() {
        return trackList;
    }

    public void clearTrack() {
        trackList.clear();
    }

    public void setTrack(java.util.List<Integer> tracked) {
        trackList = java.util.List.copyOf(tracked);
    }

    public void setTrack(int[] tracked) {
        if (tracked == null) {
            trackList = new java.util.ArrayList<Integer>();
            return;
        }
        trackList = new java.util.ArrayList<Integer>(tracked.length);
        for (int i = 0; i < tracked.length; i++) {
            trackList.add(Integer.valueOf(tracked[i]));
        }
    }
}

interface ITest {

}

/**
 * Interface for graph layout engines that compute node positions.
 * Implementations should return one Point per node (index-aligned to the
 * graph's adjacency matrix rows/cols).
 */
interface LayoutEngine {
    /**
     * Compute positions for all nodes in the given graph.
     *
     * @param graph The graph model.
     * @param area  The available drawing area; engines should keep nodes inside it.
     * @return A list of points of size N where N == graph.exportMatrix().length.
     */
    List<Point> layout(AbstractGraph graph, Dimension area);

    /**
     * Human-friendly name of the engine.
     */
    default String getName() {
        return getClass().getSimpleName();
    }
}

class AdjazenzmatrixPanel extends JPanel {
    private AbstractGraph graph;
    private JTable table;

    public AdjazenzmatrixPanel(AbstractGraph graph) {
        super(new BorderLayout());
        this.graph = graph;
        initTable();
    }

    private void initTable() {
        int[][] matrix = graph.exportMatrix();
        if (matrix == null || matrix.length == 0)
            return;

        int size = matrix.length;
        Object[][] data = new Object[size][size];
        String[] columnNames = new String[size];

        for (int i = 0; i < size; i++) {
            columnNames[i] = Integer.toString(i);
            for (int j = 0; j < size; j++) {
                data[i][j] = Integer.toString(matrix[i][j]);
            }
        }

        table = new JTable(data, columnNames);
        table.getModel().addTableModelListener(new javax.swing.event.TableModelListener() {
            public void tableChanged(javax.swing.event.TableModelEvent e) {
                int row = e.getFirstRow();
                int col = e.getColumn();
                Object newValue = table.getValueAt(row, col);
                System.out.println("Cell updated at (" + row + ", " + col + "): " + newValue);
            }
        });
        removeAll();
        add(new JScrollPane(table), BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    public void render() {
        initTable();
    }

    public void render(AbstractGraph graph) {
        this.initTable();
        initTable();
    }
}

class GraphPanel extends JPanel {
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

/**
 * A simple force-directed (spring) layout engine (Fruchterman-Reingold style).
 * Not optimized, but fine for small graphs in a teaching/demo context.
 */
class SpringLayoutEngine implements LayoutEngine {

    private final int iterations;
    private final double areaPadding;

    public SpringLayoutEngine() {
        this(300, 40); // reasonable defaults
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

/**
 * Places nodes evenly spaced on a circle.
 */
class CircularLayoutEngine implements LayoutEngine {
    @Override
    public List<Point> layout(AbstractGraph graph, Dimension area) {
        int[][] m = graph.exportMatrix();
        int n = (m == null) ? 0 : m.length;
        List<Point> positions = new ArrayList<>(n);
        if (n == 0) return positions;

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