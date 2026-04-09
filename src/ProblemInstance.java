import java.util.ArrayList;
import java.util.List;

public final class ProblemInstance {
    public static final class ArcInput {
        public final int from;
        public final int to;
        public final int capacity;
        public final int cost;

        public ArcInput(int from, int to, int capacity, int cost) {
            this.from = from;
            this.to = to;
            this.capacity = capacity;
            this.cost = cost;
        }
    }

    public final int n;
    public final int m;
    public final int s;
    public final int t;
    public final Integer demandF; // optional
    public final List<ArcInput> arcs;

    public ProblemInstance(int n, int m, int s, int t, Integer demandF, List<ArcInput> arcs) {
        this.n = n;
        this.m = m;
        this.s = s;
        this.t = t;
        this.demandF = demandF;
        this.arcs = arcs;
    }

    public Graph buildGraph() {
        Graph g = new Graph(n);
        for (ArcInput a : arcs) {
            g.addEdge(a.from, a.to, a.capacity, a.cost);
        }
        return g;
    }

    public static Builder builder(int n, int m, int s, int t, Integer demandF) {
        return new Builder(n, m, s, t, demandF);
    }

    public static final class Builder {
        private final int n;
        private final int m;
        private final int s;
        private final int t;
        private final Integer demandF;
        private final List<ArcInput> arcs = new ArrayList<>();

        private Builder(int n, int m, int s, int t, Integer demandF) {
            this.n = n;
            this.m = m;
            this.s = s;
            this.t = t;
            this.demandF = demandF;
        }

        public void addArc(int from, int to, int capacity, int cost) {
            arcs.add(new ArcInput(from, to, capacity, cost));
        }

        public ProblemInstance build() {
            if (arcs.size() != m) {
                throw new IllegalStateException("Expected m=" + m + " arcs but got " + arcs.size());
            }
            return new ProblemInstance(n, m, s, t, demandF, arcs);
        }
    }
}
