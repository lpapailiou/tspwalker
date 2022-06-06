package ai;

import ch.kaiki.nn.genetic.IGene;
import ch.kaiki.nn.neuralnet.NeuralNetwork;
import ch.kaiki.nn.util.Optimizer;
import ui.State;

import java.util.*;

public class PathGene implements IGene {

    private final State state = State.getInstance();
    private List<String> path = state.getCurrentDataset().getVerticeList();
    private final Random random = new Random();
    private int iterationCount = 0;

    private final double initialLearningRate = state.getLearningRate();
    private final double initialMutationRate = state.getMutationRate();
    private double learningRate = initialLearningRate;
    private double mutationRate = initialMutationRate;
    private final Optimizer learningRateOptimizer = state.getLearningRateOptimizer();
    private final Optimizer mutationRateOptimizer = state.getMutationRateOptimizer();
    private final double learningRateDecay = state.getLearningRateDecay();
    private final double mutationRateDecay = state.getMutationRateDecay();
    private final int crossoverSliceCount = state.getCrossoverSliceCount();

    private void swap() {
        int randA = random.nextInt(path.size());
        int randB = random.nextInt(path.size());
        String a = path.get(randA);
        String b = path.get(randB);
        path.set(randA, b);
        path.set(randB, a);
    }

    private void shuffle() {
        Collections.shuffle(path);
    }


    @Override
    public List<Double> predict(double[] inputNodes) {
        List<Double> out = new ArrayList<>();
        for (double d : inputNodes) {
            out.add(d);
        }
        return out;
    }

    @Override
    public IGene crossover(List<IGene> genes) {
        List<String> newPath = new ArrayList<>();
        int[] sliceIndices = new int[crossoverSliceCount-1];

        // prepare slice indices
        for (int i = 0; i < crossoverSliceCount-1; i++) {
            sliceIndices[i] = random.nextInt(path.size());
        }
        Arrays.sort(sliceIndices);

        // collect slices
        int startIndex = 0;
        int endIndex;
        for (int i = 0; i <= sliceIndices.length; i++) {
            int rand = random.nextInt(genes.size());
            List<String> p = ((PathGene) genes.get(rand)).getPath();

            for (int j = 0; j <= sliceIndices.length; j++) {
                endIndex = i == sliceIndices.length ? sliceIndices.length : sliceIndices[i];
                for (int k = startIndex; k < endIndex; k++) {
                    newPath.add(p.get(k));
                }
                startIndex = endIndex;
            }
        }
        PathGene crossoverResult = new PathGene();
        crossoverResult.setPath(newPath);
        crossoverResult.iterationCount = this.iterationCount;
        return crossoverResult;
    }

    private void setPath(List<String> path) {
        this.path = path;
    }

    List<String> getPath() {
        return new ArrayList<>(path);
    }


    @Override
    public IGene mutate() {
        PathGene crossoverResult = new PathGene();
        crossoverResult.iterationCount = this.iterationCount;
        if (Math.random() < crossoverResult.mutationRate) {
            if (crossoverResult.learningRate > 0.9) {
                crossoverResult.shuffle();
            } else {
                int swapCount = (int) (crossoverResult.path.size() * crossoverResult.learningRate) / 2;
                for (int i = 0; i < swapCount; i++) {
                    crossoverResult.swap();
                }
            }
        }
        return crossoverResult;
    }

    @Override
    public void decreaseRate() {
        this.learningRate = learningRateOptimizer.decrease(initialLearningRate, learningRateDecay, iterationCount);
        this.mutationRate = mutationRateOptimizer.decrease(initialMutationRate, mutationRateDecay, iterationCount);
        iterationCount++;
    }

    @Override
    public IGene initialize() {
        PathGene crossoverResult = new PathGene();
        crossoverResult.iterationCount = this.iterationCount;
        return crossoverResult;
    }

    @Override
    public String getProperty(String key) {
        return NeuralNetwork.Builder.getProperty(key);
    }
}
