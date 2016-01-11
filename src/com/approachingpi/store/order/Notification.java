/*
 * Notification.java
 *
 * Created on September 22, 2004, 12:24 AM
 * @author  Terrence
 */

package com.approachingpi.store.order;

import com.approachingpi.store.Defines;
import com.approachingpi.util.PiUtility;
import com.approachingpi.store.site.Content;

import java.sql.Connection;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.InternetAddress;
import java.util.Date;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.text.NumberFormat;
import java.util.Locale;
import javax.mail.Transport;
import java.lang.Runnable;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

public class Notification implements Runnable {
    public static final int TYPE_COMPLETE           = 0;
    public static final int TYPE_CHECK_PENDING      = 1;
    public static final int TYPE_SHIPMENT           = 2;
    public static final int TYPE_MIN                = 0;
    public static final int TYPE_MAX                = 2;
    public static final int TYPE_DEFAULT            = TYPE_COMPLETE;
    
    Order order;
    Defines defines;
    int type;
    
    public Notification(Order orderIn, Defines definesIn, int type) {
        this.order = orderIn;
        this.defines = definesIn;
        setType(type);
        System.out.println("Order Notification Created");
    }

    public void run() {
        try {
            Context ctx = new InitialContext();
            Context envCtx = (Context) ctx.lookup("java:comp/env");
            //System.out.println("DS:"+defines.getProperty("datasource"));
            DataSource ds = (DataSource)envCtx.lookup(defines.getProperty("datasource"));

            Connection con = ds.getConnection();
            
            sendMail(con);

            con.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void sendMail(Connection con) {
        // TODO this should be temporary
        try {
            order.getBillAddress().getCountry().loadFromDb(con);
            order.getShipAddress().getCountry().loadFromDb(con);
        } catch (Exception e) {
            
        }
        
        Content messageBody = new Content();
        switch (type) {
            case TYPE_COMPLETE:
                messageBody.setUrl("internal/email/recipt");
                break;
            case TYPE_CHECK_PENDING:
                messageBody.setUrl("internal/email/reciptpendingcheck");
                break;
            case TYPE_SHIPMENT:
                messageBody.setUrl("internal/email/shipment");
                break;
        }
        try {
            messageBody.loadFromDbByUrl(con);

            messageBody.setTitle(messageBody.getTitle().replaceAll("#ORDERID#",order.getId()));

            messageBody.setBodyText(messageBody.getBodyText().replaceAll("#ORDERID#",order.getId()));
            messageBody.setBodyText(messageBody.getBodyText().replaceAll("\\\\t","\t"));
            messageBody.setBodyText(PiUtility.replace(messageBody.getBodyText(),"#ADDRESS#",getEmailTextAddress()));
            messageBody.setBodyText(PiUtility.replace(messageBody.getBodyText(),"#DETAILS#",getEmailTextDetail()));
            messageBody.setBodyText(PiUtility.replace(messageBody.getBodyText(), "#DATE#",PiUtility.formatDate(new Date(),"MM/d/yyyy h:m:s zzz")));

            //Get session
            javax.mail.Session session = javax.mail.Session.getDefaultInstance(defines, null);  // getInstance??
            session.setDebug(false);

            //Define message
            MimeMessage message = new MimeMessage(session);
            //Message message = new Message(session);
            message.setFrom(new InternetAddress(defines.getProperty("mail.email.from")));

            //javax.mail.Message.RecipientType.TO
            message.addRecipients(javax.mail.Message.RecipientType.TO, order.getUser().getEmail());
            if (defines.getProperty("mail.email.bcc") != null && defines.getProperty("mail.email.bcc").length() > 0) {
                message.addRecipients(javax.mail.Message.RecipientType.BCC, defines.getProperty("mail.email.bcc"));
            }

            message.setContent(message, "text/plain");
            message.setSubject(messageBody.getTitle());
            message.setText(messageBody.getBodyText());

            Transport.send(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void setType(int in) {
        if (in<TYPE_MIN || in>TYPE_MAX) {
            type = TYPE_DEFAULT;
        } else {
            type = in;
        }
    }

    
    public String getEmailTextAddress() {
	    StringBuffer text = new StringBuffer(2000);
	    OrderAddress billing = order.getBillAddress();
		OrderAddress shipping = order.getShipAddress();

	    // Billing Info
	    text.append("Billing Info:\n");
	    text.append("\t" + billing.getNameFirst() + " " + billing.getNameLast() + "\n");
	    text.append("\t" + billing.getAddress1() + "\n");
	    if (billing.getAddress2().length() > 0) {
		    text.append("\t" + billing.getAddress2() + "\n");
	    }
	    text.append("\t" + billing.getCity() + "\n");
	    if (billing.getState().getAbbrev().length() > 0) {
		    text.append("\t" + billing.getState().getAbbrev() + " " + billing.getZip() + "\n");
	    } else if (billing.getZip().length() > 0) {
		    text.append("\t" + billing.getZip() + "\n");
	    }
	    text.append("\t" + billing.getCountry().getName() + "\n");
	    text.append("\t" + billing.getPhoneNumber() + "\n");
	    text.append("\n");

	    // Shipping Info
	    text.append("Shipping Info:\n");
	    if (shipping == null || shipping.getId() == 0 || billing.getId() == shipping.getId()) {
		    text.append("\t(same as billing info)\n");
	    } else {
		    text.append("\t" + shipping.getNameFirst() + " " + shipping.getNameLast() + "\n");
		    text.append("\t" + shipping.getAddress1() + "\n");
		    if (shipping.getAddress2().length() > 0) {
			    text.append("\t" + shipping.getAddress2() + "\n");
		    }
		    text.append("\t" + shipping.getCity() + "\n");
		    if (shipping.getState().getAbbrev().length() > 0) {
			    text.append("\t" + shipping.getState().getAbbrev() + " " + shipping.getZip() + "\n");
		    } else if (shipping.getZip().length() > 0) {
			    text.append("\t" + shipping.getZip() + "\n");
		    }
		    text.append("\t" + shipping.getCountry().getName() + "\n");
		    text.append("\t" + shipping.getPhoneNumber() + "\n");
	    }
	    return text.toString();
    }
	public String getEmailTextDetail() {
		NumberFormat dollarFormat = NumberFormat.getCurrencyInstance(Locale.US);
		StringBuffer text = new StringBuffer(2000);

		// Item Details
        // TODO put a special case here for order shipments. add a column for ship date, or tracking number
		text.append("Items you have purchased:\n");
		text.append("QTY       Price       Description\n");
		text.append("-----------------------------------------\n");
		ArrayList items = order.getDetails();
		for (int i=0; i<items.size(); i++) {
			OrderDetail item = (OrderDetail)items.get(i);
			text.append(PiUtility.padRight(Integer.toString(item.getQty()),10) + PiUtility.padLeft(dollarFormat.format(item.getPriceTotal().doubleValue()),10) + "  ");
            text.append(item.getProductVariation().getSku() + "\n");
			text.append("                      " + item.getProductVariation().getProduct().getName() + "\n");
			text.append("                      " + item.getProductVariation().getStyle() + "/" + item.getProductVariation().getColor() + "\n");
			text.append("                      " + item.getSize().getName() + "\n");
		}
		text.append("-----------------------------------------\n\n");
		text.append(PiUtility.padLeft(dollarFormat.format(order.getAmountSubtotal().doubleValue()),20) + " Subtotal\n");
		text.append(PiUtility.padLeft(dollarFormat.format(order.getAmountShipping().doubleValue()),20) + " Shipping & Handling\n");
        if (order.getAmountCouponTotal().compareTo(new BigDecimal("0.00")) > 0) {
    		text.append(PiUtility.padLeft(dollarFormat.format(order.getAmountCouponTotal().doubleValue()),20) + " Coupon Discount\n");
        }
		text.append(PiUtility.padLeft(dollarFormat.format(order.getAmountTax().doubleValue()),20) + " Tax\n");
		text.append(PiUtility.padLeft(dollarFormat.format(order.getAmountTotal().doubleValue()),20) + " Total\n");

		return text.toString();
	}
}
