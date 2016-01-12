<%@ page import="java.sql.PreparedStatement, 
    java.sql.ResultSet" %>

<jsp:useBean id="content"   class="com.approachingpi.store.site.Content" scope="request"/>

<%@ include file = "./jsp/include/global.jsp"%>
<%
    strPage = "home";
    strPageTitle = "home";
    strOnLoad = "init()";
%>
<%@ include file = "./jsp/include/header.jsp"%>

<script language="javascript">
var arrayVendor = new Array();
var arrayProduct = new Array();
var arrayVariation = new Array();
<%
PreparedStatement ps = con.prepareStatement("SELECT * FROM tbRawMaterialVendor ORDER BY vcName");
ResultSet rs = ps.executeQuery();
while (rs.next()) {
    %>
    arrayVendor[arrayVendor.length] = new Array();
    arrayVendor[arrayVendor.length-1][0] = <%=rs.getInt("inId")%>;
    arrayVendor[arrayVendor.length-1][1] = "<%=rs.getString("vcName")%>";
    <%
} 

ps = con.prepareStatement("SELECT * FROM tbRawMaterial ORDER BY vcName");
rs = ps.executeQuery();
while (rs.next()) {
    %>
    arrayProduct[arrayProduct.length] = new Array();
    arrayProduct[arrayProduct.length-1][0] = <%=rs.getInt("inId")%>;
    arrayProduct[arrayProduct.length-1][1] = <%=rs.getInt("inRawMaterialVendorId")%>;
    arrayProduct[arrayProduct.length-1][2] = "<%=rs.getString("vcSku")%>";
    arrayProduct[arrayProduct.length-1][3] = "<%=rs.getString("vcName")%>";
    <%
} 

ps = con.prepareStatement("SELECT * FROM tbRawMaterialVariation ORDER BY inRawMaterialId, vcColor, vcSizes");
rs = ps.executeQuery();
while (rs.next()) {
    %>
    arrayVariation[arrayVariation.length] = new Array();
    arrayVariation[arrayVariation.length-1][0] = <%=rs.getInt("inId")%>;
    arrayVariation[arrayVariation.length-1][1] = <%=rs.getInt("inRawMaterialId")%>;
    arrayVariation[arrayVariation.length-1][2] = "<%=rs.getString("vcColor")%>";
    arrayVariation[arrayVariation.length-1][3] = "<%=rs.getString("vcSizes")%>";
    arrayVariation[arrayVariation.length-1][4] = <%=rs.getInt("inDozPerCase")%>;
    arrayVariation[arrayVariation.length-1][5] = <%=rs.getFloat("moPriceCase")%>;
    arrayVariation[arrayVariation.length-1][6] = <%=rs.getFloat("moPriceDozen")%>;
    arrayVariation[arrayVariation.length-1][7] = <%=rs.getFloat("moPricePiece")%>;
    <%
} 
%>

var arrayCurve = new Array();

arrayCurve[0] = new Array();
arrayCurve[0][0] = 1;
arrayCurve[0][1] = 10;
arrayCurve[0][2] = 10;
arrayCurve[1] = new Array();
arrayCurve[1][0] = 3;
arrayCurve[1][1] = 8;
arrayCurve[1][2] = 8;
arrayCurve[2] = new Array();
arrayCurve[2][0] = 6;
arrayCurve[2][1] = 7;
arrayCurve[2][2] = 7;
arrayCurve[3] = new Array();
arrayCurve[3][0] = 12;
arrayCurve[3][1] = 3.5;
arrayCurve[3][2] = 3;
arrayCurve[4] = new Array();
arrayCurve[4][0] = 24;
arrayCurve[4][1] = 3;
arrayCurve[4][2] = 2.5;
arrayCurve[5] = new Array();
arrayCurve[5][0] = 48;
arrayCurve[5][1] = 2.5;
arrayCurve[5][2] = 2;
arrayCurve[6] = new Array();
arrayCurve[6][0] = 100;
arrayCurve[6][1] = 2;
arrayCurve[6][2] = 1;
arrayCurve[7] = new Array();
arrayCurve[7][0] = 200;
arrayCurve[7][1] = 1.5;
arrayCurve[7][2] = .5;

var theForm;

function init() {
    theForm = document.mainForm;
    for (var i=0; i<arrayVendor.length; i++) {
        var newOption = document.createElement("OPTION");
        theForm.vendor.options.add(newOption);
        newOption.value = arrayVendor[i][0];
        newOption.text = arrayVendor[i][1];
    }
}

function vendorChangeEvent() {
    theForm.product.length = 1; 
    theForm.variation.length = 1;
    
    theForm.product.disabled = false;
    theForm.variation.disabled = true;
    theForm.colors.disabled = true;
    
    var vendorId = theForm.vendor.options[theForm.vendor.selectedIndex].value;
    
    for (var i=0; i<arrayProduct.length; i++) {
        if (arrayProduct[i][1] == vendorId) {
            var newOption = document.createElement("OPTION");
            theForm.product.options.add(newOption);
            newOption.value = arrayProduct[i][0];
            newOption.text = arrayProduct[i][2] + "-" + arrayProduct[i][3];
        }
    }
    if (theForm.product.length == 2) {
        theForm.product.selectedIndex = 1;
        productChangeEvent();
    }
}

