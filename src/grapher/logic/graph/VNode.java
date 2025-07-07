package grapher.logic.graph;

public class VNode {
    public double x, y;
    public int radius = 20;
    public double vx = 0.0, vy = 0.0; // for force based visu algos
    public String name;

    public VNode(String name) {
        this(name, 0, 0);
    }

    public VNode(String name, double x, double y) {
        this.name = name;
        this.x = x;
        this.y = y;
    }

    public boolean contains(double mx, double my) {
        double dx = x - mx;
        double dy = y - my;
        return dx * dx + dy * dy <= radius * radius;
    }

}
