// core/src/io/github/drawguess/DrawGuessMain.java
package io.github.drawguess;

import com.badlogic.gdx.Game;
import io.github.drawguess.server.FirebaseInterface;
import io.github.drawguess.server.SocketInterface;

public class DrawGuessMain extends Game {

    private final FirebaseInterface firebase;
    private final SocketInterface socket;

    public DrawGuessMain(FirebaseInterface firebase, SocketInterface socket) {
        this.firebase = firebase;
        this.socket = socket;
    }

    public FirebaseInterface getFirebase() {
        return firebase;
    }

    public SocketInterface getSocket() {
        return socket;
    }

    @Override
    public void create() {
        setScreen(new io.github.drawguess.view.MenuScreen(this));
    }

}
