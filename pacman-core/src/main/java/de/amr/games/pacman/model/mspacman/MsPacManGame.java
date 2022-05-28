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
package de.amr.games.pacman.model.mspacman;

import static de.amr.games.pacman.lib.Logging.log;
import static de.amr.games.pacman.lib.TickTimer.sec_to_ticks;
import static de.amr.games.pacman.model.common.Ghost.CYAN_GHOST;
import static de.amr.games.pacman.model.common.Ghost.ORANGE_GHOST;
import static de.amr.games.pacman.model.common.Ghost.PINK_GHOST;
import static de.amr.games.pacman.model.common.Ghost.RED_GHOST;

import java.io.File;
import java.util.Random;

import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.lib.TickTimer;
import de.amr.games.pacman.model.common.GameLevel;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.Ghost;
import de.amr.games.pacman.model.common.Pac;
import de.amr.games.pacman.model.common.world.ArcadeWorld;
import de.amr.games.pacman.model.common.world.GhostHouse;
import de.amr.games.pacman.model.common.world.World;

/**
 * Model of the Ms. Pac-Man game.
 * <p>
 * TODO: are the level data except for the bonus symbols the same as in Pac-Man?
 * <p>
 * See https://gamefaqs.gamespot.com/arcade/583976-ms-pac-man/faqs/1298
 * 
 * @author Armin Reichert
 */
public class MsPacManGame extends GameModel {

	public static final int CHERRIES = 0, STRAWBERRY = 1, PEACH = 2, PRETZEL = 3, APPLE = 4, PEAR = 5, BANANA = 6;

	private final Object[][] data = {
	/*@formatter:off*/
	/* 1*/ {CHERRIES,    80, 75, 40,  20,  80, 10,  85,  90, 50, 6, 5},
	/* 2*/ {STRAWBERRY,  90, 85, 45,  30,  90, 15,  95,  95, 55, 5, 5},
	/* Intermission scene 1: "They Meet" */
	/* 3*/ {PEACH,       90, 85, 45,  40,  90, 20,  95,  95, 55, 4, 5},
	/* 4*/ {PRETZEL,     90, 85, 45,  40,  90, 20,  95,  95, 55, 3, 5},
	/* 5*/ {APPLE,      100, 95, 50,  40, 100, 20, 105, 100, 60, 2, 5},
	/* Intermission scene 2: "The Chase" */
	/* 6*/ {PEAR,       100, 95, 50,  50, 100, 25, 105, 100, 60, 5, 5},
	/* 7*/ {BANANA,     100, 95, 50,  50, 100, 25, 105, 100, 60, 2, 5},
	/* 8*/ {BANANA,     100, 95, 50,  50, 100, 25, 105, 100, 60, 2, 5},
	/* 9*/ {BANANA,     100, 95, 50,  60, 100, 30, 105, 100, 60, 1, 3},
	/* Intermission scene 3: "Junior" */
	/*10*/ {BANANA,     100, 95, 50,  60, 100, 30, 105, 100, 60, 4, 5},
	/*11*/ {BANANA,     100, 95, 50,  60, 100, 30, 105, 100, 60, 2, 5},
	/*12*/ {BANANA,     100, 95, 50,  80, 100, 40, 105, 100, 60, 1, 3},
	/*13*/ {BANANA,     100, 95, 50,  80, 100, 40, 105, 100, 60, 1, 3},
	/* Intermission scene 3: "Junior" */
	/*14*/ {BANANA,     100, 95, 50,  80, 100, 40, 105, 100, 60, 3, 5},
	/*15*/ {BANANA,     100, 95, 50, 100, 100, 50, 105, 100, 60, 1, 3},
	/*16*/ {BANANA,     100, 95, 50, 100, 100, 50, 105, 100, 60, 1, 3},
	/*17*/ {BANANA,     100, 95, 50, 100, 100, 50, 105,   0,  0, 0, 0},
	/* Intermission scene 3: "Junior" */
	/*18*/ {BANANA,     100, 95, 50, 100, 100, 50, 105, 100, 60, 1, 3},
	/*19*/ {BANANA,     100, 95, 50, 120, 100, 60, 105,   0,  0, 0, 0},
	/*20*/ {BANANA,     100, 95, 50, 120, 100, 60, 105,   0,  0, 0, 0},
	/*21*/ {BANANA,      90, 95, 50, 120, 100, 60, 105,   0,  0, 0, 0},
	/*@formatter:on*/
	};

