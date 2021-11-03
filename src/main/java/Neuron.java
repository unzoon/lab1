import javafx.scene.paint.Color;

public class Neuron {
    private int x,y;
    private double[] vector;
    private Color color;
  

    public Neuron(int x, int y, double[] vector) {
        this.x = x;
        this.y = y;
        this.vector = vector;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public double[] getVector() {
        return vector;
    }

    public void setVector(double[] vector) {
        this.vector = vector;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }
}
