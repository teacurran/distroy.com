<%@ page import="com.approachingpi.store.servlet.admin.ImageUploadServlet,
                 com.approachingpi.servlet.PiServlet"%>

<%@ include file = "./include/global.jsp"%>
<%
    strPage = "Upload Image";
    strPageTitle = "UploadImage";
%>
<%@ include file = "./include/header.jsp"%>


<form method="POST" enctype="multipart/form-data" action="/admin/ImageUpload?action=<%=ImageUploadServlet.ACTION_UPLOAD%>&productId=<%=PiServlet.getReqInt(request,"productId")%>">

File to upload:
<table>
    <tr>
        <td class="inputField">Enlarged</td>
        <td><input type="file" name="uploadFileEnlarge"><td>
    </tr>
    <tr>
        <td class="inputField">Standard</td>
        <td>
            <input type="file" name="uploadFileStandard">
        <td>
    </tr>
    <tr>
        <td class="inputField">Thumb</td>
        <td><input type="file" name="uploadFileThumb"><td>
    </tr>
    <tr>
        <td class="inputField">Description</td>
        <td><input type="text" name="desc" value="<%=PiServlet.getReqString(request,"desc")%>"></td>
    </tr>
    <tr>
        <td></td>
        <td><input type="submit" name="button_submit" value="upload">
    </tr>
</table>
</form>

<%
con.close();
%>


