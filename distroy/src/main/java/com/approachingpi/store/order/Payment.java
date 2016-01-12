package com.approachingpi.store.order;

import com.Verisign.payment.PFProAPI;
import com.approachingpi.servlet.AltAttribute;
import com.approachingpi.store.Defines;
import com.approachingpi.store.Store;
import com.approachingpi.user.Address;
import com.approachingpi.user.User;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;
import java.net.URLEncoder;
import java.net.URL;
import java.net.URLConnection;
import java.io.UnsupportedEncodingException;
import java.io.DataOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * User: terrence
 * Date: Aug 26, 2004
 * Time: 12:21:56 PM
 */
public class Payment {
	public static final String TYPE_CC          = "cc";
	public static final String TYPE_CHECK       = "ck";
	public static final String TYPE_PO          = "po";
	public static final String TYPE_COD         = "cod";
	public static final String TYPE_SCREDIT     = "sc";
	public static final String TYPE_GIFTCERT    = "gc";
	public static final String TYPE_PAYPAL      = "pp";
	public static final String TYPE_DEFAULT     = TYPE_CC;

	public static final String[] TYPES = { TYPE_CC, TYPE_CHECK, TYPE_PO, TYPE_COD, TYPE_SCREDIT, TYPE_GIFTCERT, TYPE_PAYPAL };

    public static final String CC_TEST_NUM      = "1111222233334444";

	protected String        accountNumber   = "";
	protected BigDecimal    amount                  = new BigDecimal("0.00");
    protected BigDecimal    amountHeld              = new BigDecimal("0.00");
    protected BigDecimal    amountReturned          = new BigDecimal("0.00");
    protected BigDecimal    amountSettled           = new BigDecimal("0.00");
    protected BigDecimal    amountSettledSubtotal   = new BigDecimal("0.00");
    protected BigDecimal    amountSettledShipping   = new BigDecimal("0.00");
    protected BigDecimal    amountSettledTax        = new BigDecimal("0.00");
	protected String        creditName      = "";
	protected int           ccvNumber;
	protected boolean       cvvEntered;
	protected int           id;
	protected int           expireMonth;
	protected int           expireYear;
	protected Store         store;
	protected String        type            = TYPE_DEFAULT;
	protected User          user;
	protected ArrayList     transactions    = new ArrayList();
	protected String        errorMessage    = "";
	protected Order         order;
	protected Date          dateHeld;
	protected Date          dateSettled;
	protected Date          dateVoided;
    protected String        ip = "";
    protected String        resultReason = "";

	public Payment() {}
	public Payment(int id) {
		setId(id);
	}
	public Payment(String type) {
		setType(type);
	}

