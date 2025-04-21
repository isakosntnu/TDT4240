package io.github.drawguess.lwjgl3;

import java.util.List;

import io.github.drawguess.model.GameSession;
import io.github.drawguess.server.FirebaseInterface;

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
        System.out.println("setPlayerFinished() called [desktop mock]");
    }

    @Override
    public void uploadDrawing(String gameId, String playerId, byte[] pngData, SuccessCallback<String> onSuccess,
            FailureCallback onError) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'uploadDrawing'");
    }

    @Override
    public void getPlayerDrawingUrl(String gameId, String playerId, SuccessCallback<String> onSuccess,
            FailureCallback onError) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getPlayerDrawingUrl'");
    }

    @Override
    public void setPlayerFinished(String gameId, String playerId, String drawingUrl, String word) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setPlayerFinished'");
    }

    @Override
    public void getPlayerWord(String gameId, String playerId, SuccessCallback<String> onSuccess,
            FailureCallback onError) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getPlayerWord'");
    }

    @Override
    public void emitUserJoined(String gameId, String name) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'emitUserJoined'");
    }

    @Override
    public void getPlayersInLobby(String gameId, SuccessCallback<List<String>> successCallback,
            FailureCallback failureCallback) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getPlayersInLobby'");
    }
}
