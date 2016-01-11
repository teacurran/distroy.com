/*
 * Created by IntelliJ IDEA.
 * User: terrence
 * Date: Jul 13, 2004
 * Time: 7:03:55 PM
 * To change template for new class use 
 * Code Style | Class Templates options (Tools | IDE Options).
 */
package com.approachingpi.store.servlet.admin;

import com.approachingpi.servlet.PiServlet;
import com.approachingpi.util.MessageBean;
import com.approachingpi.store.catalog.Product;
import com.approachingpi.store.catalog.Image;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.Connection;
import java.sql.ResultSet;

public class ImageEditServlet extends PiServlet {
    public static final int ACTION_MAIN         = 0;
    public static final int ACTION_EDIT         = 1;
    public static final int ACTION_SAVE         = 2;
    public static final int ACTION_DELETE       = 3;
    public static final int ACTION_MOVE_UP      = 4;
    public static final int ACTION_MOVE_DOWN    = 5;

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

        int action = PiServlet.getReqInt(req,"action");

        Image image = new Image(PiServlet.getReqInt(req,"imageId"));
        Product product = new Product(PiServlet.getReqInt(req,"productId"));

        if (action == ACTION_DELETE) {
            try {
                image.deleteFromDb(con);
                product.sortImagesByRank(con);
            } catch (Exception e) {
                e.printStackTrace();
                errorBean.addMessage("Error deleting image: " + e.toString());
            }
            action = ACTION_MAIN;
        }

        if (action == ACTION_MOVE_DOWN) {
            if (image.getId() > 0 && product.getId() > 0) {
                try {
                    image.loadFromDb(con);
                    image.setRank(image.getRank() + 3);
                    image.saveToDb(con);
                    product.sortImagesByRank(con);
                } catch (Exception e) { e.printStackTrace(); }
            }
            action = ACTION_MAIN;
        }

        if (action == ACTION_MOVE_UP) {
            if (image.getId() > 0 && product.getId() > 0) {
                try {
                    image.loadFromDb(con);
                    image.setRank(image.getRank() - 3);
                    image.saveToDb(con);
                    product.sortImagesByRank(con);
                } catch (Exception e) { e.printStackTrace(); }
            }
            action = ACTION_MAIN;
        }

        if (action == ACTION_MAIN) {
            try {
                product.loadImagesFromDb(con);
            } catch (Exception e) { e.printStackTrace(); }

            req.setAttribute("images",product.getImages());
        }

        if (action == ACTION_EDIT) {
            try {
                image.loadFromDb(con);
            } catch (Exception e) { e.printStackTrace(); }

            if (image.getId() == 0) {
                errorBean.addMessage("Image id not found.");
            }

            if (PiServlet.getReqBoolean(req,"formSubmitted")) {
                image.setDesc(PiServlet.getReqString(req,"desc"));

                try {
                    image.saveToDb(con);
                } catch (Exception e) { e.printStackTrace(); }

            }
            req.setAttribute("image",image);
        }

        try {
            con.close();
        } catch (Exception e) { e.printStackTrace(); }

        if (action == ACTION_MAIN) {
            req.getRequestDispatcher("/jsp/admin/image_main.jsp").forward(req, res);
        } else if (action == ACTION_EDIT) {
            req.getRequestDispatcher("/jsp/admin/image_edit.jsp").forward(req, res);
        } else if (action == ACTION_SAVE) {
            req.getRequestDispatcher("/jsp/admin/image_save.jsp").forward(req, res);
        }
    }

}
