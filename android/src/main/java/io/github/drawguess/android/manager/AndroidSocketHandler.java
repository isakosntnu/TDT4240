package io.github.drawguess.android.manager;


import android.util.Log;

import org.json.JSONObject;
import com.badlogic.gdx.Gdx;

import io.github.drawguess.DrawGuessMain;
import io.github.drawguess.manager.GameManager;
import io.github.drawguess.server.SocketInterface;
import io.github.drawguess.view.LobbyScreen;
import io.github.drawguess.view.GuessingScreen;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class AndroidSocketHandler implements SocketInterface {

    private final Socket socket = SocketManager.getSocket();

    private final Emitter.Listener onUserJoined = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            if (args.length > 0 && args[0] instanceof JSONObject) {
                JSONObject json = (JSONObject) args[0];
                String username = json.optString("username", "Unknown");
                LobbyScreen.onPlayerJoined(username);
            }
        }
    };

    private final Emitter.Listener onJoinRejected = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            if (args.length > 0 && args[0] instanceof JSONObject) {
                JSONObject json = (JSONObject) args[0];
                String reason = json.optString("reason", "Could not join game.");
                LobbyScreen.onJoinRejected(reason);
            }
        }
    };

    private final Emitter.Listener onGameStarted = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Gdx.app.postRunnable(() -> LobbyScreen.onGameStarted());
        }
    };
    
    @Override
    public void registerLobbyListeners() {
        socket.on("userJoined", onUserJoined);
        socket.on("joinRejected", onJoinRejected);
        socket.on("gameStarted", onGameStarted);
        socket.on("startGuessingPhase", onStartGuessingPhase); 
    }

    
    @Override
    public void unregisterLobbyListeners() {
        socket.off("userJoined", onUserJoined);
        socket.off("joinRejected", onJoinRejected);
        socket.off("gameStarted", onGameStarted); 
        socket.off("startGuessingPhase", onStartGuessingPhase);
    }
    

    @Override
    public void emitJoinGame(String gameId, String username) {
        try {
            JSONObject data = new JSONObject();
            data.put("gameId", gameId);
            data.put("username", username);
            socket.emit("joinGame", data);
            Log.d("SOCKET", "Emit joinGame: " + username + " → " + gameId);
        } catch (Exception e) {
            Log.e("SOCKET", "Feil ved joinGame-emission", e);
        }
    }

    @Override
    public void emitStartGame(String gameId) {
        try {
            JSONObject data = new JSONObject();
            data.put("gameId", gameId);
            socket.emit("startGame", data);
            Log.d("SOCKET", "🚀 Emit startGame → " + gameId);
        } catch (Exception e) {
            Log.e("SOCKET", "❌ Feil ved startGame-emission", e);
        }
    }

    @Override
    public void emitStartGuessingPhase(String gameId) {
        try {
            JSONObject data = new JSONObject();
            data.put("gameId", gameId);
            socket.emit("startGuessingPhase", data);
            Log.d("SOCKET", "🟢 Emit startGuessingPhase → " + gameId);
        } catch (Exception e) {
            Log.e("SOCKET", "❌ Feil ved startGuessingPhase-emission", e);
        }
    }

    private final Emitter.Listener onStartGuessingPhase = new Emitter.Listener() {
        
        @Override
        public void call(Object... args) {
            Gdx.app.postRunnable(() -> {
                Log.d("SOCKET", "📨 Received startGuessingPhase!");
                DrawGuessMain game = GameManager.getGameInstance();
                String currentPlayerId = GameManager.getInstance().getPlayerId();
    
                // Første bilde som skal vises – for nå: hostens tegning
                String drawingOwnerId = GameManager.getInstance().getSession().getHostPlayer().getId();
    
                game.setScreen(new GuessingScreen(game, drawingOwnerId));
            });
        }
    };
    

    
}
