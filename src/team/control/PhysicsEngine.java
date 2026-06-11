package team.control;

import team.model.Ball;
import team.model.Point;
import team.model.Table;

import java.util.List;

public class PhysicsEngine {

    private static final double FRICTION         = 0.98;
    private static final double WALL_RESTITUTION = 0.75;
    private static final double BALL_RESTITUTION = 0.97;
    private static final double STOP_THRESHOLD   = 0.15;
    // כדור נבלע כשמרכזו נמצא בתוך תחום החור (חצי חור לפחות חופף)
    private static final double POCKET_THRESHOLD = 1.0;

    private final Table table;

    private Ball.BallType firstContactType = null;
    private boolean anyBallHit = false;

    public PhysicsEngine(Table table) {
        this.table = table;
    }

    public void resetShotTracking() {
        firstContactType = null;
        anyBallHit = false;
    }

    public Ball.BallType getFirstContactType() { return firstContactType; }
    public boolean wasAnyBallHit()             { return anyBallHit; }

    public boolean updatePhysics() {
        List<Ball> balls = table.getBalls();

        // 1. חיכוך + קידום מיקום
        for (Ball b : balls) {
            if (b.isPocketed()) continue;
            double speed = Math.hypot(b.getVx(), b.getVy());
            if (speed > STOP_THRESHOLD) {
                b.setVx(b.getVx() * FRICTION);
                b.setVy(b.getVy() * FRICTION);
                b.setPosition(b.getX() + b.getVx(), b.getY() + b.getVy());
            } else {
                b.setVx(0);
                b.setVy(0);
            }
        }

        // 2. בדיקת חורים לפני הקיר — חיוני לפינות, אחרת הקיר דוחף חזרה
        checkPockets(balls);

        // 3. התנגשויות קיר (רק כדורים שלא נבלעו)
        for (Ball b : balls) {
            if (!b.isPocketed()) checkWallCollisions(b);
        }

        // 4. התנגשויות בין כדורים
        checkBallCollisions(balls);

        // 5. Clamp — אף כדור לא יוצא מהמסגרת
        for (Ball b : balls) {
            if (!b.isPocketed()) clampToBounds(b);
        }

        // 6. האם עדיין יש תנועה?
        for (Ball b : balls) {
            if (!b.isPocketed() && Math.hypot(b.getVx(), b.getVy()) > STOP_THRESHOLD) {
                return true;
            }
        }
        return false;
    }

    private void checkWallCollisions(Ball b) {
        double r  = b.getRadius();
        double w  = table.getWidth();
        double h  = table.getHeight();
        double x  = b.getX();
        double y  = b.getY();
        double vx = b.getVx();
        double vy = b.getVy();

        if (x - r < 0)    { x = r;     vx =  Math.abs(vx) * WALL_RESTITUTION; }
        else if (x + r > w) { x = w - r; vx = -Math.abs(vx) * WALL_RESTITUTION; }

        if (y - r < 0)    { y = r;     vy =  Math.abs(vy) * WALL_RESTITUTION; }
        else if (y + r > h) { y = h - r; vy = -Math.abs(vy) * WALL_RESTITUTION; }

        b.setPosition(x, y);
        b.setVx(vx);
        b.setVy(vy);
    }

    private void checkBallCollisions(List<Ball> balls) {
        for (int i = 0; i < balls.size(); i++) {
            Ball a = balls.get(i);
            if (a.isPocketed()) continue;

            for (int j = i + 1; j < balls.size(); j++) {
                Ball b = balls.get(j);
                if (b.isPocketed()) continue;

                double dx      = b.getX() - a.getX();
                double dy      = b.getY() - a.getY();
                double distSq  = dx * dx + dy * dy;
                double minDist = a.getRadius() + b.getRadius();

                if (distSq == 0 || distSq >= minDist * minDist) continue;

                double dist = Math.sqrt(distSq);
                double nx   = dx / dist;
                double ny   = dy / dist;

                double overlap = (minDist - dist) * 0.5;
                a.setPosition(a.getX() - nx * overlap, a.getY() - ny * overlap);
                b.setPosition(b.getX() + nx * overlap, b.getY() + ny * overlap);

                double dvn = (a.getVx() - b.getVx()) * nx + (a.getVy() - b.getVy()) * ny;

                if (dvn > 0) {
                    if (!anyBallHit) {
                        if (a.getType() == Ball.BallType.WHITE) {
                            firstContactType = b.getType();
                            anyBallHit = true;
                        } else if (b.getType() == Ball.BallType.WHITE) {
                            firstContactType = a.getType();
                            anyBallHit = true;
                        }
                    }

                    double impulse = dvn * (1 + BALL_RESTITUTION) * 0.5;
                    a.setVx(a.getVx() - impulse * nx);
                    a.setVy(a.getVy() - impulse * ny);
                    b.setVx(b.getVx() + impulse * nx);
                    b.setVy(b.getVy() + impulse * ny);
                }
            }
        }
    }

    private void checkPockets(List<Ball> balls) {
        double threshold = table.getPocketRadius() * POCKET_THRESHOLD;
        for (Ball b : balls) {
            if (b.isPocketed()) continue;
            for (Point pocket : table.getPockets()) {
                double dx   = pocket.getX() - b.getX();
                double dy   = pocket.getY() - b.getY();
                double dist = Math.sqrt(dx * dx + dy * dy);
                if (dist < threshold) {
                    b.setStatus(Ball.BallStatus.POCKETED);
                    b.setVx(0);
                    b.setVy(0);
                    break;
                }
            }
        }
    }

    private void clampToBounds(Ball b) {
        double r = b.getRadius();
        double w = table.getWidth();
        double h = table.getHeight();
        double x = b.getX();
        double y = b.getY();

        if (x - r < 0)    { b.setVx( Math.abs(b.getVx()) * WALL_RESTITUTION); x = r; }
        else if (x + r > w) { b.setVx(-Math.abs(b.getVx()) * WALL_RESTITUTION); x = w - r; }

        if (y - r < 0)    { b.setVy( Math.abs(b.getVy()) * WALL_RESTITUTION); y = r; }
        else if (y + r > h) { b.setVy(-Math.abs(b.getVy()) * WALL_RESTITUTION); y = h - r; }

        b.setPosition(x, y);
    }
}
