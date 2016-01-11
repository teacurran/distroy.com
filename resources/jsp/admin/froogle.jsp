<%@ page import="com.approachingpi.store.catalog.*"%>
<%@ include file = "./include/global.jsp"%>

<%@ include file = "./include/header.jsp"%>

<%
    SearchEngine se = new SearchEngine();
    se.setActiveForRetail(true);
    se.setActiveOnly(true);

    ArrayList rsProducts = se.executeReturnProducts(con);
%>

<table>
    <tr>
        <td>product_url</td>
        <td>name</td>
        <td>description</td>
        <td>image_url</td>
        <td>category</td>
        <td>price</td>
        <td>instock</td>
        <td>shipping</td>
        <td>brand</td>
    </tr>
<% for (int i=0; i<rsProducts.size(); i++) {
    Product product = (Product)rsProducts.get(i);
    product.loadImagesFromDb(con, 1, Image.SQUARE);

    product.getDesc();
    product.getFirstImage(Image.SQUARE);
    product.getFirstVariation().getCategories();

    ArrayList variations = product.getVariations();
    for (int x=0; x<variations.size(); x++) {
        ProductVariation variation = (ProductVariation)variations.get(x);
        variation.loadImagesFromDb(con, 1, Image.SQUARE);
        variation.loadCategoriesFromDb(con);

        ArrayList categories = variation.getCategories();
        Category firstCategory = null;
        String urlString = "";
        String categoryString = "";
        if (categories != null && categories.size() > 0) {
            firstCategory = (Category)categories.get(0);
            categoryString = "tshirts > " + firstCategory.getPathAsString();
            urlString = "/categories/" + firstCategory.getId() + "/" + variation.getId();
        } else {
            urlString = "/brands" + product.getBrand().getId() + "/" + variation.getId();
            categoryString = "brands > " + product.getBrand().getName();
        }
        Image firstImage = variation.getFirstImage();

        %>
        <tr>
            <td>http://www.distroy.com<%=urlString%></td>
            <td><%=product.getName()%> - <%=variation.getStyle()%> - <%=variation.getColor()%></td>
            <td><%=product.getTextDescription()%></td>
            <td>http://www.distroy.com/img/product/standard/<%=firstImage.getName()%></td>
            <td><%=categoryString%></td>
            <td><%=ddf.format(variation.getPriceRetail())%></td>
            <td>Y</td>
            <td>0.00</td>
            <td><%=product.getBrand().getName()%></td>
        </tr>
        <%
    }
} %>
</table>

<%@ include file = "./include/bodyBottom.jsp"%>
