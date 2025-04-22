package io.github.drawguess.lwjgl3;

import java.util.List;
import java.util.Map;

import io.github.drawguess.model.GameSession;
import io.github.drawguess.server.FirebaseCallback;
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

    @Override
    public Object getFirestore() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getFirestore'");
    }

    @Override
    public void getRandomWord(String gameId, FirebaseCallback<String> callback) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getRandomWord'");
    }

    @Override
    public void setPlayerWord(String gameId, String playerId, String word) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setPlayerWord'");
    }

    @Override
    public void getPlayersWithStatus(String gameId, SuccessCallback<Map<String, Boolean>> successCallback,
            FailureCallback failureCallback) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getPlayersWithStatus'");
    }

    @Override
    public void getDrawingsForGuessing(String gameId, String myPlayerId, SuccessCallback<Map<String, String>> onSuccess,
            FailureCallback onError) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getDrawingsForGuessing'");
    }

    @Override
    public void submitGuessResult(String gameId, String playerId, int points, Runnable onSuccess,
            FailureCallback onError) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'submitGuessResult'");
    }

    @Override
    public void setPlayerGuessDone(String gameId, String playerId, Runnable onSuccess, FailureCallback onError) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setPlayerGuessDone'");
    }

    @Override
    public void getPlayersGuessStatus(String gameId, SuccessCallback<Map<String, Boolean>> onSuccess,
            FailureCallback onError) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getPlayersGuessStatus'");
    }

    @Override
    public void getPlayersWithScores(String gameId, SuccessCallback<Map<String, Integer>> onSuccess,
            FailureCallback onError) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getPlayersWithScores'");
    }

    @Override
    public void getAllPlayerProfiles(String gameId, SuccessCallback<List<Map<String, Object>>> onSuccess,
            FailureCallback onError) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAllPlayerProfiles'");
    }

    @Override
    public void deleteGameData(String gameId, Runnable onSuccess, FailureCallback onError) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteGameData'");
    }
}
