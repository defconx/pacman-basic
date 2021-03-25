package de.amr.games.pacman.ui.swing.pacman;

import static de.amr.games.pacman.model.common.GameVariant.PACMAN;
import static de.amr.games.pacman.model.world.PacManGameWorld.t;

import java.awt.Dimension;
import java.awt.Graphics2D;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.ui.pacman.PacMan_IntermissionScene2_Controller;
import de.amr.games.pacman.ui.swing.PacManGameUI_Swing;
import de.amr.games.pacman.ui.swing.common.GameScene;

/**
 * Second intermission scene: Blinky pursues Pac but kicks a nail that tears his dress apart.
 * 
 * @author Armin Reichert
 */
public class PacMan_IntermissionScene2 extends GameScene {

	private PacMan_IntermissionScene2_Controller sceneController;

	public PacMan_IntermissionScene2(PacManGameController controller, Dimension size) {
		super(controller, size, PacManGameUI_Swing.RENDERING.get(PACMAN), PacManGameUI_Swing.SOUND.get(PACMAN));
	}

	@Override
	public void start() {
		sceneController = new PacMan_IntermissionScene2_Controller(gameController, rendering, sounds);
		sceneController.start();
	}

	@Override
	public void update() {
		sceneController.update();
	}

	@Override
	public void render(Graphics2D g) {
		rendering.drawLevelCounter(g, gameController.game(), t(25), t(34));
		rendering.drawNail(g, sceneController.nail);
		rendering.drawPlayer(g, sceneController.pac);
		if (sceneController.nailDistance() < 0) {
			rendering.drawGhost(g, sceneController.blinky, false);
		} else {
			rendering.drawBlinkyStretched(g, sceneController.blinky, sceneController.nail.position,
					sceneController.nailDistance() / 4);
		}
	}
}