/*
 * Approaching Pi, Inc.
 * User: Tea Curran
 * Date: Jun 23, 2002
 * Time: 4:27:55 PM
 */
package com.approachingpi.store.servlet;

import com.approachingpi.user.User;
import com.approachingpi.util.MessageBean;
import com.approachingpi.servlet.PiServlet;
import com.approachingpi.servlet.Session;
import com.approachingpi.store.Defines;

import java.sql.*;
import java.io.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Connection;
import java.util.Calendar;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import javax.servlet.*;
import javax.servlet.http.*;

public class LoginServlet extends PiServlet {
    public static int ACTION_MAIN   = 0;
    public static int ACTION_LOGIN  = 1;
    public static int ACTION_LOGOUT = 2;
    public static int ACTION_CHANGEPASS = 3;

    public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
        doPost(req,res);
    }

    public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
        super.initPiServlet(req, res);
        int action = ACTION_MAIN;
        Connection con = openConnection();
        MessageBean errorBean = getErrorBean(req);
        MessageBean messageBean = getMessageBean(req);
        PreparedStatement ps;
        ResultSet rs;

        boolean loginSuccess = false;
        boolean passwordExpired = false;
        String loginForm = "";

        Session session = getSession(req,res,con);
	    User user = session.getUser();

        // See if we can get the location we are supposed to return to when login is successfull
        String loginReturn = "./";
        if (req.getParameter("loginReturn") != null && !req.getParameter("loginReturn").trim().equals("")) {
            // if a redirect parameter was passed in, have us return there.
            loginReturn = req.getParameter("loginReturn").trim();
        } else if (req.getSession().getAttribute("loginReturn") != null) {
            loginReturn = (String)req.getSession().getAttribute("loginReturn");
        } else {
            /*
            // if we didn't come from a redirect, try to detect the referer for us to return to.
            if (req.getHeader("REFERER") != null && !req.getHeader("REFERER").trim().equals("")) {
                loginReturn = req.getHeader("Referer").trim();

                int inStartTrim = 0;

                // search for the // after the protocol.
                inStartTrim = loginReturn.indexOf("//");

                // if we have a double slash, begin to chop off the begining of the string
                if (inStartTrim > 0) {
                    // we don't even want to bother with this referer if it wasnt from the server were currently on.
                    if (loginReturn.indexOf("://" + req.getHeader("SERVER_NAME")) == 0) {
                        loginReturn = "";
                    } else {
                        inStartTrim = inStartTrim + 2;
                        // cut off everything up until and including the // (http://)
                        loginReturn = loginReturn.substring(inStartTrim,loginReturn.length());

                        // search for the first slash after the protocol, this may not exist
                        inStartTrim = loginReturn.indexOf("/");
                        if (inStartTrim > 0) {
                            // trim off everything up until the first slash.
                            loginReturn = loginReturn.substring(inStartTrim,loginReturn.length());
                        }
                    }
                }
            }
            */
        }
        if (loginReturn.length() > 0) {
            //session.

            //req.getSession().putValue("loginReturn",loginReturn);
        }

        // see if we can get the name of our login form
	    loginForm = PiServlet.getReqString(req,"loginForm");
        if (!loginForm.equals("")) {
	        req.setAttribute("loginForm", loginForm);
        } else {
            loginForm = (String)req.getAttribute("loginForm");
        }
        if (loginForm == null || loginForm.equals("")) {
            loginForm = "/jsp/login.jsp";
        }

	    action = PiServlet.getReqInt(req,"action",ACTION_MAIN);

        if (action == ACTION_CHANGEPASS && user.getId() == 0) {
            action = ACTION_MAIN;
        }

        if (action == ACTION_LOGIN) {
            user.setEmail(req.getParameter("email"));
            user.setPassword(req.getParameter("password"));

            if (user.getUsername().length() == 0 && user.getPassword().length() == 0) {
                errorBean.addMessage(getDefines().getProperty("message.login.missinguserandpass"));
            } else {
                if (user.getUsername().length() == 0) {
                    errorBean.addMessage(getDefines().getProperty("message.login.missinguser"));
                }
                if (user.getPassword().length() == 0) {
                    errorBean.addMessage(getDefines().getProperty("message.login.missingpassword"));
                }
            }

            if (errorBean.getMessageCount() == 0) {
                try {
                    if (!user.loadFromDbByLoggingIn(con)) {
	                    errorBean.addMessage(getDefines().getProperty("message.login.noaccount"));
                    } else {
                        // login successfull, forward them to the page they need to go to
                        System.out.println("Login successful for '" + user.getUsername() + "'");
	                    session.saveToDb(con);
                        loginSuccess = true;
                    }
                    if (user.getId() > 0) {
                        // does this user need to reset their password
                        if (!user.getPasswordNoExpire() && user.getPasswordExpires() != null) {
                            Calendar calNow = Calendar.getInstance();
                            // zero out the time because password expiration works on days only
                            calNow.set(Calendar.HOUR,0);
                            calNow.set(Calendar.MINUTE,0);
                            calNow.set(Calendar.SECOND,0);
                            calNow.set(Calendar.MILLISECOND,0);
                            Calendar calExpires = Calendar.getInstance();
                            calExpires.setTime(user.getPasswordExpires());

                            if (calNow.getTime().after(calExpires.getTime())) {
                                passwordExpired = true;
                                messageBean.addMessage("Please reset your password.");
                            }
                        }
                    }
                } catch (Exception e) {
                    // we are sometimes getting an error here that says that the object has been closed.s
                    // I was missing an rs.close() above, it may have fixed it.
                    errorBean.addMessage("Error Logging In, Please Contact the System Administrator.");
                    System.err.println("Error logging user in: " + e.toString());
                    e.printStackTrace();
                }
            }
        }

        if (action == ACTION_CHANGEPASS) {
            passwordExpired = true;

            if (req.getParameter("formSubmitted") != null) {
                String oldPassword = (req.getParameter("oldpassword") != null) ? req.getParameter("oldpassword") : "";
                String newPassword1 = (req.getParameter("newpassword1") != null) ? req.getParameter("newpassword1") : "";
                String newPassword2 = (req.getParameter("newpassword2") != null) ? req.getParameter("newpassword2") : "";

                try {
                    ps = con.prepareStatement("SELECT * from tbUser WHERE inId = ? AND vcPassword = ?");
                    ps.setInt(1,user.getId());
                    ps.setString(2,oldPassword);
                    rs = ps.executeQuery();
                    if (!rs.next()) {
                        errorBean.addMessage("The original password you entered was not correct.");
                    }
                    rs.close();
                } catch (Exception e) {e.printStackTrace();}

                if (errorBean.getMessageCount() == 0 && !newPassword1.equals(newPassword2)) {
                    errorBean.addMessage("The two passwords you entered do not match.");
                }

                if (errorBean.getMessageCount() == 0 && newPassword1.equals(oldPassword)) {
                    errorBean.addMessage("Your new password cannot be the same as your old password.");
                }

                if (errorBean.getMessageCount() == 0 && newPassword1.length() < 6) {
                    errorBean.addMessage("Your new password must be at least 6 characters.");
                }

                if (errorBean.getMessageCount() == 0) {
                    Pattern pattern = Pattern.compile("[ \\n]");
                    Matcher matcher = pattern.matcher(newPassword1);
                    if (matcher.find()) {
                        errorBean.addMessage("Your new password cannot contain any spaces or line returns.");
                    }
                }

                if (errorBean.getMessageCount() == 0) {
                    int charTypeCount = 0;
                    //if (newPassword1.

                    Pattern pattern = Pattern.compile("[a-z]");
                    Matcher matcher = pattern.matcher(newPassword1);
                    if (matcher.find()) {
                        charTypeCount++;
                    }

                    pattern = Pattern.compile("[A-Z]");
                    matcher = pattern.matcher(newPassword1);
                    if (matcher.find()) {
                        charTypeCount++;
                    }

                    pattern = Pattern.compile("[0-9]");
                    matcher = pattern.matcher(newPassword1);
                    if (matcher.find()) {
                        charTypeCount++;
                    }

                    pattern = Pattern.compile("\\p{Punct}");
                    matcher = pattern.matcher(newPassword1);
                    if (matcher.find()) {
                        charTypeCount++;
                    }

                    if (charTypeCount < 2) {
                        errorBean.addMessage(""+
                        "Your new password must contain two or more of the following types of characters:<br>" +
                        "upper case letters<br>" +
                        "lower case letters<br>" +
                        "numbers<br>" +
                        "special characters {}[]~!@#$%^&*()_-+=:;,.<>?/");
                    }
                }

                if (errorBean.getMessageCount() == 0) {
                    try {
                        Calendar newPasswordExpires = Calendar.getInstance();

                        ps = con.prepareStatement("SELECT * from tbSystemPref WHERE vcKey = 'user.passwordExpire'");
                        rs = ps.executeQuery();
                        if (rs.next()) {
                            try {
                                newPasswordExpires.add(Calendar.DATE, Integer.parseInt(rs.getString("vc_Value")));
                            } catch (Exception e) {
                                newPasswordExpires.add(Calendar.DATE, Defines.DEFAULT_PASSWORD_EXPIRE);
                            }
                        } else {
                            newPasswordExpires.add(Calendar.DATE, Defines.DEFAULT_PASSWORD_EXPIRE);
                        }
                        rs.close();

                        ps = con.prepareStatement("UPDATE tbUser SET vcPassword = ?, dtPasswordExpires = ?, dtPasswordSet = CURRENT_TIMESTAMP, dtModified = CURRENT_TIMESTAMP WHERE inId = ?");
                        ps.setString(1,newPassword1);
                        ps.setDate(2,new java.sql.Date(newPasswordExpires.getTimeInMillis()));
                        ps.setInt(3,user.getId());
                        ps.execute();

                        passwordExpired = false;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            loginSuccess = true;
        }

        if (action == ACTION_LOGOUT) {
	        try {
                user=null;
				session.setUser(null);
				session.saveToDb(con);
	        } catch (Exception e) {
		        e.printStackTrace();
	        }
            try {
                con.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            getServletContext().getRequestDispatcher("/jsp/login_wholesale.jsp").forward(req,res);
            return;
        }

        try {
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        req.setAttribute ("messageBean", messageBean);
        req.setAttribute ("errorBean", errorBean);
	    this.getAltAttribute(req).put("actionInt", new Integer(action));

        if (loginSuccess) {
            if (passwordExpired) {
                getServletContext().getRequestDispatcher("/jsp/admin/changepass.jsp").forward(req, res);
            } else {
                if (loginReturn.length() > 0) {
                    //System.out.println(loginReturn);
                    res.sendRedirect(loginReturn);
                    //req.getRequestDispc\atcher(loginReturn).forward(req, res);
                } else {
                    res.sendRedirect("/");
                    //req.getRequestDispatcher("/jsp/index.jsp").forward(req, res);
                }
            }
        } else {
            getServletContext().getRequestDispatcher(loginForm).forward(req, res);
        }
    }
}
