package io.github.drawguess.android;

import android.os.Bundle;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import io.github.drawguess.DrawGuessMain;
import io.github.drawguess.android.manager.SocketManager;
import io.github.drawguess.android.manager.SocketManager;
import org.json.JSONObject;

import io.github.drawguess.view.LobbyScreen;
import io.socket.client.Socket;
import android.util.Log;


public class AndroidLauncher extends AndroidApplication {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        config.useImmersiveMode = true;

        SocketManager.init();

        initialize(new DrawGuessMain(new AndroidFirebase()), config);

        Socket socket = SocketManager.getSocket();

        socket.on("userJoined", args -> {
            if (args.length > 0) {
                JSONObject data = (JSONObject) args[0];
                String newPlayer = data.optString("username", "Unknown");
                Log.d("SOCKET", "🎉 New player joined: " + newPlayer);

                LobbyScreen.onPlayerJoined(newPlayer);
            }
        });

    }
}