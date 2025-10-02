package graphit.swingui;

import java.awt.*;
import javax.swing.*;

import graphit.interfaces.AbstractGraph;

public class AdjazenzmatrixPanel extends JPanel {
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
