package com.drawguess.game;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.maps.tiled.TideMapLoader;
import com.drawguess.game.WordBattle;

// Please note that on macOS your application needs to be started with the -XstartOnFirstThread JVM argument
public class DesktopLauncher {
	public static void main (String[] arg) {
		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		int width = WordBattle.WIDTH;
		int height = WordBattle.HEIGHT;
		String title = WordBattle.TITLE;
		config.setWindowedMode(width, height);
		config.setTitle(title);
		new Lwjgl3Application(new WordBattle(), config);
	}
}
