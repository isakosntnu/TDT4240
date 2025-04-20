package io.github.drawguess;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;

import io.github.drawguess.manager.SocketManager;
import io.github.drawguess.services.FirebaseInterface;
import io.github.drawguess.view.MenuScreen;

public class DrawGuessMain extends Game {
    private final FirebaseInterface firebase;

    public DrawGuessMain(FirebaseInterface firebase) {
        this.firebase = firebase;
    }

    public FirebaseInterface getFirebase() {
        return firebase;
    }

    @Override
    public void create() {
        SocketManager.getInstance().connect(() -> {
            Gdx.app.log("Main", "✅ Connected callback triggered");
    
            // Bare for test: simuler en join
            SocketManager.getInstance().emitJoinLobby("Benjamin", "123456");
        });
    
        setScreen(new MenuScreen(this));
    }
    

}