	private static final byte[][] MAP1 = {
			//@formatter:off
			{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,},
			{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,},
			{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,},
			{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,},
			{1,3,3,3,3,3,3,1,1,3,3,3,3,3,3,3,3,3,3,1,1,3,3,3,3,3,3,1,},
			{1,4,1,1,1,1,3,1,1,3,1,1,1,1,1,1,1,1,3,1,1,3,1,1,1,1,4,1,},
			{1,3,1,1,1,1,3,1,1,3,1,1,1,1,1,1,1,1,3,1,1,3,1,1,1,1,3,1,},
			{1,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,1,},
			{1,1,1,3,1,1,3,1,1,1,1,1,3,1,1,3,1,1,1,1,1,3,1,1,3,1,1,1,},
			{0,0,1,3,1,1,3,1,1,1,1,1,3,1,1,3,1,1,1,1,1,3,1,1,3,1,0,0,},
			{1,1,1,3,1,1,3,1,1,1,1,1,3,1,1,3,1,1,1,1,1,3,1,1,3,1,1,1,},
			{2,2,2,3,1,1,3,3,3,3,3,3,3,1,1,3,3,3,3,3,3,3,1,1,3,2,2,2,},
			{1,1,1,3,1,1,1,1,1,0,1,1,1,1,1,1,1,1,0,1,1,1,1,1,3,1,1,1,},
			{0,0,1,3,1,1,1,1,1,0,1,1,1,1,1,1,1,1,0,1,1,1,1,1,3,1,0,0,},
			{0,0,1,3,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,3,1,0,0,},
			{0,0,1,3,1,1,1,1,1,0,1,1,1,0,0,1,1,1,0,1,1,1,1,1,3,1,0,0,},
			{0,0,1,3,1,1,1,1,1,0,1,0,0,0,0,0,0,1,0,1,1,1,1,1,3,1,0,0,},
			{0,0,1,3,1,1,0,0,0,0,1,0,0,0,0,0,0,1,0,0,0,0,1,1,3,1,0,0,},
			{0,0,1,3,1,1,0,1,1,0,1,0,0,0,0,0,0,1,0,1,1,0,1,1,3,1,0,0,},
			{1,1,1,3,1,1,0,1,1,0,1,1,1,1,1,1,1,1,0,1,1,0,1,1,3,1,1,1,},
			{2,2,2,3,0,0,0,1,1,0,0,0,0,0,0,0,0,0,0,1,1,0,0,0,3,2,2,2,},
			{1,1,1,3,1,1,1,1,1,1,1,1,0,1,1,0,1,1,1,1,1,1,1,1,3,1,1,1,},
			{0,0,1,3,1,1,1,1,1,1,1,1,0,1,1,0,1,1,1,1,1,1,1,1,3,1,0,0,},
			{0,0,1,3,3,3,3,3,3,3,0,0,0,1,1,0,0,0,3,3,3,3,3,3,3,1,0,0,},
			{0,0,1,3,1,1,1,1,1,3,1,1,1,1,1,1,1,1,3,1,1,1,1,1,3,1,0,0,},
			{1,1,1,3,1,1,1,1,1,3,1,1,1,1,1,1,1,1,3,1,1,1,1,1,3,1,1,1,},
			{1,3,3,3,3,3,3,3,3,3,3,3,3,0,0,3,3,3,3,3,3,3,3,3,3,3,3,1,},
			{1,3,1,1,1,1,3,1,1,1,1,1,3,1,1,3,1,1,1,1,1,3,1,1,1,1,3,1,},
			{1,3,1,1,1,1,3,1,1,1,1,1,3,1,1,3,1,1,1,1,1,3,1,1,1,1,3,1,},
			{1,3,1,1,1,1,3,1,1,3,3,3,3,1,1,3,3,3,3,1,1,3,1,1,1,1,3,1,},
			{1,4,1,1,1,1,3,1,1,3,1,1,1,1,1,1,1,1,3,1,1,3,1,1,1,1,4,1,},
			{1,3,1,1,1,1,3,1,1,3,1,1,1,1,1,1,1,1,3,1,1,3,1,1,1,1,3,1,},
			{1,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,1,},
			{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,},
			{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,},
			{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,},
			//@formatter:on
	};

