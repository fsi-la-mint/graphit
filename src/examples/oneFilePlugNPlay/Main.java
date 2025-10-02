public class Main {
    public static void main(String[] args) {

        AdjGraph graph = new AdjGraph(10);
        int[][] matrix = graph.getMatrix();

        // connections (undirected / symmetric)
        matrix[0][1] = 7;  matrix[1][0] = 7;
        matrix[0][2] = 3;  matrix[2][0] = 3;
        matrix[0][4] = 2;  matrix[4][0] = 2;

        matrix[1][2] = 1;  matrix[2][1] = 1;
        matrix[1][3] = 5;  matrix[3][1] = 5;
        matrix[1][9] = 6;  matrix[9][1] = 6;

        matrix[2][5] = 8;  matrix[5][2] = 8;
        matrix[2][9] = 10; matrix[9][2] = 10;

        matrix[3][4] = 4;  matrix[4][3] = 4;
        matrix[3][6] = 6;  matrix[6][3] = 6;

        matrix[4][5] = 2;  matrix[5][4] = 2;
        matrix[4][9] = 5;  matrix[9][4] = 5;

        matrix[5][7] = 3;  matrix[7][5] = 3;

        matrix[6][7] = 1;  matrix[7][6] = 1;
        matrix[6][8] = 9;  matrix[8][6] = 9;

        matrix[7][8] = 2;  matrix[8][7] = 2;

        matrix[8][9] = 7;  matrix[9][8] = 7;

        Graphit graphit = new Graphit();
        graphit.showAdjazenzmatrix(graph);
        graphit.showGraph(graph);
        graphit.showTraversal(graph);
    }

}
