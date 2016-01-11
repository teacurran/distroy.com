package com.approachingpi.store;

import java.sql.SQLException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

/**
 * User: terrence
 * Date: Aug 25, 2004
 * Time: 6:26:38 PM
 */
public class Store {
	protected String  abbreviation    = "";
	protected int     id;
	protected String  name            = "";
	protected boolean wholesale       = false;

	public Store(int id) {
		setId(id);
	}
	public Store(String abbreviation) {
		setAbbreviation(abbreviation);
	}

    public static ArrayList getAllStoresFromDb(Connection con) {
        ArrayList allStores = new ArrayList(5);
        try {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM tbStore ORDER BY vcName");
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                Store store = new Store(rs.getInt("inId"));
                store.loadFromRs(rs);
                allStores.add(store);
            }
            rs.close();
        } catch (Exception e) { 
            e.printStackTrace();
        }
        
        return allStores;
    }
    
	public void loadFromDb(Connection con) throws SQLException {
		if (getId() == 0) {
			return;
		}
		PreparedStatement ps = con.prepareStatement("SELECT * FROM tbStore WHERE inId = ?");
		ps.setInt(1,getId());
		ResultSet rs = ps.executeQuery();
		if (rs.next()) {
			loadFromRs(rs);
		}
		rs.close();
	}

	public void loadFromDbByAbbreviation(Connection con) throws SQLException {
		PreparedStatement ps = con.prepareStatement("SELECT * FROM tbStore WHERE vcAbbrev = ?");
		ps.setString(1,getAbbreviation());
		ResultSet rs = ps.executeQuery();
		if (rs.next()) {
			setId(rs.getInt("inId"));
			loadFromRs(rs);
		}
		rs.close();
	}

	public void loadFromRs(ResultSet rs) throws SQLException {
		setName(rs.getString("vcName"));
		setAbbreviation(rs.getString("vcAbbrev"));
		setWholesale(rs.getBoolean("btWholesale"));
	}



	public String getAbbreviation() {
		return abbreviation;
	}

	public void setAbbreviation(String abbreviation) {
		this.abbreviation = (abbreviation == null) ? "" : abbreviation;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = (name == null) ? "" : name;
	}

	public boolean isWholesale() {
		return wholesale;
	}

	public void setWholesale(boolean wholesale) {
		this.wholesale = wholesale;
	}


}
