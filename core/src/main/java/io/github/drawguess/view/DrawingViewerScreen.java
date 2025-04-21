package io.github.drawguess.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import io.github.drawguess.DrawGuessMain;
import io.github.drawguess.manager.GameManager;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;

public class DrawingViewerScreen implements Screen {

    private final DrawGuessMain game;
    private final Stage stage;

    private Image imageDisplay;
    private Texture finalTexture;

    private TextField guessInput;
    private TextButton guessButton;
    private Label resultLabel;

    // —— TIMER FIELDS ——
    private Label timerLabel;
    private int guessTimeLeft = 30;
    private Timer.Task guessTimerTask;
    private boolean hasGuessed = false;

    public DrawingViewerScreen(DrawGuessMain game, String imageUrl) {
        this.game = game;
        this.stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        Skin skin = new Skin(Gdx.files.internal("uiskin.json"));

        imageDisplay = new Image();
        imageDisplay.setFillParent(true);
        stage.addActor(imageDisplay);

        new Thread(() -> loadImage(imageUrl)).start();

        float sw = Gdx.graphics.getWidth();
        float sh = Gdx.graphics.getHeight();

        float inputWidth = sw * 0.7f;
        float inputHeight = sh * 0.08f;
        float buttonWidth = sw * 0.35f;
        float buttonHeight = sh * 0.065f;
        float fontScale = sh * 0.0018f;

        // —— TIMER LABEL ——
        timerLabel = new Label(String.valueOf(guessTimeLeft), skin);
        timerLabel.setFontScale(fontScale * 1.2f);
        timerLabel.setPosition(sw - 60, sh - 40);
        stage.addActor(timerLabel);

        // Guess input
        guessInput = new TextField("", skin);
        guessInput.setMessageText("Write your guess...");
        guessInput.setSize(inputWidth, inputHeight);
        guessInput.setPosition((sw - inputWidth) / 2f, sh * 0.12f);
        guessInput.getStyle().font.getData().setScale(fontScale);
        stage.addActor(guessInput);

        // Guess button
        guessButton = new TextButton("Guess", skin);
        guessButton.setSize(buttonWidth, buttonHeight);
        guessButton.setPosition((sw - buttonWidth) / 2f, sh * 0.04f);
        guessButton.getLabel().setFontScale(fontScale * 1.1f);
        stage.addActor(guessButton);

        // Result label
        resultLabel = new Label("", skin);
        resultLabel.setFontScale(fontScale);
        resultLabel.setPosition(20, sh * 0.2f);
        stage.addActor(resultLabel);

        guessButton.addListener(event -> {
            if (event.toString().equals("touchDown") && !hasGuessed) {
                submitGuessAndStopTimer(guessInput.getText().trim());
                return true;
            }
            return false;
        });

        // Back button
        TextButton backButton = new TextButton("Back", skin);
        backButton.setSize(buttonWidth * 0.6f, buttonHeight);
        backButton.setPosition(20, sh - buttonHeight - 20);
        backButton.getLabel().setFontScale(fontScale);
        backButton.addListener(e -> {
            if (e.toString().equals("touchDown")) {
                cancelTimer();
                game.setScreen(new MenuScreen(game));
                return true;
            }
            return false;
        });
        stage.addActor(backButton);

        // —— START TIMER ——
        guessTimerTask = new Timer.Task() {
            @Override public void run() {
                guessTimeLeft--;
                timerLabel.setText(String.valueOf(guessTimeLeft));
                if (guessTimeLeft <= 0) {
                    cancel();
                    if (!hasGuessed) {
                        submitGuessAndStopTimer("");
                    }
                }
            }
        };
        Timer.schedule(guessTimerTask, 1, 1, guessTimeLeft - 1);
    }

    private void submitGuessAndStopTimer(String guess) {
        hasGuessed = true;
        cancelTimer();
        resultLabel.setText("Checking...");
        checkGuess(guess);
        guessButton.setDisabled(true);
        guessInput.setDisabled(true);
    }

    private void cancelTimer() {
        if (guessTimerTask != null) {
            guessTimerTask.cancel();
        }
    }

    private void loadImage(String url) {
        try (InputStream in = new URL(url).openStream();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            byte[] buf = new byte[16_384];
            int n;
            while ((n = in.read(buf)) != -1) out.write(buf, 0, n);

            Pixmap pixmap = new Pixmap(out.toByteArray(), 0, out.size());
            Gdx.app.postRunnable(() -> {
                Pixmap white = new Pixmap(pixmap.getWidth(), pixmap.getHeight(), Pixmap.Format.RGBA8888);
                white.setColor(1, 1, 1, 1);
                white.fill();
                white.drawPixmap(pixmap, 0, 0);

                finalTexture = new Texture(white);
                imageDisplay.setDrawable(new TextureRegionDrawable(new TextureRegion(finalTexture)));
                imageDisplay.setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

                pixmap.dispose();
                white.dispose();
            });
        } catch (Exception e) {
            Gdx.app.error("DrawingViewer", "Failed to load image", e);
        }
    }

    private void checkGuess(String guess) {
        String gameId = GameManager.getInstance().getSession().getGameId();
        String playerId = GameManager.getInstance().getSession().getHostPlayer().getId();

        game.getFirebase().getPlayerWord(
                gameId,
                playerId,
                correctWord -> {
                    boolean correct = correctWord.equalsIgnoreCase(guess.trim());
                    String message = correct
                            ? "✅ Correct!"
                            : "❌ Wrong! It was: " + correctWord;
                    Gdx.app.postRunnable(() -> resultLabel.setText(message));
                },
                error -> Gdx.app.postRunnable(() -> resultLabel.setText("Error checking word."))
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
    @Override public void hide() { dispose(); }
    @Override public void dispose() {
        cancelTimer();
        stage.dispose();
        if (finalTexture != null) finalTexture.dispose();
    }
}
