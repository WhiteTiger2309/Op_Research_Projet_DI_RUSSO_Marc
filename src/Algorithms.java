import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.PriorityQueue;

public final class Algorithms {
    private static final long INF = Long.MAX_VALUE / 4;

    public static final class MaxFlowResult {
        public final long maxFlow;
        public final boolean[] reachableFromSInResidual;

        public MaxFlowResult(long maxFlow, boolean[] reachableFromSInResidual) {
            this.maxFlow = maxFlow;
            this.reachableFromSInResidual = reachableFromSInResidual;
        }
    }

    public static final class MinCostFlowResult {
        public final long flow;
        public final long cost;
        public final List<Augmentation> augmentations;

        public MinCostFlowResult(long flow, long cost, List<Augmentation> augmentations) {
            this.flow = flow;
            this.cost = cost;
            this.augmentations = augmentations;
        }
    }

    public static final class Augmentation {
        public final int deltaFlow;
        public final long deltaCost;

        public Augmentation(int deltaFlow, long deltaCost) {
            this.deltaFlow = deltaFlow;
            this.deltaCost = deltaCost;
        }

        public long marginalCostPerUnit() {
            if (deltaFlow == 0) return 0;
            return deltaCost / deltaFlow;
        }
    }

    public static MaxFlowResult edmondsKarpMaxFlow(Graph g, int s, int t) {
        long flow = 0;
        int n = g.n;
        int[] parentNode = new int[n];
        int[] parentEdgeIndex = new int[n];

        while (bfsForAugmentingPath(g, s, t, parentNode, parentEdgeIndex)) {
            int bottleneck = Integer.MAX_VALUE;
            int v = t;
            while (v != s) {
                int u = parentNode[v];
                Edge e = g.adj.get(u).get(parentEdgeIndex[v]);
                bottleneck = Math.min(bottleneck, e.capacity);
                v = u;
            }

            v = t;
            while (v != s) {
                int u = parentNode[v];
                Edge e = g.adj.get(u).get(parentEdgeIndex[v]);
                e.capacity -= bottleneck;
                Edge rev = g.adj.get(e.to).get(e.rev);
                rev.capacity += bottleneck;
                v = u;
            }

            flow += bottleneck;
        }

        boolean[] reachable = reachableInResidual(g, s);
        return new MaxFlowResult(flow, reachable);
    }

    private static boolean bfsForAugmentingPath(Graph g, int s, int t, int[] parentNode, int[] parentEdgeIndex) {
        Arrays.fill(parentNode, -1);
        Arrays.fill(parentEdgeIndex, -1);
        ArrayDeque<Integer> q = new ArrayDeque<>();
        q.add(s);
        parentNode[s] = s;

        while (!q.isEmpty()) {
            int u = q.poll();
            if (u == t) {
                return true;
            }
            List<Edge> edges = g.adj.get(u);
            for (int i = 0; i < edges.size(); i++) {
                Edge e = edges.get(i);
                if (e.capacity <= 0) continue;
                int v = e.to;
                if (parentNode[v] != -1) continue;
                parentNode[v] = u;
                parentEdgeIndex[v] = i;
                q.add(v);
            }
        }
        return parentNode[t] != -1;
    }

    public static boolean[] reachableInResidual(Graph g, int s) {
        boolean[] vis = new boolean[g.n];
        ArrayDeque<Integer> q = new ArrayDeque<>();
        vis[s] = true;
        q.add(s);
        while (!q.isEmpty()) {
            int u = q.poll();
            for (Edge e : g.adj.get(u)) {
                if (e.capacity <= 0) continue;
                if (!vis[e.to]) {
                    vis[e.to] = true;
                    q.add(e.to);
                }
            }
        }
        return vis;
    }

    public static List<Graph.OriginalArc> minCutEdges(Graph g, boolean[] reachable) {
        List<Graph.OriginalArc> cut = new ArrayList<>();
        for (Graph.OriginalArc a : g.getOriginalArcs()) {
            if (reachable[a.from] && !reachable[a.to]) {
                cut.add(a);
            }
        }
        return cut;
    }

