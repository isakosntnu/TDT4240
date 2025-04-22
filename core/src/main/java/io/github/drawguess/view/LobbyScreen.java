package io.github.drawguess.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import java.util.ArrayList;
import java.util.List;

import io.github.drawguess.DrawGuessMain;
import io.github.drawguess.controller.GameController;
import io.github.drawguess.manager.GameManager;
import io.github.drawguess.model.GameSession;
import io.github.drawguess.model.Player;
import io.github.drawguess.server.FirebaseInterface;
import io.github.drawguess.server.SocketInterface;

public class LobbyScreen implements Screen {

    private final DrawGuessMain game;
    private final Stage stage;
    private final Skin skin;

    private Texture backgroundTexture;
    private Image backgroundImage;

    private Table playerTable;
    private List<String> playerNames;
    private GameSession session;
    private TextButton startGameButton;
    private Label waitingMessageLabel;

    private final SocketInterface socketHandler;
    private static LobbyScreen instance;

    private float updateTimer = 0f;
    private static final float UPDATE_INTERVAL = 0.5f;

    private float screenHeight = Gdx.graphics.getHeight();
    private float screenWidth = Gdx.graphics.getWidth(); 
    private float baseFontScale = screenHeight * 0.0018f; 

    public LobbyScreen(DrawGuessMain game) {
        this.game = game;
        this.socketHandler = game.getSocket(); 
        this.stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);
        instance = this;

        this.session = GameManager.getInstance().getSession();
        this.playerNames = new ArrayList<>();

        game.getFirebase().emitUserJoined(session.getGameId(), session.getHostPlayer().getName());
        updateLobbyFromFirestore();

        skin = new Skin(Gdx.files.internal("uiskin.json"));

        backgroundTexture = new Texture("board.png");
        backgroundImage = new Image(backgroundTexture);
        backgroundImage.setFillParent(true);
        stage.addActor(backgroundImage);

        Table rootTable = new Table();
        rootTable.setFillParent(true);
        rootTable.top().padTop(screenHeight * 0.2f);
        stage.addActor(rootTable);

        Label pinLabel = new Label("GAME PIN: " + session.getGameId(), skin);
        pinLabel.setFontScale(baseFontScale * 1.1f); // Slightly larger for PIN
        rootTable.add(pinLabel).padBottom(screenHeight * 0.05f).row();

        Label title = new Label("Waiting on players...", skin);
        title.setFontScale(baseFontScale);
        rootTable.add(title).padBottom(screenHeight * 0.06f).row();

        playerTable = new Table();
        rootTable.add(playerTable);

        for (Player player : session.getPlayers()) {
            addPlayer(player.getName());
        }

        startGameButton = new TextButton("Start Game", skin);
        startGameButton.getLabel().setFontScale(baseFontScale);
        rootTable.row().padTop(screenHeight * 0.07f);
        rootTable.add(startGameButton).expandY().bottom().padBottom(screenHeight * 0.01f);

        waitingMessageLabel = new Label("Cannot start without two or more players", skin);
        waitingMessageLabel.setFontScale(baseFontScale * 0.9f);
        waitingMessageLabel.setColor(1, 0.8f, 0.8f, 1);
        rootTable.row();
        rootTable.add(waitingMessageLabel).padBottom(screenHeight * 0.04f);

        startGameButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                String gameId = session.getGameId();
                socketHandler.emitStartGame(gameId); 
            }
        });
        
    }

    public static void onPlayerJoined(String playerName) {
        if (instance != null) {
            Gdx.app.postRunnable(() -> {
                instance.addPlayer(playerName);
                instance.updateLobbyFromFirestore();
            });
        }
    }


    public static void onJoinRejected(String reason) {
        if (instance != null) {
            Gdx.app.postRunnable(() -> instance.showPopup(reason));
        }
    }

    private void showPopup(String message) {
        Dialog dialog = new Dialog("Join Rejected", skin);
        dialog.text(message);
        dialog.button("OK", true);
        dialog.pad(20);
        dialog.show(stage);
    }

    private void updateLobbyFromFirestore() {
        String gameId = session.getGameId();
        game.getFirebase().getPlayersInLobby(
            gameId,
            players -> Gdx.app.postRunnable(() -> {
                playerNames.clear();
                playerTable.clearChildren();
                for (String player : players) {
                    addPlayer(player);
                }
                boolean canStart = players.size() >= 2;
                startGameButton.setDisabled(!canStart);
                startGameButton.setVisible(canStart);
                waitingMessageLabel.setVisible(!canStart);
            }),
            e -> Gdx.app.log("LobbyScreen", "error" + e.getMessage())
        );
    }

    public void addPlayer(String name) {
        if (name == null || name.trim().isEmpty()) return;
        if (playerNames.contains(name)) return;

        Label playerLabel = new Label(name, skin);
        playerLabel.setFontScale(baseFontScale);

        playerNames.add(name);
        playerTable.add(playerLabel).padBottom(Gdx.graphics.getHeight() * 0.01f).row();

        Gdx.app.log("LobbyScreen", "Player added" + name);
    }

    public static void onGameStarted() {
        if (instance != null) {
            Gdx.app.postRunnable(() -> {
                DrawGuessMain game = GameManager.getGameInstance();
    
                GameController controller = GameManager.getInstance().getGameController();
                if (controller != null) {
                    Gdx.app.log("LobbyScreen", "Using existing GameController");
                    controller.startGame(game);
                } else {
                    Gdx.app.log("LobbyScreen", "Fallback to DrawingScreen");
                    game.setScreen(new DrawingScreen(game));
                }
            });
        }
    }
    
    
    
    

    public static void updateLobbyExternally() {
        if (instance != null) {
            instance.updateLobbyFromFirestore();
        }
    }

    @Override
    public void show() {
        socketHandler.registerLobbyListeners();
    }

    @Override
    public void hide() {
        socketHandler.unregisterLobbyListeners();
        dispose();
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

    @Override public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void dispose() {
        stage.dispose();
        backgroundTexture.dispose();
    }
}
