<jsp:useBean id="errorBean" class="com.approachingpi.util.MessageBean" scope="request"/>
<jsp:useBean id="messageBean" class="com.approachingpi.util.MessageBean" scope="request"/>
<%
errorBean.reset();
messageBean.reset();

if (messageBean.hasNext()) {
    %>
    <div class="messages">
        <% while (messageBean.hasNext()) { %>
            <%=messageBean.getNextMessage()%><br>
        <% } %>
    </div>
    <%
}
if (errorBean.hasNext() && !errorBean.getIsFatal()) {
    %>
    <div class="errors">
        <% while (errorBean.hasNext()) { %>
            <%=errorBean.getNextMessage()%><br>
        <% } %>
    </div>
    <%
}
%>
