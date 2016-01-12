<%@ page import="com.approachingpi.store.servlet.admin.ContentServlet,
                 com.approachingpi.servlet.PiServlet,
                 com.approachingpi.store.site.Content,
                 com.approachingpi.user.User,
                 java.util.ArrayList"%>

<jsp:useBean id="content"       class="com.approachingpi.store.site.Content" scope="request"/>

<%@ include file = "./include/global.jsp"%>
<%
    strPage = "content";
    strPageTitle = "Content Edit";
%>
<%@ include file = "./include/header.jsp"%>
<%@ include file = "./include/bodyTop.jsp"%>

<a href="/admin/Content?action=<%=ContentServlet.ACTION_LIST%>">back to main</a><br>

<form method="post" action="/admin/Content" name="mainForm">
<input type="hidden" name="action" value="<%=ContentServlet.ACTION_EDIT%>">
<input type="hidden" name="contentId" value="<%=content.getId()%>">
<input type="hidden" name="formSubmitted" value="true">

<table>
    <tr>
        <td class="inputLabel">Id</td>
        <td><% if (content.getId() > 0) {
                %><%=content.getId()%><%
            } else {
                %>new<%
            }
            %>
        </td>
    </tr>
    <tr>
        <td class="inputLabel">Created</td>
        <td><%if(content.getDateCreated() != null) { out.write(timeFormat.format(content.getDateCreated())); }%></td>
    </tr>
    <tr>
        <td class="inputLabel">Modified</td>
        <td><%if(content.getDateModified() != null) { out.write(timeFormat.format(content.getDateModified())); }%></td>
    </tr>
    <tr>
        <td class="inputLabel">Access Level</td>
        <td>
            <select name="accessRequired">
            <%
            int[] accessTypes = User.TYPES;
            for (int i=0; i<accessTypes.length; i++) {
                int thisAccess = accessTypes[i];
                String selectVal = (thisAccess == content.getAccessRequired()) ? " selected" : "";
                %><option value="<%=thisAccess%>"<%=selectVal%>><%=User.getTypeName(thisAccess)%></option>
                <%
            }
            %>
            </select>
        </td>
    </tr>
    <tr>
        <td class="inputLabel">Url</td>
        <td><input type="text" name="url" size="50" maxlength="50" value="<%=content.getUrl()%>"></td>
    </tr>
    <tr>
        <td class="inputLabel">Title</td>
        <td><input type="text" name="title" size="50" maxlength="50" value="<%=content.getTitle()%>"></td>
    </tr>
    <tr>
        <td class="inputLabel">Body HTML</td>
        <td>
            <textarea name="bodyHtml" cols="50" rows="10"><%=content.getBodyHtml()%></textarea>
        </td>
    </tr>
    <tr>
        <td class="inputLabel">Body Text</td>
        <td>
            <textarea name="bodyText" cols="50" rows="10"><%=content.getBodyText()%></textarea>
        </td>
    </tr>
    
    <tr>
        <td></td>
        <td><input type="submit" name="button_save" value="save"></td>
    </tr>
</table>
</form>

<%@ include file = "./include/bodyBottom.jsp"%>

