<%@ page import="com.approachingpi.store.servlet.admin.ProductServlet,
                 com.approachingpi.servlet.PiServlet,
                 com.approachingpi.store.servlet.admin.ImageEditServlet,
                 com.approachingpi.store.servlet.admin.ImageUploadServlet,
                 com.approachingpi.store.servlet.admin.ImageAssociateServlet,
                 java.util.ArrayList,
                 com.approachingpi.store.catalog.*"%>

<jsp:useBean id="product"       class="com.approachingpi.store.catalog.Product" scope="request"/>
<jsp:useBean id="variation"     class="com.approachingpi.store.catalog.ProductVariation" scope="request"/>
<jsp:useBean id="categories"    class="java.util.ArrayList" scope="request"/>
<jsp:useBean id="sizes"         class="java.util.ArrayList" scope="request"/>

<%@ include file = "./include/global.jsp"%>
<%
    strPage = "product";
    strPageTitle = "Product Variation Edit";
    strOnLoad = "init()";

    ArrayList variationSizes = variation.getSizes();

%>
<%@ include file = "./include/header.jsp"%>
<script language="javascript">
    var selectedImage = 0;
    var selectedSizeRow = 0;
    var selectedSizeStyle = "";

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

    var arrayAllCategories = new Array();
    <% for (int i=0; i<categories.size(); i++) {
        Category cat = (Category)categories.get(i);
        %>
        arrayAllCategories[<%=i%>] = new Array();
        arrayAllCategories[<%=i%>][0] = <%=cat.getId()%>;
        arrayAllCategories[<%=i%>][1] = '<%=cat.getPathAsString()%>';
    <% } %>

    var arrayAllSizes = new Array();
    <% for (int i=0; i<sizes.size(); i++) {
        Size size = (Size)sizes.get(i);
        %>
        arrayAllSizes[<%=i%>] = new Array();
        arrayAllSizes[<%=i%>][0] = <%=size.getId()%>;
        arrayAllSizes[<%=i%>][1] = '<%=size.getName()%>';
        arrayAllSizes[<%=i%>][2] = 0;  // qty defaults to 0
        arrayAllSizes[<%=i%>][3] = 0;
        <%
        for (int x=0; x<variationSizes.size(); x++) {
            Size varSize = (Size)variationSizes.get(x);
            if (varSize.getId() == size.getId()) {
                %>arrayAllSizes[<%=i%>][2]=<%=varSize.getQtyInStock()%>;
                <%
                break;
            }
        }
    }
    %>

    var arrayCategories = new Array();
    <%
    ArrayList prodCats = variation.getCategories();
    for (int i=0; i<prodCats.size(); i++) {
        Category cat = (Category)prodCats.get(i);
        %>
        arrayCategories[arrayCategories.length] = <%=cat.getId()%>;
    <% } %>

    var arraySizes = new Array();
    <%
    for (int i=0; i<variationSizes.size(); i++) {
        Size size = (Size)variationSizes.get(i);
        %>arraySizes[arraySizes.length] = <%=size.getId()%>;
        <%
    }
    %>

    function addNewCategory() {
        if (document.mainForm.addCategory.selectedIndex >= 0) {
            var thisCatId = document.mainForm.addCategory.options[document.mainForm.addCategory.selectedIndex].value;
            arrayCategories[arrayCategories.length] = thisCatId;

            drawCategories();
        }
    }

    function addNewSize() {
        if (document.mainForm.addSize.selectedIndex >= 0) {
            var thisSizeId = document.mainForm.addSize.options[document.mainForm.addSize.selectedIndex].value;
            arraySizes[arraySizes.length] = thisSizeId;

            drawSizeTable();
        }
    }

    function clearSizeTable() {
        var sizeTable = document.getElementById("sizeTable").getElementsByTagName("TBODY")[0];
        oNodes = sizeTable.childNodes;

        // remove nodes backwards because the array gets messed up if we go forwards
        for (x = oNodes.length-1; x > -1; x--) {
            if (oNodes[x].nodeName == "TR") {
                thisId = oNodes[x].id;
                if (thisId.indexOf("size_row_") == 0) {
                    oNodes[x].parentNode.removeChild(oNodes[x]);
                }
            }
        }
    }

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

        var url = "/admin/ImageAssociate?imageId=" + selectedImage + "&action=<%=ImageAssociateServlet.ACTION_DELETE%>&productVariationId=<%=variation.getId()%>";
        loadImageBoxUrl(url);

        cancelDelete();
    }

    function disableButtons() {
        var iconEdit = document.getElementById("iconEdit");
        var iconUp = document.getElementById("iconUp");
        var iconDown = document.getElementById("iconDown");
        var iconDelete = document.getElementById("iconDelete");

        iconEdit.src    = imgIconEditDisabled.src;
        iconUp.src      = imgIconUpDisabled.src;
        iconDown.src    = imgIconDownDisabled.src;
        iconDelete.src  = imgIconDeleteDisabled.src;

        selectedImage = 0;
    }

    function drawCategories() {
        document.mainForm.categories.length    = 0;
        document.mainForm.addCategory.length    = 1;
        var catList = "";

        for (var i=0; i<arrayAllCategories.length; i++) {
            if (isInArray(arrayCategories, arrayAllCategories[i][0])) {
                oOption = document.createElement("OPTION");
                oOption.value = arrayAllCategories[i][0];
                oOption.text = arrayAllCategories[i][1];
                try {
                    document.mainForm.categories.add(oOption);
                } catch(ex) {
                    document.mainForm.categories.add(oOption, null);
                }
                catList += "," + arrayAllCategories[i][0];
            } else {
                oOption = document.createElement("OPTION");
                oOption.value = arrayAllCategories[i][0];
                oOption.text = arrayAllCategories[i][1];
                try {
                    document.mainForm.addCategory.add(oOption);
                } catch(ex) {
                    document.mainForm.addCategory.add(oOption, null);
                }
            }

        }
        document.mainForm.catList.value = catList;
   }

    function drawSizeTable() {
        clearSizeTable();

        var sizeDropDownIndex   = 0;
        var sizeList            = "";
        var qtyToAddList        = "";
        var cssClass            = "matrixRow";
        var rowsInSizeTable     = 0;

        document.mainForm.addSize.length    = 1;

        var sizeTable = document.getElementById("sizeTable").getElementsByTagName("TBODY")[0];

        for (var i=0;i<arrayAllSizes.length; i++) {
            if (isInArray(arraySizes, arrayAllSizes[i][0])) {
                rowsInSizeTable++;

                if (rowsInSizeTable > 1) {
                    sizeList += "," + arrayAllSizes[i][0];
                    qtyToAddList += "," + arrayAllSizes[i][3];
                } else {
                    sizeList = arrayAllSizes[i][0];
                    qtyToAddList = arrayAllSizes[i][3];
                }

                if (cssClass == "matrixRow") {
                    cssClass = "matrixRowAlternate";
                } else {
                    cssClass = "matrixRow";
                }

                oCellDelete = document.createElement("TD");
                oCellSize   = document.createElement("TD");
                oCellQty    = document.createElement("TD");
                oCellAdd    = document.createElement("TD");

                oCellDelete.className   = cssClass;
                oCellSize.className     = cssClass;
                oCellQty.className      = cssClass;
                oCellAdd.className      = cssClass;

                oCellDelete.innerHTML   = "<input type=\"checkbox\" name=\"size_delete_" + arrayAllSizes[i][0] + "\" id=\"size_delete_" + arrayAllSizes[i][0] + "\">";
                oCellSize.innerHTML     = arrayAllSizes[i][1];
                oCellQty.innerHTML      = (arrayAllSizes[i][2] + arrayAllSizes[i][3]);
                oCellAdd.innerHTML      = "<input type=\"text\" size=\"3\" name=\"size_add_box_" + arrayAllSizes[i][0] + "\" id=\"size_add_box_" + arrayAllSizes[i][0] + "\">";
                if (arrayAllSizes[i][3] != 0) {
                    oCellQty.style.color="#FF0000";
                }

                oCellDelete.style.textAlign = "center";

                oRow=document.createElement("TR");
                oRow.id="size_row_" + arrayAllSizes[i][0];
                <%--
                oCellSize.onClick = "selectSizeRow(" + arrayAllSizes[i][0] + ")";
                oRow.attachEvent("onClick","selectSizeRow(" + arrayAllSizes[i][0] + ")");
                oRow.addEventListener("onClick", "selectSizeRow(arrayAllSizes[i][0])", false);
                oCellSize.addEventListener("onClick", "selectSizeRow(arrayAllSizes[i][0])", false);
                --%>

                oRow.appendChild(oCellDelete);
                oRow.appendChild(oCellSize);
                oRow.appendChild(oCellQty);
                oRow.appendChild(oCellAdd);

                sizeTable.appendChild(oRow);
            } else {
                oOption = document.createElement("OPTION");
                oOption.value = arrayAllSizes[i][0];
                oOption.text = arrayAllSizes[i][1];
                document.mainForm.addSize.appendChild(oOption);
            }
        }

        // append the bottom row with the update sizes button on it.
        if (rowsInSizeTable > 0) {
            var oCellBottom = document.createElement("TD");
            oCellBottom.colSpan = 4;
            oCellBottom.className = "inputLabel";
            oCellBottom.style.textAlign="center";
            oCellBottom.innerHTML = "<input type=\"button\" name=\"button_update_size\" onClick=\"updateSizes()\" value=\"update sizes\">";
            var oRowBottom=document.createElement("TR");
            oRowBottom.id="size_row_bottom";
            oRowBottom.appendChild(oCellBottom);
            sizeTable.appendChild(oRowBottom);
        }

        document.mainForm.sizeList.value = sizeList;
        document.mainForm.qtyToAdd.value = qtyToAddList;
    }

    function disableImgButtons() {
        var iconLeft    = document.getElementById("iconLeft");
        var iconRight   = document.getElementById("iconRight");
        var iconDelete  = document.getElementById("iconDelete");

        iconLeft.src    = imgIconLeftDisabled.src;
        iconRight.src   = imgIconRightDisabled.src;
        iconDelete.src  = imgIconDeleteDisabled.src;

        selectedImage = 0;
    }

    function enableImgButtons(imageId) {

        if (selectedImage == 0) {
            var iconLeft    = document.getElementById("iconLeft");
            var iconRight   = document.getElementById("iconRight");
            var iconDelete  = document.getElementById("iconDelete");

            iconLeft.src    = imgIconLeft.src;
            iconRight.src   = imgIconRight.src;
            iconDelete.src  = imgIconDelete.src;
        }
        selectedImage = imageId;
    }

    function findSubArray(arrayIn, subIndex, searchIn) {
        for (var i=0; i<arrayIn.length; i++) {
            if (arrayIn[i][subIndex] == searchIn) {
                return arrayIn[i];
            }
        }
        return;
    }

    function init() {
        drawSizeTable();
        drawCategories();
    }

    function isInArray(arrayIn, searchIn) {
        for (var i=0; i<arrayIn.length; i++) {
            if (arrayIn[i] == searchIn) {
                return true;
            }
        }
        return false;
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

    function moveImageDown() {
        if (selectedImage == 0) {
            return;
        }
        var url = "/admin/ImageAssociate?action=<%=ImageAssociateServlet.ACTION_MOVE_DOWN%>&productVariationId=<%=variation.getId()%>&imageId=" + selectedImage;
        loadImageBoxUrl(url);
    }

    function moveImageUp() {
        if (selectedImage == 0) {
            return;
        }
        var url = "/admin/ImageAssociate?action=<%=ImageAssociateServlet.ACTION_MOVE_UP%>&productVariationId=<%=variation.getId()%>&imageId=" + selectedImage;
        loadImageBoxUrl(url);
    }

    function refreshImages() {
        var url = "/admin/ImageEdit?action=<%=ImageEditServlet.ACTION_MAIN%>&productId=<%=product.getId()%>";
        loadImageBoxUrl(url);
    }

    function removeCategory() {
        if (document.mainForm.categories.selectedIndex >= 0) {
            var thisCatId = document.mainForm.categories.options[document.mainForm.categories.selectedIndex].value;

            var newArray = new Array();
            for (var i=0;i<arrayCategories.length; i++) {
                if (arrayCategories[i] != thisCatId) {
                    newArray[newArray.length] = arrayCategories[i];
                }
            }
            arrayCategories = newArray;
            drawCategories();
        }
    }

    // i can't get the onclick to work so this may never be used
    function selectSizeRow(rowIn) {
        if (selectedSizeRow > 0) {
            var row = document.getElementById("size_row_" + selectedSizeRow);
            if (row) {
                row.className="selectedSizeRowStyle";
                selectedSizeStyle = "";
            }
        }
        var selectedRow = document.getElementById("size_row_" + rowIn);
        if (selectedRow) {
            selectedSizeRow = rowIn;
            selectedSizeStyle = selectedSizeRow.className;
            selectedRow.className="matrixRowSelected";
        }
    }

    function addNewImage(imageId) {
        var url = "/admin/ImageAssociate?action=<%=ImageAssociateServlet.ACTION_ADD%>&productId=<%=product.getId()%>&productVariationId=<%=variation.getId()%>&imageId=" + imageId;
        loadImageBoxUrl(url);
    }

    function updateSizes() {
        for (var i=arraySizes.length-1; i>=0; i--) {
            var sizeDelete = document.getElementById("size_delete_" + arraySizes[i]);
            if (sizeDelete) {
                if (sizeDelete.checked) {
                    var sizeRow = document.getElementById("size_row_" + arraySizes[i]);
                    sizeRow.parentNode.removeChild(sizeRow);
                    arraySizes[i] = 0;
                }
            }

            var addBox = document.getElementById("size_add_box_" + arraySizes[i]);
            if (addBox) {
                var addAmount = parseInt(addBox.value);
                if (!isNaN(addAmount)) {
                    for (var x=0;x<arrayAllSizes.length; x++) {
                        if (arrayAllSizes[x][0] == arraySizes[i]) {
                            arrayAllSizes[x][3] = arrayAllSizes[x][3] + addAmount;
                            //if (arrayAllSizes[x][3] < arrayAllSizes[x][2]) {
                            //    arrayAllSizes[x][3] = 0;
                            //}
                            //if (arrayAllSizes[x][3] < 0) {
                            //    arrayAllSizes[x][3] = 0;
                            //}
                            break;
                        }
                    }
                }
            }
        }
        drawSizeTable();
    }

    function associateImage() {
        if (<%=variation.getId()%> > 0) {
            var oAssociateWindow = window.open("/admin/ImageAssociate?action=<%=ImageAssociateServlet.ACTION_SHOW_AVAILABLE%>&productId=<%=product.getId()%>&productVariationId=<%=variation.getId()%>", "imageUpload", "width=400,height=400,status=yes,resizable=yes,scrollbars=yes");
            oAssociateWindow.focus();
        }
    }

</script>

<%@ include file = "./include/bodyTop.jsp"%>

<table>
    <tr>
        <td><a href="/admin/Product?action=<%=ProductServlet.ACTION_LIST%>">search</a></td>
        <td><a href="/admin/Product?action=<%=ProductServlet.ACTION_EDIT%>&productId=<%=product.getId()%>">Product</a></td>
        <td><b>Variation</b></td>
    </tr>
</table>

<form method="post" action="/admin/Product" name="mainForm">
<input type="hidden" name="action" value="<%=ProductServlet.ACTION_EDIT_VARIATION%>">
<input type="hidden" name="productId" value="<%=product.getId()%>">
<input type="hidden" name="variationId" value="<%=variation.getId()%>">
<input type="hidden" name="sizeList" value="">
<input type="hidden" name="qtyToAdd" value="">
<input type="hidden" name="catList" value="">
<input type="hidden" name="formSubmitted" value="true">

<table>
    <tr>
        <td class="inputLabel">Id</td>
        <td><% if (variation.getId() > 0) {
                %><%=variation.getId()%><%
        } else {
                %>new<%
        }
            %>
        </td>
        <td class="inputLabel">Product Id</td>
        <td><%=product.getId()%></td>
        <td>
            <table width="100%">
                <tr>
                    <td class="inputLabel" style="text-align:left;">Images</td>
                    <td align="right" style="background-color: #EEEEEE;">
                        <a href="javascript:associateImage()" title="Upload an image."><img id="iconNew" src="<%=strImgAdminDir%>icons/icon_new_file<%if(product.getId()==0){out.write("_disabled");}%>.gif" height="20" width="20" border="0" alt="Associate an image."></a>
                        <a href="javascript:moveImageUp()" title="Move image up."><img id="iconLeft" src="<%=strImgAdminDir%>icons/icon_arrow_left_disabled.gif" height="20" width="20" border="0" alt="Move image up."></a>
                        <a href="javascript:moveImageDown()" title="Move image down."><img id="iconRight" src="<%=strImgAdminDir%>icons/icon_arrow_right_disabled.gif" height="20" width="20" border="0" alt="Move image down."></a>
                        <a href="javascript:confirmDeleteImg()" title="Delete image."><img id="iconDelete" src="<%=strImgAdminDir%>icons/icon_file_delete_disabled.gif" height="20" width="20" border="0" alt="Delete image."></a>
                    </td>
                </tr>
            </table>
        </td>
    </tr>

    <tr>
        <td class="inputLabel">Name</td>
        <td><%=product.getName()%></td>
        <td class="inputLabel">Product SKU</td>
        <td><%=product.getSku()%></td>
        <td rowspan="12" valign="top">
            <iframe id="imageBox" src="/admin/ImageAssociate?action=<%=ImageAssociateServlet.ACTION_MAIN%>&productVariationId=<%=variation.getId()%>" style="border-style:solid; border-width:1px; height:175px;"></iframe>
        </td>
    </tr>
    <tr>
        <td class="inputLabel">Brand</td>
        <td><%=product.getBrand().getName()%></td>
        <td class="inputLabel">Artist</td>
        <td>
            <%
                Artist artist = new Artist();
                // we only have one artist per design right now, so grab the first one
                if (product.getArtists().size() > 0) {
                    artist = (Artist)product.getArtists().get(0);
                    out.write(artist.getNameLast() + ", " + artist.getNameFirst());
                }
            %>
        </td>
    </tr>
    <tr>
        <td class="inputLabel">Style</td>
        <td><input type="text" name="style" value="<%=variation.getStyle()%>"></td>
    </tr>
    <tr>
        <td class="inputLabel">Color</td>
        <td><input type="text" name="color" value="<%=variation.getColor()%>"></td>
    </tr>
    <%--
    <tr>
        <td class="inputLabel">Description</td>
        <td><input type="text" name="desc" value="<%=variation.getDesc()%>"></td>
    </tr>
    --%>
    <tr>
        <td class="inputLabel">SKU</td>
        <td><input type="text" name="sku" value="<%=variation.getSku()%>"></td>
    </tr>
    <tr>
        <td class="inputLabel">Active</td>
        <td><input type="checkbox" name="active" value="true"<% if (variation.getActive()) { out.write(" checked"); } %>></td>
    </tr>
    <tr>

        <td class="inputLabel">Cost</td>
        <td><input type="text" name="priceCost" value="<%=variation.getPriceCost()%>"></td>
    </tr>
    <tr>
        <td class="inputLabel">Wholesale</td>
        <td><input type="text" name="priceWholesale" value="<%=variation.getPriceWholesale()%>"></td>
    </tr>
    <tr>
        <td class="inputLabel">Retail</td>
        <td><input type="text" name="priceRetail" value="<%=variation.getPriceRetail()%>"></td>
    </tr>
    <tr>
        <td class="inputLabel">On Sale</td>
        <td><input type="checkbox" name="sale" value="true"<% if (variation.isSale()) { out.write(" checked"); } %>></td>
    </tr>
    <tr>
        <td class="inputLabel">Wholesale Sale</td>
        <td><input type="text" name="priceWholesaleSale" value="<%=variation.getPriceWholesaleSale()%>"></td>
    </tr>
    <tr>
        <td class="inputLabel">Retail Sale</td>
        <td><input type="text" name="priceRetailSale" value="<%=variation.getPriceRetailSale()%>"></td>
    </tr>
    <tr>
        <td class="inputLabel">Categories</td>
        <td>
            <select name="addCategory"><option value="0">None Selected</option></select>
            <input type="button" name="button_addCategory" value="add" onClick="addNewCategory()"><br>
            <select name="categories" size="5"></select>
            <input type="button" name="button_removeCategory" value="remove" onClick="removeCategory()">
        </td>
    </tr>

    <tr>
        <td class="inputLabel">Sizes</td>
        <td>
            <select name="addSize">
            <option value="0">None Selected</option>
            <% for (int i=0; i<sizes.size(); i++) {
                Size size = (Size)sizes.get(i);
                %><option value="<%=size.getId()%>"><%=size.getName()%></option><%
            } %>
            </select>
            <input type="button" name="button_addSize" value="add" onClick="addNewSize()">
        </td>
    </tr>
    <tr>
        <td colspan="2" align="right">
            <table width="100%" id="sizeTable">
                <tr>
                    <td class="matrixHead" style="text-align:center;">X</td>
                    <td class="matrixHead">Size</td>
                    <td class="matrixHead">Qty</td>
                    <td class="matrixHead">Add</td>
                </tr>
            </table>
        </td>
    </tr>

    <tr>
        <td colspan="2" align="center" class="inputLabel">&nbsp;</td>
    </tr>

    <tr>
        <td colspan="2" align="center">
            <input type="submit" name="button_submit" value="save">
            <p>
            <input type="submit" name="button_submit_new" value="save and new">
            <p>
            <input type="submit" name="button_submit_return" value="save and return">
        </td>
    </tr>
</table>

</form>
<%@ include file = "./include/bodyBottom.jsp"%>

