<%@ page import="com.approachingpi.store.servlet.CartServlet"%>

<jsp:useBean id="order"      class="com.approachingpi.store.order.Order" scope="request"/>

<%@ include file = "../include/global.jsp"%>
<%
	strPageTitle = "Shopping Cart";
%>

<script language="javascript">
function processPaypal() {
	document.mainForm.submit();
}
</script>

<body onLoad="processPaypal()">
<b>Almost done</b>
<p>
You will now be forwarded to PayPal to complete payment for your order.
If you are not forwarded automatically, please <a href="javascript:processPaypal()">click here</a>.
</p>

<form action="https://www.paypal.com/cgi-bin/webscr" method="post" name="mainForm">
<input type="hidden" name="cmd" value="_xclick">
<input type="hidden" name="business" value="weborders@distroy.com">
<input type="hidden" name="return" value="https://www.distroy.com<%=strBaseUrl%>cart?action=<%=CartServlet.ACTION_PAYPALTHANKS%>&orderId=<%=order.getId()%>">
<input type="hidden" name="item_name" value="DISTRO.Y Order [<%=order.getId()%>]">
<input type="hidden" name="item_number" value="<%=order.getId()%>">
<input type="hidden" name="amount" value="<%=order.getAmountTotal()%>">
<input type="hidden" name="custom" value="<%=order.getId()%>">
<input type="hidden" name="invoice" value="<%=order.getId()%>">
<input type="hidden" name="no_note" value="1">
<input type="hidden" name="no_shipping" value="1">
<input type="hidden" name="currency_code" value="USD">
<input type="hidden" name="lc" value="US">
</form>

</body>
