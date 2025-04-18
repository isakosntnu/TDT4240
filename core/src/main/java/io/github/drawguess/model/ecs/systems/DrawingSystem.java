package io.github.drawguess.model.ecs.systems;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Color;
import io.github.drawguess.model.ecs.components.ColorComponent;
import io.github.drawguess.model.ecs.components.ToolComponent;
import io.github.drawguess.model.ecs.entities.Entity;

import java.util.function.BiPredicate;

public class DrawingSystem {

    public static void draw(Entity tool, Pixmap canvas, float x1, float y1, float x2, float y2,
                            BiPredicate<Float, Float> withinBounds) {
        if (tool == null || !tool.hasComponent(ToolComponent.class)) return;

        ToolComponent toolType = tool.getComponent(ToolComponent.class);
        ColorComponent colorComp = tool.getComponent(ColorComponent.class);

        // Calculate the distance and steps for smooth drawing
        float distance = (float) Math.hypot(x2 - x1, y2 - y1);
        int steps = Math.max(1, (int) distance); // convert to int for loop iteration

        switch (toolType.type) {
            case PEN:
                canvas.setColor(colorComp.color);
                for (int i = 0; i <= steps; i++) {
                    float t = i / (float) steps;
                    float x = x1 + t * (x2 - x1);
                    float y = y1 + t * (y2 - y1);
                    if (withinBounds.test(x, y)) {
                        canvas.fillCircle((int) x, (int) y, toolType.size); // Still cast to int for drawing
                    }
                }
                break;

            case ERASER:
                canvas.setColor(0, 0, 0, 0);
                canvas.setBlending(Pixmap.Blending.None);
                for (int i = 0; i <= steps; i++) {
                    float t = i / (float) steps;
                    float x = x1 + t * (x2 - x1);
                    float y = y1 + t * (y2 - y1);
                    if (withinBounds.test(x, y)) {
                        canvas.fillCircle((int) x, (int) y, 8); // fixed radius for eraser
                    }
                }
                canvas.setBlending(Pixmap.Blending.SourceOver);
                break;
        }
    }
}
