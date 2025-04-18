package io.github.drawguess.android;

import android.util.Log;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;
import io.github.drawguess.server.FirebaseInterface;


import java.util.HashMap;
import java.util.Map;

public class AndroidFirebase implements FirebaseInterface {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    public void createGame() {
        Map<String, Object> gameData = new HashMap<>();
        gameData.put("status", "waiting");
        gameData.put("createdAt", FieldValue.serverTimestamp());

        db.collection("games").add(gameData)
            .addOnSuccessListener(documentReference -> {
                Log.d("Firebase", "Game created: " + documentReference.getId());
            })
            .addOnFailureListener(e -> {
                Log.w("Firebase", "Error creating game", e);
            });
    }

    @Override
    public void joinGame(String gameId, String playerName) {
        Map<String, Object> playerData = new HashMap<>();
        playerData.put("name", playerName);
        playerData.put("joinedAt", FieldValue.serverTimestamp());

        db.collection("games").document(gameId)
            .collection("players")
            .add(playerData);
    }

    @Override
    public void sendGuess(String guess) {
        Log.d("Firebase", "Guess sent: " + guess);
        // Legg til firestore-logikk her også!
    }
}
