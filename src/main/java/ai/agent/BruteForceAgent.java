package ai.agent;

import ch.kaiki.nn.data.Graph;
import ch.kaiki.nn.data.IEdge;
import ch.kaiki.nn.data.IVertice;
import data.Dataset;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import java.util.*;
import java.util.stream.Collectors;

public class BruteForceAgent extends Agent {

    private Dataset dataset = state.getCurrentDataset();
    private Graph graph = dataset.getGraph();

    public BruteForceAgent() {
        state.addStopTimeLineListener(e -> {
            if (timeline != null) {
                timeline.stop();
            }
        });
    }

    @Override
    public void run() {
        Map<String, Double> permutations = new HashMap<>();
        collectPermutations(permutations);
        double max = permutations.keySet().stream().map(permutations::get).min(Double::compareTo).orElse(0.0);
        for (String key : permutations.keySet()) {
            double cost = permutations.get(key);
            if (cost == max) {
                String[] path = key.split(",");
                for (int i = 1; i < path.length; i++) {
                    String fNode = path[i-1].replaceAll(" ", "").replaceAll("\\[", "").replaceAll("]","");
                    String next = path[i].replaceAll(" ", "").replaceAll("\\[", "").replaceAll("]","");
                    IVertice v = graph.getVertices().stream().filter(vt -> vt.getName().equals(fNode)).findAny().orElse(null);
                    v.setVisited(true);
                    IEdge e = v.getEdges().stream().filter(ed -> ed.getTo().getName().equals(next)).findAny().orElse(null);
                    e.setVisited(true);
                }
                String firstNode = path[0].replaceAll(" ", "").replaceAll("\\[", "").replaceAll("]","");
                String lastNode = path[path.length-1].replaceAll(" ", "").replaceAll("\\[", "").replaceAll("]","");
                IVertice f = graph.getVertices().stream().filter(vt -> vt.getName().equals(firstNode)).findAny().orElse(null);
                IVertice l = graph.getVertices().stream().filter(vt -> vt.getName().equals(lastNode)).findAny().orElse(null);
                IEdge e = l.getEdges().stream().filter(ed -> ed.getTo().getName().equals(f)).findAny().orElse(null);
                if (e != null) {
                    e.setVisited(true);
                }
                f.setHighlighted(true);


                state.refreshAttributes(permutations.keySet().size(),
                        permutations.keySet().size(),
                        cost,
                        cost,
                        key,
                        0,
                        0,
                        cost,
                        graph
                );
                state.plotGraph();
                break;
            }
            state.stopTimeline();
        }

        timeline = new Timeline((new KeyFrame(Duration.millis(200), e -> {
        })));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();

    }

    private void collectPermutations(Map<String, Double> result) {

        List<String> nodes = graph.getVertices().stream().map(n -> n.getName()).collect(Collectors.toList());
        String[] nodeNames = new String[nodes.size()-1];
        String first = nodes.get(0);
        for (int i = 0; i < nodes.size()-1; i++) {
            nodeNames[i] = nodes.get(i+1);
        }
        collectPermutations(graph.getVertices().size()-1, first, nodeNames, result);
    }

    private void collectPermutations(int n, String first, String[] elements, Map<String, Double> result) {
        if(n == 1) {
            String[] el = new String[elements.length+2];
            for (int i = 0; i < elements.length; i++) {
                el[i+1] = elements[i];
            }
            el[0] = first;
            el[elements.length+1] = first;
            double cost = getCost(el);
            result.put(Arrays.toString(el), cost);
            System.out.println(Arrays.toString(el) + " " + cost);
        } else {
            for(int i = 0; i < n-1; i++) {
                collectPermutations(n - 1, first, elements, result);
                if(n % 2 == 0) {
                    swap(elements, i, n-1);
                } else {
                    swap(elements, 0, n-1);
                }
            }
            collectPermutations(n - 1, first, elements, result);
        }
    }

    private double getCost(String[] elements) {
        double cost = 0;
        String currentNode = elements[0];
        for (int i = 1; i < elements.length; i++) {
            String next = elements[i];
            String fNode = currentNode;
            IVertice v = graph.getVertices().stream().filter(vt -> vt.getName().equals(fNode)).findAny().orElse(null);
            IEdge e = v.getEdges().stream().filter(ed -> ed.getTo().getName().equals(next)).findAny().orElse(null);
            cost += e.getWeight();
            currentNode = next;
        }
        return cost;
    }

    private void swap(String[] input, int a, int b) {
        String tmp = input[a];
        input[a] = input[b];
        input[b] = tmp;
    }

}
