<%@ page import="com.approachingpi.store.servlet.admin.ArtistServlet,
                 com.approachingpi.servlet.PiServlet,
                 com.approachingpi.store.catalog.Artist"%>

<jsp:useBean id="artists"        class="java.util.ArrayList" scope="request"/>

<%@ include file = "./include/global.jsp"%>
<%
    int categoryId = altAttribute.getInt("categoryId");
    strPage = "artist";
    strPageTitle = "Artist List";
%>
<%@ include file = "./include/header.jsp"%>
<%@ include file = "./include/bodyTop.jsp"%>

<a href="/admin/Artist?action=<%=ArtistServlet.ACTION_EDIT%>" title="Create a new Artist"><img src="<%=strImgAdminDir%>icons/icon_new_file.gif" height="20" width="20" border="0" vspace="10"></a>
<br>
<table>
    <tr>
        <td class="matrixHead">Id</td>
        <td class="matrixHead">Last</td>
        <td class="matrixHead">First</td>
        <td class="matrixHead">Display</td>
        <td class="matrixHead">Active</td>
    </tr>
    <%
    String cssClass = "matrixRow";
    for (int i=0; i<artists.size(); i++) {
        Artist artist = (Artist)artists.get(i);

        if (cssClass.equalsIgnoreCase("matrixRow")) {
            cssClass = "matrixRowAlternate";
        } else {
            cssClass = "matrixRow";
        }

        %>
        <tr>
            <td class="<%=cssClass%>"><a href="/admin/Artist?action=<%=ArtistServlet.ACTION_EDIT%>&artistId=<%=artist.getId()%>"><%=artist.getId()%></a></td>
            <td class="<%=cssClass%>"><a href="/admin/Artist?action=<%=ArtistServlet.ACTION_EDIT%>&artistId=<%=artist.getId()%>"><%=artist.getNameLast()%></a></td>
            <td class="<%=cssClass%>"><a href="/admin/Artist?action=<%=ArtistServlet.ACTION_EDIT%>&artistId=<%=artist.getId()%>"><%=artist.getNameFirst()%></a></td>
            <td class="<%=cssClass%>"><a href="/admin/Artist?action=<%=ArtistServlet.ACTION_EDIT%>&artistId=<%=artist.getId()%>"><%=artist.getNameDisplay()%></a></td>
            <td class="<%=cssClass%>"><%=artist.getActive()%></td>
        </tr>
        <%
    } 
    %>
</table>

<%@ include file = "./include/bodyBottom.jsp"%>



