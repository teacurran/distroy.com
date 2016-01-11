/*
This file can use some serious optimization
*/
package com.approachingpi.util;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.text.NumberFormat;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.Calendar;
import java.lang.String;

public class PiUtility {
    private void StringReplace(){}

    public static String replace(String strIn,String strRemove,String strReplace) {
        if (strIn == null || strRemove == null) {
            return strIn;
        }
        if (strRemove.length() == 0) {
            return strIn;
        }
        if (strReplace == null) {
            strReplace = "";
        }
        int exists = -1;
        int intCurrentPlace = 0;

        if (strIn.indexOf(strRemove) != -1) { exists = 1; }

        while (exists == 1) {
            intCurrentPlace = strIn.indexOf(strRemove,intCurrentPlace);
            String leftSide = strIn.substring(0,intCurrentPlace);
            String rightSide = strIn.substring(intCurrentPlace + strRemove.length(),strIn.length());

            strIn = leftSide + strReplace + rightSide;

            intCurrentPlace = intCurrentPlace + strReplace.length();
            if (strIn.indexOf(strRemove,intCurrentPlace) == -1) {
                exists = -1;
            }
        }
        return(strIn);
    }

	/**
	 * Method to return a datetime value as a formatted string.
	 *      ex. "MM/dd/yy" - 01/05/00
	 *          "M/d/yyyy" - 1/5/2000
	 *          "hh/mm/ss" - 12:05:39
	 *          "MM/dd/yy hh/mm/ss - 12/29/99 12:05:39
	 * @param date
	 * @param format a string indicating desired format of datetime
	 * @return
	 */
    public static String formatDate(Date date, String format) {
        // Format the date and return it.
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(date);
    }

    public static String formatDoubleDecimal(float in) {
        DecimalFormat df = new DecimalFormat();
        df.applyPattern("0.00");
        return df.format(in);
    }

  

	public static String padLeft(String in, int length) {
		if (in.length() > length) {
			return in;
		}
		String padding = "";
		for (int i=0; i<length-in.length(); i++) {
			padding = padding + " ";
		}
		return padding+in;
	}
	public static String padRight(String in, int length) {
		if (in.length() > length) {
			return in;
		}
		String padding = "";
		for (int i=0; i<length-in.length(); i++) {
			padding = padding + " ";
		}
		return in+padding;
	}
        
        public static String escapeHtml(String in){
            String lt="<"; 
            String lt2="&lt";
            String gt=">";
            String gt2="&gt";
            String quote="\"";
            String quote2="&quot";
            
            in=in.replaceAll(lt, lt2);
            in=in.replaceAll(gt, gt2);
            in=in.replaceAll(quote, quote2);
            
            return in;
        }
        
            
          public static String truncateSentence(String input, int length){

           int length2=length;

           while (length2 > 0){
               char c=(input.charAt(length));
               if (c < 'A' || c > 'z'){
                   length2=0;
               }
               else {
                   length--;
                   length2--;
               }
           }
           return input.substring(0, length);
   }

        
}

