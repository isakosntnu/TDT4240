package io.github.drawguess.controller;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;

import java.util.ArrayList;
import java.util.List;

public class SizeController {

    private final Group container;
    private final List<Image> buttons = new ArrayList<>();
    private Image selectedButton;
    private final float SIZE_BUTTON_Y = 70f;
    private final float SIZE_BUTTON_PRESSED_Y = 85f;

    public interface SizeChangeListener {
        void onSizeSelected(int size);
    }

    private final SizeChangeListener listener;

    public SizeController(Group container, Texture[] textures, int[] sizes, SizeChangeListener listener) {
        this.container = container;
        this.listener = listener;

        float buttonWidth = 50;
        float spacing = 10;
        float totalWidth = textures.length * buttonWidth + (textures.length - 1) * spacing;
        float startX = (container.getWidth() - totalWidth) / 2f;

        for (int i = 0; i < textures.length; i++) {
            final int size = sizes[i];
            final Image btn = new Image(textures[i]);
            btn.setSize(buttonWidth, buttonWidth);
            btn.setPosition(startX + i * (buttonWidth + spacing), SIZE_BUTTON_Y);

            btn.addListener(new InputListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    selectButton(btn);
                    if (listener != null) {
                        listener.onSizeSelected(size);
                    }
                    return true;
                }
            });

            container.addActor(btn);
            buttons.add(btn);
        }

        if (!buttons.isEmpty()) {
            selectButton(buttons.get(0));
            listener.onSizeSelected(sizes[0]);
        }
    }

    private void selectButton(Image btn) {
        for (Image b : buttons) {
            b.setY(SIZE_BUTTON_Y);
        }
        btn.setY(SIZE_BUTTON_PRESSED_Y);
        selectedButton = btn;
    }
}

