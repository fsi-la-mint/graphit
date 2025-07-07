package grapher.logic.graph.visualizer;

import org.w3c.dom.Node;

import grapher.interfaces.adj.AbstractGraph;
import grapher.logic.graph.VEdge;
import grapher.logic.graph.VGraph;
import grapher.logic.graph.VNode;

public class ForceSimulator {
    private final AbstractGraph original_graph;
    private final VGraph graph;
    private final double repulsion = 5000;
    private final double attraction = 0.01;
    private final double damping = 0.85;

    public ForceSimulator(AbstractGraph graph) {
        this.original_graph = graph;
        this.graph = new VGraph();

        int[][] matrix = graph.getMatrix();

        for (int i = 0; i < matrix.length; i++) {
            this.graph.nodes.add(new VNode(Integer.toString(i)));
        }

        for (int i = 0; i < matrix.length; i++) {
            VNode node = this.graph.nodes.get(i);
            for (int j = 0; j < matrix.length; j++) {
                VNode other = this.graph.nodes.get(j);
                this.graph.addEdge(node, other);
            }
        }
    }

    public void step() {

        // repulsion force (get away from other node)
        for (VNode node : graph.nodes) {
            node.vx = 0;
            node.vy = 0;

            for (VNode other : graph.nodes) {
                if (node == other) {
                    continue;
                }

                double dx = node.x - other.x;
                double dy = node.y - other.y;
                double dist = Math.max(1, Math.sqrt(dx * dx + dy * dy));
                double force = repulsion / (dist * dist);
                node.vx += (dx / dist) * force;
                node.vy += (dy / dist) * force;
            }
        }

        // attraction force (get away from other node)
        for (VEdge e : graph.getAllEdges()) {
            double dx = e.to.x - e.from.x;
            double dy = e.to.y - e.from.y;
            double dist = Math.max(1, Math.sqrt(dx * dx + dy * dy));

            double force = dist * attraction;
            double fx = (dx / dist) * force;
            double fy = (dy / dist) * force;

            // move towards target
            e.from.vx += fx;
            e.from.vy += fy;

            // move towards target
            e.to.vx -= fx;
            e.to.vy -= fy;
        }

        for (VNode n : graph.nodes) {
            n.vx *= damping;
            n.vy *= damping;
            n.x += n.vx;
            n.y += n.vy;
        }
    }

}