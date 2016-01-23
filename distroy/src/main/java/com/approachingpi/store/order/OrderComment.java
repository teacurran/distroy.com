/**
 * User: Terrence Curran
 * Date: Jan 31, 2005
 * Time: 8:53:41 PM
 */

package com.approachingpi.store.order;

import com.approachingpi.PiObject;
import com.approachingpi.user.User;
//import com.approachingpi.store.order.Order;

import java.util.Date;
import java.util.ArrayList;
import java.sql.*;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.ResultSet;

public class OrderComment extends PiObject {
    String body = "";
    ArrayList comments;
    Date date;
    int id;
    Order order;
    User user;

    public OrderComment() {}
    public OrderComment(int in) {
        id = in;
    }

    public String getBody() {
    return body;
    }

    public String getBody2() {
        return body;
    }

    public Date getDate() { return date; }
    public int getId() { return id; }
    public Order getOrder() { return order; }
    public User getUser() { return user; }

    public void loadFromDb(Connection con) throws SQLException {
		PreparedStatement ps = con.prepareStatement("SELECT * FROM tbOrderComment WHERE vcOrderId = ?");
                ps.setString(1,getOrder().getId());
                //PreparedStatement ps = con.prepareStatement("SELECT * FROM tbOrderComment WHERE vcId = ?");
		//ps.setString(1,getId());
		//ps.executeQuery();
                ResultSet rs = ps.executeQuery();
		if (rs.next()) {
			loadFromRs(rs);
		}
		rs.close();


    }
    public void loadFromRs(ResultSet rs) throws SQLException {

         setOrder(new Order(rs.getString("vcOrderId")));
	setUser(new User(rs.getInt("inUserId")));
        setDate(rs.getTimestamp("dtStamp"));
	//setPrivate(rs.getInt("boPrivate"));
	setBody(rs.getString("txBody"));

    }
    public void saveToDb(Connection con) throws SQLException {
            if (getId() == 0) {
            //if (getId().length() == 0) {
            loadFromDb(con);
            //loadIdFromDb(con);
            }

            //SECTIONS OF THIS SQL STATEMENT ARE COMMENTED OUT BECAUSE IT FAILED TO SAVE THE COMMENT TO THE DB DUE TO A
            //SYNTAX ERROR NEAR WHERE STATEMENT
			String sqlStatement = "INSERT INTO tbOrderComment (" +
				"inId, vcOrderId, inUserId, dtStamp, boPrivate, txBody\n" +
				") VALUES (?, ?, ?, CURRENT_TIMESTAMP, 0, ?)\n" +
				"ON DUPLICATE KEY UPDATE\n" +
				"inUserId=?, dtStamp=CURRENT_TIMESTAMP, boPrivate=0, txBody=?";

			PreparedStatement ps = con.prepareStatement(sqlStatement);
			int i = 0;

			//INSERT
			ps.setInt(++i, this.getId());
			ps.setString(++i, this.getOrder().getId());
			if (getUser() == null) {
				ps.setNull(++i, Types.INTEGER);
			} else {
				ps.setInt(++i, this.getUser().getId());
			}
			ps.setString(++i, this.getBody());

			//UPDATE
			if (getUser() == null) {
				ps.setNull(++i, Types.INTEGER);
			} else {
				ps.setInt(++i, this.getUser().getId());
			}
			ps.setString(++i, this.getBody());


			ps.execute();
    }


    public void setBody(String in) { body = (in==null) ? "" : in; }
    public void setDate(Date in) { date = in; }
    public void setId(int in) { id = in; }
    public void setOrder(Order in) { order = in; }
    public void setUser(User in) { user = in; }
}
