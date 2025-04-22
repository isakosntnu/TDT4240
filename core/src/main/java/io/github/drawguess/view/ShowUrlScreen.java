package io.github.drawguess.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import java.util.Map;

import io.github.drawguess.DrawGuessMain;
import io.github.drawguess.manager.GameManager;

public class ShowUrlScreen implements Screen {

    private final DrawGuessMain game;
    private final Stage stage;
    private final String gameId;

    private Table rootTable;
    private Skin skin;

    public ShowUrlScreen(DrawGuessMain game, String gameId) {
        this.game = game;
        this.gameId = gameId;
        this.stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        skin = new Skin(Gdx.files.internal("uiskin.json"));

        setupUI();
        displayDrawings(GameManager.getInstance().getPlayerDrawings());
    }

    private void setupUI() {
        rootTable = new Table();
        rootTable.setFillParent(true);
        rootTable.top().padTop(50);
        stage.addActor(rootTable);

        Label title = new Label("🖼️ Uploaded Drawings", skin);
        title.setFontScale(1.5f);
        rootTable.add(title).padBottom(20).row();
    }


    private void displayDrawings(Map<String, String> drawingMap) {
        for (Map.Entry<String, String> entry : drawingMap.entrySet()) {
            String playerName = entry.getKey();
            String url = entry.getValue();

            Label playerLabel = new Label(playerName + ":", skin);
            Label urlLabel = new Label(url, skin);
            urlLabel.setWrap(true);

            rootTable.add(playerLabel).left().pad(10);
            rootTable.add(urlLabel).expandX().fillX().pad(10).row();
        }

        TextButton backButton = new TextButton("Back to Lobby", skin);
        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new WaitingScreen(game)); // Tilbake til lobby / waiting screen
            }
        });
        rootTable.add(backButton).colspan(2).padTop(30).center();
    }

    @Override
    public void render(float delta) {
        stage.act(delta);
        stage.draw();
    }

    @Override public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }
    @Override public void show() {}
    @Override public void hide() { dispose(); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void dispose() { stage.dispose(); }
}
