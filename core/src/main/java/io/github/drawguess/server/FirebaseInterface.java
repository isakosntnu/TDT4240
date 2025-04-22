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

    void getPlayersWithStatus(String gameId, SuccessCallback<Map<String, Boolean>> successCallback, FailureCallback failureCallback);


    void getDrawingsForGuessing(String gameId,
                                String myPlayerId,
                                SuccessCallback<Map<String,String>> onSuccess,
                                FailureCallback onError);


    void submitGuessResult(String gameId,
                           String playerId,
                           int points,
                           Runnable onSuccess,
                           FailureCallback onError);


    void setPlayerGuessDone(String gameId,
                            String playerId,
                            Runnable onSuccess,
                            FailureCallback onError);


    void getPlayersGuessStatus(String gameId,
                               SuccessCallback<Map<String,Boolean>> onSuccess,
                               FailureCallback onError);


    void getPlayersWithScores(String gameId,
                             SuccessCallback<Map<String,Integer>> onSuccess,
                             FailureCallback onError);


    void getAllPlayerProfiles(String gameId,
                             SuccessCallback<List<Map<String,Object>>> onSuccess,
                             FailureCallback onError);


    void deleteGameData(String gameId,
                        Runnable onSuccess,
                        FailureCallback onError);


    void checkGameExists(String gameId,
                         SuccessCallback<Boolean> onSuccess,
                         FailureCallback onError);
    interface SuccessCallback<T> {
        void onSuccess(T result);
    }

    interface FailureCallback {
        void onFailure(Exception e);
    }
}
