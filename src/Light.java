public class Light
{

	private int	uniqueID	= 0;
	private int	index		= 0;
	private double	x	= 0, y = 0;
	private double	r	= 0, theta = 0;
	private int		next	= 0, prev = 0;
	private double	debug_startX	= 0, debug_endX = 0, debug_startY = 0, debug_endY = 0;
	public int		portRed			= 0, portBlue = 0, portGreen = 0;

	public double getDebug_endX()
	{
		return debug_endX;
	}

	public void setDebug_endX(double debug_endX)
	{
		this.debug_endX = debug_endX;
	}

	public double getDebug_startY()
	{
		return debug_startY;
	}

	public void setDebug_startY(double debug_startY)
	{
		this.debug_startY = debug_startY;
	}

	public double getDebug_endY()
	{
		return debug_endY;
	}

	public void setDebug_endY(double debug_endY)
	{
		this.debug_endY = debug_endY;
	}

	public Light()
	{

	}

	public Light(int uniqueID, int index, double sx, double ex, double sy, double ey, int next, int prev)
	{
		this.setUniqueID(uniqueID);
		this.setIndex(index);
		this.setDebug_startX(sx);
		this.setDebug_endX(ex);
		this.setDebug_startY(sy);
		this.setDebug_endY(ey);
		this.setX((sx + ex) / 2d);
		this.setY((sy + ey) / 2d);
		this.setTheta(Math.atan(y / x));
		this.setR(Math.sqrt(x * x + y * y));
		this.setNext(next);
		this.setPrev(prev);
	}

	public void setPorts(int r, int g, int b)
	{
		this.portRed = r;
		this.portGreen = g;
		this.portBlue = b;
	}

	public int getIndex()
	{
		return index;
	}

	public void setIndex(int index)
	{
		this.index = index;
	}

	public double getX()
	{
		return x;
	}

	public void setX(double x)
	{
		this.x = x;
	}

	public double getY()
	{
		return y;
	}

	public void setY(double y)
	{
		this.y = y;
	}

	public double getR()
	{
		return r;
	}

	public void setR(double r)
	{
		this.r = r;
	}

	public double getTheta()
	{
		return theta;
	}

	public void setTheta(double theta)
	{
		this.theta = theta;
	}

	public int getNext()
	{
		return next;
	}

	public void setNext(int next)
	{
		this.next = next;
	}

	public int getPrev()
	{
		return prev;
	}

	public void setPrev(int prev)
	{
		this.prev = prev;
	}

	public int getUniqueID()
	{
		return uniqueID;
	}

	public void setUniqueID(int uniqueID)
	{
		this.uniqueID = uniqueID;
	}

	public double getDebug_startX()
	{
		return debug_startX;
	}

	public void setDebug_startX(double debug_startX)
	{
		this.debug_startX = debug_startX;
	}
}
