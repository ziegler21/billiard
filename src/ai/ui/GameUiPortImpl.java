package ai.ui;

import java.awt.Color;
import java.util.Map;
import javax.swing.JPanel;
import shared.ui_ports.GameUiPort;

public class GameUiPortImpl extends GameUiPort {
    private Map<String, Circle> circles;
    private JPanel panel;

    public GameUiPortImpl(Map<String, Circle> circles, JPanel panel) {
        this.circles = circles;
        this.panel = panel;
    }

    @Override
    public void addBall(int id, String ballType, double x, double y, double radius) {
        Color color = parseBallColor(ballType);
        Circle circle = new Circle((int) x, (int) y, (int) radius, color);
        circles.put(String.valueOf(id), circle);
        panel.repaint();
    }

    @Override
    public void updateBallPosition(int id, double x, double y) {
        Circle circle = circles.get(String.valueOf(id));
        if (circle != null) {
            circle.cx = (int) x;
            circle.cy = (int) y;
            panel.repaint();
        }
    }

    @Override
    public void hideBall(int id) {
        circles.remove(String.valueOf(id));
        panel.repaint();
    }

    @Override
    public void updateScoreBoard(String turn, int p1Score, int p2Score) {
        log("Turn: " + turn + " | Player 1: " + p1Score + " | Player 2: " + p2Score);
    }

    @Override
    public void showMessage(String message) {
        log("Message: " + message);
    }

    @Override
    public void log(String message) {
        System.out.println(message);
    }

    private Color parseBallColor(String ballType) {
        try {
            switch (ballType.toUpperCase()) {
                case "WHITE":
                    return Color.WHITE;
                case "RED":
                    return new Color(200, 50, 50);
                case "YELLOW":
                    return new Color(255, 200, 50);
                case "BLACK":
                    return new Color(50, 50, 50);
                default:
                    return Color.BLACK;
            }
        } catch (Exception e) {
            return Color.BLACK;
        }
    }
}
