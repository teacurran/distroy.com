/*
 * OrderSearch.java
 *
 * Created on September 24, 2004, 11:19 PM
 */

package com.approachingpi.store.order;

import com.approachingpi.store.Store;
import com.approachingpi.search.SearchEngine;
import com.approachingpi.search.ResultPage;
import com.approachingpi.user.User;

import java.util.Date;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 *
 * @author  Administrator
 */
public class OrderSearch extends SearchEngine {
    public static final int SORT_DATE   = 0;
    public static final int SORT_NAME   = 1;
    public static final int SORT_STATUS = 2;
    public static final int SORT_ID     = 3;
    public static final int SORT_AMOUNT = 4;

    public static final int SORT_MIN    = 0;
    public static final int SORT_MAX    = 4;
    public static final int SORT_DEFAULT= SORT_DATE;

    public static final int SORT_DESC   = 0;
    public static final int SORT_ASC    = 1;

    public static final int STATUS_ANY  = -1;
    public static final int STORE_ANY   = 0;

    Date    dateStart;
    Date    dateEnd;
    int     page        = 1;
    int     pageSize    = 20;
    int     sort        = SORT_DATE;
    int     sortOrder   = SORT_DESC;
    int     status      = STATUS_ANY;
    Store   store;
    User    user;
    /**
     * Creates a new instance of OrderSearch */
    public OrderSearch() {
    }

    public ArrayList executeAndReturn(Connection con) {
        return executeAndReturn(con,-1);
    }

    public ArrayList executeAndReturn(Connection con, int max) {
        int start = -1;
        int end = -1;

        if (rp != null) {
            start = rp.getDesiredStart();
            end = rp.getDesiredEnd();
        }
        ArrayList returnArray = new ArrayList();
        try {
            //System.out.println(getSql());
            PreparedStatement ps = con.prepareStatement(getSql());
            ResultSet rs = ps.executeQuery();
            int counter=0;
            while (rs.next()) {
                counter++;

                if ((start == -1 || (start != -1 && counter >= start))
                    && (end == -1 || (end != -1 && counter <= end))) {

                    Order order = new Order(rs.getString("vcId"));
                    order.loadFromRs(rs);
                    order.getBillAddress().setNameFirst(rs.getString("vcNameFirst"));
                    order.getBillAddress().setNameLast(rs.getString("vcNameLast"));
                    returnArray.add(order);

                    if (max>0 && returnArray.size()==max) {
                        break;
                    }
                }
            }
            rs.close();
        } catch (Exception e) {
            System.err.println("ERROR IN SEARCH ENGINE:" + getSql());
            e.printStackTrace();
        }
        return returnArray;
    }

    public ResultPage executeReturnResultPage(Connection con) {
        ResultPage set = getResultPage();
        set.setItems(executeAndReturn(con));
        return set;
    }

	public String getSql() {
		resetWhereAnd();
		StringBuffer sql = new StringBuffer(4000);
		SimpleDateFormat sdf = new SimpleDateFormat("M/dd/yy hh:mm:ss");

		sql.append("SELECT O.*, B.inId AS inBillId, B.vcNameFirst, B.vcNameLast\n");
		sql.append("FROM tbOrder O\n");
		sql.append("LEFT OUTER JOIN tbOrderAddress B ON O.inBillId = B.inId\n");

		if (getDateStart() != null && getDateEnd() != null) {
			sql.append(getWhereAnd()).append("O.dtCreated BETWEEN ").append(sdf.format(getDateStart())).append(" AND ")
				.append(sdf.format(getDateEnd())).append("\n");
		} else if (getDateStart() != null) {
			sql.append(getWhereAnd()).append("O.dtCreated >= ").append(sdf.format(getDateStart())).append("\n");
		} else if (getDateEnd() != null) {
			sql.append(getWhereAnd()).append("O.dtCreated <= ").append(sdf.format(getDateEnd())).append("\n");
		}

        if (getStatus() != STATUS_ANY) {
            sql.append(getWhereAnd()).append("O.inStatus=").append(getStatus()).append("\n");
        }

        if (getStore().getId() != STORE_ANY) {
            sql.append(getWhereAnd()).append("O.inStoreId=").append(getStore().getId()).append("\n");
        }
        if (getUser().getId() > 0) {
            sql.append(getWhereAnd()).append("O.inUserId=").append(getUser().getId()).append("\n");
        }

        sql.append("ORDER BY ");
        switch(getFirstSort()) {
            case SORT_DATE:
                sql.append("O.dtCreated ");
            break;
            case SORT_NAME:
                sql.append("B.vcNameLast ");
                switch (sortOrder) {
                    case SORT_ASC :
                        sql.append("ASC, ");
                    break;
                    default :
                        sql.append("DESC, ");
                    break;
                }
                sql.append("B.vcNameFirst ");
                switch (sortOrder) {
                    case SORT_ASC :
                        sql.append("ASC\n");
                    break;
                    default :
                        sql.append("DESC\n");
                    break;
                }
            break;
            case SORT_STATUS:
                sql.append("O.inStatus ");
            break;
            case SORT_ID:
                sql.append("O.vcId ");
            break;
            case SORT_AMOUNT:
                sql.append("O.moTotal ");
            break;
        }
        if (getFirstSort() != SORT_NAME) {
            switch (sortOrder) {
                case SORT_ASC :
                    sql.append("ASC\n");
                break;
                default :
                    sql.append("DESC\n");
                break;
            }
        }
        return sql.toString();
    }

    public Date getDateEnd() { return dateEnd; }
    public Date getDateStart() { return dateStart; }
    public int getStatus() { return status; }
    public Store getStore() {
        if (store==null) {
            store = new Store("");
        }
        return store;
    }
    public User getUser() {
        if (user == null) {
            user = new User();
        }
        return user;
    }

    public void setDateStart(Date in) {
        setDateStart(in, true);
    }
    public void setDateEnd(Date in) {
        setDateEnd(in, true);
    }
    public void setDateEnd(Date in, boolean ignoreHour) {
        if (ignoreHour || in != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(in);
            cal.set(Calendar.HOUR, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            this.dateStart = cal.getTime();
        } else {
            this.dateStart = in;
        }
    }
    public void setDateStart(Date in, boolean ignoreHour) {
        if (ignoreHour || in != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(in);
            cal.set(Calendar.HOUR, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            this.dateStart = cal.getTime();
        } else {
            this.dateStart = in;
        }
    }
    public void setStatus(int in) { this.status = in; }
    public void setStore(Store in) { this.store = in; }
    public void setUser(User in) { user = in; }
}
