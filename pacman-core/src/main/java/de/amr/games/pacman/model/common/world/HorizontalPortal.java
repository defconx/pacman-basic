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

import static de.amr.games.pacman.model.common.world.World.TS;

import de.amr.games.pacman.lib.math.Vector2i;
import de.amr.games.pacman.model.common.actors.Creature;

/**
 * A portal connects two tunnel ends leading out of the map.
 * <p>
 * This kind of portal prolongates the right tunnel end by <code>DEPTH</code> tiles before wrapping with the left part
 * (also <code>DEPTH</code> tiles) of the portal.
 * 
 * @author Armin Reichert
 */
public record HorizontalPortal(Vector2i leftTunnelEnd, Vector2i rightTunnelEnd) implements Portal {

	private static final int DEPTH = 2;

	@Override
	public void teleport(Creature guy) {
		var oldPos = guy.position();
		if (guy.tile().y() == leftTunnelEnd.y() && guy.position().x() < (leftTunnelEnd.x() - DEPTH) * TS) {
			guy.placeAtTile(rightTunnelEnd);
			guy.moveResult.teleported = true;
			guy.moveResult.addMessage("%s: Teleported from %s to %s".formatted(guy.name(), oldPos, guy.position()));
		} else if (guy.tile().equals(rightTunnelEnd.plus(DEPTH, 0))) {
			guy.placeAtTile(leftTunnelEnd.minus(DEPTH, 0), 0, 0);
			guy.moveResult.teleported = true;
			guy.moveResult.addMessage("%s: Teleported from %s to %s".formatted(guy.name(), oldPos, guy.position()));
		}
	}

	@Override
	public boolean contains(Vector2i tile) {
		for (int i = 1; i <= DEPTH; ++i) {
			if (tile.equals(leftTunnelEnd.minus(i, 0))) {
				return true;
			}
			if (tile.equals(rightTunnelEnd.plus(i, 0))) {
				return true;
			}
		}
		return false;
	}

	@Override
	public double distance(Creature guy) {
		var leftEndPosition = leftTunnelEnd.minus(DEPTH, 0).scaled(TS).toFloatVec();
		var rightEndPosition = rightTunnelEnd.plus(DEPTH, 0).scaled(TS).toFloatVec();
		var guyPos = guy.position();
		return Math.abs(Math.min(guyPos.euclideanDistance(leftEndPosition), guyPos.euclideanDistance(rightEndPosition)));
	}
}