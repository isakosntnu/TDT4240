package io.github.drawguess.android.manager;

import android.util.Log;
import java.net.URISyntaxException;
import io.socket.client.IO;
import io.socket.client.Socket;


public class SocketManager {
    private static Socket socket;

    public static void init() {
        try {
            IO.Options options = new IO.Options();
            options.reconnection = true;
            socket = IO.socket("https://drawguess-server.onrender.com", options); // ← din Render-URL
            socket.connect();

            socket.on(Socket.EVENT_CONNECT, args -> {
                Log.d("SOCKET", "✅ Tilkoblet til Socket.IO-server!");
            });

            socket.on(Socket.EVENT_DISCONNECT, args -> {
                Log.d("SOCKET", "❌ Frakoblet fra Socket.IO-server!");
            });

        } catch (URISyntaxException e) {
            Log.e("SOCKET", "⚠️ URI error: " + e.getMessage());
        }
    }

    public static Socket getSocket() {
        return socket;
    }
}