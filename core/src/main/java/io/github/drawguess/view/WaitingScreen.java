package io.github.drawguess.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;


import java.util.HashMap;
import java.util.Map;

import io.github.drawguess.DrawGuessMain;
import io.github.drawguess.manager.GameManager;
import io.github.drawguess.model.GameSession;
import io.github.drawguess.model.Player;

public class WaitingScreen implements Screen {

    private final DrawGuessMain game;
    private final Stage stage;
    private final GameSession session;

    private Texture backgroundTexture;
    private Image backgroundImage;

    private Table playerTable;
    private Map<String, Label> statusLabels;

    private TextButton nextRound;
    private Label messageLabel;

    private float updateTimer = 0f;
    private static final float UPDATE_INTERVAL = 1.0f;

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

        for (Player player : session.getPlayers()) {
            addPlayerRow(player.getName(), player.hasFinishedDrawing());
        }

        // Statusbeskjed
        messageLabel = new Label("", skin);
        rootTable.add(messageLabel).padBottom(20).row();

        // Neste runde-knapp
        nextRound = new TextButton("Next Round", skin);
        nextRound.getLabel().setFontScale(screenHeight * 0.0015f);
        rootTable.add(nextRound).expandY().bottom().padBottom(30);

        nextRound.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                messageLabel.setText("Loading drawing...");
                nextRound.setDisabled(true);

                String gameId = session.getGameId();
                String drawingPlayerId = session.getHostPlayer().getId(); // evt. annen spiller

                game.getFirebase().getPlayerDrawingUrl(
                    gameId,
                    drawingPlayerId,
                    url -> Gdx.app.postRunnable(() -> {
                        if (url != null && !url.isEmpty()) {
                            game.setScreen(new DrawingViewerScreen(game, url));
                        } else {
                            messageLabel.setText("Drawing not uploaded yet. Try again soon.");
                            nextRound.setDisabled(false);
                        }
                    }),
                    error -> Gdx.app.postRunnable(() -> {
                        messageLabel.setText("Error fetching drawing. Try again.");
                        nextRound.setDisabled(false);
                    })
                );
            }
        });

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

    public void updatePlayerStatus(String playerName, boolean isFinished) {
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
                for (Map.Entry<String, Boolean> entry : playerStatuses.entrySet()) {
                    updatePlayerStatus(entry.getKey(), entry.getValue());
                }
            }),
            e -> Gdx.app.error("WaitingScreen", "❌ Klarte ikke hente spillerstatus", e)
        );
    }
    
    

    @Override
    public void render(float delta) {
        updateTimer += delta;
        if (updateTimer >= UPDATE_INTERVAL) {
            updateTimer = 0f;
            updatePlayerStatuses(); // 🔁
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
