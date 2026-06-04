package team.model;

import java.util.ArrayList;
import java.util.List;

public class Canvas {
    private Table table;
    private Point[] points;
    private Circle[] circles;
    private List<Ball> balls = new ArrayList<>();

    public void initCanvas() {
        table = new Table(760, 360);
        table.setPocketRadius(18);

        List<Point> pockets = new ArrayList<>();
        // 4 corner pockets
        pockets.add(new Point(0, 30, 30));
        pockets.add(new Point(1, 770, 30));
        pockets.add(new Point(2, 30, 370));
        pockets.add(new Point(3, 770, 370));
        // 2 side pockets (middle height on left and right)
        pockets.add(new Point(4, 30, 200));
        pockets.add(new Point(5, 770, 200));
        table.setPockets(pockets);

        balls = new ArrayList<>();
        int nextId = 0;

        balls.add(createBall(nextId++, Ball.BallType.WHITE, 70, 180));

        double rackCenterX = 520;
        double rackCenterY = 180;
        double rowSpacingX = 24;
        double rowSpacingY = 24;

        Ball.BallType[][] rows = new Ball.BallType[][] {
            { Ball.BallType.BLACK },
            { Ball.BallType.RED, Ball.BallType.YELLOW },
            { Ball.BallType.RED, Ball.BallType.YELLOW, Ball.BallType.RED },
            { Ball.BallType.RED, Ball.BallType.YELLOW, Ball.BallType.RED, Ball.BallType.YELLOW },
            { Ball.BallType.YELLOW, Ball.BallType.RED, Ball.BallType.YELLOW, Ball.BallType.RED, Ball.BallType.YELLOW }
        };

        for (int row = 0; row < rows.length; row++) {
            Ball.BallType[] rowTypes = rows[row];
            double y = rackCenterY + (row * rowSpacingY);

            for (int col = 0; col < rowTypes.length; col++) {
                double x = rackCenterX + ((col - ((rowTypes.length - 1) / 2.0)) * rowSpacingX);
                balls.add(createBall(nextId++, rowTypes[col], x, y));
            }
        }

        table.setBalls(balls);
        points = table.getPockets().toArray(new Point[0]);
        circles = new Circle[balls.size()];

        for (int i = 0; i < balls.size(); i++) {
            Ball ball = balls.get(i);
            circles[i] = new Circle(ball.getId(), new Point(ball.getId(), ball.getX(), ball.getY()), ball.getRadius());
        }
    }

    private Ball createBall(int id, Ball.BallType type, double x, double y) {
        return new Ball(id, type, x, y, 12);
    }

    public Circle getCircle(int index) {
        return circles[index];
    }

    public Point getPoint(int index) {
        return points[index];
    }

    public int getCircleCount() {
        return circles.length;
    }

    public int getPointCount() {
        return points.length;
    }

    public Table getTable() {
        return table;
    }

    public List<Ball> getBalls() {
        return new ArrayList<>(balls);
    }
}
