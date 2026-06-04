package team.control;

import my_base.App;
import shared.ui_ports.Ex3UiPort;
import team.model.Canvas;
import team.model.Circle;
import team.model.Point;
import team.model.Table;

public class Ex3Backend {

    /**
     * Use ex3UiPort() as a function and not a variableto get the UI port
     * to avoid trying to get it before it was set up by the UI
     * (which happens at UI startup, but this backend is constructed at app
     * startup).
     */
    private Ex3UiPort ex3UiPort() {
        return Ex3UiPort.getInstance();
    }

    private boolean runPeriodic = false;

    // Called once at UI startup
    public void startScenario() {
        Canvas canvas = App.content().canvas();

        for (int i = 0; i < canvas.getCircleCount(); i++) {
            Circle c = canvas.getCircle(i);
            String ballColor = getBallColor(i);
            ex3UiPort().addCircle(c.getId(), c.getCenter().getX(), c.getCenter().getY(), c.getR(), ballColor);
        }

        for (int i = 0; i < canvas.getPointCount(); i++) {
            Point p = canvas.getPoint(i);
            ex3UiPort().addPoint(p.getId(), p.getX(), p.getY());
        }

        ex3UiPort().log("Scenario started: " + canvas.getCircleCount() + " balls + " + canvas.getPointCount() + " pocket markers.");
        evaluateAndCommandUi();
    }

    // UI input events call these via router
    public void movePoint(int pointId, double x, double y) {
        Point p = App.content().canvas().getPoint(pointId);
        p.setX(x);
        p.setY(y);
        ex3UiPort().updatePoint(pointId, x, y);
        evaluateAndCommandUi();
    }

    public void moveCircle(int circleId, double cx, double cy) {
        Circle c = App.content().canvas().getCircle(circleId);
        c.getCenter().setX(cx);
        c.getCenter().setY(cy);
        ex3UiPort().updateCircle(circleId, cx, cy, c.getR());
        evaluateAndCommandUi();
    }

    public void setCircleRadius(int circleId, double r) {
        Circle c = App.content().canvas().getCircle(circleId);
        c.setR(r);
        ex3UiPort().updateCircle(circleId, c.getCenter().getX(), c.getCenter().getY(), c.getR());
        evaluateAndCommandUi();
    }

    // Business logic: checks + UI commands
    private void evaluateAndCommandUi() {
        Canvas canvas = App.content().canvas();
        Table table = canvas.getTable();

        for (int i = 0; i < canvas.getCircleCount(); i++) {
            Circle c = canvas.getCircle(i);
            double left = c.getCenter().getX() - c.getR();
            double right = c.getCenter().getX() + c.getR();
            double top = c.getCenter().getY() - c.getR();
            double bottom = c.getCenter().getY() + c.getR();

            boolean outOfBounds = table.isOutOfBounds(left, top)
                    || table.isOutOfBounds(right, top)
                    || table.isOutOfBounds(left, bottom)
                    || table.isOutOfBounds(right, bottom);

            if (outOfBounds) {
                ex3UiPort().blinkCircle(i, 1);
            }
        }
    }

    public void moveCircle2(double dx, double dy) {
        if (!runPeriodic)
            return;
        double cx = App.content().canvas().getCircle(2).getCenter().getX() + dx;
        double cy = App.content().canvas().getCircle(2).getCenter().getY() + dy;
        moveCircle(2, cx, cy);
    }

    public void toggleRunPeriodic() {
         this.runPeriodic = !this.runPeriodic;
    }

    private String getBallColor(int ballId) {
        if (ballId == 0) {
            return "white";
        } else if (ballId == 5) {
            return "black";
        } else {
            int colorPattern = (ballId - 1) % 2;
            return colorPattern == 0 ? "red" : "yellow";
        }
    }
}