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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Armin Reichert
 */
public class MoveResult {

	public boolean moved;
	public boolean tunnelEntered;
	public boolean teleported;

	private List<String> messages = new ArrayList<>();

	public MoveResult() {
		reset();
	}

	public void reset() {
		moved = false;
		tunnelEntered = false;
		teleported = false;
		messages.clear();
	}

	public void addMessage(String message) {
		messages.add(message);
	}

	public String messages() {
		return messages.stream().collect(Collectors.joining(", "));
	}

	@Override
	public String toString() {
		var sb = new StringBuilder("");
		sb.append(tunnelEntered ? " entered tunnel" : "");
		sb.append(teleported ? " teleported" : "");
		sb.append(moved ? " moved" : "");
		return sb.isEmpty() ? "" : "[" + sb.toString().trim() + "]";
	}
}