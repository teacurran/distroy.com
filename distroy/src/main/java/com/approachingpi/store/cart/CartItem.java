/*
 * CartItem.java
 *
 * Created on August 7, 2004, 3:07 PM
 *
 * @author  Terrence
 *
 */

package com.approachingpi.store.cart;

import java.util.Date;
import java.util.ArrayList;
import java.math.BigDecimal;
import java.sql.*;
import com.approachingpi.servlet.Session;
import com.approachingpi.store.catalog.ProductVariation;
import com.approachingpi.store.catalog.Size;

public class CartItem {
    Cart cart;
    Date dateAdded;
    int id;
    ProductVariation productVariation;
    Size size;
    int qty;
	ArrayList couponsApplied = new ArrayList();
	ArrayList couponsAppliedAmounts = new ArrayList();

    /** Creates a new instance of CartItem */
    public CartItem() {
    }
    public CartItem(Cart in) {
        setCart(in);
    }


    public void applyCoupon(int qtyIndex, int couponId, BigDecimal amount) {
	    if (couponId==0 || amount == null || amount.equals(new BigDecimal("0.00"))) {
		    return;
	    }
	    if (couponsApplied.size()>qtyIndex) {
		    couponsApplied.set(qtyIndex, new Integer(couponId));
		    couponsAppliedAmounts.set(qtyIndex, amount);
	    } else {
		    while(couponsApplied.size()<qtyIndex) {
			    couponsApplied.add(new Integer(0));
			    couponsAppliedAmounts.add(new BigDecimal("0.00"));
		    }
		    couponsApplied.add(new Integer(couponId));
		    couponsAppliedAmounts.add(amount);
	    }
    }

    public Cart getCart() {
		if (cart==null) {
            cart = new Cart();
        }
        return cart;
    }
    public Date getDateAdded() {
        return dateAdded;
    }
    public int getId() {
        return id;
    }
	public BigDecimal getPriceItem() {
        return productVariation.getPrice(cart.getSession().getStore());
	}
	public BigDecimal getPriceTotal() {
		return getPriceItem().multiply(new BigDecimal(new Integer(getQty()).toString()));
	}
    public ProductVariation getProductVariation() {
        return productVariation;
    }
    public Size getSize() {
        return size;
    }
    public int getQty() {
        return qty;
    }

	public void deleteFromDb(Connection con) throws SQLException {
		PreparedStatement ps = con.prepareStatement("DELETE FROM tbCart WHERE inId=? AND inSessionId=? AND vcSessionCode=?");
		ps.setInt(1, getId());
		ps.setInt(2,getCart().getSession().getId());
		ps.setString(3,getCart().getSession().getSessionCode());
		ps.execute();
	}

    public void loadFromDb(Connection con) throws SQLException {
        PreparedStatement ps = con.prepareStatement("SELECT * FROM tbCart WHERE inId=? AND inSessionId=? AND vcSessionCode=?");
        ps.setInt(1, getId());
        ps.setInt(2,getCart().getSession().getId());
        ps.setString(3,getCart().getSession().getSessionCode());

        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            loadFromRs(rs);
        }
        rs.close();
    }

    public void loadFromRs(ResultSet rs) throws SQLException {
        this.setProductVariation(new ProductVariation(rs.getInt("inProductVariationId")));
        this.setSize(new Size(rs.getInt("inSizeId")));
        this.setQty(rs.getInt("inQty"));
        this.setDateAdded(new Date(rs.getTimestamp("dtAdded").getTime()));
    }

    public void saveToDb(Connection con) throws Exception {
		if (getCart().getSession()==null || getCart().getSession().getId() == 0) {
            throw new Exception ("Session cannot be null");
        }

	    // if we have an id, and the qty is 0, delete from the database
	    if (getProductVariation()==null || getQty()==0 && getId()>0) {
		    PreparedStatement ps = con.prepareStatement("DELETE FROM tbCart WHERE inSessionId=? AND vcSessionCode=? AND inId=?");
		    ps.setInt(1,getCart().getSession().getId());
		    ps.setString(2,getCart().getSession().getSessionCode());
		    ps.setInt(3, getId());
		    ps.execute();
		    setId(0);
		    return;
		// if we don't have an id, but the qty is 0, don't do anything.
	    } else if (getProductVariation()==null || getQty()==0) {
		    return;
	    }

        if (getId() == 0) {
            PreparedStatement ps = con.prepareStatement("SELECT MAX(inId) AS inMaxId FROM tbCart WHERE inSessionId=? AND vcSessionCode=?");
            ps.setInt(1,getCart().getSession().getId());
            ps.setString(2,getCart().getSession().getSessionCode());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                setId(rs.getInt("inMaxId")+1);
            }
            rs.close();
        }

        PreparedStatement ps = con.prepareStatement("IF ((SELECT COUNT(*) FROM tbCart WHERE inId=? AND inSessionId=? AND vcSessionCode=?)>0) "+
            "BEGIN "+
            "UPDATE tbCart SET inProductVariationId=?, inSizeId=?, inQty=? WHERE "+
            "inId=? AND inSessionId=? AND vcSessionCode=? "+
            "END ELSE BEGIN "+
            "INSERT into tbCart (inId, inSessionId, vcSessionCode, inProductVariationId, inSizeId, inQty, dtAdded) VALUES (?,?,?,?,?,?,CURRENT_TIMESTAMP) "+
            "END");
        // IF
        int i=0;
        ps.setInt(++i,getId());
        ps.setInt(++i,getCart().getSession().getId());
        ps.setString(++i,getCart().getSession().getSessionCode());

        // UPDATE
        ps.setInt(++i,getProductVariation().getId());
        ps.setInt(++i,getSize().getId());
        ps.setInt(++i,getQty());
        ps.setInt(++i,getId());
        ps.setInt(++i,getCart().getSession().getId());
        ps.setString(++i,getCart().getSession().getSessionCode());

        // INSERT
        ps.setInt(++i,getId());
        ps.setInt(++i,getCart().getSession().getId());
        ps.setString(++i,getCart().getSession().getSessionCode());
        ps.setInt(++i,getProductVariation().getId());
        ps.setInt(++i,getSize().getId());
        ps.setInt(++i,getQty());

        ps.execute();
    }

    public void setCart(Cart in) {
        this.cart = in;
    }
    public void setDateAdded(Date in) {
        this.dateAdded = in;
    }
    public void setId(int in) {
        this.id = in;
    }
    public void setProductVariation(ProductVariation in) {
        this.productVariation = in;
    }
    public void setSize(Size in) {
        this.size = in;
    }
    public void setQty(int in) {
        this.qty = in;
    }
}
