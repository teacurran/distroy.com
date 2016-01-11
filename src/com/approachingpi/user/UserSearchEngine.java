package com.approachingpi.user;

import com.approachingpi.search.SearchEngine;
import com.approachingpi.search.SearchDateSpan;
import com.approachingpi.search.ResultPage;

import java.util.Vector;
import java.util.ArrayList;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Approaching Pi, Inc.
 * User: Tea Curran
 * Date: Nov 19, 2004
 * Time: 5:34:39 PM
 * Desc:
 */
public class UserSearchEngine extends SearchEngine {
    public static final int SORT_NAME           = 0;
    public static final int SORT_TYPE           = 1;
    public static final int SORT_DATE_CREATED   = 2;
    public static final int SORT_DATE_MODIFIED  = 3;
    public static final int SORT_DATE_ACTIVE    = 4;
    public static final int SORT_ID             = 5;
    public static final int SORT_COMPANY        = 6;
    public static final int SORT_NAME_FIRST     = 7;
    public static final int SORT_EMAIL          = 8;
    public static final int SORT_DEFAULT        = SORT_EMAIL;

    public static final int DATE_SPAN_CREATED   = 0;
    public static final int DATE_SPAN_MODIFIED  = 1;

    public static final int USER_TYPE_ANY     = -1;

    Company company;
    SearchDateSpan dateSpan;
    String searchString = "";
    Vector sort = new Vector();
    int sortOrder = SORT_ORDER_DEFAULT;
    int type = USER_TYPE_ANY;


