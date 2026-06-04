package team.model;

import base.IdentifiedObject;

public class Player extends IdentifiedObject {

    private String name;
    private int score;
    private int fouls;
    private Ball.BallType assignedBallType;

    public Player(int id, String name) {
        super(id);
        setName(name);
        this.score = 0;
        this.fouls = 0;
        this.assignedBallType = null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Player name cannot be empty");
        }
        this.name = name;
    }

    public int getScore() {
        return score;
    }

    public void addScore(int points) {
        if (points < 0) {
            throw new IllegalArgumentException("Score increment cannot be negative");
        }
        this.score += points;
    }

    public int getFouls() {
        return fouls;
    }

    public void incrementFouls() {
        this.fouls++;
    }

    public Ball.BallType getAssignedBallType() {
        return assignedBallType;
    }

    public void assignBallType(Ball.BallType assignedBallType) {
        this.assignedBallType = assignedBallType;
    }

    public boolean hasAssignedBallType() {
        return assignedBallType != null;
    }
}
