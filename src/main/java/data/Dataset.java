package data;

import ch.kaiki.nn.data.Graph;
import ch.kaiki.nn.data.IVertice;
import ch.kaiki.nn.data.Vertice;

import java.util.List;

public class Dataset {

    private String name;
    private Graph graph;
    private Object[] rawVertices;
    private double[][] rawEdges;
    private double minPathLength;
    private double maxWeight;

    public Dataset(String name, Graph graph, double minPathLength) {
        this.name = name;
        this.graph = graph;
        this.minPathLength = minPathLength;
    }

    private Dataset() {}

    public String getName() {
        return name;
    }

    public Graph getGraph() {
        return graph;
    }

    public double getMinPathLength() {
        return minPathLength;
    }

    public void addRawVertices(Object[] data) {
        rawVertices = data;
    }

    public void addRawEdges(double[][] data) {
        rawEdges = data;
    }

    public Dataset copy() {
        Dataset copy = new Dataset();
        copy.name = this.name;
        copy.rawVertices = this.rawVertices;
        copy.rawEdges = this.rawEdges;
        copy.minPathLength = this.minPathLength;
        copy.maxWeight = this.maxWeight;
        copy.graph = new Graph();

        for (Object obj : rawVertices) {
            Object[] o = (Object[]) obj;
            copy.graph.addVertice(new Vertice(Double.parseDouble(o[1].toString()), Double.parseDouble(o[2].toString()), Double.parseDouble(o[3].toString()), o[0].toString()));
        }

        List<IVertice> vertices = copy.graph.getVertices();

        for (int row = 0; row < rawEdges.length; row++) {
            for (int col = 0; col < rawEdges.length; col++) {
                copy.graph.addEdge(vertices.get(row), vertices.get(col), rawEdges[row][col]);
            }
        }
        return copy;
    }

    public void setMaxWeight(double weight) {
        this.maxWeight = weight;
    }

    public double getMaxWeight() {
        return maxWeight;
    }

}
