/***********************************************************************************

    MODULE: FormError.java

    DESC:	This class is used to store error messages for displaying to the user.
            It is simply an Arraylist of data with some methods for accessing it.

    METHODS:
            reset()				void		- resets the List to the first error.
            hasNext()			boolean		- returns true if there are more errors in the list to be accessed.
            next()				boolean		- moves the iterator to the next error, returns true on success.
            addError(String)	void		- takes a string and adds it to the error list.
            setErrors(List)		void		- takes a List object and completely overwrites the list of errors.
            setIteratorOffset(int)	void	- sets the list iterator to the value specified
            getErrorCount()		int			- returns the total number of errors
            getIteratorIndex()	int			- returns the current placement of the iterator
            getNextError()		String		- moves the iterator to the next error and returns it
            getError()			String		- returns current error

    DATE:   7/11/2000

    AUTHOR: Tea Curran

    MODIFY: 8/21/2000 - made it not implement serilizable because it isn't going to be used as a bean.

 ***********************************************************************************
 */
package com.approachingpi.util;

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;
import java.util.ListIterator;

public class MessageBean implements Serializable {
    private ArrayList lstMessageList = new ArrayList();
    private ArrayList highlightFields;
    private ListIterator iterator;
    private String message = "";
    private boolean fatal = false;

    public MessageBean() {}
    public MessageBean(MessageBean in) {
        this.merge(in);
    }

    // takes a string and adds it to the list of errors
    public void addMessage(String in) {
        lstMessageList.add(in);
    }
    public void clear() {
        lstMessageList = new ArrayList();
    }
    public boolean getIsFatal() {
        return fatal;
    }
    // returns the number of errors in the list
    public int getMessageCount() {
        return(lstMessageList.size());
    }
    // returns the current place of the list iterator
    public int getIteratorIndex() {
        if (iterator == null) {
            reset();
        }
        return(iterator.nextIndex());
    }

    // moved to the next error and returns it
    public String getNextMessage() {
        if (next()) {
            return getMessage();
        } else {
            return "";
        }
    }

    // returns current error
    public String getMessage() {
        return message;
    }

    public void addHighlightField(String in) {
        getHighlightFields().add(in);
    }
    public ArrayList getHighlightFields() {
        if (highlightFields == null) {
            highlightFields = new ArrayList();
        }
        return highlightFields;
    }
    // checks to see if there are any more errors in the list
    public boolean hasNext() {
        if (iterator == null) {
            reset();
        }
        return(iterator.hasNext());
    }
    public boolean isFieldHighlighted(String field) {
        getHighlightFields();
        for (int i=0; i<highlightFields.size(); i++) {
            if (((String)highlightFields.get(i)).equalsIgnoreCase(field)) {
                return true;
            }
        }
        return false;
    }
    public void merge(MessageBean in) {
        if (in == null) {
            return;
        }
        in.reset();
        while (in.hasNext()) {
            this.addMessage(new String(in.getNextMessage()));
        }
	    for (int i=0; i < in.getHighlightFields().size(); i++) {
		    this.addHighlightField((String)in.getHighlightFields().get(i));
	    }
    }
    // moved the current error to the next in the list
    public boolean next() {
        if (!hasNext()) {
            return(false);
        }

        try {
            message = (String)iterator.next();
        } catch(Exception e) {
            return(false);
        }
        return(true);
    }
    // Reset the iterator to the start of the List
    public void reset() {
        this.iterator = lstMessageList.listIterator();
    }
    public void setIsFatal(boolean in) {
        fatal = in;
    }
    public void setMessages(List in) {
        this.lstMessageList = new ArrayList(in);
        this.iterator = lstMessageList.listIterator();
    }
    // Set the iterator to start at the given offset
    public void setIteratorOffset(int offset) {
        if (iterator == null) {
            reset();
        }
        int off = lstMessageList.size();
        if (offset < lstMessageList.size()) {
            off = offset;
        }

        // move iterator to offset
        try {
            this.iterator = lstMessageList.listIterator(off);
        } catch(IndexOutOfBoundsException e) {
            // just get an iterator at the begining of the list
            this.iterator = lstMessageList.listIterator();
        }
    }
}
