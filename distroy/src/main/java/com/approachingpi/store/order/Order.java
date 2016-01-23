package com.approachingpi.store.order;

import com.approachingpi.store.Store;
import com.approachingpi.store.cart.Cart;
import com.approachingpi.store.cart.CartItem;
import com.approachingpi.store.cart.ShipMethod;
import com.approachingpi.user.User;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;

/**
 * User: terrence
 * Date: Aug 24, 2004
 * Time: 8:06:50 PM
 */
public class Order implements Serializable {
    public static final int STATUS_PENDING                  = 0;
    public static final int STATUS_INCOMPLETE               = 1;
    public static final int STATUS_WAITING_PAYMENT          = 2;
    public static final int STATUS_SHIPPED                  = 3;
    public static final int STATUS_PARTIAL_SHIP             = 4;
    public static final int STATUS_DELETED                  = 5;
    public static final int STATUS_MIN                      = 0;
    public static final int STATUS_MAX                      = 5;

    public static final int[] STATUS_TYPES = {STATUS_PENDING, STATUS_INCOMPLETE, STATUS_WAITING_PAYMENT, STATUS_PARTIAL_SHIP, STATUS_SHIPPED, STATUS_DELETED};

    private static final char[] saltChars = ("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray());

    protected User          affiliate;
    protected BigDecimal    amountCouponOrder;
    protected BigDecimal    amountCouponProduct;
    protected BigDecimal    amountCouponShipping;
    protected BigDecimal    amountShipping;
    protected BigDecimal    amountSubtotal;
    protected BigDecimal    amountTax;
    protected BigDecimal    amountTotal;
    protected BigDecimal    amountCapturedSubtotal;
    protected BigDecimal    amountCapturedShipping;
    protected BigDecimal    amountCapturedTax;
    protected BigDecimal    amountCapturedTotal;
    protected OrderAddress  billAddress;
    protected String        comment;
    protected ArrayList     comments;
    protected CouponClaim   couponClaimShip;
    protected CouponClaim   couponClaimOrder;
    protected Date          dateCreated;
    protected Date          dateModified;
    protected Date          dateShipBegan;
    protected Date          dateShipComplete;
    protected ArrayList     details         = new ArrayList();
    protected String        id              = "";
    protected String        ipAddress;
    protected ArrayList     payments        = new ArrayList();
    protected String        po  = "";
    protected OrderAddress  shipAddress;
    protected int           shipCount;
    protected ArrayList     shipments      = new ArrayList();
    protected ShipMethod    shipMethod;
    protected int           status;
    protected Store         store;
    protected float         taxRate;
    protected Date          timestamp;
    protected User          user;

    public Order(String id) {
        setId(id);
    }
    public Order() {
    }

    public void addComment(OrderComment in) {
        getComments().add(in);
    }
	public void addDetail(OrderDetail in) {
		if (in != null) {
			details.add(in);
		}
	}
	public void addPayment(Payment in) {
		if (in != null) {
			payments.add(in);
		}
	}
    public void addShipment(Shipment in) {
        if (in == null) {
            return;
        }
        shipments.add(in);
        for (int i=0; i<details.size(); i++) {
            OrderDetail detail = (OrderDetail)details.get(i);
            if (in.equals(detail.getShipment())) {
                detail.setShipment(in);
            }
        }
    }
    public OrderDetail findDetail(int findId) {
        for (int i=0; i<details.size(); i++) {
            OrderDetail detail = (OrderDetail)details.get(i);
            if (detail.getId() == findId) {
                return detail;
            }
        }
        return null;
    }
    public Shipment findShipment(int findId) {
        for (int i=0; i<shipments.size(); i++) {
            Shipment shipment = (Shipment)shipments.get(i);
            if (shipment.getId() == findId) {
                return shipment;
            }
        }
        return null;
    }
	public User getAffiliate() {
		if (affiliate==null) {
			affiliate = new User();
		}
		return affiliate;
	}
    public BigDecimal getAmountCapturedSubtotal() {
        if (amountCapturedSubtotal==null) { amountCapturedSubtotal=new BigDecimal("0.00"); }
        return amountCapturedSubtotal.setScale(2,BigDecimal.ROUND_HALF_EVEN);
    }
    public BigDecimal getAmountCapturedShipping() {
        if (amountCapturedShipping==null) { amountCapturedShipping=new BigDecimal("0.00"); }
        return amountCapturedShipping.setScale(2,BigDecimal.ROUND_HALF_EVEN);
    }
    public BigDecimal getAmountCapturedTax() {
        if (amountCapturedTax==null) { amountCapturedTax=new BigDecimal("0.00"); }
        return amountCapturedTax.setScale(2,BigDecimal.ROUND_HALF_EVEN);
    }
    public BigDecimal getAmountCapturedTotal() {
        if (amountCapturedTotal==null) { amountCapturedTotal=new BigDecimal("0.00"); }
        return amountCapturedTotal.setScale(2,BigDecimal.ROUND_HALF_EVEN);
    }
	public BigDecimal getAmountCouponOrder() {
		if (amountCouponOrder==null) { amountCouponOrder=new BigDecimal("0.00"); }
		return amountCouponOrder.setScale(2,BigDecimal.ROUND_HALF_EVEN);
	}
	public BigDecimal getAmountCouponProduct() {
		if (amountCouponProduct==null) { amountCouponProduct=new BigDecimal("0.00"); }
		return amountCouponProduct.setScale(2,BigDecimal.ROUND_HALF_EVEN);
	}
	public BigDecimal getAmountCouponShipping() {
		if (amountCouponShipping==null) { amountCouponShipping=new BigDecimal("0.00"); }
		return amountCouponShipping.setScale(2,BigDecimal.ROUND_HALF_EVEN);
	}
    public BigDecimal getAmountCouponTotal() {
        BigDecimal couponTotal = new BigDecimal("0.00");
        couponTotal = couponTotal.add(getAmountCouponOrder());
        couponTotal = couponTotal.add(getAmountCouponProduct());
        couponTotal = couponTotal.add(getAmountCouponShipping());
        return couponTotal.setScale(2,BigDecimal.ROUND_HALF_EVEN);
    }
	public BigDecimal getAmountShipping() {
		if (amountShipping==null) { amountShipping=new BigDecimal("0.00"); }
		return amountShipping.setScale(2,BigDecimal.ROUND_HALF_EVEN);
	}
	public BigDecimal getAmountSubtotal() {
		if (amountSubtotal==null) { amountSubtotal=new BigDecimal("0.00"); }
		return amountSubtotal.setScale(2,BigDecimal.ROUND_HALF_EVEN);
	}
	public BigDecimal getAmountTax() {
		if (amountTax==null) { amountTax=new BigDecimal("0.00"); }
		return amountTax.setScale(2,BigDecimal.ROUND_HALF_EVEN);
	}
	public BigDecimal getAmountTotal() {
		if (amountTotal==null) { amountTotal=new BigDecimal("0.00"); }
		return amountTotal.setScale(2,BigDecimal.ROUND_HALF_EVEN);
	}
	public BigDecimal getAmountTotalDue() {
		BigDecimal totalDue = getAmountTotal().subtract(getAmountCapturedTotal());
        if (totalDue.compareTo(new BigDecimal("0.00")) < 0) {
            totalDue = new BigDecimal("0.00");
        }
        return totalDue.setScale(2,BigDecimal.ROUND_HALF_EVEN);
	}
	public OrderAddress getBillAddress() {
        if (billAddress == null) {
            billAddress = new OrderAddress();
        }
        return billAddress;
    }
	public String getComment() {
            return comment;
            //return com.approachingpi.store.order.OrderComment.getBody2();
        }

    public ArrayList getComments() {
        if (comments == null) {
            comments = new ArrayList();
        }
        /*
        PreparedStatement ps2 = prepareStatement("SELECT * FROM tbOrderComment WHERE vcOrderId = ?");
        ps2.setString(1,getId());
        ResultSet rs2 = ps2.executeQuery();
        comments = new ArrayList();
        return comments;*/

        return comments;
        //Order.loadCommentsFromDb(con);
    }
	public Date getDateCreated() { return dateCreated; }
	public Date getDateModified() { return dateModified; }
    public Date getDateShipBegan() { return dateShipBegan; }
    public Date getDateShipComplete() { return dateShipComplete; }
    public ArrayList getDetails() { return details; }
    public ArrayList getDetailsGrouped() {
        ArrayList grouped = new ArrayList();
        for (int i=0; i<details.size(); i++) {
            OrderDetail thisDetail = (OrderDetail)details.get(i);

            boolean detailFound = false;
            for (int x=0; x<grouped.size(); x++) {
                OrderDetail groupedDetail = (OrderDetail)grouped.get(x);
                if (groupedDetail.getProductVariation().equals(thisDetail.getProductVariation())) {
                    groupedDetail.addDetailSize(new OrderDetailSize(thisDetail.getQty(), thisDetail.getSize()));
                    groupedDetail.setPriceTotal(groupedDetail.getPriceTotal().add(thisDetail.getPriceTotal()));
                    detailFound = true;
                    break;
                }
            }
            if (!detailFound) {
                OrderDetailSize detailSize = new OrderDetailSize(thisDetail.getQty(), thisDetail.getSize());
                thisDetail.setQty(0);
                thisDetail.setSize(null);
                thisDetail.addDetailSize(detailSize);
                grouped.add(thisDetail);
            }
        }
        return grouped;
    }

	public String getId() { return id; }
	public String getIpAddress() { return ipAddress; }
    public ArrayList getPayments() { return payments; }
    public String getPo() {
        return po;
    }
	public OrderAddress getShipAddress() {
        if (shipAddress == null) {
            shipAddress = new OrderAddress();
        }
    return shipAddress;
    }
    public int getShipCount() { return shipCount; }
	public ShipMethod getShipMethod() { return shipMethod; }
    public ArrayList getShipments() {
        return shipments;
    }
    public int getStatus() { return status; }
    public static String getStatusById(int status) {
        switch(status) {
            case STATUS_PENDING:
                return "pending";
            case STATUS_INCOMPLETE:
                return "incomplete";
            case STATUS_WAITING_PAYMENT:
                return "waiting for payment";
            case STATUS_SHIPPED:
                return "shipped";
            case STATUS_PARTIAL_SHIP:
                return "partial ship";
            case STATUS_DELETED:
                return "deleted";
            default:
                return "unknown";
        }
    }
	public Store getStore() {
		if (store == null) {
			store = new Store("");
		}
		return store;
	}
	public float getTaxRate() { return taxRate; }
	public Date getTimestamp() {
		return timestamp;
	}

	public User getUser() {
        if (user==null) {
            user = new User();
        }
		return user;
	}

    public void loadFromDb(Connection con) throws SQLException {
		PreparedStatement ps = con.prepareStatement("SELECT * FROM tbOrder WHERE vcId = ?");
		ps.setString(1,getId());
		ResultSet rs = ps.executeQuery();
		if (rs.next()) {
			loadFromRs(rs);
		}
		rs.close();
		this.getBillAddress().loadFromDb(con);
		this.getShipAddress().loadFromDb(con);
	}

    public void loadCommentsFromDb(Connection con) throws SQLException {
        PreparedStatement ps = con.prepareStatement("SELECT * FROM tbOrderComment WHERE vcOrderId = ?");
        ps.setString(1,getId());
        ResultSet rs = ps.executeQuery();
        comments = new ArrayList();
        while (rs.next()) {
            OrderComment thisComment = new OrderComment(rs.getInt("inId"));
            thisComment.loadFromRs(rs);
            getComments().add(thisComment);
        }
        rs.close();
    }

    // COMMENTING OUT THIS SECTION TO TRY TO LOAD THE COMMENTS FROM THE ORDER TABLE IN prev BLOCK
    /*
        public void loadCommentsFromDb(Connection con) throws SQLException {
        PreparedStatement ps = con.prepareStatement("SELECT * FROM tbOrder WHERE vcId = ?");
        ps.setString(1,getId());
        ResultSet rs = ps.executeQuery();
        comments = new ArrayList();
        while (rs.next()) {
       //     Order thisComment = new Order(rs.getInt("inId"));
         //   thisComment.loadFromRs(rs);
        }
        rs.close();
    }
    */
    public void loadExtendedFromDb(Connection con) throws SQLException {
        this.getBillAddress().loadFromDb(con);
        this.getShipAddress().loadFromDb(con);
        this.getUser().loadFromDb(con);
        this.getStore().loadFromDb(con);
        if (this.getShipMethod() != null) {
            this.getShipMethod().loadFromDb(con);
        }
        this.loadDetailsFromDb(con);
        this.loadDetailsExtendedFromDb(con);
        this.loadPaymentsFromDb(con);
        this.loadCommentsFromDb(con);
        this.loadShipmentsFromDb(con);
    }
    public void loadDetailsFromDb(Connection con) throws SQLException {
        // TODO replace this with a query that incorperates the Product Variation
		PreparedStatement ps = con.prepareStatement("SELECT * FROM tbOrderDetail WHERE vcOrderId = ?");
		ps.setString(1,getId());
		ResultSet rs = ps.executeQuery();
        details = new ArrayList();
		while (rs.next()) {
            OrderDetail detail = new OrderDetail(this,rs.getInt("inId"));
            detail.loadFromRs(rs);
            details.add(detail);
        }
        rs.close();
    }
    public void loadDetailsExtendedFromDb(Connection con) throws SQLException {
        for (int i=0; i<details.size(); i++) {
            OrderDetail detail = (OrderDetail)details.get(i);
            detail.getProductVariation().loadFromDb(con);
            detail.loadInStockFromDb(con);
        }
    }
	public void loadFromRs(ResultSet rs) throws SQLException {
		setStore(new Store(rs.getInt("inStoreId")));
		setUser(new User(rs.getInt("inUserId")));
        setStatus(rs.getInt("inStatus"));
		this.setBillAddress(new OrderAddress(rs.getInt("inBillId")));
		this.setShipAddress(new OrderAddress(rs.getInt("inShipId")));
		this.setShipMethod(new ShipMethod(rs.getInt("inShipMethodId")));
		this.setAffiliate(new User(rs.getInt("inAffiliateId")));
        this.setPo(rs.getString("vcPO"));
        try {
        	this.setDateCreated(new Date(rs.getTimestamp("dtCreated").getTime()));
        } catch (Exception e) {}
        try {
    		this.setDateModified(new Date(rs.getTimestamp("dtCreated").getTime()));
        } catch (Exception e) {}
		this.setAmountSubtotal(rs.getBigDecimal("moSubtotal"));
		this.setAmountTotal(rs.getBigDecimal("moTotal"));
		this.setAmountShipping(rs.getBigDecimal("moTotal_Ship"));
		this.setAmountTax(rs.getBigDecimal("moTotal_Tax"));
		this.setAmountCouponProduct(rs.getBigDecimal("moTotal_Coupon_Product"));
		this.setAmountCouponOrder(rs.getBigDecimal("moTotal_Coupon_Order"));
		this.setAmountCouponShipping(rs.getBigDecimal("moTotal_Coupon_Ship"));
        this.setAmountCapturedSubtotal(rs.getBigDecimal("moCharged_Subtotal"));
        this.setAmountCapturedShipping(rs.getBigDecimal("moCharged_Ship"));
        this.setAmountCapturedTax(rs.getBigDecimal("moCharged_Tax"));
        this.setAmountCapturedTotal(rs.getBigDecimal("moCharged_Total"));
        this.setShipCount(rs.getInt("inShipCount"));
        try {
            this.setDateShipBegan(new Date(rs.getTimestamp("dtShipBegan").getTime()));
        } catch (Exception e) {}
        try {
            this.setDateShipComplete(new Date(rs.getTimestamp("dtShipComplete").getTime()));
        } catch (Exception e) {}
        // TODO may be out of order
        this.setComment(rs.getString("txComments"));
		this.setIpAddress(rs.getString("vcIpAddress"));
	}

    public void loadIdFromDb(Connection con) throws SQLException {
		if (getId().length() > 0) {
			return;
		}

	    // generate a random value to ensure that this order id is unique
	    java.util.Random randomGenerator = new java.util.Random();
	    StringBuffer randomValueBuffer = new StringBuffer();
	    for (int i=0; i <10; i++) {
	        randomValueBuffer.append(saltChars[Math.abs(randomGenerator.nextInt()) % saltChars.length]);
	    }
	    String randomValue = randomValueBuffer.toString();


	    int newOrderId = 0;

		String sqlStatement = "INSERT INTO tbIdOrder (inId, inStoreId, inUserId, vcRandom) "+
				"SELECT Max(inId)+1, "+
				"?,?,? " +
				"FROM tbIdOrder WHERE inStoreId=?";
	    PreparedStatement ps = con.prepareStatement(sqlStatement);

		int i = 0;

		ps.setInt(++i, getStore().getId());
		ps.setInt(++i, getUser().getId());
		ps.setString(++i, randomValue);
		ps.setInt(++i, getStore().getId());

		ps.execute();

		ps = con.prepareStatement("SELECT Max(inId) AS inMaxId\n" +
			"FROM tbIdOrder\n" +
			"WHERE inStoreId=?\n" +
			"AND inUserId=?\n" +
			"AND vcRandom=?");
		ps.setInt(1, getStore().getId());
		ps.setInt(2, getUser().getId());
		ps.setString(3, randomValue);
		ResultSet rs = ps.executeQuery();
		if (rs.next()) {
			newOrderId = rs.getInt("inMaxId");
		}
		rs.close();

 	    this.setId(getStore().getAbbreviation()+user.getId() + "-" + newOrderId);
    }
    public void loadPaymentsFromDb(Connection con) throws SQLException {
        PreparedStatement ps = con.prepareStatement("SELECT * FROM tbPayment WHERE vcOrderId = ?");
        ps.setString(1,getId());
        ResultSet rs = ps.executeQuery();
        payments = new ArrayList();
        while (rs.next()){
            Payment payment = new Payment(rs.getInt("inId"));
            payment.loadFromRs(rs);
            payment.loadTransactionsFromDb(con);
            this.addPayment(payment);
        }
        rs.close();
    }
    public void loadShipmentsFromDb(Connection con) throws SQLException {
        PreparedStatement ps = con.prepareStatement("SELECT * FROM tbShipment WHERE vcOrderId = ? ORDER BY dtCreated");
        ps.setString(1,getId());
        ResultSet rs = ps.executeQuery();
        shipments = new ArrayList();
        while (rs.next()){
            Shipment shipment = new Shipment(rs.getInt("inId"));
            shipment.setOrder(this);
            shipment.loadFromRs(rs);
            this.addShipment(shipment);
        }
        rs.close();
    }
    public void reconcilePayments(Connection con) throws SQLException {
        this.loadFromDb(con);
        loadPaymentsFromDb(con);
        BigDecimal amountCapturedSubtotal = new BigDecimal("0.00");
        BigDecimal amountCapturedTax = new BigDecimal("0.00");
        BigDecimal amountCapturedShipping = new BigDecimal("0.00");
        for (int i=0; i<payments.size(); i++) {
            Payment thisPayment = (Payment)payments.get(i);
            amountCapturedSubtotal = amountCapturedSubtotal.add(thisPayment.getAmountSettledSubtotal());
            amountCapturedShipping = amountCapturedShipping.add(thisPayment.getAmountSettledShipping());
            amountCapturedTax = amountCapturedTax.add(thisPayment.getAmountSettledTax());
        }
        this.setAmountCapturedSubtotal(amountCapturedSubtotal);
        this.setAmountCapturedShipping(amountCapturedShipping);
        this.setAmountCapturedTax(amountCapturedTax);
        this.setAmountCapturedTotal(amountCapturedSubtotal.add(amountCapturedShipping).add(amountCapturedTax));
        this.saveToDb(con);
    }
	public void saveToDb(Connection con) throws SQLException {
		if (getId().length() == 0) {
			loadIdFromDb(con);
		}

		StringBuilder sql = new StringBuilder(5000);

		sql.append("INSERT INTO tbOrder (");
			sql.append("vcId, inStoreId, inUserId, inStatus, inBillId, ");
			sql.append("inShipId, inGiftId, inShipMethodId, inAffiliateId, vcPO, dtCreated, ");
			sql.append("dtModified, moSubtotal, moTotal, moTotal_Ship, moTotal_Tax, ");
			sql.append("moTotal_Coupon_Product, moTotal_Coupon_Order, moTotal_Coupon_Ship, inShipCount, dtShipBegan, ");
			sql.append("dtShipComplete, txComments, vcIpAddress, ftTax_Rate, inOrder_Coupon, ");
			sql.append("inShip_Coupon");
		sql.append(") VALUES (");
			sql.append("?,?,?,?,?,");
			sql.append("?,null,?,?,?,CURRENT_TIMESTAMP,");
			sql.append("CURRENT_TIMESTAMP,?,?,?,?,");
			sql.append("?,?,?,0,null,");
			sql.append("null,?,?,?,?,");
			sql.append("?");
		sql.append(")\n");
		sql.append("ON DUPLICATE KEY UPDATE\n");
			sql.append("inStatus=?, ");
			sql.append("inGiftId=null, ");
			sql.append("inShipMethodId=?, ");
			//sql.append("inAffiliateId=?, ");
			sql.append("vcPO=?,");
			sql.append("dtModified=CURRENT_TIMESTAMP, ");
			sql.append("moSubtotal=?, ");
			sql.append("moTotal=?, ");
			sql.append("moTotal_Ship=?, ");
			sql.append("moTotal_Tax=?, ");
			sql.append("moTotal_Coupon_Product=?, ");
			sql.append("moTotal_Coupon_Order=?, ");
			sql.append("moTotal_Coupon_Ship=?, ");

			sql.append("moCharged_Subtotal=?, ");
			sql.append("moCharged_Ship=?, ");
			sql.append("moCharged_Tax=?, ");
			sql.append("moCharged_Total=?, ");

			//sql.append("inShipCount=?, ");
			sql.append("dtShipBegan=?, ");
			sql.append("dtShipComplete=?, ");
			//sql.append("vcIpAddress=?, ");
			sql.append("ftTax_Rate=?, ");
			sql.append("inOrder_Coupon=?, ");
			sql.append("inShip_Coupon=? ");
			//sql.append("btOrder_Complete=?, ");
			//sql.append("btDeleted=? \n");

        PreparedStatement ps = con.prepareStatement(sql.toString());
		int i=0;

		// INSERT
		ps.setString(++i, this.getId());
		ps.setInt(++i, store.getId());
		ps.setInt(++i, user.getId());
		ps.setInt(++i, getStatus());
		if (getBillAddress() == null) {
			ps.setNull(++i, Types.INTEGER);
		} else {
			ps.setInt(++i, this.getBillAddress().getId());
		}
		if (getShipAddress() == null) {
			ps.setNull(++i, Types.INTEGER);
		} else {
			ps.setInt(++i, this.getShipAddress().getId());
		}

		if (this.getShipMethod() == null) {
			ps.setNull(++i, Types.INTEGER);
		} else {
			ps.setInt(++i, this.getShipMethod().getId());
		}
		ps.setInt(++i, this.getAffiliate().getId());
		ps.setString(++i, this.getPo());
		ps.setBigDecimal(++i, this.getAmountSubtotal());

		ps.setBigDecimal(++i,this.getAmountTotal());
		ps.setBigDecimal(++i,this.getAmountShipping());
		ps.setBigDecimal(++i,this.getAmountTax());
		ps.setBigDecimal(++i,this.getAmountCouponProduct());
		ps.setBigDecimal(++i,this.getAmountCouponOrder());

		ps.setBigDecimal(++i,this.getAmountCouponShipping());

		//ps.setString(++i,this.getComment());
		ps.setString(++i, this.getComment());
		System.out.println(this.getComment());
		System.out.println(this.getComments());
		//System.out.println(com.approachingpi.store.order.OrderComment.getBody());

		ps.setString(++i, this.getIpAddress());
		System.out.println(this.getIpAddress());
		ps.setFloat(++i, this.getTaxRate());
		if (couponClaimOrder == null) {
			ps.setNull(++i, Types.INTEGER); // Coupon Order
		} else {
			ps.setInt(++i, couponClaimOrder.getId());
		}
		if (couponClaimShip == null) {
			ps.setNull(++i, Types.INTEGER); // Coupon Shipping
		} else {
			ps.setInt(++i, couponClaimShip.getId());
		}

		// UPDATE
		ps.setInt(++i, this.getStatus());
		if (this.getShipMethod() == null) {
			ps.setNull(++i, Types.INTEGER);
		} else {
			ps.setInt(++i, this.getShipMethod().getId());
		}
		ps.setString(++i, this.getPo());
		ps.setBigDecimal(++i, this.getAmountSubtotal());
		ps.setBigDecimal(++i, this.getAmountTotal());
		ps.setBigDecimal(++i, this.getAmountShipping());
		ps.setBigDecimal(++i, this.getAmountTax());
		ps.setBigDecimal(++i, this.getAmountCouponProduct());
		ps.setBigDecimal(++i, this.getAmountCouponOrder());
		ps.setBigDecimal(++i, this.getAmountCouponShipping());

		ps.setBigDecimal(++i, this.getAmountCapturedSubtotal());
		ps.setBigDecimal(++i, this.getAmountCapturedShipping());
		ps.setBigDecimal(++i, this.getAmountCapturedTax());
		ps.setBigDecimal(++i, this.getAmountCapturedTotal());

		if (getDateShipBegan() == null) {
			ps.setNull(++i, Types.TIMESTAMP);
		} else {
			ps.setTimestamp(++i, new Timestamp(getDateShipBegan().getTime()));
		}
		if (getDateShipComplete() == null) {
			ps.setNull(++i, Types.TIMESTAMP);
		} else {
			ps.setTimestamp(++i, new Timestamp(getDateShipComplete().getTime()));
		}
		ps.setFloat(++i, this.getTaxRate());
		if (couponClaimOrder == null) {
			ps.setNull(++i, Types.INTEGER); // Coupon Order
		} else {
			ps.setInt(++i, couponClaimOrder.getId());
		}
		if (couponClaimShip == null) {
			ps.setNull(++i, Types.INTEGER); // Coupon Shipping
		} else {
			ps.setInt(++i, couponClaimShip.getId());
		}

		ps.execute();

	}

	public void setAffiliate(User in) {
		if (in!=null && in.getId() != getUser().getId()) {
			affiliate = in;
		}
	}
	public void setAmountCapturedSubtotal(BigDecimal in) { amountCapturedSubtotal = in; }
	public void setAmountCapturedShipping(BigDecimal in) { amountCapturedShipping = in; }
	public void setAmountCapturedTax(BigDecimal in) { amountCapturedTax = in; }
	public void setAmountCapturedTotal(BigDecimal in) { amountCapturedTotal = in; }
    public void setAmountCouponOrder(BigDecimal in) { amountCouponOrder = in; }
	public void setAmountCouponProduct(BigDecimal in) { amountCouponProduct = in; }
	public void setAmountCouponShipping(BigDecimal in) { amountCouponShipping = in; }
	public void setAmountShipping(BigDecimal in) { amountShipping = in; }
	public void setAmountSubtotal(BigDecimal in) { amountSubtotal = in; }
	public void setAmountTax(BigDecimal in) { amountTax = in; }
	public void setAmountTotal(BigDecimal in) { amountTotal = in; }
	public void setBillAddress(OrderAddress billAddress) {
		this.billAddress = billAddress;
	}
	public void setComment(String in) { comment = in; }
	public void setDateCreated(Date in) { dateCreated = in; }
	public void setDateModified(Date in) { dateModified = in; }
	public void setDateShipBegan(Date in) { dateShipBegan = in; }
	public void setDateShipComplete(Date in) { dateShipComplete = in; }
	public void setDetails(ArrayList details) {
		this.details = details;
	}
	public void setId(String in) {
		id = (in == null)? "" : in;
	}
	public void setIpAddress(String in) {
		ipAddress = (in==null) ? "" : in;
	}
    public void setPo(String in) {
        po = (in==null) ? "" : in;
    }
	public void setShipAddress(OrderAddress shipAddress) {
		this.shipAddress = shipAddress;
	}
    public void setShipCount(int in) {
        this.shipCount = in;
    }
	public void setShipMethod(ShipMethod shipMethod) {
		this.shipMethod = shipMethod;
	}
    public void setStatus(int in) { this.status = in; }
	public void setStore(Store in) {
		this.store = in;
	}
	public void setTaxRate(float in) { taxRate = in; }
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
	public void setUser(User user) {
		this.user = user;
	}
	public void writeOrderToDb(Connection con, Cart cart) throws Exception {
		User user = cart.getSession().getUser();
		setUser(user);
        loadIdFromDb(con);

		setBillAddress(new OrderAddress(user.getActiveBillingAddress()));
		getBillAddress().setOrder(this);
		getBillAddress().saveToDb(con);
		if (user.getActiveShippingAddress().getId() != user.getActiveBillingAddress().getId()) {
			setShipAddress(new OrderAddress(user.getActiveShippingAddress()));
			getShipAddress().setOrder(this);
			getShipAddress().saveToDb(con);
		}
		setAmountSubtotal(cart.getSubtotalPrice());
		setAmountShipping(cart.getShipPrice());
		setAmountTax(cart.getTaxPrice());
        setAmountCouponProduct(cart.getCouponDiscountProducts());
        setAmountCouponShipping(cart.getCouponDiscountShipping());
        setAmountCouponOrder(cart.getCouponDiscountOrder());
		setAmountTotal(cart.getTotalPrice());
		setShipMethod(cart.getActiveShipMethod());
        this.couponClaimShip = cart.getCouponClaimShip();
        this.couponClaimOrder = cart.getCouponClaimOrder();

		Store store = cart.getSession().getStore();
		store.loadFromDbByAbbreviation(con);
		setStore(store);

        saveToDb(con);

		for (int i=0; i<payments.size(); i++) {
			Payment p = (Payment)payments.get(i);
			p.setOrder(this);
			p.saveToDb(con);
		}

		ArrayList items = cart.getCartItems();
		for (int i=0; i<items.size(); i++) {
			CartItem item = (CartItem)items.get(i);
            OrderDetail newDetail = new OrderDetail(this, item);
			try {
				newDetail.saveToDb(con);
			} catch (Exception e) {
				System.err.println("ERROR WRITING CART ITEM:ITEM=" + item.getProductVariation().getId() + " ORDER=" + this.getId() + " SESSION=" + cart.getSession().getSessionCode() + " USER=" + cart.getSession().getUser().getId());
				e.printStackTrace();
			}
            this.addDetail(newDetail);
		}
        for (int i2=0; i2<comments.size(); i2++) {
            OrderComment thisComment = (OrderComment)comments.get(i2);

            try {
                thisComment.saveToDb(con);
            } catch (Exception e) {
                System.err.println("ERROR WRITING ORDER COMMENT:COMMENT=" + thisComment.getBody() + " ORDER=" + this.getId() + " SESSION=" + cart.getSession().getSessionCode() + " USER=" + cart.getSession().getUser().getId());
                e.printStackTrace();
            }
        }
            for (int i2=0; i2<comments.size(); i2++) {
            OrderComment thisComment = (OrderComment)comments.get(i2);

            try {
                        // test from here
                StringBuffer sql = new StringBuffer(5000);
                sql.append("BEGIN\n");
                sql.append("UPDATE tbOrder SET\n");
                sql.append("txComments=? ");
                sql.append("WHERE vcId = ? AND inUserId=? \n");
                sql.append("END");

                PreparedStatement ps = con.prepareStatement(sql.toString());
                int i=0;

                ps.setString(++i2, thisComment.getBody());

                //WHERE
            ps.setString(++i2, this.getId());
            ps.setInt(++i2, this.getUser().getId());
            ps.execute();

// to here
            } catch (Exception e) {
                System.err.println("ERROR WRITING ORDER COMMENT:COMMENT=" + thisComment.getBody() + " ORDER=" + this.getId() + " SESSION=" + cart.getSession().getSessionCode() + " USER=" + cart.getSession().getUser().getId());
                e.printStackTrace();
            }


        }
		try {
			cart.clearCart(con);
		} catch (Exception e) {
			e.printStackTrace();
		}

		cart = new Cart();
	}
}
