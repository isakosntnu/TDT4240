package io.github.drawguess.manager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.ObjectMap;
import io.socket.client.IO;
import io.socket.client.Socket;

import java.net.URISyntaxException;

public class SocketManager {

    private static SocketManager instance;
    private Socket socket;

    private static final String SERVER_URL = "http://10.0.2.2:3000";


    private LobbyUpdateListener lobbyUpdateListener;

    public interface LobbyUpdateListener {
        void onLobbyUpdate(Array<String> playerNames);
    }

    private SocketManager() {
        try {
            IO.Options options = new IO.Options();
            options.transports     = new String[]{"polling"};  // tvungent polling
            options.reconnection   = true;
            options.timeout        = 10000;
    
            // ✨ Lag ÉN socket – ikke to
            socket = IO.socket(SERVER_URL, options);
    
            // (valgfritt) ekstra logging
            socket.on(Socket.EVENT_CONNECT_ERROR,   args -> Gdx.app.error("Socket", "EVENT_ERROR "+args[0]));
            socket.on("error",              args -> Gdx.app.error("Socket", "error "+args[0]));
        } catch (URISyntaxException e) {
            Gdx.app.error("SocketManager", "Invalid URI", e);
        }
    }
    

    public static SocketManager getInstance() {
        if (instance == null) {
            instance = new SocketManager();
        }
        return instance;
    }

    public void connect(Runnable onConnected) {
        if (socket == null) return;
    
        if (socket.connected()) {
            Gdx.app.log("SocketManager", "⚠️ Already connected, running callback immediately");
            Gdx.app.postRunnable(onConnected);
            return;
        }
    
        socket.connect();
        Gdx.app.log("SocketManager", "🔌 Connecting to server...");
    
        socket.on(Socket.EVENT_CONNECT, args -> {
            Gdx.app.log("SocketManager", "✅ Connected to server");
            Gdx.app.postRunnable(onConnected); // 🚀 Kjør callback
        });
    
        socket.on("connect_error", args -> {
            Gdx.app.error("SocketManager", "❌ Connection error: " + args[0]);
        });
        
    
        socket.on("connect_timeout", args -> {
            Gdx.app.error("SocketManager", "⌛ Connection timeout");
        });
        
    }
    
    

    public void emitJoinLobby(String playerName, String gameId) {
        if (socket == null || !socket.connected()) {
            Gdx.app.error("SocketManager", "Cannot emit, socket is not connected");
            return;
        }
    
        // Send direkte Java Map, ikke string!
        ObjectMap<String, Object> payload = new ObjectMap<>();
        payload.put("playerName", playerName);
        payload.put("gameId", gameId);
    
        socket.emit("join-lobby", payload);
        Gdx.app.log("SocketManager", "📤 Emitted join-lobby with player: " + playerName + " and gameId: " + gameId);
    }
    

    public void setLobbyUpdateListener(LobbyUpdateListener listener) {
        this.lobbyUpdateListener = listener;
    }

    public void disconnect() {
        if (socket != null && socket.connected()) {
            socket.disconnect();
            Gdx.app.log("SocketManager", "🔌 Disconnected");
        }
    }

    public boolean isConnected() {
        return socket != null && socket.connected();
    }

    public void connect() {
        connect(() -> {}); // Kall den andre connect-metoden med en tom callback
    }
    
}
