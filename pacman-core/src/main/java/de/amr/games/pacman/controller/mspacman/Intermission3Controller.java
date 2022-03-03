/*
MIT License

Copyright (c) 2021 Armin Reichert

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package de.amr.games.pacman.controller.mspacman;

import static de.amr.games.pacman.model.common.world.World.t;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.controller.mspacman.Intermission3Controller.IntermissionState;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.FiniteStateMachine;
import de.amr.games.pacman.lib.V2d;
import de.amr.games.pacman.model.common.GameEntity;
import de.amr.games.pacman.model.common.Pac;
import de.amr.games.pacman.model.mspacman.Flap;
import de.amr.games.pacman.model.mspacman.JuniorBag;

/**
 * Intermission scene 3: "Junior".
 * 
 * <p>
 * Pac-Man and Ms. Pac-Man gradually wait for a stork, who flies overhead with a little blue bundle. The stork drops the
 * bundle, which falls to the ground in front of Pac-Man and Ms. Pac-Man, and finally opens up to reveal a tiny Pac-Man.
 * (Played after rounds 9, 13, and 17)
 * 
 * @author Armin Reichert
 */
public class Intermission3Controller extends FiniteStateMachine<IntermissionState> {

	public enum IntermissionState {
		FLAP, ACTION, READY_TO_PLAY;
	}

	static final int GROUND_Y = t(24);

	public final GameController gameController;
	public Runnable playIntermissionSound = NOP;
	public Runnable playFlapAnimation = NOP;
	public Flap flap;
	public Pac pacMan;
	public Pac msPacMan;
	public GameEntity stork;
	public JuniorBag bag;
	public int numBagBounces;

	public Intermission3Controller(GameController gameController) {
		super(IntermissionState.values());
		configState(IntermissionState.FLAP, this::state_FLAP_enter, this::state_FLAP_update, null);
		configState(IntermissionState.ACTION, this::state_ACTION_enter, this::state_ACTION_update, null);
		configState(IntermissionState.READY_TO_PLAY, this::state_READY_TO_PLAY_enter, this::state_READY_TO_PLAY_update,
				null);
		this.gameController = gameController;
	}

	public void init() {
		state = null;
		changeState(IntermissionState.FLAP);
	}

	private void state_FLAP_enter() {
		stateTimer().setSeconds(1).start();

		flap = new Flap();
		flap.number = 3;
		flap.text = "JUNIOR";
		flap.setPosition(t(3), t(10));
		flap.show();

		pacMan = new Pac("Pac-Man");
		pacMan.setMoveDir(Direction.RIGHT);
		pacMan.setPosition(t(3), GROUND_Y - 4);

		msPacMan = new Pac("Ms. Pac-Man");
		msPacMan.setMoveDir(Direction.RIGHT);
		msPacMan.setPosition(t(5), GROUND_Y - 4);

		stork = new GameEntity();
		stork.setPosition(t(30), t(12));

		bag = new JuniorBag();
		bag.acceleration = V2d.NULL;
		bag.open = false;
		bag.position = stork.position.plus(-14, 3);
		numBagBounces = 0;
	}

	private void state_FLAP_update() {
		if (stateTimer().isRunningSeconds(1)) {
			playFlapAnimation.run();
		} else if (stateTimer().isRunningSeconds(2)) {
			changeState(IntermissionState.ACTION);
		}
	}

	private void state_ACTION_enter() {
		stateTimer().setIndefinite().start();
		flap.hide();
		playIntermissionSound.run();
	}

	private void state_ACTION_update() {
		stork.move();
		bag.move();
		if (stateTimer().hasJustStarted()) {
			pacMan.show();
			msPacMan.show();
			stork.show();
			bag.show();
			stork.setVelocity(-1.25, 0);
			bag.setVelocity(-1.25f, 0);
		}
		// release bag from storks beak?
		if ((int) stork.position.x == t(24)) {
			bag.acceleration = new V2d(0, 0.04);
		}
		// (closed) bag reaches ground for first time?
		if (!bag.open && bag.position.y > GROUND_Y) {
			++numBagBounces;
			if (numBagBounces < 5) {
				bag.setVelocity(-0.2f, -1f / numBagBounces);
				bag.setPosition(bag.position.x, GROUND_Y);
			} else {
				bag.open = true;
				bag.velocity = V2d.NULL;
				changeState(IntermissionState.READY_TO_PLAY);
			}
		}
	}

	private void state_READY_TO_PLAY_enter() {
		stateTimer().setSeconds(3).start();
	}

	private void state_READY_TO_PLAY_update() {
		stork.move();
		if (stateTimer().hasExpired()) {
			gameController.stateTimer().expire();
		}
	}
}