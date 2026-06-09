package ai.ui;

import java.awt.Color;

public class Circle {
    public int cx, cy, radius;
    public Color color;
    public boolean isBlinking;

    public Circle(int cx, int cy, int radius) {
        this.cx = cx;
        this.cy = cy;
        this.radius = radius;
        this.color = Color.BLACK;
        this.isBlinking = false;
    }

    public Circle(int cx, int cy, int radius, Color color) {
        this.cx = cx;
        this.cy = cy;
        this.radius = radius;
        this.color = color;
        this.isBlinking = false;
    }

    public void update(int cx, int cy, int radius) {
        this.cx = cx;
        this.cy = cy;
        this.radius = radius;
    }

    public void setColor(Color color) {
        this.color = color;
    }
}
