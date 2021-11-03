
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
    private final List<double[]> inputVectors;
    private final boolean isGaussian = false;
    private final List<Color> colorsList = new ArrayList<>();
    private int colorIndex = 0;

    public SOM(int neuronAmount, int epoch, List<double[]> inputVectors, Controller controller) {
        this.inputVectors = inputVectors;
        this.epoch = epoch;
        this.controller = controller;
        sampleSize = neuronAmount;
//        sigma0 = sampleSize / 2;
        initMaps(neuronAmount);
        initColorList();
        controller.createMap(neuronAmount);
    }

    public void initMaps(int neuronAmount) {
        List<double[]> randVectors = randomSelectionOfVectors((int) Math.pow(neuronAmount, 2), inputVectors);
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

    public List<double[]> randomSelectionOfVectors(int i, List<double[]> vectors) {
        List<double[]> randVectors = new ArrayList<>();
        List<Integer> usedIds = new ArrayList<>();
        int j = 0;
        while (j < i) {
            int idVector = (int) (Math.random() * vectors.size() - 1);
            double[] arr;
            if (!usedIds.contains(idVector)) {
                arr = vectors.get((int) (Math.random() * vectors.size() - 1)).clone();
                usedIds.add(idVector);
                randVectors.add(arr);
                j++;
            }
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
                learningRate = A / (B + iteration);
                sigma = sigma0 * Math.exp(-(iteration / learningRate));
//                sigma = sigma0 / (1 + (iteration / epoch));


                for (int j = 0; j < inputVectors.size(); j++) {
                    double[] vector = inputVectors.get(j).clone();
                    Neuron winner = getWinners(vector);
                    System.out.println("winner X: " + winner.getX() + " Y: " + winner.getY());
                    if (winner.getColor() == null) {
                        winner.setColor(colorsList.get(colorIndex));
                        colorIndex++;
                    }
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
            }
            System.out.println("Stop train");
        }).start();
        controller.show(neuronMap);
    }

    public Neuron getWinners(double[] vector) {
        Neuron winner;
        double sqSum = 0;
        weight = new double[neuronMap.length][neuronMap.length];
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
        winner = neuronMap[0][0];
        for (int i = 0; i < weight.length; i++) {
            for (int j = 0; j < weight[0].length; j++) {
                if (weight[i][j] < minWeight) {
                    winner = neuronMap[i][j];
                    minWeight = weight[i][j];
                }
            }
        }
        return winner;
    }

    public void clarifyWeights(Neuron winner, double[] vector) {
        int x = winner.getX();
        int y = winner.getY();

        for (int i = 0; i < neuronMap.length; i++) {
            for (int j = 0; j < neuronMap[0].length; j++) {
                    double[] mapVector = neuronMap[i][j].getVector();
                    double h = learningRate * (isGaussian ? gaussianNeighborhood(neuronMap[i][j], winner) : rectangularNeighborhood(neuronMap[i][j]));
                    if (h != 0) neuronMap[i][j].setColor(winner.getColor());
                if (neuronMap[i][j].getX() != x || neuronMap[i][j].getX() != y)
                    for (int k = 0; k < mapVector.length; k++) {
                        mapVector[k] = mapVector[k] + h * (vector[k]-mapVector[k]);
                    }
                }
        }
    }


    public double rectangularNeighborhood(Neuron neuron) {
        if (weight[neuron.getX()][neuron.getY()] <= K && weight[neuron.getX()][neuron.getY()] >= -K) {
            System.out.println("Neighborhood X: " + neuron.getX() + " Y: " + neuron.getY());
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

    public Neuron[][] getNeuronMap() {
        return neuronMap;
    }

    public void initColorList() {
        colorsList.add(Color.RED);
        colorsList.add(Color.BLUE);
        colorsList.add(Color.ORANGE);
        colorsList.add(Color.CYAN);
        colorsList.add(Color.GREEN);
        colorsList.add(Color.MAGENTA);
        colorsList.add(Color.YELLOW);
        colorsList.add(Color.LIME);
        colorsList.add(Color.DARKBLUE);
        colorsList.add(Color.PURPLE);
        colorsList.add(Color.OLIVE);
        colorsList.add(Color.GREY);
        colorsList.add(Color.CORNFLOWERBLUE);
        colorsList.add(Color.VIOLET);
        colorsList.add(Color.SIENNA);
        colorsList.add(Color.TOMATO);
        colorsList.add(Color.TAN);
        colorsList.add(Color.DARKSALMON);
        colorsList.add(Color.ROSYBROWN);
        colorsList.add(Color.TEAL);
        colorsList.add(Color.PLUM);
        colorsList.add(Color.LIGHTPINK);
        colorsList.add(Color.GOLD);
        colorsList.add(Color.HOTPINK);
        colorsList.add(Color.SLATEBLUE);
        colorsList.add(Color.DARKKHAKI);
        colorsList.add(Color.DIMGRAY);
        colorsList.add(Color.DARKVIOLET);
        colorsList.add(Color.TURQUOISE);
        colorsList.add(Color.NAVAJOWHITE);
        colorsList.add(Color.MEDIUMAQUAMARINE);
        colorsList.add(Color.DARKGREEN);
        colorsList.add(Color.KHAKI);
        colorsList.add(Color.GREENYELLOW);
        colorsList.add(Color.FIREBRICK);
        colorsList.add(Color.MEDIUMORCHID);
        colorsList.add(Color.FUCHSIA);
        colorsList.add(Color.OLIVEDRAB);
        colorsList.add(Color.DEEPPINK);
        colorsList.add(Color.DARKOLIVEGREEN);
        colorsList.add(Color.OLDLACE);
        colorsList.add(Color.LIGHTGOLDENRODYELLOW);
        colorsList.add(Color.BEIGE);
        colorsList.add(Color.BLACK);
        colorsList.add(Color.PEACHPUFF);
        colorsList.add(Color.MOCCASIN);
        colorsList.add(Color.SALMON);
        colorsList.add(Color.SILVER);
        colorsList.add(Color.AZURE);
        colorsList.add(Color.DODGERBLUE);
        colorsList.add(Color.WHITE);
        colorsList.add(Color.WHEAT);
        colorsList.add(Color.BROWN);
        colorsList.add(Color.PAPAYAWHIP);
    }
}
