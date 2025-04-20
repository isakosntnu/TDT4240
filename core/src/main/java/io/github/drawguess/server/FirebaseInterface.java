package io.github.drawguess.server;

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

    
    
    void setPlayerFinished(String gameId, String playerId, String drawingUrl, String word);
    interface SuccessCallback<T> {
        void onSuccess(T result);
    }

    interface FailureCallback {
        void onFailure(Exception e);
    }

    public void getPlayerWord(String gameId, String playerId,
                              SuccessCallback<String> onSuccess,
                              FailureCallback onError);
}
