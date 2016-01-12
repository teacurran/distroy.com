package com.approachingpi.store.order;

import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.math.BigDecimal;

import java.util.Date;

/**
 * User: terrence
 * Date: Aug 27, 2004
 * Time: 12:49:31 PM
 */
public class Coupon {


	/*
	' Get $ or % off totals over
	if (intCouponType = 1) then

	'Buy x products get y products.
	elseif (intCouponType = 4) then

	' Get $ or % off Items. over $X
	elseif (intCouponType = 5) then

	' Get $ or % off Shipping.
	elseif (intCouponType = 6) then

	' Get $ or % off Specific Products
	elseif (intCouponType = 7) then

	*/

	int buy;
	int category;
	Date dateStart;
	Date dateEnd;
	String desc             = "";
	BigDecimal dollarOff;
	int get;
    int id;
	String name             = "";
	BigDecimal over;
	BigDecimal percentOff;
	int type;
	BigDecimal under;

	Coupon() {
	}
	Coupon(int id) {
		setId(id);
	}

	public int getBuy() { return buy; }
	public int getCategory() { return category; }
	public Date getDateEnd() { return dateEnd; }
	public Date getDateStart() { return dateStart; }
	public String getDesc() { return desc; }
	public BigDecimal getDollarOff() { return dollarOff; }
	public int getGet() { return get; }
	public int getId() { return id; }
	public String getName() { return name; }
	public BigDecimal getOver() { return over; }
	public BigDecimal getPercentOff() { return percentOff; }
	public int getType() { return type; }
	public BigDecimal getUnder() { return under; }

	public void loadFromDb(Connection con) throws SQLException {
		PreparedStatement ps = con.prepareStatement("SELECT * FROM tbCoupon WHERE inId = ?");
		ps.setInt(1, getId());
		ResultSet rs = ps.executeQuery();
		if (rs.next()) {
			loadFromRs(rs);
		} else {
			setId(0);
		}
		rs.close();
	}

	public void loadFromRs(ResultSet rs) throws SQLException {
		setName(rs.getString("vcName"));
        setType(rs.getInt("inType"));
		setDateStart(new Date(rs.getTimestamp("dtStart").getTime()));
		setDateEnd(new Date(rs.getTimestamp("dtEnd").getTime()));
		setPercentOff(rs.getBigDecimal("dePercentOff"));
		setDollarOff(rs.getBigDecimal("moDollarOff"));
		setOver(rs.getBigDecimal("moOver"));
		setUnder(rs.getBigDecimal("moUnder"));
		setBuy(rs.getInt("smBuy"));
		setGet(rs.getInt("smGet"));
		setCategory(rs.getInt("smCategory"));
		setDesc(rs.getString("txDesc"));
	}
	public void saveToDb(Connection con) throws SQLException {
		PreparedStatement ps;
		if (getId()>0) {
			ps = con.prepareStatement("UPDATE tbCoupon SET inName=?, vcType=?, dtStart=?, dtEnd=?, dePercentOff=?, moDollarOff=?, moOver=?, moUnder=?, smBuy=?, smGet=?, smCategory=?, txDesc=? WHERE inId=?");
		} else {
			ps = con.prepareStatement("INSERT INTO tbCoupon ("+
			"vcName, inType, dtStart, dtEnd, dePercentOff, moDollarOff,"+
			"moOver, moUnder, smBuy, smGet, smCategory,"+
			"txDesc"+
			") VALUES ("+
			"?,?,?,?,?,?,"+
			"?,?,?,?,?,"+
			"?)");
		}

		int i=0;
		ps.setString(++i, getName());
        ps.setInt(++i, getType());
		ps.setDate(++i, new java.sql.Date(getDateStart().getTime()));
		ps.setDate(++i, new java.sql.Date(getDateEnd().getTime()));
		ps.setBigDecimal(++i, getPercentOff());
		ps.setBigDecimal(++i, getDollarOff());

		ps.setBigDecimal(++i, getOver());
		ps.setBigDecimal(++i, getUnder());
		ps.setInt(++i, getBuy());
		ps.setInt(++i, getGet());
		ps.setInt(++i, getCategory());

		ps.setString(++i, getDesc());

		if (getId() > 0) {
			ps.setInt(++i, getId());
		}
		ps.execute();

		if (getId()==0) {
			ps = con.prepareStatement("SELECT MAX(inId) AS inMaxId FROM tbCoupon WHERE vcName=?");
			ps.setString(1, getName());
            ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				setId(rs.getInt("inMaxId"));
			}
			rs.close();
		}
	}

	public void setBuy(int in) { buy = in; }
	public void setCategory(int in) { category=in; }
	public void setDateEnd(Date in) { dateEnd = in; }
	public void setDateStart(Date in) { dateStart = in; }
	public void setDesc(String in) { desc=in; }
	public void setDollarOff(BigDecimal in) { dollarOff=in; }
	public void setGet(int in) { get=in; }
	public void setId(int in) { id = in; }
	public void setName(String in) { name = in; }
	public void setOver(BigDecimal in) { over=in; }
	public void setPercentOff(BigDecimal in) { percentOff=in; }
	public void setType(int in) { type = in; }
	public void setUnder(BigDecimal in) { under=in; }

}
