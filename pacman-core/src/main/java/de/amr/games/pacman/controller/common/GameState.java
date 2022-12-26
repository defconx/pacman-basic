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

import static java.util.function.Predicate.not;

import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.event.GameEvents;
import de.amr.games.pacman.lib.anim.EntityAnimation;
import de.amr.games.pacman.lib.fsm.FsmState;
import de.amr.games.pacman.lib.timer.TickTimer;
import de.amr.games.pacman.model.common.GameLevel;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.GameSound;
import de.amr.games.pacman.model.common.GameVariant;
import de.amr.games.pacman.model.common.ScoreManager;
import de.amr.games.pacman.model.common.actors.AnimKeys;
import de.amr.games.pacman.model.common.actors.Entity;
import de.amr.games.pacman.model.common.actors.Ghost;
import de.amr.games.pacman.model.common.actors.GhostState;

/**
 * Rule of thumb: here, specify "what" and "when", not "how" (this should be implemented in the model).
 * 
 * @author Armin Reichert
 */
public enum GameState implements FsmState<GameModel>, GameCommands {

	BOOT() { // Boot steigt! Jawoll Herr Kaleu!
		@Override
		public void onEnter(GameModel game) {
			timer.restartIndefinitely();
			game.reset();
			ScoreManager.loadScore(game.highScore(), game.variant());
		}

		@Override
		public void onUpdate(GameModel game) {
			if (timer.hasExpired()) {
				gc.changeState(INTRO);
			}
		}
	},

	INTRO() {
		@Override
		public void onEnter(GameModel game) {
			timer.restartIndefinitely();
			game.setPlaying(false);
		}

		@Override
		public void onUpdate(GameModel game) {
			if (timer.hasExpired()) {
				gc.changeState(READY);
			}
		}

		@Override
		public void selectGameVariant(GameVariant variant) {
			gc.createGame(variant);
		}

		@Override
		public void addCredit(GameModel game) {
			boolean added = game.changeCredit(1);
			if (added) {
				gc.sounds().play(GameSound.CREDIT);
			}
			gc.changeState(CREDIT);
		}

		@Override
		public void requestGame(GameModel game) {
			if (game.hasCredit()) {
				gc.changeState(READY);
			}
		}

		@Override
		public void startCutscenesTest(GameModel game) {
			gc.intermissionTestNumber = 1;
			gc.changeState(INTERMISSION_TEST);
		}
	},

	CREDIT {
		@Override
		public void onEnter(GameModel game) {
			game.gameScore().setShowContent(false);
			game.highScore().setShowContent(true);
		}

		@Override
		public void onUpdate(GameModel context) {
			// nothing to do here
		}

		@Override
		public void addCredit(GameModel game) {
			boolean added = game.changeCredit(1);
			if (added) {
				gc.sounds().play(GameSound.CREDIT);
			}
		}

		@Override
		public void requestGame(GameModel game) {
			gc.changeState(READY);
		}
	},

	READY {
		@Override
		public void onEnter(GameModel game) {
			gc.sounds().stopAll();
			if (!game.hasCredit()) {
				gc.pacSteeringInAttractMode.init();
				game.enterAttractMode();
			} else if (game.isPlaying()) {
				game.level().get().letsGetReadyToRumble();
				game.guys().forEach(Entity::show);
			} else {
				game.reset();
				game.buildAndEnterLevel(1);
				gc.sounds().play(GameSound.GAME_READY);
			}
		}

		@Override
		public void onUpdate(GameModel game) {
			final int showGuysTick = 134;
			if (game.hasCredit() && !game.isPlaying()) {
				// starting new game
				if (timer.tick() == showGuysTick) {
					game.guys().forEach(Entity::show);
					game.setOneLessLifeDisplayed(true);
				} else if (timer.tick() == showGuysTick + 118) {
					// start playing
					game.setPlaying(true);
					game.level().ifPresent(level -> level.startHuntingPhase(0));
					gc.changeState(GameState.HUNTING);
				}
			} else {
				// in attract mode or game already running
				if (timer.tick() == 90) {
					game.guys().forEach(Entity::show);
					game.level().ifPresent(level -> level.startHuntingPhase(0));
					gc.changeState(GameState.HUNTING);
				}
			}
		}
	},

