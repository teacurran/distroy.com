<%@ page import="com.approachingpi.store.catalog.Image,
                 java.util.ArrayList,
                 com.approachingpi.store.catalog.*,
                 com.approachingpi.store.site.HomepageItem,
                 com.approachingpi.store.catalog.Image"%>
<jsp:useBean id="items"   class="java.util.ArrayList" scope="request"/>

<%@ include file = "./include/global.jsp"%>
<%
    strPage = "home";
    strPageTitle = "Designer Clothing and TShirts from Distro.Y, Zazzi, and Angeldustrial";
    
    java.util.Random randomGenerator = new java.util.Random();
    int postcardNumber = (Math.abs(randomGenerator.nextInt()) % 7) + 1;

%>
<%@ include file = "./include/header.jsp"%>

<table width="100%">
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
                <table>
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
                            <td class="content"<% if (item.getLink().length() > 0) { out.print(" rowspan=\"2\""); } %>><a href="<%=strBaseUrl%>brands/<%=thisVariation.getProduct().getBrand().getId()%>/<%=thisVariation.getId()%>"><img src="<%=strImgProductDir%>thumb/<%=thisImage.getName()%>" border="0"></a></td>
                            <%
                        }
                        %>
                    </tr>
                    <% if (item.getLink().length() > 0) { %>
                        <tr>
                            <td class="content" align="right" valign="bottom" style="text-align:right;">
                                <a href="<%=strBaseUrl%><%=item.getLink()%>"><img src="<%=strImageDir%>icon_arrow_more.gif" border="0"></a>
                            </td>
                        </tr>
                    <% } %>
                </table>
                <br />
                <% 
            } %>
        </td>
        <td style="text-align:right; vertical-align:top;"><img src="<%=strImageDir%>home/<%=postcardNumber%>.jpg" width="325" height="487"></td>
    </tr>
</table>


<%@ include file = "./include/footer.jsp"%>

