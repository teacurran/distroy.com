<%@ page import="com.approachingpi.store.servlet.admin.ProductServlet,
                 com.approachingpi.servlet.PiServlet,
                 com.approachingpi.store.catalog.*"%>
<jsp:useBean id="categories"    class="java.util.ArrayList" scope="request"/>
<jsp:useBean id="products"      class="java.util.ArrayList" scope="request"/>
<jsp:useBean id="artists"       class="java.util.ArrayList" scope="request"/>
<jsp:useBean id="brands"        class="java.util.ArrayList" scope="request"/>
<jsp:useBean id="search"        class="com.approachingpi.store.catalog.SearchEngine" scope="request"/>

<%@ include file = "./include/global.jsp"%>
<%
    int categoryId = altAttribute.getInt("categoryId");
    strPage = "product";
    strPageTitle = "Products";
%>
<%@ include file = "./include/header.jsp"%>
<%@ include file = "./include/bodyTop.jsp"%>

<a href="/admin/Product?action=<%=ProductServlet.ACTION_EDIT%>" title="Create a new Product"><img src="<%=strImgAdminDir%>icons/icon_new_file.gif" height="20" width="20" border="0" vspace="10"></a>
<br>

<form method="post" action="/admin/Product">
<input type="hidden" name="action" value="<%=ProductServlet.ACTION_LIST%>">
<input type="hidden" name="formSubmitted" value="true">
<select name="categoryId">
<option value="-1">All Categories</option>
<%
    for(int i=0;i<categories.size();i++) {
        Category thisCategory = (Category)categories.get(i);
    %><option value="<%=thisCategory.getId()%>"<% if(search.containsCategory(thisCategory.getId())) { out.write(" selected"); }%>><%=thisCategory.getPathAsString()%></option><%
    }
%>
</select>
<select name="brandId">
<option value="-1">All Brands</option>
<%
    for(int i=0;i<brands.size();i++) {
        Brand thisBrand = (Brand)brands.get(i);
    %><option value="<%=thisBrand.getId()%>"<% if(thisBrand.getId()==search.getInput().getBrand().getId()) { out.write(" selected"); }%>><%=thisBrand.getName()%></option><%
    }
%>
</select>
<select name="artistId">
<option value="-1">All Artists</option>
<%
    for(int i=0;i<artists.size();i++) {
        Artist thisArtist = (Artist)artists.get(i);
    %><option value="<%=thisArtist.getId()%>"<% if(search.containsArtist(thisArtist.getId())) { out.write(" selected"); }%>><%=thisArtist.getNameLast()%>, <%=thisArtist.getNameFirst()%></option><%
    }
%>
</select>
<input type="submit" value="search">
</form>


<% if(PiServlet.getReqBoolean(request,"formSubmitted") || products.size() > 0) { %>
<table>
    <tr>
        <td class="matrixHead">Brand</td>
        <td class="matrixHead">Image</td>
        <td class="matrixHead">Name</td>
        <td class="matrixHead">Artist(s)</td>
        <td class="matrixHead">Variations</td>
        <td class="matrixHead">Retail</td>
        <td class="matrixHead">Wholesale</td>
    </tr>
    <%
    String cssClass = "matrixRow";
    for (int i=0; i<products.size(); i++) {
        Product product = (Product)products.get(i);

        Image image = product.getFirstVariation().getFirstImage();

        if (image.getId() == 0) {
            image = product.getFirstImage();
        }

        if (cssClass.equalsIgnoreCase("matrixRow")) {
            cssClass = "matrixRowAlternate";
        } else {
            cssClass = "matrixRow";
        }

        %>
        <tr>
            <td class="<%=cssClass%>"><%=product.getBrand().getName()%></td>
            <td class="<%=cssClass%>" style="vertical-align: text-bottom;">
                <% if (image.getName().length() > 0) { %>
                    <img src="/productimages/thumb/<%=image.getName().replaceAll(".jpg","")%>/thumb-<%=image.getName().replaceAll(".jpg","")%>-<%=product.getName().replaceAll("[^A-Za-z0-9]","_")%>.jpg" width="50">
                <% } %>
            </td>
            <td class="<%=cssClass%>"><a href="/admin/Product?action=<%=ProductServlet.ACTION_EDIT%>&productId=<%=product.getId()%>"><%=product.getName()%></a></td>
            <td class="<%=cssClass%>">
                <% if (product.getArtists().size() > 0) {
                    for (int x=0;x<product.getArtists().size();x++) {
                        Artist artist = (Artist)product.getArtists().get(x);
                        %><%=artist.getNameLast()%>, <%=artist.getNameFirst()%><br />
                        <%
                    }
                } %>
            </td>
            <td class="<%=cssClass%>" style="text-align:center;"><%=product.getVariations().size()%></td>
            <td class="<%=cssClass%>"><%=product.getActiveForRetail()%></td>
            <td class="<%=cssClass%>"><%=product.getActiveForWholesale()%></td>
        </tr>
        <%
    } %>
</table>
<% } %>



<%@ include file = "./include/bodyBottom.jsp"%>
