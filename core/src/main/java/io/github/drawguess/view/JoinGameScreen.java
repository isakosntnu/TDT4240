package io.github.drawguess.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import io.github.drawguess.DrawGuessMain;
import io.github.drawguess.controller.JoinGameController;

public class JoinGameScreen implements Screen {
    private final DrawGuessMain game;
    private final Stage stage;

    private Texture backgroundTexture;
    private Image backgroundImage;

    private Texture backButtonTexture;
    private Image backButtonImage;

    private TextField gamePinField;
    private TextField nameField;
    private TextButton joinButton;

    public JoinGameScreen(final DrawGuessMain game) {
        this.game = game;
        this.stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        Skin skin = new Skin(Gdx.files.internal("uiskin.json"));

        // Bakgrunn
        backgroundTexture = new Texture("canvas.png");
        backgroundImage = new Image(backgroundTexture);
        backgroundImage.setFillParent(true);
        stage.addActor(backgroundImage);

        // Tilbake-knapp
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

        // Navnefelt
        nameField = new TextField("", skin);
        nameField.setMessageText("Enter your name");
        nameField.setSize(250, 80);
        nameField.setPosition(
            (Gdx.graphics.getWidth() - nameField.getWidth()) / 2f,
            Gdx.graphics.getHeight() / 2f + 40
        );
        stage.addActor(nameField);

        // PIN-felt
        gamePinField = new TextField("", skin);
        gamePinField.setMessageText("Enter Game PIN");
        gamePinField.setSize(250, 80);
        gamePinField.setPosition(
            (Gdx.graphics.getWidth() - gamePinField.getWidth()) / 2f,
            Gdx.graphics.getHeight() / 2f - 20
        );
        stage.addActor(gamePinField);

        // Join-knapp
        joinButton = new TextButton("Join Game", skin);
        joinButton.setSize(250, 80);
        joinButton.setPosition(
            (Gdx.graphics.getWidth() - joinButton.getWidth()) / 2f,
            Gdx.graphics.getHeight() / 2f - 100
        );
        joinButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                String name = nameField.getText().trim();
                String pin = gamePinField.getText().trim();

                if (!name.isEmpty() && !pin.isEmpty()) {
                    JoinGameController controller = new JoinGameController(game);
                    controller.tryJoinGame(pin, name);
                } else {
                    Gdx.app.log("JoinGameScreen", "Name or PIN is empty");
                }
            }
        });
        stage.addActor(joinButton);
    }

    @Override public void show() {}

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

    @Override public void pause() {}
    @Override public void resume() {}

    @Override
    public void hide() {
        dispose();
    }

    @Override
    public void dispose() {
        stage.dispose();
        backgroundTexture.dispose();
        backButtonTexture.dispose();
    }
}
