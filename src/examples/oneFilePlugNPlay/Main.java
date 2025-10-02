public class Main {
    public static void main(String[] args) {

        AdjGraph graph = new AdjGraph(4);

        graph.getMatrix()[0][1] = 10;
        graph.getMatrix()[2][0] = 4;
        graph.getMatrix()[3][1] = 12;
        graph.getMatrix()[3][0] = 1;

        Graphit graphit = new Graphit();
        graphit.showAdjazenzmatrix(graph);
        graphit.showGraph(graph);
    }

}
