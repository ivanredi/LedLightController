

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.Semaphore;
import processing.core.*;
import oscP5.*;

/**
 * Use as led-light event listener.
 * @author niri
 *
 */
public class AgentController implements OscEventListener
{
	private static final int MAX_ACTIVE_AGENTS = 5;

	// Time interval between two consecutive agent position updates, in milliseconds.
	private static final int AGENT_UPDATER_THREAD_CYCLE_LENGTH = 66;
	
	// list of agents
	public ArrayList<Agent> agents;
	public int numberOfActiveAgents;

	private int minSpaceWidth;
	private int maxSpaceWidth;

	private int minSpaceHeight;
	private int maxSpaceHeight;

	private int minKinectWidth;
	private int maxKinectWidth;

	private int minKinectHeight;
	private int maxKinectHeight;
	
	private float maxAgentStepSize;

	private int radius;

	// OSC network variable for led-light event listener
	private OscP5 oscP5;
	
	private OscMessage[] latestOscMessages = new OscMessage[MAX_ACTIVE_AGENTS];
	// Initialize messages to null.
	{
		for (int i = 0; i < latestOscMessages.length; i++) {
			latestOscMessages[i] = null;
		}
	}
	
	private int nextOscMessageIndex = 0;

	public Semaphore controlOscMessagesAccess = new Semaphore(1, true);
	
	public Semaphore controlAgentArrayAccess = new Semaphore(1, true);
	
	private boolean saveMode;
	
	 // A thread that periodically updates agent positions
	private Thread updaterThread = new Thread(new Runnable() {
		
		@Override
		public void run()
		{
			runUpdaterThread();
		}
	});
	
	// constructor
	public AgentController(int radius, boolean saveMode, float maxAgentStepSize, int kinectPort)
	{
		super();
		this.radius = radius;
		this.saveMode = saveMode;
		this.maxAgentStepSize = maxAgentStepSize;
		// initialize 5 agents
		initializeAgents();
		
		updaterThread.start();

		// registerAgentsAndSetDirections();

		// OSC- network listener initialization
		oscP5 = new OscP5(this, kinectPort);
	
	}

	// initialize 5 agents with id-s; they are still inactive agents
	private void initializeAgents()
	{
		agents = new ArrayList<Agent>();
		for (int i = 0; i < MAX_ACTIVE_AGENTS; i++) {
			agents.add(new Agent(i));
		}
		numberOfActiveAgents = 0;
	}

	// initialize buffer coordinates
	public void setBufferCoordinates(int minSpaceWidth, int maxSpaceWidth,
			int minSpaceHeight, int maxSpaceHeight)
	{
		this.minSpaceWidth = minSpaceWidth;
		this.maxSpaceWidth = maxSpaceWidth;
		this.minSpaceHeight = minSpaceHeight;
		this.maxSpaceHeight = maxSpaceHeight;
	}

	// initialize kinect coordinates
	public void setKinectCoordinates(int minKinectWidth, int maxKinectWidth,
			int minKinectHeight, int maxKinectHeight)
	{
		this.minKinectWidth = minKinectWidth;
		this.maxKinectWidth = maxKinectWidth;
		this.minKinectHeight = minKinectHeight;
		this.maxKinectHeight = maxKinectHeight;
	}

	// adjust kinect coordinate space to processing coordinate space
	private void setMappedAgentCoord(Agent agent, float xCood, float yCoord)
	{

		float xNew = PApplet.map(xCood, minKinectWidth, maxKinectWidth, minSpaceWidth, maxSpaceWidth);
		float yNew = PApplet.map(yCoord, maxKinectHeight, minKinectHeight, minSpaceHeight, maxSpaceHeight);
		agent.setAgentPosition(xNew, yNew);
	}
	
	/**
	 * Smooth the agent movement by not allowing big leaps in positions.
	 * @param oldPosition - last position of the agent
	 * @param newPosition - new reported position
	 */
	private void smoothMovement(Point2D oldPosition, Point2D newPosition)
	{
		float xOld = oldPosition.getX();
		float yOld = oldPosition.getY();
		float xNew = newPosition.getX();
		float yNew = newPosition.getY();
		float distance = PApplet.dist(xOld, yOld, xNew, yNew);
		if (distance > maxAgentStepSize) {
			float ratio = maxAgentStepSize / distance;
			xNew = (xNew - xOld) * ratio + xOld;
			yNew = (yNew - yOld) * ratio + yOld;
		}
		newPosition.setX(xNew);
		newPosition.setY(yNew);
	}
	
