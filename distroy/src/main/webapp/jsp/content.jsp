<%@ page import="com.approachingpi.store.catalog.Image,
                 java.util.ArrayList,
                 com.approachingpi.store.site.Content"%>
<jsp:useBean id="content"   class="com.approachingpi.store.site.Content" scope="request"/>

<%@ include file = "./include/global.jsp"%>
<%
    strPage = "home";
    strPageTitle = "home";
    if (content.getUrl().indexOf("support") > -1){
        strActivateMenu = "2";
    } else if (content.getUrl().indexOf("links") > -1){        
            strActivateMenu = "99"; //no such menu
}
%>
<%@ include file = "./include/header.jsp"%>

<table width="100%">
    <tr>
        <td class="pageHead"><%=content.getTitle()%></td>
    </tr>
    <tr>
        <td class="contentText" height="300"><%=content.getBodyHtmlFormatted()%></td>
    </tr>
</table>

<%@ include file = "./include/footer.jsp"%>

