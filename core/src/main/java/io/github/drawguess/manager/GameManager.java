package io.github.drawguess.manager;

import io.github.drawguess.model.GameSession;

public class GameManager {

    private static GameManager instance;
    private String playerId;        // settes når man oppretter / joiner
    private GameSession session;

    public static GameManager getInstance() {
        if (instance == null) instance = new GameManager();
        return instance;
    }

    public void setPlayerId(String id)      { this.playerId = id; }
    public String getPlayerId()             { return playerId; }

    public void setSession(GameSession s)   { this.session  = s; }
    public GameSession getSession()         { return session; }
}