	private static final byte[][] MAP2 = {
			//@formatter:off
			{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,},
			{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,},
			{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,},
			{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,},
			{2,2,2,2,2,2,0,1,1,3,3,3,3,3,3,3,3,3,3,1,1,0,2,2,2,2,2,2,},
			{1,1,1,1,1,1,0,1,1,3,1,1,1,1,1,1,1,1,3,1,1,0,1,1,1,1,1,1,},
			{1,1,1,1,1,1,0,1,1,3,1,1,1,1,1,1,1,1,3,1,1,0,1,1,1,1,1,1,},
			{1,4,3,3,3,3,3,3,3,3,3,3,3,1,1,3,3,3,3,3,3,3,3,3,3,3,4,1,},
			{1,3,1,1,1,1,1,1,1,3,1,1,3,1,1,3,1,1,3,1,1,1,1,1,1,1,3,1,},
			{1,3,1,1,1,1,1,1,1,3,1,1,3,1,1,3,1,1,3,1,1,1,1,1,1,1,3,1,},
			{1,3,1,1,3,3,3,3,3,3,1,1,3,1,1,3,1,1,3,3,3,3,3,3,1,1,3,1,},
			{1,3,1,1,3,1,1,1,1,0,1,1,3,3,3,3,1,1,0,1,1,1,1,3,1,1,3,1,},
			{1,3,1,1,3,1,1,1,1,0,1,1,1,1,1,1,1,1,0,1,1,1,1,3,1,1,3,1,},
			{1,3,3,3,3,3,3,1,1,0,1,1,1,1,1,1,1,1,0,1,1,3,3,3,3,3,3,1,},
			{1,1,1,1,1,1,3,1,1,0,0,0,0,0,0,0,0,0,0,1,1,3,1,1,1,1,1,1,},
			{1,1,1,1,1,1,3,1,1,0,1,1,1,0,0,1,1,1,0,1,1,3,1,1,1,1,1,1,},
			{1,3,3,3,3,3,3,1,1,0,1,0,0,0,0,0,0,1,0,1,1,3,3,3,3,3,3,1,},
			{1,3,1,1,1,1,3,1,1,0,1,0,0,0,0,0,0,1,0,1,1,3,1,1,1,1,3,1,},
			{1,3,1,1,1,1,3,0,0,0,1,0,0,0,0,0,0,1,0,0,0,3,1,1,1,1,3,1,},
			{1,3,3,3,1,1,3,1,1,0,1,1,1,1,1,1,1,1,0,1,1,3,1,1,3,3,3,1,},
			{1,1,1,3,1,1,3,1,1,0,0,0,0,0,0,0,0,0,0,1,1,3,1,1,3,1,1,1,},
			{0,0,1,3,1,1,3,1,1,1,1,0,1,1,1,1,0,1,1,1,1,3,1,1,3,1,0,0,},
			{0,0,1,3,1,1,3,1,1,1,1,0,1,1,1,1,0,1,1,1,1,3,1,1,3,1,0,0,},
			{0,0,1,3,3,3,3,3,3,3,3,3,1,1,1,1,3,3,3,3,3,3,3,3,3,1,0,0,},
			{0,0,1,3,1,1,1,1,1,1,1,3,1,1,1,1,3,1,1,1,1,1,1,1,3,1,0,0,},
			{1,1,1,3,1,1,1,1,1,1,1,3,1,1,1,1,3,1,1,1,1,1,1,1,3,1,1,1,},
			{2,2,2,3,3,3,3,1,1,3,3,3,0,0,0,0,3,3,3,1,1,3,3,3,3,2,2,2,},
			{1,1,1,3,1,1,3,1,1,3,1,1,1,1,1,1,1,1,3,1,1,3,1,1,3,1,1,1,},
			{1,1,1,3,1,1,3,1,1,3,1,1,1,1,1,1,1,1,3,1,1,3,1,1,3,1,1,1,},
			{1,4,3,3,1,1,3,3,3,3,3,3,3,1,1,3,3,3,3,3,3,3,1,1,3,3,4,1,},
			{1,3,1,1,1,1,3,1,1,1,1,1,3,1,1,3,1,1,1,1,1,3,1,1,1,1,3,1,},
			{1,3,1,1,1,1,3,1,1,1,1,1,3,1,1,3,1,1,1,1,1,3,1,1,1,1,3,1,},
			{1,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,1,},
			{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,},
			{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,},
			{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,},
			//@formatter:on
	};

