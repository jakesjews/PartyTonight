package com.immersiveapplications.partytonight;

import com.beoui.geocell.GeocellManager;
import com.beoui.geocell.annotations.Geocells;
import com.beoui.geocell.annotations.Latitude;
import com.beoui.geocell.annotations.Longitude;
import com.beoui.geocell.model.Point;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import java.util.*;

@SuppressWarnings({"UnusedDeclaration", "InstanceVariableMayNotBeInitialized"})
@PersistenceCapable
class Party {

    //The different rating descriptions a party can have
    private static final String LOW_RATING = "Lame";
    private static final String AVERAGE_RATING = "Average";
    private static final String GOOD_RATING = "Awesome";

    private static final int AVERAGE_RATING_MAX = 15;
    private static final int STRINGBUILDER_CAPACITY = 50;

    //The unique identifier of the party
    @PrimaryKey
    @Persistent
    protected String id = null;

    public String getId() {
        return id;
    }

    public void setId(String value) {
        id = value;
    }

    //The latitude of the party
    @Persistent
    @Latitude
    protected double latitude;

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double value) {
        latitude = value;
    }

    //The longitude of the party
    @Persistent
    @Longitude
    protected double longitude;

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double value) {
        longitude = value;
    }

    //The geocells of the party used for proximity searches
    @Persistent
    @Geocells
    protected List<String> geocells;

    public List<String> getGeocells() {
        return Collections.unmodifiableList(geocells);
    }

    @SuppressWarnings("AssignmentToCollectionOrArrayFieldFromParameter")
    public void setGeocells(List<String> value) {
        geocells = value;
    }

    //The apartment number of the party
    @Persistent
    protected String apartment = "";

    public String getApartment() {
        return apartment;
    }

    public void setApartment(String value) {
        apartment = value;
    }

    //The rating of the party
    @Persistent
    protected int rating;

    public int getRating() {
        return rating;
    }

    //Whether the party is busted or not
    @Persistent
    protected boolean busted;

    public boolean isBusted() {
        return busted;
    }

    public void setBusted(boolean value) {
        busted = value;
    }

    //The date and time the party will be removed from the datastore
    @Persistent
    protected Date createdDate;

    public Date getCreatedDate() {
        return (Date) createdDate.clone();
    }

    // The id's that have already rated the party
    @Persistent
    protected List<String> ratings = new ArrayList<String>();

    Party(double latitude, double longitude) {

        ratings = new ArrayList<String>(10);
        apartment = "";

        this.longitude = longitude;
        this.latitude = latitude;

        //The id is just a combination of the latitude,longitude, and apartment number
        id = String.format("%s-%s-%s", latitude, longitude, apartment);

        //Create and stores geocells for the party with the latitude and longitude
        Point location = new Point(latitude,longitude);
        geocells = GeocellManager.generateGeoCell(location);

        createdDate = getCurrentTime();
    }

    public static Date getCurrentTime() {
        Calendar cal = Calendar.getInstance();
        return cal.getTime();
    }

    /**
     * Updates the rating of the party
     *
     * @param rating   The amount to be added or subtracted from the rating
     * @param ratingID The id of the user rating the party
     * @return {@code True} If the party was rated
     *         {@code False} If the party could not be rated
     */
    public boolean addToRating(int rating, String ratingID) {

        boolean canRate = canUserRate(ratingID);

        if (canRate) {
            this.rating += rating;
        }

        return canRate;
    }

    /**
     * Adds an id to the collection of ids that have rated the party
     *
     * @param ratingID The id rating the party
     * @return {@code True} If the id has not already rated the party
     *         {@code False} If the id has already rated the party
     */
    private boolean canUserRate(String ratingID) {
        boolean canRate = !ratings.contains(ratingID);

        if (canRate) {
            ratings.add(ratingID);
        }

        return canRate;
    }

    /**
     * Gets the geo point of the party as a string
     *
     * @return the geo point of the party as a string
     */
    public String getKeyString() {
        return getLocation().toString();
    }

    /**
     * Returns the geo point location of the party
     *
     * @return the geo point location of the party
     */
    private Point getLocation() {
        return new Point(latitude, longitude);
    }

    /**
     * Gets the party data as a plist which is an apple xml format
     * This will be primarily used for iPhone applications
     * plist - Java - Objective-C:
     * {@code real} = {@code double} = {@code NSNumber}
     * {@code string} = {@code String} = {@code NSString}
     * {@code array} = {@code array} = {@code NSArray}
     * {@code <true /> or <false />} = {@code boolean} = {@code BOOL}
     *
     * @return The party data as a plist
     */
    @SuppressWarnings("HardcodedLineSeparator")
    public String toPlist() {
        StringBuilder plistBuilder = new StringBuilder(STRINGBUILDER_CAPACITY);

        plistBuilder.append("\t<array>\n");

        plistBuilder.append("\t\t<real>").append(Double.toString(latitude)).append("</real>\n");
        plistBuilder.append("\t\t<real>").append(Double.toString(longitude)).append("</real>\n");
        plistBuilder.append("\t\t<string>").append(apartment).append("</string>\n");
        plistBuilder.append("\t\t<string>").append(getRatingString()).append("</string>\n");

        if (busted) {
            plistBuilder.append("\t\t<true />\n");
        } else {
            plistBuilder.append("\t\t<false />\n");
        }

        plistBuilder.append("\t</array>\n");

        return plistBuilder.toString();
    }

    /**
     * Gets the party data as JSON
     * This is the preferred way to retrieve party date
     *
     * @return The party data in a JSON format
     */
    @SuppressWarnings("HardcodedLineSeparator")
    public String toJSON() {
        StringBuilder jsonBuilder = new StringBuilder("\t{\n");

        jsonBuilder.append("\t\"latitude\": \"").append(Double.toString(latitude)).append("\",\n");
        jsonBuilder.append("\t\"longitude\": \"").append(Double.toString(longitude)).append("\",\n");
        jsonBuilder.append("\t\"apartment\": \"").append(apartment).append("\",\n");
        jsonBuilder.append("\t\"rating\": \"").append(getRatingString()).append("\",\n");
        jsonBuilder.append("\t\"busted\": \"").append(Boolean.toString(busted)).append("\"\n");
        jsonBuilder.append("\t},\n");

        return jsonBuilder.toString();
    }

    /**
     * Returns a string description for a rating
     *
     * @return a description of the rating
     */
    private String getRatingString() {

        String ratingResult;

        /*
           * Rating <= 0 is bad
           * Rating > 0 and <= 15 is average
           * Rating > 15 is good
           */
        if (rating <= 0) {
            ratingResult = LOW_RATING;
        } else if (rating > 0 && rating <= AVERAGE_RATING_MAX) {
            ratingResult = AVERAGE_RATING;
        } else {
            ratingResult = GOOD_RATING;
        }

        return ratingResult;
    }

    @Override
    public String toString() {
        return "Party{" +
                "_id='" + id + '\'' +
                ", _latitude=" + latitude +
                ", _longitude=" + longitude +
                ", _geocells=" + geocells +
                ", _apartment='" + apartment + '\'' +
                ", _rating=" + rating +
                ", _busted=" + busted +
                ", _createdDate=" + createdDate +
                ", _ratings=" + ratings +
                '}';
    }
}
