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

import java.util.HashMap;
import java.util.Map;

public class WaitingScreen implements Screen {

    private final DrawGuessMain game;
    private final Stage stage;
    private final GameSession session;
    private final String drawingPlayerId;    // ← Hvem sin tegning skal vi hente videre

    private Texture backgroundTexture;
    private Image backgroundImage;

    private Table playerTable;
    private Map<String, Label> statusLabels; // nøkler er playerId

    private Label messageLabel;

    private float updateTimer = 0f;
    private static final float UPDATE_INTERVAL = 1.0f;

    // —— AUTO‑ADVANCE FIELDS ——
    private boolean allFinished = false;
    private int pauseTimeLeft;
    private Timer.Task pauseTask;

    public WaitingScreen(DrawGuessMain game, String drawingPlayerId) {
        this.game = game;
        this.drawingPlayerId = drawingPlayerId;
        this.stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        this.session = GameManager.getInstance().getSession();
        this.statusLabels = new HashMap<>();

        Skin skin = new Skin(Gdx.files.internal("uiskin.json"));
        float screenHeight = Gdx.graphics.getHeight();

        // 1) Bakgrunn
        backgroundTexture = new Texture("board.png");
        backgroundImage = new Image(backgroundTexture);
        backgroundImage.setFillParent(true);
        stage.addActor(backgroundImage);

        // 2) Root‐layout
        Table rootTable = new Table();
        rootTable.setFillParent(true);
        rootTable.top().padTop(screenHeight * 0.12f);
        stage.addActor(rootTable);

        // 3) Tabell over spillere + status
        playerTable = new Table();
        rootTable.add(playerTable);
        rootTable.row().padTop(40);

        // 4) Meldings‐felt under
        messageLabel = new Label("", skin);
        rootTable.add(messageLabel).padBottom(20).row();

        // Start første oppdatering umiddelbart
        updatePlayerStatuses();
    }

    /** Legger til én rad i tabellen, bruker playerId som nøkkel, men henter display‐navnet. */
    private void addPlayerRow(String playerId, boolean isFinished) {
        Skin skin = new Skin(Gdx.files.internal("uiskin.json"));
        float scale = Gdx.graphics.getHeight() * 0.0015f;

        Player p = session.getPlayerById(playerId);
        String displayName = (p != null) ? p.getName() : playerId;

        Label nameLabel = new Label(displayName, skin);
        nameLabel.setFontScale(scale);

        Label statusLabel = new Label(isFinished ? "Finished" : "Drawing", skin);
        statusLabel.setFontScale(scale);

        // Legg i map under playerId
        statusLabels.put(playerId, statusLabel);

        // Bygg raden
        playerTable.add(nameLabel).padRight(40).left();
        playerTable.add(statusLabel).right().row();
    }

    /** Oppdaterer én spiller‐rad hvis den finnes. */
    private void updatePlayerStatus(String playerId, boolean isFinished) {
        Label statusLabel = statusLabels.get(playerId);
        if (statusLabel != null) {
            statusLabel.setText(isFinished ? "Finished" : "Drawing");
        }
    }

    /** Kaller Firebase for å hente alle spilleres ferdig‐status. */
    private void updatePlayerStatuses() {
        game.getFirebase().getPlayersWithStatus(
                session.getGameId(),
                playerStatuses -> Gdx.app.postRunnable(() -> {
                    // Legg til rader for nye spillere, oppdater eksisterende
                    for (Map.Entry<String, Boolean> entry : playerStatuses.entrySet()) {
                        String pid       = entry.getKey();
                        boolean finished = entry.getValue();
                        if (!statusLabels.containsKey(pid)) {
                            addPlayerRow(pid, finished);
                        } else {
                            updatePlayerStatus(pid, finished);
                        }
                    }
                    // Sjekk om alle er ferdige
                    if (!allFinished) {
                        boolean nowAll = playerStatuses.values().stream().allMatch(f -> f);
                        if (nowAll) {
                            allFinished = true;
                            startPauseCountdown();
                        }
                    }
                }),
                error -> Gdx.app.error("WaitingScreen", "Kunne ikke hente status", error)
        );
    }

    /** Teller ned før neste skjerm. */
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
        Timer.schedule(pauseTask, 1, 1, pauseTimeLeft);
    }

    /** Går videre til DrawingViewerScreen når avtellingen er ferdig. */
    private void goToNextRound() {
        messageLabel.setText("Loading drawing...");
        game.getFirebase().getPlayerDrawingUrl(
                session.getGameId(),
                drawingPlayerId,  // ← nå bruker vi ID-en vi fikk med fra DrawingScreen
                url -> Gdx.app.postRunnable(() -> {
                    if (url != null && !url.isEmpty()) {
                        game.setScreen(new DrawingViewerScreen(game, url));
                    } else {
                        messageLabel.setText("Drawing not uploaded yet.");
                    }
                }),
                err -> Gdx.app.postRunnable(() ->
                        messageLabel.setText("Error loading drawing.")
                )
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
    @Override public void resize(int w, int h) {
        stage.getViewport().update(w, h, true);
    }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() { dispose(); }
    @Override public void dispose() {
        stage.dispose();
        backgroundTexture.dispose();
    }
}
