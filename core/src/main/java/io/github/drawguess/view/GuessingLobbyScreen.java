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
import java.util.Objects; 

public class GuessingLobbyScreen implements Screen {

    private final DrawGuessMain game;
    private final Stage stage;
    private final GameSession session;
    private final boolean isGuessPhase;

    private Texture backgroundTexture;
    private Image backgroundImage;
    private Texture loadingTexture;
    private Image loadingImage;

    private Table playerTable;
    private Table rootTable;
    private Table loadingTable;
    private Map<String, Label> statusLabels; 
    private Map<String, Boolean> previousPlayerStatuses; 

    private Label messageLabel;
    private Label countdownLabel;

    private float updateTimer = 0f;
    private static final float UPDATE_INTERVAL = 1.0f;

    // —— AUTO‑ADVANCE FIELDS ——
    private boolean allFinished = false;
    private int pauseTimeLeft;
    private Timer.Task pauseTask;


    public GuessingLobbyScreen(DrawGuessMain game, boolean isGuessPhase) {
        this.game = game;
        this.isGuessPhase = isGuessPhase;
        this.stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        this.session = GameManager.getInstance().getSession();
        this.statusLabels = new HashMap<>();
        this.previousPlayerStatuses = new HashMap<>(); 

        Skin skin = new Skin(Gdx.files.internal("uiskin.json"));
        float sh = Gdx.graphics.getHeight();
        float sw = Gdx.graphics.getWidth(); 
        float baseFontScale = sh * 0.0014f; 


        backgroundTexture = new Texture("board.png");
        backgroundImage = new Image(backgroundTexture);
        backgroundImage.setFillParent(true);
        stage.addActor(backgroundImage);


        loadingTexture = new Texture("canvas.png");
        loadingImage = new Image(loadingTexture);
        loadingImage.setFillParent(true);
        loadingImage.setVisible(false); // Hide initially
        stage.addActor(loadingImage);


        rootTable = new Table();
        rootTable.setFillParent(true);
        rootTable.top().padTop(sh * 0.2f);
        stage.addActor(rootTable);


        Label titleLabel = new Label(isGuessPhase ? "WAITING FOR GUESSES" : "WAITING FOR DRAWINGS", skin);
        titleLabel.setFontScale(baseFontScale * 1.2f); // Slightly larger title
        rootTable.add(titleLabel).padBottom(sh * 0.04f).row(); // Add title to the layout


        playerTable = new Table();
        rootTable.add(playerTable).padBottom(sh * 0.05f).row(); // Adjusted padding


        messageLabel = new Label(
                isGuessPhase ? "Waiting for all guesses…" : "Waiting for all drawings…",
                skin
        );

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
        
        // Set status text based on phase and completion
        String statusText = isGuessPhase 
                ? (isFinished ? "DONE" : "GUESSING") 
                : (isFinished ? "DONE" : "DRAWING");
        Label status = new Label(statusText, skin);
        status.setFontScale(scale); 

        statusLabels.put(playerId, status);
        playerTable.add(name).padRight(horizontalPadding).left();
        playerTable.add(status).right().row(); 
    }

    private void updatePlayerStatuses() {
        String gameId = session.getGameId();
        if (!isGuessPhase) {
            game.getFirebase().getPlayersWithStatus(
                gameId,
                this::onStatusReceived,
                err -> Gdx.app.error("GuessingLobbyScreen", "Could not fetch drawing status", err)
            );
        } else {
            game.getFirebase().getPlayersGuessStatus(
                gameId,
                this::onStatusReceived,
                err -> Gdx.app.error("GuessingLobbyScreen", "Could not fetch guess status", err)
            );
        }
    }


    private void onStatusReceived(Map<String, Boolean> currentPlayerStatuses) {
        if (Objects.equals(previousPlayerStatuses, currentPlayerStatuses)) {
             Gdx.app.debug("GuessingLobbyScreen", "No status change detected, skipping UI update.");
            return; // No change, no need to update UI
        }

        // Statuses have changed, update the stored map
        previousPlayerStatuses = new HashMap<>(currentPlayerStatuses); // Store a copy

        Gdx.app.postRunnable(() -> {
            if (allFinished) return;

            int totalPlayers = currentPlayerStatuses.size();

            // Clear the table to rebuild it
            playerTable.clear();
            statusLabels.clear();

            Gdx.app.debug("GuessingLobbyScreen", "Status changed! Updating status for " + totalPlayers + " players in phase: " + (isGuessPhase ? "Guessing" : "Drawing"));

            // Fetch player profiles to get names
            String gameId = session.getGameId();
            game.getFirebase().getAllPlayerProfiles(
                gameId,
                playerProfiles -> {
                    Map<String, String> playerNames = new HashMap<>();
                    for (Map<String, Object> profile : playerProfiles) {
                        String id = (String) profile.get("id");
                        String name = (String) profile.get("name");
                        if (id != null && name != null) {
                            playerNames.put(id, name);
                        }
                    }
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

                        updateCompletionStatus(totalPlayers, countFinished);
                    });
                },
                error -> {
                    Gdx.app.error("GuessingLobbyScreen", "Failed to get player profiles: " + error.getMessage());
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

                        updateCompletionStatus(totalPlayers, countFinished);
                    });
                }
            );
        });
    }

    /** Updates the completion status message and checks if all players are done */
    private void updateCompletionStatus(int totalPlayers, int finishedPlayers) {
        String phaseText = isGuessPhase ? "guessing" : "drawing";
        messageLabel.setText("Players done " + phaseText + "... (" + finishedPlayers + "/" + totalPlayers + ")");
        
        boolean nowAll = (finishedPlayers == totalPlayers);
        
        if (nowAll && totalPlayers > 0 && !allFinished) {
            Gdx.app.log("GuessingLobbyScreen", "All players (" + totalPlayers + ") have finished " + phaseText + "! Starting countdown...");
            allFinished = true;
            startPauseCountdown();
        } else if (!allFinished) {
            Gdx.app.debug("GuessingLobbyScreen", finishedPlayers + "/" + totalPlayers + " players finished. Waiting...");
        }
    }


    private void startPauseCountdown() {
        pauseTimeLeft = 3;
        
        // Show loading screen and hide player information
        backgroundImage.setVisible(false);
        rootTable.setVisible(false);
        loadingImage.setVisible(true);
        loadingTable.setVisible(true);
        
        countdownLabel.setText("Guessing in " + pauseTimeLeft + "s");

        pauseTask = new Timer.Task() {
            @Override public void run() {
                pauseTimeLeft--;
                if (pauseTimeLeft > 0) {
                    countdownLabel.setText("Guessing in " + pauseTimeLeft + "s");
                } else {
                    cancel();
                    goToNextPhase();
                }
            }
        };
        Timer.schedule(pauseTask, 1, 1, pauseTimeLeft);
    }

    /** Bytter skjerm avhengig av fase. */
    private void goToNextPhase() {
        if (!isGuessPhase) {
            // === over til gjette‑fase ===
            countdownLabel.setText("Loading drawings…");
            String gid = session.getGameId();
            String me  = GameManager.getInstance().getPlayerId();
            game.getFirebase().getDrawingsForGuessing(
                    gid, me,
                    drawingsMap -> Gdx.app.postRunnable(() ->
                            game.setScreen(new DrawingViewerScreen(game, drawingsMap))
                    ),
                    err -> Gdx.app.postRunnable(() ->
                            countdownLabel.setText("Error loading drawings.")
                    )
            );
        } else {
            // Everybody is done
            game.setScreen(new LeaderboardScreen(game));
        }
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
