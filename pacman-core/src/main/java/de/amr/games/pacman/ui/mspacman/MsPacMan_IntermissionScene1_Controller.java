package de.amr.games.pacman.ui.mspacman;

import static de.amr.games.pacman.lib.God.clock;
import static de.amr.games.pacman.model.world.PacManGameWorld.t;

import java.util.stream.Stream;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.TickTimer;
import de.amr.games.pacman.model.common.Flap;
import de.amr.games.pacman.model.common.GameEntity;
import de.amr.games.pacman.model.common.Ghost;
import de.amr.games.pacman.model.common.Pac;
import de.amr.games.pacman.ui.animation.Animation;
import de.amr.games.pacman.ui.animation.PacManGameAnimations;
import de.amr.games.pacman.ui.sound.PacManGameSound;
import de.amr.games.pacman.ui.sound.SoundManager;

/**
 * Intermission scene 1: "They meet".
 * <p>
 * Pac-Man leads Inky and Ms. Pac-Man leads Pinky. Soon, the two Pac-Men are about to collide, they
 * quickly move upwards, causing Inky and Pinky to collide and vanish. Finally, Pac-Man and Ms.
 * Pac-Man face each other at the top of the screen and a big pink heart appears above them. (Played
 * after round 2)
 * 
 * @author Armin Reichert
 */
public class MsPacMan_IntermissionScene1_Controller {

	public enum Phase {

		FLAP, CHASED_BY_GHOSTS, COMING_TOGETHER, READY_TO_PLAY;

	}

	public static final int upperY = t(12), lowerY = t(24), middleY = t(18);

	public final PacManGameController controller;
	public final PacManGameAnimations animations;
	public final SoundManager sounds;
	public final TickTimer timer = new TickTimer();
	public Phase phase;
	public Flap flap;
	public Pac pacMan, msPac;
	public Ghost pinky, inky;
	public GameEntity heart;
	public boolean ghostsMet;

	public MsPacMan_IntermissionScene1_Controller(PacManGameController controller, PacManGameAnimations animations,
			SoundManager sounds) {
		this.controller = controller;
		this.animations = animations;
		this.sounds = sounds;
	}

	public void start() {
		flap = new Flap(1, "THEY MEET", animations.flapFlapping());
		flap.setTilePosition(3, 10);
		flap.visible = true;

		pacMan = new Pac("Pac-Man", Direction.RIGHT);
		pacMan.setPosition(-t(2), upperY);
		pacMan.visible = true;
		animations.playerAnimations().spouseMunching(pacMan).forEach(Animation::restart);

		inky = new Ghost(2, "Inky", Direction.RIGHT);
		inky.setPositionRelativeTo(pacMan, -t(3), 0);
		inky.visible = true;

		msPac = new Pac("Ms. Pac-Man", Direction.LEFT);
		msPac.setPosition(t(30), lowerY);
		msPac.visible = true;
		animations.playerAnimations().playerMunching(msPac).forEach(Animation::restart);

		pinky = new Ghost(1, "Pinky", Direction.LEFT);
		pinky.setPositionRelativeTo(msPac, t(3), 0);
		pinky.visible = true;

		Stream.of(inky, pinky).forEach(ghost -> {
			animations.ghostAnimations().ghostKicking(ghost).forEach(Animation::restart);
		});

		heart = new GameEntity();
		ghostsMet = false;

		enter(Phase.FLAP, clock.sec(2));
	}

	private void enter(Phase newPhase, long ticks) {
		phase = newPhase;
		timer.reset();
		timer.setDuration(ticks);
		timer.start();
	}

	public void update() {
		switch (phase) {

		case FLAP:
			if (timer.ticksRunning() == clock.sec(1)) {
				flap.flapping.restart();
			}
			if (timer.expired()) {
				flap.visible = false;
				sounds.loop(PacManGameSound.INTERMISSION_1, 1);
				startChasedByGhosts();
				return;
			}
			flap.flapping.animate();
			timer.tick();
			break;

		case CHASED_BY_GHOSTS:
			inky.move();
			pacMan.move();
			pinky.move();
			msPac.move();
			if (inky.position.x > t(30)) {
				startComingTogether();
			}
			timer.tick();
			break;

		case COMING_TOGETHER:
			inky.move();
			pinky.move();
			pacMan.move();
			msPac.move();
			if (pacMan.dir == Direction.LEFT && pacMan.position.x < t(15)) {
				pacMan.dir = msPac.dir = Direction.UP;
			}
			if (pacMan.dir == Direction.UP && pacMan.position.y < upperY) {
				pacMan.speed = msPac.speed = 0;
				pacMan.dir = Direction.LEFT;
				msPac.dir = Direction.RIGHT;
				heart.setPosition((pacMan.position.x + msPac.position.x) / 2, pacMan.position.y - t(2));
				heart.visible = true;
				animations.ghostAnimations().ghostKicking(inky).forEach(Animation::reset);
				animations.ghostAnimations().ghostKicking(pinky).forEach(Animation::reset);
				enter(Phase.READY_TO_PLAY, clock.sec(3));
			}
			if (!ghostsMet && inky.position.x - pinky.position.x < 16) {
				ghostsMet = true;
				inky.dir = inky.wishDir = inky.dir.opposite();
				pinky.dir = pinky.wishDir = pinky.dir.opposite();
				inky.speed = pinky.speed = 0.2f;
			}
			timer.tick();
			break;

		case READY_TO_PLAY:
			if (timer.ticksRunning() == clock.sec(0.5)) {
				inky.visible = false;
				pinky.visible = false;
			}
			if (timer.expired()) {
				controller.getGame().state.timer.forceExpiration();
				return;
			}
			timer.tick();
			break;

		default:
			break;
		}
	}

	public void startChasedByGhosts() {
		pacMan.speed = msPac.speed = 1.2f;
		inky.speed = pinky.speed = 1.25f;
		enter(Phase.CHASED_BY_GHOSTS, Long.MAX_VALUE);
	}

	public void startComingTogether() {
		pacMan.setPosition(t(30), middleY);
		inky.setPosition(t(33), middleY);
		pacMan.dir = Direction.LEFT;
		inky.dir = inky.wishDir = Direction.LEFT;
		pinky.setPosition(t(-5), middleY);
		msPac.setPosition(t(-2), middleY);
		msPac.dir = Direction.RIGHT;
		pinky.dir = pinky.wishDir = Direction.RIGHT;
		enter(Phase.COMING_TOGETHER, Long.MAX_VALUE);
	}
}