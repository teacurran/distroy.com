/*
 * Created by IntelliJ IDEA.
 * User: terrence
 * Date: Jul 18, 2004
 * Time: 4:39:13 AM
 * To change template for new class use
 * Code Style | Class Templates options (Tools | IDE Options).
 */
package com.approachingpi.store.servlet;

import com.approachingpi.servlet.PiServlet;
import com.approachingpi.servlet.Session;
import com.approachingpi.util.MessageBean;
import com.approachingpi.store.cart.*;
import com.approachingpi.store.catalog.*;
import com.approachingpi.user.*;
import com.approachingpi.store.Defines;
import com.approachingpi.store.site.Content;
import com.approachingpi.util.Mailer;
import com.approachingpi.util.PiUtility;
import com.approachingpi.store.order.*;
import com.approachingpi.store.order.Notification;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.Transport;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.Connection;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Date;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.text.SimpleDateFormat;

public class CartServlet extends PiServlet {
    public static final int ACTION_NONE         = -1;
    public static final int ACTION_VIEW         = 0;
    public static final int ACTION_ADD          = 1;
    public static final int ACTION_UPDATE       = 2;
	public static final int ACTION_DELETE       = 3;
    public static final int ACTION_CHECKOUT     = 4;
    public static final int ACTION_CONFIRM      = 5;
    public static final int ACTION_PROCESS      = 6;
    public static final int ACTION_BILLSHIP     = 7;
    public static final int ACTION_SHIPMETHOD   = 8;
	public static final int ACTION_CLAIMCOUPON  = 9;
	public static final int ACTION_PAYPALTHANKS = 10;

