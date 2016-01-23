/*
 * Created by IntelliJ IDEA.
 * User: terrence
 * Date: Jun 25, 2004
 * Time: 4:04:30 AM
 * To change template for new class use
 * Code Style | Class Templates options (Tools | IDE Options).
 */
package com.approachingpi.store.catalog;

import com.approachingpi.store.Store;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class ProductVariation {
    protected boolean active = false;
    protected ArrayList categories = new ArrayList();
    protected String color = "";
    protected String desc = "";
    protected int id;
    protected ArrayList images = new ArrayList();
    protected int qtyInStock;
    protected BigDecimal priceCost;
    protected BigDecimal priceRetail;
    protected BigDecimal priceRetailSale;
    protected BigDecimal priceWholesale;
    protected BigDecimal priceWholesaleSale;
    protected Product product;
    protected int rank;
    protected boolean sale = false;
    protected ArrayList sizes = new ArrayList();
    protected String sku  = "";
    protected ArrayList stores = new ArrayList();
    protected String style = "";
    protected String thumb = "";

    public ProductVariation() {
    }
    public ProductVariation(int id) {
        this.setId(id);
    }
    public ProductVariation(Product product) {
        this.setProduct(product);
    }

    public void addCategory(Category in) { categories.add(in); }
    public void addImage(Image in) { images.add(in); }
    public void addSize(Size in) { sizes.add(in); }
    public boolean equals(ProductVariation obj) {
        if (obj != null && obj.getId() == this.getId()) {
            return true;
        }
        return false;
    }
    public boolean getActive() { return this.active; }
    public ArrayList getCategories() {
        if (categories == null) {
            categories = new ArrayList();
        }
        return categories;
    }
    public String getColor() { return this.color; }
    public String getDesc() { return this.desc; }
    public Image getFirstImage() {
        if (getImages().size() > 0) {
            return (Image)getImages().get(0);
        }
        return new Image();
    }
    public int getId() { return this.id; }
    public ArrayList getImages() {
        if (images == null) {
            images = new ArrayList();
        }
        return images;
    }
    public int getQtyInStock() { return this.qtyInStock; }
    public BigDecimal getPriceCost() {
        if (priceCost == null) {
            priceCost = new BigDecimal("0.0000");
        }
        return this.priceCost;
    }
    public BigDecimal getPriceRetail() {
        if (priceRetail == null) {
            priceRetail = new BigDecimal("0.0000");
        }
        return this.priceRetail;
    }
    public BigDecimal getPriceRetailSale() {
        if (priceRetailSale == null) {
            priceRetailSale = new BigDecimal("0.0000");
        }
        return this.priceRetailSale;
    }
    public BigDecimal getPriceWholesale() {
        if (priceWholesale == null) {
            priceWholesale = new BigDecimal("0.0000");
        }
        return this.priceWholesale;
    }
    public BigDecimal getPriceWholesaleSale() {
        if (priceWholesaleSale == null) {
            priceWholesaleSale = new BigDecimal("0.0000");
        }
        return this.priceWholesaleSale;
    }
    public BigDecimal getPrice(Store store) {
        // If product is on sale, return the sale price
        if (isSale()) {
            if (store.isWholesale()) {
                if (!getPriceWholesaleSale().equals("0.00")) {
                    return getPriceWholesaleSale();
                }
            } else {
                if (!getPriceRetailSale().equals("0.00")) {
                    return getPriceRetailSale();
                }
            }
        }

        // return the regular price
        if (store.isWholesale()) {
            return getPriceWholesale();
        } else {
            return getPriceRetail();
        }
    }
    public BigDecimal getPriceRegular(Store store) {
        // return the regular price
        if (store.isWholesale()) {
            return getPriceWholesale();
        } else {
            return getPriceRetail();
        }
    }
    public Product getProduct() {
        if (product == null) {
            product = new Product();
        }
        return product;
    }
    public int getRank() {
        return this.rank;
    }
    public int getDiscountPercent(Store store) {
        if (!isSale()) {
            return 0;
        }

        float price = getPrice(store).floatValue();
        float priceRegular = getPriceRegular(store).floatValue();

        float percent = 100 - price * 100 / priceRegular;

        return (int)Math.floor(percent);
    }
    public Size getSize(Size size) {
        for (int i=0; i<sizes.size(); i++) {
            Size thisSize = (Size)sizes.get(i);
            if (thisSize.equals(size)) {
                return thisSize;
            }
        }
        return null;
    }
    public ArrayList getSizes() {
        return this.sizes;
    }
    public String getSku() {
        return this.sku;
    }
    public ArrayList getStores() {
        if (stores == null) {
            stores = new ArrayList();
        }
        return stores;
    }
    public String getStyle() {
        return this.style;
    }
    public String getThumb() {
        return this.thumb;
    }

    public boolean isInCategory(Category in) {
        if (in==null) {
            return false;
        }
        boolean retVal = false;
        for (int i=0; i<getCategories().size(); i++) {
            Category thisCategory = (Category)getCategories().get(i);
            if (thisCategory.getId() == in.getId()) {
                retVal = true;
            }
        }
        return retVal;
    }
    public boolean isSale() {
        return sale;
    }

    // load methods

    public void loadCategoriesFromDb(Connection con) throws SQLException {
        if (getId() <= 0) {
            return;
        }

        PreparedStatement ps = con.prepareStatement("SELECT * FROM tbCategory WHERE inId IN (SELECT inCategoryId FROM tbLinkProductVariationCategory WHERE inProductVariationId = ?) ORDER BY vcPath");
        ps.setInt(1,this.getId());
        ResultSet rs = ps.executeQuery();
        categories = new ArrayList();
        while (rs.next()) {
            Category category = new Category(rs.getInt("inId"));
            category.loadFromRs(rs);
            categories.add(category);
        }
        rs.close();
    }

    public void loadFromDb(Connection con) throws SQLException {
        if (getId() <= 0) {
            return;
        }
        try {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM tbProductVariation WHERE inId = ?");
            ps.setInt(1,this.getId());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                loadFromRs(rs);
            } else {
                int id = this.getId();
                this.setId(0);
                throw new Exception("Product Variation Id " + id + " not found");
            }
            rs.close();

        } catch (Exception e) {
            //System.err.println(e.toString() + " - " + e.getMessage());
            e.printStackTrace();
        }
    }
    public void loadFromRs(ResultSet rs) throws SQLException {
        if (this.getProduct().getId() == 0) {
            this.setProduct(new Product(rs.getInt("inProductId")));
        }
        this.setSku(rs.getString("vcSku"));
        this.setStyle(rs.getString("vcStyle"));
        this.setColor(rs.getString("vcColor"));
        this.setDesc(rs.getString("vcDesc"));
        //this.setThumb(rs.getString("vcThumb"));
        this.setPriceCost(rs.getBigDecimal("moPriceCost"));
        this.setPriceRetail(rs.getBigDecimal("moPriceRetail"));
        this.setPriceRetailSale(rs.getBigDecimal("moPriceRetailSale"));
        this.setPriceWholesale(rs.getBigDecimal("moPriceWholesale"));
        this.setPriceWholesaleSale(rs.getBigDecimal("moPriceWholesaleSale"));
        this.setSale(rs.getBoolean("btSale"));
        this.setActive(rs.getBoolean("btActive"));
        this.setRank(rs.getInt("inRank"));
    }
    public void loadImagesFromDb(Connection con) throws SQLException {
        loadImagesFromDb(con,-1,Image.ANY_ORIENTATION);
    }

    public void loadImagesFromDb(Connection con, int max, int orientation) throws SQLException {
        if (getId() <= 0) {
            return;
        }
        images = new ArrayList();
        try {
            int i = 0;
            PreparedStatement ps;
            if (orientation == Image.ANY_ORIENTATION) {
                ps = con.prepareStatement("SELECT I.* FROM tbImage I, tbLinkProductVariationImage L WHERE I.inId = L.inImageId AND L.inProductVariationid = ? ORDER BY L.inRank");
            } else {
                ps = con.prepareStatement("SELECT I.* FROM tbImage I, tbLinkProductVariationImage L WHERE I.inOrientation=? AND I.inId = L.inImageId AND L.inProductVariationid = ? ORDER BY L.inRank");
                ps.setInt(++i,orientation);
            }

            ps.setInt(++i,this.getId());
            ResultSet rs = ps.executeQuery();
            int imageCount = 0;
            while (rs.next()) {
                Image image = new Image(rs.getInt("inId"));
                image.loadFromRs(rs);
                this.getImages().add(image);
                imageCount++;
                if (max>0 && max==imageCount) {
                    break;
                }
            }
            rs.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void loadSizes(Connection con) throws SQLException {
        PreparedStatement ps = con.prepareStatement("SELECT S.*, L.inRank, L.inQtyInStock FROM tbSize S, tbLinkProductVariationSize L WHERE S.inId = L.inSizeId AND L.inProductVariationId = ? ORDER BY S.inRank");
        ps.setInt(1,this.getId());
        ResultSet rs = ps.executeQuery();
        sizes = new ArrayList();
        qtyInStock = 0;
        while (rs.next()) {
            Size size = new Size(rs.getInt("inId"));
            size.loadFromRs(rs);
            size.setQtyInStock(rs.getInt("inQtyInStock"));
            sizes.add(size);

            qtyInStock += size.getQtyInStock();
        }
        rs.close();
    }
    public void loadStoresFromDb(Connection con) throws SQLException {
        PreparedStatement ps = con.prepareStatement("" +
                "SELECT * FROM tbStore\n" +
                "WHERE inId IN (\n"+
                    "\tSELECT inStoreId FROM tbLNKProductVariationStore\n" +
                    "\tWHERE inProductVariationId=?\n"+
                ") ORDER BY vcName");
        ps.setInt(1, this.getId());
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            Store thisStore = new Store(rs.getInt("inId"));
            thisStore.setId(rs.getInt("inId"));
            thisStore.loadFromRs(rs);
            getStores().add(thisStore);
        }
        rs.close();
        rs = null;
    }
    public void saveCategoriesToDb(Connection con) throws Exception {
        String categoryList = "0";
        for (int x=0; x<this.getCategories().size(); x++) {
            Category cat = (Category)getCategories().get(x);

            categoryList += "," + cat.getId();

			String sqlStatement = "INSERT INTO tbLinkProductVariationCategory ("+
						"inProductVariationId,"+
						"inCategoryId,"+
						"inRank"+
					") VALUES ("+
						"?,?,0"+
					") ON DUPLICATE KEY UPDATE inProductVariationId = inProductVariationId";

			PreparedStatement ps = con.prepareStatement(sqlStatement);

			// insert
			ps.setInt(1,this.getId());
			ps.setInt(2,cat.getId());

			ps.execute();
        }
        // delete any sizes we are not using that are assigned to this variation.
        PreparedStatement ps = con.prepareStatement("DELETE FROM tbLinkProductVariationCategory WHERE inProductVariationId = ? AND inCategoryId NOT IN (" + categoryList + ")");
        ps.setInt(1,this.getId());
        ps.execute();
    }

    public void deleteSize(Connection con, Size size) throws SQLException {
        PreparedStatement ps = null;
        try {
            ps = con.prepareStatement("DELETE FROM tbLinkProductVariationSize WHERE inProductVariationId = ? AND inSizeId = ?");
            ps.setInt(1, this.getId());
            ps.setInt(2, size.getId());
            ps.executeUpdate();

            for (int i=sizes.size()-1; i>=0; i--) {
                Size thisSize = (Size)sizes.get(i);
                if (thisSize.equals(size)) {
                    sizes.remove(i);
                }
            }
        } finally {
            if (ps!=null) {
                ps.close(); ps=null;
            }
        }
    }

    public void saveToDb(Connection con) throws SQLException {
        PreparedStatement ps;

        if (this.getId() > 0) {
            ps = con.prepareStatement("UPDATE tbProductVariation " +
				"SET inProductId=?, vcSku=?, vcStyle=?, vcColor=?, vcDesc=?, " +
				"moPriceCost=?, moPriceRetail=?, moPriceRetailSale=?, moPriceWholesale=?, moPriceWholesaleSale=?, " +
				"btSale=?, btActive=?, inRank=? WHERE inId = ?");
        } else {
            ps = con.prepareStatement("INSERT INTO tbProductVariation (" +
				"inProductId, vcSku, vcStyle, vcColor, vcDesc, " +
				"moPriceCost, moPriceRetail, moPriceRetailSale, moPriceWholesale, moPriceWholesaleSale, " +
				"btSale, btActive, inRank" +
				") VALUES(" +
					"?,?,?,?,?,?,?,?,?,?,?,?,?" +
				")");
        }
        int i = 0;
        ps.setInt(++i,this.getProduct().getId());
        ps.setString(++i,this.getSku());
        ps.setString(++i,this.getStyle());
        ps.setString(++i,this.getColor());
        ps.setString(++i,this.getDesc());
        //ps.setString(++i,this.getThumb());
        ps.setBigDecimal(++i,this.getPriceCost());
        ps.setBigDecimal(++i,this.getPriceRetail());
        ps.setBigDecimal(++i,this.getPriceRetailSale());
        ps.setBigDecimal(++i,this.getPriceWholesale());
        ps.setBigDecimal(++i,this.getPriceWholesaleSale());
        ps.setBoolean(++i,this.isSale());
        ps.setBoolean(++i,this.getActive());
        ps.setInt(++i,this.getRank());
        if (this.getId() > 0) {
            ps.setInt(++i,this.getId());
        }
        ps.execute();

        if (this.getId() == 0) {
            ps = con.prepareStatement("SELECT max(inId) AS inMaxId FROM tbProductVariation WHERE vcSku = ? AND vcDesc = ?");
            ps.setString(1,this.getSku());
            ps.setString(2,this.getDesc());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                this.setId(rs.getInt("inMaxId"));
            }
			rs.close(); rs=null;
			ps.close(); ps=null;
        }


    }

    public void saveImagesToDb(Connection con) throws SQLException {
        String imageList = "0";
        PreparedStatement ps;
        for (int x=0; x<this.getImages().size(); x++) {
            Image image = (Image)getImages().get(x);

            imageList += "," + image.getId();

            ps = con.prepareStatement(""+
                "IF ((SELECT Count(*) FROM tbLinkProductVariationImage WHERE inProductVariationId = ? AND inImageId = ?) = 0) "+
                "BEGIN "+
                    "INSERT INTO tbLinkProductVariationImage ("+
                        "inProductVariationId,"+
                        "inImageId"+
                    ") VALUES ("+
                        "?,?"+
                    ")"+
                "END");
            int i=0;
            // IF
            ps.setInt(++i,this.getId());
            ps.setInt(++i,image.getId());
            ps.setInt(++i,this.getId());
            ps.setInt(++i,image.getId());
            ps.execute();
			ps.close(); ps=null;
        }
        // delete any images we are not using that are assigned to this variation.
        ps = con.prepareStatement("DELETE FROM tbLinkProductVariationImage WHERE inProductVariationId = ? AND inImageId NOT IN (" + imageList + ")");
        ps.setInt(1,this.getId());
        ps.execute();
    }

    public void saveSizesToDb(Connection con) throws SQLException {
        PreparedStatement ps = null;
        String sizeList = "0";
        try {
            // insert or update the sizes.
            for (int x=0; x<this.getSizes().size(); x++) {
                Size size = (Size)getSizes().get(x);

                sizeList += "," + size.getId();

                ps = con.prepareStatement(""+
                    "IF ((SELECT Count(*) FROM tbLinkProductVariationSize WHERE inProductVariationId = ? AND inSizeId = ?) > 0) "+
                    "BEGIN "+
                        "UPDATE tbLinkProductVariationSize SET inQtyInStock = inQtyInStock + ?, inRank = ? "+
                        "WHERE inProductVariationId = ? AND inSizeId = ? "+
                    "END "+
                    "ELSE "+
                    "BEGIN "+
                        "INSERT INTO tbLinkProductVariationSize ("+
                            "inProductVariationId,"+
                            "inSizeId,"+
                            "inQtyInStock,"+
                            "inRank"+
                        ") VALUES ("+
                            "?,?,?,?"+
                        ")"+
                    "END");
                int i=0;
                // IF
                ps.setInt(++i,this.getId());
                ps.setInt(++i,size.getId());

                // update
                ps.setInt(++i,size.getQtyToAdd());
                ps.setInt(++i,x*2);
                ps.setInt(++i,this.getId());
                ps.setInt(++i,size.getId());

                // insert
                ps.setInt(++i,this.getId());
                ps.setInt(++i,size.getId());
                ps.setInt(++i,size.getQtyToAdd());
                ps.setInt(++i,x*2);

                ps.execute();
            }
        } finally {
            if (ps != null) { ps.close(); ps=null; }
        }
        // delete any sizes we are not using that are assigned to this variation.
        try {
            ps = con.prepareStatement("DELETE FROM tbLinkProductVariationSize WHERE inProductVariationId = ? AND inSizeId NOT IN (" + sizeList + ")");
            ps.setInt(1,this.getId());
            ps.execute();
        } finally {
            if (ps != null) { ps.close(); ps=null; }
        }
    }

	public void sortImagesByRank(Connection con) throws SQLException {
		PreparedStatement ps;
        this.loadImagesFromDb(con);
		for (int i=0; i<this.getImages().size(); i++) {
		    Image image = (Image)getImages().get(i);
			ps = con.prepareStatement("UPDATE tbLinkProductVariationImage SET inRank=? WHERE inProductVariationId=? AND inImageId=?");
			ps.setInt(1,i*2);
            ps.setInt(2,this.getId());
            ps.setInt(2,image.getId());
            ps.execute();
		}
        this.loadImagesFromDb(con);
	}

    public void setActive(boolean in) { this.active = in; }
    public void setColor(String in) { this.color = (in==null) ? "" : in; }
    public void setDesc(String in) { this.desc = (in==null) ? "" : in; }
    public void setId(int in) { this.id = in; }
    public void setPriceRetail(BigDecimal in) { this.priceRetail = in; }
    public void setPriceRetailSale(BigDecimal in) { this.priceRetailSale = in; }
    public void setPriceCost(BigDecimal in) { this.priceCost = in; }
    public void setPriceWholesale(BigDecimal in) { this.priceWholesale = in; }
    public void setPriceWholesaleSale(BigDecimal in) { this.priceWholesaleSale = in; }
    public void setProduct(Product in) { this.product = in; }
    public void setSale(boolean in) { this.sale = in; }
    public void setSku(String in) { this.sku = (in==null) ? "" : in; }
    public void setStyle(String in) { this.style = (in==null) ? "" : in; }
    public void setRank(int in) { this.rank = in; }
    public void setThumb(String in) { this.thumb = (in==null) ? "" : in; }
}
