package ai.ui;

import base.Params;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;
import shared.MainRouter;
import shared.ui_ports.GameUiPort;

public class Ui {
    private MainRouter mainRouter;
    private Map<String, Circle> circles = new HashMap<>();
    private DrawingPanel drawingPanel;
    private GameUiPortImpl uiInstance;

    public void setUiPorts() {
        // Panel will be created in createAndShowWindow, so we defer this
    }

    public void start(MainRouter mainRouter) {
        this.mainRouter = mainRouter;
        createAndShowWindow();
        mainRouter.route("/game/start", Params.of());
    }

    private void createAndShowWindow() {
        JFrame frame = new JFrame("Billiard Game");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(900, 500);
        frame.setLayout(new BorderLayout());

        drawingPanel = new DrawingPanel(circles, mainRouter);
        frame.add(drawingPanel, BorderLayout.CENTER);

        // Add control panel with buttons
        JPanel controlPanel = createControlPanel();
        frame.add(controlPanel, BorderLayout.SOUTH);

        frame.setVisible(true);

        // Initialize GameUiPortImpl with references to circles and panel
        uiInstance = new GameUiPortImpl(circles, drawingPanel);
        GameUiPort.setInstance(uiInstance);
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel();
        JButton periodicButton = new JButton("Run Periodic");
        periodicButton.setFont(new Font("Arial", Font.PLAIN, 24));
        periodicButton.setPreferredSize(new Dimension(300, 80));
        boolean[] isRunning = { false };

        periodicButton.addActionListener(e -> {
            isRunning[0] = !isRunning[0];
            periodicButton.setText(isRunning[0] ? "Stop Periodic" : "Run Periodic");
            mainRouter.route("/ex3/periodic", Params.of());
        });

        panel.add(periodicButton);
        return panel;
    }
}
