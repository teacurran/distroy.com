/*
 * Created by IntelliJ IDEA.
 * User: terrence
 * Date: Jun 25, 2004
 * Time: 3:37:41 AM
 * To change template for new class use
 * Code Style | Class Templates options (Tools | IDE Options).
 */
package com.approachingpi.store.catalog;

import com.approachingpi.store.Store;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class Brand {
    private boolean active = false;
    private String desc = "";
    private int id;
    private String logo = "";
    private String name = "";

    public Brand() {
    }
    public Brand(int in) {
        this.setId(in);
    }

    public boolean getActive() {
        return this.active;
    }

    public static ArrayList getAllBrands(Connection con) {
        return Brand.getAllBrands(con,true);
    }
    public static ArrayList getAllBrands(Connection con, boolean activeOnly) {
        return getAllBrands(con,activeOnly,0);
    }
    public static ArrayList getAllBrands(Connection con, boolean activeOnly, int max) {
        return getAllBrands(con, null, activeOnly, max);
    }
    public static ArrayList getAllBrands(Connection con, Store store, boolean activeOnly, int max) {
        ArrayList brands = new ArrayList(4);
        try {
            String whereAnd = "WHERE ";
            StringBuffer sql = new StringBuffer(500);
            sql.append("SELECT * FROM tbBrand\n");
            if (activeOnly) {
                sql.append(whereAnd + " btActive=1\n");
                whereAnd = "and ";
            }
            if (store != null && store.getId() > 0) {
                sql.append(whereAnd + " inId IN (SELECT inBrandId FROM tbLNKBrandStore WHERE inStoreId=" + store.getId());
            }
            sql.append("ORDER BY vcName");

            PreparedStatement ps = con.prepareStatement(sql.toString());
            ResultSet rs = ps.executeQuery();
            int count = 0;
            while (rs.next()) {
                count++;
                // this is all we need for the product page
                Brand brand = new Brand(rs.getInt("inId"));
                brand.loadFromRs(rs);
                brands.add(brand);
                if (max > 0 && count==max) {
                    break;
                }
            }
            rs.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return brands;
    }


    public String getDesc() {
        return this.desc;
    }
    public int getId() {
        return this.id;
    }
    public String getLogo() {
        return this.logo;
    }
    public String getName() {
        return this.name;
    }

    public void loadFromDb(int id, Connection con) throws SQLException {
        this.setId(id);
        loadFromDb(con);
    }
    public void loadFromDb(Connection con) throws SQLException {
        if (getId() <= 0) {
            return;
        }
        try {

            PreparedStatement ps = con.prepareStatement("SELECT * FROM tbBrand WHERE inId = ?");
            ps.setInt(1,this.getId());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                // this is all we need for the product page
                this.loadFromRs(rs);
            } else {
                int thisId = this.getId();
                this.setId(0);
                throw new Exception("Brand Id " + thisId + " not found");

            }
            rs.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void loadFromDbByName(Connection con) throws SQLException {
        if (getName().length() == 0) {
            return;
        }
        try {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM tbBrand WHERE vcPageName = ?");
            ps.setString(1,this.getName());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                // this is all we need for the product page
                this.setId(rs.getInt("inId"));
                this.loadFromRs(rs);
            } else {
                this.setId(0);
                throw new Exception("Brand Id " + this.getName() + " not found");
            }
            rs.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void loadFromRs(ResultSet rs) throws SQLException {
        this.setName(rs.getString("vcName"));
        this.setLogo(rs.getString("vcLogo"));
        this.setActive(rs.getBoolean("btActive"));
        this.setDesc(rs.getString("txDesc"));
    }

    public void saveToDb(Connection con) throws SQLException {
        PreparedStatement ps;

        if (this.getId() > 0) {
            ps = con.prepareStatement("UPDATE tbBrand SET vcName = ?, vcLogo = ?, vcActive = ?, txDesc = ? WHERE inId = ?");
        } else {
            ps = con.prepareStatement("INSERT INTO tbBrand (vcName, vcLogo, vcActive, txDesc) VALUES(?,?,?,?)");
        }
        int i = 0;
        ps.setString(++i,this.getName());
        ps.setString(++i,this.getLogo());
        ps.setBoolean(++i,this.getActive());
        ps.setString(++i,this.getDesc());
        if (this.getId() > 0) {
            ps.setInt(++i,this.getId());
        }
        ps.execute();

        if (this.getId() == 0) {
            ps = con.prepareStatement("SELECT Max(inId) AS inMaxId FROM tbBrand WHERE vcName=? AND vcLogo=?, vcActive=?");
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                this.setId(rs.getInt("inMaxId"));
            }
        }
    }

    public void setActive(boolean in) { this.active = in; }
    public void setDesc(String in) { this.desc = (in==null) ? "" : in; }
    public void setId(int in) { this.id = in; }
    public void setLogo(String in) { this.logo = (in==null) ? "" : in; }
    public void setName(String in) { this.name = (in==null) ? "" : in; }
}
