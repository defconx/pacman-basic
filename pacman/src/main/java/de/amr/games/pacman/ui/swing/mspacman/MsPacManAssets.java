package de.amr.games.pacman.ui.swing.mspacman;

import static de.amr.games.pacman.ui.swing.PacManGameSwingUI.font;
import static de.amr.games.pacman.ui.swing.PacManGameSwingUI.image;
import static de.amr.games.pacman.ui.swing.PacManGameSwingUI.url;

import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.amr.games.pacman.lib.Animation;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.ui.sound.PacManGameSound;
import de.amr.games.pacman.ui.swing.Spritesheet;

/**
 * Sprites, animations, images etc. used in Ms. Pac-Man game.
 * 
 * @author Armin Reichert
 */
class MsPacManAssets extends Spritesheet {

	/** Sprite sheet order of directions. */
	static int index(Direction dir) {
		switch (dir) {
		case RIGHT:
			return 0;
		case LEFT:
			return 1;
		case UP:
			return 2;
		case DOWN:
			return 3;
		default:
			return -1;
		}
	}

	final BufferedImage gameLogo;
	final BufferedImage[] mazeFull;
	final BufferedImage[] mazeEmptyDark;
	final BufferedImage[] mazeEmptyBright;

	final V2i[] symbolSpriteLocations;
	final Map<Integer, V2i> bonusValueSpriteLocations;
	final Map<Integer, V2i> bountyNumberSpriteLocations;

	final List<Animation<BufferedImage>> mazesFlashing;
	final Animation<Boolean> energizerBlinking;
	final EnumMap<Direction, Animation<BufferedImage>> pacMunching;
	final Animation<BufferedImage> pacSpinning;
	final List<EnumMap<Direction, Animation<BufferedImage>>> ghostsWalking;
	final EnumMap<Direction, Animation<BufferedImage>> ghostEyes;
	final Animation<BufferedImage> ghostBlue;
	final List<Animation<BufferedImage>> ghostsFlashing;

	final Map<PacManGameSound, URL> soundMap;
	final Font scoreFont;

