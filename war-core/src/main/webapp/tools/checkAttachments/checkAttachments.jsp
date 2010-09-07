<%--

    Copyright (C) 2000 - 2009 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://repository.silverpeas.com/legal/licensing"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>

<%@ taglib uri="http://displaytag.sf.net" prefix="display" %>
<%
  response.setHeader("Cache-Control", "no-store"); //HTTP 1.1
  response.setHeader("Pragma", "no-cache"); //HTTP 1.0
  response.setDateHeader("Expires", -1); //prevents caching at the proxy server
%>

<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@page import="com.silverpeas.tools.checkAttachments.CheckAttachmentsBatch"%>
<%@page import="java.util.List"%>
<%@page
	import="com.stratelia.webactiv.util.publication.model.PublicationDetail"%>
<%@page
	import="com.stratelia.webactiv.util.publication.model.PublicationPK"%>
<%@page import="com.stratelia.webactiv.util.node.model.NodePK"%>

<%@page import="com.stratelia.silverpeas.peasCore.MainSessionController"%>
<%@page import="com.stratelia.webactiv.util.GeneralPropertiesManager"%>
<%
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

	String attachmentType = "";
  String language = "fr";
  int nbItemsPerPage = 50;
  boolean toLaunch = false;
  if (request.getParameter("toLaunch") != null)
    toLaunch = new Boolean(request.getParameter("toLaunch")).booleanValue();

  if (request.getParameter("attachmentType") != null) {
    attachmentType = request.getParameter("attachmentType");
  }
  if (request.getParameter("language") != null) {
    language = request.getParameter("language");
  }
  if (request.getParameter("nbItemsPerPage") != null) {
	  nbItemsPerPage = new Integer(request.getParameter("nbItemsPerPage")).intValue();
  }

  boolean isError = false;
  Exception theError = null;
  int nb = 0;
%>

<html>
<head>
<title>Utilitaires Silverpeas</title>
<script language="javascript">
	function submit()
	{
		document.toolForm.toLaunch.value = true;
		document.toolForm.submit();
	}
</script>
<link href="styleSheets/displaytag.css" rel="stylesheet" type="text/css" />
<link href="styleSheets/screen.css" rel="stylesheet" type="text/css" />
</head>
<body>
			<div id="workingProgress" align="center"><b>Traitement en cours... Merci de patienter.</b><br><br></div>
