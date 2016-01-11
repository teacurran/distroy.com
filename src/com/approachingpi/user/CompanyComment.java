package com.approachingpi.user;

import java.util.Date;
import java.sql.PreparedStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.ResultSet;

/**
 * User: terrence
 * Date: Sep 2, 2004
 * Time: 12:56:06 AM
 */
public class CompanyComment {
	String  comment      = "";
	Company company;
	Date    dateCreated;
	int     id;
	User    user;

	public CompanyComment() {
	}
	public CompanyComment(Company in) {
		this.setCompany(in);
	}
	public String getComment() { return comment; }
	public Company getCompany() {
		if (company==null) { company = new Company(); }
		return company;
	}
	public Date getDateCreated() { return dateCreated; }
	public int getId() { return id; }
	public User getUser() {
		if (user==null) { user = new User(); }
		return user;
	}

	public void loadFromDb(Connection con) throws SQLException  {
		if (getId()==0) {
			return;
		}
		PreparedStatement ps = con.prepareStatement("SELECT * FROM tbCompanyComment WHERE inId=?");
		ps.setInt(1,getId());
		ResultSet rs = ps.executeQuery();
		if (rs.next()) {
			loadFromRs(rs);
		}
		rs.close();
	}
	public void loadFromRs(ResultSet rs) throws SQLException {
		if (getCompany().getId()==0) {
			setCompany(new Company(rs.getInt("inCompanyId")));
		}
		if (getUser().getId()==0) {
			setUser(new User(rs.getInt("inUserId")));
		}
		setDateCreated(new Date(rs.getTimestamp("dtModified").getTime()));
		setComment(rs.getString("txComment"));
	}

    public void saveToDb(Connection con) throws SQLException {
	    PreparedStatement ps;
	    if (getId() == 0) {
			ps = con.prepareStatement("INSERT INTO tbCompanyComment (inCompanyId, inUserId, txComment) VALUES(?,?,?)");
	    } else {
		    ps = con.prepareStatement("UPDATE tbCompanyComment SET inCompanyId=?, inUserId=?, txComment=? WHERE inId=?");
	    }
	    ps.setInt(1,getCompany().getId());
	    ps.setInt(2,getUser().getId());
	    ps.setString(3,getComment());

	    if (getId()>0) {
		    ps.setInt(4,getId());
	    }
	    ps.execute();

	    if (getId()==0) {
		    ps = con.prepareStatement("SELECT MAX(inId) AS inMaxId FROM tbCompanyComment WHERE inCompanyId=? AND inUserId=?");
		    ps.setInt(1,getCompany().getId());
		    ps.setInt(2,getUser().getId());
		    ResultSet rs = ps.executeQuery();
		    if (rs.next()) {
				setId(rs.getInt("inMaxid"));
		    }
		    rs.close();
	    }
    }


	public void setComment(String in) { comment = (in==null) ? "" : in; }
	public void setCompany(Company in) { company = in; }
	public void setDateCreated(Date in) { dateCreated = in; }
	public void setId(int in) { id = in; }
	public void setUser(User in) { user = in; }
}
