package ai;

import ch.kaiki.nn.data.Graph;
import ch.kaiki.nn.data.IEdge;
import ch.kaiki.nn.data.IVertice;
import ch.kaiki.nn.genetic.GeneticObject;

import data.Dataset;
import ui.State;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class GeneticPath extends GeneticObject {
    private static final DecimalFormat df = new DecimalFormat("0.0");
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


    public GeneticPath(PathGene gene) {
        super(gene);
        vertices.get(0).setHighlighted(true);
        maxSteps = gene.getPath().size();
        path.add(vertices.get(0).getName());
    }

    @Override
    public boolean perform() {
        double[] vision = new double[vertices.size()];
        int nextIndex = steps+1;
        if (nextIndex >= vertices.size()) {
            nextIndex = 0;
        }

        vision[nextIndex] = 1;
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
        return !hasReachedGoal() && steps < maxSteps;
    }

    @Override
    public double getFitness() {
        //return (verticesVisited * maxSteps / cost + verticesVisited * maxSteps) * verticesVisited / steps;
        //return Math.pow(verticesVisited, 2) / (cost * (steps - verticesVisited));
        double fitness = 0;
        double verticeValue = Math.pow(verticesVisited, 3);
        if (isImmature()) {
            fitness = verticeValue / cost + verticeValue;
        }
        fitness = ((verticeValue / cost + verticeValue / (cost - distance)) * 2 - distance) + verticeValue;
        //return (verticeValue / cost + verticeValue) * ((double) verticesVisited / steps);

        if (fitness < 0) {
            return 0;
        }

        fitness = vertices.size() * maxWeight - distance;
        return fitness;
    }

    @Override
    public boolean isImmature() {
        return false; //verticesVisited < Math.min(5, verticesVisited / 3);
    }

    @Override
    public boolean hasReachedGoal() {
        return verticesVisited == totalVertices && currentVerticeIndex == 0 && Objects.equals(df.format(distance), df.format(minPathLength));
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
