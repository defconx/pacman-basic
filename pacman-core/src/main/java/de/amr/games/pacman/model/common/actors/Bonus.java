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

package de.amr.games.pacman.model.common.actors;

import de.amr.games.pacman.model.common.GameLevel;

/**
 * @author Armin Reichert
 */
public interface Bonus {

	public static byte STATE_INACTIVE = 0;
	public static byte STATE_EDIBLE = 1;
	public static byte STATE_EATEN = 2;

	/**
	 * @return Entity representing this bonus in the world.
	 */
	Entity entity();

	/**
	 * @return the symbol of this bonus.
	 */
	byte symbol();

	/**
	 * @return points earned when eating this bonus
	 */
	int points();

	/**
	 * @return state of the bonus
	 */
	byte state();

	/**
	 * Updates the bonus state.
	 * 
	 * @param level the game level
	 */
	void update(GameLevel level);

	/**
	 * Changes the bonus state to inactive.
	 */
	void setInactive();

	/**
	 * Consume the bonus.
	 */
	void eat();

	/**
	 * Changes the bonus state to edible.
	 * 
	 * @param points earned when eating this bonus
	 * @param ticks  time how long the bonus is edible
	 */
	void setEdible(long ticks);
}