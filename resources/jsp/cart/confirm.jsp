<%@ page import="com.approachingpi.store.catalog.Image,
                 java.util.ArrayList,
                 java.util.TreeSet,
                 com.approachingpi.store.catalog.*,
                 com.approachingpi.store.servlet.BrandServlet,
                 com.approachingpi.store.servlet.CartServlet,
                 com.approachingpi.store.cart.*,
                 java.math.BigDecimal,
                 com.approachingpi.servlet.PiServlet,
                 com.approachingpi.store.order.Coupon,
                 com.approachingpi.store.order.CouponClaim,
                 com.approachingpi.store.order.Payment"%>

<jsp:useBean id="cart"          class="com.approachingpi.store.cart.Cart" scope="request"/>
<jsp:useBean id="billing"       class="com.approachingpi.user.Address" scope="request"/>
<jsp:useBean id="shipping"      class="com.approachingpi.user.Address" scope="request"/>

<%@ include file = "../include/global.jsp"%>
<%
strPageTitle = "Checkout";
String paymentType = PiServlet.getReqString(request,"paymentType",Payment.TYPE_CC);
boShowNav = false;
boShowFoot = false;
%>
<%@ include file = "../include/header.jsp"%>
<div class="contentText">
  <div class="breadCrumb"><a href="<%=strBaseUrl%>">home</a> / <a href="cart?action=<%=CartServlet.ACTION_VIEW%>">shopping cart</a> / <a href="cart?action=<%=CartServlet.ACTION_BILLSHIP%>">account information</a> / <span class="breadCrumbProduct">checkout</span></div>

<script language="javascript">
	function claimCoupon() {
		document.processForm.action.value="<%=CartServlet.ACTION_CLAIMCOUPON%>";
		document.processForm.submit();
	}
	function cvvNumber() {
        var oCvvWindow = window.open("/jsp/cvv.jsp", "cvvWindow", "width=550,height=375,status=no,resizable=yes,scrollbars=yes");
        oCvvWindow.focus();
	}
    function editBilling() {
        location="cart?action=<%=CartServlet.ACTION_BILLSHIP%>";
    }
    function editCart() {
        location="cart?action=<%=CartServlet.ACTION_VIEW%>";
    }
    function editShipping() {
        location="cart?action=<%=CartServlet.ACTION_BILLSHIP%>";
    }
    function shipMethodChanged() {
        var shipMethodId = document.shipForm.shipMethod.options[document.shipForm.shipMethod.selectedIndex].value;

        location = "cart?action=<%=CartServlet.ACTION_SHIPMETHOD%>&shipMethodId=" + shipMethodId;
    }

    function validateProcess() {
        document.processForm.submit();
    }
</script>
<form method="post" action="cart" name="shipForm">
<input type="hidden" name="action" value="<%=CartServlet.ACTION_SHIPMETHOD%>">
<input type="hidden" name="fromAction" value="<%=CartServlet.ACTION_CONFIRM%>">

        <!-- <div class="pageHead">CHECKOUT</div> -->
