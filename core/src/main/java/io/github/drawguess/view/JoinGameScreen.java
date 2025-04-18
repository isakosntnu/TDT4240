package io.github.drawguess.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import io.github.drawguess.DrawGuessMain;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

public class JoinGameScreen implements Screen {
    private final DrawGuessMain game;
    private final Stage stage;

    private Texture backgroundTexture;
    private Image backgroundImage;

    private Texture backButtonTexture;
    private Image backButtonImage;

    private TextField gamePinField;

    public JoinGameScreen(final DrawGuessMain game) {
        this.game = game;
        this.stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        Skin skin = new Skin(Gdx.files.internal("uiskin.json"));

        // (1) Bakgrunn
        backgroundTexture = new Texture("joingame.png");
        backgroundImage = new Image(backgroundTexture);
        backgroundImage.setFillParent(true);
        stage.addActor(backgroundImage);

        // (2) Tilbake-knapp
        backButtonTexture = new Texture("backbtn.png");
        backButtonImage = new Image(backButtonTexture);
        backButtonImage.setSize(100, 50);
        backButtonImage.setPosition(30, Gdx.graphics.getHeight() - backButtonImage.getHeight() - 30);
        backButtonImage.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                game.setScreen(new MenuScreen(game));
                return true;
            }
        });
        stage.addActor(backButtonImage);

        // (3) Tekstfelt
        gamePinField = new TextField("", skin);
        gamePinField.setMessageText("Enter Game PIN");
        gamePinField.setSize(250, 80);
        gamePinField.setPosition(
            (Gdx.graphics.getWidth() - gamePinField.getWidth()) / 2f,
            (Gdx.graphics.getHeight() - gamePinField.getHeight()) / 2f - 20
        );
        stage.addActor(gamePinField);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(Gdx.gl.GL_COLOR_BUFFER_BIT);
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

    @Override
    public void dispose() {
        stage.dispose();
        backgroundTexture.dispose();
        backButtonTexture.dispose();
    }
}
