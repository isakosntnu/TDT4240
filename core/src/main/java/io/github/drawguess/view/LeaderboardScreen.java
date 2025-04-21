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
import io.github.drawguess.manager.GameManager;
import io.github.drawguess.model.Player;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class LeaderboardScreen implements Screen {

    private final DrawGuessMain game;
    private final Stage stage;

    private Texture backgroundTexture;

    public LeaderboardScreen(DrawGuessMain game) {
        this.game = game;
        this.stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        Skin skin = new Skin(Gdx.files.internal("uiskin.json"));

        // background
        backgroundTexture = new Texture("board.png");
        Image bg = new Image(backgroundTexture);
        bg.setFillParent(true);
        stage.addActor(bg);

        // table
        Table root = new Table();
        root.setFillParent(true);
        root.top().padTop(150);
        stage.addActor(root);

        // title
        Label title = new Label("LEADERBOARD", skin);
        title.setFontScale(2f);
        root.add(title).padBottom(40).row();

        // player scores
        Table tbl = new Table();
        root.add(tbl).width(Gdx.graphics.getWidth() * 0.9f).row();

        // fetch & sort
        List<Player> players = GameManager.getInstance().getSession().getPlayers();
        Collections.sort(players, Comparator.comparingInt(Player::getScore).reversed());

        for (Player p : players) {
            Label name = new Label(p.getName(), skin);
            name.setFontScale(1.5f);
            Label score = new Label(String.valueOf(p.getScore()), skin);
            score.setFontScale(1.5f);

            tbl.add(name).expandX().left().padBottom(15).padLeft(100).spaceRight(150);
            tbl.add(score).right().padBottom(15).padRight(100);
            tbl.row();
        }

        // back button
        Texture backTex = new Texture("backbtn.png");
        Image backBtn = new Image(backTex);
        backBtn.setSize(100,70);
        backBtn.setPosition(30, Gdx.graphics.getHeight()-100);
        backBtn.addListener(new InputListener(){
            @Override public boolean touchDown(InputEvent e, float x, float y, int ptr, int btn) {
                game.setScreen(new MenuScreen(game));
                return true;
            }
        });
        stage.addActor(backBtn);
    }

    @Override public void show() {}
    @Override public void render(float delta) {
        stage.act(delta);
        stage.draw();
    }
    @Override public void resize(int w, int h) {
        stage.getViewport().update(w, h, true);
    }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() { dispose(); }
    @Override public void dispose() {
        stage.dispose();
        backgroundTexture.dispose();
    }
}
