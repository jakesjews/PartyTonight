package com.immersiveapplications.partytonight;

import com.beoui.geocell.GeocellManager;
import com.beoui.geocell.model.GeocellQuery;
import com.beoui.geocell.model.Point;

import javax.jdo.PersistenceManager;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

class Parties {

    //The maximum distance for which parties will be retrieved
    private static final double DISTANCE_TO_SEARCH = 16093.44;
    //The maximum number of results to return in the party list
    private static final int MAX_RESULTS = 40;
    private static final int MAX_PARTY_AGE_HOURS = -16;
    private static final int STRINGBUILDER_CAPACITY = 200;

    //The list of parties within 10 miles
    private final List<Party> _parties;

    /**
     * Creates a collection of parties within 10 miles of a given latitude and longitude
     *
     * @param latitude  The latitude of the search
     * @param longitude The longitude of the search
     */
    Parties(Double latitude, Double longitude) {
        //Get the persistence manager
        PersistenceManager pm = PMF.get().getPersistenceManager();

        try {

            // Create a geo point from the latitude and longitude
            Point center = new Point(latitude, longitude);

            // We only want to retrieve parties created within the last 16 hours
            List<Object> params = new ArrayList<Object>(1);
            params.add(getMaxAge());
            GeocellQuery query = new GeocellQuery("createdDate > maxAge", "java.util.Date maxAge", params);

            //Run a proximity search with the geocell library
            _parties = GeocellManager.proximitySearch(center, MAX_RESULTS, DISTANCE_TO_SEARCH, Party.class, query, pm);

        } finally {
            pm.close(); //Make sure the persistence manager is closed
        }

    }

    private static Date getMaxAge() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR, MAX_PARTY_AGE_HOURS);
        return cal.getTime();
    }

    /**
     * Returns the list of parties formatted as a plist
     * This is used for iPhone applications
     *
     * @return A plist representation of the parties
     */
    @SuppressWarnings({"HardcodedLineSeparator", "HardcodedFileSeparator"})
    public String toPlist() {

        StringBuilder plistBuilder = new StringBuilder(STRINGBUILDER_CAPACITY);

        // Create the header for the Plist file
        plistBuilder.append("<?xml version='1.0' encoding='UTF-8'?>\n");
        plistBuilder.append("<!DOCTYPE plist PUBLIC '-//Apple//DTD PLIST 1.0//EN' 'http://www.apple.com/DTDs/PropertyList-1.0.dtd'>\n");
        plistBuilder.append("<plist version='1.0'>\n");
        plistBuilder.append("<array>\n");

        for (Party party : _parties) {
            plistBuilder.append(party.toPlist());
        }

        plistBuilder.append("</array>\n");
        plistBuilder.append("</plist>");

        return plistBuilder.toString();

    }

    /**
     * Returns the list of parties formatted as JSON. This is the ideal way to retrieve party data
     *
     * @param callback The callback value from the HTTP Request
     * @return A JSON representation of the parties
     */
    @SuppressWarnings("HardcodedLineSeparator")
    public String toJSON(String callback) {

        //Create the JSON string with a stringbuilder for performance
        StringBuilder jsonBuilder = new StringBuilder(STRINGBUILDER_CAPACITY);

        jsonBuilder.append("[\n");

        for (Party party : _parties) {
            jsonBuilder.append(party.toJSON());
        }

        //If there are any parties make sure the final comma is removed
        if (_parties.isEmpty()) {
            jsonBuilder.deleteCharAt(jsonBuilder.lastIndexOf(","));
        }

        jsonBuilder.append(']');

        if (callback != null) {
            jsonBuilder.insert(0, callback + '(');
            jsonBuilder.append(");");
        }

        return jsonBuilder.toString();

    }

    @Override
    public String toString() {
        return "Parties{" +
                "_parties=" + _parties +
                '}';
    }
}
