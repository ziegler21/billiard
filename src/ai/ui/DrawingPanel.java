package ai.ui;

import base.Params;
import java.awt.*;
import java.awt.event.*;
import java.util.Map;
import javax.swing.*;
import shared.MainRouter;

public class DrawingPanel extends JPanel {

    // ---- צבעים ----
    private static final Color BG_COLOR     = new Color(20, 12, 5);
    private static final Color WOOD_DARK    = new Color(85, 50, 18);
    private static final Color WOOD_MID     = new Color(115, 75, 35);
    private static final Color WOOD_EDGE    = new Color(45, 25, 8);
    private static final Color FELT_COLOR   = new Color(22, 110, 35);
    private static final Color FELT_EDGE    = new Color(15, 80, 25);
    private static final Color POCKET_COLOR = new Color(5, 5, 5);

    private static final int POCKET_RADIUS = 24;
    private static final int FRAME_THICK   = 28;   // עובי מסגרת עץ
    private static final int SCORE_BAR_H   = 62;   // גובה רצועת הניקוד בראש

    // ---- מידות שולחן ----
    private final double tableWidth   = 760;
    private final double tableHeight  = 360;
    // השולחן ממורכז אופקית; מתחיל מתחת לרצועת הניקוד
    private final double tableOffsetX = 70;
    private final double tableOffsetY = SCORE_BAR_H + 28;   // = 90

    // ---- נתונים ----
    private Map<String, Circle> circles;
    private MainRouter mainRouter;

    private String scoreTurn = "";
    private String scoreP1   = "Player 1  [?]  0/7";
    private String scoreP2   = "Player 2  [?]  0/7";

    // ---- מצב ירייה ----
    private Point   startDragPoint    = null;
    private Point   currentMousePoint = null;
    private boolean draggingCueBall   = false;

    // ---- Ball in Hand ----
    private boolean ballInHandMode = false;

    // ================================================================== //

