package io.github.drawguess.lwjgl3;

import io.github.drawguess.server.FirebaseInterface;

public class DesktopFirebase implements FirebaseInterface {
    @Override
    public void createGame() {
        System.out.println("[LWJGL3] createGame() called — dummy");
    }

    @Override
    public void joinGame(String gameId, String playerName) {
        System.out.println("[LWJGL3] joinGame(" + gameId + ", " + playerName + ") called — dummy");
    }

    @Override
    public void sendGuess(String guess) {
        System.out.println("[LWJGL3] sendGuess(" + guess + ") called — dummy");
    }
}
