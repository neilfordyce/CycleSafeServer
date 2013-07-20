/**
 * Abstract base class representing common functionality 
 * of Lorries and Bikes
 * @author Calum
 */
public class Vehicle 
{
    // TODO: Put into a class (provided by library)?
    protected double prevLatitude;
    protected double prevLongitude;
    protected double latitude;
    protected double longitude;
    
    protected double velocity;
    
    public Vehicle()
    {
        this.latitude = 0.0;
        this.longitude = 0.0;
        
        prevLatitude = 0.0;
        prevLongitude = 0.0;
        
        velocity = 0.0;
    }
    
    public Vehicle(double latitude, double longitude)
    {
        this.latitude = latitude;
        this.longitude = longitude;
        
        prevLatitude = 0.0;
        prevLongitude = 0.0;
        
        velocity = 0.0;
    }

    public void update(double newLatitude, double newLongitude) 
    {
        prevLatitude = latitude;
        prevLongitude = longitude;
        
        latitude = newLatitude;
        longitude = newLongitude;
        
//        velocity = new - old;
    }
    
    public double getLatitude()
    {
        return latitude;
    }

    public double getLongitude()
    {
        return longitude;
    }
    
    public double getVelocity()
    {
        return velocity;
    }
}
