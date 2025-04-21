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

import java.util.HashMap;
import java.util.Map;

public class WaitingScreen implements Screen {

    private final DrawGuessMain game;
    private final Stage stage;
    private final GameSession session;

    private Texture backgroundTexture;
    private Image backgroundImage;

    private Table playerTable;
    private Map<String, Label> statusLabels;

    private Label messageLabel;

    private float updateTimer = 0f;
    private static final float UPDATE_INTERVAL = 1.0f;

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

        Skin skin = new Skin(Gdx.files.internal("uiskin.json"));
        float screenHeight = Gdx.graphics.getHeight();

        // Bakgrunn
        backgroundTexture = new Texture("board.png");
        backgroundImage = new Image(backgroundTexture);
        backgroundImage.setFillParent(true);
        stage.addActor(backgroundImage);

        // Layout
        Table rootTable = new Table();
        rootTable.setFillParent(true);
        rootTable.top().padTop(screenHeight * 0.12f);
        stage.addActor(rootTable);

        // Spillerstatus-tabell
        playerTable = new Table();
        rootTable.add(playerTable);
        rootTable.row().padTop(40);

        // Statusbeskjed
        messageLabel = new Label("", skin);
        rootTable.add(messageLabel).padBottom(20).row();
    }

    private void addPlayerRow(String playerName, boolean isFinished) {
        Skin skin = new Skin(Gdx.files.internal("uiskin.json"));
        float screenHeight = Gdx.graphics.getHeight();

        Label nameLabel = new Label(playerName, skin);
        nameLabel.setFontScale(screenHeight * 0.0015f);

        Label statusLabel = new Label(isFinished ? "Finished" : "Drawing", skin);
        statusLabel.setFontScale(screenHeight * 0.0015f);
        statusLabels.put(playerName, statusLabel);

        playerTable.add(nameLabel).padRight(40).left();
        playerTable.add(statusLabel).right().row();
    }

    private void updatePlayerStatus(String playerName, boolean isFinished) {
        Label statusLabel = statusLabels.get(playerName);
        if (statusLabel != null) {
            statusLabel.setText(isFinished ? "Finished" : "Drawing");
        }
    }

    private void updatePlayerStatuses() {
        String gameId = session.getGameId();
        game.getFirebase().getPlayersWithStatus(
                gameId,
                playerStatuses -> Gdx.app.postRunnable(() -> {
                    // populate/update table
                    for (Map.Entry<String, Boolean> entry : playerStatuses.entrySet()) {
                        String name = entry.getKey();
                        boolean isFinished = entry.getValue();
                        if (!statusLabels.containsKey(name)) {
                            addPlayerRow(name, isFinished);
                        } else {
                            updatePlayerStatus(name, isFinished);
                        }
                    }

                    // detect when everyone is finished
                    if (!allFinished) {
                        boolean nowAll = playerStatuses.values().stream().allMatch(f -> f);
                        if (nowAll) {
                            allFinished = true;
                            startPauseCountdown();
                        }
                    }
                }),
                e -> Gdx.app.error("WaitingScreen", "❌ Klarte ikke hente spillerstatus", e)
        );
    }

    private void startPauseCountdown() {
        pauseTimeLeft = 5;
        messageLabel.setText("Next round in " + pauseTimeLeft + "s");
        pauseTask = new Timer.Task() {
            @Override public void run() {
                pauseTimeLeft--;
                if (pauseTimeLeft > 0) {
                    messageLabel.setText("Next round in " + pauseTimeLeft + "s");
                } else {
                    cancel();
                    goToNextRound();
                }
            }
        };
        // runs once per second, 5 times
        Timer.schedule(pauseTask, 1, 1, pauseTimeLeft);
    }

    private void goToNextRound() {
        messageLabel.setText("Loading drawing...");
        String gameId = session.getGameId();
        String drawingPlayerId = session.getHostPlayer().getId();
        game.getFirebase().getPlayerDrawingUrl(
                gameId,
                drawingPlayerId,
                url -> Gdx.app.postRunnable(() -> {
                    if (url != null && !url.isEmpty()) {
                        game.setScreen(new DrawingViewerScreen(game, url));
                    } else {
                        messageLabel.setText("Drawing not uploaded yet.");
                    }
                }),
                error -> Gdx.app.postRunnable(() -> messageLabel.setText("Error loading drawing."))
        );
    }

    @Override
    public void render(float delta) {
        updateTimer += delta;
        if (updateTimer >= UPDATE_INTERVAL) {
            updateTimer = 0f;
            updatePlayerStatuses();
        }
        stage.act(delta);
        stage.draw();
    }

    @Override public void show() {}
    @Override public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() { dispose(); }
    @Override public void dispose() {
        stage.dispose();
        backgroundTexture.dispose();
    }
}
