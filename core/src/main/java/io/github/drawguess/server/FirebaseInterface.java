package io.github.drawguess.server;

public interface FirebaseInterface {
    
    void createGame();
    void joinGame(String gameId, String playerName);
    void sendGuess(String guess);

}
