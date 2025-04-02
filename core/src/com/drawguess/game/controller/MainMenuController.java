package com.drawguess.game.controller;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.drawguess.game.states.MainMenuState;
import com.drawguess.game.states.StateManager;
import com.drawguess.game.view.MainMenuView;

public class MainMenuController {
    private MainMenuState state;
    private MainMenuView mainMenuView;


    public MainMenuController(MainMenuState state) {
        this.state = state;
        // Now you have access to stateManager through state.gsm and can instruct the state to change views or states.
        this.mainMenuView = new MainMenuView(state.getCam()); // Assuming you provide a getter for the camera in BaseState
    }

    public void handleInput() {
        if (Gdx.input.justTouched()) {
            Vector3 touchPos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);

            // Get bounds from the view
            Rectangle joinGameButtonBounds = mainMenuView.getJoinGameButtonBounds();
            Rectangle newGameButtonBounds = mainMenuView.getNewGameButtonBounds();


            // Transform touch coordinates to game coordinates
            touchPos = new Vector3(touchPos.x, Gdx.graphics.getHeight() - touchPos.y, 0);

            // Check if touch is within the bounds of the start button
            if (joinGameButtonBounds.contains(touchPos.x, touchPos.y)) {
                // Transition to the next state
/*
                stateManager.setState(new PlayState(stateManager));
*/
            }
        }
    }


    public void update(float dt) {
        handleInput();
    }

    public void render(SpriteBatch sb) {
        mainMenuView.render(sb);
    }

    public void dispose() {
        mainMenuView.dispose();
    }

    public void enter() {
        // Any initialization when entering this state
    }

    public void exit() {
        // Clean up when exiting this state
        dispose();
    }
}
