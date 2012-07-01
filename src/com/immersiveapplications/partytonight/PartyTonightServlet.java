package com.immersiveapplications.partytonight;

import javax.jdo.PersistenceManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.logging.Logger;

/**
 * Stores and retrieves parties
 *
 * @author Jacob Jewell
 */
@SuppressWarnings({"SerializableHasSerializationMethods", "ClassNamePrefixedWithPackageName"})
public class PartyTonightServlet extends HttpServlet {

    //Logs errors to Google App Engine
    private static final Logger LOG = Logger.getLogger(PartyTonightServlet.class.getName());
    private static final long serialVersionUID = 328848504627163084L;

    /*
      * Responds to HTTP GET Requests and returns all parties within 10 miles of the latitude and longitude from the request
      * (non-Javadoc)
      * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
      */
    @SuppressWarnings("HardcodedFileSeparator")
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {

        try {

            //The latitude and longitude from the GET Request
            Double latitude = Double.parseDouble(req.getParameter("lat"));
            Double longitude = Double.parseDouble(req.getParameter("lng"));

            //The callback value from the GET Request if JSON is being used
            String callback = req.getParameter("callback");

            //The collection of parties within the location of the request
            Parties parties = new Parties(latitude, longitude);

            //If the requested content type is javascript return the parties as a JSON file
            //application/javascript is used instead of application/json to allow cross domain requests
            if (req.getHeader("Accept").contains("application/javascript")) {
                resp.setContentType("application/javascript");
                resp.getWriter().print(parties.toJSON(callback));
            } else { //If JSON is not being requested return the parties as a plist file (Apple's XML format)
                resp.setContentType("Application/XML");
                resp.getWriter().print(parties.toPlist());
            }

            resp.setStatus(HttpURLConnection.HTTP_OK);

        } catch (Exception e) {
            resp.setStatus(HttpURLConnection.HTTP_INTERNAL_ERROR);
            LOG.severe(e.getMessage());

        }

    }

    /*
      * Responds to HTTP POST Requests and either stores or updates a party.
      * If a user has already rated the party in the last 16 hours then no records are changed
      * (non-Javadoc)
      * @see javax.servlet.http.HttpServlet#doPut(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
      */
    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {

        //Get a reference to the persistence manager
        PersistenceManager pm = PMF.get().getPersistenceManager();

        try {

            //The details of the party rating :
            //latitude, longitude, apartment number, the actual rating, whether the party is busted, and a unique identifier
            Double latitude = Double.parseDouble(req.getParameter("lat"));
            Double longitude = Double.parseDouble(req.getParameter("lng"));
            String apartment = req.getParameter("apt");
            Integer rating = Integer.parseInt(req.getParameter("rating"));
            Boolean busted = Boolean.parseBoolean(req.getParameter("busted"));
            String id = req.getParameter("id"); //A unique id (currently either a Facebook id or iPhone id)

            if(PartyFactory.storeParty(latitude, longitude, apartment, busted, rating, id, pm)) {
                resp.setStatus(HttpURLConnection.HTTP_CREATED);
            } else { //If the user already rated the party notify them that the rating was accepted but not stored
                resp.setStatus(HttpURLConnection.HTTP_ACCEPTED);
            }

        } catch (Exception e) {
            LOG.severe(e.getMessage());
            resp.setStatus(HttpURLConnection.HTTP_BAD_REQUEST);
        } finally {
            pm.close(); //Make sure the persistence manager is closed
        }
    }
}