<span class="contentTextBold">Please confirm your order.  When you are sure that everything is correct,<br /> enter your payment information at the bottom to complete the order.</span><br/><br/>
            <table border="0" width="100%">
                <tr id="cart_head">
                    <td class="matrixHead" style="text-align:left;">PRODUCT</td>
                    <td class="matrixHead">PRICE</td>
                    <td class="matrixHead" width="10">QTY</td>
                    <td class="matrixHead">TOTAL</td>
                </tr>
                <%
                String cssClass = "matrixRow";
                ArrayList items = cart.getCartItems();

                for (int i=0; i<items.size(); i++) {
                    CartItem thisItem = (CartItem)items.get(i);

                    cssClass = (cssClass.equalsIgnoreCase("matrixRow"))? "matrixRowAlternate" : "matrixRow";

                    %>
                    <tr id="cart_<%=i%>">
                        <td class="<%=cssClass%>">
                            <% Image thisImage = thisItem.getProductVariation().getFirstImage();
                               String thumbImage = "/productimages/thumb/" + thisImage.getName().replaceAll(".jpg","") + "/thumb-" + thisImage.getName().replaceAll(".jpg","") + "-" + thisItem.getProductVariation().getProduct().getName().replaceAll("[^A-Za-z0-9]","_") + "-" + thisItem.getProductVariation().getStyle().replaceAll("[^A-Za-z0-9]","_") + "-" + thisItem.getProductVariation().getColor().replaceAll("[^A-Za-z0-9]","_") + ".jpg";
                               String largeImage = "/productimages/standard/" + thisImage.getName().replaceAll(".jpg","") + "/thumb-" + thisImage.getName().replaceAll(".jpg","") + "-" + thisItem.getProductVariation().getProduct().getName().replaceAll("[^A-Za-z0-9]","_") + "-" + thisItem.getProductVariation().getStyle().replaceAll("[^A-Za-z0-9]","_") + "-" + thisItem.getProductVariation().getColor().replaceAll("[^A-Za-z0-9]","_") + ".jpg";
                            %>
                            <div id="enlarge_<%=i%>"><img src="<%=thumbImage%>" align="left" vspace="10" hspace="20" border="0" height="70" width="70" alt="" /></div>
                            <script type="text/javascript" language="javascript">
                                new Tip('enlarge_<%=i%>', "<img src=\"<%=largeImage%>\" width=\"200\" border=\"0\" alt=\"\" \/>", { 
                                    style: 'darkgrey',
                                    width: 'auto',
                                    stem: 'leftTop',
                                    // hook: { tip: 'topLeft', mouse: true },
                                    // hideOn: { element: 'closeButton', event: 'click' },
                                    offset: { x: 20, y: -25 }
                            });</script>
                            <b><%=thisItem.getProductVariation().getProduct().getName()%></b><br>
                            -<%=thisItem.getProductVariation().getStyle()%><br>
                            -<%=thisItem.getProductVariation().getColor()%><br>
                            -<%=thisItem.getSize().getName()%>
                            <% if (wholesale) { %>
                            <br>-<%=thisItem.getProductVariation().getSku()%>
                            <% } %>
                        </td>
                        <td class="<%=cssClass%>" style="text-align:right;">
                            <%=dollarFormat.format(thisItem.getProductVariation().getPrice(store))%>
                        </td>
                        <td class="<%=cssClass%>" style="text-align:center;" width="10"><%=thisItem.getQty()%></td>
                        <td class="<%=cssClass%>" style="text-align:right;">
                            <%=dollarFormat.format(thisItem.getPriceTotal())%>
                        </td>
                    </tr>
                    <%
                }
                %>
                <tr>
                    <td colspan="2" class="matrixRowAlternate"><input type="button" class="button" name="button_editCart" onClick="editCart()" value="edit items in cart" /></td>
                    <td  class="matrixRowAlternate" style="text-align:right; font-weight:bold;">Subtotal:</td>        
                    <td class="matrixRowAlternate" style="text-align:right; font-weight:bold;"><%=dollarFormat.format(cart.getSubtotalPrice())%></td>
                </tr>
                <tr>
                    <td class="matrixRow" style="text-align:right;">
                        <span style="font-weight:bold">Ship Method:</span>&nbsp;
                                    <%
                                    ArrayList shipMethods = cart.getShipMethods();
                                    ShipMethod activeMethod = cart.getActiveShipMethod();
                                    if (shipMethods.size()==0) {
                                        out.print("Standard");
                                    } else if (shipMethods.size()==1) {
                                        out.print(activeMethod.getName());
                                    } else {
                                        %>
                                        <select name="shipMethod" onChange="shipMethodChanged()">
                                        <% for (int i=0; i<shipMethods.size(); i++) {
                                            ShipMethod thisMethod = (ShipMethod)shipMethods.get(i);
                                            BigDecimal shipPrice = thisMethod.getFirstPrice().calculate(cart.getSubtotalPrice(),cart.getItemCount(),0);
                                            String shipPriceString = (shipPrice.doubleValue() == 0) ? "FREE" : dollarFormat.format(shipPrice);

                                            %><option value="<%=thisMethod.getId()%>"<% if (thisMethod==activeMethod) { out.print(" selected"); }%>><%=thisMethod.getName()%> (<%=shipPriceString%>)</option>
                                            <%
                                        }
                                        %>
                                        </select>
                                        <%
                                    }
                                    %>
                        </td>
                        <td colspan="2" class="matrixRow" style="text-align:right;">Shipping & Handling:</td>
                    <td class="matrixRow" style="text-align:right;"><%=dollarFormat.format(cart.getShipPrice(activeMethod))%></td>
                </tr>
                <tr>
                    <td colspan="3" class="matrixRow" style="text-align:right;">Tax:</td>
                    <td class="matrixRow" style="text-align:right;"><%=dollarFormat.format(cart.getTaxPrice())%></td>
                </tr>
                <% if (cart.getCouponDiscountTotal().compareTo(new BigDecimal("0.00")) > 0) { %>
                    <tr>
                        <td  colspan="3" class="matrixRow" style="text-align:right;">Coupon Discount:</td>
                        <td class="matrixRow" style="text-align:right;">-<%=dollarFormat.format(cart.getCouponDiscountTotal())%></td>
                    </tr>
                <% } %>
                <tr>
                    <td  colspan="3" class="matrixRowAlternate" style="text-align:right; font-weight:bold;">Total:</td>
                    <td class="matrixRowAlternate" style="text-align:right;font-weight:bold;"><%=dollarFormat.format(cart.getTotalPrice())%></td>
                </tr>
            </table>
