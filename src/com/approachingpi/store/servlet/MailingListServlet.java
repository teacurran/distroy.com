/*
 * MailingList.java
 *
 * Created on October 9, 2004, 2:05 AM
 *
 * @author  Tea Curran
 *
 * @desctiption Allows a user to subscribe or unsubscribe their email address to our mailing list.
 *
 */

package com.approachingpi.store.servlet;

import com.approachingpi.servlet.*;
import com.approachingpi.util.Mailer;
import com.approachingpi.util.MessageBean;
import com.approachingpi.user.MailingList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import java.sql.PreparedStatement;
import java.sql.Connection;
import java.io.IOException;

public class MailingListServlet extends PiServlet{
	public static final int ACTION_SUB      = 0;
	public static final int ACTION_UNSUB    = 1;

	public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
		doPost(req,res);
	}

	public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
		super.initPiServlet(req, res);
        MessageBean errorBean = this.getErrorBean(req);
		MessageBean messageBean = this.getMessageBean(req);

		int action = PiServlet.getReqInt(req,"action", ACTION_SUB);
        String email = PiServlet.getReqString(req, "email", "");

		if (email.length() > 0) {
			if (!Mailer.isValidEmailAddress(email)) {
				errorBean.addMessage(getDefines().getProperty("message.field.bademail"));
				errorBean.addHighlightField("email");
			} else {
				Connection con = openConnection();
                if (action == ACTION_SUB) {
                    MailingList mailingList = new MailingList(email);
                    mailingList.setSubscribed(true);
                    try {
                        mailingList.saveToDb(con);
                        messageBean.addMessage(email + " has been added to the mailing list.");
                    } catch (Exception e) {
                        errorBean.addMessage("Error subscribing to mailing list, please try again later");
                        e.printStackTrace();
                    }
                } else {
                    MailingList mailingList = new MailingList(email);
                    mailingList.setSubscribed(false);
                    try {
                        mailingList.saveToDb(con);
                        messageBean.addMessage(email + " has been removed from the mailing list.");
                    } catch (Exception e) {
                        errorBean.addMessage("Error removing from mailing list, please try again later");
                        e.printStackTrace();
                    }
                }
                email = "";
                try {
                    con.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
			}
		}

		req.setAttribute("email",email);
		this.getAltAttribute(req).put("action",action);
		req.getRequestDispatcher("/jsp/mailinglist.jsp").forward(req, res);
	}
}
