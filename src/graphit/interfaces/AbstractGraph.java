package graphit.interfaces;

public abstract class AbstractGraph {
    private java.util.List<Integer> trackList = new java.util.ArrayList<>();

    public abstract int[][] exportMatrix();

    public void bfs(int start) {
        throw new UnsupportedOperationException("BFS not implemented");
    }

    public void dfs(int start) {
        throw new UnsupportedOperationException("DFS not implemented");
    }

    public void track(int current) {
        trackList.add(current);
    }

    public final java.util.List<Integer> exportTrack() {
        return trackList;
    }

    public void clearTrack() {
        trackList.clear();
    }

    public void setTrack(java.util.List<Integer> tracked) {
        trackList = java.util.List.copyOf(tracked);
    }

    public void setTrack(int[] tracked) {
        if (tracked == null) {
            trackList = new java.util.ArrayList<Integer>();
            return;
        }
        trackList = new java.util.ArrayList<Integer>(tracked.length);
        for (int i = 0; i < tracked.length; i++) {
            trackList.add(Integer.valueOf(tracked[i]));
        }
    }

    public double[][] deepClone() {
        int[][] m = this.exportMatrix();
        double[][] undirected = new double[m.length][m.length];

        for (int i = 0; i < m.length; i++) {
            for (int j = i + 1; j < m.length; j++) {
                if (m[i][j] == 0) {
                    m[i][j] = m[j][i];
                } else {
                    m[j][i] = m[i][j];
                }
            }
        }

        return undirected;
    }
}