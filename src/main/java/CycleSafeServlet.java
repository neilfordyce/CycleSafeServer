
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.javadocmd.simplelatlng.*;
import com.javadocmd.simplelatlng.util.LengthUnit;

import com.google.gson.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import javax.servlet.ServletOutputStream;

import java.util.logging.Logger;

/**
 * Main Servlet to receive GPS data from Bikes and Lorries
 * Performs proximity logic and notifies Lorry drivers.
 * @author Calum
 */
@WebServlet(urlPatterns = {"/*"})
public class CycleSafeServlet extends HttpServlet 
{
    private static final int DISTANCE_THRESHOLD        = 20;
    private static final int DANGER_DISTANCE_THRESHOLD = 10;

    private HashMap<Integer, Bike>  bikeMap  = new HashMap<Integer, Bike>();
    private HashMap<Integer, Lorry> lorryMap = new HashMap<Integer, Lorry>();
   
    private static void initLogFile() 
    {
        try 
        {
            Handler fileHandler = new FileHandler("/tmp/log");
            Logger.getLogger("").addHandler(fileHandler);
        } 
        catch (IOException ex) 
        {
            Logger.getLogger(CycleSafeServlet.class.getName()).log(Level.SEVERE, null, ex);
        } 
        catch (SecurityException ex) 
        {
            Logger.getLogger(CycleSafeServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }    
    
    /**
     * Handles the HTTP
     * <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
    {
        initLogFile();
        
        Logger.getLogger(CycleSafeServlet.class.getName()).log(Level.INFO, "In doGet ({0})", request.getRequestURI());
        
        // Get requested ID from URI
        int id = Integer.parseInt(request.getParameter("id"));
        Logger.getLogger(CycleSafeServlet.class.getName()).log(Level.INFO, "Lorry Parameter ID: {0}", id);
        
        ServletOutputStream output = response.getOutputStream();
        
        if (lorryMap.containsKey(id))
        {
            Lorry lorry = lorryMap.get(id);
            String jsonCyclists = findNearByCyclists(lorry);
            output.println(jsonCyclists);
            Logger.getLogger(CycleSafeServlet.class.getName()).log(Level.INFO, "ID Valid: Cyclists: {0}", jsonCyclists);
        }
        else
        {
            output.println("Invalid Lorry ID");
            Logger.getLogger(CycleSafeServlet.class.getName()).log(Level.INFO, "Invalid Lorry ID");
        }
    }

    /**
     * Handles the HTTP
     * <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
    {
        initLogFile();

        Logger.getLogger(CycleSafeServlet.class.getName()).log(Level.INFO, "In doPost");

        Logger.getLogger(CycleSafeServlet.class.getName()).log(Level.INFO, "Request: {0}", request.getParameterNames());

        //Extract the parameters
        int type = 0;
        int id = 0;
        double latitude = 0.0;
        double longitude = 0.0;
        try
        {
            type      = Integer.parseInt(request.getParameter("type"));
            id        = Integer.parseInt(request.getParameter("id"));
            longitude = Double.parseDouble(request.getParameter("long"));
            latitude  = Double.parseDouble(request.getParameter("lat"));
        }
        catch (Exception e)
        {
            Logger.getLogger(CycleSafeServlet.class.getName()).log(Level.SEVERE, "Paramter exception: {0}, {1}", new Object[]{e.getClass().getName(), e.getMessage()});
        }
        Logger.getLogger(CycleSafeServlet.class.getName()).log(Level.INFO, "Type param: {0}", type);
        Logger.getLogger(CycleSafeServlet.class.getName()).log(Level.INFO, "ID param: {0}", id);
        Logger.getLogger(CycleSafeServlet.class.getName()).log(Level.INFO, "Longitude param: {0}", longitude);
        Logger.getLogger(CycleSafeServlet.class.getName()).log(Level.INFO, "Latitude param: {0}", latitude);

        // Update position OR put new Vehicle into appropriate map
        if (type == 0)  // Bike
        {
            if (bikeMap.containsKey(id))
            {
                bikeMap.get(id).update(latitude, longitude);
                Logger.getLogger(CycleSafeServlet.class.getName()).log(Level.INFO, "Updating bike with {0}, {1}", new Object[]{latitude, longitude});
            }            
            else
            {
                bikeMap.put(id, new Bike(latitude, longitude));
                Logger.getLogger(CycleSafeServlet.class.getName()).log(Level.INFO, "Creating new Bike");
            }
        }
        else if (type == 1)    // Lorry
        {
            if (lorryMap.containsKey(id))
            {
                lorryMap.get(id).update(latitude, longitude);
                Logger.getLogger(CycleSafeServlet.class.getName()).log(Level.INFO, "Updating lorry with {0}, {1}", new Object[]{latitude, longitude});
            }
            else
            {
                lorryMap.put(id, new Lorry(latitude, longitude));
                Logger.getLogger(CycleSafeServlet.class.getName()).log(Level.INFO, "Creating new lorry");
            }
        }
       
    }

    private String findNearByCyclists(Lorry lorry)
    {
        List<Proximity> cyclists = new ArrayList<Proximity>();
        for (Bike bike : bikeMap.values())
        {
            double lorryLat = lorry.getLatitude();
            double lorryLng = lorry.getLongitude();
            double bikeLat = bike.getLatitude();
            double bikeLng = bike.getLongitude();
            
            double distance = calculateDistance(lorryLat, lorryLng,
                                                bikeLat,  bikeLng);

            Logger.getLogger(CycleSafeServlet.class.getName()).log(Level.INFO, "Distance: {0}", distance);
            
            //TODO: Calculate direction
            if (distance > DISTANCE_THRESHOLD)
                cyclists.add(new Proximity(distance, bikeLat, bikeLng));

        }
        
        // Package in JSON to return to lorry client
        Gson gson = new Gson();
        String jsonCyclists = gson.toJson(cyclists);
        return jsonCyclists;
    }
    
    private double calculateDistance(double lat1, double lng1, double lat2, double lng2)
    {
        LatLng p1 = new LatLng(lat1, lng1);
        LatLng p2 = new LatLng(lat2, lng2);
        
        return LatLngTool.distance(p1, p2, LengthUnit.METER); 
    }
}
