/*
 * ContentServlet.java
 *
 * Created on July 31, 2004, 1:58 PM
 *
 * @author  Terrence Curran
 */

package com.approachingpi.store.servlet;

import com.approachingpi.servlet.PiServlet;
import com.approachingpi.servlet.Session;
import com.approachingpi.store.catalog.*;
import com.approachingpi.util.MessageBean;
import com.approachingpi.store.site.Content;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.Connection;
import java.util.ArrayList;
import java.sql.SQLException;
import java.io.PrintWriter;

public class ContentServlet extends PiServlet {

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

        Content content = new Content();
	    content.setAccessRequired(session.getUser().getType());
        content.setUrl(pathInfo);
        try {
            content.loadFromDbByUrl(con);
        } catch (Exception e) {
            //errorBean.addMessage(pathInfo + " was not found.  Please check the url and try again.");
            //errorBean.setIsFatal(true);
            e.printStackTrace();
        }

        if (content.getId() == 0) {
            content.setTitle("404 - File Not Found");
            content.setBodyHtml("Error: The requested resource \"" + pathInfo + "\" was not found.  Please check the url and try again.");
        }

        try {
            res.setDateHeader("Last-Modified", content.getDateModified().getTime());
        } catch (Exception e) {
        }

        if (content.getId() > 0 && PiServlet.getReqBoolean(req,"text")) {
            try {
                con.close();
            } catch (Exception e) { e.printStackTrace(); }

            res.setContentType("text/plain");
            PrintWriter out = res.getWriter();
            out.print(content.getBodyText());
            return;
        }

        req.setAttribute("content", content);

        try {
            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        req.getRequestDispatcher("/jsp/content.jsp").forward(req, res);
    }
}
