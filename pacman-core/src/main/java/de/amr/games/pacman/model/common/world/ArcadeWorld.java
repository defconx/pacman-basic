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
package de.amr.games.pacman.model.common.world;

import static de.amr.games.pacman.lib.math.Vector2i.v2i;
import static de.amr.games.pacman.model.common.world.World.halfTileRightOf;
import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import de.amr.games.pacman.lib.anim.AnimationMap;
import de.amr.games.pacman.lib.math.Vector2f;
import de.amr.games.pacman.lib.math.Vector2i;
import de.amr.games.pacman.lib.steering.Direction;
import de.amr.games.pacman.model.common.IllegalGhostIDException;
import de.amr.games.pacman.model.common.actors.Ghost;

/**
 * The world used in the Arcade versions of Pac-Man and Ms. Pac-Man. Maze structure varies but ghost house
 * structure/position, ghost starting positions/directions and Pac-Man starting position/direction are the same for each
 * world.
 * 
 * @author Armin Reichert
 */
public class ArcadeWorld extends TileMapWorld {

	/** Arcade world size is 28x36 tiles. */
	public static final Vector2i SIZE_TILES = v2i(28, 36);

	private final ArcadeGhostHouse house;
	private final Collection<Vector2i> upwardBlockedTiles;
	private AnimationMap animationMap;

	/**
	 * @param tileMapData        byte-array of tile map data
	 * @param upwardBlockedTiles list of tiles where ghosts can sometimes not move upwards
	 */
	public ArcadeWorld(byte[][] tileMapData, Collection<Vector2i> upwardBlockedTiles) {
		super(tileMapData);
		this.upwardBlockedTiles = requireNonNull(upwardBlockedTiles);
		house = new ArcadeGhostHouse();
	}

	/**
	 * @param tileMapData byte-array of tile map data
	 */
	public ArcadeWorld(byte[][] tileMapData) {
		this(tileMapData, Collections.emptyList());
	}

	/**
	 * @return tiles where chasing ghosts cannot move upwards
	 */
	public Collection<Vector2i> upwardBlockedTiles() {
		return Collections.unmodifiableCollection(upwardBlockedTiles);
	}

	@Override
	public Vector2f pacInitialPosition() {
		return halfTileRightOf(13, 26);
	}

	@Override
	public Direction pacInitialDirection() {
		return Direction.LEFT;
	}

	@Override
	public Vector2f ghostInitialPosition(byte ghostID) {
		return switch (ghostID) {
		case Ghost.ID_RED_GHOST -> ghostHouse().doors().get(0).entryPosition();
		case Ghost.ID_PINK_GHOST -> ghostHouse().seatPositions().get(0);
		case Ghost.ID_CYAN_GHOST -> ghostHouse().seatPositions().get(1);
		case Ghost.ID_ORANGE_GHOST -> ghostHouse().seatPositions().get(2);
		default -> throw new IllegalGhostIDException(ghostID);
		};
	}

	@Override
	public Direction ghostInitialDirection(byte ghostID) {
		return switch (ghostID) {
		case Ghost.ID_RED_GHOST -> Direction.LEFT;
		case Ghost.ID_PINK_GHOST -> Direction.DOWN;
		case Ghost.ID_CYAN_GHOST -> Direction.UP;
		case Ghost.ID_ORANGE_GHOST -> Direction.UP;
		default -> throw new IllegalGhostIDException(ghostID);
		};
	}

	@Override
	public Vector2f ghostRevivalPosition(byte ghostID) {
		return switch (ghostID) {
		case Ghost.ID_RED_GHOST -> ghostHouse().seatPositions().get(1);
		case Ghost.ID_PINK_GHOST, Ghost.ID_CYAN_GHOST, Ghost.ID_ORANGE_GHOST -> ghostInitialPosition(ghostID);
		default -> throw new IllegalGhostIDException(ghostID);
		};
	}

	@Override
	public Vector2i ghostScatterTargetTile(byte ghostID) {
		return switch (ghostID) {
		case Ghost.ID_RED_GHOST -> v2i(25, 0);
		case Ghost.ID_PINK_GHOST -> v2i(2, 0);
		case Ghost.ID_CYAN_GHOST -> v2i(27, 34);
		case Ghost.ID_ORANGE_GHOST -> v2i(0, 34);
		default -> throw new IllegalGhostIDException(ghostID);
		};
	}

	@Override
	public ArcadeGhostHouse ghostHouse() {
		// WTF! I learned today, 2022-05-27, that Java allows co-variant return types since JDK 5.0!
		return house;
	}

	@Override
	public boolean isIntersection(Vector2i tile) {
		requireNonNull(tile);

		if (tile.x() <= 0 || tile.x() >= numCols() - 1) {
			return false; // exclude portal entries and tiles outside of the map
		}
		if (ghostHouse().contains(tile)) {
			return false;
		}
		long numWallNeighbors = tile.neighbors().filter(this::isWall).count();
		long numDoorNeighbors = tile.neighbors().filter(ghostHouse().doors().get(0)::contains).count();
		return numWallNeighbors + numDoorNeighbors < 2;
	}

	@Override
	public Optional<AnimationMap> animations() {
		return Optional.ofNullable(animationMap);
	}

	@Override
	public void setAnimations(AnimationMap animationMap) {
		this.animationMap = animationMap;
	}
}