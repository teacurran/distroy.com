<%@ page import="java.util.ArrayList,
                 com.approachingpi.store.servlet.*,
                 com.approachingpi.servlet.*,
                 com.approachingpi.user.*"%>

<jsp:useBean id="billing"       class="com.approachingpi.user.Address" scope="request"/>
<jsp:useBean id="shipping"      class="com.approachingpi.user.Address" scope="request"/>
<jsp:useBean id="theUser"      class="com.approachingpi.user.User" scope="request"/>

<%@ include file = "../include/global.jsp"%>
<%
strPageTitle = "Account Information";
boShowNav = false;
boShowFoot = false;
%>
<%@ include file = "../include/header.jsp"%>
<div class="contentText">
  <div class="breadCrumb"><a href="<%=strBaseUrl%>">home</a> / <a href="cart?action=<%=CartServlet.ACTION_VIEW%>">shopping cart</a> / <span class="breadCrumbProduct">account information</span></div>
<br /><br />

<%
ArrayList countries = Country.getAll(con,true);
ArrayList states = State.getAll(con,true);

%>

<script language="javascript">
    function goBack() {
        location = "cart?action=<%=CartServlet.ACTION_VIEW%>";
    }
    function goNext() {
        // any pre validation goes here
        document.mainForm.submit();
    }

    function forgotPassword() {
        var email = document.loginForm.email.value;
        var oPasswordWindow = window.open("/forgotpassword?firstTime=true&amp;email=" + email, "forgotPassword", "width=550,height=200,status=yes,resizable=yes,scrollbars=yes");
        oPasswordWindow.focus();
        return;
    }    
</script>

<!-- <div class="pageHead">Account Information</div> -->
<form method="post" action="login" name="loginForm">
<input type="hidden" name="action" value="<%=LoginServlet.ACTION_LOGIN%>">
<input type="hidden" name="loginForm" value="/cart?action=<%=CartServlet.ACTION_BILLSHIP%>">
<input type="hidden" name="loginReturn" value="/cart?action=<%=CartServlet.ACTION_CONFIRM%>">
<span class="contentTextBold">Please fill out the form below and click "NEXT" when you are done.</span><br /><br />

                    <% if (theUser.getId() == 0) { %>
                            <table border="0" width="50%%">
                                <tr>
                                    <td colspan="2">
                                        If you are already registered, login here.  toughguy.<br /><br />
                                    </td>
                                </tr>
                                <tr>
                                    <td class="inputLabel" id="l_email">Email Address</td>
                                    <td><input type="text" class="text" name="email" size="20" maxlength="200" value="<%=PiServlet.getReqString(request,"email")%>"></td>
                                </tr>
                                <tr>
                                    <td class="inputLabel" id="l_password">Password</td>
                                    <td><input type="password" class="text" size="20" maxlength="30" name="password" value="">
                                </tr>
                                <tr>
                                    <td></td>
                                    <td><input type="submit" class="button" value="LOGIN">&nbsp;&nbsp;&nbsp; <a href="javascript:forgotPassword()">forgot your password?</a></td>
                                </tr>
                            </table>
                    <% } %>
</form>
<br /><br />

<form method="post" action="cart" name="mainForm">
<input type="hidden" name="action" value="<%=CartServlet.ACTION_CONFIRM%>">
<input type="hidden" name="fromAction" value="<%=CartServlet.ACTION_BILLSHIP%>">

