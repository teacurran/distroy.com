<%@ page import="com.approachingpi.store.servlet.admin.RoyaltiesServlet,
                 com.approachingpi.servlet.PiServlet,
                 com.approachingpi.store.catalog.Royalties"%>

<jsp:useBean id="artists"        class="java.util.ArrayList" scope="request"/>

<%@ include file = "./include/global.jsp"%>
<%
    int categoryId = altAttribute.getInt("categoryId");
    strPage = "artist";
    strPageTitle = "Artist Royalties";
%>
<%@ include file = "./include/header.jsp"%>
<%@ include file = "./include/bodyTop.jsp"%>

<br>
<table>
    <tr>
        <td class="matrixHead">Id</td>
        <td class="matrixHead">Display</td>
        <td class="matrixHead">Check #</td>
        <td class="matrixHead">Check Amt</td>
        <td class="matrixHead">Check Date</td>
        <td class="matrixHead">Royalties Paid</td>
        <td class="matrixHead">Royalties Owed</td>
    </tr>
    <%
    String cssClass = "matrixRow";
    for (int i=0; i<artists.size(); i++) {
        Royalties artist = (Royalties)artists.get(i);

        if (cssClass.equalsIgnoreCase("matrixRow")) {
            cssClass = "matrixRowAlternate";
        } else {
            cssClass = "matrixRow";
        }
        %>
        <tr>
<td class="<%=cssClass%>"><a href="/admin/Royalties?action=<%=RoyaltiesServlet.ACTION_EDIT%>&artistId=<%=artist.getId()%>"><%=artist.getId()%></td>
<td class="<%=cssClass%>"><a href="/admin/Royalties?action=<%=RoyaltiesServlet.ACTION_EDIT%>&artistId=<%=artist.getId()%>"><%=artist.getNameDisplay()%></td>
<td class="<%=cssClass%>"><a href="/admin/Royalties?action=<%=RoyaltiesServlet.ACTION_EDIT%>&artistId=<%=artist.getId()%>"><%=artist.getCheckNumber()%></td>
<td class="<%=cssClass%>"><a href="/admin/Royalties?action=<%=RoyaltiesServlet.ACTION_EDIT%>&artistId=<%=artist.getId()%>"><%=dollarFormat.format(artist.getCheckAmount())%></td>
<td class="<%=cssClass%>"><a href="/admin/Royalties?action=<%=RoyaltiesServlet.ACTION_EDIT%>&artistId=<%=artist.getId()%>"><%=timeFormat.format(artist.getDateCheck())%></td>
<td class="<%=cssClass%>"><a href="/admin/Royalties?action=<%=RoyaltiesServlet.ACTION_EDIT%>&artistId=<%=artist.getId()%>"><%=dollarFormat.format(artist.getAmtPaid())%></td>
<td class="<%=cssClass%>"><a href="/admin/Royalties?action=<%=RoyaltiesServlet.ACTION_EDIT%>&artistId=<%=artist.getId()%>"><%=dollarFormat.format(artist.getAmtOwed())%></td> 
        </tr>
        <%
    } 
    %>
<tr>
</tr>
</table>

<%@ include file = "./include/bodyBottom.jsp"%>



