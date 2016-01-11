<%@ page import="com.approachingpi.store.servlet.admin.ProductServlet,
                 com.approachingpi.servlet.PiServlet,
                 com.approachingpi.store.catalog.Brand,
                 com.approachingpi.store.catalog.Artist,
                 com.approachingpi.store.servlet.admin.ImageEditServlet,
                 com.approachingpi.store.servlet.admin.ImageUploadServlet,
                 java.util.ArrayList,
                 com.approachingpi.store.catalog.ProductVariation,
                 com.approachingpi.store.catalog.Image"%>

<jsp:useBean id="product"       class="com.approachingpi.store.catalog.Product" scope="request"/>
<jsp:useBean id="artists"       class="java.util.ArrayList" scope="request"/>
<jsp:useBean id="brands"        class="java.util.ArrayList" scope="request"/>

<%@ include file = "./include/global.jsp"%>
<%
    strPage = "product";
    strPageTitle = "Product Edit";
%>
<%@ include file = "./include/header.jsp"%>
<script language="javascript">
    var selectedImage = 0;
    var selectedVariation = -1;

    var imgIconEdit             = new Image();
    var imgIconEditDisabled     = new Image();
    var imgIconUp               = new Image();
    var imgIconUpDisabled       = new Image();
    var imgIconDown             = new Image();
    var imgIconDownDisabled     = new Image();
    var imgIconRight            = new Image();
    var imgIconRightDisabled    = new Image();
    var imgIconLeft             = new Image();
    var imgIconLeftDisabled     = new Image();
    var imgIconDelete           = new Image();
    var imgIconDeleteDisabled   = new Image();

    imgIconEdit.src             = "<%=strImgAdminDir%>icons/icon_window_new.gif";
    imgIconEditDisabled.src     = "<%=strImgAdminDir%>icons/icon_window_new_disabled.gif";
    imgIconUp.src               = "<%=strImgAdminDir%>icons/icon_arrow_up.gif";
    imgIconUpDisabled.src       = "<%=strImgAdminDir%>icons/icon_arrow_up_disabled.gif";
    imgIconDown.src             = "<%=strImgAdminDir%>icons/icon_arrow_down.gif";
    imgIconDownDisabled.src     = "<%=strImgAdminDir%>icons/icon_arrow_down_disabled.gif";
    imgIconLeft.src             = "<%=strImgAdminDir%>icons/icon_arrow_left.gif";
    imgIconLeftDisabled.src     = "<%=strImgAdminDir%>icons/icon_arrow_left_disabled.gif";
    imgIconRight.src            = "<%=strImgAdminDir%>icons/icon_arrow_right.gif";
    imgIconRightDisabled.src    = "<%=strImgAdminDir%>icons/icon_arrow_right_disabled.gif";
    imgIconDelete.src           = "<%=strImgAdminDir%>icons/icon_file_delete.gif";
    imgIconDeleteDisabled.src   = "<%=strImgAdminDir%>icons/icon_file_delete_disabled.gif";

    var arrayVariations = new Array();
    <%
    String cssClass = "matrixRow";
    ArrayList variations = product.getVariations();
    for (int i=0; i<variations.size(); i++) {
        ProductVariation variation = (ProductVariation)variations.get(i);
        if (cssClass.equalsIgnoreCase("matrixRow")) {
            cssClass = "matrixRowAlternate";
        } else {
            cssClass = "matrixRow";
        }
        %>
        arrayVariations[<%=i%>] = new Array();
        arrayVariations[<%=i%>][0] = <%=variation.getId()%>;
        arrayVariations[<%=i%>][1] = "<%=cssClass%>";
        <%
    }
    %>
    
    function confirmDeleteImg() {
        if (selectedImage == 0) {
            return;
        }

        showDeleteBox('Image', 'javascript:deleteImg()');
    }

    function deleteImg() {
        if (selectedImage == 0) {
            return;
        }

        var url = "/admin/ImageEdit?imageId=" + selectedImage + "&action=<%=ImageEditServlet.ACTION_DELETE%>&productId=<%=product.getId()%>";
        loadImageBoxUrl(url);

        cancelDelete();
    }

    function disableImgButtons() {
        var iconEdit    = document.getElementById("iconEdit");
        var iconLeft    = document.getElementById("iconLeft");
        var iconRight   = document.getElementById("iconRight");
        var iconDelete  = document.getElementById("iconDelete");

        iconEdit.src    = imgIconEditDisabled.src;
        iconLeft.src    = imgIconLeftDisabled.src;
        iconRight.src   = imgIconRightDisabled.src;
        iconDelete.src  = imgIconDeleteDisabled.src;

        selectedImage = 0;
    }

    function enableImgButtons(imageId) {

        if (selectedImage == 0) {
            var iconEdit    = document.getElementById("iconEdit");
            var iconLeft    = document.getElementById("iconLeft");
            var iconRight   = document.getElementById("iconRight");
            var iconDelete  = document.getElementById("iconDelete");

            iconEdit.src    = imgIconEdit.src;
            iconLeft.src    = imgIconLeft.src;
            iconRight.src   = imgIconRight.src;
            iconDelete.src  = imgIconDelete.src;
        }
        selectedImage = imageId;
    }

    function enableVariationButtons() {
        var iconVariationUp     = document.getElementById("iconVariationUp");
        var iconVariationDown   = document.getElementById("iconVariationDown");

        iconVariationUp.src     = imgIconUp.src;
        iconVariationDown.src   = imgIconDown.src;
    }
    
    function loadImageBoxUrl(url) {
        if (document.all) {
            // this way of referencing the iframe is called for all ie implementations, although is only neccessary for ie 5.2 on the mac
            document.imageBox.document.location = url;
        } else {
            imageBox = document.getElementById("imageBox");
            imageBox.src = url;
        }
    }
    
    function mouseOverVariation(id) {
        var variationRow = document.getElementById("variation_" + id);
        if (variationRow) {
            var childNodes = variationRow.childNodes;
            for (var i=0; i<childNodes.length; i++) {
                if (childNodes[i].tagName == 'TD') {
                    childNodes[i].className="matrixRowSelected";
                }
            }
        }
    }

    function mouseOutVariation(id) {
        var variationRow = document.getElementById("variation_" + id);
        if (variationRow) {
            var childNodes = variationRow.childNodes;
            for (var i=0; i<childNodes.length; i++) {
                if (childNodes[i].tagName == 'TD') {
                    childNodes[i].className=arrayVariations[id][1];
                }
            }
        }
   }

    function moveImageDown() {
        if (selectedImage == 0) {
            return;
        }
        var url = "/admin/ImageEdit?action=<%=ImageEditServlet.ACTION_MOVE_DOWN%>&productId=<%=product.getId()%>&imageId=" + selectedImage;
        loadImageBoxUrl(url);
    }

    function moveImageUp() {
        if (selectedImage == 0) {
            return;
        }
        var url = "/admin/ImageEdit?action=<%=ImageEditServlet.ACTION_MOVE_UP%>&productId=<%=product.getId()%>&imageId=" + selectedImage;
        loadImageBoxUrl(url);
    }

    function moveVariationDown() {
        if (selectedVariation == -1) {
            return;
        }
        var variationId = arrayVariations[selectedVariation][0];
        location = "/admin/Product?action=<%=ProductServlet.ACTION_MOVE_VARIATION_DOWN%>&productId=<%=product.getId()%>&variationId=" + variationId;
    }

    function moveVariationUp() {
        if (selectedVariation == -1) {
            return;
        }
        var variationId = arrayVariations[selectedVariation][0];
        location = "/admin/Product?action=<%=ProductServlet.ACTION_MOVE_VARIATION_UP%>&productId=<%=product.getId()%>&variationId=" + variationId;
    }

    function refreshImages() {
        var url = "/admin/ImageEdit?action=<%=ImageEditServlet.ACTION_MAIN%>&productId=<%=product.getId()%>";
        loadImageBoxUrl(url);
    }

    function selectVariation(id) {
        unSelectVariation();
        selectedVariation = id;
        mouseOverVariation(id);
        enableVariationButtons();
    }
    
    function unSelectVariation() {
        if (selectedVariation > -1) {
            id = selectedVariation;
            selectedVariation = -1;
            mouseOutVariation(id);
        }
    }
    
    function uploadImage() {
        if (<%=product.getId()%> > 0) {
            var oUploadWindow = window.open("/admin/ImageUpload?action=<%=ImageUploadServlet.ACTION_MAIN%>&productId=<%=product.getId()%>", "imageUpload", "width=400,height=400,status=yes,resizable=yes,scrollbars=yes");
            oUploadWindow.focus();
        }
    }

    tinyMCE.init({
        mode : "exact",
        elements: "desc",
        theme : "advanced",
        plugins : "safari,pagebreak,style,save,advhr,advimage,advlink,emotions,preview,searchreplace,print,contextmenu,paste,directionality,visualchars,nonbreaking,xhtmlxtras,template,inlinepopups",


        // Theme options
        theme_advanced_buttons1 : "code,bold,italic,underline,strikethrough,charmap,|,fontselect,fontsizeselect",
        theme_advanced_buttons2 : "",
        theme_advanced_buttons3 : "",

        theme_advanced_toolbar_location : "top",
        theme_advanced_toolbar_align : "left",
        theme_advanced_resizing : true,

        // Example word content CSS (should be your site CSS) this one removes paragraph margins
        content_css : "/css/retail.css"
    });

