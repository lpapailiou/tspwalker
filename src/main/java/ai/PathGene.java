package ai;

import ch.kaiki.nn.genetic.IGene;
import ch.kaiki.nn.neuralnet.NeuralNetwork;
import ch.kaiki.nn.util.Optimizer;
import ui.State;

import java.nio.file.Path;
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

    public PathGene() {
        shuffle();
    }

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
        repair();
    }

    private void repair() {
        int firstNode = 0;
        for (int i = 0; i < path.size(); i++) {
            if (path.get(i).trim().equals("1")) {
                firstNode = i;
                break;
            }
        }
        String a = path.get(0);
        String b = path.get(firstNode);
        path.set(0, b);
        path.set(firstNode, a);
    }


    @Override
    public List<Double> predict(double[] inputNodes) {
        int index = 0;
        for (int i = 0; i < path.size(); i++) {
            if ((int) Math.ceil(inputNodes[i]) == 1) {
                index = i;
                break;
            }
        }
        List<Double> out = new ArrayList<>();
        for (double d : inputNodes) {
            out.add(0.0);
        }
        int targetIndex = Integer.parseInt(path.get(index))-1;
        out.set(targetIndex, 1.0);
        return out;
    }

    @Override
    public IGene crossover(List<IGene> genes) {
        PathGene crossoverResult = (PathGene) this.copy();

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
        int rand = random.nextInt(genes.size());
        PathGene randomParent = ((PathGene) genes.get(rand));
        crossoverResult.setPath(randomParent.getPath());
        /*
        PathGene pg = ((PathGene) genes.get(0));
        List<String> p = pg.getPath();
        List<List<String>> slices = new ArrayList<>();
        for (int i = 0; i <= sliceIndices.length; i++) {    // for every slice
            List<String> slice = new ArrayList<>();
            endIndex = i == sliceIndices.length ? path.size() : sliceIndices[i];
            for (int k = startIndex; k < endIndex; k++) {   // for every slice, collect items
                slice.add(p.get(k));
            }
            slices.add(slice);
            startIndex = endIndex;
        }
        //Collections.shuffle(slices);
        for (List<String> s : slices) {
            newPath.addAll(s);
        }
        crossoverResult.setPath(newPath);

         */
        return crossoverResult;
    }

    private void setPath(List<String> path) {
        this.path = path;
        repair();
    }

    List<String> getPath() {
        return new ArrayList<>(path);
    }


    @Override
    public IGene mutate() {
        PathGene crossoverResult = (PathGene) this.copy();
        if (Math.random() < crossoverResult.mutationRate) {
            int swapCount = (int) (crossoverResult.path.size() * crossoverResult.learningRate) / 2;
            for (int i = 0; i < swapCount; i++) {
                crossoverResult.swap();
            }
            crossoverResult.repair();
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
        return this.copy();
    }

    private IGene copy() {
        PathGene copy = new PathGene();
        copy.iterationCount = this.iterationCount;
        copy.setPath(new ArrayList<>(this.path));
        return copy;
    }

    @Override
    public String getProperty(String key) {
        return NeuralNetwork.Builder.getProperty(key);
    }

    @Override
    public String toString() {
        return path.toString();
    }
}
