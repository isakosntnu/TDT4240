package io.github.drawguess.android;

import android.os.Bundle;
import android.util.Log;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;

import io.github.drawguess.DrawGuessMain;
import io.github.drawguess.android.manager.SocketManager;
import io.github.drawguess.view.LobbyScreen;

import org.json.JSONObject;
import io.socket.client.Socket;

public class AndroidLauncher extends AndroidApplication {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        config.useImmersiveMode = true;

        // Init Socket før appen starter
        SocketManager.init();

        // Start appen med Firebase backend
        initialize(new DrawGuessMain(new AndroidFirebase()), config);

        // Lytt på sockets
        Socket socket = SocketManager.getSocket();

        socket.on("userJoined", args -> {
            if (args.length > 0 && args[0] instanceof JSONObject) {
                JSONObject data = (JSONObject) args[0];
                String newPlayer = data.optString("username", "Unknown");

                Log.d("SOCKET", "🎉 New player joined: " + newPlayer);

                // Send til LobbyScreen (hvis aktiv)
                LobbyScreen.onPlayerJoined(newPlayer);
            } else {
                Log.w("SOCKET", "⚠️ userJoined-event mottatt uten data");
            }
        });
    }
}
