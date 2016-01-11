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
            
            StringBuffer sql = new StringBuffer(5000);

            //SECTIONS OF THIS SQL STATEMENT ARE COMMENTED OUT BECAUSE IT FAILED TO SAVE THE COMMENT TO THE DB DUE TO A
            //SYNTAX ERROR NEAR WHERE STATEMENT
            sql.append("IF ((SELECT Count(*) FROM tbOrderComment WHERE inId=?) > 0) \n");
            sql.append("BEGIN\n");
            sql.append("UPDATE tbOrderComment SET\n");
            sql.append("vcOrderId=?, ");
            sql.append("inUserId=?, ");
            sql.append("dtStamp=CURRENT_TIMESTAMP, ");
            sql.append("boPrivate=0, ");
            sql.append("txBody=? ");
            sql.append("WHERE inId = ? AND vcOrderId=? AND inUserId=? \n");
            sql.append("END ELSE BEGIN\n");
            sql.append("INSERT INTO tbOrderComment (");
            sql.append("vcOrderId, inUserId, dtStamp, boPrivate, txBody ");
            sql.append(") VALUES (");
            sql.append("?,?,CURRENT_TIMESTAMP,0,?");
            sql.append(")\n");
            sql.append("END");
            
            PreparedStatement ps = con.prepareStatement(sql.toString());
            int i=0;
            
            //IF
            ps.setInt(++i, this.getId());
            
            //UPDATE
            ps.setString(++i, this.getOrder().getId());
            if (getUser() == null) {
                ps.setNull(++i, Types.INTEGER);
            } else {
                ps.setInt(++i, this.getUser().getId());
            }
            ps.setString(++i, this.getBody());
            
            //WHERE
            ps.setInt(++i, this.getId());
            ps.setString(++i, this.getOrder().getId());
            if (getUser() == null) {
                ps.setNull(++i, Types.INTEGER);
            } else {
                ps.setInt(++i, this.getUser().getId());
            }

            //INSERT
            ps.setString(++i, this.getOrder().getId());
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