function productChangeEvent() {
    theForm.variation.length = 1;

    theForm.product.disabled = false;
    theForm.variation.disabled = false;
    theForm.colors.disabled = true;

    var productId = theForm.product.options[theForm.product.selectedIndex].value;
    
    for (var i=0; i<arrayVariation.length; i++) {
        if (arrayVariation[i][1] == productId) { 
            var newOption = document.createElement("OPTION");
            theForm.variation.options.add(newOption);
            newOption.value = i;
            newOption.text = arrayVariation[i][2] + "-" + arrayVariation[i][3];
        }
    }
    if (theForm.variation.length == 2) {
        theForm.variation.selectedIndex = 1;
        variationChangeEvent();
    }
}

function variationChangeEvent() {
   theForm.product.disabled = false;
   theForm.variation.disabled = false;
   theForm.colors.disabled = false;

   calculate();
}

function colorsChangeEvent() {
    calculate();
}

function qtyChangeEvent() {
    if (theForm.variation.selectedIndex > 0) {
        calculate();
    }
}

function calculate() {
    var qty = theForm.qty.value;
    var colors = theForm.colors.options[theForm.colors.selectedIndex].value;
    var i = theForm.variation.options[theForm.variation.selectedIndex].value;
    var ps = 0;
    var pp = 0;
    var pv = 0;
    var pc1 = 0;
    var pc2 = 0;
    
    for (var c=0; c<arrayCurve.length;c++) {
        if (arrayCurve[c][0] <= qty) {
            pc1 = arrayCurve[c][1];
            pc2 = arrayCurve[c][2];
        }
    }
    
    if (qty >= (arrayVariation[i][4]*12)) {
        pv = arrayVariation[i][5];
    } else if (qty >= 12) {
        pv = arrayVariation[i][6];
    } else {
        pv = arrayVariation[i][7];
    }
    
    pv = pv*1.33;
    
    if (pv<2 && qty < 25) {
        pv = 2;
    } else if (pv<1 && qty<50) {
        pv = 1;
    } else if (pv<.5 && qty<100) {
        pv = .5;
    }
    
    ps = colors * 25;
    pp = (pc1 * qty) + ((colors-1) *pc2 * qty);
    pvp = (pv*qty)+pp;
    
    total = ps + pvp;
    pps = total / qty;
    
    msg = "product: " + pv + "\n";
    msg = msg + "labor: " + pp + "\n";
    msg = msg + "printing: " + pvp + "\n";
    msg = msg + "setup: " + ps + "\n";
    //alert(msg);
    
    theForm.screenSetup.value = "$" + ps;
    theForm.printing.value = "$" + pvp;
    theForm.total.value = "$" + total;
    theForm.pricePerShirt.value = "$" + pps;
}

</script>

<form name="mainForm">
<table width="100%">
    <tr>
        <td class="pageHead">Custom Printing Estimate: VERY VERY BETA</td>
    </tr>
    <tr>
        <td class="content" height="300">
            <table>
                <tr>
                    <td class="inputLabel">1.</td>
                    <td class="inputLabel">QTY</td>
                    <td><input type="text" name="qty" value="" onChange="qtyChangeEvent()"></td>
                </tr>
                <tr>
                    <td class="inputLabel">2.</td>
                    <td class="inputLabel">Product Type</td>
                    <td>
                        <select name="vendor" onChange="vendorChangeEvent()">
                            <option value=""></option>
                        </select>
                    </td>
                </tr>
                <tr>
                    <td class="inputLabel">3.</td>
                    <td class="inputLabel">Product</td>
                    <td>
                        <select name="product" onChange="productChangeEvent()">
                            <option value=""></option>
                        </select>
                    </td>
                </tr>
                <tr>
                    <td class="inputLabel">4.</td>
                    <td class="inputLabel">Variation</td>
                    <td>
                        <select name="variation" onChange="variationChangeEvent()">
                            <option value=""></option>
                        </select>
                    </td>
                </tr>
                <tr>
                    <td class="inputLabel">5.</td>
                    <td class="inputLabel">Is this a reorder?</td>
                    <td><input type="checkbox" name="reorder" value="1"> (not working)</td>
                </tr>
                <tr>
                    <td class="inputLabel">5.</td>
                    <td class="inputLabel">Ink Colors</td>
                    <td>
                        <select name="colors" onChange="colorsChangeEvent()">
                            <option value="1">1</option>
                            <option value="2">2</option>
                            <option value="3">3</option>
                            <option value="4">4</option>
                        </select>
                    </td>
                </tr>
                <tr>
                    <td></td>
                    <td></td>
                    <td><input type="button" onclick="calculate()" value="calculate"></td>
                </tr>
                <tr>
                    <td colspan="2" class="inputLabel">Screen Setup:</td>
                    <td><input type="text" name="screenSetup" value=""></td>
                </tr>
                <tr>
                    <td colspan="2" class="inputLabel">Printing:</td>
                    <td><input type="text" name="printing" value=""></td>
                </tr>
                <tr>
                    <td colspan="2" class="inputLabel">Total</td>
                    <td><input type="text" name="total" value=""></td>
                </tr>
                <tr>
                    <td colspan="2" class="inputLabel">Total Per Shirt</td>
                    <td><input type="text" name="pricePerShirt" value=""></td>
                </tr>
            </table>
        </td>
    </tr>
</table>
</form>

<%@ include file = "./jsp/include/footer.jsp"%>
