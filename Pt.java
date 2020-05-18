public class Pt implements Positionable
{
    private double x, y;

    public Pt(double x, double y)
    {
        this.x = x;
        this.y = y;
    }

    public Pt(int x, int y)
    {
        this.x = (double)x;
        this.y = (double)y;
    }
    
    public double getX() { return x; }
    public double getY() { return y; }
    
    public void setPosition(double x, double y)
    {
        this.x = x;
        this.y = y;
    }

    public double distanceSquared(Pt other)
    {
        double diffX = other.x - x;
        double diffY = other.y - y;
        return diffX * diffX + diffY * diffY;
    }
    
    public String toString()
    {
        return "(" + x + "," + y + ")";
    }
}
