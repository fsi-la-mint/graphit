package graphit.swingui.traversal;

import graphit.interfaces.AbstractGraph;
import graphit.interfaces.TraversalRunner;

public class DefaultBfsRunner implements TraversalRunner {
    @Override
    public String getName() { return "BFS"; }

    @Override
    public void run(AbstractGraph graph, int start) {
        graph.clearTrack();
        graph.bfs(start);
    }
}
