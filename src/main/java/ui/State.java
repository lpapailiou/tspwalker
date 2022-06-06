package ui;

import ch.kaiki.nn.data.Graph;
import ch.kaiki.nn.genetic.CrossoverStrategy;
import ch.kaiki.nn.neuralnet.NeuralNetwork;
import ch.kaiki.nn.ui.NN2DPlot;
import ch.kaiki.nn.ui.NNGraph;
import ch.kaiki.nn.ui.color.GraphColor;
import ch.kaiki.nn.ui.color.NNChartColor;
import ch.kaiki.nn.ui.color.NNGraphColor;
import ch.kaiki.nn.util.Initializer;
import ch.kaiki.nn.util.Optimizer;
import ch.kaiki.nn.util.Rectifier;
import data.Dataset;
import data.DatasetBuilder;
import data.DatasetType;
import javafx.animation.Timeline;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import static javafx.scene.paint.Color.*;

public class State {
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private static State instance;
    private int delay = 50;
    private List<Dataset> datasetCache = new ArrayList<>();
    private Dataset currentDataset;
    private NeuralNetwork neuralNetwork;
    private int generationCount = 1000;
    private int populationSize = 1000;
    private double mutationRate = 1;
    private double learningRate = 1;
    private int parentCount = 3;
    private double poolSize = 0.75;
    private Optimizer mutationRateOptimizer = Optimizer.NONE;
    private double mutationRateDecay = 0.01;
    private Optimizer learningRateOptimizer = Optimizer.NONE;
    private CrossoverStrategy crossoverStrategy = CrossoverStrategy.SLICE;
    private int crossoverSliceCount = 6;
    private double learningRateDecay = 0.01;
    private Rectifier rectifier = Rectifier.SIGMOID;
    private Initializer initializer = Initializer.XAVIER;
    private NNGraph nnPlot;
    private Graph currentGraph;
    private Color accentColor = Color.web("#c71585");
    private final GraphColor graphColor = new GraphColor(PINK, TRANSPARENT, accentColor, accentColor, LIGHTSALMON, accentColor);


    private int steps;
    private int maxSteps;
    private double distance;
    private double tDistance;
    private String path;
    private int gen;
    private double fitness;
    private double cost;
    private NN2DPlot graphPlot;
    private NN2DPlot statsPlot;
    private GraphicsContext graphChartContext;
    private GraphicsContext statsChartContext;
    private NNChartColor chartColor;

    private int[] configuration = {0,0,0};


    public static synchronized State getInstance() {
        if (instance == null) {
            instance = new State();
        }
        return instance;
    }

    public void addAttributeRefreshlistener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener("attributes", l);
    }

    public void addStopTimeLineListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener("timeline", l);
    }

    public void stopTimeline() {
        pcs.firePropertyChange("timeline", false, true);
    }

    public void inititializeNNPlot(GraphicsContext context, NNGraphColor colorPalette) {
        nnPlot = new NNGraph(context);
        nnPlot.setColorPalette(colorPalette);
        nnPlot.setNodeRadius(8);
        nnPlot.setLineWidth(0.5);
        nnPlot.setDynamicGrowth(false, true);
        nnPlot.setNodeColorThreshold(-0.3, 0.3);
    }

    public void initializeCharts(GraphicsContext graphChartContext, GraphicsContext statsChartContext, NNChartColor chartColor) {
        this.graphChartContext = graphChartContext;
        this.statsChartContext = statsChartContext;
        this.chartColor = chartColor;
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

    public void refreshAttributes(int steps, int maxSteps, double distance, double tDistance, String path, int gen, double fitness, double cost, Graph currentGraph) {
        this.steps = steps;
        this.maxSteps = maxSteps;
        this.distance = distance;
        this.tDistance = tDistance;
        this.path = path;
        this.gen = gen;
        this.fitness = fitness;
        this.cost = cost;
        this.currentGraph = currentGraph;
        pcs.firePropertyChange("attributes", false, true);
    }

    public int getSteps() { return steps; }
    public int getMaxSteps() { return maxSteps; }
    public double getDistance() { return distance; }
    public double gettDistance() { return tDistance; }
    public String getPath() { return path; }
    public int getGen() { return gen; }
    public double getFitness() { return fitness; }
    public double getCost() { return cost; }
    public Graph getCurrentGraph() { return currentGraph; }
    public Dataset getCurrentDataset() {
        return currentDataset.copy();
    }

    public void setCurrentGraph(Graph graph) {
        this.currentGraph = graph;
    }

    public void plotStats() {
        //statsPlot.plotLine(gen, cost, "cost", ORANGE.darker());
        statsPlot.plotLine(gen, fitness, "fitness", LIMEGREEN.darker());
        statsPlot.plotLine(gen, distance, "distance", accentColor.darker());
    }

    public void plotGraph() {
        graphPlot.plotGraph(currentGraph, graphColor);
    }


    public void setGraphs() {
        graphPlot = new NN2DPlot(graphChartContext);
        graphPlot.setChartColors(chartColor);
        graphPlot.showTickMarkLabels(false);
        graphPlot.showTickMarks(false);
        graphPlot.showGridContent(false);
        graphPlot.setTitle("graph for dataset " + getDatasetName());
        graphPlot.triggerInvalidate();

        statsPlot = new NN2DPlot(statsChartContext);
        statsPlot.setChartColors(chartColor);
        statsPlot.setTitle("statistics");
        statsPlot.showLegend(true);
        statsPlot.triggerInvalidate();

    }

    public String getDatasetName() {
        if (currentDataset == null) {
            return "";
        }
        return currentDataset.getName();
    }

    public void loadNeuralNetwork() {
        NeuralNetwork neuralNetwork = new NeuralNetwork.Builder(getConfiguration())
                .setMutationRate(getMutationRate())
                .setMutationRateOptimizer(getMutationRateOptimizer())
                .setMutationRateMomentum(getMutationRateDecay())
                .setLearningRate(getLearningRate())
                .setLearningRateOptimizer(getLearningRateOptimizer())
                .setLearningRateMomentum(getLearningRateDecay())
                .setCrossoverStrategy(getCrossoverStrategy())
                .setCrossoverSliceCount(getCrossoverSliceCount())
                .setInitializer(getInitializer())
                .setDefaultRectifier(getRectifier())
                .build();
        setNeuralNetwork(neuralNetwork);

    }

    public void setNeuralNetwork(NeuralNetwork neuralNetwork) {
        this.neuralNetwork = neuralNetwork;
        nnPlot.setNeuralNetwork(neuralNetwork);
    }

    public NeuralNetwork getNeuralNetwork() {
        return neuralNetwork;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    public int getDelay() {
        return delay;
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

    public void setCrossoverStrategy(CrossoverStrategy crossoverStrategy) {
        this.crossoverStrategy = crossoverStrategy;
    }

    public CrossoverStrategy getCrossoverStrategy() {
        return crossoverStrategy;
    }

    public void setCrossoverSliceCount(int crossoverSliceCount) {
        this.crossoverSliceCount = crossoverSliceCount;
    }

    public int getCrossoverSliceCount() {
        return crossoverSliceCount;
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
