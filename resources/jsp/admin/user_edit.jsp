<%@ page import="com.approachingpi.store.servlet.admin.UserServlet,
                 com.approachingpi.servlet.PiServlet,
                 com.approachingpi.user.User,
                 com.approachingpi.user.Address,
                 com.approachingpi.store.servlet.admin.CompanyServlet"%>
<jsp:useBean id="userEdit" class="com.approachingpi.user.User" scope="request"/>

<%@ include file = "./include/global.jsp"%>
<%
    strPage = "user";
    strPageTitle = "User Edit";

    String strBaseUrl = "User?x=y";

    String dateCreated  = (userEdit.getDateCreated() == null) ? "" : timeFormat.format(userEdit.getDateCreated());
    String dateModified = (userEdit.getDateModified() == null) ? "" : timeFormat.format(userEdit.getDateModified());
    String dateLastActive = ""; //(userEdit.getDateLastActive() == null) ? "" : timeFormat.format(userEdit.getDateLastActive());

    Address billing = userEdit.getActiveBillingAddress();
    Address shipping = userEdit.getActiveBillingAddress();

    if (billing.getId() == shipping.getId()) { 
        shipping = new Address();
    }
%>
<%@ include file = "./include/header.jsp"%>
<%@ include file = "./include/bodyTop.jsp"%>

<% if (PiServlet.getReqInt(request,"companyId") > 0) { %>
    <a href="/admin/Company?action=<%=CompanyServlet.ACTION_EDIT%>&companyId=<%=userEdit.getCompany().getId()%>" title="Back to Company"><img src="<%=strImgAdminDir%>icons/icon_arrow_left.gif" border="0" alt="left arrow"></a><br>
<% } else { %>
    <a href="/admin/User?action=<%=UserServlet.ACTION_LIST%>" title="Back to User List"><img src="<%=strImgAdminDir%>icons/icon_arrow_left.gif" border="0" alt="left arrow"></a><br>
<% } %>

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
    <% if (userEdit.getCompany().getId() > 0) { %>
        <tr>
            <td class="inputLabel">Customer</td>
            <td><%=userEdit.getCompany().getName()%></td>
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
        <td class="inputLabel">Type</td>
        <td>
            <select name="type">
                <option value="<%=User.TYPE_PUBLIC%>"<% if (userEdit.getType() == User.TYPE_PUBLIC) { out.write(" selected"); } %>>Public (retail site)</option>
                <option value="<%=User.TYPE_WHOLESALE_PENDING%>"<% if (userEdit.getType() == User.TYPE_WHOLESALE_PENDING) { out.write(" selected"); } %>>Wholesale Pending</option>
                <option value="<%=User.TYPE_WHOLESALE%>"<% if (userEdit.getType() == User.TYPE_WHOLESALE) { out.write(" selected"); } %>>Wholesale</option>
            </select>
        </td>
    </tr>
    <tr>
        <td></td>
        <td><input type="submit" name="button_save" value="save"></td>
    </tr>
</table>
</form>

<% if (billing.getId() > 0) { %>
    <b>Billing Information:</b><br/>
    <%=billing.getNameFirst()%>&nbsp;<%=billing.getNameLast()%><br/>
    <%=billing.getAddress1()%><br/>
    <% if (billing.getAddress2().length() > 0) { %>
        <%=billing.getAddress2()%><br/>
    <% } %>
    <%=billing.getCity()%><br/>
    <%=billing.getState().getAbbrev()%><br/>
    <%=billing.getZip()%><br/>
    <%=billing.getCountry().getName()%><br/>
    <%=billing.getPhoneNumber()%><br/>
<% } %>

<% if (shipping.getId() > 0) { %>
    <b>Shipping Information:</b><br/>
    <%=shipping.getNameFirst()%>&nbsp;<%=shipping.getNameLast()%><br/>
    <%=shipping.getAddress1()%><br/>
    <% if (shipping.getAddress2().length() > 0) { %>
        <%=shipping.getAddress2()%><br/>
    <% } %>
    <%=shipping.getCity()%><br/>
    <%=shipping.getState().getAbbrev()%><br/>
    <%=shipping.getZip()%><br/>
    <%=shipping.getCountry().getName()%><br/>
    <%=shipping.getPhoneNumber()%><br/>
<% } %>


<%@ include file = "./include/bodyBottom.jsp"%>

