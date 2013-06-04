

import processing.core.PGraphics;

/**
 * A class that renders a {@link PGraphics} buffer to a led light stripe panel.
 * @author niri
 *
 */
public class LedLightPanelRenderer extends PGraphicsRenderer
{
	private static final boolean DEBUG_MODE = false;
	/**
	 * Width of the panel.
	 */
	private int width;
	/**
	 * Height of the panel.
	 */
	private int height;
	/**
	 * Stores all the DMX connectors.
	 */
	private DmxUniversesConnector[] universeConnectors;
	/**
	 * Maps pixel coordinates to the corresponding DMX connector, universe and pixel.
	 */
	private int[][][] coordinateConnectorUniversePixelMapping;
	/**
	 * Stores the sizes of each universe of each DMX connector.
	 */
	private int[][] connectorsUniverseLengths;
	
	/**
	 * Constructs a new {@link LedLightPanelRenderer}, for rendering to a rectangular panel of Led RGB lights.
	 * @param width - Width of the panel.
	 * @param height - Height of the panel.
	 * @param universesConnectors - The DMX connectors for communicating with RGB lights.
	 * @param coordinateUniversePixelMapping - Mapping of the pixel coordinates to their corresponding DMX connectors, universes and pixels.
	 */
	public LedLightPanelRenderer(int width, int height, DmxUniversesConnector[] universesConnectors, int[][][] coordinateUniversePixelMapping)
	{
		super();
		if (DEBUG_MODE) {
			System.out.println("Creating the LedLightPanelRenderer...");
			System.out.println("Width:\t" + width);
			System.out.println("Height:\t" + height);
			System.out.println("Number of connectors:\t" + universesConnectors.length);
			System.out.println("Pixel mapping:");
			for (int i = 0; i < coordinateUniversePixelMapping[0].length; i++) {
				for (int j = 0; j < coordinateUniversePixelMapping.length; j++) {
					System.out.print("{" + coordinateUniversePixelMapping[j][i][0] + ", " + coordinateUniversePixelMapping[j][i][1] + ", " + coordinateUniversePixelMapping[j][i][2] + "}\t");
				}
				System.out.println("\n");
			}
		}
		this.width = width;
		this.height = height;
		this.universeConnectors = universesConnectors;
		this.coordinateConnectorUniversePixelMapping = coordinateUniversePixelMapping;
		
		//
		// Determine and store the sizes of all universes of all controllers, for easy access at rendering time
		//
		
		connectorsUniverseLengths = new int [universesConnectors.length][];
		
		// Find the largest universe index for each connector, 
		// and determine the number of its universes as the largest index increased by 1
		int[] connectorUniverseCounts = new int[universesConnectors.length];
		for (int i = 0; i < connectorUniverseCounts.length; i++) {
			connectorUniverseCounts[i] = 0;
		}
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				if (connectorUniverseCounts[coordinateUniversePixelMapping[i][j][0]] < coordinateUniversePixelMapping[i][j][1] + 1) {
					connectorUniverseCounts[coordinateUniversePixelMapping[i][j][0]] = coordinateUniversePixelMapping[i][j][1] + 1;
				}
			}
		}
		
		// Find the largest pixel index for each universe, 
		// and determine the number of its pixels as the largest index increased by 1
		for (int i = 0; i < universesConnectors.length; i++) {
			connectorsUniverseLengths[i] = new int[connectorUniverseCounts[i]];
		}
		for (int i = 0; i < universesConnectors.length; i++) {
			for (int j = 0; j < connectorsUniverseLengths[i].length; j++) {
				connectorsUniverseLengths[i][j] = 0;
			}
		}
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				if (connectorsUniverseLengths[coordinateUniversePixelMapping[i][j][0]][coordinateUniversePixelMapping[i][j][1]] < coordinateUniversePixelMapping[i][j][2] + 1) {
					connectorsUniverseLengths[coordinateUniversePixelMapping[i][j][0]][coordinateUniversePixelMapping[i][j][1]] = coordinateUniversePixelMapping[i][j][2] + 1;
				}
			}
		}
	}
	
	@Override
	public void drawPixelsPatch(int[][] colors, int x, int y)
	{
		if (DEBUG_MODE) {
			System.out.println("Rendering pixels to Led RGB panel.");
			System.out.println("Colors:");
			for (int i = 0; i < colors[0].length; i ++) {
				for (int j = 0; j < colors.length; j++) {
					int currentPixelColor = colors[j][i];
					int r = (currentPixelColor & (255 << 16)) >> 16;
					int g = (currentPixelColor & (255 << 8)) >> 8;
					int b = currentPixelColor & 255;
					System.out.print("{" + r + ", " + g + ", " + b + "}\t");
				}
				System.out.println("\n");
			}
		}
		int pixelsWidth = colors.length;
		if (pixelsWidth == 0) return;
		int pixelsHeight = colors[0].length;
		
		// Make the container to store color values for each universe of each connector
		int[][][] connectorUniversesValues = new int[universeConnectors.length][][];
		for (int i = 0; i < connectorUniversesValues.length; i++) {
			connectorUniversesValues[i] = new int[connectorsUniverseLengths[i].length][];
			for (int j = 0; j < connectorsUniverseLengths[i].length; j++) {
				connectorUniversesValues[i][j] = new int[connectorsUniverseLengths[i][j]];
				for (int k = 0; k < connectorsUniverseLengths[i][j]; k++) {
					connectorUniversesValues[i][j][k] = 0;
				}
			}
		}
		
		// Remap color values from the pixel matrix to the corresponding connectors and universes
		for (int i = 0; i < pixelsWidth; i++) {
			for (int j = 0; j < pixelsHeight; j++) {
				int currentConnector = coordinateConnectorUniversePixelMapping[i + x][j + y][0];
				int currentUniverse = coordinateConnectorUniversePixelMapping[i + x][j + y][1];
				int currentPixel = coordinateConnectorUniversePixelMapping[i + x][j + y][2];
				connectorUniversesValues[currentConnector][currentUniverse][currentPixel] = colors[i][j];
			}
		}
		
		// Render all universes on all connectors
		for (int i = 0; i < connectorUniversesValues.length; i++) {
			for (int j = 0; j < connectorUniversesValues[i].length; j++) {
				universeConnectors[i].render(j, connectorUniversesValues[i][j]);
			}
		}
	}
	
	

}