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
package de.amr.games.pacman.model.mspacman;

import static de.amr.games.pacman.lib.steering.NavigationPoint.np;
import static de.amr.games.pacman.model.common.actors.Ghost.ID_CYAN_GHOST;
import static de.amr.games.pacman.model.common.actors.Ghost.ID_ORANGE_GHOST;
import static de.amr.games.pacman.model.common.actors.Ghost.ID_PINK_GHOST;
import static de.amr.games.pacman.model.common.actors.Ghost.ID_RED_GHOST;

import java.util.ArrayList;

import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.event.GameEvents;
import de.amr.games.pacman.lib.steering.Direction;
import de.amr.games.pacman.lib.steering.NavigationPoint;
import de.amr.games.pacman.lib.timer.TickTimer;
import de.amr.games.pacman.model.common.GameLevel;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.GameVariant;
import de.amr.games.pacman.model.common.actors.Bonus;
import de.amr.games.pacman.model.common.actors.Ghost;
import de.amr.games.pacman.model.common.actors.Pac;
import de.amr.games.pacman.model.common.world.ArcadeWorld;
import de.amr.games.pacman.model.common.world.HorizontalPortal;

/**
 * The Ms. Pac-Man game.
 * <p>
 * Clyde, the orange ghost, has become a transvestite and calls himself "Sue" now (pronouns: what/thefuck). Pac-Man too
 * wears women's dresses and calls himself "Ms. Pac-Man" (pronouns: she/male). The bonus guy couldn't stand all this
 * madness anymore, became an alcoholic and tumbles through the maze.
 * <p>
 * What a freak show!
 * <p>
 * 
 * @see <a href="https://gamefaqs.gamespot.com/arcade/583976-ms-pac-man/faqs/1298">GameFAQs</a>
 * 
 * @author Armin Reichert
 */
public class MsPacManGame extends GameModel {

