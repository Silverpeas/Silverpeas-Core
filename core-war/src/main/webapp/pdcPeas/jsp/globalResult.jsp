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

<%@ page import="org.silverpeas.web.pdc.control.PdcSearchSessionController"%>
<%@ page import="org.silverpeas.web.pdc.vo.Facet"%>
<%@ page import="org.silverpeas.web.pdc.vo.FacetEntryVO"%>
<%@ page import="org.silverpeas.web.pdc.vo.ResultGroupFilter"%>
<%@ page import="org.apache.lucene.queryParser.QueryParser"%>
<%@ page import="org.silverpeas.core.index.search.model.IndexSearcher"%>
<%@ page import="org.silverpeas.core.util.StringUtil"%>
<%@ page import="java.net.URLDecoder"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.buttons.Button" %>

<%@ include file="checkAdvancedSearch.jsp"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<%-- Set resource bundle --%>
<fmt:setLocale value="${sessionScope['SilverSessionController'].favoriteLanguage}" />
<view:setBundle basename="org.silverpeas.pdcPeas.multilang.pdcBundle"/>
<view:setBundle basename="org.silverpeas.pdcPeas.settings.pdcPeasIcons" var="icons" />

<%!
void displayItemsListHeader(String query, Pagination pagination, MultiSilverpeasBundle resource, JspWriter out) throws IOException {
	out.println("<tr valign=\"middle\">");
	out.println("<td align=\"center\" class=\"ArrayNavigation\">");
	out.println("<img align=\"absmiddle\" src=\""+resource.getIcon("pdcPeas.1px")+"\" height=\"20\">");
	out.println(pagination.printCounter());
	out.println(resource.getString("pdcPeas.ManyResultPages"));
	if (query != null && query.length() > 0)
		out.println(" <span id=\"globalResultForYourQuery\">" + resource.getString("pdcPeas.ForYourQuery") + query + "</div>");
	out.println("</td>");
	out.println("</tr>");
}

void displayFacet(Facet facet, MultiSilverpeasBundle resource, JspWriter out) throws IOException {
	if (facet != null && !facet.isEmpty()) {
		int facetResultLength = resource.getSetting("searchengine.facet.max.length", 30);
		int nbDefaultFacetEntries = resource.getSetting("searchengine.facet.default.nbEntries", 5);
		String facetId = facet.getId().replace("$$", "__"); // $$ is not supported by jQuery
	out.println("<div class=\"facet\" id=\"facet-"+facetId+"\">");
        out.println("<div id=\"searchGroupTitle\"><span class=\""+facetId+"\">"+facet.getName()+"</span></div>");
		out.println("<div id=\"searchGroupValues\">");
		out.println("<ul>");
		String selectedEntryId = "";
		String entryClass = "mainEntry";
		boolean displayToggle = facet.getEntries().size() > nbDefaultFacetEntries;
	for(int cpt=0; cpt < facet.getEntries().size(); cpt++){
			FacetEntryVO entry = facet.getEntries().get(cpt);
		    String entryName = entry.getName();
		    String entryId = entry.getId();
		    String displayComp = StringUtil.abbreviate(entryName, facetResultLength);
		    displayComp += "&nbsp;(" + entry.getNbElt() + ")";
		    String lastClass = "";
		    if (cpt == facet.getEntries().size() - 1) {
		      lastClass = "last";
		    }
		    if (cpt >= nbDefaultFacetEntries) {
		      entryClass = "otherEntry";
		    }
		    String jsAction = "filterResult('" + entryId + "', '"+facetId+"')";
		    String linkTitle = resource.getStringWithParams("pdcPeas.facet.tooltip.enable", entryName);
		    if (entry.isSelected()) {
		      selectedEntryId = entryId;
		      lastClass += " selected";
		      jsAction = "clearFilter('"+facetId+"')";
		      linkTitle = resource.getStringWithParams("pdcPeas.facet.tooltip.disable", entryName);
		    }
		    out.println("<li class=\""+lastClass+" "+entryClass+"\"><a href=\"javascript:"+jsAction+";\" title=\""+linkTitle+"\">"+displayComp+"</a></li>");
	}
		out.println("<input type=\"hidden\" name=\""+facet.getId()+"Filter\" id=\""+facetId+"FilterId\" value=\""+selectedEntryId+"\"/>");
		out.println("</ul>");
		if (displayToggle) {
			out.println("<a href=\"#\" onclick=\"javascript:toggleFacet('facet-"+facetId+"')\" class=\"toggle more\"><span>"+resource.getString("pdcPeas.facet.toggle.show")+"</span></a>");
		}
		out.println("</div>");
		out.println("</div>");
	}
}
%>

