package io.github.drawguess.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import io.github.drawguess.DrawGuessMain;
import io.github.drawguess.controller.GameController;

import io.github.drawguess.model.GameSession;

public class CreateGameScreen implements Screen {
    private final DrawGuessMain game;
    private final Stage stage;

    private Texture backgroundTexture;
    private Image backgroundImage;

    private Texture backButtonTexture;
    private Image backButtonImage;

    private TextField nameField;
    private TextButton createGameButton;

    public CreateGameScreen(final DrawGuessMain game) {
        this.game = game;
        this.stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        Skin skin = new Skin(Gdx.files.internal("uiskin.json"));

        // Skjermdimensjoner
        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();

        // Base font scale factor relative to screen height
        float baseFontScale = screenHeight * 0.002f; 

        // Dynamiske størrelser
        float fieldWidth = screenWidth * 0.7f;
        float fieldHeight = screenHeight * 0.08f;
        float buttonHeight = screenHeight * 0.08f;
        float horizontalPadding = screenWidth * 0.03f; // Relative horizontal padding
        float verticalPadding = screenHeight * 0.03f; // Relative vertical padding

        // (1) Bakgrunn
        backgroundTexture = new Texture("canvas.png");
        backgroundImage = new Image(backgroundTexture);
        backgroundImage.setFillParent(true);
        stage.addActor(backgroundImage);

        // (2) Tilbake-knapp
        backButtonTexture = new Texture("backbtn.png");
        backButtonImage = new Image(backButtonTexture);
        backButtonImage.setSize(screenWidth * 0.15f, screenHeight * 0.07f);
        backButtonImage.setPosition(horizontalPadding, screenHeight - backButtonImage.getHeight() - verticalPadding);
        backButtonImage.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                game.setScreen(new MenuScreen(game));
                return true;
            }
        });
        stage.addActor(backButtonImage);

        // (3) Navnefelt
        nameField = new TextField("", skin);
        nameField.setMessageText("Enter your name");
        nameField.setSize(fieldWidth, fieldHeight);
        nameField.setPosition(
            (screenWidth - nameField.getWidth()) / 2f,
            screenHeight / 2f + nameField.getHeight()
        );
        // Apply dynamic font scaling
        nameField.getStyle().font.getData().setScale(baseFontScale);
        // Center text vertically and horizontally
        nameField.setAlignment(com.badlogic.gdx.utils.Align.center); 
        stage.addActor(nameField);

        // (4) Create Game-knapp
        createGameButton = new TextButton("Create New Game", skin);
        createGameButton.setSize(fieldWidth, buttonHeight);
        createGameButton.setPosition(
            (screenWidth - createGameButton.getWidth()) / 2f,
            screenHeight / 2f - createGameButton.getHeight() - (screenHeight * 0.02f)
        );
        // Apply dynamic font scaling to button label
        createGameButton.getLabel().setFontScale(baseFontScale);
        // Center label within the button
        createGameButton.getLabel().setAlignment(com.badlogic.gdx.utils.Align.center);
        createGameButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                String playerName = nameField.getText().trim();
                if (!playerName.trim().isEmpty()) {
                    GameController gameController = new GameController();
                    gameController.createGame(playerName, game);
                }
            }
        });
        stage.addActor(createGameButton);
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

    @Override public void dispose() {
        stage.dispose();
        backgroundTexture.dispose();
        backButtonTexture.dispose();
    }
}

