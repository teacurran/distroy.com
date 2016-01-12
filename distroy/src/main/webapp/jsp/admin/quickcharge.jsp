<jsp:useBean id="payment"     class="com.approachingpi.store.order.Payment" scope="request"/>
<jsp:useBean id="reference"     class="java.lang.String" scope="request"/>

<%
String expireMonth = (payment.getExpireMonth() > 0) ? Integer.toString(payment.getExpireMonth()) : "";
String expireYear = (payment.getExpireYear() > 0) ? Integer.toString(payment.getExpireYear()) : "";
String amount = (payment.getAmount().doubleValue() > 0) ? payment.getAmount().toString() : "";
String cvvNumber = (payment.getCcvNumber() > 0) ? Integer.toString(payment.getCcvNumber()) : "";


%>

<%@ include file = "./include/global.jsp"%>

<html>
<head>
<title>Approaching Pi :: <%=strPageTitle%></title>

<link rel="stylesheet" type="text/css" href="/css/admin.css">
<script language="javaScript" SRC="/js/util.js"></script>

</head>
<body>
<%@ include file = "./include/messages.jsp"%>

<form method="post" action="qc">
<table>
    <tr>
        <td class="inputLabel">Name:</td>
        <td><input type="text" size="20" name="name" value="<%=payment.getCreditName()%>"></td>
    </tr>
    <tr>
        <td class="inputLabel">Credit Number:</td>
        <td><input type="text" size="20" name="cardnum" value="<%=payment.getAccountNumber()%>"></td>
    </tr>
    <tr>
        <td class="inputLabel">Expire:</td>
        <td>
            <input type="text" size="5" name="expireMonth" value="<%=expireMonth%>">/<input type="text" size="5" name="expireYear" value="<%=expireYear%>">
       </td>
    </tr>
    <tr>
        <td class="inputLabel">CVV 2:</td>
        <td><input type="text" size="10" name="cvvNumber" value="<%=cvvNumber%>"></td>
    </tr>
    <tr>
        <td class="inputLabel">Amount $:</td>
        <td><input type="text" size="10" name="amount" value="<%=amount%>"></td>
    </tr>
    <tr>
        <td class="inputLabel">Reference:</td>
        <td><input type="text" size="10" name="reference" value="<%=reference%>"></td>
    </tr>

    <tr>
        <td></td>
        <td><input type="submit" name="button_submit" value="charge"></td>
    </tr>

</table>
</form>

</body>

