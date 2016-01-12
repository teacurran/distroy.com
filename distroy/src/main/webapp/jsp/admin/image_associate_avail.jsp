<%@ page import="com.approachingpi.store.catalog.Image"%>
<jsp:useBean id="images"      class="java.util.ArrayList" scope="request"/>

<%@ include file = "./include/global.jsp"%>


<html>
<head>
<title>Approaching Pi :: <%=strPageTitle%></title>
<link rel="stylesheet" type="text/css" href="/css/admin.css">
</head>

<body bgcolor="#FFFFFF" leftmargin="<%=leftMargin%>" topmargin="<%=topMargin%>" marginwidth="<%=marginWidth%>" marginheight="<%=marginHeight%>"<% if (strOnLoad.length() > 0) { out.print(" onLoad=\"" + strOnLoad + "\""); } %>>

<script language="javascript">

    function selectImage(imageId) {
        opener.addNewImage(imageId);
    }
</script>
<table>
<%
int matrixCols = 3;
int cellCount = 0;
for (int i=0; i<images.size(); i++) {
    Image image = (Image)images.get(i);
    cellCount++;

    if (cellCount == 1) {
        out.write("<tr>");
    }
    %><td valign="top" align="center"><img id="image_<%=image.getId()%>" alt="image_<%=image.getId()%>" src="<%=strImgProductDir%>thumb/<%=image.getName()%>" class="imageNotSelected" onClick="selectImage(<%=image.getId()%>)"></td><%
    if (cellCount == matrixCols) {
        out.write("</tr>");
        cellCount = 0;
    }
}
if (cellCount > 0) {
    out.write("</tr>");
}
%>
</table>
</body>
</html>

