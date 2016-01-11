/*
 * User: terrence
 * Date: Jul 18, 2004
 * Time: 3:23:25 AM
 */
package com.approachingpi.user;

import java.sql.*;
import java.util.ArrayList;

public class Country {
    boolean active = false;
    String code = "";
    int id;
    String lead = "";
    String name = "";
    String nameProper = "";
    String postalMask = "";

    public Country() {}
    public Country(int in) {
        setId(in);
    }
    public Country(String in) {
        setCode(in);
    }

    public static ArrayList getAll(Connection con) {
        return getAll(con,true);
    }
    public static ArrayList getAll(Connection con, boolean activeOnly) {
        return getAll(con,activeOnly,0);
    }
    public static ArrayList getAll(Connection con, boolean activeOnly, int max) {
        ArrayList all = new ArrayList(4);
        try {
            String sql = (activeOnly) ? "SELECT * FROM tbCountry WHERE btActive = 1 ORDER BY vcName" : "SELECT * FROM tbCountry ORDER BY vcName";
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
                
            int count = 0;
            while (rs.next()) {
                count++;
                Country country = new Country(rs.getInt("inId"));
                country.loadFromRs(rs);
                all.add(country);
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
    
    public boolean getActive() {
        return active;
    }
    public String getCode() {
        return code;
    }
    public int getId() {
        return id;
    }
    public String getLead() {
        return lead;
    }
    public String getName() {
        return name;
    }
    public String getNameProper() {
        return nameProper;
    }
    public String getPostalMask() {
        return postalMask;
    }
    
    public void loadFromDb(Connection con) throws SQLException {
        PreparedStatement ps = con.prepareStatement("SELECT * FROM tbCountry WHERE inId = ?");
        ps.setInt(1,getId());
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            loadFromRs(rs);
        }
        rs.close();
    }
    public void loadFromRs(ResultSet rs) throws SQLException {
        setName(rs.getString("vcName"));
        setNameProper(rs.getString("vcNameProper"));
        setLead(rs.getString("vcLead"));
        setCode(rs.getString("vcCode"));
        setPostalMask(rs.getString("vcPostalMask"));
        setActive(rs.getBoolean("btActive"));
    }
    
    public void setActive(boolean in) { 
        active = in;
    } 
    public void setCode(String in) {
        code = (in==null) ? "" : in;
    }
    public void setId(int in) {
        id = in;
    }
    public void setLead(String in) {
        lead = (in==null) ? "" : in;
    }
    public void setName(String in) {
        name = (in==null) ? "" : in;
    }
    public void setNameProper(String in) {
        nameProper = (in==null) ? "" : in;
    }
    public void setPostalMask(String in) {
        postalMask = (in==null) ? "" : in;
    }
    

}
