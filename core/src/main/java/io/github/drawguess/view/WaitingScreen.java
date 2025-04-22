package io.github.drawguess.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import java.util.HashMap;
import java.util.Map;

import io.github.drawguess.DrawGuessMain;
import io.github.drawguess.manager.GameManager;
import io.github.drawguess.model.GameSession;
import io.github.drawguess.server.SocketInterface;

public class WaitingScreen implements Screen {

    private final DrawGuessMain game;
    private final Stage stage;
    private final GameSession session;

    private Texture backgroundTexture;
    private Image backgroundImage;

    private Table rootTable;
    private Table playerTable;
    private Map<String, Label> statusLabels = new HashMap<>();

    private Label messageLabel;
    private TextButton nextRoundButton;
    private final SocketInterface socketHandler;

    private float updateTimer = 0f;
    private static final float UPDATE_INTERVAL = 1.0f;

    private boolean buttonAdded = false;

    public WaitingScreen(DrawGuessMain game) {
        this.game = game;
        this.stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);
        this.socketHandler = game.getSocket(); // <--- kommer fra Android-implementasjonen

        this.session = GameManager.getInstance().getSession();

        Skin skin = new Skin(Gdx.files.internal("uiskin.json"));
        float screenHeight = Gdx.graphics.getHeight();

        backgroundTexture = new Texture("board.png");
        backgroundImage = new Image(backgroundTexture);
        backgroundImage.setFillParent(true);
        stage.addActor(backgroundImage);

        rootTable = new Table();
        rootTable.setFillParent(true);
        rootTable.top().padTop(screenHeight * 0.12f);
        stage.addActor(rootTable);

        // Oppsett i konstruktør:
        playerTable = new Table();
        rootTable.add(playerTable).padBottom(20).row();

        messageLabel = new Label("Waiting for players to finish...", skin);
        messageLabel.setFontScale(screenHeight * 0.0014f);
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
                boolean allFinished = true;

                for (Map.Entry<String, Boolean> entry : playerStatuses.entrySet()) {
                    String name = entry.getKey();
                    boolean isFinished = entry.getValue();

                    if (!statusLabels.containsKey(name)) {
                        addPlayerRow(name, isFinished);
                    } else {
                        updatePlayerStatus(name, isFinished);
                    }

                    if (!isFinished) {
                        allFinished = false;
                    }
                }

                if (allFinished && !buttonAdded) {
                    showNextRoundButton();
                    buttonAdded = true;
                }
            }),
            e -> Gdx.app.error("WaitingScreen", "❌ Failed to fetch statuses", e)
        );
    }

    private void showNextRoundButton() {
        String gameId = session.getGameId();
    
        game.getFirebase().checkIfPlayerIsHost(gameId,
            isHost -> {
                if (!isHost) {
                    Gdx.app.log("WaitingScreen", "🙅 Not host, button will not be shown.");
                    return; // 🚫 Ikke host → ikke vis knappen
                }
    
                // ✅ Hvis host → opprett knappen som før:
                Skin skin = new Skin(Gdx.files.internal("uiskin.json"));
                float screenHeight = Gdx.graphics.getHeight();
    
                nextRoundButton = new TextButton("Next Round", skin);
                nextRoundButton.getLabel().setFontScale(screenHeight * 0.0015f);
    
                nextRoundButton.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        messageLabel.setText("Starting guessing phase...");
                        nextRoundButton.setDisabled(true);
    
                        String gameId = session.getGameId();
                        fetchDrawingUrls();

                    }
                });
    
                rootTable.add(nextRoundButton).expandY().bottom().padBottom(30).row();
            },
            error -> {
                Gdx.app.error("WaitingScreen", "❌ Failed to check if player is host", error);
            }
        );
    }
    
    private void fetchDrawingUrls() {
        String gameId = session.getGameId();
        game.getFirebase().getAllPlayerDrawings(
            gameId,
            drawingMap -> Gdx.app.postRunnable(() -> {
                GameManager.getInstance().setPlayerDrawings(drawingMap);
                Gdx.app.log("WaitingScreen", "✅ Drawing URLs cached in GameManager: " + drawingMap.size());
                
                // 👉 Nå kan vi bytte skjerm trygt:
                game.setScreen(new ShowUrlScreen(game, gameId));
            }),
            error -> Gdx.app.error("WaitingScreen", "❌ Failed to fetch drawing URLs", error)
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

    @Override
    public void show() {
        socketHandler.registerLobbyListeners();
    }

    @Override
    public void hide() {
        socketHandler.unregisterLobbyListeners();
        dispose();
    }

    @Override public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }
    @Override public void pause() {}
    @Override public void resume() {}

    @Override public void dispose() {
        stage.dispose();
        backgroundTexture.dispose();
    }
}
