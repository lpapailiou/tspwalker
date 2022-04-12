package ai.agent;

import ai.GeneticAlgorithm;
import ch.kaiki.nn.genetic.GeneticAlgorithmBatch;
import ch.kaiki.nn.neuralnet.NeuralNetwork;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.util.Duration;

import java.util.concurrent.atomic.AtomicInteger;

import static javafx.scene.paint.Color.LIMEGREEN;
import static javafx.scene.paint.Color.ORANGE;

public class GeneticAgent extends Agent {
    private final GeneticAlgorithm[] currentPerformer = {null};
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
        GeneticAlgorithmBatch<GeneticAlgorithm> batch = new GeneticAlgorithmBatch<>(GeneticAlgorithm.class, state.getNeuralNetwork(), state.getPopulationSize())
                .setReproductionPoolSize(state.getPoolSize())
                .setReproductionSpecimenCount(state.getParentCount());
        int maxGenerations = state.getGenerationCount();
        AtomicInteger currentGeneration = new AtomicInteger(-1);

        timeline = new Timeline((new KeyFrame(Duration.millis(delay), e -> {
            if (!stopped) {


            if (currentPerformer[0] == null) {
                currentGeneration.getAndIncrement();
                batch.processGeneration();
                NeuralNetwork best = batch.getBestNeuralNetwork();
                currentPerformer[0] = new GeneticAlgorithm(best);

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
