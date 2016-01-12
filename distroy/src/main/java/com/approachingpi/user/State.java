/*
 * Created by IntelliJ IDEA.
 * User: terrence
 * Date: Jul 18, 2004
 * Time: 3:03:42 AM
 * To change template for new class use 
 * Code Style | Class Templates options (Tools | IDE Options).
 */
package com.approachingpi.user;

import java.sql.*;
import java.util.ArrayList;

public class State {
    private String abbrev = "";
    private int id;
    private String name = "";

    public State(String in) {
        abbrev = in;
    }

    public static ArrayList getAll(Connection con) {
        return getAll(con,true);
    }
    public static ArrayList getAll(Connection con, boolean activeOnly) {
        return getAll(con,activeOnly,0);
    }
    public static ArrayList getAll(Connection con, boolean activeOnly, int max) {
        ArrayList all = new ArrayList(50);
        try {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM tbState ORDER BY vcName");
            ResultSet rs = ps.executeQuery();
                
            int count = 0;
            while (rs.next()) {
                count++;
                State state = new State(rs.getString("vcAbbrev"));
                state.loadFromRs(rs);
                all.add(state);
                if (max > 0 && count==max) {
                    break;
                }
            }
            rs.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return all;
    }

    
    public String getAbbrev() {
        return abbrev;
    }
    public int getId() {
        return id;
    }
    public String getName() {
        return name;
    }

    public void loadFromDb(Connection con) throws SQLException {
        PreparedStatement ps = con.prepareStatement("SELECT * FROM tbState WHERE vcAbbrev = ?");
        ps.setString(1,getAbbrev());
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            loadFromRs(rs);
        }
        rs.close();
    }
    
    public void loadFromRs(ResultSet rs) throws SQLException {
        setId(rs.getInt("inId"));
        setAbbrev(rs.getString("vcAbbrev"));
        setName(rs.getString("vcName"));
    }

    public void setAbbrev(String in) {
        abbrev = (in == null) ? "" : in;
    }
    public void setId(int in) {
        id = in;
    }
    public void setName(String in) {
        name = (in == null) ? "" : in;
    }
}
