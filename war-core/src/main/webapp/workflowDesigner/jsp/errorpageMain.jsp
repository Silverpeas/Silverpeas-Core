<%
    if (response.isCommitted() == false)
        response.resetBuffer();
%>
<%--
 % This page is invoked when an error happens at the server.  The
 % error details are available in the explicit 'exception' object.
--%>

<%@ page import="javax.servlet.*"%>
<%@ page import="javax.servlet.http.*"%>
<%@ page import="javax.servlet.jsp.*"%>
<%@ page import="java.io.PrintWriter"%>
<%@ page import="java.io.IOException"%>
<%@ page import="java.io.FileInputStream"%>
<%@ page import="java.io.ObjectInputStream"%>
<%@ page import="java.util.Vector"%>
<%@ page import="java.beans.*"%>

<%@ page import="javax.ejb.EJBException, javax.ejb.FinderException, javax.ejb.NoSuchEntityException, java.rmi.RemoteException, java.sql.SQLException, javax.ejb.RemoveException, javax.ejb.CreateException, javax.naming.NamingException, javax.transaction.TransactionRolledbackException"%>
<%@ page import="java.util.Collection, java.util.Iterator, java.lang.Throwable"%>
<%@ page import="com.stratelia.webactiv.util.*"%>
<%@ page import="com.stratelia.silverpeas.silvertrace.*"%>
<%@ page import="com.stratelia.webactiv.util.exception.*"%>
<%@ page import="com.stratelia.webactiv.homepage.*"%>
<%@ page import="com.stratelia.silverpeas.peasCore.MainSessionController"%>

<%@ include file="check.jsp" %>

<% 
    MainSessionController m_MainSessionCtrl = (MainSessionController) session.getAttribute("SilverSessionController");
    String language = m_MainSessionCtrl.getFavoriteLanguage();
    ResourceLocator generalMessage = GeneralPropertiesManager.getGeneralMultilang(language);
	Exception exception = (Exception) request.getAttribute("javax.servlet.jsp.jspException");
	Throwable toDisplayException = HomePageUtil.getExceptionToDisplay(exception);
	String exStr = HomePageUtil.getMessageToDisplay(exception , language);
	String detailedString = null;
    
    if ( exception instanceof SilverpeasException )
        detailedString = ( (SilverpeasException)exception ).getExtraInfos();
%>
<HTML>
<HEAD>
<TITLE><%= generalMessage.getString("GML.popupTitle")%></TITLE>
<%
out.println(gef.getLookStyleSheet());
%>
</HEAD>

<BODY marginwidth=5 marginheight=5 leftmargin=5 topmargin=5>
<%
					out.println(window.printBefore());
					out.println(frame.printBefore());
%>
<CENTER>
<table CELLPADDING=0 CELLSPACING=2 BORDER=0 WIDTH="98%" CLASS=intfdcolor>
	<tr>
		<td CLASS=intfdcolor4 NOWRAP>
			<center>
				<br>
				<span class="txtnav">
					<% if (exStr != null)
                       {
                           out.print(exStr);
                       }
                    
                       if ( detailedString != null )
                       {
                           out.println("<br>");
                           out.println(detailedString);
                       }
                    %>
				</span>
			</center>
			<br>
		</td>
	</tr>
	<!--  /form -->
</table>
<br>
<%
	      ButtonPane buttonPane = gef.getButtonPane();
          buttonPane.addButton((Button) gef.getFormButton(generalMessage.getString("GML.back"), "javascript:history.go(-1);", false));
          out.println(buttonPane.print());
%>
</CENTER>
<%
                out.println(frame.printAfter());
                out.println(window.printAfter());
%>
</BODY>
</HTML>