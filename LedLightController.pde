

import java.awt.Point;
import java.util.ArrayList;

import processing.core.*;
import oscP5.*;
import netP5.*;

/**
 * The main sketch that manages all individual components of the application,
 * and also handles drawing.
 * @author niri
 *
 */
public class LedLightController extends PApplet
{

	// KINECT SPACE COORDINATES
	int minKinectWidth = -100;
	int maxKinectWidth = 100;
	int minKinectHeight = -100;
	int maxKinectHeight = 100;
	
	// PROCESSING SPACE COORDINATES
	int minProcessingSpaceWidth = 0;
	int maxProcessingSpaceWidth = 56;
	int minProcessingSpaceHeight = 0;
	int maxProcessingSpaceHeight = 40;
	
	//  MISIC SPACE COORDINATES
	int minMusicSpaceWidth = 0;
	int maxMusicSpaceWidth = 127;
	int minMusicSpaceHeight = 0;
	int maxMusicSpaceHeight = 127;

	// 1. OSC NETWORK INSTANCES
	OscP5 oscP5;	
	NetAddress eventDestinationForKinectOscPackages;
	NetAddress eventDestinationForMusicOscPackages;
	int kinectPort = 7000;
	int musicPort = 7001;
	
	// 2. CONTROLLERS INSTANCES
	AgentController agentController;
	AgentGroupController agentGroupController;
	
	// 2.1. CONTROLLERS VARIABLES
	// diameter of the surface that agent takes in space
	int agentRadius = 0;
	// maximum range for the agent to stay in the group
	float maxDistanceFromCentroid = 15.0f;
	// mode for agents group position
	int systemMode = 0;

	
	// 3. DRAWING:
	// drawing buffer
	PGraphics buffer;
	
	// largeness values for agents
	float[] adjustBrightness = { 0.005f, // 1 active agent
			                     1.0f, // 2 active agents
			                     20.0f, // 3 active agents
			                     200.0f, // 4 active agents
			                     10000.0f }; // 5 active agents
	
	// color values for each mode
	int[][] adjustColor = { {255, 255, 255}, //mode 1
			                {255, 0, 0}, //mode 2
			                {0, 255, 0}, //mode 3
			                {0, 255, 255}}; //mode 4
	
	
	// 4. KINECT SIMULATOR VARIABLES (will be removed)
	ArrayList<Agent> agentsInKinectSpace;
	int numberOfActiveAgentsInKinectSpace;
	int draggedAgentIndex = -1;
	
	// debug and print mode
	boolean debugMode = true;
	boolean printMode = true;
	boolean printAgentsPositions = true;
	boolean fileSaveMode = false;
	boolean runLedSimulator = false;
	
	//array for debug-print mode
	ArrayList<Agent> previousAgentsState  = new ArrayList<Agent>();
	
	// music controller variables
	MusicController musicController;
	float maxAgentStepSize = 6;
	
	// processing method setup()
	public void setup()
	{
		if (debugMode) {
			agentsInKinectSpace = new ArrayList<Agent>();
			for (int i = 0; i < 5; i++) {
				agentsInKinectSpace.add(new Agent(i));
			}
		}
		
		// osc-network initialization
		oscP5 = new OscP5(this, 0);
		eventDestinationForKinectOscPackages = new NetAddress("127.0.0.1", kinectPort);
		eventDestinationForMusicOscPackages = new NetAddress("127.0.0.1", musicPort);
		
		// create music listener for debug mode
		if (debugMode) {
			musicController = new MusicController(musicPort);
		}
		
		// processing frame initializations
		// set frame title
		frame.setTitle("Led Screen Controller");
		// set window size
		size(560, 400);
		
		// create buffer for drawing
		buffer = createGraphics(maxProcessingSpaceWidth, maxProcessingSpaceHeight);

		// agent_controller initialization; set radius
		agentController = new AgentController(agentRadius, fileSaveMode, maxAgentStepSize, kinectPort);
		agentController.setBufferCoordinates(minProcessingSpaceWidth, maxProcessingSpaceWidth, minProcessingSpaceHeight, maxProcessingSpaceHeight);
		agentController.setKinectCoordinates(minKinectWidth, maxKinectWidth, minKinectHeight, maxKinectHeight);
		
		if(printAgentsPositions) {
			previousAgentsState.addAll(agentController.agents);
		}
		
		// initialize agent_group_controller with agents in system; set max distance from centroid
		agentGroupController = new AgentGroupController(agentController.agents, maxDistanceFromCentroid);
		
		// debug mode
		if (runLedSimulator) {
			//crate screen for LED simulator 
			LedScreen.buffer = this.buffer;
			PApplet.main(new String[] { LedScreen.class.getName() });
		}
		
	}
	
