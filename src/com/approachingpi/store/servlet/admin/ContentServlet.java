/*
 * ContentServlet.java
 *
 * Created on July 31, 2004, 4:37 PM
 *
 * @author  Terrence Curran
 *
 */

package com.approachingpi.store.servlet.admin;

import com.approachingpi.servlet.PiServlet;
import com.approachingpi.store.catalog.Category;
import com.approachingpi.util.MessageBean;
import com.approachingpi.util.NotNullHash;
import com.approachingpi.store.site.Content;
import com.approachingpi.user.User;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

public class ContentServlet extends PiServlet {
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

		int action = PiServlet.getReqInt(req,"action");

        Content content = new Content(PiServlet.getReqInt(req,"contentId"));
	    content.setAccessRequired(User.TYPE_ADMIN);

        if (action == ACTION_EDIT && PiServlet.getReqBoolean(req,"formSubmitted")) {
            content.setAccessRequired(PiServlet.getReqInt(req,"accessRequired"));
            content.setBodyHtml(PiServlet.getReqString(req,"bodyHtml"));
            content.setBodyText(PiServlet.getReqString(req,"bodyText"));
            content.setTitle(PiServlet.getReqString(req,"title"));
            content.setUrl(PiServlet.getReqString(req,"url"));

            try {
                content.saveToDb(con);
            } catch (Exception e) {
                errorBean.addMessage("Error saving content:" + e.toString());
                e.printStackTrace();
            }
        }

        if (errorBean.getMessageCount() == 0 && action == ACTION_EDIT) {
            try {
                content.loadFromDb(con);
            } catch (Exception e) {
                errorBean.addMessage("Error loading content:" + e.toString());
                e.printStackTrace();
            }
        }
        req.setAttribute("content", content);

        if (action == ACTION_LIST) {
            ArrayList allContent = Content.getAllContent(con);
            req.setAttribute("allContent", allContent);
        }

        try {
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (action == ACTION_LIST) {
            req.getRequestDispatcher("/jsp/admin/content_list.jsp").forward(req, res);
        } else if (action == ACTION_EDIT) {
            req.getRequestDispatcher("/jsp/admin/content_edit.jsp").forward(req, res);
        }
    }
}
