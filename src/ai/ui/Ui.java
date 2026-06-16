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

    public void setUiPorts() { }

    public void start(MainRouter mainRouter) {
        this.mainRouter = mainRouter;
        createAndShowWindow();
        mainRouter.route("/game/start", Params.of());
    }

    private void createAndShowWindow() {
        JFrame frame = new JFrame("Billiard Game");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(920, 520);
        frame.setLayout(new BorderLayout());

        drawingPanel = new DrawingPanel(circles, mainRouter);
        frame.add(drawingPanel, BorderLayout.CENTER);

        // Control panel with Reset button
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton resetButton = new JButton("Reset");
        resetButton.addActionListener(e -> {
            if (this.mainRouter != null) {
                this.mainRouter.route("/game/reset", Params.of());
            }
        });
        controlPanel.add(resetButton);
        frame.add(controlPanel, BorderLayout.SOUTH);

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        uiInstance = new GameUiPortImpl(circles, drawingPanel);
        GameUiPort.setInstance(uiInstance);
    }
}
