package ai.ui;

import base.Params;
import java.awt.*;
import java.awt.event.*;
import java.util.Map;
import javax.swing.*;
import shared.MainRouter;

public class DrawingPanel extends JPanel {
    private static final Color TABLE_COLOR = new Color(34, 139, 34); // Billiard green
    private double tableWidth = 760;
    private double tableHeight = 360;
    private double tableOffsetX = 20;
    private double tableOffsetY = 20;
    
    private Map<String, Circle> circles;
    private MainRouter mainRouter;
    
    // Aiming mechanic
    private Point startDragPoint = null;
    private Point currentMousePoint = null;
    private boolean draggingCueBall = false;

    public DrawingPanel(Map<String, Circle> circles, MainRouter mainRouter) {
        this.circles = circles;
        this.mainRouter = mainRouter;

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                handleMousePressed(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                handleMouseReleased(e);
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                handleMouseDragged(e);
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                currentMousePoint = new Point(e.getX(), e.getY());
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        renderTable(g);
        renderCircles(g);
        renderAimingLine(g);
    }

    private void renderTable(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(TABLE_COLOR);
        g2d.fillRect((int) tableOffsetX, (int) tableOffsetY, (int) tableWidth, (int) tableHeight);
        
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(3.0f));
        g2d.drawRect((int) tableOffsetX, (int) tableOffsetY, (int) tableWidth, (int) tableHeight);
    }

    private void renderCircles(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setStroke(new BasicStroke(2.5f));

        for (Map.Entry<String, Circle> entry : circles.entrySet()) {
            Circle circle = entry.getValue();
            g2d.setColor(circle.color);
            g2d.fillOval(circle.cx - circle.radius, circle.cy - circle.radius, circle.radius * 2,
                    circle.radius * 2);
            g2d.setColor(Color.BLACK);
            g2d.setStroke(new BasicStroke(1.5f));
            g2d.drawOval(circle.cx - circle.radius, circle.cy - circle.radius, circle.radius * 2,
                    circle.radius * 2);
        }
    }

    private void renderAimingLine(Graphics g) {
        if (draggingCueBall && startDragPoint != null && currentMousePoint != null) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setColor(Color.WHITE);
            g2d.setStroke(new BasicStroke(2.0f));
            // Draw line from cue ball center to current mouse position
            g2d.drawLine(startDragPoint.x, startDragPoint.y, currentMousePoint.x, currentMousePoint.y);
        }
    }

    private void handleMousePressed(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();

        // Check if cue ball (ID 0) was clicked
        Circle cueBall = circles.get("0");
        if (cueBall != null) {
            double dist = Math.sqrt(Math.pow(cueBall.cx - x, 2) + Math.pow(cueBall.cy - y, 2));
            if (dist <= cueBall.radius) {
                draggingCueBall = true;
                startDragPoint = new Point(cueBall.cx, cueBall.cy);
                currentMousePoint = new Point(x, y);
                return;
            }
        }
    }

    private void handleMouseDragged(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();

        if (draggingCueBall) {
            currentMousePoint = new Point(x, y);
            repaint();
        }
    }

    private void handleMouseReleased(MouseEvent e) {
        if (draggingCueBall && startDragPoint != null && currentMousePoint != null) {
            // Calculate force vector from ball center to current mouse position
            double forceX = startDragPoint.x - currentMousePoint.x;
            double forceY = startDragPoint.y - currentMousePoint.y;
            
            // Send the strike command to the router
            mainRouter.route("/game/ball/strike", Params.of(forceX, forceY));
            
            // Clear aiming line
            draggingCueBall = false;
            startDragPoint = null;
            currentMousePoint = null;
            repaint();
        }
    }
}