	HUNTING {
		@Override
		public void onEnter(GameModel game) {
			game.level().ifPresent(level -> {
				if (!gc.levelTestMode) {
					int sirenIndex = level.huntingPhase() / 2;
					gc.sounds().ensureSirenStarted(sirenIndex);
				}
				level.energizerPulse().restart();
			});
		}

		@Override
		public void onUpdate(GameModel game) {
			game.level().ifPresent(level -> {

				// Level test mode? Maybe this should become a separate state
				if (gc.levelTestMode) {
					if (level.number() <= gc.levelTestLastLevelNumber) {
						// show bonus, update it for one second, then eat it and show won points
						if (gc.state().timer().atSecond(0.0)) {
							game.onBonusReached(level.bonus());
						} else if (gc.state().timer().atSecond(1.0)) {
							level.bonus().eat();
						} else if (gc.state().timer().atSecond(2.0)) {
							gc.changeState(LEVEL_COMPLETE);
						}
						level.bonus().update(level);
					} else {
						// end level test
						gc.levelTestMode = false;
						gc.boot();
					}
					return;
				}

				var memo = level.memo;
				renderSound(level);
				memo.forgetEverything(); // ich scholze jetzt
				level.checkIfPacFindsFood();
				if (memo.lastFoodFound) {
					gc.changeState(LEVEL_COMPLETE);
					return;
				}
				level.checkHowTheGuysAreDoing();
				if (memo.pacMetKiller) {
					gc.changeState(PACMAN_DYING);
					return;
				}
				if (memo.ghostsKilled) {
					gc.changeState(GHOST_DYING);
					return;
				}
				gc.steering().steer(level, game.pac());
				level.update();
			});
		}

		private void renderSound(GameLevel level) {
			var memo = level.memo;
			var snd = gc.sounds();
			if (level.huntingTimer().tick() == 1) {
				var sirenIndex = level.huntingPhase() / 2;
				snd.ensureSirenStarted(sirenIndex);
			}
			if (memo.pacPowered) {
				snd.stopSirens();
				snd.ensureLoop(GameSound.PACMAN_POWER, GameSoundController.LOOP_FOREVER);
			}
			if (memo.pacPowerLost) {
				snd.stop(GameSound.PACMAN_POWER);
				snd.ensureSirenStarted(level.huntingPhase() / 2);
			}
			if (memo.foodFoundTile.isPresent()) {
				snd.ensureLoop(GameSound.PACMAN_MUNCH, GameSoundController.LOOP_FOREVER);
			}
			if (level.game().pac().starvingTicks() >= 12) { // ???
				snd.stop(GameSound.PACMAN_MUNCH);
			}
			if (level.game().ghosts(GhostState.RETURNING_TO_HOUSE).count() > 0) {
				if (!snd.isPlaying(GameSound.GHOST_RETURNING)) {
					snd.loop(GameSound.GHOST_RETURNING, GameSoundController.LOOP_FOREVER);
				}
			} else {
				snd.stop(GameSound.GHOST_RETURNING);
			}
		}

		@Override
		public void addCredit(GameModel game) {
			if (!game.isPlaying()) {
				boolean added = game.changeCredit(1);
				if (added) {
					gc.sounds().play(GameSound.CREDIT);
				}
				gc.changeState(CREDIT);
			}
		}

		@Override
		public void cheatEatAllPellets(GameModel game) {
			if (game.isPlaying()) {
				var level = game.level().get();
				var world = level.world();
				world.tiles().filter(not(world::isEnergizerTile)).forEach(world::removeFood);
				GameEvents.publish(GameEventType.PAC_FINDS_FOOD, null);
			}
		}

		@Override
		public void cheatKillAllEatableGhosts(GameModel game) {
			if (game.isPlaying()) {
				var level = game.level().get();
				level.killAllPossibleGhosts();
				gc.changeState(GameState.GHOST_DYING);
			}
		}

		@Override
		public void cheatEnterNextLevel(GameModel game) {
			if (game.isPlaying()) {
				var level = game.level().get();
				var world = level.world();
				world.tiles().forEach(world::removeFood);
				gc.changeState(GameState.LEVEL_COMPLETE);
			}
		}
	},

	LEVEL_COMPLETE {
		@Override
		public void onEnter(GameModel game) {
			timer.restartSeconds(4);
			gc.sounds().stopAll();
			game.exitLevel();
			GameEvents.publish(GameEventType.UI_FORCE_UPDATE, null);
		}

		@Override
		public void onUpdate(GameModel game) {
			var level = game.level().get();
			if (timer.hasExpired()) {
				if (!game.hasCredit()) {
					gc.changeState(INTRO);
					// attract mode -> back to intro scene
				} else if (game.intermissionNumber(level.number()) != 0) {
					gc.changeState(INTERMISSION); // play intermission scene
				} else {
					gc.changeState(LEVEL_STARTING); // next level
				}
			} else {
				level.world().levelCompleteAnimation().ifPresent(animation -> {
					if (timer.atSecond(1)) {
						animation.setRepetitions(level.params().numFlashes());
						animation.restart();
					} else {
						animation.animate();
					}
				});
				game.pac().update(level);
			}
		}
	},

