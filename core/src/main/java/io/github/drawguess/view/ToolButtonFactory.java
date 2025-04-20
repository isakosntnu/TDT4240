package io.github.drawguess.view;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Scaling;
import io.github.drawguess.controller.DrawingController;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntSupplier;

public class ToolButtonFactory {

    private static final List<PencilButton> penButtons = new ArrayList<>();
    private static final float SELECTED_OFFSET = 35f;

    private static Image eraserButton = null;
    private static float eraserOriginalY;

    // 🎯 Marker initialt valgt fargeknapp
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

    /**
     * Legger til en fargeblyant-knapp
     */
    public static void addColorPenButton(Stage stage,
                                         DrawingController controller,
                                         Color color,
                                         String texturePath,
                                         float x, float y,
                                         float width, float height,
                                         IntSupplier sizeSupplier) {
        Texture texture = new Texture(texturePath);
        Image button = new Image(texture);
        button.setSize(width, height);
        button.setScaling(Scaling.fill);
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

    /**
     * Legger til viskelær-knapp
     */
    public static void addEraserButton(Stage stage,
                                       DrawingController controller,
                                       String texturePath,
                                       float x, float y,
                                       float size) {
        Texture texture = new Texture(texturePath);
        Image button = new Image(texture);
        button.setSize(size, size);
        button.setScaling(Scaling.fill);
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

    /**
     * Flytter valgt blyant oppover, og nullstiller andre
     */
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

    /**
     * Intern hjelpestruktur
     */
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
