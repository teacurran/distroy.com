package com.approachingpi.store.order;

import com.approachingpi.PiObject;

import java.util.Date;
import java.sql.*;

/**
 * Approaching Pi, Inc.
 * User: Tea Curran
 * Date: Nov 29, 2004
 * Time: 2:41:09 PM
 * Desc:
 */
public class Shipment extends PiObject {
    Date date;
    int id;
    String trackingNumber = "";
    ShipmentType type;
    Order order = null;

    public Shipment() {
        
    }
    public Shipment(int idIn) {
        this.setId(idIn);
    }

    public boolean equals(Shipment in) {
        if (in==null) {
            return false;
        }
        if (in.getId() == this.getId()) {
            return true;
        }
        return false;
    }

    public Date getDate() {
        return date;
    }
    public int getId() {
        return id;
    }
    public Order getOrder() {
        return order;
    }
    public String getTrackingNumber() {
        return trackingNumber;
    }
    public ShipmentType getType() {
        return type;
    }

    public void loadFromDb(Connection con) throws SQLException {
        if (getId() == 0) {
             return;
        }
        PreparedStatement ps = con.prepareStatement("SELECT * FROM tbShipment WHERE inId=?");
        ps.setInt(1,getId());
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            loadFromRs(rs);
        }
        rs.close();
    }

    public void loadFromRs(ResultSet rs) throws SQLException {
        this.setId(rs.getInt("inId"));
        if (this.getOrder() == null) {
            this.setOrder(new Order(rs.getString("vcOrderId")));
        }
        this.setType(new ShipmentType(rs.getInt("inShipmentTypeId")));
        try {
            this.setDate(new Date(rs.getTimestamp("dtCreated").getTime()));
        } catch (Exception e) {
        }
        this.setTrackingNumber(rs.getString("vcTrackingNumber"));
    }

    public void saveToDb(Connection con) throws SQLException {

        PreparedStatement ps;
        if (getId()>0) {
            ps = con.prepareStatement("UPDATE tbShipment SET vcOrderId=?, inShipmentTypeId=?, dtCreated=?, vcTrackingNumber=? WHERE inId=?");
        } else {
            ps = con.prepareStatement("INSERT INTO tbShipment (vcOrderId, inShipmentTypeId, dtCreated, vcTrackingNumber) VALUES (?,?,?,?)");
        }

        int i=0;
        if (getOrder() == null) {
            ps.setNull(++i, Types.VARCHAR);
        } else {
            ps.setString(++i, getOrder().getId());
        }
        if (getType() == null) {
            ps.setNull(++i,Types.INTEGER);
        } else {
            ps.setInt(++i, getType().getId());
        }
        if (getDate() == null) {
            setDate(new Date());
        }
        ps.setTimestamp(++i, new Timestamp(getDate().getTime()));
        ps.setString(++i, getTrackingNumber());

        if (getId() > 0) {
            ps.setInt(++i,getId());
        }
        ps.execute();

        if (getId() == 0) {
            ps = con.prepareStatement("SELECT MAX(inId) AS inMaxId FROM tbShipment WHERE inShipmentTypeId=? AND vcTrackingNumber=?");
            if (getType() == null) {
                ps.setNull(1, Types.INTEGER);
            } else {
                ps.setInt(1, getType().getId());
            }
            ps.setString(2, getTrackingNumber());

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                this.setId(rs.getInt("inMaxId"));
            }
            rs.close();
        }
    }

    public void setDate(Date date) {
        this.date = date;
    }
    public void setId(int id) {
        this.id = id;
    }
    public void setOrder(Order in) {
        this.order = in;
    }
    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }
    public void setType(ShipmentType type) {
        this.type = type;
    }
}
