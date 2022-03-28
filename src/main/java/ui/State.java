package ui;

import ch.kaiki.nn.neuralnet.NeuralNetwork;
import ch.kaiki.nn.ui.NNGraph;
import ch.kaiki.nn.util.Optimizer;
import data.Dataset;
import data.DatasetBuilder;
import data.DatasetType;

import java.util.ArrayList;
import java.util.List;

public class State {

    private static State instance;

    private List<Dataset> datasetCache = new ArrayList<>();
    private Dataset currentDataset;
    private NeuralNetwork neuralNetwork;
    private int generationCount = 100;
    private int populationSize = 500;
    private double mutationRate = 0.6;
    private int parentCount = 3;
    private double poolSize = 0.1;
    private Optimizer mutationRateOptimizer = Optimizer.NONE;
    private double mutationRateDecay = 0.01;


    private int[] configuration = {0,0,0};


    public static synchronized State getInstance() {
        if (instance == null) {
            instance = new State();
        }
        return instance;
    }

    public Dataset getDataset(DatasetType type) {
        Dataset dataset = datasetCache.stream().filter(d -> d.getName().toUpperCase().equals(type.name())).findFirst().orElse(null);
        if (dataset == null) {
            dataset = new DatasetBuilder().buildDataset(type);
            datasetCache.add(dataset);
        }
        currentDataset = dataset;
        return currentDataset.copy();
    }

    public Dataset getCurrentDataset() {
        return currentDataset.copy();
    }

    public String getDatasetName() {
        if (currentDataset == null) {
            return "";
        }
        return currentDataset.getName();
    }

    public void setNeuralNetwork(NeuralNetwork neuralNetwork, NNGraph nnGraph) {
        this.neuralNetwork = neuralNetwork;
        nnGraph.setNeuralNetwork(neuralNetwork);
    }

    public NeuralNetwork getNeuralNetwork() {
        return neuralNetwork;
    }

    public void setGenerationCount(int count) {
        this.generationCount = count;
    }

    public int getGenerationCount() {
        return generationCount;
    }

    public void setPopulationSize(int size) {
        this.populationSize = size;
    }

    public int getPopulationSize() {
        return populationSize;
    }

    public void setMutationRate(double mutationRate) {
        this.mutationRate = mutationRate;
    }

    public double getMutationRate() {
        return mutationRate;
    }

    public void setConfiguration(int[] configuration) {
        this.configuration = configuration;
    }

    public int[] getConfiguration() {
        return configuration;
    }

    public void setParentCount(int parentCount) {
        this.parentCount = parentCount;
    }

    public int getParentCount() {
        return parentCount;
    }

    public void setPoolSize(double poolSize) {
        this.poolSize = poolSize;
    }

    public double getPoolSize() {
        return poolSize;
    }

    public void setMutationRateOptimizer(Optimizer mutationRateOptimizer) {
        this.mutationRateOptimizer = mutationRateOptimizer;
    }

    public Optimizer getMutationRateOptimizer() {
        return mutationRateOptimizer;
    }

    public void setMutationRateDecay(double mutationRateDecay) {
        this.mutationRateDecay = mutationRateDecay;
    }

    public double getMutationRateDecay() {
        return mutationRateDecay;
    }
}
