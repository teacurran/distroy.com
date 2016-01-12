/*
 * (c)Approaching Pi, Inc.
 * User: terrence
 * Date: Jul 3, 2004
 * Time: 8:44:08 PM
 * Desc:    Alt attribute is a store to hold objects that cannot be saved
 *          with HttpServletRequest.setAttribute.  This is because the
 *          Jsp 2.0 spec does not allow you to use <jsp:useBean> tag with
 *          any object that does not have a default (no-args) constructor.
 *          As a convenience, this class can also hold primitaves.
 *
 */
package com.approachingpi.servlet;

import java.util.HashMap;

// TODO this class has a bug where you can insert a null as a value and it will fail when you try to retreive the value

public class AltAttribute extends HashMap {

    public void put(Object key, boolean value) {
        this.put(key,new Boolean(value));
    }
    public void put(Object key, int value) {
        this.put(key,new Integer(value));

    }
    public boolean getBoolean(Object key) {
        boolean retVal = false;
        if (this.containsKey(key))  {
            // System.out.println(this.get(key).getClass());
            if (this.get(key).getClass().equals(Boolean.class)) {
                retVal = ((Boolean)this.get(key)).booleanValue();
            } else {
                try {
                    retVal = Boolean.getBoolean((String)this.get(key));
                } catch (Exception e) {
                }
            }
        }
        return retVal;
    }
    public int getInt(Object key) {
        int retVal = 0;
        if (this.containsKey(key)) {
            if (this.get(key).getClass().getSuperclass().equals(Number.class)) {
                retVal = ((Number)this.get(key)).intValue();
            } else {
                try {
                    retVal = Integer.parseInt((String)this.get(key));
                } catch (Exception e) {
                }
            }
        }
        return retVal;
    }
    public Integer getInteger(Object key) {
        Integer retVal = null;
        if (this.containsKey(key)) {
            if (this.get(key).getClass().getSuperclass().equals(Number.class)) {
                retVal = new Integer(((Number)this.get(key)).intValue());
            }
        }
        if (retVal == null) {
            retVal = new Integer(0);
        }
        return retVal;
    }
	public String getString(Object key) {
		String retVal = "";
		if (this.containsKey(key)) {
			if (this.get(key).getClass().equals(String.class)) {
				retVal = (String)this.get(key);
			}
		}
    	return retVal;
	}
}
