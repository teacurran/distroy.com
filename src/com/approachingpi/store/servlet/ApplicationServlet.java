package com.approachingpi.store.servlet;

import com.approachingpi.servlet.PiServlet;
import com.approachingpi.user.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import java.io.IOException;
import java.sql.Connection;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.Transport;


/**
 * User: terrence
 * Date: Aug 31, 2004
 * Time: 9:46:01 PM
 */
public class ApplicationServlet extends PiServlet {


	public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
	    doPost(req,res);
	}

	public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
	    super.initPiServlet(req, res);

		Connection con = openConnection();

		if (PiServlet.getReqBoolean(req,"formSubmitted")) {
			Company company = new Company();
			company.setStatus(Company.STATUS_PENDING);
			company.setName(PiServlet.getReqString(req,"companyName"));

			User wsUser = new User();
			wsUser.setCompany(company);
			wsUser.setType(User.TYPE_WHOLESALE_PENDING);
			wsUser.setEmail(PiServlet.getReqString(req,"emailAddress"));
            wsUser.setCompany(company);

			Address address = new Address();
			address.setUser(wsUser);
			address.setReference("Main Contact");
			address.setNameFirst(PiServlet.getReqString(req,"nameFirst"));
			address.setNameLast(PiServlet.getReqString(req,"nameLast"));
			address.setAddress1(PiServlet.getReqString(req,"address1"));
			address.setAddress2(PiServlet.getReqString(req,"address2"));
			address.setCity(PiServlet.getReqString(req,"city"));
			address.setState(new State(PiServlet.getReqString(req,"state")));
			address.setZip(PiServlet.getReqString(req,"zip"));
			address.setCountry(new Country(PiServlet.getReqInt(req,"country")));
			address.setPhoneNumber(PiServlet.getReqString(req,"phoneNumber"));
			address.setFaxNumber(PiServlet.getReqString(req,"faxNumber"));

			CompanyComment comment = new CompanyComment();
			comment.setCompany(company);
			comment.setUser(wsUser);
			comment.setComment(PiServlet.getReqString(req,"comment"));

            CompanyComment hearAboutUs = new CompanyComment();
			hearAboutUs.setCompany(company);
			hearAboutUs.setUser(wsUser);
			hearAboutUs.setComment(PiServlet.getReqString(req,"hearAboutUs"));

			try {
				company.saveToDb(con);
				wsUser.saveToDb(con);
				address.saveToDb(con);
				if (!comment.getComment().equals("")) {
					company.saveToDb(con);
				}
				if (!hearAboutUs.getComment().equals("")) {
                    hearAboutUs.setComment("Heard About Us:\t" + hearAboutUs.getComment());
					hearAboutUs.saveToDb(con);
				}
            } catch (Exception e) {
				e.printStackTrace();
			}
			try {
                address.getCountry().loadFromDb(con);
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            try {
                StringBuffer content = new StringBuffer(5000);
                content.append("Company Name:\t" + company.getName() + "\n");
                content.append("First Name:\t" + address.getNameFirst() + "\n");
                content.append("Last Name:\t" + address.getNameLast() + "\n");
                content.append("Contact type:\t" + address.getReference() + "\n");
                content.append("Address 1:\t" + address.getAddress1() + "\n");
                content.append("Address 2:\t" + address.getAddress2() + "\n");
                content.append("City:\t" + address.getCity() + "\n");
                content.append("State:\t" + address.getState().getAbbrev() + "\n");
                content.append("Zip:\t" + address.getZip() + "\n");
                content.append("Country:\t" + address.getCountry().getName() + "\n");
                content.append("Phone Number:\t" + address.getPhoneNumber() + "\n");
                content.append("Fax Number:\t" + address.getFaxNumber() + "\n");
                content.append("Email:\t" + wsUser.getEmail() + "\n");
                content.append(hearAboutUs.getComment() + "\n");
                content.append("Comments:\t" + comment.getComment() + "\n");
                
                //Get session
                javax.mail.Session session = javax.mail.Session.getDefaultInstance(getDefines(), null);  // getInstance??
                session.setDebug(false);

                //Define message
                MimeMessage message = new MimeMessage(session);
                //Message message = new Message(session);
                message.setFrom(new  javax.mail.internet.InternetAddress("support@distroy.com"));

                //javax.mail.Message.RecipientType.TO
                message.addRecipients(javax.mail.Message.RecipientType.TO, "support@distroy.com");
                message.addRecipients(javax.mail.Message.RecipientType.CC, "gill@approachingpi.com");
                message.addRecipients(javax.mail.Message.RecipientType.CC, "tea@distroy.com");

                message.setContent(message, "text/plain");
                message.setSubject("DISTRO.Y - New Wholesale Application");
                message.setText(content.toString());

                Transport.send(message);
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                con.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

			req.getRequestDispatcher("/wholesale/content/application/thanks").forward(req, res);
			return;
		}

		try {
			con.close();
		} catch (Exception e) {
		}

		req.getRequestDispatcher("/jsp/wholesale/application.jsp").forward(req, res);

	}
}
