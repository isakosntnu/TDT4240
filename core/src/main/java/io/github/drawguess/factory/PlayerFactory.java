/* ────────────────────────────────────────────────────────────────
 *  File: PlayerFactory.java
 *  Package: io.github.drawguess.factory
 * ──────────────────────────────────────────────────────────────── */
package io.github.drawguess.factory;

import java.util.UUID;

import io.github.drawguess.model.Player;

/**  Lager Player‑objekter på en standardisert måte. */
public class PlayerFactory {

    /** Returnerer en spiller med et tilfeldig UUID som ID. */
    public static Player createPlayer(String name) {
        String uuid = UUID.randomUUID().toString();
        return new Player(uuid, name, /*initialScore=*/0, /*host=*/false);
    }

    /** Returnerer spilleren som host (score = 0, host=true). */
    public static Player createHost(String name) {
        String uuid = UUID.randomUUID().toString();
        return new Player(uuid, name, /*initialScore=*/0, /*host=*/true);
    }

    /* Hvis du bruker Firebase, kan du også lage en factory‑metode som
       bygger Player fra et DocumentSnapshot:  
     
       public static Player fromSnapshot(DocumentSnapshot doc) { ... }
    */
}
