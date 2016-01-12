/*
 * User: Administrator
 * Date: Mar 5, 2002
 * Time: 12:25:26 AM
 * To change template for new class use
 */
package com.approachingpi.user;

import com.approachingpi.store.cart.ShipMethod;

import java.sql.*;

public class Address {
    public static final int TYPE_ANY        = 0;
    public static final int TYPE_BILLING    = 1;
    public static final int TYPE_SHIPPING   = 2;

    private String address1         = "";
    private String address2         = "";
    private String city             = "";
    private Company company;
    private Country country;
	private String faxNumber        = "";
    private int id;
    private String nameFirst        = "";
    private String nameLast         = "";
    private String phoneNumber      = "";
    private String reference        = "";
    private State state;
	private String title            = "";
    private int type                = TYPE_ANY;
    private User user;
    private String zip              = "";
    private ShipMethod shipMethod;

    public void deleteFromDb(Connection con) throws SQLException {
        PreparedStatement ps = con.prepareStatement("DELETE FROM tbAddress WHERE inId = ?");
        ps.setInt(1,getId());
        ps.execute();
        setId(0);
    }

    public String getAddress1() { return address1; }
    public String getAddress2() { return address2; }
    public String getCity() { return city; }
    public Company getCompany() {
        if (company == null) {
            company = new Company();
        }
        return company;
    }
    public Country getCountry() {
        if (country == null) {
            return new Country();
        }
        return country;
    }
	public String getFaxNumber() { return faxNumber; }
    public boolean getHasData() {
        boolean retVal = false;
        // this doesn't worry about drop downs because that could be a mistake
        if (getNameFirst().length() > 0 ||
            getNameLast().length() > 0 ||
            getAddress1().length() > 0 ||
            getAddress2().length() > 0 ||
            getCity().length() > 0 ||
            getZip().length() > 0 ||
            getPhoneNumber().length() > 0) {
                retVal = true;
        }
        return retVal;
    }
    public int getId() { return id; }
    public String getNameFirst() { return nameFirst; }
    public String getNameLast() { return nameLast; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getReference() { return reference; }
    public ShipMethod getShipMethod() {
	    if (shipMethod==null) {
		    shipMethod = new ShipMethod();
	    }
	    return shipMethod;
    }
    public State getState() {
        if (state == null) {
            return new State("");
        }
        return state;
    }
	public String getTitle() { return title; }
    public int getType() { return type; }
    public User getUser() {
        if (user == null) {
            user = new User();
        }
        return user;
    }
    public String getZip() {
        return zip;
    }

    // load methods
    public void loadFromDb(Connection con) throws SQLException {
        if (getId() <= 0) {
            return;
        }
        try {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM tbAddress WHERE inId = ?");
            ps.setInt(1,this.getId());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                loadFromRs(rs);
            } else {
                int id = this.getId();
                this.setId(0);
                throw new Exception("Address Id " + id + " not found");
            }
            rs.close();

        } catch (Exception e) {
            //System.err.println(e.toString() + " - " + e.getMessage());
            e.printStackTrace();
        }
    }
    public void loadFromRs(ResultSet rs) throws SQLException {
        // only set the user if we haven't previously set it
        // this is used so when we load from an active user
        // we can just setUser(this) and we have a user
        // with all the info populated
        if (getUser().getId() == 0) {
            user = new User(rs.getInt("inUserId"));
        }
        setType(rs.getInt("inType"));
        setReference(rs.getString("vcReference"));
        setNameFirst(rs.getString("vcNameFirst"));
	    setNameLast(rs.getString("vcNameLast"));
	    setTitle(rs.getString("vcTitle"));
        setAddress1(rs.getString("vcAddress1"));
        setAddress2(rs.getString("vcAddress2"));
        setCity(rs.getString("vcCity"));
        if (getState().getAbbrev().equals("")) {
            setState(new State(rs.getString("vcState")));
        }
        setZip(rs.getString("vcZip"));
        setCountry(new Country(rs.getInt("inCountryId")));
        setPhoneNumber(rs.getString("vcPhoneNumber"));
	    setFaxNumber(rs.getString("vcFaxNumber"));
        setShipMethod(new ShipMethod(rs.getInt("inPreferredShipMethodId")));
    }

    public void saveToDb(Connection con) throws SQLException {
        String sql;
        if (getId() > 0) {
            sql = "UPDATE tbAddress SET inType=?, vcReference=?, vcNameFirst=?, vcNameLast=?, vcTitle=?, vcAddress1=?, vcAddress2=?, vcCity=?, vcState=?, vcZip=?, inCountryId=?, vcPhoneNumber=?, vcFaxNumber=?, inPreferredShipMethodId=? WHERE inUserId=? AND inId=?";
        } else {
            sql = "INSERT INTO tbAddress (inType, vcReference, vcNameFirst, vcNameLast, vcTitle, vcAddress1, vcAddress2, vcCity, vcState, vcZip, inCountryId, vcPhoneNumber, vcFaxNumber, inPreferredShipMethodId, inUserId) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        }
	    //System.out.println(sql);
        PreparedStatement ps = con.prepareStatement(sql);
        int i=0;
        ps.setInt(++i, getType());
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
	    if (getShipMethod().getId()==0) {
		    ps.setNull(++i, Types.INTEGER);
	    } else {
            ps.setInt(++i, getShipMethod().getId());
	    }
        ps.setInt(++i,getUser().getId());
        if (getId() > 0) {
            ps.setInt(++i, getId());
        }
        ps.execute();

        if (getId() == 0) {
            ps = con.prepareStatement("SELECT MAX(inId) AS inMaxId FROM tbAddress WHERE inUserId=? AND vcNameLast=? AND vcNameFirst=? AND inType=?");
            i=0;
            ps.setInt(++i,getUser().getId());
            ps.setString(++i, getNameLast());
            ps.setString(++i, getNameFirst());
            ps.setInt(++i, getType());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                setId(rs.getInt("inMaxId"));
            }
            rs.close();
        }
    }

    public void setAddress1(String in) {
        address1 = (in==null) ? "" : in;
    }
    public void setAddress2(String in) {
        address2 = (in==null) ? "" : in;
    }
    public void setCity(String in) {
        city = (in==null) ? "" : in;
    }
    public void setCompany(Company in) {
        company = in;
    }
    public void setCountry(Country in) {
        country = in;
    }
	public void setFaxNumber(String in) {
		faxNumber = (in==null) ? "" : in;
	}
    public void setId(int in) {
        id = in;
    }
    public void setNameFirst(String in) {
        nameFirst = (in==null) ? "" : in;
    }
    public void setNameLast(String in) {
        nameLast = (in==null) ? "" : in;
    }
    public void setPhoneNumber(String in) {
        phoneNumber = (in==null) ? "" : in;
    }
    public void setReference(String in) {
        reference = (in==null) ? "" : in;
    }
    public void setShipMethod(ShipMethod in) {
        shipMethod = in;
    }
    public void setState(State in) {
        state = in;
    }
	public void setTitle(String in) {
		title = (in==null) ? "" : in;
	}
    public void setType(int in) {
        type = in;
    }
    public void setUser(User in) {
        user = in;
    }
    public void setZip(String in) {
        zip = (in==null) ? "" : in;
    }
}
