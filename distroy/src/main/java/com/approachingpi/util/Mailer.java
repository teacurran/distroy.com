/*
 * Mailer.java
 *
 * Created on August 12, 2004, 11:46 PM
 *
 */

package com.approachingpi.util;

import javax.mail.Transport;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.AddressException;
import javax.mail.internet.MimeMessage;
import javax.mail.Session;
import java.util.Properties;

public class Mailer implements Runnable {
	Properties properties = new Properties();
	MimeMessage message;
	Session session;

	public Mailer() {
	}

	public Mailer(String from, String to, String subject, String body) throws AddressException, MessagingException {
		setFrom(from);
		addTo(to);
		setSubject(subject);
		setBody(body);
	}

	public void addBCC(String email) throws AddressException, MessagingException {
		getMimeMessage().addRecipients(javax.mail.Message.RecipientType.BCC, email);
	}
	public void addCC(String email) throws AddressException, MessagingException {
		getMimeMessage().addRecipients(javax.mail.Message.RecipientType.CC, email);
	}
	public void addTo(String email) throws AddressException, MessagingException {
		getMimeMessage().addRecipients(javax.mail.Message.RecipientType.TO, email);
	}

	private MimeMessage getMimeMessage() {
		if (message == null) {
			message = new MimeMessage(getSession());
		}
		return message;
	}
	private Session getSession() {
		if (session == null) {
			session = Session.getDefaultInstance(properties);
		}
		return session;
	}


	public void run() {
	    try {
		    Transport.send(message);
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}

	public void setBody(String body) throws MessagingException {
		getMimeMessage().setText(body);

	}
	public void setFrom(String from) throws AddressException, MessagingException {
		getMimeMessage().setFrom(new InternetAddress(from));
	}
	public void setSmtpHost(String host) {
		properties.put("mail.smtp.host", host);
	}
	public void setSmtpPort(int port) {
		properties.put("mail.smtp.port", Integer.toString(port));
	}
	public void setSmtp(String host, int port) {
		properties.put("mail.smtp.host", host);
		properties.put("mail.smtp.port", Integer.toString(port));
	}
	public void setSubject(String subject) throws MessagingException {
		getMimeMessage().setSubject(subject);
	}




    public static boolean isValidEmailAddress(String aEmailAddress){
        if (aEmailAddress == null) return false;

        if (aEmailAddress.length() == 0) {
            return false;
        }

        boolean result = true;
        try {
            InternetAddress emailAddr = new InternetAddress(aEmailAddress);

            emailAddr.validate();
        } catch (AddressException ex){
            result = false;
        }
        return result;
    }
}
