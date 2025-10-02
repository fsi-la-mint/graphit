public class AdjGraph extends AbstractGraph {
    private int[][] matrix;

    public AdjGraph(int maxKnotenZahl) {
        matrix = new int[maxKnotenZahl][maxKnotenZahl];
    }

    public void matrixAusgeben() {
        for (int i = 0; i < matrix.length; i++) {
            System.out.println("| \t");
            for (int j = 0; j < matrix.length; j++) {
                System.out.print(matrix[i][j] + "\t | \t");
            }
            System.out.println();
        }
    }

    @Override
    public void bfs(int start) {
        // Implement BFS algorithm
        boolean[] visited = new boolean[matrix.length];
        java.util.Queue<Integer> queue = new java.util.LinkedList<>();
        queue.add(start);
        visited[start] = true;
        track(start);
        while (!queue.isEmpty()) {
            int node = queue.poll();
            for (int i = 0; i < matrix.length; i++) {
                if (matrix[node][i] != 0 && !visited[i]) {
                    queue.add(i);
                    visited[i] = true;
                    track(i);
                }
            }
        }
    }

    public int[][] getMatrix() {
        return matrix;
    }

    @Override
    public int[][] exportMatrix() {
        return matrix;
    }
}
