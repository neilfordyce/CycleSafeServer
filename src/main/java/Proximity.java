/**
 * Entity class representing a distance and direction between two 
 * vehicles
 * To be packaged into JSON or sending to (lorry) clients.
 * @author Calum
 */
public class Proximity 
{
//    enum DIRECTION
//    {
//       NORTH,
//       NORTH_EAST,
//       EAST,
//       SOUTH_EAST,
//       SOUTH,
//       SOUTH_WEST,
//       WEST,
//       NORTH_WEST
//    }    
    private double distance;
//    private DIRECTION direction;
    
    public Proximity(double distance)
    {
        this.distance = distance;
//        this.direction = direction;
    }
    
    public double getDistance()
    {
        return distance;
    }
}
