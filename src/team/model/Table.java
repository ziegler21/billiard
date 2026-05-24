package team.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Table {

    private final List<Ball> balls = new ArrayList<>();
    private final List<Point> pockets = new ArrayList<>();
    private double width;
    private double height;
    private double pocketRadius;

    public Table(double width, double height) {
        setWidth(width);
        setHeight(height);
        setPocketRadius(10);
    }

    public List<Ball> getBalls() {
        return Collections.unmodifiableList(new ArrayList<>(balls));
    }

    public void setBalls(List<Ball> balls) {
        this.balls.clear();
        if (balls == null) {
            return;
        }
        for (Ball ball : balls) {
            addBall(ball);
        }
    }

    public void addBall(Ball ball) {
        if (ball == null) {
            throw new IllegalArgumentException("Ball cannot be null");
        }
        balls.add(ball);
    }

    public Ball getBallById(int id) {
        for (Ball ball : balls) {
            if (ball.getId() == id) {
                return ball;
            }
        }
        return null;
    }

    public void removeBall(int id) {
        Ball ball = getBallById(id);
        if (ball != null) {
            balls.remove(ball);
        }
    }

    public List<Ball> getActiveBalls() {
        List<Ball> activeBalls = new ArrayList<>();
        for (Ball ball : balls) {
            if (!ball.isPocketed()) {
                activeBalls.add(ball);
            }
        }
        return Collections.unmodifiableList(activeBalls);
    }

    public List<Point> getPockets() {
        return Collections.unmodifiableList(new ArrayList<>(pockets));
    }

    public void setPockets(List<Point> pockets) {
        this.pockets.clear();
        if (pockets == null) {
            return;
        }
        for (Point pocket : pockets) {
            addPocket(pocket);
        }
    }

    public void addPocket(Point pocket) {
        if (pocket == null) {
            throw new IllegalArgumentException("Pocket cannot be null");
        }
        pockets.add(pocket);
    }

    public void addPocket(double x, double y) {
        pockets.add(new Point(pockets.size(), x, y));
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        if (width <= 0) {
            throw new IllegalArgumentException("Table width must be greater than 0");
        }
        this.width = width;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        if (height <= 0) {
            throw new IllegalArgumentException("Table height must be greater than 0");
        }
        this.height = height;
    }

    public double getPocketRadius() {
        return pocketRadius;
    }

    public void setPocketRadius(double pocketRadius) {
        if (pocketRadius <= 0) {
            throw new IllegalArgumentException("Pocket radius must be greater than 0");
        }
        this.pocketRadius = pocketRadius;
    }

    public boolean isInPocket(double x, double y) {
        for (Point pocket : pockets) {
            double dx = pocket.getX() - x;
            double dy = pocket.getY() - y;
            double distanceSquared = dx * dx + dy * dy;
            if (distanceSquared <= pocketRadius * pocketRadius) {
                return true;
            }
        }
        return false;
    }

    public boolean isOutOfBounds(double x, double y) {
        return x < 0 || y < 0 || x > width || y > height;
    }
}
