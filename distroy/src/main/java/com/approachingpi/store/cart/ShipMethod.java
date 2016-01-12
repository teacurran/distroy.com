/*
 * ShipMethod.java
 *
 * Created on August 23, 2004, 5:42 PM
 */

package com.approachingpi.store.cart;

import com.approachingpi.PiObject;
import com.approachingpi.servlet.Session;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class ShipMethod extends PiObject {
    int id;
    String name = "";
    ArrayList prices = new ArrayList();
    boolean retail = false;
    boolean wholesale = false;
    
    /** Creates a new instance of ShipMethod */
    public ShipMethod() {}
    public ShipMethod(int id) {
        setId(id);
    }
    
    public void addShipPrice(ShipPrice price) {
        prices.add(price);
    }

    public static ArrayList getAvailableShipMethods(Connection con, Cart cart, Session session) {
        ArrayList methods = new ArrayList();

        StringBuffer sql = new StringBuffer(500);
        sql.append("SELECT M.inId, P.inId as inPriceId,\n");
        sql.append("M.vcName, M.btWholesale, M.btRetail,\n");
        sql.append("P.moDollarMin, P.moDollarMax, P.inWeightMin, P.inWeightMax, P.inItemMin, P.inItemMax, P.vcCalculation\n");

        sql.append("FROM tbShipMethod M, tbShipTerritory T, tbShipPrice P\n");
        sql.append("WHERE M.inId = T.inShipMethodId\n");
        sql.append("AND M.inId = P.inShipMethodId\n");
        
        sql.append("AND T.inCountryId = " + session.getUser().getActiveShippingAddress().getCountry().getId() + "\n");
        // price
        sql.append("AND (P.moDollarMin = 0 OR P.moDollarMin <=" + cart.getSubtotalPrice() + ")\n");
        sql.append("AND (P.moDollarMax = 0 OR P.moDollarMax >=" + cart.getSubtotalPrice() + ")\n");
        // items
        sql.append("AND (P.inItemMin = 0 OR P.inItemMin <=" + cart.getItemCount() + ")\n");
        sql.append("AND (P.inItemMax = 0 OR P.inItemMax >=" + cart.getItemCount() + ")\n");
        
        // TODO weight not implemented
        
        if (session.getIsWholesale()) {
            sql.append("AND M.btWholesale=1\n");
        } else {
            sql.append("AND M.btRetail=1\n");
        }
        
        try {
            PreparedStatement ps = con.prepareStatement(sql.toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ShipMethod method = new ShipMethod(rs.getInt("inId"));
                ShipPrice price = new ShipPrice(rs.getInt("inPriceId"));
                method.loadFromRs(rs);
                price.loadFromRs(rs);
                method.addShipPrice(price);
                methods.add(method);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //System.out.println(sql);
        return methods;
    }
    
    public ShipPrice getFirstPrice() {
        if (prices.size() > 0) {
            return (ShipPrice)prices.get(0);
        }
        return new ShipPrice();
    }
    public int getId() { return this.id; } 
    public String getName() { return this.name; }
    public boolean getRetail() { return this.retail; }
    public boolean getWholesale() { return this.wholesale; }
    
    public void loadFromDb(Connection con) throws SQLException {
        if (getId()==0) {
            return;
        }
        PreparedStatement ps = con.prepareStatement("SELECT * FROM tbShipMethod WHERE inId = ?");
        ps.setInt(1,getId());
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            loadFromRs(rs);
        }
        rs.close();
    }
    public void loadFromRs(ResultSet rs) throws SQLException {
        this.setName(rs.getString("vcName"));
        this.setWholesale(rs.getBoolean("btWholesale"));
        this.setRetail(rs.getBoolean("btRetail"));
    }
    
    public void setId(int in) {
        this.id = in;
    }
    public void setName(String in) {
        this.name = (in==null)?"":in;
    }
    public void setRetail(boolean in) { this.retail = in; } 
    public void setWholesale(boolean in) { this.wholesale = in; } 
}
