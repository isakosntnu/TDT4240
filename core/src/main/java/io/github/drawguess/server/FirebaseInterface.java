package io.github.drawguess.server;



import java.util.List;
import java.util.Map;

import io.github.drawguess.model.GameSession;

public interface FirebaseInterface {

    void createGame(GameSession session);

    void joinGame(String gameId, String playerName);

    void sendGuess(String guess);

    void uploadDrawing(String gameId,
                       String playerId,
                       byte[] pngData,
                       SuccessCallback<String> onSuccess,
                       FailureCallback onError);

    void getPlayerDrawingUrl(String gameId,
                             String playerId,
                             SuccessCallback<String> onSuccess,
                             FailureCallback onError);

    void setPlayerFinished(String gameId,
                            String playerId,
                            String drawingUrl,
                            String word);

    void getPlayerWord(String gameId,
                       String playerId,
                       SuccessCallback<String> onSuccess,
                       FailureCallback onError);

    void emitUserJoined(String gameId, String name);

    void getPlayersInLobby(String gameId,
                           SuccessCallback<List<String>> successCallback,
                           FailureCallback failureCallback);

    Object getFirestore();
    void getRandomWord(String gameId, FirebaseCallback<String> callback);
    void setPlayerWord(String gameId, String playerId, String word);
    public void checkIfPlayerIsHost(String gameId, SuccessCallback<Boolean> onSuccess, FailureCallback onFailure);

    void getPlayersWithStatus(String gameId, SuccessCallback<Map<String, Boolean>> successCallback, FailureCallback failureCallback);
    public void getAllPlayerDrawings(String gameId, SuccessCallback<Map<String, String>> onSuccess, FailureCallback onFailure);



    interface SuccessCallback<T> {
        void onSuccess(T result);
    }

    interface FailureCallback {
        void onFailure(Exception e);
    }
}
