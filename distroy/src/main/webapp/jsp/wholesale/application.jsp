<%@ page import="com.approachingpi.servlet.PiServlet,
                 com.approachingpi.user.State,
                 com.approachingpi.user.Country"%>
<jsp:useBean id="content"   class="com.approachingpi.store.site.Content" scope="request"/>

<%@ include file = "../include/global.jsp"%>
<%
	strPage = "application";
	strPageTitle = "Wholesale Application";
%>
<%@ include file = "../include/header.jsp"%>

<%
ArrayList countries = Country.getAll(con,false);
ArrayList states = State.getAll(con,true);

%>

<form method="post" action="<%=strBaseUrl%>application">
<input type="hidden" name="formSubmitted" value="true">
<table width="100%">
    <tr>
        <td class="pageHead">Wholesale Application</td>
    </tr>
    <tr>
        <td class="content" height="300">
            <p class="contentTextBold">Welcome to DISTRO.Y Wholesale.</p>
            <p>
            Before you can be approved for a wholesale account
            we ask that you fill out this quick application so we know
            who you are.  Once you are approved you will be able to log
            in and get product information, download catalogs, and place orders.
            </p>
            <p>
            We will try to process your application as quickly as possible and
            will contact you if we have any questions or problems.  If you need
            your account set up quicker, please call Customer Service at <%=phoneNumberWholesale%>
            </p>

            <center>
            <table>
                <tr>
                    <td class="inputLabel" id="l_companyName">Company Name</td>
                    <td><input type="text" class="text" size="30" maxlength="100" name="companyName" value="<%=PiServlet.getReqEsc(request,"companyName")%>">
				</tr>
                <tr>
                    <td class="inputLabel" id="l_nameFirst">Contact First Name</td>
                    <td><input type="text" class="text" size="30" maxlength="100" name="nameFirst" value="<%=PiServlet.getReqEsc(request,"nameFirst")%>">
				</tr>
                <tr>
                    <td class="inputLabel" id="l_nameLast">Contact Last Name</td>
                    <td><input type="text" class="text" size="30" maxlength="100" name="nameLast" value="<%=PiServlet.getReqEsc(request,"nameLast")%>">
				</tr>
				<tr>
					<td class="inputLabel" id="l_contactTitle">Contact Title</td>
					<td>
						<select name="contactTitle">
						<option value="">Please Choose</option>
						<%
						String[] contactTitles = {"Owner", "Buyer", "Executive", "Other"};
						for (int i=0; i<contactTitles.length; i++) {
							String selectedVal = contactTitles[i].equalsIgnoreCase(PiServlet.getReqString(request,"contactTitle")) ? " selected" : "";
							%><option value="<%=contactTitles[i]%>"<%=selectedVal%>><%=contactTitles[i]%></option>
							<%
						}
						%>
						</select>
					</td>
				</tr>
                <tr>
                    <td class="inputLabel" id="l_address1">Address 1</td>
                    <td><input type="text" class="text" size="30" maxlength="100" name="address1" value="<%=PiServlet.getReqEsc(request,"address1")%>">
				</tr>
                <tr>
                    <td class="inputLabel" id="l_address2">Address 2</td>
                    <td><input type="text" class="text" size="30" maxlength="100" name="address2" value="<%=PiServlet.getReqEsc(request,"address2")%>">
				</tr>
                <tr>
                    <td class="inputLabel" id="l_city">City</td>
                    <td><input type="text" class="text" size="30" maxlength="100" name="city" value="<%=PiServlet.getReqEsc(request,"city")%>">
				</tr>
				<tr>
					<td class="inputLabel" id="l_state">State</td>
					<td>
						<select name="state">
							<option></option>
							<% for (int i=0; i<states.size(); i++) {
								State state = (State)states.get(i);
								String selectedVal = (PiServlet.getReqString(request,"state").equalsIgnoreCase(state.getAbbrev())) ? " selected" : "";
								%><option value="<%=state.getAbbrev()%>"<%=selectedVal%>><%=state.getName()%>
								<%
							}
							%>
						</select>
					</td>
				</tr>
				<tr>
					<td class="inputLabel" id="l_zip">Zip</td>
					<td><input type="text" class="text" name="zip" value="<%=PiServlet.getReqEsc(request,"zip")%>"></td>
				</tr>
				<tr>
					<td class="inputLabel" id="l_country">Country</td>
					<td>
						<select name="country">
							<option></option>
							<% for (int i=0; i<countries.size(); i++) {
								Country country = (Country)countries.get(i);
								String selectedVal = (PiServlet.getReqInt(request,"country") == country.getId()) ? " selected" : "";
								%><option value="<%=country.getId()%>"<%=selectedVal%>><%=country.getName()%>
								<%
							}
							%>
						</select>
					</td>
				</tr>
				<tr>
					<td class="inputLabel" id="l_phoneNumber">Phone Number</td>
					<td><input type="text" size="30" class="text" name="phoneNumber" value="<%=PiServlet.getReqEsc(request,"phoneNumber")%>"></td>
				</tr>
				<tr>
					<td class="inputLabel" id="l_faxNumber">Fax Number</td>
					<td><input type="text" size="30" class="text" name="faxNumber" value="<%=PiServlet.getReqEsc(request,"faxNumber")%>"></td>
				</tr>
				<tr>
					<td class="inputLabel" id="l_emailAddress">Email Address</td>
					<td><input type="text" size="30" class="text" name="emailAddress" value="<%=PiServlet.getReqEsc(request,"emailAddress")%>"></td>
				</tr>
				<tr>
					<td class="inputLabel" id="l_hearAboutUs">How did you hear about us?</td>
					<td><input type="text" size="30" class="text" name="hearAboutUs" maxlength="100" value="<%=PiServlet.getReqEsc(request,"hearAboutUs")%>"></td>
				</tr>
				<tr>
					<td class="inputLabel" id="l_comment">Comments</td>
					<td><textarea name="comment" class="text" rows="4" cols="30"><%=PiServlet.getReqEsc(request,"comment")%></textarea></td>
				</tr>
				<tr>
					<td></td>
					<td>
						<input type="submit" class="button" name="button_submit" value="Submit Application">
					</td>
				</tr>
			</table>
			</center>
        </td>
    </tr>
</table>
</form>

<%@ include file = "../include/footer.jsp"%>
