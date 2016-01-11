
<%@ page import="java.util.Enumeration,
                 java.sql.Connection,
                 javax.sql.DataSource,
                 javax.naming.Context,
                 javax.naming.InitialContext,
                 java.util.ArrayList,
                 com.approachingpi.store.catalog.Brand,
                 com.approachingpi.store.catalog.Artist,
                 com.approachingpi.store.catalog.Category,
                 com.approachingpi.store.servlet.LoginServlet,
                 com.approachingpi.store.servlet.MailingListServlet"%>
<jsp:useBean id="errorBean" class="com.approachingpi.util.MessageBean" scope="request"/>
<jsp:useBean id="messageBean" class="com.approachingpi.util.MessageBean" scope="request"/>
<%
    // just to be safe, make sure the message beans are reset.
    errorBean.reset();
    messageBean.reset();

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
    ArrayList allBrands = Brand.getAllBrands(con);
	ArrayList allArtists = Artist.getAllArtists(con,true,"vcNameDisplay");
	Category rootCategory = new Category();
	rootCategory.loadChildrenFromDb(con,true);

	if (strOnLoad.length() > 0) {
		strOnLoad = "headerInit();" + strOnLoad;
	} else {
		strOnLoad = "headerInit()";
	}
%>
<%-- <!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/1999/REC-html401-19991224/loose.dtd"> --%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<html>
<head>
<title>DISTRO.Y :: <%=strPageTitle%></title>

<meta name="keywords" content="apparel, t-shirt, girls, american apparel, shirt, design, baby doll, art, cheap, styleish, style, indie, indy, inexpensive, teeshirts, scene, scenester, tee, clothing, killer, girly, sleeveless, tank, industrial, goth, gothic, hip-hop, tank, gun, pink, black, anime, cute, cartoon, geek, nerd, funny, political, boston, angeldustrial, zazzi, vintage, distroy" />
<% if (strMetaDescription.length() > 0) { %>
    <meta name="description" content="<%=strMetaDescription%>" />
<% } else { %>
    <meta name="description" content="Manufacturer and distributor of the best indie designer brands of clothing and t-shirts." />
<% } %>

<link rel="stylesheet" type="text/css" href="<%=strCss%>" />
<link rel="stylesheet" href="/css/lightbox.css" type="text/css" media="screen" />
<link rel="stylesheet" type="text/css" href="/css/prototip.css" />

<%-- <script type="text/javascript" language="javaScript" SRC="/js/menu.js"></script> --%>
<script type="text/javascript" src="/js/prototype.js"></script>
<script type='text/javascript' src="/js/prototip.js"></script>
<script type="text/javascript" src="/js/effects.js"></script>
<script type="text/javascript" src="/js/accordion.js"></script>
<script type="text/javascript" src="/js/scriptaculous.js?load=effects,builder"></script>
<script type="text/javascript" src="/js/lightbox.js"></script>
<script type="text/javascript" language="javaScript" SRC="/js/util.js"></script>
<script type="text/javascript" src="/js/tabcontent.js"></script>

/***********************************************
* Tab Content script v2.2- &copy; Dynamic Drive DHTML code library (www.dynamicdrive.com)
* This notice MUST stay intact for legal use
* Visit Dynamic Drive at http://www.dynamicdrive.com/ for full source code
***********************************************/

</script>

<script type="text/javascript" language="javascript">

function headerInit() {
	<% for (int i=0; i < errorBean.getHighlightFields().size(); i++) { %>
		highlightErrorField('<%=errorBean.getHighlightFields().get(i)%>');
	<% } %>
	<% for (int i=0; i < messageBean.getHighlightFields().size(); i++) { %>
		highlightErrorField('<%=messageBean.getHighlightFields().get(i)%>');
	<% } %>
  <% if (!boIsPopup) { %>
    var contentHeight = ($("mainContainer"). clientHeight) + 230;
    var leftNavHeight = ($("mainContainer"). clientHeight) + 40;
    if (contentHeight < <%=strPageHeight%>) {
        contentHeight = <%=strPageHeight%>;
        leftNavHeight = <%=strPageHeight%> - 180;
    }
    // alert("old:" + $("shadow1").style.height + " new:" + contentHeight + "px");
    $("shadow1").style.height = contentHeight + 'px';
    <% if (boShowNav) { %>
        $("leftNav").style.height = leftNavHeight + 'px';
    <% }%>
  <% } %>
}

