
package com.drawguess.game.states;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.drawguess.game.controller.MainMenuController;

public class MainMenuState extends BaseState {
    private MainMenuController controller;

    public MainMenuState(StateManager gsm) {
        super(gsm);
        this.controller = new MainMenuController(this); // 'this' provides context
    }

    @Override
    public void handleInput() {

    }

    @Override
    public void update(float dt) {
        controller.update(dt);
    }

    @Override
    public void render(SpriteBatch sb) {
        controller.render(sb);
    }

    @Override
    public void enter() {

    }

    @Override
    public void exit() {

    }

    @Override
    public void dispose() {
        controller.dispose();
    }

}