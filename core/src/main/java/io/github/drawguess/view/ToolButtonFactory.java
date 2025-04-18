package io.github.drawguess.view;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import io.github.drawguess.controller.DrawingController;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntSupplier;

public class ToolButtonFactory {

    private static final List<PencilButton> penButtons = new ArrayList<>();
    private static final float SELECTED_OFFSET = 10f;

    private static Image eraserButton = null;
    private static float eraserOriginalY;

    // 🎯 Marker initialt valgt fargeknapp (brukes ved oppstart)
    public static void selectInitialColor(Color color) {
        for (PencilButton pb : penButtons) {
            if (pb.color.equals(color)) {
                pb.image.setY(pb.originalY + SELECTED_OFFSET);
            } else {
                pb.image.setY(pb.originalY);
            }
        }

        if (eraserButton != null) {
            eraserButton.setY(eraserOriginalY);
        }
    }

    public static void addColorPenButton(Stage stage, DrawingController controller, Color color, String texturePath, float x, float y, IntSupplier sizeSupplier) {
        Texture texture = new Texture(texturePath);
        Image button = new Image(texture);
        button.setSize(30, 90);
        button.setPosition(x, y);

        penButtons.add(new PencilButton(button, y, color));

        button.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float xx, float yy, int pointer, int buttonIndex) {
                controller.selectPen(color, sizeSupplier.getAsInt());
                updateSelection(button);
                return true;
            }
        });

        stage.addActor(button);
    }


    // 🧽 Legg til viskelær-knapp
    public static void addEraserButton(Stage stage, DrawingController controller, String texturePath, float x, float y) {
        Texture texture = new Texture(texturePath);
        Image button = new Image(texture);
        button.setSize(40, 70);
        button.setPosition(x, y);

        eraserButton = button;
        eraserOriginalY = y;

        button.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float xx, float yy, int pointer, int buttonIndex) {
                controller.selectEraser();
                resetSelection();
                eraserButton.setY(eraserOriginalY + SELECTED_OFFSET);
                return true;
            }
        });

        stage.addActor(button);
    }


    private static void updateSelection(Image selected) {
        for (PencilButton pb : penButtons) {
            if (pb.image == selected) {
                pb.image.setY(pb.originalY + SELECTED_OFFSET);
            } else {
                pb.image.setY(pb.originalY);
            }
        }

        if (eraserButton != null) {
            eraserButton.setY(eraserOriginalY);
        }
    }

    public static void resetSelection() {
        for (PencilButton pb : penButtons) {
            pb.image.setY(pb.originalY);
        }

        if (eraserButton != null) {
            eraserButton.setY(eraserOriginalY);
        }
    }

    private static class PencilButton {
        Image image;
        float originalY;
        Color color;

        PencilButton(Image image, float originalY, Color color) {
            this.image = image;
            this.originalY = originalY;
            this.color = color;
        }
    }
}
