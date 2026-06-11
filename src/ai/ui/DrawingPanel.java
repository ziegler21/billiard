package ai.ui;

import base.Params;
import java.awt.*;
import java.awt.event.*;
import java.util.Map;
import javax.swing.*;
import shared.MainRouter;

public class DrawingPanel extends JPanel {

    private static final Color BG_COLOR     = new Color(20, 12, 5);
    private static final Color WOOD_DARK    = new Color(85, 50, 18);
    private static final Color WOOD_MID     = new Color(115, 75, 35);
    private static final Color WOOD_EDGE    = new Color(45, 25, 8);
    private static final Color FELT_COLOR   = new Color(22, 110, 35);
    private static final Color FELT_EDGE    = new Color(15, 80, 25);
    private static final Color POCKET_COLOR = new Color(5, 5, 5);
    private static final int   POCKET_RADIUS = 18;
    private static final int   FRAME_THICK   = 38;

    private double tableWidth   = 760;
    private double tableHeight  = 360;
    private double tableOffsetX = 50;
    private double tableOffsetY = 50;

    private Map<String, Circle> circles;
    private MainRouter mainRouter;

    private Point startDragPoint    = null;
    private Point currentMousePoint = null;
    private boolean draggingCueBall = false;

    public DrawingPanel(Map<String, Circle> circles, MainRouter mainRouter) {
        this.circles = circles;
        this.mainRouter = mainRouter;
        setBackground(BG_COLOR);

        addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e)  { handleMousePressed(e);  }
            @Override public void mouseReleased(MouseEvent e) { handleMouseReleased(e); }
        });
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override public void mouseDragged(MouseEvent e) { handleMouseDragged(e); }
            @Override public void mouseMoved(MouseEvent e)   { currentMousePoint = new Point(e.getX(), e.getY()); }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,        RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,   RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING,           RenderingHints.VALUE_RENDER_QUALITY);

        renderFrame(g2d);
        renderTable(g2d);
        renderPockets(g2d);
        renderCircles(g2d);
        renderAimingLine(g2d);
        renderInstructions(g2d);
    }

    private void renderFrame(Graphics2D g) {
        int ox = (int) tableOffsetX;
        int oy = (int) tableOffsetY;
        int w  = (int) tableWidth;
        int h  = (int) tableHeight;
        int ft = FRAME_THICK;

        // Drop shadow
        g.setColor(new Color(0, 0, 0, 120));
        g.fillRoundRect(ox - ft + 7, oy - ft + 7, w + ft * 2, h + ft * 2, 14, 14);

        // Outer wood layer
        g.setColor(WOOD_DARK);
        g.fillRoundRect(ox - ft, oy - ft, w + ft * 2, h + ft * 2, 12, 12);

        // Inner wood layer (lighter)
        g.setColor(WOOD_MID);
        g.fillRoundRect(ox - ft + 5, oy - ft + 5, w + ft * 2 - 10, h + ft * 2 - 10, 8, 8);

        // Wood inner edge
        g.setColor(WOOD_EDGE);
        g.setStroke(new BasicStroke(3f));
        g.drawRoundRect(ox - ft + 5, oy - ft + 5, w + ft * 2 - 10, h + ft * 2 - 10, 8, 8);
    }

    private void renderTable(Graphics2D g) {
        int ox = (int) tableOffsetX;
        int oy = (int) tableOffsetY;
        int w  = (int) tableWidth;
        int h  = (int) tableHeight;

        g.setColor(FELT_COLOR);
        g.fillRect(ox, oy, w, h);

        // Subtle inner shadow at felt edges
        g.setColor(FELT_EDGE);
        g.setStroke(new BasicStroke(3f));
        g.drawRect(ox, oy, w, h);
    }

    private void renderPockets(Graphics2D g) {
        for (int[] pos : getPocketScreenPositions()) {
            int px = pos[0];
            int py = pos[1];

            // Pocket shadow
            g.setColor(new Color(0, 0, 0, 90));
            g.fillOval(px - POCKET_RADIUS + 3, py - POCKET_RADIUS + 3, POCKET_RADIUS * 2, POCKET_RADIUS * 2);

            // Pocket hole
            g.setColor(POCKET_COLOR);
            g.fillOval(px - POCKET_RADIUS, py - POCKET_RADIUS, POCKET_RADIUS * 2, POCKET_RADIUS * 2);

            // Pocket leather ring
            g.setColor(new Color(55, 35, 10));
            g.setStroke(new BasicStroke(2.5f));
            g.drawOval(px - POCKET_RADIUS, py - POCKET_RADIUS, POCKET_RADIUS * 2, POCKET_RADIUS * 2);
        }
    }

    private int[][] getPocketScreenPositions() {
        int ox = (int) tableOffsetX;
        int oy = (int) tableOffsetY;
        int w  = (int) tableWidth;
        int h  = (int) tableHeight;
        return new int[][] {
            { ox,         oy     },
            { ox + w,     oy     },
            { ox,         oy + h },
            { ox + w,     oy + h },
            { ox + w / 2, oy     },
            { ox + w / 2, oy + h }
        };
    }

    private void renderCircles(Graphics2D g) {
        int ox = (int) tableOffsetX;
        int oy = (int) tableOffsetY;

        for (Map.Entry<String, Circle> entry : circles.entrySet()) {
            Circle circle = entry.getValue();
            int sx = circle.cx + ox;
            int sy = circle.cy + oy;
            int r  = circle.radius;

            // Ball shadow
            g.setColor(new Color(0, 0, 0, 70));
            g.fillOval(sx - r + 3, sy - r + 3, r * 2, r * 2);

            // Ball fill
            g.setColor(circle.color);
            g.fillOval(sx - r, sy - r, r * 2, r * 2);

            // Ball outline
            g.setColor(new Color(0, 0, 0, 160));
            g.setStroke(new BasicStroke(1.2f));
            g.drawOval(sx - r, sy - r, r * 2, r * 2);

            // Specular highlight
            int hs = Math.max(3, r / 3);
            g.setColor(new Color(255, 255, 255, 90));
            g.fillOval(sx - r / 2, sy - r / 2, hs, hs);
        }
    }

    private void renderAimingLine(Graphics2D g) {
        if (!draggingCueBall || startDragPoint == null || currentMousePoint == null) return;
        g.setColor(new Color(255, 255, 200, 200));
        g.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                0, new float[]{7, 5}, 0));
        g.drawLine(startDragPoint.x, startDragPoint.y, currentMousePoint.x, currentMousePoint.y);
    }

    private void renderInstructions(Graphics2D g) {
        g.setColor(new Color(190, 160, 100, 160));
        g.setFont(new Font("Arial", Font.PLAIN, 13));
        String text = "Drag the white ball to aim  •  Release to shoot";
        int textW = g.getFontMetrics().stringWidth(text);
        int x = (int) (tableOffsetX + tableWidth / 2) - textW / 2;
        int y = (int) (tableOffsetY + tableHeight) + FRAME_THICK + 16;
        g.drawString(text, x, y);
    }

    private void handleMousePressed(MouseEvent e) {
        int ox = (int) tableOffsetX;
        int oy = (int) tableOffsetY;

        Circle cueBall = circles.get("0");
        if (cueBall != null) {
            int sx = cueBall.cx + ox;
            int sy = cueBall.cy + oy;
            if (Math.hypot(sx - e.getX(), sy - e.getY()) <= cueBall.radius + 5) {
                draggingCueBall   = true;
                startDragPoint    = new Point(sx, sy);
                currentMousePoint = new Point(e.getX(), e.getY());
            }
        }
    }

    private void handleMouseDragged(MouseEvent e) {
        if (draggingCueBall) {
            currentMousePoint = new Point(e.getX(), e.getY());
            repaint();
        }
    }

    private void handleMouseReleased(MouseEvent e) {
        if (!draggingCueBall || startDragPoint == null || currentMousePoint == null) return;
        double forceX = startDragPoint.x - currentMousePoint.x;
        double forceY = startDragPoint.y - currentMousePoint.y;
        mainRouter.route("/game/ball/strike", Params.of(forceX, forceY));
        draggingCueBall   = false;
        startDragPoint    = null;
        currentMousePoint = null;
        repaint();
    }
}
