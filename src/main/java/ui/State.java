package ui;

import ch.kaiki.nn.neuralnet.NeuralNetwork;
import ch.kaiki.nn.ui.NNGraph;
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
    private int generationCount = 3;
    private int populationSize = 500;
    private double randomizationRate = 0.6;
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

    public void setRandomizationRate(double randomizationRate) {
        this.randomizationRate = randomizationRate;
    }

    public double getRandomizationRate() {
        return randomizationRate;
    }

    public void setConfiguration(int[] configuration) {
        this.configuration = configuration;
    }

    public int[] getConfiguration() {
        return configuration;
    }
}
