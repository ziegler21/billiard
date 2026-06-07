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

        balls = new ArrayList<>();
        int nextId = 0;

        // מיקום הכדור הלבן (באזור "קו הראש" של השולחן, משמאל)
        balls.add(createBall(nextId++, Ball.BallType.WHITE, 150, 180));

        // הגדרות הפירמידה
        double rackTipX = 520; // קודקוד הפירמידה הפונה שמאלה
        double rackCenterY = 180; // מרכז השולחן בציר האנכי
        double R = 12; // רדיוס הכדור
        
        // המרחק האופקי בין השורות (צפוף יותר מקוטר כדי שיישבו אחד בתוך השני)
        double rowSpacingX = R * Math.sqrt(3); 
        // המרחק האנכי בין כדורים באותה שורה
        double ballDiameter = R * 2;

        // סידור תקני של כדורי 8-Ball:
        // שחור באמצע השורה ה-3, פינות אחוריות בצבעים שונים.
        Ball.BallType[][] rows = new Ball.BallType[][] {
            { Ball.BallType.YELLOW },
            { Ball.BallType.RED, Ball.BallType.RED },
            { Ball.BallType.YELLOW, Ball.BallType.BLACK, Ball.BallType.YELLOW },
            { Ball.BallType.RED, Ball.BallType.YELLOW, Ball.BallType.RED, Ball.BallType.YELLOW },
            { Ball.BallType.RED, Ball.BallType.YELLOW, Ball.BallType.RED, Ball.BallType.YELLOW, Ball.BallType.RED }
        };

        // יצירת הפירמידה
        for (int row = 0; row < rows.length; row++) {
            Ball.BallType[] rowTypes = rows[row];
            
            // ככל שמתקדמים בשורות, ה-X גדל (זזים ימינה על השולחן)
            double x = rackTipX + (row * rowSpacingX);

            for (int col = 0; col < rowTypes.length; col++) {
                // חישוב ה-Y כדי שהשורה תהיה ממורכזת סביב rackCenterY
                double y = rackCenterY + ((col - ((rowTypes.length - 1) / 2.0)) * ballDiameter);
                balls.add(createBall(nextId++, rowTypes[col], x, y));
            }
        }

        table.setBalls(balls);
        points = table.getPockets().toArray(new Point[0]);
        circles = new Circle[balls.size()];

        // המרת נתוני המודל לאובייקטי הציור של ה-UI
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