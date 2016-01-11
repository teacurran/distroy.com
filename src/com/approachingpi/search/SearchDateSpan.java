/*
 * Approaching Pi, Inc.
 * User: Tea Curran
 * Date: Aug 27, 2002
 * Time: 6:28:25 PM
 */
package com.approachingpi.search;

import java.util.Date;
import java.text.SimpleDateFormat;

public class SearchDateSpan {
    public static final int DATE_BEFORE         = 0;
    public static final int DATE_ON             = 1;
    public static final int DATE_AFTER          = 2;
    public static final int DATE_BETWEEN        = 3;

    SimpleDateFormat sdf = new SimpleDateFormat("M/dd/yy");

    private Date start;
    private Date end;
    private int constraint;
    private int field;

    public SearchDateSpan() {
    }
    public SearchDateSpan(Date start,Date end) {
        setStart(start);
        setEnd(end);
    }

    public int getConstraint() {
        if (constraint == DATE_BETWEEN && getEnd() == null) {
            return DATE_AFTER;
        }
        return constraint;
    }
    public Date getEnd() {
        if (constraint == DATE_BETWEEN && end == null) {
            return start;
        }
        return end;
    }
    public String getEndString() {
        String dateEndSql = sdf.format(getEnd());
        dateEndSql = dateEndSql + " 23:59:59";
        return dateEndSql;
    }
    public int getField() { return field; }
    public Date getStart() {
        return start;
    }
    public String getStartString() {
        String dateStartSql = sdf.format(start);
        String dateStart = dateStartSql + " 00:00:00";
        return dateStart;
    }

    public void setConstraint(int in) {
        if (in >= 0 && in <= 3) {
            constraint = in;
        }
    }
    public void setEnd(Date in) {
        end = in;
    }
    public void setField(int in) { field = in; }
    public void setStart(Date in) {
        start = in;
    }

    public static String getConstraintNameById(int in) {
        switch (in) {
            case DATE_ON :
                return "on";
            case DATE_BEFORE :
                return "before";
            case DATE_AFTER :
                return "after";
            case DATE_BETWEEN :
                return "between";
            default:
                return "on";
        }
    }
}
