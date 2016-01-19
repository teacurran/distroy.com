/*
 * Created by netbeans ide 4.1.
 * User: britt
 * Date: dec 2005
 */
package com.approachingpi.store.catalog;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.text.SimpleDateFormat;

import com.approachingpi.servlet.PiServlet;
import com.approachingpi.util.PiUtility;


public class Royalties {
	private int id;
	private int ArtistId;
	private String nameFirst = "";
	private String nameDisplay = "";
	private String relationship = "";
	private String CheckNumber = "";
	private BigDecimal CheckAmount;
	private BigDecimal moPaid;
	private BigDecimal moOwed;
	private Date dateCheck;
	private Date dateCreated;
	private Date dateModified;
	//added for auditing
	private Date dateShipComplete;
	private String vcOrderId = "";
	private int inQty;
	private String vcProductVariationId = "";
	private String vcItemDesc = "";

	public Royalties() {
	}

	public Royalties(int in) {
		this.setId(in);
		//        System.out.println("artistID from royalties method: " + in);
		//        System.out.println("getId output from royalties method: " + this.getId());
	}

	//    public void updateOrderSummary (Connection con) throws SQLException {
	//        PreparedStatement ps = con.prepareStatement("begin " +
	//                "drop table viOrderSummary " +
	//                "end " +
	//                "begin " +
	//                "select D.inId, D.vcOrderId, D.moPriceTotal, sum(D.moPriceTotal) 'moPriceTotalSum', O.inStatus,
	// S.vcAbbrev, S.btWholesale, A.inId 'inArtistId', D.inQty, D.vcItemDesc, O.dtShipComplete " +
	//                "into viOrderSummary " +
	//                "FROM tbOrderDetail D, tbOrder O, tbStore S, tbProductVariation V, tbArtist A,
	// tbLinkProductArtist LNK " +
	//                "WHERE D.vcOrderId = O.vcId  " +
	//                "AND S.inId = O.inStoreId " +
	//                "AND LNK.inArtistId = A.inId " +
	//                "AND D.inProductVariationId = V.inId " +
	//                "AND V.inProductId = LNK.inProductId " +
	//                "GROUP BY D.inId, D.vcOrderId, D.moPriceTotal, O.inStatus, S.vcAbbrev, S.btWholesale, A.inId, D
	// .inQty, D.vcItemDesc, O.dtShipComplete " +
	//                "end"
	//
	//                );
	//
	//        ps.execute();
	//    }
	public void updateRoyalties(Connection con) throws SQLException {

		String sql = "SELECT r.inArtistId, r.vcNameDisplay, r.vcCheckNumber, r.moCheckAmount," +
			"r.dtCheck, r.dtCreated, r.dtModified, a.moRoyaltiesPaid, a.moRoyaltiesOwed " +
			"FROM tbArtistRoyalties r\n" +
			"JOIN tbArtist a ON r.inArtistId=a.inId\n" +
			"where r.vcCheckNumber = (\n" +
				"\tSELECT max(b.vcCheckNumber) " +
				"\tFROM tbArtistRoyalties b " +
				"\tWHERE r.inArtistId=b.inArtistId" +
			") GROUP BY r.vcNameDisplay, r.inArtistId, r.vcCheckNumber, " +
			"r.moCheckAmount, r.dtCheck, r.dtCreated, r.dtModified, " +
			"a.moRoyaltiesPaid, a.moRoyaltiesOwed";

		PreparedStatement ps = con.prepareStatement(sql);
		ResultSet rs = ps.executeQuery();
		while (rs.next()) {
			String sql2 = "UPDATE tbArtist " +
				"set moRoyaltiesPaid=(\n" +
					"\tSELECT sum(moCheckAmount)\n" +
					"\tFROM tbArtistRoyalties\n" +
					"\tWHERE inArtistId=?\n" +
				") WHERE inId=?";
			PreparedStatement ps2 = con.prepareStatement(sql2);
			ps2.setInt(1, rs.getInt("inArtistId"));
			ps2.setInt(2, rs.getInt("inArtistId"));
			ps2.execute();

			//            String sql ="begin select a.inArtistId, a.vcNameDisplay, a.vcCheckNumber, a.moCheckAmount, a
			// .dtCheck, a
			// .dtCreated, a.dtModified, a.moRoyaltiesPaid, a.moRoyaltiesOwed from tbArtistRoyalties a where a
			// .vcCheckNumber=(select max(b.vcCheckNumber) from tbArtistRoyalties b where a.inArtistId=b.inArtistId)
			// group by vcNameDisplay, inArtistId,vcCheckNumber,moCheckAmount, dtCheck, dtCreated, dtModified,
			// moRoyaltiesPaid, moRoyaltiesOwed end";
			//            PreparedStatement ps = con.prepareStatement(sql);
			//            ResultSet rs = ps.executeQuery();
			//            while (rs.next()) {
			//
			//                   String sql2 = "begin update tbArtistRoyalties set moRoyaltiesPaid=( select sum
			// (moCheckAmount) from tbArtistRoyalties where inArtistId=?) where inArtistId=? end";
			//                PreparedStatement ps2 = con.prepareStatement(sql2);
			//                        int i2=1;
			//                        ps2.setInt(i2++, rs.getInt("inArtistId"));
			//                        ps2.setInt(i2++, rs.getInt("inArtistId"));
			//                        ps2.execute();

			String sql3 = "IF (select moRoyaltyDollarRetail from tbArtist where inId=?)>0 AND  (select " +
				"moRoyaltyDollarWholesale from tbArtist where inId=?)>0 " +
				"update tbArtist set moRoyaltiesOwed= " +
				//                        "update tbArtistRoyalties set moRoyaltiesOwed= " +
				"(select moRoyaltyDollarRetail * (SELECT count(*) " +
				"FROM viOrderSummary " +
				"WHERE inArtistId=? and inStatus='3' and btWholesale='0') + (select moRoyaltyDollarWholesale * (SELECT" +
				" count(*) " +
				"FROM viOrderSummary " +
				"WHERE inArtistId=? and inStatus='3' and btWholesale='1') " +
				"FROM tbArtist " +
				"WHERE inId=?) " +
				"-(select max(moRoyaltiesPaid) from tbArtist where inId=?) " +
				"FROM tbArtist " +
				"WHERE inId=? )" +
				" WHERE inId=? " +

				"ELSE IF (select moRoyaltyDollarRetail from tbArtist where inId=?)>0 AND  (select " +
				"deRoyaltyPercentWholesale from tbArtist where inId=?)>0 " +
				"update tbArtist set moRoyaltiesOwed= " +
				//                        "update tbArtistRoyalties set moRoyaltiesOwed= " +
				"(select moRoyaltyDollarRetail * (SELECT count(*) " +
				"FROM viOrderSummary " +
				"WHERE inArtistId=? and inStatus='3' and btWholesale='0') + (select deRoyaltyPercentWholesale/100 * " +
				"(SELECT isnull(sum(moPriceTotalSum),0) " +
				"FROM viOrderSummary " +
				"WHERE inArtistId=? and inStatus='3' and btWholesale='1') " +
				"FROM tbArtist " +
				"WHERE inId=?) " +
				"-(select max(moRoyaltiesPaid) from tbArtist where inId=?) " +
				"FROM tbArtist " +
				"WHERE inId=?) " +
				" WHERE inId=? " +

				"ELSE IF (select deRoyaltyPercentRetail from tbArtist where inId=?)>0 AND  (select " +
				"moRoyaltyDollarWholesale from tbArtist where inId=?)>0  " +
				"update tbArtist set moRoyaltiesOwed= " +
				//                        "update tbArtistRoyalties set moRoyaltiesOwed= " +
				"(select deRoyaltyPercentRetail/100 * (SELECT isnull(sum(moPriceTotalSum),0) " +
				"FROM viOrderSummary " +
				"WHERE inArtistId=? and inStatus='3' and btWholesale='0') + (select moRoyaltyDollarWholesale * (SELECT" +
				" count(*) " +
				"FROM viOrderSummary " +
				"WHERE inArtistId=? and inStatus='3' and btWholesale='1') " +
				"FROM tbArtist " +
				"WHERE inId=?) " +
				"-(select max(moRoyaltiesPaid) from tbArtist where inId=?) " +
				"FROM tbArtist " +
				"WHERE inId=?) " +
				" WHERE inId=? " +

				" ELSE IF (select deRoyaltyPercentRetail from tbArtist where inId=?)>0 AND (select " +
				"deRoyaltyPercentWholesale from tbArtist where inId=?)>0 " +
				"update tbArtist set moRoyaltiesOwed= " +
				//                        "update tbArtistRoyalties set moRoyaltiesOwed= " +
				"(select deRoyaltyPercentRetail/100 * (SELECT isnull(sum(moPriceTotalSum),0) " +
				"FROM viOrderSummary " +
				"WHERE inArtistId=? and inStatus='3' and btWholesale='0') + (select deRoyaltyPercentWholesale/100 * " +
				"(SELECT isnull(sum(moPriceTotalSum),0) " +
				"FROM viOrderSummary " +
				"WHERE inArtistId=? and inStatus='3' and btWholesale='1') " +
				"FROM tbArtist " +
				"WHERE inId=?) " +
				"-(select max(moRoyaltiesPaid) from tbArtist where inId=?) " +
				"FROM tbArtist " +
				"WHERE inId=?) " +
				" WHERE inId=? " +

				" ELSE update tbArtist set moRoyaltiesOwed=0 where inId=? "
				// " ELSE update tbArtistRoyalties set moRoyaltiesOwed=0 where inArtistId=? "
				;
			PreparedStatement ps3 = con.prepareStatement(sql3);
			int i = 1;
			ps3.setInt(i++, rs.getInt("inArtistId"));
			ps3.setInt(i++, rs.getInt("inArtistId"));
			ps3.setInt(i++, rs.getInt("inArtistId"));
			ps3.setInt(i++, rs.getInt("inArtistId"));
			ps3.setInt(i++, rs.getInt("inArtistId"));
			ps3.setInt(i++, rs.getInt("inArtistId"));
			ps3.setInt(i++, rs.getInt("inArtistId"));
			ps3.setInt(i++, rs.getInt("inArtistId"));
			ps3.setInt(i++, rs.getInt("inArtistId"));
			ps3.setInt(i++, rs.getInt("inArtistId"));
			ps3.setInt(i++, rs.getInt("inArtistId"));
			ps3.setInt(i++, rs.getInt("inArtistId"));
			ps3.setInt(i++, rs.getInt("inArtistId"));
			ps3.setInt(i++, rs.getInt("inArtistId"));
			ps3.setInt(i++, rs.getInt("inArtistId"));
			ps3.setInt(i++, rs.getInt("inArtistId"));
			ps3.setInt(i++, rs.getInt("inArtistId"));
			ps3.setInt(i++, rs.getInt("inArtistId"));
			ps3.setInt(i++, rs.getInt("inArtistId"));
			ps3.setInt(i++, rs.getInt("inArtistId"));
			ps3.setInt(i++, rs.getInt("inArtistId"));
			ps3.setInt(i++, rs.getInt("inArtistId"));
			ps3.setInt(i++, rs.getInt("inArtistId"));
			ps3.setInt(i++, rs.getInt("inArtistId"));
			ps3.setInt(i++, rs.getInt("inArtistId"));
			ps3.setInt(i++, rs.getInt("inArtistId"));
			ps3.setInt(i++, rs.getInt("inArtistId"));
			ps3.setInt(i++, rs.getInt("inArtistId"));
			ps3.setInt(i++, rs.getInt("inArtistId"));
			ps3.setInt(i++, rs.getInt("inArtistId"));
			ps3.setInt(i++, rs.getInt("inArtistId"));
			ps3.setInt(i++, rs.getInt("inArtistId"));
			ps3.setInt(i++, rs.getInt("inArtistId"));
			ps3.execute();
		}

		rs.close();
	}

