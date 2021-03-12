package de.amr.games.pacman.ui.swing.rendering.standard;

import static de.amr.games.pacman.lib.Direction.DOWN;
import static de.amr.games.pacman.lib.Direction.LEFT;
import static de.amr.games.pacman.lib.Direction.RIGHT;
import static de.amr.games.pacman.lib.Direction.UP;
import static de.amr.games.pacman.ui.swing.assets.AssetLoader.font;
import static de.amr.games.pacman.ui.swing.assets.AssetLoader.image;

import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.model.common.Ghost;
import de.amr.games.pacman.model.common.Pac;
import de.amr.games.pacman.ui.animation.Animation;
import de.amr.games.pacman.ui.swing.assets.Spritesheet;

/**
 * Assets used in Pac-Man game.
 * 
 * @author Armin Reichert
 */
public class PacMan_StandardAssets extends Spritesheet {

	/** Sprite sheet order of directions. */
	static final List<Direction> order = Arrays.asList(RIGHT, LEFT, UP, DOWN);

	private static int index(Direction dir) {
		return order.indexOf(dir);
	}

	public static Color ghostColor(int ghostType) {
		return ghostType == 0 ? Color.RED : ghostType == 1 ? Color.pink : ghostType == 2 ? Color.CYAN : Color.ORANGE;
	}

	public final Font scoreFont;

	public final BufferedImage mazeFullImage;
	public final BufferedImage mazeEmptyImage;
	public final Animation<BufferedImage> mazeFlashingAnim;
	public final BufferedImage[] symbolSprites;
	public final Map<Integer, BufferedImage> numberSprites;
	public final Map<Pac, EnumMap<Direction, Animation<BufferedImage>>> pacMunchingAnimations = new HashMap<>();
	public final Animation<BufferedImage> pacCollapsingAnim;
	public final Map<Ghost, EnumMap<Direction, Animation<BufferedImage>>> ghostsWalkingAnimsByGhost = new HashMap<>();
	public final EnumMap<Direction, Animation<BufferedImage>> ghostEyesAnimsByDir;
	public final Animation<BufferedImage> ghostBlueAnim;
	public final List<Animation<BufferedImage>> ghostFlashingAnim;
	public final Animation<Boolean> energizerBlinkingAnim;
	public final Animation<BufferedImage> bigPacManAnim;
	public final Animation<BufferedImage> blinkyHalfNaked;
	public final Animation<BufferedImage> blinkyDamaged;
	public final Animation<BufferedImage> blinkyStretched;
	public final Animation<BufferedImage> blinkyPatched;
	public final BufferedImage nailSprite;

