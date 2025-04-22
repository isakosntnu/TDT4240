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




    // Henter alle tegninger for gjett‐runde (unntatt deg selv)
    void getDrawingsForGuessing(String gameId,
                                String myPlayerId,
                                SuccessCallback<Map<String,String>> onSuccess,
                                FailureCallback onError);

    // Sender poeng du fikk for ett gjett
    void submitGuessResult(String gameId,
                           String playerId,
                           int points,
                           Runnable onSuccess,
                           FailureCallback onError);

    // Marker at du er ferdig med å gjette alle bilder
    void setPlayerGuessDone(String gameId,
                            String playerId,
                            Runnable onSuccess,
                            FailureCallback onError);

    // Hent alle spilleres gjett‐status (true = ferdig)
    void getPlayersGuessStatus(String gameId,
                               SuccessCallback<Map<String,Boolean>> onSuccess,
                               FailureCallback onError);

    // Hent alle spilleres poeng
    void getPlayersWithScores(String gameId,
                             SuccessCallback<Map<String,Integer>> onSuccess,
                             FailureCallback onError);

    // Hent alle spilleres profiler (navn og poeng)
    void getAllPlayerProfiles(String gameId,
                             SuccessCallback<List<Map<String,Object>>> onSuccess,
                             FailureCallback onError);


    interface SuccessCallback<T> {
        void onSuccess(T result);
    }

    interface FailureCallback {
        void onFailure(Exception e);
    }
}
