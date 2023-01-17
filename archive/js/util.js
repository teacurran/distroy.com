var HTTP_UNINITIALIZED	= 0;
var HTTP_LOADING		= 1;
var HTTP_LOADED			= 2;
var HTTP_INTERACTIVE	= 3;
var HTTP_COMPLETE		= 4;

var IE = document.all?true:false;
if (!IE) document.captureEvents(Event.MOUSEMOVE)

var mouseX = 0;
var mouseY = 0;

function autoSelect(textbox) {
    if (textbox.createTextRange) {
        var oRange = textbox.createTextRange();
        oRange.moveStart("character", 0);
        oRange.moveEnd("character", textbox.value.length);
        oRange.select();
    } else if (textbox.setSelectionRange) {
        textbox.setSelectionRange(0, textbox.value.length);
    }
}

function closeMessageWindow() {
    var objMessageWindow = document.getElementById("message_Window");
    objMessageWindow.style.visibility = "hidden";
}

function dialogHide(dialogName) {
    var dialog = document.getElementById(dialogName);
    dragRelease(dialog);
    dialog.style.visibility = "hidden";
}


var dragDialogHeld = null;
var dragOffsetX = null;
var dragOffsetY = null;
function dragGrab(dlg) {
    dragDialogHeld = document.getElementById(dlg);

    if (dragDialogHeld) {
        if (IE) {
            dragOffsetX = mouseX - document.all[dlg].style.pixelLeft;
            dragOffsetY = mouseY - document.all[dlg].style.pixelTop;
        } else {
            dragOffsetX = mouseX - parseInt(dragDialogHeld.style.left);
            dragOffsetY = mouseY - parseInt(dragDialogHeld.style.top);
        }
    }

}

function dragMousemove(e) {
        if (IE) {
            var scrollXY = getScrollXY();
            mouseX = event.clientX + scrollXY[0];
            mouseY = event.clientY + scrollXY[1];
        } else {
            mouseX = e.pageX
            mouseY = e.pageY
        }
        if (mouseX < 0) {
            pageX = 0;
        }
        if (mouseY < 0) {
            mouseY = 0;
        }

    if (dragDialogHeld) {
        /*
        if (e.button == 0) {
            heldDialog = null;
            return;
        }
        */

        dragDialogHeld.style.left = (mouseX - dragOffsetX) + 'px';
        dragDialogHeld.style.top = (mouseY - dragOffsetY) + 'px';
    }
}

function dragRelease(dlg) {
    dragDialogHeld = null;
}

function findInArray(arrayIn, searchIn) {
    if (!arrayIn) {
        return -1;
    }
    for (var i=0; i< arrayIn.length; i++) {
        if (arrayIn[i] == searchIn) {
            return i;
        }
    }
    return -1;
}

function forceNumeric(field) {
    field.value=field.value.replace(/\D/g,'');
}

function getBrowserHeight() {
    if (IE) {
        return(document.body.clientHeight);
    } else {
        return(window.outerHeight);
    }
}

function getBrowserWidth() {
    if (IE)	{
        return(document.body.clientWidth);
    } else {
        return window.outerWidth;
    }
}

function getHttpRequest() {
    var http_request;
    if (window.XMLHttpRequest) { // Mozilla, Safari, ...
        http_request = new XMLHttpRequest();
    } else if (window.ActiveXObject) { // IE
        http_request = new ActiveXObject("Microsoft.XMLHTTP");
    }
    return http_request;
}

function getScrollXY() {
    var scrOfX = 0, scrOfY = 0;
    if( typeof( window.pageYOffset ) == 'number' ) {
        //Netscape compliant
        scrOfY = window.pageYOffset;
        scrOfX = window.pageXOffset;
    } else if( document.body && ( document.body.scrollLeft || document.body.scrollTop ) ) {
        //DOM compliant
        scrOfY = document.body.scrollTop;
        scrOfX = document.body.scrollLeft;
    } else if( document.documentElement && ( document.documentElement.scrollLeft || document.documentElement.scrollTop ) ) {
        //IE6 standards compliant mode
        scrOfY = document.documentElement.scrollTop;
        scrOfX = document.documentElement.scrollLeft;
    }
    return [ scrOfX, scrOfY ];
}

function highlightErrorField(fieldName) {
	var label = document.getElementById("l_" + fieldName);
	if (label) {
		label.className="inputLabelError";
	}
}

function isInArray(arrayIn, searchIn) {
    if (!arrayIn) {
        return false;
    }
    for (var i=0; i< arrayIn.length; i++) {
        if (arrayIn[i] == searchIn) {
            return true;
        }
    }
    return false;
}

function positionDialog(dlg) {
    // document.body.clientWidth is the width of the page, even if it is bigger than the window.
    // this is the best we can get from ie however.
    //var pageWidth = document.all ? document.body.clientWidth : window.innerWidth;
    // ie 5.2 on mac does not support window.dialogWidth or window.innerWidth
    //if (!pageWidth && document.body.clientWidth) {
    //    pageWidth = document.body.clientWidth;
    //}

    //page width is always 780 (actual content width)
    var pageWidth = 780;

    // center the message window
    dlg.style.left = parseInt((pageWidth / 2) - (dlg.offsetWidth / 2)) + "px";

    if (IE) {
        var scrollXY = getScrollXY();
        dlg.style.top = (150 + scrollXY[1]) + "px";
    } else {
        dlg.style.top = "150px";
    }
}


function positionMessageWindow() {
    // document.body.clientWidth is the width of the page, even if it is bigger than the window.
    // this is the best we can get from ie however.
    var pageWidth = document.all ? document.body.clientWidth : window.innerWidth;
    // ie 5.2 on mac does not support window.dialogWidth or window.innerWidth
    if (!pageWidth && document.body.clientWidth) {
        pageWidth = document.body.clientWidth;
    }
    var objMessageWindow = document.getElementById("message_Window");
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

    objMessageWindow.style.left = parseInt((pageWidth / 2) - (objMessageWindow.offsetWidth / 2)) + "px";
}

function randomString(stringLength) {
    var chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXTZabcdefghiklmnopqrstuvwxyz";
    var randomstring = '';
    for (var i=0; i<stringLength; i++) {
        var rnum = Math.floor(Math.random() * chars.length);
        randomstring += chars.substring(rnum,rnum+1);
    }
    return randomstring;
}

function removeFromArray(arrayIn, index) {
    var returnArray = new Array();
    if (!arrayIn) {
        return returnArray;
    }
    for (var i=0; i<arrayIn; i++) {
        if (i != index) {
            returnArray[returnArray.length] = arrayIn[i];
        }
    }
    return returnArray;
}

function removeMatchesFromArray(arrayIn, searchIn) {
    var returnArray = new Array();
    if (!arrayIn) {
        return returnArray;
    }
    for (var i=0; i< arrayIn.length; i++) {
        if (arrayIn[i] != searchIn) {
            returnArray[returnArray.length] = arrayIn[i];
        }
    }
    return returnArray;
}

function showMessageWindow() {
    positionMessageWindow();
    var objMessageWindow = document.getElementById("message_Window");
    objMessageWindow.style.visibility = "visible";
}

function trim(str) {
    return str.replace(/^\s*|\s*$/g,"");
}

