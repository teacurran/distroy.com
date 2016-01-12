/*
 * © Approaching Pi, Inc.
 * User: terrence
 * Date: Jul 1, 2004
 * Time: 3:12:04 PM
 * Desc: Admin servlet for editing all information about a product.
 */
package com.approachingpi.store.servlet.admin;

import com.approachingpi.servlet.PiServlet;
import com.approachingpi.servlet.Session;
import com.approachingpi.store.catalog.*;
import com.approachingpi.store.Defines;
import com.approachingpi.user.User;
import com.approachingpi.util.MessageBean;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

public class ProductServlet extends PiServlet {
    public static final int ACTION_LIST                 = 0;
    public static final int ACTION_EDIT                 = 1;
    public static final int ACTION_EDIT_VARIATION       = 2;
    public static final int ACTION_MOVE_VARIATION_UP    = 3;
    public static final int ACTION_MOVE_VARIATION_DOWN  = 4;

    public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
        doPost(req,res);
    }

    public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
        super.initPiServlet(req, res);


        Connection con = openConnection();

        MessageBean errorBean = this.getErrorBean(req);
        MessageBean messageBean = this.getMessageBean(req);
	    Session session = this.getSession(req,res,con);
	    User user = session.getUser();

	    // Check to see if the user is logged in
        if (user.getId()==0 || user.getType() < User.TYPE_ADMIN) {
            try {
                con.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            errorBean.addMessage(Defines.ERROR_NOT_LOGGED_IN);
            req.setAttribute("loginReturn","/admin/Product");
            req.setAttribute("loginForm","/jsp/admin/login.jsp");
            forwardRequest(req,res,"/login");

            return;
		}

        int action = PiServlet.getReqInt(req, "action", ACTION_LIST);

        if (action == ACTION_LIST) {
            ProductServlet.getArtists(req,con);
            ProductServlet.getBrands(req,con);
            ProductServlet.getCategories(req, con);

            // set up the search engine
            SearchEngine se = new SearchEngine();
            se.addCategory(new Category(PiServlet.getReqInt(req,"categoryId",user.getPrefInt("admin.product.categoryId"))));
            se.addArtist(new Artist(PiServlet.getReqInt(req,"artistId",user.getPrefInt("admin.product.artistId"))));
            Product searchProduct = new Product();
            searchProduct.setBrand(new Brand(PiServlet.getReqInt(req,"brandId",user.getPrefInt("admin.product.brandId"))));
            se.setInput(searchProduct);

            user.setPref("admin.product.categoryId", PiServlet.getReqInt(req,"categoryId",user.getPrefInt("admin.product.categoryId")));
            user.setPref("admin.product.artistId", PiServlet.getReqInt(req,"artistId",user.getPrefInt("admin.product.artistId")));
            user.setPref("admin.product.brandId", PiServlet.getReqInt(req,"brandId",user.getPrefInt("admin.product.brandId")));

            try {
                user.savePrefs(con);
            } catch (Exception e) {
                e.printStackTrace();
            }

            ArrayList products = se.executeReturnProducts(con);
            for (int i=0; i<products.size(); i++) {
                try {
                    ((Product)products.get(i)).loadImagesFromDb(con, 1, Image.ANY_ORIENTATION);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            req.setAttribute("products",products);

            req.setAttribute("search",se);
        }

        Product product = new Product(PiServlet.getReqInt(req,"productId"));
        ProductVariation variation = new ProductVariation(PiServlet.getReqInt(req,"variationId"));
        variation.setProduct(product);

        boolean formSubmitted = PiServlet.getReqBoolean(req,"formSubmitted");

        if (action == ACTION_EDIT_VARIATION) {
            if (formSubmitted) {

                String sizeList = PiServlet.getReqString(req,"sizeList");
                String qtyToAddList = PiServlet.getReqString(req,"qtyToAdd");
                if (sizeList.length() > 0) {
                    String sizeSplit[] = sizeList.split(",");
                    String qtyToAddSplit[] = qtyToAddList.split(",");
                    ArrayList sizesSaved = new ArrayList();
                    for (int i=0; i<sizeSplit.length; i++) {
                        try {
                            Size size = new Size(Integer.parseInt(sizeSplit[i]));
                            sizesSaved.add(size);
                            try {
                                size.setQtyToAdd(Integer.parseInt(qtyToAddSplit[i]));
                            } catch (Exception e) {}
                            variation.addSize(size);
                        } catch (Exception e) {
                        }
                    }
                    /*
                    try {
                        variation.loadStoresFromDb(con);
                        ArrayList sizesInDb = variation.getSizes();
                        for (int i=sizesInDb.size()-1; i>=0; i--) {
                            Size thisSize = (Size)sizesInDb.get(i);
                            boolean foundSize = false;
                            for (int x=sizesSaved.size()-1; x>=0; x--) {
                                Size savedSize = (Size)sizesSaved.get(x);
                                if (savedSize.equals(thisSize)) {
                                    foundSize = true;
                                    break;
                                }
                            }
                            if (!foundSize) {
                                variation.deleteSize(con, thisSize);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    */
                }

                variation.setActive(PiServlet.getReqBoolean(req,"active"));
                variation.setSale(PiServlet.getReqBoolean(req,"sale"));
                variation.setColor(PiServlet.getReqString(req,"color"));
                variation.setDesc(PiServlet.getReqString(req,"desc"));
                variation.setPriceCost(new BigDecimal(PiServlet.getReqString(req,"priceCost")));
                variation.setPriceWholesale(new BigDecimal(PiServlet.getReqString(req,"priceWholesale")));
                variation.setPriceWholesaleSale(new BigDecimal(PiServlet.getReqString(req,"priceWholesaleSale")));
                variation.setPriceRetail(new BigDecimal(PiServlet.getReqString(req,"priceRetail")));
                variation.setPriceRetailSale(new BigDecimal(PiServlet.getReqString(req,"priceRetailSale")));
                variation.setSku(PiServlet.getReqString(req,"sku"));
                variation.setStyle(PiServlet.getReqString(req,"style"));

                int imageId = PiServlet.getReqInt(req,"imageId");
                if (imageId > 0) {
                    variation.addImage(new Image(imageId));
                }

                if (variation.getStyle().equalsIgnoreCase("") && variation.getColor().equalsIgnoreCase("")) {
                    errorBean.addMessage("You must enter a style and/or color");
                }


                if (errorBean.getMessageCount() == 0) {
                    try {
                        variation.saveToDb(con);
                        variation.saveSizesToDb(con);
                        messageBean.addMessage(getDefines().getProperty("message.save.done").replaceAll("#OBJECT#","Variation"));
                    } catch (Exception e) {
                        errorBean.addMessage("error saving product: " + e.toString());
                        e.printStackTrace();
                    }
                }
                try {
                    String catList = PiServlet.getReqString(req,"catList");
                    if (catList.length() > 0) {
                        String catSplit[] = catList.split(",");
                        for (int i=0; i<catSplit.length; i++) {
                            try {
                                Category cat = new Category(Integer.parseInt(catSplit[i]));
                                variation.addCategory(cat);
                            } catch (Exception e) {
                            }
                        }
                    }

                    variation.saveCategoriesToDb(con);
                    if (req.getParameter("button_submit_new") != null) {
                        variation = new ProductVariation();
                    } else if (req.getParameter("button_submit_return") != null) {
                        variation = null;
                        action = ACTION_EDIT;
                        formSubmitted = false;
                    }
                } catch (Exception e) {
                    errorBean.addMessage("error saving product variation: " + e.toString());
                }
            }
        }

        // action may have changed.
        if (action == ACTION_EDIT_VARIATION) {
            // load all the categories
            try {
                ArrayList categories = Category.loadAllCategories(con);
                req.setAttribute("categories",categories);
            } catch (Exception e) { e.printStackTrace(); }

            // load all the sizes
            try {
                ArrayList sizes = Size.loadAllSizes(con);
                req.setAttribute("sizes",sizes);
            } catch (Exception e) { e.printStackTrace(); }

            // load the product
            try {
                product.loadFromDb(con);
                product.loadExtendedFromDb(con);
                req.setAttribute("product",product);
            } catch (Exception e) { e.printStackTrace(); }

            // load the variation if we currently have no errors
            try {
                variation.loadImagesFromDb(con);
                variation.loadCategoriesFromDb(con);
                variation.loadSizes(con);
                if (errorBean.getMessageCount() == 0) {
                    variation.loadFromDb(con);
                }
            } catch (Exception e) { e.printStackTrace(); }
            req.setAttribute("variation",variation);
        }

        if (action == ACTION_MOVE_VARIATION_UP) {
            try {
                variation.loadFromDb(con);
                variation.setRank(variation.getRank()-3);
                variation.saveToDb(con);
                product.sortVariationsByRank(con);
            } catch (Exception e) {
                e.printStackTrace();
            }
            action = ACTION_EDIT;
        }

        if (action == ACTION_MOVE_VARIATION_DOWN) {
            try {
                variation.loadFromDb(con);
                variation.setRank(variation.getRank()+3);
                variation.saveToDb(con);
                product.sortVariationsByRank(con);
            } catch (Exception e) {
                e.printStackTrace();
            }
            action = ACTION_EDIT;
        }

        if (action == ACTION_EDIT) {
            ProductServlet.getArtists(req,con);
            ProductServlet.getBrands(req,con);

            if (formSubmitted) {
                product.setName(PiServlet.getReqString(req,"name"));
                product.setSku(PiServlet.getReqString(req,"sku"));
                product.setBrand(new Brand(PiServlet.getReqInt(req,"brandId")));
                product.setActiveForRetail(PiServlet.getReqBoolean(req,"activeForRetail"));
                product.setActiveForWholesale(PiServlet.getReqBoolean(req,"activeForWholesale"));
                product.setDesc(PiServlet.getReqString(req,"desc"));
                product.setTextDescription(PiServlet.getReqString(req,"textDescription"));
                product.addArtist(new Artist(PiServlet.getReqInt(req,"artistId")));

                if (product.getName().equalsIgnoreCase("")) {
                    errorBean.addMessage("Name cannot be left blank.");
                    errorBean.addHighlightField("name");
                }
                if (product.getBrand().getId() == 0) {
                    errorBean.addMessage("You must choose a brand.");
                    errorBean.addHighlightField("brandId");
                }

                if (errorBean.getMessageCount() == 0) {
                    try {
                        product.saveToDb(con);
                        product.saveArtistsToDb(con);
                        // reload the product so we can get the created and modified times.
                        product.loadFromDb(con);
                    } catch (Exception e) {
                        e.printStackTrace();
                        errorBean.addMessage("Error saving Product: " + e.toString());
                    }
                    messageBean.addMessage("Product successfully saved.");
                }
            }

            if (errorBean.getMessageCount() == 0) {
                try {
                    product.loadFromDb(con);
                    product.loadArtistsFromDb(con);
                    product.loadVariations(con);
                    product.loadVariationImages(con,1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            req.setAttribute("product",product);
        }


        try {
            con.close();
        } catch (Exception e) { e.printStackTrace(); }

        if (action == ACTION_LIST) {
            req.getRequestDispatcher("/jsp/admin/product_list.jsp").forward(req, res);
        } else if (action == ACTION_EDIT) {
            req.getRequestDispatcher("/jsp/admin/product_edit.jsp").forward(req, res);
        } else if (action == ACTION_EDIT_VARIATION) {
            req.getRequestDispatcher("/jsp/admin/product_variation_edit.jsp").forward(req, res);
        }
    }

    protected static void getArtists(HttpServletRequest req, Connection con) {
        ArrayList artists = new ArrayList();
        try {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM tbArtist ORDER BY vcNameLast, vcNameFirst");
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
        req.setAttribute("artists",artists);
    }


    protected static void getBrands(HttpServletRequest req, Connection con) {
        ArrayList brands = new ArrayList();
        try {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM tbBrand ORDER BY vcName");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Brand newBrand = new Brand(rs.getInt("inId"));
                newBrand.loadFromRs(rs);
                brands.add(newBrand);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        req.setAttribute("brands",brands);
    }

    protected static void getCategories(HttpServletRequest req, Connection con) {
        ArrayList categories = new ArrayList();
        try {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM tbCategory ORDER BY vcPath");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Category newCat = new Category(rs.getInt("inId"));
                newCat.loadFromRs(rs);
                newCat.loadParentFromDb(con);
                categories.add(newCat);
            }
            rs.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        req.setAttribute("categories",categories);
    }

}

