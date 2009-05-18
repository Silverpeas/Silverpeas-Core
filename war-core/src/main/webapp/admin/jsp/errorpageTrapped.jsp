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
	String gobackPage = null;
	String extraInfos = "";

    if (exception instanceof SilverpeasTrappedException)
    {
        gobackPage = ((SilverpeasTrappedException)exception).getGoBackPage();
        extraInfos = ((SilverpeasTrappedException)exception).getExtraInfos();
        if ((extraInfos == null) || ("null".equalsIgnoreCase(extraInfos)))
            extraInfos = "";
    }
    if ((gobackPage == null) || (gobackPage.length() <= 0))
        gobackPage = m_context + "/admin/jsp/errorpageMainMax.jsp";

    HomePageUtil.traceException(exception);
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
        Window window = gef.getWindow();
		out.println(window.printBefore());
		Frame frame = gef.getFrame();
		out.println(frame.printBefore());
%>
<CENTER>
<table CELLPADDING=0 CELLSPACING=2 BORDER=0 WIDTH="98%" CLASS=intfdcolor>
	<form name="formError" action="<%=gobackPage%>" method="post">
	<input type="Hidden" name="message" value="<% if (exStr != null){out.print(exStr);}%>">
    <input type="Hidden" name="detailedMessage" value="<% if (detailedString != null){out.print(detailedString);}%>">
	<input type="Hidden" name="pile" value="<% if (toDisplayException != null) {toDisplayException.printStackTrace(new PrintWriter(out));}%>">
	<tr>
		<td CLASS=intfdcolor4 NOWRAP>
			<center>
				<br>
				<span class="txtnav">
					<% if (exStr != null){out.print(exStr);}%>
                <br><br>
                <%=extraInfos%>
                </span>
			</center>
			<br>
		</td>
	</tr>
	</form>
</table>
<br>
<%
	  ButtonPane buttonPane = gef.getButtonPane();
      buttonPane.addButton((Button) gef.getFormButton(generalMessage.getString("GML.ok"), "javascript:document.formError.submit();", false));
      out.println(buttonPane.print());
%>
</CENTER>
<%
	  out.println(frame.printAfter());
	  out.println(window.printAfter());
%>
</BODY>
</HTML>
