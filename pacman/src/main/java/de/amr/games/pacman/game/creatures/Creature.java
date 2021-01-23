package de.amr.games.pacman.game.creatures;

import static de.amr.games.pacman.game.worlds.PacManGameWorld.TS;
import static de.amr.games.pacman.lib.Direction.DOWN;
import static de.amr.games.pacman.lib.Direction.LEFT;
import static de.amr.games.pacman.lib.Direction.RIGHT;
import static de.amr.games.pacman.lib.Direction.UP;
import static java.lang.Math.abs;

import de.amr.games.pacman.game.worlds.PacManGameWorld;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.V2f;
import de.amr.games.pacman.lib.V2i;

/**
 * Base class for Pac-Man, the ghosts and the bonus. Creatures can move through the maze.
 * 
 * @author Armin Reichert
 */
public class Creature {

	/** Left upper corner of TSxTS collision box. Sprites can be larger. */
	public V2f position = V2f.NULL;

	/** The current move direction. */
	public Direction dir;

	/** The intended move direction that will be taken as soon as possible. */
	public Direction wishDir;

	/** Relative speed (between 0 and 1). */
	public float speed;

	/** If the creature is drawn on the screen. */
	public boolean visible;

	/** If the creature entered a new tile with its last movement or placement. */
	public boolean changedTile;

	/** If the creature could move in the last try. */
	public boolean couldMove;

	/** If the next move will in any case take the intended direction if possible. */
	public boolean forcedDirection;

	/** If movement is constrained to be aligned with the tiles. */
	public boolean forcedOnTrack;

	public void placeAt(V2i tile, float offsetX, float offsetY) {
		position = new V2f(tile.x * TS + offsetX, tile.y * TS + offsetY);
		changedTile = true;
	}

	public V2i tile() {
		return PacManGameWorld.tile(position);
	}

	public V2f offset() {
		return PacManGameWorld.offset(position);
	}

	public void setOffset(float offsetX, float offsetY) {
		placeAt(tile(), offsetX, offsetY);
	}

	public boolean canAccessTile(PacManGameWorld world, int x, int y) {
		if (world.isPortal(x, y)) {
			return true;
		}
		if (world.isGhostHouseDoor(x, y)) {
			return false;
		}
		return world.inMapRange(x, y) && !world.isWall(x, y);
	}

	public boolean canAccessTile(PacManGameWorld world, V2i tile) {
		return canAccessTile(world, tile.x, tile.y);
	}

	public void tryMoving(PacManGameWorld world) {
		V2i guyLocation = tile();
		// teleport?
		for (int i = 0; i < world.numPortals(); ++i) {
			if (guyLocation.equals(world.portalRight(i)) && dir == RIGHT) {
				placeAt(world.portalLeft(i), 0, 0);
				return;
			}
			if (guyLocation.equals(world.portalLeft(i)) && dir == LEFT) {
				placeAt(world.portalRight(i), 0, 0);
				return;
			}
		}
		tryMoving(world, wishDir);
		if (couldMove) {
			dir = wishDir;
		} else {
			tryMoving(world, dir);
		}
	}

	public void tryMoving(PacManGameWorld world, Direction dir) {
		// 100% speed corresponds to 1.25 pixels/tick (75px/sec)
		float pixels = speed * 1.25f;

		V2i guyLocationBeforeMove = tile();
		V2f offset = offset();
		V2i neighbor = guyLocationBeforeMove.sum(dir.vec);

		// check if guy can change its direction now
		if (forcedOnTrack && canAccessTile(world, neighbor)) {
			if (dir == LEFT || dir == RIGHT) {
				if (abs(offset.y) > pixels) {
					couldMove = false;
					return;
				}
				setOffset(offset.x, 0);
			} else if (dir == UP || dir == DOWN) {
				if (abs(offset.x) > pixels) {
					couldMove = false;
					return;
				}
				setOffset(0, offset.y);
			}
		}

		V2f velocity = new V2f(dir.vec).scaled(pixels);
		V2f newPosition = position.sum(velocity);
		V2i newTile = PacManGameWorld.tile(newPosition);
		V2f newOffset = PacManGameWorld.offset(newPosition);

		// block moving into inaccessible tile
		if (!canAccessTile(world, newTile)) {
			couldMove = false;
			return;
		}

		// align with edge of inaccessible neighbor
		if (!canAccessTile(world, neighbor)) {
			if (dir == RIGHT && newOffset.x > 0 || dir == LEFT && newOffset.x < 0) {
				setOffset(0, offset.y);
				couldMove = false;
				return;
			}
			if (dir == DOWN && newOffset.y > 0 || dir == UP && newOffset.y < 0) {
				setOffset(offset.x, 0);
				couldMove = false;
				return;
			}
		}

		placeAt(newTile, newOffset.x, newOffset.y);
		changedTile = !tile().equals(guyLocationBeforeMove);
		couldMove = true;
	}
}