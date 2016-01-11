<% if (!boIsPopup) { %>
    </div>
<% } %>
<% if (boShowFoot) { %>
        <div class="foot">
            <form method="post" action="<%=strBaseUrl%>mailinglist">
            <input type="hidden" name="action" value="<%=MailingListServlet.ACTION_SUB%>">
            Mailing List Signup:
            <input type="text" name="email" class="text" size="20" value="">
            <input type="submit" class="button" name="button_signup" value="SIGNUP">
            </form>
            <br />
        &copy;<%=thisYear%> DISTRO.Y / <a href="http://www.approachingpi.com">Approaching Pi, Inc.</a>
        </div>
<% } %>
<% if (!boIsPopup) { %>
    </div>
</div>
<% } %>
<div id="message_Window" style="position:absolute; width:300; height:600; z-index:20; left:-400px; top:235px; visibility:hidden;">
<table cellpadding="0" width="300" border="0" cellspacing="0" id="message_Table">
    <tr>
        <td width="300" class="messageCell">
            <table width="100%">
                <tr>
                    <td id="message_textTd" class="messageCellText">Please choose a variation and size</td>
                </tr>
                <tr>
                    <td class="messageCellText">&nbsp;</td>
                </tr>
                <tr>
                    <td id="message_textTd" class="messageCellText" style="text-align:center;">
                        <input type="button" name="button_dismiss" class="button" onClick="closeMessageWindow()" value="OKAY" />
                    </td>
                </tr>
            </table>
        </td>
    </tr>
</table>
</div>

</body>
</html>

<%
con.close();
%>

