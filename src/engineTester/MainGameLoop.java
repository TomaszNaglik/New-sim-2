package engineTester;

import org.lwjgl.opengl.Display;

import game.Game;
import renderEngine.DisplayManager;

public class MainGameLoop {
	
	public static void main(String[] args) throws InterruptedException {
		
		DisplayManager.createDisplay();	
		
		Game game = new Game();
		game.start();
		game.end();
			
		DisplayManager.closeDisplay();
	}
}

