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

import java.util.List;

import de.amr.games.pacman.lib.math.Vector2f;
import de.amr.games.pacman.lib.math.Vector2i;
import de.amr.games.pacman.model.common.actors.Creature;

/**
 * @author Armin Reichert
 */
public interface GhostHouse {

	Vector2i topLeftTile();

	Vector2i sizeInTiles();

	Door door();

	List<Vector2f> seatPositions();

	default boolean contains(Vector2i tile) {
		Vector2i topLeft = topLeftTile();
		Vector2i bottomRightOutside = topLeft.plus(sizeInTiles());
		return tile.x() >= topLeft.x() && tile.x() < bottomRightOutside.x() //
				&& tile.y() >= topLeft.y() && tile.y() < bottomRightOutside.y();
	}

	default boolean contains(Creature ghost) {
		return contains(ghost.tile());
	}

	boolean leadOutside(Creature ghost);

	boolean leadInside(Creature ghost, Vector2f targetPosition);
}