	/*@formatter:off*/
	public static final byte[][] MAP1 = {
		{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
		{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
		{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
		{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
		{1,3,3,3,3,3,3,1,1,3,3,3,3,3,3,3,3,3,3,1,1,3,3,3,3,3,3,1},
		{1,4,1,1,1,1,3,1,1,3,1,1,1,1,1,1,1,1,3,1,1,3,1,1,1,1,4,1},
		{1,3,1,1,1,1,3,1,1,3,1,1,1,1,1,1,1,1,3,1,1,3,1,1,1,1,3,1},
		{1,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,1},
		{1,1,1,3,1,1,3,1,1,1,1,1,3,1,1,3,1,1,1,1,1,3,1,1,3,1,1,1},
		{0,0,1,3,1,1,3,1,1,1,1,1,3,1,1,3,1,1,1,1,1,3,1,1,3,1,0,0},
		{1,1,1,3,1,1,3,1,1,1,1,1,3,1,1,3,1,1,1,1,1,3,1,1,3,1,1,1},
		{2,2,2,3,1,1,3,3,3,3,3,3,3,1,1,3,3,3,3,3,3,3,1,1,3,2,2,2},
		{1,1,1,3,1,1,1,1,1,0,1,1,1,1,1,1,1,1,0,1,1,1,1,1,3,1,1,1},
		{0,0,1,3,1,1,1,1,1,0,1,1,1,1,1,1,1,1,0,1,1,1,1,1,3,1,0,0},
		{0,0,1,3,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,3,1,0,0},
		{0,0,1,3,1,1,1,1,1,0,1,1,1,0,0,1,1,1,0,1,1,1,1,1,3,1,0,0},
		{0,0,1,3,1,1,1,1,1,0,1,0,0,0,0,0,0,1,0,1,1,1,1,1,3,1,0,0},
		{0,0,1,3,1,1,0,0,0,0,1,0,0,0,0,0,0,1,0,0,0,0,1,1,3,1,0,0},
		{0,0,1,3,1,1,0,1,1,0,1,0,0,0,0,0,0,1,0,1,1,0,1,1,3,1,0,0},
		{1,1,1,3,1,1,0,1,1,0,1,1,1,1,1,1,1,1,0,1,1,0,1,1,3,1,1,1},
		{2,2,2,3,0,0,0,1,1,0,0,0,0,0,0,0,0,0,0,1,1,0,0,0,3,2,2,2},
		{1,1,1,3,1,1,1,1,1,1,1,1,0,1,1,0,1,1,1,1,1,1,1,1,3,1,1,1},
		{0,0,1,3,1,1,1,1,1,1,1,1,0,1,1,0,1,1,1,1,1,1,1,1,3,1,0,0},
		{0,0,1,3,3,3,3,3,3,3,0,0,0,1,1,0,0,0,3,3,3,3,3,3,3,1,0,0},
		{0,0,1,3,1,1,1,1,1,3,1,1,1,1,1,1,1,1,3,1,1,1,1,1,3,1,0,0},
		{1,1,1,3,1,1,1,1,1,3,1,1,1,1,1,1,1,1,3,1,1,1,1,1,3,1,1,1},
		{1,3,3,3,3,3,3,3,3,3,3,3,3,0,0,3,3,3,3,3,3,3,3,3,3,3,3,1},
		{1,3,1,1,1,1,3,1,1,1,1,1,3,1,1,3,1,1,1,1,1,3,1,1,1,1,3,1},
		{1,3,1,1,1,1,3,1,1,1,1,1,3,1,1,3,1,1,1,1,1,3,1,1,1,1,3,1},
		{1,3,1,1,1,1,3,1,1,3,3,3,3,1,1,3,3,3,3,1,1,3,1,1,1,1,3,1},
		{1,4,1,1,1,1,3,1,1,3,1,1,1,1,1,1,1,1,3,1,1,3,1,1,1,1,4,1},
		{1,3,1,1,1,1,3,1,1,3,1,1,1,1,1,1,1,1,3,1,1,3,1,1,1,1,3,1},
		{1,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,1},
		{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
		{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
		{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
	};

	public static final byte[][] MAP2 = {
		{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
		{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
		{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
		{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
		{2,2,2,2,2,2,0,1,1,3,3,3,3,3,3,3,3,3,3,1,1,0,2,2,2,2,2,2},
		{1,1,1,1,1,1,0,1,1,3,1,1,1,1,1,1,1,1,3,1,1,0,1,1,1,1,1,1},
		{1,1,1,1,1,1,0,1,1,3,1,1,1,1,1,1,1,1,3,1,1,0,1,1,1,1,1,1},
		{1,4,3,3,3,3,3,3,3,3,3,3,3,1,1,3,3,3,3,3,3,3,3,3,3,3,4,1},
		{1,3,1,1,1,1,1,1,1,3,1,1,3,1,1,3,1,1,3,1,1,1,1,1,1,1,3,1},
		{1,3,1,1,1,1,1,1,1,3,1,1,3,1,1,3,1,1,3,1,1,1,1,1,1,1,3,1},
		{1,3,1,1,3,3,3,3,3,3,1,1,3,1,1,3,1,1,3,3,3,3,3,3,1,1,3,1},
		{1,3,1,1,3,1,1,1,1,0,1,1,3,3,3,3,1,1,0,1,1,1,1,3,1,1,3,1},
		{1,3,1,1,3,1,1,1,1,0,1,1,1,1,1,1,1,1,0,1,1,1,1,3,1,1,3,1},
		{1,3,3,3,3,3,3,1,1,0,1,1,1,1,1,1,1,1,0,1,1,3,3,3,3,3,3,1},
		{1,1,1,1,1,1,3,1,1,0,0,0,0,0,0,0,0,0,0,1,1,3,1,1,1,1,1,1},
		{1,1,1,1,1,1,3,1,1,0,1,1,1,0,0,1,1,1,0,1,1,3,1,1,1,1,1,1},
		{1,3,3,3,3,3,3,1,1,0,1,0,0,0,0,0,0,1,0,1,1,3,3,3,3,3,3,1},
		{1,3,1,1,1,1,3,1,1,0,1,0,0,0,0,0,0,1,0,1,1,3,1,1,1,1,3,1},
		{1,3,1,1,1,1,3,0,0,0,1,0,0,0,0,0,0,1,0,0,0,3,1,1,1,1,3,1},
		{1,3,3,3,1,1,3,1,1,0,1,1,1,1,1,1,1,1,0,1,1,3,1,1,3,3,3,1},
		{1,1,1,3,1,1,3,1,1,0,0,0,0,0,0,0,0,0,0,1,1,3,1,1,3,1,1,1},
		{0,0,1,3,1,1,3,1,1,1,1,0,1,1,1,1,0,1,1,1,1,3,1,1,3,1,0,0},
		{0,0,1,3,1,1,3,1,1,1,1,0,1,1,1,1,0,1,1,1,1,3,1,1,3,1,0,0},
		{0,0,1,3,3,3,3,3,3,3,3,3,1,1,1,1,3,3,3,3,3,3,3,3,3,1,0,0},
		{0,0,1,3,1,1,1,1,1,1,1,3,1,1,1,1,3,1,1,1,1,1,1,1,3,1,0,0},
		{1,1,1,3,1,1,1,1,1,1,1,3,1,1,1,1,3,1,1,1,1,1,1,1,3,1,1,1},
		{2,2,2,3,3,3,3,1,1,3,3,3,0,0,0,0,3,3,3,1,1,3,3,3,3,2,2,2},
		{1,1,1,3,1,1,3,1,1,3,1,1,1,1,1,1,1,1,3,1,1,3,1,1,3,1,1,1},
		{1,1,1,3,1,1,3,1,1,3,1,1,1,1,1,1,1,1,3,1,1,3,1,1,3,1,1,1},
		{1,4,3,3,1,1,3,3,3,3,3,3,3,1,1,3,3,3,3,3,3,3,1,1,3,3,4,1},
		{1,3,1,1,1,1,3,1,1,1,1,1,3,1,1,3,1,1,1,1,1,3,1,1,1,1,3,1},
		{1,3,1,1,1,1,3,1,1,1,1,1,3,1,1,3,1,1,1,1,1,3,1,1,1,1,3,1},
		{1,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,1},
		{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
		{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
		{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
	};

	public static final byte[][] MAP3 = {
		{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
		{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
		{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
		{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
		{1,3,3,3,3,3,3,3,3,3,1,1,3,3,3,3,1,1,3,3,3,3,3,3,3,3,3,1},
		{1,3,1,1,1,1,1,1,1,3,1,1,3,1,1,3,1,1,3,1,1,1,1,1,1,1,3,1},
		{1,4,1,1,1,1,1,1,1,3,1,1,3,1,1,3,1,1,3,1,1,1,1,1,1,1,4,1},
		{1,3,1,1,3,3,3,3,3,3,3,3,3,1,1,3,3,3,3,3,3,3,3,3,1,1,3,1},
		{1,3,1,1,3,1,1,3,1,1,1,1,3,1,1,3,1,1,1,1,3,1,1,3,1,1,3,1},
		{1,3,3,3,3,1,1,3,1,1,1,1,3,1,1,3,1,1,1,1,3,1,1,3,3,3,3,1},
		{1,1,1,1,3,1,1,3,1,1,1,1,3,1,1,3,1,1,1,1,3,1,1,3,1,1,1,1},
		{1,1,1,1,3,1,1,3,3,3,3,3,3,3,3,3,3,3,3,3,3,1,1,3,1,1,1,1},
		{2,3,3,3,3,1,1,1,1,0,1,1,1,1,1,1,1,1,0,1,1,1,1,3,3,3,3,2},
		{1,3,1,1,0,1,1,1,1,0,1,1,1,1,1,1,1,1,0,1,1,1,1,0,1,1,3,1},
		{1,3,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,3,1},
		{1,3,1,1,1,1,0,1,1,0,1,1,1,0,0,1,1,1,0,1,1,0,1,1,1,1,3,1},
		{1,3,1,1,1,1,0,1,1,0,1,0,0,0,0,0,0,1,0,1,1,0,1,1,1,1,3,1},
		{1,3,0,0,0,0,0,1,1,0,1,0,0,0,0,0,0,1,0,1,1,0,0,0,0,0,3,1},
		{1,3,1,1,0,1,1,1,1,0,1,0,0,0,0,0,0,1,0,1,1,1,1,0,1,1,3,1},
		{1,3,1,1,0,1,1,1,1,0,1,1,1,1,1,1,1,1,0,1,1,1,1,0,1,1,3,1},
		{1,3,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,3,1},
		{1,3,1,1,1,1,0,1,1,1,1,1,0,1,1,0,1,1,1,1,1,0,1,1,1,1,3,1},
		{1,3,1,1,1,1,0,1,1,1,1,1,0,1,1,0,1,1,1,1,1,0,1,1,1,1,3,1},
		{1,3,3,3,3,3,3,1,1,3,3,3,3,1,1,3,3,3,3,1,1,3,3,3,3,3,3,1},
		{1,1,1,3,1,1,3,1,1,3,1,1,1,1,1,1,1,1,3,1,1,3,1,1,3,1,1,1},
		{1,1,1,3,1,1,3,1,1,3,1,1,1,1,1,1,1,1,3,1,1,3,1,1,3,1,1,1},
		{1,4,3,3,1,1,3,3,3,3,3,3,3,0,0,3,3,3,3,3,3,3,1,1,3,3,4,1},
		{1,3,1,1,1,1,3,1,1,1,1,1,3,1,1,3,1,1,1,1,1,3,1,1,1,1,3,1},
		{1,3,1,1,1,1,3,1,1,1,1,1,3,1,1,3,1,1,1,1,1,3,1,1,1,1,3,1},
		{1,3,3,3,3,3,3,1,1,3,3,3,3,1,1,3,3,3,3,1,1,3,3,3,3,3,3,1},
		{1,3,1,1,1,1,3,1,1,3,1,1,1,1,1,1,1,1,3,1,1,3,1,1,1,1,3,1},
		{1,3,1,1,1,1,3,1,1,3,1,1,1,1,1,1,1,1,3,1,1,3,1,1,1,1,3,1},
		{1,3,3,3,3,3,3,1,1,3,3,3,3,3,3,3,3,3,3,1,1,3,3,3,3,3,3,1},
		{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
		{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
		{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
	};

	public static final byte[][] MAP4 = {
		{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
		{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
		{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
		{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
		{1,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,1},
		{1,3,1,1,3,1,1,1,1,3,1,1,1,1,1,1,1,1,3,1,1,1,1,3,1,1,3,1},
		{1,4,1,1,3,1,1,1,1,3,1,1,1,1,1,1,1,1,3,1,1,1,1,3,1,1,4,1},
		{1,3,1,1,3,1,1,1,1,3,1,1,3,3,3,3,1,1,3,1,1,1,1,3,1,1,3,1},
		{1,3,1,1,3,3,3,3,3,3,1,1,3,1,1,3,1,1,3,3,3,3,3,3,1,1,3,1},
		{1,3,1,1,1,1,3,1,1,3,1,1,3,1,1,3,1,1,3,1,1,3,1,1,1,1,3,1},
		{1,3,1,1,1,1,3,1,1,3,1,1,3,1,1,3,1,1,3,1,1,3,1,1,1,1,3,1},
		{1,3,3,3,3,3,3,1,1,3,3,3,3,1,1,3,3,3,3,1,1,3,3,3,3,3,3,1},
		{1,1,1,3,1,1,1,1,1,1,1,1,0,1,1,0,1,1,1,1,1,1,1,1,3,1,1,1},
		{0,0,1,3,1,1,1,1,1,1,1,1,0,1,1,0,1,1,1,1,1,1,1,1,3,1,0,0},
		{0,0,1,3,3,3,3,1,1,0,0,0,0,0,0,0,0,0,0,1,1,3,3,3,3,1,0,0},
		{1,1,1,0,1,1,3,1,1,0,1,1,1,0,0,1,1,1,0,1,1,3,1,1,0,1,1,1},
		{2,2,2,0,1,1,3,1,1,0,1,0,0,0,0,0,0,1,0,1,1,3,1,1,0,2,2,2},
		{1,1,1,1,1,1,3,0,0,0,1,0,0,0,0,0,0,1,0,0,0,3,1,1,1,1,1,1},
		{1,1,1,1,1,1,3,1,1,0,1,0,0,0,0,0,0,1,0,1,1,3,1,1,1,1,1,1},
		{2,2,2,0,1,1,3,1,1,0,1,1,1,1,1,1,1,1,0,1,1,3,1,1,0,2,2,2},
		{1,1,1,0,1,1,3,1,1,0,0,0,0,0,0,0,0,0,0,1,1,3,1,1,0,1,1,1},
		{0,0,1,3,3,3,3,1,1,1,1,1,0,1,1,0,1,1,1,1,1,3,3,3,3,1,0,0},
		{0,0,1,3,1,1,3,1,1,1,1,1,0,1,1,0,1,1,1,1,1,3,1,1,3,1,0,0},
		{0,0,1,3,1,1,3,3,3,3,0,0,0,1,1,0,0,0,3,3,3,3,1,1,3,1,0,0},
		{0,0,1,3,1,1,1,1,1,3,1,1,0,1,1,0,1,1,3,1,1,1,1,1,3,1,0,0},
		{1,1,1,3,1,1,1,1,1,3,1,1,0,1,1,0,1,1,3,1,1,1,1,1,3,1,1,1},
		{1,3,3,3,3,3,3,3,3,3,1,1,0,0,0,0,1,1,3,3,3,3,3,3,3,3,3,1},
		{1,3,1,1,1,1,3,1,1,3,1,1,1,1,1,1,1,1,3,1,1,3,1,1,1,1,3,1},
		{1,3,1,1,1,1,3,1,1,3,1,1,1,1,1,1,1,1,3,1,1,3,1,1,1,1,3,1},
		{1,3,1,1,3,3,3,1,1,3,3,3,3,3,3,3,3,3,3,1,1,3,3,3,1,1,3,1},
		{1,4,1,1,3,1,1,1,1,1,1,1,3,1,1,3,1,1,1,1,1,1,1,3,1,1,4,1},
		{1,3,1,1,3,1,1,1,1,1,1,1,3,1,1,3,1,1,1,1,1,1,1,3,1,1,3,1},
		{1,3,3,3,3,3,3,3,3,3,3,3,3,1,1,3,3,3,3,3,3,3,3,3,3,3,3,1},
		{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
		{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
		{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
	};
	//@formatter:on

	public static final String AK_CLAPPERBOARD = "clapperboard";

	@Override
	public Pac createPac() {
		return new Pac("Ms. Pac-Man");
	}

	@Override
	public Ghost[] createGhosts() {
		return new Ghost[] { //
				new Ghost(ID_RED_GHOST, "Blinky"), //
				new Ghost(ID_PINK_GHOST, "Pinky"), //
				new Ghost(ID_CYAN_GHOST, "Inky"), //
				new Ghost(ID_ORANGE_GHOST, "Sue") //
		};
	}

	@Override
	public ArcadeWorld createWorld(int levelNumber) {
		checkLevelNumber(levelNumber);
		int mapNumber = mapNumber(levelNumber);
		var map = switch (mapNumber) {
		case 1 -> MAP1;
		case 2 -> MAP2;
		case 3 -> MAP3;
		case 4 -> MAP4;
		default -> throw new IllegalArgumentException(
				"Illegal map number: %d. Allowed values: 1, 2, 3, 4.".formatted(mapNumber));
		};
		return new ArcadeWorld(map);
	}

	@Override
	public boolean isFirstBonusReached() {
		return level.world().eatenFoodCount() == 64; // from Ms. Pac-Man FAQ, but is this correct?
	}

	@Override
	public boolean isSecondBonusReached() {
		return level.world().uneatenFoodCount() == 66; // from Ms. Pac-Man FAQ, but is this correct?
	}

	@Override
	public Bonus createBonus(int levelNumber) {
		checkLevelNumber(levelNumber);
		int n = (levelNumber > 7) ? 1 + RND.nextInt(7) : levelNumber;
		return switch (n) {
		//@formatter:off
		case 1 -> new MovingBonus(0,  100); // Cherries
		case 2 -> new MovingBonus(1,  200); // Strawberry
		case 3 -> new MovingBonus(2,  500); // Peach
		case 4 -> new MovingBonus(3,  700); // Pretzel (A Brezn, Herr Gott Sakra!)
		case 5 -> new MovingBonus(4, 1000); // Apple
		case 6 -> new MovingBonus(5, 2000); // Pear
		case 7 -> new MovingBonus(6, 5000); // Bananas
		default -> throw new IllegalArgumentException();
		//@formatter:on
		};
	}

	/**
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
	 * <p>
	 * 
	 * @param levelNumber level number (starting at 1)
	 * @return number (starting at 1) of maze used in this level
	 */
	@Override
	public int mazeNumber(int levelNumber) {
		checkLevelNumber(levelNumber);
		return switch (levelNumber) {
		case 1, 2 -> 1;
		case 3, 4, 5 -> 2;
		case 6, 7, 8, 9 -> 3;
		case 10, 11, 12, 13 -> 4;
		default -> (levelNumber - 14) % 8 < 4 ? 5 : 6;
		};
	}

	private int mapNumber(int levelNumber) {
		return levelNumber < 14 ? mazeNumber(levelNumber) : mazeNumber(levelNumber) - 2;
	}

	@Override
	public GameVariant variant() {
		return GameVariant.MS_PACMAN;
	}

	@Override
	public int numCutScenes() {
		return 3;
	}

	@Override
	public void onBonusReached() {
		int numPortals = level.world().portals().size();
		var leftToRight = RND.nextBoolean();
		var entryPortal = (HorizontalPortal) level.world().portals().get(RND.nextInt(numPortals));
		var exitPortal = (HorizontalPortal) level.world().portals().get(RND.nextInt(numPortals));
		var startPoint = leftToRight ? np(entryPortal.leftTunnelEnd()) : np(entryPortal.rightTunnelEnd());
		var exitPoint = leftToRight ? np(exitPortal.rightTunnelEnd().plus(1, 0))
				: np(exitPortal.leftTunnelEnd().minus(1, 0));
		var houseEntry = level.world().ghostHouse().door().entryTile().minus(0, 1);
		int houseHeight = level.world().ghostHouse().sizeInTiles().y();
		var route = new ArrayList<NavigationPoint>();
		route.add(np(houseEntry));
		route.add(np(houseEntry.plus(0, houseHeight + 1)));
		route.add(np(houseEntry));
		route.add(exitPoint);
		LOG.trace("Bonus route: %s, orientation: %s", route, (leftToRight ? "left to right" : "right to left"));
		var movingBonus = (MovingBonus) level.bonus();
		movingBonus.setRoute(route);
		movingBonus.entity().placeAtTile(startPoint.tile(), 0, 0);
		movingBonus.entity().setMoveAndWishDir(leftToRight ? Direction.RIGHT : Direction.LEFT);
		movingBonus.setEdible(TickTimer.INDEFINITE);
		GameEvents.publishGameEvent(GameEventType.BONUS_GETS_ACTIVE, movingBonus.entity().tile());
	}

	/**
	 * In Ms. Pac-Man, Blinky and Pinky move randomly during the *first* hunting/scatter phase. Some say, the original
	 * intention had been to randomize the scatter target of *all* ghosts in Ms. Pac-Man but because of a bug, only the
	 * scatter target of Blinky and Pinky would have been affected. Who knows?
	 */
	@Override
	public void doGhostHuntingAction(GameLevel level, Ghost ghost) {
		if (level.huntingPhase() == 0 && (ghost.id() == Ghost.ID_RED_GHOST || ghost.id() == Ghost.ID_PINK_GHOST)) {
			ghost.roam(level); // not sure
		} else if (level.chasingPhase().isPresent() || ghost.id() == Ghost.ID_RED_GHOST && level.cruiseElroyState() > 0) {
			ghost.chase(level);
		} else {
			ghost.scatter(level);
		}
	}

	@Override
	public void enterDemoLevel() {
		level = new MsPacManGameDemoLevel(this);
		level.letsGetReadyToRumbleAndShowGuys(true);
		scoringEnabled = false;
		GameEvents.setSoundEventsEnabled(false);
		LOG.info("Ms. Pac-Man demo level entered");
	}
}