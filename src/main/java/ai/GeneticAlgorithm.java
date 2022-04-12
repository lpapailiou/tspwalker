package ai;

import ch.kaiki.nn.data.*;
import ch.kaiki.nn.genetic.GeneticAlgorithmObject;
import ch.kaiki.nn.neuralnet.NeuralNetwork;
import data.Dataset;
import ui.State;

import java.util.*;

public class GeneticAlgorithm extends GeneticAlgorithmObject {

    private final Dataset dataset = State.getInstance().getCurrentDataset();
    private final Graph graph = dataset.getGraph();
    private final List<IVertice> vertices = graph.getVertices();
    private final int totalVertices = vertices.size();
    private int currentVerticeIndex = 0;
    private int verticesVisited = 0;
    private final double maxWeight = dataset.getMaxWeight();
    private final List<String> path = new ArrayList<>();
    private double cost = 1;
    private final double minPathLength = dataset.getMinPathLength();
    private double distance;
    private int steps;
    private final int maxSteps;

    public GeneticAlgorithm(NeuralNetwork neuralNetwork) {
        super(neuralNetwork);
        vertices.get(0).setHighlighted(true);
        int n = neuralNetwork.getConfiguration()[0] / 3;
        maxSteps = n * 5; // (n*(n-1))/2;
        path.add(vertices.get(0).getName());
    }

    @Override
    public boolean perform() {
        double[] vision = new double[totalVertices*3];
        vision[currentVerticeIndex] = 1;
        for (int i = 0; i < totalVertices; i++) {
            vision[i+totalVertices] = vertices.get(i).isVisited() ? 1 : 0;
            double weight = vertices.get(currentVerticeIndex).getEdges().get(i).getWeight();
            //vision[i+totalVertices*2] = weight <= 0 ? 1 : maxWeight - weight / maxWeight;
            vision[i+totalVertices*2] = weight <= 0 ? 1 : weight / maxWeight;
        }
        List<Double> prediction = predict(vision);
        int maxIndex = prediction.indexOf(Collections.max(prediction));

        IVertice currentVertex = vertices.get(currentVerticeIndex);
        IVertice nextVertex = vertices.get(maxIndex);

        vertices.get(currentVerticeIndex).setHighlighted(false);
        boolean penalty = false;
        if (!currentVertex.isVisited()) {
            currentVertex.setVisited(true);
            verticesVisited++;
        } else if (currentVerticeIndex != 0) {
            penalty = true;
        }

        path.add(nextVertex.getName());

        IEdge edge = currentVertex.getEdges().stream().filter(e -> e.getTo().equals(nextVertex)).findFirst().orElse(null);
        edge.setVisited(true);

        double weight = edge.getWeight();
        distance += weight;
        cost += weight == 0 ? minPathLength : weight;
        steps++;
        currentVerticeIndex = maxIndex;
        vertices.get(currentVerticeIndex).setHighlighted(true);
       /* if (path.size() > verticesVisited * 5) {
            cost += minPathLength * maxSteps * (maxSteps-steps);
        }*/
        return !(verticesVisited == totalVertices && currentVerticeIndex == 0) && steps < maxSteps && path.size() <= verticesVisited * 3;
    }

    @Override
    public double getFitness() {
        //return (verticesVisited * maxSteps / cost + verticesVisited * maxSteps) * verticesVisited / steps;
        //return Math.pow(verticesVisited, 2) / (cost * (steps - verticesVisited));
        double verticeValue = Math.pow(verticesVisited, 3);
        return ((verticeValue / cost + verticeValue / (cost - distance)) * 2 - distance) / cost;
        //return (verticeValue / cost + verticeValue) * ((double) verticesVisited / steps);
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
    public double getCost() {
        return cost;
    }

    public double getTargetDistance() {
        return minPathLength;
    }
}
