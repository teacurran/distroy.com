package com.approachingpi.store.servlet;

import com.approachingpi.util.MessageBean;
import com.approachingpi.util.Mailer;
import com.approachingpi.servlet.Session;
import com.approachingpi.servlet.PiServlet;
import com.approachingpi.user.User;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * User: tcurran
 * Date: Dec 23, 2004
 * Time: 9:45:51 PM
 */
public class ChangeUserInfoServlet extends PiServlet{
    public static final int ACTION_MAIN     = 0;
    public static final int ACTION_CHANGE   = 1;

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
        User user = session.getUser();
        int action = PiServlet.getReqInt(req, "action", ACTION_MAIN);

        // Check to see if the user is logged in
        if (user.getId()==0 || user.getType() < User.TYPE_PUBLIC) {
            try {
                con.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            errorBean.addMessage(getDefines().getProperty("ERROR_NOT_LOGGED_IN"));
            req.setAttribute("loginReturn","/changeuserinfo?action=" + action);
            req.setAttribute("loginForm","/jsp/login.jsp");
            forwardRequest(req,res,"/login");

            return;
        }

        if (action == ACTION_CHANGE) {
            String email = PiServlet.getReqString(req,"u_email","");
            String password1 = PiServlet.getReqString(req,"u_password1");
            String password2 = PiServlet.getReqString(req,"u_password2");

            if (email.length() == 0) {
                errorBean.addMessage(getDefines().getProperty("message.field.missing").replaceAll("#FIELD#","Email Address"));
                errorBean.addHighlightField("u_email");
            } else if (!Mailer.isValidEmailAddress(user.getEmail())) {
                errorBean.addMessage(getDefines().getProperty("message.field.bademail"));
                errorBean.addHighlightField("u_email");
            }

            if (password1.length() > 0 || password2.length() > 0) {
                if (!PiServlet.getReqString(req,"u_password1").equals(PiServlet.getReqString(req,"u_password2"))) {
                    errorBean.addMessage(getDefines().getProperty("message.field.passnomatch"));
                    errorBean.addHighlightField("u_password1");
                    errorBean.addHighlightField("u_password2");
                }

            }

            if (errorBean.getMessageCount() == 0) {
                // we could possibly check here to make sure that there isn't already a user/password
                // combo for what we are currently setting the value to.  I decided not to do that.
                // we can perhaps link users like this in the future, so their orders and preferences and
                // whatnot could be virtually linked
                user.setEmail(email);
                if (password1.length() > 0) {
                    user.setPassword(password1);
                }

                try {
                    user.saveToDb(con);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                messageBean.addMessage("Account successfully updated.");
                try {
                    con.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                forwardRequest(req,res,"/myaccount");

                return;
            }

            try {
                con.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            req.getRequestDispatcher("/jsp/myaccount/change_user_info.jsp").forward(req, res);
        }
    }
}
