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

import static de.amr.games.pacman.model.common.world.World.HTS;
import static de.amr.games.pacman.model.common.world.World.TS;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import de.amr.games.pacman.lib.math.Vector2f;
import de.amr.games.pacman.lib.math.Vector2i;

/**
 * @author Armin Reichert
 */
public class Door {

	private Vector2i leftUpperTile;
	private int sizeInTiles;

	public Door(Vector2i leftUpperTile, int sizeInTiles) {
		this.leftUpperTile = leftUpperTile;
		this.sizeInTiles = sizeInTiles;
	}

	public Vector2i leftUpperTile() {
		return leftUpperTile;
	}

	public int sizeInTiles() {
		return sizeInTiles;
	}

	public Stream<Vector2i> tiles() {
		return IntStream.range(0, sizeInTiles).mapToObj(x -> leftUpperTile.plus(x, 0));
	}

	public boolean contains(Vector2i tile) {
		for (int x = 0; x < sizeInTiles; ++x) {
			if (tile.equals(leftUpperTile.plus(x, 0))) {
				return true;
			}
		}
		return false;
	}

	public Vector2f entryPosition() {
		var x = leftUpperTile.x() * TS + HTS;
		var y = (leftUpperTile.y() - 1) * TS;
		return new Vector2f(x, y);
	}

	public Vector2i entryTile() {
		return leftUpperTile.plus(sizeInTiles / 2, 0);
	}
}