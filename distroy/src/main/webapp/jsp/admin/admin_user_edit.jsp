<%@ page import="com.approachingpi.store.servlet.admin.UserServlet,
                 com.approachingpi.servlet.PiServlet,
                 com.approachingpi.user.User,
                 com.approachingpi.user.Address,
                 com.approachingpi.store.servlet.admin.CompanyServlet"%>
<jsp:useBean id="userEdit" class="com.approachingpi.user.User" scope="request"/>

<%@ include file = "./include/global.jsp"%>
<%
    strPage = "user";
    strPageTitle = "Admin User Edit";

    String strBaseUrl = "AdminUser?x=y";

    String dateCreated  = (userEdit.getDateCreated() == null) ? "" : timeFormat.format(userEdit.getDateCreated());
    String dateModified = (userEdit.getDateModified() == null) ? "" : timeFormat.format(userEdit.getDateModified());
    String dateLastActive = ""; //(userEdit.getDateLastActive() == null) ? "" : timeFormat.format(userEdit.getDateLastActive());

%>
<%@ include file = "./include/header.jsp"%>
<%@ include file = "./include/bodyTop.jsp"%>

<div class="toolbar">
    <a href="/admin/AdminUser?action=<%=UserServlet.ACTION_LIST%>" title="Back to User List"><img src="<%=strImgAdminDir%>icons/icon_arrow_left.gif" border="0" alt="left arrow"></a><br>
</div>

<form method="post" action="<%=strBaseUrl%>%>" name="mainForm">
<input type="hidden" name="action" value="<%=CompanyServlet.ACTION_SAVE%>">
<input type="hidden" name="userId" value="<%=userEdit.getId()%>">
<input type="hidden" name="companyId" value="<%=userEdit.getCompany().getId()%>">
<input type="hidden" name="formSubmitted" value="true">

<table>
    <tr>
        <td class="inputLabel">Id</td>
        <td>
            <% if (userEdit.getId() > 0) {
                %><%=userEdit.getId()%><%
            } else {
                    %>new<%
            }
            %>
        </td>
    </tr>
    <% if (userEdit.getId() > 0) { %>
        <tr>
            <td class="inputLabel">Date Created</td>
            <td><%=dateCreated%></td>
        </tr>
        <tr>
            <td class="inputLabel">Date Modified</td>
            <td><%=dateModified%></td>
        </tr>
    <% } %>
    <tr>
        <td class="inputLabel">Email</td>
        <td><input type="text" name="email" value="<%=userEdit.getEmail()%>"></td>
    </tr>
    <tr>
        <td class="inputLabel">Password</td>
        <td><input type="text" name="password" value="<%=userEdit.getPassword()%>"></td>
    </tr>
    <tr>
        <td></td>
        <td><input type="submit" name="button_save" value="save"></td>
    </tr>
</table>
</form>

<%@ include file = "./include/bodyBottom.jsp"%>

