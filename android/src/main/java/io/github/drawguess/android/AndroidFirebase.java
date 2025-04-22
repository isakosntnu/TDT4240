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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;


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
import io.github.drawguess.android.WordUploader;
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
                    uploadWordsToRealtime(gameId); 


                    Map<String, Object> playerData = new HashMap<>();
                    playerData.put("name", hostName);
                    playerData.put("joinedAt", FieldValue.serverTimestamp());
                    playerData.put("score", 0);
                    playerData.put("finished", false);
                    playerData.put("guessFinished", false);


                    db.collection("games").document(gameId)
                            .collection("players").document(playerId)
                            .set(playerData)
                            .addOnSuccessListener(unused -> Log.d("Firebase", "Host added with ID: " + playerId))
                            .addOnFailureListener(e -> Log.w("Firebase", "Failed to add host", e));
                })
                .addOnFailureListener(e -> Log.w("Firebase", "Failed to create game", e));
    }

    private void uploadWordsToRealtime(String gameId) {
        Map<String, Object> wordsMap = new HashMap<>();
        List<String> words = WordBank.getInstance().getAllWords();

        for (String word : words) {
            wordsMap.put(word, true);
        }

        realtimeDb.child("games").child(gameId).child("available_words")
                .setValue(wordsMap)
                .addOnSuccessListener(aVoid -> Log.d("Firebase", "Word uploaded"))
                .addOnFailureListener(e -> Log.e("Firebase", "Word upload error", e));
    }

    @Override
    public void joinGame(String gameId, String playerName) {
        String playerId = GameManager.getInstance().getPlayerId();

        Map<String, Object> playerData = new HashMap<>();
        playerData.put("name", playerName);
        playerData.put("joinedAt", FieldValue.serverTimestamp());
        playerData.put("score", 0);
        playerData.put("finished", false);
        playerData.put("guessFinished", false);

        db.collection("games").document(gameId)
                .collection("players").document(playerId)
                .set(playerData)
                .addOnSuccessListener(aVoid -> Log.d("Firebase", "Player joined with ID: " + playerId))
                .addOnFailureListener(e -> Log.w("Firebase", "Failed to add player", e));
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
            Log.e("SOCKET", "join game error", e);
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
                .child("available_words");

        wordsRef.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                Map<String, Object> wordMap = currentData.getValue(new GenericTypeIndicator<Map<String, Object>>() {
                });
                if (wordMap == null || wordMap.isEmpty()) {
                    return Transaction.abort(); // ingen ord igjen
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
                    callback.onFailure(new Exception("Could not get word"));
                    return;
                }

                String selectedWord = currentData.child("_selected").getValue(String.class);
                if (selectedWord != null) {
                    callback.onSuccess(selectedWord);
                } else {
                    callback.onFailure(new Exception("No words available"));
                }
            }
        });
    }

    @Override
    public void getPlayersWithStatus(String gameId,
                                     SuccessCallback<Map<String, Boolean>> onSuccess,
                                     FailureCallback onFailure) {
        db.collection("games")
            .document(gameId)
            .collection("players")
            .get()
            .addOnSuccessListener(querySnapshot -> {
                Map<String, Boolean> result = new HashMap<>();
                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    String name = doc.getString("name");
                    Boolean finished = doc.getBoolean("finished");
                    if (name != null && finished != null) {
                        result.put(name, finished);
                    }
                }
                onSuccess.onSuccess(result);
            })
            .addOnFailureListener(onFailure::onFailure);
    }

    public void checkGameExists(String gameId,
                                SuccessCallback<Boolean> onSuccess,
                                FailureCallback onError) {
        db.collection("games")
        .document(gameId)
        .get()
        .addOnSuccessListener(doc -> onSuccess.onSuccess(doc.exists()))
        .addOnFailureListener(onError::onFailure);
    }


    @Override
    public void setPlayerWord(String gameId, String playerId, String word) {
        Map<String, Object> update = new HashMap<>();
        update.put("word", word);

        db.collection("games").document(gameId)
                .collection("players").document(playerId)
                .update(update)
                .addOnSuccessListener(aVoid -> Log.d("Firebase", "Success" + playerId))
                .addOnFailureListener(e -> Log.e("Firebase", "error", e));
    }




    @Override
    public void getDrawingsForGuessing(String gameId,
                                       String myPlayerId,
                                       SuccessCallback<Map<String,String>> onSuccess,
                                       FailureCallback onError) {
        db.collection("games").document(gameId)
                .collection("players")
                .get()
                .addOnSuccessListener(qs -> {
                    Map<String, String> drawings = new HashMap<>();
                    for (DocumentSnapshot doc : qs.getDocuments()) {
                        String pid = doc.getId();
                        if (pid.equals(myPlayerId)) continue;
                        String url = doc.getString("drawingUrl");
                        if (url != null) drawings.put(pid, url);
                    }
                    onSuccess.onSuccess(drawings);
                })
                .addOnFailureListener(e -> onError.onFailure(new Exception(e)));
    }


    @Override
    public void submitGuessResult(String gameId,
                                  String playerId,
                                  int points,
                                  Runnable onSuccess,
                                  FailureCallback onError) {
        DocumentReference ref = db.collection("games")
                .document(gameId)
                .collection("players")
                .document(playerId);
        ref.update("score", FieldValue.increment(points))
                .addOnSuccessListener(a -> onSuccess.run())
                .addOnFailureListener(e -> onError.onFailure(new Exception(e)));
    }


    @Override
    public void setPlayerGuessDone(String gameId,
                                   String playerId,
                                   Runnable onSuccess,
                                   FailureCallback onError) {
        Log.d("AndroidFirebase", "Setting guessFinished=true for player " + playerId + " in game " + gameId);
        
        DocumentReference ref = db.collection("games")
                .document(gameId)
                .collection("players")
                .document(playerId);
        

        ref.get().addOnSuccessListener(document -> {
            if (document.exists()) {
                // Document exists, update the field
                Map<String, Object> updates = new HashMap<>();
                updates.put("guessFinished", true);
                
                ref.update(updates)
                   .addOnSuccessListener(aVoid -> {
                       Log.d("AndroidFirebase", "Successfully marked player " + playerId + " as done guessing");
                       onSuccess.run();
                   })
                   .addOnFailureListener(e -> {
                       Log.e("AndroidFirebase", "Failed to mark player as done guessing", e);
                       onError.onFailure(new Exception("Failed to update guessFinished status: " + e.getMessage()));
                   });
            } else {
                // Document doesn't exist, create it
                Map<String, Object> playerData = new HashMap<>();
                playerData.put("guessFinished", true);
                
                ref.set(playerData)
                   .addOnSuccessListener(aVoid -> {
                       Log.d("AndroidFirebase", "Created player document and marked as done guessing");
                       onSuccess.run();
                   })
                   .addOnFailureListener(e -> {
                       Log.e("AndroidFirebase", "Failed to create player document", e);
                       onError.onFailure(new Exception("Failed to create player document: " + e.getMessage()));
                   });
            }
        }).addOnFailureListener(e -> {
            Log.e("AndroidFirebase", "Error checking if player document exists", e);
            onError.onFailure(new Exception("Error checking if player document exists: " + e.getMessage()));
        });
    }


    @Override
    public void getPlayersGuessStatus(String gameId,
                                      SuccessCallback<Map<String,Boolean>> onSuccess,
                                      FailureCallback onError) {
        db.collection("games").document(gameId)
                .collection("players")
                .get()
                .addOnSuccessListener(qs -> {
                    Map<String, Boolean> status = new HashMap<>();
                    for (DocumentSnapshot doc : qs.getDocuments()) {
                        // Include all players, defaulting to false if guessFinished is not set
                        Boolean done = doc.getBoolean("guessFinished");
                        status.put(doc.getId(), done != null ? done : false);
                    }
                    onSuccess.onSuccess(status);
                })
                .addOnFailureListener(e -> onError.onFailure(new Exception(e)));
    }


    @Override
    public void getPlayersWithScores(String gameId,
                                     SuccessCallback<Map<String,Integer>> onSuccess,
                                     FailureCallback onError) {
        db.collection("games").document(gameId)
                .collection("players")
                .get()
                .addOnSuccessListener(qs -> {
                    Map<String, Integer> scores = new HashMap<>();
                    for (DocumentSnapshot doc : qs.getDocuments()) {
                        Long scoreLong = doc.getLong("score");
                        if (scoreLong != null) {
                            scores.put(doc.getId(), scoreLong.intValue());
                        } else {
                            scores.put(doc.getId(), 0); // Default score
                        }
                    }
                    onSuccess.onSuccess(scores);
                })
                .addOnFailureListener(e -> onError.onFailure(new Exception(e)));
    }


    @Override
    public void getAllPlayerProfiles(String gameId,
                                    SuccessCallback<List<Map<String,Object>>> onSuccess,
                                    FailureCallback onError) {
        db.collection("games").document(gameId)
                .collection("players")
                .get()
                .addOnSuccessListener(qs -> {
                    List<Map<String,Object>> players = new ArrayList<>();
                    for (DocumentSnapshot doc : qs.getDocuments()) {
                        Map<String,Object> player = new HashMap<>();
                        
                        // Add player ID
                        player.put("id", doc.getId());
                        
                        // Add player name
                        String name = doc.getString("name");
                        player.put("name", name != null ? name : "Unknown");
                        
                        // Add player score
                        Long scoreLong = doc.getLong("score");
                        int score = scoreLong != null ? scoreLong.intValue() : 0;
                        player.put("score", score);
                        
                        players.add(player);
                    }
                    onSuccess.onSuccess(players);
                })
                .addOnFailureListener(e -> onError.onFailure(new Exception(e)));
    }

    @Override
    public void deleteGameData(String gameId,
                               Runnable onSuccess,
                               FailureCallback onError) {
        // 1. Delete Firestore game document
        db.collection("games").document(gameId)
            .delete()
            .addOnSuccessListener(aVoid -> {
                Log.d("Firebase", "Deleted Firestore game document: " + gameId);
                
                // 2. Delete drawings folder in Storage
                String storagePath = "drawings/" + gameId;
                StorageReference storageRef = storage.getReference().child(storagePath);

                // List all items in the folder and delete them
                storageRef.listAll()
                    .addOnSuccessListener(listResult -> {
                        List<StorageReference> items = listResult.getItems();
                        if (items.isEmpty()) {
                            Log.d("Firebase", "No drawings to delete in Storage for game: " + gameId);
                            onSuccess.run(); 
                            return;
                        }

                        final int totalItems = items.size();
                        final int[] deletedCount = {0};
                        final boolean[] errorOccurred = {false};

                        for (StorageReference item : items) {
                            item.delete().addOnSuccessListener(taskSnapshot -> {
                                synchronized (deletedCount) {
                                    deletedCount[0]++;
                                    if (!errorOccurred[0] && deletedCount[0] == totalItems) {
                                        Log.d("Firebase", "Deleted Storage drawings for game: " + gameId);
                                        onSuccess.run();
                                    }
                                }
                            }).addOnFailureListener(e -> {
                                synchronized (errorOccurred) {
                                    if (!errorOccurred[0]) {
                                        errorOccurred[0] = true;
                                        Log.e("Firebase", "Failed to delete item " + item.getPath(), e);
                                        onError.onFailure(new Exception("Failed to delete a drawing: " + e.getMessage()));
                                    }
                                }
                            });
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Firebase", "Failed to list items in Storage for game: " + gameId, e);
                        onError.onFailure(new Exception("Failed to list drawings: " + e.getMessage()));
                    });
            })
            .addOnFailureListener(e -> {
                Log.e("Firebase", "Failed to delete Firestore game document: " + gameId, e);
                onError.onFailure(new Exception("Failed to delete game document: " + e.getMessage()));
            });
    }

}
