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
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ page import="java.net.URLDecoder"%>
<%@ page import="com.stratelia.webactiv.util.FileRepositoryManager"%>
<%@ page import="com.silverpeas.util.StringUtil"%>
<%@ page import="com.silverpeas.util.EncodeHelper"%>
<%@page import="com.stratelia.silverpeas.pdcPeas.control.PdcSearchSessionController"%>
<%@page import="com.stratelia.silverpeas.pdcPeas.vo.*"%>
<%@ include file="checkAdvancedSearch.jsp"%>

<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<%-- Set resource bundle --%>
<fmt:setLocale value="${sessionScope['SilverSessionController'].favoriteLanguage}" />
<view:setBundle basename="com.stratelia.silverpeas.pdcPeas.multilang.pdcBundle"/>

<%!
String displayPertinence(float score, String fullStarSrc, String emptyStarSrc)
{
	StringBuffer stars = new StringBuffer();
	if (score <= 0.2) {
		for (int l = 0; l < 1; l++) {
			stars.append("").append(fullStarSrc);
		}
		for (int k = 2; k <= 5; k++) {
			stars.append("").append(emptyStarSrc);
		}
	} else if (score > 0.2 && score <= 0.4) {
		for (int l = 0; l < 2; l++) {
			stars.append("").append(fullStarSrc);
		}
		for (int k = 3; k <= 5; k++) {
			stars.append("").append(emptyStarSrc);
		}
	} else if (score > 0.4 && score <= 0.6) {
		for (int l = 0; l < 3; l++) {
			stars.append("").append(fullStarSrc);
		}
		for (int k = 4; k <= 5; k++) {
			stars.append("").append(emptyStarSrc);
		}
	} else if (score > 0.6 && score <= 0.8) {
		for (int l = 0; l < 4; l++) {
			stars.append("").append(fullStarSrc);
		}
		stars.append("").append(emptyStarSrc);
	} else if (score > 0.8) {
		for (int l = 0; l < 5; l++) {
			stars.append("").append(fullStarSrc);
		}
	}
	return stars.toString();
}

void displayItemsListHeader(String query, Pagination pagination, ResourcesWrapper resource, JspWriter out) throws IOException {
	out.println("<tr valign=\"middle\" class=\"intfdcolor\">");
	out.println("<td align=\"center\" class=\"ArrayNavigation\">");
	out.println("<img align=\"absmiddle\" src=\""+resource.getIcon("pdcPeas.1px")+"\" height=\"20\">");
	out.println(pagination.printCounter());
	out.println(resource.getString("pdcPeas.ManyResultPages"));
	if (query != null && query.length() > 0)
		out.println(" <span id=\"globalResultForYourQuery\">" + resource.getString("pdcPeas.ForYourQuery") + query + "</div>");
	out.println("</td>");
	out.println("</tr>");
}
%>

<%
List results 			= (List) request.getAttribute("Results");
int nbTotalResults		= ((Integer) request.getAttribute("NbTotalResults")).intValue();

int indexOfFirstResult	= ((Integer) request.getAttribute("IndexOfFirstResult")).intValue();
Boolean exportEnabled	= (Boolean) request.getAttribute("ExportEnabled");
Boolean refreshEnabled	= (Boolean) request.getAttribute("RefreshEnabled");
Boolean	xmlSearch		= (Boolean) request.getAttribute("XmlSearchVisible");
boolean	showPertinence	= ((Boolean) request.getAttribute("PertinenceVisible")).booleanValue();

String 	displayParamChoices = (String) request.getAttribute("DisplayParamChoices"); // All || Req || Res
List choiceNbResToDisplay = (List) request.getAttribute("ChoiceNbResToDisplay");
Integer nbResToDisplay		= (Integer) request.getAttribute("NbResToDisplay");
Integer sortValue		= (Integer) request.getAttribute("SortValue");
String sortOrder		= (String) request.getAttribute("SortOrder");
String sortResXForm = (String) request.getAttribute("XmlFormSortValue");
String sortImplementor = (String) request.getAttribute("sortImp");

List	webTabs			= (List) request.getAttribute("WebTabs");
// spelling words
String [] spellingWords = (String []) request.getAttribute("spellingWords");

// List of Group result filter (new function added by EBO)
ResultGroupFilter resultGroup = (ResultGroupFilter) request.getAttribute("ResultGroup");

