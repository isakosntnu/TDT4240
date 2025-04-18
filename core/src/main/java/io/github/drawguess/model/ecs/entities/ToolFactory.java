package io.github.drawguess.model.ecs.entities;
import com.badlogic.gdx.graphics.Color;
import io.github.drawguess.model.ecs.components.*;

public class ToolFactory {

    public static Entity createPen(Color color, int size) {
        Entity pen = new Entity();
        pen.addComponent(new ToolComponent(ToolComponent.ToolType.PEN, size));
        pen.addComponent(new ColorComponent(color));
        return pen;
    }
    

    public static Entity createEraser() {
        Entity eraser = new Entity();
        eraser.addComponent(new ToolComponent(ToolComponent.ToolType.ERASER, 8)); // default størrelse
        eraser.addComponent(new ColorComponent(Color.CLEAR));
        return eraser;
    }

    public static boolean isPen(Entity tool) {
        return tool.hasComponent(ToolComponent.class) &&
               tool.getComponent(ToolComponent.class).type == ToolComponent.ToolType.PEN;
    }
}
