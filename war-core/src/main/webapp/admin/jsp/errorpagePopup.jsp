<% 
    if (response.isCommitted() == false)
        response.resetBuffer();
%>
<%--
 % This page is invoked when an error happens at the server.  The
 % error details are available in the implicit 'exception' object.
 % We set the error page to this file in each of our screens.
 % (via the template.jsp)
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

<%@ include file="import.jsp" %>

<% 
	Exception exception = (Exception) request.getAttribute("javax.servlet.jsp.jspException");
	Throwable toDisplayException = HomePageUtil.getExceptionToDisplay(exception);
	String exStr = HomePageUtil.getMessageToDisplay(exception , language);
	String detailedString = HomePageUtil.getMessagesToDisplay(exception , language);

    // Trace the exception
    HomePageUtil.traceException(exception);
%>

<html> 
<head>
    <title><%= generalMessage.getString("GML.popupTitle")%></title>
<%
out.println(gef.getLookStyleSheet());
%>
    <script language="JavaScript">
        function displayPopup()
        {
            SP_openWindow("<%=m_context%>/admin/jsp/popupError.jsp?action=NOBack","popup","650","180","");
        }
    </script>
    <script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
</head>
<BODY marginwidth=5 marginheight=5 leftmargin=5 topmargin=5 onload="javascript:displayPopup()">
<%
          Window window = gef.getWindow();
					out.println(window.printBefore());
					Frame frame = gef.getFrame();
					out.println(frame.printBefore());
%>
<CENTER>
    <form name="formulaire" action="Main.jsp" method="post">
        <input type="Hidden" name="message" value="<% if (exStr != null){out.print(exStr);}%>">
        <input type="Hidden" name="detailedMessage" value="<% if (detailedString != null){out.print(detailedString);}%>">
        <input type="Hidden" name="pile" value="<% if (toDisplayException != null) {toDisplayException.printStackTrace(new PrintWriter(out));}%>">
    </form>
<table CELLPADDING=0 CELLSPACING=2 BORDER=0 WIDTH="98%" CLASS=intfdcolor>
	<tr>
		<td CLASS=intfdcolor4 NOWRAP>
			<center>
				<br>
				<span class="txtnav">
					<% if (exStr != null){out.print(exStr);}%>
				</span>
			</center>
			<br>
		</td>
	</tr>
</table>
<br>
</CENTER>
<%
				out.println(frame.printAfter());
				out.println(window.printAfter());
%>
</BODY>
</HTML>
