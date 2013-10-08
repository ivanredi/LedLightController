

import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.Semaphore;
import processing.core.*;
import processing.event.MouseEvent;

/**
 * Processing-Screen for led-light simulations.
 * @author niri
 */
public class LedScreen extends PApplet 
{

	private static final long serialVersionUID = 8168846396880514847L;
	private HashSet<LedLightPanelRenderer> ledLightPanelRenderers = new HashSet<LedLightPanelRenderer>();
	public static PGraphics buffer;
	public static Semaphore allowBufferRenderer = new Semaphore(1, true);
	public static int setWidth;// = 1232;
	public static int setHeight;// = 880;
	private static boolean screenTestMode = false;
        public static boolean runSimulator = false;
        public static boolean runArtNet = false;
	
	@Override
	public void setup() 
	{
		// set frame title
		if (screenTestMode) {
			buffer = createGraphics(56, 40);
		} else {
			frame.setTitle("Led Screen");
		}
		// set window size 
		size(setWidth, setHeight);
                if (runSimulator) {
                  LedPanelSimulator ledPanelSimulator1 = new LedPanelSimulator(this, 28, 40, 28, 0, 170, 2, this.width / 2, this.height);
                  LedPanelSimulator ledPanelSimulator2 = new LedPanelSimulator(this, 28, 40, 0, 0, 170, 0, this.width / 2, this.height);
                  LedLightStripPanelRendererFactory.StripPanelConfiguration[] panelconfigurations = new LedLightStripPanelRendererFactory.StripPanelConfiguration[2];
                  panelconfigurations[0] = new LedLightStripPanelRendererFactory.StripPanelConfiguration(28, 0, 28, 40, 170, LedLightStripPanelRendererFactory.Corner.BOTTOM_LEFT, ledPanelSimulator1);
                  panelconfigurations[1] = new LedLightStripPanelRendererFactory.StripPanelConfiguration(0, 0, 28, 40, 170, LedLightStripPanelRendererFactory.Corner.BOTTOM_RIGHT, ledPanelSimulator2);
                  LedLightPanelRenderer ledLightStripePanelRenderer = LedLightStripPanelRendererFactory.getLedLightStripPanelRenderer
                                  ( 56,
                                              40, 
                                              panelconfigurations
                                  );
                  ledLightPanelRenderers.add(ledLightStripePanelRenderer);
                } else {
                  this.width = 0;
                  this.height = 0;
                }
                if (runArtNet) {
                  ArtNetConnector artNetConnector1 = new ArtNetConnector(new String[] {"192.168.0.210", "192.168.0.211"});
                  ArtNetConnector artNetConnector2 = new ArtNetConnector(new String[] {"192.168.0.214", "192.168.0.215"});
                  LedLightStripPanelRendererFactory.StripPanelConfiguration[] panelconfigurations = new LedLightStripPanelRendererFactory.StripPanelConfiguration[2];
                  panelconfigurations[0] = new LedLightStripPanelRendererFactory.StripPanelConfiguration(28, 0, 28, 40, 170, LedLightStripPanelRendererFactory.Corner.BOTTOM_LEFT, artNetConnector1);
                  panelconfigurations[1] = new LedLightStripPanelRendererFactory.StripPanelConfiguration(0, 0, 28, 40, 170, LedLightStripPanelRendererFactory.Corner.BOTTOM_RIGHT, artNetConnector2);
                  LedLightPanelRenderer ledLightStripePanelRenderer = LedLightStripPanelRendererFactory.getLedLightStripPanelRenderer
                                  ( 56,
                                              40, 
                                              panelconfigurations
                                  );
                  ledLightPanelRenderers.add(ledLightStripePanelRenderer);
                }
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
		
//		ledLightStripePanelRenderer.render(buffer);
		for (Iterator renderersIterator = ledLightPanelRenderers.iterator(); renderersIterator.hasNext();) {
			LedLightPanelRenderer currentLedLightPanelRenderer = (LedLightPanelRenderer) renderersIterator.next();
			currentLedLightPanelRenderer.render(buffer);
		}
		
		allowBufferRenderer.release();
	}
	
	@Override
	public void mouseClicked(MouseEvent event)
	{
		try {
			allowBufferRenderer.acquire();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		buffer.beginDraw();
		buffer.background(10);
		buffer.stroke(0, 0, 255);
		buffer.point(event.getX() / 22, event.getY() / 22);
		buffer.endDraw();
		allowBufferRenderer.release();
	}
	
	public static void main(String[] args)
	{
		screenTestMode = true;
		setWidth = 1232;
		setHeight = 880;
		PApplet.main(new String[] { LedScreen.class.getName() });
		buffer.beginDraw();
		buffer.background(10);
		buffer.endDraw();
	}
}