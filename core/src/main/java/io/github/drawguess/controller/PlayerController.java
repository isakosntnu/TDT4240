package io.github.drawguess.controller;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import io.github.drawguess.DrawGuessMain;
import io.github.drawguess.manager.GameManager;
import io.github.drawguess.model.GameSession;
import io.github.drawguess.model.Player;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

public class PlayerController {

    private final DrawGuessMain game;
    private final GameSession session;

    public PlayerController(DrawGuessMain game) {
        this.game = game;
        this.session = GameManager.getInstance().getSession();
    }

    public void finishDrawing(Pixmap canvas) {
        if (canvas == null) {
            Gdx.app.error("PlayerController", "Canvas is null, cannot save drawing.");
            return;
        }

        // 1️⃣ Lag PNG-bilde i minne
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        PixmapIO.PNG writer = new PixmapIO.PNG();
        writer.setFlipY(false);

        try {
            writer.write(dos, canvas);
            dos.flush();
        } catch (Exception e) {
            Gdx.app.error("PlayerController", "PNG encode failed", e);
            return;
        } finally {
            writer.dispose();
            try { dos.close(); } catch (Exception ignored) {}
        }

        byte[] pngData = baos.toByteArray();
        Gdx.app.log("PlayerController", "Encoded PNG, size: " + pngData.length + " bytes");

        // 2️⃣ Last opp til Firebase
        String playerId = GameManager.getInstance().getPlayerId();
        String gameId = session.getGameId();

        game.getFirebase().uploadDrawing(
                gameId,
                playerId,
                pngData,
                url -> {
                    Gdx.app.log("PlayerController", "Upload succeeded: " + url);
                    game.getFirebase().setPlayerFinished(gameId, playerId, url);
                    Player me = session.getPlayerById(playerId);
                    if (me != null) me.setFinishedDrawing(true);
                },
                error -> Gdx.app.error("PlayerController", "Could not upload drawing", error)
        );
    }
}
