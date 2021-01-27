package de.amr.games.pacman.ui.swing.mspacman;

import static de.amr.games.pacman.game.heaven.God.clock;
import static de.amr.games.pacman.game.worlds.PacManGameWorld.HTS;
import static de.amr.games.pacman.game.worlds.PacManGameWorld.TS;
import static de.amr.games.pacman.game.worlds.PacManGameWorld.t;
import static de.amr.games.pacman.ui.swing.classic.PacManClassicAssets.DIR;
import static java.util.stream.IntStream.range;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

import de.amr.games.pacman.game.core.PacManGameModel;
import de.amr.games.pacman.game.core.PacManGameState;
import de.amr.games.pacman.game.creatures.Creature;
import de.amr.games.pacman.game.creatures.Ghost;
import de.amr.games.pacman.game.creatures.GhostState;
import de.amr.games.pacman.game.creatures.Pac;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.ui.swing.Animation;
import de.amr.games.pacman.ui.swing.PacManGameSwingUI;
import de.amr.games.pacman.ui.swing.scene.PacManGamePlayScene;

/**
 * Scene where the game is played.
 * 
 * @author Armin Reichert
 */
public class MsPacManPlayScene extends PacManGamePlayScene {

	private final MsPacManAssets assets;
	private final EnumMap<Direction, BufferedImage> pacFull;
	private final Animation pacCollapsing;
	private final EnumMap<Direction, Animation> pacWalking;
	private final List<EnumMap<Direction, Animation>> ghostsWalking;
	private final EnumMap<Direction, BufferedImage> ghostEyes;
	private final Animation ghostBlue;
	private final Animation ghostFlashing;

	public MsPacManPlayScene(PacManGameSwingUI ui, PacManGameModel game, V2i size, MsPacManAssets assets) {
		super(ui, game, size);
		this.assets = assets;

		pacFull = new EnumMap<>(Direction.class);
		for (Direction direction : Direction.values()) {
			int dir = MsPacManAssets.DIR.get(direction);
			pacFull.put(direction, assets.section(2, dir));
		}

		pacCollapsing = new Animation(10);
		for (int i = 0; i < 2; ++i) {
			pacCollapsing.addFrame(assets.section(0, 3));
			pacCollapsing.addFrame(assets.section(0, 0));
			pacCollapsing.addFrame(assets.section(0, 1));
			pacCollapsing.addFrame(assets.section(0, 2));
		}

		pacWalking = new EnumMap<>(Direction.class);
		for (Direction direction : Direction.values()) {
			int dir = MsPacManAssets.DIR.get(direction);
			Animation animation = new Animation(4);
			animation.setLoop(true);
			animation.start();
			animation.addFrame(assets.section(0, dir));
			animation.addFrame(assets.section(1, dir));
			animation.addFrame(assets.section(2, dir));
			animation.addFrame(assets.section(1, dir));
			pacWalking.put(direction, animation);
		}

		ghostsWalking = new ArrayList<>();
		for (int ghostID = 0; ghostID < 4; ++ghostID) {
			EnumMap<Direction, Animation> animationForDir = new EnumMap<>(Direction.class);
			for (Direction direction : Direction.values()) {
				int dir = DIR.get(direction);
				Animation animation = new Animation(10);
				animation.setLoop(true);
				animation.start();
				animation.addFrame(assets.section(2 * dir, 4 + ghostID));
				animation.addFrame(assets.section(2 * dir + 1, 4 + ghostID));
				animationForDir.put(direction, animation);
			}
			ghostsWalking.add(animationForDir);
		}

		ghostEyes = new EnumMap<>(Direction.class);
		for (Direction direction : Direction.values()) {
			int dir = DIR.get(direction);
			ghostEyes.put(direction, assets.section(8 + dir, 5));
		}

		ghostBlue = new Animation(20);
		ghostBlue.setLoop(true);
		ghostBlue.start();
		ghostBlue.addFrame(assets.section(8, 4));
		ghostBlue.addFrame(assets.section(9, 4));

		ghostFlashing = new Animation(15);
		ghostFlashing.setLoop(true);
		ghostFlashing.start();
		ghostFlashing.addFrame(assets.section(8, 4));
		ghostFlashing.addFrame(assets.section(9, 4));
		ghostFlashing.addFrame(assets.section(10, 4));
		ghostFlashing.addFrame(assets.section(11, 4));

	}

	@Override
	public void startPacManCollapsing() {
		pacCollapsing.reset();
		pacCollapsing.start();
	}

	@Override
	public void endPacManCollapsing() {
		pacCollapsing.stop();
		pacCollapsing.reset();
	}

	@Override
	public void draw(Graphics2D g) {
		drawScore(g);
		drawLivesCounter(g);
		drawLevelCounter(g);
		drawMaze(g);
		drawGuy(g, game.pac, sprite(game.pac));
		for (Ghost ghost : game.ghosts) {
			drawGuy(g, ghost, sprite(ghost));
		}
		drawDebugInfo(g);
	}

	private void drawGuy(Graphics2D g, Creature guy, BufferedImage sprite) {
		if (guy.visible) {
			int dx = (sprite.getWidth() - TS) / 2, dy = (sprite.getHeight() - TS) / 2;
			g.drawImage(sprite, (int) (guy.position.x) - dx, (int) (guy.position.y) - dy, null);
		}
	}

