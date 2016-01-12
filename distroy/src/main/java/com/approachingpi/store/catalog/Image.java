/*
 * Created by IntelliJ IDEA.
 * User: terrence
 * Date: Jul 11, 2004
 * Time: 1:06:39 AM
 * To change template for new class use 
 * Code Style | Class Templates options (Tools | IDE Options).
 */
package com.approachingpi.store.catalog;

import com.approachingpi.util.PiUtility;

import java.sql.PreparedStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.io.File;

public class Image {
    public static final int ANY_ORIENTATION  = 0;

    public static final int HORIZONTAL  = 1;
    public static final int VERTICAL    = 2;
    public static final int SQUARE      = 3;

    private String desc                 = "";
    private int id;
    private String name                 = "";
    private String nameOriginalEnlarge  = "";
    private String nameOriginalStandard = "";
    private String nameOriginalThumb    = "";
    private int orientation;
    private Product product;
    private int rank;

    public Image() {
    }
    public Image(int in) {
        this.setId(in);
    }

    public void deleteFromDb(Connection con) throws Exception {
        if (getId() <= 0) {
            return;
        }

        PreparedStatement ps = con.prepareStatement("DELETE FROM tbLinkProductVariationImage WHERE inImageId=?");
        ps.setInt(1,this.getId());
        ps.execute();
        
        ps = con.prepareStatement("DELETE FROM tbImage WHERE inId = ?");
        ps.setInt(1,this.getId());
        ps.execute();
    }
    public String getDesc() {
        return this.desc;
    }
    public int getId() {
        return this.id;
    }
    public String getName() {
        return this.name;
    }
    public String getNameOriginalEnlarge() {
        return this.nameOriginalEnlarge;
    }
    public String getNameOriginalStandard() {
        return this.nameOriginalStandard;
    }
    public String getNameOriginalThumb() {
        return this.nameOriginalThumb;
    }
    public int getOrientation() {
        return this.orientation;
    }
    public Product getProduct() {
        if (product == null) {
            return new Product();
        } else {
            return product;
        }
    }
    public int getRank() {
        return this.rank;
    }
    public String getThumbName() {
        //return PiUtility.replace(this.getName(),".","_thumb.");
        return "thumb" + File.separator + this.getName();
    }

    public void loadFromDb(int id, Connection con) throws Exception {
        this.setId(id);
        loadFromDb(con);
    }
    public void loadFromDb(Connection con) throws Exception {
        if (getId() <= 0) {
            return;
        }
        PreparedStatement ps = con.prepareStatement("SELECT * FROM tbImage WHERE inId = ?");
        ps.setInt(1,this.getId());
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            // this is all we need for the product page
            this.loadFromRs(rs);
        } else {
            int thisId = this.getId();
            this.setId(0);
            throw new Exception("Image Id " + thisId + " not found");

        }
        rs.close();
    }

    public void loadFromRs(ResultSet rs) throws Exception {
        this.setProduct(new Product(rs.getInt("inProductId")));
        this.setName(rs.getString("vcName"));
        this.setNameOriginalThumb(rs.getString("vcNameOriginalThumb"));
        this.setNameOriginalStandard(rs.getString("vcNameOriginalStandard"));
        this.setNameOriginalEnlarge(rs.getString("vcNameOriginalEnlarge"));
        this.setOrientation(rs.getInt("inOrientation"));
        this.setRank(rs.getInt("inRank"));
        this.setDesc(rs.getString("txDesc"));
    }

    public void saveToDb(Connection con) throws Exception {
        PreparedStatement ps;

        if (this.getId() == 0) {
            ps = con.prepareStatement("SELECT Count(*) AS inCount FROM tbImage WHERE inProductId = ?");
            ps.setInt(1, this.getProduct().getId());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                this.setRank((rs.getInt("inCount")*2));
            }
            rs.close();
        }

        if (this.getId() > 0) {
            ps = con.prepareStatement("UPDATE tbImage SET inProductId = ?, vcName = ?, vcNameOriginalThumb = ?, vcNameOriginalStandard = ?, vcNameOriginalEnlarge = ?, inOrientation = ?, inRank = ?, txDesc = ? WHERE inId = ?");
        } else {
            ps = con.prepareStatement("INSERT INTO tbImage (inProductId, vcName, vcNameOriginalThumb, vcNameOriginalStandard, vcNameOriginalEnlarge, inOrientation, inRank, txDesc) VALUES(?,?,?,?,?,?,?,?)");
        }
        int i = 0;
        ps.setInt(++i,this.getProduct().getId());
        ps.setString(++i,this.getName());
        ps.setString(++i,this.getNameOriginalThumb());
        ps.setString(++i,this.getNameOriginalStandard());
        ps.setString(++i,this.getNameOriginalEnlarge());
        ps.setInt(++i,this.getOrientation());
        ps.setInt(++i,this.getRank());
        ps.setString(++i,this.getDesc());
        if (this.getId() > 0) {
            ps.setInt(++i,this.getId());
        }
        ps.execute();
        if (this.getId() == 0) {
            ps = con.prepareStatement("SELECT Max(inId) as inMaxId FROM tbImage WHERE inProductId = ? and vcName = ?");
            i = 0;
            ps.setInt(++i,this.getProduct().getId());
            ps.setString(++i,this.getName());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                this.setId(rs.getInt("inMaxId"));
            }
            rs.close();
        }
    }

    public void setDesc(String in) { this.desc = (in==null) ? "" : in; }
    public void setId(int in) { this.id = in; }
    public void setName(String in) { this.name = (in==null) ? "" : in; }
    public void setNameOriginalEnlarge(String in) { this.nameOriginalEnlarge = (in==null) ? "" : in; }
    public void setNameOriginalStandard(String in) { this.nameOriginalStandard = (in==null) ? "" : in; }
    public void setNameOriginalThumb(String in) { this.nameOriginalThumb = (in==null) ? "" : in; }
    public void setOrientation(int in) { this.orientation = in; }
    public void setProduct(Product in) { this.product = in; }
    public void setRank(int in) { this.rank= in; }
}
