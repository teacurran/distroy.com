/*
 * Created by IntelliJ IDEA.
 * User: terrence
 * Date: Jul 18, 2004
 * Time: 9:59:15 PM
 * To change template for new class use
 * Code Style | Class Templates options (Tools | IDE Options).
 */
package com.approachingpi.store.servlet;

import com.approachingpi.servlet.PiServlet;
import com.approachingpi.servlet.Session;
import com.approachingpi.store.catalog.*;
import com.approachingpi.util.MessageBean;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.math.BigDecimal;

public class BrandServlet extends PiServlet {
    public static final int ACTION_CATEGORY     = 0;
    public static final int ACTION_BRAND        = 1;
    public static final int ACTION_ARTIST       = 2;
    public static final int ACTION_BRAND_LIST   = 3;
    public static final int ACTION_ALL          = 4;
    public static final int ACTION_SEARCH       = 5;

    public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
        doPost(req,res);
    }

    public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
        super.initPiServlet(req, res);

        Connection con = openConnection();
        PreparedStatement ps;
        ResultSet rs;

        MessageBean errorBean = this.getErrorBean(req);
        MessageBean messageBean = this.getMessageBean(req);

	    Session session = this.getSession(req,res,con);

        String pathInfo = (req.getPathInfo()==null) ? "" : req.getPathInfo();
        //System.out.println("Brand:" + pathInfo);
        String[] pathSplit = pathInfo.split("/");

        Brand brand = new Brand();
        Artist artist = new Artist();
        Category category = new Category();
        ProductVariation variation = new ProductVariation();
        BigDecimal priceLow = null;
        BigDecimal priceHigh = null;

        // fix this, detect what servlet is being called

        // default to category because it is the easiest to recover from
        int action = ACTION_CATEGORY;
        String reqUrl = req.getRequestURL().toString();

        if (reqUrl.indexOf("/brands") > -1) {
            action = ACTION_BRAND;
        } else if (reqUrl.indexOf("/artists") > -1) {
            action = ACTION_ARTIST;
        } else if (reqUrl.indexOf("/categories") > -1) {
            action = ACTION_CATEGORY;
        } else if (reqUrl.indexOf("/allproducts") > -1) {
            action = ACTION_ALL;
        } else if (reqUrl.indexOf("/search") > -1) {
            action = ACTION_SEARCH;
        }

        if (action == ACTION_BRAND) {
            if (pathSplit.length < 2) {
                //action = ACTION_BRAND_LIST;
                //ArrayList brands = Brand.getAllBrands(con);
                //req.setAttribute("brands",brands);
            } else {
                try {
                    brand.setId(Integer.parseInt(pathSplit[1]));
                    brand.loadFromDb(con);
                } catch (Exception e) {
                    brand.setId(0);
                    try {
                        brand.setName(pathSplit[1]);
                        brand.loadFromDbByName(con);
                    } catch (Exception e2) {
                        errorBean.addMessage("Unable to find brand.");
                        errorBean.setIsFatal(true);
                    }
                }
            }
            // If we still don't have a brand loaded, get the first brand for this store.
            if (brand.getId() == 0) {
                ArrayList allBrandsTemp = Brand.getAllBrands(con,true,1);
                if (allBrandsTemp.size() > 0) {
                    brand = (Brand)allBrandsTemp.get(0);
                }
            }
        } else if (action == ACTION_ARTIST) {
            try {
                artist.setId(Integer.parseInt(pathSplit[1]));
                artist.loadFromDb(con);
            } catch (NumberFormatException nfe) {
                artist.setId(0);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (artist.getId() == 0) {
                try {
                    artist.setNameDisplay(pathSplit[1].replaceAll("_", " "));
                    artist.loadFromDbByNameDisplay(con);
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
            if (artist.getId() == 0) {
                errorBean.addMessage("Unable to find artist.");
                errorBean.setIsFatal(true);
            }
        } else if (action == ACTION_CATEGORY) {
            try {
                category.setId(Integer.parseInt(pathSplit[1]));
                category.loadFromDb(con);
                category.loadParentFromDb(con);
                category.loadChildrenFromDb(con);
                ArrayList categoryChildren = category.getChildren();
                for (int i=0; i<categoryChildren.size(); i++) {
                    ((Category)categoryChildren.get(i)).loadProductsFromDb(con,1);
                    ((Category)categoryChildren.get(i)).getFirstProduct().loadImagesFromDb(con,1,Image.VERTICAL);
                }

            } catch (Exception e) {
            }
        } else if (action == ACTION_SEARCH) {
            try {
                priceLow = new BigDecimal(PiServlet.getReqString(req, "low"));
            } catch (Exception e) {
            }
            try {
                priceHigh = new BigDecimal(PiServlet.getReqString(req, "high"));
            } catch (Exception e) {
            }
        }


        // set up the search engine
        SearchEngine se = new SearchEngine();
        if (category.getId() > 0) {
            se.addCategory(category);
        }
        if (artist.getId() > 0) {
            se.addArtist(artist);
        }
        Product searchProduct = new Product();
        if (brand.getId() > 0) {
            searchProduct.setBrand(brand);
        }
        se.setInput(searchProduct);
        se.setVariationRequred(true);
        se.setActiveForRetail(true);
        se.setActiveOnly(true);
        se.setPriceLow(priceLow);
        se.setPriceHigh(priceHigh);
        se.setSession(session);

        ArrayList productList = se.executeReturnProducts(con);
        for (int i=0; i<productList.size(); i++) {
            try {
                Product thisProduct = (Product)productList.get(i);
                thisProduct.loadVariationImages(con,1,Image.SQUARE);
                // if there are no images for this variation, load the first image for the product
                if (thisProduct.getFirstVariation().getFirstImage().getId()==0) {
                    thisProduct.loadImagesFromDb(con, 1, Image.SQUARE);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        /// * BRANDS   CATEGORIES
        try {
            variation.setId(Integer.parseInt(pathSplit[2]));
            variation.loadFromDb(con);
            variation.loadImagesFromDb(con,1,Image.SQUARE);

            variation.getProduct().loadFromDb(con);
            variation.getProduct().loadVariations(con,true);
            variation.getProduct().loadArtistsFromDb(con);
            variation.getProduct().loadImagesFromDb(con,-1,Image.SQUARE);
            variation.getProduct().loadVariationImages(con,1,Image.SQUARE);

        } catch (Exception e) {
            // don't print anything, this error is usual and expected
            // the url may not have a product variation
            //e.printStackTrace();
        }

        if (variation.getId() == 0) {
            /*
            if (productList.size() > 0) {
                try {
                    Product tempProduct = (Product)productList.get(0);
                    variation = new ProductVariation((tempProduct.getFirstVariation()).getId());
                    variation.loadFromDb(con);
                    variation.loadImagesFromDb(con,1,Image.SQUARE);

                    // we need to load this product again because the instance in the search rsult has a differnt size image
                    variation.getProduct().loadFromDb(con);
                    variation.getProduct().loadArtistsFromDb(con);
                    variation.getProduct().loadVariations(con,true);
                    variation.getProduct().loadImagesFromDb(con,-1,Image.SQUARE);
                    // this is needed for rollovers on the page
                    variation.getProduct().loadVariationImages(con,1,Image.SQUARE);
                } catch (Exception e2) {

                }
            }
            */
        }

        req.setAttribute("se", se);
        req.setAttribute("brand", brand);
        req.setAttribute("artist", artist);
        req.setAttribute("category", category);
        req.setAttribute("variation",variation);
        req.setAttribute("product",variation.getProduct());
        req.setAttribute("productList",productList);

        this.getAltAttribute(req).put("action",action);

        try {
            con.close();
        } catch (Exception e) { e.printStackTrace(); }

        req.getRequestDispatcher("/jsp/brand.jsp").forward(req, res);

        /*
        PrintWriter out = res.getWriter();

        for (int i=0; i<pathSplit.length; i++) {
            out.println(i + ":" + pathSplit[i]);
        }
        */

    }

}