</script>
<% if (boShowNav) { %>
    <script type="text/javascript">

		//
		//  In my case I want to load them onload, this is how you do it!
		//
		Event.observe(window, 'load', loadAccordions, false);

		//
		//	Set up all accordions
		//
		function loadAccordions() {
			/* var topAccordion = new accordion('horizontal_container', {
				classNames : {
					toggle : 'horizontal_accordion_toggle',
					toggleActive : 'horizontal_accordion_toggle_active',
					content : 'horizontal_accordion_content'
				},
				defaultSize : {
					width : 100
				},
				direction : 'horizontal'
			});

			var bottomAccordion = new accordion('vertical_container'); */

			var nestedVerticalAccordion = new accordion('vertical_nested_container', {
			  classNames : {
					toggle : 'vertical_accordion_toggle',
					toggleActive : 'vertical_accordion_toggle_active',
					content : 'vertical_accordion_content'
				}
			});

			// Open first one: bottomAccordion.activate($$('#vertical_container .accordion_toggle')[0]);

			// Open second one: topAccordion.activate($$('#horizontal_container .horizontal_accordion_toggle')[2]);
                        
                      nestedVerticalAccordion.activate($$('#vertical_nested_container .vertical_accordion_toggle')[<%=strActivateMenu%>]);
}

	</script>
<% } %>
</head>
<body onload="<%=strOnLoad%>" background="<%=backPattern%>">
<!-- <body leftmargin="<%=leftMargin%>" topmargin="<%=topMargin%>" marginwidth="<%=marginWidth%>" marginheight="<%=marginHeight%>" onLoad="<%=strOnLoad%>" background="<%=backPattern%>"> -->
<% if (!boIsPopup) { %>
  <div id="shadow1">
    <div class="shadow2">
        <div class="head"><a href="<%=strBaseUrl%>"><img src="<%=topImage%>" alt="distroy" border="0" /></a></div>
        <div class="headFloat"><img src="<%=topLogo%>" alt="distroy" border="0" /></div>
        <div class="topLinks"><a href="<%=strBaseUrl%>content/support/about">ABOUT</a>&nbsp;
	<% if (wholesale) { %>
            . &nbsp;<a href="<%=strBaseUrl%>content/support/contact">CONTACT</a>&nbsp;
        <% } %>
        <%-- if (boShowNav) { --%>
            . &nbsp;<a href="<%=strBaseUrl%>cart">CHECKOUT/VIEW CART</a>&nbsp;
            	<% if (user.getId() > 0) { %>
                    . &nbsp;<a href="<%=strBaseUrl%>login?action=<%=LoginServlet.ACTION_LOGOUT%>&loginForm=<%=strBaseUrl%>/">LOGOUT</a>&nbsp;
            	<% } %>
            <%-- } --%>
        </div>
        <%--
        int navHeight = (boShowNav) ? 5 : 3;
        if (errorBean.getMessageCount()>0) {
            navHeight+=2;
        }
        if (messageBean.getMessageCount()>0) {
            navHeight+=2;
        }
        --%>
        <%-- rowspan="<%=navHeight%>" --%>
    
         <% if (boShowNav) { %>
    <!-- start left nav -->

            <div id="leftNav">
		<div id="vertical_nested_container">
        			        <span class="vertical_accordion_toggle">BRANDS</span>
                			<div class="vertical_accordion_content">
                    			<% for (int i=0; i<allBrands.size(); i++) {
                                            Brand thisBrand = (Brand)allBrands.get(i);
                                            if (thisBrand.getName().equals(menuBrand)) { %>
                                            <span class="vertical_accordion_content_this"><%=thisBrand.getName()%></span><br />
                                            <% } else { %>
                                                <a href="<%=strBaseUrl%>brands/<%=thisBrand.getName().replaceAll(" ","_")%>"><%=thisBrand.getName()%></a><br />
                                            <% }
                                         } %>
		                	</div>
                                        <span class="vertical_accordion_toggle">CATEGORIES</span>
			                <div class="vertical_accordion_content">
    <%
    ArrayList categories = rootCategory.getChildren();
    for (int i=0; i<categories.size(); i++) {
        Category thisCategory = (Category)categories.get(i);
        if (thisCategory.getId() == menuCategory) { %>
            <span class="vertical_accordion_content_this"><%=thisCategory.getName()%></span><br />
        <% } else { %>
            <a href="<%=strBaseUrl%>categories/<%=thisCategory.getId()%>"><%=thisCategory.getName()%></a><br />
        <% } 
        ArrayList categories2 = thisCategory.getChildren();
        if (categories2.size()>0) { %>
          <div class="vertical_accordion_content">
        <%
        for (int i2=0; i2<categories2.size(); i2++) {
            Category thisCategory2 = (Category)categories2.get(i2);
        if (thisCategory2.getId() == menuCategory) { %>
            <span class="vertical_accordion_content_this"><%=thisCategory2.getName()%></span><br />
        <% } else { %>
            <a href="<%=strBaseUrl%>categories/<%=thisCategory2.getId()%>"><%=thisCategory2.getName()%></a><br />
        <% } 
        }
        %> </div> <%
    }
 } %>	
</div>
                                        <span class="vertical_accordion_toggle">SUPPORT</span>
			                <div class="vertical_accordion_content">
                                          <a href="<%=strBaseUrl%>content/support/about">About</a><br />
                                          <a href="<%=strBaseUrl%>content/support/policies">Policies</a><br />
                                          <a href="<%=strBaseUrl%>content/support/faqs">FAQ's</a><br />
                                          <a href="<%=strBaseUrl%>content/support/contact">Contact Us</a><br />
                                          <a href="<%=strBaseUrl%>orderstatus">Order Status</a><br />
                                          <!-- <a href="<%=strBaseUrl%>wholesale">Wholesale</a><br /> -->
					</div>
                                        <% if (strActivateMenu == "99"){ %>
                                            <span class="vertical_accordion_notoggle_active">
                                        <% } else { %>
                                            <span class="vertical_accordion_notoggle">
                                        <% } %>
                                        <a href="<%=strBaseUrl%>content/links">LINKS</a></span>
                                        <span class="vertical_accordion_notoggle">
                                        <% if (wholesale) { %>
                                            <a href="/">RETAIL</a>
                                        <% } else { %>
                                            <a href="/wholesale">WHOLESALE</a>
                                        <% } %>
                                        </span>
                                        <span class="vertical_accordion_notoggle"><a href="http://blog.distroy.com/">BLOG</a></span>
			            </div>					
                                  </div>
        <div id="mainContainer" class="contentWithLeftNav">
    <% } else { %>
        <div id="mainContainer" class="contentWithoutLeftNav">
    <% } %>
 <% } %>


            <%-- body goes here --%>
            
    <% if (errorBean.getMessageCount() > 0) { %>
        <div class="contentError">
                <% while (errorBean.hasNext()) { %>
                    <%=errorBean.getNextMessage()%><br>
                <% } %>
        </div>
    <% } %>
<% if (messageBean.getMessageCount() > 0) { %>
            <div class="contentMessage">
                <% while (messageBean.hasNext()) { %>
                    <%=messageBean.getNextMessage()%><br>
                <% } %>
            </div>
    <% } %>