	public static ArrayList getAllArtists(Connection con) {
		return Royalties.getAllArtists(con, "a.vcNameDisplay");
	}

	public static ArrayList getAllArtists(Connection con, String orderBy) {
		ArrayList<Royalties> artists = new ArrayList<>();
		try {

			String sql = "select a.inArtistId, a.vcNameDisplay, a.vcCheckNumber, a.moCheckAmount, " +
				"a.dtCheck, a.dtCreated, a.dtModified, c.moRoyaltiesPaid, c.moRoyaltiesOwed\n" +
				"FROM tbArtistRoyalties a\n" +
				"JOIN tbArtist c ON a.inArtistId=c.inId\n" +
				"WHERE a.vcCheckNumber = (" +
				"SELECT MAX(b.vcCheckNumber)\n" +
				"FROM tbArtistRoyalties b \n" +
				"where a.inArtistId = b.inArtistId" +
				") GROUP BY a.vcNameDisplay, inArtistId,vcCheckNumber, moCheckAmount," +
				"dtCheck, a.dtCreated, a.dtModified, moRoyaltiesPaid, moRoyaltiesOwed ";

			if (orderBy.length() > 0) {
				sql += "ORDER BY " + orderBy;
			}

			PreparedStatement ps = con.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				Royalties newRoyalties = new Royalties(rs.getInt("inArtistId"));
				newRoyalties.loadFromRs(rs);
				artists.add(newRoyalties);
			}
			rs.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return artists;
	}

