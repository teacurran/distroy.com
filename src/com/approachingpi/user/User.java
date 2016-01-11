package com.approachingpi.user;

import com.approachingpi.user.Address;
import com.approachingpi.store.Store;

import javax.servlet.http.HttpServletRequest;
import java.sql.*;
import java.util.*;
import java.util.Date;

public class User {
	public static final int TYPE_PUBLIC_ANON        = 0;
    public static final int TYPE_PUBLIC             = 1;
	public static final int TYPE_WHOLESALE_PENDING  = 99;
    public static final int TYPE_WHOLESALE          = 100;
    public static final int TYPE_ADMIN              = 1000;

    public static final int[] TYPES         = {TYPE_PUBLIC_ANON,TYPE_PUBLIC,TYPE_WHOLESALE_PENDING,TYPE_WHOLESALE,TYPE_ADMIN};

	Company company;
    private int id = 0;
    private String username = "";
    private String password = "";
    private String email = "";

    protected int billId;
    protected Date dateCreated;
    protected Date dateModified;
    protected int shipId;
    protected ArrayList billingAddresses = new ArrayList();
    protected ArrayList shippingAddresses = new ArrayList();
    protected ArrayList anyTypeAddresses  = new ArrayList();
    protected boolean billShipSame = false;
    protected boolean mailingList;
    private ArrayList permissions;
    private Hashtable prefs = new Hashtable();
    private boolean prefsChanged;
    private Date passwordLastReset;
    private Date passwordExpires;
    private boolean passwordNoExpire = true;
    protected ArrayList stores = new ArrayList();
	private int type    = TYPE_PUBLIC_ANON;

    public User() {
        init();
    }

    public User(int in) {
        setId(in);
    }

    public User(int id, String username, String email) {
        init();
        setId(id);
        setUsername(username);
        setEmail(email);
    }

    private void init() {
        prefs = new Hashtable();
        prefsChanged = false;
    }

    public void addBillingAddress(Address in) {
        billingAddresses.add(in);
    }
    public void addShippingAddress(Address in) {
        shippingAddresses.add(in);
    }
    public void addAddress(Address in) {
        anyTypeAddresses.add(in);
    }
    public Address getActiveBillingAddress() {
        for (int i=0; i<billingAddresses.size(); i++) {
            Address address = (Address)billingAddresses.get(i);
            if (address.getId() == billId) {
                return address;
            }
        }
        for (int i=0; i<anyTypeAddresses.size(); i++) {
            Address address = (Address)anyTypeAddresses.get(i);
            if (address.getId() == billId) {
                return address;
            }
        }
        if (anyTypeAddresses.size() > 0) {
            Address address = (Address)anyTypeAddresses.get(0);
            setShipId(address.getId());
            return address;
        }
        if (billingAddresses.size() > 0) {
            Address address = (Address)billingAddresses.get(0);
            setBillId(address.getId());
            return address;
        }
        return new Address();
    }
    public Address getActiveShippingAddress() {
        if (shipId > 0) {
            for (int i=0; i<shippingAddresses.size(); i++) {
                Address address = (Address)shippingAddresses.get(i);
                if (address.getId() == shipId) {
                    return address;
                }
            }
            for (int i=0; i<anyTypeAddresses.size(); i++) {
                Address address = (Address)anyTypeAddresses.get(i);
                if (address.getId() == shipId) {
                    return address;
                }
            }
        }
        if (shippingAddresses.size() > 0) {
            Address address = (Address)shippingAddresses.get(0);
            setShipId(address.getId());
            return address;
        }
        if (anyTypeAddresses.size() > 0) {
            Address address = (Address)anyTypeAddresses.get(0);
            setShipId(address.getId());
            return address;
        }
        return new Address();
    }
    public int getBillId() { return this.billId; }
	public Company getCompany() {
		if (company==null) {
			company = new Company();
		}
		return company;
	}
    public Date getDateCreated() { return this.dateCreated; }
    public Date getDateModified() { return this.dateModified; } 
    public int getShipId() { return this.shipId; }
    public int getId() { return this.id; }
    public String getEmail() { return this.email; }
    public boolean getMailingList() { return this.mailingList; }
    public String getPassword() { return this.password; }
    public Date getPasswordExpires() { return this.passwordExpires; }
    public Date getPasswordLastReset() { return this.passwordLastReset; }
    public boolean getPasswordNoExpire() { return this.passwordNoExpire; }
    public String getPref(String key) {
        if (prefs.containsKey(key)) {
            return (String)prefs.get(key);
        } else {
            // never return a null value for prefs
            return new String();
        }
    }
    public boolean getPrefBoolean(String key) {
        if (getPref(key).equalsIgnoreCase("t") || getPref(key).equalsIgnoreCase("true") || getPref(key).equalsIgnoreCase("yes")) {
            return true;
        }
        return false;
    }
	public int getPrefInt(String key) {
		return getPrefInt(key,0);
	}
	public int getPrefInt(String key, int defaultValue) {
	    int value = defaultValue;
	    try {
	        value = Integer.parseInt(getPref(key));
	    } catch (Exception e) {
	    }
	    return value;
	}
    public ArrayList getStores() {
        if (stores == null) {
            stores = new ArrayList();
        }
        return stores;
    }


