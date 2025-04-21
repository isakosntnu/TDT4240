package io.github.drawguess.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;

import io.github.drawguess.DrawGuessMain;
import io.github.drawguess.manager.GameManager;

public class GuessingScreen implements Screen {

    private final DrawGuessMain game;
    private final Stage stage;
    private final String drawingOwnerId;
    private final String currentPlayerId;

    private Texture backgroundTexture;
    private Texture drawingTexture;
    private Image drawingImage;

    private Label questionLabel;
    private Label playerCounter;
    private TextField guessInput;
    private TextButton submitButton;

    private boolean hasGuessed = false;
    private int totalPlayers = 8;
    private int playersAnswered = 5;

    public GuessingScreen(DrawGuessMain game, String drawingOwnerId) {
        this.game = game;
        this.drawingOwnerId = drawingOwnerId;
        this.currentPlayerId = GameManager.getInstance().getPlayerId();
        this.stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        Skin skin = new Skin(Gdx.files.internal("uiskin.json"));

        backgroundTexture = new Texture("guessbg.png");
        Image backgroundImage = new Image(backgroundTexture);
        backgroundImage.setFillParent(true);
        stage.addActor(backgroundImage);

        drawingTexture = new Texture("placeholder.png");
        drawingImage = new Image(drawingTexture);
        drawingImage.setScaling(Scaling.fit);
        drawingImage.setSize(250, 300);
        drawingImage.setPosition(
                (Gdx.graphics.getWidth() - drawingImage.getWidth()) / 2f,
                Gdx.graphics.getHeight() - drawingImage.getHeight() - 80
        );
        stage.addActor(drawingImage);

        Table sheetTable = new Table();
        sheetTable.setFillParent(true);
        sheetTable.bottom().padBottom(50);
        stage.addActor(sheetTable);

        questionLabel = new Label("What is this?", skin);
        questionLabel.setFontScale(1.4f);
        sheetTable.add(questionLabel).padBottom(20).row();

        playerCounter = new Label(playersAnswered + " of " + totalPlayers + " has answered", skin);
        sheetTable.add(playerCounter).padBottom(20).row();

        guessInput = new TextField("", skin);
        guessInput.setMessageText("Write down your guess here...");
        guessInput.setMaxLength(40);
        guessInput.getStyle().font.getData().setScale(1.2f);
        sheetTable.add(guessInput).width(400).height(80).padBottom(20).row();

        submitButton = new TextButton("Guess", skin);
        submitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!hasGuessed && !guessInput.getText().trim().isEmpty()) {
                    hasGuessed = true;
                    submitButton.setText("Sent!");
                    guessInput.setDisabled(true);
                    submitGuess(guessInput.getText().trim());
                }
            }
        });
        sheetTable.add(submitButton).width(150).height(50);

        if (drawingOwnerId.equals(currentPlayerId)) {
            guessInput.setDisabled(true);
            guessInput.setMessageText("You can't guess your own drawing!");
            submitButton.setDisabled(true);
        }

        fetchDrawingFromFirebase();
    }

    private void fetchDrawingFromFirebase() {
        String gameId = GameManager.getInstance().getSession().getGameId();

        game.getFirebase().getPlayerDrawingUrl(
                gameId,
                drawingOwnerId,
                url -> Gdx.app.postRunnable(() -> {
                    if (url != null && !url.isEmpty()) {
                        loadDrawingFromUrl(url);
                    } else {
                        questionLabel.setText("Drawing not available yet.");
                    }
                }),
                error -> Gdx.app.postRunnable(() -> {
                    Gdx.app.error("GuessingScreen", "Error fetching drawing URL", error);
                    questionLabel.setText("Error loading drawing.");
                })
        );
    }

    private void loadDrawingFromUrl(String imageUrl) {
        new Thread(() -> {
            try {
                InputStream input = new URL(imageUrl).openStream();
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                byte[] data = new byte[1024];
                int nRead;
                while ((nRead = input.read(data, 0, data.length)) != -1) {
                    buffer.write(data, 0, nRead);
                }
                buffer.flush();
                byte[] bytes = buffer.toByteArray();

                Pixmap pixmap = new Pixmap(bytes, 0, bytes.length);
                Texture texture = new Texture(pixmap);
                pixmap.dispose();

                Gdx.app.postRunnable(() -> {
                    if (drawingTexture != null) drawingTexture.dispose();
                    drawingTexture = texture;
                    drawingImage.setDrawable(new TextureRegionDrawable(drawingTexture));
                });

            } catch (Exception e) {
                Gdx.app.error("GuessingScreen", "Failed to load image from URL", e);
            }
        }).start();
    }

    private void submitGuess(String guess) {
        Gdx.app.log("GuessingScreen", "Guess submitted: " + guess);
        game.getFirebase().sendGuess(guess);
    }

    @Override public void show() {}
    @Override public void render(float delta) {
        stage.act(delta);
        stage.draw();
    }
    @Override public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override public void dispose() {
        stage.dispose();
        backgroundTexture.dispose();
        if (drawingTexture != null) drawingTexture.dispose();
    }
}
