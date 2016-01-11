/*
 * UserServlet.java
 *
 * Created on September 26, 2004, 3:08 PM
 *
 * @author   Terrence Curran
 *
 */

package com.approachingpi.store.servlet.admin;

import com.approachingpi.servlet.*;
import com.approachingpi.util.MessageBean;
import com.approachingpi.util.PasswordGenerator;
import com.approachingpi.user.UserSearchEngine;
import com.approachingpi.user.User;
import com.approachingpi.user.Company;
import com.approachingpi.search.ResultPage;
import com.approachingpi.store.order.OrderSearch;
import com.approachingpi.store.site.Content;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.Transport;
import java.io.IOException;

import java.sql.*;
import java.util.ArrayList;

public class UserServlet extends PiServlet {
    public static final int ACTION_LIST         = 0;
    public static final int ACTION_EDIT         = 1;
    public static final int ACTION_SAVE         = 2;
    public static final int ACTION_DELETE       = 3;
    public static final int ACTION_SEND_EMAIL   = 4;

	public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
	    doPost(req,res);
	}

	public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
	    super.initPiServlet(req, res);

        MessageBean errorBean = super.getErrorBean(req);
        MessageBean messageBean = super.getMessageBean(req);

        Connection con = openConnection();

        User user = getSession(req,res,con).getUser();

        int action = PiServlet.getReqInt(req,"action",ACTION_LIST);

        if (action == ACTION_LIST) {
            this.listUsers(req, res, con, user);
        }
        if (action == ACTION_EDIT || action == ACTION_SAVE || action == ACTION_SEND_EMAIL) {
            User userEdit = new User(PiServlet.getReqInt(req,"userId"));
            try {
                userEdit.loadFromDb(con);
                userEdit.loadAddressesFromDb(con);
                userEdit.getCompany().loadFromDb(con);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (userEdit.getId() == 0) {
                Company company = new Company(PiServlet.getReqInt(req, "companyId"));
                if (company.getId() > 0) {
                    userEdit.setCompany(company);
                }
                userEdit.setPassword(PasswordGenerator.generate("LLLL###"));
            }

            if (action == ACTION_SAVE) {
                userEdit.setEmail(PiServlet.getReqString(req,"email"));
                userEdit.setMailingList(PiServlet.getReqBoolean(req,"mailingList"));
                if (!PiServlet.getReqString(req,"password").equals("")) {
                    userEdit.setPassword(PiServlet.getReqString(req,"password"));
                }
                userEdit.setType(PiServlet.getReqInt(req,"type"));
                try {
                    userEdit.saveToDb(con);
                    userEdit.loadFromDb(con);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                messageBean.addMessage("User successfully saved.");
                action = ACTION_EDIT;
            }

            try {
                userEdit.getCompany().loadFromDb(con);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (action == ACTION_SEND_EMAIL) {
                try {
                    Content messageBody = new Content();
                    messageBody.setUrl("internal/email/userapproved");
                    messageBody.loadFromDbByUrl(con);
                    messageBody.setBodyText(messageBody.getBodyText().replaceAll("#LOGIN#",userEdit.getEmail()));
                    messageBody.setBodyText(messageBody.getBodyText().replaceAll("#PASSWORD#",userEdit.getPassword()));

                    //Get session
                    javax.mail.Session session = javax.mail.Session.getDefaultInstance(getDefines(), null);  // getInstance??
                    session.setDebug(false);

                    //Define message
                    MimeMessage message = new MimeMessage(session);
                    //Message message = new Message(session);
                    message.setFrom(new InternetAddress("sales@ireiss.com")); // TODO CHANGE THIS
                    message.setContent(message, "text/plain");
                    message.setSubject(messageBody.getTitle());
                    message.setText(messageBody.getBodyText());
                    message.addRecipient(MimeMessage.RecipientType.TO,new InternetAddress(userEdit.getEmail()));

                    Transport.send(message);
                    messageBean.addMessage("Password has successfully been sent to the user.");
                } catch (Exception e) {
                    errorBean.addMessage("Error sending password to user");
                    e.printStackTrace();
                }
            }

            OrderSearch se = new OrderSearch();
            se.setUser(userEdit);
            ArrayList orderHistory = se.executeAndReturn(con);

            req.setAttribute("userEdit", userEdit);
            req.setAttribute("orderHistory", orderHistory);
        }

        try {
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (action == ACTION_LIST) {
            req.getRequestDispatcher("/jsp/admin/user_list.jsp").forward(req, res);
        } else {
            req.getRequestDispatcher("/jsp/admin/user_edit.jsp").forward(req, res);
        }
    }

    protected void listUsers(HttpServletRequest req, HttpServletResponse res, Connection con, User user) {
        ArrayList users = new ArrayList();
        req.setAttribute("users",users);

        UserSearchEngine se = new UserSearchEngine();

        se.setSortOrder(PiServlet.getReqInt(req,"sortOrder", user.getPrefInt("admin.company.sortorder", UserSearchEngine.SORT_ORDER_DEFAULT)));
        se.addSort(PiServlet.getReqInt(req,"sort", user.getPrefInt("admin.company.sort", UserSearchEngine.SORT_DEFAULT)));
        if (req.getAttribute("typeFilter") != null) {
            try {
                se.setType(Integer.parseInt((String)req.getAttribute("typeFilter")));
            } catch (Exception e) {
            }
        } else {
            se.setType(PiServlet.getReqInt(req,"typeFilter", user.getPrefInt("admin.user.typeFilter", UserSearchEngine.USER_TYPE_ANY)));
        }

        ResultPage rp = new ResultPage();
        rp.setPageSize(20);
        rp.setPage(PiServlet.getReqInt(req,"page", user.getPrefInt("admin.company.page",1)));
        se.setResultPage(rp);

        rp = se.executeReturnResultPage(con);
        req.setAttribute("se", se);
        req.setAttribute("rp", rp);
    }
}