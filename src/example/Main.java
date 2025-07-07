package example;

import grapher.swingui.MainFrame;

public class Main {
    public static void main(String[] args) {

        AdjGraph graph = new AdjGraph(5);

        graph.getMatrix()[0][1] = 10;
        graph.getMatrix()[2][0] = 4;
        graph.getMatrix()[3][4] = 4;

        MainFrame frame = new MainFrame();
        frame.showAdjazenzmatrix(graph);
        frame.showGraph(graph);
    }

}
