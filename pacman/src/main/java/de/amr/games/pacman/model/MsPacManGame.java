package de.amr.games.pacman.model;

import static de.amr.games.pacman.heaven.God.random;
import static de.amr.games.pacman.lib.Direction.DOWN;
import static de.amr.games.pacman.lib.Direction.LEFT;
import static de.amr.games.pacman.lib.Direction.UP;
import static de.amr.games.pacman.lib.Logging.log;

import de.amr.games.pacman.world.MapBasedPacManGameWorld;
import de.amr.games.pacman.world.WorldMap;

/**
 * Game model of the Ms. Pac-Man game variant.
 * 
 * @author Armin Reichert
 */
public class MsPacManGame extends PacManGameModel {

	/*@formatter:off*/
	public static final int[][] MSPACMAN_LEVELS = {
	/* 1*/ {0,  80, 75, 40,  20,  80, 10,  85,  90, 50, 6, 5},
	/* 2*/ {1,  90, 85, 45,  30,  90, 15,  95,  95, 55, 5, 5},
	/* 3*/ {2,  90, 85, 45,  40,  90, 20,  95,  95, 55, 4, 5},
	/* 4*/ {3,  90, 85, 45,  40,  90, 20,  95,  95, 55, 3, 5},
	/* 5*/ {4, 100, 95, 50,  40, 100, 20, 105, 100, 60, 2, 5},
	/* 6*/ {5, 100, 95, 50,  50, 100, 25, 105, 100, 60, 5, 5},
	/* 7*/ {6, 100, 95, 50,  50, 100, 25, 105, 100, 60, 2, 5},
	/* 8*/ {0, 100, 95, 50,  50, 100, 25, 105, 100, 60, 2, 5},
	/* 9*/ {0, 100, 95, 50,  60, 100, 30, 105, 100, 60, 1, 3},
	/*10*/ {0, 100, 95, 50,  60, 100, 30, 105, 100, 60, 5, 5},
	/*11*/ {0, 100, 95, 50,  60, 100, 30, 105, 100, 60, 2, 5},
	/*12*/ {0, 100, 95, 50,  80, 100, 40, 105, 100, 60, 1, 3},
	/*13*/ {0, 100, 95, 50,  80, 100, 40, 105, 100, 60, 1, 3},
	/*14*/ {0, 100, 95, 50,  80, 100, 40, 105, 100, 60, 3, 5},
	/*15*/ {0, 100, 95, 50, 100, 100, 50, 105, 100, 60, 1, 3},
	/*16*/ {0, 100, 95, 50, 100, 100, 50, 105, 100, 60, 1, 3},
	/*17*/ {0, 100, 95, 50, 100, 100, 50, 105,   0,  0, 0, 0},
	/*18*/ {0, 100, 95, 50, 100, 100, 50, 105, 100, 60, 1, 3},
	/*19*/ {0, 100, 95, 50, 120, 100, 60, 105,   0,  0, 0, 0},
	/*20*/ {0, 100, 95, 50, 120, 100, 60, 105,   0,  0, 0, 0},
	/*21*/ {0,  90, 95, 50, 120, 100, 60, 105,   0,  0, 0, 0},
	};
	/*@formatter:on*/

	private final MapBasedPacManGameWorld world;

	public MsPacManGame() {
		// all levels share this world
		world = new MapBasedPacManGameWorld();

		bonusNames = new String[] { "CHERRIES", "STRAWBERRY", "PEACH", "PRETZEL", "APPLE", "PEAR", "BANANA" };
		bonusValues = new int[] { 100, 200, 500, 700, 1000, 2000, 5000 };
		bonus = new MovingBonus();

		pac = new Pac("Ms. Pac-Man", LEFT);

		ghosts = new Ghost[4];
		ghosts[BLINKY] = new Ghost(BLINKY, "Blinky", LEFT);
		ghosts[PINKY] = new Ghost(PINKY, "Pinky", UP);
		ghosts[INKY] = new Ghost(INKY, "Inky", DOWN);
		ghosts[SUE] = new Ghost(SUE, "Sue", DOWN);

		bonus.world = world;
		pac.world = world;
		for (Ghost ghost : ghosts) {
			ghost.world = world;
		}

		highscoreFileName = "hiscore-mspacman.xml";
	}

	@Override
	public int mazeNumber(int levelNumber) {
		switch (levelNumber) {
		case 1:
		case 2:
			return 1; // pink maze, white dots
		case 3:
		case 4:
		case 5:
			return 2; // light blue maze, yellow dots
		case 6:
		case 7:
		case 8:
		case 9:
			return 3; // orange maze, red dots
		case 10:
		case 11:
		case 12:
		case 13:
			return 4; // dark blue maze, white dots
		default:
			if (levelNumber < 1) {
				throw new IllegalArgumentException("Illegal level number: " + levelNumber);
			}
			// From level 14 on, maze switches between 5 and 6 every 4 levels
			// Maze #5 = pink maze, cyan dots (same map as maze 3)
			// Maze #6 = orange maze, white dots (same map as maze 4)
			return (levelNumber - 14) % 8 < 4 ? 5 : 6;
		}
	}

	@Override
	public int mapNumber(int mazeNumber) {
		// Maze #5 has the same map as #3 but a different color, same for #6 vs. #4
		return mazeNumber == 5 ? 3 : mazeNumber == 6 ? 4 : mazeNumber;
	}

	@Override
	protected void buildLevel(int levelNumber) {
		int mazeNumber = mazeNumber(levelNumber);
		world.setMap(new WorldMap("/mspacman/maps/map" + mapNumber(mazeNumber) + ".txt"));
		level = new GameLevel(MSPACMAN_LEVELS[levelNumber <= 21 ? levelNumber - 1 : 20]);
		level.setWorld(world);
		level.mazeNumber = mazeNumber;
		if (levelNumber > 7) {
			level.bonusSymbol = (byte) random.nextInt(7);
		}
		log("Ms. Pac-Man level %d created, maze index is %d", levelNumber, mazeNumber);
	}

	@Override
	public long bonusActivationTicks(int levelNumber) {
		return Long.MAX_VALUE;
	}
}