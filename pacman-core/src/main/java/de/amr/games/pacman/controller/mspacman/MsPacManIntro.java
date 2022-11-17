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

import static de.amr.games.pacman.lib.Direction.LEFT;
import static de.amr.games.pacman.lib.Direction.UP;
import static de.amr.games.pacman.lib.V2i.v2i;
import static de.amr.games.pacman.model.common.world.World.HTS;
import static de.amr.games.pacman.model.common.world.World.t;

import de.amr.games.pacman.controller.common.GameController;
import de.amr.games.pacman.controller.common.GameState;
import de.amr.games.pacman.controller.common.SceneControllerContext;
import de.amr.games.pacman.lib.TickTimer;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.lib.animation.EntityAnimation;
import de.amr.games.pacman.lib.animation.EntityAnimationSet;
import de.amr.games.pacman.lib.animation.SingleEntityAnimation;
import de.amr.games.pacman.lib.fsm.Fsm;
import de.amr.games.pacman.lib.fsm.FsmState;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.actors.AnimKeys;
import de.amr.games.pacman.model.common.actors.Ghost;

/**
 * Intro scene of the Ms. Pac-Man game.
 * <p>
 * The ghosts and Ms. Pac-Man are introduced on a billboard and are marching in one after another.
 * 
 * @author Armin Reichert
 */
public class MsPacManIntro extends Fsm<MsPacManIntro.IntroState, MsPacManIntro.IntroData> {

	public static class IntroData extends SceneControllerContext {
		public final V2i redGhostEndPosition = v2i(t(8), t(11));
		public final V2i turningPoint = v2i(t(6), t(20)).plus(0, HTS);
		public final int msPacManStopX = t(15);
		public final V2i titlePosition = v2i(t(10), t(8));
		public final EntityAnimation blinking = SingleEntityAnimation.pulse(30);
		public final TickTimer lightsTimer = new TickTimer("lights-timer", GameModel.FPS);
		public final double actorSpeed = 1.1f;
		public int ghostIndex = 0;
		public boolean creditVisible = false;

		public IntroData(GameController gameController) {
			super(gameController);
		}
	}

	public enum IntroState implements FsmState<MsPacManIntro.IntroData> {

		START {
			@Override
			public void onEnter(IntroData ctx) {
				ctx.game().gameScore.showContent = false;
				ctx.game().highScore.showContent = true;
				ctx.lightsTimer.resetIndefinitely();
				ctx.lightsTimer.start();
				ctx.game().pac.setMoveDir(LEFT);
				ctx.game().pac.setPosition(t(34), ctx.turningPoint.y());
				ctx.game().pac.setAbsSpeed(ctx.actorSpeed);
				ctx.game().pac.selectAndRunAnimation(AnimKeys.PAC_MUNCHING);
				ctx.game().pac.show();
				for (Ghost ghost : ctx.game().theGhosts) {
					ghost.enterStateHuntingPac(ctx.game());
					ghost.setMoveDir(LEFT);
					ghost.setWishDir(LEFT);
					ghost.setPosition(t(34), ctx.turningPoint.y());
					ghost.setAbsSpeed(ctx.actorSpeed);
					ghost.show();
				}
				ctx.ghostIndex = 0;
			}

			@Override
			public void onUpdate(IntroData ctx) {
				if (timer.tick() == 1) {
					ctx.game().gameScore.visible = true;
					ctx.game().highScore.visible = true;
				} else if (timer.tick() == 2) {
					ctx.creditVisible = true;
				} else if (timer.atSecond(1)) {
					controller.changeState(IntroState.GHOSTS);
				}
				ctx.lightsTimer.advance();
			}
		},

		GHOSTS {
			@Override
			public void onEnter(IntroData ctx) {
				ctx.game().ghosts().forEach(ghost -> ghost.animationSet().ifPresent(EntityAnimationSet::ensureRunning));
			}

			@Override
			public void onUpdate(IntroData ctx) {
				ctx.lightsTimer.advance();
				Ghost ghost = ctx.game().theGhosts[ctx.ghostIndex];
				ghost.move();
				ghost.updateAnimation();
				if (ghost.moveDir() != UP && ghost.position().x() <= ctx.turningPoint.x()) {
					ghost.setMoveDir(UP);
					ghost.setWishDir(UP);
				}
				if (ghost.position().y() <= ctx.redGhostEndPosition.y() + ghost.id * 18) {
					ghost.setAbsSpeed(0);
					ghost.animationSet().ifPresent(EntityAnimationSet::stop);
					if (++ctx.ghostIndex == ctx.game().theGhosts.length) {
						controller.changeState(IntroState.MSPACMAN);
					}
				}
			}
		},

		MSPACMAN {
			@Override
			public void onEnter(IntroData ctx) {
				ctx.game().pac.animationSet().ifPresent(EntityAnimationSet::ensureRunning);
			}

			@Override
			public void onUpdate(IntroData ctx) {
				ctx.lightsTimer.advance();
				ctx.game().pac.move();
				ctx.game().pac.updateAnimation();
				if (ctx.game().pac.position().x() <= ctx.msPacManStopX) {
					ctx.game().pac.setAbsSpeed(0);
					ctx.game().pac.animationSet().ifPresent(animSet -> animSet.animation(AnimKeys.PAC_MUNCHING).get().reset());
					controller.changeState(IntroState.READY_TO_PLAY);
				}
			}
		},

		READY_TO_PLAY {
			@Override
			public void onUpdate(IntroData ctx) {
				if (timer.atSecond(1.5) && !ctx.game().hasCredit()) {
					ctx.gameController().changeState(GameState.READY);
					return;
				}
				if (timer.atSecond(5)) {
					ctx.gameController().restartIntro();
					return;
				}
				ctx.lightsTimer.advance();
				ctx.blinking.advance();
			}
		};

		MsPacManIntro controller;
		final TickTimer timer = new TickTimer("Timer-" + name(), GameModel.FPS);

		@Override
		public TickTimer timer() {
			return timer;
		}
	}

	private final IntroData introData;

	public MsPacManIntro(GameController gameController) {
		states = IntroState.values();
		for (var state : states) {
			state.controller = this;
		}
		introData = new IntroData(gameController);
	}

	@Override
	public IntroData context() {
		return introData;
	}
}