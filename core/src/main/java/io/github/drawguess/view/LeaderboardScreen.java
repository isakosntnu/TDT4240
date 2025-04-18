package io.github.drawguess.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import io.github.drawguess.DrawGuessMain;

public class LeaderboardScreen implements Screen {

    private final DrawGuessMain game;
    private final Stage stage;

    private Texture backgroundTexture;
    private Image backgroundImage;

    public LeaderboardScreen(DrawGuessMain game) {
        this.game = game;
        this.stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        Skin skin = new Skin(Gdx.files.internal("uiskin.json"));

        // Bakgrunn
        backgroundTexture = new Texture("board.png");
        backgroundImage = new Image(backgroundTexture);
        backgroundImage.setFillParent(true);
        stage.addActor(backgroundImage);

        // Rot-tabell
        Table rootTable = new Table();
        rootTable.setFillParent(true);
        rootTable.top().padTop(150);
        stage.addActor(rootTable);

        // Tittel
        Label pinLabel = new Label("LEADERBOARD", skin);
        pinLabel.setFontScale(2f);
        rootTable.add(pinLabel).padBottom(40).row();

        // Tabell for spillere
        Table playerTable = new Table();
        rootTable.add(playerTable).width(Gdx.graphics.getWidth() * 0.9f).row(); // bredere tabell

        // Eksempel-data
        String[] names = {"Ninon", "Arya", "Benjamin", "Jacob", "Isak"};
        int[] scores = {660, 630, 580, 540, 510};

        for (int i = 0; i < names.length; i++) {
            Label nameLabel = new Label(names[i], skin);
            nameLabel.setFontScale(1.5f);
            Label scoreLabel = new Label(String.valueOf(scores[i]), skin);
            scoreLabel.setFontScale(1.5f);

            playerTable.add(nameLabel).expandX().left().padBottom(15).padLeft(100).spaceRight(150);
            playerTable.add(scoreLabel).right().padBottom(15).padRight(100);
            playerTable.row();
        }

        // Tilbake-knapp
        Texture backTexture = new Texture("backbtn.png");
        Image backButton = new Image(backTexture);
        backButton.setSize(100, 70);
        backButton.setPosition(30, Gdx.graphics.getHeight() - backButton.getHeight() - 30);
        backButton.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                game.setScreen(new MenuScreen(game));
                return true;
            }
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
    @Override public void hide() {
        dispose();
    }
    @Override public void dispose() {
        stage.dispose();
        backgroundTexture.dispose();
    }
}