</form>
<form method="post" action="cart" name="processForm">
<input type="hidden" name="action" value="<%=CartServlet.ACTION_PROCESS%>">
<input type="hidden" name="fromAction" value="<%=CartServlet.ACTION_CONFIRM%>">
<input type="hidden" name="paymentType" value="<%=paymentType%>">

    <!-- ACCOUNT INFORMATION -->
<br /><br />
<span class="contentTextBold">ACCOUNT INFORMATION</span><br /><br />
Email Address: <%=user.getEmail()%>
<br /><br />
    <!-- BILLING / SHIPPING INFORMATION -->
<table border="0" width="100%">
    <tr>
    <td class="matrixHead">BILLING INFORMATION</td>
    <td class="sectionSpace" width="10">&nbsp;</td>
    <td class="matrixHead">SHIPPING INFORMATION</td>
    </tr>
    <tr>
        <td width="45%" class="matrix" style="vertical-align:top">
          <span style="position: absolute; padding-left: 240px;"><input type="button" class="button" name="button_editBilling" onClick="editBilling()" value="edit" /></span>
                        <%=billing.getNameFirst()%>&nbsp;<%=billing.getNameLast()%><br />
                        <%=billing.getAddress1()%><br />
                        <% if (billing.getAddress2().length() > 0) { %>
                            <%=billing.getAddress2()%><br />
                        <% } %>
                        <%=billing.getCity()%>
                        <% if (billing.getState().getAbbrev().length() > 0) { %>
                            <%=billing.getState().getAbbrev()%>,&nbsp;
                        <% } else { %>
                            ,&nbsp;
                        <% } %>
                        <%=billing.getZip()%><br />
                        <%=billing.getCountry().getName()%><br />
                        <%=billing.getPhoneNumber()%>
            <td class="sectionSpace" width="10">&nbsp;</td>
            <td width="45%" class="matrix" style="vertical-align:top">
              <span style="position: absolute; padding-left: 240px;"><input type="button" class="button" name="button_editShipping" onClick="editShipping()" value="edit" /></span>
                        <% if (shipping.getId() == 0) { %>
                            (same as billing address)
                        <% } else { %>
                            <%=shipping.getNameFirst()%>&nbsp;<%=shipping.getNameLast()%><br />
                            <%=shipping.getAddress1()%><br />
                            <% if (shipping.getAddress2().length() > 0) { %>
                                <%=shipping.getAddress2()%><br />
                            <% } %>
                            <%=shipping.getCity()%>
                            <% if (shipping.getState().getAbbrev().length() > 0) { %>
                                <%=shipping.getState().getAbbrev()%>,&nbsp;
                            <% } else { %>
                                ,&nbsp;
                            <% } %>
                            <%=shipping.getZip()%><br />
                            <%=shipping.getCountry().getName()%><br />
                            <%=shipping.getPhoneNumber()%>
                        <% } %>
        </td>
    </tr>
  </table>
    <% if (!wholesale) { %>
<br /><br />
		<!-- COUPONS -->
<span class="contentTextBold">Coupons & Gift Certificates</span><br />
                <% if (cart.getCouponClaims().size() > 0) {
                    CouponClaim claim = (CouponClaim)cart.getCouponClaims().get(0);
                    Coupon coupon = claim.getCoupon();
                    %>
                    You have the following coupon applied to this order:<br />
                    <b><%=coupon.getName()%></b><br />
                    <%=coupon.getDesc()%>
                <% } else { %>
                    If you have any coupons or gift certificates please enter them here.  Enter gift certificates first.<br />
                    <table border="0">
                        <tr>
                            <td class="inputLabel" id="l_claimCode">Claim Code</td>
                            <td><input type="text" class="text" size="15" maxlength="20" name="claimCode" value="<%=PiServlet.getReqString(request,"claimCode")%>"></td>
                            <td><input type="button" class="button" name="button_claim" value="Claim coupon or Gift Certificate" onClick="claimCoupon()"></td>
                        </tr>
                    </table>
                <% } %>
	<% } %>

<br /><br />
    <!-- COMMENTS -->
<span class="contentTextBold">Comments</span><br />
Got any special shipping instructions?  Love something about us?  Hate something about us?  Any music we should listen to?  Let us know here.<br />
<textarea name="comments" class="text" cols="60" rows="4"><%=PiServlet.getReqString(request,"comments")%></textarea>

<br /><br />
    <!-- PAYMENT INFORMATION -->
<span class="contentTextBold">PAYMENT</span><br /><br />


