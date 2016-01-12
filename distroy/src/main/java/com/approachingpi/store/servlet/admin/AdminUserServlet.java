package com.approachingpi.store.servlet.admin;

import com.approachingpi.user.User;
import com.approachingpi.user.Company;
import com.approachingpi.util.MessageBean;
import com.approachingpi.util.PasswordGenerator;
import com.approachingpi.servlet.PiServlet;
import com.approachingpi.store.site.Content;
import com.approachingpi.store.order.OrderSearch;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.InternetAddress;
import javax.mail.Transport;
import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;

/**
 * User: Terrence Curran
 * Date: Aug 22, 2006
 * Time: 1:31:06 AM
 * Desc:
 */
public class AdminUserServlet extends UserServlet {
    public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
        doPost(req,res);
    }

    public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
        req.setAttribute("typeFilter", Integer.toString(User.TYPE_ADMIN));

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
                userEdit.setType(User.TYPE_ADMIN);
            }

            if (action == ACTION_SAVE) {
                userEdit.setEmail(PiServlet.getReqString(req,"email"));
                if (!PiServlet.getReqString(req,"password").equals("")) {
                    userEdit.setPassword(PiServlet.getReqString(req,"password"));
                }
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
            req.getRequestDispatcher("/jsp/admin/admin_user_list.jsp").forward(req, res);
        } else {
            req.getRequestDispatcher("/jsp/admin/admin_user_edit.jsp").forward(req, res);
        }
    }
}
