package com.approachingpi.store.servlet.admin;

import com.approachingpi.servlet.PiServlet;
import com.approachingpi.servlet.Session;
import com.approachingpi.store.Store;
import com.approachingpi.store.Defines;
import com.approachingpi.store.catalog.Size;
import com.approachingpi.store.catalog.ProductVariation;
import com.approachingpi.store.order.*;
import com.approachingpi.user.User;
import com.approachingpi.util.MessageBean;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Calendar;

/**
 * User: terrence
 * Date: Sep 9, 2004
 * Time: 6:36:43 PM
 */
public class OrderServlet extends PiServlet {
    public static final int ACTION_LIST     = 0;
	public static final int ACTION_EDIT     = 1;
	public static final int ACTION_CAPTURE  = 2;
    public static final int ACTION_SALE     = 3;
    public static final int ACTION_SAVE     = 4;
    public static final int ACTION_RECONCILE    = 5;
    public static final int ACTION_SHIP     = 6;
    public static final int ACTION_AUTH     = 7;

	public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
	    doPost(req,res);
	}

	public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
	    super.initPiServlet(req, res);

		Connection con = openConnection();

        MessageBean errorBean = this.getErrorBean(req);

		int action = PiServlet.getReqInt(req, "action");

        Session session = this.getSession(req,res,con);
        User user = session.getUser();

        if (user.getId()==0 || user.getType() < User.TYPE_ADMIN) {
            try {
                con.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            errorBean.addMessage(Defines.ERROR_NOT_LOGGED_IN);
            req.setAttribute("loginReturn","/admin/Order");
            req.setAttribute("loginForm","/jsp/admin/login.jsp");
            forwardRequest(req,res,"/login");

            return;
        }

        if (action == ACTION_LIST) {
            OrderSearch search = new OrderSearch();

            SimpleDateFormat sdf = new SimpleDateFormat("M/d/yy");
            try {
                search.setDateStart(sdf.parse(PiServlet.getReqString(req,"dateStart")));
            } catch (Exception e) {}
            try {
                search.setDateEnd(sdf.parse(PiServlet.getReqString(req,"dateEnd")));
            } catch (Exception e) {}

            search.setStore(new Store(PiServlet.getReqInt(req,"storeId",OrderSearch.STORE_ANY)));
            search.addSort(PiServlet.getReqInt(req,"sort"));
            search.setSortOrder(PiServlet.getReqInt(req,"sortOrder"));
            search.setStatus(PiServlet.getReqInt(req,"status", OrderSearch.STATUS_ANY));
            ArrayList orders = search.executeAndReturn(con);
            req.setAttribute("orders", orders);
            req.setAttribute("search", search);
        }

        Order order = new Order(PiServlet.getReqString(req,"orderId"));
		if (action != ACTION_LIST) {
			try {
				order.loadFromDb(con);
                order.loadExtendedFromDb(con);
                ArrayList comments = order.getComments();
                for (int i=0; i<comments.size(); i++) {
                    ((OrderComment)comments.get(i)).getUser().loadAddressesFromDb(con);
                }
			} catch (Exception e) {
				e.printStackTrace();
			}
            req.setAttribute("order",order);

            /* we put this here because a comment can come in through a save or a capture */
            OrderComment newComment = new OrderComment();
            newComment.setBody(PiServlet.getReqString(req, "newComment"));
            //TODO newComment.setUser(user);
            newComment.setOrder(order);
            if (!newComment.getBody().equals("")) {
                order.addComment(newComment);
                try {
                    newComment.saveToDb(con);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }

        if (action == ACTION_SAVE) {
            int oldStatus = order.getStatus();
            order.setStatus(PiServlet.getReqInt(req,"statusId"));

            // if the order wasn't shipped before but now is, set the shipcomplete date
            if (order.getStatus() != oldStatus && order.getStatus() == Order.STATUS_SHIPPED) {
                if (order.getDateShipBegan() == null) {
                    order.setDateShipBegan(new Date());
                }
                order.setDateShipComplete(new Date());
            } else if (order.getStatus() != oldStatus && order.getStatus() == Order.STATUS_PARTIAL_SHIP) {
                order.setDateShipBegan(new Date());
            }
            try {
                order.saveToDb(con);
            } catch (Exception e) {
                e.printStackTrace();
            }
            action = ACTION_EDIT;
        }

        if (action == ACTION_CAPTURE) {
            Payment payment = null;
            int paymentIdToCapture = PiServlet.getReqInt(req,"paymentId");
            BigDecimal captureAmountShipping = new BigDecimal(PiServlet.getReqString(req,"captureAmountShipping", "0.00"));
            BigDecimal captureAmountSubtotal = new BigDecimal(PiServlet.getReqString(req,"captureAmountSubtotal", "0.00"));
            BigDecimal captureAmountTax = new BigDecimal(PiServlet.getReqString(req,"captureAmountTax", "0.00"));
            String reference = PiServlet.getReqString(req,"pnref");

            ArrayList allPayments = order.getPayments();
            for (int i=0; i<allPayments.size(); i++) {
                Payment thisPayment = (Payment)allPayments.get(i);
                if (thisPayment.getId() == paymentIdToCapture) {
                    payment = thisPayment;
                }
            }
            if (payment == null) {
                errorBean.addMessage("Invalid payment id");
            }
            BigDecimal captureAmount = captureAmountSubtotal.add(captureAmountShipping).add(captureAmountTax);
            if (errorBean.getMessageCount() == 0 && captureAmount.compareTo(new BigDecimal("0.00")) > 0) {
                try {
                    payment.loadFromDb(con);
                    payment.loadTransactionsFromDb(con);

                    try {
                        payment.capture(con, getDefines(), captureAmountSubtotal, captureAmountShipping, captureAmountTax, reference);
                        order.reconcilePayments(con);
                    } catch (Exception e2) {
                        e2.printStackTrace();
                        errorBean.addMessage(e2.getMessage());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    errorBean.addMessage("Error capturing payment: " + e.getMessage());
                }
            }
            action = ACTION_EDIT;
        }

        if (action == ACTION_RECONCILE) {
            try {
                order.reconcilePayments(con);
            } catch (Exception e2) {
                e2.printStackTrace();
                errorBean.addMessage(e2.getMessage());
            }
            action = ACTION_EDIT;
        }

        if (action == ACTION_AUTH) {
            Payment payment = new Payment(PiServlet.getReqString(req,"type"));
            payment.setStore(order.getStore());
            payment.setOrder(order);
            payment.setAccountNumber(PiServlet.getReqString(req,"accountNumber"));
            payment.setExpireMonth(PiServlet.getReqInt(req,"expireMonth"));
            payment.setExpireYear(PiServlet.getReqInt(req,"expireYear"));
            payment.setAmount(new BigDecimal(PiServlet.getReqString(req,"amount")));

            if (payment.getType().equalsIgnoreCase(Payment.TYPE_CC)) {
                if (payment.getCreditName().length() == 0) {
                    errorBean.addMessage("You must enter your name as it appears on your credit card.");
                    errorBean.addHighlightField("creditName");
                }
                if (!payment.getAccountNumber().equalsIgnoreCase("1111222233334444")) {
                    if (!CCUtils.validCC(payment.getAccountNumber())) {
                        errorBean.addMessage("The credit card number you entered is invalid.  Please check the number and try again.");
                        errorBean.addHighlightField("accountNumber");
                    }
                }

                SimpleDateFormat sdf = new SimpleDateFormat("M/yyyy");
                Date dateExpire = null;
                try {
                    dateExpire = sdf.parse(payment.getExpireMonth() + "/" + payment.getExpireYear());
                } catch (Exception e) {}

                if (dateExpire == null) {
                    errorBean.addMessage("The credit card expiration you entered is invalid.");
                    errorBean.addHighlightField("expiration");
                    errorBean.addHighlightField("expireMonth");
                    errorBean.addHighlightField("expireYear");
                } else {
                    GregorianCalendar cal = new GregorianCalendar();
                    cal.setTime(dateExpire);
                    cal.set(Calendar.DAY_OF_MONTH,cal.getActualMaximum(Calendar.DAY_OF_MONTH));
                    if (cal.getTimeInMillis() < GregorianCalendar.getInstance().getTimeInMillis()) {
                        errorBean.addMessage("The credit card expiration you entered is invalid.");
                        errorBean.addHighlightField("expiration");
                        errorBean.addHighlightField("expireMonth");
                        errorBean.addHighlightField("expireYear");
                    }
                }
            }

            // run any authorization we might need to do.
            // for credit cards this means putting a hold on the card.
            // some other types it just means putting a PENDING transaction
            try {
                if (errorBean.getMessageCount() == 0) {
                    try {
                        payment.authorize(con,getDefines());
                    } catch (PaymentException pe) {
                        errorBean.addMessage(payment.getErrorMessage());
                    }
                }
            // of course we should never get here, but just incase.
            } catch (Exception e) {
                errorBean.addMessage("Error processing your request.  If this error continues please contact webmaster.");
                e.printStackTrace();
            }
        }

        if (action == ACTION_SALE) {
            BigDecimal captureAmountShipping = new BigDecimal(PiServlet.getReqString(req,"captureAmountShipping", "0.00"));
            BigDecimal captureAmountSubtotal = new BigDecimal(PiServlet.getReqString(req,"captureAmountSubtotal", "0.00"));
            BigDecimal captureAmountTax = new BigDecimal(PiServlet.getReqString(req,"captureAmountTax", "0.00"));
            String reference = PiServlet.getReqString(req,"pnref");

            BigDecimal captureAmount = captureAmountSubtotal.add(captureAmountShipping).add(captureAmountTax);

            Payment payment = new Payment(PiServlet.getReqString(req,"type"));
            payment.setStore(order.getStore());
            payment.setOrder(order);
            payment.setAccountNumber(PiServlet.getReqString(req,"accountNumber"));
            payment.setExpireMonth(PiServlet.getReqInt(req,"expireMonth"));
            payment.setExpireYear(PiServlet.getReqInt(req,"expireYear"));
            payment.setAmount(captureAmount);

            if (payment.getType().equalsIgnoreCase(Payment.TYPE_CC)) {
                /*
                if (payment.getCreditName().length() == 0) {
                    errorBean.addMessage("You must enter your name as it appears on your credit card.");
                    errorBean.addHighlightField("creditName");
                }
                */
                if (!payment.getAccountNumber().equalsIgnoreCase(Payment.CC_TEST_NUM)) {
                    if (!CCUtils.validCC(payment.getAccountNumber())) {
                        errorBean.addMessage("The credit card number you entered is invalid.  Please check the number and try again.");
                        errorBean.addHighlightField("accountNumber");
                    }
                }

                SimpleDateFormat sdf = new SimpleDateFormat("M/yyyy");
                Date dateExpire = null;
                try {
                    dateExpire = sdf.parse(payment.getExpireMonth() + "/" + payment.getExpireYear());
                } catch (Exception e) {}

                if (dateExpire == null) {
                    errorBean.addMessage("The credit card expiration you entered is invalid.");
                    errorBean.addHighlightField("expiration");
                    errorBean.addHighlightField("expireMonth");
                    errorBean.addHighlightField("expireYear");
                } else {
                    GregorianCalendar cal = new GregorianCalendar();
                    cal.setTime(dateExpire);
                    cal.set(Calendar.DAY_OF_MONTH,cal.getActualMaximum(Calendar.DAY_OF_MONTH));
                    if (cal.getTimeInMillis() < GregorianCalendar.getInstance().getTimeInMillis()) {
                        errorBean.addMessage("The credit card expiration you entered is invalid.");
                        errorBean.addHighlightField("expiration");
                        errorBean.addHighlightField("expireMonth");
                        errorBean.addHighlightField("expireYear");
                    }
                }
            }

            if (errorBean.getMessageCount() == 0 && captureAmount.compareTo(new BigDecimal("0.00")) > 0) {
                // run any authorization we might need to do.
                try {
                    payment.directSale(con, getDefines(), captureAmountSubtotal, captureAmountShipping, captureAmountTax, PiServlet.getReqString(req, "pnref"));
                    order.addPayment(payment);
                    order.reconcilePayments(con);
                } catch (PaymentException pe) {
                    errorBean.addMessage(payment.getErrorMessage());
                } catch (Exception e) {
                    errorBean.addMessage("ERROR: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            action = ACTION_EDIT;
        }

        if (action == ACTION_SHIP) {
            ShipmentType type = new ShipmentType(PiServlet.getReqInt(req, "shipTypeId"));
            Shipment shipment = new Shipment();
            shipment.setType(type);
            shipment.setTrackingNumber(PiServlet.getReqString(req,"trackingNumber"));
            shipment.setOrder(order);

            ArrayList variations = new ArrayList();
            ArrayList details = new ArrayList();
            boolean detailShipped = false;
            String ids[] = req.getParameterValues("lstOrderDetailId");
            if (ids == null || ids.length == 0) {
                errorBean.addMessage("You must choose one or more details to ship");
            } else {
                for (int i=0; i<ids.length; i++) {
                    try {
                        OrderDetail od = order.findDetail(Integer.parseInt(ids[i]));
                        if (od != null && od.getShipment() == null) {
                            if (od.getQtyInStock() < od.getQty()) {
                                errorBean.addMessage("You do not have enough QTY in stock to ship \"" + od.getDescription() + "\"");
                            } else {
                                details.add(od);

                                ProductVariation variation = od.getProductVariation();
                                Size size = od.getSize();
                                size.setQtyToAdd(0-od.getQty());
                                variation.addSize(size);
                                variations.add(variation);
                            }
                            detailShipped = true;
                        }
                    } catch (Exception e) {
                        // probably not major
                        errorBean.addMessage("Error: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
            if (errorBean.getMessageCount() == 0) {
                try {
                    shipment.saveToDb(con);
                    for (int i=0; i<details.size(); i++) {
                        OrderDetail od = (OrderDetail)details.get(i);
                        od.setShipment(shipment);
                        od.saveToDb(con);
                    }
                    for (int i=0; i<variations.size(); i++) {
                        ProductVariation var = (ProductVariation)variations.get(i);
                        var.saveSizesToDb(con);
                    }

                    if (order.getShipments().size() == 0) {
                        order.setDateShipBegan(new Date());
                    }
                    boolean lastShipment = true;
                    ArrayList dets = order.getDetails();
                    for (int i=0; i<dets.size(); i++) {
                        OrderDetail detail = (OrderDetail)dets.get(i);
                        if (detail.getShipment() == null) {
                            lastShipment = false;
                        }
                    }
                    if (lastShipment) {
                        order.setDateShipComplete(new Date());
                        order.setStatus(Order.STATUS_SHIPPED);
                    }
                    order.saveToDb(con);

                    // refresh the in stock counts for details in this order.
                    order.loadDetailsExtendedFromDb(con);
                } catch (Exception e) {
                    errorBean.addMessage("Error: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            action = ACTION_EDIT;
        }

        if (action == ACTION_EDIT) {
            try {
                ArrayList sizes = Size.loadAllSizes(con);
                req.setAttribute("sizes", sizes);

                ArrayList shiptypes = ShipmentType.getAllFromDb(con);
                req.setAttribute("shiptypes", shiptypes);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


		try {
		    con.close();
		} catch (Exception e) { e.printStackTrace(); }

		if (action == ACTION_LIST) {
		    req.getRequestDispatcher("/jsp/admin/order_list.jsp").forward(req, res);
		} else if (action == ACTION_EDIT) {
		    req.getRequestDispatcher("/jsp/admin/order_edit.jsp").forward(req, res);
		}
	}
}
