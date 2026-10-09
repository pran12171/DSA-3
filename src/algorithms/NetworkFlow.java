package algorithms;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Queue;

public class NetworkFlow {
    public static class Edge {
        public final int from;
        public final int to;
        public final int capacity;
        public final int cost;
        private int flow;
        private Edge reverse;

        private Edge(int from, int to, int capacity, int cost) {
            this.from = from;
            this.to = to;
            this.capacity = Math.max(0, capacity);
            this.cost = cost;
        }

        public int residualCapacity() { return capacity - flow; }
    }

    public static class FlowNetwork {
        private final List<List<Edge>> adjacency;
        private final List<int[]> originalEdges = new ArrayList<>();

        public FlowNetwork(int vertices) {
            if (vertices < 0) throw new IllegalArgumentException("Vertex count cannot be negative");
            adjacency = new ArrayList<>(vertices);
            for (int i = 0; i < vertices; i++) adjacency.add(new ArrayList<>());
        }

        public int vertexCount() { return adjacency.size(); }

        public void addEdge(int from, int to, int capacity) { addEdge(from, to, capacity, 0); }

        public void addEdge(int from, int to, int capacity, int cost) {
            checkVertex(from);
            checkVertex(to);
            Edge forward = new Edge(from, to, capacity, cost);
            Edge reverse = new Edge(to, from, 0, -cost);
            forward.reverse = reverse;
            reverse.reverse = forward;
            adjacency.get(from).add(forward);
            adjacency.get(to).add(reverse);
            originalEdges.add(new int[] {from, to, Math.max(0, capacity), cost});
        }

        private void checkVertex(int vertex) {
            if (vertex < 0 || vertex >= adjacency.size()) throw new IllegalArgumentException("Invalid vertex: " + vertex);
        }
    }

    public static class FlowResult {
        private final int maxFlow;
        private final List<Integer> sourceSide;

        private FlowResult(int maxFlow, List<Integer> sourceSide) {
            this.maxFlow = maxFlow;
            this.sourceSide = sourceSide;
        }

        public int getMaxFlow() { return maxFlow; }
        public List<Integer> getSourceSideOfMinCut() { return Collections.unmodifiableList(sourceSide); }
    }

    public static int fordFulkerson(FlowNetwork network, int source, int sink) {
        validateEndpoints(network, source, sink);
        FlowNetwork working = copy(network);
        int total = 0;
        while (true) {
            int pushed = sendAnyPath(working, source, sink, Integer.MAX_VALUE, new boolean[working.vertexCount()]);
            if (pushed == 0) return total;
            total += pushed;
        }
    }

    public static int edmondsKarp(FlowNetwork network, int source, int sink) {
        return runMaxFlow(network, source, sink, true).getMaxFlow();
    }

    public static int dinic(FlowNetwork network, int source, int sink) {
        validateEndpoints(network, source, sink);
        FlowNetwork working = copy(network);
        int total = 0;
        int[] level = new int[working.vertexCount()];
        while (buildLevels(working, source, sink, level)) {
            int[] next = new int[working.vertexCount()];
            int pushed;
            while ((pushed = sendFlow(working, source, sink, Integer.MAX_VALUE, next, level)) > 0) total += pushed;
        }
        return total;
    }

    public static FlowResult minCut(FlowNetwork network, int source, int sink) {
        validateEndpoints(network, source, sink);
        FlowNetwork working = copy(network);
        int maxFlow = runMaxFlowInPlace(working, source, sink, true).getMaxFlow();
        return new FlowResult(maxFlow, vertices(reachable(working, source)));
    }

    public static int bipartiteMatching(int leftSize, int rightSize, int[][] pairs) {
        FlowNetwork network = new FlowNetwork(leftSize + rightSize + 2);
        int source = leftSize + rightSize;
        int sink = source + 1;
        for (int left = 0; left < leftSize; left++) network.addEdge(source, left, 1);
        for (int right = 0; right < rightSize; right++) network.addEdge(leftSize + right, sink, 1);
        if (pairs != null) {
            for (int[] pair : pairs) {
                if (pair != null && pair.length >= 2 && pair[0] >= 0 && pair[0] < leftSize && pair[1] >= 0 && pair[1] < rightSize) {
                    network.addEdge(pair[0], leftSize + pair[1], 1);
                }
            }
        }
        return dinic(network, source, sink);
    }

    public static int[] minCostMaxFlow(FlowNetwork network, int source, int sink) {
        validateEndpoints(network, source, sink);
        FlowNetwork working = copy(network);
        int flow = 0;
        int cost = 0;
        while (true) {
            int[] distance = new int[working.vertexCount()];
            Edge[] previous = new Edge[working.vertexCount()];
            Arrays.fill(distance, Integer.MAX_VALUE);
            distance[source] = 0;
            for (int pass = 0; pass < working.vertexCount() - 1; pass++) {
                boolean changed = false;
                for (int vertex = 0; vertex < working.vertexCount(); vertex++) {
                    if (distance[vertex] == Integer.MAX_VALUE) continue;
                    for (int index = 0; index < working.adjacency.get(vertex).size(); index++) {
                        Edge edge = working.adjacency.get(vertex).get(index);
                        if (edge.residualCapacity() > 0 && distance[edge.to] > distance[vertex] + edge.cost) {
                            distance[edge.to] = distance[vertex] + edge.cost;
                            previous[edge.to] = edge;
                            changed = true;
                        }
                    }
                }
                if (!changed) break;
            }
            if (previous[sink] == null) break;
            int amount = Integer.MAX_VALUE;
            for (int vertex = sink; vertex != source;) {
                Edge edge = previous[vertex];
                amount = Math.min(amount, edge.residualCapacity());
                vertex = edge.from;
            }
            for (int vertex = sink; vertex != source;) {
                Edge edge = previous[vertex];
                edge.flow += amount;
                edge.reverse.flow -= amount;
                vertex = edge.from;
            }
            flow += amount;
            cost += amount * distance[sink];
        }
        return new int[] {flow, cost};
    }

