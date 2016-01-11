<%@ page import="com.approachingpi.store.catalog.Image,
                 java.util.ArrayList,
                 java.util.TreeSet,
                 com.approachingpi.store.catalog.*,
                 com.approachingpi.store.servlet.BrandServlet,
                 com.approachingpi.store.servlet.CartServlet"%>
<jsp:useBean id="brand"     class="com.approachingpi.store.catalog.Brand" scope="request"/>
<jsp:useBean id="artist"    class="com.approachingpi.store.catalog.Artist" scope="request"/>
<jsp:useBean id="category"  class="com.approachingpi.store.catalog.Category" scope="request"/>
<jsp:useBean id="variation" class="com.approachingpi.store.catalog.ProductVariation" scope="request"/>
<jsp:useBean id="product"   class="com.approachingpi.store.catalog.Product" scope="request"/>
<jsp:useBean id="se"        class="com.approachingpi.store.catalog.SearchEngine" scope="request"/>
<jsp:useBean id="productList"   class="java.util.ArrayList" scope="request"/>

<%@ include file = "./include/global.jsp"%>
<%
    int action = altAttribute.getInt("action");

    strPage = "product";
    if (action == BrandServlet.ACTION_ALL) { 
        strPageTitle = "All Products :: " + product.getName();
    } else {
        strPageTitle = brand.getName() + " :: " + product.getName();
        
    }
    strMetaDescription = product.getTextDescription();

    String q = "";

    String strTypePath = "brands/";
    if (action == BrandServlet.ACTION_BRAND) {
        strTypePath = "brands/" + brand.getName().replaceAll(" " ,"_") + "/";
        strActivateMenu = "0";
        menuBrand = brand.getName();
    } else if (action == BrandServlet.ACTION_ARTIST) {
        strTypePath = "artists/" + artist.getNameDisplay().replaceAll(" ","_") + "/";
    } else if (action == BrandServlet.ACTION_CATEGORY) {
        strTypePath = "categories/" + category.getId() + "/";
        strActivateMenu = "1";
        menuCategory = category.getId();
    } else if (action == BrandServlet.ACTION_ALL) {
        strTypePath = "allproducts/0/";
        
    } else if (action == BrandServlet.ACTION_SEARCH) {
        strTypePath = "search/0/";
        q = "?x=1";
        if (se.getPriceLow() != null) {
            q = q + "&low=" + se.getPriceLow();
        }
        if (se.getPriceHigh() != null) {
            q = q + "&high=" + se.getPriceHigh();
        }
    }

    ArrayList variations = product.getVariations();
    TreeSet allUsedSizes = new TreeSet();

    strOnLoad="init()";
    
%>
<%@ include file = "./include/header.jsp"%>

