/**
 * User: terrence
 * Date: Sep 27, 2004
 * Time: 1:08:35 AM
 * To change this template use Options | File Templates.
 */

package com.approachingpi.store.servlet;

import com.approachingpi.servlet.PiServlet;
import com.approachingpi.util.Mailer;
import com.approachingpi.util.MessageBean;
import com.approachingpi.store.catalog.ProductVariation;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import java.io.IOException;
import java.sql.Connection;

public class EmailAFriendServlet extends PiServlet {
	public static final int ACTION_MAIN     = 0;
	public static final int ACTION_SEND     = 1;


	public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
	    doPost(req,res);
	}

	public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
	    super.initPiServlet(req, res);
		MessageBean errorBean = this.getErrorBean(req);
		MessageBean messageBean = this.getMessageBean(req);

	    Connection con = openConnection();

		int action = PiServlet.getReqInt(req,"action", ACTION_MAIN);
        String senderEmail = PiServlet.getReqString(req, "senderEmail", "");
		String friendEmail = PiServlet.getReqString(req, "friendEmail", "");
		ProductVariation variation = new ProductVariation(PiServlet.getReqInt(req, "productVariationId"));
		int imageId = PiServlet.getReqInt(req,"imageId");
        try {
	        variation.loadFromDb(con);
	        variation.loadImagesFromDb(con);
	        variation.getProduct().loadFromDb(con);
        } catch (Exception e) {
	        e.printStackTrace();
        }

		if (action == ACTION_SEND) {
			if (senderEmail.length() == 0 || !Mailer.isValidEmailAddress(senderEmail)) {
				errorBean.addMessage("The email address you entered is not valid.");
				errorBean.addHighlightField("senderEmail");
			}
			if (friendEmail.length() == 0 || !Mailer.isValidEmailAddress(friendEmail)) {
				errorBean.addMessage("The email address you entered for your friend is not valid.");
				errorBean.addHighlightField("friendEmail");
			}


		}

		try {
			con.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		req.setAttribute(senderEmail,senderEmail);
		req.setAttribute(friendEmail,friendEmail);
		this.getAltAttribute(req).put("action",action);
		req.getRequestDispatcher("/jsp/mailinglist.jsp").forward(req, res);

	}
}
