package de.amr.games.pacman.core;

import static de.amr.games.pacman.lib.Logging.log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.V2i;

/**
 * Controls Pac-Man movement.
 * 
 * @author Armin Reichert
 */
public class Autopilot {

	private static final int FIXED_DIRECTION_TICKS = 2;
	private static final int MAX_GHOST_DETECTION_TILES = 4;
	private static final int MAX_CHASE_TILES = 10;

	private final Game game;
	private final PacMan pacMan;
	private final Ghost[] ghosts;
	private int directionFixedTicks;

	public Autopilot(Game game) {
		this.game = game;
		pacMan = game.pacMan;
		ghosts = game.ghosts;
	}

	public void controlPacMan() {
		V2i pacManTile = pacMan.tile();

		if (directionFixedTicks > 0) {
			--directionFixedTicks;
			return;
		}

		pacMan.targetTile = null;

		Ghost hunter = findHuntingGhostAhead(); // Where is Hunter?
		if (hunter != null) {
			pacMan.wishDir = findEscapeDirectionExcluding(Collections.singleton(pacMan.wishDir));
			log("Detected ghost %s moving %s ahead, changing direction to %s", hunter.name(), hunter.dir, pacMan.wishDir);
			directionFixedTicks = FIXED_DIRECTION_TICKS; // TODO how long is best?
			return;
		}

		// when targeting a food tile, keep moving till next intersection
		if (pacMan.couldMove && !game.world.isIntersectionTile(pacManTile.x, pacManTile.y))
			return;

		Ghost prey = findFrightenedGhostInReach();
		if (prey != null) {
			log("Detected frightened ghost %s %.0g tiles away", prey.name(), prey.tile().manhattanDistance(pacManTile));
			pacMan.targetTile = prey.tile();
		} else if (game.bonusAvailableTicks > 0) {
			log("Detected active bonus");
			pacMan.targetTile = game.world.bonusTile;
		} else {
			pacMan.targetTile = findTileFarestFromGhosts(findNearestFoodTiles());
		}
		approachTarget();
	}

	private void approachTarget() {
		if (pacMan.targetTile == null) {
			return;
		}
		double minDist = Double.MAX_VALUE;
		Direction minDistDir = null;
		for (Direction dir : Direction.shuffled()) {
			if (dir == pacMan.dir.opposite()) {
				continue; // TODO sometimes reversing direction could be useful
			}
			V2i neighbor = pacMan.tile().sum(dir.vec);
			if (!game.canAccessTile(pacMan, neighbor.x, neighbor.y)) {
				continue;
			}
			double dist = neighbor.euclideanDistance(pacMan.targetTile);
			if (dist < minDist) {
				minDist = dist;
				minDistDir = dir;
			}
		}
		pacMan.wishDir = minDistDir;
		log("Approach target tile %s from tile %s by turning %s", pacMan.tile(), pacMan.targetTile, pacMan.wishDir);
	}

	private Ghost findFrightenedGhostInReach() {
		for (Ghost ghost : ghosts) {
			if (ghost.frightened && ghost.tile().manhattanDistance(pacMan.tile()) < MAX_CHASE_TILES) {
				return ghost;
			}
		}
		return null;
	}

	private Ghost findHuntingGhostAhead() {
		V2i pacManTile = pacMan.tile();
		for (int i = 1; i <= MAX_GHOST_DETECTION_TILES; ++i) {
			V2i ahead = pacManTile.sum(pacMan.dir.vec.scaled(i));
			if (!game.canAccessTile(pacMan, ahead.x, ahead.y)) {
				break;
			}
			V2i aheadLeft = ahead.sum(pacMan.dir.turnLeft().vec), aheadRight = ahead.sum(pacMan.dir.turnRight().vec);
			for (Ghost ghost : ghosts) {
				if (!game.isGhostHunting(ghost)) {
					continue;
				}
				if (ghost.tile().equals(ahead) || ghost.tile().equals(aheadLeft) || ghost.tile().equals(aheadRight)) {
					return ghost;
				}
			}
		}
		return null;
	}

	private Direction findEscapeDirectionExcluding(Collection<Direction> forbidden) {
		V2i pacManTile = pacMan.tile();
		for (Direction dir : Direction.shuffled()) {
			if (forbidden.contains(dir)) {
				continue;
			}
			V2i neighbor = pacManTile.sum(dir.vec);
			if (game.canAccessTile(pacMan, neighbor.x, neighbor.y)) {
				return dir;
			}
		}
		return null;
	}

	private List<V2i> findNearestFoodTiles() {
		List<V2i> foodTiles = new ArrayList<>();
		V2i pacManTile = pacMan.tile();
		double minDist = Double.MAX_VALUE;
		for (int x = 0; x < game.world.size.x; ++x) {
			for (int y = 0; y < game.world.size.y; ++y) {
				if (!game.world.isFoodTile(x, y) || game.world.foodRemoved(x, y)) {
					continue;
				}
				V2i foodTile = new V2i(x, y);
				double dist = pacManTile.manhattanDistance(foodTile);
				if (dist < minDist) {
					minDist = dist;
					foodTiles.clear();
					foodTiles.add(foodTile);
				} else if (dist == minDist) {
					foodTiles.add(foodTile);
				}
			}
		}
		log("Nearest food tiles from Pac-Man location %s:", pacManTile);
		for (V2i t : foodTiles) {
			log("\t%s (%.2g tiles away from Pac-Man, %.2g tiles away from ghosts)", t, t.manhattanDistance(pacManTile),
					minDistanceFromGhosts(t));
		}
		return foodTiles;
	}

	private V2i findTileFarestFromGhosts(List<V2i> tiles) {
		V2i farestTile = null;
		double maxDist = -1;
		for (V2i tile : tiles) {
			double dist = minDistanceFromGhosts(tile);
			if (dist > maxDist) {
				maxDist = dist;
				farestTile = tile;
			}
		}
		return farestTile;
	}

	private double minDistanceFromGhosts(V2i tile) {
		double minDist = Double.MAX_VALUE;
		for (Ghost ghost : ghosts) {
			double dist = tile.manhattanDistance(ghost.tile());
			if (dist < minDist) {
				minDist = dist;
			}
		}
		return minDist;
	}
}