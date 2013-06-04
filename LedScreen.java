

import java.util.concurrent.Semaphore;
import processing.core.*;

/**
 * Processing-Screen for led-light simulations.
 * @author niri
 */
public class LedScreen extends PApplet 
{

	private static final long serialVersionUID = 8168846396880514847L;
	private LedLightPanelRenderer ledLightStripePanelRenderer;
	public static PGraphics buffer;
	public static Semaphore allowBufferRenderer = new Semaphore(1, true);
	
	@Override
	public void setup() 
	{
		// set frame title
		frame.setTitle("Led Screen Simulator");
		
		// set window size 
		size(1232, 880);
		
		LedPanelSimulator ledPanelSimulator1 = new LedPanelSimulator(this, 28, 40, 0, 0, 170, 3);
		LedPanelSimulator ledPanelSimulator2 = new LedPanelSimulator(this, 28, 40, 28, 0, 170, 1);
		LedLightStripPanelRendererFactory.StripPanelConfiguration[] panelconfigurations = new LedLightStripPanelRendererFactory.StripPanelConfiguration[2];
		panelconfigurations[0] = new LedLightStripPanelRendererFactory.StripPanelConfiguration(0, 0, 28, 40, 170, LedLightStripPanelRendererFactory.Corner.TOP_LEFT, ledPanelSimulator1);
		panelconfigurations[1] = new LedLightStripPanelRendererFactory.StripPanelConfiguration(28, 0, 28, 40, 170, LedLightStripPanelRendererFactory.Corner.TOP_RIGHT, ledPanelSimulator2);
		ledLightStripePanelRenderer = LedLightStripPanelRendererFactory.getLedLightStripPanelRenderer
									  ( 56, 
				                        40, 
				                        panelconfigurations
									  );
	}
	
	@Override
	public void draw() 
	{
		
		background(0);
		
		//provide thread safe rendering with semaphore
		try {
			allowBufferRenderer.acquire();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		
		ledLightStripePanelRenderer.render(buffer);
		
		allowBufferRenderer.release();
	}
}