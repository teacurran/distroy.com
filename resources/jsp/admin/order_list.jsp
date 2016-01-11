<%@ page import="com.approachingpi.store.servlet.admin.OrderServlet,
                 com.approachingpi.store.servlet.admin.UserServlet,
                 com.approachingpi.servlet.PiServlet,
                 com.approachingpi.store.Store,
                 com.approachingpi.store.order.*"%>
<jsp:useBean id="orders"    class="java.util.ArrayList" scope="request"/>
<jsp:useBean id="search"    class="com.approachingpi.store.order.OrderSearch" scope="request"/>

<%@ include file = "./include/global.jsp"%>
<%
    strPage = "order";
    strPageTitle = "Orders";
%>
<%@ include file = "./include/header.jsp"%>
<%@ include file = "./include/bodyTop.jsp"%>

<%
    ArrayList stores = Store.getAllStoresFromDb(con);
%>
<script language="javascript">
function sortResults(sort) {
    <%
    String dateEnd = (search.getDateEnd()==null) ? "" : sdf.format(search.getDateEnd());
    String dateStart = (search.getDateStart()==null) ? "" : sdf.format(search.getDateStart());
    %>
    var url = "/admin/Order?action=<%=OrderServlet.ACTION_LIST%>&dateStart=<%=dateStart%>&dateEnd=<%=dateEnd%>&status=<%=search.getStatus()%>";
    url += "&sort=" + sort;
    if (sort == <%=search.getSort()%>) {
        if (<%=search.getSortOrder()%> == <%=OrderSearch.SORT_ASC%>) {
            url += "&sortOrder=<%=OrderSearch.SORT_DESC%>";
        } else {
            url += "&sortOrder=<%=OrderSearch.SORT_ASC%>";
        }
    }
    location = url;
}
</script>

<form method="post" action="/admin/Order" name="mainForm">
<input type="hidden" name="action" value="<%=OrderServlet.ACTION_LIST%>">
<select name="storeId">
    <option value="">[store]</option>
    <% for (int i=0; i<stores.size(); i++) { 
        Store store = (Store)stores.get(i);
        String selectedVal = (store.getId()==search.getStore().getId()) ? " selected" : "";
        %>
        <option value="<%=store.getId()%>"<%=selectedVal%>><%=store.getName()%></option>
        <%
        }
    %>
</select>
<select name="status">
    <option value="">[status]</option>
    <% for (int i=Order.STATUS_MIN; i<=Order.STATUS_MAX; i++) { 
        String selectedVal = (i==search.getStatus()) ? " selected" : "";
        %>
        <option value="<%=i%>"<%=selectedVal%>><%=Order.getStatusById(i)%></option>
    <% } %>
</select>
<input type="text" name="dateStart" size="10" value="<%=dateStart%>">
<input type="text" name="dateEnd" size="10" value="<%=dateEnd%>">
<input type="submit" name="button_go" value="go">
</select>

<p>
<table>
    <tr>
        <td class="matrixHead"><a href="javascript:sortResults(<%=OrderSearch.SORT_ID%>)">Id</a></td>
        <td class="matrixHead"><a href="javascript:sortResults(<%=OrderSearch.SORT_NAME%>)">Name</a></td>
        <td class="matrixHead"><a href="javascript:sortResults(<%=OrderSearch.SORT_DATE%>)">Date</a></td>
        <td class="matrixHead"><a href="javascript:sortResults(<%=OrderSearch.SORT_AMOUNT%>)">Amount</a></td>
        <td class="matrixHead"><a href="javascript:sortResults(<%=OrderSearch.SORT_STATUS%>)">Status</a></td>
    </tr>
    <% 
    String cssClass = "matrixRow";
    for (int i=0; i<orders.size(); i++) { 
        Order order = (Order)orders.get(i);
        cssClass = (cssClass.equals("matrixRow")) ? "matrixRowAlternate" : "matrixRow";
        %>
        <tr>
            <td class="<%=cssClass%>"><a href="/admin/Order?action=<%=OrderServlet.ACTION_EDIT%>&orderId=<%=order.getId()%>"><%=order.getId()%></a></td>
            <td class="<%=cssClass%>"><a href="/admin/User?action=<%=UserServlet.ACTION_EDIT%>&userId=<%=order.getUser().getId()%>"><%=order.getBillAddress().getNameLast()%>, <%=order.getBillAddress().getNameFirst()%></a></td>
            <%-- <td class="<%=cssClass%>"><a href="/admin/User?action=<%=UserServlet.ACTION_EDIT%>&userId=<%=order.getUser().getId()%>"><%=order.getBillAddress().getNameLast()%>, <%=order.getBillAddress().getNameFirst()%></a></td> --%>
            <td class="<%=cssClass%>"><%=timeFormat.format(order.getDateCreated())%></td>
            <td class="<%=cssClass%>"><%=dollarFormat.format(order.getAmountTotal())%></td>
            <td class="<%=cssClass%>"><%=Order.getStatusById(order.getStatus())%></td>
        </tr>
        <%
    }
    %>
</table>
<%@ include file = "./include/bodyBottom.jsp"%>

