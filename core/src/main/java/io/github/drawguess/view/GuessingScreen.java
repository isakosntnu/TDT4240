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
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import io.github.drawguess.DrawGuessMain;
import io.github.drawguess.manager.GameManager;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;

public class GuessingScreen implements Screen {

    private final DrawGuessMain game;
    private final Stage stage;

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

    // —— TIMER FIELDS ——
    private Label timerLabel;
    private int guessTimeLeft = 30;
    private Timer.Task guessTimerTask;

    public GuessingScreen(DrawGuessMain game) {
        this.game = game;
        this.stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        Skin skin = new Skin(Gdx.files.internal("uiskin.json"));

        // Bakgrunn
        backgroundTexture = new Texture("guessbg.png");
        Image backgroundImage = new Image(backgroundTexture);
        backgroundImage.setFillParent(true);
        stage.addActor(backgroundImage);

        // Placeholder for drawing
        drawingTexture = new Texture("placeholder.png");
        drawingImage = new Image(drawingTexture);
        drawingImage.setScaling(Scaling.fit);
        drawingImage.setSize(250, 300);
        drawingImage.setPosition(
                (Gdx.graphics.getWidth() - drawingImage.getWidth()) / 2f,
                Gdx.graphics.getHeight() - drawingImage.getHeight() - 80
        );
        stage.addActor(drawingImage);

        // Bottom table
        Table sheetTable = new Table();
        sheetTable.setFillParent(true);
        sheetTable.bottom().padBottom(50);
        stage.addActor(sheetTable);

        // Question
        questionLabel = new Label("What is this?", skin);
        questionLabel.setFontScale(1.4f);
        sheetTable.add(questionLabel).padBottom(20).row();

        // Player counter
        playerCounter = new Label(playersAnswered + " of " + totalPlayers + " has answered", skin);
        sheetTable.add(playerCounter).padBottom(20).row();

        // Guess input
        guessInput = new TextField("", skin);
        guessInput.setMessageText("Write down your guess here...");
        guessInput.setMaxLength(40);
        guessInput.getStyle().font.getData().setScale(1.2f);
        sheetTable.add(guessInput).width(400).height(80).padBottom(20).row();

        // Submit button
        submitButton = new TextButton("Guess", skin);
        submitButton.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                if (!hasGuessed && !guessInput.getText().trim().isEmpty()) {
                    hasGuessed = true;
                    guessTimerTask.cancel();
                    submitButton.setText("Sent!");
                    guessInput.setDisabled(true);
                    submitGuess(guessInput.getText().trim());
                }
            }
        });
        sheetTable.add(submitButton).width(150).height(50);

        // —— TIMER LABEL & TASK ——
        timerLabel = new Label(String.valueOf(guessTimeLeft), skin);
        timerLabel.setFontScale(2f);
        timerLabel.setPosition(
                Gdx.graphics.getWidth() - 60,
                Gdx.graphics.getHeight() - 40
        );
        stage.addActor(timerLabel);

        guessTimerTask = new Timer.Task() {
            @Override public void run() {
                guessTimeLeft--;
                timerLabel.setText(String.valueOf(guessTimeLeft));
                if (guessTimeLeft <= 0) {
                    cancel();
                    if (!hasGuessed) {
                        submitButton.setText("Time!");
                        guessInput.setDisabled(true);
                        submitGuess("");
                    }
                }
            }
        };
        Timer.schedule(guessTimerTask, 1, 1, guessTimeLeft - 1);

        // Fetch and display the drawing
        fetchDrawingFromFirebase();
    }

    private void fetchDrawingFromFirebase() {
        String gameId = GameManager.getInstance().getSession().getGameId();
        String drawingPlayerId = GameManager.getInstance().getSession().getHostPlayer().getId();
        game.getFirebase().getPlayerDrawingUrl(
                gameId,
                drawingPlayerId,
                url -> Gdx.app.postRunnable(() -> loadDrawingFromUrl(url)),
                error -> Gdx.app.error("GuessingScreen", "Failed to get drawing URL", error)
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
        Gdx.app.log("GuessingScreen", "Gjettet: " + guess);
        game.getFirebase().sendGuess(guess);
        
        // Mark player as done with guessing and transition to waiting screen
        String gameId = GameManager.getInstance().getSession().getGameId();
        String playerId = GameManager.getInstance().getPlayerId();
        
        game.getFirebase().setPlayerGuessDone(
            gameId, 
            playerId,
            () -> Gdx.app.postRunnable(() -> 
                game.setScreen(new WaitingScreen(game, true))
            ),
            err -> Gdx.app.error("GuessingScreen", "Failed to mark guess as done", err)
        );
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
