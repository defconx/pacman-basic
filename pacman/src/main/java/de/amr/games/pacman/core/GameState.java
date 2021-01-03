package de.amr.games.pacman.core;

/**
 * The different states of the game. Each state has a timer.
 * 
 * @author Armin Reichert
 */
public enum GameState {

	INTRO, READY, HUNTING, CHANGING_LEVEL, PACMAN_DYING, GHOST_DYING, GAME_OVER;

	private long duration;
	private long running;

	public void tick() {
		++running;
	}

	public void setDuration(long ticks) {
		duration = ticks;
		running = 0;
	}

	public void resetTimer() {
		running = 0;
	}

	public long ticksRemaining() {
		return duration == Long.MAX_VALUE ? Long.MAX_VALUE : Math.max(duration - running, 0);
	}

	public long duration() {
		return duration;
	}

	public long running() {
		return running;
	}

	public boolean running(long ticks) {
		return running == ticks;
	}

	public boolean expired() {
		return ticksRemaining() == 0;
	}

	public void runAfter(long ticks, Runnable code) {
		if (running > ticks) {
			code.run();
		}
	}

	public void runAt(long ticks, Runnable code) {
		if (running == ticks) {
			code.run();
		}
	}
}