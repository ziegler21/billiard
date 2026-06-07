package team.control;

import team.model.Ball;
import team.model.Table;
import java.util.List;

public class PhysicsEngine {
    private final double FRICTION = 0.985; // מקדם חיכוך
    private Table table;

    public PhysicsEngine(Table table) {
        this.table = table;
    }

    // הפונקציה מחזירה true אם יש עדיין כדורים בתנועה, ו-false אם כולם נעצרו
    public boolean updatePhysics() {
        boolean isMoving = false;
        List<Ball> balls = table.getBalls();

        for (Ball b : balls) {
            // אם הכדור נפל לחור, לא מחשבים לו תנועה
            if (b.isPotted()) continue; 

            if (Math.abs(b.getVx()) > 0.05 || Math.abs(b.getVy()) > 0.05) {
                isMoving = true;
                
                // 1. קידום מיקום
                b.setX(b.getX() + b.getVx());
                b.setY(b.getY() + b.getVy());
                
                // 2. הפעלת חיכוך להאטה
                b.setVx(b.getVx() * FRICTION);
                b.setVy(b.getVy() * FRICTION);
                
                // 3. בדיקת התנגשויות עם הקירות
                checkWallCollisions(b);
            } else {
                // עצירה מוחלטת כדי למנוע "רעידות"
                b.setVx(0);
                b.setVy(0);
            }
        }
        
        // כאן בעתיד תתווסף הפונקציה: checkBallCollisions(balls);
        return isMoving;
    }

    private void checkWallCollisions(Ball b) {
        double r = b.getRadius();
        // פגיעה בקיר שמאלי או ימני
        if (b.getX() - r < 0 || b.getX() + r > table.getWidth()) {
            b.setVx(b.getVx() * -1); // היפוך מהירות בציר X
            b.setX(Math.max(r, Math.min(b.getX(), table.getWidth() - r))); // מניעת חדירה לקיר
        }
        // פגיעה בקיר עליון או תחתון
        if (b.getY() - r < 0 || b.getY() + r > table.getHeight()) {
            b.setVy(b.getVy() * -1); // היפוך מהירות בציר Y
            b.setY(Math.max(r, Math.min(b.getY(), table.getHeight() - r)));
        }
    }
}