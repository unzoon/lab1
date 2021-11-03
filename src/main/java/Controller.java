
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;


import javafx.scene.shape.Rectangle;


import java.awt.*;
import java.io.IOException;
import java.util.Arrays;


public class Controller {

    @FXML
    private AnchorPane q;


    private Rectangle[][] rectangles;

    private static SOM som;

    /**
     * Initializing the controller class. This method is called automatically after the fxml file is loaded.
     */
    @FXML
    private void initialize() throws IOException {

    }

    /**
     *
     */
    public void additionalInit() throws IOException {

    }

    public void createMap(int n) {
//        q.getChildren().clear();
        rectangles = new Rectangle[n][n];
        int step = (int) (q.getWidth() / n);
        int valX = 0;
        int valY = 0;
        for (int i = 0; i < rectangles.length; i++) {
            for (int j = 0; j < rectangles[0].length; j++) {
                Rectangle rectangle = new Rectangle(valX, valY, step, step);
                rectangles[i][j] = rectangle;
                q.getChildren().add(rectangle);
                valX += step;
            }
            valX = 0;
            valY += step;
        }
    }
    public void show(Neuron[][] neurons) {
//
        for (int i = 0; i < neurons.length; i++) {
            for (int j = 0; j < neurons[0].length; j++) {
//                int rgb = (int) (som.getNeuronMap()[i][j].getVector()[ind]*1000);
//                if(rgb>255)rgb=255;
//                javafx.scene.paint.Color fxColor = javafx.scene.paint.Color.rgb(rgb,rgb,rgb);
                if (neurons[i][j].getColor() != null)
                    rectangles[i][j].setFill(neurons[i][j].getColor());
//                System.out.print(Arrays.toString(som.getNeuronMap()[i][j].getVector()));
            }
//            System.out.println("");
        }
    }
}
