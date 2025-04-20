package io.github.drawguess.android;

import android.net.Uri;
import android.util.Log;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import io.github.drawguess.model.GameSession;
import io.github.drawguess.model.Player;
import io.github.drawguess.server.FirebaseInterface;

import java.util.HashMap;
import java.util.Map;

public class AndroidFirebase implements FirebaseInterface {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseStorage storage = FirebaseStorage.getInstance();

    @Override
    public void createGame(GameSession session) {
        String hostName = session.getHostPlayer().getName();
        String gameId = session.getGameId();
        String playerId = session.getHostPlayer().getId();

        Map<String, Object> gameData = new HashMap<>();
        gameData.put("status", "waiting");
        gameData.put("host", hostName);
        gameData.put("gamePin", gameId);
        gameData.put("createdAt", FieldValue.serverTimestamp());

        db.collection("games").document(gameId)
            .set(gameData)
            .addOnSuccessListener(aVoid -> {
                Log.d("Firebase", "Game created: " + gameId);

                Map<String, Object> playerData = new HashMap<>();
                playerData.put("name", hostName);
                playerData.put("joinedAt", FieldValue.serverTimestamp());
                playerData.put("score", 0);

                db.collection("games").document(gameId)
                    .collection("players").document(playerId)
                    .set(playerData)
                    .addOnSuccessListener(unused -> Log.d("Firebase", "Host added with ID: " + playerId))
                    .addOnFailureListener(e -> Log.w("Firebase", "Failed to add host", e));
            })
            .addOnFailureListener(e -> Log.w("Firebase", "Failed to create game", e));
    }

    @Override
    public void joinGame(String gameId, String playerName) {
        // Generate a unique ID for the player (should be handled earlier and stored in GameManager)
        String playerId = io.github.drawguess.manager.GameManager.getInstance().getPlayerId();

        Map<String, Object> playerData = new HashMap<>();
        playerData.put("name", playerName);
        playerData.put("joinedAt", FieldValue.serverTimestamp());
        playerData.put("score", 0);

        db.collection("games").document(gameId)
            .collection("players").document(playerId)
            .set(playerData)
            .addOnSuccessListener(aVoid -> Log.d("Firebase", "Player joined with ID: " + playerId))
            .addOnFailureListener(e -> Log.w("Firebase", "Failed to add player", e));
    }

    @Override
    public void sendGuess(String guess) {
        Log.d("Firebase", "Guess sent: " + guess);
        // TODO: implement actual logic for guess saving
    }

    @Override
    public void uploadDrawing(String gameId, String playerId, byte[] pngData,
                              SuccessCallback<String> onSuccess,
                              FailureCallback onError) {

        String filePath = "drawings/" + gameId + "/" + playerId + ".png";
        StorageReference ref = storage.getReference().child(filePath);

        UploadTask uploadTask = ref.putBytes(pngData);

        uploadTask.addOnSuccessListener(taskSnapshot ->
            ref.getDownloadUrl().addOnSuccessListener(uri -> {
                String downloadUrl = uri.toString();
                Log.d("Firebase", "Drawing uploaded: " + downloadUrl);
                onSuccess.onSuccess(downloadUrl);
            }).addOnFailureListener(e -> {
                Log.e("Firebase", "Failed to get download URL", e);
                onError.onFailure(e);
            })
        ).addOnFailureListener(e -> {
            Log.e("Firebase", "Upload failed", e);
            onError.onFailure(e);
        });
    }

    @Override
    public void setPlayerFinished(String gameId, String playerId, String drawingUrl) {
        Map<String, Object> update = new HashMap<>();
        update.put("finished", true);
        update.put("drawingUrl", drawingUrl);
        update.put("finishedAt", FieldValue.serverTimestamp());

        db.collection("games").document(gameId)
            .collection("players").document(playerId)
            .update(update)
            .addOnSuccessListener(aVoid ->
                Log.d("Firebase", "Player marked as finished: " + playerId))
            .addOnFailureListener(e ->
                Log.w("Firebase", "Failed to mark player as finished", e));
    }
}
