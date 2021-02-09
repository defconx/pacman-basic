package de.amr.games.pacman.ui.swing.pacman.rendering;

import static de.amr.games.pacman.ui.swing.AssetLoader.font;
import static de.amr.games.pacman.ui.swing.AssetLoader.image;
import static de.amr.games.pacman.ui.swing.AssetLoader.url;

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
 * Assets used in Pac-Man game.
 * 
 * <p>
 * Just for testing, some animations or maps just store the sprite coordinates and the subimage gets
 * created every time the animation frame or image is rendered. This does not really make sense if
 * the subimage object has to be created anyway but could be useful if there was a way to draw the
 * corresponding section from the spritesheet image without having to create a subimage object.
 * 
 * @author Armin Reichert
 */
public class PacManGameAssets extends Spritesheet {

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

	final Color[] ghostColors = { Color.RED, Color.PINK, Color.CYAN, Color.ORANGE };

	final BufferedImage mazeFull;
	final BufferedImage mazeEmpty;
	final V2i[] symbolSpriteLocation;
	final Map<Integer, BufferedImage> numbers;

	final EnumMap<Direction, Animation<BufferedImage>> pacMunching;
	final Animation<BufferedImage> pacCollapsing;
	final List<EnumMap<Direction, Animation<BufferedImage>>> ghostsWalking;
	final EnumMap<Direction, Animation<BufferedImage>> ghostEyes;
	final Animation<BufferedImage> ghostBlue;
	final Animation<BufferedImage> ghostFlashing;
	final Animation<BufferedImage> mazeFlashing;
	final Animation<Boolean> energizerBlinking;

	final Map<PacManGameSound, URL> soundMap;
	final Font scoreFont;

