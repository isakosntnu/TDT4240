package io.github.drawguess.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import io.github.drawguess.DrawGuessMain;

import java.util.ArrayList;
import java.util.List;

public class WaitingForPlayersScreen implements Screen {

    private final DrawGuessMain game;
    private final Stage stage;

    private Texture backgroundTexture;
    private Image backgroundImage;

    private Table playerTable;
    private List<Label> playerLabels;

    public WaitingForPlayersScreen(DrawGuessMain game) {
        this.game = game;
        this.stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        Skin skin = new Skin(Gdx.files.internal("uiskin.json"));
        playerLabels = new ArrayList<>();

        // Bakgrunn
        backgroundTexture = new Texture("board.png");
        backgroundImage = new Image(backgroundTexture);
        backgroundImage.setFillParent(true);
        stage.addActor(backgroundImage);

        // Layout table
        Table rootTable = new Table();
        rootTable.setFillParent(true);
        rootTable.top().padTop(150); 
        stage.addActor(rootTable);

        // Game PIN 
        Label pinLabel = new Label("GAME PIN: 34782", skin); //TODO: Endre til dynamisk gamepin
        pinLabel.setFontScale(2f);
        rootTable.add(pinLabel).padBottom(40).row();

        Label title = new Label("Waiting on players", skin);
        title.setFontScale(1.5f);
        rootTable.add(title).padBottom(50).row();

        // Tabell for spillere
        playerTable = new Table();
        rootTable.add(playerTable);

        //Eksempel
        addPlayer("Benjamin");
        addPlayer("Isak");
        addPlayer("Arya");
        addPlayer("Jacob");
    }

    public void addPlayer(String name) {
        Skin skin = new Skin(Gdx.files.internal("uiskin.json"));
        Label playerLabel = new Label(name, skin);
        playerLabel.setFontScale(1.3f);
        playerLabels.add(playerLabel);
        playerTable.add(playerLabel).padBottom(8).row();
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

    @Override
    public void dispose() {
        stage.dispose();
        backgroundTexture.dispose();
    }
}
