<%@ page import="java.util.ArrayList,
                 com.approachingpi.store.servlet.admin.OrderServlet,
                 com.approachingpi.store.servlet.admin.UserServlet,
                 com.approachingpi.servlet.PiServlet,
                 com.approachingpi.store.Store,
                 com.approachingpi.store.order.*,
                 com.approachingpi.store.servlet.admin.OrderInvoiceServlet,
                 com.approachingpi.store.catalog.Size"%>

<jsp:useBean id="order"    class="com.approachingpi.store.order.Order" scope="request"/>
<jsp:useBean id="sizes"    class="java.util.ArrayList" scope="request"/>
<jsp:useBean id="shiptypes"    class="java.util.ArrayList" scope="request"/>


<%@ include file = "./include/global.jsp"%>
<%
    strPage = "order";
    strPageTitle = "Order Detail";
%>
<%@ include file = "./include/header.jsp"%>
<%@ include file = "./include/bodyTop.jsp"%>

<%
OrderAddress billing = order.getBillAddress();
OrderAddress shipping = order.getShipAddress();
String dateCreated = (order.getDateCreated()!=null) ? timeFormat.format(order.getDateCreated()) : "";
String dateModified = (order.getDateModified()!=null) ? timeFormat.format(order.getDateModified()) : "";
String dateShipBegan = (order.getDateShipBegan()!=null) ? timeFormat.format(order.getDateShipBegan()) : "";
String dateShipComplete = (order.getDateShipComplete()!=null) ? timeFormat.format(order.getDateShipComplete()) : "";

ArrayList payments = order.getPayments();
Payment lastCreditPayment = null;
for (int i=0; i<payments.size(); i++) {
    Payment payment = (Payment)payments.get(i);
    if (payment.getType().equalsIgnoreCase(Payment.TYPE_CC)) {
        lastCreditPayment = payment;
    }
}

String authAccount = "";
int authExpireMonth = 0;
int authExpireYear = 0;

if (lastCreditPayment != null) {
    authAccount = lastCreditPayment.getAccountNumber();
    authExpireMonth = lastCreditPayment.getExpireMonth();
    authExpireYear = lastCreditPayment.getExpireYear();
}
%>

<script language="JavaScript">
var selectedDetails = new Array();

function addPayment() {
    var dialog = document.getElementById("dialogAuth");
    positionDialog(dialog);
    dialog.style.visibility = "visible";
}

function authAmountChanged() {
    captureAmountSubtotal   = parseFloat(document.authForm.captureAmountSubtotal.value);
    captureAmountShipping   =  parseFloat(document.authForm.captureAmountShipping.value);
    captureAmountTax        =  parseFloat(document.authForm.captureAmountTax.value);

    var captureAmountTotal = 0;
    if (captureAmountSubtotal != NaN) {
        captureAmountTotal += captureAmountSubtotal;
    }
    if (captureAmountShipping != NaN) {
        captureAmountTotal += captureAmountShipping;
    }
    if (captureAmountTax != NaN) {
        captureAmountTotal += captureAmountTax;
    }

    var objTotalCell = document.getElementById("authTotalTd");
    objTotalCell.innerHTML = "$" + captureAmountTotal;
}

function captureAmountChanged() {
    captureAmountSubtotal   = parseFloat(document.captureForm.captureAmountSubtotal.value);
    captureAmountShipping   =  parseFloat(document.captureForm.captureAmountShipping.value);
    captureAmountTax        =  parseFloat(document.captureForm.captureAmountTax.value);

    var captureAmountTotal = 0;
    if (captureAmountSubtotal != NaN) {
        captureAmountTotal += captureAmountSubtotal;
    }
    if (captureAmountShipping != NaN) {
        captureAmountTotal += captureAmountShipping;
    }
    if (captureAmountTax != NaN) {
        captureAmountTotal += captureAmountTax;
    }

    var objTotalCell = document.getElementById("captureTotalTd");
    objTotalCell.innerHTML = "$" + captureAmountTotal;
}

