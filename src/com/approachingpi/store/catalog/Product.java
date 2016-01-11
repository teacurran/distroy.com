/*
 * User: Terrence
 * Date: Mar 5, 2002
 * Time: 1:05:25 AM
 * To change template for new class use
 * Code Style | Class Templates options (Tools | IDE Options).
 */
package com.approachingpi.store.catalog;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.ListIterator;

public class Product implements Serializable {
    private boolean activeForRetail     = false;
    private boolean activeForWholesale  = false;
    private ArrayList artists           = new ArrayList();
    private Brand brand;
    private ArrayList categories        = new ArrayList();
    private ListIterator categoriesIterator;
    private Date dateCreated;
    private Date dateModified;
    private Date dateInStock;
    private String desc                 = "";
    private int id;
    private ArrayList images            = new ArrayList();
    private String name                 = "";
    private String sku                  = "";
    private boolean taxable             = false;
    private String textDescription      = "";
    private ArrayList variations        = new ArrayList();

    public Product() {
    }
    public Product(String inName) {
        setName(inName);
    }
    public Product(int id) {
        setId(id);
    }
    public Product(int id, Connection con) throws Exception {
        loadFromDb(id, con);
    }

    public void addArtist(Artist in) {
        this.getArtists().add(in);
    }
    public void addVariation(ProductVariation in) {
        this.getVariations().add(in);
    }

    // GETTERS
    public boolean getActiveForRetail() { return this.activeForRetail; }
    public boolean getActiveForWholesale() { return this.activeForWholesale; }
    public ArrayList getArtists() {
        // never return null
        if (this.artists == null) {
            this.artists = new ArrayList();
        }
        return this.artists;
    }
    public Brand getBrand() {
        if (this.brand == null) {
            this.brand = new Brand();
        }
        return this.brand;
    }
    public Date getDateCreated() { return this.dateCreated; }
    public Date getDateInStock() { return this.dateInStock; }
    public Date getDateModified() { return this.dateModified; }
    public String getDesc() { return this.desc; }
	public String getDescFormatted() {
		String retVal = getDesc().trim();
		retVal = retVal.replaceAll("\\r","");
		retVal = retVal.replaceAll("\\f","");
		retVal = retVal.replaceAll("\\n","<br />");
		return retVal;
	}
    public Artist getFirstArtist() {
        if (getArtists().size() > 0) {
            return (Artist)getArtists().get(0);
        }
        return new Artist();
    }
    public Image getFirstImage() {
        return getFirstImage(Image.ANY_ORIENTATION);
    }
    public Image getFirstImage(int orientation) {
        Image image = new Image();
        for(int i=0; i<images.size(); i++) {
            Image thisImage = (Image)images.get(i);
            if (orientation == Image.ANY_ORIENTATION || thisImage.getOrientation() == orientation) {
                image = thisImage;
                break;
            }
        }
        return image;
    }
    // this is a convienence method since the first variation is often needed.
    // it never returns null, even if there are no variations.
    public ProductVariation getFirstVariation() {
        if (variations.size() > 0) {
            return (ProductVariation)variations.get(0);
        } else {
            return new ProductVariation();
        }
    }
    public int getId() { return this.id; }
    public ArrayList getImages() { return this.images; }
    public String getName() { return this.name; }
    public String getSku() { return this.sku; }
    public boolean getTaxable() { return this.taxable; }
    public String getTextDescription() { return this.textDescription; }
    public ProductVariation getVariationById(int variationId) {
        for (int i=0; i<getVariations().size(); i++) {
            if (((ProductVariation)variations.get(i)).getId() == variationId) {
                return (ProductVariation)variations.get(i);
            }
        }
        return null;
    }

    public ArrayList getVariations() { return this.variations; }

	public boolean equals(Object obj) {
        try {
            if (((Product)obj).getId() == this.getId()) {
                return true;
            }
        } catch (Exception e) {}
        return false;
    }


