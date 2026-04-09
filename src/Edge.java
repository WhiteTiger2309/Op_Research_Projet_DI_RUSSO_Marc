public final class Edge {
    public final int to;
    public final int rev;
    public int capacity; // residual capacity
    public final int cost;

    public Edge(int to, int rev, int capacity, int cost) {
        this.to = to;
        this.rev = rev;
        this.capacity = capacity;
        this.cost = cost;
    }
}
