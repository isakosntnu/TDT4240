package io.github.drawguess.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
public class WordBank {
    private static WordBank instance;
    private final List<String> words;

    private WordBank() {
        words = new ArrayList<>();
        Collections.addAll(words,
            "apple", "airplane", "banana", "balloon", "bicycle",
            "boat", "book", "bread", "bridge", "bus",
            "camera", "car", "cat", "cloud", "cookie",
            "cow", "cupcake", "dog", "door", "dragon",
            "elephant", "eye", "fire", "fish", "flower",
            "fork", "giraffe", "glasses", "hammer", "hat",
            "house", "ice cream", "key", "ladder", "lamp",
            "leaf", "lion", "moon", "mountain", "mushroom",
            "pencil", "penguin", "pizza", "rain", "robot",
            "shark", "shoe", "snake", "star", "tree",
            "train", "turtle", "umbrella", "vampire", "violin",
            "volcano", "watch", "whale", "window", "witch",
            "zebra", "zoo", "backpack", "bat", "beach",
            "bell", "bench", "bottle", "cactus", "candle",
            "castle", "cheese", "chicken", "clock", "cookie",
            "crayon", "crown", "desert", "diamond", "dolphin",
            "drum", "fan", "feather", "fence", "flag",
            "fridge", "ghost", "grapes", "guitar", "hamburger",
            "helicopter", "honey", "jellyfish", "kangaroo", "kitchen",
            "ladle", "lemon", "mirror", "motorcycle", "octopus"
        );
    }

    public static WordBank getInstance() {
        if (instance == null) {
            instance = new WordBank();
        }
        return instance;
    }

    public List<String> getAllWords() {
        return new ArrayList<>(words);
    }

    public String pullRandomWord() {
        if (words.isEmpty()) return null;
        int index = new Random().nextInt(words.size());
        return words.remove(index);
    }
}
