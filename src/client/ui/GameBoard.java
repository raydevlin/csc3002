package client.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class GameBoard {

    /*int width = 320, height = 180;*/
    int width = 600, height = 300;

    private final String FRAME_NAME = "GOL_0.3";
    private final String CONFIG_FRAME_NAME = "Options";
    private JFrame mainFrame = new JFrame(FRAME_NAME);
    private JFrame configFrame = new JFrame(CONFIG_FRAME_NAME);
    private Screen screen = new Screen();


    public GameBoard() {
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setPreferredSize(new Dimension(width,height));
        mainFrame.setResizable(true);
        mainFrame.setLayout(new GridLayout(1,1,0,0));
        mainFrame.add(screen);
        mainFrame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                super.componentResized(e);
                screen.repaint();
            }
        });

        mainFrame.setVisible(true);
        mainFrame.pack();
    }

    public JFrame getMainFrame() { return mainFrame; }

    public void setData(int[][] data) {
        screen.setData(data);
        screen.repaint();
    }


}