	private static final byte[][] MAP3 = {
			//@formatter:off
			{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,},
			{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,},
			{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,},
			{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,},
			{1,3,3,3,3,3,3,3,3,3,1,1,3,3,3,3,1,1,3,3,3,3,3,3,3,3,3,1,},
			{1,3,1,1,1,1,1,1,1,3,1,1,3,1,1,3,1,1,3,1,1,1,1,1,1,1,3,1,},
			{1,4,1,1,1,1,1,1,1,3,1,1,3,1,1,3,1,1,3,1,1,1,1,1,1,1,4,1,},
			{1,3,1,1,3,3,3,3,3,3,3,3,3,1,1,3,3,3,3,3,3,3,3,3,1,1,3,1,},
			{1,3,1,1,3,1,1,3,1,1,1,1,3,1,1,3,1,1,1,1,3,1,1,3,1,1,3,1,},
			{1,3,3,3,3,1,1,3,1,1,1,1,3,1,1,3,1,1,1,1,3,1,1,3,3,3,3,1,},
			{1,1,1,1,3,1,1,3,1,1,1,1,3,1,1,3,1,1,1,1,3,1,1,3,1,1,1,1,},
			{1,1,1,1,3,1,1,3,3,3,3,3,3,3,3,3,3,3,3,3,3,1,1,3,1,1,1,1,},
			{2,3,3,3,3,1,1,1,1,0,1,1,1,1,1,1,1,1,0,1,1,1,1,3,3,3,3,2,},
			{1,3,1,1,0,1,1,1,1,0,1,1,1,1,1,1,1,1,0,1,1,1,1,0,1,1,3,1,},
			{1,3,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,3,1,},
			{1,3,1,1,1,1,0,1,1,0,1,1,1,0,0,1,1,1,0,1,1,0,1,1,1,1,3,1,},
			{1,3,1,1,1,1,0,1,1,0,1,0,0,0,0,0,0,1,0,1,1,0,1,1,1,1,3,1,},
			{1,3,0,0,0,0,0,1,1,0,1,0,0,0,0,0,0,1,0,1,1,0,0,0,0,0,3,1,},
			{1,3,1,1,0,1,1,1,1,0,1,0,0,0,0,0,0,1,0,1,1,1,1,0,1,1,3,1,},
			{1,3,1,1,0,1,1,1,1,0,1,1,1,1,1,1,1,1,0,1,1,1,1,0,1,1,3,1,},
			{1,3,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,3,1,},
			{1,3,1,1,1,1,0,1,1,1,1,1,0,1,1,0,1,1,1,1,1,0,1,1,1,1,3,1,},
			{1,3,1,1,1,1,0,1,1,1,1,1,0,1,1,0,1,1,1,1,1,0,1,1,1,1,3,1,},
			{1,3,3,3,3,3,3,1,1,3,3,3,3,1,1,3,3,3,3,1,1,3,3,3,3,3,3,1,},
			{1,1,1,3,1,1,3,1,1,3,1,1,1,1,1,1,1,1,3,1,1,3,1,1,3,1,1,1,},
			{1,1,1,3,1,1,3,1,1,3,1,1,1,1,1,1,1,1,3,1,1,3,1,1,3,1,1,1,},
			{1,4,3,3,1,1,3,3,3,3,3,3,3,0,0,3,3,3,3,3,3,3,1,1,3,3,4,1,},
			{1,3,1,1,1,1,3,1,1,1,1,1,3,1,1,3,1,1,1,1,1,3,1,1,1,1,3,1,},
			{1,3,1,1,1,1,3,1,1,1,1,1,3,1,1,3,1,1,1,1,1,3,1,1,1,1,3,1,},
			{1,3,3,3,3,3,3,1,1,3,3,3,3,1,1,3,3,3,3,1,1,3,3,3,3,3,3,1,},
			{1,3,1,1,1,1,3,1,1,3,1,1,1,1,1,1,1,1,3,1,1,3,1,1,1,1,3,1,},
			{1,3,1,1,1,1,3,1,1,3,1,1,1,1,1,1,1,1,3,1,1,3,1,1,1,1,3,1,},
			{1,3,3,3,3,3,3,1,1,3,3,3,3,3,3,3,3,3,3,1,1,3,3,3,3,3,3,1,},
			{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,},
			{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,},
			{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,},
			//@formatter:on
	};

