/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model.actors;

/**
 * @author Armin Reichert
 */
public interface Animations<A, S> {

	A selectedAnimation();

	String selectedAnimationName();

	void select(String name, Object... args);

	void startSelected();

	void stopSelected();

	void resetSelected();

	S currentSprite();
}