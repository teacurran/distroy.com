package com.approachingpi.store.order;

/**
 * Approaching Pi, Inc.
 * User: Tea Curran
 * Date: Nov 29, 2004
 * Time: 1:54:14 PM
 * Desc:
 */
public class PaymentException extends Exception {
    String message;
    PaymentException(String in) {
        message = in;
    }

    public String getMessage() {
        return message;
    }
}