    public DrawingPanel(Map<String, Circle> circles, MainRouter mainRouter) {
        this.circles    = circles;
        this.mainRouter = mainRouter;
        setBackground(BG_COLOR);

        addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e)  { handleMousePressed(e);  }
            @Override public void mouseReleased(MouseEvent e) { handleMouseReleased(e); }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                currentMousePoint = new Point(e.getX(), e.getY());
                if (ballInHandMode) moveCueBallToMouse(e.getX(), e.getY());
            }
            @Override
            public void mouseDragged(MouseEvent e) {
                currentMousePoint = new Point(e.getX(), e.getY());
                if (ballInHandMode) moveCueBallToMouse(e.getX(), e.getY());
                else                handleMouseDragged(e);
            }
        });
    }

    // ---- ממשק ציבורי ----

    public void setScoreBoard(String turn, String p1Label, String p2Label) {
        this.scoreTurn = turn  == null ? "" : turn;
        this.scoreP1   = p1Label == null ? "" : p1Label;
        this.scoreP2   = p2Label == null ? "" : p2Label;
        repaint();
    }

    public void setBallInHandMode(boolean active) {
        ballInHandMode  = active;
        draggingCueBall = false;
        startDragPoint  = null;
        repaint();
    }

    // ================================================================== //
    //  ציור                                                                //
    // ================================================================== //

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING,         RenderingHints.VALUE_RENDER_QUALITY);

        renderTopScoreBar(g2d);   // ← ניקוד למעלה
        renderFrame(g2d);
        renderTable(g2d);
        renderPockets(g2d);
        renderCircles(g2d);
        if (ballInHandMode) renderBallInHandOverlay(g2d);
        else                renderAimingLine(g2d);
        renderBottomHint(g2d);   // ← רק הוראות קצרות למטה
    }

    // ---- רצועת ניקוד עליונה ----
    private void renderTopScoreBar(Graphics2D g) {
        int W = getWidth();

        // רקע כהה
        g.setColor(new Color(10, 6, 2, 220));
        g.fillRect(0, 0, W, SCORE_BAR_H);

        // קו הפרדה תחתון
        g.setColor(new Color(90, 58, 20));
        g.setStroke(new BasicStroke(1.5f));
        g.drawLine(0, SCORE_BAR_H - 1, W, SCORE_BAR_H - 1);

        int midX = W / 2;
        int rowY = SCORE_BAR_H / 2;  // אמצע הרצועה

        // ---- שחקן 1 — שמאל ----
        drawPlayerBadge(g, 18, rowY, scoreP1, true);

        // ---- שחקן 2 — ימין ----
        drawPlayerBadge(g, W - 18, rowY, scoreP2, false);

        // ---- תור — מרכז ----
        if (!scoreTurn.isEmpty()) {
            boolean gameOver = scoreTurn.contains("OVER");
            String  turnText = (gameOver ? "★ " : "▶  ") + scoreTurn;
            g.setFont(new Font("Arial", Font.BOLD, 14));
            Color tc = gameOver ? new Color(255, 120, 50) : new Color(240, 240, 240);
            g.setColor(tc);
            int tw = g.getFontMetrics().stringWidth(turnText);
            g.drawString(turnText, midX - tw / 2, rowY + 5);
        }
    }

    private void drawPlayerBadge(Graphics2D g, int anchorX, int midY, String label, boolean leftAlign) {
        Color ballColor = labelColor(label);
        int dotR = 7;

        // כדור-סמל
        int dotX = leftAlign ? anchorX : anchorX - dotR * 2;
        g.setColor(ballColor);
        g.fillOval(dotX, midY - dotR, dotR * 2, dotR * 2);
        g.setColor(new Color(0, 0, 0, 100));
        g.setStroke(new BasicStroke(1f));
        g.drawOval(dotX, midY - dotR, dotR * 2, dotR * 2);

        // טקסט
        g.setFont(new Font("Arial", Font.BOLD, 13));
        g.setColor(ballColor);
        int textX = leftAlign ? anchorX + dotR * 2 + 8 : anchorX - dotR * 2 - 8 - g.getFontMetrics().stringWidth(label);
        g.drawString(label, textX, midY + 5);
    }

    // ---- מסגרת עץ ----
    private void renderFrame(Graphics2D g) {
        int ox = (int) tableOffsetX, oy = (int) tableOffsetY;
        int w  = (int) tableWidth,   h  = (int) tableHeight;
        int ft = FRAME_THICK;

        g.setColor(new Color(0, 0, 0, 130));
        g.fillRoundRect(ox-ft+6, oy-ft+6, w+ft*2, h+ft*2, 12, 12);

        g.setColor(WOOD_DARK);
        g.fillRoundRect(ox-ft, oy-ft, w+ft*2, h+ft*2, 10, 10);

        g.setColor(WOOD_MID);
        g.fillRoundRect(ox-ft+4, oy-ft+4, w+ft*2-8, h+ft*2-8, 7, 7);

        g.setColor(WOOD_EDGE);
        g.setStroke(new BasicStroke(2.5f));
        g.drawRoundRect(ox-ft+4, oy-ft+4, w+ft*2-8, h+ft*2-8, 7, 7);
    }

    // ---- שטיח ירוק ----
    private void renderTable(Graphics2D g) {
        int ox = (int) tableOffsetX, oy = (int) tableOffsetY;
        int w  = (int) tableWidth,   h  = (int) tableHeight;
        g.setColor(FELT_COLOR);
        g.fillRect(ox, oy, w, h);
        g.setColor(FELT_EDGE);
        g.setStroke(new BasicStroke(2.5f));
        g.drawRect(ox, oy, w, h);
    }

    // ---- חורים ----
    private void renderPockets(Graphics2D g) {
        for (int[] pos : getPocketScreenPositions()) {
            int px = pos[0], py = pos[1];
            g.setColor(new Color(0, 0, 0, 90));
            g.fillOval(px-POCKET_RADIUS+3, py-POCKET_RADIUS+3, POCKET_RADIUS*2, POCKET_RADIUS*2);
            g.setColor(POCKET_COLOR);
            g.fillOval(px-POCKET_RADIUS, py-POCKET_RADIUS, POCKET_RADIUS*2, POCKET_RADIUS*2);
            g.setColor(new Color(55, 35, 10));
            g.setStroke(new BasicStroke(2.5f));
            g.drawOval(px-POCKET_RADIUS, py-POCKET_RADIUS, POCKET_RADIUS*2, POCKET_RADIUS*2);
        }
    }

    private int[][] getPocketScreenPositions() {
        int ox=(int)tableOffsetX, oy=(int)tableOffsetY, w=(int)tableWidth, h=(int)tableHeight;
        return new int[][] {
            {ox,     oy    }, {ox+w, oy    },
            {ox,     oy+h  }, {ox+w, oy+h  },
            {ox+w/2, oy    }, {ox+w/2, oy+h}
        };
    }

    // ---- כדורים ----
    private void renderCircles(Graphics2D g) {
        int ox = (int) tableOffsetX, oy = (int) tableOffsetY;
        for (Map.Entry<String, Circle> entry : circles.entrySet()) {
            Circle  c         = entry.getValue();
            boolean isCueBall = "0".equals(entry.getKey());
            int sx = c.cx + ox, sy = c.cy + oy, r = c.radius;

            if (isCueBall && ballInHandMode) {
                g.setColor(new Color(255, 255, 200, 50));
                g.fillOval(sx-r-9, sy-r-9, (r+9)*2, (r+9)*2);
                g.setColor(new Color(255, 240, 150, 140));
                g.setStroke(new BasicStroke(2f));
                g.drawOval(sx-r-6, sy-r-6, (r+6)*2, (r+6)*2);
            }

            g.setColor(new Color(0, 0, 0, 70));
            g.fillOval(sx-r+3, sy-r+3, r*2, r*2);

            g.setColor(c.color);
            g.fillOval(sx-r, sy-r, r*2, r*2);

            g.setColor(new Color(0, 0, 0, 160));
            g.setStroke(new BasicStroke(1.2f));
            g.drawOval(sx-r, sy-r, r*2, r*2);

            int hs = Math.max(3, r/3);
            g.setColor(new Color(255, 255, 255, 90));
            g.fillOval(sx-r/2, sy-r/2, hs, hs);
        }
    }

    // ---- Ball in Hand overlay ----
    private void renderBallInHandOverlay(Graphics2D g) {
        int ox=(int)tableOffsetX, oy=(int)tableOffsetY, w=(int)tableWidth, h=(int)tableHeight;

        g.setColor(new Color(0, 0, 0, 55));
        g.fillRect(ox, oy, w, h);

        String line1 = "BALL IN HAND";
        String line2 = "Click anywhere on the table to place the white ball";

        g.setFont(new Font("Arial", Font.BOLD, 18));
        int tw1 = g.getFontMetrics().stringWidth(line1);
        g.setFont(new Font("Arial", Font.PLAIN, 13));
        int tw2 = g.getFontMetrics().stringWidth(line2);

        int cx = ox + w/2, cy = oy + h/2;
        int boxW = Math.max(tw1, tw2) + 30, boxH = 52;

        g.setColor(new Color(0, 0, 0, 160));
        g.fillRoundRect(cx-boxW/2, cy-boxH/2, boxW, boxH, 12, 12);
        g.setColor(new Color(180, 140, 50, 180));
        g.setStroke(new BasicStroke(1.5f));
        g.drawRoundRect(cx-boxW/2, cy-boxH/2, boxW, boxH, 12, 12);

        g.setFont(new Font("Arial", Font.BOLD, 18));
        g.setColor(new Color(255, 230, 100));
        g.drawString(line1, cx - tw1/2, cy - 5);

        g.setFont(new Font("Arial", Font.PLAIN, 12));
        g.setColor(new Color(210, 210, 210));
        g.drawString(line2, cx - tw2/2, cy + 16);
    }

    // ---- קו כיוון ----
    private void renderAimingLine(Graphics2D g) {
        if (!draggingCueBall || startDragPoint == null || currentMousePoint == null) return;
        g.setColor(new Color(255, 255, 200, 200));
        g.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                0, new float[]{7, 5}, 0));
        g.drawLine(startDragPoint.x, startDragPoint.y, currentMousePoint.x, currentMousePoint.y);
    }

    // ---- הוראות תחתית ----
    private void renderBottomHint(Graphics2D g) {
        int bottomY = (int)(tableOffsetY + tableHeight + FRAME_THICK + 14);
        int centerX = (int)(tableOffsetX + tableWidth / 2);
        g.setFont(new Font("Arial", Font.PLAIN, 12));
        g.setColor(new Color(140, 110, 65, 150));
        String hint = ballInHandMode
                ? "Click on the table to place the white ball"
                : "Drag the white ball to aim  •  Release to shoot";
        int hw = g.getFontMetrics().stringWidth(hint);
        g.drawString(hint, centerX - hw/2, bottomY);
    }

    // ---- עזר ----
    private Color labelColor(String label) {
        if (label != null && label.contains("RED"))    return new Color(215, 75, 75);
        if (label != null && label.contains("YELLOW")) return new Color(235, 205, 50);
        return new Color(170, 170, 170);
    }

    // ================================================================== //
    //  אינטראקציה                                                          //
    // ================================================================== //

    private void handleMousePressed(MouseEvent e) {
        int ox = (int) tableOffsetX, oy = (int) tableOffsetY;

        if (ballInHandMode) {
            int modelX = clamp(e.getX() - ox, 13, (int)tableWidth  - 13);
            int modelY = clamp(e.getY() - oy, 13, (int)tableHeight - 13);
            mainRouter.route("/game/ball/place", Params.of((double) modelX, (double) modelY));
            ballInHandMode = false;
            repaint();
            return;
        }

        Circle cueBall = circles.get("0");
        if (cueBall != null) {
            int sx = cueBall.cx + ox, sy = cueBall.cy + oy;
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

    private void moveCueBallToMouse(int screenX, int screenY) {
        int modelX = clamp(screenX - (int)tableOffsetX, 13, (int)tableWidth  - 13);
        int modelY = clamp(screenY - (int)tableOffsetY, 13, (int)tableHeight - 13);
        Circle cueBall = circles.get("0");
        if (cueBall != null) {
            cueBall.cx = modelX;
            cueBall.cy = modelY;
            repaint();
        }
    }

    private int clamp(int val, int min, int max) {
        return Math.max(min, Math.min(val, max));
    }
}
