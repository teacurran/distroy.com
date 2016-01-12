<%@ page import="com.approachingpi.store.servlet.admin.ArtistServlet,
                 com.approachingpi.servlet.PiServlet,
                 com.approachingpi.store.catalog.Artist,
                 java.util.ArrayList"%>

<jsp:useBean id="artist"       class="com.approachingpi.store.catalog.Artist" scope="request"/>

<%@ include file = "./include/global.jsp"%>
<%
    strPage = "artist";
    strPageTitle = "Artist Edit";
%>
<%@ include file = "./include/header.jsp"%>
<%@ include file = "./include/bodyTop.jsp"%>

<a href="/admin/Artist?action=<%=ArtistServlet.ACTION_LIST%>">back to main</a><br>

<form method="post" action="/admin/Artist" name="mainForm">
<input type="hidden" name="action" value="<%=ArtistServlet.ACTION_EDIT%>">
<input type="hidden" name="artistId" value="<%=artist.getId()%>">
<input type="hidden" name="formSubmitted" value="true">

<table>
    <tr>
        <td class="inputLabel">Id</td>
        <td><% if (artist.getId() > 0) {
                %><%=artist.getId()%><%
            } else {
                %>new<%
            }
            %>
        </td>
    </tr>
    <tr>
        <td class="inputLabel">Created</td>
        <td><%if(artist.getDateCreated() != null) { out.write(timeFormat.format(artist.getDateCreated())); }%></td>
    </tr>
    <tr>
        <td class="inputLabel">Modified</td>
        <td><%if(artist.getDateModified() != null) { out.write(timeFormat.format(artist.getDateModified())); }%></td>
    </tr>
    <tr>
        <td class="inputLabel">First Name</td>
        <td><input type="text" name="nameFirst" size="50" maxlength="50" value="<%=artist.getNameFirst()%>"></td>
    </tr>
    <tr>
        <td class="inputLabel">Last Name</td>
        <td><input type="text" name="nameLast" size="50" maxlength="50" value="<%=artist.getNameLast()%>"></td>
    </tr>
    <tr>
        <td class="inputLabel">Display Name</td>
        <td><input type="text" name="nameDisplay" size="50" maxlength="50" value="<%=artist.getNameDisplay()%>"></td>
    </tr>

    <tr>
        <td class="inputLabel">Active</td>
        <td class="label"><input type="checkbox" name="active" value="true"<% if (artist.getActive()) { out.print(" checked"); } %>></td>
    </tr>

    <tr>
        <td class="inputLabel">Royalty Dollar Retail</td>
        <td><input type="text" name="royaltyDollarRetail" size="5" maxlength="10" value="<%=ddf.format(artist.getRoyaltyDollarRetail())%>"></td>
    </tr>

    <tr>
        <td class="inputLabel">Royalty Dollar Wholesale</td>
        <td><input type="text" name="royaltyDollarWholesale" size="5" maxlength="10" value="<%=ddf.format(artist.getRoyaltyDollarWholesale())%>"></td>
    </tr>

    <tr>
        <td class="inputLabel">Royalty Percent Retail</td>
        <td><input type="text" name="royaltyPercentRetail" size="5" maxlength="10" value="<%=artist.getRoyaltyPercentRetail()%>"></td>
    </tr>

    <tr>
        <td class="inputLabel">Royalty Percent Wholesale</td>
        <td><input type="text" name="royaltyPercentWholesale" size="5" maxlength="10" value="<%=artist.getRoyaltyPercentWholesale()%>"></td>
    </tr>
   
    <tr>
        <td class="inputLabel">Description</td>
        <td>
            <textarea name="desc" cols="50" rows="10"><%=artist.getDesc()%></textarea>
        </td>
    </tr>
    
    <tr>
        <td></td>
        <td><input type="submit" name="button_save" value="save"></td>
    </tr>
</table>
</form>

<%@ include file = "./include/bodyBottom.jsp"%>

