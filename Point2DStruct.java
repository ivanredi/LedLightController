

/**
 * A lightweight implementation of the {@link Point2D} interface.
 * @author niri
 */
public class Point2DStruct implements Point2D
{
	private float x;
	private float y;
	
	public Point2DStruct(float x, float y)
	{
		super();
		this.x = x;
		this.y = y;
	}
	
	@Override
	public float getX()
	{
		return x;
	}

	@Override
	public float getY()
	{
		return y;
	}

	@Override
	public void setX(float x)
	{
		this.x = x;
	}

	@Override
	public void setY(float y)
	{
		this.y = y;
	}

}