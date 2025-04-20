package io.github.drawguess.controller;

import io.github.drawguess.DrawGuessMain;
import io.github.drawguess.factory.PlayerFactory;
import io.github.drawguess.manager.GameManager;
import io.github.drawguess.model.GameSession;
import io.github.drawguess.model.Player;
import io.github.drawguess.model.WordBank;
import io.github.drawguess.view.LobbyScreen;


// GameController.java
public class GameController {
    private final GameManager gameManager = GameManager.getInstance();
    private WordBank wordBank;

    public void createGame(String hostPlayerName, DrawGuessMain game) {
        Player host = PlayerFactory.createHost(hostPlayerName);
        GameSession session = new GameSession(host); 
        gameManager.setSession(session);
        this.wordBank = new WordBank();

        GameManager.getInstance().setGameController(this);
        game.getFirebase().createGame(session); // NEW GAME IN FIREBASE
        game.setScreen(new LobbyScreen(game));

    }

    public String getRandomWord() {
        return wordBank.pullRandomWord();
    }
}
