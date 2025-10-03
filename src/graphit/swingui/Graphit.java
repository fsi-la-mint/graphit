package graphit.swingui;

import javax.swing.*;

import graphit.interfaces.AbstractGraph;
import graphit.interfaces.LayoutEngine;
import graphit.swingui.layout.CircularLayoutEngine;
import graphit.swingui.layout.SpringLayoutEngine;
import graphit.swingui.layout.MdsLayoutEngine;

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

    public void setMethod2() {
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
