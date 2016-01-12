<%
    if (strPagePath.length() == 0) {
        strPagePath = strPageTitle;
    }
%>

<center>
<img src="/img/admin/top.gif">
</center>

<table width="100%" border="0">
<tr>
    <td width="150" valign="top"><%@ include file = "navigation.jsp"%></td>
    <td style="padding-left:20px; padding-top:0px;">
        <span class="pageHead"><%=strPagePath%></span>
        <br>
    
    