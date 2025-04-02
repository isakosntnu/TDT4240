package com.drawguess.game.states;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class StateManager {


    private GameState currentState;

    public StateManager() {

    }


    public void setState(GameState newState) {
        if (currentState != null) {
            currentState.exit();
        }
        currentState = newState;
        newState.enter(); // Ensure resources are ready before updating or rendering
    }


    public void update(float dt) {
        if (currentState != null) currentState.update(dt);
    }

    public void render(SpriteBatch batch) {
        if (currentState != null) currentState.render(batch); // Modify this line
    }

    public void dispose() {
    }
}
