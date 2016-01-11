<%@ page import="com.approachingpi.servlet.PiServlet,
                 com.approachingpi.store.order.Payment"%>

<jsp:useBean id="order"      class="com.approachingpi.store.order.Order" scope="request"/>

<%@ include file = "../include/global.jsp"%>
<%
	strPageTitle = "Your Order is Completed - Thank You!";
    String paymentType = PiServlet.getReqString(request,"paymentType",Payment.TYPE_CC);
boShowNav = false;
%>
<%@ include file = "../include/header.jsp"%>

<table width="100%">
    <tr>
        <td class="pageHead">Thank You!</td>
    </tr>
    <tr>
        <td class="contentText" height="300" style="text-align:center;">
            <b>
			Thank you for your order.
			<br><br>
            Your order id is:<br>
            <%=order.getId()%>
            <br><br>
                Please print this page for your records.
                <br><br>
                <% if (paymentType.equalsIgnoreCase(Payment.TYPE_CHECK)) { %>
                <table width="90%" style="border-style:solid;border-color:black;border-width:1px; margin-left:auto; margin-right:auto">
                    <tr>
                        <td class="contentText">
                            <b>
                            You chose to pay by check or money order for this order.  Nothing will ship until we recieve payment in the mail.  
                            <br><br>
                            Personal checks may be used for US orders ONLY and must be drawn on a US bank.
                            <br><br>
                            Personal checks will be held for 10 days before shipment is sent.
                            <br><br>
                            International orders must send an international money order in US dollar funds.
                            <br><br>
                            Please write the Order id "<%=order.getId()%>" in the memo field of payment.
                            <br><br>
                            All checks and Money Orders should be made out to "DISTRO.Y" and mailed to:<br>
                            DISTRO.Y<br>
                            129 Kingston St.<br>
                            5th Floor<br>
                            Boston MA, 02111<br>
                            </b>
                        </td>
                    </tr>
                </table>
                <br><br>
            <% } %>
            </b>
        </td>
    </tr>
</table>

<%@ include file = "../include/footer.jsp"%>
