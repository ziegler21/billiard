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
    private static final int   POCKET_RADIUS = 24;
    private static final int   FRAME_THICK   = 38;

    private double tableWidth   = 760;
    private double tableHeight  = 360;
    private double tableOffsetX = 50;
    private double tableOffsetY = 50;

    private Map<String, Circle> circles;
    private MainRouter mainRouter;

    // --- לוח תוצאות ---
    private String scoreTurn = "";
    private String scoreP1   = "Player 1";
    private String scoreP2   = "Player 2";

    // --- ירייה רגילה ---
    private Point   startDragPoint    = null;
    private Point   currentMousePoint = null;
    private boolean draggingCueBall   = false;

    // --- Ball in Hand ---
    private boolean ballInHandMode = false;

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

    // --- ממשק ציבורי ---

    public void setScoreBoard(String turn, String p1Label, String p2Label) {
        this.scoreTurn = turn;
        this.scoreP1   = p1Label;
        this.scoreP2   = p2Label;
        repaint();
    }

    public void setBallInHandMode(boolean active) {
        ballInHandMode  = active;
        draggingCueBall = false;
        startDragPoint  = null;
        repaint();
    }

    // ------------------------------------------------------------------ //
    //  ציור                                                                //
    // ------------------------------------------------------------------ //

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING,         RenderingHints.VALUE_RENDER_QUALITY);

        renderFrame(g2d);
        renderTable(g2d);
        renderPockets(g2d);
        renderCircles(g2d);
        if (ballInHandMode) renderBallInHandOverlay(g2d);
        else                renderAimingLine(g2d);
        renderInstructions(g2d);
    }

    private void renderFrame(Graphics2D g) {
        int ox = (int) tableOffsetX, oy = (int) tableOffsetY;
        int w  = (int) tableWidth,   h  = (int) tableHeight;
        int ft = FRAME_THICK;

        g.setColor(new Color(0, 0, 0, 120));
        g.fillRoundRect(ox-ft+7, oy-ft+7, w+ft*2, h+ft*2, 14, 14);

        g.setColor(WOOD_DARK);
        g.fillRoundRect(ox-ft, oy-ft, w+ft*2, h+ft*2, 12, 12);

        g.setColor(WOOD_MID);
        g.fillRoundRect(ox-ft+5, oy-ft+5, w+ft*2-10, h+ft*2-10, 8, 8);

        g.setColor(WOOD_EDGE);
        g.setStroke(new BasicStroke(3f));
        g.drawRoundRect(ox-ft+5, oy-ft+5, w+ft*2-10, h+ft*2-10, 8, 8);
    }

    private void renderTable(Graphics2D g) {
        int ox = (int) tableOffsetX, oy = (int) tableOffsetY;
        int w  = (int) tableWidth,   h  = (int) tableHeight;
        g.setColor(FELT_COLOR);
        g.fillRect(ox, oy, w, h);
        g.setColor(FELT_EDGE);
        g.setStroke(new BasicStroke(3f));
        g.drawRect(ox, oy, w, h);
    }

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

    private void renderCircles(Graphics2D g) {
        int ox = (int) tableOffsetX, oy = (int) tableOffsetY;
        for (Map.Entry<String, Circle> entry : circles.entrySet()) {
            Circle c  = entry.getValue();
            boolean isCueBall = "0".equals(entry.getKey());
            int sx = c.cx + ox, sy = c.cy + oy, r = c.radius;

            // הילה לכדור הלבן במצב ball-in-hand
            if (isCueBall && ballInHandMode) {
                g.setColor(new Color(255, 255, 255, 55));
                g.fillOval(sx-r-8, sy-r-8, (r+8)*2, (r+8)*2);
                g.setColor(new Color(255, 255, 180, 130));
                g.setStroke(new BasicStroke(2f));
                g.drawOval(sx-r-6, sy-r-6, (r+6)*2, (r+6)*2);
            }

            // צל
            g.setColor(new Color(0, 0, 0, 70));
            g.fillOval(sx-r+3, sy-r+3, r*2, r*2);
            // כדור
            g.setColor(c.color);
            g.fillOval(sx-r, sy-r, r*2, r*2);
            // מסגרת
            g.setColor(new Color(0, 0, 0, 160));
            g.setStroke(new BasicStroke(1.2f));
            g.drawOval(sx-r, sy-r, r*2, r*2);
            // נקודת אור
            int hs = Math.max(3, r/3);
            g.setColor(new Color(255, 255, 255, 90));
            g.fillOval(sx-r/2, sy-r/2, hs, hs);
        }
    }

    private void renderBallInHandOverlay(Graphics2D g) {
        int ox=(int)tableOffsetX, oy=(int)tableOffsetY, w=(int)tableWidth, h=(int)tableHeight;

        // הכהייה קלה של השולחן
        g.setColor(new Color(0, 0, 0, 60));
        g.fillRect(ox, oy, w, h);

        // טקסט מרכזי
        g.setFont(new Font("Arial", Font.BOLD, 18));
        String line1 = "BALL IN HAND";
        String line2 = "Click anywhere to place the white ball";
        int cx = ox + w / 2;
        int cy = oy + h / 2;

        // רקע לטקסט
        int tw1 = g.getFontMetrics().stringWidth(line1);
        g.setFont(new Font("Arial", Font.PLAIN, 13));
        int tw2 = g.getFontMetrics().stringWidth(line2);
        int maxW = Math.max(tw1, tw2) + 24;
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRoundRect(cx - maxW/2, cy - 28, maxW, 48, 10, 10);

        g.setFont(new Font("Arial", Font.BOLD, 18));
        g.setColor(new Color(255, 230, 100));
        g.drawString(line1, cx - tw1/2, cy - 8);

        g.setFont(new Font("Arial", Font.PLAIN, 13));
        g.setColor(new Color(220, 220, 220));
        g.drawString(line2, cx - tw2/2, cy + 14);
    }

    private void renderAimingLine(Graphics2D g) {
        if (!draggingCueBall || startDragPoint == null || currentMousePoint == null) return;
        g.setColor(new Color(255, 255, 200, 200));
        g.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                0, new float[]{7, 5}, 0));
        g.drawLine(startDragPoint.x, startDragPoint.y, currentMousePoint.x, currentMousePoint.y);
    }

    private void renderInstructions(Graphics2D g) {
        int bottomY = (int)(tableOffsetY + tableHeight) + FRAME_THICK;
        int centerX = (int)(tableOffsetX + tableWidth / 2);

        g.setFont(new Font("Arial", Font.BOLD, 14));

        g.setColor(labelColor(scoreP1));
        g.drawString(scoreP1, (int) tableOffsetX, bottomY + 15);

        g.setColor(labelColor(scoreP2));
        int p2W = g.getFontMetrics().stringWidth(scoreP2);
        g.drawString(scoreP2, (int)(tableOffsetX + tableWidth) - p2W, bottomY + 15);

        if (!scoreTurn.isEmpty()) {
            boolean gameOver = scoreTurn.contains("OVER");
            g.setColor(gameOver ? new Color(255, 120, 50) : Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 13));
            String turnText = (gameOver ? "★ " : "▶ ") + scoreTurn;
            int tw = g.getFontMetrics().stringWidth(turnText);
            g.drawString(turnText, centerX - tw/2, bottomY + 15);
        }

        g.setFont(new Font("Arial", Font.PLAIN, 12));
        g.setColor(new Color(190, 160, 100, 130));
        String instr = ballInHandMode
                ? "Click on the table to place the white ball"
                : "Drag the white ball to aim  •  Release to shoot";
        int instrW = g.getFontMetrics().stringWidth(instr);
        g.drawString(instr, centerX - instrW/2, bottomY + 32);
    }

    private Color labelColor(String label) {
        if (label.contains("RED"))    return new Color(220, 80, 80);
        if (label.contains("YELLOW")) return new Color(240, 210, 50);
        return new Color(180, 180, 180);
    }

    // ------------------------------------------------------------------ //
    //  אינטראקציה                                                          //
    // ------------------------------------------------------------------ //

    private void handleMousePressed(MouseEvent e) {
        int ox = (int) tableOffsetX, oy = (int) tableOffsetY;

        if (ballInHandMode) {
            // הנחת כדור לבן בנקודה שנבחרה
            int modelX = clamp(e.getX() - ox, 12, (int)tableWidth  - 12);
            int modelY = clamp(e.getY() - oy, 12, (int)tableHeight - 12);
            mainRouter.route("/game/ball/place", Params.of((double) modelX, (double) modelY));
            ballInHandMode = false;
            repaint();
            return;
        }

        // ירייה רגילה — גרור מהכדור הלבן
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

    // הזזת הכדור הלבן בעקבות העכבר (רק במצב ball-in-hand)
    private void moveCueBallToMouse(int screenX, int screenY) {
        int modelX = clamp(screenX - (int)tableOffsetX, 12, (int)tableWidth  - 12);
        int modelY = clamp(screenY - (int)tableOffsetY, 12, (int)tableHeight - 12);
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
