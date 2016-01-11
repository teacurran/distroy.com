<%@ page import="com.approachingpi.store.servlet.admin.ProductServlet,
                 com.approachingpi.servlet.PiServlet,
                 com.approachingpi.store.catalog.*,
                 com.approachingpi.store.servlet.admin.PriceServlet,
                 com.approachingpi.store.servlet.admin.InventoryServlet"%>
<jsp:useBean id="categories"    class="java.util.ArrayList" scope="request"/>
<jsp:useBean id="products"      class="java.util.ArrayList" scope="request"/>
<jsp:useBean id="artists"       class="java.util.ArrayList" scope="request"/>
<jsp:useBean id="brands"        class="java.util.ArrayList" scope="request"/>
<jsp:useBean id="search"        class="com.approachingpi.store.catalog.SearchEngine" scope="request"/>

<%@ include file = "./include/global.jsp"%>
<%
    int categoryId = altAttribute.getInt("categoryId");
    strPage = "product";
    strPageTitle = "Inventory";
%>
<%@ include file = "./include/header.jsp"%>
<%
	ArrayList sizes = Size.loadAllSizes(con);
%>

<script language="javascript">

var arrayItemIds = new Array();
<%
for (int i=0; i<products.size(); i++) {
    Product product = (Product)products.get(i);
    ArrayList variations = product.getVariations();
    for (int v=0; v<variations.size(); v++) {
        ProductVariation var = (ProductVariation)variations.get(v);
        %>arrayItemIds[arrayItemIds.length]='<%=var.getId()%>';
        <%
    }
}
%>

var focusSize;
var focusItem;

function sizeChange(variation, size) {
    var cell = document.getElementById("size_" + variation + "_" + size);
    cell.className = "inputAltered";
    updateSize(cell, variation, size);
}

function sizeKeyHandler(event) {
    var event = (event) ? event : ((window.event) ? window.event : "")

    // Up Arroe
    if (event.keyCode==38) {
        moveFocusUp();
        event.returnValue = false; // IE
        if (event.preventDefault) {
            event.preventDefault();
        }
        return false;
    // Down Arrow
    } else if (event.keyCode==40) {
        moveFocusDown();
        event.returnValue = false; // IE
        if (event.preventDefault) {
            event.preventDefault();
        }
        return false;
    // Right Arrow
    } else if (event.keyCode==39) {
    }
}

function moveFocusDown() {
    if (focusItem < arrayItemIds.length-1) {
        focusItem++;
        var boxToFocus = document.getElementById("size_" + arrayItemIds[focusItem] + "_" + focusSize);
        if (boxToFocus) {
            if (boxToFocus.disabled) {
                moveFocusDown();
            } else {
                boxToFocus.focus();
                autoSelect(boxToFocus);
            }
        }
    }
}

function moveFocusUp() {
    if (focusItem > 0) {
        focusItem--;
        var boxToFocus = document.getElementById("price_" + arrayItemIds[focusItem]  + "_" + focusSize);
        if (boxToFocus) {
            if (boxToFocus.disabled) {
                moveFocusUp();
            } else {
                boxToFocus.focus();
                autoSelect(boxToFocus);
            }
        }
    }
}

function sizeFocusEvent(strId, size) {
    focusSize = size;
    for (var i=0; i<arrayItemIds.length; i++) {
        if (arrayItemIds[i] == strId) {
            focusItem = i;
            break;
        }
    }
}

function updateSize(cell, variation, size) {
    var http_request = getHttpRequest();
    http_request.onreadystatechange = function() { updateSizeResponseHandler(http_request, cell); };

    var url = "/admin/Inventory?action=<%=InventoryServlet.ACTION_UPDATE%>&productVariationId=" + variation + "&randomString=" + randomString(10);
    url += "&size=" + size + "&sizeValue=" + cell.value;
    http_request.open('GET',  url, true);

    http_request.send(null);
}

function updateSizeResponseHandler(request, cell) {
    if (request.readyState == HTTP_COMPLETE) {
        if (request.status == 200) {
            var responseXML = request.responseXML;

            var list = responseXML.getElementsByTagName('update')[0];
            if (list.getAttribute("success") == "true") {
                cell.className = "input";
            }
        }
    }
}


</script>

<%@ include file = "./include/bodyTop.jsp"%>

<a href="/admin/Product?action=<%=ProductServlet.ACTION_EDIT%>" title="Create a new Product"><img src="<%=strImgAdminDir%>icons/icon_new_file.gif" height="20" width="20" border="0" vspace="10"></a>
<br>

<form method="post" action="/admin/Inventory">
<input type="hidden" name="action" value="<%=ProductServlet.ACTION_LIST%>">
<input type="hidden" name="formSubmitted" value="true">
<select name="categoryId">
<option value="-1">All Categories</option>
<%
    for(int i=0;i<categories.size();i++) {
        Category thisCategory = (Category)categories.get(i);
    %><option value="<%=thisCategory.getId()%>"<% if(search.containsCategory(thisCategory.getId())) { out.write(" selected"); }%>><%=thisCategory.getPathAsString()%></option><%
    }
