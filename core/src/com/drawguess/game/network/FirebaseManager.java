package com.drawguess.game.network;

public class FirebaseManager {
    private static FirebaseManager instance;

    // Private constructor prevents instantiation from other classes
    private FirebaseManager() {
        // Initialize your FirebaseManager here
    }

    // Lazy initialization of the instance
    public static synchronized FirebaseManager getInstance() {
        if (instance == null) {
            instance = new FirebaseManager();
        }
        return instance;
    }


}