<ul id="countrytabs" class="tabs">
<li><a href="#" rel="country1" name="<%=Payment.TYPE_CC%>" class="selected">Credit Card</a></li>
<li><a href="#" rel="country2" name="<%=Payment.TYPE_PAYPAL%>">Paypal</a></li>
<!-- <li><a href="#" rel="country3" name="payment type">Check/Money Order</a></li> -->
</ul>

<div class="tabContent">

<div id="country1">
    <img src="<%=strImageDir%>credit_card_logos_half.jpg" width="190" height="34"><br/><br/>
    We accept Visa, Mastercard, American Express, and Discover.<br/>
    <table border="0">
    <tr><td class="inputLabel" id="l_creditName">Name on Card</td><td><input type="text" class="text" size="20" maxlength="100" name="creditName" value="<%=PiServlet.getReqString(request,"creditName")%>"></td>
    </tr>
    <tr><td class="inputLabel" id="l_accountNumber">Card Number</td><td><input type="text" class="text" size="20" maxlength="20" name="accountNumber" value="<%=PiServlet.getReqString(request,"accountNumber")%>"></td>
    </tr>
    <tr><td class="inputLabel" id="l_expiration">Card Expiration</td>
    <td><select name="expireMonth">
	<option value="">Month</option>
	<%
	SimpleDateFormat monthFormat = new SimpleDateFormat("[MM] MMMM");
	SimpleDateFormat monthValueFormat = new SimpleDateFormat("MM");
	GregorianCalendar cal = new GregorianCalendar();
	cal.set(Calendar.DAY_OF_MONTH,cal.getMinimum(Calendar.DAY_OF_MONTH));
	for (int month=cal.getMinimum(Calendar.MONTH); month<=cal.getMaximum(Calendar.MONTH); month++) {
            cal.set(Calendar.MONTH, month);
            Date monthDate = cal.getTime();
            String value = monthValueFormat.format(monthDate);
            String text = monthFormat.format(monthDate);
            String selectedVal = (value.equalsIgnoreCase(PiServlet.getReqString(request,"expireMonth"))) ? " selected" : "";
	%>
        <option value="<%=value%>"<%=selectedVal%>><%=text%></option>
	<% } %>
        </select>
	<select name="expireYear">
	<option value="">Year</option>
	<%
	int maxYear = cal.get(Calendar.YEAR) + 10;
	for (int year=cal.get(Calendar.YEAR); year<maxYear; year++) {
            String selectedVal = (year==PiServlet.getReqInt(request,"expireYear")) ? " selected" : "";
	%>
        <option value="<%=year%>"<%=selectedVal%>><%=year%>
	<% } %>
        </select>
	</td>
        </tr>
	<tr>
	<td class="inputLabel" id="l_cvvNumber">Card Verification Number</td><td><input type="text" class="text" size="4" maxlength="4" name="cvvNumber" value="<%=PiServlet.getReqString(request,"cvvNumber")%>">&nbsp;<a href="javascript:cvvNumber()">What is This?</a></td>
        </tr>
        </table>
        <br />
	<center>
            <input type="button" class="button" name="button_process" value="PLACE ORDER" onClick="validateProcess()"><br />
            (please click this button only once, it will take a few moments to complete your order)
	</center>
</div>

<div id="country2">
    <img src="<%=strImageDir%>paypal_logo.gif" width="200" height="50"><br/><br/>
    In order to pay via PayPal you must complete your payment information on the PayPal website.<br/><br/>
    Click the button below to be forwarded to the PayPal website.<br/><br/>
    <center>
        <input type="button" class="button" name="button_process" value="COMPLETE ORDER VIA PAYPAL" onClick="validateProcess()"><br />
	(please click this button only once, it will take a few moments to complete your order)
    </center>
</div>

<!-- <div id="country3">
    <br/><br/>In order to pay with a check or money order simply complete your order here.  You will get an order number that you can write on a check or money order that you mail in.  We will ship your order once the check clears.<br/><br/>
    <b>Checks can only be used for US orders and must be drawn on a US bank.</b>  Sorry, we cannot take international checks.<br/><br/>
    Personal checks will be held for 10 business days before before order is shipped.<br/><br/>
    All international orders must send international money orders.<br/><br/>
    <center>
        <input type="button" class="button" name="button_process" value="PLACE ORDER" onClick="validateProcess()"><br />
	(please click this button only once, it will take a few moments to complete your order)
    </center>
</div> -->

</div>

<script type="text/javascript">

var countries=new ddtabcontent("countrytabs")
countries.setpersist(true)
countries.setselectedClassTarget("link") //"link" or "linkparent"
countries.init()

</script>

</form>
</div>

<%@ include file = "../include/footer.jsp"%>
