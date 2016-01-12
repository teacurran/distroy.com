package com.approachingpi.store.order;

import java.util.Date;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * User: terrence
 * Date: Sep 6, 2004
 * Time: 5:54:34 AM
 */
public class CouponClaim {

	String claimCode        = "";
	int claimCount;
	Coupon coupon;
	Date dateClaimed;
	Date dateUsed;
	Date dateExpire;
	boolean expire;
	int id;
	int usedCount;

	public CouponClaim() { }
	public CouponClaim(String claimCode) {
		setClaimCode(claimCode);
	}

	public String getClaimCode() { return claimCode; }
    public int getClaimCount() { return claimCount; }
	public Coupon getCoupon() {
		if (coupon==null) {
			coupon = new Coupon();
		}
		return coupon;
	}
	public Date getDateClaimed() { return dateClaimed; }
	public Date getDateUsed() { return dateUsed; }
	public Date getDateExpire() { return dateExpire; }
	public int getId() { return id; }
	public int getUsedCount() { return usedCount; }
	public boolean expires() { return expire; }

	public void loadFromDb(Connection con) throws SQLException {
		if (getId()==0) {
			return;
		}
		PreparedStatement ps = con.prepareStatement("SELECT * FROM tbCouponClaim WHERE inId=?");
		ps.setInt(1,getId());
		ResultSet rs = ps.executeQuery();
		if (rs.next()) {
			loadFromRs(rs);
		}
		rs.close();
	}
	public void loadFromDbByCode(Connection con) throws SQLException {
		if (getClaimCode().length()==0) {
			return;
		}
		PreparedStatement ps = con.prepareStatement("SELECT * FROM tbCouponClaim WHERE vcClaimCode=?");
		ps.setString(1,getClaimCode());
		ResultSet rs = ps.executeQuery();
		if (rs.next()) {
			setId(rs.getInt("inId"));
			loadFromRs(rs);
		}
		rs.close();
	}

	public void loadFromRs(ResultSet rs) throws SQLException {

		Coupon newCoupon = new Coupon(rs.getInt("inCouponId"));
		if (getCoupon()==null || getCoupon().getId()!=newCoupon.getId()) {
			setCoupon(newCoupon);
		}
		setClaimCode(rs.getString("vcClaimCode"));
        try {
            setDateClaimed(new Date(rs.getTimestamp("dtClaimed").getTime()));
        } catch (Exception e) {}
        try {
    		setDateUsed(new Date(rs.getTimestamp("dtUsed").getTime()));
        } catch (Exception e) {}
        try {
    		setDateExpire(new Date(rs.getTimestamp("dtExpire").getTime()));
        } catch (Exception e) {}
        setClaimCount(rs.getInt("inClaimCount"));
		setUsedCount(rs.getInt("inUsedCount"));
	}


	public void setClaimCode(String in) { claimCode=(in==null) ? "" : in; }
	public void setClaimCount(int in) { claimCount = in; }
	public void setCoupon(Coupon in) { coupon=in; }
	public void setDateClaimed(Date in) { dateClaimed=in; }
	public void setDateUsed(Date in) { dateUsed=in; }
	public void setDateExpire(Date in) { dateExpire=in; }
	public void setExpires(boolean in) { expire=in; }
	public void setId(int in) { id=in; }
	public void setUsedCount(int in) { usedCount=in; }


}
