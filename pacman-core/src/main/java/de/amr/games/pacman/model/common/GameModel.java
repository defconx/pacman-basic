/*
MIT License

Copyright (c) 2021-2023 Armin Reichert

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
package de.amr.games.pacman.model.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.amr.games.pacman.event.GameEvents;
import de.amr.games.pacman.lib.math.Vector2i;
import de.amr.games.pacman.lib.steering.Direction;
import de.amr.games.pacman.model.common.actors.Bonus;
import de.amr.games.pacman.model.common.actors.Ghost;
import de.amr.games.pacman.model.common.actors.Pac;
import de.amr.games.pacman.model.common.world.World;

/**
 * Common part of the Pac-Man and Ms. Pac-Man game models.
 * 
 * @author Armin Reichert
 */
public abstract class GameModel {

	protected static final Logger LOG = LogManager.getFormatterLogger();
	protected static final Random RND = new Random();

	/** Game loop speed in ticks/sec. */
	public static final short FPS = 60;
	/** Move distance (pixels/tick) at 100% relative speed. */
	public static final float SPEED_100_PERCENT_PX = 1.25f;
	public static final float SPEED_GHOST_INSIDE_HOUSE_PX = 0.5f; // unsure
	public static final float SPEED_GHOST_RETURNING_TO_HOUSE_PX = 2.0f; // unsure
	public static final float SPEED_GHOST_ENTERING_HOUSE_PX = 1.25f; // unsure
	public static final short MAX_CREDIT = 99;
	public static final short LEVEL_COUNTER_MAX_SYMBOLS = 7;
	public static final short INITIAL_LIVES = 3;
	public static final short RESTING_TICKS_NORMAL_PELLET = 1;
	public static final short RESTING_TICKS_ENERGIZER = 3;
	public static final short POINTS_NORMAL_PELLET = 10;
	public static final short POINTS_ENERGIZER = 50;
	public static final short POINTS_ALL_GHOSTS_KILLED = 12_000;
	public static final short[] POINTS_GHOSTS_SEQUENCE = { 200, 400, 800, 1600 };
	public static final short SCORE_EXTRA_LIFE = 10_000;
	public static final short TICKS_BONUS_POINTS_SHOWN = 2 * FPS; // unsure
	public static final short TICKS_PAC_POWER_FADES = 2 * FPS; // unsure

	// Animation keys
	public static final String AK_GHOST_BLUE = "ghost_blue";
	public static final String AK_GHOST_COLOR = "ghost_color";
	public static final String AK_GHOST_EYES = "ghost_eyes";
	public static final String AK_GHOST_FLASHING = "ghost_flashing";
	public static final String AK_GHOST_VALUE = "ghost_value";
	public static final String AK_MAZE_ENERGIZER_BLINKING = "maze_energizer_blinking";
	public static final String AK_MAZE_FLASHING = "maze_flashing";
	public static final String AK_PAC_DYING = "pac_dying";
	public static final String AK_PAC_MUNCHING = "pac_munching";

	// Sound events
	public static final String SE_BONUS_EATEN = "bonus_eaten";
	public static final String SE_CREDIT_ADDED = "credit_added";
	public static final String SE_EXTRA_LIFE = "extra_life";
	public static final String SE_GHOST_EATEN = "ghost_eaten";
	public static final String SE_HUNTING_PHASE_STARTED_0 = "hunting_phase_started_0";
	public static final String SE_HUNTING_PHASE_STARTED_2 = "hunting_phase_started_2";
	public static final String SE_HUNTING_PHASE_STARTED_4 = "hunting_phase_started_4";
	public static final String SE_HUNTING_PHASE_STARTED_6 = "hunting_phase_started_5";
	public static final String SE_PACMAN_DEATH = "pacman_death";
	public static final String SE_PACMAN_FOUND_FOOD = "pacman_found_food";
	public static final String SE_PACMAN_POWER_ENDS = "pacman_power_ends";
	public static final String SE_PACMAN_POWER_STARTS = "pacman_power_starts";
	public static final String SE_READY_TO_PLAY = "ready_to_play";
	public static final String SE_START_INTERMISSION_1 = "start_intermission_1";
	public static final String SE_START_INTERMISSION_2 = "start_intermission_2";
	public static final String SE_START_INTERMISSION_3 = "start_intermission_3";
	public static final String SE_STOP_ALL_SOUNDS = "stop_all_sounds";

