<%@ page import="com.approachingpi.store.servlet.LoginServlet" %>

<jsp:useBean id="strUserName"   class="java.lang.String" scope="request"/>

<%@ include file = "./include/global.jsp"%>
<%
strPageTitle = "Login";
%>
<%@ include file = "./include/header.jsp"%>


<table border="0" cellpadding="0" cellspacing="0" width="100%">
    <tr>
        <td class="pageHead">LOGIN</td>
    </tr>
    <tr>
        <td class="contentText">

            <form method="post" action="<%=strBaseUrl%>login">
            <input type="hidden" name="action" value="<%=LoginServlet.ACTION_LOGIN%>">
            <table>
            <tr>
                <td>Username</td>
                <td><input type="text" class="text" name="email" value="<%=strUserName%>"></td>
            </tr>
            <tr>
                <td>Password</td>
                <td><input type="password" class="text" name="password" value=""></td>
            </tr>
            <tr>
                <td></td>
                <td><input type="submit" class="button" name="button_login" value="LOGIN"></td>
            </tr>
            </table>
            </form>
            
        </td>
    </tr>
</table>



<%@ include file = "./include/footer.jsp"%>
