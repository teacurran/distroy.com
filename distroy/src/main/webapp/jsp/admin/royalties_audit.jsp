<%@ page import="com.approachingpi.store.servlet.admin.RoyaltiesServlet,
                 com.approachingpi.servlet.PiServlet,
                 com.approachingpi.store.catalog.Royalties,
                 java.util.ArrayList"%>

<jsp:useBean id="artist"       class="com.approachingpi.store.catalog.Royalties" scope="request"/>

<%@ include file = "./include/global.jsp"%>

<%
    strPage = "artist";
    strPageTitle = "Audit Artist Royalties";
%>
<%@ include file = "./include/header.jsp"%>
<%@ include file = "./include/bodyTop.jsp"%>

<a href="/admin/Royalties?action=<%=RoyaltiesServlet.ACTION_LIST%>">back to main</a><br>
<p>
<form method="post" action="/admin/Royalties" name="mainForm">
<input type="hidden" name="action" value="<%=RoyaltiesServlet.ACTION_AUDIT%>">
<input type="hidden" name="artistId" value="<%=artist.getId()%>">

<table>
    <tr>
        <td class="inputLabel">Display Name</td>
        <td><%=artist.getNameDisplay()%></td>
    </tr>
    <tr>
        <td class="inputLabel">Audit</td>
        <td><a href="/admin/Royalties?action=<%=RoyaltiesServlet.ACTION_AUDIT%>&artistId=<%=artist.getId()%>" >Order Overview</td>
    </tr>

<tr>
	<td class="pageHead">Order Overview</td>
</tr>
<tr>
        <td class="matrixHead">Order Id</td>
        <td class="matrixHead">Quantity</td>
        <td class="matrixHead">Item Desc.</td>
        <td class="matrixHead">Shipped Date</td>
</tr>
<%
String cssClass = "matrixRow";
ArrayList audit = artist.loadAudit(con,artist.getId());
for (int i=0; i<audit.size(); i++) {
	Royalties aud = (Royalties)audit.get(i);

if (cssClass.equalsIgnoreCase("matrixRow")) {
            cssClass = "matrixRowAlternate";
        } else {
            cssClass = "matrixRow";
        }
%>

    <tr>
        <td class="<%=cssClass%>"><%=aud.getOrderId()%></td>
        <td class="<%=cssClass%>"><%=aud.getQuantity()%></td>
        <td class="<%=cssClass%>"><%=aud.getItemDesc()%></td>
        <td class="<%=cssClass%>"><%=timeFormat.format(aud.getDateShipped())%></td>
    </tr>
<%}%>

</table>
</form>

<%@ include file = "./include/bodyBottom.jsp"%>

