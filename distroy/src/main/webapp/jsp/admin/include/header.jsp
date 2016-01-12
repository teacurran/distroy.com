<%@ page import="java.util.Enumeration,
                 java.sql.Connection,
                 javax.sql.DataSource,
                 javax.naming.Context,
                 javax.naming.InitialContext,
                 java.util.ArrayList
"%>
<%

    /*
    Enumeration headers = request.getHeaderNames();
    String headerName;
    while (headers.hasMoreElements()) {
        headerName = (String)headers.nextElement();
        out.println(headerName + "<br>");
    }
    String header = request.getHeader("user-agent");
    out.println("<b>" + header + "</b>");
    */

    InitialContext initialContext = new InitialContext();
	Context envCtx=(Context)initialContext.lookup("java:comp/env");
    DataSource dataSource = (DataSource)envCtx.lookup("jdbc//MainDs");
    Connection con = dataSource.getConnection();

%>
<%-- <!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/1999/REC-html401-19991224/loose.dtd"> --%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<html>
<head>
<title>Approaching Pi :: <%=strPageTitle%></title>

<link rel="stylesheet" type="text/css" href="/css/admin.css">

<script type="text/javascript" src="/js/prototype.js"></script>
<script type='text/javascript' src="/js/prototip.js"></script>
<script type="text/javascript" src="/js/effects.js"></script>
<script type="text/javascript" src="/js/accordion.js"></script>
<script type="text/javascript" src="/js/scriptaculous.js?load=effects,builder"></script>
<script type="text/javascript" src="/js/lightbox.js"></script>
<script type="text/javascript" src="/js/util.js"></script>
<script type="text/javascript" src="/js/tabcontent.js"></script>
<script type="text/javascript" src="/js/tiny_mce/tiny_mce.js"></script>
    
</head>

<body bgcolor="#FFFFFF" leftmargin="<%=leftMargin%>" topmargin="<%=topMargin%>" marginwidth="<%=marginWidth%>" marginheight="<%=marginHeight%>"<% if (strOnLoad.length() > 0) { out.print(" onLoad=\"" + strOnLoad + "\""); } %>>

<script language="javascript">
var MOVE_DOWN           = 0;
var MOVE_UP             = 1;
var MOVE_NONE           = 2;
var boMessageWindowOpen = false;
var intMoveDestination  = 0;
var intMoveDirection    = MOVE_DOWN;
var intMoveInterval     = 10;
var intMoveSpeed        = 10;
var intervalId;
var objMessageWindow;
var boDeleteInQueue     = false;
var strDeleteName       = "";
var strDeleteUrl        = "";

function cancelDelete() {
    strDeleteName = "";
    strDeleteUrl = "";
    dismissMessageWindow();
}
function showDeleteBox(name, url) {
    strDeleteName  = name;
    strDeleteUrl   = url;

    if (boMessageWindowOpen) {
        boDeleteInQueue = true;
    } else {
        var buttonTd = document.getElementById("message_buttonTd");
        if (buttonTd) {
            buttonTd.innerHTML = "<a href=\"javascript:cancelDelete()\"><img src=\"<%=strImgAdminDir%>button_cancel_70.png\" border=\"0\">&nbsp;";
            buttonTd.innerHTML = buttonTd.innerHTML + "<a href=\"" + url + "\"><img src=\"<%=strImgAdminDir%>button_ok_70.png\" border=\"0\">";
        }

        var messageTd = document.getElementById("message_textTd");
        if (messageTd) {
            messageTd.innerHTML = "<BR>Are you sure you wish to delete this " + strDeleteName + "?";
        }


        initMessageWindow();
    }
}

function disableForm() {
    if(document.mainForm) {
        document.mainForm.disabled = true;
        var oNodes = document.mainForm.elements;
        for (x = 0; x < oNodes.length; x ++) {
            oNodes[x].disabled = true;
        }
        // make all the rows white
        oNodes = document.getElementsByTagName("input");
        for (x = 0; x < oNodes.length; x ++) {
            thisId = oNodes[x].name;
            if (thisId.indexOf("button_") == 0) {
                oNodes[x].disabled = true;
                oNodes[x].src = oNodes[x].src.replace(".gif","_disabled.gif");
            }
        }
    }
}

function dismissMessageWindow() {
    intMoveDestination = 0 - objMessageWindow.offsetHeight;
    intMoveDirection = MOVE_UP;

    boMessageWindowOpen = false;
    enableForm();

    // move the dialog back up.
    intervalId = setInterval("moveMessageWindow()",intMoveSpeed)
}

function enableForm() {
    if(document.mainForm) {
        document.mainForm.disabled = false;
        var oNodes = document.mainForm.elements;
        for (x = 0; x < oNodes.length; x ++) {
            oNodes[x].disabled = false;
        }
        // make all the rows white
        oNodes = document.getElementsByTagName("input");
        for (x = 0; x < oNodes.length; x ++) {
            thisId = oNodes[x].name;
            if (thisId.indexOf("button_") == 0) {
                oNodes[x].disabled = false;
                oNodes[x].src = oNodes[x].src.replace("_disabled.gif",".gif");
            }
        }
    }
}

