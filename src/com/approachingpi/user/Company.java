package com.approachingpi.user;

import java.sql.*;
import java.util.Date;
import java.util.ArrayList;

/**
 * User: terrence
 * Date: Sep 1, 2004
 * Time: 9:11:15 PM
 */
public class Company {
	public static final int STATUS_ANY      = -1;
	public static final int STATUS_UNKNOWN  = 0;
	public static final int STATUS_PENDING  = 1;
	public static final int STATUS_APPROVED = 2;
	public static final int STATUS_CUSTOMER = 3;
	public static final int STATUS_INACTIVE = 4;

    public static final int[] STATUS_LIST = {STATUS_PENDING, STATUS_APPROVED};

	Date        dateCreated;
	Date        dateModified;
	Date        dateActive;
	int         id;
	String      name        = "";
	int         status      = STATUS_UNKNOWN;
	int         userCount   = 0;
	ArrayList   users       = new ArrayList();

	public Company(int id) {
		setId(id);
	}
	public Company() {}

	public void addUser(User in) {
		if (in!=null) {
			userCount++;
			users.add(in);
		}
	}
	public static ArrayList getAllCompanies(Connection con, int status) {
		ArrayList companies = new ArrayList();
		try {
			PreparedStatement ps;
			if (status == STATUS_ANY) {
				ps = con.prepareStatement("SELECT * FROM tbCompany WHERE ORDER BY vcName");
			} else {
				ps = con.prepareStatement("SELECT * FROM tbCompany WHERE inStatus=? ORDER BY vcName");
				ps.setInt(1,status);
			}
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				Company company = new Company(rs.getInt("inId"));
				company.loadFromRs(rs);
				companies.add(company);
			}
			rs.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return companies;
	}

	public Date getDateCreated() {
		return dateCreated;
	}
	public Date getDateModified() {
		return dateModified;
	}
	public Date getDateActive() {
		return dateActive;
	}
	public int getId() {
		return id;
	}
	public String getName() {
		return name;
	}
	public int getStatus() {
		return status;
	}
    public static String getStatusNameById(int id) {
        switch (id) {
            case STATUS_PENDING:
                return "pending";
            case STATUS_APPROVED:
                return "approved";
            case STATUS_CUSTOMER:
                return "customer";
            case STATUS_INACTIVE:
                return "inactive";
            default:
                return "unknown";
        }
    }
    public ArrayList getUsers() {
        if (users == null) {
            users = new ArrayList();
        }
        return this.users;
    }

	public void loadFromDb(Connection con) throws SQLException {
		PreparedStatement ps = con.prepareStatement("SELECT * FROM tbCompany WHERE inId = ?");
		ps.setInt(1,getId());
		ResultSet rs = ps.executeQuery();
		if (rs.next()) {
			loadFromRs(rs);
		}
		rs.close();
	}
	public void loadCommentsFromDb(Connection con) throws SQLException {
		PreparedStatement ps = con.prepareStatement("SELECT * FROM tbCompanyComment WHERE inCompanyId = ? ORDER BY dtModified");
		ps.setInt(1,getId());
		ResultSet rs = ps.executeQuery();
		while (rs.next()) {
			CompanyComment comment = new CompanyComment(this);
			comment.loadFromRs(rs);
		}
		rs.close();
	}
	public void loadFromRs(ResultSet rs) throws SQLException {
		this.setName(rs.getString("vcName"));
		this.setStatus(rs.getInt("inStatus"));
		this.setDateCreated(new Date(rs.getTimestamp("dtCreated").getTime()));
        try {
            this.setDateModified(new Date(rs.getTimestamp("dtModified").getTime()));
        } catch (Exception e) { }
        try {
    		this.setDateActive(new Date(rs.getTimestamp("dtActive").getTime()));
        } catch (Exception e) { }
	}
	public void loadUsersFromDb(Connection con) throws SQLException {
        UserSearchEngine se = new UserSearchEngine();
        se.setCompany(this);
        //se.addSort(UserSearchEngine.SORT_EMAIL);
        users = se.executeAndReturn(con);
	}
    

	public void saveToDb(Connection con) throws SQLException  {
		PreparedStatement ps;
		if (getId()==0) {
			ps = con.prepareStatement("INSERT INTO tbCompany (vcName, inStatus, dtCreated, dtModified, dtActive) VALUES(?,?,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,?)");
		} else {
			ps = con.prepareStatement("UPDATE tbCompany SET vcName=?, inStatus=?, dtModified=CURRENT_TIMESTAMP WHERE inId=?");
		}
		ps.setString(1,getName());
		ps.setInt(2,getStatus());
		if (getId()>0) {
			ps.setInt(3,getId());
		} else {
			if (getDateActive()==null) {
				ps.setNull(3,Types.DATE);
			} else {
				ps.setDate(3,new java.sql.Date(getDateActive().getTime()));
			}
		}
		ps.execute();

		if (getId() ==0) {
			ps = con.prepareStatement("SELECT Max(inId) AS inMaxId FROM tbCompany WHERE vcName=? AND inStatus=?");
			ps.setString(1,getName());
			ps.setInt(2,getStatus());
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				setId(rs.getInt("inMaxId"));
			}
			rs.close();
		}
	}

	public void setDateActive(Date in) { this.dateActive = in; }
	public void setDateCreated(Date in) { this.dateCreated = in; }
	public void setDateModified(Date in) { this.dateModified = in; }
	public void setId(int in) { this.id = in; }
	public void setName(String in) { this.name = (in==null) ? "" : in; }
	public void setStatus(int in) { this.status = in; }
}