function capturePayment(paymentId, paymentType, amount) {

    document.captureForm.paymentId.value=paymentId;

    var objCaptureWindow = document.getElementById("dialogCapture");

    positionDialog(objCaptureWindow);
    objCaptureWindow.style.visibility = "visible";
}


function selectDetail(id) {
    selectedDetail = id;
    mouseOverDetail(id);
}

function unSelectVariation() {
    if (selectedVariation > -1) {
        id = selectedVariation;
        selectedVariation = -1;
        mouseOutVariation(id);
    }
}

document.onmousemove = dragMousemove;
</script>

<table width="90%">
    <tr>
        <td style="vertical-align:top">
            <table>
                <tr>
                    <td class="inputLabel">Order Id</td>
                    <td><b><%=order.getId()%></b></td>
                </tr>
                <tr>
                    <td class="inputLabel">Customer</td>
                    <td><a href="/User?action=<%=UserServlet.ACTION_EDIT%>&userId=<%=order.getUser().getId()%>"><%=order.getUser().getEmail()%></a></td>
                </tr>
                <tr>
                    <td class="inputLabel">Date</td>
                    <td><%=dateCreated%></td>
                </tr>
                <tr>
                    <td class="inputLabel">Modified</td>
                    <td><%=dateModified%></td>
                </tr>
                <tr>
                    <td class="inputLabel">Ship Began</td>
                    <td><%=dateShipBegan%></td>
                </tr>
                <tr>
                    <td class="inputLabel">Ship Complete</td>
                    <td><%=dateShipComplete%></td>
                </tr>
                <tr>
                    <td class="inputLabel">Status</td>
                    <td>
                        <form method="post" action="Order" name="mainForm">
                        <input type="hidden" name="action" value="<%=OrderServlet.ACTION_SAVE%>">
                        <input type="hidden" name="formSubmitted" value="true">
                        <input type="hidden" name="orderId" value="<%=order.getId()%>">
                        <select name="statusId">
                            <%
                            int[] statusTypes = Order.STATUS_TYPES;
                            for(int i=0; i<statusTypes.length; i++) {
                                String selectedVal = (statusTypes[i] == order.getStatus()) ? " selected" : "";
                                %><option value="<%=statusTypes[i]%>"<%=selectedVal%>><%=Order.getStatusById(statusTypes[i])%></option>
                                <%
                            }
                            %>
                        </select>
                        <input type="submit" name="button_submit" value="save">
                        </form>
                    </td>
                </tr>
                <tr>
                    <td colspan="2">
                        <a href="OrderInvoice?orderId=<%=order.getId()%>" target="_blank">invoice</a><br/>
                        <a href="OrderInvoice?orderId=<%=order.getId()%>&type=<%=OrderInvoiceServlet.TYPE_RECEIPT%>" target="_blank">receipt</a><br/>
                        <a href="OrderInvoice?orderId=<%=order.getId()%>&type=<%=OrderInvoiceServlet.TYPE_GIFT_RECEIPT%>" target="_blank">gift receipt</a>
                    </td>
                </tr>
            </table>
        </td>
        <td style="vertical-align:top">
            <table>
                <tr>
                    <td></td>
                    <td style="text-align:left;">billed</td>
                    <td>captured</td>
                </tr>

                <% String cssClass = "matrixRow"; %>
                <% cssClass = (cssClass.equals("matrixRow")) ? "matrixRowAlternate" : "matrixRow"; %>
                <tr>
                    <td class="inputLabel">Subtotal</td>
                    <td class="<%=cssClass%>" style="text-align:right;"><%=dollarFormat.format(order.getAmountSubtotal())%></td>
                    <td class="<%=cssClass%>" style="text-align:right;"><%=dollarFormat.format(order.getAmountCapturedSubtotal())%></td>
                </tr>
                <% cssClass = (cssClass.equals("matrixRow")) ? "matrixRowAlternate" : "matrixRow"; %>
                <tr>
                    <td class="inputLabel">Shipping</td>
                    <td class="<%=cssClass%>" style="text-align:right"><%=dollarFormat.format(order.getAmountShipping())%></td>
                    <td class="<%=cssClass%>" style="text-align:right"><%=dollarFormat.format(order.getAmountCapturedShipping())%></td>
                </tr>
                <% cssClass = (cssClass.equals("matrixRow")) ? "matrixRowAlternate" : "matrixRow"; %>
                <tr>
                    <td class="inputLabel">Tax</td>
                    <td class="<%=cssClass%>" style="text-align:right;"><%=dollarFormat.format(order.getAmountTax())%></td>
                    <td class="<%=cssClass%>" style="text-align:right;"><%=dollarFormat.format(order.getAmountCapturedTax())%></td>
                </tr>
                <% cssClass = (cssClass.equals("matrixRow")) ? "matrixRowAlternate" : "matrixRow"; %>
                <tr>
                    <td class="inputLabel">Coupon</td>
                    <td class="<%=cssClass%>" style="text-align:right;">-<%=dollarFormat.format(order.getAmountCouponTotal())%></td>
                    <td class="<%=cssClass%>" style="text-align:right;"></td>
                </tr>
                <% cssClass = (cssClass.equals("matrixRow")) ? "matrixRowAlternate" : "matrixRow"; %>
                <tr>
                    <td class="inputLabel">Total</td>
                    <td class="<%=cssClass%>" style="text-align:right;"><b><%=dollarFormat.format(order.getAmountTotal())%></b></td>
                    <td class="<%=cssClass%>" style="text-align:right;"><%=dollarFormat.format(order.getAmountCapturedTotal())%></td>
                </tr>
            </table>
        </td>
    </tr>
    
    <tr>
        <td class="inputLabel" style="text-align:left;">Bill To</td>
        <td class="inputLabel" style="text-align:left;">Ship To</td>
    </tr>
    
    <tr>
        <td style="vertical-align:top">
            <%=billing.getNameFirst()%>&nbsp;<%=billing.getNameLast()%><br />
            <%=billing.getAddress1()%><br />
            <% if (billing.getAddress2().length() > 0) { %>
                <%=billing.getAddress2()%><br />
            <% } %>
            <%=billing.getCity()%>
            <% if (billing.getState().getAbbrev().length() > 0) { %>
                <%=billing.getState().getAbbrev()%>,&nbsp;
            <% } else { %>
                ,&nbsp;
            <% } %>
            <%=billing.getZip()%><br />
            <%=billing.getCountry().getName()%><br />
            <%=billing.getPhoneNumber()%>
        </td>
        <td style="vertical-align:top">
            <% if (shipping.getId() == 0) { %>
                (same as billing address)
            <% } else { %>
                <%=shipping.getNameFirst()%>&nbsp;<%=shipping.getNameLast()%><br />
                <%=shipping.getAddress1()%><br />
                <% if (shipping.getAddress2().length() > 0) { %>
                    <%=shipping.getAddress2()%><br />
                <% } %>
                <%=shipping.getCity()%>
                <% if (shipping.getState().getAbbrev().length() > 0) { %>
                    <%=shipping.getState().getAbbrev()%>,&nbsp;
                <% } else { %>
                    ,&nbsp;
                <% } %>
                <%=shipping.getZip()%><br />
                <%=shipping.getCountry().getName()%><br />
                <%=shipping.getPhoneNumber()%>
            <% } %>
        </td>
    </tr>
    <%-- spacer row --%>
    <tr>
        <td colspan="2">&nbsp;</td>
    </tr>
    <tr>
        <td colspan="2" class="inputLabel" style="text-align:left;">Comments</td>
    </tr>
    <tr>
        <td colspan="2">
            <%
            ArrayList comments = order.getComments();
            for (int i=0; i<comments.size(); i++) {
                OrderComment thisComment = (OrderComment)comments.get(i);
                %>
                <p>
                <%=thisComment.getBody()%>
                </p>
                <%
            }
            %>
        </td>
    </tr>
    <%-- spacer row --%>
    <tr>
        <td colspan="2">&nbsp;</td>
    </tr>
    <tr>
        <td colspan="2" class="inputLabel" style="text-align:left;">Payments</td>
    </tr>
    <tr>
        <td colspan="2"><a href="javascript:addPayment()" title="Add a new payment"><img src="/img/icons/icon_new_file.gif" alt="new file" border="0" /></a></td>
    </tr>
    <tr>
        <td colspan="2">
            <table width="100%">
                <tr>
                    <td class="matrixHead">Type</td>
                    <td class="matrixHead">Status</td>
                    <td class="matrixHead">Account</td>
                    <td class="matrixHead">AVS</td>
                    <td class="matrixHead">Held</td>
                    <td class="matrixHead">Captured</td>
                    <td class="matrixHead">Returned</td>
                    <td class="matrixHead">Auth</td>
                    <td class="matrixHead">Capture</td>
                    <td class="matrixHead">Return</td>
                    <td class="matrixHead">Action</td>
                </tr>
                <%
                cssClass = "matrixRow";
                for (int i=0; i<payments.size(); i++) {
                    Payment payment = (Payment)payments.get(i);
                    String dateSettled = (payment.getDateSettled()!=null) ? timeFormat.format(payment.getDateSettled()) : "";
                    String dateReturned = (payment.getDateVoided()!=null) ? timeFormat.format(payment.getDateVoided()) : "";
                    cssClass = (cssClass.equals("matrixRow")) ? "matrixRowAlternate" : "matrixRow";

                    PaymentTransaction authTrans = payment.getTransactionOfType(PaymentTransaction.TYPE_HOLD);
                    if (authTrans == null) {
                        authTrans = payment.getTransactionOfType(PaymentTransaction.TYPE_SALE);
                    }

                    PaymentTransaction lastTrans = (PaymentTransaction)payment.getTransactions().get(payment.getTransactions().size() - 1);;
                    String dateAuth = "";
                    if (authTrans != null) {
                        dateAuth = (authTrans.getDateCreated() != null) ? timeFormat.format(authTrans.getDateCreated()) : "";
                    }

                    %>
                    <tr>
                        <td class="<%=cssClass%>"><%=Payment.getTypeName(payment.getType())%></td>
                        <td class="<%=cssClass%>">
                            <% if (lastTrans != null) { %>
                                <%=PaymentTransaction.getTypeById(lastTrans.getType())%>
                            <% } %>
                            &nbsp;
                        </td>
                        <td class="<%=cssClass%>"><%=payment.getAccountNumberTail()%></td>
                        <td class="<%=cssClass%>">
                            <% if (authTrans != null) { %>
                                <%=authTrans.getAvsAddress()%>
                            <% } %>
                            &nbsp;
                        </td>
                        <td class="<%=cssClass%>"><%=payment.getAmountHeld()%></td>
                        <td class="<%=cssClass%>"><%=payment.getAmountSettled()%></td>
                        <td class="<%=cssClass%>"><%=payment.getAmountReturned()%></td>
                        <td class="<%=cssClass%>"><%=dateAuth%></td>
                        <td class="<%=cssClass%>"><%=dateSettled%></td>
                        <td class="<%=cssClass%>"><%=dateReturned%></td>
                        <td class="<%=cssClass%>">
                            <%
                            if (payment.needsCapture()) {
                                %>
                                <a href="javascript:capturePayment(<%=payment.getId()%>, '<%=payment.getType()%>', '<%=payment.getAmountHeld()%>')">capture</a>
                                <%
                            }
                            %>
                        </td>
                    </tr>
                    <%
                }
                %>
            </table>
        </td>
    </tr>

    <tr>
        <td colspan="2">&nbsp;<br/></td>
    </tr>

    <tr>
        <td colspan="2" class="inputLabel" style="text-align:left;">Details</td>
    </tr>
    <% if (!order.getStore().isWholesale()) { %>
        <tr>
            <td colspan="2">
                <form method="post" action="Order" name="mainForm">
                <input type="hidden" name="action" value="<%=OrderServlet.ACTION_SHIP%>">
                <input type="hidden" name="formSubmitted" value="true">
                <input type="hidden" name="orderId" value="<%=order.getId()%>">

                <table width="100%">
                    <tr>
                        <td class="matrixHead">Shipment</td>
                        <td class="matrixHead">SKU</td>
                        <td class="matrixHead">Description</td>
                        <td class="matrixHead">QTY</td>
                        <td class="matrixHead">In Stock</td>
                        <td class="matrixHead">Size</td>
                        <td class="matrixHead">Price</td>
                        <td class="matrixHead">Total</td>
                    </tr>
                    <%
                    ArrayList details = order.getDetails();
                    cssClass = "matrixRow";
                    for (int i=0; i<details.size(); i++)  {
                        OrderDetail detail = (OrderDetail)details.get(i);
                        cssClass = (cssClass.equals("matrixRow")) ? "matrixRowAlternate" : "matrixRow";
                        %>
                        <tr id="detail_<%=i%>" onclick="selectDetail(<%=i%>)">
                            <td class="<%=cssClass%>">
                                <% if (detail.getShipment() == null) { %>
                                    <input type="checkbox" name="lstOrderDetailId" value="<%=detail.getId()%>"/>
                                <% } else { %>
                                    <% if (detail.getShipment().getDate() != null) { %>
                                        <%=sdf.format(detail.getShipment().getDate())%>
                                    <% } %>
                                <% } %>
                                &nbsp;
                            </td>
                            <td class="<%=cssClass%>"><%=detail.getProductVariation().getSku()%></td>
                            <td class="<%=cssClass%>"><%=detail.getDescription()%></td>
                            <td class="<%=cssClass%>"><%=detail.getQty()%></td>
                            <td class="<%=cssClass%>"><%=detail.getQtyInStock()%></td>
                            <td class="<%=cssClass%>"><%=detail.getSizeDesc()%></td>
                            <td class="<%=cssClass%>" style="text-align:right;"><%=dollarFormat.format(detail.getPriceItem())%></td>
                            <td class="<%=cssClass%>" style="text-align:right;"><%=dollarFormat.format(detail.getPriceTotal())%></td>
                        </tr>
                        <%
                    }
                    %>
                </table>
                <br />
                <b>Type:</b>
                <select name="shipTypeId">
                    <option value="0">Ship Method</option>
                    <%
                    for (int i=0; i<shiptypes.size(); i++) {
                        ShipmentType type = (ShipmentType)shiptypes.get(i);
                        %><option value="<%=type.getId()%>"><%=type.getName()%></option>
                        <%
                    }
                    %>
                </select>
                &nbsp;
                <b>Tracking:</b>
                &nbsp;
                <input type="text" size="25" maxlength="30" value=""/>
                <input type="submit" name="button_addshipment" value="Add Shipment"/>
                </form>
            </td>
        </tr>
    <% } else { %>
        <tr>
            <td colspan="2">
                <table width="100%">
                    <tr>
                        <td class="matrixHead"></td>
                        <td class="matrixHead">SKU</td>
                        <td class="matrixHead">Description</td>
                        <%
                        for (int i=0; i<sizes.size(); i++) {
                            Size thisSize = (Size)sizes.get(i);
                            %>
                            <td class="matrixHead"><%=thisSize.getNameShort()%></td>
                            <%
                        }
                        %>
                        <td class="matrixHead">QTY</td>
                        <td class="matrixHead">Price</td>
                        <td class="matrixHead">Total</td>
                    </tr>
                    <%
                    ArrayList detailsGrouped = order.getDetailsGrouped();
                    cssClass = "matrixRow";
                    for (int i=0; i<detailsGrouped.size(); i++)  {
                        OrderDetail detail = (OrderDetail)detailsGrouped.get(i);
                        cssClass = (cssClass.equals("matrixRow")) ? "matrixRowAlternate" : "matrixRow";
                        %>
                        <tr id="detail_<%=i%>" onclick="selectDetail(<%=i%>)">
                            <td class="<%=cssClass%>"><input type="checkbox" name="lstDetails" value="<%=detail.getId()%>"/></td>
                            <td class="<%=cssClass%>"><%=detail.getProductVariation().getSku()%></td>
                            <td class="<%=cssClass%>"><%=detail.getDescription()%></td>
                            <%
                            for (int x=0; x<sizes.size(); x++) {
                                Size thisSize = (Size)sizes.get(x);
                                %>
                                <td class="<%=cssClass%>">
                                    <%
                                    int qty = detail.getQtyForSize(thisSize);
                                    if (qty > 0) {
                                        out.print(qty);
                                    } else {
                                        out.print("&nbsp;");
                                    }
                                    %>
                                </td>
                                <%
                            }
                            %>
                            <td class="<%=cssClass%>"><%=detail.getQty()%></td>
                            <td class="<%=cssClass%>" style="text-align:right;"><%=dollarFormat.format(detail.getPriceItem())%></td>
                            <td class="<%=cssClass%>" style="text-align:right;"><%=dollarFormat.format(detail.getPriceTotal())%></td>
                        </tr>
                        <%
                    }
                    %>
                </table>
            </td>
        </tr>
    <% } %>

