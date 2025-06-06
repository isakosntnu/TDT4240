package io.github.drawguess.android;

import android.os.Bundle;
import android.util.Log;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;

import io.github.drawguess.DrawGuessMain;
import io.github.drawguess.android.manager.SocketManager;
import io.github.drawguess.view.LobbyScreen;
import io.github.drawguess.android.manager.AndroidSocketHandler;
import io.github.drawguess.android.WordUploader;

import org.json.JSONObject;
import io.socket.client.Socket;
import com.badlogic.gdx.Gdx;

public class AndroidLauncher extends AndroidApplication {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        config.useImmersiveMode = true;

        // 1. Init Socket.IO
        SocketManager.init();

        // 2. GameManager init
        DrawGuessMain game = new DrawGuessMain(new AndroidFirebase(), new AndroidSocketHandler());
        io.github.drawguess.manager.GameManager.setGameInstance(game); // Denne er kritisk

        

        // 3. Start LibGDX-game
        initialize(game, config);

    }

}