<%
	List resultAttachments = null;
	List orphansAttachments = null;
  if (toLaunch) {
    %>
    <%

    CheckAttachmentsBatch cab = new CheckAttachmentsBatch();
    try {
      resultAttachments = cab.check(attachmentType, language);
      if (attachmentType.equals("All"))
      {
	      orphansAttachments = cab.getOrphansFiles("Images");
	      orphansAttachments.addAll(cab.getOrphansFiles("wysiwyg"));
			}
			else
	      orphansAttachments = cab.getOrphansFiles(attachmentType);

	  request.setAttribute( "listAttachments", resultAttachments );
	  request.setAttribute( "listOrphans", orphansAttachments );
    } catch (Exception e) {
      isError = true;
      theError = e;
    }
  }

  if (isError || (theError != null)) {
    out.print("Une erreur s'est produite :");
    out.print(theError.toString());
    theError.printStackTrace();
  } else  {
%>

		<form action="checkAttachments.jsp" method="get" name="toolForm">
			<b>Langue : </b><input type="text" name="language" value="<%=language%>" maxlength="2" size="5">
			<b>Résultats par page :</b> <input type="text" name="nbItemsPerPage" value="<%=nbItemsPerPage%>" maxlength="3" size="5">
		<input type="hidden" name="toLaunch" value="true"><b>Type de fichiers</b>
		:&nbsp; <select name="attachmentType">
			<option value="dummy" selected></option>
			<option value="All" <%if (attachmentType.equals("All"))  out.println("selected");%>>Tous les fichiers</option>

			<option value="Images"
				<%if (attachmentType.equals("Images"))
		          out.println("selected");%>>Fichiers
			joints</option>
			<option value="ImagesWysiwyg"
				<%if (attachmentType.equals("ImagesWysiwyg"))
		          out.println("selected");%>>Images
			et Fichiers de wysiwyg</option>
			<option value="wysiwyg"
				<%if (attachmentType.equals("wysiwyg"))
		          out.println("selected");%>>Wysiwyg
			de Thème et Publications</option>
			<option value="XMLFormImages"
				<%if (attachmentType.equals("XMLFormImages"))
		          out.println("selected");%>>Images
			et Fichiers de formulaires XML</option>
			</select>
			<input type="button" value="OK" onClick="submit();">
		</form>
		<center><h2>OUTIL DE SUPERVISION DES FICHIERS JOINTS DE SILVERPEAS (PJ, wysiwyg, etc)</h2></center>
		<center>- Liste des fichiers lus dans la table <b>sb_attachment_attachment</b> -</center>

		<display:table export="true" name="listAttachments" id="row" pagesize="<%=nbItemsPerPage%>" defaultsort="2" defaultorder="ascending">
			<display:setProperty name="paging.banner.one_item_found">1 {0} trouvé.</display:setProperty>
			<display:setProperty name="paging.banner.all_items_found"><b>{0} {1}</b> trouvés.</display:setProperty>
			<display:setProperty name="paging.banner.some_items_found"><b>{0} {1}</b> trouvés, Affichés: <b>{2} à {3}</b>.</display:setProperty>
			<display:setProperty name="paging.banner.full"><span class="pagelinks">[<a href="{1}">Premier</a> / <a href="{2}">Précédent</a>] {0} [<a href="{3}">Suivant</a> / <a href="{4}">Dernier</a>]</span></display:setProperty>
			<display:setProperty name="paging.banner.first"><span class="pagelinks">[Premier/Précédent] {0} [<a href="{3}">Suivant</a> / <a href="{4}">Dernier</a>]</span></display:setProperty>
			<display:setProperty name="paging.banner.last"><span class="pagelinks">[<a href="{1}">Premier</a> / <a href="{2}">Précédent</a>] {0} [Suivant / Dernier]</span></display:setProperty>

			<display:setProperty name="paging.banner.item_name">fichier</display:setProperty>
			<display:setProperty name="paging.banner.items_name">fichiers</display:setProperty>

			<display:column title="ID" property="attachmentId" sortable="true" headerClass="sortable" />
			<display:column title="NOM LOGIQUE" property="logicalName" sortable="true" headerClass="sortable" />
			<display:column title="NOM PHYSIQUE" property="physicalName" sortable="true" headerClass="sortable" />
			<display:column title="CHEMIN COMPLET" property="path" sortable="true" headerClass="sortable" />
			<display:column title="TAILLE" property="size" sortable="true" headerClass="sortable" />
			<display:column title="TITRE" property="title" sortable="true" headerClass="sortable" />
			<display:column title="ESPACE" property="spaceLabel" sortable="true" headerClass="sortable" />
			<display:column title="COMPOSANT" property="componentLabel" sortable="true" headerClass="sortable" />
			<display:column title="CONTEXTE" property="context" sortable="true" headerClass="sortable" />
			<display:column title="PUBLICATION" property="publicationPath" sortable="true" headerClass="sortable" />
			<display:column title="PUBLIEUR" property="actionsDate" sortable="true" headerClass="sortable" />
			<display:column title="STATUT" property="status" sortable="true" headerClass="sortable" />
		</display:table>
		<br>

		<center><h2>FICHIERS JOINTS ORPHELINS</h2></center>
	<% if (attachmentType.equals("ImagesWysiwyg")) { %>
		<center><b>Type de fichiers non pris en charge par cette fonction.</b></center>
	<% } else { %>
		<center>- Liste des fichiers lus sur le serveur, n'ayant pas de correspondance dans la table <b>sb_attachment_attachment</b> -</center>
		<display:table export="true" name="listOrphans" pagesize="<%=nbItemsPerPage%>" defaultsort="2" defaultorder="ascending">
			<display:setProperty name="paging.banner.one_item_found">1 {0} trouvé.</display:setProperty>
			<display:setProperty name="paging.banner.all_items_found"><b>{0} {1}</b> trouvés.</display:setProperty>
			<display:setProperty name="paging.banner.some_items_found"><b>{0} {1}</b> trouvés, Affichés: <b>{2} à {3}</b>.</display:setProperty>
			<display:setProperty name="paging.banner.full"><span class="pagelinks">[<a href="{1}">Premier</a> / <a href="{2}">Précédent</a>] {0} [<a href="{3}">Suivant</a> / <a href="{4}">Dernier</a>]</span></display:setProperty>
			<display:setProperty name="paging.banner.first"><span class="pagelinks">[Premier/Précédent] {0} [<a href="{3}">Suivant</a> / <a href="{4}">Dernier</a>]</span></display:setProperty>
			<display:setProperty name="paging.banner.last"><span class="pagelinks">[<a href="{1}">Premier</a> / <a href="{2}">Précédent</a>] {0} [Suivant / Dernier]</span></display:setProperty>

			<display:setProperty name="paging.banner.item_name">fichier</display:setProperty>
			<display:setProperty name="paging.banner.items_name">fichiers</display:setProperty>

			<display:column title="NOM PHYSIQUE" property="physicalName" sortable="true" headerClass="sortable" />
			<display:column title="CHEMIN COMPLET" property="path" sortable="true" headerClass="sortable" />
			<display:column title="TAILLE" property="size" sortable="true" headerClass="sortable" />
			<display:column title="ESPACE" property="spaceLabel" sortable="true" headerClass="sortable" />
			<display:column title="COMPOSANT" property="componentLabel" sortable="true" headerClass="sortable" />
			<display:column title="CONTEXTE" property="context" sortable="true" headerClass="sortable" />
		</display:table>
	<% } %>
<% } %>
</body>
<script language="javascript">
	document.getElementById("workingProgress").style.display='none';
</script>
</html>
