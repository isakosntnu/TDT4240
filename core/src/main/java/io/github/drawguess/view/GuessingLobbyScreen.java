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
    private Map<String, Label> statusLabels; // playerId → Label

    private Label messageLabel;
    private Label countdownLabel;

    private float updateTimer = 0f;
    private static final float UPDATE_INTERVAL = 1.0f;

    // —— AUTO‑ADVANCE FIELDS ——
    private boolean allFinished = false;
    private int pauseTimeLeft;
    private Timer.Task pauseTask;

    /**
     * @param game         referanse til hoved‑klassen
     * @param isGuessPhase true = vi venter på gjett‑runde, false = vi venter på tegne‑runde
     */
    public GuessingLobbyScreen(DrawGuessMain game, boolean isGuessPhase) {
        this.game = game;
        this.isGuessPhase = isGuessPhase;
        this.stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        this.session = GameManager.getInstance().getSession();
        this.statusLabels = new HashMap<>();

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

        // 4) Tabell over spillere + status
        playerTable = new Table();
        rootTable.add(playerTable);
        rootTable.row().padTop(40);

        // 5) Meldings‑felt under
        messageLabel = new Label(
                isGuessPhase ? "Waiting for all guesses…" : "Waiting for all drawings…",
                skin
        );
        rootTable.add(messageLabel).padBottom(20).row();

        // 6) Loading overlay with countdown (initially invisible)
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
    private void addPlayerRow(String playerId, boolean isFinished) {
        Skin skin = new Skin(Gdx.files.internal("uiskin.json"));
        float scale = Gdx.graphics.getHeight() * 0.0015f;
        Player p = session.getPlayerById(playerId);
        String displayName = (p != null) ? p.getName() : playerId;

        Label name = new Label(displayName, skin);
        name.setFontScale(scale);
        Label status = new Label(isFinished ? "✔️" : "⌛", skin);
        status.setFontScale(scale);

        statusLabels.put(playerId, status);
        playerTable.add(name).padRight(40).left();
        playerTable.add(status).right().row();
    }

    /** Oppdaterer én spiller‑rad hvis den finnes. */
    private void updatePlayerStatus(String playerId, boolean isFinished) {
        Label lbl = statusLabels.get(playerId);
        if (lbl != null) {
            lbl.setText(isFinished ? "✔️" : "⌛");
        }
    }

    /** Henter korrekt felt fra Firebase basert på fase. */
    private void updatePlayerStatuses() {
        if (!isGuessPhase) {
            game.getFirebase().getPlayersWithStatus(
                    session.getGameId(),
                    this::onStatusReceived,
                    err -> Gdx.app.error("WaitingScreen", "Could not fetch drawing status", err)
            );
        } else {
            game.getFirebase().getPlayersGuessStatus(
                    session.getGameId(),
                    this::onStatusReceived,
                    err -> Gdx.app.error("WaitingScreen", "Could not fetch guess status", err)
            );
        }
    }

    /** Felles callback for begge faser. */
    private void onStatusReceived(Map<String, Boolean> playerStatuses) {
        Gdx.app.postRunnable(() -> {
            // Skip if we're already in countdown
            if (allFinished) return;
            
            // legg til nye rader / oppdater eksisterende
            for (Map.Entry<String, Boolean> e : playerStatuses.entrySet()) {
                String pid = e.getKey();
                boolean done = e.getValue();
                if (!statusLabels.containsKey(pid)) {
                    addPlayerRow(pid, done);
                } else {
                    updatePlayerStatus(pid, done);
                }
            }
            // sjekk om alle er ferdige
            if (!allFinished) {
                boolean nowAll = playerStatuses.values().stream().allMatch(b -> b);
                if (nowAll) {
                    allFinished = true;
                    startPauseCountdown();
                }
            }
        });
    }

    /** Teller ned 5 sek før neste skjerm. */
    private void startPauseCountdown() {
        pauseTimeLeft = 5;
        
        // Show loading screen and hide player information
        backgroundImage.setVisible(false);
        rootTable.setVisible(false);
        loadingImage.setVisible(true);
        loadingTable.setVisible(true);
        
        countdownLabel.setText("Next in " + pauseTimeLeft + "s");

        pauseTask = new Timer.Task() {
            @Override public void run() {
                pauseTimeLeft--;
                if (pauseTimeLeft > 0) {
                    countdownLabel.setText("Next in " + pauseTimeLeft + "s");
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
            // === alle har gjettet ===
            game.setScreen(new LeaderboardScreen(game));
        }
    }

    @Override
    public void render(float delta) {
        // Only update player statuses if not in countdown mode
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
