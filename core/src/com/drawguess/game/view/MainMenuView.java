package com.drawguess.game.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.drawguess.game.WordBattle;

public class MainMenuView {
    private Texture backgroundTexture;
    private Texture joinGameButton;
    private Texture newGameButton;
    private Rectangle joinGameButtonBounds;
    private Rectangle newGameButtonBounds;
    private OrthographicCamera cam;
    private BitmapFont titleFont;

    public MainMenuView(OrthographicCamera cam) {
        this.cam = cam;
        this.backgroundTexture = new Texture("bakgrunn.jpeg"); // Change to vertical-friendly background
        this.joinGameButton = new Texture("join_game.png"); // Updated buttons
        this.newGameButton = new Texture("new_game.png");

        titleFont = new BitmapFont();
        titleFont.setColor(Color.WHITE); // Title color
        titleFont.getData().setScale(3); // Adjust size

        // Position elements for vertical screen
        float screenWidth = WordBattle.WIDTH;
        float screenHeight = WordBattle.HEIGHT;

        joinGameButtonBounds = new Rectangle(
                (screenWidth - joinGameButton.getWidth()) / 2,
                screenHeight * 0.5f, // Center vertically
                joinGameButton.getWidth(),
                joinGameButton.getHeight()
        );

        newGameButtonBounds = new Rectangle(
                (screenWidth - newGameButton.getWidth()) / 2,
                joinGameButtonBounds.y - newGameButton.getHeight() - 50, // Spacing below
                newGameButton.getWidth(),
                newGameButton.getHeight()
        );
    }

    public void render(SpriteBatch spriteBatch) {
        Gdx.gl.glClearColor(0.1f, 0.2f, 0.5f, 1); // Dark blue background
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        spriteBatch.begin();
        spriteBatch.draw(backgroundTexture, 0, 0, WordBattle.WIDTH, WordBattle.HEIGHT);
        titleFont.draw(spriteBatch, "DrawGuess", WordBattle.WIDTH * 0.35f, WordBattle.HEIGHT - 100); // Title at top

        spriteBatch.draw(joinGameButton, joinGameButtonBounds.x, joinGameButtonBounds.y);
        spriteBatch.draw(newGameButton, newGameButtonBounds.x, newGameButtonBounds.y);
        spriteBatch.end();
    }

    public Rectangle getJoinGameButtonBounds() {
        return joinGameButtonBounds;
    }

    public Rectangle getNewGameButtonBounds() {
        return newGameButtonBounds;
    }

    public void dispose() {
        backgroundTexture.dispose();
        joinGameButton.dispose();
        newGameButton.dispose();
        titleFont.dispose();
    }
}