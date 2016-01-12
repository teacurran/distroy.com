/*
 * Created by IntelliJ IDEA.
 * User: terrence
 * Date: Jun 25, 2004
 * Time: 4:55:19 AM
 * To change template for new class use 
 * Code Style | Class Templates options (Tools | IDE Options).
 */
package com.approachingpi.store.catalog;

import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.sql.SQLException;

public class Size implements Comparable {
    private int id;
    private String name = "";
    private String nameShort = "";
    private int qtyInStock;
    private int qtyToAdd;
    private int rank;

    public Size() {}
    public Size(int inId) {
        this.setId(inId);
    }

    public int compareTo(Size anotherSize) {
        int thisVal = this.getRank();
        int anotherVal = anotherSize.getRank();
        return (thisVal<anotherVal ? -1 : (thisVal==anotherVal ? 0 : 1));
    }

    /**
     * Compares this <code>Integer</code> object to another object.
     * If the object is an <code>Integer</code>, this function behaves
     * like <code>compareTo(Integer)</code>.  Otherwise, it throws a
     * <code>ClassCastException</code> (as <code>Integer</code>
     * objects are only comparable to other <code>Integer</code>
     * objects).
     *
     * @param   o the <code>Object</code> to be compared.
     * @return  the value <code>0</code> if the argument is a 
     *		<code>Size</code> wiah a rank numerically equal to this 
     *		<code>Size</code>; a value less than <code>0</code> 
     *		if the argument is a <code>Size</code> with a rank numerically 
     *		greater than this <code>Size</code>; and a value 
     *		greater than <code>0</code> if the argument is a 
     *		<code>Size</code> with a rank numerically less than this 
     *		<code>Size</code>.
     * @exception <code>ClassCastException</code> if the argument is not an
     *		  <code>Size</code>.
     * @see     java.lang.Comparable
     * @since   1.2
     */
    public int compareTo(Object o) {
        return compareTo((Size)o);
    }

    public boolean equals(Size size) {
        if (size != null && size.getId() == this.getId()) {
            return true;
        }
        return false;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getNameShort() { return nameShort; }
    public int getQtyInStock() { return qtyInStock; }
    public int getQtyToAdd() { return qtyToAdd; }
    public int getRank() { return rank; }

    public static ArrayList loadAllSizes(Connection con) throws Exception {
        ArrayList sizes = new ArrayList();
        PreparedStatement ps = con.prepareStatement("SELECT * FROM tbSize ORDER BY inRank");
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            Size size= new Size(rs.getInt("inId"));
            size.loadFromRs(rs);
            sizes.add(size);
        }
        rs.close();
        return sizes;
    }

    public void loadFromDb(Connection con) throws SQLException {
        PreparedStatement ps = con.prepareStatement("SELECT * FROM tbSize WHERE inId = ?");
        ps.setInt(1,getId());
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            this.loadFromRs(rs);
        } else {
            setId(0);
        }
        rs.close();
    }
    
    public void loadFromRs(ResultSet rs) throws SQLException {
        setName(rs.getString("vcName"));
        setNameShort(rs.getString("vcNameShort"));
        setRank(rs.getInt("inRank"));
    }

    public void setId(int in) { this.id = in; }
    public void setName(String in) { this.name = (in == null) ? "" : in; }
    public void setNameShort(String in) { this.nameShort = (in == null) ? "" : in; }
    public void setQtyInStock(int in) { this.qtyInStock = in; }
    public void setQtyToAdd(int in) { this.qtyToAdd = in; }
    public void setRank(int in) { this.rank = in; }
}
