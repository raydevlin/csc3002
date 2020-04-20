package client.ui;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Adapted from the article: JavaFX Tip 1: Resizable Canvas from our JCG partner Dirk Lemmermann at the Pixel Perfect blog
 * https://www.javacodegeeks.com/2014/04/javafx-tip-1-resizable-canvas.html
 */
public class ResizableCanvas extends Canvas {

    int[][] data;
    boolean doResizeRedraw = true;

    public ResizableCanvas() {
        // Redraw canvas when size changes.
        if(doResizeRedraw) {
            widthProperty().addListener(evt -> draw());
            heightProperty().addListener(evt -> draw());
        }
    }

    public void draw() {

        if(data != null) {

            double width = getWidth();
            double height = getHeight();

            GraphicsContext gc = getGraphicsContext2D();
            gc.clearRect(0, 0, width, height);

            gc.setFill(Color.BLACK);
            double cellHeight = height/(double)data.length;
            double cellWidth = width/(double)data[0].length;

            for(int x = 0; x < data.length; x++) {
                for(int y = 0; y < data[x].length; y++) {
                    if(data[x][y] == 1)
                        gc.fillRect(((double)y * cellWidth),
                                ((double)x * cellHeight),
                                cellWidth,
                                cellHeight);
                }
            }

        }
        else {
            System.out.println("data null");
        }
    }

    public void setData(int[][] data) {
        this.data = data;
    }

    @Override
    public boolean isResizable() {
        return true;
    }

    @Override
    public double prefWidth(double height) {
        return getWidth();
    }

    @Override
    public double prefHeight(double width) {
        return getHeight();
    }
}
