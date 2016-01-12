package com.approachingpi.store.order;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.math.BigDecimal;
import java.util.Date;

/**
 * User: terrence
 * Date: Aug 26, 2004
 * Time: 12:22:26 PM
 */
public class PaymentTransaction {
	public static final int TYPE_UNKNOWN  = 0;    // THIS BETTER NOT EVER BE A CASE
	public static final int TYPE_HOLD     = 1;
	public static final int TYPE_CAPTURE  = 2;
	public static final int TYPE_REFUND   = 3;
	public static final int TYPE_PENDING  = 4;
	public static final int TYPE_SALE     = 5;

	private static final char[] saltChars = ("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray());

    BigDecimal amount           = new BigDecimal("0.00");
	String authCode             = "";
	String avsAddress           = "";
    String avsI                 = "";
	String avsZip               = "";
    String cvv2Match            = "";
    Date dateCreated;
	int id;
	Payment payment;
	String pnref                = "";
	String responseMessage      = "";
	int result;

	int type;

    public BigDecimal getAmount() { return amount; }
	public String getAuthCode() { return authCode; }
	public String getAvsAddress() { return avsAddress; }
    public String getAvsI() { return avsI; }
	public String getAvsZip() { return avsZip; }
    public String getCvv2Match() { return cvv2Match; }
    public Date getDateCreated() {
        return dateCreated;
    }
	public int getId() { return id; }
	public Payment getPayment() {
		if (payment == null) {
			payment = new Payment();
		}
		return payment;
	}
	public String getPnRef() { return pnref; }
	public String getResponseMessage() { return responseMessage; }
	public int getResult() { return result; }
	public int getType() { return type; }
    public static String getTypeById(int id) {
        switch (id) {
            case TYPE_HOLD:
                return "hold";
            case TYPE_CAPTURE:
                return "capture";
            case TYPE_REFUND:
                return "refund";
            case TYPE_PENDING:
                return "pending";
            case TYPE_SALE:
                return "sale";
			default:
                return "unknown";
        }
    }

	public void loadFromDb(Connection con) throws SQLException {
		if (getId() == 0) {
			return;
		}
		PreparedStatement ps = con.prepareStatement("SELECT * FROM tbPaymentTransaction WHERE inId=?");
		ps.setInt(1,getId());
		ResultSet rs = ps.executeQuery();
		if (rs.next()) {
			loadFromRs(rs);
		}
		rs.close();
	}

	public void loadFromRs(ResultSet rs) throws SQLException {
		if (this.getPayment().getId() == 0) {
			setPayment(new Payment(rs.getInt("inPaymentId")));
		}
		setType(rs.getInt("inType"));
        setAmount(rs.getBigDecimal("moAmount"));
		setResult(rs.getInt("inResult"));
		setAuthCode(rs.getString("vcAuthCode"));
		setAvsAddress(rs.getString("vcAvsAddress"));
		setAvsI(rs.getString("vcAvsI"));
		setAvsZip(rs.getString("vcAvsZip"));
        setCvv2Match(rs.getString("vcCvv2Match"));
		setPnref(rs.getString("vcPnRef"));
		setResponseMessage(rs.getString("vcResponseMessage"));
        setDateCreated(new Date(rs.getTimestamp("dtCreated").getTime()));
	}

	public void loadIdFromDb(Connection con) throws SQLException {
		if (getId() > 0) {
			return;
		}

		// generate a random value to ensure that this order id is unique
		java.util.Random randomGenerator = new java.util.Random();
		StringBuffer randomValueBuffer = new StringBuffer();
		for (int i=0; i <10; i++) {
		    randomValueBuffer.append(saltChars[Math.abs(randomGenerator.nextInt()) % saltChars.length]);
		}
		String randomValue = randomValueBuffer.toString();


		int newId = 0;

		PreparedStatement ps = con.prepareStatement("INSERT INTO tbIdTransaction (inStoreId, inUserId, vcRandom) VALUES (?,?,?)");
		ps.setInt(1, getPayment().getStore().getId());
		ps.setInt(2, getPayment().getUser().getId());
		ps.setString(3, randomValue);
		ps.execute();

		// we don't even need MAX() here because it is nearly impossible for there to be more than one, but we'll just be safe.
		ps = con.prepareStatement("SELECT Max(inId) AS inMaxId FROM tbIdTransaction WHERE inStoreId=? AND inUserId=? AND vcRandom=?");
		ps.setInt(1, getPayment().getStore().getId());
		ps.setInt(2, getPayment().getUser().getId());
		ps.setString(3, randomValue);
		ResultSet rs = ps.executeQuery();
		if (rs.next()) {
			newId = rs.getInt("inMaxId");
		}
		rs.close();

		setId(newId);
	}
	public void saveToDb(Connection con) throws SQLException {
        if (getId()<=0) {
            this.loadIdFromDb(con);
        }

        String sql = "IF ((SELECT Count(*) FROM tbPaymentTransaction WHERE inId=?) > 0)\n"+
        "BEGIN\n"+
            "UPDATE tbPaymentTransaction SET inPaymentId=?, inType=?, moAmount=?, inResult=?, vcAuthCode=?, "+
            "vcAvsAddress=?, vcAvsI=?, vcAvsZip=?, vcCvv2Match=?, vcPnRef=?, "+
            "vcResponseMessage=? "+
            "WHERE inId=?\n"+
        "END ELSE BEGIN\n"+
            "INSERT INTO tbPaymentTransaction (inId, inPaymentId, inType, moAmount, inResult, "+
                "vcAuthCode, vcAvsAddress, vcAvsI, vcAvsZip, vcCvv2Match, "+
                "vcPnRef, vcResponseMessage) " +
                "VALUES ("+
                    "?,?,?,?,?,"+
                    "?,?,?,?,?,"+
                    "?,?"+
                ")\n"+
        "END";
        //System.out.println(sql);
        PreparedStatement ps = con.prepareStatement(sql);

        int i=0;
        // IF
        ps.setInt(++i, getId());

        // UPDATE
        ps.setInt(++i, getPayment().getId());
        ps.setInt(++i, getType());
        ps.setBigDecimal(++i, getAmount());
        ps.setInt(++i,getResult());
        ps.setString(++i, getAuthCode());

        ps.setString(++i, getAvsAddress());
        ps.setString(++i, getAvsI());
        ps.setString(++i,getAvsZip());
        ps.setString(++i, getCvv2Match());
        ps.setString(++i, getPnRef());

        ps.setString(++i, getResponseMessage());
        // WHERE
        ps.setInt(++i, getId());

        // INSERT
        ps.setInt(++i, getId());
        ps.setInt(++i, getPayment().getId());
        ps.setInt(++i, getType());
        ps.setBigDecimal(++i, getAmount());
        ps.setInt(++i,getResult());

        ps.setString(++i, getAuthCode());
        ps.setString(++i, getAvsAddress());
        ps.setString(++i, getAvsI());
        ps.setString(++i,getAvsZip());
        ps.setString(++i, getCvv2Match());

        ps.setString(++i, getPnRef());
        ps.setString(++i, getResponseMessage());

        ps.execute();
	}
    public void setAmount(BigDecimal in) { amount = in; }
	public void setAuthCode(String in) { authCode = (in==null) ? "" : in; }
	public void setAvsAddress(String in) { avsAddress = (in==null) ? "" : in; }
	public void setAvsI(String in) { avsI = (in==null) ? "" : in; }
	public void setAvsZip(String in) { avsZip = (in==null) ? "" : in; }
    public void setCvv2Match(String in) { cvv2Match = (in==null) ? "" : in; }
    public void setDateCreated(Date in) {
        dateCreated = in;
    }
    public void setId(int in) { id = in; }
	public void setPnref(String in) { pnref = (in==null) ? "" : in; }
	public void setResponseMessage(String in) { responseMessage = (in==null) ? "" : in; }
	public void setResult(int in) { result = in; }
	public void setType(int in) {
		type = in;
	}
	public void setPayment(Payment in) {
		payment = in;
	}


}
