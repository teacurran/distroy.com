<%@ page import="com.approachingpi.store.catalog.Image,
                 java.util.ArrayList,
                 java.util.TreeSet,
                 com.approachingpi.store.catalog.*,
                 com.approachingpi.store.servlet.BrandServlet,
                 com.approachingpi.store.servlet.CartServlet,
                 com.approachingpi.store.cart.CartItem"%>

<jsp:useBean id="cart"   class="com.approachingpi.store.cart.Cart" scope="request"/>
<%@ include file = "../include/global.jsp"%>
<%
strPageTitle = "Shopping Cart";
%>
<%@ include file = "../include/header.jsp"%>

<%--
<script language="javascript">
    function purchase() {
        document.mainForm.action.value="<%=CartServlet.ACTION_CHECKOUT%>";
        document.mainForm.submit();
    }

    function update() {
        document.mainForm.action.value="<%=CartServlet.ACTION_UPDATE%>";
        document.mainForm.submit();
    }
</script>
--%>

<% if (request.getServerName() != null && request.getServerName().indexOf("beta") >= 0) { %>
	<form method="post" action="cart" name="mainForm">
<% } else { %>
	<form method="post" action="https://www.distroy.com<%=strBaseUrl%>cart" name="mainForm">
<% } %>
<input type="hidden" name="action" value="<%=CartServlet.ACTION_UPDATE%>">
<input type="hidden" name="fromAction" value="<%=CartServlet.ACTION_VIEW%>">

<table border="0" cellpadding="0" cellspacing="0" width="100%">
    <tr>
        <td class="pageHead">SHOPPING CART</td>
    </tr>
    <tr>
        <td class="content" style="padding-top:10px;" height="300">
            <% if (cart.getItemCount()==0) { %>
                <b>You currently have no items in your shopping cart.</b>
            <% } else { %>
                <table width="80%" style="margin-left:auto; margin-right:auto;">
                    <tr id="cart_head">
                        <td class="matrixHead">QTY</td>
                        <td class="matrixHead">DEL</td>
                        <td class="matrixHead">PRODUCT</td>
                        <td class="matrixHead" style="text-align:center;">PRICE</td>
                        <td class="matrixHead" style="text-align:center;">TOTAL</td>
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
                                <input type="text" class="text" size="5" value="<%=thisItem.getQty()%>" name="item_<%=thisItem.getId()%>" tabindex="<%=i%>">
                            </td>
                            <td class="<%=cssClass%>" style="text-align:center;">
                                <a href="cart?action=<%=CartServlet.ACTION_DELETE%>&itemId=<%=thisItem.getId()%>"><img src="<%=strImageDir%>icon_cart_delete.gif" border="0" alt="remove this item from you're cart"></a>
                            </td>
                            <td class="<%=cssClass%>">
                                <b><%=thisItem.getProductVariation().getProduct().getName()%></b><br>
                                -<%=thisItem.getProductVariation().getStyle()%><br>
                                -<%=thisItem.getProductVariation().getColor()%><br />
                                <% if (wholesale) { %>
                                    -<%=thisItem.getProductVariation().getSku()%><br />
                                <% } %>
                                -<%=thisItem.getSize().getName()%>
                            </td>
                            <td class="<%=cssClass%>" style="text-align:right;">
                                <%=dollarFormat.format(thisItem.getProductVariation().getPrice(store))%>
                            </td>
                            <td class="<%=cssClass%>" style="text-align:right;">
                                <%=dollarFormat.format(thisItem.getPriceTotal())%>
                            </td>
                        </tr>
                        <%
                    }
                    %>
                    <tr>
                        <td colspan="4" class="matrixRowAlternate" style="text-align:right; font-weight:bold;">Total before shipping and discounts:</td>
                        <td class="matrixRowAlternate" style="text-align:right; font-weight:bold;"><%=dollarFormat.format(cart.getSubtotalPrice())%></td>
                    </tr>
                </table>
                <br />
                <table width="80%" style="margin-left:auto; margin-right:auto;">
                    <%--
		    <tr>
                        <td style="text-align:left;">
                            <input type="button" class="button" onClick="update()" value="UPDATE">
                        </td>
                        <td style="text-align:right;">
                            <input type="button" class="button" onClick="purchase()" value="SECURE CHECKOUT&nbsp;&nbsp;&gt;">
                        </td>
                    </tr>
                    --%>
                    <tr>
                        <td colspan="2">&nbsp;</td>
                    </tr>
                    <tr>
                        <td colspan="2" class="contentTextBold">
                            Sorry, Unfortunately we are not taking orders at this time. We closed down our studio and are on the process of moving to new cities.
<br><br>Once we have established permanent residencies again we will once again be able to ship orders. In the meantime, sign up for the mailing list and you will be notified of when the store is live again. 
                        </td>
                    </tr>
		    <%--
                    <% if (!store.isWholesale()) { %> 
                    <tr>
                        <td colspan="2" class="contentTextBold">
                            Coupons and promotions can be redeemed on the last page of checkout
                        </td>
                    </tr>
                    <% } %>
                    --%>
					<tr>
						<td colspan="2"><br /><br /><br /></td>
					</tr>
					<tr>
						<td>
							<!-- GeoTrust QuickSSL [tm] Smart Icon tag. Do not edit. -->
							<SCRIPT LANGUAGE="JavaScript" TYPE="text/javascript" SRC="//smarticon.geotrust.com/si.js"></SCRIPT>
							<!-- end GeoTrust Smart Icon tag -->
						</td>
						<td style="text-align:right;">
							<img src="/img/we_accept.jpg" alt="we accept Visa, Mastercard, Amex, Paypal, and Checks" border="0"/>
						</td>
					</tr> 
                </table>
            <% } %>
            <br />
            <br />
        </td>
    </tr>
</table>
</form>

<%@ include file = "../include/footer.jsp"%>
