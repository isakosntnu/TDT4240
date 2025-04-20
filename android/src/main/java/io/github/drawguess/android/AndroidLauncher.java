package io.github.drawguess.android;

import android.os.Bundle;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import io.github.drawguess.DrawGuessMain;
import io.github.drawguess.android.manager.SocketManager;

public class AndroidLauncher extends AndroidApplication {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        config.useImmersiveMode = true;

        SocketManager.init();

        initialize(new DrawGuessMain(new AndroidFirebase()), config);
    }
}


