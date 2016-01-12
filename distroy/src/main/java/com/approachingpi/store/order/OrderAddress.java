package com.approachingpi.store.order;

import com.approachingpi.user.Address;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * User: terrence
 * Date: Aug 24, 2004
 * Time: 8:07:49 PM
 */
public class OrderAddress extends Address {
	Order order;

    public OrderAddress() {
    }
    
	public OrderAddress(int id) {
		setId(id);
	}
	public OrderAddress(Address in) {
		this.setAddress1(in.getAddress1());
		this.setAddress2(in.getAddress2());
		this.setCity(in.getCity());
		this.setCountry(in.getCountry());
		this.setNameFirst(in.getNameFirst());
		this.setNameLast(in.getNameLast());
        this.setTitle(in.getTitle());
        this.setFaxNumber(in.getFaxNumber());
		this.setPhoneNumber(in.getPhoneNumber());
		this.setState(in.getState());
		this.setUser(in.getUser());
		this.setZip(in.getZip());
	}

	public Order getOrder() {
		if (order==null) {
			order = new Order();
		}
		return order;
	}

	// load methods
	public void loadFromDb(Connection con) throws SQLException {
	    if (getId() <= 0) {
	        return;
	    }
	    try {
	        PreparedStatement ps = con.prepareStatement("SELECT * FROM tbOrderAddress WHERE inId = ?");
	        ps.setInt(1,this.getId());
	        ResultSet rs = ps.executeQuery();
	        if (rs.next()) {
	            loadFromRs(rs);
	        } else {
	            int id = this.getId();
	            this.setId(0);
	            throw new Exception("Order Address Id " + id + " not found");
	        }
	        rs.close();
            this.getCountry().loadFromDb(con);
	    } catch (Exception e) {
	        //System.err.println(e.toString() + " - " + e.getMessage());
	        e.printStackTrace();
	    }
	}
    
    

	public void saveToDb(Connection con) throws SQLException {
	    String sql;
	    if (getId() > 0) {
	        sql = "UPDATE tbOrderAddress SET vcReference=?, vcNameFirst=?, vcNameLast=?, vcTitle=?, vcAddress1=?, vcAddress2=?, vcCity=?, vcState=?, vcZip=?, inCountryId=?, vcPhoneNumber=?, vcFaxNumber=?, inUserId=? WHERE vcOrderId=? AND inId=?";
	    } else {
	        sql = "INSERT INTO tbOrderAddress (vcReference, vcNameFirst, vcNameLast, vcTitle, vcAddress1, vcAddress2, vcCity, vcState, vcZip, inCountryId, vcPhoneNumber, vcFaxNumber, inUserId, vcOrderId) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
	    }
        
        System.out.println(sql);
        
	    PreparedStatement ps = con.prepareStatement(sql);
	    int i=0;
	    ps.setString(++i, getReference());
	    ps.setString(++i, getNameFirst());
	    ps.setString(++i, getNameLast());
        ps.setString(++i, getTitle());
	    ps.setString(++i, getAddress1());
	    ps.setString(++i, getAddress2());
	    ps.setString(++i, getCity());
	    ps.setString(++i, getState().getAbbrev());
	    ps.setString(++i, getZip());
	    ps.setInt(++i, getCountry().getId());
	    ps.setString(++i, getPhoneNumber());
        ps.setString(++i, getFaxNumber());
	    ps.setInt(++i,getUser().getId());
		ps.setString(++i,getOrder().getId());
	    if (getId() > 0) {
	        ps.setInt(++i, getId());
	    }
	    ps.execute();

	    if (getId() == 0) {
	        ps = con.prepareStatement("SELECT MAX(inId) AS inMaxId FROM tbOrderAddress WHERE vcOrderId=? AND inUserId=? AND vcNameLast=? AND vcNameFirst=?");
	        i=0;
		    ps.setString(++i, getOrder().getId());
	        ps.setInt(++i,getUser().getId());
	        ps.setString(++i, getNameLast());
	        ps.setString(++i, getNameFirst());
	        ResultSet rs = ps.executeQuery();
	        if (rs.next()) {
	            setId(rs.getInt("inMaxId"));
	        }
	        rs.close();
	    }
	}


	public void setOrder(Order in) {
		order = in;
	}


}
