<%--

    Copyright (C) 2000 - 2013 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://www.silverpeas.org/docs/core/legal/floss_exception.html"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>

<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%--
 % This page is invoked when an error happens at the server.  The
 % error details are available in the implicit 'exception' object.
 % We set the error page to this file in each of our screens.
 % (via the template.jsp)
--%>

<%@ page isErrorPage="true" %>
<%@ page import="org.silverpeas.core.util.LocalizationBundle"%>
<%@ page import="javax.ejb.CreateException"%>
<%@ page import="javax.ejb.FinderException"%>
<%@ page import="javax.ejb.RemoveException"%>
<%@ page import="javax.naming.NamingException"%>
<%@ page import="java.io.PrintWriter"%>
<%@ page import="java.rmi.RemoteException"%>
<%@ page import="java.sql.SQLException"%>


<%@ include file="checkSilvermail.jsp" %>


<%
   message = ResourceLocator.getLocalizationBundle("org.silverpeas.notificationserver.channel.silvermail.multilang.silvermail", silvermailScc.getLanguage());

  if (exception == null)
     exception = new Exception("Exception UNAVAILABLE: Tracing Stack...");
%>

<%!

private LocalizationBundle message;

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