	private static final byte[][] MAP4 = {
			//@formatter:off
			{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,},
			{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,},
			{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,},
			{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,},
			{1,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,1,},
			{1,3,1,1,3,1,1,1,1,3,1,1,1,1,1,1,1,1,3,1,1,1,1,3,1,1,3,1,},
			{1,4,1,1,3,1,1,1,1,3,1,1,1,1,1,1,1,1,3,1,1,1,1,3,1,1,4,1,},
			{1,3,1,1,3,1,1,1,1,3,1,1,3,3,3,3,1,1,3,1,1,1,1,3,1,1,3,1,},
			{1,3,1,1,3,3,3,3,3,3,1,1,3,1,1,3,1,1,3,3,3,3,3,3,1,1,3,1,},
			{1,3,1,1,1,1,3,1,1,3,1,1,3,1,1,3,1,1,3,1,1,3,1,1,1,1,3,1,},
			{1,3,1,1,1,1,3,1,1,3,1,1,3,1,1,3,1,1,3,1,1,3,1,1,1,1,3,1,},
			{1,3,3,3,3,3,3,1,1,3,3,3,3,1,1,3,3,3,3,1,1,3,3,3,3,3,3,1,},
			{1,1,1,3,1,1,1,1,1,1,1,1,0,1,1,0,1,1,1,1,1,1,1,1,3,1,1,1,},
			{0,0,1,3,1,1,1,1,1,1,1,1,0,1,1,0,1,1,1,1,1,1,1,1,3,1,0,0,},
			{0,0,1,3,3,3,3,1,1,0,0,0,0,0,0,0,0,0,0,1,1,3,3,3,3,1,0,0,},
			{1,1,1,0,1,1,3,1,1,0,1,1,1,0,0,1,1,1,0,1,1,3,1,1,0,1,1,1,},
			{2,2,2,0,1,1,3,1,1,0,1,0,0,0,0,0,0,1,0,1,1,3,1,1,0,2,2,2,},
			{1,1,1,1,1,1,3,0,0,0,1,0,0,0,0,0,0,1,0,0,0,3,1,1,1,1,1,1,},
			{1,1,1,1,1,1,3,1,1,0,1,0,0,0,0,0,0,1,0,1,1,3,1,1,1,1,1,1,},
			{2,2,2,0,1,1,3,1,1,0,1,1,1,1,1,1,1,1,0,1,1,3,1,1,0,2,2,2,},
			{1,1,1,0,1,1,3,1,1,0,0,0,0,0,0,0,0,0,0,1,1,3,1,1,0,1,1,1,},
			{0,0,1,3,3,3,3,1,1,1,1,1,0,1,1,0,1,1,1,1,1,3,3,3,3,1,0,0,},
			{0,0,1,3,1,1,3,1,1,1,1,1,0,1,1,0,1,1,1,1,1,3,1,1,3,1,0,0,},
			{0,0,1,3,1,1,3,3,3,3,0,0,0,1,1,0,0,0,3,3,3,3,1,1,3,1,0,0,},
			{0,0,1,3,1,1,1,1,1,3,1,1,0,1,1,0,1,1,3,1,1,1,1,1,3,1,0,0,},
			{1,1,1,3,1,1,1,1,1,3,1,1,0,1,1,0,1,1,3,1,1,1,1,1,3,1,1,1,},
			{1,3,3,3,3,3,3,3,3,3,1,1,0,0,0,0,1,1,3,3,3,3,3,3,3,3,3,1,},
			{1,3,1,1,1,1,3,1,1,3,1,1,1,1,1,1,1,1,3,1,1,3,1,1,1,1,3,1,},
			{1,3,1,1,1,1,3,1,1,3,1,1,1,1,1,1,1,1,3,1,1,3,1,1,1,1,3,1,},
			{1,3,1,1,3,3,3,1,1,3,3,3,3,3,3,3,3,3,3,1,1,3,3,3,1,1,3,1,},
			{1,4,1,1,3,1,1,1,1,1,1,1,3,1,1,3,1,1,1,1,1,1,1,3,1,1,4,1,},
			{1,3,1,1,3,1,1,1,1,1,1,1,3,1,1,3,1,1,1,1,1,1,1,3,1,1,3,1,},
			{1,3,3,3,3,3,3,3,3,3,3,3,3,1,1,3,3,3,3,3,3,3,3,3,3,3,3,1,},
			{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,},
			{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,},
			{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,},
			//@formatter:on
	};

