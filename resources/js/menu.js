
// do some very basic browser detection
var agt=navigator.userAgent.toLowerCase();
var ie = 0;
var ns = 0;
var ns6 = 0;
var win = 0;
var mac = 0;
if (agt.indexOf('msie') != -1) {
	ie = 1;
} else if (agt.indexOf('gecko') != -1) {
	ns6 = 1;
} else if (agt.indexOf('netscape6') != -1) {
	ns6 = 1;
} else if (document.layers) {
	ns = 1;
}
win=((agt.indexOf("win")!=-1) || (agt.indexOf("16bit")!=-1))
mac=(agt.indexOf("mac")!=-1);
// end browser detection

// CONSTANTS
var NAV_TOP_INITIAL_LEFT 	= 556;		// Position of the first hidden menu layer for the top menu
var NAV_TOP_ROOT_LEFT 		= 689;		// The left position of the root menu (menu that is always static)
var NAV_BOTTOM_INITIAL_LEFT	= 91;		// Position of the first menu layer for the bottom menu
//var NAV_OFF_TIMEOUT			= 2;		// number of ticks allowed for the mouse to be off a nav before all navs are closed, increase to add more delay
var NAV_OFF_TIMEOUT			= 5;		// number of ticks allowed for the mouse to be off a nav before all navs are closed, increase to add more delay


// VARIABLES
var mouseOverNav 	= false;			// true / false indication if the mouse is over a nav element
var mouseOffTick 	= NAV_OFF_TIMEOUT;	// counter for the over nav checker

// this array keeps track of what nav is open at any given nav level
var levelActive 	= new Array();
levelActive[0] = null;
levelActive[1] = null;
levelActive[2] = null;

// prototype the node object
function Node(name,url) {
	this.name = name;
	this.url = url;
	this.children = new Array();
	this.parent = null;
	this.top = 0;
	this.height = 20;
	this.width = 135;
}
Node.prototype.addChild = function(child) {
	child.parent = this;
	this.children[this.children.length] = child;
}
Node.prototype.childCount = function() {
	return this.children.length;
}
Node.prototype.getChildren = function() {
	return this.children;
}
Node.prototype.getChild = function(index) {
	return this.children[index];
}
Node.prototype.getHeight = function() {
	// calculate the height if there is a line return in the name.
	// this does not detect multiple line returns.  we can maybe add that later
	if (this.name.indexOf("<br>") >= 0 && this.height == 20) {
		this.height = 30;
	}
	return this.height;
}
Node.prototype.lastChild = function() {
	return this.children[this.children.length - 1];
}

// This function runs to see if the mouse is over a nav element, if it is not when the mouseOffTick runs out,
// it closes all open nav elements.
function checkOpenNav() {
	if (mouseOverNav == false && mouseOffTick < NAV_OFF_TIMEOUT) {
		mouseOffTick++;
		//alert(mouseOffTick);

		if (mouseOffTick == NAV_OFF_TIMEOUT) {
			navClose(0);
			mouseOffTick++;
		}
	}
}

// Writes the DHTML for the menus
function drawMenu(menu,level,parent) {
	var arrowImage;
	var thisNode;
	var thisNodeId;
	var children = menu.getChildren();
	var menuStack = new Array();

	width = 135;

	left = NAV_TOP_INITIAL_LEFT - ((level-1)*width);

	menuId = parent;
	menuName = "menu_" + parent;

	height = 0;

	if (menu.width > 0) {
		width = menu.width;
	}

	/*
	if (children[0].parent.width > 0) {
		width = children[0].parent.width;
	}
	*/
	for (var i = 0; i < children.length; i++) {
		height = height + children[i].getHeight();
	}

	// we are not rendering the root
	if (level > 0) {
		document.write('<div id="' + menuName + '" class="menuBorder" style="z-index:5; visibility:hidden; position:absolute; left:' + left + 'px; top:' + menu.top + 'px; width:' + width + 'px; height:' + height + 'px">\n');
	}
	for (var i = 0; i < children.length; i++) {
		thisNodeId = parent + "_" + i;
		thisNode = children[i];
		thisNodeName = "nav_" + thisNodeId;

		arrowImage = '';
		/*
		if (thisNode.childCount() > 0) {
			if (isBottom || isHome) {
				arrowImage = '<img src="/images/nav/arrow_right.gif">';
			} else {
				arrowImage = '<img src="/images/nav/arrow_left.gif">';
			}
		}
		*/
        if (thisNode.height == -1) {
            heightText = "";
        } else {
            heightText = "height:" + thisNode.height + "px;";
        }
		if (level > 0) {
			document.write('\t<div id="' + thisNodeName + '" class="menuDrop" style="' + heightText + '" onMouseOver="navOver(\'' + thisNodeId + '\',' + level + ')" onMouseOut="navOff()" onClick="location=\'' + thisNode.url + '\'"><a href="' + thisNode.url + '"><span class="menuText" id="' + thisNodeName + '_link">' + thisNode.name + '</span></a></div>\n');
		}

		if (thisNode.childCount() > 0) {
			var nextItem = menuStack.length;
			menuStack[nextItem] = new Array();
			menuStack[nextItem][0] = thisNode;
			menuStack[nextItem][1] = thisNodeId;
		}
	}
	if (level >= 0) {
		document.write('</div>\n');
	}

	for (var i = 0; i < menuStack.length; i++) {
		drawMenu(menuStack[i][0],level+1,menuStack[i][1]);
	}
}

