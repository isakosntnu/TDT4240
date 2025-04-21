package io.github.drawguess.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import io.github.drawguess.DrawGuessMain;
import io.github.drawguess.manager.GameManager;
import io.github.drawguess.model.GameSession;
import io.github.drawguess.model.Player;
import io.github.drawguess.server.FirebaseInterface;

import java.util.ArrayList;
import java.util.List;

public class LobbyScreen implements Screen {

    private final DrawGuessMain game;
    private final Stage stage;

    private Texture backgroundTexture;
    private Image backgroundImage;

    private Table playerTable;
    private List<String> playerNames;
    private GameSession session;
    private TextButton startGameButton;

    private static LobbyScreen instance;

    private float updateTimer = 0f;
    private static final float UPDATE_INTERVAL = 0.5f; // Juster etter behov

    public LobbyScreen(DrawGuessMain game) {
        this.game = game;
        this.stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);
        instance = this;

        this.session = GameManager.getInstance().getSession();
        this.playerNames = new ArrayList<>();

        game.getFirebase().emitUserJoined(session.getGameId(), session.getHostPlayer().getName());
        updateLobbyFromFirestore();

        Skin skin = new Skin(Gdx.files.internal("uiskin.json"));
        float screenHeight = Gdx.graphics.getHeight();

        backgroundTexture = new Texture("board.png");
        backgroundImage = new Image(backgroundTexture);
        backgroundImage.setFillParent(true);
        stage.addActor(backgroundImage);

        Table rootTable = new Table();
        rootTable.setFillParent(true);
        rootTable.top().padTop(screenHeight * 0.15f);
        stage.addActor(rootTable);

        Label pinLabel = new Label("GAME PIN: " + session.getGameId(), skin);
        pinLabel.setFontScale(screenHeight * 0.002f);
        rootTable.add(pinLabel).padBottom(40).row();

        Label title = new Label("Waiting on players...", skin);
        title.setFontScale(screenHeight * 0.0015f);
        rootTable.add(title).padBottom(50).row();

        playerTable = new Table();
        rootTable.add(playerTable);

        for (Player player : session.getPlayers()) {
            addPlayer(player.getName());
        }

        startGameButton = new TextButton("Start Game", skin);
        startGameButton.getLabel().setFontScale(screenHeight * 0.0015f);
        rootTable.row().padTop(60);
        rootTable.add(startGameButton).expandY().bottom().padBottom(30);

        startGameButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new DrawingScreen(game));
            }
        });
    }

    public void addPlayer(String name) {
        if (name == null || name.trim().isEmpty()) {
            Gdx.app.log("LobbyScreen", "⚠️ Ignorerer tomt navn!");
            return;
        }

        if (playerNames.contains(name)) {
            Gdx.app.log("LobbyScreen", "🔁 Spiller allerede i lobbyen: " + name);
            return;
        }

        Skin skin = new Skin(Gdx.files.internal("uiskin.json"));
        Label playerLabel = new Label(name, skin);
        playerLabel.setFontScale(Gdx.graphics.getHeight() * 0.0015f);

        playerNames.add(name);
        playerTable.add(playerLabel).padBottom(8).row();

        Gdx.app.log("LobbyScreen", "👤 Ny spiller lagt til: " + name);
    }

    public static void onPlayerJoined(String playerName) {
        if (instance != null) {
            Gdx.app.postRunnable(() -> {
                instance.addPlayer(playerName);
                instance.updateLobbyFromFirestore(); // Sørg for at hele listen er korrekt
            });
        } else {
            Gdx.app.log("LobbyScreen", "❌ Instance er null, kunne ikke legge til spiller: " + playerName);
        }
    }

    public static void updateLobbyExternally() {
        if (instance != null) {
            instance.updateLobbyFromFirestore();
        }
    }

    private void updateLobbyFromFirestore() {
        String gameId = session.getGameId();
        game.getFirebase().getPlayersInLobby(
                gameId,
                new FirebaseInterface.SuccessCallback<List<String>>() {
                    @Override
                    public void onSuccess(List<String> players) {
                        Gdx.app.postRunnable(() -> {
                            playerNames.clear();
                            playerTable.clearChildren();

                            for (String player : players) {
                                addPlayer(player);
                            }
                        });
                    }
                },
                new FirebaseInterface.FailureCallback() {
                    @Override
                    public void onFailure(Exception e) {
                        Gdx.app.log("LobbyScreen", "❌ Feil ved henting av spillere fra Firebase: " + e.getMessage());
                    }
                }
        );
    }

    @Override
    public void render(float delta) {
        updateTimer += delta;
        if (updateTimer >= UPDATE_INTERVAL) {
            updateTimer = 0f;
            updateLobbyFromFirestore();
        }

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() { }

    @Override
    public void resume() { }

    @Override
    public void hide() {
        dispose();
    }

    @Override
    public void show() { }

    @Override
    public void dispose() {
        stage.dispose();
        backgroundTexture.dispose();
    }
}