	//@formatter:off
	protected static final byte[][] LEVEL_PARAMETERS = {
	/* 1*/ { 80, 75, 40,  20,  80, 10,  85,  90, 50, 6, 5, 0},
	/* 2*/ { 90, 85, 45,  30,  90, 15,  95,  95, 55, 5, 5, 1},
	/* 3*/ { 90, 85, 45,  40,  90, 20,  95,  95, 55, 4, 5, 0},
	/* 4*/ { 90, 85, 45,  40,  90, 20,  95,  95, 55, 3, 5, 0},
	/* 5*/ {100, 95, 50,  40, 100, 20, 105, 100, 60, 2, 5, 2},
	/* 6*/ {100, 95, 50,  50, 100, 25, 105, 100, 60, 5, 5, 0},
	/* 7*/ {100, 95, 50,  50, 100, 25, 105, 100, 60, 2, 5, 0},
	/* 8*/ {100, 95, 50,  50, 100, 25, 105, 100, 60, 2, 5, 0},
	/* 9*/ {100, 95, 50,  60, 100, 30, 105, 100, 60, 1, 3, 3},
	/*10*/ {100, 95, 50,  60, 100, 30, 105, 100, 60, 5, 5, 0},
	/*11*/ {100, 95, 50,  60, 100, 30, 105, 100, 60, 2, 5, 0},
	/*12*/ {100, 95, 50,  80, 100, 40, 105, 100, 60, 1, 3, 0},
	/*13*/ {100, 95, 50,  80, 100, 40, 105, 100, 60, 1, 3, 3},
	/*14*/ {100, 95, 50,  80, 100, 40, 105, 100, 60, 3, 5, 0},
	/*15*/ {100, 95, 50, 100, 100, 50, 105, 100, 60, 1, 3, 0},
	/*16*/ {100, 95, 50, 100, 100, 50, 105, 100, 60, 1, 3, 0},
	/*17*/ {100, 95, 50, 100, 100, 50, 105,   0,  0, 0, 0, 3},
	/*18*/ {100, 95, 50, 100, 100, 50, 105, 100, 60, 1, 3, 0},
	/*19*/ {100, 95, 50, 120, 100, 60, 105,   0,  0, 0, 0, 0},
	/*20*/ {100, 95, 50, 120, 100, 60, 105,   0,  0, 0, 0, 0},
	/*21*/ { 90, 95, 50, 120, 100, 60, 105,   0,  0, 0, 0, 0},
	};

	// Hunting duration (in ticks) of chase and scatter phases. See Pac-Man dossier.
	private static final int[][] HUNTING_DURATIONS = {
		{ 7 * FPS, 20 * FPS, 7 * FPS, 20 * FPS, 5 * FPS,   20 * FPS, 5 * FPS, -1 },
		{ 7 * FPS, 20 * FPS, 7 * FPS, 20 * FPS, 5 * FPS, 1033 * FPS,       1, -1 },
		{ 5 * FPS, 20 * FPS, 5 * FPS, 20 * FPS, 5 * FPS, 1037 * FPS,       1, -1 },
	};
	//@formatter:on

	public int[] huntingDurations(int levelNumber) {
		int index = switch (levelNumber) {
		case 1 -> 0;
		case 2, 3, 4 -> 1;
		default -> 2;
		};
		return HUNTING_DURATIONS[index];
	}

	// Parameter validation

	private static final String MSG_GAME_NULL = "Game model must not be null";
	private static final String MSG_LEVEL_NULL = "Game level must not be null";
	private static final String MSG_LEVEL_NUMBER_ILLEGAL = "Level number must be at least 1, but is: %d";
	private static final String MSG_TILE_NULL = "Tile must not be null";
	private static final String MSG_DIR_NULL = "Direction must not be null";
	private static final String MSG_GHOST_ID_ILLEGAL = "Illegal ghost ID: %d";

	public static void checkGameNotNull(GameModel game) {
		Objects.requireNonNull(game, MSG_GAME_NULL);
	}

	public static void checkGhostID(byte id) {
		if (id < 0 || id > 3) {
			throw new IllegalArgumentException(MSG_GHOST_ID_ILLEGAL.formatted(id));
		}
	}

	public static void checkLevelNumber(int levelNumber) {
		if (levelNumber < 1) {
			throw new IllegalArgumentException(MSG_LEVEL_NUMBER_ILLEGAL.formatted(levelNumber));
		}
	}

	public static void checkTileNotNull(Vector2i tile) {
		Objects.requireNonNull(tile, MSG_TILE_NULL);
	}

