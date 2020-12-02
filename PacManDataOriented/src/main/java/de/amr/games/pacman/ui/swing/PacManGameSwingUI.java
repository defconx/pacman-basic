package de.amr.games.pacman.ui.swing;

import static de.amr.games.pacman.PacManGame.level;
import static de.amr.games.pacman.World.HTS;
import static de.amr.games.pacman.World.TS;
import static de.amr.games.pacman.World.WORLD_HEIGHT_TILES;
import static de.amr.games.pacman.World.WORLD_WIDTH_TILES;

import java.awt.BasicStroke;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.util.Map;

import javax.swing.JFrame;

import de.amr.games.pacman.GameState;
import de.amr.games.pacman.PacManGame;
import de.amr.games.pacman.common.Direction;
import de.amr.games.pacman.entities.Creature;
import de.amr.games.pacman.entities.Ghost;
import de.amr.games.pacman.ui.PacManGameUI;

/**
 * Swing UI for Pac-Man game.
 * 
 * @author Armin Reichert
 */
public class PacManGameSwingUI extends JFrame implements PacManGameUI {

	//@formatter:off
	private static final Map<Direction,Integer> DIR_INDEX = Map.of(
		Direction.RIGHT, 0,
		Direction.LEFT,  1,
		Direction.UP,    2,
		Direction.DOWN,  3
	);
	
	private static final Polygon TRIANGLE = new Polygon(
		new int[] { -4, 4, 0 }, 
		new int[] { 0, 0, 4 },
		3);
	
	private static final Map<String, Color> GHOST_COLORS = Map.of(
		"Blinky", Color.RED, 
		"Pinky",  Color.PINK, 
		"Inky",	  Color.CYAN, 
		"Clyde",  Color.ORANGE
		);
	//@formatter:on

	private final Assets assets;
	private final PacManGame game;
	private final float scaling;
	private final Canvas canvas;
	private final Keyboard keyboard;

	private boolean debugMode;
	private String messageText;
	private Color messageColor;

