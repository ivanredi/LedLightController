

import java.io.IOException;
import java.util.ArrayList;
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

	public Semaphore controlOscAccess = new Semaphore(1, true);
	private boolean saveMode;
	// constructor
	public AgentController(int radius, boolean saveMode, float maxAgentStepSize, int kinectPort)
	{
		super();
		this.radius = radius;
		this.saveMode = saveMode;
		this.maxAgentStepSize = maxAgentStepSize;
		// initialize 5 agents
		initializeAgents();

		// registerAgentsAndSetDirections();

		// OSC- network listener initialization
		oscP5 = new OscP5(this, kinectPort);
	
	}

	// initialize 5 agents with id-s; they are still inactive agents
	private void initializeAgents()
	{
		agents = new ArrayList<Agent>();
		for (int i = 0; i < 5; i++) {
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

	public synchronized void oscEvent(OscMessage theOscMessage)
	{
		try {
			controlOscAccess.acquire();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		
		numberOfActiveAgents = 0;
		for (int i = 0; i < 5; i++) {

			// get osc message
			int agentId = theOscMessage.get(i * 6 + 0).intValue();
			boolean isActive = theOscMessage.get(i * 6 + 1).intValue() != 0;
			if (isActive) {
				numberOfActiveAgents++;
			}
			float xPosition = theOscMessage.get(i * 6 + 2).floatValue();
			float yPosition = theOscMessage.get(i * 6 + 3).floatValue();

			// update agent
			Agent newAgent = new Agent(agentId);
			newAgent.setActive(isActive);

			// map agent coordinate for each agent
			setMappedAgentCoord(newAgent, xPosition, yPosition);
			if (agents.get(i).isActive()) {
				smoothMovement(agents.get(i), newAgent);
			}

			newAgent.setNearestAgentDistance(theOscMessage.get(i * 6 + 4).floatValue());

			float angle = theOscMessage.get(i * 6 + 5).floatValue();// ignore

			agents.set(i, newAgent);
		}
		
		controlOscAccess.release();
		
		if (saveMode) {
			// save osc-message in file
			Object[] messageArguments = theOscMessage.arguments();
			try {
				for (int i = 0; i < messageArguments.length; i++) {
					TextFileManipulation.appendFile("oscpackage.txt",
							messageArguments[i].toString() + "\t");
				}
				TextFileManipulation.appendFile("oscpackage.txt", "\r\n");
			} catch (IOException e) {
				e.printStackTrace();

			}
		}
	}

	public void oscStatus(OscStatus theStatus)
	{
	}

	/*
	 * //debug mode
	 * 
	 * public class AgentDirection { public int xDirection; public int
	 * yDirection;
	 * 
	 * public AgentDirection(int xDirection, int yDirection) { super();
	 * this.xDirection = xDirection; this.yDirection = yDirection; }
	 * 
	 * }
	 * 
	 * public ArrayList<AgentDirection> directions = new ArrayList<>();
	 * 
	 * private void registerAgentsAndSetDirections() {
	 * 
	 * // set directions directions.add(0, new AgentDirection(1, 1));
	 * directions.add(1, new AgentDirection(-1, -1)); directions.add(0, new
	 * AgentDirection(1, -1)); directions.add(1, new AgentDirection(-1, 1));
	 * directions.add(1, new AgentDirection(1, 1));
	 * 
	 * // set random start position for 2 agents for (int i = 0; i < 5; i++) {
	 * 
	 * Random randomNum = new Random(); float randomX =
	 * randomNum.nextInt(maxKinectWidth * 2) - maxKinectWidth; float randomY =
	 * randomNum.nextInt(maxKinectHeight * 2) - maxKinectHeight;
	 * 
	 * System.out.println("AGENT KINECT POSITION = ( " + randomX + ", " +
	 * randomY + ")");
	 * 
	 * // map agent coordinate for each agent setMappedAgentCoord(agents.get(i),
	 * randomX, randomY); // set isActive agents.get(i).setActive(true); //
	 * increase number of active agents numberOfActiveAgents++;
	 * 
	 * System.out.println("ACTIVE AGENT: ID = " + i + ". SPACE POSITION = (" +
	 * agents.get(i).getX() + ", " + agents.get(i).getY() + ")"); }
	 * 
	 * }
	 * 
	 * 
	 * // method called from LedLightController to provide agent motion public
	 * void moveAgents(ArrayList<Agent> agents) { int numberOfAgents =
	 * agents.size(); // move agents for (int i = 0; i < numberOfAgents; i++) {
	 * agentMotion(agents.get(i), 1.5f, 1.5f, directions.get(i)); } }
	 * 
	 * // when agent hits the edge of the window, it reverses its direction
	 * private void agentMotion(Agent agent, float xSpeed, float ySpeed,
	 * AgentDirection direction) {
	 * 
	 * float xPosition = agent.getX(); xPosition = xPosition + (xSpeed *
	 * direction.xDirection);
	 * 
	 * float yPosition = agent.getY(); yPosition = yPosition + (ySpeed *
	 * direction.yDirection);
	 * 
	 * // Test to see if the shape exceeds the boundaries of the screen // If it
	 * does, reverse its direction by multiplying by -1 if (xPosition >
	 * maxSpaceWidth - radius || xPosition < radius) { direction.xDirection *=
	 * -1; }
	 * 
	 * if (yPosition > maxSpaceHeight - radius || yPosition < radius) {
	 * direction.yDirection *= -1; }
	 * 
	 * // set new agent position agent.setAgentPosition(xPosition, yPosition); }
	 * 
	 * private void setNumberOfNearbyAgents() {
	 * 
	 * int numberOfAgents = agents.size();
	 * 
	 * for (int i = 0; i < numberOfAgents - 1; i++) { for (int j = i + 1; j <
	 * numberOfAgents; j++) {
	 * 
	 * Agent agentOne = agents.get(i); Agent agentTwo = agents.get(j);
	 * 
	 * float agentsDistance = PApplet.dist(agentOne.getX(), agentOne.getY(),
	 * agentTwo.getX(), agentTwo.getY());
	 * 
	 * if (agentsDistance <= radius) {
	 * 
	 * agentOne.setNearbyObjectCount(agentOne.getNearbyObjectCount() + 1); if
	 * (agentsDistance < agentOne.getNearestAgentDistance()) {
	 * agentOne.setNearestAgentDistance(agentsDistance); }
	 * 
	 * agentTwo.setNearbyObjectCount(agentTwo.getNearbyObjectCount() + 1); if
	 * (agentsDistance < agentTwo.getNearestAgentDistance()) {
	 * agentTwo.setNearestAgentDistance(agentsDistance); }
	 * 
	 * }
	 * 
	 * } } }
	 */

}