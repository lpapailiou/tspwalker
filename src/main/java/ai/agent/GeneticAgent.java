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

public class GeneticAgent extends Agent {


    private void runAlgorithm() {
        /*
        loadNeuralNetwork();
        setDisable(true);
        setGraphs();
        GeneticAlgorithmBatch<GeneticAlgorithm> batch = new GeneticAlgorithmBatch<>(GeneticAlgorithm.class, state.getNeuralNetwork(), state.getPopulationSize())
                .setReproductionPoolSize(state.getPoolSize())
                .setReproductionSpecimenCount(state.getParentCount());

        int maxGenerations = state.getGenerationCount();
        AtomicInteger currentGeneration = new AtomicInteger(-1);
        timeline = new Timeline((new KeyFrame(Duration.millis(200), e -> {
            if (geneticAlgorithm[0] == null) {
                currentGeneration.getAndIncrement();
                batch.processGeneration();
                NeuralNetwork best = batch.getBestNeuralNetwork();
                geneticAlgorithm[0] = new GeneticAlgorithm(best);

                if (maxGenerations != currentGeneration.get()) {
                    state.setNeuralNetwork(best, nnPlot);
                    graphPlot.plotGraph(geneticAlgorithm[0].getGraph(), graphColor);
                }
            }

            if (geneticAlgorithm[0] == null || maxGenerations == currentGeneration.get()) {
                stopTimeline();
            } else {
                boolean running = geneticAlgorithm[0].perform();
                graphPlot.plotGraph(geneticAlgorithm[0].getGraph(), graphColor);
                int steps = geneticAlgorithm[0].getSteps();
                int maxSteps = geneticAlgorithm[0].getMaxSteps();
                double distance = geneticAlgorithm[0].getDistance();
                double tDistance = geneticAlgorithm[0].getTargetDistance();
                String path = geneticAlgorithm[0].getPath().toString();
                int gen = currentGeneration.get();
                Platform.runLater(() -> {
                    stepCount.setText(steps + "");
                    maxStepCount.setText(maxSteps + "");
                    distanceCount.setText(distance + "");
                    targetDistance.setText(tDistance + "");
                    generationCount.setText(gen + "");
                    pathTxt.setText(path);
                });

                if (!running) {
                    statsPlot.plotLine(currentGeneration.get(), distance, "distance", accentColor.darker());
                    geneticAlgorithm[0] = null;
                }

            }
        })));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();*/
    }

}
