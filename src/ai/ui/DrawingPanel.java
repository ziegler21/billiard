package ai.ui;

import base.Params;
import java.awt.*;
import java.awt.event.*;
import java.util.Map;
import javax.swing.*;
import shared.MainRouter;

public class DrawingPanel extends JPanel {
    private static final Color TABLE_COLOR = new Color(100, 180, 220);
    private double tableWidth = 760;
    private double tableHeight = 360;
    private double tableOffsetX = 20;
    private double tableOffsetY = 20;
    
    private Map<String, Point> points;
    private Map<String, Circle> circles;
    private MainRouter mainRouter;
    private String draggedPointId;
    private String draggedCircleId;

    public DrawingPanel(Map<String, Point> points, Map<String, Circle> circles, MainRouter mainRouter) {
        this.points = points;
        this.circles = circles;
        this.mainRouter = mainRouter;

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                handleMousePressed(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                draggedPointId = null;
                draggedCircleId = null;
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                handleMouseDragged(e);
            }
        });

        addMouseWheelListener(e -> handleMouseWheel(e));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        renderTable(g);
        renderPoints(g);
        renderCircles(g);
    }

    private void renderTable(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(TABLE_COLOR);
        g2d.fillRect((int) tableOffsetX, (int) tableOffsetY, (int) tableWidth, (int) tableHeight);
        
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(3.0f));
        g2d.drawRect((int) tableOffsetX, (int) tableOffsetY, (int) tableWidth, (int) tableHeight);
    }

    private void renderPoints(Graphics g) {
        for (Map.Entry<String, Point> entry : points.entrySet()) {
            Point point = entry.getValue();
            g.setColor(point.color);
            g.fillOval(point.x - 5, point.y - 5, 10, 10);
            g.setColor(Color.BLACK);
            g.drawString(entry.getKey(), point.x + 8, point.y - 5);
        }
    }

    private void renderCircles(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setStroke(new BasicStroke(2.5f));

        for (Map.Entry<String, Circle> entry : circles.entrySet()) {
            Circle circle = entry.getValue();
            if (!circle.isBlinking) {
                g2d.setColor(circle.color);
                g2d.fillOval(circle.cx - circle.radius, circle.cy - circle.radius, circle.radius * 2,
                        circle.radius * 2);
                g2d.setColor(Color.BLACK);
                g2d.setStroke(new BasicStroke(1.5f));
                g2d.drawOval(circle.cx - circle.radius, circle.cy - circle.radius, circle.radius * 2,
                        circle.radius * 2);
            }
        }
    }

    private void handleMousePressed(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();

        // Check if a point was clicked
        for (Map.Entry<String, Point> entry : points.entrySet()) {
            Point p = entry.getValue();
            if (Math.abs(p.x - x) < 10 && Math.abs(p.y - y) < 10) {
                draggedPointId = entry.getKey();
                return;
            }
        }

        // Check if a circle was clicked
        for (Map.Entry<String, Circle> entry : circles.entrySet()) {
            Circle c = entry.getValue();
            double dist = Math.sqrt(Math.pow(c.cx - x, 2) + Math.pow(c.cy - y, 2));
            if (dist <= c.radius) {
                draggedCircleId = entry.getKey();
                // Stop any blinking animation on this circle
                stopBlinking(entry.getKey());
                return;
            }
        }
    }

    private void handleMouseDragged(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();

        if (draggedPointId != null) {
            Point p = points.get(draggedPointId);
            if (p != null) {
                p.x = x;
                p.y = y;
                int pointId = Integer.parseInt(draggedPointId);
                mainRouter.route("/ex3/point/move", Params.of(pointId, (double) x, (double) y));
                repaint();
            }
        } else if (draggedCircleId != null) {
            Circle c = circles.get(draggedCircleId);
            if (c != null) {
                c.cx = x;
                c.cy = y;
                int circleId = Integer.parseInt(draggedCircleId);
                mainRouter.route("/ex3/circle/move", Params.of(circleId, (double) x, (double) y));
                repaint();
            }
        }
    }

    private void handleMouseWheel(MouseWheelEvent e) {
        int x = e.getX();
        int y = e.getY();
        int rotation = e.getWheelRotation();

        // Check if hovering over a circle
        for (Map.Entry<String, Circle> entry : circles.entrySet()) {
            Circle c = entry.getValue();
            double dist = Math.sqrt(Math.pow(c.cx - x, 2) + Math.pow(c.cy - y, 2));
            if (dist <= c.radius + 10) {
                // Stop any blinking animation on this circle
                stopBlinking(entry.getKey());
                int newRadius = Math.max(10, c.radius - rotation * 5);
                c.radius = newRadius;
                int circleId = Integer.parseInt(entry.getKey());
                mainRouter.route("/ex3/circle/radius", Params.of(circleId, (double) newRadius));
                repaint();
                return;
            }
        }
    }

    public void stopBlinking(String circleId) {
        Circle c = circles.get(circleId);
        if (c != null) {
            c.isBlinking = false;
            repaint();
        }
    }
}
