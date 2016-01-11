<%@ page import="com.approachingpi.store.servlet.admin.CompanyServlet,
                 com.approachingpi.user.Company,
                 com.approachingpi.user.User,
                 com.approachingpi.user.UserSearchEngine,
                 com.approachingpi.store.servlet.admin.UserServlet,
                 com.approachingpi.store.servlet.admin.ProductServlet"%>
<jsp:useBean id="company" class="com.approachingpi.user.Company" scope="request"/>

<%@ include file = "./include/global.jsp"%>
<%
    strPage = "company";
    strPageTitle = "Company Edit";

    String strBaseUrl = "Company?x=y";

    String dateCreated  = (company.getDateCreated() == null) ? "" : timeFormat.format(company.getDateCreated());
    String dateModified = (company.getDateModified() == null) ? "" : timeFormat.format(company.getDateModified());
    String dateActive   = (company.getDateActive() == null) ? "" : timeFormat.format(company.getDateActive());
%>
<%@ include file = "./include/header.jsp"%>
<%@ include file = "./include/bodyTop.jsp"%>

<a href="/admin/Company?action=<%=CompanyServlet.ACTION_LIST%>" title="Back to List"><img src="<%=strImgAdminDir%>icons/icon_arrow_left.gif" border="0" alt="left arrow"></a><br>

<form method="post" action="<%=strBaseUrl%>%>" name="mainForm">
<input type="hidden" name="action" value="<%=CompanyServlet.ACTION_SAVE%>">
<input type="hidden" name="companyId" value="<%=company.getId()%>">
<input type="hidden" name="formSubmitted" value="true">

<table>
    <tr>
        <td class="inputLabel">Id</td>
        <td>
            <% if (company.getId() > 0) {
                %><%=company.getId()%><%
            } else {
                    %>new<%
            }
            %>
        </td>
    </tr>
    <% if (company.getId() > 0) { %>
        <tr>
            <td class="inputLabel">Date Created</td>
            <td><%=dateCreated%></td>
        </tr>
        <tr>
            <td class="inputLabel">Date Modified</td>
            <td><%=dateModified%></td>
        </tr>
        <%--
        <tr>
            <td class="inputLabel">Date Active</td>
            <td><%=dateActive%></td>
        </tr>
        --%>
    <% } %>
    <tr>
        <td class="inputLabel">Name</td>
        <td><input type="text" name="name" value="<%=company.getName()%>"></td>
    </tr>
    <tr>
        <td class="inputLabel">Status</td>
        <td>
            <select name="status">
                <option value=""></option>
                <%
                int[] list = Company.STATUS_LIST;
                for (int i=0; i<list.length; i++) {
                    int thisStatus = list[i];
                    String selectedVal = (thisStatus == company.getStatus()) ? " selected" : "";
                    %><option value="<%=thisStatus%>"<%=selectedVal%>><%=Company.getStatusNameById(thisStatus)%></option>
                    <%
                }
                %>
            </select>
        </td>
    </tr>
    <tr>
        <td></td>
        <td><input type="submit" name="button_save" value="save"></td>
    </tr>
<table>
</form>

<% if (company.getId() > 0) { %>
    <table width="500">
    <tr>
        <td style="backgroung-color: #EEEEEE;">
            <table width="100%">
                <tr>
                    <td class="inputLabel" style="text-align:left;">Users</td>
                    <td>
                        <a href="/admin/User?action=<%=UserServlet.ACTION_EDIT%>&companyId=<%=company.getId()%>" title="Create a new User"><img src="<%=strImgAdminDir%>icons/icon_new_file<% if (company.getId()==0) { out.print("_disabled"); } %>.gif" height="20" width="20" border="0"></a>
                    </td>
                </tr>
            </table>
        </td>
    </tr>
    <tr>
        <td>
            <table width="100%">
                <tr>
                    <td class="matrixHead">Id</td>
                    <td class="matrixHead">Type</td>
                    <td class="matrixHead">Name</td>
                    <td class="matrixHead">Email</td>
                    <td class="matrixHead">Created</td>
                    <td class="matrixHead">Modified</td>
                </tr>
                <%
                String cssClass = "matrixRow";
                ArrayList users = company.getUsers();
                for (int i=0; i<users.size(); i++) {
                    User thisUser = (User)users.get(i);

                    cssClass = (cssClass.equalsIgnoreCase("matrixRow")) ? "matrixRowAlternate" : "matrixRow";
                    %>
                    <tr>
                        <td class="<%=cssClass%>"><a href="/admin/User?action=<%=UserServlet.ACTION_EDIT%>&userId=<%=thisUser.getId()%>&companyId=<%=company.getId()%>"><%=thisUser.getId()%></a></td>
                        <td class="<%=cssClass%>"><%=User.getTypeName(thisUser.getType())%></td>
                        <td class="<%=cssClass%>"><a href="/admin/User?action=<%=UserServlet.ACTION_EDIT%>&userId=<%=thisUser.getId()%>&companyId=<%=company.getId()%>"><%=thisUser.getActiveBillingAddress().getNameLast()%>, <%=thisUser.getActiveBillingAddress().getNameFirst()%></a></td>
                        <td class="<%=cssClass%>"><a href="/admin/User?action=<%=UserServlet.ACTION_EDIT%>&userId=<%=thisUser.getId()%>&companyId=<%=company.getId()%>"><%=thisUser.getEmail()%></a></td>
                        <td class="<%=cssClass%>"><% if (thisUser.getDateCreated() != null) { out.write(timeFormat.format(thisUser.getDateCreated())); } %></td>
                        <td class="<%=cssClass%>"><% if (thisUser.getDateCreated() != null) { out.write(timeFormat.format(thisUser.getDateModified())); } %></td>
                    </tr>
                    <%
                }
                %>
            </table>
        </td>
    </tr>
    </table>
<% } %>

<%@ include file = "./include/bodyBottom.jsp"%>

