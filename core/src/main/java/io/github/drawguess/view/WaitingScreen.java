package io.github.drawguess.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import io.github.drawguess.DrawGuessMain;
import io.github.drawguess.manager.GameManager;
import io.github.drawguess.model.GameSession;
import io.github.drawguess.model.Player;

import java.util.*;
import java.util.List;
import java.util.Objects; // Import Objects for deep equality check

public class WaitingScreen implements Screen {

    private final DrawGuessMain game;
    private final Stage stage;
    private final GameSession session;

    private Texture backgroundTexture;
    private Image backgroundImage;
    private Texture loadingTexture;
    private Image loadingImage;

    private Table playerTable;
    private Table rootTable;
    private Table loadingTable;
    private Map<String, Label> statusLabels; // playerId → Label
    private Map<String, Boolean> previousPlayerStatuses; // Store previous statuses

    private Label messageLabel;
    private Label countdownLabel;

    private float updateTimer = 0f;
    private static final float UPDATE_INTERVAL = 0.5f;


    private boolean allFinished = false;
    private int pauseTimeLeft;
    private Timer.Task pauseTask;

    public WaitingScreen(DrawGuessMain game) {
        this.game = game;
        this.stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        this.session = GameManager.getInstance().getSession();
        this.statusLabels = new HashMap<>();
        this.previousPlayerStatuses = new HashMap<>(); 

        Skin skin = new Skin(Gdx.files.internal("uiskin.json"));
        float sh = Gdx.graphics.getHeight();
        float sw = Gdx.graphics.getWidth(); 
        float baseFontScale = sh * 0.0015f; 

        backgroundTexture = new Texture("board.png");
        backgroundImage = new Image(backgroundTexture);
        backgroundImage.setFillParent(true);
        stage.addActor(backgroundImage);

        loadingTexture = new Texture("canvas.png");
        loadingImage = new Image(loadingTexture);
        loadingImage.setFillParent(true);
        loadingImage.setVisible(false); 
        stage.addActor(loadingImage);

        rootTable = new Table();
        rootTable.setFillParent(true);
        rootTable.top().padTop(sh * 0.2f);
        stage.addActor(rootTable);


        Label titleLabel = new Label("WAITING FOR ALL PLAYERS", skin);
        titleLabel.setFontScale(baseFontScale * 1.1f); 
        rootTable.add(titleLabel).padBottom(sh * 0.04f).row();


        playerTable = new Table();
        rootTable.add(playerTable).padBottom(sh * 0.04f).row();


        messageLabel = new Label("Waiting for all players to finish guessing...", skin);
        messageLabel.setFontScale(baseFontScale);
        rootTable.add(messageLabel).padBottom(sh * 0.03f).row();

        loadingTable = new Table();
        loadingTable.setFillParent(true);
        loadingTable.center();
        loadingTable.setVisible(false);
        stage.addActor(loadingTable);

        countdownLabel = new Label("", skin);
        countdownLabel.setFontScale(baseFontScale * 2.0f);
        loadingTable.add(countdownLabel);

        // start polling 
        updatePlayerStatuses();
    }


    private void addPlayerRow(String playerId, String displayName, boolean isFinished) {
        Skin skin = new Skin(Gdx.files.internal("uiskin.json"));
        float scale = Gdx.graphics.getHeight() * 0.0018f; 
        float horizontalPadding = Gdx.graphics.getWidth() * 0.05f; 

        Label name = new Label(displayName, skin);
        name.setFontScale(scale);
        Label status = new Label(isFinished ? "DONE" : "GUESSING", skin);
        status.setFontScale(scale);

        statusLabels.put(playerId, status);
        playerTable.add(name).padRight(horizontalPadding).left();
        playerTable.add(status).right().row(); 
    }

    private void updatePlayerStatuses() {
        game.getFirebase().getPlayersGuessStatus(
                session.getGameId(),
                this::onStatusReceived,
                err -> Gdx.app.error("WaitingScreen", "Could not fetch guess status", err)
        );
    }

