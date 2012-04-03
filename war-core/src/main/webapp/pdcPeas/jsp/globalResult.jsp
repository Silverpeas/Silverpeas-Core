<%--

    Copyright (C) 2000 - 2011 Silverpeas

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
<%@ page import="com.stratelia.silverpeas.pdcPeas.control.PdcSearchSessionController"%>
<%@ page import="com.stratelia.silverpeas.pdcPeas.vo.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.result.HtmlSearchResultTag"%>
<%@ include file="checkAdvancedSearch.jsp"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<%-- Set resource bundle --%>
<fmt:setLocale value="${sessionScope['SilverSessionController'].favoriteLanguage}" />
<view:setBundle basename="com.stratelia.silverpeas.pdcPeas.multilang.pdcBundle"/>
<view:setBundle basename="com.stratelia.silverpeas.pdcPeas.settings.pdcPeasIcons" var="icons" />

<%!
void displayItemsListHeader(String query, Pagination pagination, ResourcesWrapper resource, JspWriter out) throws IOException {
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
if (xmlSearch != null) {
	isXmlSearchVisible = xmlSearch.booleanValue();
}

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

int resultsDisplayMode = ((Integer) request.getAttribute("ResultsDisplay")).intValue();
String pageId = (String) request.getAttribute("ResultPageId");
if (!StringUtil.isDefined(pageId)) {
  pageId = "globalResult";
}
%>

<html>
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
<link rel="stylesheet" type="text/css" href="<%=m_context%>/util/styleSheets/jquery.autocomplete.css" media="screen">
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
<script type="text/javascript" src="<%=m_context%>/pdcPeas/jsp/javascript/formUtil.js"></script>
<!--[if IE 6]>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/jquery/jquery.bgiframe.min.js"></script>
<![endif]-->
<script type="text/javascript" src="<%=m_context%>/util/javaScript/jquery/jquery.autocomplete.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/jquery/thickbox-compressed.js"></script>
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
		if (type == 'author') {
			$("#userFilterId").val(value);
		} else if (type == 'component'){
			$("#componentFilterId").val(value);
		} else {
			$("#"+type).val(value);
		}
		$.progressMessage();
    	setTimeout("document.AdvancedSearch.submit();", 500);
	}

	// clearFilter
	function clearFilter(type) {
		document.AdvancedSearch.action = "FilterSearchResult";
		if (type == 'author') {
			$("#userFilterId").val("");
		} else if (type == 'component'){
			$("#componentFilterId").val("");
		} else {
			$("#"+type).val("");
		}
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

function showExternalSearchError() {
  $("#externalSearchErrorDivId").dialog();
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
	if ( isXmlSearchVisible ) {
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
        <c:if test="${activeSelection or exportEnabled}">
			<tr id="globalResultSelectAllResult">
				<td class="txtlibform"><fmt:message key="pdcPeas.selectAll" /></td><td><input type="checkbox" name="selectAll" onClick="selectEveryResult(this);"/></td>
			</tr>
        </c:if>
		</table>
<%
	}
%>
  </view:board>
</div>  
<div id="globalResultList">
<view:board>
  
<%
	if (results != null) {
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

%>
  <c:if test="${not empty results}">
    <table border="0" id="globalResultListDetails" cellspacing="0" cellpadding="0">
      <c:forEach var="result" items="${results}">
        <view:displayResult gsr="${result}" sortValue="${sortValue}" userId="0" activeSelection="${activeSelection}" exportEnabled="${exportEnabled}"></view:displayResult>
      </c:forEach>
    </table>
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
    </table>
<%
	}
%>
    </view:board>
    </div>
    <div id="globalResultHelp" class="inlineMessage">
		<table width="100%" border="0"><tr><td valign="top" width="30%">
        <fmt:message key="pdcPeas.helpCol1Header" /><br><br>
        <fmt:message key="pdcPeas.helpCol1Content1" /><br>
        <fmt:message key="pdcPeas.helpCol1Content2" /><br>
        <fmt:message key="pdcPeas.helpCol1Content3" /><br>
		</td>
		<td>&nbsp;</td>
		<td valign="top" width="30%">
        <fmt:message key="pdcPeas.helpCol2Header" /><br><br>
        <fmt:message key="pdcPeas.helpCol2Content1" /><br>
        <fmt:message key="pdcPeas.helpCol2Content2" /><br>
        <fmt:message key="pdcPeas.helpCol2Content3" /><br>
        <fmt:message key="pdcPeas.helpCol2Content4" /><br>
        <fmt:message key="pdcPeas.helpCol2Content5" /><br>
		</td>
		<td>&nbsp;</td>
		<td valign="top" width="30%">
        <fmt:message key="pdcPeas.helpCol3Header" /><br><br>
        <fmt:message key="pdcPeas.helpCol3Content1" /><br>
        <fmt:message key="pdcPeas.helpCol3Content2" /><br>
        <fmt:message key="pdcPeas.helpCol3Content3" /><br>
        <fmt:message key="pdcPeas.helpCol3Content4" /><br>
		</td>
		</tr></table>
  </div>
  </view:frame>
</div>
	<%

	// Adding facet search group
  	int facetResultLength = Integer.parseInt(resource.getSetting("searchengine.facet.max.length", "30"));
  	if (resultGroup != null) {
    	%>
	  <input type="hidden" name="changeFilter" id="changeFilterId" value="" />
      <div id="globalResultGroupDivId">
      	<div id="facetSearchDivId">
      	<%
      	Facet authorFacet = resultGroup.getAuthorFacet();
      	if (authorFacet != null) {
      	  %>
      	  <div class="facet">
      	  <div id="searchGroupTitle"><span class="author"><%=authorFacet.getName() %></span></div>
   		  <div id="searchGroupValues">
   			<ul>
      	  <%
      	  String selectedEntryId = "";
  	      for(int cpt=0; cpt < authorFacet.getEntries().size(); cpt++){
  	        FacetEntryVO author = authorFacet.getEntries().get(cpt);
  	        String authorName = author.getName();
  	        String authorId = author.getId();
  	        String displayAuthor = (authorName != null && authorName.length() > facetResultLength)? authorName.substring(0,facetResultLength) + "...":authorName;
  	        displayAuthor += "&nbsp;(" + author.getNbElt() + ")";
  	        String lastClass = "";
  	        if (cpt == authorFacet.getEntries().size() - 1) {
  	          lastClass = "last";
  	        }
  	        String jsAction = "filterResult('" + authorId + "', 'author')";
  	        String linkTitle = resource.getStringWithParam("pdcPeas.facet.tooltip.enable", authorName);
  	        if (author.isSelected()) {
  	          selectedEntryId = authorId;
  	          lastClass += " selected";
  	          jsAction = "clearFilter('author')";
  	          linkTitle = resource.getStringWithParam("pdcPeas.facet.tooltip.disable", authorName);
  	        } 
  	        %>
				<li class="<%=lastClass%>"><a href="javascript:<%=jsAction%>;" title="<%=linkTitle%>"><%=displayAuthor%></a></li>
  	      <% } %>
      		</ul>
      		<input type="hidden" name="authorFilter" id="userFilterId" value="<%=selectedEntryId%>"/>
      	  </div>
      	  </div>
      	  <%
      	}
      	%>
      	
      	<%
      	List<Facet> fieldFacets = resultGroup.getFormFieldFacets();
      	for (Facet facet : fieldFacets) {
	      	if (facet != null) {
	      	  %>
	      	  <div class="facet">
	      	  <div id="searchGroupTitle"><span class="formField"><%=facet.getName() %></span></div>
	   		  <div id="searchGroupValues">
	   			<ul>
	      	  <%
	      	  String selectedEntryId = "";
	      	  String facetId = facet.getId().replace("$$", "__"); // $$ is not supported by jQuery
	  	      for(int cpt=0; cpt < facet.getEntries().size(); cpt++){
	  	        FacetEntryVO entry = facet.getEntries().get(cpt);
	  	        String entryName = entry.getName();
	  	        String entryId = entry.getId();
	  	        String displayEntry = (entryName != null && entryName.length() > facetResultLength)? entryName.substring(0,facetResultLength) + "...":entryName;
	  	        displayEntry += "&nbsp;(" + entry.getNbElt() + ")";
	  	        String lastClass = "";
	  	        if (cpt == facet.getEntries().size() - 1) {
	  	          lastClass = "last";
	  	        }
	  	        String jsAction = "filterResult('" + entryId + "', '"+facetId+"')";
	  	        String linkTitle = resource.getStringWithParam("pdcPeas.facet.tooltip.enable", entryName);
	  	        if (entry.isSelected()) {
	  	          selectedEntryId = entryId;
	  	          lastClass += " selected";
	  	          jsAction = "clearFilter('"+facetId+"')";
	  	          linkTitle = resource.getStringWithParam("pdcPeas.facet.tooltip.disable", entryName);
	  	        }
	  	        %>
	  	  			<li class="<%=lastClass%>"><a href="javascript:<%=jsAction%>;" title="<%=linkTitle%>"><%=displayEntry%></a></li>
	  	       <% } %>
	      		</ul>
	      		<input type="hidden" name="<%=facet.getId() %>" id="<%=facetId %>" value="<%=selectedEntryId%>"/>
	      	  </div>
	      	  </div>
	      	  <%
	      	}
      	}
      	%>

      	<%
      	Facet componentFacet = resultGroup.getComponentFacet();
      	if (componentFacet != null) {
      	  %>
      	  <div class="facet">
      	  <div id="searchGroupTitle"><span class="component"><%=componentFacet.getName() %></span></div>
   		  <div id="searchGroupValues">
   			<ul>
      	  <%
      	  String selectedEntryId = "";
  	      for(int cpt=0; cpt < componentFacet.getEntries().size(); cpt++){
  	        FacetEntryVO comp = componentFacet.getEntries().get(cpt);
  	        String compName = comp.getName();
  	        String compId = comp.getId();
  	        String displayComp = (compName != null && compName.length() > facetResultLength)? compName.substring(0,facetResultLength) + "...":compName;
  	        displayComp += "&nbsp;(" + comp.getNbElt() + ")";
  	        String lastClass = "";
  	        if (cpt == componentFacet.getEntries().size() - 1) {
  	          lastClass = "last";
  	        }
  	        String jsAction = "filterResult('" + compId + "', 'component')";
  	        String linkTitle = resource.getStringWithParam("pdcPeas.facet.tooltip.enable", compName);
  	        if (comp.isSelected()) {
  	          selectedEntryId = compId;
  	          lastClass += " selected";
  	          jsAction = "clearFilter('component')";
  	          linkTitle = resource.getStringWithParam("pdcPeas.facet.tooltip.disable", compName);
  	        }
  	        %>
  				<li class="<%=lastClass%>"><a href="javascript:<%=jsAction%>;" title="<%=linkTitle%>"><%=displayComp%></a></li>
  	      <% } %>
      	  	<input type="hidden" name="componentFilter" id="componentFilterId" value="<%=selectedEntryId%>"/>
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
%>
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