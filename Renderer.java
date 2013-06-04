

/**
 * An interface for rendering matrices of pixels to arbitrary outputs.
 * @author niri
 *
 */
public interface Renderer
{
	/**
	 * Draw a rectangular patch of pixels to output.
	 * @param colors - Matrix of pixel color values.
	 * @param x - X axis offset of the top left corner.
	 * @param y - Y axis offset of the top left corner.
	 */
	public void drawPixelsPatch(int[][] colors, int x, int y);
}