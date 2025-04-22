package io.github.drawguess.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameSession {

    public enum Status {
        WAITING_FOR_PLAYERS,
        IN_PROGRESS,
        FINISHED
    }

    private final String gameId;
    private final Player hostPlayer;
    private final List<Player> players;
    private Status status;
    private final Map<String, String> playerWords = new HashMap<>();

    public GameSession(Player hostPlayer) {
        this.gameId = generateGameId();
        this.hostPlayer = hostPlayer;
        this.players = new ArrayList<>();
        this.players.add(hostPlayer);
        this.status = Status.WAITING_FOR_PLAYERS;
        playerWords.put(hostPlayer.getId(), "unknown");
    }


    public GameSession(String gameId, Player hostPlayer, List<Player> players, Status status) {
        this.gameId = gameId;
        this.hostPlayer = hostPlayer;
        this.players = players;
        this.status = status;
    }

    // ---------- Word Tracking ----------

    public void setWordForPlayer(String playerId, String word) {
        playerWords.put(playerId, word);
    }

    public String getWordForPlayer(String playerId) {
        return playerWords.getOrDefault(playerId, "unknown");
    }

    // Game-pin method
    private String generateGameId() {
        return String.valueOf((int)(Math.random() * 900000) + 100000);
    }

    // ---------- Getters ----------
    public String getGameId() {
        return gameId;
    }

    public Player getHostPlayer() {
        return hostPlayer;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    // ---------- Utility ----------
    public void addPlayer(Player player) {
        players.add(player);
        playerWords.put(player.getId(), "unknown");
    }

    public Player getPlayerById(String id) {
        for (Player player : players) {
            if (player.getId().equals(id)) {
                return player;
            }
        }
        return null;
    }
}
