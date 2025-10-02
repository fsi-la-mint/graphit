
import java.awt.*;

import javax.swing.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

public class Graphit extends JFrame {

    private JTabbedPane tabbedPane;

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

        setVisible(true);
    }

    public void showAdjazenzmatrix(AbstractGraph graph) {
        AdjazenzmatrixPanel matrixPanel = new AdjazenzmatrixPanel(graph);
        tabbedPane.addTab("Adjazenzmatrix", matrixPanel);
        tabbedPane.setSelectedComponent(matrixPanel);
    }

    public void showGraph(AbstractGraph graph) {
        GraphPanel matrixPanel = new GraphPanel(graph);
        tabbedPane.addTab("Adjazenzmatrix", matrixPanel);
        tabbedPane.setSelectedComponent(matrixPanel);
    }
}

abstract class AbstractGraph {

    public abstract int[][] exportMatrix();

    // public final int[][] exportMatrix() {
    // return new int[0][0];
    // }

    public void bfs(int start) {
        bfs(start, -1);
    }

    public void bfs(int start, int end) {

    }
}

interface ITest {

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
        System.out.println("hello?");

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
        System.out.println("repaint should be performed");
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