package team.control;

import my_base.App;
import shared.ui_ports.GameUiPort;
import team.model.Ball;
import team.model.Canvas;

public class GameController {

    private PhysicsEngine physicsEngine;
    private boolean isMovementPhase = false;

    private GameUiPort uiPort() {
        return GameUiPort.getInstance();
    }

    public void startScenario() {
        Canvas canvas = App.content().canvas();
        physicsEngine = new PhysicsEngine(canvas.getTable());

        // שליחת כל הכדורים ל-UI
        for (Ball b : canvas.getBalls()) {
            // שולחים את סוג הכדור כמחרוזת (למשל "WHITE" או "RED") כדי שה-UI ידע איך לצבוע אותו
            uiPort().addBall(b.getId(), b.getType().name(), b.getX(), b.getY(), b.getRadius());
        }

        uiPort().log("Billiard game started! Ready for break.");
    }

    public void strikeCueBall(double forceX, double forceY) {
        // הכדור הלבן תמיד נמצא באינדקס 0 במבנה הנתונים שלנו
        Ball cueBall = App.content().canvas().getBalls().get(0);
        
        // הגדרת המהירות לפי וקטור המשיכה
        cueBall.setVx(forceX * 0.1);
        cueBall.setVy(forceY * 0.1);
        
        isMovementPhase = true;
        uiPort().log("Cue ball struck!");
    }

    // פונקציה זו תיקרא מהלולאה המחזורית (MyPeriodicLoop)
    public void updatePhysics() {
        if (physicsEngine == null || !isMovementPhase) return;

        boolean stillMoving = physicsEngine.updatePhysics();

        // עדכון ה-UI על המיקומים החדשים
        for (Ball b : App.content().canvas().getBalls()) {
            if (!b.isPotted()) {
                uiPort().updateBallPosition(b.getId(), b.getX(), b.getY());
            }
        }

        // כאשר הכל נעצר - מעבירים את השרביט ללוגיקת המשחק (GameState)
        if (!stillMoving) {
            isMovementPhase = false;
            uiPort().log("Movement stopped. Evaluating game state...");
            // TODO: לקרוא ל- GameState.evaluateShot() ולבדוק אם יש עבירות / להעביר תור
        }
    }
}