	public int getType() { return this.type; }
    public static String getTypeName(int type) {
        switch (type) {
	        case TYPE_PUBLIC_ANON:
		        return "public anonymous";
            case TYPE_PUBLIC:
                return "public";
		    case TYPE_WHOLESALE_PENDING:
		        return "wholesale pending";
            case TYPE_WHOLESALE:
                return "wholesale";
            case TYPE_ADMIN:
                return "admin";
        }
        return "unknown";
    }
    public String getUsername() {
        // this site doesn't use a username
        return this.email;
        //return this.username;
    }
    
    public void loadAddressesFromDb(Connection con) throws SQLException {
        PreparedStatement ps = con.prepareStatement("SELECT * FROM tbAddress WHERE inUserId = ? ORDER BY vcReference");
        ps.setInt(1,getId());
        ResultSet rs = ps.executeQuery();
        while(rs.next()) {
            Address address = new Address();
            address.setId(rs.getInt("inId"));
            address.setUser(this);
            address.loadFromRs(rs);
            if (address.getType() == Address.TYPE_ANY) {
                anyTypeAddresses.add(address);
            }
            if (address.getType() == Address.TYPE_BILLING) {
                billingAddresses.add(address);
            }
            if (address.getType() == Address.TYPE_SHIPPING) {
                shippingAddresses.add(address);
            }
            address.getState().loadFromDb(con);
            address.getCountry().loadFromDb(con);
        }
    }

    public void loadFromDb(Connection con) throws SQLException {
        if (getId() == 0) {
            return;
        }
        ResultSet rs;
        PreparedStatement ps;

        ps = con.prepareStatement("SELECT * FROM tbUser WHERE inId = ?");
        ps.setInt(1,getId());
        rs = ps.executeQuery();
        if (!rs.next()) {
            this.setId(0);
        } else {
	        loadFromRs(rs);
        }
        rs.close(); rs=null;
        // TODO IMPLEMENT THESE
        //refreshPermissions(con);
        refreshPrefs(con);
    }

    public boolean loadFromDbByLoggingIn(Connection con) {
        boolean retVal = false;
        try {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM tbUser WHERE vcEmail = ? AND vcPassword = ?");
            ps.setString(1,getEmail());
            ps.setString(2,getPassword());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                this.setId(rs.getInt("inId"));
                this.loadFromRs(rs);
                retVal = true;
            }
            rs.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return retVal;
    }

	public void loadFromRs(ResultSet rs) throws SQLException {
        setType(rs.getInt("smUserType"));
		setCompany(new Company(rs.getInt("inCompanyId")));
		setEmail(rs.getString("vcEmail"));
		setPassword(rs.getString("vcPassword"));
		setPasswordNoExpire(rs.getBoolean("btPasswordNoExpire"));
		// may be out of order
		try {
		    setPasswordLastReset(new Date(rs.getDate("dtPasswordLastReset").getTime()));
		} catch (Exception e) {}
		try {
		    setPasswordExpires(new Date(rs.getDate("dtPasswordExpires").getTime()));
		} catch (Exception e) {}
        try {
            this.setDateCreated(new Date(rs.getTimestamp("dtCreated").getTime()));
		} catch (Exception e) {}
        try {
            this.setDateModified(new Date(rs.getTimestamp("dtModified").getTime()));
		} catch (Exception e) {}
        setMailingList(rs.getBoolean("btMailingList"));
        setBillId(rs.getInt("inBillId"));
        setShipId(rs.getInt("inShipId"));
	}