    // Load Methods
    public void loadArtistsFromDb(Connection con) throws SQLException {
        if (getId() <= 0) {
            return;
        }
        PreparedStatement ps = con.prepareStatement("SELECT * FROM tbArtist WHERE inId IN (SELECT inArtistId FROM tbLinkProductArtist WHERE inProductId = ?)");
        ps.setInt(1,this.getId());
        ResultSet rs = ps.executeQuery();

        artists = new ArrayList();
        while (rs.next()) {
            Artist artist = new Artist(rs.getInt("inId"));
            artist.loadFromRs(rs);
            artists.add(artist);
        }
        rs.close();
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
            PreparedStatement ps = con.prepareStatement("SELECT * FROM tbProduct WHERE inId = ?");
            ps.setInt(1,this.getId());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                this.loadFromRs(rs);
                rs.close();

                ps = con.prepareStatement("SELECT * FROM tbBrand WHERE inId = ?");
                ps.setInt(1,this.getBrand().getId());
                rs = ps.executeQuery();
                if (rs.next()) {
                    // this is all we need for the product page
                    this.getBrand().setName(rs.getString("vcName"));
                    this.getBrand().setLogo(rs.getString("vcLogo"));
                    this.getBrand().setActive(rs.getBoolean("btActive"));
                    this.getBrand().setDesc(rs.getString("txDesc"));
                }
                rs.close();
            } else {
                int productId = this.getId();
                this.setId(0);
                throw new Exception("Product Id " + productId + " not found");
            }
            rs.close();

        } catch (Exception e) {
            //System.err.println(e.toString() + " - " + e.getMessage());
            e.printStackTrace();
        }
    }
    public void loadImagesFromDb(Connection con) throws SQLException {
        loadImagesFromDb(con, -1, Image.ANY_ORIENTATION);
    }
    public void loadImagesFromDb(Connection con, int max, int orientation) throws SQLException {
        if (getId() <= 0) {
            return;
        }
        try {
            int i = 0;
            PreparedStatement ps;
            if (orientation == Image.ANY_ORIENTATION) {
                ps = con.prepareStatement("SELECT * FROM tbImage WHERE inProductId = ? ORDER BY inRank, inId");
            } else {
                ps = con.prepareStatement("SELECT * FROM tbImage WHERE inOrientation = ? AND inProductId = ? ORDER BY inRank, inId");
                ps.setInt(++i, orientation);
            }

            ps.setInt(++i,this.getId());
            ResultSet rs = ps.executeQuery();
            images = new ArrayList();
            while (rs.next()) {
                Image image = new Image(rs.getInt("inId"));
                image.loadFromRs(rs);
                this.getImages().add(image);
                if (max>0 && max==this.getImages().size()) {
                    rs.close();
                    break;
                }
            }
            rs.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadFromRs(ResultSet rs) throws SQLException {
        this.setName(rs.getString("vcName"));
        // vcSku does not appear when we are coming from the search engine.
        try {
            this.setSku(rs.getString("vcSku"));
        } catch (Exception e) {}
        this.setActiveForRetail(rs.getBoolean("btActiveForRetail"));
        this.setActiveForWholesale(rs.getBoolean("btActiveForWholesale"));
        this.setTaxable(rs.getBoolean("btTaxable"));
        this.setBrand(new Brand(rs.getInt("inBrandId")));
        this.setDateCreated(rs.getTimestamp("dtCreated"));
        this.setDateModified(rs.getTimestamp("dtModified"));
        this.setDateInStock(rs.getTimestamp("dtInStock"));
        this.setDesc(rs.getString("txDesc"));
        this.setTextDescription(rs.getString("txTextDescription"));
    }
    public void loadExtendedFromDb(Connection con) throws SQLException {
        getBrand().loadFromDb(con);
        loadVariations(con);
        loadImagesFromDb(con);
        loadArtistsFromDb(con);
    }

    public void loadVariations(Connection con) throws SQLException {
        loadVariations(con,false);
    }

    public void loadVariations(Connection con, boolean activeOnly) throws SQLException {
        variations = new ArrayList();
        try {
            PreparedStatement ps;
            if (activeOnly) {
                ps = con.prepareStatement("SELECT * FROM tbProductVariation WHERE inProductId = ? AND btActive = 1 ORDER BY inRank");
            } else {
                ps = con.prepareStatement("SELECT * FROM tbProductVariation WHERE inProductId = ? ORDER BY inRank");
            }
            ps.setInt(1,this.getId());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ProductVariation variation = new ProductVariation(this);
                variation.setId(rs.getInt("inId"));
                variation.loadFromRs(rs);
                variation.loadSizes(con);
                this.addVariation(variation);
            }
            rs.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadVariationImages(Connection con, int maxPerVariation) throws SQLException {
        loadVariationImages(con,maxPerVariation,com.approachingpi.store.catalog.Image.ANY_ORIENTATION);
    }

    public void loadVariationImages(Connection con, int maxPerVariation, int orientation) throws SQLException {
        for (int i=0; i<variations.size(); i++) {
            ((ProductVariation)variations.get(i)).loadImagesFromDb(con,maxPerVariation,orientation);

        }
    }


    public void loadVariationSizes(Connection con) throws SQLException {
        for (int i=0; i<variations.size(); i++) {
            ((ProductVariation)variations.get(i)).loadSizes(con);
        }
    }

    public void saveToDb(Connection con) throws Exception {
        PreparedStatement ps;

        if (this.getId() > 0) {
            ps = con.prepareStatement("UPDATE tbProduct SET vcName=?, vcSku=?, btActiveForRetail=?, btActiveForWholesale=?, btTaxable=?, inBrandId=?, dtModified=CURRENT_TIMESTAMP, dtInStock=?, txDesc=?, txTextDescription=? WHERE inId = ?");
        } else {
            ps = con.prepareStatement("INSERT INTO tbProduct (vcName, vcSku, btActiveForRetail, btActiveForWholesale, btTaxable, inBrandId, dtInStock, dtModified, dtCreated, txDesc, txTextDescription) VALUES(?,?,?,?,?,?,?,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,?,?)");
        }
        int i = 0;
        ps.setString(++i,this.getName());
        ps.setString(++i,this.getSku());
        ps.setBoolean(++i,this.getActiveForRetail());
        ps.setBoolean(++i,this.getActiveForWholesale());
        ps.setBoolean(++i,this.getTaxable());
        ps.setInt(++i,this.getBrand().getId());
        if (this.getDateInStock() == null) {
            ps.setNull(++i,java.sql.Types.TIMESTAMP);
        } else {
            ps.setDate(++i,new java.sql.Date(this.getDateInStock().getTime()));
        }
        ps.setString(++i,this.getDesc());
        ps.setString(++i,this.getTextDescription());

        if (this.getId() > 0) {
            ps.setInt(++i,this.getId());
        }
        ps.execute();

        if (this.getId() == 0) {
            ps = con.prepareStatement("SELECT P.* FROM tbProduct P WHERE P.inId IN(SELECT max(inId) FROM tbProduct WHERE vcName=? AND inBrandId=?)");
            ps.setString(1,this.getName());
            ps.setInt(2,this.getBrand().getId());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                this.setId(rs.getInt("inId"));
                this.setDateCreated(rs.getTimestamp("dtCreated"));
                this.setDateModified(rs.getTimestamp("dtModified"));
            }
			rs.close(); rs=null;
        }
    }
    public void saveArtistsToDb(Connection con) throws Exception {
        String artistList = "0";
        for (int x=0; x<getArtists().size(); x++) {
            Artist artist = (Artist)getArtists().get(x);

            PreparedStatement ps = con.prepareStatement(""+
                "IF ((SELECT Count(*) FROM tbLinkProductArtist WHERE inProductId = ? AND inArtistId = ?) > 0) "+
                "BEGIN "+
                    "UPDATE tbLinkProductArtist SET vcRelationship = ? "+
                    "WHERE inProductId = ? AND inArtistId = ? "+
                "END "+
                "ELSE "+
                "BEGIN "+
                    "INSERT INTO tbLinkProductArtist ("+
                        "inProductId,"+
                        "inArtistId,"+
                        "vcRelationship"+
                    ") VALUES ("+
                        "?,?,?"+
                    ")"+
                "END");
            int i = 0;
            ps.setInt(++i,getId());
            ps.setInt(++i,artist.getId());
            ps.setString(++i,artist.getRelationship());
            ps.setInt(++i,getId());
            ps.setInt(++i,artist.getId());
            ps.setInt(++i,getId());
            ps.setInt(++i,artist.getId());
            ps.setString(++i,artist.getRelationship());
            ps.execute();

            artistList += "," + artist.getId();

            ps.close(); ps=null;
        }

        PreparedStatement ps = con.prepareStatement("DELETE FROM tbLinkProductArtist WHERE inProductId = ? and inArtistId NOT IN (" + artistList + ")");
        ps.setInt(1,this.getId());
        ps.execute();
        ps.close(); ps=null;
    }

    // SETTERS
    public void setActiveForRetail(boolean inActive) { this.activeForRetail = inActive; }
    public void setActiveForWholesale(boolean in) { this.activeForWholesale = in; }
    public void setBrand(Brand in) { this.brand = in; }
    public void setDateCreated(Date in) {this.dateCreated = in; }
    public void setDateModified(Date in) { this.dateModified = in; }
    public void setDateInStock(Date in) { this.dateInStock = in; }
    public void setDesc(String in) { this.desc = (in == null) ? "" : in; }
    public void setId(int in) { this.id = in; }
    public void setName(String in) { this.name = (in == null) ? "" : in; }
    public void setSku(String in) { this.sku = (in==null) ? "" : in; }
    public void setTaxable(boolean inTaxable) { this.taxable = inTaxable; }
    public void setTextDescription(String in) { this.textDescription = (in==null) ? "" : in; }

    public void sortImagesByRank(Connection con) throws Exception {
        if (getId() <= 0) {
            return;
        }
        this.loadImagesFromDb(con);
        for (int i=0; i<getImages().size(); i++) {
            Image image = (Image)getImages().get(i);
            image.setRank(i*2);
            image.saveToDb(con);
        }
    }
    public void sortVariationsByRank(Connection con) throws SQLException {
        if (getId() <= 0) {
            return;
        }
        this.loadVariations(con);
        for (int i=0; i<getVariations().size(); i++) {
            ProductVariation variation = (ProductVariation)getVariations().get(i);
            variation.setRank(i*2);
            variation.saveToDb(con);
        }
    }

    public void addCategory(Category in) {
        this.categories.add(in);
    }
    public boolean hasNextCategory() {
        if (categoriesIterator == null) {
            resetCategories();
        }
        return categoriesIterator.hasNext();
    }
    public Category nextCategory() {
        if (categoriesIterator == null) {
            resetCategories();
        }
        if (categoriesIterator.hasNext()) {
            return (Category)categoriesIterator.next();
        } else {
            return null;
        }
    }
    public void resetCategories() {
        categoriesIterator = categories.listIterator();
    }


}
