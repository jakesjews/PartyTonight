package com.immersiveapplications.partytonight;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import java.util.List;

public class PartyFactory {

    /**
     * Stores a party in the datastore
     *
     * @param lat    The latitude of the user
     * @param lng    The longitude of the user
     * @param apt    The apartment number of the party
     * @param busted Whether the party has been busted
     * @param rating The actual rating of the party
     * @param pm     The persistence manager to access party data
     */
    public static boolean storeParty(double lat, double lng, String apt, boolean busted, int rating, String ratingID, PersistenceManager pm) {

        //The query used to determine if a party record needs to be created or updated
        Query query = pm.newQuery(Party.class);
        query.setFilter("latitude == " + lat + " && longitude == " + lng);
        List<Party> parties = (List<Party>) query.execute();

        Party party;

        //If there was no existing party record create a new record
        if (parties.isEmpty()) {
            party = new Party(lat, lng);
        } else {  //If there is a matching party then update the party record
            party = parties.get(0);
        }

        party.setBusted(busted);

        // If there wasn't an apartment stored for the party but there is in this rating,
        // then add the apartment number to the party record
        if (apt != null && party.getApartment().isEmpty() && !apt.isEmpty()) {
            party.setApartment(apt);
        }

        boolean successful = party.addToRating(rating, ratingID);

        pm.makePersistent(party);

        return successful;

    }

}
