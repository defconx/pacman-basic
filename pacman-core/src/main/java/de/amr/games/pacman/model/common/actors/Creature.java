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

import static de.amr.games.pacman.lib.Direction.DOWN;
import static de.amr.games.pacman.lib.Direction.LEFT;
import static de.amr.games.pacman.lib.Direction.RIGHT;
import static de.amr.games.pacman.lib.Direction.UP;
import static de.amr.games.pacman.model.common.world.World.t;
import static java.lang.Math.abs;

import java.util.Objects;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.V2d;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.world.World;

/**
 * Base class for creatures which can move through the world.
 * 
 * @author Armin Reichert
 */
public class Creature extends Entity {

	protected static final Direction[] TURN_PRIORITY = { UP, LEFT, DOWN, RIGHT };

	/** Readable name, for display and logging purposes. */
	public final String name;

	/** The current move direction. */
	protected Direction moveDir = RIGHT;

	/** The wish direction. Will be taken as soon as possible. */
	protected Direction wishDir = RIGHT;

	/** The target tile. Can be inaccessible or outside of the world. */
	public V2i targetTile = null;

	/** Tells if the creature entered a new tile with its last move or placement. */
	public boolean newTileEntered = true;

	/** Tells if the creature got stuck. */
	public boolean stuck = false;

	public Creature(String name) {
		this.name = name;
	}

	@Override
	public void placeAt(V2i tile, double offsetX, double offsetY) {
		super.placeAt(tile, offsetX, offsetY);
		newTileEntered = true;
	}

	/**
	 * Sets the move direction and updates the velocity vector.
	 * 
	 * @param dir the new move direction
	 */
	public void setMoveDir(Direction dir) {
		moveDir = Objects.requireNonNull(dir);
		double speed = velocity.length();
		velocity = new V2d(dir.vec).scaled(speed);
	}

	public Direction moveDir() {
		return moveDir;
	}

	public void setWishDir(Direction dir) {
		wishDir = Objects.requireNonNull(dir);
	}

	public Direction wishDir() {
		return wishDir;
	}

	public void setBothDirs(Direction dir) {
		setMoveDir(dir);
		setWishDir(dir);
	}

	public V2i tilesAhead(int n) {
		return tile().plus(moveDir.vec.scaled(n));
	}

	public V2i tilesAheadWithBug(int n) {
		return moveDir == UP ? tilesAhead(n).minus(n, 0) : tilesAhead(n);
	}

	/**
	 * Sets the fraction of the given base speed.
	 * 
	 * @param fraction  fraction of base speed
	 * @param baseSpeed base speed (speed at fraction=1.0)
	 */
	private void setRelSpeed(double fraction, double baseSpeed) {
		if (fraction < 0) {
			throw new IllegalArgumentException("Negative speed fraction: " + fraction);
		}
		setAbsSpeed(fraction * baseSpeed);
	}

	/**
	 * Sets the speed as a fraction of the game base speed (1.25 pixels/sec).
	 * 
	 * @param fraction fraction of base speed
	 */
	public void setRelSpeed(double fraction) {
		setRelSpeed(fraction, GameModel.BASE_SPEED);
	}

	/**
	 * Sets the absolute speed and updates the velocity vector.
	 * 
	 * @param pixelsPerTick speed in pixels per tick
	 */
	public void setAbsSpeed(double pixelsPerTick) {
		velocity = pixelsPerTick == 0 ? V2d.NULL : new V2d(moveDir.vec).scaled(pixelsPerTick);
	}

	/**
	 * @param world the world
	 * @param tile  some tile inside or outside of the world
	 * @return if this creature can access the given tile
	 */
	public boolean canAccessTile(World world, V2i tile) {
		if (!world.insideMap(tile)) {
			// portal tiles are the only tiles accessible outside of the map
			return world.isPortal(tile);
		}
		if (world.isWall(tile)) {
			return false;
		}
		V2i leftDoor = world.ghostHouse().doorTileLeft();
		V2i rightDoor = leftDoor.plus(1, 0);
		return !leftDoor.equals(tile) && !rightDoor.equals(tile);
	}

