package graphit.swingui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

import graphit.interfaces.*;

/**
 * Panel to execute a traversal (e.g., BFS) and visualize the visitation order
 * using the graph's track list (exportTrack). Time is represented as the step
 * index.
 */
public class TraversalPanel extends JPanel {
    private final AbstractGraph graph;
    private final JComboBox<Integer> startNode;
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
        controls.add(new JLabel("Start:"));
        startNode = new JComboBox<>();
        for (int i = 0; i < n; i++)
            startNode.addItem(i);
        controls.add(startNode);

        runBfsBtn = new JButton("Run BFS");
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
    }

    private void onRunBfs(ActionEvent e) {
        Integer start = (Integer) startNode.getSelectedItem();
        if (start == null)
            return;
        graph.clearTrack();
        try {
            graph.bfs(start.intValue());
        } catch (UnsupportedOperationException ex) {
            JOptionPane.showMessageDialog(this, "bfs(start) not implemented for this graph.", "Not Implemented",
                    JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error running BFS: " + ex.getMessage(), "Error",
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
}