    public ArrayList executeAndReturn(Connection con, int max) {
        ArrayList list = new ArrayList(10);
        try {
            int start = -1;
            int end = -1;

            if (rp != null) {
                start = rp.getDesiredStart();
                end = rp.getDesiredEnd();
            }

            PreparedStatement ps = con.prepareStatement(getSql());
            ResultSet rs = ps.executeQuery();
            int counter=0;
            while (rs.next()) {
                counter++;
                if ((start == -1 || (start != -1 && counter >= start))
                    && (end == -1 || (end != -1 && counter <= end))) {
                    User user = new User(rs.getInt("inUserId"));
                    Company company = new Company(rs.getInt("inCompanyId"));
                    user.setCompany(company);
                    user.loadFromRs(rs);
                    company.loadFromRs(rs);
                    user.loadAddressesFromDb(con);
                    list.add(user);
                }

                if (max>0 && counter==max) {
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
        return list;
    }
    public ResultPage executeReturnResultPage(Connection con) {
        ResultPage set = getResultPage();
        set.setItems(executeAndReturn(con));
        return set;
    }

    public Company getCompany() {
        if (company == null) {
            company = new Company();
        }
        return company;
    }
    public SearchDateSpan getDateSpan() { return dateSpan; }
    public String getSearchString() { return searchString; }
    public String getSql() {
        this.resetWhereAnd();
        String companyJoin = "*=";
        //String billAddressJoin = "*=";

        StringBuffer buffer = new StringBuffer(2000);

        buffer.append("SELECT U.inId as inUserId, C.inId as inCompanyId, U.*, C.* FROM tbUser U, tbCompany C\n");
        if (getCompany().getId() > 0) {
            companyJoin = "=";
            buffer.append(getWhereAnd());
            buffer.append("U.inCompanyId=");
            buffer.append(getCompany().getId());
            buffer.append("\n");
        }
        buffer.append(getWhereAnd());
        buffer.append("U.inCompanyId");
        buffer.append(companyJoin);
        buffer.append("C.inId");
        buffer.append("\n");
        if (getType() != USER_TYPE_ANY) {
            buffer.append(getWhereAnd());
            buffer.append("U.smUserType=");
            buffer.append(getType());
            buffer.append("\n");
        }
        try {
            int userIdSearch = Integer.parseInt(this.getSearchString());
            if (userIdSearch > 0) {
                buffer.append(getWhereAnd());
                buffer.append("U.inId=");
                buffer.append(userIdSearch);
                buffer.append("\n");
            }
        } catch (Exception e) {
            if (this.getSearchString().length() > 0) {
                String term = getSearchString();
                String escapedTerm = term.replaceAll("'","''");
                buffer.append(getWhereAnd());
                buffer.append("U.vcEmail LIKE '%" + escapedTerm + "%'\n");

                /*
                String firstName = term;
                String lastName = term;
                int indexOfComma = term.indexOf(",");
                if (indexOfComma > 0) {
                    lastName = term.substring(0, indexOfComma);
                    firstName = term.substring(indexOfComma+1);
                } else {
                    int indexOfSpace = term.indexOf(" ");
                    if (indexOfSpace > 0) {
                        firstName = term.substring(0, indexOfSpace);
                        lastName = term.substring(indexOfSpace+1);
                    }
                }
                String firstNameEscaped = firstName.replaceAll("'","''");
                String lastNameEscaped = lastName.replaceAll("'","''");
                buffer.append(getWhereAnd());
                buffer.append("(");
                    buffer.append("B.vcNameFirst LIKE '%" + firstNameEscaped + "%'\n");
                    buffer.append("OR B.vcNameLast LIKE '%" + lastNameEscaped + "%'\n");
                buffer.append(")");
                billAddressJoin = "=";
                */
            }
        }
        //buffer.append(getWhereAnd());
        //buffer.append("U.inBillId");
        //buffer.append(billAddressJoin);
        //buffer.append("B.inId");
        //buffer.append("\n");
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
            if (getSort().size() == 0) {
                this.addSort(UserSearchEngine.SORT_DEFAULT);
            }
            for (int i=0; i<getSort().size(); i++) {
                Integer thisSort = (Integer)getSort().get(i);
                if (i>0) {
                    buffer.append(", ");
                }
                switch (thisSort.intValue()) {
                    //case SORT_NAME :
                    //    switch (getSortOrder()) {
                    //        case SORT_ORDER_ASC :
                    //            buffer.append(" B.vcNameLast ASC, B.vcNameFirst ASC ");
                    //            break;
                    //        case SORT_ORDER_DESC :
                    //            buffer.append(" B.vcNameLast DESC, B.vcNameFirst DESC ");
                    //            break;
                    //    }
                    //    break;
                    /*
                    case SORT_NAME_FIRST :
                        switch (getSortOrder()) {
                            case SORT_ORDER_ASC :
                                buffer.append(" U.vcNameFirst ASC, U.vcNameLast ASC ");
                                break;
                            case SORT_ORDER_DESC :
                                buffer.append(" U.vcNameFirst DESC, U.vcNameLast DESC ");
                                break;
                        }
                        break;
                    */
                    case SORT_TYPE :
                        buffer.append(" U.inType");
                        break;
                    case SORT_DATE_CREATED :
                        buffer.append(" U.dtCreated");
                        break;
                    case SORT_DATE_MODIFIED :
                        buffer.append(" U.dtModified");
                        break;
                    case SORT_DATE_ACTIVE :
                        buffer.append("U.dtActive");
                        break;
                    case SORT_ID :
                        buffer.append("U.inId");
                        break;
                    case SORT_EMAIL :
                        buffer.append("U.vcEmail");
                        break;
                }
                if (thisSort.intValue() != SORT_NAME && thisSort.intValue() != SORT_NAME_FIRST) {
                    switch (getSortOrder()) {
                        case SORT_ORDER_ASC :
                            buffer.append(" ASC ");
                            break;
                        case SORT_ORDER_DESC :
                            buffer.append(" DESC ");
                            break;
                    }
                }
            }
        }
        //System.out.println(buffer);
        return buffer.toString();
    }
    public int getType() {
        return type;
    }

    public void setCompany(Company in) {
        company = in;
    }
    public void setDateSpan(SearchDateSpan span) {
        dateSpan = span;
    }
    public void setSearchString(String in) { searchString = (in==null) ? "" : in; }
    public void setType(int type) {
        this.type = type;
    }
}
