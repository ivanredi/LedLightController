
import java.util.ArrayList;
import processing.core.*;

/**
 * Determines system modes for the active users.
 * @author niri
 */
public class AgentGroupController 
{
	public ArrayList<Agent> activeAgentsInSystem;	
	public ArrayList<Agent> agentsInGroup;
	
	public Point2D centroid;
	
	private int mode;
	private boolean groupIsCreated;
	private float maxDistanceFromCentroid;
	
	public AgentGroupController(ArrayList<Agent> agentsInSystem, float maxDistanceFromCentroid)
	{
		super();
		getActiveAgentsInSystem(agentsInSystem);
		this.maxDistanceFromCentroid = maxDistanceFromCentroid;
		
	}

	
	public void getActiveAgentsInSystem(ArrayList<Agent> agentsInSystem)
	{
		activeAgentsInSystem = new ArrayList<Agent>();
		for (int i = 0; i < agentsInSystem.size(); i ++) {
			
			Agent agent = agentsInSystem.get(i);
			if(agent.isActive()) {
				activeAgentsInSystem.add(agent);
			}
		}
		
		setMode();
	}
	
	private void setMode() {
		
		int numberOfActiveAgentsInSystem = activeAgentsInSystem.size();
		
		// (MODE 1) zero or one active agents in the system 
		if (numberOfActiveAgentsInSystem <= 1) {
			mode = 1;
			groupIsCreated = false;
		}

		// more then one agent
		if (numberOfActiveAgentsInSystem > 1) {
			
			// (MODE 2) no group 
			if (!groupIsCreated) {
				mode = 2;
			}
			else {
				// (MODE 4) group is created
				if (agentsInGroup.size() < numberOfActiveAgentsInSystem) {
					mode = 4;
				} else {
					// (MODE 3) group is created, all agents are in group
					mode = 3;
				}
			}
		}
		
	}
	
	public int getModeControl()
	{
		
		setMode();

		if (mode == 1 || mode == 2) {
			getSystemCentroid();
		}
		
		if (mode == 3 || mode == 4) {
			getGroupCentroid();
		}
		
		groupCreating();
		
		return mode;
	}

	// get centroid when group is not created
	private Point2D getSystemCentroid() 
	{
		centroid =  Point2DCalculations.getCentroid(activeAgentsInSystem);
		return centroid;
	}
	
	// get centroid when group is created
	private Point2D getGroupCentroid()
	{
		centroid =  Point2DCalculations.getCentroid(agentsInGroup);
		return centroid;

	}
	
	
	private void groupCreating()
	{
		
		ArrayList<Agent> agentsInGroup = new ArrayList<Agent>();
		
		int numberOfActiveAgentsInSystem = activeAgentsInSystem.size();

                if (numberOfActiveAgentsInSystem == 2) {
                  Agent agent1 = activeAgentsInSystem.get(0);
                  Agent agent2 = activeAgentsInSystem.get(1);
                  if (PApplet.dist(agent1.getX(), agent1.getY(), agent2.getX(), agent2.getY()) <= maxDistanceFromCentroid) {
                    agentsInGroup.add(agent1);
                    agentsInGroup.add(agent2);
                  }
                }
		else {
		for(int i = 0; i < numberOfActiveAgentsInSystem; i++) {
			
			Agent agent = activeAgentsInSystem.get(i);
			
			float agentDistanceFromCentroid = PApplet.dist(agent.getX(), agent.getY(), centroid.getX(), centroid.getY());
			
			// agent.setDistanceFromCentroid(agentDistanceFromCentroid);
			
			if (agentDistanceFromCentroid <= maxDistanceFromCentroid) {
				agentsInGroup.add(agent);
			}
		}
                if (agentsInGroup.size() == 2) {
                 Agent agent1 = agentsInGroup.get(0);
                  Agent agent2 = agentsInGroup.get(1);
                  if (PApplet.dist(agent1.getX(), agent1.getY(), agent2.getX(), agent2.getY()) > maxDistanceFromCentroid) {
                    agentsInGroup = new ArrayList<Agent>();
                  }
                }
		}
		if (agentsInGroup.size() > 1) {
			this.agentsInGroup = agentsInGroup;
			groupIsCreated = true;
		} else {
			this.agentsInGroup = new ArrayList<Agent>();
			groupIsCreated = false;
		}
		
	}
	
}
