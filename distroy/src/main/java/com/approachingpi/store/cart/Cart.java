/*
 * Cart.java
 *
 * Created on August 7, 2004, 3:05 PM
 *
 * @author  Terrence
 *
 */

package com.approachingpi.store.cart;

import java.sql.*;
import java.util.ArrayList;
import java.math.*;
import com.approachingpi.servlet.Session;
import com.approachingpi.store.cart.CartItem;
import com.approachingpi.store.cart.ShipMethod;
import com.approachingpi.store.order.CouponClaim;
import com.approachingpi.store.order.Coupon;
import com.approachingpi.PiObject;
import com.approachingpi.store.catalog.*;

public class Cart extends PiObject {
    ArrayList cartItems = new ArrayList();
	ArrayList couponClaims = new ArrayList();
    Session session;
    BigDecimal subtotal = new BigDecimal("0");
    // BigDecimal subtotalPriceRetail = new BigDecimal("0");
    // BigDecimal subtotalPriceWholesale = new BigDecimal("0");
	BigDecimal couponDiscountOrder = new BigDecimal("0");
	BigDecimal couponDiscountShipping = new BigDecimal("0");
	BigDecimal couponDiscountProducts = new BigDecimal("0");
    int itemCount = 0;
    ArrayList shipMethods = new ArrayList();
    CouponClaim couponClaimOrder;
    CouponClaim couponClaimShip;

    /** Creates a new instance of Cart */
    public Cart() {

    }

    public void addCartItem(CartItem item) {
        cartItems.add(item);
        item.setCart(this);

        subtotal = subtotal.add(item.getPriceTotal());
        //subtotalPriceRetail = subtotalPriceRetail.add(item.getPriceTotalRetail());
        //subtotalPriceWholesale = subtotalPriceWholesale.add(item.getPriceTotalWholesale());
        itemCount += item.getQty();
    }
	public void addCoupon(CouponClaim claim) {
		if (claim != null || claim.getCoupon() != null || claim.getCoupon().getId() != 0) {
			couponClaims.add(claim);
		}
	}
	public void clearCart(Connection con) throws SQLException {
		PreparedStatement ps = con.prepareStatement("DELETE FROM tbCart WHERE inSessionId=? AND vcSessionCode=?");
		ps.setInt(1,getSession().getId());
		ps.setString(2,getSession().getSessionCode());
		ps.execute();

        ps = con.prepareStatement("DELETE FROM tbCartCoupon WHERE inSessionId=? AND vcSessionCode=?");
		ps.setInt(1,getSession().getId());
		ps.setString(2,getSession().getSessionCode());
		ps.execute();

		cartItems = new ArrayList();
		couponClaims = new ArrayList();
        subtotal = new BigDecimal("0");
		/*
        subtotalPriceRetail = new BigDecimal("0");
		subtotalPriceWholesale = new BigDecimal("0");
        */
		couponDiscountOrder = new BigDecimal("0");
		itemCount = 0;
	}

	public void calculateCoupons() {
        this.couponDiscountOrder = new BigDecimal("0.00");

		for (int i=0; i<couponClaims.size(); i++) {
           	CouponClaim claim = (CouponClaim)couponClaims.get(i);
			Coupon coupon = claim.getCoupon();

			// Get $ or % off totals over
			if (coupon.getType() == 1) {
				if (this.getSubtotalPrice().compareTo(new BigDecimal("0.00")) > 0) {
					if (coupon.getPercentOff().compareTo(new BigDecimal("0.00")) > 0) {
						// couponDiscount = round_even(subtotal * percent_off / 100)
                    	couponDiscountOrder = this.getSubtotalPrice().multiply(coupon.getPercentOff()).divide(new BigDecimal("100"),BigDecimal.ROUND_HALF_EVEN);
                    } else if (coupon.getDollarOff().compareTo(new BigDecimal("0.00")) > 0) {
						couponDiscountOrder = coupon.getDollarOff();
					}
                    couponClaimOrder = claim;
				}
			// Get $ or % off Items. over $X
			} else if (coupon.getType() == 2) {
                for (int x=0; x<this.cartItems.size(); x++) {
	                CartItem item = (CartItem)cartItems.get(x);
	                if (item.getPriceItem().compareTo(coupon.getOver()) >= 0) {
		                BigDecimal dollarOff = null;
		                if (coupon.getPercentOff().compareTo(new BigDecimal("0.00")) > 0) {
			                dollarOff = item.getPriceItem().multiply(coupon.getPercentOff()).divide(new BigDecimal("100"),BigDecimal.ROUND_HALF_EVEN);
		                } else if (coupon.getDollarOff().compareTo(new BigDecimal("0.00")) > 0) {
							dollarOff = coupon.getDollarOff();
		                }
		                for (int qtyIndex=0; qtyIndex<item.getQty(); qtyIndex++) {
			                item.applyCoupon(qtyIndex,coupon.getId(),dollarOff);
                            // TODO
                            // SUBTRACT FROM SUBTOTAL PRICE
		                }
	                }
                }
			}
		}
	}