	public MsPacManAssets() {
		super(image("/worlds/mspacman/sprites.png"), 16);
		scoreFont = font("/PressStart2P-Regular.ttf", 8);
		gameLogo = image("/worlds/mspacman/logo.png");

		// Left part of spritesheet contains the 6 mazes, rest is on the right
		mazeFull = new BufferedImage[6];
		mazeEmptyDark = new BufferedImage[6];
		mazeEmptyBright = new BufferedImage[6];
		for (int i = 0; i < 6; ++i) {
			mazeFull[i] = subImage(0, i * 248, 226, 248);
			mazeEmptyDark[i] = subImage(226, i * 248, 226, 248);
			mazeEmptyBright[i] = createFlashEffect(mazeEmptyDark[i], getMazeWallBorderColor(i), getMazeWallColor(i));
		}

		energizerBlinking = Animation.of(true, false);
		energizerBlinking.frameDuration(10).endless();

		// Switch to right part of spritesheet
		setOrigin(456, 0);

		symbolSpriteLocations = new V2i[] { v2(3, 0), v2(4, 0), v2(5, 0), v2(6, 0), v2(7, 0), v2(8, 0), v2(9, 0) };

		//@formatter:off
		bonusValueSpriteLocations = new HashMap<>();
		bonusValueSpriteLocations.put(100,  v2(3, 1));
		bonusValueSpriteLocations.put(200,  v2(4, 1));
		bonusValueSpriteLocations.put(500,  v2(5, 1));
		bonusValueSpriteLocations.put(700,  v2(6, 1));
		bonusValueSpriteLocations.put(1000, v2(7, 1));
		bonusValueSpriteLocations.put(2000, v2(8, 1));
		bonusValueSpriteLocations.put(5000, v2(9, 1));
		
		bountyNumberSpriteLocations = new HashMap<>();
		bountyNumberSpriteLocations.put(200, v2(0,8));
		bountyNumberSpriteLocations.put(400, v2(1,8));
		bountyNumberSpriteLocations.put(800, v2(2,8));
		bountyNumberSpriteLocations.put(1600, v2(3,8));
		//@formatter:on

		mazesFlashing = new ArrayList<>(6);
		for (int mazeIndex = 0; mazeIndex < 6; ++mazeIndex) {
			Animation<BufferedImage> mazeFlashing = Animation.of(mazeEmptyBright[mazeIndex], mazeEmptyDark[mazeIndex]);
			mazeFlashing.frameDuration(15);
			mazesFlashing.add(mazeFlashing);
		}

		pacMunching = new EnumMap<>(Direction.class);
		for (Direction dir : Direction.values()) {
			int d = index(dir);
			Animation<BufferedImage> animation = Animation.of(spriteAt(0, d), spriteAt(1, d), spriteAt(2, d), spriteAt(1, d));
			animation.frameDuration(2).endless();
			pacMunching.put(dir, animation);
		}

		pacSpinning = Animation.of(spriteAt(0, 3), spriteAt(0, 0), spriteAt(0, 1), spriteAt(0, 2));
		pacSpinning.frameDuration(10).repetitions(2);

		ghostsWalking = new ArrayList<>(4);
		for (int g = 0; g < 4; ++g) {
			EnumMap<Direction, Animation<BufferedImage>> animationForDir = new EnumMap<>(Direction.class);
			for (Direction dir : Direction.values()) {
				int d = index(dir);
				Animation<BufferedImage> animation = Animation.of(spriteAt(2 * d, 4 + g), spriteAt(2 * d + 1, 4 + g));
				animation.frameDuration(4).endless();
				animationForDir.put(dir, animation);
			}
			ghostsWalking.add(animationForDir);
		}

		ghostEyes = new EnumMap<>(Direction.class);
		for (Direction dir : Direction.values()) {
			ghostEyes.put(dir, Animation.ofSingle(spriteAt(8 + index(dir), 5)));
		}

		ghostBlue = Animation.of(spriteAt(8, 4), spriteAt(9, 4));
		ghostBlue.frameDuration(20).endless().run();

		ghostsFlashing = new ArrayList<>(4);
		for (int g = 0; g < 4; ++g) {
			ghostsFlashing
					.add(Animation.of(spriteAt(8, 4), spriteAt(9, 4), spriteAt(10, 4), spriteAt(11, 4)).frameDuration(5));
		}

		//@formatter:off
		soundMap = new EnumMap<>(PacManGameSound.class);
		soundMap.put(PacManGameSound.CREDIT,           url("/sound/mspacman/Coin Credit.wav"));
		soundMap.put(PacManGameSound.EXTRA_LIFE,       url("/sound/mspacman/Extra Life.wav"));
		soundMap.put(PacManGameSound.GAME_READY,       url("/sound/mspacman/Start.wav"));
		soundMap.put(PacManGameSound.PACMAN_EAT_BONUS, url("/sound/mspacman/Fruit.wav"));
		soundMap.put(PacManGameSound.PACMAN_MUNCH,     url("/sound/mspacman/Ms. Pac Man Pill.wav"));
		soundMap.put(PacManGameSound.PACMAN_DEATH,     url("/sound/mspacman/Died.wav"));
		soundMap.put(PacManGameSound.PACMAN_POWER,     url("/sound/mspacman/Scared Ghost.wav"));
		soundMap.put(PacManGameSound.GHOST_EATEN,      url("/sound/mspacman/Ghost.wav"));
		soundMap.put(PacManGameSound.GHOST_EYES,       url("/sound/mspacman/Ghost Eyes.wav"));
		soundMap.put(PacManGameSound.GHOST_SIREN_1,    url("/sound/mspacman/Ghost Noise.wav"));
		soundMap.put(PacManGameSound.GHOST_SIREN_2,    url("/sound/mspacman/Ghost Noise 1.wav"));
		soundMap.put(PacManGameSound.GHOST_SIREN_3,    url("/sound/mspacman/Ghost Noise 2.wav"));
		soundMap.put(PacManGameSound.GHOST_SIREN_4,    url("/sound/mspacman/Ghost Noise 3.wav"));
		soundMap.put(PacManGameSound.GHOST_SIREN_5,    url("/sound/mspacman/Ghost Noise 4.wav"));
		//@formatter:on
	}

	/**
	 * Note: maze numbers are 1-based, maze index as stored here is 0-based.
	 * 
	 * @param mazeIndex
	 * @return
	 */
	public Color getMazeWallColor(int mazeIndex) {
		switch (mazeIndex) {
		case 0:
			return new Color(255, 183, 174);
		case 1:
			return new Color(71, 183, 255);
		case 2:
			return new Color(222, 151, 81);
		case 3:
			return new Color(33, 33, 255);
		case 4:
			return new Color(255, 183, 255);
		case 5:
			return new Color(255, 183, 174);
		default:
			return Color.WHITE;
		}
	}

	/**
	 * Note: maze numbers are 1-based, maze index as stored here is 0-based.
	 * 
	 * @param mazeIndex
	 * @return
	 */
	public Color getMazeWallBorderColor(int mazeIndex) {
		switch (mazeIndex) {
		case 0:
			return new Color(255, 0, 0);
		case 1:
			return new Color(222, 222, 255);
		case 2:
			return new Color(222, 222, 255);
		case 3:
			return new Color(255, 183, 81);
		case 4:
			return new Color(255, 255, 0);
		case 5:
			return new Color(255, 0, 0);
		default:
			return Color.WHITE;
		}
	}

	public Font getScoreFont() {
		return scoreFont;
	}

}