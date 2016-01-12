/*
 * Created by IntelliJ IDEA.
 * User: terrence
 * Date: Jun 25, 2004
 * Time: 3:38:44 AM
 * To change template for new class use
 * Code Style | Class Templates options (Tools | IDE Options).
 */
package com.approachingpi.store.catalog;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;

public class Artist {
    private boolean active      = false;
    private String desc         = "";
    private int id;
    private String nameFirst    = "";
    private String nameLast     = "";
    private String nameDisplay  = "";
    private String relationship = "";
    private BigDecimal royaltyDollarRetail;
    private BigDecimal royaltyDollarWholesale;
    private float royaltyPercentRetail;
    private float royaltyPercentWholesale;
    private Date dateCreated;
    private Date dateModified;

    public Artist() {
    }
    public Artist(int in) {
        this.setId(in);
    }


	public static ArrayList getAllArtists(Connection con) {
		return Artist.getAllArtists(con,true,"vcNameLast,vcNameFirst");
	}

	public static ArrayList getAllArtists(Connection con, boolean activeOnly, String orderBy) {
        ArrayList artists = new ArrayList();
        try {
	        String sql = "SELECT * FROM tbArtist ";
	        if (activeOnly) {
		        sql += "WHERE btActive = 1 ";
	        }
	        if (orderBy.length() > 0) {
		        sql += "ORDER BY " + orderBy;
	        }
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Artist newArtist = new Artist(rs.getInt("inId"));
                newArtist.loadFromRs(rs);
                artists.add(newArtist);
            }
            rs.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return artists;
    }
    public boolean getActive() { return this.active; }
    public Date getDateCreated() { return this.dateCreated; }
    public Date getDateModified() { return this.dateModified; }
    public String getDesc() { return this.desc; }
    public int getId() { return this.id; }
    public String getNameFirst() { return this.nameFirst; }
    public String getNameLast() { return this.nameLast; }
    public String getNameDisplay() { return this.nameDisplay; }
    public BigDecimal getRoyaltyDollarRetail() { 
        if (this.royaltyDollarRetail == null) {
            this.royaltyDollarRetail = new BigDecimal("0");
        }
        return this.royaltyDollarRetail; 
    }
    public BigDecimal getRoyaltyDollarWholesale() { 
        if (this.royaltyDollarWholesale == null) {
            this.royaltyDollarWholesale = new BigDecimal("0");
        }
    return this.royaltyDollarWholesale; 
    }
    public float getRoyaltyPercentRetail() { 
        return this.royaltyPercentRetail; 
    }
    public float getRoyaltyPercentWholesale() { 
        return this.royaltyPercentWholesale; 
    }
    public String getRelationship() { return this.relationship; }