</script>

<%@ include file = "./include/bodyTop.jsp"%>

<a href="/admin/Product?action=<%=ProductServlet.ACTION_LIST%>">back to main</a><br>

<form method="post" action="/admin/Product" name="mainForm">
<input type="hidden" name="action" value="<%=ProductServlet.ACTION_EDIT%>">
<input type="hidden" name="productId" value="<%=product.getId()%>">
<input type="hidden" name="formSubmitted" value="true">

<table>
    <tr>
        <td class="inputLabel">Id</td>
        <td colspan="3"><% if (product.getId() > 0) {
                %><%=product.getId()%><%
        } else {
                %>new<%
        }
            %>
        </td>
        <td style="padding:0px;">
            <table width="100%">
                <tr>
                    <td class="inputLabel" style="text-align:left;">Images</td>
                    <td align="right" style="background-color: #EEEEEE;">
                        <a href="javascript:uploadImage()" title="Upload an image."><img id="iconNew" src="<%=strImgAdminDir%>icons/icon_new_file<%if(product.getId()==0){out.write("_disabled");}%>.gif" height="20" width="20" border="0" alt="Upload an image."></a>
                        <a href="" title="Edit image."><img id="iconEdit" src="<%=strImgAdminDir%>icons/icon_window_new_disabled.gif" height="20" width="20" border="0" alt="Edit image."></a>
                        <a href="javascript:moveImageUp()" title="Move image up."><img id="iconLeft" src="<%=strImgAdminDir%>icons/icon_arrow_left_disabled.gif" height="20" width="20" border="0" alt="Move image up."></a>
                        <a href="javascript:moveImageDown()" title="Move image down."><img id="iconRight" src="<%=strImgAdminDir%>icons/icon_arrow_right_disabled.gif" height="20" width="20" border="0" alt="Move image down."></a>
                        <a href="javascript:confirmDeleteImg()" title="Delete image."><img id="iconDelete" src="<%=strImgAdminDir%>icons/icon_file_delete_disabled.gif" height="20" width="20" border="0" alt="Delete image."></a>
                    </td>
                </tr>
            </table>
        </td>
    </tr>
    <tr>
        <td class="inputLabel">Created</td>
        <td><%if(product.getDateCreated() != null) { out.write(timeFormat.format(product.getDateCreated())); }%></td>
        <td class="inputLabel">Modified</td>
        <td><%if(product.getDateModified() != null) { out.write(timeFormat.format(product.getDateModified())); }%></td>
        <td rowspan="7" valign="top">
            <iframe id="imageBox" src="/admin/ImageEdit?productId=<%=product.getId()%>" style="border-style:solid; border-width:1px; height:175px;"></iframe>
        </td>
    </tr>
    <tr>
        <td class="inputLabel">Name</td>
        <td colspan="3"><input type="text" size="40" maxlength="100" name="name" value="<%=product.getName()%>"></td>
    </tr>
    <tr>
        <td class="inputLabel">SKU</td>
        <td colspan="3"><input type="text" size="40" maxlength="100" name="sku" value="<%=product.getSku()%>"></td>
    </tr>
    <tr>
        <td class="inputLabel">Retail</td>
        <td class="label"><input type="checkbox" name="activeForRetail" value="true"<% if (product.getActiveForRetail()) { out.print(" checked"); } %>></td>
        <td class="inputLabel">Wholesale</td>
        <td class="label"><input type="checkbox" name="activeForWholesale" value="true"<% if (product.getActiveForWholesale()) { out.print(" checked"); } %>></td>
    </tr>
    <tr>
        <td class="inputLabel">Brand</td>
        <td class="label">
            <select name="brandId">
            <option value="0">None Selected</option>
            <%
                for(int i=0;i<brands.size();i++) {
                    Brand thisBrand = (Brand)brands.get(i);
                %><option value="<%=thisBrand.getId()%>"<% if(thisBrand.getId()==product.getBrand().getId()) { out.write(" selected"); }%>><%=thisBrand.getName()%></option><%
                }
            %>
            </select>
        </td>
        <td class="inputLabel">Artist</td>
        <td class="label">
            <select name="artistId">
            <option value="0">None Selected</option>
            <%
                Artist artist = new Artist();
                // we only have one artist per design right now, so grab the first one
                if (product.getArtists().size() > 0) {
                    artist = (Artist)product.getArtists().get(0);
                }
                for(int i=0;i<artists.size();i++) {
                    Artist thisArtist = (Artist)artists.get(i);
                %><option value="<%=thisArtist.getId()%>"<% if(thisArtist.getId()==artist.getId()) { out.write(" selected"); }%>><%=thisArtist.getNameLast()%>, <%=thisArtist.getNameFirst()%></option><%
                }
            %>
            </select>
        </td>
    </tr>
    <tr>
        <td class="inputLabel">Description</td>
        <td class="label" colspan="3">
            <textarea name="desc" rows="5" cols="45"><%=product.getDesc()%></textarea>
        </td>
    </tr>
    <tr>
        <td class="inputLabel">Text Desc</td>
        <td class="label" colspan="3">
            <textarea name="textDescription" rows="2" cols="45"><%=product.getTextDescription()%></textarea>
        </td>
    </tr>
    
    <tr>
        <td></td>
        <td><input type="submit" name="button_save" value="save"></td>
    </tr>

    <tr>
        <td colspan="5" style="background-color: #EEEEEE;">
            <table width="100%">
                <tr>
                    <td class="inputLabel" style="text-align:left;">Variations</td>
                    <td align="right">
                        <a href="/admin/Product?action=<%=ProductServlet.ACTION_EDIT_VARIATION%>&productId=<%=product.getId()%>" title="Create a new Variation"><img src="<%=strImgAdminDir%>icons/icon_new_file<% if (product.getId()==0) { out.print("_disabled"); } %>.gif" height="20" width="20" border="0"></a>
                        <%-- <a href="" title="Edit variation."><img id="iconEdit" src="<%=strImgAdminDir%>icons/icon_window_new_disabled.gif" height="20" width="20" border="0" alt="Edit image."></a> --%>
                        <a href="javascript:moveVariationUp()" title="Move variation up."><img id="iconVariationUp" src="<%=strImgAdminDir%>icons/icon_arrow_up_disabled.gif" height="20" width="20" border="0" alt="Move variation up."></a>
                        <a href="javascript:moveVariationDown()" title="Move variation down."><img id="iconVariationDown" src="<%=strImgAdminDir%>icons/icon_arrow_down_disabled.gif" height="20" width="20" border="0" alt="Move variation down."></a>
                    </td>
                </tr>
            </table>
        </td>
    </tr>
    <tr>
        <td colspan="6">
            <table width="100%">
                <tr>
                    <td class="matrixHead">Image</td>
                    <td class="matrixHead">Style</td>
                    <td class="matrixHead">Color</td>
                    <td class="matrixHead">SKU</td>
                    <td class="matrixHead">Cost</td>
                    <td class="matrixHead">Wholesale</td>
                    <td class="matrixHead">Retail</td>
                </tr>
                <%
                cssClass = "matrixRow";
                for (int i=0; i<variations.size(); i++) {
                    ProductVariation variation = (ProductVariation)variations.get(i);

                    Image image = variation.getFirstImage();

                    if (cssClass.equalsIgnoreCase("matrixRow")) {
                        cssClass = "matrixRowAlternate";
                    } else {
                        cssClass = "matrixRow";
                    }

                    %>
                    <tr id="variation_<%=i%>" onclick="selectVariation(<%=i%>)">
                        <td class="<%=cssClass%>" style="vertical-align: text-bottom;">
                            <% if (image.getName().length() > 0) { %>
                                <img src="<%=strImgProductDir%><%=image.getThumbName()%>" width="50">
                            <% } %>
                        </td>
                        <td class="<%=cssClass%>"><a href="/admin/Product?action=<%=ProductServlet.ACTION_EDIT_VARIATION%>&productId=<%=product.getId()%>&variationId=<%=variation.getId()%>"><%=variation.getStyle()%></a></td>
                        <td class="<%=cssClass%>"><a href="/admin/Product?action=<%=ProductServlet.ACTION_EDIT_VARIATION%>&productId=<%=product.getId()%>&variationId=<%=variation.getId()%>"><%=variation.getColor()%></a></td>
                        <td class="<%=cssClass%>"><a href="/admin/Product?action=<%=ProductServlet.ACTION_EDIT_VARIATION%>&productId=<%=product.getId()%>&variationId=<%=variation.getId()%>"><%=variation.getSku()%></a></td>
                        <td class="<%=cssClass%>"><%=variation.getPriceCost()%></td>
                        <td class="<%=cssClass%>"><%=variation.getPriceWholesale()%></td>
                        <td class="<%=cssClass%>"><%=variation.getPriceRetail()%></td>
                    </tr>
                    <%
                }
                %>
            </table>
        </td>
    </tr>
</table>

</form>

<%@ include file = "./include/bodyBottom.jsp"%>
