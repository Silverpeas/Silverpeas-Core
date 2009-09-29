<%--

    Copyright (C) 2000 - 2009 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have recieved a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://repository.silverpeas.com/legal/licensing"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@ page import="com.stratelia.webactiv.agenda.control.AgendaAccess,
                 java.util.Iterator,
                 java.net.URLEncoder,
                 com.stratelia.silverpeas.peasCore.URLManager,
                 com.silverpeas.interestCenter.model.InterestCenter,
                 java.util.ArrayList,
                 java.util.Vector,
                 com.stratelia.webactiv.util.attachment.control.AttachmentBmImpl,
                 com.stratelia.webactiv.util.attachment.model.AttachmentDetail,
                 com.stratelia.silverpeas.versioning.ejb.VersioningBm,
                 com.stratelia.silverpeas.versioning.ejb.VersioningBmHome,
                 com.stratelia.silverpeas.versioning.model.Document,
                 com.silverpeas.interestCenter.util.InterestCenterUtil"%>
<%@ page import="com.stratelia.webactiv.todo.control.ToDoAccess"%>
<%@ page import="com.stratelia.webactiv.calendar.model.Schedulable"%>
<%@ page import="com.stratelia.webactiv.calendar.model.ToDoHeader"%>
<%@ page import="com.stratelia.webactiv.util.*"%>
<%@ page import="com.stratelia.silverpeas.notificationserver.channel.silvermail.SILVERMAILMessage"%>
<%@ page import="com.stratelia.silverpeas.notificationserver.channel.silvermail.SILVERMAILUtil"%>
<%@ page import="java.util.Date"%>
<%@ page import="java.net.URLEncoder"%>

<%//____/ VIEW GENERATOR \_________________________________________________________________________%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.buttons.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.buttonPanes.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.icons.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.iconPanes.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.arrayPanes.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.tabs.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.operationPanes.OperationPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.browseBars.BrowseBar"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.window.Window"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.navigationList.NavigationList"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.frame.Frame"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.monthCalendar.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.Encode"%>
<%@ page import="com.silverpeas.util.StringUtil"%>
<%@ page import="com.silverpeas.admin.ejb.AdminBm"%>
<%@ page import="com.silverpeas.admin.ejb.AdminBmHome"%>
<%@ page import="com.silverpeas.pdc.ejb.PdcBm"%>
<%@ page import="com.silverpeas.pdc.ejb.PdcBmHome"%>
<%@ page import="com.stratelia.webactiv.quickinfo.control.QuickInfoTransversalSC"%>
<%@ page import="com.stratelia.webactiv.kmelia.KmeliaTransversal"%>

<%@ page import="com.silverpeas.myLinks.model.LinkDetail"%>
<%@ page import="com.silverpeas.myLinks.ejb.MyLinksBm"%>
<%@ page import="com.silverpeas.myLinks.ejb.MyLinksBmHome"%>

<%@ include file="importFrameSet.jsp"%>
<%@ page errorPage="errorpage.jsp"%>

<%
ResourceLocator generalMessage = new ResourceLocator("com.stratelia.webactiv.multilang.generalMultilang", language); 
%>
<%!
private NodeBm getNodeBm() throws Exception {
    NodeBm currentNodeBm = null;
    NodeBmHome nodeBmHome = (NodeBmHome) EJBUtilitaire.getEJBObjectRef(JNDINames.NODEBM_EJBHOME, NodeBmHome.class);
    currentNodeBm = nodeBmHome.create();
    return currentNodeBm;
}

private FavoritBm getFavoriteBm() throws Exception {
    FavoritBm currentFavoriteBm = null;
    FavoritBmHome favoriteBmHome = (FavoritBmHome) EJBUtilitaire.getEJBObjectRef(JNDINames.FAVORITBM_EJBHOME, FavoritBmHome.class);
    currentFavoriteBm = favoriteBmHome.create();
    return currentFavoriteBm;
}


private MyLinksBm getMyLinksBm() throws Exception {
    MyLinksBm currentMyLinksBm = null;
    MyLinksBmHome myLinksBmHome = (MyLinksBmHome) EJBUtilitaire.getEJBObjectRef(JNDINames.MYLINKSBM_EJBHOME, MyLinksBmHome.class);
    currentMyLinksBm = myLinksBmHome.create();
    return currentMyLinksBm;
}

/*
 * Do not remove this functions - usefull to test taglib under Orion
 */
private AdminBm getAdminBm() throws Exception {
	AdminBm bra = null;
	AdminBmHome braHome = (AdminBmHome) EJBUtilitaire.getEJBObjectRef(JNDINames.ADMINBM_EJBHOME, AdminBmHome.class);
	bra = braHome.create();
	return bra;
}

private VersioningBm getVersioningBm() throws Exception {
	VersioningBm vbm = null;
	VersioningBmHome vbmHome = (VersioningBmHome) EJBUtilitaire.getEJBObjectRef(JNDINames.VERSIONING_EJBHOME, VersioningBmHome.class);
	vbm = vbmHome.create();
	return vbm;
}

private PdcBm getPdcBm() throws Exception {
	PdcBm bra = null;
	PdcBmHome braHome = (PdcBmHome) EJBUtilitaire.getEJBObjectRef(JNDINames.PDCBM_EJBHOME, PdcBmHome.class);
	bra = braHome.create();
	return bra;
}


private Collection getFavoritesList(String userId) throws Exception{
    //rechercher tous les liens favoris
    Collection list = getMyLinksBm().getAllLinks(userId);
    return list;
}	

private String s_sUserLogin 	= "%ST_USER_LOGIN%";
private String s_sUserPassword 	= "%ST_USER_PASSWORD%";
private String s_sUserFullName 	= "%ST_USER_FULLNAME%";
private String s_sUserId 		= "%ST_USER_ID%";
private String s_sSessionId		= "%ST_SESSION_ID%";

