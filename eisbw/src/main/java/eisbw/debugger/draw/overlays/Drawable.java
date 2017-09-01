package eisbw.debugger.draw.overlays;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import jnibwapi.JNIBWAPI;
import jnibwapi.Position;
import jnibwapi.util.BWColor;

/**
 * @author Wesley.
 */
public class Drawable {
	
	private static final int[] colors = new int[] {
			16777215,	// White
			0,			// Black
			16711680,	// Red
			65280,		// Green
			255,		// Blue
			32896,		// Teal
			9699539,	// Purple
			16753920,	// Orange
			9127187,	// Brown
			16776960,	// Yellow
			65535,		// Cyan
			8421504		// Grey
	};
	
	private static final BWColor[] bwColors = new BWColor[] {
			BWColor.White,
			BWColor.Black,
			BWColor.Red,
			BWColor.Green,
			BWColor.Blue,
			BWColor.Teal,
			BWColor.Purple,
			BWColor.Orange,
			BWColor.Brown,
			BWColor.Yellow,
			BWColor.Cyan,
			BWColor.Grey
	};
	
	private BWColor[][] grid;
	
	/**
	 * Constructor for a Drawable object.
	 * @param grid Grid used to to draw the object.
	 */
	public Drawable(BWColor[][] grid) {
		this.grid = grid;
	}
	
	/**
	 * Gets the colour at a certain position of the drawable object.
	 * @param x X position.
	 * @param y Y position.
	 * @return The colour at the position.
	 */
	public BWColor getColor(int x, int y) {
		return grid[x][y];
	}
	
	/**
	 * Gets the width of the drawable object.
	 * @return The width.
	 */
	public int getWidth() {
		return grid.length;
	}
	
	/**
	 * Gets the height of the drawable object.
	 * @return The height.
	 */
	public int getHeight() {
		if (grid.length <= 0 || grid[0] == null)
			return 0;
		return grid[0].length;
	}
	
	/**
	 * Draws the current Drawable object to the screen.
	 * @param api API to use to draw.
	 * @param coords Location of where to draw.
	 * @param onScreen Whether the location is in screen coords rather than map coords.
	 */
	public void draw(JNIBWAPI api, Position coords, boolean onScreen) {
		for (int x = 0; x < grid.length; ++x) {
			for (int y = 0; y < grid[0].length; ++y) {
				BWColor color = grid[x][y];
				if (color != null) {
					api.drawDot(coords.translated(new Position(x, y)), color, onScreen);;
				}
			}
		}
	}
	
	/**
	 * Draws the current Drawable object to the screen.
	 * @param api api API to use to draw.
	 * @param topLeft Top left location of where to draw.
	 * @param bottomRight Bottom right location of where to draw.
	 * @param onScreen Whether the location is in screen coords rather than map coords.
	 */
	public void draw(JNIBWAPI api, Position topLeft, Position bottomRight, boolean onScreen) {
		int width = bottomRight.getPX() - topLeft.getPX();
		int height = bottomRight.getPY() - topLeft.getPY();
		
		double xRatio = width/(double)getWidth();
		double yRatio = height/(double)getHeight();
		
		for (int x = 0; x < width; ++x) {
			for (int y = 0; y < height; ++y) {
				int gridX = (int)Math.floor(x/xRatio);
				int gridY = (int)Math.floor(y/yRatio);
				BWColor color = grid[gridX][gridY];
				if (color != null) {
					api.drawDot(topLeft.translated(new Position(x, y)), color, onScreen);;
				}
			}
		}
	}
	
	/**
	 * Creates a drawable object from an image source.
	 * @param img Image source.
	 * @return An drawable object.
	 */
	public static Drawable createFromImage(BufferedImage img) {
		int height = img.getHeight();
		int width = img.getWidth();
		BWColor[][] grid = new BWColor[width][height];
		
		for (int x = 0; x < width; ++x) {
		    for (int y = 0; y < height; ++y) {
		    	Color col = new Color(img.getRGB(x, y), true);
		    	
		    	if (col.getAlpha() < 180) {
		    		grid[x][y] = null;
		    	} else {
		    		grid[x][y] = getClosestColor(col);
		    	}
		    }
		}
		return new Drawable(grid);
	}
	
	/**
	 * Creates a drawable object from an image source.
	 * @param img Image source.
	 * @return An drawable object.
	 */
	public static Drawable createFromImage(String filePath) {
		try {
			BufferedImage img = ImageIO.read(new File(filePath));
			return Drawable.createFromImage(img);
		} catch (IOException e) {
			return null;
		}
	}
	
	/**
	 * Gets the closest represented BWColor to the actual color.
	 * @param color Color to look for.
	 * @return Closest BWColor found.
	 */
	private static BWColor getClosestColor(Color color) {
		int index = 0;
		int dif = Integer.MAX_VALUE;
		
		for (int i = 0; i < colors.length; ++i)
		{
			Color match = new Color(colors[i]);
			int newDif = Math.abs(color.getRed()-match.getRed()) +
					Math.abs(color.getBlue()-match.getBlue()) +
					Math.abs(color.getGreen()-match.getGreen());
			if (dif > newDif) {
				index = i;
				dif = newDif;
			}
		}
		return bwColors[index];
	}

}
