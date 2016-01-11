package com.approachingpi.store.servlet.admin;

import com.approachingpi.servlet.PiServlet;
import com.approachingpi.servlet.Session;
import com.approachingpi.store.catalog.*;
import com.approachingpi.user.User;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.math.BigDecimal;

/**
 * User: Terrence Curran
 * Date: May 5, 2006
 * Time: 11:46:14 AM
 * Desc:
 */
public class PriceServlet extends PiServlet {
    public static final int ACTION_LIST     = 0;
    public static final int ACTION_UPDATE   = 1;
    public static final int ACTION_UPDATE_PRODUCT = 2;

    public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
        doPost(req,res);
    }

    public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
        super.initPiServlet(req, res);
        Connection con = openConnection();

	    Session session = this.getSession(req,res,con);
	    User user = session.getUser();

        int action = PiServlet.getReqInt(req, "action");

        if (action == ACTION_UPDATE) {
            ProductVariation variation = new ProductVariation(PiServlet.getReqInt(req, "productVariationId"));
            try {
                variation.loadFromDb(con);
            } catch (Exception e) {
            }
            if (variation.getId() > 0) {
                if (req.getParameter("active") != null) {
                    variation.setActive(PiServlet.getReqBoolean(req,"active"));
                }
                if (req.getParameter("sale") != null) {
                    variation.setSale(PiServlet.getReqBoolean(req,"sale"));
                }
                if (req.getParameter("priceCost") != null) {
                    variation.setPriceCost(new BigDecimal(PiServlet.getReqString(req,"priceCost")));
                }
                if (req.getParameter("priceWholesale") != null) {
                    variation.setPriceWholesale(new BigDecimal(PiServlet.getReqString(req,"priceWholesale")));
                }
                if (req.getParameter("priceWholesaleSale") != null) {
                    variation.setPriceWholesaleSale(new BigDecimal(PiServlet.getReqString(req,"priceWholesaleSale")));
                }
                if (req.getParameter("priceRetail") != null) {
                    variation.setPriceRetail(new BigDecimal(PiServlet.getReqString(req,"priceRetail")));
                }
                if (req.getParameter("priceRetailSale") != null) {
                    variation.setPriceRetailSale(new BigDecimal(PiServlet.getReqString(req,"priceRetailSale")));
                }
                try {
                    variation.saveToDb(con);
                    this.getAltAttribute(req).put("update_success", true);
                } catch (Exception e) {
                    e.printStackTrace();
                    this.getAltAttribute(req).put("update_success", false);
                }
            }
            req.setAttribute("variation", variation);
        }
        if (action == ACTION_UPDATE_PRODUCT) {
            Product product = new Product(PiServlet.getReqInt(req, "productId"));
            try {
                product.loadFromDb(con);
            } catch (Exception e) {
            }
            if (product.getId() > 0) {
                if (req.getParameter("activeRetail") != null) {
                    product.setActiveForRetail(PiServlet.getReqBoolean(req,"activeRetail"));
                }
                if (req.getParameter("activeWholesale") != null) {
                    product.setActiveForWholesale(PiServlet.getReqBoolean(req,"activeWholesale"));
                }
                try {
                    product.saveToDb(con);
                    this.getAltAttribute(req).put("update_success", true);
                } catch (Exception e) {
                    e.printStackTrace();
                    this.getAltAttribute(req).put("update_success", false);
                }
            }
            req.setAttribute("product", product);
        }

        if (action == ACTION_LIST) {
            ProductServlet.getArtists(req, con);
            ProductServlet.getBrands(req, con);
            ProductServlet.getCategories(req, con);

            // set up the search engine
            SearchEngine se = new SearchEngine();
            se.addCategory(new Category(PiServlet.getReqInt(req,"categoryId",user.getPrefInt("admin.price.filter.categoryId"))));
            se.addArtist(new Artist(PiServlet.getReqInt(req,"artistId",user.getPrefInt("admin.price.filter.artistId"))));
            Product searchProduct = new Product();
            searchProduct.setBrand(new Brand(PiServlet.getReqInt(req,"brandId",user.getPrefInt("admin.price.filter.brandId"))));
            se.setInput(searchProduct);

            user.setPref("admin.price.filter.categoryId", PiServlet.getReqInt(req,"categoryId",user.getPrefInt("admin.price.filter.categoryId")));
            user.setPref("admin.price.filter.artistId", PiServlet.getReqInt(req,"artistId",user.getPrefInt("admin.price.filter.artistId")));
            user.setPref("admin.price.filter.brandId", PiServlet.getReqInt(req,"brandId",user.getPrefInt("admin.price.filter.brandId")));

            try {
                user.savePrefs(con);
            } catch (Exception e) {
                e.printStackTrace();
            }


            ArrayList products = se.executeReturnProducts(con);
            for (int i=0; i<products.size(); i++) {
                try {
                    ((Product)products.get(i)).loadImagesFromDb(con, 1, Image.ANY_ORIENTATION);
                    ((Product)products.get(i)).loadVariationSizes(con);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            req.setAttribute("search", se);
            req.setAttribute("products",products);
        }

        try {
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (action == ACTION_LIST) {
            req.getRequestDispatcher("/jsp/admin/price_list.jsp").forward(req, res);
        } else if (action == ACTION_UPDATE) {
            req.getRequestDispatcher("/jsp/admin/price_update_xml.jsp").forward(req, res);
        } else if (action == ACTION_UPDATE_PRODUCT) {
            req.getRequestDispatcher("/jsp/admin/price_update_product_xml.jsp").forward(req, res);
        }

    }
}

