package io.github.drawguess.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import io.github.drawguess.DrawGuessMain;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;

public class LeaderboardScreen implements Screen {
    private final DrawGuessMain game;
    private Stage stage;

    private Texture bgTexture;
    private Texture leaderboardTexture;
    private Image leaderboardImage;
    private BitmapFont font;

    public LeaderboardScreen(DrawGuessMain game) {
        this.game = game;
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        // 1. Bakgrunn
        bgTexture = new Texture("bg0.png");
        Image bgImage = new Image(bgTexture);
        bgImage.setFillParent(true);
        stage.addActor(bgImage);

        // 2. Leaderboard-bilde
        leaderboardTexture = new Texture("leaderboard.png");
        leaderboardImage = new Image(leaderboardTexture);

        float width = Gdx.graphics.getWidth() * 0.8f;
        float height = Gdx.graphics.getHeight() * 0.8f;
        leaderboardImage.setSize(width, height);
        leaderboardImage.setPosition(
            (Gdx.graphics.getWidth() - width) / 2f,
            (Gdx.graphics.getHeight() - height) / 2f
        );
        stage.addActor(leaderboardImage); // Må legges før tabellen

        // 3. Font
        font = new BitmapFont();
        font.getData().setScale(2f);
        Label.LabelStyle labelStyle = new Label.LabelStyle(font, Color.BLACK);

        // 4. Tabellen plasseres oppå tavla
        Table table = new Table();

        // Beregn posisjonen relativt til leaderboard-bildet
        float tableX = leaderboardImage.getX(); // 60px fra venstre kant av brettet
        float tableY = leaderboardImage.getY(); // Y-posisjon starter fra bunnen i libGDX
        float tableWidth = leaderboardImage.getWidth() - 120;
        float tableHeight = leaderboardImage.getHeight() - 180;

        table.setSize(tableWidth, tableHeight);
        table.setPosition(tableX, tableY);
        table.top(); // Juster innholdet til toppen av tabellen

        // 5. Eksempel-data
        String[] names = {"Liam", "Emma", "Sofie", "Noah", "Lucas"};
        int[] scores = {660, 630, 580, 540, 510};

        for (int i = 0; i < names.length; i++) {
            Label nameLabel = new Label(names[i], labelStyle);
            Label scoreLabel = new Label(String.valueOf(scores[i]), labelStyle);
            table.add(nameLabel).expandX().left().padBottom(20);
            table.add(scoreLabel).right().padBottom(20);
            table.add(nameLabel).expandX().left().padBottom(20).padRight(20);
            table.add(scoreLabel).right().padBottom(20);
            table.row();
        }
        
        stage.addActor(table); // Legg tabellen etter leaderboard-bildet

        // 6. Tilbake-knapp
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
        bgTexture.dispose();
        leaderboardTexture.dispose();
        font.dispose();
    }
}
