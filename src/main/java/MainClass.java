import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainClass extends Application {

    private static SOM som;
    private Controller controller;

    public static void main(String args[]) throws IOException {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {

        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(Object.class.getResource("/form.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);

        controller = loader.getController();
        controller.additionalInit();
        primaryStage.show();

        List<double[]> inputVectors = InputVectorProcessing.readCSV("G:\\iris.csv");
        InputVectorProcessing.normalizing(inputVectors);
        som = new SOM(10,20000,inputVectors, controller);
        som.train();

    }

}
