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

public class GameScreen implements Screen {
    private final DrawGuessMain game;
    private Stage stage;

    private Texture backgroundTexture;
    private Image backgroundImage;

    private Texture backButtonTexture;
    private Image backButtonImage;

    private Texture leaderboardButtonTexture;
    private Image leaderboardButtonImage;

    private Texture drawingButtonTexture;
    private Image drawingButtonImage;

    private Texture guessButtonTexture;
    private Image guessButtonImage;

    private Texture waitingButtonTexture;
    private Image waitingButtonImage;

    public GameScreen(final DrawGuessMain game) {
        this.game = game;
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        // Bakgrunn
        backgroundTexture = new Texture("bg0.png");
        backgroundImage = new Image(backgroundTexture);
        backgroundImage.setFillParent(true);
        stage.addActor(backgroundImage);

        // Tilbake-knapp
        backButtonTexture = new Texture("backbtn.png");
        backButtonImage = new Image(backButtonTexture);
        backButtonImage.setSize(150, 90);
        backButtonImage.setPosition(30, Gdx.graphics.getHeight() - backButtonImage.getHeight() - 30);
        backButtonImage.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                game.setScreen(new MenuScreen(game));
                return true;
            }
        });
        stage.addActor(backButtonImage);

        // Leaderboard-knapp
        leaderboardButtonTexture = new Texture("right.png");
        leaderboardButtonImage = new Image(leaderboardButtonTexture);
        leaderboardButtonImage.setSize(200, 100);
        float centerX = (Gdx.graphics.getWidth() - leaderboardButtonImage.getWidth()) / 2f;
        float centerY = (Gdx.graphics.getHeight() - leaderboardButtonImage.getHeight()) / 2f;
        leaderboardButtonImage.setPosition(centerX, centerY);
        leaderboardButtonImage.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                game.setScreen(new LeaderboardScreen(game));
                return true;
            }
        });
        stage.addActor(leaderboardButtonImage);

        // Tegne-knapp
        drawingButtonTexture = new Texture("right.png");
        drawingButtonImage = new Image(drawingButtonTexture);
        drawingButtonImage.setSize(200, 100);
        drawingButtonImage.setPosition(centerX, centerY - 120);
        drawingButtonImage.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                game.setScreen(new DrawingScreen(game));
                return true;
            }
        });
        stage.addActor(drawingButtonImage);

        // Guessing-knapp under tegneknappen
        guessButtonTexture = new Texture("button.png");
        guessButtonImage = new Image(guessButtonTexture);
        guessButtonImage.setSize(200, 100);
        guessButtonImage.setPosition(centerX, centerY - 240);
        guessButtonImage.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                game.setScreen(new GuessingScreen(game));
                return true;
            }
        });
        stage.addActor(guessButtonImage);

        waitingButtonTexture = new Texture("right.png");
        waitingButtonImage = new Image(waitingButtonTexture);
        waitingButtonImage.setSize(200, 100);
        waitingButtonImage.setPosition(centerX, centerY - 360);
        waitingButtonImage.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                game.setScreen(new WaitingForPlayersScreen(game));
                return true;
            }
        });
        stage.addActor(waitingButtonImage);
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
        leaderboardButtonTexture.dispose();
        drawingButtonTexture.dispose();
        guessButtonTexture.dispose();
        waitingButtonTexture.dispose();
    }
}
