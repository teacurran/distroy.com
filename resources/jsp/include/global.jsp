<%@ page import="java.text.SimpleDateFormat,
    java.text.*,
    java.util.*,
    com.approachingpi.user.User,
    com.approachingpi.store.Store"%>
<jsp:useBean id="altAttribute"  class="com.approachingpi.servlet.AltAttribute" scope="request"/>
<jsp:useBean id="piSession"     class="com.approachingpi.servlet.Session" scope="request"/>
<jsp:useBean id="formHash"      class="com.approachingpi.util.NotNullHash" scope="request"/>
<%
    User user = piSession.getUser();

    String strCss               = "/css/retail.css";
    String strPageTitle         = "";
    String strPagePath          = "";
    String strOnLoad            = "";
    String strPage              = "";
    String strPageHeight        = "1000";
    String strImageDir          = "/img/";
    String strImgAdminDir       = "/img/admin/";
    String strImgProductDir     = "/img/product/";
    String strMetaDescription   = "";
    String strActivateMenu      = "1";
    String menuBrand            = "";
    int    menuCategory         = 0;

    String leftMargin       = "0";
    String topMargin        = "0";
    String marginWidth      = "0";
    String marginHeight     = "0";

    String classRow1        = "classRow1";
    String classRow2        = "classRow2";
    String classRowCurrent  = classRow1;

    SimpleDateFormat sdf = new SimpleDateFormat("M/d/yyyy");
    SimpleDateFormat timeFormat = new SimpleDateFormat("M/d/yyyy HH:mm:ss");
    SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");

    Date dateYear = Calendar.getInstance().getTime();
    String thisYear = yearFormat.format(dateYear);

    NumberFormat dollarFormat = NumberFormat.getCurrencyInstance(Locale.US);

    Store store = piSession.getStore();
    boolean wholesale   = store.isWholesale();

    String backPattern  = strImageDir + "background.png";
    String topImage     = strImageDir + "distroy_black.png";
    String topLogo      = strImageDir + "logo_black.png";
    String voidAboveLogo  = strImageDir + "voidabove.gif";
    String strBaseUrl   = "/";

    boolean boShowFoot = true;
    boolean boShowNav = true;
    boolean boIsPopup = false;

    if (wholesale) {
        strCss      = "/css/wholesale.css";
        backPattern = strImageDir + "background.png"; // "background_wholesale.png";
        topImage    = strImageDir + "distroy_blue.png";
        topLogo     = strImageDir + "logo_blue.png";
        strBaseUrl  = "/wholesale/";
        if (user.getType() < User.TYPE_WHOLESALE) {
            boShowNav = false;
        }
    }
    if (store.getAbbreviation().equals("DCU")) {
        strCss      = "/css/custom.css";
        backPattern = strImageDir + "background.png";
        topImage    = strImageDir + "distroy_red.png";
        topLogo     = strImageDir + "logo_red.png";
        strBaseUrl  = "/custom/";
    }

	String phoneNumber = "(800) 627-4980";
	String phoneNumberWholesale = phoneNumber;

%>