package ui;

import ch.kaiki.nn.neuralnet.NeuralNetwork;
import ch.kaiki.nn.ui.NNGraph;
import ch.kaiki.nn.util.Initializer;
import ch.kaiki.nn.util.Optimizer;
import ch.kaiki.nn.util.Rectifier;
import data.Dataset;
import data.DatasetBuilder;
import data.DatasetType;
import javafx.animation.Timeline;

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
    private double learningRate = 0.8;
    private int parentCount = 3;
    private double poolSize = 0.1;
    private Optimizer mutationRateOptimizer = Optimizer.NONE;
    private double mutationRateDecay = 0.01;
    private Optimizer learningRateOptimizer = Optimizer.NONE;
    private double learningRateDecay = 0.01;
    private Rectifier rectifier = Rectifier.SIGMOID;
    private Initializer initializer = Initializer.XAVIER;
    private Timeline timeline;


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

    public void setLearningRate(double learningRate) {
        this.learningRate = learningRate;
    }

    public double getLearningRate() {
        return learningRate;
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

    public void setLearningRateOptimizer(Optimizer learningRateOptimizer) {
        this.learningRateOptimizer = learningRateOptimizer;
    }

    public Optimizer getLearningRateOptimizer() {
        return learningRateOptimizer;
    }

    public void setLearningRateDecay(double learningRateDecay) {
        this.learningRateDecay = learningRateDecay;
    }

    public double getLearningRateDecay() {
        return learningRateDecay;
    }

    public void setTimeline(Timeline timeline) {
        this.timeline = timeline;
    }

    public Timeline getTimeline() {
        return timeline;
    }

    public void setRectifier(Rectifier rectifier) {
        this.rectifier = rectifier;
    }

    public Rectifier getRectifier() {
        return rectifier;
    }
    public void setInitializer(Initializer initializer) {
        this.initializer = initializer;
    }

    public Initializer getInitializer() {
        return initializer;
    }
}
