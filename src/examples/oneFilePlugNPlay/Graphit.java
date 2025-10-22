
import java.awt.*;

import java.awt.Dimension;

import javax.swing.*;
import java.awt.*;
import java.awt.Point;
import java.awt.event.*;
import java.awt.event.ActionEvent;
import java.util.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

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

        // setSpring(); // Default layout engine
        // setCircular();
        setMDS(); // Default layout engine
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

    public void showTraversal(AbstractGraph graph) {
        TraversalPanel traversalPanel = new TraversalPanel(graph);
        if (currentEngine != null) {
            traversalPanel.setLayoutEngine(currentEngine);
        }
        tabbedPane.addTab("Traversal", traversalPanel);
        tabbedPane.setSelectedComponent(traversalPanel);
    }

    // API: select engines
    public void setSpring() {
        this.currentEngine = new SpringLayoutEngine();
        if (currentGraphPanel != null)
            currentGraphPanel.setLayoutEngine(currentEngine, true);
    }

    public void setCircular() {
        this.currentEngine = new CircularLayoutEngine();
        if (currentGraphPanel != null)
            currentGraphPanel.setLayoutEngine(currentEngine, true);
    }

    public void setMDS() {
        this.currentEngine = new MdsLayoutEngine();
        if (currentGraphPanel != null)
            currentGraphPanel.setLayoutEngine(currentEngine, true);
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

/**
 * Strategy interface to execute a traversal on a graph that fills the track list.
 */
interface TraversalRunner {
    /**
     * Display name for UI selection.
     */
    String getName();

    /**
     * Run the traversal with the given start node.
     * Implementations should call graph.clearTrack() if they need a clean slate
     * before populating the track.
     */
    void run(AbstractGraph graph, int start) throws Exception;
}

abstract class AbstractGraph {
    private java.util.List<Integer> trackList = new java.util.ArrayList<>();

    public abstract int[][] exportMatrix();

    public void bfs(int start) {
        throw new UnsupportedOperationException("BFS not implemented");
    }

    public void dfs(int start) {
        throw new UnsupportedOperationException("DFS not implemented");
    }

    public void track(int current) {
        trackList.add(current);
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

    public double[][] deepClone() {
        int[][] m = this.exportMatrix();
        double[][] undirected = new double[m.length][m.length];

        for (int i = 0; i < m.length; i++) {
            for (int j = i + 1; j < m.length; j++) {
                if (m[i][j] == 0) {
                    m[i][j] = m[j][i];
                } else {
                    m[j][i] = m[i][j];
                }
            }
        }

        return undirected;
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

/**
 * Panel to execute a traversal (e.g., BFS) and visualize the visitation order
 * using the graph's track list (exportTrack). Time is represented as the step
 * index.
 */
class TraversalPanel extends JPanel {
    private final AbstractGraph graph;
    private final JComboBox<Integer> startNode;
    private final JComboBox<TraversalRunner> algoSelect;
    private final JButton runBfsBtn;
    private final JSlider timeSlider;
    private final JTable table;
    private final DefaultTableModel model;
    private final GraphPanel graphPanel;
    private java.util.List<Integer> currentTrack = java.util.Collections.emptyList();

    public TraversalPanel(AbstractGraph graph) {
        super(new BorderLayout());
        this.graph = graph;

        int n = 0;
        int[][] m = graph.exportMatrix();
        if (m != null)
            n = m.length;

        // Controls
    JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT));
    controls.add(new JLabel("Algo:"));
    algoSelect = new JComboBox<>();
        // default strategies
        algoSelect.addItem(new DefaultBfsRunner());
        algoSelect.addItem(new DefaultDfsRunner());
        // custom renderer to show getName()
        algoSelect.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof TraversalRunner) setText(((TraversalRunner) value).getName());
                return this;
            }
        });
    controls.add(algoSelect);
        startNode = new JComboBox<>();
        for (int i = 0; i < n; i++)
            startNode.addItem(i);
        controls.add(startNode);

        runBfsBtn = new JButton("Run");
        runBfsBtn.addActionListener(this::onRunBfs);
        controls.add(runBfsBtn);

        controls.add(new JLabel(" Time:"));
        timeSlider = new JSlider(0, 0, 0);
        timeSlider.setPreferredSize(new Dimension(200, 40));
        timeSlider.addChangeListener(e -> onTimeChanged());
        controls.add(timeSlider);

        add(controls, BorderLayout.NORTH);

        // Table
        model = new DefaultTableModel(new Object[] { "Time (step)", "Node" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(model);
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        // Graph panel on top; reuse current engine by leaving null here, Graphit can
        // set it
        graphPanel = new GraphPanel(graph);
        split.setTopComponent(graphPanel);
        split.setBottomComponent(new JScrollPane(table));
        split.setResizeWeight(0.7);
        add(split, BorderLayout.CENTER);

        // Empty state
        refreshTable(java.util.Collections.emptyList());

        // Try an immediate run (first algorithm, start node 0) to give instant feedback
        SwingUtilities.invokeLater(() -> {
            try {
                if (algoSelect.getItemCount() > 0 && startNode.getItemCount() > 0) {
                    algoSelect.setSelectedIndex(0);
                    startNode.setSelectedIndex(0);
                    onRunBfs(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "auto-run"));
                }
            } catch (UnsupportedOperationException ex) {
                showNotImplementedMessage(ex.getMessage());
            } catch (Exception ex) {
                // If traversal method throws, show helpful message
                showNotImplementedMessage(ex.getMessage());
            }
        });
    }

    private void onRunBfs(ActionEvent e) {
        Integer start = (Integer) startNode.getSelectedItem();
        if (start == null)
            return;
        TraversalRunner runner = (TraversalRunner) algoSelect.getSelectedItem();
        try {
            if (runner != null) {
                runner.run(graph, start.intValue());
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error running traversal: " + ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
        currentTrack = graph.exportTrack();
        refreshTable(currentTrack);
        updateSliderAndGraph();
    }

    private void refreshTable(List<Integer> track) {
        model.setRowCount(0);
        if (track == null)
            return;
        for (int i = 0; i < track.size(); i++) {
            model.addRow(new Object[] { i, track.get(i) });
        }
    }

    private void updateSliderAndGraph() {
        int max = Math.max(0, (currentTrack == null) ? 0 : (currentTrack.size() - 1));
        timeSlider.setMaximum(max);
        int cur = Math.min(timeSlider.getValue(), max);
        graphPanel.setTraversalState(currentTrack, cur);
    }

    private void onTimeChanged() {
        updateSliderAndGraph();
    }

    // Allow Graphit to inject the current layout engine used in the main Graph
    // panel
    public void setLayoutEngine(LayoutEngine engine) {
        graphPanel.setLayoutEngine(engine, true);
    }

    private void showNotImplementedMessage(String details) {
        removeAll();
        String msg = "Traversal not implemented. Please implement bfs(int) or dfs(int) in your graph.";
        if (details != null && !details.isBlank()) {
            msg += "\nDetails: " + details;
        }
        JLabel label = new JLabel("<html>" + msg.replace("\n", "<br/>") + "</html>");
        label.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        add(label, BorderLayout.CENTER);
        revalidate();
        repaint();
    }
}

/**
 * Places nodes evenly spaced on a circle.
 */
class CircularLayoutEngine implements LayoutEngine {
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

/**
 * Multidimensional Scaling (MDS) layout using classical MDS on graph geodesic
 * distances.
 * - Distances use weighted shortest-paths based on the adjacency matrix entries
 * (>0 as edge length).
 * - Embeds into 2D by taking the top-2 eigenpairs of the double-centered
 * matrix.
 * Only uses Java standard library.
 */
class MdsLayoutEngine implements LayoutEngine {

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

/**
 * A simple force-directed (spring) layout engine (Fruchterman-Reingold style).
 * Not optimized, but fine for small graphs in a teaching/demo context.
 */
class SpringLayoutEngine implements LayoutEngine {

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
        double[][] m = graph.deepClone();
        int n = (m == null) ? 0 : m.length;
        List<Point> positions = new ArrayList<>(n);
        if (n == 0)
            return positions;

        int width = Math.max(1, area.width);
        int height = Math.max(1, area.height);
        double pad = areaPadding;
        double minX = pad, minY = pad, maxX = width - pad, maxY = height - pad;

        // Compute average absolute edge weight for normalization
        double sumW = 0.0;
        int cntW = 0;
        if (m != null) {
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    double wij = m[i][j];
                    if (wij != 0) {
                        sumW += Math.abs((double) wij);
                        cntW++;
                    }
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
            for (int i = 0; i < n; i++) {
                dx[i] = 0;
                dy[i] = 0;
            }

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
                    dx[i] += fx;
                    dy[i] += fy;
                    dx[j] -= fx;
                    dy[j] -= fy;
                }
            }

            // attractive forces for edges (weighted)
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    if (m[i][j] != 0) { // edge present
                        // Normalize weight so typical edges are ~1.0 influence
                        double w = Math.abs((double) m[i][j]) / avgW;
                        // Clamp to keep the system stable
                        if (w < 0.1)
                            w = 0.1;
                        else if (w > 10.0)
                            w = 10.0;
                        double dxij = x[i] - x[j];
                        double dyij = y[i] - y[j];
                        double dist = Math.sqrt(dxij * dxij + dyij * dyij) + 0.01;
                        double force = ((dist * dist) / k) * w; // weighted attractive
                        double fx = force * dxij / dist;
                        double fy = force * dyij / dist;
                        // i pulls towards j (negative of dxij sign)
                        dx[i] -= fx;
                        dy[i] -= fy;
                        dx[j] += fx;
                        dy[j] += fy;
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
            if (temperature < 0.1)
                break;
        }

        for (int i = 0; i < n; i++) {
            positions.add(new Point((int) Math.round(x[i]), (int) Math.round(y[i])));
        }
        return positions;
    }
}

class DefaultBfsRunner implements TraversalRunner {
    @Override
    public String getName() { return "BFS"; }

    @Override
    public void run(AbstractGraph graph, int start) {
        graph.clearTrack();
        graph.bfs(start);
    }
}

class DefaultDfsRunner implements TraversalRunner {
    @Override
    public String getName() {
        return "DFS";
    }

    @Override
    public void run(AbstractGraph graph, int start) {
        graph.clearTrack();
        graph.dfs(start);
    }
}