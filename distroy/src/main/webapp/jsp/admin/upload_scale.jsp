<%@ page import="com.approachingpi.store.servlet.admin.ImageUploadServlet,
                 com.approachingpi.servlet.PiServlet"%>
<jsp:useBean id="image" class="com.approachingpi.store.catalog.Image" scope="request"/>

<%@ include file = "./include/global.jsp"%>
<%
    strPage = "Upload Image";
    strPageTitle = "UploadImage";
    strOnLoad = "scaleImg()";
%>
<%@ include file = "./include/header.jsp"%>

<script language="javascript">
    function scaleImg() {
        location = "/admin/ImageUpload?action=<%=ImageUploadServlet.ACTION_SCALE%>&imageId=<%=image.getId()%>&productId=<%=PiServlet.getReqInt(request,"productId")%>";

    }
</script>

Upload successfull.
Scaling image...
Please wait...

<%
con.close();
%>
