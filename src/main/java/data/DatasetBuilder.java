package data;

import ch.kaiki.nn.data.Graph;
import ch.kaiki.nn.data.IVertice;
import ch.kaiki.nn.data.Vertice;
import main.Main;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public class DatasetBuilder {


    public Dataset buildDataset(DatasetType type) {
        return getDataset(type.name().toLowerCase());
    }

    private Dataset getDataset(String name) {
        File spec = getFile(name + "/spec.txt");
        File distance = getFile(name + "/distance.txt");
        Dataset dataset = parseDataset(name, spec);
        parseEdges(distance, dataset);
        return dataset;
    }


    private Dataset parseDataset(String name, File file) {
        if (file != null) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                boolean isFirstLine = true;
                Graph graph = new Graph();
                long minPath = 0;
                List<Object[]> lineMatrix = new ArrayList<>();
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (isFirstLine) {
                        minPath = Integer.parseInt(line);
                        isFirstLine = false;
                    } else {
                        String[] verticeStr = line.trim().replaceAll(" +", " ").split("\\s");
                        String id = verticeStr[0];
                        double x = verticeStr.length > 1 ? Double.parseDouble(verticeStr[1]) : 0;
                        double y = verticeStr.length > 2 ? Double.parseDouble(verticeStr[2]) : 0;
                        double z = verticeStr.length > 3 ? Double.parseDouble(verticeStr[3]) : 0;
                        lineMatrix.add(new Object[]{id, x, y, z});
                        graph.addVertice(new Vertice(x, y, z, id));
                    }
                }
                Dataset dataset = new Dataset(name, graph, minPath);
                dataset.addRawVertices(lineMatrix.toArray());
                return dataset;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private void parseEdges(File file, Dataset dataset) {
        Graph graph = dataset.getGraph();
        if (file != null) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                List<IVertice> vertices = graph.getVertices();
                int row = 0;
                double[][] matrix = null;
                double maxWeight = 0;
                while ((line = reader.readLine()) != null) {
                    String[] edgeStr = line.trim().replaceAll(" +", " ").split("\\s");
                    for (int col = 0; col < edgeStr.length; col++) {
                        if (row == 0 && col == 0) {
                            matrix = new double[edgeStr.length][edgeStr.length];
                        }
                        String wStr = edgeStr[col];
                        if (wStr.isEmpty()) {
                            continue;
                        }
                        double weight = Double.parseDouble(wStr);
                        if (weight > maxWeight) {
                            maxWeight = weight;
                        }
                        matrix[row][col] = weight;
                        graph.addEdge(vertices.get(row), vertices.get(col), weight);
                    }
                    row++;
                }
                dataset.addRawEdges(matrix);
                dataset.setMaxWeight(maxWeight);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private File getFile(String name) {
        try (InputStream in = Main.class.getClassLoader().getResourceAsStream("datasets/" + name)) {
            File file = File.createTempFile("tspwalker_tmp_upload", ".txt");
            Files.copy(in, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            file.deleteOnExit();
            return file;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }




}