    private void onStatusReceived(Map<String, Boolean> currentPlayerStatuses) {
        if (Objects.equals(previousPlayerStatuses, currentPlayerStatuses)) {
            Gdx.app.debug("WaitingScreen", "No status change detected, skipping UI update.");
            return; // No change, no need to update UI
        }

        previousPlayerStatuses = new HashMap<>(currentPlayerStatuses); 

        Gdx.app.postRunnable(() -> {
            // Skip if we're already in countdown
            if (allFinished) return;

            int totalPlayers = currentPlayerStatuses.size();

            // Clear player table to rebuild it
            playerTable.clear();
            statusLabels.clear();

            Gdx.app.debug("WaitingScreen", "Status changed! Checking statuses for " + totalPlayers + " players from Firebase:");

            // Get all player names from Firebase at once
            String gameId = session.getGameId();
            game.getFirebase().getAllPlayerProfiles(
                gameId,
                playerProfiles -> {
                    // Create a map of player IDs to names for easy lookup
                    Map<String, String> playerNames = new HashMap<>();
                    for (Map<String, Object> profile : playerProfiles) {
                        String id = (String) profile.get("id");
                        String name = (String) profile.get("name");
                        if (id != null && name != null) {
                            playerNames.put(id, name);
                        }
                    }

                    // Now update the UI with player names and statuses
                    Gdx.app.postRunnable(() -> {
                        int countFinished = 0; 
                        for (Map.Entry<String, Boolean> entry : currentPlayerStatuses.entrySet()) {
                            String pid = entry.getKey();
                            boolean done = entry.getValue();

                            String playerName = playerNames.getOrDefault(pid, pid);

                            addPlayerRow(pid, playerName, done);

                            if (done) {
                                countFinished++; 
                            }
                        }

                        // Update the message and check if all are finished
                        updateCompletionStatus(currentPlayerStatuses.size(), countFinished);
                    });
                },
                error -> {
                    // Fallback to using just player IDs if we can't get names
                    Gdx.app.error("WaitingScreen", "Failed to get player profiles: " + error.getMessage());
                    Gdx.app.postRunnable(() -> {
                        int countFinished = 0; 
                        for (Map.Entry<String, Boolean> entry : currentPlayerStatuses.entrySet()) {
                            String pid = entry.getKey();
                            boolean done = entry.getValue();

                            Player player = session.getPlayerById(pid);
                            String displayName = (player != null) ? player.getName() : pid;

                            addPlayerRow(pid, displayName, done);

                            if (done) {
                                countFinished++; 
                            }
                        }

                        updateCompletionStatus(currentPlayerStatuses.size(), countFinished);
                    });
                }
            );
        });
    }
    
    /** Updates the completion status message and checks if all players are done */
    private void updateCompletionStatus(int totalPlayers, int finishedPlayers) {
        messageLabel.setText("Players done (" + finishedPlayers + "/" + totalPlayers + ")");
        
        boolean nowAll = (finishedPlayers == totalPlayers);
        

        if (nowAll && totalPlayers > 0 && !allFinished) {
            Gdx.app.log("WaitingScreen", "All players (" + totalPlayers + ") have finished guessing! Starting countdown...");
            allFinished = true;
            startPauseCountdown();
        } else if (!allFinished) {
            Gdx.app.debug("WaitingScreen", finishedPlayers + "/" + totalPlayers + " players finished. Waiting...");
        }
    }


    private void startPauseCountdown() {
        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                pauseTimeLeft = 5;
                
                // Show loading screen and hide player information
                backgroundImage.setVisible(false);
                rootTable.setVisible(false);
                loadingImage.setVisible(true);
                loadingTable.setVisible(true);
                
                countdownLabel.setText("Results in " + pauseTimeLeft + "s");

                pauseTask = new Timer.Task() {
                    @Override public void run() {
                        pauseTimeLeft--;
                        if (pauseTimeLeft > 0) {
                            countdownLabel.setText("Results in " + pauseTimeLeft + "s");
                        } else {
                            cancel();
                            goToLeaderboard();
                        }
                    }
                };
                Timer.schedule(pauseTask, 1, 1, pauseTimeLeft);
            }
        }, 2); 
    }


    private void goToLeaderboard() {
        countdownLabel.setText("Loading results...");
        game.setScreen(new LeaderboardScreen(game));
    }

    @Override
    public void render(float delta) {
        if (!allFinished) {
            updateTimer += delta;
            if (updateTimer >= UPDATE_INTERVAL) {
                updateTimer = 0f;
                updatePlayerStatuses();
            }
        }
        stage.act(delta);
        stage.draw();
    }

    @Override public void resize(int w, int h) { stage.getViewport().update(w, h, true); }
    @Override public void show() {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() { dispose(); }
    @Override
    public void dispose() {
        stage.dispose();
        backgroundTexture.dispose();
        if (loadingTexture != null) loadingTexture.dispose();
    }
} 