    public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
        doPost(req,res);
    }

    public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
        super.initPiServlet(req, res);

        Connection con = openConnection();
        PreparedStatement ps;
        ResultSet rs;

        MessageBean errorBean = this.getErrorBean(req);
        MessageBean messageBean = this.getMessageBean(req);

        int action = PiServlet.getReqInt(req, "action", ACTION_VIEW);
        int fromAction = PiServlet.getReqInt(req,"fromAction", ACTION_NONE);

        boolean debug = false;

        //System.out.println("Action:" + action + " : " + fromAction);

        Session session = getSession(req,res,con);
        User user = session.getUser();

        Cart cart = new Cart();
        cart.setSession(session);

        if (action == ACTION_CONFIRM || action == ACTION_PROCESS) {
            if (user.getId() == 0 && fromAction != ACTION_BILLSHIP) {
                action = ACTION_BILLSHIP;
            }
        }

        // The checkout action is a little generic, it determines what the next step
        // should be for this user based on the step the user is coming from.
        if (action == ACTION_CHECKOUT) {
            if (fromAction == ACTION_VIEW) {
                // we are using getBillId() here because it loads without loading all the addresses
                if (user.getId() > 0 && user.getBillId() > 0) {
                    action = ACTION_CONFIRM;
                } else {
                    action = ACTION_BILLSHIP;
                }
            }

        }

        if (action == ACTION_ADD) {
            addItem(req, con, cart);
            action = ACTION_VIEW;
        }

	    if (action == ACTION_DELETE) {
            deleteItem(req, con, cart);
            action = ACTION_VIEW;
	    }

        if (action == ACTION_UPDATE || fromAction == ACTION_VIEW) {
            updateItems(req, con, cart);
            if (action == ACTION_UPDATE) {
                action = ACTION_VIEW;
            }
        }

	    if (action == ACTION_CLAIMCOUPON) {
       	    this.addCoupon(req,con,cart);
		    action = ACTION_CONFIRM;
	    }

        if (action == ACTION_BILLSHIP || action == ACTION_CONFIRM || action == ACTION_PROCESS || action == ACTION_SHIPMETHOD || fromAction == ACTION_BILLSHIP) {
            try {
                user.loadAddressesFromDb(con);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

	    Address billing = user.getActiveBillingAddress();
	    Address shipping = user.getActiveShippingAddress();

		if (action == ACTION_SHIPMETHOD) {
			try {
				shipping.setShipMethod(new ShipMethod(PiServlet.getReqInt(req,"shipMethodId")));
				shipping.saveToDb(con);
			} catch (Exception e) {
				e.printStackTrace();
			}
			action = ACTION_CONFIRM;
		}

        System.out.println("Bill:" + billing.getId() + " Ship:" + shipping.getId());
	    if (billing.getId() == shipping.getId()) {
            System.out.println("Bill id and Ship id are the same");
	        shipping = new Address();
	    }
        System.out.println("Bill:" + billing.getId() + " Ship:" + shipping.getId());

		if (fromAction == ACTION_BILLSHIP) {
			boolean isNewUser = false;
			if (user.getId()==0) {
				isNewUser = true;
				user.setEmail(PiServlet.getReqString(req,"u_email"));
				user.setMailingList(PiServlet.getReqBoolean(req,"u_mailingList"));
				user.setPassword(PiServlet.getReqString(req,"u_password1"));
				// attempt to log the user in.
				// this could happen if someone tries to create a new account with an exact
				// username and password to one they already have.
				if (user.loadFromDbByLoggingIn(con)) {
					try {
						user.loadAddressesFromDb(con);
						billing = user.getActiveBillingAddress();
						shipping = user.getActiveShippingAddress();
						isNewUser = false;
					} catch (Exception e) {
						// this is generally not a failure, it just means the user didn't log in.
						// e.printStackTrace();
					}
				}
			}
			if (billing.getId() > 0 && billing.getId() == shipping.getId()) {
				shipping = new Address();
			}

			billing.setReference(PiServlet.getReqString(req,"b_reference"));
			billing.setNameFirst(PiServlet.getReqString(req,"b_nameFirst"));
			billing.setNameLast(PiServlet.getReqString(req,"b_nameLast"));
			billing.setAddress1(PiServlet.getReqString(req,"b_address1"));
			billing.setAddress2(PiServlet.getReqString(req,"b_address2"));
			billing.setCity(PiServlet.getReqString(req,"b_city"));
			billing.setState(new State(PiServlet.getReqString(req,"b_state")));
			billing.setZip(PiServlet.getReqString(req,"b_zip"));
			billing.setCountry(new Country(PiServlet.getReqInt(req,"b_country")));
			billing.setPhoneNumber(PiServlet.getReqString(req,"b_phoneNumber"));

			shipping.setReference(PiServlet.getReqString(req,"s_reference"));
			shipping.setNameFirst(PiServlet.getReqString(req,"s_nameFirst"));
			shipping.setNameLast(PiServlet.getReqString(req,"s_nameLast"));
			shipping.setAddress1(PiServlet.getReqString(req,"s_address1"));
			shipping.setAddress2(PiServlet.getReqString(req,"s_address2"));
			shipping.setCity(PiServlet.getReqString(req,"s_city"));
			shipping.setState(new State(PiServlet.getReqString(req,"s_state")));
			shipping.setZip(PiServlet.getReqString(req,"s_zip"));
			shipping.setCountry(new Country(PiServlet.getReqInt(req,"s_country")));
			shipping.setPhoneNumber(PiServlet.getReqString(req,"s_phoneNumber"));

			// countries are used later on
			try {
				billing.getCountry().loadFromDb(con);
				shipping.getCountry().loadFromDb(con);
			} catch (Exception e) {
				e.printStackTrace();
			}

			Defines defines = getDefines();
			if (billing.getNameFirst().length() == 0) {
				errorBean.addMessage(defines.getProperty("message.field.missing").replaceAll("#FIELD#","Billing First Name"));
				errorBean.addHighlightField("b_nameFirst");
			}
			if (billing.getNameLast().length() == 0) {
				errorBean.addMessage(defines.getProperty("message.field.missing").replaceAll("#FIELD#","Billing Last Name"));
				errorBean.addHighlightField("b_nameLast");
			}
			if (billing.getAddress1().length() == 0) {
				errorBean.addMessage(defines.getProperty("message.field.missing").replaceAll("#FIELD#","Billing Address 1"));
				errorBean.addHighlightField("b_address1");
			}
			if (billing.getCity().length() == 0) {
				errorBean.addMessage(defines.getProperty("message.field.missing").replaceAll("#FIELD#","Billing City"));
				errorBean.addHighlightField("b_city");
			}
			if (billing.getCountry().getCode().equalsIgnoreCase("US")) {
				if (billing.getState().getAbbrev().length() == 0) {
					errorBean.addMessage(defines.getProperty("message.field.missing").replaceAll("#FIELD#","Billing State"));
					errorBean.addHighlightField("b_state");
				}
				if (billing.getZip().length() == 0) {
					errorBean.addMessage(defines.getProperty("message.field.missing").replaceAll("#FIELD#","Billing Zip Code"));
					errorBean.addHighlightField("b_zip");
				}
			}
			if (billing.getCountry().getId() == 0) {
				errorBean.addMessage(defines.getProperty("message.field.missing").replaceAll("#FIELD#","Billing Country"));
				errorBean.addHighlightField("b_country");
			}
			if (billing.getPhoneNumber().length() == 0) {
				errorBean.addMessage(defines.getProperty("message.field.missing").replaceAll("#FIELD#","Billing Phone Number"));
				errorBean.addHighlightField("b_phoneNumber");
			}

			// if they entered no shipping data, delete the record for retail customers, set it to 0
			// for wholesale customers
			if (!shipping.getHasData()) {
			    if (!session.getIsWholesale()) {
			        try {
			            shipping.deleteFromDb(con);
			        } catch (Exception e) {
			            e.printStackTrace();
			        }
			    } else {
			        shipping.setId(0);
			    }
			} else {
				if (shipping.getNameFirst().length() == 0) {
					errorBean.addMessage(defines.getProperty("message.field.missing").replaceAll("#FIELD#","Shipping First Name"));
					errorBean.addHighlightField("s_nameFirst");
				}
				if (shipping.getNameLast().length() == 0) {
					errorBean.addMessage(defines.getProperty("message.field.missing").replaceAll("#FIELD#","Shipping Last Name"));
					errorBean.addHighlightField("s_nameLast");
				}
				if (shipping.getAddress1().length() == 0) {
					errorBean.addMessage(defines.getProperty("message.field.missing").replaceAll("#FIELD#","Shipping Address 1"));
					errorBean.addHighlightField("s_address1");
				}
				if (shipping.getCity().length() == 0) {
					errorBean.addMessage(defines.getProperty("message.field.missing").replaceAll("#FIELD#","Shipping City"));
					errorBean.addHighlightField("s_city");
				}
				if (shipping.getCountry().getCode().equalsIgnoreCase("US")) {
					if (billing.getState().getAbbrev().length() == 0) {
						errorBean.addMessage(defines.getProperty("message.field.missing").replaceAll("#FIELD#","Shipping State"));
						errorBean.addHighlightField("s_state");
					}
					if (shipping.getZip().length() == 0) {
						errorBean.addMessage(defines.getProperty("message.field.missing").replaceAll("#FIELD#","Shipping Zip Code"));
						errorBean.addHighlightField("s_zip");
					}
				}
				if (shipping.getCountry().getId() == 0) {
					errorBean.addMessage(defines.getProperty("message.field.missing").replaceAll("#FIELD#","Shipping Country"));
					errorBean.addHighlightField("s_country");
				}
				if (shipping.getPhoneNumber().length() == 0) {
					errorBean.addMessage(defines.getProperty("message.field.missing").replaceAll("#FIELD#","Shipping Phone Number"));
					errorBean.addHighlightField("s_phoneNumber");
				}
			}

			if (isNewUser) {
				if (user.getEmail().length() == 0) {
					errorBean.addMessage(defines.getProperty("message.field.missing").replaceAll("#FIELD#","Email Address"));
					errorBean.addHighlightField("u_email");
				} else if (!Mailer.isValidEmailAddress(user.getEmail())) {
					errorBean.addMessage(defines.getProperty("message.field.bademail"));
					errorBean.addHighlightField("u_email");
				}
				if (user.getPassword().length() == 0) {
					errorBean.addMessage(defines.getProperty("message.field.missing").replaceAll("#FIELD#","Password"));
					errorBean.addHighlightField("u_password1");
					errorBean.addHighlightField("u_password2");
				} else if (!PiServlet.getReqString(req,"u_password1").equals(PiServlet.getReqString(req,"u_password2"))) {
					errorBean.addMessage(defines.getProperty("message.field.passnomatch"));
					errorBean.addHighlightField("u_password1");
					errorBean.addHighlightField("u_password2");
				}
			}

			if (errorBean.getMessageCount() > 0) {
				action = ACTION_BILLSHIP;
			} else {
				if (isNewUser) {
					try {
						if (user.getType() == User.TYPE_PUBLIC_ANON) {
							user.setType(User.TYPE_PUBLIC);
						}
						user.saveToDb(con);
						// log this new user in.
						session.setUser(user);
						session.saveToDb(con);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				billing.setUser(user);
				shipping.setUser(user);
				try {
					billing.saveToDb(con);
					if (shipping.getHasData()) {
						shipping.saveToDb(con);
					}
					boolean userChanged = false;
					if (user.getBillId() != billing.getId()) {
						user.setBillId(billing.getId());
						userChanged = true;
					}
					if (user.getShipId() != shipping.getId()) {
						user.setShipId(shipping.getId());
						userChanged = true;
					}
					if (userChanged) {
						user.saveToDb(con);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}


        if (action == ACTION_VIEW || action == ACTION_CONFIRM || action == ACTION_PROCESS) {
            try {
                cart.loadCartItemsFromDb(con);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (cart.getItemCount()==0) {
                action = ACTION_VIEW;
            }
            if (action != ACTION_VIEW) {
                try {
                    cart.loadCoupons(con);
                    cart.loadShipMethods(con);
                    cart.calculateCoupons();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            req.setAttribute("cart", cart);
        }

	    if (action == ACTION_PROCESS) {
		    if (PiServlet.getReqString(req,"claimCode").length() > 0) {
			    this.addCoupon(req,con,cart);
                cart.calculateCoupons();
		    }
		    if (errorBean.getMessageCount() == 0) {
		        errorBean.merge(processOrder(req,con,cart));
		    }

		    if (errorBean.getMessageCount() > 0) {
			    action = ACTION_CONFIRM;
		    }
	    }


        try {
            con.close();
        } catch (Exception e) { e.printStackTrace(); }

        req.setAttribute("billing",billing);
	    req.setAttribute("shipping",shipping);
	    req.setAttribute("theUser",user);

        switch (action) {
            case ACTION_VIEW:
            req.getRequestDispatcher("/jsp/cart/view.jsp").forward(req, res);
            break;

            case ACTION_BILLSHIP:
            req.getRequestDispatcher("/jsp/cart/billship.jsp").forward(req, res);
            break;

            case ACTION_CONFIRM:
            req.getRequestDispatcher("/jsp/cart/confirm.jsp").forward(req, res);
            break;

			case ACTION_PROCESS:
		        if (PiServlet.getReqString(req,"paymentType").equalsIgnoreCase(Payment.TYPE_PAYPAL)) {
			        req.getRequestDispatcher("/jsp/cart/paypal_forward.jsp").forward(req, res);
		        } else {
					req.getRequestDispatcher("/jsp/cart/thankyou.jsp").forward(req, res);
		        }
			break;

            case ACTION_PAYPALTHANKS:
                Order order = new Order();
                order.setId(PiServlet.getReqString(req,"orderId"));
                req.setAttribute("order",order);
                req.getRequestDispatcher("/jsp/cart/thankyou.jsp").forward(req, res);
            break;
        }
    }

	private void addCoupon(HttpServletRequest req, Connection con, Cart cart) {
		CouponClaim claim = new CouponClaim(PiServlet.getReqString(req,"claimCode"));
		MessageBean errors = this.getErrorBean(req);
		try {
			claim.loadFromDbByCode(con);
			Coupon coupon = claim.getCoupon();
			coupon.loadFromDb(con);

			if (claim.getCoupon().getId() == 0) {
				errors.addMessage("The claim code you entered '" + claim.getClaimCode() + "' is not valid.");
			} else if (claim.expires() || claim.getUsedCount() > 0) {
				errors.addMessage("The claim code you entered '" + claim.getClaimCode() + "' has already been used.");
			} else if(coupon.getDateStart().getTime() > GregorianCalendar.getInstance().getTimeInMillis()) {
				errors.addMessage("The coupon code you entered is not valid until " + PiUtility.formatDate(coupon.getDateStart(), "M/d/yyyy"));
			} else if (coupon.getDateEnd().getTime() < GregorianCalendar.getInstance().getTimeInMillis()) {
				errors.addMessage("The coupon code you entered expired on " + PiUtility.formatDate(coupon.getDateEnd(), "M/d/yyyy"));
			} else {

				String sqlStatement = "INSERT INTO tbCartCoupon (\n"+
						"inSessionId, vcSessionCode, inUserId, inCouponClaimId, dtAdded\n"+
					") VALUES (\n"+
						"?,?,?,?,CURRENT_TIMESTAMP\n"+
					") ON DUPLICATE KEY UPDATE inSessionId = inSessionId";

				PreparedStatement ps = con.prepareStatement(sqlStatement);
				int i = 0;

				ps.setInt(++i, cart.getSession().getId());
				ps.setString(++i, cart.getSession().getSessionCode());
				ps.setInt(++i, cart.getSession().getUser().getId());
				ps.setInt(++i, claim.getId());

                ps.execute();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

    private void addItem(HttpServletRequest req, Connection con, Cart cart) {
        ProductVariation variation = new ProductVariation();
        variation.setId(PiServlet.getReqInt(req,"productVariationId"));

        Size size = new Size();
        size.setId(PiServlet.getReqInt(req,"sizeId"));

        int qty = PiServlet.getReqInt(req,"qty");
        if (qty <=0) {
            qty=1;
        }

        if (variation.getId() > 0 && size.getId() > 0) {
            CartItem item = new CartItem(cart);
            item.setProductVariation(variation);
            item.setSize(size);
            item.setQty(qty);
            try {
                item.saveToDb(con);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void deleteItem(HttpServletRequest req, Connection con, Cart cart) {
        CartItem item = new CartItem(cart);
        item.setId(PiServlet.getReqInt(req,"itemId"));
        item.setQty(0);

        try {
            item.saveToDb(con);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateItems(HttpServletRequest req, Connection con, Cart cart) {
        // TODO this can be done more efficiently
        Enumeration parameters = req.getParameterNames();
        while (parameters.hasMoreElements()) {
            String parameterName = (String)parameters.nextElement();
            if (parameterName.indexOf("item_") > -1) {
                CartItem item = new CartItem(cart);
                try {
                    item.setId(Integer.parseInt(parameterName.substring(5)));
                    item.loadFromDb(con);
                } catch (Exception e) {
                    item.setId(0);
                    e.printStackTrace();
                }
                item.setQty(PiServlet.getReqInt(req,parameterName,1));
                try {
                    item.saveToDb(con);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

	private MessageBean processOrder(HttpServletRequest req, Connection con, Cart cart) {
        MessageBean errors = new MessageBean();
        User user = cart.getSession().getUser();

		// set it up before charging the card to save processing later
		Order order = new Order();
		//order.setIpAddress(req.getAttribute("CLIENT_IP"));
        OrderComment comment = new OrderComment();
        comment.setBody(PiServlet.getReqString(req,"comments"));
        if (comment.getBody().length() > 0) {
            comment.setUser(user);
            comment.setOrder(order);
            order.addComment(comment);
        }
		//order.setComments(PiServlet.getReqString(req,"comments"));

        Payment payment = new Payment(PiServlet.getReqString(req,"paymentType",Payment.TYPE_CC));
        try {
            com.approachingpi.store.Store store = this.getStore(req);

            try {
                store.loadFromDbByAbbreviation(con);
            } catch (Exception e) {
                e.printStackTrace();
            }
            order.setStore(store);
            payment.setStore(store);
        } catch (Exception e) {e.printStackTrace();}
        payment.setUser(user);
		payment.setType(PiServlet.getReqString(req,"paymentType"));
		payment.setAccountNumber(PiServlet.getReqString(req,"accountNumber"));
		payment.setCreditName(PiServlet.getReqString(req,"creditName"));
		payment.setExpireMonth(PiServlet.getReqInt(req,"expireMonth"));
		payment.setExpireYear(PiServlet.getReqInt(req,"expireYear"));
		payment.setCcvNumber(PiServlet.getReqInt(req,"cvvNumber"));
		payment.setAmount(cart.getTotalPrice());

		if (payment.getType().equalsIgnoreCase(Payment.TYPE_CC)) {
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
		}


		// run any authorization we might need to do.
		// for credit cards this means putting a hold on the card.
		// some other types it just means putting a PENDING transaction
        try {
            if (errors.getMessageCount() == 0) {
                try {
                    payment.authorize(con,getDefines());
                } catch (PaymentException pe) {
                    errors.addMessage(payment.getErrorMessage());
                }
            }
        // of course we should never get here, but just incase.
        } catch (Exception e) {
            errors.addMessage("Error processing your request.  If this error continues please contact webmaster.");
            e.printStackTrace();
        }

		if (errors.getMessageCount() == 0) {
			try {
				order.addPayment(payment);
				order.writeOrderToDb(con,cart);
				req.setAttribute("order", order);

                com.approachingpi.store.order.Notification notification = null;
                if (payment.getType().equalsIgnoreCase(Payment.TYPE_CC)) {
                    notification = new com.approachingpi.store.order.Notification(order,getDefines(),com.approachingpi.store.order.Notification.TYPE_COMPLETE);
                } else if (payment.getType().equalsIgnoreCase(Payment.TYPE_CHECK)) {
                    notification = new com.approachingpi.store.order.Notification(order,getDefines(),com.approachingpi.store.order.Notification.TYPE_CHECK_PENDING);
                }
                // send the email in a seperate thread so it won't delay us if the mail server is busy.
                if (notification != null) {
                    Thread emailThread = new Thread(notification);
                    emailThread.setPriority(emailThread.getPriority() - 1);
                    emailThread.run();
                }
			} catch (Exception e) {
				// THIS IS SERIOUS IF WE GET HERE
				// TODO write something that sends an email if this happens
				e.printStackTrace();
			}
		}

        return errors;
	}
}
