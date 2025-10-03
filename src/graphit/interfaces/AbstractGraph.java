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
}