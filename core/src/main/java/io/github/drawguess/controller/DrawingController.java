package io.github.drawguess.controller;

import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import io.github.drawguess.model.ecs.components.ColorComponent;
import io.github.drawguess.model.ecs.components.ToolComponent;
import io.github.drawguess.model.ecs.entities.Entity;
import io.github.drawguess.model.ecs.entities.ToolFactory;
import io.github.drawguess.model.ecs.systems.DrawingSystem;

import java.util.ArrayList;
import java.util.List;

public class DrawingController extends InputAdapter {

    private final Pixmap canvas;
    private final Texture drawingTexture;
    private final SpriteBatch batch;
    private Entity selectedTool;
    private float lastX = -1, lastY = -1; // Endret til float
    private boolean isDrawing = false;
    private boolean hasDrawnThisStroke = false;
    private final List<Pixmap> undoStack = new ArrayList<>();
    private boolean isToolPanelOpen = false;

    private final float minX, maxX, minY, maxY; // Endret til float

    public DrawingController(Pixmap canvas, Texture drawingTexture,
                             float minX, float maxX, float minY, float maxY) {
        this.canvas = canvas;
        this.drawingTexture = drawingTexture;
        this.batch = new SpriteBatch();
        this.selectedTool = ToolFactory.createPen(Color.BLACK, 1);
        this.minX = minX;
        this.maxX = maxX;
        this.minY = minY;
        this.maxY = maxY;
        undoStack.add(copyPixmap(canvas));
    }

    public void setToolPanelOpen(boolean open) {
        this.isToolPanelOpen = open;
    }

    public Color getSelectedColor() {
        if (selectedTool.hasComponent(ColorComponent.class)) {
            return selectedTool.getComponent(ColorComponent.class).color;
        }
        return null;
    }

    public void selectPen(Color color, int size) {
        selectedTool = ToolFactory.createPen(color, size);
    }

    public void selectEraser() {
        selectedTool = ToolFactory.createEraser();
    }

    public void updateCanvas() {
        drawingTexture.draw(canvas, 0, 0);
    }

    public Pixmap getCanvas() {
        return canvas;
    }

    public Texture getCanvasTexture() {
        return drawingTexture;
    }

    public boolean canUndo() {
        return undoStack.size() > 1;
    }

    public void undo() {
        if (undoStack.size() > 1) {
            Pixmap last = undoStack.remove(undoStack.size() - 1);
            last.dispose();
            Pixmap previous = undoStack.get(undoStack.size() - 1);
            canvas.setColor(0, 0, 0, 0);
            canvas.fill();
            canvas.drawPixmap(previous, 0, 0);
        } else {
            System.out.println("Undo stack tom");
        }
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        float radius = selectedTool.getComponent(ToolComponent.class).size;
        if (isToolPanelOpen || !withinBounds(screenX, screenY, radius)) return false;
        if (lastX != -1 && lastY != -1 && (lastX != screenX || lastY != screenY)) {
            DrawingSystem.draw(selectedTool, canvas, lastX, lastY, screenX, screenY, (xx, yy) -> withinBounds(xx, yy, radius));
            hasDrawnThisStroke = true;
        }

        lastX = screenX;
        lastY = screenY;
        return true;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {

        float radius = selectedTool.getComponent(ToolComponent.class).size;
        if (isToolPanelOpen || !withinBounds(screenX, screenY, radius)) return false;


        lastX = screenX;
        lastY = screenY;
        isDrawing = true;
        hasDrawnThisStroke = false;
        return true;
    }


    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        isDrawing = false;
        if (hasDrawnThisStroke) {
            undoStack.add(copyPixmap(canvas));
        }
        lastX = lastY = -1;
        hasDrawnThisStroke = false;
        return true;
    }

    private Pixmap copyPixmap(Pixmap original) {
        Pixmap copy = new Pixmap(original.getWidth(), original.getHeight(), original.getFormat());
        copy.drawPixmap(original, 0, 0);
        return copy;
    }

    private boolean withinBounds(float x, float y, float radius) {
        return x - radius >= minX && x + radius <= maxX &&
               y - radius >= minY && y + radius <= maxY;
    }
    

    public void dispose() {
        batch.dispose();
        drawingTexture.dispose();
        canvas.dispose();
        for (Pixmap p : undoStack) {
            if (p != null) p.dispose();
        }
        undoStack.clear();
    }
}
