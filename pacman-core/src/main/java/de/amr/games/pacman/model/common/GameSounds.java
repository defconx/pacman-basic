/*
MIT License

Copyright (c) 2022 Armin Reichert

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

package de.amr.games.pacman.model.common;

/**
 * @author Armin Reichert
 */
public interface GameSounds {

	public static final int FOREVER = -1;

	default void play(GameSound snd) {
	}

	default void ensurePlaying(GameSound snd) {
	}

	default void loop(GameSound snd, int repetitions) {
	}

	default void setSilent(boolean silent) {
	}

	default boolean isMuted() {
		return false;
	}

	default void setMuted(boolean muted) {
	}

	default void stopAll() {
	}

	default void stop(GameSound snd) {
	}

	default void ensureLoop(GameSound snd, int repetitions) {
	}

	default void stopSirens() {
	}

	default void ensureSirenStarted(int siren) {
	}
}