    public ShipMethod getActiveShipMethod() {
        //System.out.println("address:" + session.getUser().getActiveShippingAddress().getId());
        ShipMethod usersMethod = session.getUser().getActiveShippingAddress().getShipMethod();
        for (int i=0; i<shipMethods.size(); i++) {
            ShipMethod thisMethod = (ShipMethod)shipMethods.get(i);
            if (thisMethod.getId() == usersMethod.getId()) {
                return thisMethod;
            }
        }

        // if we can't determine the ship method from the user, pick the first one we have.
        if (shipMethods.size() > 0) {
            return (ShipMethod)shipMethods.get(0);
        }
        return new ShipMethod();
    }
    public ArrayList getCartItems() {
        return cartItems;
    }
    public ArrayList getCouponClaims() {
        return couponClaims;
    }
    public CouponClaim getCouponClaimShip() {
        return couponClaimShip;
    }
    public CouponClaim getCouponClaimOrder() {
        return couponClaimOrder;
    }
    public int getItemCount() {
        return itemCount;
    }
    public BigDecimal getCouponDiscountShipping() {
        return this.couponDiscountShipping;
    }
    public BigDecimal getCouponDiscountProducts() {
        return this.couponDiscountProducts;
    }
    public BigDecimal getCouponDiscountOrder() {
        return this.couponDiscountOrder;
    }
    public BigDecimal getCouponDiscountTotal() {
        BigDecimal couponDiscountTotal = new BigDecimal("0.00");
        couponDiscountTotal = couponDiscountTotal.add(this.couponDiscountOrder);
        couponDiscountTotal = couponDiscountTotal.add(this.couponDiscountProducts);
        return couponDiscountTotal;
    }
    public Session getSession() {
        return session;
    }
    public ArrayList getShipMethods() {
        return shipMethods;
    }
    public BigDecimal getShipPrice(ShipMethod shipMethod) {
        return shipMethod.getFirstPrice().calculate(getSubtotalPrice(),itemCount,0);
    }
    public BigDecimal getShipPrice() {
        return getActiveShipMethod().getFirstPrice().calculate(getSubtotalPrice(),itemCount,0);
    }
    public BigDecimal getSubtotalPrice() {
        return subtotal;
    }
    public BigDecimal getTotalPrice() {
        BigDecimal totalPrice = subtotal;
        totalPrice = totalPrice.add(getShipPrice());
        totalPrice = totalPrice.subtract(getCouponDiscountTotal());
        totalPrice = totalPrice.add(getTaxPrice());
        return totalPrice;
    }

    // TODO implement this method.  nothing is taxable right now so we are good for a little while
    public BigDecimal getTaxPrice() {
        return new BigDecimal("0.00");
    }

	public void loadCoupons(Connection con) throws SQLException {
		PreparedStatement ps = con.prepareStatement("SELECT * FROM tbCouponClaim WHERE inId IN (SELECT inCouponClaimId FROM tbCartCoupon WHERE inSessionId=? AND vcSessionCode=?)");
        ps.setInt(1,getSession().getId());
        ps.setString(2,getSession().getSessionCode());
		ResultSet rs = ps.executeQuery();
		while (rs.next()) {
			CouponClaim claim = new CouponClaim();
			claim.setId(rs.getInt("inId"));
			claim.loadFromRs(rs);
			claim.getCoupon().loadFromDb(con);
			this.addCoupon(claim);
		}
		rs.close();
	}

    public void loadShipMethods(Connection con) {
        shipMethods = ShipMethod.getAvailableShipMethods(con, this, session);
    }

    public void loadCartItemsFromDb(Connection con) throws Exception {
        if (getSession()==null || getSession().getId() == 0) {
            return;
        }

        // TODO speed this up by making an uber statement that loads all of the products, variations, and brands so we don't have to do all
        // these subselects
        PreparedStatement ps = con.prepareStatement("SELECT * FROM tbCart WHERE inSessionId=? AND vcSessionCode=?");
        ps.setInt(1,getSession().getId());
        ps.setString(2,getSession().getSessionCode());
        ResultSet rs = ps.executeQuery();
        cartItems = new ArrayList();
        while (rs.next()) {
            CartItem item = new CartItem(this);
            item.setId(rs.getInt("inId"));
            item.loadFromRs(rs);
            item.getProductVariation().loadFromDb(con);
            // it is possible that the id would be 0 if the product was deleted
            if (item.getProductVariation().getId() > 0) {
                item.getSize().loadFromDb(con);
                item.getProductVariation().loadSizes(con);
                item.getProductVariation().getProduct().loadFromDb(con);
                ProductVariation variation = item.getProductVariation();
                variation.loadImagesFromDb(con, 1, Image.SQUARE);

                item.getProductVariation().getProduct().getBrand().loadFromDb(con);
                addCartItem(item);
            }
        }
        rs.close();
    }

    public void setSession(Session in) {
        session = in;
    }


}