	public Date getDateShipped() {
		return this.dateShipComplete;
	}

	public Date getDateCreated() {
		return this.dateCreated;
	}

	public Date getDateModified() {
		return this.dateModified;
	}

	public Date getDateCheck() {
		return this.dateCheck;
	}

	public int getId() {
		return this.id;
	}

	public int getArtistId() {
		return this.ArtistId;
	}

	public String getNameDisplay() {
		return this.nameDisplay;
	}

	public String getCheckNumber() {
		return this.CheckNumber;
	}

	public BigDecimal getCheckAmount() {
		return this.CheckAmount;
	}

	public BigDecimal getAmtPaid() {
		if (moPaid == null) {
			moPaid = new BigDecimal("0.00");
		}
		return this.moPaid;
	}

	public String getOrderId() {
		return this.vcOrderId;
	}

	public int getQuantity() {
		return this.inQty;
	}

	public String getItemDesc() {
		return this.vcItemDesc;
	}

	public String getProductVariationId() {
		return this.vcProductVariationId;
	}

	public BigDecimal getAmtOwed() {
		if (moOwed == null) {
			moOwed = new BigDecimal("0.00");
		}
		return this.moOwed;
	}

	public static ArrayList getHistory(Connection con, int in) throws SQLException {
		ArrayList history = new ArrayList();

		try {
			//            PreparedStatement ps = con.prepareStatement("SELECT * FROM tbArtistRoyalties WHERE
			// inArtistId = ? AND moCheckAmount is not null ORDER BY vcCheckNumber");
			PreparedStatement ps = con.prepareStatement("SELECT a.inId, a.vcNameDisplay, a.vcCheckNumber, a" +
				".moCheckAmount, a.dtCheck, a.dtCreated, a.dtModified, b.moRoyaltiesPaid, b.moRoyaltiesOwed from " +
				"tbArtistRoyalties a, tbArtist b WHERE a.inArtistId=b.inId AND a.inArtistId = ? AND a.moCheckAmount > " +
				"0 ORDER BY a.dtCheck DESC");
			//                        PreparedStatement ps = con.prepareStatement("SELECT * FROM tbArtistRoyalties
			// WHERE inArtistId = ? AND moCheckAmount > 0 ORDER BY dtCheck DESC");
			ps.setInt(1, in);
			ResultSet rs = ps.executeQuery();

			while (rs.next()) {
				Royalties newRoyalties = new Royalties(rs.getInt("inId"));
				newRoyalties.loadFromRs(rs);
				history.add(newRoyalties);
			}
			rs.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return history;
	}

	public static ArrayList loadAudit(Connection con, int in) throws SQLException {
		ArrayList audit = new ArrayList();

		try {
			/*
            PreparedStatement ps = con.prepareStatement("SELECT inId, vcOrderId, inQty, vcItemDesc, dtShipComplete " +
                                                        "FROM viOrderSummary " +
                                                        "WHERE inArtistId=? AND inStatus='3' " +
                                                        "ORDER BY dtShipComplete DESC"
                                                        );
             */
			PreparedStatement ps = con.prepareStatement("SELECT O.inId, O.vcOrderId, O.inQty, O.vcItemDesc, O" +
				".dtShipComplete " +
				"FROM viOrderSummary O " +
				"WHERE O.inArtistId=? AND O.inStatus='3'  " +
				"and O.dtShipComplete >= (select max(A.dtCheck)  " +
				"			from tbArtistRoyalties A " +
				"			where A.inArtistId=?) " +
				"ORDER BY O.dtShipComplete DESC");


			ps.setInt(1, in);
			ps.setInt(2, in);
			ResultSet rs = ps.executeQuery();

			while (rs.next()) {
				//                Royalties newRoyalties = new Royalties(rs.getInt("vcOrderId"));
				Royalties newRoyalties = new Royalties(rs.getInt("inId"));
				newRoyalties.loadFromRs2(rs);
				audit.add(newRoyalties);
			}
			rs.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return audit;
	}

	public void loadFromDb(Connection con) throws SQLException {
		if (getId() <= 0) {
			return;
		}
		try {
			PreparedStatement ps = con.prepareStatement("SELECT a.vcNameDisplay, a.vcCheckNumber, a.moCheckAmount, a" +
				".dtCheck, a.dtCreated, a.dtModified, b.moRoyaltiesPaid, b.moRoyaltiesOwed from tbArtistRoyalties a, " +
				"tbArtist b WHERE a.inArtistId=b.inId and inArtistID=?");
			//              PreparedStatement ps = con.prepareStatement("SELECT * from tbArtistRoyalties WHERE
			// inArtistID=?");
			ps.setInt(1, this.getId());
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				loadFromRs(rs);
			} else {
				int id = this.getId();
				this.setId(0);
				throw new Exception("Artist Id " + id + " not found");
			}
			rs.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void loadRoyaltiesFromDb(Connection con) throws SQLException {
		//    public static ArrayList loadRoyaltiesFromDb(Connection con) throws SQLException {
		if (getId() <= 0) {
			return;
		}
		try {
			PreparedStatement ps = con.prepareStatement("select sum(moCheckAmount) from tbArtistRoyalties where " +
				"moCheckAmount is not null and inArtistId=?");
			ps.setInt(1, this.getId());
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				loadFromRs(rs);
			} else {
				int id = this.getId();
				this.setId(0);
				throw new Exception("Artist Id " + id + " not found in tbArtistRoyalties");
			}
			rs.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
		//        return ps;
	}

	public void loadFromDbByNameDisplay(Connection con) throws SQLException {
		if (getNameDisplay().length() == 0) {
			return;
		}
		try {
			PreparedStatement ps = con.prepareStatement("SELECT * FROM tbArtist WHERE vcNameDisplay = ?");
			ps.setString(1, this.getNameDisplay());
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				this.setId(rs.getInt("inId"));
				loadFromRs(rs);
			} else {
				throw new Exception("Artist " + getNameDisplay() + " not found");
			}
			rs.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void loadFromRs(ResultSet rs) throws SQLException {
		this.setNameDisplay(rs.getString("vcNameDisplay"));
		this.setCheckNumber(rs.getString("vcCheckNumber"));
		this.setCheckAmount(rs.getBigDecimal("moCheckAmount"));
		this.setDateCheck(rs.getTimestamp("dtCheck"));
		this.setDateCreated(rs.getTimestamp("dtCreated"));
		this.setDateModified(rs.getTimestamp("dtModified"));
		//        this.setId(rs.getId("inId"));
		this.setAmtPaid(rs.getBigDecimal("moRoyaltiesPaid"));
		this.setAmtOwed(rs.getBigDecimal("moRoyaltiesOwed"));
	}

	//added for audit purposes:
	public void loadFromRs2(ResultSet rs) throws SQLException {
		this.setOrderId(rs.getString("vcOrderId"));
		this.setQuantity(rs.getInt("inQty"));
		this.setItemDesc(rs.getString("vcItemDesc"));
		this.setDateShipped(rs.getTimestamp("dtShipComplete"));
		//        this.setDateCreated(rs.getTimestamp("dtCreated"));
	}

	public void saveToDb(Connection con) throws SQLException {
		PreparedStatement ps;

		if (this.getId() > 0) {
			ps = con.prepareStatement("INSERT INTO tbArtistRoyalties (inArtistId, vcNameDisplay, vcCheckNumber, " +
				"moCheckAmount, dtCheck, dtModified, dtCreated) VALUES(?,?,?,?,?,CURRENT_TIMESTAMP," +
				"CURRENT_TIMESTAMP)");
		} else {
			ps = con.prepareStatement("INSERT INTO tbArtistRoyalties (inArtistId, vcNameDisplay, vcCheckNumber, " +
				"moCheckAmount, dtCheck, dtModified, dtCreated) VALUES(?,?,?,?,?,CURRENT_TIMESTAMP," +
				"CURRENT_TIMESTAMP)");
		}
		int i = 0;
		ps.setInt(++i, this.getId());
		ps.setString(++i, this.getNameDisplay());
		ps.setString(++i, this.getCheckNumber());
		ps.setBigDecimal(++i, this.getCheckAmount());
		ps.setString(++i, PiUtility.formatDate(this.getDateCheck(), "M/d/yyyy"));

		//        if (this.getId() > 0) {
		//            ps.setInt(++i,this.getId());
		//        }
		ps.execute();

	}

	public void setNameFirst(String in) {
		this.nameFirst = (in == null) ? "" : in;
	}

	public void setDateCreated(Date in) {
		this.dateCreated = in;
	}

	public void setDateModified(Date in) {
		this.dateModified = in;
	}

	public void setDateCheck(Date in) {
		this.dateCheck = in;
	}

	public void setId(int in) {
		this.id = in;
	}

	public void setArtistId(int in) {
		this.ArtistId = in;
	}

	public void setCheckNumber(String in) {
		this.CheckNumber = in;
	}

	public void setNameDisplay(String in) {
		this.nameDisplay = (in == null) ? "" : in;
	}

	public void setCheckAmount(BigDecimal in) {
		this.CheckAmount = in;
	}

	public void setAmtPaid(BigDecimal in) {
		this.moPaid = in;
	}

	public void setAmtOwed(BigDecimal in) {
		this.moOwed = in;
	}

	public void setRelationship(String in) {
		this.relationship = (in == null) ? "" : in;
	}

	public void setOrderId(String in) {
		this.vcOrderId = in;
	}

	public void setQuantity(int in) {
		this.inQty = in;
	}

	public void setItemDesc(String in) {
		this.vcItemDesc = in;
	}

	public void setProductVariationId(String in) {
		this.vcProductVariationId = in;
	}

	public void setDateShipped(Date in) {
		this.dateShipComplete = in;
	}
}
