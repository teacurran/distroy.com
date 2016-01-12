<%@ page import="com.approachingpi.store.site.HomepageItem,
                 com.approachingpi.store.catalog.ProductVariation,
                 com.approachingpi.store.catalog.Image,
                 com.approachingpi.user.User"%>
<jsp:useBean id="items"     class="java.util.ArrayList" scope="request"/>


<%@ include file = "./include/global.jsp"%>
<%
    int accessLevel = altAttribute.getInt("accessLevel");
    
	strPage = "Homepage";
	strPageTitle = "Homepage";
%>
<%@ include file = "./include/header.jsp"%>
<%@ include file = "./include/bodyTop.jsp"%>

<script language="javascript">
var selectedSection = 0;

function selectSection(id) {
    if (selectedSection != id) {
        unSelectSection();
    }
    var section = document.getElementById("section_" + id);
    section.className="contentSelected";
    selectedSection = id;
}

function unSelectSection() {
    if (selectedSection > 0) {
        var section = document.getElementById("section_" + selectedSection);
        section.className="contentNotSelected";
        selectedSection = 0;
    }
}
    
</script>    


<br /><b>This admin tool is not yet complete</b><br /><br />
<table width="678">
    <tr>
        <td>
            <table width="100%">
                <tr>
                    <td>
                        <select name="accessLevel">
                        <%
                        int[] accessTypes = User.TYPES;
                        for (int i=0; i<accessTypes.length; i++) {
                            int thisAccess = accessTypes[i];
                            String selectVal = (thisAccess == accessLevel) ? " selected" : "";
                            %><option value="<%=thisAccess%>"<%=selectVal%>><%=User.getTypeName(thisAccess)%></option>
                            <%
                        }
                        %>
                        </select>
                    </td>
                    <td style="text-align:right;">
                        <a href="javascript:addNew()" title="Add a new Item"><img id="iconNew" src="<%=strImgAdminDir%>icons/icon_new_file.gif" height="20" width="20" border="0" alt="Add new section."></a>
                        <a href="javascript:editSection()" title="Edit section."><img id="iconEdit" src="<%=strImgAdminDir%>icons/icon_window_new_disabled.gif" height="20" width="20" border="0" alt="Edit section."></a>
                        <a href="javascript:moveSectionUp()" title="Move section up."><img id="iconUp" src="<%=strImgAdminDir%>icons/icon_arrow_up_disabled.gif" height="20" width="20" border="0" alt="Move section up."></a>
                        <a href="javascript:moveSectionDown()" title="Move section down."><img id="iconDown" src="<%=strImgAdminDir%>icons/icon_arrow_down_disabled.gif" height="20" width="20" border="0" alt="Move section down."></a>
                    </td>
                </tr>
            </table>
        </td>
    </tr>
    <tr>
        <td width="325" style="text-align:left; vertical-align:top;">
            <% for(int i=0; i<items.size(); i++) { 
                HomepageItem item = (HomepageItem)items.get(i);
                String colspan = "";
                if (item.getProductVariations().size() == 2) {
                    colspan = " colspan=\"3\"";
                } else if (item.getProductVariations().size() == 1) {
                    colspan = " colspan=\"2\"";
                }
                %>
                <table class="contentNotSelected" width="100%" onClick="selectSection(<%=item.getId()%>)" id="section_<%=item.getId()%>">
                    <tr>
                        <td class="menu"<%=colspan%>><%=item.getTitle()%></td>
                    </tr>
                    <tr>
                        <td class="content">
                            <%=item.getBlurb()%>
                        </td>
                        <%
                        ArrayList variations = item.getProductVariations();
                        for (int x=0; x<variations.size(); x++) {
                            ProductVariation thisVariation = (ProductVariation)variations.get(x);
                            
                            Image thisImage = thisVariation.getFirstImage();
                            if (thisImage.getId() == 0) {
                                thisImage = thisVariation.getProduct().getFirstImage();
                            }
                            %>
                            <td class="content"<% if (item.getLink().length() > 0) { out.print(" rowspan=\"2\""); } %>><a href="/brands/<%=thisVariation.getProduct().getBrand().getId()%>/<%=thisVariation.getId()%>"><img src="<%=strImgProductDir%>thumb/<%=thisImage.getName()%>" border="0"></a></td>
                            <%
                        }
                        %>
                    </tr>
                    <% if (item.getLink().length() > 0) { %>
                        <tr>
                            <td class="content" align="right" valign="bottom" style="text-align:right;">
                                <a href="<%=item.getLink()%>"><img src="<%=strImageDir%>icon_arrow_more.gif" border="0"></a>
                            </td>
                        </tr>
                    <% } %>
                </table>
                <br />
                <% 
            } %>
        </td>
        <td style="text-align:right; vertical-align:top;"><img src="<%=strImageDir%>home/speed_racer.jpg"></td>
	</tr>
</table>

<%@ include file = "./include/bodyBottom.jsp"%>
