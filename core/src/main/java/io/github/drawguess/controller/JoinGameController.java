package io.github.drawguess.controller;

import com.badlogic.gdx.Gdx;
import io.github.drawguess.DrawGuessMain;
import io.github.drawguess.manager.GameManager;
import io.github.drawguess.manager.SocketManager;
import io.github.drawguess.model.GameSession;
import io.github.drawguess.model.Player;
import io.github.drawguess.view.LobbyScreen;
import io.github.drawguess.view.WaitingScreen;
import io.github.drawguess.factory.PlayerFactory;

public class JoinGameController {
    private final DrawGuessMain game;

    public JoinGameController(DrawGuessMain game) {
        this.game = game;
    }

    public void tryJoinGame(String pin, String playerName) {
        if (pin == null || pin.trim().isEmpty()) {
            Gdx.app.log("JoinGameController", "PIN is empty");
            return;
        }
    
        Gdx.app.log("JoinGameController", "Trying to join game with PIN: " + pin);
    
        // 1. Opprett Player
        Player player = PlayerFactory.createPlayer(playerName);
    
        // 2. Lag ny GameSession med dummy host
        GameSession session = new GameSession(player);
        GameManager.getInstance().setSession(session);
        GameManager.getInstance().setPlayerId(player.getId());
        session.setGameId(pin);
    
        // 3. Join Firebase
        game.getFirebase().joinGame(pin, playerName);
    
        // 🔍 Debug log
        Gdx.app.log("JoinGameController", "Setting up listener and connecting...");
    
        // 4. Listener før vi bytter skjerm
        SocketManager.getInstance().setLobbyUpdateListener(playerNames -> {
            for (String name : playerNames) {
                if (!session.getPlayers().stream().anyMatch(p -> p.getName().equals(name))) {
                    Player newPlayer = PlayerFactory.createPlayer(name);
                    session.addPlayer(newPlayer);
                }
            }
        });
    
        // 5. Connect socket, så emit når klar
        SocketManager.getInstance().connect(() -> {
            SocketManager.getInstance().emitJoinLobby(playerName, pin);
        });
    
        // 🔍 Debug log
        Gdx.app.log("JoinGameController", "Navigating to lobby screen");
    
        // 6. Gå til lobby
        game.setScreen(new LobbyScreen(game));
    }
    
    
}
