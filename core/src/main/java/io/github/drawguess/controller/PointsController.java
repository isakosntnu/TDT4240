package io.github.drawguess.controller;

import com.badlogic.gdx.Gdx;

public class PointsController {

    private static final int Tmax = 30;  
    private static final int Smax = 500; 

    // Checking the time it takes to calculate points
    public static int calculatePointsWithTiming(boolean correct, int guessTimeLeft) {
        long startTime = System.nanoTime();  

        int points = calculatePoints(correct, guessTimeLeft);

        long endTime = System.nanoTime();    
        long durationNs = endTime - startTime;
        double durationMs = durationNs / 1_000_000.0;  

        Gdx.app.log("PointsController", "Point calculation took " + durationMs + " ms");

        return points;
    }


    public static int calculatePoints(boolean correct, int guessTimeLeft) {
        if (!correct) {
            return 0; 
        }

        int Tguess = Tmax - guessTimeLeft;

        if (Tguess < 0 || Tguess > Tmax) {
            return 0; 
        }

        double numerator = Math.log(Tguess + 1);
        double denominator = Math.log(Tmax + 1);
        double scoreFraction = 1.0 - (numerator / denominator);
        return (int) Math.floor(Smax * scoreFraction);
    }
}
