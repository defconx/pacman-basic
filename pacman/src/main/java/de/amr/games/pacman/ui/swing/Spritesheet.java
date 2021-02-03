package de.amr.games.pacman.ui.swing;

import java.awt.Color;
import java.awt.image.BufferedImage;

import de.amr.games.pacman.lib.V2i;

/**
 * A spritesheet.
 * 
 * @author Armin Reichert
 */
public class Spritesheet {

	public static BufferedImage createBrightEffect(BufferedImage src, Color borderColor, Color wallColor) {
		BufferedImage dst = new BufferedImage(src.getWidth(), src.getHeight(), src.getType());
		dst.getGraphics().drawImage(src, 0, 0, null);
		for (int x = 0; x < src.getWidth(); ++x) {
			for (int y = 0; y < src.getHeight(); ++y) {
				if (src.getRGB(x, y) == borderColor.getRGB()) {
					dst.setRGB(x, y, Color.WHITE.getRGB());
				} else if (src.getRGB(x, y) == wallColor.getRGB()) {
					dst.setRGB(x, y, Color.BLACK.getRGB());
				} else {
					dst.setRGB(x, y, src.getRGB(x, y));
				}
			}
		}
		return dst;
	}

	private final BufferedImage image;
	private final int tileSize;
	private int offsetX, offsetY;

	public Spritesheet(BufferedImage image, int tileSize) {
		this.image = image;
		this.tileSize = tileSize;
	}

	protected V2i v2(int x, int y) {
		return new V2i(x, y);
	}

	public void setOrigin(int x, int y) {
		offsetX = x;
		offsetY = y;
	}

	public BufferedImage subImage(int x, int y, int w, int h) {
		return image.getSubimage(x, y, w, h);
	}

	public BufferedImage spritesAt(int tileX, int tileY, int numTilesX, int numTilesY) {
		return subImage(offsetX + tileX * tileSize, offsetY + tileY * tileSize, numTilesX * tileSize, numTilesY * tileSize);
	}

	public BufferedImage spriteAt(int tileX, int tileY) {
		return spritesAt(tileX, tileY, 1, 1);
	}

	public BufferedImage spriteAt(V2i tile) {
		return spriteAt(tile.x, tile.y);
	}
}