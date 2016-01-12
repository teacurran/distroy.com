package com.approachingpi.store.servlet;

import com.approachingpi.servlet.PiServlet;
import com.approachingpi.util.MessageBean;
import com.approachingpi.user.User;
import com.approachingpi.store.site.Content;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import javax.mail.Message;
import javax.mail.MessageContext;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.InternetAddress;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.mail.Address;
import javax.mail.internet.MimeMessage.RecipientType;

/**
 * User: terrence
 * Date: Aug 30, 2004
 * Time: 1:28:43 PM
 */
public class ForgotPasswordServlet extends PiServlet {

	public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
	    doPost(req,res);
	}

	public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
	    super.initPiServlet(req, res);

		Connection con = openConnection();
		MessageBean errorBean = getErrorBean(req);
		MessageBean messageBean = getMessageBean(req);

		String email = PiServlet.getReqString(req,"email");
                boolean boFirstTime = PiServlet.getReqBoolean(req,"firstTime", false);

	if (!boFirstTime) {
                if (email.equals("")) {
			errorBean.addMessage("Please enter an email address.");
		}

		if (errorBean.getMessageCount() == 0) {
			try {
				PreparedStatement ps = con.prepareStatement("SELECT * FROM tbUser WHERE inId = (SELECT MAX(inId) FROM tbUser WHERE vcEmail=?)");
				ps.setString(1,email);
				ResultSet rs = ps.executeQuery();
				if (!rs.next()) {
					errorBean.addMessage("Unable to find a user account with the email address you entered.");
				} else {
					User user = new User(rs.getInt("inId"));
					user.loadFromRs(rs);

					Content messageBody = new Content();
					messageBody.setUrl("internal/email/forgotpassword");
					messageBody.loadFromDbByUrl(con);
					messageBody.setBodyText(messageBody.getBodyText().replaceAll("#LOGIN#",user.getEmail()));
					messageBody.setBodyText(messageBody.getBodyText().replaceAll("#PASSWORD#",user.getPassword()));

					//Get session
					Session session = Session.getDefaultInstance(getDefines(), null);  // getInstance??
					session.setDebug(false);

					//Define message
					MimeMessage message = new MimeMessage(session);
					//Message message = new Message(session);
					message.setFrom(new InternetAddress("support@distroy.com")); // TODO CHANGE THIS
					message.setContent(message, "text/plain");
					message.setSubject(messageBody.getTitle());
					message.setText(messageBody.getBodyText());
                                        message.setRecipient(RecipientType.TO, new InternetAddress(email));

					Transport.send(message);
					messageBean.addMessage("Your password has been sent to the email address you specified.");
				}
				rs.close();
			} catch (Exception e) {
				errorBean.addMessage("Unable to send password.  Please try again at a later time.");
				e.printStackTrace();
			}
		}
           }            
		                try {
                con.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                getServletContext().getRequestDispatcher("/jsp/forgot_password_popup.jsp").forward(req, res);
	}
}