	public PacManGameAssets() {
		super(image("/pacman/graphics/sprites.png"), 16);

		scoreFont = font("/emulogic.ttf", 8);

		mazeFull = image("/pacman/graphics/maze_full.png");
		mazeEmpty = image("/pacman/graphics/maze_empty.png");

		symbolSpriteLocation = new V2i[] { v2(2, 3), v2(3, 3), v2(4, 3), v2(5, 3), v2(6, 3), v2(7, 3), v2(8, 3), v2(9, 3) };

		//@formatter:off
		numbers = new HashMap<>();
		numbers.put(200,  spriteAt(0, 8));
		numbers.put(400,  spriteAt(1, 8));
		numbers.put(800,  spriteAt(2, 8));
		numbers.put(1600, spriteAt(3, 8));
		
		numbers.put(100,  spriteAt(0, 9));
		numbers.put(300,  spriteAt(1, 9));
		numbers.put(500,  spriteAt(2, 9));
		numbers.put(700,  spriteAt(3, 9));
		
		numbers.put(1000, spritesAt(4, 9, 2, 1)); // left-aligned 
		numbers.put(2000, spritesAt(3, 10, 3, 1));
		numbers.put(3000, spritesAt(3, 11, 3, 1));
		numbers.put(5000, spritesAt(3, 12, 3, 1));
		//@formatter:on

		// Animations

		BufferedImage mazeEmptyDark = image("/pacman/graphics/maze_empty.png");
		BufferedImage mazeEmptyBright = createBrightEffect(mazeEmptyDark, new Color(33, 33, 255), Color.BLACK);
		mazeFlashing = Animation.of(mazeEmptyBright, mazeEmptyDark).frameDuration(15);

		energizerBlinking = Animation.pulse().frameDuration(15);

		pacMunching = new EnumMap<>(Direction.class);
		for (Direction dir : Direction.values()) {
			Animation<BufferedImage> animation = Animation.of(spriteAt(2, 0), spriteAt(1, index(dir)),
					spriteAt(0, index(dir)), spriteAt(1, index(dir)));
			animation.frameDuration(2).endless().run();
			pacMunching.put(dir, animation);
		}

		pacCollapsing = Animation.of(spriteAt(3, 0), spriteAt(4, 0), spriteAt(5, 0), spriteAt(6, 0), spriteAt(7, 0),
				spriteAt(8, 0), spriteAt(9, 0), spriteAt(10, 0), spriteAt(11, 0), spriteAt(12, 0), spriteAt(13, 0));
		pacCollapsing.frameDuration(8);

		ghostsWalking = new ArrayList<>(4);
		for (int g = 0; g < 4; ++g) {
			EnumMap<Direction, Animation<BufferedImage>> walkingTo = new EnumMap<>(Direction.class);
			for (Direction dir : Direction.values()) {
				Animation<BufferedImage> animation = Animation.of(spriteAt(2 * index(dir), 4 + g),
						spriteAt(2 * index(dir) + 1, 4 + g));
				animation.frameDuration(10).endless();
				walkingTo.put(dir, animation);
			}
			ghostsWalking.add(walkingTo);
		}

		ghostEyes = new EnumMap<>(Direction.class);
		for (Direction dir : Direction.values()) {
			ghostEyes.put(dir, Animation.ofSingle(spriteAt(8 + index(dir), 5)));
		}

		ghostBlue = Animation.of(spriteAt(8, 4), spriteAt(9, 4));
		ghostBlue.frameDuration(20).endless();

		ghostFlashing = Animation.of(spriteAt(8, 4), spriteAt(9, 4), spriteAt(10, 4), spriteAt(11, 4));
		ghostFlashing.frameDuration(5).endless();

		//@formatter:off
		soundMap = new EnumMap<>(PacManGameSound.class);
		soundMap.put(PacManGameSound.CREDIT,           url("/pacman/sound/credit.wav"));
		soundMap.put(PacManGameSound.EXTRA_LIFE,       url("/pacman/sound/extend.wav"));
		soundMap.put(PacManGameSound.GAME_READY,       url("/pacman/sound/game_start.wav"));
		soundMap.put(PacManGameSound.PACMAN_EAT_BONUS, url("/pacman/sound/eat_fruit.wav"));
		soundMap.put(PacManGameSound.PACMAN_MUNCH,     url("/pacman/sound/munch_1.wav"));
		soundMap.put(PacManGameSound.PACMAN_DEATH,     url("/pacman/sound/death_1.wav"));
		soundMap.put(PacManGameSound.PACMAN_POWER,     url("/pacman/sound/power_pellet.wav"));
		soundMap.put(PacManGameSound.GHOST_EATEN,      url("/pacman/sound/eat_ghost.wav"));
		soundMap.put(PacManGameSound.GHOST_EYES,       url("/pacman/sound/retreating.wav"));
		soundMap.put(PacManGameSound.GHOST_SIREN_1,    url("/pacman/sound/siren_1.wav"));
		soundMap.put(PacManGameSound.GHOST_SIREN_2,    url("/pacman/sound/siren_2.wav"));
		soundMap.put(PacManGameSound.GHOST_SIREN_3,    url("/pacman/sound/siren_3.wav"));
		soundMap.put(PacManGameSound.GHOST_SIREN_4,    url("/pacman/sound/siren_4.wav"));
		soundMap.put(PacManGameSound.GHOST_SIREN_5,    url("/pacman/sound/siren_5.wav"));
		soundMap.put(PacManGameSound.INTERMISSION_1,   url("/pacman/sound/intermission.wav"));
		soundMap.put(PacManGameSound.INTERMISSION_2,   url("/pacman/sound/intermission.wav"));
		soundMap.put(PacManGameSound.INTERMISSION_3,   url("/pacman/sound/intermission.wav"));
		//@formatter:on
	}

	public Font getScoreFont() {
		return scoreFont;
	}

	public BufferedImage ghostImage(int g, Direction dir) {
		return spriteAt(2 * index(dir), 4 + g);
	}

	public Color ghostColor(int g) {
		return ghostColors[g];
	}

	public URL getSoundURL(PacManGameSound sound) {
		return soundMap.get(sound);
	}
}