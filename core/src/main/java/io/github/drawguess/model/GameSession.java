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

    // 🎯 Brukes når HOST lager spillet
    public GameSession(Player hostPlayer) {
        this.gameId = generateGameId();
        this.hostPlayer = hostPlayer;
        this.players = new ArrayList<>();
        this.players.add(hostPlayer);
        this.status = Status.WAITING_FOR_PLAYERS;
        // Initialiser alle spillere (foreløpig kun host) med "unknown"
        playerWords.put(hostPlayer.getId(), "unknown");
    }

    // ✅ Brukes når en SPILLER joiner eksisterende spill
    public GameSession(String gameId, Player hostPlayer, List<Player> players, Status status) {
        this.gameId = gameId;
        this.hostPlayer = hostPlayer;
        this.players = players;
        this.status = status;
        // existing playerWords må deserialiseres fra Firebase dersom dere bruker det
    }

    // ---------- Word Tracking ----------

    /**
     * Sett ord for en spiller (brukes internt og av PlayerController).
     */
    public void setWordForPlayer(String playerId, String word) {
        playerWords.put(playerId, word);
    }

    /**
     * Hent ord for en spiller. Returnerer "unknown" om ikke funnet.
     */
    public String getWordForPlayer(String playerId) {
        return playerWords.getOrDefault(playerId, "unknown");
    }

    // Intern metode for å lage 6-sifret PIN
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
        // Når ny spiller legges til, initialiser med unknown
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
