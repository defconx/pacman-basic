/*
MIT License

Copyright (c) 2021-22 Armin Reichert

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

import de.amr.games.pacman.controller.common.GameController;
import de.amr.games.pacman.controller.mspacman.Intermission2Controller.Context;
import de.amr.games.pacman.controller.mspacman.Intermission2Controller.State;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.TickTimer;
import de.amr.games.pacman.lib.fsm.Fsm;
import de.amr.games.pacman.lib.fsm.FsmState;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.GameSound;
import de.amr.games.pacman.model.common.actors.Pac;
import de.amr.games.pacman.model.mspacman.Flap;

/**
 * Intermission scene 2: "The chase".
 * <p>
 * Pac-Man and Ms. Pac-Man chase each other across the screen over and over. After three turns, they both rapidly run
 * from left to right and right to left. (Played after round 5)
 * 
 * @author Armin Reichert
 */
public class Intermission2Controller extends Fsm<State, Context> {

	public final GameController gameController;
	public final Context context;

	public Intermission2Controller(GameController gameController) {
		super(State.values());
		this.gameController = gameController;
		this.context = new Context(gameController.game());
	}

	@Override
	public Context context() {
		return context;
	}

	public static class Context {
		public final GameModel game;
		public final int upperY = t(12), middleY = t(18), lowerY = t(24);
		public Flap flap;
		public Pac pacMan, msPacMan;

		public Context(GameModel game) {
			this.game = game;
		}
	}

	public enum State implements FsmState<Context> {

		FLAP {
			@Override
			public void onEnter(Context $) {
				timer.setIndefinite();
				timer.start();
				$.flap = new Flap();
				$.flap.number = 2;
				$.flap.text = "THE CHASE";
				$.flap.setPosition(t(3), t(10));
				$.flap.show();
				$.pacMan = new Pac("Pac-Man");
				$.pacMan.setMoveDir(Direction.RIGHT);
				$.msPacMan = new Pac("Ms. Pac-Man");
				$.msPacMan.setMoveDir(Direction.RIGHT);
			}

			@Override
			public void onUpdate(Context $) {
				if (timer.atSecond(1)) {
					$.game.sounds().ifPresent(snd -> snd.play(GameSound.INTERMISSION_2));
					if ($.flap.animation != null) {
						$.flap.animation.restart();
					}
				} else if (timer.atSecond(2)) {
					$.flap.hide();
				} else if (timer.atSecond(3)) {
					controller.changeState(State.CHASING);
				}
			}
		},

		CHASING {
			@Override
			public void onEnter(Context $) {
				timer.setIndefinite();
				timer.start();
			}

			@Override
			public void onUpdate(Context $) {
				if (timer.atSecond(2.5)) {
					$.pacMan.setPosition(-t(2), $.upperY);
					$.pacMan.setMoveDir(Direction.RIGHT);
					$.pacMan.setAbsSpeed(2.0);
					$.pacMan.show();
					$.msPacMan.setPosition(-t(8), $.upperY);
					$.msPacMan.setMoveDir(Direction.RIGHT);
					$.msPacMan.setAbsSpeed(2.0);
					$.msPacMan.show();
				} else if (timer.atSecond(7)) {
					$.pacMan.setPosition(t(36), $.lowerY);
					$.pacMan.setMoveDir(Direction.LEFT);
					$.pacMan.setAbsSpeed(2.0);
					$.msPacMan.setPosition(t(30), $.lowerY);
					$.msPacMan.setMoveDir(Direction.LEFT);
					$.msPacMan.setAbsSpeed(2.0);
				} else if (timer.atSecond(11.5)) {
					$.pacMan.setMoveDir(Direction.RIGHT);
					$.pacMan.setAbsSpeed(2.0);
					$.msPacMan.setPosition(t(-8), $.middleY);
					$.msPacMan.setMoveDir(Direction.RIGHT);
					$.msPacMan.setAbsSpeed(2.0);
					$.pacMan.setPosition(t(-2), $.middleY);
				} else if (timer.atSecond(15.5)) {
					$.pacMan.setPosition(t(42), $.upperY);
					$.pacMan.setMoveDir(Direction.LEFT);
					$.pacMan.setAbsSpeed(4.0);
					$.msPacMan.setPosition(t(30), $.upperY);
					$.msPacMan.setMoveDir(Direction.LEFT);
					$.msPacMan.setAbsSpeed(4.0);
				} else if (timer.atSecond(16.5)) {
					$.pacMan.setPosition(t(-2), $.lowerY);
					$.pacMan.setMoveDir(Direction.RIGHT);
					$.pacMan.setAbsSpeed(4.0);
					$.msPacMan.setPosition(t(-14), $.lowerY);
					$.msPacMan.setMoveDir(Direction.RIGHT);
					$.msPacMan.setAbsSpeed(4.0);
				} else if (timer.atSecond(21)) {
					controller.gameController.state().timer().expire();
					return;
				}
				$.pacMan.move();
				$.msPacMan.move();
			}
		};

		protected Intermission2Controller controller;
		protected final TickTimer timer = new TickTimer("Timer-" + name());

		@Override
		public void setOwner(Fsm<? extends FsmState<Context>, Intermission2Controller.Context> fsm) {
			controller = (Intermission2Controller) fsm;
		}

		@Override
		public TickTimer timer() {
			return timer;
		}
	}
}