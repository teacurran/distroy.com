<%@ page import="com.approachingpi.store.servlet.admin.ProductServlet,
                 com.approachingpi.servlet.PiServlet,
                 com.approachingpi.store.catalog.*,
                 com.approachingpi.store.servlet.admin.PriceServlet"%>
<jsp:useBean id="categories"    class="java.util.ArrayList" scope="request"/>
<jsp:useBean id="products"      class="java.util.ArrayList" scope="request"/>
<jsp:useBean id="artists"       class="java.util.ArrayList" scope="request"/>
<jsp:useBean id="brands"        class="java.util.ArrayList" scope="request"/>
<jsp:useBean id="search"        class="com.approachingpi.store.catalog.SearchEngine" scope="request"/>

<%@ include file = "./include/global.jsp"%>
<%
    int categoryId = altAttribute.getInt("categoryId");
    strPage = "product";
    strPageTitle = "Prices";
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

var focusCellType;
var focusItem;

function priceCostChange(variation) {
    var cell = document.getElementById("price_cost_" + variation);
    cell.className = "inputAltered";
    updatePrice(cell, variation, "Cost");
}

function priceRetailChange(variation) {
    var cell = document.getElementById("price_retail_" + variation);
    cell.className = "inputAltered";
    updatePrice(cell, variation, "Retail");
}

function priceRetailSaleChange(variation) {
    var cell = document.getElementById("price_retailsale_" + variation);
    cell.className = "inputAltered";
    updatePrice(cell, variation, "RetailSale");
}

function priceWholeChange(variation) {
    var cell = document.getElementById("price_whole_" + variation);
    cell.className = "inputAltered";
    updatePrice(cell, variation, "Wholesale");
}