	/**
	 * Force turning to the opposite direction.
	 */
	public void forceTurningBack(World world) {
		Direction oppositeDir = moveDir.opposite();
		if (canAccessTile(world, tile().plus(oppositeDir.vec))) {
			setWishDir(oppositeDir);
			setMoveDir(oppositeDir);
		}
	}

	/**
	 * Move through the world.
	 * <p>
	 * First checks if the creature can teleport, then if the creature can move to its current wish direction. If this is
	 * not possible it moves to its current move direction.
	 */
	public void tryMoving(World world) {
		tryTeleport(world);
		tryMoving(world, wishDir);
		if (stuck) {
			tryMoving(world, moveDir);
		} else {
			moveDir = wishDir;
		}
	}

	private void tryTeleport(World world) {
		if (moveDir == Direction.RIGHT) {
			world.portals().stream() //
					.filter(portal -> tile().equals(portal.right)) //
					.findFirst() //
					.ifPresent(portal -> placeAt(portal.left, 0, 0));
		} else if (moveDir == Direction.LEFT) {
			world.portals().stream() //
					.filter(portal -> tile().equals(portal.left)) //
					.findFirst() //
					.ifPresent(portal -> placeAt(portal.right, 0, 0));
		}
	}

	/**
	 * Tries to move to the given direction. If creature reaches an inaccessible tile, it gets stuck.
	 * <p>
	 * TODO: this should really be simpler
	 * 
	 * @param world the world where this creatures is located
	 * @param dir   intended move direction
	 */
	private void tryMoving(World world, Direction dir) {
		var tile = tile();
		var offset = offset();
		var neighborTile = tile.plus(dir.vec);

		// check if creature can turn towards move direction from its current position
		if (canAccessTile(world, neighborTile)) {
			if (dir.isHorizontal()) {
				if (abs(offset.y) > velocity.length()) {
					stuck = true;
					return;
				}
				setOffset(offset.x, 0);
			} else if (dir.isVertical()) {
				if (abs(offset.x) > velocity.length()) {
					stuck = true;
					return;
				}
				setOffset(0, offset.y);
			}
		}

		var newPosition = position.plus(new V2d(dir.vec).scaled(velocity.length()));
		var newOffset = World.offset(newPosition);
		var newTile = World.tile(newPosition);

		// avoid moving into inaccessible neighbor tile
		if (!canAccessTile(world, newTile)) {
			stuck = true;
			return;
		}

		// align with inaccessible neighbor tile
		if (!canAccessTile(world, neighborTile)) {
			if (dir == Direction.RIGHT && newOffset.x > 0 || dir == Direction.LEFT && newOffset.x < 0) {
				setOffset(0, offset.y);
				stuck = true;
				return;
			}
			if (dir == Direction.DOWN && newOffset.y > 0 || dir == Direction.UP && newOffset.y < 0) {
				setOffset(offset.x, 0);
				stuck = true;
				return;
			}
		}

		// yes, we can (move)
		stuck = false;
		setPosition(t(newTile.x) + newOffset.x, t(newTile.y) + newOffset.y);
		newTileEntered = !tile().equals(tile);
	}

	/**
	 * As described in the Pac-Man dossier: checks all accessible neighbor tiles in order UP, LEFT, DOWN, RIGHT and
	 * selects the one with smallest Euclidean distance to the target tile. Reversing the move direction is not allowed.
	 */
	public void computeDirectionTowardsTarget(World world) {
		if (targetTile == null || world.isPortal(tile())) {
			return;
		}
		if (!newTileEntered && !stuck) {
			return;
		}
		double minDist = Double.MAX_VALUE;
		for (var dir : TURN_PRIORITY) {
			if (isForbiddenDirection(dir)) {
				continue;
			}
			var neighbor = tile().plus(dir.vec);
			if (!canAccessTile(world, neighbor)) {
				continue;
			}
			double d = neighbor.euclideanDistance(targetTile);
			if (d < minDist) {
				minDist = d;
				wishDir = dir;
			}
		}
	}

	protected boolean isForbiddenDirection(Direction dir) {
		return dir == moveDir.opposite();
	}
}