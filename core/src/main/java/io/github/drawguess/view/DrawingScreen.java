package io.github.drawguess.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import io.github.drawguess.DrawGuessMain;
import io.github.drawguess.controller.DrawingController;
import io.github.drawguess.controller.SizeController;

import java.util.LinkedHashMap;
import java.util.Map;

public class DrawingScreen implements Screen {

    private final DrawGuessMain game;
    private final Stage stage;
    private final DrawingController controller;

    private final Pixmap canvas;
    private final Texture canvasTexture;
    private final Image canvasImage;

    private final Texture whiteboardTexture;
    private final Image whiteboardImage;

    private final Texture backTexture;
    private final Image backButton;

    private final Texture undoTexture;
    private final Image undoButton;

    private final Texture editToolTexture;
    private final Image editToolButton;

    private Texture editPanelTexture;
    private Image editPanelBackground;
    private Group editPanelGroup;

    private final Texture eraserTexture;

    private int currentSize = 1; // Default pensize

    public DrawingScreen(DrawGuessMain game) {
        this.game = game;
        this.stage = new Stage(new ScreenViewport());

        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();
        
        whiteboardTexture = new Texture("whiteboard.png");
        whiteboardImage = new Image(whiteboardTexture);
        whiteboardImage.setSize(screenWidth, screenHeight);
        whiteboardImage.setPosition(0, 0);
        stage.addActor(whiteboardImage);

        // Grenser til canvas basert på skjermprosent
        float drawMinXPct = 0.08f;
        float drawMaxXPct = 0.92f;
        float drawMinYPct = 0.12f;
        float drawMaxYPct = 0.80f;

        float minX = screenWidth * drawMinXPct;
        float maxX = screenWidth * drawMaxXPct;
        float minY = screenHeight * drawMinYPct;
        float maxY = screenHeight * drawMaxYPct;



        canvas = new Pixmap(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), Pixmap.Format.RGBA8888);
        canvas.setColor(0, 0, 0, 0);
        canvas.fill();
        canvasTexture = new Texture(canvas);
        canvasImage = new Image(canvasTexture);
        canvasImage.setFillParent(true);
        stage.addActor(canvasImage);

        controller = new DrawingController(canvas, canvasTexture, minX, maxX, minY, maxY);

        editPanelTexture = new Texture("editpanel.png");
        editPanelBackground = new Image(editPanelTexture);
        editPanelBackground.setSize(400, 200);

        editPanelGroup = new Group();
        editPanelGroup.setSize(400, 200);
        float centerX = (screenWidth - editPanelGroup.getWidth()) / 2f;
        float centerY = (screenHeight - editPanelGroup.getHeight()) / 2f;
        editPanelGroup.setPosition(centerX, centerY);
        editPanelGroup.setVisible(false);
        editPanelGroup.addActor(editPanelBackground);
        stage.addActor(editPanelGroup);

        Texture[] sizeTextures = {
            new Texture("size1.png"),
            new Texture("size2.png"),
            new Texture("size3.png"),
            new Texture("size4.png")
        };
        int[] sizes = {1, 3, 6, 12};

        new SizeController(editPanelGroup, sizeTextures, sizes, size -> {
            currentSize = size;
            controller.selectPen(controller.getSelectedColor(), currentSize);
        });

        backTexture = new Texture("backbtn.png");
        backButton = new Image(backTexture);
        backButton.setSize(80, 30);
        backButton.setPosition(30, screenHeight - 40);
        backButton.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                game.setScreen(new GameScreen(game));
                return true;
            }
        });
        stage.addActor(backButton);

        editToolTexture = new Texture("edittool.png");
        editToolButton = new Image(editToolTexture);
        editToolButton.setSize(60, 60);
        editToolButton.setPosition(screenWidth - 70, screenHeight - 70);
        editToolButton.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                boolean visible = !editPanelGroup.isVisible();
                editPanelGroup.setVisible(visible);
                editPanelGroup.toFront();
                controller.setToolPanelOpen(visible);
                return true;
            }
        });
        stage.addActor(editToolButton);

        undoTexture = new Texture("undobtn.png");
        undoButton = new Image(undoTexture);
        undoButton.setSize(100, 60);
        undoButton.setPosition(30, 30);
        undoButton.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                controller.undo();
                return true;
            }
        });
        stage.addActor(undoButton);

        eraserTexture = new Texture("eraser.png");
        ToolButtonFactory.addEraserButton(stage, controller, "eraser.png", screenWidth - 310, -10);

        Map<Color, String> colors = new LinkedHashMap<>();
        colors.put(Color.BLACK, "black.png");
        colors.put(Color.RED, "red.png");
        colors.put(Color.BLUE, "blue.png");
        colors.put(Color.GREEN, "green.png");
        colors.put(Color.YELLOW, "yellow.png");
        colors.put(Color.PURPLE, "purple.png");
        colors.put(Color.ORANGE, "orange.png");
        colors.put(Color.CYAN, "cyan.png");

        float baseX = screenWidth - 50;
        float spacing = 30;
        float y = -20;
        int index = 0;
        for (Map.Entry<Color, String> entry : colors.entrySet()) {
            float x = baseX - index * spacing;
            ToolButtonFactory.addColorPenButton(stage, controller, entry.getKey(), entry.getValue(), x, y, () -> currentSize);
            index++;
        }

        ToolButtonFactory.selectInitialColor(controller.getSelectedColor());
        controller.selectPen(controller.getSelectedColor(), currentSize);

        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(stage);
        multiplexer.addProcessor(controller);
        Gdx.input.setInputProcessor(multiplexer);
    }

    @Override
    public void render(float delta) {
        controller.updateCanvas();
        stage.act(delta);
        stage.draw();
        undoButton.setTouchable(controller.canUndo() ? Touchable.enabled : Touchable.disabled);
        undoButton.setColor(controller.canUndo() ? Color.WHITE : Color.LIGHT_GRAY);
    }

    @Override public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }
    @Override public void show() {}
    @Override public void hide() { dispose(); }
    @Override public void pause() {}
    @Override public void resume() {}

    @Override
    public void dispose() {
        stage.dispose();
        whiteboardTexture.dispose();
        canvasTexture.dispose();
        canvas.dispose();
        backTexture.dispose();
        undoTexture.dispose();
        eraserTexture.dispose();
        editToolTexture.dispose();
        editPanelTexture.dispose();
    }
}
