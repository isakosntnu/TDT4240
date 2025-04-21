package io.github.drawguess.controller;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;

import io.github.drawguess.DrawGuessMain;
import io.github.drawguess.factory.PlayerFactory;
import io.github.drawguess.manager.GameManager;
import io.github.drawguess.model.GameSession;
import io.github.drawguess.model.Player;
import io.github.drawguess.server.FirebaseCallback;
import io.github.drawguess.server.FirebaseInterface;
import io.github.drawguess.view.DrawingScreen;
import io.github.drawguess.view.LobbyScreen;

public class GameController {
    private final GameManager gameManager = GameManager.getInstance();

    public void createGame(String hostPlayerName, DrawGuessMain game) {
        Player host = PlayerFactory.createHost(hostPlayerName);
        GameSession session = new GameSession(host); 
        gameManager.setSession(session);

        GameManager.getInstance().setGameController(this);
        game.getFirebase().createGame(session); // NEW GAME IN FIREBASE
        game.setScreen(new LobbyScreen(game));
    }

    public void startGame(DrawGuessMain game) {
        String gameId = GameManager.getInstance().getSession().getGameId();
        FirebaseInterface firebase = game.getFirebase();
    
        firebase.getPlayersInLobby(gameId, playerIds -> {
            for (String playerId : playerIds) {
                firebase.getRandomWord(gameId, new FirebaseCallback<String>() {
                    @Override
                    public void onSuccess(String word) {
                        firebase.setPlayerWord(gameId, playerId, word);
                    }
    
                    @Override
                    public void onFailure(Exception exception) {
                        System.out.println("❌ Klarte ikke hente ord for spiller: " + exception.getMessage());
                    }
                });
            }
    
            Gdx.app.postRunnable(() -> {
                game.setScreen(new DrawingScreen(game));
            });

    
        }, e -> {
            System.out.println("❌ Klarte ikke hente spillere: " + e.getMessage());
        });
    }
    
}
