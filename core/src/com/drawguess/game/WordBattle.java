package com.drawguess.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.drawguess.game.states.MainMenuState;
import com.drawguess.game.states.StateManager;

public class WordBattle extends ApplicationAdapter {
	public static  int WIDTH;
	public static  int HEIGHT;
	public static final String TITLE = "WordBattle";
	private StateManager stateManager;
	public SpriteBatch batch;


	@Override
	public void create() {
		batch = new SpriteBatch();
		stateManager = new StateManager();
		ScreenUtils.clear(1, 0, 0, 1);
		stateManager.setState(new MainMenuState(stateManager)); // Set the main menu as the initial state
	}
	@Override
	public void render() {
		stateManager.update(Gdx.graphics.getDeltaTime()); // Update the current state
		stateManager.render(batch); // Render the current state
	}

	@Override
	public void dispose() {
		stateManager.dispose(); // Assuming you have a dispose method in your StateManager
		batch.dispose(); // Dispose of the SpriteBatch here.


	}
}