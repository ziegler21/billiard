package team.control;

import my_base.App;
import shared.ui_ports.GameUiPort;
import team.model.Ball;
import team.model.Canvas;
import java.util.HashSet;
import java.util.Set;

public class GameController {

    private PhysicsEngine physicsEngine;
    private boolean isMovementPhase = false;
    private final Set<Integer> hiddenBalls = new HashSet<>();

    private GameUiPort uiPort() {
        return GameUiPort.getInstance();
    }

    public void startScenario() {
        Canvas canvas = App.content().canvas();
        physicsEngine = new PhysicsEngine(canvas.getTable());

        for (Ball b : canvas.getBalls()) {
            uiPort().addBall(b.getId(), b.getType().name(), b.getX(), b.getY(), b.getRadius());
        }

        uiPort().log("Billiard game started! Ready for break.");
    }

    public void strikeCueBall(double forceX, double forceY) {
        Ball cueBall = App.content().canvas().getBalls().get(0);
        cueBall.setVx(forceX * 0.1);
        cueBall.setVy(forceY * 0.1);
        isMovementPhase = true;
        uiPort().log("Cue ball struck!");
    }

    public void updatePhysics() {
        if (physicsEngine == null || !isMovementPhase) return;

        boolean stillMoving = physicsEngine.updatePhysics();

        for (Ball b : App.content().canvas().getBalls()) {
            if (b.isPocketed()) {
                if (!hiddenBalls.contains(b.getId())) {
                    hiddenBalls.add(b.getId());
                    uiPort().hideBall(b.getId());
                    uiPort().log("Ball " + b.getId() + " (" + b.getType() + ") potted!");
                }
            } else {
                uiPort().updateBallPosition(b.getId(), b.getX(), b.getY());
            }
        }

        if (!stillMoving) {
            isMovementPhase = false;
            uiPort().log("Movement stopped. Evaluating game state...");
            // TODO: GameState.evaluateShot()
        }
    }
}