	public PacMan_StandardAssets() {
		super(image("/pacman/graphics/sprites.png"), 16);

		scoreFont = font("/emulogic.ttf", 8);

		// Sprites and images

		mazeFullImage = image("/pacman/graphics/maze_full.png");
		mazeEmptyImage = image("/pacman/graphics/maze_empty.png");

		symbolSprites = new BufferedImage[] { sprite(2, 3), sprite(3, 3), sprite(4, 3), sprite(5, 3), sprite(6, 3),
				sprite(7, 3), sprite(8, 3), sprite(9, 3) };

		//@formatter:off
		numberSprites = new HashMap<>();
		numberSprites.put(200,  sprite(0, 8));
		numberSprites.put(400,  sprite(1, 8));
		numberSprites.put(800,  sprite(2, 8));
		numberSprites.put(1600, sprite(3, 8));
		
		numberSprites.put(100,  sprite(0, 9));
		numberSprites.put(300,  sprite(1, 9));
		numberSprites.put(500,  sprite(2, 9));
		numberSprites.put(700,  sprite(3, 9));
		
		numberSprites.put(1000, spriteRegion(4, 9, 2, 1)); // left-aligned
		numberSprites.put(2000, spriteRegion(3, 10, 3, 1));
		numberSprites.put(3000, spriteRegion(3, 11, 3, 1));
		numberSprites.put(5000, spriteRegion(3, 12, 3, 1));
		//@formatter:on

		// Animations

		BufferedImage mazeEmptyDarkImage = image("/pacman/graphics/maze_empty.png");
		BufferedImage mazeEmptyBrightImage = createBrightEffect(mazeEmptyDarkImage, new Color(33, 33, 255), Color.BLACK);
		mazeFlashingAnim = Animation.of(mazeEmptyBrightImage, mazeEmptyDarkImage).frameDuration(15);

		energizerBlinkingAnim = Animation.pulse().frameDuration(15);

		pacCollapsingAnim = Animation.of(sprite(3, 0), sprite(4, 0), sprite(5, 0), sprite(6, 0), sprite(7, 0), sprite(8, 0),
				sprite(9, 0), sprite(10, 0), sprite(11, 0), sprite(12, 0), sprite(13, 0));
		pacCollapsingAnim.frameDuration(8);

		ghostEyesAnimsByDir = new EnumMap<>(Direction.class);
		for (Direction dir : Direction.values()) {
			ghostEyesAnimsByDir.put(dir, Animation.of(sprite(8 + index(dir), 5)));
		}

		ghostBlueAnim = Animation.of(sprite(8, 4), sprite(9, 4));
		ghostBlueAnim.frameDuration(20).endless();

		ghostFlashingAnim = new ArrayList<>();
		for (int i = 0; i < 4; ++i) {
			ghostFlashingAnim.add(Animation.of(sprite(8, 4), sprite(9, 4), sprite(10, 4), sprite(11, 4)).frameDuration(4));
		}

		bigPacManAnim = Animation.of(spriteRegion(2, 1, 2, 2), spriteRegion(4, 1, 2, 2), spriteRegion(6, 1, 2, 2))
				.frameDuration(4).endless().run();

		blinkyPatched = Animation.of(sprite(10, 7), sprite(11, 7)).restart().frameDuration(4).endless();
		blinkyDamaged = Animation.of(sprite(8, 7), sprite(9, 7));
		blinkyStretched = Animation.of(sprite(9, 6), sprite(10, 6), sprite(11, 6), sprite(12, 6));
		blinkyHalfNaked = Animation.of(spriteRegion(8, 8, 2, 1), spriteRegion(10, 8, 2, 1)).endless().frameDuration(4)
				.restart();

		nailSprite = sprite(8, 6);
	}

	public EnumMap<Direction, Animation<BufferedImage>> getOrCreatePacMunchingAnimation(Pac pac) {
		if (!pacMunchingAnimations.containsKey(pac)) {
			pacMunchingAnimations.put(pac, createPacMunchingAnimation());
		}
		return pacMunchingAnimations.get(pac);
	}

	private EnumMap<Direction, Animation<BufferedImage>> createPacMunchingAnimation() {
		EnumMap<Direction, Animation<BufferedImage>> munching = new EnumMap<>(Direction.class);
		for (Direction dir : Direction.values()) {
			Animation<BufferedImage> animation = Animation.of(sprite(2, 0), sprite(1, index(dir)), sprite(0, index(dir)),
					sprite(1, index(dir)));
			animation.frameDuration(2).endless().run();
			munching.put(dir, animation);
		}
		return munching;
	}

	private EnumMap<Direction, Animation<BufferedImage>> createGhostWalkingAnimation(int ghostType) {
		EnumMap<Direction, Animation<BufferedImage>> walkingTo = new EnumMap<>(Direction.class);
		for (Direction dir : Direction.values()) {
			Animation<BufferedImage> anim = Animation.of(sprite(2 * index(dir), 4 + ghostType),
					sprite(2 * index(dir) + 1, 4 + ghostType));
			anim.frameDuration(10).endless();
			walkingTo.put(dir, anim);
		}
		return walkingTo;
	}

	public EnumMap<Direction, Animation<BufferedImage>> getOrCreateGhostsWalkingAnimation(Ghost ghost) {
		if (!ghostsWalkingAnimsByGhost.containsKey(ghost)) {
			ghostsWalkingAnimsByGhost.put(ghost, createGhostWalkingAnimation(ghost.id));
		}
		return ghostsWalkingAnimsByGhost.get(ghost);
	}

	public Font getScoreFont() {
		return scoreFont;
	}

	public BufferedImage ghostImageByGhostByDir(int ghostID, Direction dir) {
		return sprite(2 * index(dir), 4 + ghostID);
	}
}