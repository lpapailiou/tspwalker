package ai.agent;

import ai.GeneticPath;
import ai.PathGene;
import ch.kaiki.nn.genetic.GeneticBatch;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import java.util.concurrent.atomic.AtomicInteger;


public class PathAgent extends Agent {
    private final GeneticPath[] currentPerformer = {null};
    private boolean stopped = false;

    public PathAgent() {
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
        GeneticBatch<GeneticPath, PathGene> batch = new GeneticBatch<>(GeneticPath.class, PathGene.class, new PathGene(), state.getPopulationSize())
                .setReproductionPoolSize(state.getPoolSize())
                .setReproductionSpecimenCount(state.getParentCount());
        int maxGenerations = state.getGenerationCount();
        AtomicInteger currentGeneration = new AtomicInteger(-1);

        timeline = new Timeline((new KeyFrame(Duration.millis(delay), e -> {
            if (!stopped) {


            if (currentPerformer[0] == null) {
                currentGeneration.getAndIncrement();
                batch.processGeneration();
                System.out.println("generation processed");
                PathGene best = (PathGene) batch.getBestGene();
                currentPerformer[0] = new GeneticPath(best);

                if (maxGenerations != currentGeneration.get()) {
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
