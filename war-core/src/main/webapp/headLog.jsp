<%@ page import="java.util.*"%>
<%@ page import="com.stratelia.webactiv.util.GeneralPropertiesManager"%>
<%@ page import="com.stratelia.webactiv.util.ResourceLocator"%>
<%@ page import="com.stratelia.silverpeas.authentication.*"%>

<%
	response.setHeader( "Expires", "Tue, 21 Dec 1993 23:59:59 GMT" );
    response.setHeader( "Pragma", "no-cache" );
    response.setHeader( "Cache-control", "no-cache" );
    response.setHeader( "Last-Modified", "Fri, Jan 25 2099 23:59:59 GMT" );
    response.setStatus( HttpServletResponse.SC_CREATED );
%>

<%
String sURI = request.getRequestURI();
String sServletPath = request.getServletPath();
String sPathInfo = request.getPathInfo();
if(sPathInfo != null)
    sURI = sURI.substring(0,sURI.lastIndexOf(sPathInfo));
String m_context = ".."+ sURI.substring(0,sURI.lastIndexOf(sServletPath));

// Get the authentication settings
ResourceLocator authenticationSettings		= new ResourceLocator("com.silverpeas.authentication.settings.authenticationSettings", "");
ResourceLocator homePageBundle = new ResourceLocator("com.stratelia.webactiv.homePage.multilang.homePageBundle", "");

// Get the logo to print
ResourceLocator general				= new ResourceLocator("com.stratelia.silverpeas.lookAndFeel.generalLook", "");
ResourceLocator generalMultilang	= new ResourceLocator("com.stratelia.webactiv.multilang.generalMultilang", "");

String logo = general.getString("logo", m_context+"/admin/jsp/icons/logo_silverpeasBig.gif");
String styleSheet = general.getString("defaultStyleSheet", m_context+"/util/styleSheets/globalSP.css");

// Get a LoginPasswordAuthentication object
LoginPasswordAuthentication lpAuth = new LoginPasswordAuthentication();

// list of domains
Hashtable domains = lpAuth.getAllDomains();
/*Hashtable domains = new Hashtable();
domains.put("0", "SilverpeasNico");*/
List domainIds = new ArrayList(domains.keySet());
//ArrayList domainsIds = lpAuth.getDomainsIds();
%>