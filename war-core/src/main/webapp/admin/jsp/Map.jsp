<%--

    Copyright (C) 2000 - 2009 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://repository.silverpeas.com/legal/licensing"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader ("Expires",-1); //prevents caching at the proxy server
%>

<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory"%>
<%@ page import="com.stratelia.silverpeas.peasCore.MainSessionController"%>
<%@ page import="com.stratelia.webactiv.beans.admin.OrganizationController"%>
<%@ page import="com.stratelia.webactiv.beans.admin.SpaceInst"%>
<%@ page import="com.stratelia.webactiv.beans.admin.ComponentInst"%>
<%@ page import="com.stratelia.webactiv.beans.admin.UserDetail"%>
<%@ page import="com.stratelia.webactiv.util.GeneralPropertiesManager"%>
<%@ page import="com.stratelia.webactiv.util.ResourceLocator"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.frame.Frame"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.window.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.browseBars.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.operationPanes.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.board.Board"%>
<%@ page import="com.stratelia.silverpeas.peasCore.URLManager"%>

<%@ page import="java.util.ArrayList"%>

<%!

//Icons
String separator;
String map;
String feedback;
String key;
String help;
String go;
String corner;

String[] domains;
String domain;

MainSessionController m_MainSessionCtrl = null;
String m_sContext = "";
OrganizationController m_OrganizationController = null;

private String printSpaceAndSubSpaces(String spaceId, int depth, String[] m_asPrivateDomainsIds)
{
    m_OrganizationController = m_MainSessionCtrl.getOrganizationController();
    String language = m_MainSessionCtrl.getFavoriteLanguage();
    
      ArrayList alCompoInst = null;
      String[] asCompoNames = null;
      SpaceInst spaceInst = m_OrganizationController.getSpaceInstById(spaceId);
      ComponentInst componentInst = null;
      String result = "";
      if (spaceInst!=null){
        result += "<table border=\"0\" cellspacing=\"0\" cellpadding=\"2\">\n";
        if (depth==0) result += "<tr><td>&nbsp;</td></tr>\n";
            result += "<tr>\n";
        if (URLManager.displayUniversalLinks())
            result += "<td class=\"txttitrecol\">&#8226; <a href=\""+URLManager.getSimpleURL(URLManager.URL_SPACE, spaceInst.getId())+"\" target=\"_top\">"+spaceInst.getName(language)+"</a></td></tr>\n";
        else
            result += "<td class=\"txttitrecol\">&#8226; "+spaceInst.getName(language)+"</td></tr>\n";
			
        result += "<tr><td>\n";

    		String[] asAvailCompoForCurUser = m_OrganizationController.getAvailCompoIds(spaceInst.getId(), m_MainSessionCtrl.getUserId());
    		alCompoInst = spaceInst.getAllComponentsInst();
    		asCompoNames = new String[alCompoInst.size()];
    		for(int nI = 0; nI <alCompoInst.size(); nI++) {
    			// Check if the component is accessible to the user
    			componentInst = (ComponentInst)alCompoInst.get(nI);
    			boolean bAllowed = false;
    			for(int nJ=0; asAvailCompoForCurUser != null && nJ < asAvailCompoForCurUser.length; nJ++)
    				if(componentInst.getId().equals(asAvailCompoForCurUser[nJ]))
    					bAllowed = true;
    
    			if(bAllowed) {
    				String label = componentInst.getLabel(language);
    				if (label == null || label.length() == 0)
    					label = componentInst.getName();
    					
    				if (URLManager.displayUniversalLinks())
    					result += "&nbsp;<img src=\""+m_sContext+"/util/icons/component/"+componentInst.getName()+"Small.gif\" border=\"0\" width=\"15\" align=\"top\" alt=\"\"/>&nbsp;<a href=\""+URLManager.getSimpleURL(URLManager.URL_COMPONENT, componentInst.getId())+"\" target=\"_top\">"+label+"</a>\n";
    				else
    					result += "&nbsp;<img src=\""+m_sContext+"/util/icons/component/"+componentInst.getName()+"Small.gif\" border=\"0\" width=\"15\" align=\"top\" alt=\"\"/>&nbsp;<a href=\""+m_sContext + URLManager.getURL(componentInst.getName(), spaceId, componentInst.getId()) + "Main\" target=\"MyMain\" title=\""+componentInst.getDescription()+"\">"+label+"</a>\n";
    			}
    		}

    		// Get all sub spaces
    		String [] subSpaceIds = m_OrganizationController.getAllowedSubSpaceIds(m_MainSessionCtrl.getUserId(), spaceId);
    		for (int nI=0; nI<subSpaceIds.length; nI++)
    		{
    		   if (!"".equals(printSpaceAndSubSpaces(subSpaceIds[nI], depth+1, m_asPrivateDomainsIds)))
    		   {
        		   result += "<table border=\"0\" cellspacing=\"0\" cellpadding=\"5\">\n";
        		   result += "<tr><td>&nbsp;&nbsp;</td>\n";
        		   result += "<td>\n";
        		   result += printSpaceAndSubSpaces(subSpaceIds[nI], depth+1, m_asPrivateDomainsIds);
        		   result += "</td></tr></table>\n";
    		   }
    		}
    
    		result += "</td>\n";
    		result += "</tr>\n";
    		result += "</table>\n";
          }
          return result;
}

%>

<%
GraphicElementFactory gef = (GraphicElementFactory) session.getAttribute("SessionGraphicElementFactory");