<table border="0" width="100%">
    <tr>
    <td class="matrixHead">BILLING INFORMATION</td>
    <td class="sectionSpace" width="10">&nbsp;</td>
    <td class="matrixHead">SHIPPING INFORMATION</td>
    </tr>
    <tr>
        <td width="45%" class="matrix">
                        This billing address must match your credit card billing address.<br />
                        <br />
                        <table border="0" cellpadding="5" cellspacing="5">
                            <tr>
                                <td class="inputLabel" id="l_b_nameFirst">First Name</td>
                                <td><input type="text" class="text" name="b_nameFirst" value="<%=billing.getNameFirst()%>"></td>
                            </tr>
                            <tr>
                                <td class="inputLabel" id="l_b_nameLast">Last Name</td>
                                <td><input type="text" class="text" name="b_nameLast" value="<%=billing.getNameLast()%>"></td>
                            </tr>
                            <tr>
                                <td class="inputLabel" id="l_b_address1">Address 1</td>
                                <td><input type="text" class="text" name="b_address1" value="<%=billing.getAddress1()%>"></td>
                            </tr>
                            <tr>
                                <td class="inputLabel" id="l_b_address2">Address 2</td>
                                <td><input type="text" class="text" name="b_address2" value="<%=billing.getAddress2()%>"></td>
                            </tr>
                            <tr>
                                <td class="inputLabel" id="l_b_city">City</td>
                                <td><input type="text" class="text" name="b_city" value="<%=billing.getCity()%>"></td>
                            </tr>
                            <tr>
                                <td class="inputLabel" id="l_b_state">State</td>
                                <td>
                                    <select name="b_state">
                                        <option></option>
                                        <% for (int i=0; i<states.size(); i++) {
                                            State state = (State)states.get(i);
                                            String selectedVal = (billing.getState().getAbbrev().equalsIgnoreCase(state.getAbbrev())) ? " selected" : "";
                                            %><option value="<%=state.getAbbrev()%>"<%=selectedVal%>><%=state.getName()%>
                                            <%
                                        }
                                        %>
                                    </select>
                                </td>
                            </tr>
                            <tr>
                                <td class="inputLabel" id="l_b_zip">Zip</td>
                                <td><input type="text" class="text" name="b_zip" value="<%=billing.getZip()%>"></td>
                            </tr>
                            <tr>
                                <td class="inputLabel">Country</td>
                                <td>
                                    <select name="b_country">
                                        <option></option>
                                        <% for (int i=0; i<countries.size(); i++) {
                                            Country country = (Country)countries.get(i);
                                            String selectedVal = (billing.getCountry().getId() == country.getId()) ? " selected" : "";
                                            %><option value="<%=country.getId()%>"<%=selectedVal%>><%=country.getName()%>
                                            <%
                                        }
                                        %>
                                    </select>
                                </td>
                            </tr>
                            <tr>
                                <td class="inputLabel" id="l_b_phoneNumber">Phone Number</td>
                                <td><input type="text" class="text" name="b_phoneNumber" value="<%=billing.getPhoneNumber()%>"></td>
                            </tr>
                        </table>
        </td>
        <td class="sectionSpace" width="10">&nbsp;</td>
        <td width="45%" class="matrix">
                        (if different from your billing)<br />
                        <br /><br />
                        <table border="0" cellpadding="5" cellspacing="5">
                            <tr>
                                <td class="inputLabel" id="l_s_nameFirst">First Name</td>
                                <td><input type="text" class="text" name="s_nameFirst" value="<%=shipping.getNameFirst()%>"></td>
                            </tr>
                            <tr>
                                <td class="inputLabel" id="l_s_namelast">Last Name</td>
                                <td><input type="text" class="text" name="s_nameLast" value="<%=shipping.getNameLast()%>"></td>
                            </tr>
                            <tr>
                                <td class="inputLabel" id="l_s_address1">Address 1</td>
                                <td><input type="text" class="text" name="s_address1" value="<%=shipping.getAddress1()%>"></td>
                            </tr>
                            <tr>
                                <td class="inputLabel" id="l_s_address2">Address 2</td>
                                <td><input type="text" class="text" name="s_address2" value="<%=shipping.getAddress2()%>"></td>
                            </tr>
                            <tr>
                                <td class="inputLabel" id="l_s_city">City</td>
                                <td><input type="text" class="text" name="s_city" value="<%=shipping.getCity()%>"></td>
                            </tr>
                            <tr>
                                <td class="inputLabel" id="l_s_state">State</td>
                                <td>
                                    <select name="s_state">
                                        <option></option>
                                        <% for (int i=0; i<states.size(); i++) {
                                            State state = (State)states.get(i);
                                            String selectedVal = (shipping.getState().getAbbrev().equalsIgnoreCase(state.getAbbrev())) ? " selected" : "";
                                            %><option value="<%=state.getAbbrev()%>"<%=selectedVal%>><%=state.getName()%>
                                            <%
                                        }
                                        %>
                                    </select>
                                </td>
                            </tr>
                            <tr>
                                <td class="inputLabel" id="l_s_zip">Zip</td>
                                <td><input type="text" class="text" name="s_zip" value="<%=shipping.getZip()%>"></td>
                            </tr>
                            <tr>
                                <td class="inputLabel" id="l_s_country">Country</td>
                                <td>
                                    <select name="s_country">
                                        <option></option>
                                        <% for (int i=0; i<countries.size(); i++) {
                                            Country country = (Country)countries.get(i);
                                            String selectedVal = (shipping.getCountry().getId() == country.getId()) ? " selected" : "";
                                            %><option value="<%=country.getId()%>"<%=selectedVal%>><%=country.getName()%>
                                            <%
                                        }
                                        %>
                                    </select>
                                </td>
                            </tr>
                            <tr>
                                <td class="inputLabel" id="l_s_phoneNumber">Phone Number</td>
                                <td><input type="text" class="text" name="s_phoneNumber" value="<%=shipping.getPhoneNumber()%>"></td>
                            </tr>
                        </table>
        </td>
    </tr>
</table>
<br />
<br />
            <% if (theUser.getId() == 0) { %>
<table border="0" cellpadding="0" cellspacing="0" width="100%">
    <tr><td class="matrixHead"><% if (theUser.getId() == 0) { out.write("ADDITIONAL INFORMATION"); } %></td>
    </tr>
    <tr><td class="matrix">
                <table border="0">
                    <tr>
                        <td class="inputLabel" id="l_u_email">Email Address</td>
                        <td><input type="text" class="text" name="u_email" size="75" maxlength="200" value="<%=theUser.getEmail()%>"></td>
                    </tr>
                    <tr>
                        <td></td>
                        <td>
                            <input type="checkbox" name="l_u_mailingList" value="true"<% if (theUser.getMailingList()) { out.print(" checked"); } %>>
                            Sign up for our mailing list.
                        </td>
                    </tr>
                    <tr>
                        <td class="inputLabel" id="l_u_password1">Password</td>
                        <td><input type="password" class="text" name="u_password1" maxlength="30" value=""></td>
                    </tr>
                    <tr>
                        <td class="inputLabel" id="l_u_password2">Password Confirm</td>
                        <td><input type="password" class="text" name="u_password2" maxlength="30" value=""></td>
                    </tr>
                </table>
      </td></tr></table>
            <br />
            <br />
            <% } %>
            <table border="0" width="100%">
                <tr>
                    <td style="text-align:left;">
                        <input type="button" class="button" name="button_back" value="&lt;&nbsp;BACK" onClick="goBack()">
                    </td>
                    <td style="text-align:right;">
                        <input type="button" class="button" name="button_next" value="NEXT&nbsp&gt;" onClick="goNext()">
                    </td>
                </tr>
            </table>
          </form>
</div>

<%@ include file = "../include/footer.jsp"%>