    public void loadFromDb(Connection con) throws Exception {
        if (getId() <= 0) {
            return;
        }
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = con.prepareStatement("SELECT * FROM tbArtist WHERE inId = ?");
            ps.setInt(1,this.getId());
            rs = ps.executeQuery();
            if (rs.next()) {
                loadFromRs(rs);
            } else {
                int id = this.getId();
                this.setId(0);
            }
            rs.close();
        } finally {
            if (rs != null) {
                rs.close(); rs=null;
            }
            if (ps != null) {
                ps.close(); ps=null;
            }
        }
    }
    public void loadFromDbByNameDisplay(Connection con) throws SQLException {
        if (getNameDisplay().length()==0) {
            return;
        }
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = con.prepareStatement("SELECT * FROM tbArtist WHERE vcNameDisplay = ?");
            ps.setString(1,this.getNameDisplay());
            rs = ps.executeQuery();
            if (rs.next()) {
                this.setId(rs.getInt("inId"));
                loadFromRs(rs);
            } else {
                int id = this.getId();
                this.setId(0);
            }
            rs.close();
        } finally {
            if (rs != null) {
                rs.close(); rs=null;
            }
            if (ps != null) {
                ps.close(); ps=null;
            }
        }
    }

    public void loadFromRs(ResultSet rs) throws SQLException {
        this.setNameFirst(rs.getString("vcNameFirst"));
        this.setNameLast(rs.getString("vcNameLast"));
        this.setNameDisplay(rs.getString("vcNameDisplay"));
        this.setActive(rs.getBoolean("btActive"));
        this.setRoyaltyDollarRetail(rs.getBigDecimal("moRoyaltyDollarRetail"));
        this.setRoyaltyDollarWholesale(rs.getBigDecimal("moRoyaltyDollarWholesale"));
        this.setRoyaltyPercentRetail(rs.getFloat("deRoyaltyPercentRetail"));
        this.setRoyaltyPercentWholesale(rs.getFloat("deRoyaltyPercentWholesale"));
        this.setDateCreated(rs.getTimestamp("dtCreated"));
        this.setDateModified(rs.getTimestamp("dtModified"));
        this.setDesc(rs.getString("txDesc"));
    }
    public void saveToDb(Connection con) throws SQLException {
        PreparedStatement ps;

        if (this.getId() > 0) {
            ps = con.prepareStatement("UPDATE tbArtist SET vcNameFirst=?, vcNameLast=?, vcNameDisplay=?, btActive=?, moRoyaltyDollarRetail=?, moRoyaltyDollarWholesale=?, deRoyaltyPercentRetail=?, deRoyaltyPercentWholesale=?, dtModified = CURRENT_TIMESTAMP, txDesc=? WHERE inId = ?");
        } else {
            ps = con.prepareStatement("INSERT INTO tbArtist (vcNameFirst, vcNameLast, vcNameDisplay, btActive, moRoyaltyDollarRetail, moRoyaltyDollarWholesale, deRoyaltyPercentRetail, deRoyaltyPercentWholesale, dtCreated, dtModified, txDesc) VALUES(?,?,?,?,?,?,?,?,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,?)");
        }
        int i = 0;
        ps.setString(++i,this.getNameFirst());
        ps.setString(++i,this.getNameLast());
        ps.setString(++i,this.getNameDisplay());
        ps.setBoolean(++i,this.getActive());
        ps.setBigDecimal(++i,this.getRoyaltyDollarRetail());
        ps.setBigDecimal(++i,this.getRoyaltyDollarWholesale());
        ps.setFloat(++i,this.getRoyaltyPercentRetail());
        ps.setFloat(++i,this.getRoyaltyPercentWholesale());
        ps.setString(++i,this.getDesc());
        if (this.getId() > 0) {
            ps.setInt(++i,this.getId());
        }
        ps.execute();

        if (this.getId() == 0) {
            ps = con.prepareStatement("SELECT max(inId) AS inMaxId FROM tbArtist WHERE vcNameFirst = ? AND vcNameLast = ?");
            ps.setString(1,this.getNameFirst());
            ps.setString(2,this.getNameLast());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                this.setId(rs.getInt("inMaxId"));
            }
        }
    }

    public void setActive(boolean in) { this.active = in; }
    public void setDateCreated(Date in) { this.dateCreated = in; }
    public void setDateModified(Date in) { this.dateModified = in; }
    public void setDesc(String in) { this.desc = (in==null) ? "" : in; }
    public void setId(int in) { this.id = in; }
    public void setNameFirst(String in) { this.nameFirst = (in==null) ? "" : in; }
    public void setNameLast(String in) { this.nameLast = (in==null) ? "" : in; }
    public void setNameDisplay(String in) { this.nameDisplay = (in==null) ? "" : in; }
    public void setRoyaltyDollarRetail(BigDecimal in) { this.royaltyDollarRetail = in; }
    public void setRoyaltyDollarWholesale(BigDecimal in) { this.royaltyDollarWholesale = in; }
    public void setRoyaltyPercentRetail(float in) { this.royaltyPercentRetail = in; }
    public void setRoyaltyPercentWholesale(float in) { this.royaltyPercentWholesale = in; }
    public void setRelationship(String in) { this.relationship = (in==null) ? "" : in; }
}
