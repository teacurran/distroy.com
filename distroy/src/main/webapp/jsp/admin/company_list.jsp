<%@ page import="com.approachingpi.store.servlet.admin.CompanyServlet,
                 com.approachingpi.user.CompanySearchEngine,
                 com.approachingpi.servlet.PiServlet,
                 com.approachingpi.user.Company,
                 com.approachingpi.user.User,
                 com.approachingpi.search.ResultPage"%>

<jsp:useBean id="rp" class="com.approachingpi.search.ResultPage" scope="request"/>
<jsp:useBean id="cse" class="com.approachingpi.user.CompanySearchEngine" scope="request"/>

<%@ include file = "./include/global.jsp"%>
<%
    strPage = "company";
    strPageTitle = "Company List";

    String strBaseUrl = "Company?x=y";
    String strSortedUrl = strBaseUrl + "&sort=" + cse.getFirstSort() + "&sortOrder" + cse.getSortOrder();
%>
<%@ include file = "./include/header.jsp"%>
<%@ include file = "./include/bodyTop.jsp"%>

<script language="javascript">
function gotoPage(intPage) {
    location="<%=strBaseUrl%>&page=" + intPage;
}
</script>

<a href="<%=strBaseUrl%>&action=<%=CompanyServlet.ACTION_EDIT%>" title="Create a new Company"><img src="<%=strImgAdminDir%>icons/icon_new_file.gif" height="20" width="20" border="0" vspace="10" alt="new file icon"></a>
<br />
<form method="post" action="<%=strBaseUrl%>">
Search <input type="text" name="term" value="<%=cse.getTerm()%>">
<input type="submit" name="button_submit" value="go">
</form>
<br />

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
        <td class="matrixHead"><a href="<%=strBaseUrl%>&sortOrder=<%=CompanySearchEngine.SORT_ID%>">Id</a></td>
        <td class="matrixHead"><a href="<%=strBaseUrl%>&sortOrder=<%=CompanySearchEngine.SORT_NAME%>">Name</a></td>
        <td class="matrixHead"><a href="<%=strBaseUrl%>&sortOrder=<%=CompanySearchEngine.SORT_STATUS%>">Status</a></td>
        <td class="matrixHead"><a href="<%=strBaseUrl%>&sortOrder=<%=CompanySearchEngine.SORT_DATE_CREATED%>">Created</a></td>
        <td class="matrixHead"><a href="<%=strBaseUrl%>&sortOrder=<%=CompanySearchEngine.SORT_DATE_MODIFIED%>">Modified</a></td>
        <td class="matrixHead"><a href="<%=strBaseUrl%>&sortOrder=<%=CompanySearchEngine.SORT_DATE_ACTIVE%>">Active</a></td>
    </tr>
    <%
    String cssClass = "matrixRow";
    rp.reset();
    while (rp.hasNext()) {
        Company company = (Company)rp.next();

        if (cssClass.equalsIgnoreCase("matrixRow")) {
            cssClass = "matrixRowAlternate";
        } else {
            cssClass = "matrixRow";
        }
        %>
        <tr>
            <td class="<%=cssClass%>"><a href="/admin/Company?action=<%=CompanyServlet.ACTION_EDIT%>&companyId=<%=company.getId()%>"><%=company.getId()%></a></td>
            <td class="<%=cssClass%>"><a href="/admin/Company?action=<%=CompanyServlet.ACTION_EDIT%>&companyId=<%=company.getId()%>"><%=company.getName()%></a></td>
            <td class="<%=cssClass%>"><%=Company.getStatusNameById(company.getStatus())%></td>
            <td class="<%=cssClass%>"><%=sdf.format(company.getDateCreated())%></td>
            <td class="<%=cssClass%>"><%=sdf.format(company.getDateModified())%></td>
            <td class="<%=cssClass%>"></td>
        </tr>
        <%
    }
    %>
</table>

<%@ include file = "./include/bodyBottom.jsp"%>
