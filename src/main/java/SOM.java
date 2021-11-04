import javafx.application.Platform;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

public class SOM {

    private double[][] potentialMap;
    private Neuron[][] neuronMap;
    private double[][] uMatrix;
    double[][] weight;
    private Controller controller;

    //скорость обучения
    private double learningRate = 0.5;
    //функция, уменьшающая количество соседей с при увеличении итерации (монотонно убывающий),
    private double sigma;
    private double sampleSize;
    private double sigma0 = 0.003;
    private final double minPotential = 0.75;
    private final int epoch;
    private final double K = 0.02;
    private int neightbours;
    private final List<double[]> inputVectors;
    private final boolean isGaussian = false;

    public SOM(int neuronAmount, int epoch, List<double[]> inputVectors, Controller controller) {
        this.inputVectors = inputVectors;
        this.epoch = epoch;
        this.controller = controller;
        sampleSize = neuronAmount;
//        sigma0 = sampleSize / 2;
        initMaps(neuronAmount);
        controller.createMap(neuronAmount);
        neightbours = neuronAmount / 10 > 0 ? neuronAmount / 10 : 1;
    }

    //True
    public void initMaps(int neuronAmount) {
        List<double[]> randVectors = randomSelectionOfVectors((int) Math.pow(neuronAmount, 2), inputVectors);
        // List<double[]> randVectors = generateVectors((int) Math.pow(neuronAmount, 2), inputVectors.get(0).length);
        neuronMap = new Neuron[neuronAmount][neuronAmount];
        potentialMap = new double[neuronAmount][neuronAmount];
        uMatrix = new double[neuronAmount][neuronAmount];
        int count = 0;
        for (int i = 0; i < neuronMap.length; i++) {
            for (int j = 0; j < neuronMap[0].length; j++) {
                neuronMap[i][j] = new Neuron(i, j, randVectors.get(count));
                potentialMap[i][j] = 1 / Math.pow(neuronAmount, 2);
                count++;
            }
        }
    }

    //True
    public List<double[]> generateVectors(int i, int vectorLength) {
        List<double[]> vectors = new ArrayList<>();
        for (int j = 0; j < i; j++) {
            double[] newVector = new double[vectorLength];
            for (int k = 0; k < newVector.length; k++) {
                newVector[k] = Math.random();
            }
            vectors.add(newVector);
        }
        return vectors;
    }

    //True
    public List<double[]> randomSelectionOfVectors(int i, List<double[]> vectors) {
        List<double[]> randVectors = new ArrayList<>();
        // List<Integer> usedIds = new ArrayList<>();
        int j = 0;
        while (j < i) {
            int idVector = (int) (Math.random() * vectors.size() - 1);
            //   if (!usedIds.contains(idVector)) {
            double[] arr = vectors.get(idVector).clone();
            //       usedIds.add(idVector);
            randVectors.add(arr);
            j++;
            //   }
        }
        return randVectors;
    }


