/**
 * User: Terrence Curran
 * Date: Feb 3, 2005
 * Time: 9:30:06 PM
 */

package com.approachingpi.user;

import com.approachingpi.PiObject;

import java.util.Date;
import java.sql.*;
import java.math.BigDecimal;
import java.math.BigInteger;

public class MailingList extends PiObject {
    Date datePromotionSent;
    Date datePromotionClaimed;
    Date datePromotionUsed;
    String email    = "";
    String referral = "";
    boolean subscribed = false;
    BigDecimal promotionValue = new BigDecimal("0.00");
    BigDecimal promotionUsed = new BigDecimal("0.00");

    public MailingList (String in) {
        this.setEmail(in);
    }

    public Date getDatePromotionClaimed() {
        return datePromotionClaimed;
    }
    public Date getDatePromotionSent() {
        return datePromotionSent;
    }
    public Date getDatePromotionUsed() {
        return datePromotionUsed;
    }
    public String getEmail() {
        return email;
    }
    public BigDecimal getPromotionAvailable() {
        return getPromotionValue().subtract(getPromotionUsed());
    }
    public BigDecimal getPromotionValue() {
        if (promotionValue == null) {
            promotionValue = new BigDecimal("0.00");
        }
        return promotionValue;
    }
    public BigDecimal getPromotionUsed() {
        if (promotionUsed == null) {
            promotionUsed = new BigDecimal("0.00");
        }
        return promotionUsed;
    }
    public String getReferral() {
        return referral;
    }
    public boolean isSubscribed() {
        return subscribed;
    }

    public void loadFromDb(Connection con) throws SQLException {
        PreparedStatement ps = con.prepareStatement("SELECT * FROM tbMailingList WHERE vcEmail = ?");
        ps.setString(1,getEmail());
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            loadFromRs(rs);
        }
        rs.close();
    }

    public void loadFromRs(ResultSet rs) throws SQLException {
        setDatePromotionClaimed(new Date(rs.getTimestamp("dtPromotionClaimed").getTime()));
        setDatePromotionSent(new Date(rs.getTimestamp("dtPromotionSent").getTime()));
        setDatePromotionUsed(new Date(rs.getTimestamp("dtPromotionUsed").getTime()));
        setReferral(rs.getString("vcReferral"));
        setSubscribed(rs.getBoolean("btSubscribed"));
        setPromotionUsed(rs.getBigDecimal("moPromotionUsed"));
    }

    public void loadPromotionValue(Connection con) throws SQLException {
        PreparedStatement ps = con.prepareStatement("SELECT Count(*) AS inCount FROM tbMailingList WHERE vcReferral = ? AND dtPromotionClaimed = null");
        ps.setString(1,getEmail());
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            int count = rs.getInt("inCount");
            double value = 1.00;
            double totalValue = count * value;
            this.setPromotionValue(new BigDecimal(totalValue));
        }
        rs.close();
    }

    public void saveToDb(Connection con) throws SQLException {
        PreparedStatement ps = con.prepareStatement("IF ((SELECT COUNT(*) FROM tbMailingList WHERE vcEmail = ?) = 0)\n"+
                "BEGIN\n"+
                    "INSERT INTO tbMailingList (vcEmail, btSubscribed) VALUES(?,?)\n"+
                "END ELSE BEGIN\n"+
                    "UPDATE tbMailingList SET "+
                    "vcEmail=?, btSubscribed=?, dtPromotionSent=?, dtPromotionClaimed=?, dtPromotionUsed=?, moPromotionUsed=?\n"+
                "END");
        int i=0;
        // IF
        ps.setString(++i,getEmail());

        // INSERT
        ps.setString(++i,getEmail());
        ps.setBoolean(++i, isSubscribed());

        // UPDATE
        ps.setString(++i,getEmail());
        ps.setBoolean(++i, isSubscribed());
        if (this.getDatePromotionSent() == null) {
            ps.setNull(++i, Types.TIMESTAMP);
        } else {
            ps.setTimestamp(++i, new Timestamp(getDatePromotionSent().getTime()));
        }
        if (this.getDatePromotionClaimed() == null) {
            ps.setNull(++i, Types.TIMESTAMP);
        } else {
            ps.setTimestamp(++i, new Timestamp(getDatePromotionClaimed().getTime()));
        }
        if (this.getDatePromotionUsed() == null) {
            ps.setNull(++i, Types.TIMESTAMP);
        } else {
            ps.setTimestamp(++i, new Timestamp(getDatePromotionUsed().getTime()));
        }
        ps.setBigDecimal(++i, this.getPromotionUsed());
        ps.execute();
    }

    public void setDatePromotionSent(Date datePromotionSent) {
        this.datePromotionSent = datePromotionSent;
    }
    public void setDatePromotionClaimed(Date datePromotionClaimed) {
        this.datePromotionClaimed = datePromotionClaimed;
    }
    public void setDatePromotionUsed(Date datePromotionUsed) {
        this.datePromotionUsed = datePromotionUsed;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public void setPromotionUsed(BigDecimal in) {
        promotionUsed = in;
    }
    public void setPromotionValue(BigDecimal in) {
        promotionValue = in;
    }
    public void setReferral(String referral) {
        this.referral = referral;
    }
    public void setSubscribed(boolean subscribed) {
        this.subscribed = subscribed;
    }

}
