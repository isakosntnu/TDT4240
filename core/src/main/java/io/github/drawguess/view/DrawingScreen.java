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

        // 4) TIMER midt øverst
        timerLabel = new Label(String.valueOf(drawTimeLeft), skin);
        timerLabel.setFontScale(2f);
        timerLabel.setPosition(W/2f, H - 20, Align.top | Align.center);
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

        // 5) Edit‑panel
        editPanelTexture = new Texture("editpanel.png");
        editPanelBackground = new Image(editPanelTexture);
        editPanelBackground.setSize(W * 0.6f, H * 0.2f);
        editPanelGroup = new Group();
        editPanelGroup.setSize(editPanelBackground.getWidth(), editPanelBackground.getHeight());
        editPanelGroup.setPosition((W - editPanelGroup.getWidth())/2f,
                (H - editPanelGroup.getHeight())/2f);
        editPanelGroup.setVisible(false);
        editPanelGroup.addActor(editPanelBackground);
        stage.addActor(editPanelGroup);

        // 6) Størrelses‑knapper
        Texture[] sizeTex = {
                new Texture("size1.png"), new Texture("size2.png"),
                new Texture("size3.png"), new Texture("size4.png")
        };
        int[] sizes = {1,3,6,12};
        new SizeController(editPanelGroup, sizeTex, sizes, s -> {
            currentSize = s;
            controller.selectPen(controller.getSelectedColor(), currentSize);
        });

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

        // 10) Finish‑knapp
        TextButton finish = new TextButton("FINISH DRAWING", skin);
        finish.setSize(W*0.28f, H*0.075f);
        finish.setPosition(20, H - H*0.075f - 20);
        finish.addListener(new InputListener(){
            @Override public boolean touchDown(InputEvent e, float x, float y, int p, int b) {
                drawTimerTask.cancel();
                finishDrawingAndAdvance();
                return true;
            }
        });
        stage.addActor(finish);

        // 11) Vis ord hentet fra GameSession
        String playerId = GameManager.getInstance().getPlayerId();
        String word = GameManager.getInstance()
                .getSession()
                .getWordForPlayer(playerId);
        Label.LabelStyle style = new Label.LabelStyle(new BitmapFont(), Color.BLACK);
        Label lbl1 = new Label("Draw:", style);
        Label lbl2 = new Label(word, style);

        float scale = H * 0.0016f;
        lbl1.setFontScale(scale);
        lbl2.setFontScale(scale);

        GlyphLayout g1 = new GlyphLayout(lbl1.getStyle().font, lbl1.getText());
        GlyphLayout g2 = new GlyphLayout(lbl2.getStyle().font, lbl2.getText());
        float textW = Math.max(g1.width, g2.width) * scale;

        lbl1.setPosition(W/2f - textW/2f, H - 60, Align.left);
        lbl2.setPosition(W/2f - textW/2f, H - 80, Align.left);

        stage.addActor(lbl1);
        stage.addActor(lbl2);

        // 12) Input multiplexer
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
}
