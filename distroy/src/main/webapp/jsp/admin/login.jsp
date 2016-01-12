<%@ page import="com.approachingpi.store.servlet.LoginServlet,
                 com.approachingpi.servlet.PiServlet"%>

<jsp:useBean id="loginForm"   class="java.lang.String" scope="request"/>
<jsp:useBean id="loginReturn"   class="java.lang.String" scope="request"/>

<form method="post" action="Login">
<input type="hidden" name="action" value="<%=LoginServlet.ACTION_LOGIN%>">
<input type="hidden" name="loginForm" value="<%=loginForm%>">
<input type="hidden" name="loginReturn" value="<%=loginReturn%>">
<table>
<tr>
	<td class="inputLabel" id="l_email">Email Address</td>
	<td><input type="text" class="text" name="email" value="<%=PiServlet.getReqString(request,"email")%>"></td>
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