	private Color getScoreColor() {
		switch (game.level.mazeNumber) {
		case 1:
			return new Color(255, 183, 174);
		case 2:
			return new Color(71, 183, 255);
		case 3:
			return new Color(222, 151, 81);
		case 4:
			return new Color(33, 33, 255);
		case 5:
			return new Color(255, 183, 255);
		case 6:
			return new Color(255, 183, 174);
		default:
			return Color.WHITE;
		}
	}

	private void drawScore(Graphics2D g) {
		g.setFont(assets.scoreFont);
		g.translate(0, 2);
		g.setColor(Color.WHITE);
		g.drawString(ui.translation("SCORE"), t(1), t(1));
		g.drawString(ui.translation("HI_SCORE"), t(16), t(1));
		g.translate(0, 1);
		g.setColor(getScoreColor());
		g.drawString(String.format("%08d", game.score), t(1), t(2));
		g.setColor(Color.LIGHT_GRAY);
		g.drawString(String.format("L%02d", game.levelNumber), t(9), t(2));
		g.setColor(getScoreColor());
		g.drawString(String.format("%08d", game.highscorePoints), t(16), t(2));
		g.setColor(Color.LIGHT_GRAY);
		g.drawString(String.format("L%02d", game.highscoreLevel), t(24), t(2));
		g.translate(0, -3);
	}

	private void drawLivesCounter(Graphics2D g) {
		int maxLives = 5;
		int displayedLives = game.lives;
		int y = size.y - t(2);
		for (int i = 0; i < Math.min(displayedLives, maxLives); ++i) {
			g.drawImage(assets.life, t(2 * (i + 1)), y, null);
		}
		if (game.lives > maxLives) {
			g.setColor(Color.YELLOW);
			g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 6));
			g.drawString("+" + (game.lives - maxLives), t(12) - 4, y + t(2));
		}
	}

	private void drawLevelCounter(Graphics2D g) {
		int x = t(game.world.xTiles() - 4);
		for (int levelNumber = 1; levelNumber <= Math.min(game.levelNumber, 7); ++levelNumber) {
			BufferedImage symbol = assets.symbols[game.levelSymbols.get(levelNumber - 1)];
			g.drawImage(symbol, x, size.y - t(2), null);
			x -= t(2);
		}
	}

	private void hideFood(Graphics2D g, int x, int y) {
		g.setColor(Color.BLACK);
		g.fillRect(t(x), t(y), TS, TS);
	}

	private void drawMaze(Graphics2D g) {
		if (game.mazeFlashesRemaining > 0) {
			clock.runAlternating(clock.sec(0.25), () -> {
				g.drawImage(assets.mazeEmptyDark[game.level.mazeNumber - 1], 0, t(3), null);
			}, () -> {
//				g.drawImage(assets.mazeEmptyBright[mazeIndex], 0, t(3), null);
			}, () -> {
				game.mazeFlashesRemaining--;
			});
			return;
		}
		g.drawImage(assets.mazeFull[game.level.mazeNumber - 1], 0, t(3), null);
		range(0, game.world.xTiles()).forEach(x -> {
			range(4, game.world.yTiles() - 3).forEach(y -> {
				if (game.level.isFoodRemoved(x, y)) {
					hideFood(g, x, y);
				} else if (game.state == PacManGameState.HUNTING && game.world.isEnergizerTile(x, y)) {
					clock.runOrBeIdle(10, () -> hideFood(g, x, y));
				}
			});
		});
		drawBonus(g);
		if (PacManGameSwingUI.debugMode) {
			drawMazeStructure(g);
		}
	}

	private static final int BONUS_JUMP[] = { -2, 0, 2 };

	private void drawBonus(Graphics2D g) {
		int x = (int) (game.bonus.position.x) - HTS;
		int y = (int) (game.bonus.position.y) - HTS;
		if (game.bonus.edibleTicksLeft > 0) {
			int frame = clock.frame(20, BONUS_JUMP.length);
			if (game.bonus.dir == Direction.LEFT || game.bonus.dir == Direction.RIGHT) {
				y += BONUS_JUMP[frame]; // TODO this is not yet correct
			}
			g.drawImage(assets.symbols[game.bonus.symbol], x, y, null);
		} else if (game.bonus.eatenTicksLeft > 0) {
			g.drawImage(assets.numbers.get(game.bonus.points), x, y, null);
		}
	}

	private BufferedImage sprite(Pac pac) {
		int dir = MsPacManAssets.DIR.get(pac.dir);
		if (pac.dead) {
			return pacCollapsing.isRunning() ? pacCollapsing.frame() : assets.section(1, dir);
		}
		if (pac.speed == 0 || !pac.couldMove) {
			return assets.section(1, dir);
		}
		return pacWalking.get(pac.dir).frame();
	}

	private BufferedImage sprite(Ghost ghost) {
		if (ghost.bounty > 0) {
			return assets.bountyNumbers.get(ghost.bounty);
		}
		if (ghost.state == GhostState.DEAD || ghost.state == GhostState.ENTERING_HOUSE) {
			return ghostEyes.get(ghost.wishDir);
		}
		if (ghost.state == GhostState.FRIGHTENED) {
			if (game.pac.powerTicksLeft <= 20 * game.level.numFlashes && game.state == PacManGameState.HUNTING) {
				return ghostFlashing.frame();
			}
			return ghostBlue.frame();
		}
		if (ghost.state == GhostState.LOCKED && game.pac.powerTicksLeft > 0) {
			return ghostBlue.frame();
		}
		return ghostsWalking.get(ghost.id).get(ghost.wishDir).frame();
	}
}