    public void train() {
        double A = 0.99;
        double B = 0.5;
        System.out.println("Start train");
        new Thread(() -> {
            for (int iteration = 0; iteration < epoch; iteration++) {
                System.out.println("Iteration " + iteration);
                learningRate = A / (B + iteration); // todo: ты задаешь рейт сверху, а потом перевычисляешь его тут
                sigma = sigma0 * Math.exp(-(iteration / learningRate));
//                sigma = sigma0 / (1 + (iteration / epoch));
                for (int j = 0; j < inputVectors.size(); j++) {
                    double[] vector = inputVectors.get(j).clone();
                    Neuron winner = getWinners(vector);
                    //  System.out.println("winner X: " + winner.getX() + " Y: " + winner.getY());
                    if (winner.getColor() == null) {
                        winner.setColor(new Color(Math.random(), Math.random(), Math.random(), 1));
                    }
                    System.out.println(winner.getX() + "" + winner.getY());
                    clarifyWeights(winner, vector);
                    try {
                        Thread.sleep(1); // Wait for 1 sec before updating the color
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Platform.runLater(
                            () -> {
                                controller.show(neuronMap);
                            });
                }
                if (neightbours != 0 && iteration != 0 && iteration % (epoch / neightbours) == 0)
                    neightbours--; //todo: количество соседей
            }
            System.out.println("Stop train");
        }).start();
        controller.show(neuronMap);
    }

    public void setNeuronColors() {
        for (int i = 0; i < neuronMap.length; i++) {
            for (int j = 0; j < neuronMap[0].length; j++) {
                double[] vector = neuronMap[i][j].getVector();
                // double sum =(Arrays.stream(vector).sum()/vector.length)*1000;
                double first = vector[2] * 10000 - (int) (vector[2] * 10000); //todo: 10000 это точность после запятой
                neuronMap[i][j].setColor(new Color(first, 0, 0, 1));
            }
        }
    }

    //Looks true
    public Neuron getWinners(double[] vector) {
        double sqSum = 0;
        weight = new double[neuronMap.length][neuronMap[0].length];
        for (int i = 0; i < neuronMap.length; i++) {
            for (int j = 0; j < neuronMap[0].length; j++) {
                double[] mapVector = neuronMap[i][j].getVector();
                for (int k = 0; k < mapVector.length; k++) {
                    sqSum += Math.pow(vector[k] - mapVector[k], 2);
                }
                sqSum = Math.sqrt(sqSum);
                weight[i][j] = sqSum;
                sqSum = 0;
            }
        }
        double minWeight = weight[0][0];
        Neuron winner = neuronMap[0][0];
        for (int i = 0; i < weight.length; i++) {
            for (int j = 0; j < weight[0].length; j++) {
                if (weight[i][j] < minWeight && potentialMap[i][j] >= minPotential) {
                    winner = neuronMap[i][j];
                    minWeight = weight[i][j];
                }
            }
        }
        for (int i = 0; i < neuronMap.length; i++) {
            for (int j = 0; j < neuronMap[0].length; j++) {
                if (neuronMap[i][j] != winner) {
                    potentialMap[i][j] += 1 / sampleSize * sampleSize;
                } else {
                    potentialMap[i][j] -= minPotential;
                }
            }
        }
        return winner;
    }

    public void clarifyWeights(Neuron winner, double[] vector) {
        int x = winner.getX();
        int y = winner.getY();
        for (int i = 0; i < neuronMap.length; i++) {
            Neuron[] neurons = neuronMap[i];
            for (int j = 0; j < neuronMap[0].length; j++) {
                double[] mapVector = neurons[j].getVector();
                double h = learningRate * (isGaussian ? gaussianNeighborhood(neurons[j], winner) : rectNeig(neurons[j], winner)); //rectangularNeighborhood(neurons[j]));
                if (h != 0) {
                    neuronMap[i][j].setColor(winner.getColor());
                    for (int k = 0; k < mapVector.length; k++) {
                        mapVector[k] = mapVector[k] + h * (vector[k] - mapVector[k]);
                    }
                }
            }
        }
        // setNeuronColors();
    }

    public double rectNeig(Neuron neuron, Neuron winner) {
        if ((neuron.getX() <= winner.getX() + neightbours && neuron.getX() >= winner.getX() - neightbours) && (neuron.getY() <= winner.getY() + neightbours && neuron.getY() >= winner.getY() - neightbours)) {
            // System.out.println("Neighborhood X: " + neuron.getX() + " Y: " + neuron.getY());
            return 1;
        } else return 0;
    }

    public double rectangularNeighborhood(Neuron neuron) {
        if (weight[neuron.getX()][neuron.getY()] <= K && weight[neuron.getX()][neuron.getY()] >= -K) {
            // System.out.println("Neighborhood X: " + neuron.getX() + " Y: " + neuron.getY());
            return K;
        } else return 0;
    }

    public double gaussianNeighborhood(Neuron neuron, Neuron winner) {
        double sqSum = 0;
        for (int k = 0; k < winner.getVector().length; k++) {
            sqSum += Math.pow(neuron.getVector()[k] - winner.getVector()[k], 2);
        }
        sqSum = Math.sqrt(sqSum);
        double n = Math.exp(-(Math.pow(sqSum, 2) / (2 * Math.pow(sigma, 2))));
        if (weight[neuron.getX()][neuron.getY()] <= n && weight[neuron.getX()][neuron.getY()] >= -n) {
            System.out.println("Neighborhood X: " + neuron.getX() + " Y: " + neuron.getY());
            return n;
        } else return 0;
    }

    public void createUMatrix() {

//        double[][] uMatrix = new double[neuronMap.length*2-1][neuronMap.length*2-1];
//        for (int i = 0; i < uMatrix.length; i++) {
//            for (int j = 0; j < uMatrix[0].length; j++) {
//              uMatrix[i] =
//            }
//        }
    }
}
