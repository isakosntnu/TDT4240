package io.github.drawguess.controller;

import com.badlogic.gdx.Gdx;

import io.github.drawguess.DrawGuessMain;
import io.github.drawguess.factory.PlayerFactory;
import io.github.drawguess.manager.GameManager;
import io.github.drawguess.manager.SocketManager;
import io.github.drawguess.model.GameSession;
import io.github.drawguess.model.Player;
import io.github.drawguess.model.WordBank;
import io.github.drawguess.view.LobbyScreen;


// GameController.java
public class GameController {
    private final GameManager gameManager = GameManager.getInstance();
    private WordBank wordBank;

    public void createGame(String hostPlayerName, DrawGuessMain game) {
        Gdx.app.log("GameController", "Creating game as host: " + hostPlayerName);

        // 1. Opprett host-spiller
        Player host = PlayerFactory.createHost(hostPlayerName);

        // 2. Opprett session og lagre i GameManager
        GameSession session = new GameSession(host);
        gameManager.setSession(session);
        gameManager.setPlayerId(host.getId());
        gameManager.setGameController(this);
        this.wordBank = new WordBank();

        // 3. Lag spill i Firebase
        game.getFirebase().createGame(session);

        // 4. Lytt etter lobby-updates før bytte
        SocketManager.getInstance().setLobbyUpdateListener(playerNames -> {
            for (String name : playerNames) {
                if (!session.getPlayers().stream().anyMatch(p -> p.getName().equals(name))) {
                    Player newPlayer = PlayerFactory.createPlayer(name);
                    session.addPlayer(newPlayer);
                }
            }
        });

        // 5. Koble til socket og emit join for host
        SocketManager.getInstance().connect(() -> {
            SocketManager.getInstance().emitJoinLobby(hostPlayerName, session.getGameId());
        });

        // 6. Bytt til LobbyScreen
        Gdx.app.log("GameController", "Navigating to lobby screen...");
        game.setScreen(new LobbyScreen(game));
    }

    public String getRandomWord() {
        return wordBank.pullRandomWord();
    }
}