	public static World createWorld(int number) {
		return switch (number) {
		case 1 -> new ArcadeWorld(MAP1);
		case 2 -> new ArcadeWorld(MAP2);
		case 3 -> new ArcadeWorld(MAP3);
		case 4 -> new ArcadeWorld(MAP4);
		default -> throw new IllegalArgumentException();
		};
	}

	private final MovingBonus movingBonus;

	/**
	 * The Ms. Pac-Man game.
	 * <p>
	 * Clyde, the orange ghost has become a transvestite and calls himself "Sue" now. Pac-Man wears woman's clothes and
	 * calls himself "Ms. Pac-Man" (pronoun she/her). The bonus probably couldn't stand all this madness anymore, became
	 * an alcoholic and tumbles through the maze, starting on a random portal, walking around the ghost house and leaving
	 * the maze at a portal on the opposite side.
	 * <p>
	 * What a freak show!
	 * <p>
	 * There are 4 different maps used by the 6 different mazes. Up to level 13, the used mazes are:
	 * <ul>
	 * <li>Maze #1: pink maze, white dots (level 1-2)
	 * <li>Maze #2: light blue maze, yellow dots (level 3-5)
	 * <li>Maze #3: orange maze, red dots (level 6-9)
	 * <li>Maze #4: dark blue maze, white dots (level 10-13)
	 * </ul>
	 * From level 14 on, the maze alternates every 4th level between maze #5 and maze #6.
	 * <ul>
	 * <li>Maze #5: pink maze, cyan dots (same map as maze #3)
	 * <li>Maze #6: orange maze, white dots (same map as maze #4)
	 * </ul>
	 */
	public MsPacManGame() {
		player = new Pac("Ms. Pac-Man");
		ghosts = new Ghost[] { //
				new Ghost(RED_GHOST, "Blinky"), //
				new Ghost(PINK_GHOST, "Pinky"), //
				new Ghost(CYAN_GHOST, "Inky"), //
				new Ghost(ORANGE_GHOST, "Sue") //
		};
		ghosts[RED_GHOST].fnChasing = this::chaseShadow;
		ghosts[PINK_GHOST].fnChasing = this::chaseSpeedy;
		ghosts[CYAN_GHOST].fnChasing = this::chaseBashful;
		ghosts[ORANGE_GHOST].fnChasing = this::chasePokey;
		movingBonus = new MovingBonus();
		hiscoreFile = new File(System.getProperty("user.home"), "highscore-ms_pacman.xml");
		setLevel(1);
	}

	@Override
	public MovingBonus bonus() {
		return movingBonus;
	}

	@Override
	public void setLevel(int n) {
		if (n < 1) {
			throw new IllegalArgumentException("Level number must be at least 1, but is: " + n);
		}
		level = new GameLevel(n, n <= data.length ? data[n - 1] : data[data.length - 1]);
		initLevel(level);
		levelCounter.add(level.bonusSymbol);
		player.starvingTimeLimit = (int) sec_to_ticks(n < 5 ? 4 : 3);
		initGhosts(level);
		ghostBounty = GameModel.FIRST_GHOST_VALUE;
	}

