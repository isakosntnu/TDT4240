package io.github.drawguess.controller;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import io.github.drawguess.DrawGuessMain;
import io.github.drawguess.manager.GameManager;
import io.github.drawguess.model.GameSession;
import io.github.drawguess.model.Player;
import io.github.drawguess.model.WordBank;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

public class PlayerController {

    private final DrawGuessMain game;
    private final GameSession session;

    public PlayerController(DrawGuessMain game) {
        this.game = game;
        this.session = GameManager.getInstance().getSession();

        // Hvis denne spilleren ikke har fått et ord enda, trekk ett fra WordBank og sett på session:
        String myId   = GameManager.getInstance().getPlayerId();
        String current = session.getWordForPlayer(myId);
        if (current == null || current.isEmpty() || "unknown".equals(current)) {
            String pulled = WordBank.getInstance().pullRandomWord();
            String toSet  = pulled != null ? pulled : "unknown";
            session.setWordForPlayer(myId, toSet);
            Gdx.app.log("PlayerController",
                    "Assigned word \"" + toSet + "\" to player " + myId);
        }
    }


    public void finishDrawing(Pixmap canvas, Runnable onFinishedCallback) {
        if (canvas == null) {
            Gdx.app.error("PlayerController", "Canvas is null, cannot save drawing.");
            return;
        }

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

        String playerId = GameManager.getInstance().getPlayerId();
        String gameId   = session.getGameId();
        String word     = session.getWordForPlayer(playerId);

        game.getFirebase().uploadDrawing(
                gameId,
                playerId,
                pngData,
                url -> {
                    game.getFirebase().setPlayerFinished(gameId, playerId, url, word);
                    Player me = session.getPlayerById(playerId);
                    if (me != null) me.setFinishedDrawing(true);

                    Gdx.app.postRunnable(onFinishedCallback);
                },
                error -> Gdx.app.error("PlayerController", "Could not upload drawing", error)
        );
    }
}