%>
</select>
<select name="brandId">
<option value="-1">All Brands</option>
<%
    for(int i=0;i<brands.size();i++) {
        Brand thisBrand = (Brand)brands.get(i);
    %><option value="<%=thisBrand.getId()%>"<% if(thisBrand.getId()==search.getInput().getBrand().getId()) { out.write(" selected"); }%>><%=thisBrand.getName()%></option><%
    }
%>
</select>
<select name="artistId">
<option value="-1">All Artists</option>
<%
    for(int i=0;i<artists.size();i++) {
        Artist thisArtist = (Artist)artists.get(i);
    %><option value="<%=thisArtist.getId()%>"<% if(search.containsArtist(thisArtist.getId())) { out.write(" selected"); }%>><%=thisArtist.getNameLast()%>, <%=thisArtist.getNameFirst()%></option><%
    }
%>
</select>
<input type="submit" value="search">
</form>


<% if(PiServlet.getReqBoolean(request,"formSubmitted")) { %>
<table>
    <tr>
        <td class="matrixHead">Brand</td>
        <td class="matrixHead">Image</td>
        <td class="matrixHead">Name</td>
        <td class="matrixHead">Retail</td>
        <td class="matrixHead">Wholesale</td>
		<%
		for (int i=0; i<sizes.size(); i++) {
			Size thisSize = (Size)sizes.get(i);
			%><td class="matrixHead"><%=thisSize.getNameShort()%></td>
			<%
		}
		%>
    </tr>
    <%
    int tabIndex = 0;
    String cssClass = "matrixRow";
    for (int i=0; i<products.size(); i++) {
        Product product = (Product)products.get(i);
		ArrayList variations = product.getVariations();

        if (cssClass.equalsIgnoreCase("matrixRow")) {
            cssClass = "matrixRowAlternate";
        } else {
            cssClass = "matrixRow";
        }

        %>
        <tr>
            <td class="<%=cssClass%>"><%=product.getBrand().getName()%></td>
            <td class="<%=cssClass%>">&nbsp;</td>
            <td class="<%=cssClass%>"><a href="/admin/Product?action=<%=ProductServlet.ACTION_EDIT%>&productId=<%=product.getId()%>"><%=product.getName()%></a></td>
            <td class="<%=cssClass%>" align="center"><input type="checkbox" name="prd_act_retail" value="<%=product.getId()%>"<% if (product.getActiveForRetail()) { out.print(" checked"); } %>/></td>
            <td class="<%=cssClass%>" align="center"><input type="checkbox" name="prd_act_whole" value="<%=product.getId()%>"<% if (product.getActiveForWholesale()) { out.print(" checked"); } %>/></td>
        </tr>
        <%
		for (int v=0; v<variations.size(); v++) {
			ProductVariation var = (ProductVariation)variations.get(v);
	        Image image = var.getFirstImage();
			%>
        	<tr>
        	    <td class="<%=cssClass%>"></td>
        	    <td class="<%=cssClass%>" style="vertical-align: text-bottom;">
        	        <% if (image.getName().length() > 0) { %>
        	            <img src="<%=strImgProductDir%><%=image.getThumbName()%>" width="50">
        	        <% } %>
        	    </td>
        	    <td class="<%=cssClass%>"><a href="/admin/Product?action=<%=ProductServlet.ACTION_EDIT_VARIATION%>&productId=<%=product.getId()%>&variationId=<%=var.getId()%>"><%=var.getStyle()%> - <%=var.getColor()%></a></td>
        	    <td class="<%=cssClass%>" colspan="2" align="center">
                    <input type="checkbox" name="var_act_whole" value="<%=var.getId()%>"<% if (var.getActive()) { out.print(" checked"); } %>/>
                </td>
                <%
                for (int s=0; s<sizes.size(); s++) {
                    Size thisSize = (Size)sizes.get(s);
                    Size thisVarSize = var.getSize(thisSize);
                    %><td class="<%=cssClass%>" align="center">
                        <% if (thisVarSize != null) { %>
                            <input type="text" size="2" id="size_<%=var.getId()%>_<%=thisSize.getId()%>" name="size_<%=var.getId()%>_<%=thisSize.getId()%>" value="<%=thisVarSize.getQtyInStock()%>"  tabindex="<%=Integer.toString(++tabIndex)%>" onFocus="sizeFocusEvent(<%=var.getId()%>, <%=thisSize.getId()%>)" onkeydown="sizeKeyHandler(event);" onChange="sizeChange(<%=var.getId()%>,  <%=thisSize.getId()%>)" autocomplete="off">
                        <% } else { %>
                            &nbsp;
                        <% } %>
                      </td>
                    <%
                }
                %>
        	</tr>
			<%
		}
    } %>
</table>
<% } %>



<%@ include file = "./include/bodyBottom.jsp"%>
