package ai.ui;

import base.Params;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import shared.MainRouter;
import shared.ui_ports.GameUiPort;

public class Ui {
    private MainRouter   mainRouter;
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
        frame.setSize(900, 580);
        frame.setLayout(new BorderLayout());
        frame.getContentPane().setBackground(new Color(20, 12, 5));

        drawingPanel = new DrawingPanel(circles, mainRouter);
        frame.add(drawingPanel, BorderLayout.CENTER);

        JPanel controlPanel = buildControlPanel();
        frame.add(controlPanel, BorderLayout.SOUTH);

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        uiInstance = new GameUiPortImpl(circles, drawingPanel);
        GameUiPort.setInstance(uiInstance);
    }

    private JPanel buildControlPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 8));
        panel.setBackground(new Color(20, 12, 5));
        panel.setBorder(new EmptyBorder(0, 0, 4, 0));

        JButton resetBtn = new JButton("↺  New Game");
        resetBtn.setFont(new Font("Arial", Font.BOLD, 13));
        resetBtn.setForeground(new Color(220, 195, 130));
        resetBtn.setBackground(new Color(60, 38, 14));
        resetBtn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(110, 75, 30), 1),
                new EmptyBorder(5, 20, 5, 20)));
        resetBtn.setFocusPainted(false);
        resetBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        resetBtn.addActionListener(e -> {
            if (mainRouter != null) mainRouter.route("/game/reset", Params.of());
        });

        panel.add(resetBtn);
        return panel;
    }
}
