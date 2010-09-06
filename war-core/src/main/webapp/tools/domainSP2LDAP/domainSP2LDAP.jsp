<%
  response.setHeader("Cache-Control", "no-store"); //HTTP 1.1
  response.setHeader("Pragma", "no-cache"); //HTTP 1.0
  response.setDateHeader("Expires", -1); //prevents caching at the proxy server
%>

<%@page import="com.silverpeas.tools.domainSP2LDAP.*"%>
<%@page import="java.util.List"%>
<%@page import="com.stratelia.silverpeas.peasCore.MainSessionController"%>
<%@page import="com.stratelia.webactiv.util.GeneralPropertiesManager"%>
<%@page import="com.stratelia.webactiv.beans.admin.UserDetail"%>
<%@page import="com.stratelia.webactiv.beans.admin.Domain"%>
<%@page import="java.util.HashMap"%>
<%@page import="java.util.Iterator"%>
<%@page import="java.util.ArrayList"%><html>

<%
String m_context = GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");
MainSessionController m_MainSessionCtrl = (MainSessionController) session.getAttribute("SilverSessionController");

if (m_MainSessionCtrl == null || !"A".equals(m_MainSessionCtrl.getUserAccessLevel())) {
    // No session controller in the request -> security exception
    String sessionTimeout = GeneralPropertiesManager.getGeneralResourceLocator().getString("sessionTimeout");
%>
<script language="javascript">
	location.href="<%=request.getContextPath()+sessionTimeout%>";
</script>
<%
}
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
<link href="/silverpeas/util/styleSheets/globalSP_SilverpeasV5.css" rel="stylesheet" type="text/css" />
<link href="/silverpeas/util/styleSheets/globalSP_SilverpeasV5-IE.css" rel="stylesheet" type="text/css" />
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
<center>
<center><h2><u>OUTIL DE MIGRATION DES UTILISATEURS SILVERPEAS VERS LE DOMAINE LDAP</h2></u></center>
<%
	DomainSP2LDAPBatch db = new DomainSP2LDAPBatch();
	if (toLaunch) {
    try {
    	ArrayList resultLDAPUsers = db.processMigration(domainLDAPId);
      HashMap usersLDAPProcessed = (HashMap) resultLDAPUsers.get(0);
      HashMap usersLDAPNotProcessed = (HashMap) resultLDAPUsers.get(1);
      %>
			<table>
  		<tr><td colspan="5">La migration se fait sur la comparaison du <b>prénom+nom</b></center></td></tr>
			<tr><td colspan="5"><b><%=db.getNbLDAPUsers(domainLDAPId) %></b> utilisateurs dans le LDAP</td></tr>
			<tr><td colspan="5"><b><%=usersLDAPProcessed.size() %></b> utilisateurs traités</td></tr>
			<tr><td></td></tr>
				<% if (usersLDAPProcessed.size() > 0) { %>
						<tr><td class="browseBar">DOMAIN ID</td><td class="browseBar">PRENOM</td><td class="browseBar">NOM</td><td class="browseBar">SPECIFIC_ID</td><td class="browseBar">EMAIL</td><td class="browseBar">LOGIN</td><td class="browseBar">DROITS</td></tr></b>
			      <%
			      	Iterator it = usersLDAPProcessed.values().iterator();
			      	int i=0;
			      	while (it.hasNext())
			      	{
			      	  UserDetail userLDAPProcessed = (UserDetail) it.next();
			      	  int classTrNumber = i++ % 2;
			      	  %>
			    			<tr class="intfdcolor<%=classTrNumber+2%>"><td><%=userLDAPProcessed.getDomainId()%></td><td><%=userLDAPProcessed.getFirstName()%></td><td><%=userLDAPProcessed.getLastName()%></td><td><%=userLDAPProcessed.getSpecificId() %></td><td><%=userLDAPProcessed.geteMail()%></td><td><%=userLDAPProcessed.getLogin()%></td><td><%=userLDAPProcessed.getAccessLevel()%></td></tr>
								<%
							}
					}
			      %>

			<tr><td></td></tr>
			<tr><td></td></tr>
			<tr><td colspan="5"><b><%=usersLDAPNotProcessed.size() %></b> utilisateurs non traités (à traiter manuellement) :</td></tr>

			<tr><td></td></tr>
				<% if (usersLDAPNotProcessed.size() > 0) { %>
						<tr><td class="browseBar">DOMAIN ID</td><td class="browseBar">PRENOM</td><td class="browseBar">NOM</td><td class="browseBar">SPECIFIC_ID</td><td class="browseBar">EMAIL</td><td class="browseBar">LOGIN</td><td class="browseBar">DROITS</td></tr></b>
			      <%
			      	Iterator it = usersLDAPNotProcessed.values().iterator();
			      	int i=0;
			      	while (it.hasNext())
			      	{
			      	  UserDetail userLDAPNotProcessed = (UserDetail) it.next();
			      	  int classTrNumber = i++ % 2;
			      	  %>
			    			<tr class="intfdcolor<%=classTrNumber+2%>"><td><%=userLDAPNotProcessed.getDomainId()%></td><td><%=userLDAPNotProcessed.getFirstName()%></td><td><%=userLDAPNotProcessed.getLastName()%></td><td><%=userLDAPNotProcessed.getSpecificId() %></td><td><%=userLDAPNotProcessed.geteMail()%></td><td><%=userLDAPNotProcessed.getLogin()%></td><td><%=userLDAPNotProcessed.getAccessLevel()%></td></tr>
								<%
							}
					}  %>
					<tr><td></td></tr>
		  		<tr><td colspan="3"></td><td><a href="<%=m_context%>/admin/jsp/MainFrameSilverpeasV5.jsp"><button>Accueil Silverpeas</button></a></td><td><a href="<%=m_context%>/tools"><button>Accueil outils</button></a></td></tr>
			</table>
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
		<center>- Pré-requis: Le domaine LDAP doit être crée et synchronisé</b> -</center>
 		<center>- La migration se fait sur la comparaison du prénom+nom</b> -</center>
 		<center><h3><span class="MessageReadHighPriority"><b>ATTENTION !!</b></span> Assurez-vous d'avoir effectué un backup de la base de données car cette opération met à jour la table des utilisateurs</h3><br></center>
		<br>
		<form action="domainSP2LDAP.jsp" name="toolForm">
			Domaine LDAP à traiter :
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
			<input type="button" value="Lancer la migration" onClick="submit();">
		</form>
		<%
	}
%>
</center>
</body>
</html>