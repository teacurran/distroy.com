/*
 * © Approaching Pi, Inc.
 * User: terrence
 * Date: Jul 8, 2004
 * Time: 4:19:11 AM
 * Desc:
 *
 */
package com.approachingpi.util;

import java.util.HashMap;

public class NotNullHash extends HashMap {
    public Object get(Object key) {
        if (super.get(key) == null) {
            return "";
        } else {
            return super.get(key);
        }
    }
}
