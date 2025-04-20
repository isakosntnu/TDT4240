package io.github.drawguess.controller;

import io.github.drawguess.DrawGuessMain;
import io.github.drawguess.factory.PlayerFactory;
import io.github.drawguess.manager.GameManager;
import io.github.drawguess.model.GameSession;
import io.github.drawguess.model.Player;
import io.github.drawguess.view.LobbyScreen;


// GameController.java
public class GameController {
    private final GameManager gameManager = GameManager.getInstance();

    public void createGame(String hostPlayerName, DrawGuessMain game) {
        Player host = PlayerFactory.createHost(hostPlayerName);
        GameSession session = new GameSession(host); 
        gameManager.setSession(session);

        game.getFirebase().createGame(session); // NEW GAME IN FIREBASE
        game.setScreen(new LobbyScreen(game));

    }
}