	// TRANSACTION METHODS
	public void authorize(Connection con, Defines defines) throws PaymentException {
        boolean retVal = true;
		try {
			this.saveToDb(con);
		} catch (Exception e) { e.printStackTrace(); }
		if (getType().equalsIgnoreCase(TYPE_CC)) {
			Address billing = getUser().getActiveBillingAddress();

			PaymentTransaction newTransaction = new PaymentTransaction();
			newTransaction.setType(PaymentTransaction.TYPE_HOLD);
			newTransaction.setPayment(this);
			try {
				newTransaction.loadIdFromDb(con);
			} catch (Exception e) {
				e.printStackTrace();
			}


			String address      = defines.getProperty("authorize.host"); // test-payflow.verisign.com
			int port            = defines.getPropertyInt("authorize.port"); // 443
			String password     = defines.getProperty("authorize.key");
			String pfUser       = defines.getProperty("authorize.user");

            StringBuffer params = new StringBuffer(500);

            // mandatory name/value pairs for all AIM CC transactions
            // as well as some "good to have" values

            params.append("x_login=").append(pfUser);             // replace with your own
            params.append("&x_tran_key=").append(password);     // replace with your own
            params.append("&x_version=3.1");
            params.append("&x_method=CC&");
            params.append("&x_delim_data=TRUE");
            params.append("&x_delim_char=|");
            params.append("&x_relay_response=FALSE");

            //params.append("&x_test_request=TRUE&");             // for testing
            params.append("&x_type=AUTH_ONLY");
            params.append("&x_amount=").append(this.getAmount().toString());
            params.append("&x_card_num=").append(this.getAccountNumber());
            params.append("&x_exp_date=").append(this.getExpireString());
            if (getCcvNumber() > 0) {
                if (Integer.toString(getCcvNumber()).length() == 2) {
                    params.append("&x_card_code=").append("0").append(Integer.toString(this.getCcvNumber()));
                } else {
                    params.append("&x_card_code=").append(Integer.toString(this.getCcvNumber()));
                }
            }

            try {
                params.append("&x_cust_id=").append(getUser().getId());
                if (ip.length() > 0) {
                    params.append("&x_customer_ip=").append(this.ip);
                }

                params.append("&x_first_name=").append(URLEncoder.encode(billing.getNameFirst(), "UTF-8"));
                params.append("&x_last_name=").append(URLEncoder.encode(billing.getNameLast(), "UTF-8"));
                params.append("&x_address=").append(URLEncoder.encode(billing.getAddress1(), "UTF-8"));
                params.append("&x_city=").append(URLEncoder.encode(billing.getCity(), "UTF-8"));
                params.append("&x_state=").append(URLEncoder.encode(billing.getState().getAbbrev(), "UTF-8"));
                params.append("&x_zip=").append(URLEncoder.encode(billing.getZip(), "UTF-8"));
            } catch (UnsupportedEncodingException usee) {
                usee.printStackTrace();
            }

            try{
                System.out.println("Authorize-host=" + address);
                System.out.println("Authorize-params=" + params.toString());

                // open secure connection
                URL url = new URL(address);
                //  Uncomment the line ABOVE for test accounts or BELOW for live merchant accounts
                //   https://secure.authorize.net/gateway/transact.dll

                /* NOTE: If you want to use SSL-specific features,change to:
                HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                */

                URLConnection connection = url.openConnection();
                connection.setDoOutput(true);
                connection.setUseCaches(false);

                // not necessarily required but fixes a bug with some servers
                connection.setRequestProperty("Content-Type","application/x-www-form-urlencoded");

                // POST the data in the string buffer
                DataOutputStream out = new DataOutputStream( connection.getOutputStream() );
                out.write(params.toString().getBytes());
                out.flush();
                out.close();


                // process and read the gateway response
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                line = in.readLine();
                in.close();	                     // no more data

                System.out.println("Authorize-result:" + line);
                newTransaction.setResponseMessage(line);

                // ONLY FOR THOSE WHO WANT TO CAPTURE GATEWAY RESPONSE INFORMATION
                // make the reply readable (be sure to use the x_delim_char for the split operation)
                String response[] = line.split("\\|", -1);

                AltAttribute results = new AltAttribute();
                results.put("result", response[0]);
                results.put("resultsub", response[1]);
                results.put("resultreasoncode", response[2]);
                results.put("resultreasontext", response[3]);
                results.put("authcode", response[4]);
                results.put("avsaddr", response[5]);
                results.put("pnref", response[6]);
                results.put("cvv2match", response[38]);

                newTransaction.setResult(results.getInt("result"));
                newTransaction.setAuthCode(results.getString("authcode"));
                newTransaction.setPnref(results.getString("pnref"));
                newTransaction.setAvsAddress(results.getString("avsaddr"));
                newTransaction.setAvsI(results.getString("iavs"));
                newTransaction.setAvsZip(results.getString("avszip"));
                newTransaction.setCvv2Match(results.getString("cvv2match"));
                newTransaction.setAmount(getAmount());

                resultReason = (String)results.get("resultreasontext");

                if (results.containsKey("result") && !results.get("result").equals("1")) {
                    // cc auth failed
                    this.errorMessage = "There was a problem with the credit card you entered.  Please check the card number and expiration and try again.";
                    retVal = false;
                } else {
                    // set the success flag to true
                    this.setDateHeld(Calendar.getInstance().getTime());
                    this.setAmountHeld(getAmount());
                }

            }catch(Exception e){
                e.printStackTrace();
            }

            // save the payment and the transaction regardless of the status.
            if (retVal) {
                try {
                    this.saveToDb(con);
                    newTransaction.saveToDb(con);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (retVal == false) {
                throw new PaymentException(this.errorMessage);
            }

		} else if (getType().equalsIgnoreCase(TYPE_CHECK) ||
		    getType().equalsIgnoreCase(TYPE_PAYPAL) ||
		    getType().equalsIgnoreCase(TYPE_PO) ||
		    getType().equalsIgnoreCase(TYPE_COD)) {

			PaymentTransaction newTransaction = new PaymentTransaction();
			newTransaction.setType(PaymentTransaction.TYPE_PENDING);
			newTransaction.setPayment(this);
            newTransaction.setAmount(getAmount());

            this.setDateHeld(Calendar.getInstance().getTime());
            this.setAmountHeld(getAmount());

			try {
                this.saveToDb(con);
				newTransaction.saveToDb(con);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
    public void capture(Connection con, Defines defines, BigDecimal captureAmountSubtotal, BigDecimal captureAmountShipping,  BigDecimal captureAmountTax) throws Exception {
        capture(con,defines, captureAmountSubtotal, captureAmountShipping, captureAmountTax ,null);
    }
	public void capture(Connection con, Defines defines, BigDecimal captureAmountSubtotal, BigDecimal captureAmountShipping, BigDecimal captureAmountTax, String reference) throws Exception {
        if (captureAmountShipping == null) {
            captureAmountShipping = new BigDecimal("0");
        }
        if (captureAmountSubtotal == null) {
            captureAmountSubtotal = new BigDecimal("0");
        }
        if (captureAmountTax == null) {
            captureAmountTax = new BigDecimal("0");
        }
		PaymentTransaction hold = getTransactionOfType(PaymentTransaction.TYPE_HOLD);
		PaymentTransaction capture = getTransactionOfType(PaymentTransaction.TYPE_CAPTURE);

        BigDecimal captureAmount = captureAmountShipping.add(captureAmountSubtotal).add(captureAmountTax);

        if (getType().equalsIgnoreCase(TYPE_CC)) {
            // make sure we have already held an amount, but not captured anything
            if (hold != null && capture == null) {
                if (hold.getAmount().compareTo(captureAmount) < 0) {
                    throw new Exception ("Capture amount must be less than or equal to hold amount.");
                }

            	Address billing = getUser().getActiveBillingAddress();

            	PaymentTransaction newTransaction = new PaymentTransaction();
            	newTransaction.setType(PaymentTransaction.TYPE_CAPTURE);
            	newTransaction.setPayment(this);
            	try {
            	    newTransaction.loadIdFromDb(con);
            	} catch (Exception e) {
            	    e.printStackTrace();
            	}

            	String address      = defines.getProperty("authorize.host"); // test-payflow.verisign.com
            	int port            = defines.getPropertyInt("authorize.port"); // 443
            	String password     = defines.getProperty("authorize.key");
            	String pfUser       = defines.getProperty("authorize.user");

            	StringBuffer params = new StringBuffer(500);

            	// mandatory name/value pairs for all AIM CC transactions
            	// as well as some "good to have" values

            	params.append("x_login=").append(pfUser);             // replace with your own
            	params.append("&x_tran_key=").append(password);     // replace with your own
            	params.append("&x_version=3.1");
            	params.append("&x_method=CC&");
            	params.append("&x_delim_data=TRUE");
            	params.append("&x_delim_char=|");
            	params.append("&x_relay_response=FALSE");

            	//params.append("&x_test_request=TRUE&");             // for testing
            	params.append("&x_type=PRIOR_AUTH_CAPTURE");
            	params.append("&x_amount=").append(captureAmount.toString());

            	try {
					params.append("&x_trans_id=").append(URLEncoder.encode(hold.getPnRef(), "UTF-8"));
            	} catch (UnsupportedEncodingException usee) {
            	    usee.printStackTrace();
            	}

            	try{
            	    System.out.println("Authorize-host=" + address);
            	    System.out.println("Authorize-params=" + params.toString());

                	// open secure connection
                	URL url = new URL(address);
                	//  Uncomment the line ABOVE for test accounts or BELOW for live merchant accounts
                	//   https://secure.authorize.net/gateway/transact.dll

                	/* NOTE: If you want to use SSL-specific features,change to:
                	HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                	*/

                	URLConnection connection = url.openConnection();
                	connection.setDoOutput(true);
                	connection.setUseCaches(false);

                	// not necessarily required but fixes a bug with some servers
                	connection.setRequestProperty("Content-Type","application/x-www-form-urlencoded");

                	// POST the data in the string buffer
                	DataOutputStream out = new DataOutputStream( connection.getOutputStream() );
                	out.write(params.toString().getBytes());
                	out.flush();
                	out.close();

                	// process and read the gateway response
                	BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                	String line;
                	line = in.readLine();
                	in.close();	                     // no more data

                	System.out.println("Authorize-result:" + line);
                	newTransaction.setResponseMessage(line);

                	// ONLY FOR THOSE WHO WANT TO CAPTURE GATEWAY RESPONSE INFORMATION
                	// make the reply readable (be sure to use the x_delim_char for the split operation)
                	String response[] = line.split("\\|", -1);

                	AltAttribute results = new AltAttribute();
                	results.put("result", response[0]);
                	results.put("resultsub", response[1]);
                	results.put("resultreasoncode", response[2]);
                	results.put("resultreasontext", response[3]);
                	results.put("authcode", response[4]);
                	results.put("avsaddr", response[5]);
                	results.put("pnref", response[6]);
                	results.put("cvv2match", response[38]);

                	newTransaction.setResult(results.getInt("result"));
                	newTransaction.setAuthCode(results.getString("authcode"));
                	newTransaction.setPnref(results.getString("pnref"));
                	newTransaction.setAvsAddress(results.getString("avsaddr"));
                	newTransaction.setAvsI(results.getString("iavs"));
                	newTransaction.setAvsZip(results.getString("avszip"));
                	newTransaction.setCvv2Match(results.getString("cvv2match"));
                	newTransaction.setAmount(getAmount());

                	if (results.containsKey("result") && !results.get("result").equals("1")) {
                	    // cc auth failed
                    	throw new PaymentException("There was a problem with the credit card you entered.  Please check the card number and expiration and try again.");
                	} else {
                    	// set the success flag to true
                    	this.setDateSettled(Calendar.getInstance().getTime());
                    	this.setAmountCaptured(captureAmount);
                    	this.setAmountCapturedShipping(captureAmountShipping);
                    	this.setAmountCapturedSubtotal(captureAmountSubtotal);
                    	this.setAmountCapturedTax(captureAmountTax);
                	}

                	// save the payment and the transaction regardless of the status.
                	try {
                	    this.saveToDb(con);
                	    newTransaction.saveToDb(con);
                	} catch (Exception e) {
                	    e.printStackTrace();
                	}
            	}catch(Exception e){
            	    e.printStackTrace();
            	}

            	// save the payment and the transaction regardless of the status.
            	try {
            	    this.saveToDb(con);
            	    newTransaction.saveToDb(con);
            	} catch (Exception e) {
            	    e.printStackTrace();
            	}
            } else {
                throw new Exception ("No hold could be found for this payment.");
			}
        } else if (getType().equalsIgnoreCase(TYPE_CHECK) ||
            getType().equalsIgnoreCase(TYPE_PAYPAL) ||
            getType().equalsIgnoreCase(TYPE_PO) ||
            getType().equalsIgnoreCase(TYPE_COD)) {

            PaymentTransaction newTransaction = new PaymentTransaction();
            newTransaction.setType(PaymentTransaction.TYPE_CAPTURE);
            newTransaction.setPayment(this);
            newTransaction.setAmount(captureAmount);
            newTransaction.setPnref(reference);

            this.setDateSettled(Calendar.getInstance().getTime());
            this.setAmountCaptured(captureAmount);
            this.setAmountCapturedShipping(captureAmountShipping);
            this.setAmountCapturedSubtotal(captureAmountSubtotal);
            this.setAmountCapturedTax(captureAmountTax);


            try {
                this.saveToDb(con);
                newTransaction.saveToDb(con);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
	}
    public void directSale(Connection con, Defines defines) throws PaymentException {
        directSale(con, defines, this.getAmount(), null, null, "");
    }
    public void directSale(Connection con, Defines defines, BigDecimal captureAmountSubtotal, BigDecimal captureAmountShipping, BigDecimal captureAmountTax, String reference) throws PaymentException {
        if (captureAmountShipping == null) {
            captureAmountShipping = new BigDecimal("0");
        }
        if (captureAmountSubtotal == null) {
            captureAmountSubtotal = new BigDecimal("0");
        }
        if (captureAmountTax == null) {
            captureAmountTax = new BigDecimal("0");
        }
        BigDecimal captureAmount = captureAmountShipping.add(captureAmountSubtotal).add(captureAmountTax);

        boolean retVal = true;
		try {
			this.saveToDb(con);
		} catch (Exception e) { e.printStackTrace(); }
		if (getType().equalsIgnoreCase(TYPE_CC)) {
			Address billing = getUser().getActiveBillingAddress();

			PaymentTransaction newTransaction = new PaymentTransaction();
			newTransaction.setType(PaymentTransaction.TYPE_SALE);
			newTransaction.setPayment(this);
			try {
				newTransaction.loadIdFromDb(con);
			} catch (Exception e) {
				e.printStackTrace();
			}

			String address      = defines.getProperty("authorize.host"); // test-payflow.verisign.com
			int port            = defines.getPropertyInt("authorize.port"); // 443
			String password     = defines.getProperty("authorize.key");
			String pfUser       = defines.getProperty("authorize.user");

            StringBuffer params = new StringBuffer(500);

            // mandatory name/value pairs for all AIM CC transactions
            // as well as some "good to have" values

            params.append("x_login=").append(pfUser);             // replace with your own
            params.append("&x_tran_key=").append(password);     // replace with your own
            params.append("&x_version=3.1");
            params.append("&x_method=CC&");
            params.append("&x_delim_data=TRUE");
            params.append("&x_delim_char=|");
            params.append("&x_relay_response=FALSE");

            //params.append("&x_test_request=TRUE&");             // for testing
            params.append("&x_type=AUTH_CAPTURE");
            params.append("&x_amount=").append(captureAmount.toString());
            params.append("&x_card_num=").append(this.getAccountNumber());
            params.append("&x_exp_date=").append(this.getExpireString());
            if (getCcvNumber() > 0) {
                if (Integer.toString(getCcvNumber()).length() == 2) {
                    params.append("&x_card_code=").append("0").append(Integer.toString(this.getCcvNumber()));
                } else {
                    params.append("&x_card_code=").append(Integer.toString(this.getCcvNumber()));
                }
            }

            try {
                params.append("&x_cust_id=").append(getUser().getId());
                if (ip.length() > 0) {
                    params.append("&x_customer_ip=").append(this.ip);
                }

                params.append("&x_first_name=").append(URLEncoder.encode(billing.getNameFirst(), "UTF-8"));
                params.append("&x_last_name=").append(URLEncoder.encode(billing.getNameLast(), "UTF-8"));
                params.append("&x_address=").append(URLEncoder.encode(billing.getAddress1(), "UTF-8"));
                params.append("&x_city=").append(URLEncoder.encode(billing.getCity(), "UTF-8"));
                params.append("&x_state=").append(URLEncoder.encode(billing.getState().getAbbrev(), "UTF-8"));
                params.append("&x_zip=").append(URLEncoder.encode(billing.getZip(), "UTF-8"));
            } catch (UnsupportedEncodingException usee) {
                usee.printStackTrace();
            }

            try{
                System.out.println("Authorize-host=" + address);
                System.out.println("Authorize-params=" + params.toString());

                // open secure connection
                URL url = new URL(address);
                //  Uncomment the line ABOVE for test accounts or BELOW for live merchant accounts
                //   https://secure.authorize.net/gateway/transact.dll

                /* NOTE: If you want to use SSL-specific features,change to:
                HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                */

                AltAttribute results = new AltAttribute();
                if (!this.getAccountNumber().equals(CC_TEST_NUM)) {
                    URLConnection connection = url.openConnection();
                    connection.setDoOutput(true);
                    connection.setUseCaches(false);

                    // not necessarily required but fixes a bug with some servers
                    connection.setRequestProperty("Content-Type","application/x-www-form-urlencoded");

                    // POST the data in the string buffer
                    DataOutputStream out = new DataOutputStream( connection.getOutputStream() );
                    out.write(params.toString().getBytes());
                    out.flush();
                    out.close();


                    // process and read the gateway response
                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String line;
                    line = in.readLine();
                    in.close();	                     // no more data

                    System.out.println("Authorize-result:" + line);
                    newTransaction.setResponseMessage(line);

                    // ONLY FOR THOSE WHO WANT TO CAPTURE GATEWAY RESPONSE INFORMATION
                    // make the reply readable (be sure to use the x_delim_char for the split operation)
                    String response[] = line.split("\\|", -1);

                    results.put("result", response[0]);
                    results.put("resultsub", response[1]);
                    results.put("resultreasoncode", response[2]);
                    results.put("resultreasontext", response[3]);
                    results.put("authcode", response[4]);
                    results.put("avsaddr", response[5]);
                    results.put("pnref", response[6]);
                    results.put("cvv2match", response[38]);
                } else {
                    results.put("result", "1");
                    results.put("resultsub", "0");
                    results.put("resultreasoncode", "");
                    results.put("resultreasontext", "");
                    results.put("authcode", "TEST");
                    results.put("avsaddr", "X");
                    results.put("pnref", "TEST");
                    results.put("cvv2match", "Y");
                }

                resultReason = (String)results.get("resultreasontext");

                newTransaction.setResult(results.getInt("result"));
                newTransaction.setAuthCode(results.getString("authcode"));
                newTransaction.setPnref(results.getString("pnref"));
                newTransaction.setAvsAddress(results.getString("avsaddr"));
                newTransaction.setAvsI(results.getString("iavs"));
                newTransaction.setAvsZip(results.getString("avszip"));
                newTransaction.setCvv2Match(results.getString("cvv2match"));
                newTransaction.setAmount(getAmount());

                if (results.containsKey("result") && !results.get("result").equals("1")) {
                    // cc auth failed
                    this.errorMessage = "There was a problem with the credit card you entered.  Please check the card number and expiration and try again.";
                    retVal = false;
                } else {
                    // set the success flag to true
					this.setDateHeld(Calendar.getInstance().getTime());
                	this.setDateSettled(this.getDateHeld());
                	this.setAmountHeld(getAmount());
                    this.setAmountCaptured(captureAmount);
                    this.setAmountCapturedShipping(captureAmountShipping);
                    this.setAmountCapturedSubtotal(captureAmountSubtotal);
                    this.setAmountCapturedTax(captureAmountTax);
                }

            }catch(Exception e){
                e.printStackTrace();
            }

            // save the payment and the transaction regardless of the status.
            try {
                this.saveToDb(con);
                newTransaction.saveToDb(con);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (retVal == false) {
                throw new PaymentException(this.errorMessage);
            }

		} else if (getType().equalsIgnoreCase(TYPE_CHECK) ||
		    getType().equalsIgnoreCase(TYPE_PAYPAL) ||
		    getType().equalsIgnoreCase(TYPE_PO) ||
		    getType().equalsIgnoreCase(TYPE_COD)) {

			PaymentTransaction newTransaction = new PaymentTransaction();
			newTransaction.setType(PaymentTransaction.TYPE_SALE);
			newTransaction.setPayment(this);
            newTransaction.setAmount(getAmount());
            newTransaction.setPnref(reference);

            this.setDateHeld(Calendar.getInstance().getTime());
            this.setDateSettled(this.getDateHeld());
            this.setAmountHeld(getAmount());
            this.setAmountCaptured(getAmount());

			try {
                this.saveToDb(con);
				newTransaction.saveToDb(con);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

    }

	public void addTransaction(PaymentTransaction in) {
		if (in != null) {
			transactions.add(in);
		}
	}

	public String getAccountNumber() { return accountNumber; }
	public String getAccountNumberTail() {
        if (getAccountNumber().length() < 4) {
            return getAccountNumber();
        } else {
            return getAccountNumber().substring(getAccountNumber().length() - 4);
        }
    }
	public BigDecimal getAmount() {
		if (this.amount == null) {
			amount = new BigDecimal("0.00");
		}
		return this.amount.setScale(2,BigDecimal.ROUND_HALF_EVEN);
	}
    public BigDecimal getAmountHeld() {
		if (this.amountHeld == null) {
			amountHeld = new BigDecimal("0.00");
		}
		return this.amountHeld.setScale(2,BigDecimal.ROUND_HALF_EVEN);
    }
    public BigDecimal getAmountReturned() {
		if (this.amountReturned == null) {
			amountReturned = new BigDecimal("0.00");
		}
		return this.amountReturned.setScale(2,BigDecimal.ROUND_HALF_EVEN);
    }
    // TODO
    public BigDecimal getAmountReturnedSubtotal() {
        return new BigDecimal("0.00");
    }
    // TODO
    public BigDecimal getAmountReturnedTax() {
        return new BigDecimal("0.00");
    }
    // TODO
    public BigDecimal getAmountReturnedShipping() {
        return new BigDecimal("0.00");
    }
    public BigDecimal getAmountSettled() {
		if (this.amountSettled == null) {
			amountSettled = new BigDecimal("0.00");
		}
		return this.amountSettled.setScale(2,BigDecimal.ROUND_HALF_EVEN);
    }
    public BigDecimal getAmountSettledShipping() {
		if (this.amountSettledShipping == null) {
			amountSettledShipping = new BigDecimal("0.00");
		}
		return this.amountSettledShipping.setScale(2,BigDecimal.ROUND_HALF_EVEN);
    }
    public BigDecimal getAmountSettledSubtotal() {
		if (this.amountSettledSubtotal == null) {
			amountSettledSubtotal = new BigDecimal("0.00");
		}
		return this.amountSettledSubtotal.setScale(2,BigDecimal.ROUND_HALF_EVEN);
    }
    public BigDecimal getAmountSettledTax() {
		if (this.amountSettledTax == null) {
			amountSettledTax = new BigDecimal("0.00");
		}
		return this.amountSettledTax.setScale(2,BigDecimal.ROUND_HALF_EVEN);
    }
	public int getCcvNumber() { return ccvNumber; }
	public boolean getCvvEntered() { return cvvEntered; }
	public String getCreditName() { return creditName;}
	public Date getDateHeld() { return dateHeld; }
	public Date getDateSettled() { return dateSettled; }
	public Date getDateVoided() { return dateVoided; }
	public int getId() { return id; }
	public String getErrorMessage() { return errorMessage; }
	public int getExpireMonth() { return expireMonth; }
	public int getExpireYear() { return expireYear; }
	public String getExpireString() {
		String expireMonthString = Integer.toString(getExpireMonth());
		String expireYearString = Integer.toString(getExpireYear());

		if (expireMonthString.length()==0) {
			// THIS IS AN ERROR AND WILL NOT VALIDATE WITH CC PEOPLE
			expireMonthString = "00";
		} else if (expireMonthString.length() == 1) {
			expireMonthString = "0" + expireMonthString;
		} else if (expireMonthString.length() > 2) {
			// THIS IS AN ERROR AND MAY NOT VALIDATE WITH CC PEOPLE
			expireMonthString = expireMonthString.substring(0,2);
		}

		if (expireYearString.length()==0) {
			// THIS IS AN ERROR AND WILL NOT VALIDATE WITH CC PEOPLE
			expireMonthString = "00";
		} else if (expireYearString.length() == 1) {
			// THIS MAY BE AN ERROR, IT MAY NOT BE.
			// if we get a year of 02, it will be translated into 2
			expireYearString = "0" + expireYearString;
		} else if (expireYearString.length() > 2) {
			expireYearString = expireYearString.substring(expireYearString.length()-2);
		}
		return expireMonthString+expireYearString;
	}
    public Order getOrder() {
	    return this.order;
    }
    public String getResultReasonText() {
        return resultReason;
    }

	public Store getStore() {
		if (store == null) {
			store = new Store("");
		}
		return store;
	}
    public ArrayList getTransactions() {
        return transactions;
    }
	public PaymentTransaction getTransactionOfType(int type) {
		for (int i=0; i<transactions.size(); i++) {
			PaymentTransaction thisTransaction = (PaymentTransaction)transactions.get(i);
			if (thisTransaction.getType() == type) {
				return thisTransaction;
			}
		}
		return null;
	}
	public String getType() {
		return type;
	}
    public static String getTypeName(String id) {
        if (id.equalsIgnoreCase(TYPE_CC)) {
            return "credit";
        } else if (id.equalsIgnoreCase(TYPE_CHECK)) {
            return "check";
        } else if (id.equalsIgnoreCase(TYPE_PO)) {
            return "PO";
        } else if (id.equalsIgnoreCase(TYPE_COD)) {
            return "COD";
        } else if (id.equalsIgnoreCase(TYPE_SCREDIT)) {
            return "store credit";
        } else if (id.equalsIgnoreCase(TYPE_GIFTCERT)) {
            return "gift certificate";
        } else if (id.equalsIgnoreCase(TYPE_PAYPAL)) {
            return "PayPal";
        } else {
            return "unknown";
        }
    }

	public User getUser() {
		if (user == null) {
			user = new User();
		}
		return user;
	}

	public void loadFromDb(Connection con) throws SQLException {
		if (getId() == 0) {
			return;
		}
		PreparedStatement ps = con.prepareStatement("SELECT * FROM tbPayment WHERE inId=?");
		ps.setInt(1,getId());
		ResultSet rs = ps.executeQuery();
		if (rs.next()) {
			loadFromRs(rs);
		}
		rs.close();
	}

	public void loadFromRs(ResultSet rs) throws SQLException {
        if (getId()==0) {
            setId(rs.getInt("inId"));
        }
        if (user==null) {
            user = new User(rs.getInt("inUserId"));
        }
        setType(rs.getString("vcType"));
        if (order==null) {
		    order = new Order();
		    order.setId(rs.getString("vcOrderId"));
	    }
        this.setAmountHeld(rs.getBigDecimal("moTotalHeld"));
        this.setAmountReturned(rs.getBigDecimal("moTotalReturned"));
        this.setAmountCaptured(rs.getBigDecimal("moTotalSettled"));
        this.setAmountCapturedShipping(rs.getBigDecimal("moTotalSettledShipping"));
        this.setAmountCapturedSubtotal(rs.getBigDecimal("moTotalSettledSubtotal"));
        this.setAmountCapturedTax(rs.getBigDecimal("moTotalSettledTax"));
		this.setAccountNumber(rs.getString("vcAccountNumber"));
		this.setCreditName(rs.getString("vcCreditName"));
		this.setExpireMonth(rs.getInt("inCardMonth"));
		this.setExpireYear(rs.getInt("inCardYear"));
		try {
			this.setDateHeld(new Date(rs.getTimestamp("dtHeld").getTime()));
		} catch (Exception e) {}
		try {
			this.setDateSettled(new Date(rs.getTimestamp("dtSettled").getTime()));
		} catch (Exception e) {}
		try {
			this.setDateVoided(new Date(rs.getTimestamp("dtVoided").getTime()));
		} catch (Exception e) {}
	}

	public void loadTransactionsFromDb(Connection con) throws SQLException {
		if (getId() == 0) {
			return;
		}
		PreparedStatement ps = con.prepareStatement("SELECT * FROM tbPaymentTransaction WHERE inPaymentId=? ORDER BY dtCreated");
		ps.setInt(1,getId());
		ResultSet rs = ps.executeQuery();
		transactions = new ArrayList();
		while (rs.next()) {
			PaymentTransaction pt = new PaymentTransaction();
			pt.setId(rs.getInt("inId"));
			pt.loadFromRs(rs);
			this.addTransaction(pt);
		}
		rs.close();
	}

    public boolean needsCapture() {
        if (getTransactionOfType(PaymentTransaction.TYPE_SALE) != null ||
            getTransactionOfType(PaymentTransaction.TYPE_REFUND) != null) {
            return false;
        }
        if (getTransactionOfType(PaymentTransaction.TYPE_CAPTURE) == null) {
            return true;
        }
        return false;
    }

	public void saveToDb(Connection con) throws SQLException {
        System.out.println("Payment Save: " + this.getAmountSettled().toString() + " - " + this.getAmountSettledSubtotal() + "-" + this.getId() + "|");
		PreparedStatement ps = null;
		if (getId() > 0) {
			ps = con.prepareStatement("UPDATE tbPayment SET "+
					"inUserId=?, vcOrderId=?, vcType=?, moTotalHeld=?, moTotalSettledSubtotal=?, "+
                    "moTotalSettledTax=?, moTotalSettledShipping=?, moTotalSettled=?, moReturnedSubtotal=?, moReturnedTax=?, "+
                    "moReturnedShipping=?, moReturnedTotal=?, vcAccountNumber=?, vcEncAccountNumber=?, vcCreditName=?, "+
                    "vcEncCreditName=?, inCardMonth=?, inCardYear=?, dtHeld=?, dtSettled=?, "+
                    "dtVoided=? "+
			        "WHERE inId=?");
		} else {
			ps = con.prepareStatement("INSERT into tbPayment ("+
                    "inuserId, vcOrderId, vcType, moTotalHeld, moTotalSettledSubtotal, "+
                    "moTotalSettledTax, moTotalSettledShipping, moTotalSettled, moReturnedSubtotal, moReturnedTax, "+
                    "moReturnedShipping, moReturnedTotal, vcAccountNumber, vcEncAccountNumber, vcCreditName, "+
                    "vcEncCreditName, inCardMonth, inCardYear, dtHeld, dtSettled, "+
                    "dtVoided "+
                ") VALUES("+
                    "?,?,?,?,?,"+
                    "?,?,?,?,?,"+
                    "?,?,?,?,?,"+
                    "?,?,?,?,?,"+
                    "?)");
		}
        int i=0;
		ps.setInt(++i,this.getUser().getId());
		if (getOrder() != null) {
			ps.setString(++i,getOrder().getId());
		} else {
			ps.setNull(++i,Types.VARCHAR);
		}
		ps.setString(++i,this.getType());
        ps.setBigDecimal(++i,this.getAmountHeld());
        ps.setBigDecimal(++i, this.getAmountSettledSubtotal());

        ps.setBigDecimal(++i, this.getAmountSettledTax());
        ps.setBigDecimal(++i, this.getAmountSettledShipping());
        ps.setBigDecimal(++i, this.getAmountSettled());
        ps.setBigDecimal(++i, this.getAmountReturnedSubtotal());
        ps.setBigDecimal(++i, this.getAmountReturnedTax());

        ps.setBigDecimal(++i, this.getAmountReturnedShipping());
        ps.setBigDecimal(++i, this.getAmountReturned());
		ps.setString(++i,getAccountNumber());
		ps.setString(++i,"");
		ps.setString(++i,getCreditName());

		ps.setString(++i,"");
		ps.setInt(++i,getExpireMonth());
		ps.setInt(++i,getExpireYear());
        if (getDateHeld()==null) {
	        ps.setNull(++i,Types.DATE);
        } else {
	        ps.setDate(++i,new java.sql.Date(getDateHeld().getTime()));
        }
		if (getDateSettled()==null) {
			ps.setNull(++i,Types.DATE);
		} else {
			ps.setDate(++i,new java.sql.Date(getDateSettled().getTime()));
		}

		if (getDateVoided()==null) {
			ps.setNull(++i,Types.DATE);
		} else {
			ps.setDate(++i,new java.sql.Date(getDateVoided().getTime()));
		}

		if (getId() > 0) {
			ps.setInt(++i,getId());
		}
		ps.execute();

		if (getId() == 0) {
			ps = con.prepareStatement("SELECT Max(inId) AS inMaxId FROM tbPayment WHERE inUserId=?");
			ps.setInt(1,getUser().getId());

			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				setId(rs.getInt("inMaxId"));
			}
			rs.close();
		}

        System.out.println("Order payment save complete");

		/*
		1	inId	int	4	0
		0	vcOrderId	varchar	50	0
		0	moTotalHeld	money	8	0
		0	moTotalReturned	money	8	1
		0	moTotalSettled	money	8	1
		0	vcAccountNumber	varchar	30	1
		0	vcEncAccountNumber	varchar	80	1
		0	vcCreditName	varchar	100	1
		0	vcEncCreditName	varchar	150	1
		0	inCardMonth	int	4	1
		0	inCardYear	int	4	1
		0	dtHeld	datetime	8	0
		0	dtSettled	datetime	8	1
		0	dtVoided	datetime	8	1
		0	moChargedSubtotal	money	8	1
		0	moChargedTax	money	8	1
		0	moChargedShipping	money	8	1
		0	moChargedTotal	money	8	1
		0	moReturnedSubtotal	money	8	1
		0	moReturnedTax	money	8	1
		0	moReturnedShipping	money	8	1
		0	moReturnedTotal	money	8	1
		*/



	}

	public void setAccountNumber(String in) { accountNumber = (in==null) ? "" : in; }
	public void setAmount(BigDecimal in) { amount = in; }
    public void setAmountHeld(BigDecimal in) { amountHeld = in; }
    public void setAmountCaptured(BigDecimal in) { amountSettled = in; }
    public void setAmountCapturedShipping(BigDecimal in) { amountSettledShipping = in; }
    public void setAmountCapturedSubtotal(BigDecimal in) { amountSettledSubtotal = in; }
    public void setAmountCapturedTax(BigDecimal in) { amountSettledTax = in; }
    public void setAmountReturned(BigDecimal in) { amountReturned = in; }
    // TODO setAmountReturnedSubtotal
    // TODO setAmountReturnedShipping
    // TODO setAmountReturnedTax
	public void setCvvEntered(boolean in) { this.cvvEntered = in; }
	public void setCcvNumber(int in) { this.ccvNumber = in; }
	public void setCreditName(String in) { this.creditName = (in==null) ? "" : in; }
	public void setDateHeld(Date in) { this.dateHeld = in; }
	public void setDateSettled(Date in) { this.dateSettled = in; }
	public void setDateVoided(Date in) { this.dateVoided = in; }
	public void setId(int in) { id = in; }
    public void setIp(String in) {
        ip = (in==null) ? "" : in;
    }
	public void setExpireMonth(int expireMonth) { this.expireMonth = expireMonth; }
	public void setExpireYear(int expireYear) { this.expireYear = expireYear; }
	public void setOrder(Order in) { this.order = in; }
	public void setStore(Store in) { store = in; }
	public void setType(String in) {
		if (in==null) {
			type = TYPE_DEFAULT;
			return;
		}
		for (int i=0; i<TYPES.length; i++) {
			if (in.equalsIgnoreCase(TYPES[i])) {
				type = in;
				return;
			}
		}
		type = TYPE_DEFAULT;
	}
	public void setUser(User in) { user = in; }
}
