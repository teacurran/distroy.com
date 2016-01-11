<%@ page import="org.apache.commons.lang.StringEscapeUtils" %>

<jsp:useBean id="email"   class="java.lang.String" scope="request"/>

<%@ include file = "./include/global.jsp"%>
<%
int action = altAttribute.getInt("action");
String subChecked = (action == MailingListServlet.ACTION_SUB) ? " checked " : "";
String unsubChecked = (action == MailingListServlet.ACTION_UNSUB) ? " checked " : "";
strPageTitle = "Login";
%>
<%@ include file = "./include/header.jsp"%>

<table border="0" cellpadding="0" cellspacing="0" width="100%">
    <tr>
        <td class="pageHead">MAILING LIST</td>
    </tr>
    <tr>
        <td class="content" height="300">
            <table width="75%" cellpadding="0" cellspacing="0" border="0" style="margin-left:auto; margin-right:auto; text-align:center;">
                <tr>
                    <td class="content" style="padding-top:0px;">
                        If you would like to receive an email when we update the site or hold special events, enter your email address below.
                        <p>
                        We don't send out mailings too often (usually once a month at most).  We also HATE spam and will never sell or give away your email address to anyone.
                        <p>
                        You can come back here any time to remove your email address from our mailing list.
                        <p>
                        <form method="post" action="<%=strBaseUrl%>mailinglist">
                        <table style="margin-left:auto; margin-right:auto; text-align:center;">
                            <tr>
                                <td class="inputLabel">Email Address</td>
                                <td><input type="text" class="text" size="50" maxlength="200" name="email" value="<%=StringEscapeUtils.escapeHtml(email)%>"></td>
                            </tr>
                            <tr>
                                <td></td>
                                <td>
                                    <input type="radio" name="action" value="<%=MailingListServlet.ACTION_SUB%>"<%=subChecked%>>Subscribe<br>
                                    <input type="radio" name="action" value="<%=MailingListServlet.ACTION_UNSUB%>"<%=unsubChecked%>>Unsubscribe
                                </td>
                            </tr>
                            <tr>
                                <td></td>
                                <td><input type="submit" class="button" name="button_signup" value="SUBMIT FORM"></td>
                            </tr>
                        </table>
                        </form>
                    </td>
                </tr>
            </table>
        </td>
    </tr>
</table>



<%@ include file = "./include/footer.jsp"%>





