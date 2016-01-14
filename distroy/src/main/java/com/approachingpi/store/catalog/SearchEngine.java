/*
 * (c)Approaching Pi, Inc.
 * User: terrence
 * Date: Jul 10, 2004
 * Time: 3:50:07 AM
 * Desc:
 *
 */
package com.approachingpi.store.catalog;

import com.approachingpi.util.PiUtility;
import com.approachingpi.store.Store;
import com.approachingpi.servlet.Session;

import java.util.ArrayList;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.math.BigDecimal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SearchEngine {
    private Log log = LogFactory.getLog(SearchEngine.class);

    public static final int ORDER_NAME      = 0;

    protected boolean activeForRetail = false;
    protected boolean activeForWholesale = false;
    protected boolean activeOnly = false;
    protected ArrayList artists = new ArrayList();
    protected ArrayList brands = new ArrayList();
    protected ArrayList categories = new ArrayList();
    protected Product input;
    protected ArrayList stores = new ArrayList();
    protected boolean variationRequired = false;
    protected BigDecimal priceLow;
    protected BigDecimal priceHigh;
    protected Session session;

    public void addArtist(Artist in) {
        if (in != null) {
            if (in.getId() > 0) {
                getArtists().add(in);
            }
        }
    }

    public void addBrand(Brand in) {
        if (in != null && in.getId() > 0) {
            getBrands().add(in);
        }
    }

    public void addCategory(Category in) {
        if (in != null) {
            if (in.getId() > 0) {
                getCategories().add(in);
            }
        }
    }

    public void addStore(Store in) {
        if (in != null && in.getId() > 0) {
            getStores().add(in);
        }
    }

    public boolean containsArtist(int artistId) {
        for (int i=0; i<artists.size(); i++) {
            if (((Artist)artists.get(i)).getId() == artistId) {
                return true;
            }
        }
        return false;
    }
    public boolean containsCategory(int categoryId) {
        for (int i=0; i<categories.size(); i++) {
            if (((Category)categories.get(i)).getId() == categoryId) {
                return true;
            }
        }
        return false;
    }
    public boolean getActiveForRetail() {
        return this.activeForRetail;
    }
    public boolean getActiveForWholesale() {
        return this.activeForWholesale;
    }
    public boolean getActiveOnly() {
        return this.activeOnly;
    }
    public ArrayList getArtists() {
        if (artists == null) {
            artists = new ArrayList();
        }
        return artists;
    }
    public ArrayList getBrands() {
        if (brands == null) {
            brands = new ArrayList();
        }
        return brands;
    }
    public ArrayList getCategories() {
        if (categories == null) {
            categories = new ArrayList();
        }
        return categories;
    }
    public Product getInput() {
        if (input == null) {
            input = new Product();
        }

        return input;
    }
    public BigDecimal getPriceLow() {
        return priceLow;
    }
    public BigDecimal getPriceHigh() {
        return priceHigh;
    }
    public ArrayList getStores() {
        if (stores == null) {
            stores = new ArrayList();
        }
        return stores;
    }

    public boolean getVariationRequired() {
        return variationRequired;
    }

    public String getSql() {
        String linkVariation = "LEFT OUTER JOIN";
        String linkBrand = "LEFT OUTER JOIN";
        String whereAnd = "WHERE";

        if (getVariationRequired()) {
            linkVariation = "JOIN";
        }

        StringBuilder sqlWhereClauses = new StringBuilder(2000);

        if (getActiveOnly()) {
            sqlWhereClauses.append(whereAnd + " PV.btActive = 1 \n");
            whereAnd = "AND";
            linkVariation = "JOIN";
        }

        if (getActiveForRetail()) {
            sqlWhereClauses.append(whereAnd + " P.btActiveForRetail = 1 \n");
            whereAnd = "AND";
        }

        if (getActiveForWholesale()) {
            sqlWhereClauses.append(whereAnd + " P.btActiveForWholesale = 1 \n");
            whereAnd = "AND";
        }

        if (getInput().getBrand().getId() > 0) {
            sqlWhereClauses.append(whereAnd + " P.inBrandId = " + input.getBrand().getId() + " \n");
            whereAnd = "AND";
        }

        if (priceLow != null || priceHigh != null) {
            String priceCol = "moPriceRetail";
            if (session != null && session.getIsWholesale()) {
                priceCol = "moPriceWholesale";
            }
            if (priceLow != null && priceHigh != null) {
                sqlWhereClauses.append(whereAnd + " (");
                    sqlWhereClauses.append("\t(PV.btSale=1 AND PV." + priceCol + "Sale BETWEEN " + priceLow + " AND " + priceHigh + ")\n");
                    sqlWhereClauses.append("\tOR (PV.btSale=0 AND PV." + priceCol + " BETWEEN " + priceLow + " AND " + priceHigh + ")\n");
                sqlWhereClauses.append(")\n");
            } else if (priceLow != null) {
                sqlWhereClauses.append(whereAnd + " (");
                    sqlWhereClauses.append("\t(PV.btSale=1 AND PV." + priceCol + "Sale >= " + priceLow + ")\n");
                    sqlWhereClauses.append("\tOR (PV.btSale=0 AND PV." + priceCol + " >= " + priceLow + ")\n");
                sqlWhereClauses.append(")\n");
            } else if (priceHigh != null) {
                sqlWhereClauses.append(whereAnd + " (");
                    sqlWhereClauses.append("\t(PV.btSale=1 AND PV." + priceCol + "Sale <= " + priceHigh + ")\n");
                    sqlWhereClauses.append("\tOR (PV.btSale=0 AND PV." + priceCol + " <= " + priceHigh + ")\n");
                sqlWhereClauses.append(")\n");
            }
        }

        if (getInput().getVariations().size() > 0) {
            ArrayList variations = input.getVariations();
            for (int i=0; i<variations.size(); i++) {
                ProductVariation thisVariation = (ProductVariation)variations.get(i);
                if (thisVariation.getSku().length() > 0) {
                    sqlWhereClauses.append(whereAnd + " PV.vcSku LIKE '" + PiUtility.replace(thisVariation.getSku(),"'","''") + "' \n");
                    linkVariation = "JOIN";
                    whereAnd = "AND";
                }
            }
        }

        if (brands.size() > 0) {
            for (int i=0; i<brands.size(); i++) {
                Brand thisBrand = (Brand)brands.get(i);
                if (i==0) {
                    sqlWhereClauses.append(whereAnd + "( \n\t");
                    whereAnd = "AND";
                } else {
                    sqlWhereClauses.append("\tOR ");
                }
                sqlWhereClauses.append("P.inBrandId=" + thisBrand.getId() + "\n");
                if (i == brands.size()-1) {
                    sqlWhereClauses.append(")\n");
                }
            }
        }

        if (stores.size() > 0) {
            for (int i=0; i<stores.size(); i++) {
                Store thisStore = (Store)stores.get(i);
                if (i==0) {
                    sqlWhereClauses.append(whereAnd + " (\n\t");
                } else {
                    sqlWhereClauses.append("\tOR");
                }
                sqlWhereClauses.append("PV.inId IN (SELECT inProductVariationId FROM tbLinkProductVariationStore WHERE inStoreId=" + thisStore.getId() + ")\n");
                if (i == stores.size()-1) {
                    sqlWhereClauses.append(")\n");
                }
                linkVariation = "JOIN";
            }
        }

        if (categories.size() > 0) {
            for (int i=0; i<categories.size(); i++) {
                Category thisCategory = (Category)categories.get(i);
                if (i==0) {
                    sqlWhereClauses.append(whereAnd + " ( \n\t");
                    whereAnd = "AND";
                } else {
                    sqlWhereClauses.append("\tOR ");
                }
                sqlWhereClauses.append("PV.inId IN (SELECT inProductVariationId FROM tbLinkProductVariationCategory WHERE inCategoryId = " + thisCategory.getId() + ") \n");
                if (i == categories.size()-1) {
                    sqlWhereClauses.append(") \n");
                }
                linkVariation = "JOIN";
            }
        }

        if (artists.size() > 0) {
            for (int i=0; i<artists.size(); i++) {
                Artist thisArtist = (Artist)artists.get(i);
                if (i==0) {
                    sqlWhereClauses.append(whereAnd + " ( \n\t");
                    whereAnd = "AND";
                } else {
                    sqlWhereClauses.append("OR \n");
                }
                sqlWhereClauses.append("P.inId IN (SELECT inProductId FROM tbLinkProductArtist WHERE inArtistId = " + thisArtist.getId() + ") \n");
                if (i == artists.size()-1) {
                    sqlWhereClauses.append(") \n");
                }
            }
        }

		StringBuilder sqlStatement = new StringBuilder();
        sqlStatement.append("SELECT P.inId AS P_inId, PV.inId AS PV_inId, B.inId AS B_inId, P.vcSku AS P_vcSku, \n");
        sqlStatement.append("P.vcName, P.vcSku, P.btActiveForRetail, P.btActiveForWholesale, P.btTaxable, P.inBrandId, P.dtCreated, P.dtModified, P.dtInStock, P.txDesc, P.txTextDescription,\n");
        sqlStatement.append("PV.*, \n");
        sqlStatement.append("B.vcName AS B_vcName, B.vcLogo AS B_vcLogo ");
		sqlStatement.append("FROM tbProduct P ");
		sqlStatement.append(linkVariation)
			.append(" tbProductVariation PV ON P.inId = PV.inProductId \n");
		sqlStatement.append(linkBrand)
			.append(" tbBrand B ON P.inBrandId = B.inId \n");
		sqlStatement.append(sqlWhereClauses);
        sqlStatement.append("ORDER BY P.vcName, PV.inRank \n");

        if (log.isDebugEnabled()) {
            log.debug(sqlStatement.toString());
        }
        return sqlStatement.toString();
    }

    public ArrayList executeReturnProducts(Connection con) {
        return executeReturnProducts(con,-1);
    }

    /**
     * Executes the search engine with the parameters that have been set.
     *
     * @return ArrayList of Product objects
     */
    public ArrayList executeReturnProducts(Connection con, int max) {
        // this method needs repairs, the images returned should be configurable
        // it should also return images for a given product variation, not just
        // the image for the product

        ArrayList productList = new ArrayList();
        try {
            PreparedStatement ps = con.prepareStatement(getSql());
            ResultSet rs = ps.executeQuery();
            Product product = new Product();
            int productId;
            while (rs.next()) {
                productId = rs.getInt("P_inId");
                if (product == null || product.getId() != productId) {
                    product = new Product(productId);

                    //product.loadArtistsFromDb(con);
                    //product.loadImagesFromDb(con,1,Image.VERTICAL);

                    productList.add(product);
                }
                ProductVariation thisVariation = new ProductVariation(rs.getInt("PV_inId"));
                Brand thisBrand = new Brand(rs.getInt("B_inId"));
                //product.setSku(rs.getString("P_vcSku"));

                product.loadFromRs(rs);
                thisVariation.loadFromRs(rs);
                product.addVariation(thisVariation);
                product.setBrand(thisBrand);
                thisBrand.setName(rs.getString("B_vcName"));
                thisBrand.setLogo(rs.getString("B_vcLogo"));

                if (max>0 && productList.size()==max) {
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return productList;
    }

    public void setActiveForRetail(boolean in) {
        this.activeForRetail = in;
    }
    public void setActiveForWholesale(boolean in) {
        this.activeForWholesale = in;
    }
    public void setActiveOnly(boolean in) {
        this.activeOnly = in;
    }
    public void setInput(Product in) {
        this.input = in;
    }
    public void setPriceLow(BigDecimal in) {
        priceLow = in;
    }
    public void setPriceHigh(BigDecimal in) {
        priceHigh = in;
    }
    public void setSession(Session in) {
        session = in;
    }
    public void setVariationRequred(boolean in) {
        this.variationRequired = in;
    }

}
