package graphit.interfaces;

/**
 * Strategy interface to execute a traversal on a graph that fills the track list.
 */
public interface TraversalRunner {
    /**
     * Display name for UI selection.
     */
    String getName();

    /**
     * Run the traversal with the given start node.
     * Implementations should call graph.clearTrack() if they need a clean slate
     * before populating the track.
     */
    void run(AbstractGraph graph, int start) throws Exception;
}
