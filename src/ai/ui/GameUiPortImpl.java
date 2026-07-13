package ai.ui;

import java.awt.Color;
import java.util.Map;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import shared.ui_ports.GameUiPort;

public class GameUiPortImpl extends GameUiPort {
    private Map<String, Circle> circles;
    private DrawingPanel panel;

    public GameUiPortImpl(Map<String, Circle> circles, DrawingPanel panel) {
        this.circles = circles;
        this.panel   = panel;
    }

    @Override
    public void addBall(int id, String ballType, double x, double y, double radius) {
        Color color = parseBallColor(ballType);
        circles.put(String.valueOf(id), new Circle((int) x, (int) y, (int) radius, color));
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
    public void resetBalls() {
        circles.clear();
        panel.repaint();
    }

    @Override
    public void setBallInHand(boolean active) {
        panel.setBallInHandMode(active);
    }

    @Override
    public void updateScoreBoard(String p1Label, String p2Label, String turn) {
        panel.setScoreBoard(turn, p1Label, p2Label);
    }

    @Override
    public void showMessage(String message) {
        String html = "<html><div style='direction:rtl; text-align:right; font-size:14px; padding:4px 8px'>"
                + message.replace("\n", "<br>")
                + "</div></html>";
        SwingUtilities.invokeLater(() ->
            JOptionPane.showMessageDialog(panel, html, "Billiard", JOptionPane.INFORMATION_MESSAGE)
        );
    }

    @Override
    public void log(String message) {
        System.out.println(message);
    }

    private Color parseBallColor(String ballType) {
        switch (ballType.toUpperCase()) {
            case "WHITE":  return Color.WHITE;
            case "RED":    return new Color(200, 50, 50);
            case "YELLOW": return new Color(255, 200, 50);
            case "BLACK":  return new Color(50, 50, 50);
            default:       return Color.BLACK;
        }
    }
}
