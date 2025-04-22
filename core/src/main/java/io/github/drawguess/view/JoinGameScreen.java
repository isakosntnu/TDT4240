package io.github.drawguess.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;

import io.github.drawguess.DrawGuessMain;
import io.github.drawguess.manager.GameManager;
import io.github.drawguess.model.GameSession;
import io.github.drawguess.model.Player;

import java.util.UUID;
import java.util.List;
import java.util.ArrayList;


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

        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();
        float horizontalPadding = screenWidth * 0.03f; 
        float verticalPadding = screenHeight * 0.03f; 
        float elementWidth = screenWidth * 0.6f; 
        float elementHeight = screenHeight * 0.08f;
        float fieldSpacing = screenHeight * 0.02f; 
        float baseFontScale = screenHeight * 0.002f; 


        backgroundTexture = new Texture("canvas.png");
        backgroundImage = new Image(backgroundTexture);
        backgroundImage.setFillParent(true);
        stage.addActor(backgroundImage);


        backButtonTexture = new Texture("backbtn.png");
        backButtonImage = new Image(backButtonTexture);
        backButtonImage.setSize(screenWidth * 0.15f, screenHeight * 0.07f); // Relative size
        backButtonImage.setPosition(horizontalPadding, screenHeight - backButtonImage.getHeight() - verticalPadding);
        backButtonImage.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                game.setScreen(new MenuScreen(game));
                return true;
            }
        });
        stage.addActor(backButtonImage);

        float screenCenterX = screenWidth / 2f;
        float startY = screenHeight / 2f + elementHeight; 


        gamePinField = new TextField("", skin);
        gamePinField.setMessageText("Enter Game PIN");
        gamePinField.setSize(elementWidth, elementHeight);
        gamePinField.setPosition(screenCenterX - elementWidth / 2f, startY);


        gamePinField.getStyle().font.getData().setScale(baseFontScale);
        gamePinField.setAlignment(Align.center); // Sentrert tekst
        stage.addActor(gamePinField);


        nameField = new TextField("", skin);
        nameField.setMessageText("Enter Your Name");
        nameField.setSize(elementWidth, elementHeight);
        nameField.setPosition(screenCenterX - elementWidth / 2f, startY - elementHeight - fieldSpacing);


        nameField.getStyle().font.getData().setScale(baseFontScale);
        nameField.setAlignment(Align.center); // Sentrert tekst
        stage.addActor(nameField);


        joinButton = new TextButton("Join Game", skin);
        joinButton.setSize(elementWidth, elementHeight);
        joinButton.setPosition(screenCenterX - elementWidth / 2f, startY - (elementHeight * 2) - (fieldSpacing * 2));


        joinButton.getLabel().setFontScale(baseFontScale);
        joinButton.getLabel().setAlignment(Align.center);

        stage.addActor(joinButton);
        //Join button event
        joinButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                String gameId     = gamePinField.getText().trim();
                String playerName = nameField.getText().trim();
        
                if (gameId.isEmpty() || playerName.isEmpty()) return;
        
                joinButton.setDisabled(true); 
        
                // Check if game exists
                game.getFirebase().checkGameExists(
                    gameId,
                    exists -> {
                        if (!exists) {
                            Gdx.app.postRunnable(() -> {
                                showPopup("Game Not Found",
                                          "Game PIN '" + gameId + "' does not exist.");
                                joinButton.setDisabled(false);
                            });
                            return;
                        }
        

                        String playerId = UUID.randomUUID().toString();
                        Player me = new Player(playerId, playerName, 0, false);
        
                        List<Player> list = new ArrayList<>();
                        list.add(me);
        
                        GameSession session = new GameSession(
                                gameId, me, list, GameSession.Status.WAITING_FOR_PLAYERS);
        
                        GameManager.getInstance().setSession(session);
                        GameManager.getInstance().setPlayerId(playerId);
        
                        game.getFirebase().joinGame(gameId, playerName);
        
                        Gdx.app.postRunnable(() -> game.setScreen(new LobbyScreen(game)));
                    },
                    err -> Gdx.app.postRunnable(() -> {
                        showPopup("Connection Error",
                                  "Could not verify Game PIN. Please try again.");
                        joinButton.setDisabled(false);
                    })
                );
            }
        });
        
    }


    private void showPopup(String title, String message) {
        Dialog dlg = new Dialog(title, new Skin(Gdx.files.internal("uiskin.json")));
        dlg.text(message);
        dlg.button("OK");
        dlg.key(com.badlogic.gdx.Input.Keys.ENTER, true);
        dlg.key(com.badlogic.gdx.Input.Keys.ESCAPE, true);
        dlg.show(stage);
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