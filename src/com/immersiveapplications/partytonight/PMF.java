package com.immersiveapplications.partytonight;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManagerFactory;

/**
 * Gets a reference to the persistence manager
 *
 * @author Jacob Jewell
 */
class PMF {

    private static final PersistenceManagerFactory PMF_INSTANCE =
            JDOHelper.getPersistenceManagerFactory("transactions-optional");

    private PMF() {
    }

    /**
     * Gets a reference to the persistence manager
     *
     * @return A reference to the persistence manager
     */
    public static PersistenceManagerFactory get() {
        return PMF_INSTANCE;
    }
}