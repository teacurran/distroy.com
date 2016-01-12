/*
 * Session.java
 *
 * Created on August 4, 2004, 8:42 PM
 *
 * @author Terrence Curran
 *
 */

package com.approachingpi.servlet;

import com.approachingpi.user.User;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import com.approachingpi.store.Store;

public class Session {
    private static final char[] saltChars = ("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray());

    Date active;
    Date Expire;
    int id = -1;
    String ip           = "";
    Date login;
    String sessionCode  = "";
    Store store;         
    User user;

    /** Creates a new instance of Session */
    public Session() {
    }

    private synchronized int loadGetNewSessionId(Connection con) {
        int retVal = 0;
        try {
            PreparedStatement ps = con.prepareStatement("SELECT MAX(inId) as inMaxId FROM tbSession");
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                retVal = rs.getInt("inMaxId");
            }
            rs.close();
        } catch (Exception e) {
	        e.printStackTrace();
        }
	    retVal++;

        return retVal;
    }

	public void loadFromDb(Connection con) throws SQLException {
		if (getId() == 0) { return; }

		PreparedStatement ps = con.prepareStatement("SELECT * FROM tbSession WHERE inId=? AND vcSessionCode=? AND vcStore=?");
        ps.setInt(1,getId());
        ps.setString(2,getSessionCode());
        ps.setString(3,getStore().getAbbreviation());
		ResultSet rs = ps.executeQuery();
		if (rs.next()) {
			loadFromRs(rs);
		} else {
			setId(0);
		}
		rs.close();
	}

	public void loadFromRs(ResultSet rs) throws SQLException {
		this.setSessionCode(rs.getString("vcSessionCode"));
		this.setStore(new Store(rs.getString("vcStore")));
        this.setIsWholesale(rs.getBoolean("wholesale"));
        User newUser = new User(rs.getInt("inUserId"));
        if (getUser().getId() != newUser.getId()) {
            this.setUser(newUser);
        }
		this.setLogin(rs.getTimestamp("dtLogin"));
		this.setActive(rs.getTimestamp("dtLogin"));
		this.setExpire(rs.getTimestamp("dtExpire"));
		this.setIp(rs.getString("vcIp"));
	}

    public void saveToDb(Connection con) throws SQLException {
        if (getId() <= 0) {
            setId(loadGetNewSessionId(con));
        }
        PreparedStatement ps = con.prepareStatement("IF ((SELECT Count(*) FROM tbSession WHERE inId=? AND vcSessionCode=? AND vcStore=?) > 0) "+
            "BEGIN "+
                "UPDATE tbSession SET "+
                "wholesale=?,"+
                "inUserId=?, "+
                "dtLogin=?, "+
                "dtActive=CURRENT_TIMESTAMP, "+
                "dtExpire=?, "+
                "vcIp=? "+
                "WHERE inId=? AND vcSessionCode=? AND vcStore=? " +
            "END ELSE BEGIN " +
                "INSERT INTO tbSession (inId,vcSessionCode,vcStore,wholesale,inUserId,dtLogin,dtActive,dtExpire,vcIp) VALUES (?,?,?,?,?,?,CURRENT_TIMESTAMP,?,?) "+
            "END");
        int i=0;
        // IF
        ps.setInt(++i,getId());
        ps.setString(++i,getSessionCode());
        ps.setString(++i,getStore().getAbbreviation());

        // UPDATE
        ps.setBoolean(++i,getIsWholesale());
        ps.setInt(++i,getUser().getId());
        if (getLogin() == null) {
            ps.setTimestamp(++i,null);
        } else {
            ps.setTimestamp(++i,new java.sql.Timestamp(getLogin().getTime()));
        }
        if (getExpire() == null) {
            ps.setTimestamp(++i,null);
        } else {
            ps.setTimestamp(++i,new java.sql.Timestamp(getExpire().getTime()));
        }
        ps.setString(++i,getIp());

        // UPDATE - WHERE
        ps.setInt(++i, getId());
        ps.setString(++i, getSessionCode());
        ps.setString(++i, getStore().getAbbreviation());

        // INSERT
        ps.setInt(++i,getId());
        ps.setString(++i, getSessionCode());
        ps.setString(++i, getStore().getAbbreviation());
        ps.setBoolean(++i,getStore().isWholesale());
        ps.setInt(++i,getUser().getId());
        if (getLogin() == null) {
            ps.setTimestamp(++i,null);
        } else {
            ps.setTimestamp(++i,new java.sql.Timestamp(getLogin().getTime()));
        }
        if (getExpire() == null) {
            ps.setTimestamp(++i,null);
        } else {
            ps.setTimestamp(++i,new java.sql.Timestamp(getExpire().getTime()));
        }
        ps.setString(++i,getIp());

        ps.execute();
    }