<script language="javascript" type="text/javascript">
    var setImageTo = -1;
    var setVariationTo = -1;
    var thumbIndex = 0;
    var arrayImages = new Array();
    var arrayCurrentSizes;
    var arrayAllSizes = new Array();
    var currentImageId = -1;

    var selectedVariation = -1;
    var selectedSize = -1;

    var distroyImageArray = [];


    <%
    ArrayList images = product.getImages();
    boolean foundFirstImage=false;
    for (int i=0; i<images.size(); i++) {
        Image image = (Image)images.get(i);
        %>
        arrayImages[<%=i%>] = new Array();
        arrayImages[<%=i%>][0] = <%=image.getId()%>;
        arrayImages[<%=i%>][1] = new Image();
        arrayImages[<%=i%>][2] = new Image();
        arrayImages[<%=i%>][3] = new Image();

        <% if (!foundFirstImage && variation.getFirstImage().getId() == image.getId()) {
            foundFirstImage=true;
            %>
            //setImageTo = <%=i%>;
            //thumbIndex = <%=i%>;
        <% }
    }
    %>

    var arrayVariations = new Array();

    <%

    for (int i=0; i<variations.size(); i++) {
        ProductVariation thisVar = (ProductVariation)variations.get(i);
        %>
        arrayVariations[<%=i%>] = new Array();
        arrayVariations[<%=i%>][0] = <%=thisVar.getId()%>;
        arrayVariations[<%=i%>][1] = new Array();
        arrayVariations[<%=i%>][2] = <%=thisVar.getFirstImage().getId()%>;
        <%
        ArrayList sizes = thisVar.getSizes();
        for (int x=0; x<sizes.size(); x++) {
            Size size = (Size)sizes.get(x);
            allUsedSizes.add(size);
        %>
            arrayVariations[<%=i%>][1][<%=x%>] = <%=size.getId()%>;
            <%
        }
        if (variation.getId() == thisVar.getId()) { %>
            setVariationTo=<%=i%>;
        <% }
    }

    Iterator it = allUsedSizes.iterator();
    while (it.hasNext()) {
        Size thisSize = (Size)it.next();
        %>
        arrayAllSizes[arrayAllSizes.length] = <%=thisSize.getId()%>;
        <%
    }
    %>

    var imgThumbArrowDown           = new Image();
    var imgThumbArrowUp             = new Image();
    var imgThumbArrowDownDisabled   = new Image();
    var imgThumbArrowUpDisabled     = new Image();

    imgThumbArrowDown.src           = "<%=strImageDir%>thumb_arrow_down.gif"; <%-- if (store.isWholesale()) { out.print("_bl"); } --%>
    imgThumbArrowUp.src             = "<%=strImageDir%>thumb_arrow_up.gif";
    imgThumbArrowDownDisabled.src   = "<%=strImageDir%>thumb_arrow_down_disabled.gif";
    imgThumbArrowUpDisabled.src     = "<%=strImageDir%>thumb_arrow_up_disabled.gif";

    function init() {
        loadImages();
        if (setImageTo >= 0) {
            changeImage(setImageTo);
        }
        if (setVariationTo >= 0) {
            selectVariation(setVariationTo);
        }
        moveThumbDown();
    }

    function changeImage(imageId) {
        currentImageId = imageId;
        var mainImage = document.getElementById("mainImage");
        mainImage.src=arrayImages[imageId][2].src;
        var largeImage = document.getElementById("enlargeImage");
        largeImage.href=arrayImages[imageId][3].src;
        largeImage.title=arrayImages[imageId][3].name;
    }

    function closeMessageWindow() {
        var objMessageWindow = document.getElementById("message_Window");
        objMessageWindow.style.visibility = "hidden";
    }

    function enableSizes() {
        selectedSize = -1;
        for(var i=0; i < arrayAllSizes.length; i++) {
            var sizeCell = document.getElementById("size_" + arrayAllSizes[i]);
            var sizeLink = document.getElementById("sizeLink_" + arrayAllSizes[i]);
            if (sizeCell) {
                if (isInArray(arrayCurrentSizes,arrayAllSizes[i])) {
                    sizeCell.className="size";
                    sizeLink.className="size";
                } else {
                    sizeCell.className="sizeDisabled";
                    sizeLink.className="sizeDisabled";
                }
            }
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


    function loadImages() {
        <%
        
        for (int i=0; i<images.size(); i++) {
            Image image = (Image)images.get(i);
            %>
            arrayImages[<%=i%>][1].src = '/productimages/thumb/<%=image.getName().replaceAll(".jpg","")%>/thumb-<%=image.getName().replaceAll(".jpg","")%>-<%=product.getName().replaceAll("[^A-Za-z0-9]","_")%>-<%=variation.getStyle().replaceAll("[^A-Za-z0-9]","_")%>-<%=variation.getColor().replaceAll("[^A-Za-z0-9]","_")%>.jpg';
            arrayImages[<%=i%>][2].src = '/productimages/standard/<%=image.getName().replaceAll(".jpg","")%>/std-<%=image.getName().replaceAll(".jpg","")%>-<%=product.getName().replaceAll("[^A-Za-z0-9]","_")%>-<%=variation.getStyle().replaceAll("[^A-Za-z0-9]","_")%>-<%=variation.getColor().replaceAll("[^A-Za-z0-9]","_")%>.jpg';
            arrayImages[<%=i%>][3].src = '/productimages/enlarge/<%=image.getName().replaceAll(".jpg","")%>/std-<%=image.getName().replaceAll(".jpg","")%>-<%=product.getName().replaceAll("[^A-Za-z0-9]","_")%>-<%=variation.getStyle().replaceAll("[^A-Za-z0-9]","_")%>-<%=variation.getColor().replaceAll("[^A-Za-z0-9]","_")%>.jpg'; 
            arrayImages[<%=i%>][3].name = "<%=product.getName()%> <%=variation.getStyle()%> <%=variation.getColor()%>"; 
            this.distroyImageArray.push(['/productimages/enlarge/<%=image.getName().replaceAll(".jpg","")%>/std-<%=image.getName().replaceAll(".jpg","")%>-<%=product.getName().replaceAll("[^A-Za-z0-9]","_")%>-<%=variation.getStyle().replaceAll("[^A-Za-z0-9]","_")%>-<%=variation.getColor().replaceAll("[^A-Za-z0-9]","_")%>.jpg', '']);
            <%
        }
        %>
    }

    // for lightbox
    function getDistroyImageArray() {
        return distroyImageArray;
    }

    function mouseOutSize(id) {
        if (id==selectedSize) {
            return;
        }
        var sizeCell = document.getElementById("size_" + id);
        var sizeLink = document.getElementById("sizeLink_" + id);
        if (sizeCell) {
            if (isInArray(arrayCurrentSizes,id)) {
                sizeCell.className="size";
                sizeLink.className="size";
            } else {
                sizeCell.className="sizeDisabled";
                sizeLink.className="sizeDisabled";
            }
        }
    }

    function mouseOverSize(id) {
        var sizeCell = document.getElementById("size_" + id);
        var sizeLink = document.getElementById("sizeLink_" + id);
        if (sizeCell) {
            if (isInArray(arrayCurrentSizes,id)) {
                sizeCell.className="sizeOver";
                sizeLink.className="sizeOver";
            }
        }
    }

    function mouseOverVariation(id) {
        var styleCell = document.getElementById("td_var_" + id);
        var priceCell = document.getElementById("td_varprice_" + id);
        if (styleCell) {
            styleCell.className="matrixRowOver";
        }
        if (priceCell) {
            priceCell.className="matrixRowOver";
        }

        var highlightImageId = arrayVariations[id][2];
        if (highlightImageId > 0) {
            for(var i=0; i < arrayImages.length; i++) {
                if (arrayImages[i][0] == highlightImageId) {
                    var mainImage = document.getElementById("mainImage");
                    mainImage.src=arrayImages[i][2].src;
                    var largeImage = document.getElementById("enlargeImage");
                    largeImage.href=arrayImages[i][3].src;
                    largeImage.title=arrayImages[i][3].name;
                    break;
                }
            }
        }
    }

    function mouseOutVariation(id) {
        if (id != selectedVariation) {
            var styleCell = document.getElementById("td_var_" + id);
            var priceCell = document.getElementById("td_varprice_" + id);
            styleCell.className="matrixRow";
            priceCell.className="matrixRow";

            if (currentImageId >= 0) {
                var mainImage = document.getElementById("mainImage");
                mainImage.src=arrayImages[currentImageId][2].src;
                var largeImage = document.getElementById("enlargeImage");
                largeImage.href=arrayImages[currentImageId][3].src;
                largeImage.title=arrayImages[currentImageId][3].name;
            }
        }
    }

    function moveThumbUp() {
        thumbIndex=thumbIndex-1;
        redrawThumbs();
    }

    function moveThumbDown() {
        thumbIndex=thumbIndex+1;
        if (thumbIndex > arrayImages.length-2) {
            thumbIndex = arrayImages.length - 2;
        }
        redrawThumbs();
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

    function purchase() {
        var error = false;
        var errorText = "";

        // if there is only one variation, select it.
        if (selectedVariation <= -1) {
            if (arrayVariations.length == 1) {
                selectVariation(0);
            }
        }

        if (selectedVariation <= -1 && selectedSize <= -1) {
            error = true;
            errorText = "Please select a variation and size first.";
        } else if (selectedVariation <= -1) {
            error = true;
            errorText = "Please select a variation first.";
        } else if (selectedSize <= -1) {
            error = true;
            errorText = "Please select a size first.";
        }

        if (error) {
            var messageCellText = document.getElementById("message_textTd");
            messageCellText.innerHTML = errorText;
            showMessageWindow();
            return;
        }

        location = "<%=strBaseUrl%>cart?action=<%=CartServlet.ACTION_ADD%>&productVariationId=" + arrayVariations[selectedVariation][0] + "&sizeId=" + selectedSize;
    }

    function redrawThumbs() {
        if (thumbIndex<0) {
            thumbIndex=0;
        }
        var thumb0 = document.getElementById("thumb_0");
        var thumb1 = document.getElementById("thumb_1");

        if (thumb0) {
            thumb0.src=arrayImages[thumbIndex][1].src;
        }
        if (thumb1) {
            thumb1.src=arrayImages[thumbIndex+1][1].src;
        }
        var thumbArrowDown = document.getElementById("thumb_arrow_down");
        var thumbArrowUp = document.getElementById("thumb_arrow_up");
        if (thumbArrowDown) {
            if (thumbIndex+2 == arrayImages.length || arrayImages.length <= 1) {
                thumbArrowDown.src = imgThumbArrowDownDisabled.src;
            } else {
                thumbArrowDown.src = imgThumbArrowDown.src;
            }
        }

        if (thumbArrowUp) {
            if (thumbIndex == 0) {
                thumbArrowUp.src = imgThumbArrowUpDisabled.src;
            } else {
                thumbArrowUp.src = imgThumbArrowUp.src;
            }
        }
        var x;
        var thumbString = "";
        for (x=0; x<arrayImages.length; x++) {
            thumbString += (x==thumbIndex || x==thumbIndex+1) ? '<span class="showThumbStringBold">&bull;</span>' : '&bull;';
        }
        document.getElementById("showThumbString").innerHTML = thumbString;

        // updateImageList();  for lightbox - will this work?
    }

    function selectSize(id) {
        unSelectSize();
        selectedSize = id;
    }

    function selectVariation(id) {
        unSelectVariation();
        selectedVariation = id;
        arrayCurrentSizes = arrayVariations[id][1];
        mouseOverVariation(id);
        enableSizes();
    }

    function showMessageWindow() {
        positionMessageWindow();
        var objMessageWindow = document.getElementById("message_Window");
        objMessageWindow.style.visibility = "visible";
    }

    function thumbClickEvent(thumbId) {
        changeImage(thumbIndex + thumbId);
    }

    function unSelectSize() {
        if (selectedSize > -1) {
            id = selectedSize;
            selectedSize = 0;
            mouseOutSize(id);
        }
    }

    function unSelectVariation() {
        if (selectedVariation > -1) {
            id = selectedVariation;
            selectedVariation = -1;
            mouseOutVariation(id);
        }
    }
</script>

<div class="breadCrumb">
<% if (action == BrandServlet.ACTION_BRAND) { %>
    <a href="<%=strBaseUrl%>brands/<%=brand.getName().replaceAll(" ", "_")%>"><%=brand.getName().toLowerCase()%></a>
    <% if (!product.getName().equals("")) { %>
        / <span class="breadCrumbProduct"><%=product.getName().toLowerCase()%></span>
    <% } %>
    <% if (brand.getName().equals("The Void Above")) { %>
        <br /><img src="<%=voidAboveLogo%>" border="0" height="60" width="260" alt="<%=brand.getName().toUpperCase()%>" align="bottom" hspace="5" vspace="10" />
    <% } %>
<% } else if (action == BrandServlet.ACTION_ARTIST) { %>
    <%=artist.getNameDisplay().toLowerCase()%>
    <% if (!product.getName().equals("")) { %>
    / <span class="breadCrumbProduct"><%=product.getName().toLowerCase()%></span>
    <% } %>
    <% } else if (action == BrandServlet.ACTION_CATEGORY) { 
        ArrayList categories = category.getParents();
        boolean foundFirstCategory=false; 
        for (int i=0; i<categories.size(); i++) {
            Category thisBCCategory = (Category)categories.get(i); %>
            <a href="<%=strBaseUrl%>categories/<%=thisBCCategory.getId()%>"><%=thisBCCategory.getName().toLowerCase()%></a>
            <% if (i>0) { %>
                /
            <% } %>
        <% } %>                           
        <a href="<%=strBaseUrl%>categories/<%=category.getId()%>"><%=category.getName().toLowerCase()%></a>
        <% if (!product.getName().equals("")) { %>
            / <span class="breadCrumbProduct"><%=product.getName().toLowerCase()%></span>
        <% } %>
    <% } %>
</div>

<% if (product.getId() > 0) { %>
    <div class="contentImages">
                <%
                Image mainImage = variation.getFirstImage();
                if (mainImage==null || mainImage.getId() == 0) {
                    mainImage = product.getFirstImage(Image.SQUARE);
                }
                %>
                <a id="enlargeImage" href="/productimages/enlarge/<%=mainImage.getName().replaceAll(".jpg", "")%>/std-<%=product.getName().replaceAll("[^A-Za-z0-9]", "_")%>-<%=variation.getStyle().replaceAll("[^A-Za-z0-9]", "_")%>-<%=variation.getColor().replaceAll("[^A-Za-z0-9]", "_")%>.jpg" rel="lightbox[product]" title="<%=product.getName()%> <%=variation.getStyle()%> <%=variation.getColor()%>"><img id="mainImage" src="/productimages/standard/<%=mainImage.getName().replaceAll(".jpg", "")%>/std-<%=product.getName().replaceAll("[^A-Za-z0-9]", "_")%>-<%=variation.getStyle().replaceAll("[^A-Za-z0-9]", "_")%>-<%=variation.getColor().replaceAll("[^A-Za-z0-9]", "_")%>.jpg" border="0" width="300" height="300" alt="<%=product.getName()%> <%=variation.getStyle()%> <%=variation.getColor()%>" /></a>
                <table cellpadding="0" cellspacing="0" border="0"><tr>
                    <td align="left" valign="middle" style="text-align:left; padding-left:12px; padding-right:12px; padding-top:12px;"><a href="javascript:moveThumbUp()"><img id="thumb_arrow_up" src="<%=strImageDir%>thumb_arrow_up.gif" border="0" alt="<" /></a></td>
                    <%
                    for (int i=0; i<images.size(); i++) {
                        Image image = (Image)images.get(i);
                        %>
                            <td style="padding-left:12px; padding-right:12px; padding-top:12px;">
                                <a href="javascript:thumbClickEvent(<%=i%>)"><img id="thumb_<%=i%>" src="<%=strImgProductDir%>thumb/<%=image.getName()%>" border="0" height="112" width="112" alt="<%=product.getName()%> <%=variation.getStyle()%> <%=variation.getColor()%>" /></a><br />
                                <%-- <a id="enlargeImage" href="/productimages/enlarge/<%=image.getName().replaceAll(".jpg", "")%>/std-<%=product.getName().replaceAll("[^A-Za-z0-9]", "_")%>-<%=variation.getStyle().replaceAll("[^A-Za-z0-9]", "_")%>-<%=variation.getColor().replaceAll("[^A-Za-z0-9]", "_")%>.jpg" rel="lightbox[product]" title="<%=product.getName()%> <%=variation.getStyle()%> <%=variation.getColor()%>"><img id="thumb_<%=i%>" src="<%=strImgProductDir%>thumb/<%=image.getName()%>" border="0" height="112" width="112" alt="<%=product.getName()%> <%=variation.getStyle()%> <%=variation.getColor()%>" /></a><br /> --%>
                            </td>
                        <%
                        if (i==1) {
                            break;
                        }
                    } %>
                        <td align="right" valign="middle" style="text-align:right; padding-left:12px; padding-right:12px; padding-top:12px;"><a href="javascript:moveThumbDown()"><img id="thumb_arrow_down" src="<%=strImageDir%>thumb_arrow_down.gif" border="0" alt=">"></a></td>
                    </tr>
                </table>
                <div id="showThumbString"></div>
            </div>
            <div class="content">
                <span class="contentTextBold"><%=product.getName().toUpperCase()%> - <%=product.getSku().toUpperCase()%></span><br />
                <span class="contentTextBold">Artist - </span><a href="<%=strBaseUrl%>artists/<%=product.getFirstArtist().getNameDisplay().replaceAll(" ","_")%>"><%=product.getFirstArtist().getNameDisplay()%></a><br />
                <%=product.getDescFormatted()%><br /><br />
                <span class="contentTextBold">VARIATIONS:</span><br />
                <table width="100%">
                <%
                for (int i=0; i<variations.size(); i++) {
                    ProductVariation thisVar = (ProductVariation)variations.get(i);
                    %>
                    <tr onClick="selectVariation(<%=i%>)">
                        <td id="td_var_<%=i%>" class="matrixRow" onMouseOver="mouseOverVariation(<%=i%>)" onMouseOut="mouseOutVariation(<%=i%>)">
                            <%=thisVar.getStyle()%><br />
                            <%=thisVar.getColor()%>
                        </td>
                        <td id="td_varprice_<%=i%>" class="matrixRow" style="text-align:right; padding-left: 0px;" onMouseOver="mouseOverVariation(<%=i%>)" onMouseOut="mouseOutVariation(<%=i%>)">
                            <% if (thisVar.getDiscountPercent(store) > 0) { %>
                                <strike><%=dollarFormat.format(thisVar.getPriceRegular(store).doubleValue())%></strike><br />
                                <b><%=dollarFormat.format(thisVar.getPrice(store).doubleValue())%></b><br />
                                <%=thisVar.getDiscountPercent(store)%>% off!
                            <% } else { %>
                                <%=dollarFormat.format(thisVar.getPrice(store).doubleValue())%>
                            <% } %>
                        </td>
                    </tr>
                    <%
                }
                %>
                </table>
                <table>
                    <tr>
                        <td class="contentTextBold">SIZES:&nbsp;</td>
                        <%
                        Iterator iterator = allUsedSizes.iterator();
                        int iCount=0;
                        while (iterator.hasNext()) {
                            Size thisSize = (Size)iterator.next();
                            iCount++;
                            if (iCount > 1) {
                                %><td class="contentTextBold">.</td><%
                            }
                            %><td id="size_<%=thisSize.getId()%>" class="sizeDisabled" onMouseOver="mouseOverSize(<%=thisSize.getId()%>)" onMouseOut="mouseOutSize(<%=thisSize.getId()%>)"><a href="javascript:selectSize(<%=thisSize.getId()%>)"><span class="sizeDisabled" id="sizeLink_<%=thisSize.getId()%>">&nbsp;<%=thisSize.getNameShort()%>&nbsp;</span></a></td>
                            <%
                        }
                        %>
                        <td>
                          <%-- this will have to be javascript - something like this?
                          function sizeChart() {
                            var oChartWindow = window.open("/sizechart?id=" + selectedVariation, "sizeChart", "width=550,height=375,status=yes,resizable=yes,scrollbars=no");
                            oChartWindow.focus();
                          }
                          use arrayVariations[selectedVariation][0] to get variation id
                          JSP: ProductVariation thisVar = (ProductVariation)variations.get(id) to get variation

                          --%>
                          <% 
                          String sizeChart = "mens";
                          if (variation.getStyle().indexOf("Women") > -1) {
                            sizeChart = "womens";
                          } %>
                          (<A HREF="javascript:void(0)" onclick="window.open('/html/<%=sizeChart%>_demo.html','sizeChart','width=550,height=375,resizable=1')">size chart</A>)
                          <%-- demo links commented out for now  (<A HREF="javascript:void(0)" onclick="sizeChart()">size chart</A>) --%>
                           </td>
                    </tr>
                </table>
                <br />
                <br />
                <div style="margin-left:auto; margin-right:auto; text-align:center;">
                    <input type="button" onClick="purchase()" value="add to cart" class="buttonStd">
                </div>
    </div>
<% } %>

    <!--<tr>
        <td colspan="3" class="contentTextBold">CATALOGUE</td>
    </tr> -->

        <div class="catalog">
            <%
            int colCounter=0;
            for (int i=0; i<productList.size(); i++) {
                colCounter++;
                Product thisProduct = (Product)productList.get(i);
                ProductVariation thisVariation = thisProduct.getFirstVariation();
                Image thisImage = thisVariation.getFirstImage();
                if (thisImage.getId() == 0) {
                    thisImage = thisProduct.getFirstImage();
                }

                String thumbImage = "/img/no_image_thumb_vertical.gif";
                
                if (thisImage.getId() != 0) {
                    thumbImage = "/productimages/thumb/" + thisImage.getName().replaceAll(".jpg","") + "/thumb-" + thisImage.getName().replaceAll(".jpg","") + "-" + thisProduct.getName().replaceAll("[^A-Za-z0-9]","_") + "-" + thisVariation.getStyle().replaceAll("[^A-Za-z0-9]","_") + "-" + thisVariation.getColor().replaceAll("[^A-Za-z0-9]","_") + ".jpg";
                }
                %><a href="<%=strBaseUrl%><%=strTypePath%><%=thisProduct.getFirstVariation().getId()%><%=q%>"><img src="<%=thumbImage%>" border="0" alt="<%=thisProduct.getName()%> <%=thisVariation.getStyle()%> <%=thisVariation.getColor()%>" /></a>&nbsp;&nbsp;
                <%
                if (colCounter==5) {
                    out.println("<br />");
                    colCounter=0;
                }
            }
            %>
        </div>

<%@ include file = "./include/footer.jsp"%>

