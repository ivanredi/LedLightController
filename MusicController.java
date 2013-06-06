

import java.util.ArrayList;
import oscP5.*;

/**
 * Use as music event listener.
 * @author niri
 */
public class MusicController implements OscEventListener {

	// OSC network variables
	private OscP5 oscP5;
	private OscMessage oscMessage;
	
	public MusicController(int musicPort) {
		super();
		
		// OSC- network listener initialization
		oscP5 = new OscP5(this, musicPort);
	}

	public void oscEvent(OscMessage theOscMessage) 
	{
		this.oscMessage = theOscMessage;
	}

	public void printOscMessages()
	{
		int numberOfActiveAgents = this.oscMessage.get(0).intValue();
		int mode = this.oscMessage.get(1).intValue();
		float centroidX = this.oscMessage.get(2).floatValue();
		float centroidY = this.oscMessage.get(3).floatValue();
		
		System.out.println("\nOSCPackage from music controller:");
		System.out.println("|active agents|  |m o d e|  |centroid coordinates|");
		System.out.println("       " + numberOfActiveAgents + "             " + mode + "       (" + centroidX + ", " + centroidY + ")");
		
		System.out.println("|agentId|  |distance from centroid|");
		for (int i = 0; i < numberOfActiveAgents; i++) {
			int currentAgentId = this.oscMessage.get(i * 2 + 4).intValue();
			float currentAgentDistanceFromCentroid = this.oscMessage.get(i * 2 + 5).floatValue();
			System.out.println("    " + currentAgentId + "              " + currentAgentDistanceFromCentroid);
		}
	}
	
	public void oscStatus(OscStatus theStatus) 
	{
		
	}


}