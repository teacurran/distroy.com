<%@ page contentType="application/xml"%><?xml version="1.0" ?>
<%@ page import="org.apache.commons.lang.StringEscapeUtils,
                 java.util.ArrayList"%>
<jsp:useBean id="product" class="com.approachingpi.store.catalog.Product" scope="request"/>
<%@ include file = "./include/global.jsp"%>
<update success="<%=altAttribute.getBoolean("update_success")%>">
    <product id="<%=product.getId()%>">
    </product>
</update>
