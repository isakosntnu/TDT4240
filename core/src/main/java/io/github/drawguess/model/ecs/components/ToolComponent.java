package io.github.drawguess.model.ecs.components;

public class ToolComponent {
    public enum ToolType { PEN, ERASER }
    public ToolType type;
    public int size;

    public ToolComponent(ToolType type, int size) {
        this.type = type;
        this.size = size;
    }

    public ToolComponent(ToolType type) {
        this(type, 3); // fallback til standard
    }
}

