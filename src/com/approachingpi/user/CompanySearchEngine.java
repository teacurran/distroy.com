package com.approachingpi.user;

import com.approachingpi.search.SearchDateSpan;
import com.approachingpi.search.SearchEngine;
import com.approachingpi.search.ResultPage;

import java.util.ArrayList;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
/**
 * Created by IntelliJ IDEA.
 * User: terrence
 * Date: Nov 11, 2004
 * Time: 7:55:04 AM
 * To change this template use File | Settings | File Templates.
 */
public class CompanySearchEngine extends SearchEngine {
    public static final int SORT_NAME           = SORT_DEFAULT;
    public static final int SORT_STATUS         = 1;
    public static final int SORT_DATE_CREATED   = 2;
    public static final int SORT_DATE_MODIFIED  = 3;
    public static final int SORT_DATE_ACTIVE    = 4;
    public static final int SORT_ID             = 5;

    public static final int DATE_SPAN_CREATED   = 0;
    public static final int DATE_SPAN_MODIFIED  = 1;

    int status = Company.STATUS_ANY;
    SearchDateSpan dateSpan;
    int sortOrder = SORT_ORDER_DEFAULT;
    String term;

    public ArrayList executeAndReturn(Connection con, int max) {
        int start = -1;
        int end = -1;

        if (rp != null) {
            start = rp.getDesiredStart();
            end = rp.getDesiredEnd();
        }

        ArrayList companies = new ArrayList(10);
        try {
            PreparedStatement ps = con.prepareStatement(getSql());
            ResultSet rs = ps.executeQuery();
            int counter=0;
            while (rs.next()) {
                counter++;
                if ((start == -1 || (start != -1 && counter >= start))
                    && (end == -1 || (end != -1 && counter <= end))) {

                    Company company = new Company(rs.getInt("inId"));
                    company.loadFromRs(rs);
                    companies.add(company);
                }
                if (max>0 && counter>max) {
                    break;
                }
            }
            rs.close();

            if (rp != null) {
                rp.setCountTotal(counter);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return companies;
    }
    public ResultPage executeReturnResultPage(Connection con) {
        ResultPage set = getResultPage();
        set.setItems(executeAndReturn(con));
        return set;
    }
    public SearchDateSpan getDateSpan() { return dateSpan; }
    public String getSql() {
        StringBuffer buffer = new StringBuffer(2000);

        buffer.append("SELECT * FROM tbCompany\n");
        if (getStatus() != Company.STATUS_ANY) {
            buffer.append(getWhereAnd());
            buffer.append("inStatus=");
            buffer.append(getStatus());
            buffer.append("\n");
        }
        if (getTerm().length() > 0) {
            buffer.append(getWhereAnd());
            buffer.append("vcName LIKE '%" + getTerm().replaceAll("'", "''") + "%'\n");
        }
        if (getDateSpan() != null) {
            SearchDateSpan span = getDateSpan();
            String field = (span.getField() == DATE_SPAN_CREATED) ? "dtCreated" : "dtModified";

            buffer.append(getWhereAnd());
            buffer.append(field);
            switch (span.getConstraint()) {
                case SearchDateSpan.DATE_ON :
                    buffer.append(" BETWEEN '" + span.getStartString() + "' AND '" + span.getEndString() + "' ");
                    break;
                case SearchDateSpan.DATE_BEFORE :
                    // before dateEnd so we are inclusive
                    buffer.append(" " + field + " <= '" + span.getEndString() + "' ");
                    break;
                case SearchDateSpan.DATE_AFTER :
                    // after dateStart so we are inclusive
                    buffer.append(" " + field + " >= '" + span.getStartString() + "' ");
                    break;
                case SearchDateSpan.DATE_BETWEEN :
                    // between dateStart and dateEnd
                    buffer.append(" " + field + " BETWEEN '" + span.getStartString() + "' AND '" + span.getEndString() + "' ");
                    break;
            }
        }
        if (getSort().size() > 0) {
            buffer.append(" ORDER BY ");
            for (int i=0; i<getSort().size(); i++) {
                Integer thisSort = (Integer)getSort().get(i);
                if (i>0) {
                    buffer.append(", ");
                }
                switch (thisSort.intValue()) {
                    case SORT_NAME :
                        buffer.append(" vcName ");
                        break;
                    case SORT_STATUS :
                        buffer.append(" inStatus");
                        break;
                    case SORT_DATE_CREATED :
                        buffer.append(" dtCreated");
                        break;
                    case SORT_DATE_MODIFIED :
                        buffer.append(" dtModified");
                        break;
                    case SORT_DATE_ACTIVE :
                        buffer.append("dtActive");
                        break;
                    case SORT_ID :
                        buffer.append("inId");
                        break;
                }
            }
            switch (getSortOrder()) {
                case SORT_ORDER_ASC :
                    buffer.append(" ASC ");
                    break;
                case SORT_ORDER_DESC :
                    buffer.append(" DESC ");
                    break;
            }
        }
        System.out.println(buffer);
        return buffer.toString();
    }
    public int getStatus() { return status; }
    public String getTerm() {
        if (term == null) {
            term = "";
        }
        return term;
    }

    public void setDateSpan(SearchDateSpan span) {
        dateSpan = span;
    }
    public void setStatus(int statusIn) {
        status = statusIn;
    }
    public void setTerm(String in) {
        term = (in==null) ? "" : in;
    }
}