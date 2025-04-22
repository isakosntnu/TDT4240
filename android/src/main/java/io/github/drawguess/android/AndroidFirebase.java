package io.github.drawguess.android;

import android.util.Log;

import androidx.annotation.NonNull;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.firebase.database.*;
import com.google.firebase.firestore.DocumentSnapshot;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import io.github.drawguess.android.manager.SocketManager;
import io.github.drawguess.manager.GameManager;
import io.github.drawguess.model.GameSession;
import io.github.drawguess.server.FirebaseInterface;
import io.github.drawguess.model.WordBank;
import io.github.drawguess.server.FirebaseCallback;

public class AndroidFirebase implements FirebaseInterface {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseStorage storage = FirebaseStorage.getInstance();
    private final DatabaseReference realtimeDb = FirebaseDatabase.getInstance().getReference();

    @Override
    public void createGame(GameSession session) {

        String hostName = session.getHostPlayer().getName();
        String gameId = session.getGameId();
        String playerId = session.getHostPlayer().getId();

        GameManager.getInstance().setPlayerId(playerId);

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
                    String word = WordBank.getInstance().pullRandomWord();
                    playerData.put("name", hostName);
                    playerData.put("joinedAt", FieldValue.serverTimestamp());
                    playerData.put("score", 0);
                    playerData.put("finished", false);
                    playerData.put("word", word);

                    db.collection("games").document(gameId)
                            .collection("players").document(playerId)
                            .set(playerData)
                            .addOnSuccessListener(unused -> {
                                Log.d("Firebase", "Host added with ID: " + playerId);

                            })
                            .addOnFailureListener(e -> Log.w("Firebase", "Failed to add host", e));
                })
                .addOnFailureListener(e -> Log.w("Firebase", "Failed to create game", e));
    }

    @Override
    public void joinGame(String gameId, String playerName) {
        String playerId = GameManager.getInstance().getPlayerId();
        String word = WordBank.getInstance().pullRandomWord();

        Map<String, Object> playerData = new HashMap<>();
        playerData.put("name", playerName);
        playerData.put("joinedAt", FieldValue.serverTimestamp());
        playerData.put("score", 0);
        playerData.put("finished", false);
        playerData.put("word", word);
        db.collection("games").document(gameId)
                .collection("players").document(playerId)
                .set(playerData)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firebase", "Player joined with ID: " + playerId);

                })

                .addOnFailureListener(e -> Log.w("Firebase", "Failed to add player", e));
    }

    private void assignWordToPlayer(String gameId, String playerId) {
        // Grab a random word from local WordBank
        String word = WordBank.getInstance().pullRandomWord(); // Or getRandomWord() if duplicates are OK

        // Store the word under the player in Firebase (no list, just one value)
        setPlayerWord(gameId, playerId, word);
    }

    @Override
    public void sendGuess(String guess) {
        Log.d("Firebase", "Guess sent: " + guess);
    }

    @Override
    public void uploadDrawing(String gameId, String playerId, byte[] pngData,
            SuccessCallback<String> onSuccess,
            FailureCallback onError) {

        String filePath = "drawings/" + gameId + "/" + playerId + ".png";
        StorageReference ref = storage.getReference().child(filePath);

        UploadTask uploadTask = ref.putBytes(pngData);

        uploadTask.addOnSuccessListener(taskSnapshot -> ref.getDownloadUrl().addOnSuccessListener(uri -> {
            String downloadUrl = uri.toString();
            Log.d("Firebase", "Drawing uploaded: " + downloadUrl);
            onSuccess.onSuccess(downloadUrl);
        }).addOnFailureListener(onError::onFailure)).addOnFailureListener(onError::onFailure);
    }

    @Override
    public void getPlayerDrawingUrl(String gameId, String playerId,
            SuccessCallback<String> onSuccess,
            FailureCallback onError) {
        db.collection("games")
                .document(gameId)
                .collection("players")
                .document(playerId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists() && snapshot.contains("drawingUrl")) {
                        onSuccess.onSuccess(snapshot.getString("drawingUrl"));
                    } else {
                        onError.onFailure(new Exception("No drawingUrl found"));
                    }
                })
                .addOnFailureListener(onError::onFailure);
    }

    @Override
    public void setPlayerFinished(String gameId, String playerId, String drawingUrl, String word) {
        Map<String, Object> update = new HashMap<>();
        update.put("finished", true);
        update.put("drawingUrl", drawingUrl);
        update.put("finishedAt", FieldValue.serverTimestamp());
        update.put("word", word);

        db.collection("games").document(gameId)
                .collection("players").document(playerId)
                .update(update)
                .addOnSuccessListener(aVoid -> Log.d("Firebase", "Player marked as finished: " + playerId))
                .addOnFailureListener(e -> Log.w("Firebase", "Failed to mark player as finished", e));
    }

    @Override
    public void getPlayerWord(String gameId, String playerId,
            SuccessCallback<String> onSuccess,
            FailureCallback onError) {
        db.collection("games").document(gameId)
                .collection("players").document(playerId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists() && snapshot.contains("word")) {
                        onSuccess.onSuccess(snapshot.getString("word"));
                    } else {
                        onError.onFailure(new Exception("Word not found"));
                    }
                })
                .addOnFailureListener(onError::onFailure);
    }

    @Override
    public void emitUserJoined(String gameId, String username) {
        try {
            JSONObject data = new JSONObject();
            data.put("gameId", gameId);
            data.put("username", username);
            SocketManager.getSocket().emit("joinGame", data);
        } catch (Exception e) {
            Log.e("SOCKET", "❌ Feil ved joinGame-emission", e);
        }
    }

    @Override
    public FirebaseFirestore getFirestore() {
        return db;
    }

    @Override
    public void getPlayersInLobby(String gameId, SuccessCallback<List<String>> onSuccess, FailureCallback onError) {
        db.collection("games")
                .document(gameId)
                .collection("players")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<String> playerNames = new ArrayList<>();
                    querySnapshot.forEach(document -> {
                        String playerName = document.getString("name");
                        if (playerName != null) {
                            playerNames.add(playerName);
                        }
                    });
                    onSuccess.onSuccess(playerNames);
                })
                .addOnFailureListener(onError::onFailure);
    }

    @Override
    public void getRandomWord(String gameId, FirebaseCallback<String> callback) {
        DatabaseReference wordsRef = FirebaseDatabase.getInstance()
                .getReference("games")
                .child(gameId)
                .child("words");

        wordsRef.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                Map<String, Object> wordMap = currentData.getValue(new GenericTypeIndicator<Map<String, Object>>() {
                });
                if (wordMap == null || wordMap.isEmpty()) {
                    return Transaction.abort();
                }

                List<String> words = new ArrayList<>(wordMap.keySet());
                String selectedWord = words.get(new Random().nextInt(words.size()));

                currentData.child(selectedWord).setValue(null);
                currentData.child("_selected").setValue(selectedWord);

                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(DatabaseError error, boolean committed, DataSnapshot currentData) {
                if (error != null || !committed || currentData == null) {
                    callback.onFailure(new Exception("Klarte ikke hente ord"));
                    return;
                }

                String selectedWord = currentData.child("_selected").getValue(String.class);
                if (selectedWord != null) {
                    callback.onSuccess(selectedWord);
                } else {
                    callback.onFailure(new Exception("Ingen ord tilgjengelig"));
                }
            }
        });
    }

    @Override
    public void getPlayersWithStatus(String gameId,
            SuccessCallback<Map<String, Boolean>> onSuccess,
            FailureCallback onFailure) {
        db.collection("games").document(gameId)
                .collection("players")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    Map<String, Boolean> playerStatuses = new HashMap<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        String name = doc.getString("name");
                        Boolean finished = doc.getBoolean("finished");

                        if (name != null) {
                            boolean isFinished = finished != null && finished;
                            Log.d("Firebase", "🔍 " + name + ": finished = " + isFinished);
                            playerStatuses.put(name, isFinished);
                        }
                    }

                    Log.d("Firebase", "Totalt spillere med status: " + playerStatuses.size());
                    onSuccess.onSuccess(playerStatuses);
                })
                .addOnFailureListener(onFailure::onFailure);
    }

    @Override
    public void setPlayerWord(String gameId, String playerId, String word) {
        DatabaseReference playerRef = FirebaseDatabase.getInstance().getReference("games")
                .child(gameId).child("players").child(playerId).child("word");

        playerRef.setValue(word)
                .addOnSuccessListener(aVoid -> Log.d("Firebase", "Word assigned successfully"))
                .addOnFailureListener(e -> Log.e("Firebase", "Failed to assign word", e));
    }

}