</table>

<br/><br/><br/>

<div id="dialogCapture" class="dialog" style="visibility: hidden">
    <iframe class="ieMask" frameBorder="0" scrolling="no"></iframe>
    <div class="border">
        <div class="header" onmousedown="dragGrab('dialogCapture')" onmouseup="dragRelease()">
            <table width="100%" border="0" cellpadding="0" cellspacing="0">
                <tr>
                    <td align="left">&nbsp;<b>Capture Payment</b></td>
                    <td align="right">
                        <a href="javascript:dialogHide('dialogCapture')">X</a>
                    </td>
                </tr>
            </table>
        </div>
        <div class="body">
            <div class="content" id="dialogCaptureContent">
                <form method="post" action="Order" name="captureForm">
                <input type="hidden" name="action" value="<%=OrderServlet.ACTION_CAPTURE%>">
                <input type="hidden" name="orderId" value="<%=order.getId()%>">
                <input type="hidden" name="paymentId" value="">
                <table width="100%">
                    <tr>
                        <td class="inputLabel">Product Amount</td>
                        <td><input type="text" size="6" name="captureAmountSubtotal" value="0.00" onChange="captureAmountChanged()"></td>
                    </tr>
                    <tr>
                        <td class="inputLabel">Shipping Amount</td>
                        <td><input type="text" size="6" name="captureAmountShipping" value="0.00" onChange="captureAmountChanged()"></td>
                    </tr>
                    <tr>
                        <td class="inputLabel">Tax Amount</td>
                        <td><input type="text" size="6" name="captureAmountTax" value="0.00" onChange="captureAmountChanged()"></td>
                    </tr>
                    <tr>
                        <td class="inputLabel">Total</td>
                        <td id="captureTotalTd">$0.00</td>
                    </tr>
                    <tr>
                        <td class="inputLabel">Reference #</td>
                        <td><input type="text" size="10" maxlength="20" name="pnref" value=""></td>
                    </tr>
                    <tr>
                        <td class="inputLabel">Comment</td>
                        <td>
                            <textarea name="captureComment" rows="3" cols="30"></textarea>
                        </td>
                    </tr>
                    <tr>
                        <td colspan="2" align="center">
                            <input type="submit" name="button_submit" value="capture">
                        </td>
                    </tr>
                </table>
                </form>
            </div>
        </div>
    </div>
