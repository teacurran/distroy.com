<%@ page import="com.approachingpi.store.catalog.Category,
                 java.util.ArrayList,
                 com.approachingpi.store.servlet.admin.CategoryServlet,
                 java.lang.Integer,
                 java.util.Set,
                 java.util.Iterator,
                 java.util.Map,
                 com.approachingpi.servlet.PiServlet"%>

<jsp:useBean id="category"      class="com.approachingpi.store.catalog.Category" scope="request"/>
<jsp:useBean id="categories"    class="java.util.ArrayList" scope="request"/>

<%@ include file = "./include/global.jsp"%>
<%
strPage = "category";
strPageTitle = "Categories";
%>
<%@ include file = "./include/header.jsp"%>
<%@ include file = "./include/bodyTop.jsp"%>

<form method="post" action="/admin/Category">
<input type="hidden" name="action" value="<%=CategoryServlet.ACTION_EDIT%>">
<select name="categoryId">
<option value="0">New Category</option>
<%
for(int i=0;i<categories.size();i++) {
    Category thisCategory = (Category)categories.get(i);
    %><option value="<%=thisCategory.getId()%>"<% if(thisCategory.getId()==category.getId()) { out.write(" selected"); }%>><%=thisCategory.getPathAsString()%></option><%
}
%>
</select>
<input type="submit" value="go">
</form>

<form method="post" action="/admin/Category" name="mainForm">
<input type="hidden" name="action" value="<%=CategoryServlet.ACTION_SAVE%>">
<input type="hidden" name="categoryId" value="<%=category.getId()%>">

<table>
    <tr>
        <td class="inputLabel">Id</td>
        <td class="label"><% if (category.getId() > 0) {
                %><%=category.getId()%><%
            } else {
                %>new<%
            }
            %>
        </td>
    </tr>
    <% if (category.getId() > 0) { %>
        <tr>
            <td class="inputLabel"></td>
            <td class="input"><a href="javascript:showDeleteBox('Category', '/admin/Category?categoryId=<%=category.getId()%>&action=<%=CategoryServlet.ACTION_DELETE%>&deleteConfirm=true')">delete</a></td>
        </tr>
    <% } %>
    <tr>
        <td class="inputLabel">Parent</td>
        <td class="input">
            <select name="parentId">
                <option value="0">No Parent</option>
                <%
                for(int i=0;i<categories.size();i++) {
                    Category thisCategory = (Category)categories.get(i);
                    if (thisCategory.getId() != category.getId()) {
                        %><option value="<%=thisCategory.getId()%>"<%if(thisCategory.getId() == category.getParent().getId()) { out.write(" selected"); }%>><%=thisCategory.getPathAsString()%></option><%
                    }
                }
                %>
            </select>
        </td>
    </tr>
    <tr>
        <td class="inputLabel">Name</td>
        <td class="input"><input type="text" name="name" value="<%=formHash.get("name")%>"></td>
    </tr>
    <tr>
        <td class="inputLabel">Active</td>
        <td class="input"><input type="checkbox" name="active" value="true"<%if(category.getActive()) {out.write(" checked");}%>></td>
    </tr>
    <tr>
        <td class="inputLabel"></td>
        <td class="input"><input type="submit" name="button_save" value="save"></td>
    </tr>
</table>

</form>

<%@ include file = "./include/bodyBottom.jsp"%>
