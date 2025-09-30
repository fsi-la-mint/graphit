package examples.oneFilePlugNPlay;

public class Main {
    public static void main(String[] args) {

        AdjGraph graph = new AdjGraph(10);

        graph.getMatrix()[0][1] = 10;
        graph.getMatrix()[2][0] = 4;
        graph.getMatrix()[3][5] = 12;

        MainFrame frame = new MainFrame();
        frame.showAdjazenzmatrix(graph);
        frame.showGraph(graph);
    }

}