</div>

<div id="dialogAuth" class="dialog" style="visibility: hidden">
    <iframe class="ieMask" frameBorder="0" scrolling="no"></iframe>
    <div class="border">
        <div class="header" onmousedown="dragGrab('dialogAuth')" onmouseup="dragRelease()">
            <table width="100%" border="0" cellpadding="0" cellspacing="0">
                <tr>
                    <td align="left">&nbsp;<b>Create New Payment</b></td>
                    <td align="right">
                        <a href="javascript:dialogHide('dialogAuth')">X</a>
                    </td>
                </tr>
            </table>
        </div>
        <div class="body">
            <div class="content" id="dialogCaptureContent">
                <form method="post" action="Order" name="authForm">
                <input type="hidden" name="action" value="<%=OrderServlet.ACTION_SALE%>">
                <input type="hidden" name="orderId" value="<%=order.getId()%>">
                <input type="hidden" name="type" value="<%=Payment.TYPE_CC%>">
                <table width="100%">
                    <tr>
                        <td class="inputLabel">Product Amount</td>
                        <td><input type="text" size="6" name="captureAmountSubtotal" value="0.00" onChange="authAmountChanged()"></td>
                    </tr>
                    <tr>
                        <td class="inputLabel">Shipping Amount</td>
                        <td><input type="text" size="6" name="captureAmountShipping" value="0.00" onChange="authAmountChanged()"></td>
                    </tr>
                    <tr>
                        <td class="inputLabel">Tax Amount</td>
                        <td><input type="text" size="6" name="captureAmountTax" value="0.00" onChange="authAmountChanged()"></td>
                    </tr>
                    <tr>
                        <td class="inputLabel">Total</td>
                        <td id="authTotalTd">$0.00</td>
                    </tr>
                    <tr>
                        <td class="inputLabel">Account Number</td>
                        <td><input type="text" size="20" name="accountNumber" value="<%=authAccount%>"></td>
                    </tr>
                    <tr>
                        <td class="inputLabel" id="l_expiration">Card Expiration</td>
                        <td>
                            <select name="expireMonth">
                                <option value="">Month</option>
                                <%
                                SimpleDateFormat monthFormat = new SimpleDateFormat("[MM] MMMM");
                                SimpleDateFormat monthValueFormat = new SimpleDateFormat("MM");
                                GregorianCalendar cal = new GregorianCalendar();
                                cal.set(Calendar.DAY_OF_MONTH,cal.getMinimum(Calendar.DAY_OF_MONTH));
                                for (int month=cal.getMinimum(Calendar.MONTH); month<=cal.getMaximum(Calendar.MONTH); month++) {
                                    cal.set(Calendar.MONTH, month);
                                    Date monthDate = cal.getTime();
                                    String value = monthValueFormat.format(monthDate);
                                    String text = monthFormat.format(monthDate);
                                    String selectedVal = (month == authExpireMonth) ? " selected" : "";
                                    %><option value="<%=value%>"<%=selectedVal%>><%=text%></option>
                                    <%
                                }
                                %>
                            </select>
                            <select name="expireYear">
                                <option value="">Year</option>
                                <%
                                int maxYear = cal.get(Calendar.YEAR) + 10;
                                for (int year=cal.get(Calendar.YEAR); year<maxYear; year++) {
                                    String selectedVal = (year==authExpireYear) ? " selected" : "";
                                    %><option value="<%=year%>"<%=selectedVal%>><%=year%>
                                    <%
                                }
                                %>
                            </select>
                        </td>
                    </tr>

                    <tr>
                        <td colspan="2" align="center">
                            <input type="submit" name="button_submit" value="charge card">
                        </td>
                    </tr>
                </table>
                </form>
            </div>
        </div>
    </div>
</div>

<%@ include file = "./include/bodyBottom.jsp"%>



    