	public static void checkLevelNotNull(GameLevel level) {
		Objects.requireNonNull(level, MSG_LEVEL_NULL);
	}

	public static void checkDirectionNotNull(Direction dir) {
		Objects.requireNonNull(dir, MSG_DIR_NULL);
	}

	protected GameLevel level;
	protected final List<Byte> levelCounter = new LinkedList<>();
	protected Score score;
	protected Score highScore;
	protected int credit;
	protected int lives;
	protected boolean playing;
	protected boolean scoringEnabled;
	protected boolean immune; // extra feature
	protected boolean oneLessLifeDisplayed; // TODO get rid of this
	public int intermissionTestNumber; // intermission test mode

	protected GameModel() {
		init();
	}

	/**
	 * Initializes the game. Credit, immunity and scores remain unchanged.
	 */
	public void init() {
		level = null;
		lives = INITIAL_LIVES;
		playing = false;
		scoringEnabled = true;
		oneLessLifeDisplayed = false; // @remove
		LOG.trace("Game model (%s) initialized", variant());
	}

	/**
	 * @return the game variant realized by this model
	 */
	public abstract GameVariant variant();

	/**
	 * @return new Pac-Man or Ms. Pac-Man
	 */
	public abstract Pac createPac();

	/**
	 * @return set of new ghosts
	 */
	public abstract Ghost[] createGhosts();

	/**
	 * @param levelNumber level number (starting at 1)
	 * @return world used in specified level
	 */
	public abstract World createWorld(int levelNumber);

	/**
	 * @param levelNumber level number (starting at 1)
	 * @return number of maze used in specified level
	 */
	public abstract int mazeNumber(int levelNumber);

	/**
	 * @param levelNumber level number (starting at 1)
	 * @return bonus used in specified level
	 */
	public abstract Bonus createBonus(int levelNumber);

	/**
	 * @param levelNumber level number (starting at 1)
	 * @return parameter values (speed, pellet counts etc.) used in specified level. From level 21 on, level parameters
	 *         remain the same
	 */
	public byte[] levelData(int levelNumber) {
		return levelNumber <= LEVEL_PARAMETERS.length //
				? LEVEL_PARAMETERS[levelNumber - 1]
				: LEVEL_PARAMETERS[LEVEL_PARAMETERS.length - 1];
	}

	/**
	 * Called when the bonus gets activated.
	 */
	public abstract void onBonusReached();

	public abstract boolean isFirstBonusReached();

	public abstract boolean isSecondBonusReached();

	public abstract int numCutScenes();

	/** @return (optional) current game level. */
	public Optional<GameLevel> level() {
		return Optional.ofNullable(level);
	}

	/**
	 * Creates and enters the given level.
	 * 
	 * @param levelNumber level number (starting at 1)
	 */
	public void enterLevel(int levelNumber) {
		level = new GameLevel(this, levelNumber);
		level.letsGetReadyToRumbleAndShowGuys(false);
		incrementLevelCounter();
		if (score != null) {
			score.setLevelNumber(levelNumber);
		}
	}

	/**
	 * Enters the demo game level ("attract mode").
	 */
	public abstract void enterDemoLevel();

	/**
	 * Enters the next game level.
	 */
	public void nextLevel() {
		if (level == null) {
			throw new IllegalStateException("Cannot enter next level, no current level exists");
		}
		enterLevel(level.number() + 1);
	}

	public void removeLevel() {
		level = null;
	}

	public void doGhostHuntingAction(GameLevel level, Ghost ghost) {
		if (level.chasingPhase().isPresent() || ghost.id() == Ghost.ID_RED_GHOST && level.cruiseElroyState() > 0) {
			ghost.chase(level);
		} else {
			ghost.scatter(level);
		}
	}

	/** @return tells if the game play is running. */
	public boolean isPlaying() {
		return playing;
	}

	public void setPlaying(boolean playing) {
		this.playing = playing;
	}