	private void initLevel(GameLevel level) {
		level.mazeNumber = switch (level.number) {
		case 1, 2 -> 1;
		case 3, 4, 5 -> 2;
		case 6, 7, 8, 9 -> 3;
		case 10, 11, 12, 13 -> 4;
		default -> (level.number - 14) % 8 < 4 ? 5 : 6;
		};
		level.mapNumber = switch (level.mazeNumber) {
		case 5 -> 3;
		case 6 -> 4;
		default -> level.mazeNumber;
		};
		level.world = createWorld(level.mapNumber);
		if (level.number >= 8) {
			level.bonusSymbol = new Random().nextInt(7);
		}
		level.globalDotLimits = new int[] { Integer.MAX_VALUE, 7, 17, Integer.MAX_VALUE };
		level.privateDotLimits = switch (level.number) {
		case 1 -> new int[] { 0, 0, 30, 60 };
		case 2 -> new int[] { 0, 0, 0, 50 };
		default -> new int[] { 0, 0, 0, 0 };
		};
	}

	private void initGhosts(GameLevel level) {
		ArcadeWorld world = (ArcadeWorld) level.world;
		GhostHouse house = world.ghostHouse();

		ghosts[RED_GHOST].homeTile = house.entry();
		ghosts[RED_GHOST].revivalTile = house.seatMiddle();
		ghosts[RED_GHOST].scatterTarget = world.rightUpperTarget;

		ghosts[PINK_GHOST].homeTile = ghosts[PINK_GHOST].revivalTile = house.seatMiddle();
		ghosts[PINK_GHOST].scatterTarget = world.leftUpperTarget;

		ghosts[CYAN_GHOST].homeTile = ghosts[CYAN_GHOST].revivalTile = house.seatLeft();
		ghosts[CYAN_GHOST].scatterTarget = world.rightLowerTarget;

		ghosts[ORANGE_GHOST].homeTile = ghosts[ORANGE_GHOST].revivalTile = house.seatRight();
		ghosts[ORANGE_GHOST].scatterTarget = world.leftLowerTarget;

		for (var ghost : ghosts) {
			ghost.dotCounter = 0;
			ghost.elroy = 0;
		}
	}

	@Override
	public boolean checkBonusAwarded() {
		if (level.world.eatenFoodCount() == 70 || level.world.eatenFoodCount() == 170) {
			movingBonus.activate(level.world, level.bonusSymbol, bonusValue(level.bonusSymbol), TickTimer.INDEFINITE);
			return true;
		}
		return false;
	}

	@Override
	public void updateBonus() {
		switch (movingBonus.state()) {
		case INACTIVE -> {
		}
		case EDIBLE -> {
			boolean leftWorld = movingBonus.followRoute(level.world);
			if (leftWorld) {
				log("%s expired (left level.world)", movingBonus);
				movingBonus.init();
				eventSupport.publish(GameEventType.BONUS_EXPIRES, movingBonus.tile());
				return;
			}
			if (player.tile().equals(movingBonus.tile())) {
				log("%s found bonus %s", player.name, movingBonus);
				score(movingBonus.value());
				movingBonus.eat(sec_to_ticks(2));
				eventSupport.publish(GameEventType.BONUS_GETS_EATEN, movingBonus.tile());
			}
		}
		case EATEN -> {
			boolean expired = movingBonus.tick();
			if (expired) {
				log("%s expired", movingBonus);
				movingBonus.init();
				eventSupport.publish(GameEventType.BONUS_EXPIRES, movingBonus.tile());
			}
		}
		}
	}

	@Override
	public int bonusValue(int symbol) {
		return switch (symbol) {
		case CHERRIES -> 100;
		case STRAWBERRY -> 200;
		case PEACH -> 500;
		case PRETZEL -> 700;
		case APPLE -> 1000;
		case PEAR -> 2000;
		case BANANA -> 5000;
		default -> throw new IllegalArgumentException("Unknown symbol ID: " + symbol);
		};
	}
}