import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Graph {
    public static final class OriginalArc {
        public final int from;
        public final int to;
        public final int capacity;
        public final int cost;
        private final int edgeIndexInAdjFrom;

        private OriginalArc(int from, int to, int capacity, int cost, int edgeIndexInAdjFrom) {
            this.from = from;
            this.to = to;
            this.capacity = capacity;
            this.cost = cost;
            this.edgeIndexInAdjFrom = edgeIndexInAdjFrom;
        }

        public int getFlow(Graph g) {
            Edge fwd = g.adj.get(from).get(edgeIndexInAdjFrom);
            Edge rev = g.adj.get(to).get(fwd.rev);
            return rev.capacity;
        }

        public boolean isSaturated(Graph g) {
            return getFlow(g) >= capacity;
        }
    }

    public final int n;
    public final List<List<Edge>> adj;
    private final List<OriginalArc> originalArcs;

    public Graph(int n) {
        this.n = n;
        this.adj = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            adj.add(new ArrayList<>());
        }
        this.originalArcs = new ArrayList<>();
    }

    public List<OriginalArc> getOriginalArcs() {
        return Collections.unmodifiableList(originalArcs);
    }

    /**
     * Adds an arc u->v with given capacity and cost. Also adds the reverse residual edge v->u
     * with capacity 0 and cost = -cost.
     */
    public void addEdge(int from, int to, int capacity, int cost) {
        int fromIndex = adj.get(from).size();
        int toIndex = adj.get(to).size();
        Edge fwd = new Edge(to, toIndex, capacity, cost);
        Edge rev = new Edge(from, fromIndex, 0, -cost);
        adj.get(from).add(fwd);
        adj.get(to).add(rev);
        originalArcs.add(new OriginalArc(from, to, capacity, cost, fromIndex));
    }
}