	public PacManGameSwingUI(PacManGame game, float scaling) {
		this.game = game;
		this.scaling = scaling;
		messageText = null;
		messageColor = Color.YELLOW;
		assets = new Assets();
		keyboard = new Keyboard(this);
		canvas = new Canvas();
		canvas.setSize((int) (WORLD_WIDTH_TILES * TS * scaling), (int) (WORLD_HEIGHT_TILES * TS * scaling));
		canvas.setFocusable(false);
		add(canvas);
		setTitle("Pac-Man");
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				onExit();
			}
		});
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
		// these must called be *after* setVisible():
		requestFocus();
		canvas.createBufferStrategy(2);
	}

	@Override
	public boolean isDebugMode() {
		return debugMode;
	}

	@Override
	public void setDebugMode(boolean debug) {
		debugMode = debug;
	}

	@Override
	public void render() {
		BufferStrategy buffers = canvas.getBufferStrategy();
		do {
			do {
				Graphics2D g = (Graphics2D) buffers.getDrawGraphics();
				g.setColor(Color.BLACK);
				g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
				g.scale(scaling, scaling);
				drawGame(g);
				g.dispose();
			} while (buffers.contentsRestored());
			buffers.show();
		} while (buffers.contentsLost());
	}

	@Override
	public void redMessage(String text) {
		messageText = text;
		messageColor = Color.RED;
	}

	@Override
	public void yellowMessage(String text) {
		messageText = text;
		messageColor = Color.YELLOW;
	}

	@Override
	public void clearMessage() {
		messageText = null;
	}

	@Override
	public boolean keyPressed(String keySpec) {
		boolean pressed = keyboard.keyPressed(keySpec);
		keyboard.clearKey(keySpec);
		return pressed;
	}

	@Override
	public void onExit() {
		game.exit();
	}

	private void drawGame(Graphics2D g) {
		drawScore(g);
		drawLivesCounter(g);
		drawLevelCounter(g);
		drawMaze(g);
		drawPacMan(g);
		for (int i = 0; i < 4; ++i) {
			drawGhost(g, i);
		}
		drawDebugInfo(g);
		g.setColor(Color.LIGHT_GRAY);
		g.setFont(new Font("Arial", Font.PLAIN, 6));
		g.drawString(String.format("%d fps", game.clock.fps), 1 * TS, 3 * TS);
		if (game.paused) {
			g.setColor(Color.WHITE);
			g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 16));
			int textWidth = g.getFontMetrics().stringWidth("PAUSED");
			g.drawString("PAUSED", WORLD_WIDTH_TILES * TS / 2 - textWidth / 2, 10 * TS);
		}
	}

	private void drawDebugInfo(Graphics2D g) {
		if (debugMode) {
			g.setColor(Color.WHITE);
			g.setFont(new Font("Arial", Font.PLAIN, 6));
			boolean chasing = game.huntingPhase % 2 != 0;
			int step = game.huntingPhase / 2;
			String hunting = game.state == GameState.HUNTING ? (chasing ? "chasing #" : "scattering #") + step : "";
			String remaining = game.state.ticksRemaining() == Long.MAX_VALUE ? "forever"
					: game.state.ticksRemaining() + " ticks remaining";
			String text = String.format("%s %s %s", game.state, hunting, remaining);
			g.drawString(text, 8 * TS, 3 * TS);
			for (Ghost ghost : game.ghosts) {
				if (ghost.targetTile != null) {
					g.setColor(GHOST_COLORS.get(ghost.name));
					g.fillRect(ghost.targetTile.x * TS + HTS / 2, ghost.targetTile.y * TS + HTS / 2, HTS, HTS);
				}
			}
		}
	}

	private void drawScore(Graphics2D g) {
		g.setFont(assets.scoreFont);
		g.setColor(Color.WHITE);
		g.translate(0, 2);
		g.drawString("SCORE", 1 * TS, 1 * TS);
		g.drawString("HI SCORE", 11 * TS, 1 * TS);
		g.translate(0, 1);
		g.setColor(Color.YELLOW);
		g.drawString(String.format("%08d", game.points), 1 * TS, 2 * TS);
		g.drawString(String.format("%08d", game.hiscore), 11 * TS, 2 * TS);
		g.setColor(Color.LIGHT_GRAY);
		g.drawString(String.format("L%02d", game.hiscoreLevel), 19 * TS, 2 * TS);
		g.setColor(Color.YELLOW);
		g.drawString(String.format("L%02d", game.level), 24 * TS, 2 * TS);
		g.translate(0, -3);
	}

	private void drawLivesCounter(Graphics2D g) {
		for (int i = 0; i < game.lives; ++i) {
			g.drawImage(assets.imageLive, 2 * (i + 1) * TS, (WORLD_HEIGHT_TILES - 2) * TS, null);
		}
	}

	private void drawLevelCounter(Graphics2D g) {
		int x = (WORLD_WIDTH_TILES - 4) * TS;
		int first = Math.max(1, game.level - 6);
		for (int level = first; level <= game.level; ++level) {
			BufferedImage symbol = assets.symbols.get(level(level).bonusSymbol);
			g.drawImage(symbol, x, (WORLD_HEIGHT_TILES - 2) * TS, null);
			x -= 2 * TS;
		}
	}

	private void hideTile(Graphics2D g, int x, int y) {
		g.setColor(Color.BLACK);
		g.fillRect(x * TS, y * TS, TS, TS);
	}

	private void drawMazeFlashing(Graphics2D g) {
		if (game.mazeFlashesRemaining > 0 && game.clock.framesTotal % 30 < 15) {
			g.drawImage(assets.imageMazeEmptyWhite, 0, 3 * TS, null);
			if (game.clock.framesTotal % 30 == 14) {
				--game.mazeFlashesRemaining;
			}
		} else {
			g.drawImage(assets.imageMazeEmpty, 0, 3 * TS, null);
		}
	}

	private void drawMaze(Graphics2D g) {
		if (game.state == GameState.CHANGING_LEVEL && game.state.ticksRemaining() <= game.clock.sec(4f)) {
			drawMazeFlashing(g);
			return;
		}
		g.drawImage(assets.imageMazeFull, 0, 3 * TS, null);
		for (int x = 0; x < WORLD_WIDTH_TILES; ++x) {
			for (int y = 0; y < WORLD_HEIGHT_TILES; ++y) {
				if (game.world.hasEatenFood(x, y)) {
					hideTile(g, x, y);
					continue;
				}
				// energizer blinking
				if (game.world.isEnergizerTile(x, y) && game.clock.framesTotal % 20 < 10 && (game.state == GameState.HUNTING)) {
					hideTile(g, x, y);
				}
			}
		}
		if (game.bonusAvailableTimer > 0) {
			String symbolName = game.level().bonusSymbol;
			g.drawImage(assets.symbols.get(symbolName), 13 * TS, 20 * TS - HTS, null);
		}
		if (game.bonusConsumedTimer > 0) {
			int number = game.level().bonusPoints;
			BufferedImage image = assets.numbers.get(number);
			if (number < 2000) {
				g.drawImage(image, 13 * TS, 20 * TS - HTS, null);
			} else {
				g.drawImage(image, (WORLD_WIDTH_TILES * TS - image.getWidth()) / 2, 20 * TS - HTS, null);
			}
		}
		if (messageText != null) {
			g.setFont(assets.scoreFont);
			g.setColor(messageColor);
			int textWidth = g.getFontMetrics().stringWidth(messageText);
			g.drawString(messageText, WORLD_WIDTH_TILES * TS / 2 - textWidth / 2, 21 * TS);
		}

		if (debugMode) {
			for (int x = 0; x < WORLD_WIDTH_TILES; ++x) {
				for (int y = 0; y < WORLD_HEIGHT_TILES; ++y) {
					if (game.world.isIntersectionTile(x, y)) {
						g.setColor(new Color(100, 100, 100));
						g.setStroke(new BasicStroke(0.1f));
						g.drawLine(x * TS, y * TS + HTS, (x + 1) * TS, y * TS + HTS);
						g.drawLine(x * TS + HTS, y * TS, x * TS + HTS, (y + 1) * TS);
					} else if (game.world.isUpwardsBlocked(x, y)) {
						g.setColor(new Color(100, 100, 100));
						g.setStroke(new BasicStroke(0.1f));
						g.translate(x * TS + HTS, y * TS);
						g.fillPolygon(TRIANGLE);
						g.translate(-(x * TS + HTS), -y * TS);
					}
				}
			}
		}
	}

	private void drawPacMan(Graphics2D g) {
		Creature pacMan = game.pacMan;
		if (!pacMan.visible) {
			return;
		}
		BufferedImage sprite;
		int mouthFrame = (int) game.clock.framesTotal % 15 / 5;
		if (game.state == GameState.PACMAN_DYING) {
			if (game.state.ticksRemaining() >= game.clock.sec(2) + 11 * 8) {
				// for 2 seconds, show full sprite before animation starts
				sprite = assets.section(2, 0);
			} else if (game.state.ticksRemaining() >= game.clock.sec(2)) {
				// run collapsing animation
				int frame = (int) (game.state.ticksRemaining() - game.clock.sec(2)) / 8;
				sprite = assets.section(13 - frame, 0);
			} else {
				// show collapsed sprite after collapsing
				sprite = assets.section(13, 0);
			}
		} else if (game.state == GameState.READY || game.state == GameState.CHANGING_LEVEL
				|| game.state == GameState.GAME_OVER) {
			// show full sprite
			sprite = assets.section(2, 0);
		} else if (!pacMan.couldMove) {
			// show mouth wide open
			sprite = assets.section(0, DIR_INDEX.get(pacMan.dir));
		} else {
			// switch between mouth closed and mouth open
			sprite = mouthFrame == 2 ? assets.section(mouthFrame, 0) : assets.section(mouthFrame, DIR_INDEX.get(pacMan.dir));
		}
		g.drawImage(sprite, (int) pacMan.position.x - HTS, (int) pacMan.position.y - HTS, null);
	}

	private void drawGhost(Graphics2D g, int ghostIndex) {
		BufferedImage sprite;
		Ghost ghost = game.ghosts[ghostIndex];
		if (!ghost.visible) {
			return;
		}

		if (ghost.dead) {
			// show as number (bounty) or as eyes
			sprite = ghost.bounty > 0 ? assets.bountyNumbers.get(ghost.bounty)
					: assets.section(8 + DIR_INDEX.get(ghost.dir), 5);
		} else if (ghost.frightened) {
			int walkingFrame = game.clock.framesTotal % 60 < 30 ? 0 : 1;
			if (game.pacManPowerTimer < game.clock.sec(2)) { // TODO flash exactly n times
				// flashing blue/white, walking
				int flashingFrame = game.clock.framesTotal % 20 < 10 ? 8 : 10;
				sprite = assets.section(flashingFrame + walkingFrame, 4);
			} else {
				// blue, walking
				sprite = assets.section(8 + walkingFrame, 4);
			}
		} else {
			int walkingFrame = game.clock.framesTotal % 60 < 30 ? 0 : 1;
			sprite = assets.section(2 * DIR_INDEX.get(ghost.dir) + walkingFrame, 4 + ghostIndex);
		}
		g.drawImage(sprite, (int) ghost.position.x - HTS, (int) ghost.position.y - HTS, null);

		if (debugMode) {
			g.setColor(Color.WHITE);
			g.drawRect((int) ghost.position.x, (int) ghost.position.y, TS, TS);
		}
	}
}