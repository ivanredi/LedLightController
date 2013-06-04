

import java.util.Collection;
import java.util.Iterator;

/**
 * Calculates centroid for two and more points.
 * @author niri
 */
public class Point2DCalculations
{
	public static Point2D getCentroid(Point2D... point2ds)
	{
		int pointCount = point2ds.length;
		float sumX = 0;
		float sumY = 0;
		for (int i = 0; i < pointCount; i++) {
			Point2D currentpoint = point2ds[i];
			sumX += currentpoint.getX();
			sumY += currentpoint.getY();
		}
		float x = sumX / pointCount;
		float y = sumY / pointCount;
		return new Point2DStruct(x, y);
	}
	
	@SuppressWarnings("rawtypes")
	public static Point2D getCentroid(Collection<? extends Point2D> point2ds)
	{
		Point2D[] point2dArray = new Point2D[point2ds.size()];
		int i = 0;
		for (Iterator iterator = point2ds.iterator(); iterator.hasNext();) {
			Point2D point2d = (Point2D) iterator.next();
			point2dArray[i++] = point2d;
		}
		return getCentroid(point2dArray);
	}
}