<%@ page import="com.approachingpi.store.cart.CartItem,
                 com.approachingpi.store.order.Order,
                 com.approachingpi.store.order.OrderAddress,
                 com.approachingpi.store.order.*,
		 java.util.ArrayList,
                 com.approachingpi.store.order.OrderDetail"%>
<jsp:useBean id="order"    class="com.approachingpi.store.order.Order" scope="request"/>

<%@ include file = "../include/global.jsp"%>
<%
strPageTitle = "Order Status";
strActivateMenu = "2";

OrderAddress billing = order.getBillAddress();
OrderAddress shipping = order.getShipAddress();
String dateCreated = (order.getDateCreated()!=null) ? timeFormat.format(order.getDateCreated()) : "";
String dateModified = (order.getDateModified()!=null) ? timeFormat.format(order.getDateModified()) : "";
String dateShipBegan = (order.getDateShipBegan()!=null) ? timeFormat.format(order.getDateShipBegan()) : "";
String dateShipComplete = (order.getDateShipComplete()!=null) ? timeFormat.format(order.getDateShipComplete()) : "";
%>
<%@ include file = "../include/header.jsp"%>

<table border="0" cellpadding="0" cellspacing="0" width="100%">
    <tr>
        <td class="pageHead">ORDER STATUS</td>
    </tr>
    <tr>
        <td class="contentText" style="padding:10px;" height="300">
            <% if (order.getId() == null || order.getId().equals("")) { %>
                <b>Order not found.</b><br />
                If you have placed an order and it is not showing up here, please contact us at
                <a href="mailto:support@distroy.com">support@distroy.com</a>.  Please be sure to include your name
                and order number.  It is also helpful to include a phone number and best time to call so we can contact
                you if neccessary.
            <% } else { %>

                <table width="100%">
                    <tr>
                        <td style="vertical-align:top">
                            <table>
                                <tr>
                                    <td class="inputLabel">Order Id</td>
                                    <td><b><%=order.getId()%></b></td>
                                </tr>
                                <tr>
                                    <td class="inputLabel">Customer</td>
                                    <td><%=order.getUser().getEmail()%></td>
                                </tr>
                                <tr>
                                    <td class="inputLabel">Date</td>
                                    <td><%=dateCreated%></td>
                                </tr>
                                <tr>
                                    <td class="inputLabel">Modified</td>
                                    <td><%=dateModified%></td>
                                </tr>
                                <tr>
                                    <td class="inputLabel">Ship Began</td>
                                    <td><%=dateShipBegan%></td>
                                </tr>
                                <tr>
                                    <td class="inputLabel">Ship Complete</td>
                                    <td><%=dateShipComplete%></td>
                                </tr>
                                <tr>
                                    <td class="inputLabel">Status</td>
                                    <td><%=Order.getStatusById(order.getStatus())%></td>
                                </tr>
                            </table>
                        </td>
                        <td style="vertical-align:top">
                            <table>
                                <tr>
                                    <td></td>
                                    <td style="text-align:left;">billed</td>
                                    <td>captured</td>
                                </tr>

                                <% String cssClass = "matrixRow"; %>
                                <% cssClass = (cssClass.equals("matrixRow")) ? "matrixRowAlternate" : "matrixRow"; %>
                                <tr>
                                    <td class="inputLabel">Subtotal</td>
                                    <td class="<%=cssClass%>" style="text-align:right;"><%=dollarFormat.format(order.getAmountSubtotal())%></td>
                                    <td class="<%=cssClass%>" style="text-align:right;"><%=dollarFormat.format(order.getAmountCapturedSubtotal())%></td>
                                </tr>
                                <% cssClass = (cssClass.equals("matrixRow")) ? "matrixRowAlternate" : "matrixRow"; %>
                                <tr>
                                    <td class="inputLabel">Shipping</td>
                                    <td class="<%=cssClass%>" style="text-align:right"><%=dollarFormat.format(order.getAmountShipping())%></td>
                                    <td class="<%=cssClass%>" style="text-align:right"><%=dollarFormat.format(order.getAmountCapturedShipping())%></td>
                                </tr>
                                <% cssClass = (cssClass.equals("matrixRow")) ? "matrixRowAlternate" : "matrixRow"; %>
                                <tr>
                                    <td class="inputLabel">Tax</td>
                                    <td class="<%=cssClass%>" style="text-align:right;"><%=dollarFormat.format(order.getAmountTax())%></td>
                                    <td class="<%=cssClass%>" style="text-align:right;"><%=dollarFormat.format(order.getAmountCapturedTax())%></td>
                                </tr>
                                <% cssClass = (cssClass.equals("matrixRow")) ? "matrixRowAlternate" : "matrixRow"; %>
                                <tr>
                                    <td class="inputLabel">Coupon</td>
                                    <td class="<%=cssClass%>" style="text-align:right;">-<%=dollarFormat.format(order.getAmountCouponTotal())%></td>
                                    <td class="<%=cssClass%>" style="text-align:right;"></td>
                                </tr>
                                <% cssClass = (cssClass.equals("matrixRow")) ? "matrixRowAlternate" : "matrixRow"; %>
                                <tr>
                                    <td class="inputLabel">Total</td>
                                    <td class="<%=cssClass%>" style="text-align:right;"><b><%=dollarFormat.format(order.getAmountTotal())%></b></td>
                                    <td class="<%=cssClass%>" style="text-align:right;"><%=dollarFormat.format(order.getAmountCapturedTotal())%></td>
                                </tr>
                            </table>
                        </td>
                    </tr>

                    <tr>
                        <td class="inputLabel" style="text-align:left;">Bill To</td>
                        <td class="inputLabel" style="text-align:left;">Ship To</td>
                    </tr>

                    <tr>
                        <td style="vertical-align:top">
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
                        </td>
                        <td style="vertical-align:top">
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
                    <tr>
                        <td colspan="2" class="inputLabel" style="text-align:left;">Comments</td>
                    </tr>
    <tr>
        <td colspan="2">
            <%
            ArrayList comments = order.getComments();
            for (int i=0; i<comments.size(); i++) {
                OrderComment thisComment = (OrderComment)comments.get(i);
                %>
                <p>
                <%=thisComment.getBody()%>
                </p>
                <%
            }
            %>
        </td>
    </tr>

                    <tr>
                        <td colspan="2" class="inputLabel" style="text-align:left;">Details</td>
                    </tr>
                    <tr>
                        <td colspan="2">
                            <table width="100%">
                                <tr>
                                    <td class="matrixHead">SKU</td>
                                    <td class="matrixHead">Description</td>
                                    <td class="matrixHead">QTY</td>
                                    <td class="matrixHead">Size</td>
                                    <td class="matrixHead">Price</td>
                                    <td class="matrixHead">Total</td>
                                </tr>
                                <%
                                ArrayList details = order.getDetails();
                                cssClass = "matrixRow";
                                for (int i=0; i<details.size(); i++)  {
                                    OrderDetail detail = (OrderDetail)details.get(i);
                                    cssClass = (cssClass.equals("matrixRow")) ? "matrixRowAlternate" : "matrixRow";
                                    %>
                                    <tr>
                                        <td class="<%=cssClass%>"><%=detail.getProductVariation().getSku()%></td>
                                        <td class="<%=cssClass%>"><%=detail.getDescription()%></td>
                                        <td class="<%=cssClass%>"><%=detail.getQty()%></td>
                                        <td class="<%=cssClass%>"><%=detail.getSizeDesc()%></td>
                                        <td class="<%=cssClass%>" style="text-align:right;"><%=dollarFormat.format(detail.getPriceItem())%></td>
                                        <td class="<%=cssClass%>" style="text-align:right;"><%=dollarFormat.format(detail.getPriceTotal())%></td>
                                    </tr>
                                    <%
                                }
                                %>
                            </table>
                        </td>
                    </tr>
                </table>
            <% } %>
        </td>
    </tr>
</table>

<%@ include file = "../include/footer.jsp"%>
