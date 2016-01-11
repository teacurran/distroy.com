/*
 * PiObject.java
 *
 * Created on September 10, 2004, 12:29 PM
 * @author  Terrence
 */
package com.approachingpi;

import java.sql.Connection;
import java.sql.SQLException;

public class PiObject {
    protected boolean debug = false;
    protected boolean loaded = false;
    
    /** Creates a new instance of PiObject */
    public PiObject() {
    }

    public boolean getDebug() {
        return debug;
    }

    public void loadFromDbOnce(Connection con) throws SQLException {
        if (!loaded) {
            loadFromDb(con);
        }
    }
    
    public void loadFromDb(Connection con) throws SQLException {
        // override this method
        loaded = true;
    }
    
    public void setDebug(boolean in) {
        debug = in;
    }
}
