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
<div class="contentText">
  <div class="breadCrumb"><a href="<%=strBaseUrl%>">home</a> / <span class="breadCrumbProduct">shopping cart</span></div>
<br /><br /><br />
<script language="javascript">
    function purchase() {
        document.mainForm.action.value="<%=CartServlet.ACTION_CHECKOUT%>";
        document.mainForm.submit();
    }

    function updateQty() {
        document.mainForm.action.value="<%=CartServlet.ACTION_UPDATE%>";
        document.mainForm.submit();
    }

function quantityKeyHandler(event,focusItem) {
    var event = (event) ? event : ((window.event) ? window.event : "")

    // Up Arrow
    if (event.keyCode==38) {
        moveFocusUp(focusItem);
        event.returnValue = false; // IE
        if (event.preventDefault) {
            event.preventDefault();
        }
        return false;
    // Down Arrow
    } else if (event.keyCode==40) {
        moveFocusDown(focusItem);
        event.returnValue = false; // IE
        if (event.preventDefault) {
            event.preventDefault();
        }
        return false;
    // Right Arrow
    } else if (event.keyCode==39) {
    }
}

function moveFocusDown(focusItem) {
    if (focusItem < <%=cart.getItemCount()%>) {
        focusItem++;
        var boxToFocus = document.getElementById("item_" + focusItem);
        if (boxToFocus) {
            if (boxToFocus.disabled) {
                moveFocusDown();
            } else {
                boxToFocus.focus();
                autoSelect(boxToFocus);
            }
        }
    }
}

function moveFocusUp(focusItem) {
    if (focusItem > 0) {
        focusItem--;
        var boxToFocus = document.getElementById("item_" + focusItem);
        if (boxToFocus) {
            if (boxToFocus.disabled) {
                moveFocusUp();
            } else {
                boxToFocus.focus();
                autoSelect(boxToFocus);
            }
        }
    }
}
</script>

<% if (request.getServerName() != null && request.getServerName().indexOf("beta") >= 0) { %>
	<form method="post" action="cart" name="mainForm">
<% } else { %>
	<form method="post" action="https://www.distroy.com<%=strBaseUrl%>cart" name="mainForm">
<% } %>
<input type="hidden" name="action" value="<%=CartServlet.ACTION_UPDATE%>">
<input type="hidden" name="fromAction" value="<%=CartServlet.ACTION_VIEW%>">

<!-- <div class="pageHead">SHOPPING CART</div> -->
            <% if (cart.getItemCount()==0) { %>
                <b>You currently have no items in your shopping cart.</b>
            <% } else { %>
                <table width="100%" border="0">
                    <tr id="cart_head">
                        <td class="matrixHead" style="text-align:left;">PRODUCT</td>
                        <td class="matrixHead">PRICE</td>
                        <td class="matrixHead">QTY</td>
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
                                      <a href="<%=strBaseUrl%>allproducts/0/<%=thisItem.getProductVariation().getId()%>"><b><%=thisItem.getProductVariation().getProduct().getName()%></b></a><br />
                                -<%=thisItem.getProductVariation().getStyle()%><br />
                                -<%=thisItem.getProductVariation().getColor()%><br />
                                <% if (wholesale) { %>
                                    -<%=thisItem.getProductVariation().getSku()%><br />
                                <% } %>
                                -<%=thisItem.getSize().getName()%><br />
                            <a href="cart?action=<%=CartServlet.ACTION_DELETE%>&itemId=<%=thisItem.getId()%>"><span style="color: #CC0001;">remove</span></a>
                            </td>
                            <td class="<%=cssClass%>" style="text-align:right;">
                                <%=dollarFormat.format(thisItem.getProductVariation().getPrice(store))%>
                            </td>
                            <td class="<%=cssClass%>">
                                <input type="text" class="text" size="5" value="<%=thisItem.getQty()%>" name="item_<%=thisItem.getId()%>" id="item_<%=thisItem.getId()%>" tabindex="<%=thisItem.getId()%>" onkeydown="quantityKeyHandler(event,<%=thisItem.getId()%>);">
                            </td>
                            <td class="<%=cssClass%>" style="text-align:right;">
                                <%=dollarFormat.format(thisItem.getPriceTotal())%>
                            </td>
                        </tr>
                        <%
                    }
                    %>
                    <tr>
                        <td colspan="3" class="matrixRowAlternate" style="text-align:right;">
                            <input type="button" class="button" onClick="updateQty()" value="update quantity">
                        </td>
                        <td class="matrixRowAlternate" >&nbsp;</td>
                    </tr>
                    <tr>
                        <td colspan="3" class="matrixRow" style="text-align:right; font-weight:bold;">Total before shipping and discounts:</td>
                        <td class="matrixRowAlternate" style="text-align:right; font-weight:bold;"><%=dollarFormat.format(cart.getSubtotalPrice())%></td>
                    </tr>
                    <tr>
                        <td colspan="2" class="contentTextBold">
                            Sorry, Unfortunately we are not taking orders at this time. We closed down our studio and are on the process of moving to new cities.
<br><br>Once we have established permanent residencies again we will once again be able to ship orders. In the meantime, sign up for the mailing list and you will be notified of when the store is live again.
                        </td>
                    </tr>
					<%--
                    <tr><td class="sectionSpace" colspan="4" height="25">&nbsp;</td></tr>
                    <tr>
                        <td colspan="4" style="text-align:right;">
                            <input type="button" class="button" onClick="purchase()" value="SECURE CHECKOUT&nbsp;&nbsp;&gt;">
                        </td>
                    </tr>
                    --%>
                </table>
                <br />
                    <% if (!store.isWholesale()) { %>
                        <span class="contentTextBold">Coupons and promotions can be redeemed on the last page of checkout</span>
                    <% } %>
                       <br /><br /><br />
							<!-- GeoTrust QuickSSL [tm] Smart Icon tag. Do not edit. -->
							<SCRIPT LANGUAGE="JavaScript" TYPE="text/javascript" SRC="//smarticon.geotrust.com/si.js"></SCRIPT>
							<!-- end GeoTrust Smart Icon tag -->
                            <img src="/img/we_accept.jpg" alt="we accept Visa, Mastercard, Amex, Paypal, and Checks" border="0" align="right" />
            <% } %>
            <br />
            <br />
</form>
</div>
<%@ include file = "../include/footer.jsp"%>
