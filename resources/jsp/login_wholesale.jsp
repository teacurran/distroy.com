<%@ page import="com.approachingpi.store.servlet.LoginServlet" %>

<jsp:useBean id="strUserName"   class="java.lang.String" scope="request"/>

<%@ include file = "./include/global.jsp"%>
<%
strPageTitle = "Login";
boShowNav = false;
%>
<%@ include file = "./include/header.jsp"%>

<script language="javascript">
    function signUp() {
        location="<%=strBaseUrl%>application";
    }
</script>

<table border="0" cellpadding="0" cellspacing="0" width="100%">
    <tr>
        <td class="pageHead">DISTRO.Y WHOLESALE WEBSITE</td>
    </tr>
    <tr>
        <td class="contentText">
            <table>
                <tr>
                    <td colspan="2" style="padding-left:10px; padding-right:10px;">
                        <p>Looking for the DISTRO.Y retail website?  It can be found <a href="/">here</a></p>
                        <p>
                        This portion of our website is restricted access for Wholesale customers only.
                        </p>
                        <p>
                        We wanted to make your experience buying from us as simple as possible so we custom
                        built this site from the ground up in order to give you a single portal to handle
                        all your ordering needs.
                        </p>
                        <p>
                        While most people still choose to place orders over the phone and via fax, using this
                        site gives you quick access to the following features:
                        <ul>
                            <li>Quick online placing of orders</li>
                            <li>Order shipment tracking</li>
                            <li>Up to date information about backorders</li>
                            <li>Current accurate prices, including closeouts and special offers</li>
                            <li>New products that may not yet be in the print catalog</li>
                            <li>PDF copies of all catalogs, forms, and pricelists</li>
                        </ul>
                        </p>
                    </td>
                </tr>
                <tr>
                    <td style="padding:10px;">
                        If you already have a wholesale account with us, login here:
                        <br /><br />
                        <form method="post" action="<%=strBaseUrl%>login">
                        <input type="hidden" name="action" value="<%=LoginServlet.ACTION_LOGIN%>">
                        <table>
                        <tr>
                            <td class="inputLabel" id="l_email">Email Address</td>
                            <td><input type="text" class="text" name="email" value="<%=strUserName%>"></td>
                        </tr>
                        <tr>
                            <td class="inputLabel">Password</td>
                            <td><input type="password" class="text" name="password" value=""></td>
                        </tr>
                        <tr>
                            <td></td>
                            <td><input type="submit" class="button" name="button_login" value="LOGIN"></td>
                        </tr>
                        </table>
                        </form>
                    </td>
                    <td width="50%" style="padding:10px; border-left-width:1px; border-left-color:#0072BC; border-left-style: solid; vertical-align:top;">
                        If you would like to set up a wholesale account, please fill out
                        <a href="<%=strBaseUrl%>application">this quick application</a>.  If everything checks out, we can usually
                        get your account set up next business day.  We will also mail you out a print catalog and assign you a sales rep
                        to handle you're orders.
                        <br /><br />
                        <input type="button" class="button" value="SIGN UP FOR AN ACCOUNT" onclick="signUp()">
                    </td>
                </tr>
            </table>
        </td>
    </tr>
</table>



<%@ include file = "./include/footer.jsp"%>