    /**
     * Min-cost flow via Successive Shortest Path using Bellman-Ford at each augmentation.
     * If targetFlow < 0: push as much as possible (min-cost max-flow).
     */
    public static MinCostFlowResult minCostFlowBellmanFord(Graph g, int s, int t, long targetFlow) {
        long flow = 0;
        long cost = 0;
        List<Augmentation> steps = new ArrayList<>();

        int n = g.n;
        long[] dist = new long[n];
        int[] parentNode = new int[n];
        int[] parentEdgeIndex = new int[n];

        while (targetFlow < 0 || flow < targetFlow) {
            bellmanFordShortestPath(g, s, dist, parentNode, parentEdgeIndex);
            if (dist[t] >= INF / 2) {
                break;
            }

            int bottleneck = Integer.MAX_VALUE;
            int v = t;
            while (v != s) {
                int u = parentNode[v];
                Edge e = g.adj.get(u).get(parentEdgeIndex[v]);
                bottleneck = Math.min(bottleneck, e.capacity);
                v = u;
            }

            long remaining = targetFlow < 0 ? bottleneck : Math.min((long) bottleneck, targetFlow - flow);
            int delta = (int) remaining;

            long deltaCost = 0;
            v = t;
            while (v != s) {
                int u = parentNode[v];
                Edge e = g.adj.get(u).get(parentEdgeIndex[v]);
                e.capacity -= delta;
                Edge rev = g.adj.get(e.to).get(e.rev);
                rev.capacity += delta;
                deltaCost += (long) delta * e.cost;
                v = u;
            }

            flow += delta;
            cost += deltaCost;
            steps.add(new Augmentation(delta, deltaCost));
        }

        return new MinCostFlowResult(flow, cost, steps);
    }

    private static void bellmanFordShortestPath(Graph g, int s, long[] dist, int[] parentNode, int[] parentEdgeIndex) {
        int n = g.n;
        Arrays.fill(dist, INF);
        Arrays.fill(parentNode, -1);
        Arrays.fill(parentEdgeIndex, -1);
        dist[s] = 0;
        parentNode[s] = s;

        for (int it = 0; it < n - 1; it++) {
            boolean updated = false;
            for (int u = 0; u < n; u++) {
                if (dist[u] >= INF / 2) continue;
                List<Edge> edges = g.adj.get(u);
                for (int i = 0; i < edges.size(); i++) {
                    Edge e = edges.get(i);
                    if (e.capacity <= 0) continue;
                    int v = e.to;
                    long nd = dist[u] + e.cost;
                    if (nd < dist[v]) {
                        dist[v] = nd;
                        parentNode[v] = u;
                        parentEdgeIndex[v] = i;
                        updated = true;
                    }
                }
            }
            if (!updated) break;
        }
    }

