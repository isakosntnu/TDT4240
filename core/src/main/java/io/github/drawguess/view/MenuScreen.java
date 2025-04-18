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
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        backgroundTexture = new Texture("bgmenu.png");
        backgroundImage = new Image(backgroundTexture);
        backgroundImage.setFillParent(true);
        stage.addActor(backgroundImage); // Bakgrunn legges først

        buttonTexture = new Texture("startgamebtn.png");
        buttonImage = new Image(buttonTexture);
        buttonImage.setSize(200, 50);
        buttonImage.setPosition(
            (Gdx.graphics.getWidth() - buttonImage.getWidth()) / 2f,
            (Gdx.graphics.getHeight() - buttonImage.getHeight() ) / 2f - 100
        );

        joinButtonTexture = new Texture("joingamebtn.png");
        joinButtonImage = new Image(joinButtonTexture);
        joinButtonImage.setSize(200, 50);

        // Plasser rett under "Start Game"-knappen
        joinButtonImage.setPosition(
            (Gdx.graphics.getWidth() - joinButtonImage.getWidth()) / 2f,
            buttonImage.getY() - joinButtonImage.getHeight() - 30 // 20px spacing
        );

        // Klikk-event (valgfritt)
        joinButtonImage.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                game.setScreen(new JoinGameScreen(game));
                return true;
            }
        });
        

        stage.addActor(joinButtonImage);


        logoTexture = new Texture("logo.png");
        logoImage = new Image(logoTexture);

        // Skalere ned hvis ønskelig
        logoImage.setSize(300, 200); // eller bruk logoTexture.getWidth()/getHeight()

        // Plasser øverst midtstilt
        logoImage.setPosition(
            (Gdx.graphics.getWidth() - logoImage.getWidth()) / 2f,
            Gdx.graphics.getHeight() - logoImage.getHeight() - 200 // 200px padding fra toppen
        );

        // Legg til logo over bakgrunnen men under knappen
        stage.addActor(logoImage);

        buttonImage.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                game.getFirebase().createGame(); // 🔥 Oppretter nytt spill
                game.setScreen(new GameScreen(game));
                return true;
            }
        });

        stage.addActor(buttonImage);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1); // svart bakgrunn
        Gdx.gl.glClear(Gdx.gl.GL_COLOR_BUFFER_BIT);

        stage.act(delta);
        stage.draw();
    }

    @Override public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override public void show() {}
    @Override public void hide() {
        dispose();
    }

    @Override public void pause() {}
    @Override public void resume() {}

    @Override public void dispose() {
        stage.dispose();
        buttonTexture.dispose();
        backgroundTexture.dispose();
        logoTexture.dispose();
        joinButtonTexture.dispose();

    }
}
