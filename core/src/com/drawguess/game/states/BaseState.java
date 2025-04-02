package com.drawguess.game.states;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.drawguess.game.WordBattle;

public abstract class BaseState implements GameState {
    protected StateManager gsm;
    protected OrthographicCamera cam;
    protected Viewport viewport;

    public BaseState(StateManager gsm) {
        this.gsm = gsm;

        cam = new OrthographicCamera();
        viewport = new FitViewport(WordBattle.WIDTH, WordBattle.HEIGHT, cam);
        cam.position.set(viewport.getWorldWidth() / 2, viewport.getWorldHeight() / 2, 0);
        cam.update();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
        cam.position.set(viewport.getWorldWidth() / 2, viewport.getWorldHeight() / 2, 0);
        cam.update();
    }

    public OrthographicCamera getCam() {
        return cam;
    }

    // Implement other GameState methods or leave them abstract for subclasses to implement
    // ...
}
