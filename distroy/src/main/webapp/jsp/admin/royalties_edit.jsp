<%@ page import="com.approachingpi.store.servlet.admin.RoyaltiesServlet,
                 com.approachingpi.servlet.PiServlet,
                 com.approachingpi.store.catalog.Royalties,
                 java.util.ArrayList"%>

<jsp:useBean id="artist"       class="com.approachingpi.store.catalog.Royalties" scope="request"/>

<%@ include file = "./include/global.jsp"%>
<%
    strPage = "artist";
    strPageTitle = "Artist Royalties Edit";
%>
<%@ include file = "./include/header.jsp"%>
<%@ include file = "./include/bodyTop.jsp"%>

<a href="/admin/Royalties?action=<%=RoyaltiesServlet.ACTION_LIST%>">back to main</a><br>
<p>
<form method="post" action="/admin/Royalties" name="mainForm">
<input type="hidden" name="action" value="<%=RoyaltiesServlet.ACTION_EDIT%>">
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
        <td class="inputLabel">Display Name</td>
        <td><%=artist.getNameDisplay()%></td>
    </tr>
    <tr>
        <td class="inputLabel">Check Date</td>
	<td><input type="date" name="dateCheck" size="25" maxlength="25" > (mm/dd/yyyy)</td>
    </tr>
    <tr>
        <td class="inputLabel">Check Number</td>
        <td><input type="text" name="CheckNumber" size="10" maxlength="10"> </td>
    </tr>
    <tr>
        <td class="inputLabel">Check Amount</td>
	<td><input type="text" name="CheckAmount" size="10" maxlength="10" ></td>
    </tr>
    <tr>
        <td class="inputLabel">Audit</td>
        <td><a href="/admin/Royalties?action=<%=RoyaltiesServlet.ACTION_AUDIT%>&artistId=<%=artist.getId()%>" >Order Overview</td>
    </tr>

    
    <tr>
        <td></td>
        <td><input type="submit" name="button_save" value="save"></td>
    </tr>
<tr>
	<td class="pageHead">History</td>
</tr>
<tr>
        <td class="matrixHead">Check Date</td>
        <td class="matrixHead">Check #</td>
        <td class="matrixHead">Check Amt</td>
</tr>
<%
String cssClass = "matrixRow";
ArrayList history = artist.getHistory(con,artist.getId());
for (int i=0; i<history.size() && i<=10; i++) {
	Royalties hist = (Royalties)history.get(i);

if (cssClass.equalsIgnoreCase("matrixRow")) {
            cssClass = "matrixRowAlternate";
        } else {
            cssClass = "matrixRow";
        }
%>

    <tr>
	<td class="<%=cssClass%>"><%if(hist.getDateCheck() != null) { out.write(timeFormat.format(hist.getDateCheck())); }%></td>
        <td class="<%=cssClass%>"><%=hist.getCheckNumber()%></td>
	<td class="<%=cssClass%>"><%if(hist.getCheckAmount() != null) { out.write(dollarFormat.format(hist.getCheckAmount())); }%></td>
    </tr>
<%}%>

</table>
</form>

<%@ include file = "./include/bodyBottom.jsp"%>

