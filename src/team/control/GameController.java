package team.control;

import my_base.App;
import shared.ui_ports.GameUiPort;
import team.model.Ball;
import team.model.Canvas;
import team.model.GameState;
import team.model.Player;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GameController {

    private static final int BALLS_PER_TYPE = 7; // RED x7, YELLOW x7 בהגדרת השולחן

    private PhysicsEngine physicsEngine;
    private boolean isMovementPhase = false;

    private final Set<Integer> hiddenBalls      = new HashSet<>();
    private       Set<Integer> pottedBeforeShot = new HashSet<>();

    private GameState gameState;
    private Player player1;
    private Player player2;
    private boolean tableOpen = true; // לפני שיוקצו סוגי כדורים לשחקנים

    private GameUiPort uiPort() { return GameUiPort.getInstance(); }

    // ------------------------------------------------------------------ //
    //  אתחול                                                               //
    // ------------------------------------------------------------------ //
    public void startScenario() {
        Canvas canvas = App.content().canvas();
        physicsEngine = new PhysicsEngine(canvas.getTable());

        player1   = new Player(1, "Player 1");
        player2   = new Player(2, "Player 2");
        gameState = new GameState(player1, player2);
        tableOpen = true;

        for (Ball b : canvas.getBalls()) {
            uiPort().addBall(b.getId(), b.getType().name(), b.getX(), b.getY(), b.getRadius());
        }

        uiPort().log("Billiard game started!");
        refreshScoreBoard();
    }

    // ------------------------------------------------------------------ //
    //  ירייה                                                               //
    // ------------------------------------------------------------------ //
    public void strikeCueBall(double forceX, double forceY) {
        if (isMovementPhase) return;
        if (gameState.getStatus() == GameState.GameStatus.GAME_OVER) return;

        Ball cueBall = App.content().canvas().getBalls().get(0);
        if (cueBall.isPocketed()) return;

        pottedBeforeShot = new HashSet<>(hiddenBalls); // snapshot לפני הירייה
        cueBall.setVx(forceX * 0.1);
        cueBall.setVy(forceY * 0.1);
        isMovementPhase = true;
        physicsEngine.resetShotTracking();
        uiPort().log(gameState.getActivePlayer().getName() + " strikes!");
    }

    // ------------------------------------------------------------------ //
    //  לולאת פיזיקה                                                        //
    // ------------------------------------------------------------------ //
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
            evaluateShot();
        }
    }

    // ------------------------------------------------------------------ //
    //  הערכת ירייה — לב לוגיקת המשחק                                       //
    // ------------------------------------------------------------------ //
    private void evaluateShot() {
        Canvas canvas = App.content().canvas();
        Player active = gameState.getActivePlayer();

        List<Ball> newlyPotted = getNewlyPottedBalls(canvas);

        boolean cueBallPocketed = canvas.getTable().isBallInPocket();
        boolean noHit           = !physicsEngine.wasAnyBallHit();

        gameState.setFoul(cueBallPocketed || noHit);

        if (gameState.isFoul()) {
            handleFoul(active, cueBallPocketed, noHit);
        } else {
            handleNormalShot(active, newlyPotted);
        }

        refreshScoreBoard();
    }

    private void handleFoul(Player foulPlayer, boolean cueBallPocketed, boolean noHit) {
        foulPlayer.incrementFouls();
        gameState.switchTurn();

        // אם הכדור הלבן נפל — מחזירים אותו לשולחן במיקום ברירת מחדל
        if (cueBallPocketed) respawnCueBall();

        String reason = cueBallPocketed ? "הכדור הלבן נפל!" : "לא פגע בשום כדור!";
        uiPort().showMessage("עבירה!  " + foulPlayer.getName() + "  —  " + reason
                + "\nכדור ביד ל-" + gameState.getActivePlayer().getName());

        // מפעילים מצב Ball in Hand — השחקן הבא מניח את הכדור הלבן
        uiPort().setBallInHand(true);
        gameState.clearFoul();
    }

    // נקרא מה-Router כשהשחקן בוחר מיקום לכדור הלבן
    public void placeCueBall(double x, double y) {
        Canvas canvas = App.content().canvas();
        Ball cueBall  = canvas.getBalls().get(0);
        cueBall.setPosition(x, y);
        cueBall.setVx(0);
        cueBall.setVy(0);
        uiPort().updateBallPosition(cueBall.getId(), x, y);
        uiPort().setBallInHand(false);
        uiPort().log("Cue ball placed at (" + (int)x + ", " + (int)y + ") by " + gameState.getActivePlayer().getName());
    }

    private void handleNormalShot(Player active, List<Ball> newlyPotted) {
        boolean continueTurn = false;

        for (Ball b : newlyPotted) {
            if (b.getType() == Ball.BallType.WHITE) continue;

            if (b.getType() == Ball.BallType.BLACK) {
                handleEightBall(active);
                return;
            }

            // הכדור הראשון שנפל קובע את ההקצאה (שולחן פתוח)
            if (tableOpen) {
                assignBallTypes(active, b.getType());
            }

            if (active.hasAssignedBallType() && b.getType() == active.getAssignedBallType()) {
                active.addScore(1);
                continueTurn = true;
                uiPort().log(active.getName() + " scored! (" + active.getScore() + "/" + BALLS_PER_TYPE + ")");
            }
        }

        if (continueTurn) {
            uiPort().log(active.getName() + " continues...");
        } else {
            gameState.switchTurn();
            uiPort().log("Turn: " + gameState.getActivePlayer().getName());
        }
    }

    // ------------------------------------------------------------------ //
    //  כדור שחור                                                            //
    // ------------------------------------------------------------------ //
    private void handleEightBall(Player active) {
        boolean clearedOwn = !active.hasAssignedBallType()
                || countRemaining(active.getAssignedBallType()) == 0;

        if (clearedOwn) {
            gameState.setStatus(GameState.GameStatus.GAME_OVER);
            uiPort().showMessage("🎉  " + active.getName() + " ניצח!\nהכדורים ריקים + כדור שחור!");
        } else {
            gameState.setStatus(GameState.GameStatus.GAME_OVER);
            Player winner = getOpponent(active);
            uiPort().showMessage(active.getName() + " הכניס את הכדור השחור מוקדם מדי!\n"
                    + "  →  " + winner.getName() + " ניצח!");
        }
    }

    // ------------------------------------------------------------------ //
    //  הקצאת סוגי כדורים                                                   //
    // ------------------------------------------------------------------ //
    private void assignBallTypes(Player active, Ball.BallType type) {
        tableOpen = false;
        Ball.BallType opponentType = (type == Ball.BallType.RED) ? Ball.BallType.YELLOW : Ball.BallType.RED;
        active.assignBallType(type);
        getOpponent(active).assignBallType(opponentType);
        uiPort().log("Table assigned! " + active.getName() + "=" + type
                + ", " + getOpponent(active).getName() + "=" + opponentType);
    }

    // ------------------------------------------------------------------ //
    //  עזר                                                                  //
    // ------------------------------------------------------------------ //
    private List<Ball> getNewlyPottedBalls(Canvas canvas) {
        List<Ball> result = new ArrayList<>();
        for (Ball b : canvas.getBalls()) {
            if (b.isPocketed() && !pottedBeforeShot.contains(b.getId())) {
                result.add(b);
            }
        }
        return result;
    }

    private int countRemaining(Ball.BallType type) {
        int count = 0;
        for (Ball b : App.content().canvas().getBalls()) {
            if (b.getType() == type && !b.isPocketed()) count++;
        }
        return count;
    }

    private Player getOpponent(Player player) {
        return player.getId() == player1.getId() ? player2 : player1;
    }

    private void respawnCueBall() {
        Canvas canvas = App.content().canvas();
        Ball cueBall  = canvas.getBalls().get(0);
        cueBall.setPosition(150, 180);
        cueBall.setVx(0);
        cueBall.setVy(0);
        cueBall.setStatus(Ball.BallStatus.ACTIVE);
        hiddenBalls.remove(cueBall.getId());
        uiPort().addBall(cueBall.getId(), cueBall.getType().name(),
                cueBall.getX(), cueBall.getY(), cueBall.getRadius());
    }

    // ------------------------------------------------------------------ //
    //  ריסט                                                                //
    // ------------------------------------------------------------------ //
    public void resetGame() {
        isMovementPhase = false;
        hiddenBalls.clear();
        pottedBeforeShot.clear();
        tableOpen = true;

        uiPort().resetBalls();
        App.content().canvas().initCanvas();
        Canvas canvas = App.content().canvas();
        physicsEngine = new PhysicsEngine(canvas.getTable());

        player1   = new Player(1, "Player 1");
        player2   = new Player(2, "Player 2");
        gameState = new GameState(player1, player2);

        for (Ball b : canvas.getBalls()) {
            uiPort().addBall(b.getId(), b.getType().name(), b.getX(), b.getY(), b.getRadius());
        }

        uiPort().log("Game reset!");
        refreshScoreBoard();
    }

    // ------------------------------------------------------------------ //
    //  לוח תוצאות                                                          //
    // ------------------------------------------------------------------ //
    private void refreshScoreBoard() {
        if (gameState == null) return;

        String p1Type = player1.hasAssignedBallType()
                ? player1.getAssignedBallType().name() : "?";
        String p2Type = player2.hasAssignedBallType()
                ? player2.getAssignedBallType().name() : "?";

        String p1Label = "Player 1  [" + p1Type + "]  " + player1.getScore() + "/" + BALLS_PER_TYPE;
        String p2Label = "Player 2  [" + p2Type + "]  " + player2.getScore() + "/" + BALLS_PER_TYPE;

        String turn = gameState.getStatus() == GameState.GameStatus.GAME_OVER
                ? "GAME OVER" : gameState.getActivePlayer().getName();

        uiPort().updateScoreBoard(p1Label, p2Label, turn);
    }
}
