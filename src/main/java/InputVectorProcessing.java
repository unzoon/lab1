import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InputVectorProcessing {

    public static List<double[]> readCSV(String path) {
        try (BufferedReader br = Files.newBufferedReader(Paths.get(path))) {

            List<double[]> inputVectors = new ArrayList<>();
            String line;
            br.readLine();

            while ((line = br.readLine()) != null) {
                String[] vector = new String[4];
                String[] splitLine = line.split(",");
                System.arraycopy(splitLine, 0, vector, 0, splitLine.length - 1);
                inputVectors.add(Arrays.stream(vector).mapToDouble(Double::parseDouble).toArray());
            }
            return inputVectors;

        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static void normalizing(List<double[]> inputVectors) {
        for (double[] vector : inputVectors) {
            double sqSum = 0;
            for (double v : vector) {
                sqSum += Math.pow(v, 2);
            }
            sqSum = Math.sqrt(sqSum);
            for (int k = 0; k < vector.length; k++) {
                vector[k] = vector[k] / sqSum;
            }
        }

    }
}
