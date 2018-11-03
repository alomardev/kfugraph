package com.alomardev.kfugraph;

public class UndirectedGraph extends Graph {
    
    public UndirectedGraph(int vmax) {
        super(vmax);
    }

    @Override
    public Graph addEdge(int from, int to, double cost) {
        super.addEdge(to, from, cost);
        super.addEdge(from, to, cost);
        return this;
    }
    
}
