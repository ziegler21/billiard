package team.model;

public class GameState {

    public enum GameStatus {
        BEFORE_BREAK,
        IN_PLAY,
        FOUL,
        GAME_OVER
    }

    private Player[] players;
    private int activePlayerIndex;
    private GameStatus status;

    public GameState(Player firstPlayer, Player secondPlayer) {
        if (firstPlayer == null || secondPlayer == null) {
            throw new IllegalArgumentException("Players cannot be null");
        }
        this.players = new Player[] { firstPlayer, secondPlayer };
        this.activePlayerIndex = 0;
        this.status = GameStatus.BEFORE_BREAK;
    }

    public Player[] getPlayers() {
        return players.clone();
    }

    public void setPlayers(Player[] players) {
        if (players == null || players.length != 2 || players[0] == null || players[1] == null) {
            throw new IllegalArgumentException("GameState requires exactly two non-null players");
        }
        this.players = players.clone();
        if (activePlayerIndex >= this.players.length) {
            activePlayerIndex = 0;
        }
    }

    public Player getActivePlayer() {
        return players[activePlayerIndex];
    }

    public void setActivePlayer(Player activePlayer) {
        if (activePlayer == null) {
            throw new IllegalArgumentException("Active player cannot be null");
        }
        for (int i = 0; i < players.length; i++) {
            if (players[i].getId() == activePlayer.getId()) {
                activePlayerIndex = i;
                return;
            }
        }
        throw new IllegalArgumentException("Player is not part of this game state");
    }

    public GameStatus getStatus() {
        return status;
    }

    public void setStatus(GameStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }
        this.status = status;
    }

    public boolean switchTurn() {
        if (status == GameStatus.GAME_OVER) {
            return false;
        }
        activePlayerIndex = (activePlayerIndex + 1) % players.length;
        return true;
    }

    public GameStatus checkStatus() {
        return status;
    }

    public void resetState() {
        activePlayerIndex = 0;
        status = GameStatus.BEFORE_BREAK;
    }
}
