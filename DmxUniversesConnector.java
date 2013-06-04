

/**
 * Handles communication with the DMX output.
 * @author niri
 *
 */
public interface DmxUniversesConnector
{
	/**
	 * Forward a strip of pixel colors to the desired universe.
	 * @param universeIndex - Index of the universe.
	 * @param colors - Array of pixel colors.
	 */
	public void render(int universeIndex, int[] colors);
}