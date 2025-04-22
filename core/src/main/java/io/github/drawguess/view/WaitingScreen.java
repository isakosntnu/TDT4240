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

    // —— AUTO‑ADVANCE FIELDS ——
    private boolean allFinished = false;
    private int pauseTimeLeft;
    private Timer.Task pauseTask;

    public WaitingScreen(DrawGuessMain game) {
        this.game = game;
        this.stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        this.session = GameManager.getInstance().getSession();
        this.statusLabels = new HashMap<>();
        this.previousPlayerStatuses = new HashMap<>(); // Initialize the map

        Skin skin = new Skin(Gdx.files.internal("uiskin.json"));
        float sh = Gdx.graphics.getHeight();

        // 1) Waiting background
        backgroundTexture = new Texture("board.png");
        backgroundImage = new Image(backgroundTexture);
        backgroundImage.setFillParent(true);
        stage.addActor(backgroundImage);

        // 2) Loading background (initially invisible)
        loadingTexture = new Texture("canvas.png");
        loadingImage = new Image(loadingTexture);
        loadingImage.setFillParent(true);
        loadingImage.setVisible(false); // Hide initially
        stage.addActor(loadingImage);

        // 3) Root layout for player status
        rootTable = new Table();
        rootTable.setFillParent(true);
        rootTable.top().padTop(sh * 0.12f);
        stage.addActor(rootTable);

        // 4) Title
        Label titleLabel = new Label("WAITING FOR ALL PLAYERS", skin);
        titleLabel.setFontScale(1.8f);
        rootTable.add(titleLabel).padBottom(30).row();

        // 5) Tabell over spillere + status
        playerTable = new Table();
        rootTable.add(playerTable).padBottom(30).row();

        // 6) Meldings‑felt under
        messageLabel = new Label("Waiting for all players to finish guessing...", skin);
        messageLabel.setFontScale(1.2f);
        rootTable.add(messageLabel).padBottom(20).row();

        // 7) Loading overlay with countdown (initially invisible)
        loadingTable = new Table();
        loadingTable.setFillParent(true);
        loadingTable.center();
        loadingTable.setVisible(false);
        stage.addActor(loadingTable);

        countdownLabel = new Label("", skin);
        countdownLabel.setFontScale(3f);
        loadingTable.add(countdownLabel);

        // start polling umiddelbart
        updatePlayerStatuses();
    }

    /** Legger til én rad i tabellen. */
    private void addPlayerRow(String playerId, String displayName, boolean isFinished) {
        Skin skin = new Skin(Gdx.files.internal("uiskin.json"));
        float scale = Gdx.graphics.getHeight() * 0.0015f;

        Label name = new Label(displayName, skin);
        name.setFontScale(scale);
        Label status = new Label(isFinished ? "✅ DONE" : "⏳ GUESSING", skin);
        status.setFontScale(scale);

        statusLabels.put(playerId, status);
        playerTable.add(name).padRight(40).left();
        playerTable.add(status).right().row();
    }

    /** Oppdaterer én spiller‑rad hvis den finnes. */
    private void updatePlayerStatus(String playerId, boolean isFinished) {
        Label lbl = statusLabels.get(playerId);
        if (lbl != null) {
            lbl.setText(isFinished ? "✅ DONE" : "⏳ GUESSING");
        }
    }

    /** Henter status for alle spillere fra Firebase. */
    private void updatePlayerStatuses() {
        game.getFirebase().getPlayersGuessStatus(
                session.getGameId(),
                this::onStatusReceived,
                err -> Gdx.app.error("WaitingScreen", "Could not fetch guess status", err)
        );
    }

    /** Callback når spillerstatus er hentet. */
    private void onStatusReceived(Map<String, Boolean> currentPlayerStatuses) {
        // Check if the statuses have actually changed since the last update
        if (Objects.equals(previousPlayerStatuses, currentPlayerStatuses)) {
            Gdx.app.debug("WaitingScreen", "No status change detected, skipping UI update.");
            return; // No change, no need to update UI
        }

        // Statuses have changed, update the stored map
        previousPlayerStatuses = new HashMap<>(currentPlayerStatuses); // Store a copy

        Gdx.app.postRunnable(() -> {
            // Skip if we're already in countdown
            if (allFinished) return;

            // Use the size of the map from Firebase as the total player count
            int totalPlayers = currentPlayerStatuses.size();

            // Clear player table to rebuild it
            playerTable.clear();
            statusLabels.clear();

            // Debug log - print the number of players found in Firebase
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
                        int countFinished = 0; // Create a new counter inside this lambda
                        // Use the currentPlayerStatuses map received by this method
                        for (Map.Entry<String, Boolean> entry : currentPlayerStatuses.entrySet()) {
                            String pid = entry.getKey();
                            boolean done = entry.getValue();

                            // Get player name from the profiles we fetched
                            String playerName = playerNames.getOrDefault(pid, pid);

                            // Add to the UI
                            addPlayerRow(pid, playerName, done);

                            if (done) {
                                countFinished++; // Use the local counter
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
                        int countFinished = 0; // Create a new counter inside this lambda
                        // Use the currentPlayerStatuses map received by this method
                        for (Map.Entry<String, Boolean> entry : currentPlayerStatuses.entrySet()) {
                            String pid = entry.getKey();
                            boolean done = entry.getValue();

                            // Fallback to player ID if we can't get the name
                            Player player = session.getPlayerById(pid);
                            String displayName = (player != null) ? player.getName() : pid;

                            addPlayerRow(pid, displayName, done);

                            if (done) {
                                countFinished++; // Use the local counter
                            }
                        }

                        // Update message and check if all are finished
                        updateCompletionStatus(currentPlayerStatuses.size(), countFinished);
                    });
                }
            );
        });
    }
    
    /** Updates the completion status message and checks if all players are done */
    private void updateCompletionStatus(int totalPlayers, int finishedPlayers) {
        // Update the message to show progress based on Firebase data
        messageLabel.setText("Waiting for all players to finish guessing... (" + finishedPlayers + "/" + totalPlayers + ")");
        
        // Check if all are finished
        boolean nowAll = (finishedPlayers == totalPlayers);
        
        // Only proceed when ALL players reported by Firebase have finished
        if (nowAll && totalPlayers > 0 && !allFinished) {
            Gdx.app.log("WaitingScreen", "All players (" + totalPlayers + ") have finished guessing! Starting countdown...");
            allFinished = true;
            startPauseCountdown();
        } else if (!allFinished) {
            // Optional: Log if not all finished yet
            Gdx.app.debug("WaitingScreen", finishedPlayers + "/" + totalPlayers + " players finished. Waiting...");
        }
    }

    /** Teller ned 5 sek før neste skjerm. */
    private void startPauseCountdown() {
        // Add a short delay to make sure all Firebase updates are processed
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
        }, 2); // 2-second delay before starting countdown
    }

    /** Go to leaderboard when all players are done. */
    private void goToLeaderboard() {
        countdownLabel.setText("Loading results...");
        game.setScreen(new LeaderboardScreen(game));
    }

    @Override
    public void render(float delta) {
        // Update player statuses regularly, but not in countdown mode
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