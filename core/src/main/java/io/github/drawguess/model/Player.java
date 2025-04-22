package io.github.drawguess.model;

public class Player {

    private final String id;        
    private final String name;      
    private int score;              
    private boolean host;           
    private boolean finishedDrawing; 

    public Player(String id, String name, int initialScore, boolean host) {
        this.id = id;
        this.name = name;
        this.score = initialScore;
        this.host = host;
        this.finishedDrawing = false;
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

}
