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

<%
  response.setHeader("Cache-Control", "no-store"); //HTTP 1.1
  response.setHeader("Pragma", "no-cache"); //HTTP 1.0
  response.setDateHeader("Expires", -1); //prevents caching at the proxy server
%>

<%@page import="org.silverpeas.core.web.admin.migration.DomainSP2LDAPBatch"%>
<%@page import="org.silverpeas.core.admin.domain.model.Domain"%>
<%@page import="org.silverpeas.core.admin.user.model.UserDetail"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.HashMap"%>
<%@page import="java.util.Iterator"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.buttonpanes.ButtonPane" %>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.buttons.Button" %>
<%@ include file="../check.jsp"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<html>
<%
	String domainLDAPId = null;
  boolean toLaunch = false;

  if (request.getParameter("DomainLDAPId") != null) {
		toLaunch = true;
    domainLDAPId = request.getParameter("DomainLDAPId");
  }

  boolean isError = false;
  Exception theError = null;
  int nb = 0;
%>


<head>
<title>Utilitaires Silverpeas</title>
<view:looknfeel/>
	<script language="javascript">
		function submit()
		{
			if (confirm("Confirmer la migration ?"))
			{
				document.toolForm.toLaunch.value = true;
				document.toolForm.submit();
			}
		}
	</script>
</head>
<body>
<%
	// d�claration des boutons
	Button validateButton = (Button) gef.getFormButton(resource.getString("GML.validate"), "javascript:onClick=submit();", false);
  Button homeButton = (Button) gef.getFormButton("Accueil Silverpeas", m_context+"/admin/jsp/MainFrameSilverpeasV5.jsp", false);
  Button homeTools = (Button) gef.getFormButton("Accueil Outils", m_context+"/tools", false);

	out.println(window.printBefore());

%>

<center><h2>Outil de migration des utilisateurs du domaine Silverpeas vers le domaine LDAP</h2></center>
<center>
<%
	out.println(frame.printBefore());
	out.println(board.printBefore());
	DomainSP2LDAPBatch db = new DomainSP2LDAPBatch();
	if (toLaunch) {
    try {
	ArrayList resultUsers = db.processMigration(domainLDAPId);
      HashMap usersSPProcessed = (HashMap) resultUsers.get(0);
      HashMap usersSPNotProcessed = (HashMap) resultUsers.get(1);
      %>
			<table>
		<tr><td colspan="5">La migration se fait sur la comparaison du <b>pr&eacute;nom+nom</b></td></tr>
			<tr><td colspan="5"><b><%=db.getNbUsers(db.DOMAIN_SILVERPEAS_ID) %></b> utilisateurs dans le domaine Silverpeas</td></tr>
			<tr><td colspan="5"><b><%=db.getNbUsers(domainLDAPId) %></b> utilisateurs dans le LDAP</td></tr>
			<tr><td colspan="5"><b><%=usersSPProcessed.size() %></b> utilisateurs trait&eacute;s</td></tr>
			<tr><td></td></tr>
				<% if (usersSPProcessed.size() > 0) { %>
						<tr><td class="browseBar">DOMAIN ID</td><td class="browseBar">PRENOM</td><td class="browseBar">NOM</td><td class="browseBar">SPECIFIC_ID</td><td class="browseBar">EMAIL</td><td class="browseBar">LOGIN</td><td class="browseBar">DROITS</td></tr></b>
			      <%
				Iterator it = usersSPProcessed.values().iterator();
				int i=0;
				while (it.hasNext())
				{
				  UserDetail userLDAPProcessed = (UserDetail) it.next();
				  int classTrNumber = i++ % 2;
				  %>
						<tr class="intfdcolor<%=classTrNumber+2%>"><td><%=userLDAPProcessed.getDomainId()%></td><td><%=userLDAPProcessed.getFirstName()%></td><td><%=userLDAPProcessed.getLastName()%></td><td><%=userLDAPProcessed.getSpecificId() %></td><td><%=userLDAPProcessed.geteMail()%></td><td><%=userLDAPProcessed.getLogin()%></td><td><%=userLDAPProcessed.getAccessLevel().code()%></td></tr>
								<%
							}
					}
			      %>

			<tr><td></td></tr>
			<tr><td></td></tr>
			<tr><td colspan="5"><b><%=usersSPNotProcessed.size() %></b> utilisateurs non trait&eacute;s (&agrave; traiter manuellement si n&eacute;cessaire) :</td></tr>
			<tr><td></td></tr>
				<% if (usersSPNotProcessed.size() > 0) { %>
						<tr><td class="browseBar">DOMAIN ID</td><td class="browseBar">PRENOM</td><td class="browseBar">NOM</td><td class="browseBar">SPECIFIC_ID</td><td class="browseBar">EMAIL</td><td class="browseBar">LOGIN</td><td class="browseBar">DROITS</td></tr></b>
			      <%
				Iterator it = usersSPNotProcessed.values().iterator();
				int i=0;
				while (it.hasNext())
				{
				  UserDetail userLDAPNotProcessed = (UserDetail) it.next();
				  int classTrNumber = i++ % 2;
				  %>
						<tr class="intfdcolor<%=classTrNumber+2%>"><td><%=userLDAPNotProcessed.getDomainId()%></td><td><%=userLDAPNotProcessed.getFirstName()%></td><td><%=userLDAPNotProcessed.getLastName()%></td><td><%=userLDAPNotProcessed.getSpecificId() %></td><td><%=userLDAPNotProcessed.geteMail()%></td><td><%=userLDAPNotProcessed.getLogin()%></td><td><%=userLDAPNotProcessed.getAccessLevel().code()%></td></tr>
								<%
							}
					}  %>
					<tr><td></td></tr>
			</table>
			</center>
			<%
    } catch (Exception e) {
      isError = true;
      theError = e;
    }
  }
	else
	{
    Domain[] domains = db.getDomains();
    %>
		<center>- Pr&eacute;-requis: Le domaine LDAP doit &ecirc;tre cr&eacute;e et synchronis&eacute;</b> -</center>
		<center>- La migration se fait sur la comparaison du pr&eacute;nom+nom</b> -</center>
		<center>- <u>Note:</u> Les groupes contenant des utilisateurs migr&eacute;s seront d&eacute;plac&eacute;s dans le domaine mixte.</center>
		<center><h3><span class="MessageReadHighPriority"><b>ATTENTION !!</b></span> Assurez-vous d'avoir effectu&eacute; un backup de la base de donn&eacute;es car cette op&eacute;ration met &agrave; jour la table des utilisateurs</h3><br></center>
		<br>
		<center>
		<form action="domainSP2LDAP.jsp" name="toolForm">
			<input type="hidden" name="toLaunch" value="false">
			Domaine LDAP cible :
			<select name="DomainLDAPId">
			<% for (int i=0; i<domains.length; i++)
			  {
				Domain domain = domains[i];
				if (!domain.getId().equals(db.DOMAIN_SILVERPEAS_ID))
				{
					  %>
						<option value="<%=domain.getId()%>"><%=domain.getName()%></option>
				<% } %>
			<% } %>
			</select>
		</form>
		</center>
		<%
	}
%>
</center>
<%
	out.println(board.printAfter());
	ButtonPane buttonPane = gef.getButtonPane();
	if (!toLaunch)
	  buttonPane.addButton(validateButton);
  buttonPane.addButton(homeButton);
  buttonPane.addButton(homeTools);
	out.println("<BR><center>"+buttonPane.print()+"</center><BR>");
	out.println(frame.printAfter());
	out.println(window.printAfter());
%>
</body>
</html>