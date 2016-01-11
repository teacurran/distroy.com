package com.approachingpi.store.servlet.admin;

import com.approachingpi.servlet.PiServlet;
import com.approachingpi.store.order.*;
import com.approachingpi.store.Store;
import com.approachingpi.util.MessageBean;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import java.io.IOException;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Calendar;
import java.math.BigDecimal;

/**
 * User: Terrence Curran
 * Date: Sep 13, 2006
 * Time: 5:35:30 PM
 * Desc:
 */
public class QuickChargeServlet extends PiServlet {
    public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
        doPost(req,res);
    }

    public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
        super.initPiServlet(req, res);

        MessageBean errors = this.getErrorBean(req);

        Connection con = openConnection();

        if (req.getParameter("button_submit") != null) {
            String name = PiServlet.getReqString(req, "name");
            String cardnum = PiServlet.getReqString(req, "cardnum");
            String reference = PiServlet.getReqString(req, "reference");

            Payment payment = new Payment(PiServlet.getReqString(req,"paymentType",Payment.TYPE_CC));

            req.setAttribute("payment", payment);
            req.setAttribute("reference", reference);

            Order order = new Order();

            try {
                com.approachingpi.store.Store store = new Store("DWE");
                try {
                    store.loadFromDbByAbbreviation(con);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                order.setStore(store);
                payment.setStore(store);
                payment.setOrder(order);
            } catch (Exception e) {
                e.printStackTrace();
            }

            BigDecimal amount = new BigDecimal("0.00");
            try {
                amount = new BigDecimal(PiServlet.getReqString(req, "amount"));
            } catch (Exception e) {
            }

            if (amount.doubleValue() > 0) {
                payment.setType(Payment.TYPE_CC);
                payment.setAccountNumber(cardnum);
                payment.setCreditName(name);
                payment.setExpireMonth(PiServlet.getReqInt(req,"expireMonth"));
                payment.setExpireYear(PiServlet.getReqInt(req,"expireYear"));
                payment.setCcvNumber(PiServlet.getReqInt(req,"cvvNumber"));
                payment.setAmount(amount);

                if (payment.getCreditName().length() == 0) {
                    errors.addMessage("You must enter your name as it appears on your credit card.");
                    errors.addHighlightField("creditName");
                }
                if (!payment.getAccountNumber().equalsIgnoreCase("1111222233334444")) {
                    if (!CCUtils.validCC(payment.getAccountNumber())) {
                        errors.addMessage("The credit card number you entered is invalid.  Please check the number and try again.");
                        errors.addHighlightField("accountNumber");
                    }
                }

                SimpleDateFormat sdf = new SimpleDateFormat("M/yyyy");
                Date dateExpire = null;
                try {
                    dateExpire = sdf.parse(payment.getExpireMonth() + "/" + payment.getExpireYear());
                } catch (Exception e) {}

                if (dateExpire == null) {
                    errors.addMessage("The credit card expiration you entered is invalid.");
                    errors.addHighlightField("expiration");
                    errors.addHighlightField("expireMonth");
                    errors.addHighlightField("expireYear");
                } else {
                    GregorianCalendar cal = new GregorianCalendar();
                    cal.setTime(dateExpire);
                    cal.set(Calendar.DAY_OF_MONTH,cal.getActualMaximum(Calendar.DAY_OF_MONTH));
                    if (cal.getTimeInMillis() < GregorianCalendar.getInstance().getTimeInMillis()) {
                        errors.addMessage("The credit card expiration you entered is invalid.");
                        errors.addHighlightField("expiration");
                        errors.addHighlightField("expireMonth");
                        errors.addHighlightField("expireYear");
                    }
                }
            } else {
                errors.addMessage("Zero dollar amount");
            }

            // run any authorization we might need to do.
            // for credit cards this means putting a hold on the card.
            // some other types it just means putting a PENDING transaction
            try {
                if (errors.getMessageCount() == 0) {
                    try {
                        payment.directSale(con, getDefines(), amount, null, null, name);
                    } catch (PaymentException pe) {
                        errors.addMessage(payment.getResultReasonText());
                        errors.addMessage(payment.getErrorMessage());
                    }
                }
            // of course we should never get here, but just incase.
            } catch (Exception e) {
                errors.addMessage("Error processing your request.  If this error continues please contact webmaster.");
                e.printStackTrace();
                errors.addMessage(e.getMessage());
            }

            if (errors.getMessageCount() == 0) {
                try {
                    order.setAmountSubtotal(amount);
                    order.setAmountTotal(amount);
                    order.setStatus(Order.STATUS_SHIPPED);
                    order.addPayment(payment);
                    order.saveToDb(con);
                    payment.saveToDb(con);

                    OrderComment comment = new OrderComment();
                    comment.setOrder(order);
                    comment.setBody("Name:" + name + "\nRef:" + reference);
                    comment.saveToDb(con);

                    req.setAttribute("order", order);
                    errors.addMessage("Charge Succesful!");
                    errors.addMessage("Order Number:" + order.getId());

                    req.setAttribute("payment", new Payment());
                    req.setAttribute("reference", "");

                } catch (Exception e) {
                    // THIS IS SERIOUS IF WE GET HERE
                    // TODO write something that sends an email if this happens
                    e.printStackTrace();
                    errors.addMessage(e.getMessage());
                }
            }
        }

        try {
            con.close();
        } catch (Exception e) { e.printStackTrace(); }

        req.getRequestDispatcher("/jsp/admin/quickcharge.jsp").forward(req, res);
    }
}