	LEVEL_STARTING {
		@Override
		public void onEnter(GameModel game) {
			timer.restartSeconds(1);
			game.level().ifPresent(level -> game.buildAndEnterLevel(level.number() + 1));
		}

		@Override
		public void onUpdate(GameModel game) {
			if (timer.hasExpired()) {
				gc.changeState(READY);
			}
		}
	},

	GHOST_DYING {
		@Override
		public void onEnter(GameModel game) {
			timer.restartSeconds(1);
			game.pac().hide();
			game.ghosts().forEach(ghost -> ghost.pauseFlashing(true));
			gc.sounds().play(GameSound.GHOST_EATEN);
		}

		@Override
		public void onUpdate(GameModel game) {
			if (timer.hasExpired()) {
				gc.resumePreviousState();
			} else {
				game.level().ifPresent(level -> {
					gc.steering().steer(level, game.pac());
					game.ghosts(GhostState.EATEN, GhostState.RETURNING_TO_HOUSE, GhostState.ENTERING_HOUSE)
							.forEach(ghost -> ghost.update(level));
					level.energizerPulse().animate();
				});
			}
		}

		@Override
		public void onExit(GameModel game) {
			game.level().ifPresent(level -> {
				game.pac().show();
				game.ghosts(GhostState.EATEN).forEach(ghost -> ghost.enterStateReturningToHouse(level));
				game.ghosts().forEach(ghost -> ghost.pauseFlashing(false));
			});
		}
	},

	PACMAN_DYING {
		@Override
		public void onEnter(GameModel game) {
			timer.restartSeconds(4);
			gc.sounds().stopAll();
			game.level().ifPresent(level -> {
				level.bonus().setInactive();
			});
		}

		@Override
		public void onUpdate(GameModel game) {
			game.level().ifPresent(level -> {
				level.energizerPulse().animate();
				game.pac().update(level);
				if (timer.betweenSeconds(0, 1)) {
					game.ghosts().forEach(Ghost::animate);
				} else if (timer.atSecond(1)) {
					game.pac().selectAndResetAnimation(AnimKeys.PAC_DYING);
					game.ghosts().forEach(Ghost::hide);
				} else if (timer.atSecond(1.4)) {
					game.pac().animation().ifPresent(EntityAnimation::start);
					gc.sounds().play(GameSound.PACMAN_DEATH);
				} else if (timer.atSecond(3.0)) {
					game.pac().setLives(game.pac().lives() - 1);
					if (game.pac().lives() == 0) {
						level.energizerPulse().stop();
						game.setOneLessLifeDisplayed(false);
					}
					game.pac().hide();
				} else if (timer.hasExpired()) {
					if (!game.hasCredit()) {
						gc.changeState(INTRO);
					} else {
						gc.changeState(game.pac().lives() == 0 ? GAME_OVER : READY);
					}
				}
			});
		}
	},

	GAME_OVER {
		@Override
		public void onEnter(GameModel game) {
			timer.restartSeconds(1.5);
			gc.sounds().stopAll();
			game.changeCredit(-1);
			ScoreManager.saveHiscore(game.highScore(), game.variant());
		}

		@Override
		public void onUpdate(GameModel game) {
			if (timer.hasExpired()) {
				game.setPlaying(false);
				gc.changeState(game.hasCredit() ? CREDIT : INTRO);
			}
		}
	},

	INTERMISSION {
		@Override
		public void onEnter(GameModel game) {
			timer.restartIndefinitely();
		}

		@Override
		public void onUpdate(GameModel game) {
			if (timer.hasExpired()) {
				gc.changeState(!game.hasCredit() || !game.isPlaying() ? INTRO : LEVEL_STARTING);
			}
		}
	},

	INTERMISSION_TEST {
		@Override
		public void onEnter(GameModel game) {
			timer.restartIndefinitely();
		}

		@Override
		public void onUpdate(GameModel game) {
			if (timer.hasExpired()) {
				if (gc.intermissionTestNumber < 3) {
					++gc.intermissionTestNumber;
					timer.restartIndefinitely();
					GameEvents.publish(GameEventType.UI_FORCE_UPDATE, null);
				} else {
					gc.intermissionTestNumber = 1;
					gc.changeState(INTRO);
				}
			}
		}
	};

	/* package */ GameController gc;
	/* package */ final TickTimer timer = new TickTimer("Timer-" + name());

	@Override
	public TickTimer timer() {
		return timer;
	}
}