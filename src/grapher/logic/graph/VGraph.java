package grapher.logic.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class VGraph {
    public final ArrayList<VNode> nodes = new ArrayList<VNode>();
    public final HashMap<VNode, ArrayList<VNode>> edges = new HashMap<VNode, ArrayList<VNode>>();

    public boolean addEdge(VNode node, VNode target) {
        if (!this.edges.containsKey(node)) {
            this.edges.put(node, new ArrayList<VNode>());
        }
        ArrayList<VNode> e = this.edges.get(node);
        if (e.contains(target)) {
            return false;
        }
        e.add(target);
        return true;
    }

    public List<VEdge> getAllEdges() {
        ArrayList<VEdge> ret = new ArrayList<VEdge>();
        for (VNode key : edges.keySet()) {
            for (VNode other : edges.get(key)) {
                ret.add(new VEdge(key, other));
            }
        }
        return ret;
    }

}
