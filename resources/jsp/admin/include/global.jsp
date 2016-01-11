<%@ page import="java.text.SimpleDateFormat,
    java.text.*,
    java.util.*
"%>
<jsp:useBean id="altAttribute"  class="com.approachingpi.servlet.AltAttribute" scope="request"/>
<jsp:useBean id="formHash"      class="com.approachingpi.util.NotNullHash" scope="request"/>
<%
    String strPageTitle         = "";
    String strPagePath          = "";
    String strOnLoad            = "";
    String strPage              = "";
    String strImageDir          = "/img/";
    String strImgAdminDir       = "/img/admin/";
    String strImgProductDir     = "/img/product/";

    String leftMargin       = "0";
    String topMargin        = "0";
    String marginWidth      = "0";
    String marginHeight     = "0";

    String classRow1        = "classRow1";
    String classRow2        = "classRow2";
    String classRowCurrent  = classRow1;

    SimpleDateFormat sdf = new SimpleDateFormat("M/d/yyyy");
    SimpleDateFormat timeFormat = new SimpleDateFormat("M/d/yyyy HH:mm:ss");
    
    NumberFormat dollarFormat = NumberFormat.getCurrencyInstance(Locale.US);
    DecimalFormat ddf = new DecimalFormat();
    ddf.applyPattern("0.00");
    
    

%>