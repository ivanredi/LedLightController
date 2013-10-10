

import java.io.FileWriter;
import java.io.IOException;
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

	private static final long serialVersionUID = -8381361181536369840L;
	
	// debug and print modes
	boolean debugMode = false;
	boolean printMode = false;
	boolean runLedScreen = true;
	boolean runSimulator = false;
	boolean runArtNet = false;
	boolean printAgentsPositions = false;
	boolean fileSaveMode = false; // save OSC packages to the file
	boolean visualizationFileSaveMode = true;
	String filesDestination = "/Users/ivanredi/Documents/RPC";
	String visualizationFilesDestination = "/Users/ivanredi/Documents/RPC/Visualization/visualisationData.txt";
	int maximumInactiveDuration = 3000;	// Time interval before a questionably inactive agent disappears, in milliseconds.
	int maximumEdgeInactiveDuration = 1000;	// Time interval before a questionably inactive agent near the edge disappears, in milliseconds.
	int maxColorStep = 5;
	
	// agents space size
	float[] adjustBrightness = { 	0.006f, // 1 active agent
						            0.30f, // 2 active agents
						            6.0f, // 3 active agents
						            100.0f, // 4 active agents
						            3000.0f // 5 active agents 
			                    }; 
	
	// color values for each mode
	int[][] adjustColor = { {255, 255, 255}, //mode 1
			                {255, 20, 0}, //mode 2
			                {20, 250, 250}, //mode 3
			                {50, 255, 50} //mode 4
			              };
	
	float narrowRangeFromCentroid = 14.0f; 	// maximum range for the agent to stay in the narrow group
	
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
	
	// LED SCREEN DIMENSIONS
	int ledScreenWidth = 1232;
	int ledScreenHeight = 880;

	// OSC NETWORK INSTANCES
	OscP5 oscP5;
	int kinectPort = 7000;
	int debugKinectPort = 6999;
	int musicPort = 7001;
    int musicPortForDebug = 7002;
    
	// OSC destination for debugging purposes
	NetAddress eventDestinationForKinectOscPackages;
	NetAddress eventDestinationForMusicOscPackages;
	NetAddress eventDestinationForDebugMusicOscPackages;

	// CONTROLLERS INSTANCES
	AgentController agentController;
	AgentGroupController agentNarrowGroupController;
	int agentRadius = 0; // diameter of the surface that agent takes in space (not in use)
	int systemMode = 0; // mode for agents group position
	MusicController musicController; // music controller variables
	float maxAgentStepSize = 6; // smooth variable for agents motion (agent jumps)
	PGraphics pGraphicsBuffer; // drawing buffer
	ArrayList<Agent> agentsInKinectSpace; // array for kinect simulator and debug purposes 
	ArrayList<Agent> previousAgentsState  = new ArrayList<Agent>(); //array for debug-print mode
	int numberOfActiveAgentsInKinectSpace; // variable for kinect simulator and debug purposes 
	int draggedAgentIndex = -1;  // variable for kinect simulator and debug purposes 
	int[] currentColor = {255, 255, 255};
	
	FileWriter visualizationDataFileWriter = null;
	
	// processing method setup()
	public void setup()
	{
                frameRate(12);
                for (int i = 0; i < 3; i++) {
                  currentColor[i] = adjustColor[0][i];
                }
		oscP5 = new OscP5(this, 0); // osc-network initialization
		
		if (debugMode) { // create osc-event destinations for kinect-motion and music listener for debug mode
			
			agentsInKinectSpace = new ArrayList<Agent>();
			for (int i = 0; i < 5; i++) {
				agentsInKinectSpace.add(new Agent(i));
			}
			
			eventDestinationForKinectOscPackages = new NetAddress("127.0.0.1", debugKinectPort);
			eventDestinationForDebugMusicOscPackages = new NetAddress("127.0.0.1", musicPortForDebug);
			
			musicController = new MusicController(musicPortForDebug);
		}
		
		eventDestinationForMusicOscPackages = new NetAddress("127.0.0.1", musicPort); // create osc-event destination for music
		
		frame.setTitle("Led Screen Controller"); // processing frame initialization (frame title)
		size(560, 400);	// (frame size)
		
		pGraphicsBuffer = createGraphics(maxProcessingSpaceWidth, maxProcessingSpaceHeight);// create processing buffer for drawing

		// agent_controller initialization
		if (! debugMode) { // listening port based on debug mode
			agentController = new AgentController(agentRadius, fileSaveMode, maxAgentStepSize, kinectPort, filesDestination, maximumInactiveDuration, maximumEdgeInactiveDuration);
		} else {
			agentController = new AgentController(agentRadius, fileSaveMode, maxAgentStepSize, debugKinectPort, filesDestination, maximumInactiveDuration, maximumEdgeInactiveDuration);
		}
		// forward coordinates to the agent controller
		agentController.setBufferCoordinates(minProcessingSpaceWidth, maxProcessingSpaceWidth, minProcessingSpaceHeight, maxProcessingSpaceHeight);
		agentController.setKinectCoordinates(minKinectWidth, maxKinectWidth, minKinectHeight, maxKinectHeight);
		
		if(printAgentsPositions) {// for printing the agents position in processing buffer space
			previousAgentsState.addAll(agentController.agents);
		}
		
		// initialize agent_group_controller with agents in system; set max distance from centroid
		agentNarrowGroupController = new AgentGroupController(agentController.agents, narrowRangeFromCentroid);
		
		if (runLedScreen) { // led simulator is turned on
			//crate screen for LED simulator 
			LedScreen.buffer = this.pGraphicsBuffer;
			LedScreen.setWidth = ledScreenWidth;
			LedScreen.setHeight = ledScreenHeight;
                        LedScreen.runSimulator = runSimulator;
                        LedScreen.runArtNet = runArtNet;
			PApplet.main(new String[] { LedScreen.class.getName() });
		}
		
		if (visualizationFileSaveMode) {
			try {
				visualizationDataFileWriter = new FileWriter(visualizationFilesDestination);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		smooth();
	}
	
	// processing method draw()
	public void draw() 
	{
 		//background(0);
		sendAgentsStatusToControllers(); // refresh osc-messages

		try { // check current active agents in system (provide save access to the agents array with semaphore)
			agentController.controlAgentArrayAccess.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		agentNarrowGroupController.getActiveAgentsInSystem(agentController.agents);
		
		int mode = agentNarrowGroupController.getModeControl(); // returns mode from the AgentGroupController
		
		if (visualizationFileSaveMode) {
			try {
				visualizationDataFileWriter.append(String.valueOf(mode));
				if (mode >= 2) {
					visualizationDataFileWriter.append("\t" + agentNarrowGroupController.centroid.getX() + "\t" + agentNarrowGroupController.centroid.getY());
				} else {
					visualizationDataFileWriter.append("\t0\t0");
				}
				for (Agent agent : agentController.agents) {
					boolean active = agent.isActive();
					visualizationDataFileWriter.append("\t" + (active ? 1 : 0));
				}
				for (Agent agent : agentController.agents) {
					visualizationDataFileWriter.append("\t" + agent.getX() + "\t" + agent.getY());
				}
				visualizationDataFileWriter.append("\t" + System.currentTimeMillis() + "\n");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		agentController.controlAgentArrayAccess.release(); // release semaphore
		
		if (printMode) { // console print mode
			
			if (this.systemMode != mode) { // print system mode
				System.out.println("\nMode " + mode);
				this.systemMode = mode;
			}
			
			if (printAgentsPositions) { // prints the agents position in processing buffer space
				for (int i = 0; i < 5; i ++) {
					Agent agent = agentController.agents.get(i);
					Agent previousAgentState = previousAgentsState.get(i);
					if(agent.isActive() && previousAgentState.isActive()) {
						if(agent.getX() != previousAgentState.getX() || agent.getY() != previousAgentState.getY()) {
							System.out.println("Agent position changed: AgentId = " + agent.getAgentId() + ", " +
									           "PositionInProcessingSpace = (" + agent.getX() + ", " + agent.getY() + ")") ;
						}
					}
				}
				previousAgentsState = new ArrayList<Agent>();
				previousAgentsState.addAll(agentController.agents);
			}
		}
		
		if (runLedScreen) { // led simulator is turned on
			try { // provide thread safe rendering on LED simulator with semaphore
				LedScreen.allowBufferRenderer.acquire();
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}
		
		pGraphicsBuffer.beginDraw(); // prepare and fill buffer
		pGraphicsBuffer.background(0);

		drawWithPixel(pGraphicsBuffer, mode); // draw in buffer
		
		pGraphicsBuffer.endDraw(); // close buffer

		if (runLedScreen) { // led simulator is turned on
			LedScreen.allowBufferRenderer.release(); // release semaphore for led simulator
		}
		PImage pImage = pGraphicsBuffer.get(); // display image from buffer in main screen
		image(pImage, 0, 0, width, height);
		
		pGraphicsBuffer.removeCache(pImage); // delete memory cache
                
		g.removeCache(pImage);
		
	}

	@Override
	public void destroy()
	{
		background(0);
		super.destroy();
	}

	/**
	 * Implementation of the drawing algorithm. Method is checking each pixel in the buffer network of pixels
	 * and calculate his brightness value based on the number of the agents in the system and
	 * their positions. Color of the pixel is calculating based on the system mode.
	 * @param buffer
	 * @param mode
	 */
	void drawWithPixel(PGraphics buffer, int mode)
	{
		float[][] pixi = new float[buffer.width][buffer.height];
		
		int numberOfActiveAgents = agentNarrowGroupController.activeAgentsInSystem.size();
		if (numberOfActiveAgents > 0) {
			
			// loop matrix of pixels to set pixels value for darkness
			for (int i = 0; i < buffer.width; i++) {
				for (int j = 0; j < buffer.height; j++) {

					float darkness = 1;
					float minDarkness = Float.MAX_VALUE;
					
					int numberOfAgentsInGroup = agentNarrowGroupController.agentsInGroup.size();
					// if group is created calculate darkness value of each pixel for agents in group
					if (numberOfAgentsInGroup > 0) {
						
						for(int a = 0; a < numberOfAgentsInGroup; a++) { // loop agents in group
							
							Agent agent = agentNarrowGroupController.agentsInGroup.get(a);						
							float pixelDistFromAgent = dist(i, j, agent.getX(), agent.getY());
							darkness *= pixelDistFromAgent;
							
						}
						float brightnes = adjustBrightness[numberOfAgentsInGroup - 1]; // add pixel size around agents in group
						darkness /= brightnes;
						if (darkness < minDarkness) {
							minDarkness = darkness;
						}
					}
					darkness = 1;
					
					for (int a = 0; a < numberOfActiveAgents; a++) { // loop all active agents in system
						
						Agent agent = agentNarrowGroupController.activeAgentsInSystem.get(a);						
						float pixelDistFromAgent = dist(i, j, agent.getX(), agent.getY());
						darkness *= pixelDistFromAgent;
					}
										
					float brightnes = adjustBrightness[numberOfActiveAgents - 1];
					darkness /= brightnes;
					
					// chose minimum darkness between pixel for agents in group and pixel for all agents
					if (darkness < minDarkness) {
						minDarkness = darkness;
					}
				    
					pixi[i][j] = (float) (255 - minDarkness); // set pixel value for darkness
				}
			}
		}
		currentColor[0] = constrain(adjustColor[mode - 1][0], currentColor[0] - maxColorStep, currentColor[0] + maxColorStep);
		currentColor[1] = constrain(adjustColor[mode - 1][1], currentColor[1] - maxColorStep, currentColor[1] + maxColorStep);
		currentColor[2] = constrain(adjustColor[mode - 1][2], currentColor[2] - maxColorStep, currentColor[2] + maxColorStep);
		//  loop matrix of pixels to coloring pixels
		for (int i = 0; i < buffer.width; i++) { 
			for (int j = 0; j < buffer.height; j++) {

				float pixel = pixi[i][j] / 255.0f;
				// set pixel color based on mode
				buffer.stroke(	currentColor[0] * pixel,
								currentColor[1] * pixel,
								currentColor[2] * pixel,
								255);
				buffer.point(i, j);// draw pixel point
			}
		}
		
	}
	void sendAgentsStatusToControllers()
	{
		
		if (debugMode) { // create OSC messages for kinect simulation (for debug purposes )

			for (int i = 0; i < agentsInKinectSpace.size(); i++) {
				
				Agent currentAgent = agentsInKinectSpace.get(i);
				
				OscMessage oscMessage = new OscMessage("/kinectMessageSimulator");
				oscMessage.add(currentAgent.getAgentId());// OSC message field = agent id (int)
				oscMessage.add(currentAgent.isActive() ? 1 : 0); // OSC message field = agent is active (int)
				oscMessage.add(currentAgent.getX()); // OSC message field = agent x position (float)
				oscMessage.add(currentAgent.getY()); // OSC message field = agent y position (float)
				oscMessage.add(currentAgent.getNearestAgentDistance()); // OSC message field = agent distance from nearest agent (float)
				oscMessage.add(currentAgent.getAngle()); // OSC message field = angle between 0.0 and agent position (float)
				
				oscP5.send(oscMessage, eventDestinationForKinectOscPackages); // send OSC message for each agent
			}
			
		}
		
		agentNarrowGroupController.getActiveAgentsInSystem(agentController.agents); // check current active agents in system
		int mode = agentNarrowGroupController.getModeControl(); // get returned mode from agentGroupController

		Point2D centroid = agentNarrowGroupController.centroid;
		Point2D mappedCentroidPosition = mapProcessingSpaceToMusicSpace(centroid.getX(), centroid.getY()); // map centroid position to music space
		
		ArrayList<Agent> activeAgents = agentNarrowGroupController.activeAgentsInSystem;
		int numberOfActiveAgents = activeAgents.size();

		// create OSC message for music-controller
		OscMessage oscMessage = new OscMessage("/position");
        oscMessage.add(mode);  // OSC message field = interaction mode (int)
		oscMessage.add(numberOfActiveAgents); // OSC message field = number of active agents (int)
		oscMessage.add(mappedCentroidPosition.getX()); // OSC message field = x for centroid (float)
		oscMessage.add(mappedCentroidPosition.getY()); // OSC message field = y for centroid (float)

		if(debugMode) { // send osc-music message to the event destination for music controller on debug mode
			oscP5.send(oscMessage, eventDestinationForDebugMusicOscPackages);
		}
		oscP5.send(oscMessage, eventDestinationForMusicOscPackages); // send osc-music message to the real music event destination

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
				
				Agent currentAgent = agentsInKinectSpace.get(i);
				if (!currentAgent.isActive()) {
					continue;
				}
				
				float currentDistance = dist(currentAgent.getX(), currentAgent.getY(), transformedMouseX, transformedMouseY);
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
			
			if (draggedAgentIndex == -1) { // check if agent is selected
				return;
			}
			
			// map window-click space in kinect space 
			float transformedMouseX = constrain(map(mouseX, 0, width, minKinectWidth, maxKinectWidth), minKinectWidth, maxKinectWidth);
			float transformedMouseY = constrain(map(mouseY, 0, height, maxKinectHeight, minKinectHeight), minKinectHeight, maxKinectHeight);
			
			Agent agent = agentsInKinectSpace.get(draggedAgentIndex); // get selected agent from array
			agent.setAgentPosition( transformedMouseX, transformedMouseY); // set agent's new position
		}
		
	}
	
	// processing method mouseClicked() - left click add new agent, right click remove selected agent	
	public void mouseClicked()
	{
		
		if (debugMode) {
			
			if (mouseButton == LEFT) {
				
				if (numberOfActiveAgentsInKinectSpace < 5) {
					
					for (int i = 0; i < 5; i++) { // loop all agents
						Agent agent = agentsInKinectSpace.get(i);
						if (!agent.isActive()) { // set new agent to the first free position in array
							
							// map window-click space in kinect space
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
	

	// processing method keyPressed() - prints music-controller osc messages
	public void keyPressed()
	{
		if (debugMode) {
			if (key == 'm') { // initialize music controller (listener)
				musicController.printOscMessages();
			}
		}
	}
	
	//private PApplet pApplet;
	// default constructor
	public LedLightController()
	{
		super();
	}

	// constructor for LedLightController class
	public LedLightController(int minKinectWidth, int maxKinectWidth, int minKinectHeight, int maxKinectHeight, 
			  int kinectPort, int musicPort, int musicPortForDebug,
			  float maxDistanceFromCentroid, 
			  float[] adjustBrightness,
			  int[][] adjustColor, 
			  boolean debugMode, 
			  boolean printMode,
			  boolean printAgentsPositions, 
			  boolean runLedSimulator, 
			  float maxAgentStepSize, 
			  PApplet pApplet)
							{
								super();
								this.minKinectWidth = minKinectWidth;
								this.maxKinectWidth = maxKinectWidth;
								this.minKinectHeight = minKinectHeight;
								this.maxKinectHeight = maxKinectHeight;
								this.kinectPort = kinectPort;
								this.musicPort = musicPort;
								this.musicPortForDebug = musicPortForDebug;
								this.narrowRangeFromCentroid = maxDistanceFromCentroid;
								this.adjustBrightness = adjustBrightness;
								this.adjustColor = adjustColor;
								this.debugMode = debugMode;
								this.printMode = printMode;
								this.printAgentsPositions = printAgentsPositions;
								this.runLedScreen = runLedSimulator;
								this.maxAgentStepSize = maxAgentStepSize;
								//this.pApplet = pApplet;
							}
	
}