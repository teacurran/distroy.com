/*
 * © Approaching Pi, Inc.
 * User: terrence
 * Date: Jul 1, 2004
 * Time: 9:31:09 PM
 * Desc: 
 *
 */
package com.approachingpi.store.servlet.admin;

import com.approachingpi.servlet.PiServlet;
import com.approachingpi.store.catalog.Category;
import com.approachingpi.util.MessageBean;
import com.approachingpi.util.NotNullHash;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

public class CategoryServlet extends PiServlet {
    public static final int ACTION_LIST     = 0;
    public static final int ACTION_EDIT     = 1;
    public static final int ACTION_SAVE     = 2;
    public static final int ACTION_DELETE   = 3;

    public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
        doPost(req,res);
    }

    public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
        super.initPiServlet(req, res);

        MessageBean messageBean = super.getMessageBean(req);
        MessageBean errorBean = super.getErrorBean(req);

        Connection con = openConnection();
        PreparedStatement ps;
        ResultSet rs;

        // create a hash that will hold all the form parameters
        HashMap formHash = new NotNullHash();
        // copy the parameters into the form hash
        Enumeration parameterNames = req.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            String thisName = (String)parameterNames.nextElement();
            formHash.put(thisName, req.getParameter(thisName));
        }

        int action = PiServlet.getReqInt(req,"action",ACTION_LIST);


        Category category = new Category();
        category.setId(PiServlet.getReqInt(req,"categoryId"));

        if (action == ACTION_DELETE) {
            boolean deleteConfirm = PiServlet.getReqBoolean(req,"deleteConfirm");
            if (deleteConfirm) {
                try {
                    category.deleteFromDb(con);
                    messageBean.addMessage("Category " + category.getId() + " successfully deleted.");
                    category = new Category();
                } catch (Exception e) {
                    messageBean.addMessage("Delete Failed.");
                }
                action = ACTION_EDIT;
            }
        }

        if (action == ACTION_SAVE) {
            try {
                category.loadFromDb(con);
            } catch (Exception e) {}
            category.setName(PiServlet.getReqString(req,"name"));
            category.setActive(PiServlet.getReqBoolean(req,"active"));
            category.setParent(new Category(PiServlet.getReqInt(req,"parentId")));

            if (category.getName().equals("")) {
                errorBean.addMessage("Name cannot be left blank.");
                errorBean.addHighlightField("name");
            }

            if (errorBean.getMessageCount() == 0) {
                try {
                    category.saveToDb(con);
                    messageBean.addMessage("Category successfully saved.");
                    action = ACTION_EDIT;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        if (action == ACTION_EDIT || action == ACTION_DELETE) {
            try {
                category.loadFromDb(con);
            } catch (Exception e) {}
            formHash.put("name",category.getName());
            formHash.put("parent",new Integer(category.getParent().getId()).toString());
            formHash.put("active",new Boolean(category.getActive()).toString());
        }


        ArrayList categories = new ArrayList();
        try {
            ps = con.prepareStatement("SELECT * FROM tbCategory ORDER BY vcPath");
            rs = ps.executeQuery();
            while (rs.next()) {
                Category newCat = new Category();
                newCat.loadFromRs(rs);
                newCat.loadParentFromDb(con);
                categories.add(newCat);
            }
            rs.close();
        } catch (Exception e) {

        }

        try {
            con.close();
        } catch (Exception e) { e.printStackTrace(); }

        this.getAltAttribute(req).put("action",action);
        req.setAttribute("formHash",formHash);
        req.setAttribute("category",category);
        req.setAttribute("categories",categories);
        req.getRequestDispatcher("/jsp/admin/category.jsp").forward(req, res);
    }

}
