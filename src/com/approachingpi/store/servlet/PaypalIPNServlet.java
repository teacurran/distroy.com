/*
 * PaypalIPNServlet.java
 *
 * Created on September 21, 2004, 4:23 PM
 *
 * @author  Terrence
 */

package com.approachingpi.store.servlet;

import com.approachingpi.servlet.PiServlet;
import com.approachingpi.store.order.*;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.sql.Connection;
import java.util.Enumeration;
import java.net.URLEncoder;
import java.net.URL;
import java.net.URLConnection;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.math.BigDecimal;
import java.util.Calendar;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class PaypalIPNServlet extends PiServlet {
    /** Creates a new instance of PaypalIPNServlet */
        
    public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
        doPost(req,res);
    }

    public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
        super.initPiServlet(req, res);
        Connection con = openConnection();

        // read post from PayPal system and add 'cmd'
        Enumeration en = req.getParameterNames();
        String str = "cmd=_notify-validate";
        while(en.hasMoreElements()){
            String paramName = (String)en.nextElement();
            String paramValue = req.getParameter(paramName);
            str = str + "&" + paramName + "=" + URLEncoder.encode(paramValue,"UTF-8");
        }

        // post back to PayPal system to validate
        // NOTE: change http: to https: in the following URL to verify using SSL (for increased security).
        // using HTTPS requires either Java 1.4 or greater, or Java Secure Socket Extension (JSSE)
        // and configured for older versions.
        URL u = new URL("http://www.paypal.com/cgi-bin/webscr");
        URLConnection uc = u.openConnection();
        uc.setDoOutput(true);
        uc.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
        PrintWriter pw = new PrintWriter(uc.getOutputStream());
        pw.println(str);
        pw.close();

        BufferedReader in = new BufferedReader(new InputStreamReader(uc.getInputStream()));
        String paypalResponse = in.readLine();
        in.close();

        // assign posted variables to local variables
        String itemName         = PiServlet.getReqString(req,"item_name");
        String itemNumber       = PiServlet.getReqString(req,"item_number");
        String paymentStatus    = PiServlet.getReqString(req,"payment_status");
        String paymentAmount    = PiServlet.getReqString(req,"mc_gross");
        String paymentCurrency  = PiServlet.getReqString(req,"mc_currency");
        String txnId            = PiServlet.getReqString(req,"txn_id");
        String receiverEmail    = PiServlet.getReqString(req,"receiver_email");
        String payerEmail       = PiServlet.getReqString(req,"payer_email");
        String orderId          = PiServlet.getReqString(req,"invoice");

        //check notification validation
        if(paypalResponse.equals("VERIFIED")) {
            // check that paymentStatus=Completed
            // check that txnId has not been previously processed
            // check that receiverEmail is your Primary PayPal email
            // check that paymentAmount/paymentCurrency are correct
            // process payment
            Order order = new Order();
            order.setId(orderId);
            try {
                order.loadFromDb(con);
                order.loadPaymentsFromDb(con);
                
                boolean paymentFound = false;
                
                ArrayList payments = order.getPayments();
                for (int i=0; i<payments.size(); i++) {
                    Payment payment = (Payment)payments.get(i);
                    if (payment.getType() == Payment.TYPE_PAYPAL) {
                        PaymentTransaction pending = payment.getTransactionOfType(PaymentTransaction.TYPE_PENDING);
                        PaymentTransaction success = payment.getTransactionOfType(PaymentTransaction.TYPE_CAPTURE);
                        if (pending != null && success == null) {
                            success = new PaymentTransaction();
                            success.setType(PaymentTransaction.TYPE_CAPTURE);
                            success.setPayment(payment);
                            success.setResult(0);
                            success.setPnref(txnId);
                            success.setAmount(new BigDecimal(paymentAmount));
                            success.saveToDb(con);
                            
                            payment.setAmountCaptured(new BigDecimal(paymentAmount));
                            payment.setDateSettled(Calendar.getInstance().getTime());
                            payment.saveToDb(con);
                            paymentFound = true;
                            break;
                        }
                    }
                }
                // technically it is an error if we get here.  if we have already processed this transaction ignore it. 
                // otherwise, attach it to the order
                if (!paymentFound) {
                    PreparedStatement ps = con.prepareStatement("SELECT * FROM tbPaymentTransaction WHERE inType=? AND vcPnRef = ?");
                    ps.setInt(1,PaymentTransaction.TYPE_CAPTURE);
                    ps.setString(2,txnId);
                    ResultSet rs = ps.executeQuery();
                    boolean processAsNew = false;
                    if (!rs.next()) {
                        processAsNew = true;
                    }
                    if (processAsNew) {
                        Payment newPayment = new Payment();
                        newPayment.setUser(order.getUser());
                        newPayment.setOrder(order);
                        newPayment.setType(Payment.TYPE_PAYPAL);
                        newPayment.setAmountHeld(new BigDecimal(paymentAmount));
                        newPayment.setAmountCaptured(new BigDecimal(paymentAmount));
                        newPayment.setDateHeld(Calendar.getInstance().getTime());
                        newPayment.setDateSettled(Calendar.getInstance().getTime());

                        newPayment.saveToDb(con);

                        PaymentTransaction pending = new PaymentTransaction();
                        pending.setType(PaymentTransaction.TYPE_PENDING);
                        pending.setPayment(newPayment);
                        pending.setResult(0);
                        pending.setAmount(new BigDecimal(paymentAmount));
                        pending.saveToDb(con);

                        PaymentTransaction success = new PaymentTransaction();
                        success.setType(PaymentTransaction.TYPE_CAPTURE);
                        success.setPayment(newPayment);
                        success.setResult(0);
                        success.setPnref(txnId);
                        success.setAmount(new BigDecimal(paymentAmount));
                        success.saveToDb(con);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else if(paypalResponse.equals("INVALID")) {
            // log for investigation
            System.out.println("INVALID PAYPAL IPN:");
            System.out.println("\titemName=" + itemName);
            System.out.println("\titemNumber=" + itemNumber);
            System.out.println("\tpaymentStatus=" + paymentStatus);
            System.out.println("\tpaymentAmount=" + paymentAmount);
            System.out.println("\tpaymentCurrency=" + paymentCurrency);
            System.out.println("\ttxnId=" + txnId);
            System.out.println("\treceiverEmail=" + receiverEmail);
            System.out.println("\tpayerEmail=" + payerEmail);
            System.out.println("\torderId=" + orderId);
        }
        else {
            System.out.println("INVALID IPN ERROR:");
            System.out.println("\titemName=" + itemName);
            System.out.println("\titemNumber=" + itemNumber);
            System.out.println("\tpaymentStatus=" + paymentStatus);
            System.out.println("\tpaymentAmount=" + paymentAmount);
            System.out.println("\tpaymentCurrency=" + paymentCurrency);
            System.out.println("\ttxnId=" + txnId);
            System.out.println("\treceiverEmail=" + receiverEmail);
            System.out.println("\tpayerEmail=" + payerEmail);
            System.out.println("\torderId=" + orderId);
        }
        
        try {
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