    /**
     * Min-cost flow via Successive Shortest Path using Dijkstra on reduced costs.
     * If targetFlow < 0: push as much as possible (min-cost max-flow).
     */
    public static MinCostFlowResult minCostFlowDijkstraPotentials(Graph g, int s, int t, long targetFlow) {
        long flow = 0;
        long cost = 0;
        List<Augmentation> steps = new ArrayList<>();

        int n = g.n;
        long[] potential = new long[n];
        long[] dist = new long[n];
        int[] parentNode = new int[n];
        int[] parentEdgeIndex = new int[n];

        // Initialize potentials to ensure non-negative reduced costs.
        initializePotentialsWithBellmanFord(g, s, potential);

        while (targetFlow < 0 || flow < targetFlow) {
            dijkstraReducedCosts(g, s, potential, dist, parentNode, parentEdgeIndex);
            if (dist[t] >= INF / 2) {
                break;
            }

            for (int v = 0; v < n; v++) {
                if (dist[v] < INF / 2) {
                    potential[v] += dist[v];
                }
            }

            int bottleneck = Integer.MAX_VALUE;
            int v = t;
            while (v != s) {
                int u = parentNode[v];
                Edge e = g.adj.get(u).get(parentEdgeIndex[v]);
                bottleneck = Math.min(bottleneck, e.capacity);
                v = u;
            }

            long remaining = targetFlow < 0 ? bottleneck : Math.min((long) bottleneck, targetFlow - flow);
            int delta = (int) remaining;

            long deltaCost = 0;
            v = t;
            while (v != s) {
                int u = parentNode[v];
                Edge e = g.adj.get(u).get(parentEdgeIndex[v]);
                e.capacity -= delta;
                Edge rev = g.adj.get(e.to).get(e.rev);
                rev.capacity += delta;
                deltaCost += (long) delta * e.cost;
                v = u;
            }

            flow += delta;
            cost += deltaCost;
            steps.add(new Augmentation(delta, deltaCost));
        }

        return new MinCostFlowResult(flow, cost, steps);
    }

    private static void initializePotentialsWithBellmanFord(Graph g, int s, long[] potential) {
        int n = g.n;
        long[] dist = new long[n];
        int[] pn = new int[n];
        int[] pe = new int[n];
        bellmanFordShortestPath(g, s, dist, pn, pe);
        for (int i = 0; i < n; i++) {
            potential[i] = dist[i] >= INF / 2 ? 0 : dist[i];
        }
    }

    private static final class NodeDist implements Comparable<NodeDist> {
        final int node;
        final long dist;

        NodeDist(int node, long dist) {
            this.node = node;
            this.dist = dist;
        }

        @Override
        public int compareTo(NodeDist o) {
            return Long.compare(this.dist, o.dist);
        }
    }

    private static void dijkstraReducedCosts(Graph g, int s, long[] potential, long[] dist, int[] parentNode, int[] parentEdgeIndex) {
        int n = g.n;
        Arrays.fill(dist, INF);
        Arrays.fill(parentNode, -1);
        Arrays.fill(parentEdgeIndex, -1);

        PriorityQueue<NodeDist> pq = new PriorityQueue<>();
        dist[s] = 0;
        parentNode[s] = s;
        pq.add(new NodeDist(s, 0));

        while (!pq.isEmpty()) {
            NodeDist cur = pq.poll();
            int u = cur.node;
            if (cur.dist != dist[u]) continue;
            List<Edge> edges = g.adj.get(u);
            for (int i = 0; i < edges.size(); i++) {
                Edge e = edges.get(i);
                if (e.capacity <= 0) continue;
                int v = e.to;
                long reduced = (long) e.cost + potential[u] - potential[v];
                if (reduced < 0) {
                    // With correct potentials this should not happen.
                    // We still allow it but Dijkstra's guarantee breaks; better to be explicit.
                }
                long nd = dist[u] + reduced;
                if (nd < dist[v]) {
                    dist[v] = nd;
                    parentNode[v] = u;
                    parentEdgeIndex[v] = i;
                    pq.add(new NodeDist(v, nd));
                }
            }
        }
    }

    /**
     * Detects if the current residual graph contains a negative-cost cycle.
     * Returns true if a negative cycle exists.
     */
    public static boolean hasNegativeCycleResidual(Graph g) {
        int n = g.n;
        long[] dist = new long[n];
        Arrays.fill(dist, 0);

        boolean updated = false;
        for (int it = 0; it < n; it++) {
            updated = false;
            for (int u = 0; u < n; u++) {
                long du = dist[u];
                for (Edge e : g.adj.get(u)) {
                    if (e.capacity <= 0) continue;
                    int v = e.to;
                    long nd = du + e.cost;
                    if (nd < dist[v]) {
                        dist[v] = nd;
                        updated = true;
                    }
                }
            }
            if (!updated) {
                return false;
            }
        }
        return updated;
    }

    private Algorithms() {
    }
}
