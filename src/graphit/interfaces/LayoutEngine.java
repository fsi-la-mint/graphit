package graphit.interfaces;

import java.awt.Dimension;
import java.awt.Point;
import java.util.List;

/**
 * Interface for graph layout engines that compute node positions.
 * Implementations should return one Point per node (index-aligned to the
 * graph's adjacency matrix rows/cols).
 */
public interface LayoutEngine {
    /**
     * Compute positions for all nodes in the given graph.
     *
     * @param graph The graph model.
     * @param area  The available drawing area; engines should keep nodes inside it.
     * @return A list of points of size N where N == graph.exportMatrix().length.
     */
    List<Point> layout(AbstractGraph graph, Dimension area);

    /**
     * Human-friendly name of the engine.
     */
    default String getName() {
        return getClass().getSimpleName();
    }
}
