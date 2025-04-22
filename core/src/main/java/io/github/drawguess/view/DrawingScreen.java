// DrawingScreen.java
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
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import io.github.drawguess.DrawGuessMain;
import io.github.drawguess.controller.DrawingController;
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

    // —— TIMER FIELDS ——
    private Label timerLabel;
    private int drawTimeLeft = 30;
    private Timer.Task drawTimerTask;

    public DrawingScreen(DrawGuessMain game) {
        this.game = game;
        new PlayerController(game);
        this.stage = new Stage(new ScreenViewport());

        float W = Gdx.graphics.getWidth();
        float H = Gdx.graphics.getHeight();
        
        // Define vertical boundaries based on screen height percentage
        float wordTopY = H * 0.97f; 
        float wordBottomY = H * 0.93f; 
        float timerTopY = H * 0.86f; 
        float timerBottomY = H * 0.82f; 
        
        float wordAreaHeight = wordTopY - wordBottomY; // Available height for word
        float timerAreaHeight = timerTopY - timerBottomY; // Available height for timer

        float baseFontScale = H * 0.002f; // For other elements like DONE button
        float padding = H * 0.005f; 
        float iconSize = H * 0.08f;

        // 1) Bakgrunn
        whiteboardTexture = new Texture("whiteboard.png");
        whiteboardImage = new Image(whiteboardTexture);
        whiteboardImage.setSize(W, H);
        stage.addActor(whiteboardImage);

        // 2) Canvas
        float minX = W * 0.08f, maxX = W * 0.92f, minY = H * 0.12f, maxY = H * 0.80f;
        canvas = new Pixmap((int)W, (int)H, Pixmap.Format.RGBA8888);
        canvas.setColor(0,0,0,0); canvas.fill();
        canvasTexture = new Texture(canvas);
        canvasImage = new Image(canvasTexture);
        canvasImage.setFillParent(true);
        stage.addActor(canvasImage);

        controller = new DrawingController(canvas, canvasTexture, minX, maxX, minY, maxY);

        // 3) UI Skin
        Skin skin = new Skin(Gdx.files.internal("uiskin.json"));

        // --- WORD --- Placed at the very top (1%-5%)
        String playerId = GameManager.getInstance().getPlayerId();
        String word = GameManager.getInstance().getSession().getWordForPlayer(playerId);
        Label.LabelStyle wordLabelStyle = skin.get(Label.LabelStyle.class);
        Label lblWord = new Label(word, wordLabelStyle);
        
        // Calculate font scale to fit word in its designated area
        // (This is approximate, adjust multiplier 0.8f as needed)
        float wordFontScale = calculateFitScale(lblWord.getStyle().font, word, W * 0.8f, wordAreaHeight * 0.8f);
        lblWord.setFontScale(wordFontScale);
        lblWord.setAlignment(Align.center);
        // Position top-center within the 1%-5% band
        lblWord.setPosition(W / 2f, wordTopY, Align.top | Align.center);
        stage.addActor(lblWord);

        // --- TIMER --- Placed below the word (8%-12%)
        timerLabel = new Label(String.valueOf(drawTimeLeft), skin);
        // Calculate font scale to fit timer in its area
        float timerFontScale = calculateFitScale(timerLabel.getStyle().font, "00", W * 0.2f, timerAreaHeight * 0.8f);
        timerLabel.setFontScale(timerFontScale);
        // Position top-center within the 8%-12% band
        timerLabel.setPosition(W / 2f, timerTopY, Align.top | Align.center);
        stage.addActor(timerLabel);

        drawTimerTask = new Timer.Task() {
            @Override public void run() {
                drawTimeLeft--;
                timerLabel.setText(String.valueOf(drawTimeLeft));
                if (drawTimeLeft <= 0) {
                    cancel();
                    finishDrawingAndAdvance();
                }
            }
        };
        Timer.schedule(drawTimerTask, 1, 1, drawTimeLeft - 1);

        // 5) Edit‑panel - Scaled based on screen size
        editPanelTexture = new Texture("editpanel.png");
        editPanelBackground = new Image(editPanelTexture);
        // Define panel size relative to screen
        float panelWidth = W * 0.8f; // e.g., 80% of screen width
        float panelHeight = H * 0.15f; // e.g., 15% of screen height
        editPanelBackground.setSize(panelWidth, panelHeight);

        editPanelGroup = new Group();
        editPanelGroup.setSize(panelWidth, panelHeight);
        // Center the panel on the screen
        editPanelGroup.setPosition((W - panelWidth) / 2f, (H - panelHeight) / 2f);
        editPanelGroup.setVisible(false);
        editPanelGroup.addActor(editPanelBackground);
        stage.addActor(editPanelGroup);

        // 6) Størrelses‑knapper - SizeController will handle layout
        Texture[] sizeTex = {
                new Texture("size1.png"), new Texture("size2.png"),
                new Texture("size3.png"), new Texture("size4.png")
        };
        int[] sizes = {1,3,6,12};
        
        // Create size buttons directly in DrawingScreen for better control
        float buttonSize = panelHeight * 0.30f; 
        float spacing = (panelWidth - (buttonSize * 4)) / 10; 
        float buttonStartX = spacing * 3; 
        float buttonY = panelHeight * 0.30f; 
        
        for (int i = 0; i < sizeTex.length; i++) {
            final int sizeIndex = i;
            Image sizeButton = new Image(sizeTex[i]);
            sizeButton.setSize(buttonSize, buttonSize);
            sizeButton.setPosition(buttonStartX + (spacing + buttonSize) * i, buttonY);
            sizeButton.addListener(new InputListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    currentSize = sizes[sizeIndex];
                    controller.selectPen(controller.getSelectedColor(), currentSize);
                    return true;
                }
            });
            editPanelGroup.addActor(sizeButton);
        }
        
        // We're manually creating the size buttons above, so we don't need the SizeController anymore
        // We're keeping the variable declaration in case it's referenced elsewhere
        //new SizeController(editPanelGroup, sizeTex, sizes, s -> {
        //    currentSize = s;
        //    controller.selectPen(controller.getSelectedColor(), currentSize);
        //});

        // 7) Edit‑tool knapp
        editToolTexture = new Texture("edittool.png");
        editToolButton = new Image(editToolTexture);
        editToolButton.setSize(iconSize, iconSize);
        editToolButton.setPosition(W - iconSize - 20, H - iconSize - 20);
        editToolButton.addListener(new InputListener(){
            @Override public boolean touchDown(InputEvent e, float x, float y, int p, int b) {
                boolean v = !editPanelGroup.isVisible();
                editPanelGroup.setVisible(v);
                editPanelGroup.toFront();
                controller.setToolPanelOpen(v);
                return true;
            }
        });
        stage.addActor(editToolButton);

        // 8) Undo-knapp
        undoTexture = new Texture("undobtn.png");
        undoButton = new Image(undoTexture);
        float barH = H * 0.1f;
        undoButton.setSize(barH*1.2f, barH*0.8f);
        undoButton.setPosition(20, 20);
        undoButton.addListener(new InputListener(){
            @Override public boolean touchDown(InputEvent e, float x, float y, int p, int b) {
                controller.undo(); return true;
            }
        });
        stage.addActor(undoButton);

        // 9) Eraser + farger
        eraserTexture = new Texture("eraser.png");
        ToolButtonFactory.addEraserButton(stage, controller, "eraser.png",
                undoButton.getX()+undoButton.getWidth()+20, 20, barH*0.6f);

        Map<Color,String> cols = new LinkedHashMap<>();
        cols.put(Color.BLACK,"black.png");   cols.put(Color.RED,"red.png");
        cols.put(Color.BLUE,"blue.png");     cols.put(Color.GREEN,"green.png");
        cols.put(Color.YELLOW,"yellow.png"); cols.put(Color.PURPLE,"purple.png");
        cols.put(Color.ORANGE,"orange.png"); cols.put(Color.CYAN,"cyan.png");

        float startX = undoButton.getX()+undoButton.getWidth()+20 + barH*0.6f + 20;
        float avail = W - startX - 20;
        float wBtn = avail / cols.size(), hBtn = barH;
        int i = 0;
        for (Map.Entry<Color,String> e : cols.entrySet()) {
            float x = startX + i * wBtn;
            ToolButtonFactory.addColorPenButton(stage, controller,
                    e.getKey(), e.getValue(), x, 20, wBtn, hBtn, () -> currentSize);
            i++;
        }
        ToolButtonFactory.selectInitialColor(controller.getSelectedColor());
        controller.selectPen(controller.getSelectedColor(), currentSize);

        // 10) Finish‑knapp - Position remains top-left
        TextButton finish = new TextButton("DONE", skin);
        finish.setSize(W*0.28f, H*0.075f);
        finish.setPosition(padding * 3, H - finish.getHeight() - padding * 3);
        finish.getLabel().setFontScale(baseFontScale * 0.8f); 
        finish.getLabel().setAlignment(Align.center);
        finish.addListener(new InputListener(){
            @Override public boolean touchDown(InputEvent e, float x, float y, int p, int b) {
                drawTimerTask.cancel();
                finishDrawingAndAdvance();
                return true;
            }
        });
        stage.addActor(finish);

        // 11) Input multiplexer
        InputMultiplexer m = new InputMultiplexer();
        m.addProcessor(stage);
        m.addProcessor(controller);
        Gdx.input.setInputProcessor(m);
    }

    private void finishDrawingAndAdvance() {
        String myId = GameManager.getInstance().getPlayerId();
        // Kall ny overload med callback:
        new PlayerController(game).finishDrawing(canvas, () -> {
            // Her er vi **sikre** på at Firebase har markert oss ferdige.
            game.setScreen(new GuessingLobbyScreen(game, false));
        });
    }

    @Override public void render(float delta) {
        controller.updateCanvas();
        stage.act(delta);
        stage.draw();
        undoButton.setTouchable(controller.canUndo()?Touchable.enabled:Touchable.disabled);
    }
    @Override public void resize(int width, int height){
        stage.getViewport().update(width, height, true);
    }
    @Override public void show() {}
    @Override public void hide() { dispose(); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void dispose(){
        stage.dispose();
        whiteboardTexture.dispose();
        canvasTexture.dispose();
        canvas.dispose();
        undoTexture.dispose();
        eraserTexture.dispose();
        editToolTexture.dispose();
        editPanelTexture.dispose();
    }

    /** Helper method to calculate font scale to fit text within bounds */
    private float calculateFitScale(BitmapFont font, String text, float targetWidth, float targetHeight) {
        BitmapFont.BitmapFontData fontData = font.getData();
        float originalScaleX = fontData.scaleX;
        float originalScaleY = fontData.scaleY;
        
        // Start with a reasonable guess
        float scale = 1.0f; 
        fontData.setScale(scale);
        GlyphLayout layout = new GlyphLayout(font, text);
        
        // Adjust scale based on width and height constraints
        float scaleX = (targetWidth / layout.width) * scale;
        float scaleY = (targetHeight / layout.height) * scale;
        scale = Math.min(scaleX, scaleY); // Use the smaller scale factor to fit both dimensions
        
        // Reset original scale before returning
        fontData.setScale(originalScaleX, originalScaleY);
        
        return Math.max(0.1f, scale); // Return calculated scale, ensure it's not too small
    }
}
