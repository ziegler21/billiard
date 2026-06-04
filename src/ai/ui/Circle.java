package ai.ui;

<<<<<<< HEAD
public class Circle {
    public int cx, cy, radius;
=======
import java.awt.Color;

public class Circle {
    public int cx, cy, radius;
    public Color color;
>>>>>>> b943467914e9b689d0b17c128dfc9d781a9c7350
    public boolean isBlinking;

    public Circle(int cx, int cy, int radius) {
        this.cx = cx;
        this.cy = cy;
        this.radius = radius;
<<<<<<< HEAD
=======
        this.color = Color.BLACK;
        this.isBlinking = false;
    }

    public Circle(int cx, int cy, int radius, Color color) {
        this.cx = cx;
        this.cy = cy;
        this.radius = radius;
        this.color = color;
>>>>>>> b943467914e9b689d0b17c128dfc9d781a9c7350
        this.isBlinking = false;
    }

    public void update(int cx, int cy, int radius) {
        this.cx = cx;
        this.cy = cy;
        this.radius = radius;
    }
<<<<<<< HEAD
=======

    public void setColor(Color color) {
        this.color = color;
    }
>>>>>>> b943467914e9b689d0b17c128dfc9d781a9c7350
}
