

/**
 * Represent an user in system.
 * @author niri
 *
 */
public class Agent implements Point2D
{
	// agent id
	private int agentId;

	// agent x and y coordinate
	private float xPosition;
	private float yPosition;
	
	// agent is entering in the system
	private boolean isActive;
	
	// number of nearby objects
	private int nearbyObjectCount;
	
	// distances
	private float distanceFromNearestAgent;
	//private float distanceFromCentroid;
	
	// angle form agent to (0, 0) point
	private float angle;
	

	//agent constructor
	public Agent(int agentId)
	{
		super();
		this.agentId = agentId;
		this.isActive = false;
		this.nearbyObjectCount = 0;
		this.distanceFromNearestAgent = 0;
		//this.distanceFromCentroid = 0;
		this.angle = 0;
	}
	
	public void setAgentPosition(float xPos, float yPos)
	{
		this.setX(xPos);
		this.setY(yPos);
	}
	
	public int getAgentId()
	{
		return agentId;
	}
	
	public void setAgentId(int agentId)
	{
		this.agentId = agentId;
	}
	
	public float getX()
	{
		return xPosition;
	}
	
	public void setX(float xPosition)
	{
		this.xPosition = xPosition;
	}
	
	public float getY()
	{
		return yPosition;
	}
	
	public void setY(float yPosition)
	{
		this.yPosition = yPosition;
	}
	
	public int getNearbyObjectCount()
	{
		return nearbyObjectCount;
	}

	public void setNearbyObjectCount(int nearbyObjectCount)
	{
		this.nearbyObjectCount = nearbyObjectCount;
	}

	public float getNearestAgentDistance() 
	{
		return distanceFromNearestAgent;
	}

	public void setNearestAgentDistance(float nearestAgentDistance) 
	{
		this.distanceFromNearestAgent = nearestAgentDistance;
	}
	
	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}
	
	//public float getDistanceFromCentroid()
	//{
	//	return distanceFromCentroid;
	//}

	//public void setDistanceFromCentroid(float distanceFromCentroid)
	//{
	//	this.distanceFromCentroid = distanceFromCentroid;
	//}
	
	public float getAngle()
	{
		return angle;
	}

	public void setAngle(float angle)
	{
		this.angle = angle;
	}

}