package my_base;

import team.control.GameController;
import team.model.Canvas;

/*
 * This class should hold the content of the system, i.e., all elements that are
 * related to the essence of the system.
 * 
 */
public class AppContent {
	private Canvas canvas = new Canvas();
	private GameController GameController;

	public void initContent() {
		GameController = new GameController();
		canvas.initCanvas();
	};

	public Canvas canvas() {
		return canvas;
	}	
	public GameController game() {
		return GameController;
	}
}
