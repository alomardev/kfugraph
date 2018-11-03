package com.alomardev.kfugraph;

public class Graph {

    private int v;
    private double[][] adj;
    private String[] labels;

    public Graph(int vmax) {
        v = 0;
        adj = new double[vmax][vmax];
        labels = new String[vmax];
    }

    public Graph addVertex(String label) {
        labels[v++] = label;
        return this;
    }

    public Graph addEdge(int from, int to, double cost) {
        adj[from][to] = cost;
        return this;
    }

    public double[] getAdjacency(int i) {
        return adj[i];
    }
    
    public double getCost(int from, int to) {
        return adj[from][to];
    }

    public int size() {
        return v;
    }

    public String getLabel(int i) {
        return labels[i];
    }

    public int indexOf(String label) {
        for (int i = 0; i < v; i++) {
            if (labels[i].equals(label)) {
                return i;
            }
        }
        return -1;
    }

}
