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

public class WaitingScreen implements Screen {

    private final DrawGuessMain game;
    private final Stage stage;
    private final GameSession session;
    private final boolean isGuessPhase;

    private Texture backgroundTexture;
    private Image backgroundImage;

    private Table playerTable;
    private Map<String, Label> statusLabels; // playerId → Label

    private Label messageLabel;

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
    public WaitingScreen(DrawGuessMain game, boolean isGuessPhase) {
        this.game = game;
        this.isGuessPhase = isGuessPhase;
        this.stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        this.session = GameManager.getInstance().getSession();
        this.statusLabels = new HashMap<>();

        Skin skin = new Skin(Gdx.files.internal("uiskin.json"));
        float sh = Gdx.graphics.getHeight();

        // 1) Bakgrunn
        backgroundTexture = new Texture("board.png");
        backgroundImage = new Image(backgroundTexture);
        backgroundImage.setFillParent(true);
        stage.addActor(backgroundImage);

        // 2) Root‑layout
        Table root = new Table();
        root.setFillParent(true);
        root.top().padTop(sh * 0.12f);
        stage.addActor(root);

        // 3) Tabell over spillere + status
        playerTable = new Table();
        root.add(playerTable);
        root.row().padTop(40);

        // 4) Meldings‑felt under
        messageLabel = new Label(
                isGuessPhase ? "Waiting for all guesses…" : "Waiting for all drawings…",
                skin
        );
        root.add(messageLabel).padBottom(20).row();

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

    /** Teller ned 5 sek før neste skjerm. */
    private void startPauseCountdown() {
        pauseTimeLeft = 5;
        messageLabel.setText("Next in " + pauseTimeLeft + "s");

        pauseTask = new Timer.Task() {
            @Override public void run() {
                pauseTimeLeft--;
                if (pauseTimeLeft > 0) {
                    messageLabel.setText("Next in " + pauseTimeLeft + "s");
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
            messageLabel.setText("Loading drawings…");
            String gid = session.getGameId();
            String me  = GameManager.getInstance().getPlayerId();
            game.getFirebase().getDrawingsForGuessing(
                    gid, me,
                    drawingsMap -> Gdx.app.postRunnable(() ->
                            game.setScreen(new DrawingViewerScreen(game, drawingsMap))
                    ),
                    err -> Gdx.app.postRunnable(() ->
                            messageLabel.setText("Error loading drawings.")
                    )
            );
        } else {
            // === alle har gjettet ===
            game.setScreen(new LeaderboardScreen(game));
        }
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

    @Override public void resize(int w, int h) { stage.getViewport().update(w, h, true); }
    @Override public void show() {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() { dispose(); }
    @Override
    public void dispose() {
        stage.dispose();
        backgroundTexture.dispose();
    }
}
