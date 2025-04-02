package com.drawguess.game.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
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





    public MainMenuView(OrthographicCamera cam) {
        this.cam = cam;
        this.backgroundTexture = new Texture("bg2.png");
        this.joinGameButton = new Texture("join_game.png");
        this.newGameButton = new Texture("new_game.png");


        // Calculate bounds and assign them
        joinGameButtonBounds = new Rectangle(
                (WordBattle.WIDTH - joinGameButton.getWidth()) / 2,
                (WordBattle.HEIGHT / 2 - joinGameButton.getHeight() / 2) - 50,
                joinGameButton.getWidth(),
                joinGameButton.getHeight()
        );

        newGameButtonBounds = new Rectangle(
                (WordBattle.WIDTH - newGameButton.getWidth()) / 2,
                joinGameButtonBounds.y - newGameButton.getHeight() - 20, // Adjust y-position based on joinGameButton's position
                newGameButton.getWidth(),
                newGameButton.getHeight()
        );
    }

    public void render(SpriteBatch spriteBatch) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        spriteBatch.begin();
        // Render background to fill the screen
        spriteBatch.draw(backgroundTexture, 0, 0, WordBattle.WIDTH, WordBattle.HEIGHT);
        // Render buttons using bounds for precise alignment
        spriteBatch.draw(joinGameButton, joinGameButtonBounds.x, joinGameButtonBounds.y);
        spriteBatch.draw(newGameButton, newGameButtonBounds.x, newGameButtonBounds.y);
        spriteBatch.end();
    }




    // Provide a way for the controller to access the bounds
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
    }
}

