/*
 * HomepageItem.java
 *
 * Created on July 30, 2004, 8:39 PM
 */

package com.approachingpi.store.site;

/**
 *
 * @author  Terrence
 */

import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import com.approachingpi.user.*;
import com.approachingpi.store.catalog.*;

public class HomepageItem {
    int accessRequired;
    String blurb                    = "";
    int id;
    String link                     = "";
    ArrayList<ProductVariation> productVariations     = new ArrayList<>();
    int rank;
    String title                    = "";

    /** Creates a new instance of HomepageItem */
    public HomepageItem() {
    }

    public HomepageItem(int in) {
        this.setId(in);
    }

    public void addProductVariation(ProductVariation in) {
        if (in != null) {
            productVariations.add(in);
        }
    }
    public int getAccessRequired() { return this.accessRequired; }
    public String getBlurb() { return this.blurb; }
    public int getId() { return this.id; }
    public String getLink() { return this.link; }
    public ArrayList getProductVariations() { return this.productVariations; }
    public int getRank() { return this.rank; }
    public String getTitle() { return this.title; }

    public static ArrayList getAllHomepageItems(Connection con) {
        return getAllHomepageItems(con,User.TYPE_PUBLIC,-1);
    }

    public static ArrayList getAllHomepageItems(Connection con, int access, int max) {
        ArrayList items = new ArrayList();
        try {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM tbHomepageItem WHERE inAccessRequired <= ? ORDER BY inAccessRequired DESC, inRank");
            ps.setInt(1,access);
            ResultSet rs = ps.executeQuery();

            int count = 0;
            while (rs.next()) {
                count++;
                HomepageItem item = new HomepageItem(rs.getInt("inId"));
                item.loadFromRs(rs);
                item.loadProductVariationsFromDb(con);
                items.add(item);
                if (count == max) {
                    rs.close();
                    break;
                }
            }
            rs.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return items;
    }

    public void loadFromDb(Connection con) throws SQLException {
        if (getId() <= 0) { return; }

        try {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM tbHomepageItem WHERE inId = ?");
            ps.setInt(1,this.getId());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                loadFromRs(rs);
            } else {
                int id = this.getId();
                this.setId(0);
                throw new Exception("Homepage item " + id + " not found");
            }
            rs.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void loadFromRs(ResultSet rs) throws SQLException {
        setTitle(rs.getString("vcTitle"));
        setLink(rs.getString("vcLink"));
        setAccessRequired(rs.getInt("inAccessRequired"));
        setRank(rs.getInt("inRank"));
        setBlurb(rs.getString("txBlurb"));
    }

    public void loadProductVariationsFromDb(Connection con) throws Exception {
        if (getId() <= 0) { return; }

        PreparedStatement ps = con.prepareStatement("SELECT * FROM tbProductVariation WHERE inId IN (SELECT inProductVariationId FROM tbLinkHomepageItemProductVariation WHERE inHomepageItemId = ?)");
        ps.setInt(1,getId());
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            ProductVariation variation = new ProductVariation(rs.getInt("inId"));
            variation.loadFromDb(con);
            variation.getProduct().loadFromDb(con);
            variation.getProduct().loadImagesFromDb(con,1, Image.VERTICAL);
            variation.loadImagesFromDb(con,1, Image.VERTICAL);
            this.addProductVariation(variation);
        }
        rs.close();
    }

	public void saveToDb(Connection con) throws SQLException {
		PreparedStatement ps;
		if (getId() > 0) {
			ps = con.prepareStatement("UPDATE tbHomepageItem SET vcTitle=?, vcLink=?, inAccessRequired=?, inRank=?, txBlurb=? WHERE inId=?");
		} else {
			ps = con.prepareStatement("INSERT INTO tbHomepageItem (vcTitle, vcLink, inAccessRequired, inRank, txBlurb) VALUES(?,?,?,?,?)");
		}
		int i=0;
		ps.setString(++i,getTitle());
		ps.setString(++i,getLink());
		ps.setInt(++i,getAccessRequired());
		ps.setInt(++i,getRank());
		ps.setString(++i,getBlurb());
		if (getId()>0) {
			ps.setInt(++i,getId());
		}
		ps.execute();

		if (getId()==0) {
			ps = con.prepareStatement("SELECT Max(inId) AS inMaxId FROM tbHomepageItem WHERE vcTitle=? AND vcLink=?");
			ps.setString(1,getTitle());
			ps.setString(2,getLink());
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				this.setId(rs.getInt("inMaxId"));
			}
			rs.close();
		}
	}

	public void saveProductVariationsToDb(Connection con) throws SQLException {
		String idList = "0";
		for (ProductVariation productVariation : productVariations) {
			ProductVariation variation = (ProductVariation) productVariation;
			idList = idList + "," + variation.getId();

			String sqlStatement = "INSERT INTO tbLinkHomepageItemProductVariation " +
				"(inHomepageItemId, inProductVariationId) VALUES (?,?)\n" +
				"ON DUPLICATE KEY UPDATE inHomepageItemId=inHomepageItemId";
			PreparedStatement ps = con.prepareStatement(sqlStatement);

			ps.setInt(1, getId());
			ps.setInt(2, variation.getId());
			ps.execute();

		}

		PreparedStatement ps = con.prepareStatement("DELETE FROM tbLinkHomepageItemProductVariation \n" +
			"WHERE inHomepageItemId=? \n" +
			"AND inProductVariationId NOT IN(" + idList + ")");
		ps.setInt(1,getId());
		ps.execute();
	}

    public void setAccessRequired(int in) { this.accessRequired = in; }
    public void setBlurb(String in) { this.blurb = (in==null) ? "" : in; }
    public void setId(int in) { this.id = in; }
    public void setLink(String in) { this.link = (in==null) ? "" : in; }
    public void setRank(int in) { this.rank = in; }
    public void setTitle(String in) { this.title = (in==null) ? "" : in; }


}
