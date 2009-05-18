<%@ page import="javax.ejb.RemoveException, javax.ejb.CreateException, javax.ejb.EJBException, java.sql.SQLException, javax.naming.NamingException, java.rmi.RemoteException, javax.ejb.FinderException"%>
<%@ page import="java.util.Collection, java.util.Iterator, java.util.ArrayList, java.util.Date"%>
<%@ page import="javax.servlet.*"%>
<%@ page import="javax.servlet.http.*"%>
<%@ page import="javax.servlet.jsp.*"%>
<%@ page import="java.io.PrintWriter"%>
<%@ page import="java.io.IOException"%>
<%@ page import="java.io.FileInputStream"%>
<%@ page import="java.io.ObjectInputStream"%>
<%@ page import="java.util.Vector"%>
<%@ page import="java.beans.*"%>
<%@ page import="java.util.Date"%>
<%@ page import="java.lang.String"%>
<%@ page import="java.util.*"%>

<%@ page import="com.stratelia.webactiv.util.*"%>
<%@ page import="com.stratelia.webactiv.homepage.*"%>
<%@ page import="com.stratelia.webactiv.util.ResourceLocator"%>
<%@ page import="com.stratelia.webactiv.util.publication.model.PublicationDetail"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.window.Window"%>
<%@ page import="com.stratelia.webactiv.util.favorit.control.*, com.stratelia.webactiv.util.node.model.NodePK, com.stratelia.webactiv.util.node.control.*, com.stratelia.webactiv.util.node.model.*, com.stratelia.webactiv.util.exception.* "%>

<%@ page import="com.stratelia.silverpeas.peasCore.URLManager"%>
<%@ page import="com.stratelia.silverpeas.peasCore.MainSessionController"%>

<%@ page import="com.stratelia.webactiv.beans.admin.OrganizationController"%>
<%@ page import="com.stratelia.webactiv.beans.admin.SpaceInst"%>
<%@ page import="com.stratelia.webactiv.beans.admin.SpaceInstLight"%>
<%@ page import="com.stratelia.webactiv.beans.admin.ComponentInst"%>
<%@ page import="com.stratelia.webactiv.beans.admin.ComponentInstLight"%>
<%@ page import="com.stratelia.webactiv.beans.admin.UserDetail"%>

<%
MainSessionController 	m_MainSessionCtrl 	= (MainSessionController) session.getAttribute("SilverSessionController");
OrganizationController 	organizationCtrl 	= null;
GraphicElementFactory 	gef 				= null;
String 					language 			= null;
ResourceLocator 		message 			= null;
ResourceLocator 		homePageSettings 	= null;
String 					m_sContext 			= null;
if (m_MainSessionCtrl == null)
{
%>
	<script> 
		top.location="../../Login.jsp";
	</script>
<%
}
else
{
	organizationCtrl 	= m_MainSessionCtrl.getOrganizationController();
	gef 				= (GraphicElementFactory) session.getAttribute("SessionGraphicElementFactory");
	
	language 			= m_MainSessionCtrl.getFavoriteLanguage();
	message 			= new ResourceLocator("com.stratelia.webactiv.homePage.multilang.homePageBundle", language);
	homePageSettings 	= new ResourceLocator("com.stratelia.webactiv.homePage.homePageSettings", "");
	
	m_sContext 			= GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");
}
%>