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
package de.amr.games.pacman.model.common.actors;

import static de.amr.games.pacman.model.common.actors.GhostState.HUNTING_PAC;

import java.util.Objects;
import java.util.Optional;

import de.amr.games.pacman.lib.anim.AnimatedEntity;
import de.amr.games.pacman.lib.anim.EntityAnimationSet;
import de.amr.games.pacman.lib.timer.TickTimer;
import de.amr.games.pacman.model.common.GameModel;

/**
 * (Ms.) Pac-Man.
 * 
 * @author Armin Reichert
 */
public class Pac extends Creature implements AnimatedEntity<AnimKeys> {

	private final TickTimer powerTimer;
	private int powerFadingTicks;
	private boolean dead;
	private int lives;
	private int restingTicks;
	private int starvingTicks;
	private EntityAnimationSet<AnimKeys> animationSet;
	private boolean immune; // extra
	private boolean autoControlled; // extra

	public Pac(String name) {
		super(name);
		powerTimer = new TickTimer("PacPower");
		reset();
	}

	@Override
	public void reset() {
		super.reset();
		dead = false;
		restingTicks = 0;
		starvingTicks = 0;
		selectAndResetAnimation(AnimKeys.PAC_MUNCHING);
		powerTimer.reset(0);
		powerFadingTicks = 2 * GameModel.FPS;
	}

	public void update(GameModel game) {
		Objects.requireNonNull(game, MSG_GAME_NULL);
		if (dead) {
			updateDead();
		} else {
			updateAlive(game);
		}
	}

	private void updateAlive(GameModel game) {
		switch (restingTicks) {
		case 0 -> {
			var speed = powerTimer.isRunning() ? game.level().playerSpeedPowered() : game.level().playerSpeed();
			setRelSpeed(speed);
			tryMoving(game);
			selectRunnableAnimation(AnimKeys.PAC_MUNCHING);
			if (!isStuck()) {
				animate();
			}
		}
		case Integer.MAX_VALUE -> {
			// rest in peace
		}
		default -> --restingTicks;
		}
		powerTimer.advance();
	}

	private void updateDead() {
		setAbsSpeed(0);
		animate();
	}

	public void kill() {
		stopAnimation();
		setAbsSpeed(0);
		dead = true;
	}

	public int powerFadingTicks() {
		return powerFadingTicks;
	}

	public boolean isPowerFading(GameModel game) {
		Objects.requireNonNull(game, MSG_GAME_NULL);
		return powerTimer.isRunning() && powerTimer.remaining() <= powerFadingTicks;
	}

	public boolean isMeetingKiller(GameModel game) {
		Objects.requireNonNull(game, MSG_GAME_NULL);
		return !immune && !powerTimer.isRunning() && game.ghosts(HUNTING_PAC).anyMatch(this::sameTile);
	}

	@Override
	public String toString() {
		return "[Pac: name='%s' lives=%d position=%s offset=%s tile=%s velocity=%s speed=%.2f moveDir=%s wishDir=%s dead=%s restingTicks=%d starvingTicks=%d]"
				.formatted(name(), lives, position, offset(), tile(), velocity, velocity.length(), moveDir(), wishDir(), dead,
						restingTicks, starvingTicks);
	}

	public boolean isImmune() {
		return immune;
	}

	public void setImmune(boolean immune) {
		this.immune = immune;
	}

	public int lives() {
		return lives;
	}

	public void setLives(int lives) {
		this.lives = lives;
	}

	/** Timer controlling how long Pac-Man has power. */
	public TickTimer powerTimer() {
		return powerTimer;
	}

	@Override
	public Optional<EntityAnimationSet<AnimKeys>> animationSet() {
		return Optional.ofNullable(animationSet);
	}

	public void setAnimationSet(EntityAnimationSet<AnimKeys> animationSet) {
		this.animationSet = animationSet;
	}

	public boolean isDead() {
		return dead;
	}

	public boolean isAutoControlled() {
		return autoControlled;
	}

	public void setAutoControlled(boolean autoControlled) {
		this.autoControlled = autoControlled;
	}

	/* Number of ticks Pac is resting and not moving. */
	public int restingTicks() {
		return restingTicks;
	}

	public void rest(int ticks) {
		if (ticks < 0) {
			throw new IllegalArgumentException("Resting time cannot be negative, but is: %d".formatted(ticks));
		}
		restingTicks = ticks;
	}

	/* Number of ticks since Pac has has eaten a pellet or energizer. */
	public int starvingTicks() {
		return starvingTicks;
	}

	public void starve() {
		++starvingTicks;
	}

	public void endStarving() {
		starvingTicks = 0;
	}
}