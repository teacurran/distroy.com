<%-- 
    Document   : forgot_password_popup
    Created on : Sep 18, 2008, 3:53:15 PM
    Author     : lroberts
--%>

<%@ page import="java.util.ArrayList,
                 com.approachingpi.store.servlet.*,
                 com.approachingpi.servlet.*,
                 com.approachingpi.user.*"%>

<jsp:useBean id="theUser"      class="com.approachingpi.user.User" scope="request"/>

<%@ include file = "include/global.jsp"%>
<%
strPageTitle = "Password Lookup";
boShowNav = false;
boShowFoot = false;
boIsPopup = true;
%>
<%@ include file = "include/header.jsp"%>
<div class="contentText">
 <form method="post" action="/forgotpassword" name="loginForm">
<table width="450" border="0" class="matrix">
  <tr><th class="matrixHead" colspan="2">Lookup Password</th></tr>
  <tr><td class="sectionSpace" colspan="2">&nbsp;</td></tr>
  <tr><td>&nbsp;Please enter your email address:</td><td><input type="text" class="text" name="email" size="40" maxlength="200" value="<%=PiServlet.getReqString(request,"email")%>" /></td></tr>
  <tr><td class="sectionSpace" colspan="2">&nbsp;</td></tr>
  <tr><td colspan="2" style="text-align:center"><input type="submit" class="button" value="submit"></td></tr>
  <tr><td class="sectionSpace" colspan="2">&nbsp;</td></tr>
</table>
</form>
<br /><br /><br />
</div>

<%@ include file = "include/footer.jsp"%>

