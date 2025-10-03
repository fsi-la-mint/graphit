package graphit.swingui.traversal;

import graphit.interfaces.AbstractGraph;
import graphit.interfaces.TraversalRunner;

public class DefaultDfsRunner implements TraversalRunner {
    @Override
    public String getName() {
        return "DFS";
    }

    @Override
    public void run(AbstractGraph graph, int start) {
        graph.clearTrack();
        graph.dfs(start);
    }
}