	/**
	 * Run function of a thread that periodically updates agent positions,
	 * based on the latest Osc message recieved.
	 */
	private void runUpdaterThread()
	{
		long nextCycleStartTime = System.currentTimeMillis();
		while (true) {
			try {
				controlOscMessagesAccess.acquire();
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			OscMessage[] latestOscMessages = this.latestOscMessages.clone();
			for (int i = 0; i < MAX_ACTIVE_AGENTS; i++) {
				this.latestOscMessages[i] = null;
			}
			controlOscMessagesAccess.release();
			for (int i = 0; i < MAX_ACTIVE_AGENTS; i++) {
				updateAgents(latestOscMessages[i]);
			}
			long currentTime = System.currentTimeMillis();
			
			// Determine next cycle start time
			while (currentTime > nextCycleStartTime) {
				nextCycleStartTime += AGENT_UPDATER_THREAD_CYCLE_LENGTH;
				currentTime = System.currentTimeMillis();
			}
			
			// Wait for the next cycle to begin
			try {
				Thread.sleep(nextCycleStartTime - currentTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Update agents based on the latest Osc message recieved.
	 * @param oscMessage 
	 */
	private void updateAgents(OscMessage oscMessage)
	{
		Object[] messageArguments = null;
		
		if (oscMessage != null) {
			messageArguments = oscMessage.arguments();
		}
		
		controlOscMessagesAccess.release();
		
		if (messageArguments != null) {
			
			// parse osc message
			int agentId;
			boolean isActive;
			float xPosition;
			float yPosition;
			float nearestAgentDistance;
			float angle;
			try {
				agentId = (Integer) messageArguments[0];
				isActive = ((Integer) messageArguments[1]) != 0;
				xPosition = (Float) messageArguments[2];
				yPosition = (Float) messageArguments[3];
				nearestAgentDistance = (Float) messageArguments[4];
				angle = (Float) messageArguments[5];// ignore
			} catch (Exception e1) {
				e1.printStackTrace();
				return;
			}
			
			try {
				controlAgentArrayAccess.acquire();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			Agent oldAgent = agents.get(agentId);
			
			controlAgentArrayAccess.release();
			
			boolean wasActive = oldAgent.isActive();
			if (isActive && ! wasActive) {
				numberOfActiveAgents++;		// became active
			}
			if (wasActive && ! isActive) {
				numberOfActiveAgents--;		// became inactive
			}
			

			// update agent
			Agent newAgent = new Agent(agentId);
			newAgent.setActive(isActive);

			// map agent coordinate for each agent
			setMappedAgentCoord(newAgent, xPosition, yPosition);
			if (wasActive) {
				smoothMovement(oldAgent, newAgent);
			}

			newAgent.setNearestAgentDistance(nearestAgentDistance);


			try {
				controlAgentArrayAccess.acquire();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			agents.set(agentId, newAgent);
			
			controlAgentArrayAccess.release();
		}
	}

	public void oscEvent(OscMessage theOscMessage)
	{
		try {
			controlOscMessagesAccess.acquire();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		
		latestOscMessages[nextOscMessageIndex] = theOscMessage;
		nextOscMessageIndex++;
		if (nextOscMessageIndex == MAX_ACTIVE_AGENTS) {
			nextOscMessageIndex -= MAX_ACTIVE_AGENTS;
		}
		
		controlOscMessagesAccess.release();
		
		if (saveMode) {
			// save osc-message in file
			Object[] messageArguments = theOscMessage.arguments();
			try {
				for (int i = 0; i < messageArguments.length; i++) {
					appendFile(messageArguments[i].toString() + "\t");
				}
				appendFile("\r\n");
			} catch (IOException e) {
				e.printStackTrace();

			}
		}
	}

	public void oscStatus(OscStatus theStatus)
	{
	}
	
	static FileWriter fileWriter;
	
	static {
		
		try {
			fileWriter = new FileWriter("oscpackage.txt", true);
		} catch (IOException e) {
			
		}
	}

	public static void appendFile(String text) throws IOException
	{
		fileWriter.write(text);
		fileWriter.flush();
	}

}

