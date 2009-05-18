<%--
 % $Id: errorpage.jsp,v 1.2 2005/08/18 10:48:07 neysseri Exp $
 % Copyright 1999 Sun Microsystems, Inc. All rights reserved.
 % Copyright 1999 Sun Microsystems, Inc. Tous droits réservés. 
--%>

<%--
 % This page is invoked when an error happens at the server.  The
 % error details are available in the implicit 'exception' object.
 % We set the error page to this file in each of our screens.
 % (via the template.jsp)
--%>

<%@ page isErrorPage="true" %>
<%@ page import="com.stratelia.webactiv.util.ResourceLocator"%>
<%@ page import="javax.naming.NamingException"%>
<%@ page import="java.sql.SQLException"%>
<%@ page import="java.rmi.RemoteException"%>
<%@ page import="javax.ejb.CreateException"%>
<%@ page import="javax.ejb.FinderException"%>
<%@ page import="javax.ejb.RemoveException"%>
<%@ page import="javax.servlet.*"%>
<%@ page import="javax.servlet.http.*"%>
<%@ page import="javax.servlet.jsp.*"%>
<%@ page import="java.io.PrintWriter"%>
<%@ page import="java.io.IOException"%>
<%@ page import="java.io.FileInputStream"%>
<%@ page import="java.io.ObjectInputStream"%>
<%@ page import="java.util.Vector"%>
<%@ page import="java.beans.*"%>


<%@ include file="checkSilvermail.jsp" %>


<% 
   message = new ResourceLocator("com.stratelia.silverpeas.notificationserver.channel.silvermail.multilang.silvermail", silvermailScc.getLanguage());

  if (exception == null)
     exception = new Exception("Exception UNAVAILABLE: Tracing Stack...");
%>

<%!

private ResourceLocator message;

private String displayNetworkError() {
  return message.getString("ProblemeCommunicationServeur");
}
private String displayDatabaseError() {
  return message.getString("ProblemeBaseDonnees");
}
private String displayEJBCreationError() {
  return message.getString("ProblemeCreationEJB");
}
private String displayEJBFinderError() {
  return message.getString("ProblemeRechercheEJB");
}
private String displayEJBRemoveError() {
  return message.getString("ProblemeDestructionEJB");
}
private String displayUnexpectedError() {
  return message.getString("ProblemeInattenduRencontre");
}

%>
<html> 
<head> 
<title><%=message.getString("Erreur")%></title> 
<LINK REL="stylesheet" TYPE="text/css" HREF="Styles_Kiosk_v4.css">
</head> 
<body> 
<TABLE>
<TR><TD>
<h2><%=message.getString("Erreur")%></h2>
<h3><%=message.getString("RequeteInsatisfaite")%></h3>
<p>
<%      if (exception instanceof NamingException) {
	    out.println(displayNetworkError());
	} 
        else if (exception instanceof SQLException) {
	    out.println(displayDatabaseError());
	} 
        else if (exception instanceof RemoteException) {
	    out.println(displayNetworkError());
	} 
        else if (exception instanceof CreateException) {
	    out.println(displayEJBCreationError());
	}
        else if (exception instanceof FinderException) {
	    out.println(displayEJBFinderError());
	} 
        else if (exception instanceof RemoveException) {
	    out.println(displayEJBRemoveError());
	} 
        else {
            out.println(displayUnexpectedError());
        }
%>
<p>
<font color="red" size="3"><b><em><%=exception.getMessage() %></em></b></font>
<p><%=message.getString("TransmettreAdministrateur")%></p>
<pre><%exception.printStackTrace(new PrintWriter(out));%></pre>
</TD></TR>
</TABLE>
</BODY>
</HTML>
