package grapher.swingui;

import javax.swing.*;

import grapher.interfaces.adj.AbstractGraph;

public class MainFrame extends JFrame {

    private JTabbedPane tabbedPane;

    public MainFrame() {
        super("Grapher | Learning Graphs");
        setSize(500, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        tabbedPane = new JTabbedPane();
        getContentPane().add(tabbedPane);

        // Optionally, add an initial tab
        JPanel home = new JPanel();
        home.add(new JLabel("Welcome to Grapher"));
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
        tabbedPane.addTab("Graph Visualization", matrixPanel);
        tabbedPane.setSelectedComponent(matrixPanel);
    }
}
