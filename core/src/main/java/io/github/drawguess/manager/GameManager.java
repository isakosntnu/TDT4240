package io.github.drawguess.manager;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Gdx;

import io.github.drawguess.DrawGuessMain;
import io.github.drawguess.controller.GameController;
import io.github.drawguess.model.GameSession;
import io.github.drawguess.server.FirebaseInterface.FailureCallback;

public class GameManager {

    private static GameManager instance;
    private static DrawGuessMain gameInstance;

    private String playerId;
    private GameSession session;
    private GameController gameController;
    private Map<String, String> playerDrawings = new HashMap<>();

    // Singleton pattern
    public static GameManager getInstance() {
        if (instance == null) instance = new GameManager();
        return instance;
    }

    public static void setGameInstance(DrawGuessMain game) {
        gameInstance = game;
    }

    public static DrawGuessMain getGameInstance() {
        return gameInstance;
    }

    public void setPlayerId(String id)              { this.playerId = id; }
    public String getPlayerId()                     { return playerId; }

    public void setSession(GameSession s)           { this.session = s; }
    public GameSession getSession()                 { return session; }

    public void setGameController(GameController c) { this.gameController = c; }
    public GameController getGameController()       { return gameController; }

    public void setPlayerDrawings(Map<String, String> drawings) {
        this.playerDrawings = drawings;
    }

    public Map<String, String> getPlayerDrawings() {
        return playerDrawings;
    }

    public void clearPlayerDrawings() {
        playerDrawings.clear();
    }

}
