package team.model;

import base.IdentifiedObject;

public class Ball extends IdentifiedObject {

    public enum BallType {
        WHITE,
        RED,
        YELLOW,
        BLACK
    }

    public enum BallStatus {
        ACTIVE,
        POCKETED
    }

    private BallType type;
    private double x;
    private double y;
    private double radius;
    private BallStatus status;

    public Ball(int id, BallType type, double x, double y, double radius) {
        super(id);
        setType(type);
        setPosition(x, y);
        setRadius(radius);
        this.status = BallStatus.ACTIVE;
    }

    public BallType getType() {
        return type;
    }

    public void setType(BallType type) {
        if (type == null) {
            throw new IllegalArgumentException("Ball type cannot be null");
        }
        this.type = type;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public void setPosition(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        if (radius <= 0) {
            throw new IllegalArgumentException("Radius must be greater than 0");
        }
        this.radius = radius;
    }

    public BallStatus getStatus() {
        return status;
    }

    public void setStatus(BallStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Ball status cannot be null");
        }
        this.status = status;
    }

    public boolean isPocketed() {
        return status == BallStatus.POCKETED;
    }
}
