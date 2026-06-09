package shared.ui_ports;

public abstract class GameUiPort {

    private static GameUiPort instance;

    public static void setInstance(GameUiPort ui) {
        if (instance != null) throw new IllegalStateException("GameUiPort instance already set");
        instance = ui;
    }

    public static GameUiPort getInstance() {
        if (instance == null) throw new IllegalStateException("GameUiPort instance not set yet");
        return instance;
    }

    // פקודות UI המותאמות למשחק ביליארד
    public abstract void addBall(int id, String ballType, double x, double y, double radius);
    public abstract void updateBallPosition(int id, double x, double y);
    public abstract void hideBall(int id); // להעלמת כדור שנפל לחור
    public abstract void updateScoreBoard(String turn, int p1Score, int p2Score); // לעדכון התוצאות
    public abstract void showMessage(String message); // להקפצת הודעת "עבירה!" או "ניצחון!"
    public abstract void log(String message);
}