boolean isXmlSearchVisible = false;
if (xmlSearch != null)
	isXmlSearchVisible = xmlSearch.booleanValue();

// recuperation du choix de l'utilisateur
String keywords = (String) request.getAttribute("Keywords");
if (keywords == null)
	keywords = "";
else
	keywords = EncodeHelper.javaStringToHtmlString(keywords);

Boolean activeSelection = (Boolean) request.getAttribute("ActiveSelection");
if (activeSelection == null) {
	activeSelection = new Boolean(false);
}

// Contenu
String sName		= null;
String sDescription = null;
String sURL			= null;
String sDownloadURL = null;
String sLocation	= "";
String componentId	= "";
String sCreatorName = "";
String sCreationDate = "";
String fileType		= "";
String fileIcon		= "";

//pour le thesaurus
Map synonyms = (Map) request.getAttribute("synonyms");
String urlToRedirect = (String) request.getAttribute("urlToRedirect");
String backButtonClick;
if (urlToRedirect != null) {
    backButtonClick = "location.href='" + URLDecoder.decode(urlToRedirect, "UTF-8") + "';";
}

String fullStarSrc		= "<img src=\""+m_context+"/pdcPeas/jsp/icons/starGreen.gif\">";
String emptyStarSrc		= "<img src=\""+m_context+"/pdcPeas/jsp/icons/pdcPeas_emptyStar.gif\">";
String downloadSrc		= "<img border=0 align=absmiddle src=\""+resource.getIcon("pdcPeas.download")+"\" alt=\""+resource.getString("pdcPeas.DownloadInfo")+"\">";
String attachmentSrc	= "<img border=0 align=absmiddle src=\""+resource.getIcon("pdcPeas.attachment")+"\">&nbsp;";

Board board = gef.getBoard();
Button searchButton = gef.getFormButton(resource.getString("pdcPeas.search"), "javascript:onClick=sendQuery()", false);

// keyword autocompletion
int autocompletionMinChars = Integer.parseInt(resource.getSetting("autocompletion.minChars", "3"));
boolean markResult 		= resource.getSetting("enableMarkAsRead", true);
boolean autoCompletion 	= resource.getSetting("enableAutocompletion", false);

int resultsDisplayMode = ((Integer) request.getAttribute("ResultsDisplay")).intValue();
String pageId = (String) request.getAttribute("ResultPageId");
if (!StringUtil.isDefined(pageId)) {
  pageId = "globalResult";
}
%>

<html>
<HEAD>
<TITLE><%=resource.getString("GML.popupTitle")%></TITLE>
<%
   out.println(gef.getLookStyleSheet());
%>
<% if (resultsDisplayMode == PdcSearchSessionController.SHOWRESULTS_OnlyPDC) { %>
	<style>
		#globalResultTab {
			display: none;
		}
		#globalResultQuery {
			display: none;
		}
		#globalResultHelp {
			display: none;
		}
		#globalResultParamDisplayOptions #sort1 {
			display: none;
		}
		.pertinence {
			display: none;
		}
	</style>
<% } %>
<link rel="stylesheet" type="text/css" href="<%=m_context%>/util/styleSheets/jquery.autocomplete.css" media="screen">
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
<script type="text/javascript" src="<%=m_context%>/pdcPeas/jsp/javascript/formUtil.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/jquery/jquery.bgiframe.min.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/jquery/jquery.autocomplete.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/jquery/thickbox-compressed.js"></script>

