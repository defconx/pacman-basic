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
package de.amr.games.pacman.controller.common;

import static de.amr.games.pacman.lib.Logging.log;
import static de.amr.games.pacman.model.common.actors.GhostState.FRIGHTENED;
import static de.amr.games.pacman.model.common.actors.GhostState.HUNTING_PAC;

import de.amr.games.pacman.event.GameEventing;
import de.amr.games.pacman.event.GameStateChangeEvent;
import de.amr.games.pacman.lib.Fsm;
import de.amr.games.pacman.lib.FsmState;
import de.amr.games.pacman.lib.TickTimer;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.GameModel.CheckResult;
import de.amr.games.pacman.model.common.actors.Ghost;

/**
 * Rule of thumb: here, specify the "what" and "when", not the "how" (which should be implemented in the model).
 * 
 * @author Armin Reichert
 */
public enum GameState implements FsmState<GameModel> {

	INTRO {
		@Override
		public void onEnter(GameModel game) {
			timer.setDurationIndefinite();
			timer.start();
			game.reset();
			controller.setGameRunning(false);
		}

		@Override
		public void onUpdate(GameModel game) {
			if (timer.hasExpired()) {
				controller.changeState(READY);
			}
		}
	},

	CREDIT {
		@Override
		public void onUpdate(GameModel game) {
			if (timer.hasExpired()) {
				controller.changeState(READY);
			}
		}
	},

	READY {
		@Override
		public void onEnter(GameModel game) {
			long readySeconds = controller.isGameRunning() || controller.credit() == 0 ? 2 : 5;
			timer.setDurationSeconds(readySeconds);
			timer.start();
			game.resetGuys();
			game.ghosts().forEach(Ghost::hide);
			game.pac.hide();
		}

		@Override
		public void onUpdate(GameModel game) {
			if (timer.atSecond(1.5)) {
				game.ghosts().forEach(Ghost::show);
				game.pac.show();
			}
			if (timer.hasExpired()) {
				game.startHuntingPhase(0);
				if (controller.credit() > 0) {
					game.scores().enable(true);
					controller.setGameRunning(true);
				} else {
					game.scores().enable(false);
					controller.setGameRunning(false);
				}
				controller.changeState(GameState.HUNTING);
			}
		}
	},

	HUNTING {
		@Override
		public void onUpdate(GameModel game) {
			var checkResult = new CheckResult();
			controller.steer(game.pac);
			game.updatePlayer(checkResult);
			if (checkResult.allFoodEaten) {
				controller.changeState(LEVEL_COMPLETE);
				return;
			}
			if (checkResult.playerKilled) {
				controller.changeState(PACMAN_DYING);
				return;
			}
			if (checkResult.ghostsKilled) {
				controller.changeState(GHOST_DYING);
				return;
			}
			game.updateGhosts(checkResult);
			game.updateBonus();
			game.advanceHunting();
		}
	},

	LEVEL_COMPLETE {
		@Override
		public void onEnter(GameModel game) {
			game.huntingTimer.stop();
			if (game.bonus() != null) {
				game.bonus().init();
			}
			game.pac.setAbsSpeed(0);
			game.ghosts().forEach(Ghost::hide);
			timer.setDurationIndefinite();
			timer.start();
		}

		@Override
		public void onUpdate(GameModel game) {
			if (timer.hasExpired()) {
				if (controller.credit() == 0) {
					controller.changeState(INTRO);
				} else if (game.intermissionNumber(game.level.number) != 0) {
					controller.changeState(INTERMISSION);
				} else {
					controller.changeState(LEVEL_STARTING);
				}
			}
		}
	},

	LEVEL_STARTING {
		@Override
		public void onEnter(GameModel game) {
			timer.setDurationIndefinite();
			timer.start();
			game.setLevel(game.level.number + 1);
			game.resetGuys();
			game.ghosts().forEach(Ghost::hide);
			game.pac.hide();
		}

		@Override
		public void onUpdate(GameModel game) {
			if (timer.hasExpired()) {
				controller.changeState(READY);
			}
		}
	},

	PACMAN_DYING {
		@Override
		public void onEnter(GameModel game) {
			timer.setDurationIndefinite();
			timer.start();
			game.pac.setAbsSpeed(0);
			game.ghosts(FRIGHTENED).forEach(ghost -> ghost.state = HUNTING_PAC);
			if (game.bonus() != null) {
				game.bonus().init();
			}
		}

		@Override
		public void onUpdate(GameModel game) {
			if (timer.hasExpired()) {
				game.lives--;
				if (controller.credit() == 0) {
					game.reset();
					controller.changeState(INTRO);
				} else {
					controller.changeState(game.lives > 0 ? READY : GAME_OVER);
				}
			}
		}
	},

	GHOST_DYING {
		@Override
		public void onEnter(GameModel game) {
			timer.setDurationSeconds(1);
			timer.start();
			game.pac.hide();
		}

		@Override
		public void onUpdate(GameModel game) {
			if (timer.hasExpired()) {
				controller.resumePreviousState();
				return;
			}
			controller.steer(game.pac);
			game.updateGhostsReturningHome();
		}

		@Override
		public void onExit(GameModel game) {
			game.pac.show();
			game.letDeadGhostsReturnHome();
		}
	},

	GAME_OVER {
		@Override
		public void onEnter(GameModel game) {
			timer.setDurationSeconds(5);
			timer.start();
			game.ghosts().forEach(ghost -> ghost.setAbsSpeed(0));
			game.ghosts().forEach(Ghost::show);
			game.pac.setAbsSpeed(0);
			game.pac.show();
			game.scores().saveHiscore();
		}

		@Override
		public void onUpdate(GameModel game) {
			if (timer.hasExpired()) {
				controller.setGameRunning(false);
				controller.consumeCredit();
				controller.changeState(controller.credit() > 0 ? CREDIT : INTRO);
			}
		}
	},

	INTERMISSION {
		@Override
		public void onEnter(GameModel game) {
			timer.setDurationIndefinite();
			timer.start(); // UI triggers state timeout
		}

		@Override
		public void onUpdate(GameModel game) {
			if (timer.hasExpired()) {
				controller.changeState(controller.credit() == 0 || !controller.isGameRunning() ? INTRO : LEVEL_STARTING);
			}
		}
	},

	INTERMISSION_TEST {
		@Override
		public void onEnter(GameModel game) {
			timer.setDurationIndefinite();
			timer.start();
			log("Test intermission scene #%d", game.intermissionTestNumber);
		}

		@Override
		public void onUpdate(GameModel game) {
			if (timer.hasExpired()) {
				if (game.intermissionTestNumber < 3) {
					++game.intermissionTestNumber;
					timer.setDurationIndefinite();
					timer.start();
					log("Test intermission scene #%d", game.intermissionTestNumber);
					// This is a hack to trigger the UI to update its current scene
					GameEventing.publish(new GameStateChangeEvent(game, this, this));
				} else {
					controller.changeState(INTRO);
				}
			}
		}
	};

	// -----------------------------------------------------------

	protected GameController controller;
	protected final TickTimer timer = new TickTimer("Timer-" + name());

	@Override
	public void setFsm(Fsm<? extends FsmState<GameModel>, GameModel> fsm) {
		controller = (GameController) fsm;
	}

	@Override
	public TickTimer timer() {
		return timer;
	}
}