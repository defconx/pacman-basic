/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.test;

import static de.amr.games.pacman.lib.Globals.v2i;
import static de.amr.games.pacman.model.world.World.halfTileRightOf;

import java.util.List;

import de.amr.games.pacman.model.world.ArcadeWorld;
import de.amr.games.pacman.model.world.World;
import org.junit.Assert;
import org.junit.Test;

import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.model.world.Door;
import de.amr.games.pacman.model.world.House;

/**
 * @author Armin Reichert
 */
public class HouseTest {

	static final Vector2i POSITION = v2i(10, 15);
	static final Vector2i SIZE = v2i(8, 5);
	static final Door DOOR = new Door(v2i(13, 15), v2i(14, 15));

	@Test(expected = NullPointerException.class)
	public void testLeftDoorWingNotNull() {
		new Door(null, v2i(0, 0));
	}

	@Test(expected = NullPointerException.class)
	public void testRightDoorWingNotNull() {
		new Door(v2i(0, 0), null);
	}

	@Test
	public void testHouseProperties() {
		var house = ArcadeWorld.createArcadeHouse();
		Assert.assertEquals(POSITION, house.topLeftTile());
		Assert.assertEquals(SIZE, house.size());
		Assert.assertEquals(DOOR, house.door());
		Assert.assertEquals(halfTileRightOf(11, 17), house.getSeat("left"));
		Assert.assertEquals(halfTileRightOf(13, 17), house.getSeat("middle"));
		Assert.assertEquals(halfTileRightOf(15, 17), house.getSeat("right"));
	}
}