function initMessageWindow() {
    // document.body.clientWidth is the width of the page, even if it is bigger than the window.
    // this is the best we can get from ie however.
    var pageWidth = document.all ? document.body.clientWidth : window.innerWidth;
    // ie 5.2 on mac does not support window.dialogWidth or window.innerWidth
    if (!pageWidth && document.body.clientWidth) {
        pageWidth = document.body.clientWidth;
    }
    objMessageWindow = document.getElementById("message_Window");
    var messageTable = document.getElementById("message_Table");

    if (messageTable.width > pageWidth) {
        var messageTd = document.getElementById("message_textTd");
        messageTd.width = pageWidth - 10;
        var button = document.getElementById("buttonTd");
        button.width = messageTd.width - 14 - 14;
        //var messageTable = document.getElementById("message_Table");
        messageTable.width = pageWidth - 10;

        objMessageWindow.style.width = pageWidth - 10 + "px";
    }

    objMessageWindow.style.top = 0 - objMessageWindow.offsetHeight + "px";
    objMessageWindow.style.visibility = "visible";

    boMessageWindowOpen = true;

    // center the message window
    objMessageWindow.style.left = parseInt((pageWidth / 2) - (objMessageWindow.offsetWidth / 2)) + "px";
    //alert(document.documentElement.offsetWidth + "-" + objMessageWindow.offsetWidth + "-" + parseInt((document.documentElement.offsetWidth / 2) - (objMessageWindow.offsetWidth / 2)));

    disableForm();

    intMoveDestination = 0;
    intMoveDirection = MOVE_DOWN;

    intervalId = setInterval("moveMessageWindow()",intMoveSpeed)
}

function moveMessageWindow() {
    var intNewY;
    if (intMoveDirection == MOVE_DOWN) {
        intNewY = parseInt(objMessageWindow.style.top) + intMoveInterval;
        // if we overshot where we need to move to by a little, correct it.
        if (intNewY > intMoveDestination && intNewY < intMoveDestination + intMoveInterval) {
            intNewY = intMoveDestination;
        }
        if (intNewY <= intMoveDestination) {
            objMessageWindow.style.top = intNewY + "px";
        }
    } else if (intMoveDirection == MOVE_UP) {
        intNewY = parseInt(objMessageWindow.style.top) - intMoveInterval;
        // if we overshot where we need to move to by a little, correct it.
        if (intNewY < intMoveDestination && intNewY > intMoveDestination - intMoveInterval) {
            intNewY = intMoveDestination;
        }
        if (intNewY >= intMoveDestination) {
            objMessageWindow.style.top = intNewY + "px";
        }
    } else {
        // it is an error if we get here, kill the timer
        clearInterval(intervalId);
    }

    if (intNewY == intMoveDestination) {
        // stop the timer
        clearInterval(intervalId);
        if (intMoveDirection == MOVE_DOWN) {
            setWindowOpen(true);
        } else {
            setWindowOpen(false);
        }
    }
}

function setWindowOpen(status) {
    boMessageWindowOpen = status;
    if (boMessageWindowOpen == false) {
        if (boDeleteInQueue) {
            showDeleteBox(strDeleteName, strDeleteUrl);
        }
    }
}

</script>

<div id="message_Window" STYLE="position:absolute; width:300; height:600; z-index:20; left:25; top:0; visibility:hidden;">
<table cellpadding="0" width="300" border="0" cellspacing="0" id="message_Table">
    <tr>
        <td width="300" class="messageCell" background="<%=strImgAdminDir%>grey_90.png" colspan="3" id="message_textTd">
        </td>
    </tr>
    <tr>
        <td width="14" height="40" background="<%=strImgAdminDir%>grey_90.png"></td>
        <td width="272" height="54" background="<%=strImgAdminDir%>grey_90.png" rowspan="2" align="center" valign="middle" id="message_buttonTd"><a href="javascript:dismissMessageWindow()"><img src="<%=strImgAdminDir%>button_ok_70.png" border="0"></a></td>
        <td width="14" height="40" background="<%=strImgAdminDir%>grey_90.png"></td>
    </tr>
    <tr>
        <td valign="top" align="right" width="14" height="14" style="padding:0px; padding-left:0px; padding-right:0px"><img src="<%=strImgAdminDir%>message_corner_sw_90.png"></td>
        <td valign="top" align="left" width="14" height="14" style="padding:0px; padding-left:0px; padding-right:0px"><img src="<%=strImgAdminDir%>message_corner_se_90.png" style="display: block;"></td>
    </tr>
</table>
</div>

<%@ include file = "messages.jsp"%>

<div id="tooltip" style="position:absolute;visibility:hidden"></div>