    private static FlowResult runMaxFlow(FlowNetwork network, int source, int sink, boolean breadthFirst) {
        validateEndpoints(network, source, sink);
        FlowNetwork working = copy(network);
        return runMaxFlowInPlace(working, source, sink, breadthFirst);
    }

    private static FlowResult runMaxFlowInPlace(FlowNetwork working, int source, int sink, boolean breadthFirst) {
        int flow = 0;
        while (true) {
            Edge[] previous = new Edge[working.vertexCount()];
            boolean[] visited = new boolean[working.vertexCount()];
            Queue<Integer> queue = new ArrayDeque<>();
            queue.add(source);
            visited[source] = true;
            while (!queue.isEmpty() && !visited[sink]) {
                int vertex = queue.remove();
                for (Edge edge : working.adjacency.get(vertex)) {
                    if (edge.residualCapacity() > 0 && !visited[edge.to]) {
                        visited[edge.to] = true;
                        previous[edge.to] = edge;
                        queue.add(edge.to);
                    }
                }
                if (!breadthFirst) break;
            }
            if (!visited[sink]) return new FlowResult(flow, vertices(visited));
            int amount = Integer.MAX_VALUE;
            for (Edge edge = previous[sink]; edge != null; edge = previous[edge.from]) amount = Math.min(amount, edge.residualCapacity());
            for (Edge edge = previous[sink]; edge != null; edge = previous[edge.from]) {
                edge.flow += amount;
                edge.reverse.flow -= amount;
            }
            flow += amount;
        }
    }

    private static int sendAnyPath(FlowNetwork network, int vertex, int sink, int available, boolean[] visited) {
        if (vertex == sink) return available;
        visited[vertex] = true;
        for (Edge edge : network.adjacency.get(vertex)) {
            if (edge.residualCapacity() > 0 && !visited[edge.to]) {
                int pushed = sendAnyPath(network, edge.to, sink, Math.min(available, edge.residualCapacity()), visited);
                if (pushed > 0) {
                    edge.flow += pushed;
                    edge.reverse.flow -= pushed;
                    return pushed;
                }
            }
        }
        return 0;
    }

    private static boolean buildLevels(FlowNetwork network, int source, int sink, int[] level) {
        Arrays.fill(level, -1);
        Queue<Integer> queue = new ArrayDeque<>();
        queue.add(source);
        level[source] = 0;
        while (!queue.isEmpty()) {
            int vertex = queue.remove();
            for (Edge edge : network.adjacency.get(vertex)) {
                if (edge.residualCapacity() > 0 && level[edge.to] < 0) {
                    level[edge.to] = level[vertex] + 1;
                    queue.add(edge.to);
                }
            }
        }
        return level[sink] >= 0;
    }

    private static int sendFlow(FlowNetwork network, int vertex, int sink, int available, int[] next, int[] level) {
        if (vertex == sink) return available;
        List<Edge> edges = network.adjacency.get(vertex);
        while (next[vertex] < edges.size()) {
            Edge edge = edges.get(next[vertex]);
            if (edge.residualCapacity() > 0 && level[edge.to] == level[vertex] + 1) {
                int pushed = sendFlow(network, edge.to, sink, Math.min(available, edge.residualCapacity()), next, level);
                if (pushed > 0) {
                    edge.flow += pushed;
                    edge.reverse.flow -= pushed;
                    return pushed;
                }
            }
            next[vertex]++;
        }
        return 0;
    }

    private static boolean[] reachable(FlowNetwork network, int source) {
        boolean[] visited = new boolean[network.vertexCount()];
        Queue<Integer> queue = new ArrayDeque<>();
        queue.add(source);
        visited[source] = true;
        while (!queue.isEmpty()) {
            int vertex = queue.remove();
            for (Edge edge : network.adjacency.get(vertex)) {
                if (edge.residualCapacity() > 0 && !visited[edge.to]) {
                    visited[edge.to] = true;
                    queue.add(edge.to);
                }
            }
        }
        return visited;
    }

    private static List<Integer> vertices(boolean[] visited) {
        List<Integer> result = new ArrayList<>();
        for (int i = 0; i < visited.length; i++) if (visited[i]) result.add(i);
        return result;
    }

    private static FlowNetwork copy(FlowNetwork original) {
        FlowNetwork copy = new FlowNetwork(original.vertexCount());
        for (int[] edge : original.originalEdges) copy.addEdge(edge[0], edge[1], edge[2], edge[3]);
        return copy;
    }

    private static void validateEndpoints(FlowNetwork network, int source, int sink) {
        if (network == null || source < 0 || sink < 0 || source >= network.vertexCount() || sink >= network.vertexCount() || source == sink) {
            throw new IllegalArgumentException("Invalid flow endpoints");
        }
    }
}