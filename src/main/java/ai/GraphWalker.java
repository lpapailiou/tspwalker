package ai;

import ch.kaiki.nn.data.*;
import ch.kaiki.nn.genetic.GeneticAlgorithmObject;
import ch.kaiki.nn.neuralnet.NeuralNetwork;
import data.Dataset;
import ui.State;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GraphWalker extends GeneticAlgorithmObject {

    private Dataset dataset = State.getInstance().getCurrentDataset();
    private Graph graph = dataset.getGraph();
    private List<IVertice> vertices = graph.getVertices();
    private int totalVertices = vertices.size();
    private int currentVerticeIndex = 0;
    private int verticesVisited = 0;
    private double maxWeight = dataset.getMaxWeight();
    private List<String> path = new ArrayList<>();
    private double cost = 1;
    private double minPathLength = dataset.getMinPathLength();
    private double distance;
    private int steps;
    private int maxSteps;

    public GraphWalker(NeuralNetwork neuralNetwork) {
        super(neuralNetwork);
        vertices.get(0).setHighlighted(true);
        int n = neuralNetwork.getConfiguration()[0] / 3;
        maxSteps = (n*(n-1))/2;
        path.add(vertices.get(0).getName());
    }

    @Override
    public boolean perform() {
        double[] vision = new double[totalVertices*3];
        vision[currentVerticeIndex] = 1;
        for (int i = 0; i < totalVertices; i++) {
            vision[i+totalVertices] = vertices.get(i).isVisited() ? 1 : 0;
            double weight = vertices.get(currentVerticeIndex).getEdges().get(i).getWeight();
            vision[i+totalVertices*2] = weight <= 0 ? 0 : weight / maxWeight;
        }
        List<Double> prediction = predict(vision);
        int maxIndex = prediction.indexOf(Collections.max(prediction));

        IVertice currentVertex = vertices.get(currentVerticeIndex);
        IVertice nextVertex = vertices.get(maxIndex);

        vertices.get(currentVerticeIndex).setHighlighted(false);
        if (!currentVertex.isVisited()) {
            currentVertex.setVisited(true);
            verticesVisited++;
        }
        path.add(nextVertex.getName());

        IEdge edge = currentVertex.getEdges().stream().filter(e -> e.getTo().equals(nextVertex)).findFirst().orElse(null);
        edge.setVisited(true);

        double weight = edge.getWeight();
        distance += weight;
        cost += weight == 0 ? 10 : edge.getWeight();
        steps++;
        currentVerticeIndex = maxIndex;
        vertices.get(currentVerticeIndex).setHighlighted(true);
        return !hasReachedGoal() && steps <= maxSteps;
    }

    @Override
    public double getFitness() {
        return 1 / cost + verticesVisited * 10;
    }

    @Override
    public boolean isImmature() {
        return verticesVisited < totalVertices / 2;
    }

    @Override
    public boolean hasReachedGoal() {
        return verticesVisited == totalVertices && currentVerticeIndex == 0 && distance == minPathLength;
    }

    public Graph getGraph() {
        return graph;
    }

    public List<String> getPath() {
        return path;
    }

    public double getDistance() {
        return distance;
    }

    public int getSteps() {
        return steps;
    }

    public int getMaxSteps() {
        return maxSteps;
    }

    public double getTargetDistance() {
        return minPathLength;
    }
}
