package io.github.drawguess.lwjgl3;

import io.github.drawguess.model.GameSession;
import io.github.drawguess.server.FirebaseInterface;
import java.util.List;
import java.util.ArrayList;

public class DesktopFirebase implements FirebaseInterface {

    @Override
    public void createGame(GameSession session) {
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

    @Override
    public void setPlayerFinished(String gameId, String playerId, String downloadUrl) {
        System.out.println("[LWJGL3] setPlayerFinished(" + gameId + ", " + playerId + ", " + downloadUrl + ") — dummy");
    }

    @Override
    public void uploadDrawing(String gameId, String playerId, byte[] pngData, SuccessCallback<String> onSuccess,
            FailureCallback onError) {
        System.out.println("[LWJGL3] uploadDrawing() called — dummy, returning fake URL");
        onSuccess.onSuccess("https://fake-url.com/image.png");
    }

    @Override
    public void emitUserJoined(String gameId, String username) {
        System.out.println("[LWJGL3] emitUserJoined(" + gameId + ", " + username + ") called — dummy");
    }

    @Override
    public Object getFirestore() {
        System.out.println("[LWJGL3] getFirestore() called — dummy");
        return null;
    }

    @Override
    public void getPlayersInLobby(String gameId, SuccessCallback<List<String>> onSuccess, FailureCallback onError) {
        System.out.println("[LWJGL3] getPlayersInLobby(" + gameId + ") called — dummy");
        // Return dummy data for desktop
        List<String> dummyPlayers = new ArrayList<>();
        dummyPlayers.add("Player 1");
        dummyPlayers.add("Player 2");
        onSuccess.onSuccess(dummyPlayers);
    }
}
