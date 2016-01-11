package com.approachingpi.store.order;

import com.approachingpi.store.catalog.Size;

/**
 * User: Terrence Curran
 * Date: Feb 22, 2006
 * Time: 5:08:33 PM
 * Desc:
 */
public class OrderDetailSize {
    public int qty = 0;
    public Size size;

    public OrderDetailSize(int qty, Size size) {
        setQty(qty);
        setSize(size);
    }

    public int getQty() {
        return qty;
    }

    public void setQty(int qty) {
        this.qty = qty;
    }

    public Size getSize() {
        return size;
    }

    public void setSize(Size size) {
        this.size = size;
    }
}
