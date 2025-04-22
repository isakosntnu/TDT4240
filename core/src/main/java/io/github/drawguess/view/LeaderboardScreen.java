package io.github.drawguess.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import io.github.drawguess.DrawGuessMain;
import io.github.drawguess.manager.GameManager;
import io.github.drawguess.model.Player;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LeaderboardScreen implements Screen {

    private final DrawGuessMain game;
    private final Stage stage;
    private Texture backgroundTexture;
    private Table playerTable;
    private Label statusLabel;

    public LeaderboardScreen(DrawGuessMain game) {
        this.game = game;
        this.stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        Skin skin = new Skin(Gdx.files.internal("uiskin.json"));
        float screenHeight = Gdx.graphics.getHeight();
        float screenWidth = Gdx.graphics.getWidth();
        float baseFontScale = screenHeight * 0.0018f; // Base font scale
        float titleScale = baseFontScale * 1.3f; // Larger scale for title
        float statusScale = baseFontScale * 0.9f; // Smaller scale for status

        // 1) bakgrunn
        backgroundTexture = new Texture("board.png");
        Image bg = new Image(backgroundTexture);
        bg.setFillParent(true);
        stage.addActor(bg);

        // 2) layout
        Table root = new Table();
        root.setFillParent(true);
        root.top().padTop(screenHeight * 0.2f);
        stage.addActor(root);

        // 3) tittel
        Label title = new Label("LEADERBOARD", skin);
        title.setFontScale(titleScale);
        root.add(title).padBottom(screenHeight * 0.05f).row();

        // 4) player table placeholder
        playerTable = new Table();
        root.add(playerTable).width(screenWidth * 0.9f).row();
        
        // 5) status label
        statusLabel = new Label("Loading scores...", skin);
        statusLabel.setFontScale(statusScale);
        root.add(statusLabel).padTop(screenHeight * 0.02f).row();

        // 6) tilbake‑knapp
        Texture backTex = new Texture("backbtn.png");
        Image backBtn = new Image(backTex);
        backBtn.setSize(screenWidth * 0.15f, screenHeight * 0.07f);
        backBtn.setPosition(screenWidth * 0.03f, screenHeight - backBtn.getHeight() - screenHeight * 0.03f);
        backBtn.addListener(new InputListener() {
            @Override public boolean touchDown(InputEvent e, float x, float y, int ptr, int btn) {
                game.setScreen(new MenuScreen(game));
                return true;
            }
        });
        stage.addActor(backBtn);
        
        // Fetch latest scores from Firebase
        fetchAllPlayerScores();
    }
    
    /**
     * Fetches all players and their scores from Firebase and updates the UI
     */
    private void fetchAllPlayerScores() {
        String gameId = GameManager.getInstance().getSession().getGameId();
        
        game.getFirebase().getAllPlayerProfiles(
            gameId,
            playerProfiles -> {
                // Build the leaderboard with all player data from Firebase
                Gdx.app.postRunnable(() -> buildLeaderboardTable(playerProfiles));
            },
            e -> {
                Gdx.app.error("LeaderboardScreen", "Failed to fetch player profiles", e);
                Gdx.app.postRunnable(() -> {
                    statusLabel.setText("Failed to load scores. Using local data.");
                    // Fallback to using local player data
                    List<Player> localPlayers = GameManager.getInstance().getSession().getPlayers();
                    buildLeaderboardWithLocalPlayers(localPlayers);
                });
            }
        );
    }
    
    /**
     * Builds the leaderboard table UI with player data from Firebase
     */
    private void buildLeaderboardTable(List<Map<String, Object>> playerProfiles) {
        Skin skin = new Skin(Gdx.files.internal("uiskin.json"));
        float screenHeight = Gdx.graphics.getHeight();
        float screenWidth = Gdx.graphics.getWidth();
        float baseFontScale = screenHeight * 0.0018f; // Base font scale (match constructor)
        float headerScale = baseFontScale * 1.1f; // Scale for headers
        float rowScale = baseFontScale * 1.2f;    // Scale for player rows
        float horizontalPadding = screenWidth * 0.1f; // Padding for table content

        playerTable.clear();
        statusLabel.setText("");
        
        // Sort by score in descending order
        Collections.sort(playerProfiles, (p1, p2) -> {
            Integer score1 = (Integer) p1.get("score");
            Integer score2 = (Integer) p2.get("score");
            return score2.compareTo(score1); // Descending order
        });

        // Add header row
        Label nameHeader = new Label("PLAYER", skin);
        Label scoreHeader = new Label("SCORE", skin);
        nameHeader.setFontScale(headerScale);
        scoreHeader.setFontScale(headerScale);
        
        playerTable.add(nameHeader).expandX().left().padLeft(horizontalPadding).padBottom(screenHeight * 0.02f);
        playerTable.add(scoreHeader).right().padRight(horizontalPadding).padBottom(screenHeight * 0.02f).row();
        
        // Add player rows
        for (Map<String, Object> profile : playerProfiles) {
            String name = (String) profile.get("name");
            Integer score = (Integer) profile.get("score");
            
            Label nameLabel = new Label(name, skin);
            Label scoreLabel = new Label(score.toString(), skin);
            nameLabel.setFontScale(rowScale);
            scoreLabel.setFontScale(rowScale);
            
            // Highlight current player
            String currentPlayerId = GameManager.getInstance().getPlayerId();
            if (currentPlayerId != null && currentPlayerId.equals(profile.get("id"))) {
                nameLabel.setText("➤ " + name);
            }

            playerTable.add(nameLabel)
                    .expandX()
                    .left()
                    .padBottom(screenHeight * 0.015f)
                    .padLeft(horizontalPadding)
                    .spaceRight(screenWidth * 0.1f);
            playerTable.add(scoreLabel)
                    .right()
                    .padBottom(screenHeight * 0.015f)
                    .padRight(horizontalPadding);
            playerTable.row();
        }
    }
    
    /**
     * Fallback method to build leaderboard with only local player data
     */
    private void buildLeaderboardWithLocalPlayers(List<Player> players) {
        Skin skin = new Skin(Gdx.files.internal("uiskin.json"));
        float screenHeight = Gdx.graphics.getHeight();
        float screenWidth = Gdx.graphics.getWidth();
        float baseFontScale = screenHeight * 0.0018f; // Base font scale (match constructor)
        float headerScale = baseFontScale * 1.1f; // Scale for headers
        float rowScale = baseFontScale * 1.2f;    // Scale for player rows
        float horizontalPadding = screenWidth * 0.1f; // Padding for table content

        playerTable.clear();
        
        // Sort by score in descending order
        Collections.sort(players, Comparator.comparingInt(Player::getScore).reversed());

        // Add header row
        Label nameHeader = new Label("PLAYER", skin);
        Label scoreHeader = new Label("SCORE", skin);
        nameHeader.setFontScale(headerScale);
        scoreHeader.setFontScale(headerScale);
        
        playerTable.add(nameHeader).expandX().left().padLeft(horizontalPadding).padBottom(screenHeight * 0.02f);
        playerTable.add(scoreHeader).right().padRight(horizontalPadding).padBottom(screenHeight * 0.02f).row();
        
        for (Player p : players) {
            Label name = new Label(p.getName(), skin);
            name.setFontScale(rowScale);
            Label score = new Label(String.valueOf(p.getScore()), skin);
            score.setFontScale(rowScale);

            playerTable.add(name)
                    .expandX()
                    .left()
                    .padBottom(screenHeight * 0.015f)
                    .padLeft(horizontalPadding)
                    .spaceRight(screenWidth * 0.1f);
            playerTable.add(score)
                    .right()
                    .padBottom(screenHeight * 0.015f)
                    .padRight(horizontalPadding);
            playerTable.row();
        }
    }

    @Override public void show() {}
    @Override public void render(float delta) {
        stage.act(delta);
        stage.draw();
    }
    @Override public void resize(int w, int h) { stage.getViewport().update(w,h,true); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() { dispose(); }

    @Override
    public void dispose() {
        stage.dispose();
        if (backgroundTexture != null) backgroundTexture.dispose();
    }
}
