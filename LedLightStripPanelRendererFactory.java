

/**
 * A class that create {@link LedLightPanelRenderer}.
 * @author niri
 */
public class LedLightStripPanelRendererFactory
{
	public static enum Corner
	{
		TOP_LEFT,
		TOP_RIGHT,
		BOTTOM_LEFT,
		BORROM_RIGHT;
		
		public boolean isTop()
		{
			return (this.equals(TOP_LEFT) || this.equals(TOP_RIGHT));
		}
		
		public boolean isLeft()
		{
			return (this.equals(TOP_LEFT) || this.equals(BOTTOM_LEFT));
		}
	}
	
	private static int[][][] getLedLightStripePanelRendererSubMap(int width, int height, int stripLength, Corner corner, DmxUniversesConnector dmxUniversesConnector)
	{
		int iStart;
		int jStart;
		int iIncrement;
		int jIncrement;
		
		if (corner.isTop()) {
			jStart = 0;
			jIncrement = 1;
		} else {
			jStart = height - 1;
			jIncrement = -1;
		}
		if (corner.isLeft()) {
			iStart = 0;
			iIncrement = 1;
		} else {
			iStart = width - 1;
			iIncrement = -1;
		}
		
		int i = iStart;
		int j = jStart;
		int universeIndex = 0;
		int pixelIndex = 0;
		int[][][] coordinateUniversePixelMapping = new int[width][height][2];
		for (int k = 0; k < width * height; k++) {
			coordinateUniversePixelMapping[i][j][0] = universeIndex;
			coordinateUniversePixelMapping[i][j][1] = pixelIndex;
			
			j += jIncrement;
			if (j >= height || j < 0) {
				jIncrement *= -1;
				j += jIncrement;
				i += iIncrement;
			}
			
			pixelIndex++;
			if (pixelIndex == stripLength) {
				universeIndex++;
				pixelIndex %= stripLength;
			}
		}
		return coordinateUniversePixelMapping;
	}
	
	public static LedLightPanelRenderer getLedLightStripPanelRenderer(int width, int height, StripPanelConfiguration[] panelconfigurations)
	{
		DmxUniversesConnector[] connectors = new DmxUniversesConnector[panelconfigurations.length];
		int[][][] coordinateConnectorUniversePixelMapping = new int[width][height][3];
		for (int i = 0; i < panelconfigurations.length; i++) {
			StripPanelConfiguration currentStripPanelConfiguration = panelconfigurations[i];
			connectors[i] = currentStripPanelConfiguration.dmxConnector;
			int[][][] subMapping = getLedLightStripePanelRendererSubMap(currentStripPanelConfiguration.width, currentStripPanelConfiguration.height, currentStripPanelConfiguration.stripLength, currentStripPanelConfiguration.corner, currentStripPanelConfiguration.dmxConnector);
			for (int j = 0; j < subMapping.length; j++) {
				for (int k = 0; k < subMapping[j].length; k++) {
					coordinateConnectorUniversePixelMapping[j + currentStripPanelConfiguration.x][k + currentStripPanelConfiguration.y][0] = i;
					coordinateConnectorUniversePixelMapping[j + currentStripPanelConfiguration.x][k + currentStripPanelConfiguration.y][1] = subMapping[j][k][0];
					coordinateConnectorUniversePixelMapping[j + currentStripPanelConfiguration.x][k + currentStripPanelConfiguration.y][2] = subMapping[j][k][1];
				}
			}
		}
		return new LedLightPanelRenderer(width, height, connectors, coordinateConnectorUniversePixelMapping);
	}
	
	public static class StripPanelConfiguration
	{
		public int x;
		public int y;
		public int width;
		public int height;
		public int stripLength;
		public Corner corner;
		public DmxUniversesConnector dmxConnector;
		
		public StripPanelConfiguration(int x, int y, int width, int height,
				int stripLength, Corner corner,
				DmxUniversesConnector dmxConnector)
		{
			super();
			this.x = x;
			this.y = y;
			this.width = width;
			this.height = height;
			this.stripLength = stripLength;
			this.corner = corner;
			this.dmxConnector = dmxConnector;
		}
	}
}