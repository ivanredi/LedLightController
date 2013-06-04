

import processing.core.*;

/**
 * An abstract class for rendering {@link PGraphics} buffers to arbitrary outputs.
 * @author niri
 *
 */
public abstract class PGraphicsRenderer implements Renderer
{
	/**
	 * Render a {@link PGraphics} buffer to the output.
	 * @param buffer
	 */
	public void render(PGraphics buffer)
	{
		int width = buffer.width;
		int height = buffer.height;
		int[][] colors = new int[width][height];
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				colors[i][j] = buffer.get(i, j);
			}
		}
		drawPixelsPatch(colors, 0, 0);
	}
}