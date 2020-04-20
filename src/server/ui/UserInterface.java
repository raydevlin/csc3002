package server.ui;

import server.Server;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class UserInterface extends JFrame {

    private Server server;

    public UserInterface(Server server) {
        this();
        this.server = server;
    }

    public UserInterface() {
        setTitle("Server");
        setSize(400, 80);

        // Create JButton and JPanel
        JPanel panel = new JPanel();
        JButton button = new JButton("Update");
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                server.update();
            }
        });

        // Add button to JPanel
        panel.add(button);
        this.getContentPane().add(panel);

        setVisible(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

}
