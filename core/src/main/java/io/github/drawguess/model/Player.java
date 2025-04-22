package io.github.drawguess.model;

/** Representerer én spiller i en GameSession. */
public class Player {

    private final String id;        // Firebase‑dokument‑ID eller generert UUID
    private final String name;      // Spillernavn
    private int score;              // Poengsum
    private boolean host;           // True hvis denne spilleren er host
    private boolean finishedDrawing; // True hvis spilleren er ferdig å tegne
    private String word;


    public Player(String id, String name, int initialScore, boolean host) {
        this.id = id;
        this.name = name;
        this.score = initialScore;
        this.host = host;
        this.finishedDrawing = false;
        this.word = "x";
    }

    /* ---------- Getters / Setters ---------- */

    public String getId() { return id; }
    public String getName() { return name; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public boolean isHost() { return host; }
    public void setHost(boolean host) { this.host = host; }

    public boolean hasFinishedDrawing() { return finishedDrawing; }
    public void setFinishedDrawing(boolean finishedDrawing) {
        this.finishedDrawing = finishedDrawing;
    }

    /* ---------- Convenience ---------- */

    @Override
    public String toString() {
        return (host ? "⭐ " : "") + name + " (" + score + " pts)";
    }
}
