package io.github.drawguess.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
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

    private Texture backgroundTexture;
    private Image backgroundImage;

    private Table playerTable;
    private Map<String, Label> statusLabels;

    private TextButton nextRound;

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

        for (Player player : session.getPlayers()) {
            addPlayerRow(player.getName(), player.hasFinishedDrawing());
        }

        nextRound = new TextButton("Next Round", skin);
        nextRound.getLabel().setFontScale(screenHeight * 0.0015f);

        rootTable.row().padTop(60);
        rootTable.add(nextRound).expandY().bottom().padBottom(30);

        nextRound.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                System.out.println("Next Round Started!");
        
                // 
        
                game.setScreen(new GuessingScreen(game)); 
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

    @Override
    public void show() {}

    @Override
    public void render(float delta) {
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() { dispose(); }

    @Override
    public void dispose() {
        stage.dispose();
        backgroundTexture.dispose();
    }
}
