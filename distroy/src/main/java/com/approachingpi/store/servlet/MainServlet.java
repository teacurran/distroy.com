/* ***************************

User:	terrence
Date:	Mar 22, 2003
Time:	1:50:33 AM
Desc:

*************************** */

package com.approachingpi.store.servlet;

import com.approachingpi.servlet.PiServlet;
import com.approachingpi.store.site.HomepageItem;
import com.approachingpi.servlet.Session;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.StringReader;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.Connection;
import java.net.URLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;

public class MainServlet extends PiServlet {


	public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
		//if (req.getServerName() != null
		//		&& req.getServerName().indexOf("beta") == -1
		//		&& req.getServerName().indexOf("dev") == -1
		//		&& !req.getServerName().equalsIgnoreCase("www.distroy.com")) {
		//	res.sendRedirect("http://www.distroy.com" + req.getRequestURI());
		//	return;
		//}
		doPost(req, res);
	}

	public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
		super.initPiServlet(req, res);

        /*
		Connection con = openConnection();
        Session session = getSession(req,res,con);

        PreparedStatement ps;
        ResultSet rs;

        ArrayList items = HomepageItem.getAllHomepageItems(con);
        req.setAttribute("items", items);


        try {
            con.close();
        } catch (Exception e) { e.printStackTrace(); }
        */

		//res.sendRedirect("/blog");
		req.getRequestDispatcher("/jsp/main.jsp").forward(req, res);
	}


}
