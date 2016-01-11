/*
 * User: terrence
 * Date: Jul 23, 2004
 * Time: 5:36:42 PM
 */
package com.approachingpi.store.servlet;

import com.approachingpi.servlet.PiServlet;
import com.approachingpi.servlet.Session;
import com.approachingpi.user.User;
import com.approachingpi.store.Store;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.sql.Connection;

public class WholesaleServlet extends PiServlet {


    public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
 	    if (req.getServerName() != null && req.getServerName().indexOf("beta") == -1 && req.getServerName().indexOf("dev") == -1 && !req.getServerName().equalsIgnoreCase("www.distroy.com")) {
		    res.sendRedirect("http://www.distroy.com" + req.getRequestURI());
		    return;
	    }

        doPost(req,res);
    }


    public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
        super.initPiServlet(req, res);
	    Connection con = openConnection();
        req.setAttribute(PiServlet.ATTRIBUTE_STORE, "DWH");
        Session session = this.getSession(req,res,con);
	    User user = session.getUser();

        String pathInfo = (req.getPathInfo()==null) ? "" : req.getPathInfo();

        //System.out.println("Wholesale:" + pathInfo);

        if (pathInfo.equals("")) {
            pathInfo = "/";
        }

	    if (user.getType()<User.TYPE_WHOLESALE &&
            pathInfo.indexOf("/content/support/about") != 0 &&
            pathInfo.indexOf("/content/support/contact") != 0 &&
			pathInfo.indexOf("/content/application/thanks") != 0 &&
            pathInfo.indexOf("/application") != 0 &&
            pathInfo.indexOf("/login") != 0) {
		    pathInfo = "/login";
            req.setAttribute("loginForm","/jsp/login_wholesale.jsp");
	    }

	    try {
		    con.close();
	    } catch (Exception e) {
		    e.printStackTrace();
	    }

        req.getRequestDispatcher(pathInfo).forward(req, res);
    }
}
