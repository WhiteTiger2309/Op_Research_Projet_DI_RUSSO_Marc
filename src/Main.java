import java.io.IOException;
import java.util.List;

public final class Main {
    private static final String USAGE = String.join(System.lineSeparator(),
            "Usage:",
            "  java -cp out Main <mode> <inputFile> [--one-based]",
            "Modes:",
            "  maxflow         : Ford-Fulkerson (Edmonds-Karp) + min-cut",
            "  mincost-bf      : Min-cost flow (Successive Shortest Path + Bellman-Ford)",
            "  mincost-dij     : Min-cost flow (Successive Shortest Path + Dijkstra + potentials)",
            "  bench           : Compare mincost-bf vs mincost-dij for F=1..min(Fmax,20)",
            "Notes:",
            "  Input format: line1 'n m s t [F]', then m lines 'u v cap cost'",
            "  If F missing and mode is mincost-*, the program pushes until saturation (min-cost max-flow).",
                "  Use --one-based if the file uses vertices in 1..n.",
                "  Output vertex IDs follow the chosen convention (0-based by default, 1-based with --one-based)."
    );

    public static void main(String[] args) {
        try {
            run(args);
        } catch (Exception e) {
            System.err.println("ERROR: " + e.getMessage());
            System.err.println();
            System.err.println(USAGE);
            System.exit(1);
        }
    }

    private static void run(String[] args) throws IOException {
        if (args.length < 2) {
            throw new IllegalArgumentException("Missing arguments");
        }
        String mode = args[0].trim().toLowerCase();
        String input = args[1];
        boolean oneBased = hasFlag(args, "--one-based");

        ProblemInstance instance = Parser.parse(input, oneBased);

        switch (mode) {
            case "maxflow":
                runMaxFlow(instance, oneBased);
                break;
            case "mincost-bf":
                runMinCost(instance, true, oneBased);
                break;
            case "mincost-dij":
                runMinCost(instance, false, oneBased);
                break;
            case "bench":
                runBench(instance);
                break;
            default:
                throw new IllegalArgumentException("Unknown mode: " + mode);
        }
    }

    private static void runMaxFlow(ProblemInstance instance, boolean oneBasedOutput) {
        Graph g = instance.buildGraph();
        Algorithms.MaxFlowResult res = Algorithms.edmondsKarpMaxFlow(g, instance.s, instance.t);

        System.out.println("TOTAL_FLOW = " + res.maxFlow);
        System.out.println("EDGES (original order):");
        int idx = 0;
        for (Graph.OriginalArc a : g.getOriginalArcs()) {
            int f = a.getFlow(g);
            System.out.println("  #" + idx + " " + v(a.from, oneBasedOutput) + " -> " + v(a.to, oneBasedOutput) + " : flow/cap = " + f + "/" + a.capacity);
            idx++;
        }

        System.out.println("MIN_CUT_S (reachable in residual from s):");
        StringBuilder sb = new StringBuilder();
        sb.append("  S = {");
        boolean first = true;
        for (int v = 0; v < res.reachableFromSInResidual.length; v++) {
            if (res.reachableFromSInResidual[v]) {
                if (!first) sb.append(", ");
                sb.append(v(v, oneBasedOutput));
                first = false;
            }
        }
        sb.append("}");
        System.out.println(sb);

        List<Graph.OriginalArc> cut = Algorithms.minCutEdges(g, res.reachableFromSInResidual);
        System.out.println("MIN_CUT_EDGES (u in S, v not in S):");
        for (Graph.OriginalArc a : cut) {
            System.out.println("  " + v(a.from, oneBasedOutput) + " -> " + v(a.to, oneBasedOutput) + " cap=" + a.capacity + " flow=" + a.getFlow(g));
        }
    }

    private static void runMinCost(ProblemInstance instance, boolean bellmanFord, boolean oneBasedOutput) {
        Graph g = instance.buildGraph();
        long target = instance.demandF == null ? -1 : instance.demandF.longValue();

        Algorithms.MinCostFlowResult res = bellmanFord
                ? Algorithms.minCostFlowBellmanFord(g, instance.s, instance.t, target)
                : Algorithms.minCostFlowDijkstraPotentials(g, instance.s, instance.t, target);

        System.out.println("TOTAL_FLOW = " + res.flow + (instance.demandF == null ? " (saturation)" : " (target F=" + instance.demandF + ")"));
        System.out.println("TOTAL_COST = " + res.cost);

        System.out.println("AUGMENTATIONS:");
        long cumulativeFlow = 0;
        long cumulativeCost = 0;
        for (int i = 0; i < res.augmentations.size(); i++) {
            Algorithms.Augmentation a = res.augmentations.get(i);
            cumulativeFlow += a.deltaFlow;
            cumulativeCost += a.deltaCost;
            System.out.println("  #" + i + " deltaFlow=" + a.deltaFlow + " deltaCost=" + a.deltaCost
                    + " marginal=" + a.marginalCostPerUnit() + " cumFlow=" + cumulativeFlow + " cumCost=" + cumulativeCost);
        }

        boolean neg = Algorithms.hasNegativeCycleResidual(g);
        System.out.println("NEGATIVE_CYCLE_IN_RESIDUAL = " + neg);

        System.out.println("EDGES (original order):");
        int idx = 0;
        for (Graph.OriginalArc arc : g.getOriginalArcs()) {
            int f = arc.getFlow(g);
            System.out.println("  #" + idx + " " + v(arc.from, oneBasedOutput) + " -> " + v(arc.to, oneBasedOutput) + " : flow/cap = " + f + "/" + arc.capacity + " cost=" + arc.cost);
            idx++;
        }
    }

    private static void runBench(ProblemInstance instance) {
        // Compute a crude upper bound for Fmax by running maxflow on a fresh graph.
        Graph g0 = instance.buildGraph();
        long fmax = Algorithms.edmondsKarpMaxFlow(g0, instance.s, instance.t).maxFlow;
        long limit = Math.min(fmax, 20);

        System.out.println("BENCH F in [1.." + limit + "] (Fmax=" + fmax + ")");
        for (int F = 1; F <= limit; F++) {
            Graph gA = instance.buildGraph();
            Graph gB = instance.buildGraph();
            Algorithms.MinCostFlowResult a = Algorithms.minCostFlowBellmanFord(gA, instance.s, instance.t, F);
            Algorithms.MinCostFlowResult b = Algorithms.minCostFlowDijkstraPotentials(gB, instance.s, instance.t, F);

            boolean ok = a.flow == b.flow && a.cost == b.cost;
            System.out.println("  F=" + F + " bf(flow=" + a.flow + ", cost=" + a.cost + ")"
                    + " dij(flow=" + b.flow + ", cost=" + b.cost + ")"
                    + " OK=" + ok);
            if (!ok) {
                System.out.println("  MISMATCH detected; stopping early.");
                break;
            }
        }
    }

    private static boolean hasFlag(String[] args, String flag) {
        for (String a : args) {
            if (a.equalsIgnoreCase(flag)) return true;
        }
        return false;
    }

    private static int v(int internalVertexId, boolean oneBasedOutput) {
        return oneBasedOutput ? internalVertexId + 1 : internalVertexId;
    }
}
