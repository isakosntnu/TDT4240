package io.github.drawguess.android.manager;


import android.util.Log;

import org.json.JSONObject;
import com.badlogic.gdx.Gdx;


import io.github.drawguess.server.SocketInterface;
import io.github.drawguess.view.LobbyScreen;
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
    }
    
    @Override
    public void unregisterLobbyListeners() {
        socket.off("userJoined", onUserJoined);
        socket.off("joinRejected", onJoinRejected);
        socket.off("gameStarted", onGameStarted); 
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
            Log.e("SOCKET", "error joinGame-emission", e);
        }
    }

    @Override
    public void emitStartGame(String gameId) {
        try {
            JSONObject data = new JSONObject();
            data.put("gameId", gameId);
            socket.emit("startGame", data);
            Log.d("SOCKET", "Emit startGame → " + gameId);
        } catch (Exception e) {
            Log.e("SOCKET", "error emit startGame", e);
        }
    }
    
}