    public void loadStoresFromDb(Connection con) throws SQLException {
        PreparedStatement ps = con.prepareStatement(""+
            "SELECT * FROM tbStore\n"+
            "WHERE inId IN (\n"+
                "\tSELECT inStoreId FROM tbLNKUserStore\n"+
                "\tWHERE inUserId=?\n"+
            ") ORDER BY vcName");
        ps.setInt(1,this.getId());
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            Store thisStore = new Store(rs.getInt("inId"));
            thisStore.setId(rs.getInt("inId"));
            thisStore.loadFromRs(rs);
            getStores().add(thisStore);
        }
        rs.close();
        rs = null;
    }
    public void refreshPermissions(Connection con) {
        permissions = new ArrayList();
        if (getId() == 0) {
            return;
        }
        try {
            ResultSet rs;
            PreparedStatement ps;
            // get the permissions for this user.
            ps = con.prepareStatement("SELECT * FROM tbPermission " +
                "WHERE in_Id IN ( " +
                    "SELECT Distinct(in_PermissionId) FROM tbLinkPermissionUserGroup " +
                    "WHERE inUserId = ? " +
                    "OR inGroupId IN ( " +
                        "SELECT inGroupId " +
                        "FROM tbLinkUserGroup " +
                        "WHERE inUserId = ? " +
                    ") OR inGroupId = 1 " +
                ") " +
                "ORDER BY inPermissionGroupId, vcKey ");
            ps.setInt(1,getId());
            ps.setInt(2,getId());
            rs = ps.executeQuery();
            //System.out.println("Checking user permissions");
            while (rs.next()) {
                permissions.add(new String(rs.getString("vc_Key")));
                //System.out.println("Adding Permission: " + rs.getString("vc_Key"));
            }
            rs.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void refreshPrefs(Connection con) {
        prefs = new Hashtable();
        if (getId() == 0) {
            return;
        }
        try {
            ResultSet rs;
            PreparedStatement ps;
            // get the permissions for this user.
            ps = con.prepareStatement("SELECT * FROM tbUserPref WHERE inUserId = ? ");
            ps.setInt(1,getId());
            rs = ps.executeQuery();
            while (rs.next()) {
                prefs.put(rs.getString("vcKey"), rs.getString("vcValue"));
                System.out.println("Adding Pref: " + rs.getString("vcKey"));
            }
            rs.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void loadDefaultPrefs(Connection con) {
        prefs = new Hashtable();
        if (getId() == 0) {
            return;
        }
        try {
            ResultSet rs;
            PreparedStatement ps;
            // get the permissions for this user.
            ps = con.prepareStatement("SELECT * FROM tbUserPref WHERE inUserId = 0 ");
            rs = ps.executeQuery();
            //System.out.println("Checking user permissions");
            while (rs.next()) {
                prefs.put(rs.getString("vcKey"), rs.getString("vcValue"));
                //System.out.println("Adding Pref: " + rs.getString("vc_Key"));
            }
            rs.close();
        } catch (Exception e) {
        }
        prefsChanged = true;
    }

    public void savePrefs(Connection con) {
        if (getId() == 0) {
            return;
        }
        // if the prefs haven't changed, there is no reason to commit them
        if (!prefsChanged) {
            return;
        }
        try {
            /* We are going to loop over all the prefs the user has, if they are in the database
            then we update them, othwrwise we insert them */
            PreparedStatement ps;
            Enumeration keys = prefs.keys();
            String currentKey;
            String currentValue;
            while (keys.hasMoreElements()) {
                currentKey = (String)keys.nextElement();
                currentValue = (String)prefs.get(currentKey);
                //System.out.println("Saving Pref: " + currentKey + "=" + currentValue);
                ps = con.prepareStatement("IF ((SELECT Count(*) FROM tbUserPref WHERE inUserId = ? AND vcKey = ?) > 0) "+
                        "BEGIN " +
                        "UPDATE tbUserPref SET vcValue = ? WHERE inUserId = ? AND vcKey = ? " +
                        "END ELSE BEGIN " +
                        "INSERT INTO tbUserPref (inUserId, vcKey, vcValue) VALUES (?,?,?) "+
                        "END");
                ps.setInt(1,getId());
                ps.setString(2,currentKey);
                ps.setString(3,currentValue);
                ps.setInt(4,getId());
                ps.setString(5,currentKey);
                ps.setInt(6,getId());
                ps.setString(7,currentKey);
                ps.setString(8,currentValue);
                ps.execute();
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }
        prefsChanged = false;
    }

    public void saveToDb(Connection con) throws SQLException {
        PreparedStatement ps;
        if (getId() > 0) {
            ps = con.prepareStatement("UPDATE tbUser SET smUserType=?, inCompanyId=?, vcEmail=?, btPasswordNoExpire=?, dtModified=CURRENT_TIMESTAMP, btMailingList=?, inBillId=?, inShipId=? WHERE inId = ?");
        } else {
            ps = con.prepareStatement("INSERT INTO tbUser (smUserType, inCompanyId, vcEmail, btPasswordNoExpire, dtCreated, dtModified, btMailingList, inBillId, inShipId) " +
                "VALUES (?,?,?,?,CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,?,?,?) ");
        }
        int i=0;
        ps.setInt(++i,getType());
	    if (getCompany().getId()==0) {
		    ps.setNull(++i,Types.INTEGER);
	    } else {
	        ps.setInt(++i,getCompany().getId());
	    }
        ps.setString(++i,getEmail());
        ps.setBoolean(++i,getPasswordNoExpire());
        ps.setBoolean(++i,getMailingList());
        ps.setInt(++i,this.billId);
        ps.setInt(++i,this.shipId);

        if (getId() > 0) {
            ps.setInt(++i, getId());
        }
        ps.execute();

        if (getId() == 0) {
            // lookup the user we just inserted
            ps = con.prepareStatement("SELECT MAX(inId) AS inMaxId FROM tbUser WHERE vcEmail=? AND smUserType=?");

            ps.setString(1,getEmail());
            ps.setInt(2,getType());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                setId(rs.getInt("inMaxId"));
            }
            rs.close();
        }

        // updating password is optional, if the left it blank, don't update it
        if (getPassword().length() > 0) {
            ps = con.prepareStatement("UPDATE tbUser SET vcPassword = ?, dtPasswordLastReset = CURRENT_TIMESTAMP WHERE inId = ?");
            ps.setString(1,getPassword());
            ps.setInt(2,getId());
            ps.execute();
        }
    }

    public boolean hasPermission(String key) {
        ListIterator iterator = permissions.listIterator();
        String thisPermission;
        while (iterator.hasNext()) {
            thisPermission = (String)iterator.next();
            //System.out.println(thisPermission + "-" + key);
            if (thisPermission.equalsIgnoreCase(key)) {
                return true;
            }
        }
        return false;
    }

    public void setBillId(int in) { this.billId = in; }
    public void setDateCreated(Date in) { this.dateCreated = in; } 
    public void setDateModified(Date in) { this.dateModified = in; } 
	public void setCompany(Company in) { this.company = in; }
    public void setId(int in) { this.id = in; }
    public void setEmail(String in) { this.email = (in == null)?"":in; }
    public void setMailingList(boolean in) { this.mailingList = in; }
    public void setPassword(String in) { this.password = (in == null)?"":in; }
    public void setPasswordExpires(Date in) { this.passwordExpires = in; }
    public void setPasswordLastReset(Date in) { this.passwordLastReset = in; }
    public void setPasswordNoExpire(boolean in) { this.passwordNoExpire = in; }
	public void setShipId(int in) { this.shipId = in; }

    public void setPref(String key, String value) {
        if (key == null) {
            return;
        }
        if (value == null) {
            value = "";
        }
        String previousValue = (String)prefs.put(key,value);
        // see if the value has changed. if so, set the flag to save it to the database later
        if (previousValue == null) {
            this.prefsChanged = true;
        } else {
            if (!previousValue.equals(value)) {
                //System.out.println("Pref Changed:" + key + " was:" +previousValue + " is:" + value);
                this.prefsChanged = true;
            }
        }
    }
    public void setPref(String key, boolean value) {
        String stringValue = value ? "T" : "F";
        setPref(key,stringValue);
    }
    public void setPref(String key, int value) {
        setPref(key, Integer.toString(value));
    }
    public void setStores(ArrayList in) {
        stores = in;
    }
	public void setType(int in) { this.type = in; }
    public void setUsername(String in) { this.username = (in == null)?"":in; }
}
