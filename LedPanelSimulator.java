

import processing.core.PApplet;

/**
 * A class that simulates a connector to a led panel and displays emulated results on screen. 
 * @author niri
 *
 */
public class LedPanelSimulator implements DmxUniversesConnector
{
	/**
	 * The {@link PApplet} to be used for drawing the emulated view of Led RGB panel.
	 */
	private PApplet pApplet;
	/**
	 * Width of the emulated panel.
	 */
	private int width;
	/**
	 * Height of the emulated panel.
	 */
	private int height;
	/**
	 * X axis offset of the top left pixel.
	 */
	private int x;
	/**
	 * Y axis offset of the top left pixel.
	 */
	private int y;
	/**
	 * Maps each pixel of each universe to its physical coordinates.
	 */
	private int[][][] universesMapping;
	
	private float ledSpacingX;
	private float ledSpacingY;
	private float ledDiameterX;
	private float ledDiameterY;

	public LedPanelSimulator(PApplet pApplet, int[][][] universesMapping)
	{
		super();
		this.pApplet = pApplet;
		this.universesMapping = universesMapping;
		x = 0;
		y = 0;
	}

	public LedPanelSimulator(PApplet pApplet, int width, int height, int x, int y, int stripLength, int corner, int realWidth, int realHeight)
	{
		super();
		this.pApplet = pApplet;
		this.width = width;
		this.height = height;
		this.x = x;
		this.y = y;
		
		int universeCount = width * height / stripLength;
		if ((width * height) % stripLength > 0) universeCount++;
		
		this.universesMapping = new int[universeCount][stripLength][2];
		
		int topCornerFlag = corner & 1;
		int leftCornerFlag = (corner & 2) >> 1;
		int xIncrement = (leftCornerFlag << 1) - 1;
		int yIncrement = (topCornerFlag << 1) - 1;
		int xStart = (leftCornerFlag == 1) ? 0 : width - 1;
		int yStart = (topCornerFlag == 1) ? 0 : height - 1;
		int i = xStart;
		int j = yStart;
		int universeIndex = 0;
		int pixelIndex = 0;
		for (int k = 0; k < width * height; k++) {
			universesMapping[universeIndex][pixelIndex][0] = i;
			universesMapping[universeIndex][pixelIndex][1] = j;
			pixelIndex++;
			while (pixelIndex >= stripLength) {
				pixelIndex %= stripLength;
				universeIndex++;
			}
			j += yIncrement;
			if (j < 0 || j >= height) {
				yIncrement *= -1;
				j += yIncrement;
				i += xIncrement;
			}
		}
		this.ledSpacingX = ((float) realWidth) / ((float) width);
		this.ledSpacingY = ((float) realHeight) / ((float) height);
		this.ledDiameterX = ledSpacingX / 3.66f;
		this.ledDiameterY = ledSpacingY / 3.66f;
	}
	
	@Override
	public void render(int universeIndex, int[] pixelColors)
	{
		
		for (int i = 0; i < pixelColors.length && i < universesMapping[universeIndex].length; i++) {
			int x = universesMapping[universeIndex][i][0];
			int y = universesMapping[universeIndex][i][1];
			float ledX = (this.x + x + 0.5f) * ledSpacingX;
			float ledY = (this.y + y + 0.5f) * ledSpacingY;
			pApplet.noStroke();
			pApplet.fill(0);
			pApplet.ellipse(ledX, ledY, ledDiameterX * 2 + 1, ledDiameterY * 2 + 1);
			pApplet.fill(pixelColors[i], 100f);
			pApplet.ellipse(ledX, ledY, ledDiameterX * 2, ledDiameterY * 2);
			pApplet.fill(pixelColors[i]);
			pApplet.ellipse(ledX, ledY, ledDiameterX, ledDiameterY);
		}
	}

}