<script language="javascript">
	function submitContent(cUrl, componentId) {

		jumpToComponent(componentId);

		document.AdvancedSearch.contentURL.value = cUrl;
		document.AdvancedSearch.componentId.value = componentId;
		document.AdvancedSearch.action = "GlobalContentForward";
		document.AdvancedSearch.submit();
	}

	function jumpToComponent(componentId) {
		if (<%=refreshEnabled.booleanValue()%>)
		{
			//Reload DomainsBar
			parent.SpacesBar.document.privateDomainsForm.component_id.value=componentId;
			parent.SpacesBar.document.privateDomainsForm.privateDomain.value="";
			parent.SpacesBar.document.privateDomainsForm.privateSubDomain.value="";
			parent.SpacesBar.document.privateDomainsForm.submit();

			//Reload Topbar
			parent.SpacesBar.reloadTopBar(true);
		}
	}

	function goToSpace(spaceId)
	{
		//Reload DomainsBar
		parent.SpacesBar.document.privateDomainsForm.component_id.value=spaceId;
		parent.SpacesBar.document.privateDomainsForm.privateDomain.value="";
		parent.SpacesBar.document.privateDomainsForm.privateSubDomain.value="";
		parent.SpacesBar.document.privateDomainsForm.submit();

		//Reload Topbar
		parent.SpacesBar.reloadTopBar(true);
	}


	function openGlossary(uniqueId)
	{
		chemin="<%=m_context%>/RpdcSearch/jsp/AxisTree?query=&uniqueId="+uniqueId;
		largeur = "700";
		hauteur = "500";
		SP_openWindow(chemin,"Pdc_Pop",largeur,hauteur,"scrollbars=yes, resizable=yes");
	}

	function getSelectedOjects()
	{
		return getObjects(true);
	}
	function getNotSelectedOjects()
	{
		return getObjects(false);
	}

	function getObjects(selected)
	{
		var  items = "";
		var boxItems = document.AdvancedSearch.resultObjects;
		if (boxItems != null){
			// au moins une checkbox exist
			var nbBox = boxItems.length;
			if ( (nbBox == null) && (boxItems.checked == selected) ){
				// il n'y a qu'une checkbox non selectionn?e
				items += boxItems.value+",";
			} else{
				// search not checked boxes
				for (i=0;i<boxItems.length ;i++ ){
					if (boxItems[i].checked == selected){
						items += boxItems[i].value+",";
					}
				}
			}
		}
		return items;
	}

	// this function get all checked boxes by the user and sent
	// data to the router
	function getSelectedOjectsFromResultList()
	{
		var  selectItems 	= getSelectedOjects();
		var  notSelectItems = getNotSelectedOjects();

		if ( selectItems.length > 0) {
			// an axis has been selected !
			document.AdvancedSearch.selectedIds.value = selectItems;
			document.AdvancedSearch.notSelectedIds.value = notSelectItems;
			document.AdvancedSearch.action = "ValidateSelectedObjects";
			document.AdvancedSearch.submit();
		}
	}

	function selectEveryResult(chkMaster) {
		var boxItems = document.AdvancedSearch.resultObjects;
		if (boxItems != null){
			// r?f?rence
			var selected = chkMaster.checked;

			// au moins une checkbox existe
			var nbBox = boxItems.length;
			if (nbBox == null) {
				// il n'y a qu'une checkbox dont le "checked" est identique ? la r?f?rence
				boxItems.checked = selected;

			} else {
				// on coche (ou d?coche) les checkboxs, en fonction de la valeur de r?f?rence
				for (i=0; i<boxItems.length; i++ ){
					if (!boxItems[i].disabled)
						boxItems[i].checked = selected;
				}
			}
		}
	}

	function openExportPopup() {
		openPopup("ExportPublications");
	}

	function openExportPDFPopup() {
		openPopup("ExportAttachementsToPDF");
	}

	function openPopup(url) {
		var selectItems = getSelectedOjects();
		var notSelectItems = getNotSelectedOjects();

		chemin = url + "?query=&selectedIds=" + selectItems + "&notSelectedIds=" + notSelectItems;
		largeur = "700";
		hauteur = "500";
		SP_openWindow(chemin, "ExportWindow", largeur, hauteur, "scrollbars=yes, resizable=yes");
	}

	function doPagination(index)
	{
		var  selectItems 	= getSelectedOjects();
		var  notSelectItems = getNotSelectedOjects();

		document.AdvancedSearch.Index.value 			= index;
		document.AdvancedSearch.selectedIds.value 		= selectItems;
		document.AdvancedSearch.notSelectedIds.value 	= notSelectItems;
		document.AdvancedSearch.action					= "Pagination";
		document.AdvancedSearch.submit();
	}

	function sendQuery() {
		try {
			 //empty query field
			 top.topFrame.document.searchForm.query.value = "";
		} catch (e) {
			 //Topbar don't have waited form and/or field
		}
		document.AdvancedSearch.action 		= "AdvancedSearch";

		$.progressMessage();
    	setTimeout("document.AdvancedSearch.submit();", 500);
	}

	// This javascript method submit form in order to filter existing result
	function filterResult(value, type) {
		document.AdvancedSearch.action = "FilterSearchResult";
		if (type == 'author') {
			$("#userFilterId").val(value);
		} else {
			$("#componentFilterId").val(value);
		}
		$("#changeFilterId").val("change");
		$.progressMessage();
    	setTimeout("document.AdvancedSearch.submit();", 500);
	}

	// clearFilter
	function clearFilter(type) {
		document.AdvancedSearch.action = "FilterSearchResult";
		if (type == 'author') {
			$("#userFilterId").val("");
		} else {
			$("#componentFilterId").val("");
		}
		$("#changeFilterId").val("change");
		$.progressMessage();
    	setTimeout("document.AdvancedSearch.submit();", 500);
	}

	function changeResDisplay() {
		document.AdvancedSearch.action = "SortResults";
		document.AdvancedSearch.submit();
	}

	function setSortOrder(order){
		document.AdvancedSearch.sortOrder.value = order;
		document.AdvancedSearch.action = "SortResults";
		document.AdvancedSearch.submit();
	}


    //used for mark as read functionality
  	<%  if(markResult){ %>
		function markAsRead(id) {
			if(id!=""){
				$.post('<%=m_context%>/RpdcSearch/jsp/markAsRead', {id:id});
			}
		}
	<%}%>

	<% if(spellingWords!= null && StringUtil.isDefined(spellingWords[0])){ %>
		function dymsend() {
			document.AdvancedSearch.query.value = '<%=EncodeHelper.javaStringToJsString(spellingWords[0])%>';
			document.AdvancedSearch.submit();
		}
	<% } %>

	function viewUserProfile(userId) {
		location.href = "<%=m_context%>/Rprofil/jsp/Main?userId="+userId;
	}

	$(document).ready(function(){
		//used for keywords autocompletion
	    <%  if(autoCompletion){ %>
			        $("#query").autocomplete("<%=m_context%>/AutocompleteServlet", {
			                    minChars: <%=autocompletionMinChars%>,
			                    max: 50,
			                    autoFill: false,
			                    mustMatch: false,
			                    matchContains: false,
			                    scrollHeight: 220
			            });
	    <%}%>
	  });