// Closes the currently open nav at the given level
function navClose(level) {
	if (level < 3) {
		navClose(level + 1);
	}
	if (levelActive[level] != null) {
		var nodeId = levelActive[level];
		var nodeIdHack = (nodeId.indexOf("b2") >= 0) ? nodeId.replace("b2","b") : nodeId;

		var node = document.getElementById("nav_" + nodeId);
		if (node != null && level > 0) {
			node.className = "menuDrop";
		}
        var nodeLink = document.getElementById("nav_" + nodeId + "_link");
        if (nodeLink) {
            nodeLink.className = "menuText";
        }
		var subNav = document.getElementById("menu_" + nodeIdHack);
		if (subNav != null) {
			subNav.style.visibility = "hidden";
		}

	}
}

// sets flag saying that the mouse is exiting a nav element,
// the nav will close if mouseOverNav stays false for the
// set timeout period
function navOff() {
	mouseOverNav = false;
	mouseOffTick = 0;
}

// just like navOff except it changes the images for the nav root
function navOffRoot(nodeId) {
	document['root_' + nodeId].src = eval("imgRoot_" + nodeId + "_off.src");
	navOff();
}

// Hilights the nav the mouse is currently over and opens a subnav if there is one
function navOver(nodeId,level) {

	if (level > -1) {
		if (levelActive[level] != null && levelActive[level] != nodeId) {
			navClose(level);
		}
	}

	if (nodeId == null) {
		return;
	}

	var activeNode = document.getElementById("nav_" + nodeId);
	var activeNodeLink = document.getElementById("nav_" + nodeId + "_link");
	var activeSubNav = document.getElementById("menu_" + nodeId);
				//alert(activeNode.offsetTop + "-" +  activeSubNav.style.height + "-" + activeNode.style.height + "-" + activeNode.style.width);

	if (activeNode != null && level > 0) {
		if (activeSubNav != null) {
			activeNode.className = "menuDropOverWithChildren"
		} else {
			activeNode.className = "menuDropOver";
		}
	}

    if (activeNodeLink) { 
        activeNodeLink.className = "menuTextOver";
    }

	//alert(activeNode + "-" + activeSubNav);
	//alert(activeNode.offsetTop + "-" + activeNode.offsetParent.offsetTop + "-" + activeNode.clientHeight + "-" + activeSubNav.style.height + "-" + activeNode.style.height);
	//alert(activeNode.offsetLeft + "-" + activeNode.offsetParent.offsetLeft + "-" + activeSubNav.clientWidth + "-" + activeSubNav.style.width);

	// if we found a subnav for this nav, display it
	if (activeSubNav != null) {
		var setTop;
		var setLeft;
		if (activeNode != null) {
				setTop = activeNode.offsetTop + activeNode.clientHeight + 3;

				if (activeNode.clientHeight == 0) {
					setTop = setTop + activeNode.style.height;

 					//setTop = activeNode.offsetTop + activeNode.offsetParent.offsetTop + 3;
					//setTop = setTop + activeSubNav.clientHeight - activeNode.clientHeight;
				} else {
					//setTop = setTop + parseInt(activeSubNav.style.height) - parseInt(activeNode.style.height);
				}

				if (level > 0) {
					setTop = activeNode.offsetParent.offsetTop + activeNode.offsetTop;
					setLeft = activeNode.offsetParent.offsetLeft + activeNode.offsetLeft + activeNode.clientWidth + 1;

				} else {
					if (activeSubNav.clientWidth) {
						setLeft = activeNode.offsetLeft - 15;
					} else {
						setLeft = activeNode.offsetLeft - parseInt(activeSubNav.style.width);
					}
				}
			//alert(activeNode.offsetLeft + "-" + activeNode.offsetParent.offsetLeft + "-" + activeNode.clientWidth + "-");
		} else if (activeSubNav.clientWidth) {
			setLeft = 689 - activeSubNav.clientWidth;
		}
		if (setTop != null) {
			activeSubNav.style.top = setTop + "px";
		}
		if (setLeft != null) {
			activeSubNav.style.left = setLeft + "px";
		}

		activeSubNav.style.visibility = "visible";
	}
	levelActive[level] = nodeId;
	mouseOverNav = true;
}

// same as navOver() except it changes the image for the root nav
function navOverRoot(nodeId) {
	document['root_' + nodeId].src = eval("imgRoot_" + nodeId + "_on.src");
	navOver(nodeId,0);
}

// sets up the page and allows for a floating bottom bar
// not currently being used
function positionBottomFrame() {
	var windowWidth;
	var windowHeight;
	var topWidth;
	var bottomHeight;

	if (ie) {
		windowHeight = document.body.scrollTop + document.body.clientHeight;
	} else if (ns) {
		windowHeight = document.bocy.scrollTop + window.innerHeight;
	} else if (ns6) {
		// inner width in ns6 doesn't include the scrollbar, but the pagelayout does
		windowHeight = document.body.scrollTop +  document.body.clientHeight;
	}
	document.getElementById("bottomFrame").style.top = (windowHeight - 34) + "px";

	var bottomNav = document.getElementById("menu_b_0");
	if (bottomNav != null) {
		if (bottomNav.clientHeight) {
			bottomNav.style.top = windowHeight - bottomNav.clientHeight - 10 + "px";
		} else {
			bottomNav.style.top = windowHeight - parseInt(bottomNav.style.height) - 10 + "px";
		}
	}
}

// set a timer to check to see if the mouse is over a nav element
var action = setInterval("checkOpenNav()",100)
