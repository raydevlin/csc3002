package client.ui;

import javax.swing.*;
import java.awt.*;

public class Screen extends JPanel {

    private int[][] data;

    public Screen() {
    }

    public void setData(int[][] data) { this.data = data; }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        setBackground(Color.WHITE);
        if(data == null) return;

        Dimension dims = getSize();
        double height = dims.getHeight()/(double)data.length;
        double width = dims.getWidth()/(double)data[0].length;

        for(int x = 0; x < data.length; x++) {
            for(int y = 0; y < data[x].length; y++) {
                g.setColor((data[x][y]==0)?Color.WHITE:((data[x][y]==1)?Color.BLACK:Color.RED));
                g.fillRect((int)((double)y * width),
                        (int)((double)x * height),
                        (int)width,
                        (int)height);

            }
        }
    }


}

