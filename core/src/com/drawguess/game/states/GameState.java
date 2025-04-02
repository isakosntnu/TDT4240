package com.drawguess.game.states;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public interface GameState {

    void handleInput();

    void update(float dt);

    void render(SpriteBatch sb);

    void enter();

    void exit();

    void dispose();

    void resize(int width, int height);
}