function priceKeyHandler(event) {
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
        var boxToFocus = document.getElementById("price_" +focusType+ "_" + arrayItemIds[focusItem]);
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
        var boxToFocus = document.getElementById("price_" +focusType+ "_" + arrayItemIds[focusItem]);
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

function priceFocusEvent(strId, cellType) {
    focusType = cellType;
    for (var i=0; i<arrayItemIds.length; i++) {
        if (arrayItemIds[i] == strId) {
            focusItem = i;
            break;
        }
    }
}

function productActiveWholeChange(productId) {
    var checkbox = document.priceForm["prd_act_whole_" + productId];
    if (checkbox) {
        var http_request = getHttpRequest();
        http_request.onreadystatechange = function() { productActiveWholeChangeResponseHandler(http_request, productId); };

        var url = "/admin/Price?action=<%=PriceServlet.ACTION_UPDATE_PRODUCT%>&productId=" + productId + "&randomString=" + randomString(10);
        if (checkbox.checked) {
            url += "&activeWholesale=true";
        } else {
            url += "&activeWholesale=false";
        }
        http_request.open('GET',  url, true);

        http_request.send(null);
    }
}

function productActiveWholeChangeResponseHandler(request, productId) {
    if (request.readyState == HTTP_COMPLETE) {
        if (request.status == 200) {
            var responseXML = request.responseXML;

            var list = responseXML.getElementsByTagName('update')[0];
            if (list.getAttribute("success") == "true") {
                // don't know what to do here
            }
        }
    }
}

function productActiveRetailChange(productId) {
    var checkbox = document.priceForm["prd_act_retail_" + productId];
    if (checkbox) {
        var http_request = getHttpRequest();
        http_request.onreadystatechange = function() { productActiveRetailChangeResponseHandler(http_request, productId); };

        var url = "/admin/Price?action=<%=PriceServlet.ACTION_UPDATE_PRODUCT%>&productId=" + productId + "&randomString=" + randomString(10);
        if (checkbox.checked) {
            url += "&activeRetail=true";
        } else {
            url += "&activeRetail=false";
        }
        http_request.open('GET',  url, true);

        http_request.send(null);
    }

}

function productActiveRetailChangeResponseHandler(request, productId) {
    if (request.readyState == HTTP_COMPLETE) {
        if (request.status == 200) {
            var responseXML = request.responseXML;

            var list = responseXML.getElementsByTagName('update')[0];
            if (list.getAttribute("success") == "true") {
                // don't know what to do here
            }
        }
    }
}

function updatePrice(cell, variation, priceType) {
    var http_request = getHttpRequest();
    http_request.onreadystatechange = function() { updatePriceResponseHandler(http_request, cell); };

    var url = "/admin/Price?action=<%=PriceServlet.ACTION_UPDATE%>&productVariationId=" + variation + "&randomString=" + randomString(10);
    url += "&price" + priceType + "=" + cell.value;
    http_request.open('GET',  url, true);

    http_request.send(null);
}

function updatePriceResponseHandler(request, cell) {
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

function variationActiveChange(variationId) {
    var checkbox = document.priceForm["var_act_" + variationId];
    if (checkbox) {
        var http_request = getHttpRequest();
        http_request.onreadystatechange = function() { variationActiveChangeResponseHandler(http_request, variationId); };

        var url = "/admin/Price?action=<%=PriceServlet.ACTION_UPDATE%>&productVariationId=" + variationId + "&randomString=" + randomString(10);
        if (checkbox.checked) {
            url += "&active=true";
        } else {
            url += "&active=false";
        }
        http_request.open('GET',  url, true);
        http_request.send(null);
    }
}

function variationActiveChangeResponseHandler(request, variationId) {
    if (request.readyState == HTTP_COMPLETE) {
        if (request.status == 200) {
            var responseXML = request.responseXML;

            var list = responseXML.getElementsByTagName('update')[0];
            if (list.getAttribute("success") == "true") {

            }
        }
    }
}

function variationSaleChange(variationId) {
    var checkbox = document.priceForm["var_sale_" + variationId];
    if (checkbox) {
        var http_request = getHttpRequest();
        http_request.onreadystatechange = function() { variationActiveChangeResponseHandler(http_request, variationId); };

        var url = "/admin/Price?action=<%=PriceServlet.ACTION_UPDATE%>&productVariationId=" + variationId + "&randomString=" + randomString(10);
        if (checkbox.checked) {
            url += "&sale=true";
        } else {
            url += "&sale=false";
        }
        http_request.open('GET',  url, true);
        http_request.send(null);
    }
}

function variationActiveChangeResponseHandler(request, variationId) {
    if (request.readyState == HTTP_COMPLETE) {
        if (request.status == 200) {
            var responseXML = request.responseXML;

            var list = responseXML.getElementsByTagName('update')[0];
            if (list.getAttribute("success") == "true") {

            }
        }
    }
}

</script>


<%@ include file = "./include/bodyTop.jsp"%>

<br>
<form method="post" action="/admin/Price">
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


<% if(PiServlet.getReqBoolean(request,"formSubmitted") || products.size() > 0) { %>
<br /><b>Warning: Changes made to this page are automatically saved</b><br /><br />
<form method="post" action="/admin/Price" onSubmit="return false" id="priceForm" name="priceForm">
<table>
    <tr>
        <td class="matrixHead">Brand</td>
        <td class="matrixHead">Image</td>
        <td class="matrixHead">Name</td>
        <td class="matrixHead">SKU</td>
        <td class="matrixHead">Active W</td>
        <td class="matrixHead">Active R</td>
        <td class="matrixHead">$ Cost</td>
        <td class="matrixHead">$ Whole</td>
        <td class="matrixHead">$ Retail</td>
        <td class="matrixHead" colspan="2">$ Retail Sale</td>
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
        <tr id="row_product_<%=product.getId()%>">
            <td class="<%=cssClass%>"><%=product.getBrand().getName()%></td>
            <td class="<%=cssClass%>">&nbsp;</td>
            <td class="<%=cssClass%>"><a href="/admin/Product?action=<%=ProductServlet.ACTION_EDIT%>&productId=<%=product.getId()%>"><%=product.getName()%></a></td>
            <td class="<%=cssClass%>"><a href="/admin/Product?action=<%=ProductServlet.ACTION_EDIT%>&productId=<%=product.getId()%>"><%=product.getSku()%></a></td>
            <td class="<%=cssClass%>" align="center"><input type="checkbox" class="checkbox" name="prd_act_whole_<%=product.getId()%>" onClick="productActiveWholeChange(<%=product.getId()%>)" value="<%=product.getId()%>"<% if (product.getActiveForWholesale()) { out.print(" checked"); } %>/></td>
            <td class="<%=cssClass%>" align="center"><input type="checkbox" class="checkbox" name="prd_act_retail_<%=product.getId()%>" onClick="productActiveRetailChange(<%=product.getId()%>)" value="<%=product.getId()%>"<% if (product.getActiveForRetail()) { out.print(" checked"); } %>/></td>
            <td class="<%=cssClass%>" colspan="3">&nbsp;</td>
        </tr>
        <%
		for (int v=0; v<variations.size(); v++) {
			ProductVariation var = (ProductVariation)variations.get(v);
	        Image image = var.getFirstImage();
			%>
        	<tr id="row_variation_<%=var.getId()%>">
        	    <td class="<%=cssClass%>"></td>
        	    <td class="<%=cssClass%>" style="vertical-align: text-bottom;">
        	        <% if (image.getName().length() > 0) { %>
        	            <img src="<%=strImgProductDir%><%=image.getThumbName()%>" width="50">
        	        <% } %>
        	    </td>
        	    <td class="<%=cssClass%>"><a href="/admin/Product?action=<%=ProductServlet.ACTION_EDIT_VARIATION%>&productId=<%=product.getId()%>&variationId=<%=var.getId()%>"><%=var.getStyle()%> - <%=var.getColor()%></a></td>
        	    <td class="<%=cssClass%>"><a href="/admin/Product?action=<%=ProductServlet.ACTION_EDIT_VARIATION%>&productId=<%=product.getId()%>&variationId=<%=var.getId()%>"><%=var.getSku()%></a></td>
        	    <td class="<%=cssClass%>" colspan="2" align="center">
                    <input type="checkbox" class="checkbox" name="var_act_<%=var.getId()%>" value="<%=var.getId()%>" onClick="variationActiveChange(<%=var.getId()%>)" <% if (var.getActive()) { out.print(" checked"); } %>/>
                </td>
        	    <td class="<%=cssClass%>" align="center">
                    <input type="text" size="8" id="price_cost_<%=var.getId()%>" name="price_cost_<%=var.getId()%>" value="<%=var.getPriceCost().setScale(2)%>" tabindex="<%=Integer.toString(++tabIndex)%>" onFocus="priceFocusEvent(<%=var.getId()%>, 'cost')" onkeydown="priceKeyHandler(event);" onChange="priceCostChange(<%=var.getId()%>)" autocomplete="off"/>
                </td>
        	    <td class="<%=cssClass%>" align="center">
                    <input type="text" size="8" id="price_whole_<%=var.getId()%>" name="price_whole_<%=var.getId()%>" value="<%=var.getPriceWholesale().setScale(2)%>" tabindex="<%=Integer.toString(++tabIndex)%>" onFocus="priceFocusEvent(<%=var.getId()%>, 'whole')" onkeydown="priceKeyHandler(event);" onChange="priceWholeChange(<%=var.getId()%>)" autocomplete="off"/>
                </td>
        	    <td class="<%=cssClass%>" align="center">
                    <input type="text" size="8" id="price_retail_<%=var.getId()%>" name="price_retail_<%=var.getId()%>" value="<%=var.getPriceRetail().setScale(2)%>" tabindex="<%=Integer.toString(++tabIndex)%>" onFocus="priceFocusEvent(<%=var.getId()%>, 'retail')" onkeydown="priceKeyHandler(event);" onChange="priceRetailChange(<%=var.getId()%>)" autocomplete="off"/>
                </td>
        	    <td class="<%=cssClass%>" colspan="2" align="center">
                    <input type="checkbox" class="checkbox" name="var_sale_<%=var.getId()%>" value="<%=var.getId()%>" onClick="variationSaleChange(<%=var.getId()%>)" <% if (var.isSale()) { out.print(" checked"); } %>/>
                </td>
        	    <td class="<%=cssClass%>" align="center">
                    <input type="text" size="8" id="price_retailsale_<%=var.getId()%>" name="price_retailsale_<%=var.getId()%>" value="<%=var.getPriceRetailSale().setScale(2)%>" tabindex="<%=Integer.toString(++tabIndex)%>" onFocus="priceFocusEvent(<%=var.getId()%>, 'retailsale')" onkeydown="priceKeyHandler(event);" onChange="priceRetailSaleChange(<%=var.getId()%>)" autocomplete="off"/>
                </td>
        	</tr>
			<%
		}
    } %>
</table>
</form>
<% } %>



<%@ include file = "./include/bodyBottom.jsp"%>