<c:set var="results" value="${requestScope['Results']}" />
<c:set var="exportEnabled" value="${requestScope['ExportEnabled']}" />
<c:set var="activeSelection" value="${requestScope['ActiveSelection']}" />
<c:set var="sortValue" value="${requestScope['SortValue']}" />
<c:set var="userId" value="${requestScope['UserId']}" />
<%
List<GlobalSilverResult> results 			= (List) request.getAttribute("Results");
int nbTotalResults		= ((Integer) request.getAttribute("NbTotalResults")).intValue();

int indexOfFirstResult	= ((Integer) request.getAttribute("IndexOfFirstResult")).intValue();
Boolean refreshEnabled	= (Boolean) request.getAttribute("RefreshEnabled");
boolean externalSearchEnabled = ((Boolean) request.getAttribute("ExternalSearchEnabled")).booleanValue();
Boolean	xmlSearchVisible	= (Boolean) request.getAttribute("XmlSearchVisible");
boolean	expertSearchVisible  = (Boolean) request.getAttribute("ExpertSearchVisible");
boolean	showPertinence	= ((Boolean) request.getAttribute("PertinenceVisible")).booleanValue();

String 	displayParamChoices = (String) request.getAttribute("DisplayParamChoices"); // All || Req || Res
List<String> choiceNbResToDisplay = (List<String>) request.getAttribute("ChoiceNbResToDisplay");
Integer nbResToDisplay		= (Integer) request.getAttribute("NbResToDisplay");
Integer sortValue		= (Integer) request.getAttribute("SortValue");
String sortOrder		= (String) request.getAttribute("SortOrder");
String sortResXForm = (String) request.getAttribute("XmlFormSortValue");
String sortImplementor = (String) request.getAttribute("sortImp");

// spelling words
List<String> spellingWords = (List<String>) request.getAttribute("spellingWords");

// List of Group result filter (new function added by EBO)
ResultGroupFilter facets = (ResultGroupFilter) request.getAttribute("ResultGroup");

// recuperation du choix de l'utilisateur
String keywords = (String) request.getAttribute("Keywords");
if (keywords == null) {
	keywords = "";
} else {
	keywords = EncodeHelper.javaStringToHtmlString(keywords);
}

// Contenu
String componentId	= "";

//pour le thesaurus
Map synonyms = (Map) request.getAttribute("synonyms");
String urlToRedirect = (String) request.getAttribute("urlToRedirect");
String backButtonClick;
if (urlToRedirect != null) {
    backButtonClick = "location.href='" + URLDecoder.decode(urlToRedirect, "UTF-8") + "';";
}

Board board = gef.getBoard();
Button searchButton = gef.getFormButton(resource.getString("pdcPeas.search"), "javascript:onClick=sendQuery()", false);

// keyword autocompletion
int autocompletionMinChars = resource.getSetting("autocompletion.minChars", 3);
boolean markResult 		= resource.getSetting("enableMarkAsRead", true);
boolean autoCompletion 	= resource.getSetting("enableAutocompletion", false);
QueryParser.Operator defaultOperand = IndexSearcher.defaultOperand;

int resultsDisplayMode = ((Integer) request.getAttribute("ResultsDisplay")).intValue();
String pageId = (String) request.getAttribute("ResultPageId");
if (!StringUtil.isDefined(pageId)) {
  pageId = "globalResult";
}

