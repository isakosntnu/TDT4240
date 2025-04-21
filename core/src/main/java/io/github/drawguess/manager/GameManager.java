package io.github.drawguess.manager;

import io.github.drawguess.DrawGuessMain;
import io.github.drawguess.controller.GameController;
import io.github.drawguess.model.GameSession;

public class GameManager {

    private static GameManager instance;
    private String playerId;
    private GameSession session;
    private GameController gameController;

    private static DrawGuessMain gameInstance;

    public static GameManager getInstance() {
        if (instance == null) instance = new GameManager();
        return instance;
    }

    public void setPlayerId(String id)      { this.playerId = id; }
    public String getPlayerId()             { return playerId; }

    public void setSession(GameSession s)   { this.session  = s; }
    public GameSession getSession()         { return session; }

    public void setGameController(GameController controller) {
        this.gameController = controller;
    }

    public GameController getGameController() {
        return gameController;
    }

    public static void setGameInstance(DrawGuessMain game) {
        gameInstance = game;
    }

    public static DrawGuessMain getGameInstance() {
        return gameInstance;
    }

}