private String getParsedDestination(String sDestination, String sKeyword, String sValue) {
	int nLoginIndex = sDestination.indexOf(sKeyword);
	if (nLoginIndex != -1) {
		// Replace the keyword with the actual value
		String sParsed = sDestination.substring(0, nLoginIndex);
		sParsed += sValue;
		if (sDestination.length() > nLoginIndex + sKeyword.length())
			sParsed
				+= sDestination.substring(
					nLoginIndex + sKeyword.length(),
					sDestination.length());
		sDestination = sParsed;
	}
	return sDestination;
}
%>

<%
if (m_MainSessionCtrl == null)
{
  String sessionTimeout = GeneralPropertiesManager.getGeneralResourceLocator().getString("sessionTimeout");
  getServletConfig().getServletContext().getRequestDispatcher(sessionTimeout).forward(request, response);
  return;
}

/*
 * Do not remove this functions - usefull to test taglib under Orion
 */
getAdminBm();
getPdcBm();
getMyLinksBm();


String viewPersonalHomePage = request.getParameter("ViewPersonalHomePage");
String space 		= null;
String spaceShort 	= null;	//without "WA"
if (viewPersonalHomePage == null || !viewPersonalHomePage.equals("true")){
	// deal with portlet
	// test if portlet exist for this space.
	space = request.getParameter("SpaceId");
	if (!StringUtil.isDefined(space)) {
		String genSpace = organizationCtrl.getGeneralSpaceId();
		spaceShort = genSpace.substring(2);
	} else {
		spaceShort = space.substring(2);
	}

	if (StringUtil.isDefined(space))
	{
		SpaceInst spaceStruct = organizationCtrl.getSpaceInstById(space);
	
		//Page d'accueil de l'espace = Portlet
		if (spaceStruct != null && (spaceStruct.getFirstPageType() == SpaceInst.FP_TYPE_PORTLET) && (com.stratelia.silverpeas.portlet.SpaceModelFactory.portletAvailable(m_MainSessionCtrl, spaceShort)))
		{
			// if portlet, we've got to forward to portlet page
			response.sendRedirect(GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL") + URLManager.getURL(URLManager.CMP_PORTLET) + "main?spaceId="+spaceShort);
			return;
		}
	
		//Page d'accueil de l'espace = Composant
		if (spaceStruct != null && (spaceStruct.getFirstPageType() == SpaceInst.FP_TYPE_COMPONENT_INST) && spaceStruct.getFirstPageExtraParam() != null && spaceStruct.getFirstPageExtraParam().length() > 0)
		{
			String[] asAvailCompoForCurUser = organizationCtrl.getAvailCompoIds(space, m_MainSessionCtrl.getUserId());
			int      parcComp;
	
			if (asAvailCompoForCurUser != null)
			{
				parcComp = 0;
				while ((parcComp < asAvailCompoForCurUser.length) && (!asAvailCompoForCurUser[parcComp].equals(spaceStruct.getFirstPageExtraParam())))
				{
					parcComp++;
				}
				if ((parcComp < asAvailCompoForCurUser.length) && (organizationCtrl.getComponentInst(spaceStruct.getFirstPageExtraParam()) != null))
				{// If user have the rights to access this componant AND the component exist...
					response.sendRedirect(GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL") + URLManager.getURL(URLManager.CMP_ADMIN) + "goBack.jsp?space=" + space + "&component="+spaceStruct.getFirstPageExtraParam());
					return;
				}
			}
		}
		
		//Page d'accueil de l'espace = URL
		if (spaceStruct != null && (spaceStruct.getFirstPageType() == SpaceInst.FP_TYPE_HTML_PAGE) && (spaceStruct.getFirstPageExtraParam() != null) && (spaceStruct.getFirstPageExtraParam().length() > 0))
		{
			String destination = spaceStruct.getFirstPageExtraParam();
			destination = this.getParsedDestination(destination, s_sUserLogin, m_MainSessionCtrl.getCurrentUserDetail().getLogin());
			destination = this.getParsedDestination(destination, s_sUserFullName, URLEncoder.encode(m_MainSessionCtrl.getCurrentUserDetail().getDisplayedName()));
			destination = this.getParsedDestination(destination, s_sUserId, URLEncoder.encode(m_MainSessionCtrl.getUserId()));
			destination = this.getParsedDestination(destination, s_sSessionId, URLEncoder.encode(request.getSession().getId()));
	  
	  		// !!!! Add the password : this is an uggly patch that use a session variable set in the "AuthenticationServlet" servlet
	  		destination = this.getParsedDestination(destination, s_sUserPassword, (String)session.getAttribute("Silverpeas_pwdForHyperlink"));
	  		
			response.sendRedirect(destination);
		}
		
		// Maintenance Mode
		if (m_MainSessionCtrl.isSpaceInMaintenance(spaceShort) && m_MainSessionCtrl.getUserAccessLevel().equals("U"))
			out.println("<script language=\"javascript\">self.location.href=\"spaceInMaintenance.jsp\"</script>");
	}
}

UserDetail userDetail = m_MainSessionCtrl.getCurrentUserDetail();

AgendaAccess.setCurrentDay(new Date());

Collection favorites = getFavoritesList(userDetail.getId());

ResourceLocator pdcPeasSettings = new ResourceLocator("com.stratelia.silverpeas.pdcPeas.settings.pdcPeasSettings", "");
boolean refreshEnabled = new Boolean(pdcPeasSettings.getString("EnableRefresh", "false")).booleanValue();

Window window = gef.getWindow();
%>
<html>
<head>
<title>Main Page</title>
<% out.println(gef.getLookStyleSheet()); %>
<style type="text/css">
<!--
.box {
	margin: 10px;
}
.enteteBox {
	font-variant: small-caps;
	color: #5889BB;
	font-size: 12px;
	font-weight: bold;
	background-image: url(icons/silverpeasV5/degradeEnteteBox.jpg);
	background-repeat: repeat-y;
	background-position: right;
	border-width: 1px;
	border-style: solid;
	border-color: #DEDEDE;
	border-right-style: none;
	padding-left: 10px;
}
.droiteEnteteBox {
	background-position: right top;
	width: 14px;
	height: 19px;
	background-image: url(icons/silverpeasV5/droiteEnteteBox.gif);
	border-width: 1px;
	border-style: solid;
	border-color: #DEDEDE;
	border-left-style: none;
	border-bottom-style: none;
}
.contenuBox {
	border-right-width: 1px;
	border-left-width: 1px;
	border-right-style: solid;
	border-left-style: solid;
	border-right-color: #dedede;
	border-left-color: #dedede;
}
.basGaucheBox {
	background-image: url(icons/silverpeasV5/basGaucheBox.gif);
	background-repeat: no-repeat;
	background-position: left bottom;
	height: 12px;
	width: 12px;
}
.basMilieuBox {
	border-bottom-width: 1px;
	border-bottom-style: solid;
	border-bottom-color: #dedede;
}
.basDroiteBox {
	background-image: url(icons/silverpeasV5/basDroiteBox.gif);
	background-repeat: no-repeat;
	background-position: right bottom;
	height: 12px;
	width: 12px;
}
-->
</style>
<script type="text/javascript" src="<%=m_sContext%>/util/javaScript/animation.js"></script>
<script>
function editICenter(id) {
        chemin = '<%=m_sContext%>/RpdcSearch/jsp/LoadAdvancedSearch?showNotOnlyPertinentAxisAndValues=true&iCenterId='+id;
		largeur = "600";
		hauteur = "440";
		SP_openWindow(chemin,"",largeur,hauteur,"resizable=yes,scrollbars=yes");
}

function readMessage(id){
	SP_openWindow("<%=m_sContext%>/RSILVERMAIL/jsp/ReadMessage.jsp?ID=" + id + "&from=homePage&SpaceId=<%=request.getParameter("SpaceId")%>","readMessage","600","380","scrollable=yes");
}

function goTo(cUrl, componentId) {
	
	jumpToComponent(componentId);
	
	location.href=cUrl;
}

function jumpToComponent(componentId) {
	if (<%=refreshEnabled%>)
	{
		//Reload DomainsBar
		parent.SpacesBar.document.privateDomainsForm.component_id.value=componentId;
		parent.SpacesBar.document.privateDomainsForm.privateDomain.value="";
		parent.SpacesBar.document.privateDomainsForm.privateSubDomain.value="";
		parent.SpacesBar.document.privateDomainsForm.submit();
		
		//Reload Topbar
		parent.SpacesBar.reloadTopBar(true);
	}
}

</script>
</head>

<body bgcolor="#FFFFFF" leftmargin="0" topmargin="0" marginwidth="0" marginheight="0">
<table width="100%" border="0" cellpadding="5" cellspacing="0">
  <tr>
    <td valign="top" width="50%">
      <!-------------------------------------------------------------------------------------->
      <!------------------------------- Dernières publications ------------------------------->
      <!-------------------------------------------------------------------------------------->
      <%
      	int nbLatestPublications = Integer.parseInt(homePageSettings.getString("latestPublicationsNumber", "5"));
      %>
      <div id="leNomDeLaBoite" class="box">
		<table width="100%" border="0" cellspacing="0" cellpadding="0" class="box">
		  <tr>
		    <td>
		        <table width="100%" border="0" cellspacing="0" cellpadding="0">
		          <tr>
		            <td class="enteteBox"><img src="icons/accueil/picto_publications.gif" width="15" height="15" align="absmiddle"><%=message.getStringWithParam("LatestPublications", String.valueOf(nbLatestPublications))%></td>
		            <td class="droiteEnteteBox"><img src="icons/silverpeasV5/px.gif" width="1" height="1" /></td>
		          </tr>
		        </table>    
		    </td>
		  </tr>
		  <tr>
		    <td class="contenuBox">
		    	<%
								KmeliaTransversal kmeliaTransversal = new KmeliaTransversal(m_MainSessionCtrl);
							    List publications = kmeliaTransversal.getPublications(space, nbLatestPublications);
							    for (int p=0; p<publications.size() && p < nbLatestPublications; p++) 
							    {
							        PublicationDetail 	pub 		= (PublicationDetail) publications.get(p);
									UserDetail 			pubUpdater 	= m_MainSessionCtrl.getOrganizationController().getUserDetail(pub.getUpdaterId());
									String 				url 		= m_sContext+URLManager.getURL("kmelia", null, pub.getPK().getInstanceId())+pub.getURL();
							        out.println("<a href=\"javaScript:goTo('"+url+"','"+pub.getPK().getInstanceId()+"')\"><b>"+pub.getName(language)+"</b></a>");
							        if (pubUpdater != null && pub.getUpdateDate() != null)
							        	out.println("<br>"+pubUpdater.getDisplayedName()+" - "+DateUtil.getOutputDate(pub.getUpdateDate(), language));
							        else if (pubUpdater != null && pub.getUpdateDate() == null)
							        	out.println("<br>"+pubUpdater.getDisplayedName());
							        if (pubUpdater == null && pub.getUpdateDate() != null)
							        	out.println("<br>"+DateUtil.getOutputDate(pub.getUpdateDate(), language));
						            out.println("<BR>");
						            
						            if (p+1 < publications.size() && p+1 < nbLatestPublications)
							            out.println("<BR>");
							    }
    							%>
		    </td>
		  </tr>
		  <tr>
		    <td>
		        <table width="100%" border="0" cellspacing="0" cellpadding="0">
		          <tr>
		            <td class="basGaucheBox"><img src="icons/silverpeasV5/px.gif" width="1" height="1" /></td>
		            <td class="basMilieuBox"><img src="icons/silverpeasV5/px.gif" width="1" height="1" /></td>
		            <td class="basDroiteBox"><img src="icons/silverpeasV5/px.gif" width="1" height="1" /></td>
		          </tr>
		        </table>    
		    </td>
		  </tr>
		</table>
	  </div>
      <!-------------------------------------------------------------------------------------->
      <!------------------------------- Requetes Favorites ----------------------------------->
      <!-------------------------------------------------------------------------------------->
      <div id="leNomDeLaBoite" class="box">
		<table width="100%" border="0" cellspacing="0" cellpadding="0" class="box">
		  <tr>
		    <td>
		        <table width="100%" border="0" cellspacing="0" cellpadding="0">
		          <tr>
		            <td class="enteteBox"><img src="icons/accueil/picto_requetes.gif" width="15" height="15" align="absmiddle"/><%=message.getString("MyPDCSubscriptions")%></td>
		            <td class="droiteEnteteBox"><img src="icons/silverpeasV5/px.gif" width="1" height="1" /></td>
		          </tr>
		        </table>    
		    </td>
		  </tr>
		  <tr>
		    <td class="contenuBox">
		    	<table border="0" cellpadding="4" width="100%" cellspacing="0">
                     <%
                          ArrayList iCentersList = (new InterestCenterUtil()).getICByUserId(Integer.parseInt(userDetail.getId()));
						  int j = 0;
						  if (iCentersList != null && iCentersList.size() > 0 )
						  {
								Iterator i = iCentersList.iterator();
								String icLink = m_sContext+"/RpdcSearch/jsp/AdvancedSearch?urlToRedirect=" +
									  URLEncoder.encode(m_sContext + "/admin/jsp/Main.jsp?ViewPersonalHomePage=true") + "&icId=";

								while (i.hasNext()) {
									InterestCenter ic = (InterestCenter) i.next();
									if (j == 0)
										out.println("<tr>");
									out.println("<td width='50%'>&#149; <a href='" + icLink + ic.getId() + "'>" + ic.getName() + "</a></td>");
									if (j != 0)
									{
										out.println("</tr>");
										j=0;
									}
									else
									{
										j=1;
									}
								}
						  } else {
								out.println("<tr><td width='100%'>"+message.getString("NoPDCSubscriptions")+"</td></tr>");
						  }
					%>
					</table>
		    </td>
		  </tr>
		  <tr>
		    <td>
		        <table width="100%" border="0" cellspacing="0" cellpadding="0">
		          <tr>
		            <td class="basGaucheBox"><img src="icons/silverpeasV5/px.gif" width="1" height="1" /></td>
		            <td class="basMilieuBox"><img src="icons/silverpeasV5/px.gif" width="1" height="1" /></td>
		            <td class="basDroiteBox"><img src="icons/silverpeasV5/px.gif" width="1" height="1" /></td>
		          </tr>
		        </table>    
		    </td>
		  </tr>
		</table>
		</div>
      <!-------------------------------------------------------------------------------------->
      <!------------------------------- Mes prochains evenements ----------------------------->
      <!-------------------------------------------------------------------------------------->
      <%
    	int nbLatestEvents = Integer.parseInt(homePageSettings.getString("latestEventsNumber", "5"));
      	int nbEvents = nbLatestEvents;
      %>
      <div id="leNomDeLaBoite" class="box">
		<table width="100%" border="0" cellspacing="0" cellpadding="0" class="box">
		  <tr>
		    <td>
		        <table width="100%" border="0" cellspacing="0" cellpadding="0">
		          <tr>
		            <td class="enteteBox"><a href="<%=m_sContext%>/Ragenda/jsp/agenda.jsp"><img src="icons/agenda.gif" width="15" height="15" align="absmiddle" border="0"/></a><%=message.getString("MyNextEvent")%></td>
		            <td class="droiteEnteteBox"><img src="icons/silverpeasV5/px.gif" width="1" height="1" /></td>
		          </tr>
		        </table>    
		    </td>
		  </tr>
		  <tr>
		    <td class="contenuBox">
		    	<%
                    List tasks = (List) AgendaAccess.getNextDaySchedulables(userDetail.getId());
                    if (tasks.size() == 0) 
                    {
                      out.println(message.getString("NoEvents")+"<BR>");
                    } 
                    else 
                    {
                    	// si on a moins de 5 évènements 
                    	if (tasks.size() < nbLatestEvents) 
                    		nbEvents = tasks.size();
                      
                      	// convertir la date du jour 
                      	Calendar today = Calendar.getInstance();
                      	today.setTime(new Date());
                      	today.set(Calendar.HOUR_OF_DAY, 0);
                      	today.set(Calendar.MINUTE, 0);
                      	today.set(Calendar.SECOND, 0);
                      	today.set(Calendar.MILLISECOND, 0);
						
                      	// convertir la date de demain
                      	Calendar tomorrow = Calendar.getInstance();
                      	tomorrow.add(Calendar.DATE,1);
                      	tomorrow.set(Calendar.HOUR_OF_DAY, 0);
                      	tomorrow.set(Calendar.MINUTE, 0);
                      	tomorrow.set(Calendar.SECOND, 0);
                      	tomorrow.set(Calendar.MILLISECOND, 0);
                      
                      	for (int i=0 ; i<nbEvents ; i++) 
                      	{
                          	Schedulable task = (Schedulable) tasks.get(i);
                          	
                          	// convertir la date de l'évènement
                        	Calendar taskDate = Calendar.getInstance();
                        	taskDate.setTime(task.getStartDate());
                        	taskDate.set(Calendar.HOUR_OF_DAY, 0);
                        	taskDate.set(Calendar.MINUTE, 0);
                        	// formatage de la date sous forme jj/mm/aaaa pour paramètre de agenda.jsp
                        	String date = DateUtil.getInputDate(task.getStartDate(), language);
                        	if (today.equals(taskDate))
                        	{
                        		// évènement du jour
                        		if ( task.getStartHour() != null)
                        		  	out.println("&#149; " + message.getString("today") + ", "+task.getStartHour() + " - " + task.getEndHour() + " : <a href=\""+m_sContext+URLManager.getURL(URLManager.CMP_AGENDA)+"agenda.jsp?Action=SelectDay&Day="+date+"\">" + task.getName() + "</a><BR>");
                        		else
                        			out.println("&#149; " + message.getString("today") + " : <a href=\""+m_sContext+URLManager.getURL(URLManager.CMP_AGENDA)+"agenda.jsp?Action=SelectDay&Day="+date+"\">" + task.getName() + "</a><BR>");
                        	}
                        	else if (tomorrow.equals(taskDate))
                        	{
                        		// évènement du lendemain
                        		if ( task.getStartHour() != null)
                        		  	out.println("&#149; " + message.getString("tomorrow") + ", "+task.getStartHour() + " - " + task.getEndHour() + " : <a href=\""+m_sContext+URLManager.getURL(URLManager.CMP_AGENDA)+"agenda.jsp?Action=SelectDay&Day="+date+"\">" + task.getName() + "</a><BR>");    
                        		else
                           		  	out.println("&#149; " + message.getString("tomorrow") + " : <a href=\""+m_sContext+URLManager.getURL(URLManager.CMP_AGENDA)+"agenda.jsp?Action=SelectDay&Day="+date+"\">" + task.getName() + "</a><BR>");    
                        	}
                        	else          
                        	{
                        	  	// recherche du libellé du jour
                        		int day = taskDate.get(Calendar.DAY_OF_WEEK);
                        	 	String jour = "GML.jour" + day;
								// recherche du libellé du mois
                        		int month = taskDate.get(Calendar.MONTH);
                        	  	String mois = "GML.mois" + month;
                        	  	if ( task.getStartHour() != null)
                        	   		out.println("&#149; "+ generalMessage.getString(jour)+ " " + taskDate.get(Calendar.DATE) +" " + generalMessage.getString(mois) + " " + taskDate.get(Calendar.YEAR) + ", " + task.getStartHour() + " - " + task.getEndHour() + " : <a href=\""+m_sContext+URLManager.getURL(URLManager.CMP_AGENDA)+"agenda.jsp?Action=SelectDay&Day="+date+"\">" + task.getName() + "</a><BR>");
                        	  	else
                     		  		out.println("&#149; "+ generalMessage.getString(jour)+ " " + taskDate.get(Calendar.DATE) +" " + generalMessage.getString(mois) + " " + taskDate.get(Calendar.YEAR) + " : " + "<a href=\""+m_sContext+URLManager.getURL(URLManager.CMP_AGENDA)+"agenda.jsp?Action=SelectDay&Day="+date+"\">" + task.getName() + "</a><BR>");
                          	}
                      	}
                    }
                    out.flush();
                    %>
			</td>
		  </tr>
		  <tr>
		    <td>
		        <table width="100%" border="0" cellspacing="0" cellpadding="0">
		          <tr>
		            <td class="basGaucheBox"><img src="icons/silverpeasV5/px.gif" width="1" height="1" /></td>
		            <td class="basMilieuBox"><img src="icons/silverpeasV5/px.gif" width="1" height="1" /></td>
		            <td class="basDroiteBox"><img src="icons/silverpeasV5/px.gif" width="1" height="1" /></td>
		          </tr>
		        </table>    
		    </td>
		  </tr>
		</table>
		</div>
      <!-------------------------------------------------------------------------------------->
      <!------------------------------- Todos ------------------------------------------------>
      <!-------------------------------------------------------------------------------------->
      <div id="leNomDeLaBoite" class="box">
		<table width="100%" border="0" cellspacing="0" cellpadding="0" class="box">
		  <tr>
		    <td>
		        <table width="100%" border="0" cellspacing="0" cellpadding="0">
		          <tr>
		            <td class="enteteBox"><a href="<%=m_sContext%>/Rtodo/jsp/todo.jsp"><img src="icons/todo.gif" width="15" height="15" align="absmiddle" border="0"/></a><%=message.getString("ToDo")%></td>
		            <td class="droiteEnteteBox"><img src="icons/silverpeasV5/px.gif" width="1" height="1" /></td>
		          </tr>
		        </table>    
		    </td>
		  </tr>
		  <tr>
		    <td class="contenuBox">
		    	<%
                    	Collection todos = ToDoAccess.getNotCompletedToDos(userDetail.getId());
                    	if (todos.size() == 0) {
                    		out.println(message.getString("NoTodos")+"<BR>");
                    	} else {
                    		Iterator iterator = todos.iterator();
                    		while (iterator.hasNext()) {
                    			ToDoHeader todo = (ToDoHeader) iterator.next();
                    			if (todo.getPercentCompleted() != 100) {
                    				if (todo.getPercentCompleted() != -1)
                    					out.println("&#149; <a href=\""+m_sContext+URLManager.getURL(URLManager.CMP_TODO)+"todo.jsp\">" + todo.getName() + "</a> <i>("+todo.getPercentCompleted()+"%)</i><BR>");
                    				else
                    					out.println("&#149; <a href=\""+m_sContext+URLManager.getURL(URLManager.CMP_TODO)+"todo.jsp\">" + todo.getName() + "</a> <i>(0%)</i><BR>");
                    			}
                    		}
                    	}
                    	out.flush();
                    %>
			</td>
		  </tr>
		  <tr>
		    <td>
		        <table width="100%" border="0" cellspacing="0" cellpadding="0">
		          <tr>
		            <td class="basGaucheBox"><img src="icons/silverpeasV5/px.gif" width="1" height="1" /></td>
		            <td class="basMilieuBox"><img src="icons/silverpeasV5/px.gif" width="1" height="1" /></td>
		            <td class="basDroiteBox"><img src="icons/silverpeasV5/px.gif" width="1" height="1" /></td>
		          </tr>
		        </table>    
		    </td>
		  </tr>
		</table>
		</div>
      <!-------------------------------------------------------------------------------------->
      <!------------------------------- Notifications ---------------------------------------->
      <!-------------------------------------------------------------------------------------->
      <div id="leNomDeLaBoite" class="box">
		<table width="100%" border="0" cellspacing="0" cellpadding="0" class="box">
		  <tr>
		    <td>
		        <table width="100%" border="0" cellspacing="0" cellpadding="0">
		          <tr>
		            <td class="enteteBox"><a href="<%=m_sContext%>/RSILVERMAIL/jsp/Main"><img src="icons/notifications.gif" width="15" height="15" align="absmiddle" border="0"/></a><%=message.getString("Mail")%></td>
		            <td class="droiteEnteteBox"><img src="icons/silverpeasV5/px.gif" width="1" height="1" /></td>
		          </tr>
		        </table>    
		    </td>
		  </tr>
		  <tr>
		    <td class="contenuBox">
		    	<%
				  SILVERMAILUtil silvermailUtil = new SILVERMAILUtil(m_MainSessionCtrl.getUserId(), language);
				  // Arraypane
				  ArrayPane list = gef.getArrayPane("silvermail", "Main.jsp?SpaceId="+space, request,session );
				  ArrayColumn col = list.addArrayColumn( message.getString("notification.date") );
				  col = list.addArrayColumn( message.getString("notification.source") );
				  col = list.addArrayColumn( message.getString("notification.from") );
				  col = list.addArrayColumn( message.getString("notification.url") );
				  col = list.addArrayColumn( message.getString("notification.subject") );

				  Iterator messageIterator = silvermailUtil.getFolderMessageList( "INBOX" ).iterator();
				  String	hasBeenReadenOrNotBegin	= "";
				  String	hasBeenReadenOrNotEnd	= "";

				  while( messageIterator.hasNext() == true )
				  {
					hasBeenReadenOrNotBegin = "";
					hasBeenReadenOrNotEnd = "";
					SILVERMAILMessage smMessage = (SILVERMAILMessage)messageIterator.next();
					if (smMessage.getReaden() == 0) {
						hasBeenReadenOrNotBegin = "<B>";
						hasBeenReadenOrNotEnd = "</B>";
					}

					String link = "<A HREF =\"javascript:onClick=readMessage(" + smMessage.getId() + ");\">";
					ArrayLine line = list.addArrayLine();
					Date date = smMessage.getDate();
					ArrayCellText cell1 = line.addArrayCellText(hasBeenReadenOrNotBegin + DateUtil.getOutputDate(date, language) + hasBeenReadenOrNotEnd );
					cell1.setCompareOn(date);
					
					ArrayCellText cell2 = line.addArrayCellText(hasBeenReadenOrNotBegin + Encode.javaStringToHtmlString(smMessage.getSource()) + "</A>" + hasBeenReadenOrNotEnd );
					cell2.setCompareOn(smMessage.getSource());
					
					ArrayCellText cell3 = line.addArrayCellText(hasBeenReadenOrNotBegin + link + Encode.javaStringToHtmlString(smMessage.getSenderName()) + "</A>" + hasBeenReadenOrNotEnd );
					cell3.setCompareOn(smMessage.getSenderName());

					if ( smMessage.getUrl()!=null && smMessage.getUrl().length()>0 )
						line.addArrayCellText(hasBeenReadenOrNotBegin + "<A HREF =\"" + Encode.javaStringToHtmlString(smMessage.getUrl()) + "\" target=_top><img src=\""+m_sContext+"/util/icons/Lien.gif\" border=\"0\"></A>" + hasBeenReadenOrNotEnd );
					else
						line.addArrayCellText( "" );
					ArrayCellText cell5 = line.addArrayCellText(hasBeenReadenOrNotBegin + link + Encode.javaStringToHtmlString(smMessage.getSubject()) + "</A>" + hasBeenReadenOrNotEnd );
					cell5.setCompareOn(smMessage.getSubject());
				  }
				  out.println(list.print());
				  out.flush();
				%>
		    </td>
		  </tr>
		  <tr>
		    <td>
		        <table width="100%" border="0" cellspacing="0" cellpadding="0">
		          <tr>
		            <td class="basGaucheBox"><img src="icons/silverpeasV5/px.gif" width="1" height="1" /></td>
		            <td class="basMilieuBox"><img src="icons/silverpeasV5/px.gif" width="1" height="1" /></td>
		            <td class="basDroiteBox"><img src="icons/silverpeasV5/px.gif" width="1" height="1" /></td>
		          </tr>
		        </table>    
		    </td>
		  </tr>
		</table>
		</div>
<!-- end of user's notification -->
    </td>  
    <td valign="top" width="50%">
      <!-------------------------------------------------------------------------------------->
      <!------------------------ Liens Favoris ----------------------------------------------->
      <!-------------------------------------------------------------------------------------->
      <div id="leNomDeLaBoite" class="box">
		<table width="100%" border="0" cellspacing="0" cellpadding="0" class="box">
		  <tr>
		    <td>
		        <table width="100%" border="0" cellspacing="0" cellpadding="0">
		          <tr>
		            <td class="enteteBox"><a href="<%=m_sContext%>/RmyLinksPeas/jsp/Main"><img src="icons/favorits.gif" width="15" height="15" align="absmiddle" border="0"/></a><%=message.getString("MyFavorites")%></td>
		            <td class="droiteEnteteBox"><img src="icons/silverpeasV5/px.gif" width="1" height="1" /></td>
		          </tr>
		        </table>    
		    </td>
		  </tr>
		  <tr>
		    <td class="contenuBox">
		    	<%
                    	if (favorites.size() == 0) {
                    		out.println(message.getString("NoFavorites"));
                    	}
                    	else 
                    	{
                    		//affichage des liens favoris de l'utilisateur
                    		Iterator it = (Iterator) favorites.iterator();
                    		while (it.hasNext()) 
                    		{
								LinkDetail link = (LinkDetail) it.next();
								if (link.isVisible())
								{
									// afficher que les liens que l'utilisateur a topé "visible en page d'accueil"
									String lien = link.getUrl();
									String name = link.getName();
									if (name.equals(""))
										name = lien;
									// ajouter le context devant le lien si nécéssaire
									if (lien.indexOf("://") == -1)
									{
										String context = URLManager.getApplicationURL();
										lien = context + lien;
									}
									String popup = "";
									// regarder si on doit l'ouvrir dans une autre fenêtre 
									if (link.isPopup())
										popup = "target=_blank";
									out.println("&#149; <a href='" + lien + "' " + popup + ">" + name + "</a>");
									out.println("<br>");
								}
                    		}
                    	}
                    	out.flush();
                  %>
		    </td>
		  </tr>
		  <tr>
		    <td>
		        <table width="100%" border="0" cellspacing="0" cellpadding="0">
		          <tr>
		            <td class="basGaucheBox"><img src="icons/silverpeasV5/px.gif" width="1" height="1" /></td>
		            <td class="basMilieuBox"><img src="icons/silverpeasV5/px.gif" width="1" height="1" /></td>
		            <td class="basDroiteBox"><img src="icons/silverpeasV5/px.gif" width="1" height="1" /></td>
		          </tr>
		        </table>    
		    </td>
		  </tr>
		</table>
		</div>
      <!-------------------------------------------------------------------------------------->
      <!------------------------------- Fichiers réservés ------------------------------------>
      <!-------------------------------------------------------------------------------------->
      <%
      boolean full = false;
      Vector attachment = (new AttachmentBmImpl()).getAttachmentsByWorkerId(userDetail.getId());
      List versioning = getVersioningBm().getAllFilesReserved(Integer.parseInt(userDetail.getId()));
      
	  if ((attachment != null && attachment.size() > 0) || (versioning != null && versioning.size() > 0))
	  {
		  %>
	  	<br>
	  	<div id="leNomDeLaBoite" class="box">
		<table width="100%" border="0" cellspacing="0" cellpadding="0" class="box">
		  <tr>
		    <td>
		        <table width="100%" border="0" cellspacing="0" cellpadding="0">
		          <tr>
		            <td class="enteteBox"><img src="<%=m_sContext%>/util/icons/lock.gif" width="15" height="15" align="absmiddle"/><%=message.getString("MyAttachment")%></td>
		            <td class="droiteEnteteBox"><img src="icons/silverpeasV5/px.gif" width="1" height="1" /></td>
		          </tr>
		        </table>    
		    </td>
		  </tr>
		  <tr>
		    <td class="contenuBox">
		    	<%                 
                     	// convertir la date du jour 
		                Calendar today = Calendar.getInstance();
		                today.setTime(new Date());
		                today.set(Calendar.HOUR_OF_DAY, 0);
		                today.set(Calendar.MINUTE, 0);
		                today.set(Calendar.SECOND, 0);
		                today.set(Calendar.MILLISECOND, 0);
								
		                // convertir la date de demain
		                Calendar tomorrow = Calendar.getInstance();
		                tomorrow.add(Calendar.DATE,1);
		                tomorrow.set(Calendar.HOUR_OF_DAY, 0);
		                tomorrow.set(Calendar.MINUTE, 0);
		                tomorrow.set(Calendar.SECOND, 0);
		                tomorrow.set(Calendar.MILLISECOND, 0);
		                
                   		// traitement des liens vers les fichiers joints
 						if (attachment != null && attachment.size() > 0 ) 
						{
 							Iterator i = attachment.iterator();
							while (i.hasNext()) 
							{
								AttachmentDetail att = (AttachmentDetail) i.next();
								String url 	= m_sContext+URLManager.getURL(null,null,att.getPK().getInstanceId())+"GoToFilesTab?Id="+att.getForeignKey().getId();
								String name = att.getTitle();
								if (name == null || name.trim().equals(""))
									name = att.getLogicalName();
							
								if (att.getExpiryDate() != null)
								{
									// convertir la date de l'évènement
			                        Calendar atDate = Calendar.getInstance();
			                        atDate.setTime(att.getExpiryDate());
			                        atDate.set(Calendar.HOUR_OF_DAY, 0);
			                        atDate.set(Calendar.MINUTE, 0);

			                        // formatage de la date sous forme jj/mm/aaaa 
			                        String date = DateUtil.getInputDate(att.getExpiryDate(), language);
			                        if (today.equals(atDate))
			                        {
			                        	// évènement du jour
		                        		out.println("&#149; <a href=\"javaScript:goTo('"+url+"','"+att.getPK().getInstanceId()+"')\">"+name+"</a> " + " (" + message.getString("today") + ") <BR>");
			                        }
			                        else if (tomorrow.equals(atDate))
			                        {
			                        	// évènement du lendemain
		                           	  	out.println("&#149; <a href=\"javaScript:goTo('"+url+"','"+att.getPK().getInstanceId()+"')\">"+name+"</a> " + " (" + message.getString("tomorrow") + ") <BR>");    
			                        }
			                        else          
			                        {
			                          	// recherche du libellé du jour
			                        	int day = atDate.get(Calendar.DAY_OF_WEEK);
			                         	String jour = "GML.jour" + day;
										// recherche du libellé du mois
			                        	int month = atDate.get(Calendar.MONTH);
			                          	String mois = "GML.mois" + month;
			                   			out.println("&#149; <a href=\"javaScript:goTo('"+url+"','"+att.getPK().getInstanceId()+"')\">"+name+"</a> " + " (" + generalMessage.getString(jour)+ " " + atDate.get(Calendar.DATE) +" " + generalMessage.getString(mois) + " " + atDate.get(Calendar.YEAR) + ") " + "<BR>");
			                        }
								}
								else
								{
									// affichage sans la date
		                   			out.println("&#149; " + "<a href=\"javaScript:goTo('"+url+"','"+att.getPK().getInstanceId()+"')\">"+name+"</a><BR>");

								}
							}
						}
						// traitement des liens vers les fichiers joints versionnés
						if (versioning != null && versioning.size() > 0 ) 
						{
							Iterator i = versioning.iterator();
							while (i.hasNext()) 
							{
								Document doc = (Document) i.next();
								String url 	= m_sContext+URLManager.getURL(null,null,doc.getPk().getInstanceId())+"GoToFilesTab?Id="+doc.getForeignKey().getId();
								String name = doc.getName(); 					
									
								
								if (doc.getExpiryDate() != null)
								{
									// convertir la date de l'évènement
			                        Calendar veDate = Calendar.getInstance();
			                        veDate.setTime(doc.getExpiryDate());
			                        veDate.set(Calendar.HOUR_OF_DAY, 0);
			                        veDate.set(Calendar.MINUTE, 0);

			                        // formatage de la date sous forme jj/mm/aaaa 
			                        String date = DateUtil.getInputDate(doc.getExpiryDate(), language);
			                        if (today.equals(veDate))
			                        {
			                        	// évènement du jour
		                        		out.println("&#149; <a href=\"javaScript:goTo('"+url+"','"+doc.getPk().getInstanceId()+"')\">"+name+"</a>" + " (" + message.getString("today") + ") <BR>");
			                        }
			                        else if (tomorrow.equals(veDate))
			                        {
			                        	// évènement du lendemain
		                        		out.println("&#149; <a href=\"javaScript:goTo('"+url+"','"+doc.getPk().getInstanceId()+"')\">"+name+"</a>" + " (" + message.getString("tomorrow") + ") <BR>");
				                        }
			                        else          
			                        {
			                          	// recherche du libellé du jour
			                        	int day = veDate.get(Calendar.DAY_OF_WEEK);
			                         	String jour = "GML.jour" + day;
										// recherche du libellé du mois
			                        	int month = veDate.get(Calendar.MONTH);
			                          	String mois = "GML.mois" + month;
			                   			out.println("&#149; <a href=\"javaScript:goTo('"+url+"','"+doc.getPk().getInstanceId()+"')\">"+name+"</a>" + " ("+ generalMessage.getString(jour)+ " " + veDate.get(Calendar.DATE) +" " + generalMessage.getString(mois) + " " + veDate.get(Calendar.YEAR) + ") <BR>");
			                        }
								}
								else
								{
									out.println("&#149; <a href=\"javaScript:goTo('"+url+"','"+doc.getPk().getInstanceId()+"')\">"+name+"</a><br>");
								}
							}
						}
					%>
		    </td>
		  </tr>
		  <tr>
		    <td>
		        <table width="100%" border="0" cellspacing="0" cellpadding="0">
		          <tr>
		            <td class="basGaucheBox"><img src="icons/silverpeasV5/px.gif" width="1" height="1" /></td>
		            <td class="basMilieuBox"><img src="icons/silverpeasV5/px.gif" width="1" height="1" /></td>
		            <td class="basDroiteBox"><img src="icons/silverpeasV5/px.gif" width="1" height="1" /></td>
		          </tr>
		        </table>    
		    </td>
		  </tr>
		</table>
		</div>
      	 <%
	  	out.flush();
		} 
	  %>
	  <!-------------------------------------------------------------------------------------->
      <!------------------------------- Quick Infos ------------------------------------------>
      <!-------------------------------------------------------------------------------------->
      <div id="leNomDeLaBoite" class="box">
		<table width="100%" border="0" cellspacing="0" cellpadding="0" class="box">
		  <tr>
		    <td>
		        <table width="100%" border="0" cellspacing="0" cellpadding="0">
		          <tr>
		            <td class="enteteBox"><img src="icons/quickinfos.gif" width="15" height="15" align="absmiddle"/><%=message.getString("QuickInfos", "Quick infos")%></td>
		            <td class="droiteEnteteBox"><img src="icons/silverpeasV5/px.gif" width="1" height="1" /></td>
		          </tr>
		        </table>    
		    </td>
		  </tr>
		  <tr>
		    <td class="contenuBox">
		    	<%
					QuickInfoTransversalSC quickinfoTransversal = new QuickInfoTransversalSC();
				    quickinfoTransversal.init(m_MainSessionCtrl);
				    Collection quickinfos = quickinfoTransversal.getAllQuickInfos();
				    String description = "";
				    for (Iterator i = quickinfos.iterator(); i.hasNext(); ) 
				    {
				        PublicationDetail pub = (PublicationDetail) i.next();
						UserDetail pubCreator = m_MainSessionCtrl.getOrganizationController().getUserDetail(pub.getCreatorId());
						
				        out.println("<table cellpadding=3 cellspacing=0 border=0 width=\"100%\"><tr><td class=intfdcolor><span class=textePetitBold>" + pub.getName(language) + "</span><br>"+pubCreator.getDisplayedName()+" - "+DateUtil.getOutputDate(pub.getCreationDate(), language)+"</td></tr></table>");
						out.println("<table cellpadding=1 cellspacing=0 border=0 class=intfdcolor width=\"100%\"><tr><td>");
				        out.println("<table cellpadding=3 cellspacing=0 border=0 class=intfdcolor4 width=\"100%\"><tr><td>");
						
				        if (pub.getWysiwyg() != null && !"".equals(pub.getWysiwyg()))
				        	description = pub.getWysiwyg();
						else if (pub.getDescription(language) != null && !pub.getDescription(language).equals(""))
							description = Encode.javaStringToHtmlParagraphe(pub.getDescription(language));
				        
						out.println("<span class=txtnote>" + description +"</span>");
				
				        out.println("</td></tr></table>");
				        out.println("</td></tr></table>");
				            
				        if (i.hasNext())
					    	out.println("<BR>");
				    }
				    out.flush();
 							%>
		    </td>
		  </tr>
		  <tr>
		    <td>
		        <table width="100%" border="0" cellspacing="0" cellpadding="0">
		          <tr>
		            <td class="basGaucheBox"><img src="icons/silverpeasV5/px.gif" width="1" height="1" /></td>
		            <td class="basMilieuBox"><img src="icons/silverpeasV5/px.gif" width="1" height="1" /></td>
		            <td class="basDroiteBox"><img src="icons/silverpeasV5/px.gif" width="1" height="1" /></td>
		          </tr>
		        </table>    
		    </td>
		  </tr>
		</table>
		</div>
    </td>
  </tr>
</table>
</body>
</html>