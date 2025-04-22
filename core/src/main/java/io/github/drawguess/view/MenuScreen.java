package io.github.drawguess.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import io.github.drawguess.DrawGuessMain;

public class MenuScreen implements Screen {
    private final DrawGuessMain game;
    private Stage stage;

    private Texture buttonTexture;
    private Image buttonImage;

    private Texture backgroundTexture;
    private Image backgroundImage;

    private Texture logoTexture;
    private Image logoImage;

    private Texture joinButtonTexture;
    private Image joinButtonImage;

    public MenuScreen(final DrawGuessMain game) {
        this.game = game;
        this.stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();


        backgroundTexture = new Texture("bgmenu.png");
        backgroundImage = new Image(backgroundTexture);
        backgroundImage.setFillParent(true);
        stage.addActor(backgroundImage);


        logoTexture = new Texture("logo.png");
        logoImage = new Image(logoTexture);
        float logoWidth = screenWidth * 0.6f;
        float logoHeight = screenHeight * 0.2f;
        logoImage.setSize(logoWidth, logoHeight);
        logoImage.setPosition(
            (screenWidth - logoWidth) / 2f,
            screenHeight - logoHeight - screenHeight * 0.1f
        );
        stage.addActor(logoImage);

        // Start game
        buttonTexture = new Texture("startgamebtn.png");
        buttonImage = new Image(buttonTexture);
        float buttonWidth = screenWidth * 0.6f;
        float buttonHeight = screenHeight * 0.09f;
        buttonImage.setSize(buttonWidth, buttonHeight);
        buttonImage.setPosition(
            (screenWidth - buttonWidth) / 2f,
            screenHeight * 0.4f
        );
        buttonImage.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                game.setScreen(new CreateGameScreen(game));
                return true;
            }
        });
        stage.addActor(buttonImage);

        // Join Game
        joinButtonTexture = new Texture("joingamebtn.png");
        joinButtonImage = new Image(joinButtonTexture);
        joinButtonImage.setSize(buttonWidth, buttonHeight);
        joinButtonImage.setPosition(
            (screenWidth - buttonWidth) / 2f,
            buttonImage.getY() - buttonHeight - screenHeight * 0.03f
        );
        joinButtonImage.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                game.setScreen(new JoinGameScreen(game));
                return true;
            }
        });
        stage.addActor(joinButtonImage);
    }

    @Override public void render(float delta) {
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
        buttonTexture.dispose();
        backgroundTexture.dispose();
        logoTexture.dispose();
        joinButtonTexture.dispose();
    }
}