String facetToggleShow = resource.getString("pdcPeas.facet.toggle.show");
String facetToggleHide = resource.getString("pdcPeas.facet.toggle.hide");
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title><%=resource.getString("GML.popupTitle")%><c:out value="${anotherUserId}"></c:out></title>
<view:looknfeel />
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
<view:script src="/util/javaScript/checkForm.js"/>
<script type="text/javascript" src="<%=m_context%>/pdcPeas/jsp/javascript/formUtil.js"></script>
<!--[if IE 6]>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/jquery/jquery.bgiframe.min.js"></script>
<![endif]-->
<script type="text/javascript" src="<%=m_context%>/util/javaScript/jquery/thickbox-compressed.js"></script>
<view:includePlugin name="popup"/>
<view:includePlugin name="preview"/>
<script type="text/javascript">
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

	// This javascript method submit form in order to filter existing results
	function filterResult(value, type) {
		document.AdvancedSearch.action = "FilterSearchResult";
		$("#"+type+"FilterId").val(value);
		$.progressMessage();
	setTimeout("document.AdvancedSearch.submit();", 500);
	}

	// clearFilter
	function clearFilter(type) {
		document.AdvancedSearch.action = "FilterSearchResult";
		$("#"+type+"FilterId").val("");
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

	function toggleFacet(facet) {
		$("#"+facet+" .otherEntry").toggle();
		if ($("#"+facet+" .otherEntry").css("display") == "none") {
			$("#"+facet+" .toggle span").html("<%=facetToggleShow%>");
			$("#"+facet+" .toggle").addClass("more");
			$("#"+facet+" .toggle").removeClass("less");
		} else {
			$("#"+facet+" .toggle span").html("<%=facetToggleHide%>");
			$("#"+facet+" .toggle").addClass("less");
			$("#"+facet+" .toggle").removeClass("more");
		}
	}


    //used for mark as read functionality
	<%  if(markResult){ %>
function markAsRead(id) {
  if(id!=""){
    //$.post('<%=m_context%>/RpdcSearch/jsp/markAsRead', {id:id});
    $.ajax({
      url: '<%=m_context%>/SearchEngineAjaxServlet',
      async: false,
      data: { Action: 'markAsRead',
        id:id},
      success: function(data){
        //alert('markAsRead succeeded on element ' + data.id);
        $("#readSpanId_" + data.id).attr('class', 'markedkAsRead');
      },
      error: function() {
        //alert('XMLHttpRequest error');
        //HttpRequest, textStatus, errorThrown
        //alert(HttpRequest.status + " - " + textStatus+" - "+errorThrown);
      },
      dataType: 'json'
    });
  }
}

<%}%>

	<% if(spellingWords!= null && !spellingWords.isEmpty() && StringUtil.isDefined(spellingWords.get(0))){ %>
		function dymsend() {
			document.AdvancedSearch.query.value = '<%=EncodeHelper.javaStringToJsString(spellingWords.get(0))%>';
			document.AdvancedSearch.submit();
		}
	<% } %>

	function viewUserProfile(userId) {
		location.href = "<%=m_context%>/Rprofil/jsp/Main?userId="+userId;
	}

	$(document).ready(function(){
		//used for keywords autocompletion
	    <%  if(autoCompletion){ %>
			        $("#query").autocomplete({
                source: "<%=m_context%>/AutocompleteServlet",
                minLength: <%=autocompletionMinChars%>
      });
	    <%}%>
	  });

function showExternalSearchError() {
  $("#externalSearchErrorDivId").dialog();
}

function previewFile(target, attachmentId, versioned, componentId) {
  $(target).preview("previewAttachment", {
    componentInstanceId: componentId,
    attachmentId: attachmentId,
    versioned: versioned
  });
  return false;
}

function viewFile(target, attachmentId, versioned, componentId) {
  $(target).view("viewAttachment", {
	componentInstanceId: componentId,
    attachmentId: attachmentId,
    versioned: versioned
  });
  return false;
}
</script>
</head>
<body class="searchEngine" id="<%=pageId %>">
<form name="AdvancedSearch" action="javascript:sendQuery()" method="post">
<fmt:message var="resultLabel" key="pdcPeas.ResultPage" />
<view:browseBar extraInformations="${resultLabel}" />
<view:operationPane>
  <c:if test="${activeSelection}">
    <fmt:message var="iconSelection" key="pdcPeas.folder_to_valid" bundle="${icons}"/>
    <fmt:message var="messageSelection" key="pdcPeas.tracker_to_select" />
    <view:operation altText="${messageSelection}" icon="${iconSelection}" action="javascript:getSelectedOjectsFromResultList();"></view:operation>
  </c:if>
  <c:if test="${exportEnabled}">
    <fmt:message var="iconExport" key="pdcPeas.ToExport" bundle="${icons}"/>
    <fmt:message var="messageExport" key="pdcPeas.ToExport" />
    <fmt:message var="iconPDF" key="pdcPeas.exportPDF" bundle="${icons}"/>
    <fmt:message var="messagePDF" key="pdcPeas.exportPDF" />
    <view:operation altText="${messageExport}" icon="${iconExport}" action="javascript:openExportPopup();"></view:operation>
    <view:operation altText="${messagePDF}" icon="${iconPDF}" action="javascript:openExportPDFPopup();"></view:operation>
  </c:if>
</view:operationPane>
<view:window>
<%
	tabs = gef.getTabbedPane();
	tabs.addTab(resource.getString("pdcPeas.SearchResult"), "#", true);
	if (expertSearchVisible) {
	tabs.addTab(resource.getString("pdcPeas.SearchSimple"), "ChangeSearchTypeToAdvanced", false);
	tabs.addTab(resource.getString("pdcPeas.SearchAdvanced"), "ChangeSearchTypeToExpert", false);
	} else {
		tabs.addTab(resource.getString("pdcPeas.SearchPage"), "ChangeSearchTypeToAdvanced", false);
	}
	if (xmlSearchVisible) {
		tabs.addTab(resource.getString("pdcPeas.SearchXml"), "ChangeSearchTypeToXml", false);
    }

%>
<div id="globalResultTab"><%=tabs.print()%></div>
<div id="globalResultFrame">
  <view:frame>
    <div id="globalResultForm">
    <view:board>

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

<% if ("All".equals(displayParamChoices) || "Res".equals(displayParamChoices)) { %>
		<table id="globalResultParamDisplay" border="0" cellspacing="0" cellpadding="5" width="100%">
		<tr align="center">
          <td id="globalResultParamDisplayLabel"><%=resource.getString("pdcPeas.NbResultSearch")%></td>
          <td align="left" id="globalResultParamDisplayOptions"><select name="nbRes" size="1" onchange="javascript:changeResDisplay()">
            <%
				if (choiceNbResToDisplay != null) {
				String selected = "";
					for (String choice : choiceNbResToDisplay) {
						selected = "";
						if(choice.equals(nbResToDisplay.toString())) {
							selected = "selected=\"selected\"";
						}
						out.println("<option value=\""+choice+"\" "+selected+">"+choice+"</option>");
					}
				}
             %>
          </select>
		  <span>&nbsp;&nbsp;&nbsp;<%=resource.getString("pdcPeas.SortResultSearch")%>&nbsp;&nbsp;&nbsp;</span>
		  <select name="sortRes" size="1" onchange="javascript:changeResDisplay()">
            <%
		String selected = "";
				for (int i=1; i<=7; i++) {
					selected = "";
					if(sortValue.intValue() == i) {
						selected = "selected=\"selected\"";
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
        <c:if test="${activeSelection or exportEnabled}">
			<tr id="globalResultSelectAllResult">
				<td class="txtlibform"><fmt:message key="pdcPeas.selectAll" /></td><td><input type="checkbox" name="selectAll" onclick="selectEveryResult(this);"/></td>
			</tr>
        </c:if>
		</table>
<% } %>
  </view:board>
</div>
<div id="globalResultList">
<view:board>

<%
	if (results != null) {
		Pagination	pagination = gef.getPagination(nbTotalResults, nbResToDisplay.intValue(), indexOfFirstResult);

		List resultsOnThisPage = results;

		out.println("<table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"100%\">");
		displayItemsListHeader(keywords, pagination, resource, out);
        if(request.getAttribute("parseException") != null) {
		  out.println("<tr valign=\"middle\">");
		  out.println("<td align=\"center\">" + resource.getString("pdcPeas.NoResult") + ": " + resource.getString((String) request.getAttribute("parseException")) + "</td>");
		  out.println("</tr>");
		}
		out.println("<tr><td>");
	if(spellingWords!= null && !spellingWords.isEmpty() && StringUtil.isDefined(spellingWords.get(0))){
%>
		<table border="0" cellspacing="0" cellpadding="0" width="100%" id="globalResultListDidYouMean">
			<tr>
				<td>
					<span class="spellText" >
						 &nbsp;&nbsp; <% out.println(resource.getString("pdcpeas.didYouMean"));%>
					</span>
					<a href="javascript:dymsend();"><b><span class="spellWord"> <%=spellingWords.get(0)%></span></b></a>
					<p>&nbsp;</p>
				</td>
			</tr>
		</table>
<%  }

%>
  <c:if test="${not empty results}">
    <ul id="globalResultListDetails">
      <c:forEach var="result" items="${results}">
        <view:displayResult gsr="${result}" sortValue="${sortValue}" userId="0" activeSelection="${activeSelection}" exportEnabled="${exportEnabled}" resources="<%=resource %>"></view:displayResult>
      </c:forEach>
    </ul>
  </c:if>
<%
		out.println("</td></tr>");
		out.println("<tr class=\"intfdcolor4\"><td>&nbsp;</td></tr>");

		if (nbTotalResults > resultsOnThisPage.size()) {
			out.println("<tr valign=\"middle\" class=\"intfdcolor\">");
			out.println("<td align=\"center\">");
			out.println(pagination.printIndex("doPagination"));
			out.println("</td>");
			out.println("</tr>");
		}

		out.println("</table>");
	} else {
%>
    <table border="0" cellspacing="0" cellpadding="0" width="100%">
      <tr valign="middle">
        <td align="center"><%=resource.getString("pdcPeas.NoResult")%></td>
      </tr>
      <% if(request.getAttribute("parseException") != null) {%>
      <tr valign="middle">
        <td align="center"><%=resource.getString("pdcPeas.NoResult")%>: <%=resource.getString((String) request.getAttribute("parseException"))%></td>
      </tr>
      <%}%>
    </table>
<%
	}
%>
    </view:board>
    </div>
    <div id="globalResultHelp" class="inlineMessage">
		<table width="100%" border="0"><tr><td valign="top" width="30%">
        <fmt:message key="pdcPeas.helpCol1Header" /><br/><br/>
        <fmt:message key="pdcPeas.helpCol1Content1" /><br/>
        <fmt:message key="pdcPeas.helpCol1Content2" /><br/>
        <fmt:message key="pdcPeas.helpCol1Content3" /><br/>
		</td>
		<td>&nbsp;</td>
		<td valign="top" width="30%">
        <fmt:message key="pdcPeas.helpCol2Header" /><br/><br/>
        <%=resource.getStringWithParams("pdcPeas.helpCol2Content1", resource.getString("pdcPeas.help.operand."+defaultOperand.toString()))%><br/>
		<%=resource.getStringWithParams("pdcPeas.helpCol2Content2", defaultOperand.toString())%><br/>
        <fmt:message key="pdcPeas.helpCol2Content3" /><br/>
        <fmt:message key="pdcPeas.helpCol2Content4" /><br/>
        <fmt:message key="pdcPeas.helpCol2Content5" /><br/>
		</td>
		<td>&nbsp;</td>
		<td valign="top" width="30%">
        <fmt:message key="pdcPeas.helpCol3Header" /><br/><br/>
        <fmt:message key="pdcPeas.helpCol3Content1" /><br/>
        <fmt:message key="pdcPeas.helpCol3Content2" /><br/>
        <fmt:message key="pdcPeas.helpCol3Content3" /><br/>
        <fmt:message key="pdcPeas.helpCol3Content4" /><br/>
		</td>
		</tr></table>
  </div>
  </view:frame>
</div>
	<% if (facets != null) { %>
	  <input type="hidden" name="changeFilter" id="changeFilterId" value="" />
      <div id="globalResultGroupDivId">
	<div id="facetSearchDivId">
	<%
	displayFacet(facets.getAuthorFacet(), resource, out);

	List<Facet> fieldFacets = facets.getFormFieldFacets();
	for (Facet facet : fieldFacets) {
		displayFacet(facet, resource, out);
		}

	displayFacet(facets.getDatatypeFacet(), resource, out);
	displayFacet(facets.getFiletypeFacet(), resource, out);
	displayFacet(facets.getComponentFacet(), resource, out);
		%>
        </div>
      </div>
    <% } %>
</view:window>

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
<div id="externalSearchErrorDivId" style="display:none" title="<fmt:message key="pdcPeas.error"/>">
  <p><span class="ui-icon ui-icon-alert" style="float:left; margin:0 7px 0 0;"></span>
  <fmt:message key="pdcPeas.external.search.error" />
  </p>
</div>
<view:progressMessage/>
</body>
</html>