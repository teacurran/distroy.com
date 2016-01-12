<%@ page import="com.approachingpi.store.cart.CartItem,
                 com.approachingpi.store.order.Order,
                 com.approachingpi.store.servlet.OrderStatusServlet"%>
<jsp:useBean id="resultPage"    class="com.approachingpi.search.ResultPage" scope="request"/>

<%@ include file = "../include/global.jsp"%>
<%
strPageTitle = "Order Status";
strActivateMenu = "2";
%>
<%@ include file = "../include/header.jsp"%>

<table border="0" cellpadding="0" cellspacing="0" width="100%">
    <tr>
        <td class="pageHead">ORDER STATUS</td>
    </tr>
    <tr>
        <td class="contentText" style="padding:10px;" height="300">
            <% if (resultPage.getCountTotal() == 0) { %>
                <b>No orders found.</b><br />
                If you have placed an order and it is not showing up here, please contact us at
                <a href="mailto:support@distroy.com">support@distroy.com</a>.  Please be sure to include your name
                and order number.  It is also helpful to include a phone number and best time to call so we can contact
                you if neccessary.
            <% } else { %>
                <b>Please select an order from the list below.</b><br /><br />
                <table width="80%" style="margin-left:auto; margin-right:auto;">
                    <tr id="cart_head">
                        <td class="matrixHead" scope="column">DATE</td>
                        <td class="matrixHead" scope="column">ORDER ID</td>
                        <td class="matrixHead" scope="column" style="text-align:center;padding-left:0px;">PRICE</td>
                        <td class="matrixHead" scope="column">STATUS</td>
                        <td class="matrixHead" scope="column"></td>
                    </tr>
                    <%
                    String cssClass = "matrixRow";
                    ArrayList items = resultPage.getItems();

                    for (int i=0; i<items.size(); i++) {
                        Order thisOrder = (Order)items.get(i);

                        cssClass = (cssClass.equalsIgnoreCase("matrixRow"))? "matrixRowAlternate" : "matrixRow";
                        %>
                        <tr id="row_<%=i%>">
                            <td class="<%=cssClass%>"><%=sdf.format(thisOrder.getDateCreated())%></td>
                            <td class="<%=cssClass%>"><%=thisOrder.getId()%></td>
                            <td class="<%=cssClass%>"><%=dollarFormat.format(thisOrder.getAmountTotal())%></td>
                            <td class="<%=cssClass%>"><%=Order.getStatusById(thisOrder.getStatus())%></td>
                            <td class="<%=cssClass%>"><a href="/orderstatus?action=<%=OrderStatusServlet.ACTION_VIEW%>&id=<%=thisOrder.getId()%>">view detail</a></td>
                        </tr>
                        <%
                    }
                    %>
                </table>
            <% } %>
        </td>
    </tr>
</table>

<%@ include file = "../include/footer.jsp"%>
