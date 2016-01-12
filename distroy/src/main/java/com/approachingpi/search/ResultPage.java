package com.approachingpi.search;

import java.util.ArrayList;
import java.util.ListIterator;

/**
 * Created by IntelliJ IDEA.
 * User: terrence
 * Date: Nov 10, 2004
 * Time: 6:39:24 PM
 * To change this template use File | Settings | File Templates.
 */
public class ResultPage implements IResultPage {
    protected int total = 0;
    private int desiredEnd = -2;
    int page            = 1;
    int pageSize        = 15;
    int totalResults    = 0;
    int start           = 0;
    ArrayList items   = new ArrayList();
    protected ListIterator itemsIterator;

    public ResultPage() {
        start = -1;
        total = -1;
    }

    public void addItem(Object in) {
        this.items.add(in);
    }

    public boolean hasNext() {
        if (itemsIterator == null) {
            reset();
        }
        return itemsIterator.hasNext();
    }

    public Object next() {
        if (itemsIterator == null) {
            reset();
        }
        if (itemsIterator.hasNext()) {
            return itemsIterator.next();
        } else {
            return null;
        }
    }

    public void reset() {
        itemsIterator = items.listIterator();
    }

    public void setCountTotal(int in) { this.total = in; }

    public void setPage(int in) { this.page = in; }

    public void setPageSize (int in) { this.pageSize = in; }

    public void setItems (ArrayList in) { this.items = in; }

    public void setStart(int inStart) { this.start = inStart; }

    public ArrayList getItems() {
        return this.items;
    }

    public int getCountTotal() {
        if (this.total == -1) {
            return this.items.size();
        } else {
            return this.total;
        }
    }

    public int getPage() {
        if (page > getPageCount()) {
            page = getPageCount();
        }
        return this.page;
    }

    public int getCountCurrent() { return this.items.size(); }

    public int getDesiredEnd() {
        if (this.desiredEnd == -1) {
            return desiredEnd;
        } else {
            return getDesiredStart() + getPageSize()-1;
        }
    }

    public int getEnd() {
        int end = getStart() + this.items.size() - 1;
        if (end < 0) {
            end = 0;
        }
        return end;
    }

    public int getPageCount() {
        // we get the page count by divind the total number of records by the page size
        int totalPages = getCountTotal() / getPageSize();
        // if there is a remainder in the division it is because the last page has
        // less than the page size of products.  up the page count by 1
        if ((getCountTotal() % getPageSize()) > 0) {
            totalPages++;
        }
        return totalPages;
    }

    public int getPageSize() { return this.pageSize; }

    public int getDesiredStart() {
        if (this.page > 0 && this.pageSize > 0) {
            return ((this.page * this.pageSize) - this.pageSize + 1);
        } else {
            return 0;
        }
    }

    public int getStart() {
        if (this.start == -1) {
            if (this.page > 0 && this.pageSize > 0 && getCountTotal() > 0) {
                return ((this.page * this.pageSize) - this.pageSize + 1);
            } else {
                return 0;
            }
        } else {
            return this.start;
        }
    }
}