String[] m_asPrivateDomainsNames = null;
String[] m_asPrivateDomainsIds = null;
String sGenSpace = "";
SpaceInst spaceInst = null;
ArrayList alCompoInst = null;
String[] asCompoNames = null;
String sPrivateDomain;

m_MainSessionCtrl = (MainSessionController) session.getAttribute("SilverSessionController");

if (m_MainSessionCtrl == null) {
    // No session controller in the request -> security exception
    String sessionTimeout = GeneralPropertiesManager.getGeneralResourceLocator().getString("sessionTimeout");
    getServletConfig().getServletContext().getRequestDispatcher(sessionTimeout).forward(request, response);
    return;
}

m_OrganizationController = m_MainSessionCtrl.getOrganizationController();
UserDetail currentUser = m_OrganizationController.getUserDetail(m_MainSessionCtrl.getUserId());

String language = m_MainSessionCtrl.getFavoriteLanguage();
ResourceLocator message = new ResourceLocator("com.stratelia.webactiv.homePage.multilang.homePageBundle", language);

String sURI = request.getRequestURI();
String sServletPath = request.getServletPath();
String sPathInfo = request.getPathInfo();
if(sPathInfo != null)
    sURI = sURI.substring(0,sURI.lastIndexOf(sPathInfo));
m_sContext = sURI.substring(0,sURI.lastIndexOf(sServletPath));

m_asPrivateDomainsIds = m_MainSessionCtrl.getUserAvailRootSpaceIds();
m_asPrivateDomainsNames = m_OrganizationController.getSpaceNames(m_asPrivateDomainsIds);

//Icons
separator = "icons/trait.gif";
map = "icons/map.gif";
feedback = "icons/mail.gif";
key = "icons/key.gif";
help = "icons/help.gif";
go = "icons/go.gif";
corner = "icons/corner.gif";

sGenSpace = m_OrganizationController.getGeneralSpaceId();

%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" 
   "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title><%=message.getString("MyMap")%></title>
<%
out.println(gef.getLookStyleSheet());
%>
</head>
<body>
<%
		Window window = gef.getWindow();

	BrowseBar browseBar = window.getBrowseBar();
	browseBar.setComponentName(message.getString("MyMap"));

	out.println(window.printBefore());
	Frame frame=gef.getFrame();
 out.println(frame.printBefore());

 ResourceLocator lookSettings = gef.getFavoriteLookSettings(); 
 String accessLoginId = lookSettings.getString("guestId");
 boolean isAnonymAccess = currentUser.isAccessGuest() && currentUser.getId().equals(accessLoginId);
 
 Board board = gef.getBoard();
%>

<% if (!isAnonymAccess) { %>
<%=board.printBefore() %>
		<table>
		<tr align="left">
			<td><img src="icons/accueil/esp_perso.gif" align="middle" alt=""/>&nbsp;&nbsp;<span class="txtnav"><%=message.getString("SpacePersonal")%></span></td>
		</tr>
		<tr>
			<td>&nbsp;&nbsp;<img src="<%=m_sContext%>/util/icons/component/agendaSmall.gif" border="0" width="15" align="top" alt=""/>&nbsp;<a href="<%=m_sContext + URLManager.getURL(URLManager.CMP_AGENDA) + "agenda.jsp"%>" target="MyMain"><%=message.getString("Diary")%></a>
				&nbsp;&nbsp;<img src="<%=m_sContext%>/util/icons/component/todoSmall.gif" border="0" width="15" align="top" alt=""/>&nbsp;<a href="<%=m_sContext + URLManager.getURL(URLManager.CMP_TODO)+ "todo.jsp"%>" target="MyMain"><%=message.getString("ToDo")%></a>
				&nbsp;&nbsp;<img src="<%=m_sContext%>/util/icons/component/mailserviceSmall.gif" border="0" width="15" align="top" alt=""/>&nbsp;<a href="<%=m_sContext + URLManager.getURL(URLManager.CMP_SILVERMAIL) + "Main"%>" target="MyMain"><%=message.getString("Mail")%></a>
				&nbsp;&nbsp;<a href="<%=m_sContext + URLManager.getURL(URLManager.CMP_PDCSUBSCRIPTION) + "subscriptionList.jsp"%>" target="MyMain"><%=message.getString("MyInterestCenters")%></a>
				&nbsp;&nbsp;<a href="<%=m_sContext + URLManager.getURL(URLManager.CMP_INTERESTCENTERPEAS) + "iCenterList.jsp"%>" target="MyMain"><%=message.getString("FavRequests")%></a></td>
		</tr>
		</table>
<%=board.printAfter() %>
<br/>
		<%
out.println(frame.printMiddle());
		}
%>

<%=board.printBefore() %>
		<table>
			<tr>
				<td><img src="icons/accueil/esp_collabo.gif" align="middle" alt=""/> <span class="txtnav"><%=message.getString("SpaceCollaboration")%></span></td>
			</tr>
			<tr><td valign="top">
							<%
							if (m_asPrivateDomainsNames != null) {
									for(int nK = 0; nK < m_asPrivateDomainsIds.length; nK++) {
											spaceInst = m_OrganizationController.getSpaceInstById(m_asPrivateDomainsIds[nK]);
											if (spaceInst.getDomainFatherId().equals("0")) {
												out.println(printSpaceAndSubSpaces(m_asPrivateDomainsIds[nK], 0, m_asPrivateDomainsIds));
											}
									}
							}
							%>
					</td>
			</tr>
		</table>
<%=board.printAfter() %>
<%
out.println(frame.printAfter());
out.println(window.printAfter());
%>
</body>
</html>