	/**
	 * @return tells if Pac-Man can get killed by ghosts
	 */
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
		if (lives < 0) {
			throw new IllegalArgumentException("Lives must not be negative but is: " + lives);
		}
		this.lives = lives;
	}

	/** @return collected level symbols. */
	public List<Byte> levelCounter() {
		return Collections.unmodifiableList(levelCounter);
	}

	public void clearLevelCounter() {
		levelCounter.clear();
	}

	public void incrementLevelCounter() {
		if (level.number() == 1) {
			levelCounter.clear();
		}
		levelCounter.add(level.bonus().symbol());
		if (levelCounter.size() > LEVEL_COUNTER_MAX_SYMBOLS) {
			levelCounter.remove(0);
		}
	}

	public Optional<Score> score() {
		return Optional.ofNullable(score);
	}

	public void newScore() {
		score = new Score();
	}

	public Optional<Score> highScore() {
		return Optional.ofNullable(highScore);
	}

	public void scorePoints(int points) {
		if (points < 0) {
			throw new IllegalArgumentException("Scored points value must not be negative but is: " + points);
		}
		if (level == null) {
			throw new IllegalStateException("Cannot score points: No game level selected");
		}
		if (!scoringEnabled) {
			return;
		}
		final int oldScore = score.points();
		final int newScore = oldScore + points;
		score.setPoints(newScore);
		if (newScore > highScore.points()) {
			highScore.setPoints(newScore);
			highScore.setLevelNumber(level.number());
			highScore.setDate(LocalDate.now());
		}
		if (oldScore < SCORE_EXTRA_LIFE && newScore >= SCORE_EXTRA_LIFE) {
			lives += 1;
			GameEvents.publishSoundEvent(SE_EXTRA_LIFE);
		}
	}

	private static File highscoreFile(GameVariant variant) {
		Objects.requireNonNull(variant, "Game variant is null");
		var dir = System.getProperty("user.home");
		return switch (variant) {
		case PACMAN -> new File(dir, "highscore-pacman.xml");
		case MS_PACMAN -> new File(dir, "highscore-ms_pacman.xml");
		default -> throw new IllegalArgumentException("Unknown game variant: '%s'".formatted(variant));
		};
	}

	private static Score loadHighscore(File file) {
		try (var in = new FileInputStream(file)) {
			var props = new Properties();
			props.loadFromXML(in);
			var points = Integer.parseInt(props.getProperty("points"));
			var levelNumber = Integer.parseInt(props.getProperty("level"));
			var date = LocalDate.parse(props.getProperty("date"), DateTimeFormatter.ISO_LOCAL_DATE);
			Score scoreFromFile = new Score();
			scoreFromFile.setPoints(points);
			scoreFromFile.setLevelNumber(levelNumber);
			scoreFromFile.setDate(date);
			LOG.info("Highscore loaded. File: '%s' Points: %d Level: %d", file.getAbsolutePath(), scoreFromFile.points(),
					scoreFromFile.levelNumber());
			return scoreFromFile;
		} catch (Exception x) {
			LOG.info("Highscore could not be loaded. File '%s' Reason: %s", file, x.getMessage());
			return new Score();
		}
	}

	public void loadHighscore() {
		highScore = loadHighscore(highscoreFile(variant()));
	}

	public void saveNewHighscore() {
		var oldHiscore = loadHighscore(highscoreFile(variant()));
		if (highScore.points() <= oldHiscore.points()) {
			return;
		}
		var props = new Properties();
		props.setProperty("points", String.valueOf(highScore.points()));
		props.setProperty("level", String.valueOf(highScore.levelNumber()));
		props.setProperty("date", highScore.date().format(DateTimeFormatter.ISO_LOCAL_DATE));
		var highScoreFile = highscoreFile(variant());
		try (var out = new FileOutputStream(highScoreFile)) {
			props.storeToXML(out, "%s Hiscore".formatted(variant()));
			LOG.info("Highscore saved. File: '%s' Points: %d Level: %d", highScoreFile.getAbsolutePath(), highScore.points(),
					highScore.levelNumber());
		} catch (Exception x) {
			LOG.info("Highscore could not be saved. File '%s' Reason: %s", highScoreFile, x.getMessage());
		}
	}

	/** @return number of coins inserted. */
	public int credit() {
		return credit;
	}

	public boolean setCredit(int credit) {
		if (0 <= credit && credit <= MAX_CREDIT) {
			this.credit = credit;
			return true;
		}
		return false;
	}

	public boolean changeCredit(int delta) {
		return setCredit(credit + delta);
	}

	public boolean hasCredit() {
		return credit > 0;
	}

	// TODO: get rid of this:

	/** If one less life is displayed in the lives counter. */
	public boolean isOneLessLifeDisplayed() {
		return oneLessLifeDisplayed;
	}

	public void setOneLessLifeDisplayed(boolean value) {
		this.oneLessLifeDisplayed = value;
	}
}