</script>
</HEAD>
<BODY class="searchEngine" id="<%=pageId %>">
<form name="AdvancedSearch" action="javascript:sendQuery()" method="post">
<%
	browseBar.setComponentName(resource.getString("pdcPeas.ResultPage"));

	if (activeSelection.booleanValue())
		operationPane.addOperation(resource.getIcon("pdcPeas.folder_to_valid"), resource.getString("pdcPeas.tracker_to_select"), "javascript:getSelectedOjectsFromResultList()");

	if (exportEnabled.booleanValue())
	{
		//To export elements
		operationPane.addOperation(resource.getIcon("pdcPeas.toExport"), resource.getString("pdcPeas.ToExport"), "javascript:openExportPopup();");
		operationPane.addOperation(resource.getIcon("pdcPeas.exportPDF"), resource.getString("pdcPeas.exportPDF"), "javascript:openExportPDFPopup();");
	}

	out.println(window.printBefore());

	tabs = gef.getTabbedPane();
	tabs.addTab(resource.getString("pdcPeas.SearchResult"), "#", true);
	if (webTabs != null)
	{
		for (int i=0; i<webTabs.size(); i++)
		{
			GoogleTab webTab = (GoogleTab) webTabs.get(i);
			tabs.addTab(webTab.getLabel(), "ViewWebTab?Id="+i, false);
		}
	}
	tabs.addTab(resource.getString("pdcPeas.SearchSimple"), "ChangeSearchTypeToAdvanced", false);
	tabs.addTab(resource.getString("pdcPeas.SearchAdvanced"), "ChangeSearchTypeToExpert", false);
	if ( isXmlSearchVisible )
		tabs.addTab(resource.getString("pdcPeas.SearchXml"), "ChangeSearchTypeToXml", false);

	out.println("<div id=\"globalResultTab\">" + tabs.print() + "</div>");
	out.println("<div id=\"globalResultFrame\">");
    out.println(frame.printBefore());
    out.println("<div id=\"globalResultForm\">");
    out.println(board.printBefore());

%>
		<table id="globalResultQuery" border="0" cellspacing="0" cellpadding="5" width="100%">
        <tr align="center">
          <td id="globalResultQueryLabel"><%=resource.getString("pdcPeas.SearchFind")%></td>
          <td align="left" valign="middle">
          	<table border="0" cellspacing="0" cellpadding="0"><tr valign="middle">
          		<td valign="middle"><input id="query" type="text" name="query" size="60" value="<%=keywords%>"><input type="hidden" name="mode"></td>
          		<td valign="middle">&nbsp;</td>
          		<td valign="middle" align="left" width="100%"><% out.println(searchButton.print());%></td>
          	</tr></table>
          </td>
        </tr>
        </table>

