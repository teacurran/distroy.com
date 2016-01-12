package com.approachingpi.store.order;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Approaching Pi, Inc.
 * User: Tea Curran
 * Date: Nov 29, 2004
 * Time: 2:41:18 PM
 * Desc:
 */
public class ShipmentType {
    int id;
    String name = "";

    public ShipmentType() {
    }
    public ShipmentType(int idIn) {
        this.setId(idIn);
    }

    public static ArrayList getAllFromDb(Connection con) {
        ArrayList returnList = new ArrayList();
        try {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM tbShipmentType ORDER BY vcName");
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                ShipmentType type = new ShipmentType();
                type.loadFromRs(rs);
                returnList.add(type);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return returnList;
    }

    public int getId() {
        return this.id;
    }
    public String getName() {
        return this.name;
    }
    public void loadFromDb(Connection con) throws SQLException {

        if (getId() == 0) {
             return;
        }
        PreparedStatement ps = con.prepareStatement("SELECT * FROM tbShipmentType WHERE inId=?");
        ps.setInt(1,getId());
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            loadFromRs(rs);
        }
        rs.close();
    }
    public void loadFromRs(ResultSet rs) throws SQLException {
        this.setId(rs.getInt("inId"));
        this.setName(rs.getString("vcName"));
    }

    public void saveToDb(Connection con) throws SQLException {

        PreparedStatement ps;
        if (getId()>0) {
            ps = con.prepareStatement("UPDATE tbShipmentType SET vcName = ? WHERE inId = ?");
        } else {
            ps = con.prepareStatement("INSERT INTO tbShipmentType (vcName) VALUES (?)");
        }

        int i=0;
        ps.setString(++i, getName());
        if (getId() > 0) {
            ps.setInt(++i,getId());
        }
        ps.execute();

        if (getId() == 0) {
            ps = con.prepareStatement("SELECT MAX(inId) AS inMaxId FROM tbShipmentType WHERE vcName = ?");
            ps.setString(1,getName());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                this.setId(rs.getInt("inMaxId"));
            }
            rs.close();
        }
    }

    public void setId(int in) {
        id = in;
    }
    public void setName(String in) {
        this.name = (in==null) ? "" : in;
    }


}
