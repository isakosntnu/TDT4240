package io.github.drawguess.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import io.github.drawguess.DrawGuessMain;
import io.github.drawguess.controller.DrawingController;
import io.github.drawguess.controller.GameController;
import io.github.drawguess.controller.PlayerController;
import io.github.drawguess.controller.SizeController;
import io.github.drawguess.factory.ToolButtonFactory;
import io.github.drawguess.manager.GameManager;

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

    private final Texture undoTexture;
    private final Image undoButton;

    private final Texture editToolTexture;
    private final Image editToolButton;

    private Texture editPanelTexture;
    private Image editPanelBackground;
    private Group editPanelGroup;

    private final Texture eraserTexture;

    private int currentSize = 1;

    public DrawingScreen(DrawGuessMain game) {
        this.game = game;
        this.stage = new Stage(new ScreenViewport());

        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();
        float iconSize = screenHeight * 0.08f;

        // Game controller init
        GameController gameController = GameManager.getInstance().getGameController();

        // UI skin for buttons
        Skin skin = new Skin(Gdx.files.internal("uiskin.json"));

        // Bakgrunnstavle
        whiteboardTexture = new Texture("whiteboard.png");
        whiteboardImage = new Image(whiteboardTexture);
        whiteboardImage.setSize(screenWidth, screenHeight);
        whiteboardImage.setPosition(0, 0);
        stage.addActor(whiteboardImage);

        // Canvas-grenser
        float minX = screenWidth * 0.08f;
        float maxX = screenWidth * 0.92f;
        float minY = screenHeight * 0.12f;
        float maxY = screenHeight * 0.80f;

        // Canvas
        canvas = new Pixmap((int) screenWidth, (int) screenHeight, Pixmap.Format.RGBA8888);
        canvas.setColor(0, 0, 0, 0);
        canvas.fill();
        canvasTexture = new Texture(canvas);
        canvasImage = new Image(canvasTexture);
        canvasImage.setFillParent(true);
        stage.addActor(canvasImage);

        controller = new DrawingController(canvas, canvasTexture, minX, maxX, minY, maxY);

        // Edit panel
        editPanelTexture = new Texture("editpanel.png");
        editPanelBackground = new Image(editPanelTexture);
        editPanelBackground.setSize(screenWidth * 0.6f, screenHeight * 0.2f);

        editPanelGroup = new Group();
        editPanelGroup.setSize(editPanelBackground.getWidth(), editPanelBackground.getHeight());
        float centerX = (screenWidth - editPanelGroup.getWidth()) / 2f;
        float centerY = (screenHeight - editPanelGroup.getHeight()) / 2f;
        editPanelGroup.setPosition(centerX, centerY);
        editPanelGroup.setVisible(false);
        editPanelGroup.addActor(editPanelBackground);
        stage.addActor(editPanelGroup);

        // Pen size-knapper
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

        // Edit-tool
        editToolTexture = new Texture("edittool.png");
        editToolButton = new Image(editToolTexture);
        editToolButton.setSize(iconSize, iconSize);
        editToolButton.setPosition(screenWidth - iconSize - 20, screenHeight - iconSize - 20);
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

        // 🔘 Bunnknapper
        float barHeight = screenHeight * 0.1f;
        float penY = -barHeight * 0.4f;
        float undoY = screenHeight * 0.02f;

        // Undo-knapp
        undoTexture = new Texture("undobtn.png");
        undoButton = new Image(undoTexture);
        undoButton.setSize(barHeight * 1.2f, barHeight * 0.8f);
        undoButton.setPosition(screenWidth * 0.02f, undoY);
        undoButton.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                controller.undo();
                return true;
            }
        });
        stage.addActor(undoButton);

        // Eraser-knapp
        eraserTexture = new Texture("eraser.png");
        float eraserSize = barHeight * 0.6f;
        float eraserX = undoButton.getX() + undoButton.getWidth() + screenWidth * 0.02f;
        ToolButtonFactory.addEraserButton(
            stage, controller, "eraser.png",
            eraserX, penY, eraserSize
        );

        // Fargeknapper
        Map<Color, String> colors = new LinkedHashMap<>();
        colors.put(Color.BLACK, "black.png");
        colors.put(Color.RED, "red.png");
        colors.put(Color.BLUE, "blue.png");
        colors.put(Color.GREEN, "green.png");
        colors.put(Color.YELLOW, "yellow.png");
        colors.put(Color.PURPLE, "purple.png");
        colors.put(Color.ORANGE, "orange.png");
        colors.put(Color.CYAN, "cyan.png");

        int totalColors = colors.size();
        float startX = eraserX + eraserSize + screenWidth * 0.02f;
        float availableWidth = screenWidth - startX - screenWidth * 0.02f;
        float buttonWidth = availableWidth / totalColors;
        float buttonHeight = barHeight;

        int index = 0;
        for (Map.Entry<Color, String> entry : colors.entrySet()) {
            float x = startX + index * buttonWidth;
            ToolButtonFactory.addColorPenButton(
                stage, controller,
                entry.getKey(),
                entry.getValue(),
                x, penY,
                buttonWidth, buttonHeight,
                () -> currentSize
            );
            index++;
        }
    
        ToolButtonFactory.selectInitialColor(controller.getSelectedColor());
        controller.selectPen(controller.getSelectedColor(), currentSize);

        // Finish button

        TextButton finishButton = new TextButton("FINISH DRAWING", skin);
        float finishWidth = screenWidth * 0.28f;
        float finishHeight = screenHeight * 0.075f;
        finishButton.setSize(finishWidth, finishHeight);
        finishButton.setPosition(20, screenHeight - finishHeight - 20);

        BitmapFont font = finishButton.getLabel().getStyle().font;
        GlyphLayout layout = new GlyphLayout(font, finishButton.getText());

        float scaleX = (finishWidth * 0.9f) / layout.width;
        float scaleY = (finishHeight * 0.8f) / layout.height;
        float finalScale = Math.min(scaleX, scaleY); 

        finishButton.getLabel().setFontScale(finalScale);

        PlayerController pc = new PlayerController(game);

        finishButton.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent e, float x, float y,
                                     int pointer, int button) {
        
                pc.finishDrawing(canvas);           
                game.setScreen(new WaitingScreen(game));
                return true;
            }
        });
        
        stage.addActor(finishButton);


        // Word to draw
        String wordToDraw = gameController.getRandomWord();
        Label.LabelStyle labelStyle = new Label.LabelStyle(new BitmapFont(), Color.BLACK);

        // "Draw:" label
        Label drawLabel = new Label("Draw:", labelStyle);
        float drawFontScale = screenHeight * 0.0016f;
        drawLabel.setFontScale(drawFontScale);

        // Word label
        Label wordLabel = new Label(wordToDraw, labelStyle);
        wordLabel.setFontScale(drawFontScale);

        // Mål opp bredde og beregn midtstilt posisjon
        GlyphLayout layoutDraw = new GlyphLayout(drawLabel.getStyle().font, drawLabel.getText());
        GlyphLayout layoutWord = new GlyphLayout(wordLabel.getStyle().font, wordLabel.getText());

        float textWidth = Math.max(layoutDraw.width, layoutWord.width) * drawFontScale;
        float wordCenter = (finishButton.getX() + editToolButton.getX() + editToolButton.getWidth()) / 2f;
        float wordCenterX = wordCenter - (textWidth / 2f);

        // Plasser "Draw:" litt høyere
        drawLabel.setPosition(wordCenterX, screenHeight * 0.97f);
        // Plasser ordet rett under
        wordLabel.setPosition(wordCenterX, screenHeight * 0.945f);

        stage.addActor(drawLabel);
        stage.addActor(wordLabel);


        // Input
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
        undoTexture.dispose();
        eraserTexture.dispose();
        editToolTexture.dispose();
        editPanelTexture.dispose();
    }
}