<%
	if ("All".equals(displayParamChoices) || "Res".equals(displayParamChoices))
	{
%>
		<table id="globalResultParamDisplay" border="0" cellspacing="0" cellpadding="5" width="100%">
		<tr align="center">
          <td id="globalResultParamDisplayLabel"><%=resource.getString("pdcPeas.NbResultSearch")%></td>
          <td align="left" valign="selectNS" id="globalResultParamDisplayOptions"><select name="nbRes" size="1" onChange="javascript:changeResDisplay()">
            <%
				String selected = "";
				if (choiceNbResToDisplay != null)
		  		{
		  			Iterator it = (Iterator) choiceNbResToDisplay.iterator();
					String choice;
		  			while (it.hasNext())
			  		{
						selected = "";
						choice = (String) it.next();
						if(choice.equals(nbResToDisplay.toString())) {
							selected = "selected";
						}
						out.println("<option value=\""+choice+"\" "+selected+">"+choice+"</option>");
					}
				}
             %>
          </select>
		  <span>&nbsp;&nbsp;&nbsp;<%=resource.getString("pdcPeas.SortResultSearch")%>&nbsp;&nbsp;&nbsp;</span>
		  <select name="sortRes" size="1" onChange="javascript:changeResDisplay()">
            <%
				for (int i=1; i<=7; i++) {
					selected = "";
					if(sortValue.intValue() == i) {
						selected = "selected";
					}
					out.println("<option id=\"sort"+i+"\" value=\""+i+"\""+selected+">"+resource.getString("pdcPeas.SortValueSearch."+i)+"</option>");
				}
             %>
          </select>
		  <%
				String classCSS = "";
				if("ASC".equals(sortOrder)) {
					classCSS = "ArrayNavigationOn";
				}
		  %>
		  <a href="javascript:setSortOrder('ASC')" class="<%=classCSS%>">ASC</a>&nbsp;
		  <%
				classCSS = "";
				if("DESC".equals(sortOrder)) {
					classCSS = "ArrayNavigationOn";
				}
		  %>
		  <a href="javascript:setSortOrder('DESC')" class="<%=classCSS%>">DESC</a>
		  </td>
        </tr>
		<% if (activeSelection.booleanValue() || exportEnabled.booleanValue()) { %>
			<tr id="globalResultSelectAllResult">
				<td class="txtlibform"><%=resource.getString("pdcPeas.selectAll") %></td><td><input type="checkbox" name="selectAll" onClick="selectEveryResult(this);"></td></tr>
			</tr>
		<% }  %>
		</table>
<%
	}

	out.println(board.printAfter());
	out.println("</div>");
    out.println("<div id=\"globalResultList\">");
	out.println(board.printBefore());

	if (results != null)
	{
		Pagination	pagination			= gef.getPagination(nbTotalResults, nbResToDisplay.intValue(), indexOfFirstResult);

		List		resultsOnThisPage	= results;

		out.println("<table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"100%\">");
		displayItemsListHeader(keywords, pagination, resource, out);
		out.println("<tr><td>");
	if(spellingWords!= null && spellingWords[0]!= null && !spellingWords[0].equals("")){
%>
		<table border="0" cellspacing="0" cellpadding="0" width="100%" id="globalResultListDidYouMean">
			<tr >
				<td>
					<span class="spellText" >
						 &nbsp;&nbsp; <% out.println(resource.getString("pdcpeas.didYouMean"));%>

					</span>
					<a href="javascript:dymsend();"><b><span class="spellWord"> <%=spellingWords[0]%></span></b></a>
					<p>&nbsp;</p>
				</td>
			</tr>
		</table>
<%  }
		out.println("<table border=\"0\" id=\"globalResultListDetails\" cellspacing=\"0\" cellpadding=\"0\">");
		GlobalSilverResult	gsr			= null;

		for(int nI=0; resultsOnThisPage != null && nI < resultsOnThisPage.size(); nI++){
			gsr				= (GlobalSilverResult) resultsOnThisPage.get(nI);
			sName			= EncodeHelper.javaStringToHtmlString(gsr.getName(language));
			sDescription	= gsr.getDescription(language);
			if (sDescription != null && sDescription.length() > 400)
				sDescription = sDescription.substring(0, 400)+"...";
			sURL			= gsr.getTitleLink();
			sDownloadURL	= gsr.getDownloadLink();
			sLocation		= gsr.getLocation();
			sCreatorName	= gsr.getCreatorName();
			try	{
			  	if (sortValue.intValue() == 4) {
			  	  	sCreationDate = resource.getOutputDate(gsr.getCreationDate());
			  	} else {
					sCreationDate = resource.getOutputDate(gsr.getDate());
			  	}
			} catch (Exception e) {
				sCreationDate	= null;
			}

			out.println("<tr class=\"lineResult " + gsr.getSpaceId() + " " + gsr.getInstanceId() + "\">");

			if (showPertinence)
				out.println("<td class=\"pertinence\">"+displayPertinence(gsr.getRawScore(), fullStarSrc, emptyStarSrc)+"&nbsp;</td>");

			if (activeSelection.booleanValue() || exportEnabled.booleanValue()) {
				if (gsr.isExportable()) {
					String checked = "";
					if (gsr.isSelected()) {
						checked = "checked";
					}
					out.println("<td class=\"selection\"><input type=\"checkbox\" "+checked+" name=\"resultObjects\" value=\""+gsr.getId()+"-"+gsr.getInstanceId()+"\"></td>");
				} else { 
			   		out.println("<td class=\"selection\"><input type=\"checkbox\" disabled name=\"resultObjects\" value=\""+gsr.getId()+"-"+gsr.getInstanceId()+"\"></td>");
				}
			}

			if (gsr.getType() != null && (gsr.getType().startsWith("Attachment")|| gsr.getType().startsWith("Versioning") || gsr.getType().equals("LinkedFile")) ) {
                fileType	= sName.substring(sName.lastIndexOf(".")+1, sName.length());
				fileIcon	= FileRepositoryManager.getFileIcon(fileType);
				sName = "<img src=\""+fileIcon+"\" border=\"0\" width=\"30\" heigth=\"30\" align=\"absmiddle\"/>"+sName;
				//no preview, display this is an attachment
				if (gsr.getType().startsWith("Attachment") || gsr.getType().equals("LinkedFile")) {
					sDescription = null;
				}
			}

			out.println("<td class=\"content\">");

			out.println("<table cellspacing=\"0\" cellpadding=\"0\"><tr>");

			if (gsr.getThumbnailURL() != null && gsr.getThumbnailURL().length()>0)
			{
			  	if ("UserFull".equals(gsr.getType())) {
			  	  out.println("<td><img class=\"avatar\" src=\""+m_context+gsr.getThumbnailURL()+"\" /></td>");
			  	} else {
			  	  out.println("<td><img src=\""+gsr.getThumbnailURL()+"\" border=\"0\" width=\""+gsr.getThumbnailWidth()+"\" height=\""+gsr.getThumbnailHeight()+"\"/></td>");
			  	}
				out.println("<td>&nbsp;</td>");
			}

			out.println("<td>");
			if (activeSelection.booleanValue())
				out.println("<span class=\"textePetitBold\">"+sName+"</span>");
			else {
			  	String cssClass="textePetitBold";
			  	String cssClassDisableVisited="";
			  	if(gsr.isHasRead()){
			  	  cssClass="markedkAsRead";
			  	  cssClassDisableVisited ="markedkAsReadDisableVisited";
			  	}
				out.println("<a href=\""+sURL+"\" class=\""+cssClassDisableVisited +"\"><span class=\""+ cssClass+ "\">"+sName+"</span></a>");
			} 
			if (StringUtil.isDefined(sDownloadURL))
			{
				//affiche le lien pour le téléchargement
				out.println("<a href=\""+sDownloadURL+"\" target=\"_blank\">"+downloadSrc+"</a>");
			}
			if (StringUtil.isDefined(sCreatorName)) {
				out.println(" <span class=\"creatorName\"> - "+EncodeHelper.javaStringToHtmlString(sCreatorName)+"</span>");
			}
			if (StringUtil.isDefined(sCreationDate)) {
				out.print(" <span class=\"creationDate\"> ("+sCreationDate + ") </span>");
			}

			if (StringUtil.isDefined(sDescription)) {
				out.println("<span class=\"description\"><br/><i> "+EncodeHelper.javaStringToHtmlParagraphe(sDescription)+"</i></span>");
			}

			if (sortValue.intValue() == 7 && gsr.getHits() >= 0) {
			  	out.println("<br/><span class=\"popularity\">"+resource.getStringWithParam("pdcPeas.popularity", Integer.toString(gsr.getHits()))+"</span>");
			}

			if (StringUtil.isDefined(sLocation)) {
				out.println("<span class=\"location\"> <br/>"+EncodeHelper.javaStringToHtmlString(sLocation)+"</span>");
			}
			out.println("<td>");

			out.println("</tr></table>");

			out.println("</td>");
			out.println("</tr>");
		}
		out.println("</table>");

		out.println("</td></tr>");
		out.println("<tr class=\"intfdcolor4\"><td>&nbsp;</td></tr>");

		if (nbTotalResults > resultsOnThisPage.size())
		{
			out.println("<tr valign=\"middle\" class=\"intfdcolor\">");
			out.println("<td align=\"center\">");
			out.println(pagination.printIndex("doPagination"));
			out.println("</td>");
			out.println("</tr>");
		}

		out.println("</table>");
	} else {
		out.println("<table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"100%\">");
		out.println("<tr valign=\"middle\" class=\"intfdcolor\">");
		out.println("<td align=\"center\">"+resource.getString("pdcPeas.NoResult")+"</td>");
		out.println("</tr>");
		out.println("</table>");
	}
	out.println(board.printAfter());
  	out.println("</div>");

    out.println("<div id=\"globalResultHelp\">");
	out.println(board.printBefore());
    %>
		<table width="100%" border="0"><tr><td valign="top" width="30%">
		<%=resource.getString("pdcPeas.helpCol1Header")%><br><br>
		<%=resource.getString("pdcPeas.helpCol1Content1")%><br>
		<%=resource.getString("pdcPeas.helpCol1Content2")%><br>
		<%=resource.getString("pdcPeas.helpCol1Content3")%><br>
		</td>
		<td>&nbsp;</td>
		<td valign="top" width="30%">
		<%=resource.getString("pdcPeas.helpCol2Header")%><br><br>
		<%=resource.getString("pdcPeas.helpCol2Content1")%><br>
		<%=resource.getString("pdcPeas.helpCol2Content2")%><br>
		<%=resource.getString("pdcPeas.helpCol2Content3")%><br>
		<%=resource.getString("pdcPeas.helpCol2Content4")%><br>
		<%=resource.getString("pdcPeas.helpCol2Content5")%><br>
		</td>
		<td>&nbsp;</td>
		<td valign="top" width="30%">
		<%=resource.getString("pdcPeas.helpCol3Header")%><br><br>
		<%=resource.getString("pdcPeas.helpCol3Content1")%><br>
		<%=resource.getString("pdcPeas.helpCol3Content2")%><br>
		<%=resource.getString("pdcPeas.helpCol3Content3")%><br>
		<%=resource.getString("pdcPeas.helpCol3Content4")%><br>
		</td>
		</tr></table>
	<%
	out.println(board.printAfter());
	out.println("</div>");
	out.println(frame.printAfter());
	out.println("</div>");

	// Adding facet search group
  	int facetResultLength = Integer.parseInt(resource.getSetting("searchengine.facet.max.length", "30"));
	String filteredUserId = (String) request.getAttribute("FilteredUserId");
	String filteredComponentId = (String) request.getAttribute("FilteredComponentId");

  	if (resultGroup != null) {
    	%>
	  <input type="hidden" name="changeFilter" id="changeFilterId" value="" />
	  <input type="hidden" name="authorFilter" id="userFilterId" value="<%=filteredUserId%>"/>
	  <input type="hidden" name="componentFilter" id="componentFilterId" value="<%=filteredComponentId%>"/>

      <div id="globalResultGroupDivId">
      	<div id="facetSearchDivId">
      	<%
      	List authors = resultGroup.getAuthors();
      	if (authors != null) {
      	  %>
      	  <div id="facetAuthor">
      	  <div id="searchGroupTitle"><span class="author"><fmt:message key="pdcPeas.group.author" /></span></div>
   		  <div id="searchGroupValues">
   			<ul>
      	  <%
  	      for(int cpt=0; cpt < authors.size(); cpt++){
  	        AuthorVO author = (AuthorVO) authors.get(cpt);
  	        String authorName = author.getName();
  	        String authorId = author.getId();
  	        String displayAuthor = (authorName != null && authorName.length() > facetResultLength)? authorName.substring(0,facetResultLength) + "...":authorName;
  	        displayAuthor += "&nbsp;(" + author.getNbElt() + ")";
  	        String lastClass = "";
  	        if (cpt == authors.size() - 1) {
  	          lastClass = "last";
  	        }
  	        String jsAction = "";
  	        if (authorId.equalsIgnoreCase(filteredUserId)) {
  	          lastClass += " selected";
  	          jsAction = "clearFilter('author')";
  	        %>
       	  <fmt:message var="tooltip" key="pdcPeas.group.tooltip.disable">
			<fmt:param value="<%=authorName%>"/>
		  </fmt:message>
  	  				<li class="<%=lastClass%>"><a href="javascript:<%=jsAction%>;" title="${tooltip}"><%=displayAuthor%></a></li>
  	        <%
  	        } else {
  	          jsAction = "filterResult('" + authorId + "', 'author')";
    	    %>
       	  <fmt:message var="tooltip" key="pdcPeas.group.tooltip.enable">
			<fmt:param value="<%=authorName%>"/>
		  </fmt:message>
  	  				<li class="<%=lastClass%>"><a href="javascript:<%=jsAction%>;" title="${tooltip}"><%=displayAuthor%></a></li>
  	        <%
  	        }

  	      }
      	  %>
      		</ul>
      	  </div>
      	  </div>
      	  <%
      	}
      	%>

      	<%
      	List components = resultGroup.getComponents();
      	if (components != null) {
      	  %>
      	  <div id="facetInstance">
      	  <div id="searchGroupTitle"><span class="component"><fmt:message key="pdcPeas.group.service" /></span></div>
   		  <div id="searchGroupValues">
   			<ul>
      	  <%
  	      for(int cpt=0; cpt < components.size(); cpt++){
  	        ComponentVO comp = (ComponentVO) components.get(cpt);
  	        String compName = comp.getName();
  	        String compId = comp.getId();
  	        String displayComp = (compName != null && compName.length() > facetResultLength)? compName.substring(0,facetResultLength) + "...":compName;
  	        displayComp += "&nbsp;(" + comp.getNbElt() + ")";
  	        String lastClass = "";
  	        if (cpt == components.size() - 1) {
  	          lastClass = "last";
  	        }
  	        String jsAction = "";
  	        if (compId.equalsIgnoreCase(filteredComponentId)) {
  	          lastClass += " selected";
  	          jsAction = "clearFilter('component')";
    	        %>
           	  <fmt:message var="tooltip" key="pdcPeas.group.tooltip.disable">
    			<fmt:param value="<%=compName%>"/>
    		  </fmt:message>
      	  				<li class="<%=lastClass%>"><a href="javascript:<%=jsAction%>;" title="${tooltip}"><%=displayComp%></a></li>
      	        <%
  	        } else {
  	          jsAction = "filterResult('" + compId + "', 'component')";
  	    	    %>
  	       	  <fmt:message var="tooltip" key="pdcPeas.group.tooltip.enable">
  				<fmt:param value="<%=compName%>"/>
  			  </fmt:message>
  	  	  				<li class="<%=lastClass%>"><a href="javascript:<%=jsAction%>;" title="${tooltip}"><%=displayComp%></a></li>
  	  	        <%
  	        }
  	      }
      	  %>
      		</ul>
      	  </div>
      	  </div>
      	  <%
      	}
      	%>
      	<%--
      	  <div id="searchGroupTitle"><span class="file">Type de fichier</span></div>
   		  <div id="searchGroupValues">
   			<ul>
  	  				<li><a href="#">pdf</a></li>
  	  				<li><a href="#">xls</a></li>
      		</ul>
      	  </div>
        --%>
        </div>
      </div>
    	<%
    }

	out.println(window.printAfter());
%>

	<input type="hidden" name="selectedIds"/>
	<input type="hidden" name="notSelectedIds"/>
	<input type="hidden" name="Index"/>
	<input type="hidden" name="contentURL"/>
	<input type="hidden" name="componentId"/>
	<input type="hidden" name="sortOrder" value="<%=sortOrder%>"/>
	<input type="hidden" name="ShowResults" value="<%=resultsDisplayMode%>"/>
	<input type="hidden" name="ResultPageId" value="<%=pageId %>"/>
	<input type="hidden" name="SortResXForm" value="<%=sortResXForm %>"/>
	<input type="hidden" name="sortImp" value="<%=sortImplementor%>"/>

</form>
<view:progressMessage/>
</body>
</html>