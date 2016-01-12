<jsp:useBean id="image" class="com.approachingpi.store.catalog.Image" scope="request"/>

<%@ include file = "./include/global.jsp"%>
<%
    strPage = "Upload Image";
    strPageTitle = "UploadImage";
    strOnLoad = "refreshImages()";
%>
<%@ include file = "./include/header.jsp"%>
<script language="javascript">
    function refreshImages() {
        opener.refreshImages();
    }
</script>

<center>
Upload is complete.
<br>
<img src="<%=strImgProductDir%>thumb/<%=image.getName()%>">
<br>
<a href="javascript:window.close()">Close Window</a>
</center>

<%
con.close();
%>


