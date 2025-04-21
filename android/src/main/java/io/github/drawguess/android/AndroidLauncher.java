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
import com.badlogic.gdx.Gdx;

public class AndroidLauncher extends AndroidApplication {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        config.useImmersiveMode = true;

        // 1. Init Socket.IO-tilkobling
        SocketManager.init();

        // 2. Start LibGDX-spillet med Firebase-backend
        initialize(new DrawGuessMain(new AndroidFirebase()), config);

        // 3. Hent Socket-instans
        Socket socket = SocketManager.getSocket();

        // 4. Sett opp lytter for "userJoined"-event fra Socket.IO
        socket.on("userJoined", args -> {
            if (args.length > 0 && args[0] instanceof JSONObject) {
                JSONObject data = (JSONObject) args[0];
                String newPlayer = data.optString("username", "Unknown");

                Log.d("SOCKET", "🎉 New player joined: " + newPlayer);

                // 5. Kjør på UI-tråd
                Gdx.app.postRunnable(() -> {
                    LobbyScreen.onPlayerJoined(newPlayer);
                });

            } else {
                Log.w("SOCKET", "⚠️ userJoined-event mottatt uten gyldig data");
            }
        });

        // (Valgfritt) Andre event listeners kan defineres her
    }
}
