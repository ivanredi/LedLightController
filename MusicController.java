

import java.util.ArrayList;
import oscP5.*;

/**
 * Use as music event listener.
 * @author niri
 */
public class MusicController implements OscEventListener {

	// OSC network variables
	private OscP5 oscP5;
	
	
	public MusicController() {
		super();
		
		// OSC- network listener initialization
		oscP5 = new OscP5(this, 5002);
	}

	public void oscEvent(OscMessage theOscMessage) 
	{
		int numberOfActiveAgents = theOscMessage.get(0).intValue();
		int mode = theOscMessage.get(1).intValue();
		float centroidX = theOscMessage.get(2).floatValue();
		float centroidY = theOscMessage.get(3).floatValue();
		System.out.println("MUSIC CONTROLLER: active users in the system = " + numberOfActiveAgents);
		System.out.println("MUSIC CONTROLLER: mode = " + mode);
		System.out.println("MUSIC CONTROLLER: centroid coordinates = (" + centroidX + ", " + centroidY + ")");
		for (int i = 0; i < numberOfActiveAgents; i++) {
			int currentAgentId = theOscMessage.get(i * 2 + 4).intValue();
			float currentAgentDistanceFromCentroid = theOscMessage.get(i * 2 + 5).floatValue();
			System.out.println("MUSIC CONTROLLER: distance from centroid for agent (id = " + currentAgentId + ") = " + currentAgentDistanceFromCentroid);
		}
	}

	public void oscStatus(OscStatus theStatus) 
	{
		
	}

}