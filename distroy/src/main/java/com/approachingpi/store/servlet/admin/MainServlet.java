/* ***************************

User:	terrence
Date:	Mar 22, 2003
Time:	1:50:33 AM
Desc:

*************************** */

package com.approachingpi.store.servlet.admin;

import com.approachingpi.servlet.PiServlet;
import com.approachingpi.servlet.Session;
import com.approachingpi.user.User;
import com.approachingpi.store.Defines;
import com.approachingpi.util.MessageBean;

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
        doPost(req,res);
    }

    public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
        super.initPiServlet(req, res);

        Connection con = openConnection();
        Session session = this.getSession(req,res,con);
        User user = session.getUser();

        MessageBean errorBean = this.getErrorBean(req);
        if (user.getId()==0 || user.getType() < User.TYPE_ADMIN) {
            try {
                con.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            errorBean.addMessage(Defines.ERROR_NOT_LOGGED_IN);
            req.setAttribute("loginReturn","/admin");
            req.setAttribute("loginForm","/jsp/admin/login.jsp");
            forwardRequest(req,res,"/login");

            return;
        }

        try {
            con.close();
        } catch (Exception e) { e.printStackTrace(); }

        req.getRequestDispatcher("/jsp/admin/main.jsp").forward(req, res);
    }
}
