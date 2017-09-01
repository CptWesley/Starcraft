package eisbw.debugger.draw.overlays;

import eisbw.Game;
import eisbw.debugger.draw.IDraw;
import jnibwapi.JNIBWAPI;
import jnibwapi.Position;
import jnibwapi.Region;
import jnibwapi.util.BWColor;

/**
 * @author Wesley.
 */
public abstract class Overlay extends IDraw {

	/**
	 * Constructor for an overlay.
	 * @param game
	 */
	protected Overlay(Game game) {
		super(game);
	}
	
	/**
	 * Draws a background to the screen.
	 * @param api API used for drawing.
	 * @param color Color of the background.
	 */
	protected void drawBackground(JNIBWAPI api, BWColor color) {
		Position tl = new Position(0, 0);
		Position br = new Position(1920, 1080);
		api.drawBox(tl, br, color, true, true);
	}
	
	/**
	 * Draws region borders to the screen.
	 * @param api API used for drawing.
	 * @param color Color used for drawing.
	 */
	protected void drawRegionBorders(JNIBWAPI api, BWColor color) {
		for (Region region : api.getMap().getRegions()) {
			Position[] p = region.getPolygon();
			for (int j = 0; j < p.length; ++j) {
				api.drawLine(p[j], p[(j + 1) % p.length], color, false);
			}
		}
	}

}
