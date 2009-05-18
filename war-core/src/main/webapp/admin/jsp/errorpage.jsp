<%
    if (response.isCommitted() == false)
        response.resetBuffer();
%>

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

<%
  	//test si la page source n'est pas Main
	String uri = (String)request.getAttribute("com.stratelia.webactiv.servlets.ComponentRequestRouter.requestURI");
	if (uri == null || (uri != null && uri.indexOf("/Main") != -1)) {
		// le cas echeant, l'erreur est affichee dans la page
		getServletConfig().getServletContext().getRequestDispatcher("/admin/jsp/errorpageMain.jsp").forward(request, response);
		return;
	}
%>

<%@ include file="import.jsp" %>

<%
    Throwable exception = (Throwable) request.getAttribute("javax.servlet.jsp.jspException");
	Throwable toDisplayException = HomePageUtil.getExceptionToDisplay(exception);
	String exStr = HomePageUtil.getMessageToDisplay(exception , language);
	String detailedString = HomePageUtil.getMessagesToDisplay(exception , language);

    // Trace the exception
    HomePageUtil.traceException(exception);
%>

<html>
	<head>
        <script language="JavaScript">
            function displayPopup()
            {
                SP_openWindow("<%=m_context%>/admin/jsp/popupError.jsp","popup","650","180","");
            }
        </script>
		<title></title>
		<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
	</head>
	<body onload="javascript:displayPopup()">
		<center>
			<form name="formulaire" action="Main.jsp" method="post">
				<input type="Hidden" name="message" value="<% if (exStr != null){out.print(exStr);}%>">
				<input type="Hidden" name="detailedMessage" value="<% if (detailedString != null){out.print(detailedString);}%>">
				<input type="Hidden" name="pile" value="<% if (toDisplayException != null) {toDisplayException.printStackTrace(new PrintWriter(out));}%>">
			</form>
		</center>
	</body>
</html>