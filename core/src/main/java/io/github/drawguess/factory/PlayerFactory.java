package io.github.drawguess.factory;

import java.util.UUID;

import io.github.drawguess.model.Player;


public class PlayerFactory {


    public static Player createPlayer(String name) {
        String uuid = UUID.randomUUID().toString();
        return new Player(uuid, name, /*initialScore=*/0, /*host=*/false);
    }


    public static Player createHost(String name) {
        String uuid = UUID.randomUUID().toString();
        return new Player(uuid, name, /*initialScore=*/0, /*host=*/true);
    }

}
