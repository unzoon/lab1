import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InputVectorProcessing {

    public static List<double[]> readCSV(String path){
        try (BufferedReader br = Files.newBufferedReader(Paths.get(path))) {

            List<double[]> inputVectors = new ArrayList<>();
            String line;
            br.readLine();

            while ((line = br.readLine()) != null) {
                String[] vector = new String[5];
                vector[0]=line.split(",")[0];
                vector[1]= line.split(",")[1];
                vector[2]=line.split(",")[2];
                vector[3]= line.split(",")[3];
                vector[4]= line.split(",")[4];
                inputVectors.add(Arrays.stream(vector).mapToDouble(Double::parseDouble).toArray());
            }
            return inputVectors;

        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static void normalizing(List<double[]> inputVectors) {
        for (int i = 0; i <inputVectors.size() ; i++) {
            double sqSum = 0;
            double[] inputVector = inputVectors.get(i);
            for (int j = 0; j < inputVector.length; j++) {
                sqSum+= Math.pow(inputVector[j],2);
            }
            sqSum = Math.sqrt(sqSum);
            for (int k = 0; k < inputVector.length; k++) {
                inputVector[k] =  inputVector[k]/sqSum;
            }
            inputVectors.set(i,inputVector);
        }

    }
}
