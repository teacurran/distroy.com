/*
 * Approaching Pi, Inc.
 * User: Tea Curran
 * Date: Aug 27, 2002
 * Time: 6:28:25 PM
 */
package com.approachingpi.store.catalog;

import java.util.Date;

public class SearchDateSpan {
    public static final int DATE_BEFORE         = 0;
    public static final int DATE_ON             = 1;
    public static final int DATE_AFTER          = 2;
    public static final int DATE_BETWEEN        = 3;

    public static final int STOCK_IN_STOCK      = 0;
    public static final int STOCK_RELISTED      = 1;
    public static final int STOCK_RELEASED      = 2;
    public static final int CREATED             = 3;
    public static final int MODIFIED            = 4;
    public static final int STOCK_FORTHCOMING   = 5;
    public static final int SALE                = 6;
    public static final int STOCK_ONE_SHEET     = 7;

    private String andRaw;
    private Date start;
    private Date end;
    private int constraint;
    private int dateStock;

    public SearchDateSpan() {
    }
    public SearchDateSpan(Date start,Date end) {
        setStart(start);
        setEnd(end);
    }

    public String getAndRaw() { return andRaw; }
    public int getConstraint() {
        return constraint;
    }
    public int getDateStock() {
        return dateStock;
    }
    public static String getDateStockNameById(int in) {
        switch (in) {
            case STOCK_IN_STOCK :
                return "in stock";
            case STOCK_ONE_SHEET :
                return "one sheet";
            case STOCK_RELISTED :
                return "relisted";
            case STOCK_RELEASED :
                return "released";
            case CREATED :
                return "created";
            case MODIFIED :
                return "modified";
            case STOCK_FORTHCOMING :
                return "forthcoming";
            case SALE :
                return "sale";
            default :
                return "in stock";
        }
    }
    public Date getEnd() {
        return end;
    }
    public Date getStart() {
        return start;
    }

    public void setAndRaw(String in) { andRaw = in; }
    public void setConstraint(int in) {
        if (in >= 0 && in <= 3) {
            constraint = in;
        }
    }
    public void setDateStock(int in) {
        dateStock = in;
    }
    public void setEnd(Date in) {
        end = in;
    }
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
