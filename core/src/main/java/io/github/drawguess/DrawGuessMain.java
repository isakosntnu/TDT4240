package io.github.drawguess;

import com.badlogic.gdx.Game;
import io.github.drawguess.server.FirebaseInterface;
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
        this.setScreen(new MenuScreen(this));
    }
}
