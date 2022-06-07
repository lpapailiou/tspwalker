package ai.agent;

import ai.GeneticNeuralNetwork;
import ch.kaiki.nn.genetic.GeneticBatch;
import ch.kaiki.nn.neuralnet.NeuralNetwork;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import java.util.concurrent.atomic.AtomicInteger;


public class GeneticAgent extends Agent {
    private final GeneticNeuralNetwork[] currentPerformer = {null};
    private boolean stopped = false;

    public GeneticAgent() {
        state.addStopTimeLineListener(e -> {
            stopped = true;
            currentPerformer[0] = null;
            if (timeline != null) {
                timeline.stop();
            }
        });
    }

    @Override
    public void run() {
        GeneticBatch<GeneticNeuralNetwork, NeuralNetwork> batch = new GeneticBatch<>(GeneticNeuralNetwork.class, NeuralNetwork.class, state.getNeuralNetwork(), state.getPopulationSize())
                .setReproductionPoolSize(state.getPoolSize())
                .setParentCount(state.getParentCount());
        int maxGenerations = state.getGenerationCount();
        AtomicInteger currentGeneration = new AtomicInteger(-1);

        timeline = new Timeline((new KeyFrame(Duration.millis(delay), e -> {
            if (!stopped) {


            if (currentPerformer[0] == null) {
                currentGeneration.getAndIncrement();
                batch.processGeneration();
                NeuralNetwork best = (NeuralNetwork) batch.getBestGene();
                currentPerformer[0] = new GeneticNeuralNetwork(best);

                if (maxGenerations != currentGeneration.get()) {
                    state.setNeuralNetwork(best);
                    state.plotGraph();
                }

            }

            if (currentPerformer[0] == null || maxGenerations == currentGeneration.get()) {
                state.stopTimeline();
            } else {
                boolean running = currentPerformer[0].perform();
                state.plotGraph();
                state.refreshAttributes(currentPerformer[0].getSteps(),
                        currentPerformer[0].getMaxSteps(),
                        currentPerformer[0].getDistance(),
                        currentPerformer[0].getTargetDistance(),
                        currentPerformer[0].getPath().toString(),
                        currentGeneration.get(),
                        currentPerformer[0].getFitness(),
                        currentPerformer[0].getCost(),
                        currentPerformer[0].getGraph()
                        );


                if (currentPerformer[0].hasReachedGoal()) {
                    state.stopTimeline();
                }

                if (!running) {
                    state.plotStats();
                    currentPerformer[0] = null;
                }

            }
            }
        })));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();

    }

}
