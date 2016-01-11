<%@ page import="com.approachingpi.store.servlet.admin.ContentServlet,
                 com.approachingpi.servlet.PiServlet,
                 com.approachingpi.store.site.Content,
                 com.approachingpi.user.User"%>

<jsp:useBean id="allContent"        class="java.util.ArrayList" scope="request"/>

<%@ include file = "./include/global.jsp"%>
<%
    int categoryId = altAttribute.getInt("categoryId");
    strPage = "content";
    strPageTitle = "Content List";
%>
<%@ include file = "./include/header.jsp"%>
<%@ include file = "./include/bodyTop.jsp"%>

<a href="/admin/Content?action=<%=ContentServlet.ACTION_EDIT%>" title="Create a new Content Page"><img src="<%=strImgAdminDir%>icons/icon_new_file.gif" height="20" width="20" border="0" vspace="10"></a>
<br>

<table>
    <tr>
        <td class="matrixHead">Id</td>
        <td class="matrixHead">Url</td>
        <td class="matrixHead">Access Level</td>
        <td class="matrixHead">Title</td>
    </tr>
    <%
    String cssClass = "matrixRow";
    for (int i=0; i<allContent.size(); i++) {
        Content content = (Content)allContent.get(i);

        if (cssClass.equalsIgnoreCase("matrixRow")) {
            cssClass = "matrixRowAlternate";
        } else {
            cssClass = "matrixRow";
        }

        %>
        <tr>
            <td class="<%=cssClass%>"><a href="/admin/Content?action=<%=ContentServlet.ACTION_EDIT%>&contentId=<%=content.getId()%>"><%=content.getId()%></a></td>
            <td class="<%=cssClass%>"><a href="/admin/Content?action=<%=ContentServlet.ACTION_EDIT%>&contentId=<%=content.getId()%>"><%=content.getUrl()%></a></td>
            <td class="<%=cssClass%>"><%=User.getTypeName(content.getAccessRequired())%></td>
            <td class="<%=cssClass%>"><a href="/admin/Content?action=<%=ContentServlet.ACTION_EDIT%>&contentId=<%=content.getId()%>"><%=content.getTitle()%></a></td>
        </tr>
        <%
    } 
    %>
</table>
    
<%@ include file = "./include/bodyBottom.jsp"%>
