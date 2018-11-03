package com.alomardev.kfugraph;

import java.util.Comparator;
import java.util.PriorityQueue;

public class GraphUtils {

    public static final int[] performGBFS(Graph graph, int f, int t, double[] ht, Analysis analysis) {
        if (analysis != null) {
            analysis.expandedNodes = 0;
        }
        int p = 0;
        int[] path = new int[graph.size()];

        PriorityQueue<SimpleNode> queue = new PriorityQueue<>(new Comparator<SimpleNode>() {
            @Override
            public int compare(SimpleNode o1, SimpleNode o2) {
                if (o1.value < o2.value) {
                    return -1;
                } else if (o1.value > o2.value) {
                    return 1;
                } else {
                    return 0;
                }
            }
        });

        queue.add(new SimpleNode(f, ht[f], null));
        while (!queue.isEmpty()) {
            SimpleNode n = queue.poll();
            if (n.vindex == t) {
                if (analysis != null) {
                    analysis.cost = 0;
                }
                do {
                    int to = n.vindex;
                    int from = (n.parent != null) ? n.parent.vindex : -1;

                    if (analysis != null && from != -1) {
                        analysis.cost += graph.getCost(from, to);
                    }

                    path[p++] = to;

                    n = n.parent;
                    if (n == null) {
                        break;
                    }
                } while (true);
                break;
            }
            double adj[] = graph.getAdjacency(n.vindex);

            if (analysis != null) {
                analysis.expandedNodes++;
            }
            outer:
            for (int i = 0; i < adj.length; i++) {
                if (adj[i] == 0) {
                    continue; // Skip if not adjacent
                }

                // Skip if the adjacent is in the path
                SimpleNode parent = n.parent;
                while (parent != null) {
                    if (parent.vindex == i) {
                        continue outer;
                    }
                    parent = parent.parent;
                }

                queue.add(new SimpleNode(i, ht[i], n));
            }
        }

        int[] finalPath = new int[p];
        for (int i = 0; i < p; i++) {
            finalPath[p - 1 - i] = path[i];
        }
        return finalPath;
    }

    public static final int[] performUCS(Graph graph, int f, int t, Analysis analysis) {
        if (analysis != null) {
            analysis.expandedNodes = 0;
        }

        int p = 0;
        int[] path = new int[graph.size()];

        PriorityQueue<SimpleNode> queue = new PriorityQueue<>(new Comparator<SimpleNode>() {
            @Override
            public int compare(SimpleNode o1, SimpleNode o2) {
                if (o1.value < o2.value) {
                    return -1;
                } else if (o1.value > o2.value) {
                    return 1;
                } else {
                    return 0;
                }
            }
        });

        queue.add(new SimpleNode(f, 0, null));
        while (!queue.isEmpty()) {
            SimpleNode n = queue.poll();
            if (n.vindex == t) {
                if (analysis != null) {
                    analysis.cost = 0;
                }
                do {
                    int to = n.vindex;
                    int from = (n.parent != null) ? n.parent.vindex : -1;

                    if (analysis != null && from != -1) {
                        analysis.cost += graph.getCost(from, to);
                    }

                    path[p++] = to;

                    n = n.parent;
                    if (n == null) {
                        break;
                    }
                } while (true);
                break;
            }
            double adj[] = graph.getAdjacency(n.vindex);

            if (analysis != null) {
                analysis.expandedNodes++;
            }
            outer:
            for (int i = 0; i < adj.length; i++) {
                if (adj[i] == 0) {
                    continue; // Skip if not adjacent
                }

                // Skip if the adjacent is in the path
                SimpleNode parent = n.parent;
                while (parent != null) {
                    if (parent.vindex == i) {
                        continue outer;
                    }
                    parent = parent.parent;
                }

                queue.add(new SimpleNode(i, adj[i] + n.value, n));
            }
        }

        int[] finalPath = new int[p];
        for (int i = 0; i < p; i++) {
            finalPath[p - 1 - i] = path[i];
        }
        return finalPath;
    }

    public static final int[] performAStar(Graph graph, int f, int t, double[] ht, Analysis analysis) {
        if (analysis != null) {
            analysis.expandedNodes = 0;
        }
        int p = 0;
        int[] path = new int[graph.size()];

        PriorityQueue<SimpleNode> queue = new PriorityQueue<>(new Comparator<SimpleNode>() {
            @Override
            public int compare(SimpleNode o1, SimpleNode o2) {
                if (o1.value + ht[o1.vindex] < o2.value + ht[o2.vindex]) {
                    return -1;
                } else if (o1.value + ht[o1.vindex] > o2.value + ht[o2.vindex]) {
                    return 1;
                } else {
                    return 0;
                }
            }
        });

        queue.add(new SimpleNode(f, ht[f], null));
        while (!queue.isEmpty()) {
            SimpleNode n = queue.poll();
            if (n.vindex == t) {
                if (analysis != null) {
                    analysis.cost = 0;
                }
                do {
                    int to = n.vindex;
                    int from = (n.parent != null) ? n.parent.vindex : -1;

                    if (analysis != null && from != -1) {
                        analysis.cost += graph.getCost(from, to);
                    }

                    path[p++] = to;

                    n = n.parent;
                    if (n == null) {
                        break;
                    }
                } while (true);
                break;
            }
            double adj[] = graph.getAdjacency(n.vindex);

            if (analysis != null) {
                analysis.expandedNodes++;
            }
            outer:
            for (int i = 0; i < adj.length; i++) {
                if (adj[i] == 0) {
                    continue; // Skip if not adjacent
                }

                // Skip if the adjacent is in the path
                SimpleNode parent = n.parent;
                while (parent != null) {
                    if (parent.vindex == i) {
                        continue outer;
                    }
                    parent = parent.parent;
                }

                queue.add(new SimpleNode(i, adj[i] + n.value, n));
            }
        }

        int[] finalPath = new int[p];
        for (int i = 0; i < p; i++) {
            finalPath[p - 1 - i] = path[i];
        }
        return finalPath;
    }

    private static class SimpleNode {

        private SimpleNode parent;
        private int vindex;
        private double value;

        private SimpleNode(int vindex, double value, SimpleNode parent) {
            this.vindex = vindex;
            this.value = value;
            this.parent = parent;
        }
    }

    public static class Analysis {

        public String header;
        public int edges;
        public int vertices;
        public int expandedNodes;
        public double timeInSecond;
        public double cost;
        public String[] path;

        public Analysis() {
            path = null;
            header = "";
            vertices = 0;
            edges = 0;
            cost = 0;
            expandedNodes = 0;
        }

        @Override
        public String toString() {
            String output = ">> " + header + "\n";
            output += "    Number of vertices: " + vertices + "\n";
            output += "    Number of edges: " + edges + "\n";
            output += "    Expanded nodes: " + expandedNodes;
            if (path != null && path.length > 0) {
                output += "\n    Cost: " + (Math.round(cost * 100) / 100d);
                output += "\n    Path: ";
                boolean first = true;
                for (String p : path) {
                    if (!first) {
                        output += " -> ";
                    }
                    output += p;
                    first = false;
                }
            } else {
                output += "\n    No path found!";
            }
            output += "\n";

            return output;
        }
    }
}
