<%@ page contentType="application/xml"%><?xml version="1.0" ?>
<%@ page import="org.apache.commons.lang.StringEscapeUtils,
                 java.util.ArrayList"%>
<jsp:useBean id="variation" class="com.approachingpi.store.catalog.ProductVariation" scope="request"/>
<%@ include file = "./include/global.jsp"%>
<update success="<%=altAttribute.getBoolean("update_success")%>">
    <product id="<%=variation.getProduct().getId()%>">
        <variation id="<%=variation.getId()%>" sale="<%=variation.isSale()%>"/>
    </product>
</update>
