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
import java.util.List;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

public class DrawingViewerScreen implements Screen {

    private final DrawGuessMain game;
    private final Stage stage;
    private final Skin skin;

    private final List<Map.Entry<String,String>> drawings;
    private int currentIndex = 0;

    // UI‑elements
    private Image imageDisplay;
    private Texture finalTexture;
    private Label timerLabel;
    private TextField guessInput;
    private TextButton guessButton;
    private Label resultLabel;

    // Timer
    private int guessTimeLeft;
    private Timer.Task guessTimerTask;
    private boolean hasGuessed;

    public DrawingViewerScreen(DrawGuessMain game, Map<String,String> drawingsMap) {
        this.game = game;
        this.stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);
        this.skin = new Skin(Gdx.files.internal("uiskin.json"));
        this.drawings = new ArrayList<>(drawingsMap.entrySet());

        buildUi();
        loadCurrentDrawing();
    }


    private void buildUi() {
        float sw = Gdx.graphics.getWidth();
        float sh = Gdx.graphics.getHeight();

        imageDisplay = new Image();
        imageDisplay.setFillParent(true);
        stage.addActor(imageDisplay);

        // Timer‑label
        timerLabel = new Label("", skin);
        timerLabel.setPosition(sw - 60, sh - 40);
        stage.addActor(timerLabel);

        // Guess‑input
        guessInput = new TextField("", skin);
        guessInput.setMessageText("Your guess...");
        guessInput.setSize(sw * 0.7f, sh * 0.08f);
        guessInput.setPosition((sw - guessInput.getWidth())/2f, sh * 0.12f);
        stage.addActor(guessInput);

        // Guess‑button
        guessButton = new TextButton("Guess", skin);
        guessButton.setSize(sw * 0.35f, sh * 0.065f);
        guessButton.setPosition((sw - guessButton.getWidth())/2f, sh * 0.04f);
        guessButton.addListener(evt -> {
            if (evt.toString().equals("touchDown") && !hasGuessed) {
                submitGuessAndStopTimer(guessInput.getText().trim());
                return true;
            }
            return false;
        });
        stage.addActor(guessButton);

        resultLabel = new Label("", skin);
        resultLabel.setPosition(sw / 2f, sh * 0.3f);
        resultLabel.setAlignment(com.badlogic.gdx.utils.Align.center);
        stage.addActor(resultLabel);
    }


    private void loadCurrentDrawing() {
        if (currentIndex >= drawings.size()) {
            finishGuessingRound();
            return;
        }

        // Reset UI & timer
        hasGuessed     = false;
        guessTimeLeft  = 30;
        timerLabel.setText(String.valueOf(guessTimeLeft));
        resultLabel.setText("");
        resultLabel.setColor(1, 1, 1, 1); // Reset to white color
        guessInput.setText("");
        guessInput.setDisabled(false);
        guessButton.setDisabled(false);

        // Download url
        String url = drawings.get(currentIndex).getValue();
        new Thread(() -> loadImage(url)).start();

        // Starts counter
        guessTimerTask = new Timer.Task() {
            @Override public void run() {
                guessTimeLeft--;
                timerLabel.setText(String.valueOf(guessTimeLeft));
                if (guessTimeLeft <= 0) {
                    cancel();
                    if (!hasGuessed) submitGuessAndStopTimer("");
                }
            }
        };
        Timer.schedule(guessTimerTask, 1, 1, guessTimeLeft - 1);
    }

    //Sends guess to firebase and stops timer. 
    private void submitGuessAndStopTimer(String guess) {
        hasGuessed = true;
        if (guessTimerTask != null) guessTimerTask.cancel();
        guessInput.setDisabled(true);
        guessButton.setDisabled(true);
        resultLabel.setText("Checking…");

        String gameId   = GameManager.getInstance().getSession().getGameId();
        String targetId = drawings.get(currentIndex).getKey();

        game.getFirebase().getPlayerWord(
                gameId, targetId,
                correctWord -> {
                    boolean correct = correctWord.equalsIgnoreCase(guess);
                    int points = correct ? 10 + (guessTimeLeft * 2) : 0;
                    String msg = correct
                            ? "CORRECT! +" + points + "P"
                            : "WRONG! WAS: " + correctWord.toUpperCase();
                    
                    Gdx.app.postRunnable(() -> {
                        // Set result label text
                        resultLabel.setText(msg);
                        if (correct) {
                            resultLabel.setColor(0, 0.8f, 0, 1); // Green
                        } else {
                            resultLabel.setColor(1, 0, 0, 1); // Red
                        }
                        

                        float screenHeight = Gdx.graphics.getHeight();
                        float resultFontScale = screenHeight * 0.0025f; 
                        resultLabel.setFontScale(resultFontScale);
                    });

                    String me = GameManager.getInstance().getPlayerId();
                    game.getFirebase().submitGuessResult(
                            gameId, me, points,
                            () -> {
                                Timer.schedule(new Timer.Task(){
                                    @Override public void run() {
                                        currentIndex++;
                                        Gdx.app.postRunnable(DrawingViewerScreen.this::loadCurrentDrawing);
                                    }
                                }, 5);
                            },
                            err -> Gdx.app.error("DrawingViewer", "Failed submitting points", err)
                    );
                },
                err -> Gdx.app.postRunnable(() -> resultLabel.setText("Error checking word."))
        );
    }


    private void finishGuessingRound() {
        String gameId = GameManager.getInstance().getSession().getGameId();
        String me     = GameManager.getInstance().getPlayerId();

        resultLabel.setText("Finishing up...");
        guessInput.setDisabled(true);
        guessButton.setDisabled(true);

        Gdx.app.log("DrawingViewer", "Marking player " + me + " as finished guessing");
        
        game.getFirebase().setPlayerGuessDone(
                gameId, me,
                () -> {
                    game.getFirebase().getPlayersGuessStatus(
                        gameId,
                        statuses -> {
                            Boolean myStatus = statuses.get(me);
                            Gdx.app.log("DrawingViewer", "Player " + me + " guess status: " + myStatus);
                            

                            if (myStatus != null && myStatus) {
                                Gdx.app.postRunnable(() -> game.setScreen(new WaitingScreen(game)));
                            } else {
                                Gdx.app.error("DrawingViewer", "Failed to confirm guess status, retrying...");
                                Timer.schedule(new Timer.Task() {
                                    @Override
                                    public void run() {
                                        finishGuessingRound();
                                    }
                                }, 1);
                            }
                        },
                        err -> {
                            Gdx.app.error("DrawingViewer", "Could not verify guess-done status", err);
                            Gdx.app.postRunnable(() -> game.setScreen(new WaitingScreen(game)));
                        }
                    );
                },
                err -> Gdx.app.error("DrawingViewer","Could not set guess-done",err)
        );
    }

    private void loadImage(String url) {
        try (InputStream in = new URL(url).openStream();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            byte[] buf = new byte[16_384];
            int n;
            while ((n = in.read(buf)) != -1) out.write(buf, 0, n);

            Pixmap pix = new Pixmap(out.toByteArray(), 0, out.size());
            Gdx.app.postRunnable(() -> {
                Pixmap white = new Pixmap(pix.getWidth(), pix.getHeight(), Pixmap.Format.RGBA8888);
                white.setColor(1,1,1,1);
                white.fill();
                white.drawPixmap(pix, 0, 0);
                finalTexture = new Texture(white);
                imageDisplay.setDrawable(new TextureRegionDrawable(new TextureRegion(finalTexture)));
                imageDisplay.setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
                pix.dispose(); white.dispose();
            });
        } catch (Exception e) {
            Gdx.app.error("DrawingViewer", "Failed load image", e);
        }
    }

    @Override public void render(float delta) { stage.act(delta); stage.draw(); }
    @Override public void resize(int w, int h) { stage.getViewport().update(w,h,true); }
    @Override public void show() {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() { dispose(); }
    @Override
    public void dispose() {
        if (guessTimerTask != null) guessTimerTask.cancel();
        stage.dispose();
        if (finalTexture != null) finalTexture.dispose();
    }
}
