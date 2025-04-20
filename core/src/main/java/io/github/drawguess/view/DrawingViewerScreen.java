package io.github.drawguess.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import io.github.drawguess.DrawGuessMain;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;

public class DrawingViewerScreen implements Screen {

    private final DrawGuessMain game;
    private final Stage stage;
    private Image imageDisplay;

    public DrawingViewerScreen(DrawGuessMain game, String imageUrl) {
        this.game = game;
        this.stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        Skin skin = new Skin(Gdx.files.internal("uiskin.json"));

        // Image placeholder
        imageDisplay = new Image();
        stage.addActor(imageDisplay);

        // Load image async
        new Thread(() -> {
            try {
                InputStream input = new URL(imageUrl).openStream();
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                byte[] data = new byte[16384];
                int nRead;
                while ((nRead = input.read(data, 0, data.length)) != -1) {
                    buffer.write(data, 0, nRead);
                }
                buffer.flush();
                byte[] bytes = buffer.toByteArray();

                Pixmap pixmap = new Pixmap(bytes, 0, bytes.length);
                Gdx.app.log("DrawingViewer", "Loaded pixmap: " + pixmap.getWidth() + "x" + pixmap.getHeight());

                Texture texture = new Texture(pixmap);
                TextureRegion region = new TextureRegion(texture);

                Gdx.app.postRunnable(() -> {
                    float screenWidth = Gdx.graphics.getWidth();
                    float screenHeight = Gdx.graphics.getHeight();
                
                    // --- Lag hvit bakgrunn ---
                    Pixmap white = new Pixmap(pixmap.getWidth(), pixmap.getHeight(), Pixmap.Format.RGBA8888);
                    white.setColor(1, 1, 1, 1); // Hvit
                    white.fill();
                    white.drawPixmap(pixmap, 0, 0); // Lim inn det originale bildet oppå
                    Texture finalTexture = new Texture(white);
                
                    float aspectRatio = white.getWidth() / (float) white.getHeight();
                
                    float targetWidth = screenWidth;
                    float targetHeight = targetWidth / aspectRatio;
                
                    if (targetHeight > screenHeight) {
                        targetHeight = screenHeight;
                        targetWidth = targetHeight * aspectRatio;
                    }
                
                    imageDisplay.setDrawable(new TextureRegionDrawable(new TextureRegion(finalTexture)));
                    imageDisplay.setSize(targetWidth, targetHeight);
                    imageDisplay.setPosition(
                        (screenWidth - targetWidth) / 2f,
                        (screenHeight - targetHeight) / 2f
                    );
                
                    // Rydd opp
                    pixmap.dispose();
                    white.dispose();
                });
                

            } catch (Exception e) {
                Gdx.app.error("DrawingViewer", "Failed to load image from Firebase", e);
            }
        }).start();

        // Back button
        TextButton backButton = new TextButton("Back", skin);
        backButton.setPosition(20, 20);
        backButton.addListener(event -> {
            if (event.toString().equals("touchDown")) {
                game.setScreen(new MenuScreen(game));
                return true;
            }
            return false;
        });
        stage.addActor(backButton);
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
    }
}
