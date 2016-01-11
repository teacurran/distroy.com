<%@ page import="com.approachingpi.store.servlet.admin.UserServlet,
                 com.approachingpi.user.UserSearchEngine,
                 com.approachingpi.user.User,
                 com.approachingpi.store.servlet.admin.AdminUserServlet"%>
<jsp:useBean id="rp" class="com.approachingpi.search.ResultPage" scope="request"/>
<jsp:useBean id="se" class="com.approachingpi.user.UserSearchEngine" scope="request"/>

<%@ include file = "./include/global.jsp"%>
<%
    strPage = "user";
    strPageTitle = "Admin User List";

    String strBaseUrl = "AdminUser?x=y";
    String strSortedUrl = strBaseUrl + "&sort=" + se.getFirstSort() + "&sortOrder=" + se.getSortOrder();
%>
<%@ include file = "./include/header.jsp"%>
<%@ include file = "./include/bodyTop.jsp"%>

<script language="javascript">
function gotoPage(intPage) {
    location="<%=strBaseUrl%>&page=" + intPage;
}
</script>

<div class="toolbar">
    <a href="<%=strBaseUrl%>&action=<%=AdminUserServlet.ACTION_EDIT%>" title="Create a new Admin User"><img src="<%=strImgAdminDir%>icons/icon_new_file.gif" height="20" width="20" border="0" vspace="10" alt="new file icon"></a>&nbsp;<a href="<%=strBaseUrl%>&action=<%=AdminUserServlet.ACTION_EDIT%>" title="Create a new Admin User">Create a new Admin User</a><br />
</div>

Page <%=rp.getPage()%> of <%=rp.getPageCount()%><br/>

<%
if (rp.getPageCount() > 0) {
    String uri = strSortedUrl;
    if (rp.getPage() > 1) {
        out.print("<a href=\"" + uri + "&page=" + (rp.getPage()-1) + "\">&lt;&lt;&nbsp;previous</a>&nbsp;&nbsp;");
    }
    for (int i=1; i<=rp.getPageCount(); i++) {
        if (i== rp.getPage()) {
            out.print(i + "&nbsp;&nbsp;");
        } else {
            out.print("<a href=\"" + uri + "&page=" + i + "\">" + i + "</a>&nbsp;&nbsp;");
        }
    }
    if (rp.getPage() < rp.getPageCount()) {
        out.print("<a href=\"" + uri + "&page=" + (rp.getPage()+1) + "\">next&nbsp;&gt;&gt;</a>");
    }
}
%>

<table>
    <tr>
        <td class="matrixHead"><a href="<%=strBaseUrl%>&sort=<%=UserSearchEngine.SORT_ID%>">Id</a></td>
        <td class="matrixHead"><a href="<%=strBaseUrl%>&sort=<%=UserSearchEngine.SORT_EMAIL%>">Email</a></td>
        <td class="matrixHead"><a href="<%=strBaseUrl%>&sort=<%=UserSearchEngine.SORT_DATE_CREATED%>">Created</a></td>
        <td class="matrixHead"><a href="<%=strBaseUrl%>&sort=<%=UserSearchEngine.SORT_DATE_MODIFIED%>">Modified</a></td>
    </tr>
    <%
    String cssClass = "matrixRow";
    rp.reset();
    while (rp.hasNext()) {
        User thisUser = (User)rp.next();
        cssClass = (cssClass.equalsIgnoreCase("matrixRow")) ? "matrixRowAlternate" : "matrixRow";
        %>
        <tr>
            <td class="<%=cssClass%>"><a href="<%=strBaseUrl%>&action=<%=UserServlet.ACTION_EDIT%>&userId=<%=thisUser.getId()%>"><%=thisUser.getId()%></a></td>
            <td class="<%=cssClass%>"><a href="<%=strBaseUrl%>&action=<%=UserServlet.ACTION_EDIT%>&userId=<%=thisUser.getId()%>"><%=thisUser.getEmail()%></a></td>
            <td class="<%=cssClass%>"><% if (thisUser.getDateCreated() != null) { %><%=sdf.format(thisUser.getDateCreated())%><% } %></td>
            <td class="<%=cssClass%>"><% if (thisUser.getDateModified() != null) { %><%=sdf.format(thisUser.getDateModified())%><% } %></td>
        </tr>
        <%
    }
    %>
</table>

<%@ include file = "./include/bodyBottom.jsp"%>
