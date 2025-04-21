package io.github.drawguess.android;

import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WordUploader {

    public static void uploadWords(String gameId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        List<String> words = Arrays.asList(
                "apple", "banana", "car", "dog", "elephant", "fish", "guitar", "house", "ice", "jungle",
                "kite", "lion", "mountain", "noodle", "ocean", "pencil", "queen", "robot", "sun", "tree",
                "umbrella", "violin", "whale", "xylophone", "yacht", "zebra", "airplane", "ball", "cake", "drum",
                "ear", "flag", "glove", "hat", "igloo", "jar", "kangaroo", "lamp", "mirror", "necklace",
                "orange", "penguin", "quilt", "rocket", "snake", "turtle", "vase", "watch", "x-ray", "yogurt",
                "zoo", "anchor", "beach", "cloud", "dolphin", "eagle", "feather", "glasses", "hammer", "ink",
                "jacket", "key", "ladder", "moon", "needle", "owl", "pizza", "quicksand", "ring", "star",
                "tent", "unicorn", "volcano", "wheel", "xmas", "yeti", "zeppelin", "candle", "dice", "envelope",
                "fan", "grape", "hamburger", "igloo", "jeep", "koala", "lizard", "magnet", "nest", "octopus",
                "pumpkin", "quiet", "radio", "scissors", "trampoline", "ukulele", "vulture", "window", "yarn",
                "zipper");

        Map<String, Object> wordData = new HashMap<>();
        wordData.put("words", words);

        db.collection("games").document(gameId)
                .update(wordData)
                .addOnSuccessListener(aVoid -> Log.d("Firebase", "✅ Words added to game: " + gameId))
                .addOnFailureListener(e -> Log.e("Firebase", "❌ Failed to add words to game", e));
    }
}
