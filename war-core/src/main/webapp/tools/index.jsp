<%
    response.setHeader("Cache-Control","no-store"); //HTTP 1.1
    response.setHeader("Pragma","no-cache");        //HTTP 1.0
    response.setDateHeader ("Expires",-1);          //prevents caching at the proxy server
%>

<%@page import="com.stratelia.silverpeas.peasCore.MainSessionController"%>
<%@page import="com.stratelia.webactiv.util.GeneralPropertiesManager"%><html>
<%
MainSessionController m_MainSessionCtrl = (MainSessionController) session.getAttribute("SilverSessionController");
String m_context = GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");

if (m_MainSessionCtrl == null || !"A".equals(m_MainSessionCtrl.getUserAccessLevel())) {
    // No session controller in the request -> security exception
    String sessionTimeout = GeneralPropertiesManager.getGeneralResourceLocator().getString("sessionTimeout");
%>
<script language="javascript">
	location.href="<%=request.getContextPath()+sessionTimeout%>";
</script>
<%
}

%>
<link href="silverpeas/util/styleSheets/globalSP_SilverpeasV5.css" rel="stylesheet" type="text/css" />
<link href="silverpeas/util/styleSheets/globalSP_SilverpeasV5-IE.css" rel="stylesheet" type="text/css" />
<head>
  <title>Utilitaires Silverpeas</title>
	<script language="javascript">
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
		function launchTool()
		{
			location.href=document.getElementById('tool').value;
		}
	</script>
</head>
<body>
	<center><h3>Utilitaires Silverpeas</h2></center>
    <hr/>
    <p>
        Sélectionner un utilitaire :
        <br/>
        <form name="tools" method="get">
            <select name="tool" id="tool" size="5">
                <option selected value="checkAttachments/checkAttachments.jsp?attachmentType=dummy">Vérification des fichiers joints</option>
                <option selected value="domainSP2LDAP/domainSP2LDAP.jsp">Migration des utilisateurs du domaine Silverpeas vers un domaine LDAP</option>
            </select>
            <br>
            <br><input type="button" value="Executer" onClick="launchTool()" />&nbsp;<a href="<%=m_context%>/admin/jsp/MainFrameSilverpeasV5.jsp"><button>Accueil Silverpeas</button></a>
		  			</tr>
        </form>
    </p>
</body>
</html>