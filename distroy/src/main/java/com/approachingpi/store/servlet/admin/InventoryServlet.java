package com.approachingpi.store.servlet.admin;

import com.approachingpi.servlet.PiServlet;
import com.approachingpi.servlet.Session;
import com.approachingpi.store.catalog.*;
import com.approachingpi.util.MessageBean;
import com.approachingpi.user.User;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;

/**
 * User: Terrence Curran
 * Date: May 5, 2006
 * Time: 11:46:14 AM
 * Desc:
 */
public class InventoryServlet extends PiServlet {
    public static final int ACTION_LIST     = 0;
    public static final int ACTION_UPDATE   = 1;
    public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
        doPost(req,res);
    }

    public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
        super.initPiServlet(req, res);
        Connection con = openConnection();

	    Session session = this.getSession(req,res,con);
	    User user = session.getUser();

        int action = PiServlet.getReqInt(req, "action", ACTION_LIST);

        if (action == ACTION_UPDATE) {
            ProductVariation var = new ProductVariation(PiServlet.getReqInt(req, "productVariationId", 0));
            Size size = new Size(PiServlet.getReqInt(req, "size"));
            int sizeValue = PiServlet.getReqInt(req, "sizeValue", 0);

            try {
                var.loadFromDb(con);
                size.loadFromDb(con);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (var.getId() > 0 && size.getId() > 0) {

                try {
                    PreparedStatement ps = con.prepareStatement(""+
                        "IF ((SELECT Count(*) FROM tbLinkProductVariationSize WHERE inProductVariationId = ? AND inSizeId = ?) > 0) "+
                        "BEGIN "+
                            "UPDATE tbLinkProductVariationSize SET inQtyInStock=? "+
                            "WHERE inProductVariationId = ? AND inSizeId = ? "+
                        "END ELSE BEGIN "+
                            "INSERT INTO tbLinkProductVariationSize ("+
                                "inProductVariationId,"+
                                "inSizeId,"+
                                "inQtyInStock,"+
                                "inRank "+
                            ") "+
                            "SELECT ?, ?, ?, Count(*) * 2 FROM tbLinkProductVariationSize "+
                            "WHERE inProductVariationId=? "+
                        "END");
                    int i=0;
                    // IF
                    ps.setInt(++i,var.getId());
                    ps.setInt(++i,size.getId());

                    // update
                    ps.setInt(++i,sizeValue);
                    ps.setInt(++i,var.getId());
                    ps.setInt(++i,size.getId());

                    // insert
                    ps.setInt(++i,var.getId());
                    ps.setInt(++i,size.getId());
                    ps.setInt(++i,size.getQtyToAdd());
                    ps.setInt(++i,var.getId());

                    ps.execute();
                    this.getAltAttribute(req).put("update_success", true);
                } catch (Exception e) {
                    e.printStackTrace();
                    this.getAltAttribute(req).put("update_success", false);
                }
            }
        }

        if (action == ACTION_LIST) {
            ProductServlet.getArtists(req, con);
            ProductServlet.getBrands(req, con);
            ProductServlet.getCategories(req, con);


            // set up the search engine
            SearchEngine se = new SearchEngine();
            se.addCategory(new Category(PiServlet.getReqInt(req,"categoryId",user.getPrefInt("admin.product.categoryId"))));
            se.addArtist(new Artist(PiServlet.getReqInt(req,"artistId",user.getPrefInt("admin.product.artistId"))));
            Product searchProduct = new Product();
            searchProduct.setBrand(new Brand(PiServlet.getReqInt(req,"brandId",user.getPrefInt("admin.product.brandId"))));
            se.setInput(searchProduct);

            ArrayList products = se.executeReturnProducts(con);
            for (int i=0; i<products.size(); i++) {
                try {
                    ((Product)products.get(i)).loadImagesFromDb(con, 1, Image.ANY_ORIENTATION);
                    ((Product)products.get(i)).loadVariationSizes(con);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            req.setAttribute("products",products);
        }

        try {
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }


        if (action == ACTION_LIST) {
            req.getRequestDispatcher("/jsp/admin/inventory_list.jsp").forward(req, res);
        } else if (action == ACTION_UPDATE) {
            req.getRequestDispatcher("/jsp/admin/inventory_update_xml.jsp").forward(req, res);
        }
    }
}