	// processing method draw()
	public void draw() 
	{
		background(0);
		
		sendAgentsStatusToControllers();

		// check current active agents in system
		agentGroupController.getActiveAgentsInSystem(agentController.agents);
		
		// get returned mode from agent_group_controller
		int mode = agentGroupController.getModeControl();

		// print mode
		if (printMode) {
			
			// mode
			if (this.systemMode != mode) {
				System.out.println("\nMode " + mode);
				this.systemMode = mode;
			}
		}
		
		if (printAgentsPositions) {
			// print agent's positions			
			for (int i = 0; i < 5; i ++) {
				Agent agent = agentController.agents.get(i);
				Agent previousAgentState = previousAgentsState.get(i);
				if(agent.isActive() && previousAgentState.isActive()) {
					if(agent.getX() != previousAgentState.getX() || agent.getY() != previousAgentState.getY()) {
						System.out.println("Agent position changed: AgentId = " + agent.getAgentId() + ", PositionInProcessingSpace = (" + agent.getX() + ", " + agent.getY() + ")") ;
					}
				}
			}
			previousAgentsState = new ArrayList<Agent>();
			previousAgentsState.addAll(agentController.agents);
		}
		
		// debug mode
		if (runLedSimulator) {
			// provide thread safe rendering on LED simulator with semaphore
			try {
				LedScreen.allowBufferRenderer.acquire();
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}
		
		// prepare and fill buffer
		buffer.beginDraw();
		buffer.background(0);

		// draw in buffer
		drawWithPixel(buffer, mode);

		/* try {
		// move agents with pause
			Thread.sleep(40);
			agentControler.moveAgents(agentControler.agents);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} */

		// close buffer
		buffer.endDraw();

		// debug mode
		if (runLedSimulator) {
			// provide thread safe rendering with semaphore
			LedScreen.allowBufferRenderer.release();
		}
		
		// display image from buffer in main screen
		image(buffer.get(), 0, 0, width, height);
		
	}

	// drawing method
	void drawWithPixel(PGraphics buffer2, int mode)
	{
		float[][] pixi = new float[buffer.width][buffer.height];
		int numberOfActiveAgents = agentGroupController.activeAgentsInSystem.size(); //agentController.numberOfActiveAgents;
		if (numberOfActiveAgents > 0) {
			
			// loop matrix of pixels
			for (int i = 0; i < buffer.width; i++) {
				for (int j = 0; j < buffer.height; j++) {

					float darkness = 1;
					float minDarkness = Float.MAX_VALUE;
					
					// if group is created
					int numberOfAgentsInGroup = agentGroupController.agentsInGroup.size();
					
					if (numberOfAgentsInGroup > 0) {
						
						// loop agents in group
						for(int a = 0; a < numberOfAgentsInGroup; a++) {
							Agent agent = agentGroupController.agentsInGroup.get(a);						
							// create color for active agents
							float pixelDistFromAgent = dist(i, j, agent.getX(), agent.getY());
							darkness *= pixelDistFromAgent;
						}
							
						// add size to agent pixels
						float brightnes = adjustBrightness[numberOfAgentsInGroup - 1];
						darkness /= brightnes;
						
						if (darkness < minDarkness) {
							minDarkness = darkness;
						}
					}
					
					darkness = 1;
					// loop all active agents in system
					for (int a = 0; a < numberOfActiveAgents; a++) {
						Agent agent = agentGroupController.activeAgentsInSystem.get(a);						
						// create color for active agents
						float pixelDistFromAgent = dist(i, j, agent.getX(), agent.getY());
						darkness *= pixelDistFromAgent;
					
					}
										
					float brightnes = adjustBrightness[numberOfActiveAgents - 1];
					darkness /= brightnes;
					
					if (darkness < minDarkness) {
						minDarkness = darkness;
					}
				
					pixi[i][j] = (float) (255 - minDarkness);

				}
			}
		}

		// draw each pixel with his corresponding color
		for (int i = 0; i < buffer.width; i++) {
			for (int j = 0; j < buffer.height; j++) {

				float pixel = pixi[i][j] / 255.0f;
				buffer.stroke(adjustColor[mode - 1][0] * pixel, adjustColor[mode - 1][1] * pixel, adjustColor[mode - 1][2] * pixel, 255);
				buffer.point(i, j);

			}
		}
		
	}
	
	//(FIXME: replace with real kinect data)
	void sendAgentsStatusToControllers()
	{
		// create OSC messages for kinect simulation (for debug purposes )
		if (debugMode) {
			OscMessage oscMessage = new OscMessage("/agentsStatusForLedLight");
			for (int i = 0; i < agentsInKinectSpace.size(); i++) {

				Agent currentAgent = agentsInKinectSpace.get(i);

				// OSC message field = agent id (int)
				oscMessage.add(currentAgent.getAgentId());

				// OSC message field = agent is active (int)
				oscMessage.add(currentAgent.isActive() ? 1 : 0);

				// OSC message field = agent x position (float)
				oscMessage.add(currentAgent.getX());

				// OSC message field = agent y position (float)
				oscMessage.add(currentAgent.getY());

				// OSC message field = agent distance from nearest agent (float)
				oscMessage.add(currentAgent.getNearestAgentDistance());

				// OSC message field = agent x position (float)
				oscMessage.add(currentAgent.getAngle());
			}
			oscP5.send(oscMessage, eventDestinationForKinectOscPackages);
		}
		
		// create OSC message for music controller

		
		// check current active agents in system
		agentGroupController.getActiveAgentsInSystem(agentController.agents);

		// get returned mode from agent_group_controller
		int mode = agentGroupController.getModeControl();

		Point2D centroid = agentGroupController.centroid;
		
		// map centroid position to music space
		Point2D mappedCentroidPosition = mapProcessingSpaceToMusicSpace(centroid.getX(), centroid.getY());
		
		ArrayList<Agent> activeAgents = agentGroupController.activeAgentsInSystem;
		int numberOfActiveAgents = activeAgents.size();

		// create OSC message
		OscMessage oscMessage = new OscMessage("/agentsStatusForMusic");

		// OSC message field = number of active agents (int)
		oscMessage.add(numberOfActiveAgents);

		// OSC message field = interaction mode (int)
		oscMessage.add(mode);

		// OSC message field = x for centroid (float)
		oscMessage.add(mappedCentroidPosition.getX());

		// OSC message field = y for centroid (float)
		oscMessage.add(mappedCentroidPosition.getY());

		for (int i = 0; i < numberOfActiveAgents; i++) {

			Agent currentAgent = activeAgents.get(i);
			
			// OSC message field = agent id (int)
			oscMessage.add(currentAgent.getAgentId());
			
			// map agent position to music space
			Point2D mappedAgentPosition = mapProcessingSpaceToMusicSpace(currentAgent.getX(), currentAgent.getY());
			
			// calculate mapped agent position from mapped centroid position
			float agentDistanceFromCentroid = PApplet.dist( mappedAgentPosition.getX(), mappedAgentPosition.getY(), 
					                                        mappedCentroidPosition.getX(), mappedCentroidPosition.getY());
			
			// OSC message field = agent distance from centroid (float)
			oscMessage.add(agentDistanceFromCentroid);

		}
		oscP5.send(oscMessage, eventDestinationForMusicOscPackages);
		
	}
	
	// adjust kinect coordinate space to music coordinate space
	private Point2D mapProcessingSpaceToMusicSpace(float xCood, float yCoord)
	{

		float xNew = PApplet.map(xCood, minProcessingSpaceWidth, maxProcessingSpaceWidth, minMusicSpaceWidth, maxMusicSpaceWidth);
		float yNew = PApplet.map(yCoord, maxProcessingSpaceHeight, minProcessingSpaceHeight, minMusicSpaceHeight, maxMusicSpaceHeight);
		return new Point2DStruct(xNew, yNew);
	}

	// processing method mousePressed() - select agent with mouse
	public void mousePressed()
	{
		if (debugMode) {
			// map window-click space to kinect space (for kinect simulations) 
			float transformedMouseX = map(mouseX, 0, width, minKinectWidth, maxKinectWidth);
			float transformedMouseY = map(mouseY, 0, height, maxKinectHeight, minKinectHeight);
			
			draggedAgentIndex = -1;
			float minimumDistance = Float.MAX_VALUE;
			int numberOfAgents = agentsInKinectSpace.size();
			
			for (int i = 0; i < numberOfAgents; i++) {
				
				Agent currentagent = agentsInKinectSpace.get(i);
				if (!currentagent.isActive()) {
					continue;
				}
				
				float currentDistance = dist(currentagent.getX(), currentagent.getY(), transformedMouseX, transformedMouseY);
				if (currentDistance < minimumDistance) {
					minimumDistance = currentDistance;
					draggedAgentIndex = i;
				}
			}
		}

	}
	
	// processing method mouseDragged() - move agents with mouse drag
	public void mouseDragged()
	{
		
		if (debugMode) {
			
			// check if agent is selected
			if (draggedAgentIndex == -1) {
				return;
			}
			
			// transform window-click space in kinect space 
			float transformedMouseX = constrain(map(mouseX, 0, width, minKinectWidth, maxKinectWidth), minKinectWidth, maxKinectWidth);
			float transformedMouseY = constrain(map(mouseY, 0, height, maxKinectHeight, minKinectHeight), minKinectHeight, maxKinectHeight);
			
			// get selected agent from array
			Agent agent = agentsInKinectSpace.get(draggedAgentIndex);
			// set agent's new position
			agent.setAgentPosition( transformedMouseX, transformedMouseY);
		}
		
	}
	
	// processing method mouseClicked() - left click add new agent, right click remove selected agent	
	public void mouseClicked()
	{
		
		if (debugMode) {
			
			if (mouseButton == LEFT) {
				
				if (numberOfActiveAgentsInKinectSpace < 5) {
					
					for (int i = 0; i < 5; i++) {
						
						Agent agent = agentsInKinectSpace.get(i);
						
						// set new agent to the first free position in array
						if (!agent.isActive()) {
							
							// transform window-click space in kinect space
							agent.setAgentPosition( map(mouseX, 0, width, minKinectWidth, maxKinectWidth),
						                            map(mouseY, 0, height, maxKinectHeight, minKinectHeight));
							agent.setActive(true);
							numberOfActiveAgentsInKinectSpace++;
							
							if (printMode) {
								System.out.println("\nAgent is active: AgentId = " + i + ", PositionInKinectSpace = (" + agent.getX() + ", " + agent.getY() + "), " +
										           "NumberOfActiveAgents = " + numberOfActiveAgentsInKinectSpace);
							}
							
							return;
						}
					}
				}
			} 
			
			if (mouseButton == RIGHT) {
				
				if (draggedAgentIndex != -1) {
					
					Agent agent = agentsInKinectSpace.get(draggedAgentIndex);	
					agent.setActive(false);
					numberOfActiveAgentsInKinectSpace--;
					
					if (printMode) {
						System.out.println("\nInactive agent: AgentId = " + draggedAgentIndex + ", PositionInKinectSpace = (" + agent.getX() + ", " + agent.getY() + "), " +
								           "NumberOfActiveAgents = " + numberOfActiveAgentsInKinectSpace);
					}
				}
			}
		}

	}
	

	// processing method keyPressed()
	public void keyPressed()
	{
		if (debugMode) {
			// initialize music controller (listener)
			if (key == 'm') {
				musicController.printOscMessages();
			}
		}
		
	}
	
}