    /**
     * Getter for property active.
     * @return Value of property active.
     */
    public java.util.Date getActive() {
        return active;
    }

    /**
     * Setter for property active.
     * @param active New value of property active.
     */
    public void setActive(java.util.Date active) {
        this.active = active;
    }
    
    public boolean getIsWholesale() {
        return getStore().isWholesale();
    }
    
    public void setIsWholesale(boolean in) {
        getStore().setWholesale(in);
    }

    /**
     * Getter for property Expire.
     * @return Value of property Expire.
     */
    public java.util.Date getExpire() {
        return Expire;
    }

    /**
     * Setter for property Expire.
     * @param Expire New value of property Expire.
     */
    public void setExpire(java.util.Date Expire) {
        this.Expire = Expire;
    }

    /**
     * Getter for property id.
     * @return Value of property id.
     */
    public int getId() {
        return id;
    }

    /**
     * Setter for property id.
     * @param id New value of property id.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Getter for property ip.
     * @return Value of property ip.
     */
    public java.lang.String getIp() {
        return ip;
    }

    /**
     * Setter for property ip.
     * @param ip New value of property ip.
     */
    public void setIp(java.lang.String ip) {
        this.ip = ip;
    }

    /**
     * Getter for property login.
     * @return Value of property login.
     */
    public java.util.Date getLogin() {
        return login;
    }

    /**
     * Setter for property login.
     * @param login New value of property login.
     */
    public void setLogin(java.util.Date login) {
        this.login = login;
    }

    /**
     * Getter for property session.
     * @return Value of property session.
     */
    public java.lang.String getSessionCode() {
        if (sessionCode.length() == 0){
            java.util.Random randomGenerator = new java.util.Random();
            int numSaltChars = saltChars.length;

            StringBuffer sessionValueBuffer = new StringBuffer();

            for (int i=0; i <10; i++) {
                sessionValueBuffer.append(saltChars[Math.abs(randomGenerator.nextInt()) % numSaltChars]);
            }
            sessionCode = sessionValueBuffer.toString();
        }
        return sessionCode;
    }

    /**
     * Setter for property session.
     * @param in New value of property session.
     */
    public void setSessionCode(java.lang.String in) {
        this.sessionCode = (in == null) ? "" : in;
    }

    /**
     * Getter for property store.
     * @return Value of property store.
     */
    public Store getStore() {
        if (store == null) {
            store = new Store(PiServlet.STORE_DEFAULT);
        }
        return store;
    }

    /**
     * Setter for property store.
     * @param store New value of property store.
     */
    public void setStore(Store store) {
        this.store = store;
    }

    /**
     * Getter for property user.
     * @return Value of property user.
     */
    public com.approachingpi.user.User getUser() {
        if (user==null) {
            user = new User();
            user.setType(User.TYPE_PUBLIC_ANON);
        }
        return user;
    }

    /**
     * Setter for property user.
     * @param user New value of property user.
     */
    public void setUser(com.approachingpi.user.User user) {
        this.user = user;
    }

}
