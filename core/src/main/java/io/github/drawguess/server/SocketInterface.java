
package io.github.drawguess.server;

public interface SocketInterface {
    void registerLobbyListeners();
    void unregisterLobbyListeners();
    void emitJoinGame(String gameId, String username);
    void emitStartGame(String gameId);

}
