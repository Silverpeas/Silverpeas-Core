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


<%@page import="org.silverpeas.core.util.URLUtil"%>
<%@page import="org.apache.lucene.queryParser.QueryParser"%>
<%@page import="org.silverpeas.core.index.search.model.IndexSearcher"%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ page import="org.silverpeas.core.util.StringUtil"%>
<%@ page import="org.silverpeas.core.pdc.interests.model.Interests" %>

<%@ include file="checkAdvancedSearch.jsp"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<c:set var="dataTypes" value="${requestScope.ComponentSearchType}"></c:set>

<%
//recuperation des parametres pour le PDC
String			showSndSearchAxis	= (String) request.getAttribute("ShowSndSearchAxis");
SearchContext	searchContext		= (SearchContext) request.getAttribute("SearchContext");
Boolean 		activeSelection 	= (Boolean) request.getAttribute("ActiveSelection");
boolean			xmlSearchVisible	= (Boolean) request.getAttribute("XmlSearchVisible");
boolean			expertSearchVisible = (Boolean) request.getAttribute("ExpertSearchVisible");

String 			displayParamChoices = (String) request.getAttribute("DisplayParamChoices"); // All || Req || Res
List 			choiceNbResToDisplay = (List) request.getAttribute("ChoiceNbResToDisplay");
Integer			nbResToDisplay		= (Integer) request.getAttribute("NbResToDisplay");
Integer			sortValue			= (Integer) request.getAttribute("SortValue");
String			sortOrder			= (String) request.getAttribute("SortOrder");

if (activeSelection == null) {
	activeSelection = new Boolean(false);
}

int				searchType			= ((Integer) request.getAttribute("SearchType")).intValue();

// recuperation des parametres pour la recherche classique
List	favoriteRequests	= (List)request.getAttribute("RequestList");
String	requestSaved		= (String)request.getAttribute("requestSaved");
String	requestSelected		= (String)request.getAttribute("RequestSelected");

String showNotOnlyPertinentAxisAndValues = (String) request.getAttribute("showAllAxis");
boolean showAllAxis = ("true".equals(showNotOnlyPertinentAxisAndValues));
showNotOnlyPertinentAxisAndValues = showAllAxis ? showNotOnlyPertinentAxisAndValues : "";

List<ComponentInstLight> allComponents		= (List<ComponentInstLight>) request.getAttribute("ComponentList");
List			allSpaces			= (List) request.getAttribute("SpaceList");
QueryParameters query				= (QueryParameters) request.getAttribute("QueryParameters");
String			spaceSelected		= null;
String			componentSelected	= null;
String			keywords			= null;
String			createAfterDate		= null;
String			createBeforeDate	= null;
String			updateAfterDate		= null;
String			updateBeforeDate	= null;
UserDetail		userDetail			= null;
String			folder				= null;
if (query != null) {
	spaceSelected		= query.getSpaceId();
	componentSelected	= query.getInstanceId();
	keywords			= EncodeHelper.javaStringToHtmlString(query.getKeywords());
	createAfterDate		= DateUtil.getInputDate(query.getAfterDate(), language);
	createBeforeDate	= DateUtil.getInputDate(query.getBeforeDate(), language);
	updateAfterDate		= DateUtil.getInputDate(query.getAfterUpdateDate(), language);
	updateBeforeDate	= DateUtil.getInputDate(query.getBeforeUpdateDate(), language);
	userDetail			= query.getCreatorDetail();
	folder				= query.getFolder();
}

if (keywords == null) {
	keywords = "";
}
if (createAfterDate == null) {
	createAfterDate = "";
}
if (createBeforeDate == null) {
	createBeforeDate = "";
}
if (updateAfterDate == null) {
	updateAfterDate = "";
}
if (updateBeforeDate == null) {
	updateBeforeDate = "";
}

String itemType = (String) request.getAttribute("ItemType");

boolean			isEmptySearchContext = true;
SearchCriteria	searchCriteria		= null;

if (showSndSearchAxis == null) {
    showSndSearchAxis = "NO";
}

// l'objet SearchContext n'est pas vide
if (searchContext != null && searchContext.getCriterias().size() > 0){
	isEmptySearchContext = false;
}

String selected = "";

String icoHelp	= m_context + "/util/icons/info.gif";
String icoUser	= m_context + "/util/icons/user.gif";

Board board = gef.getBoard();

ButtonPane buttonPane = gef.getButtonPane();
Button searchButton = gef.getFormButton(resource.getString("pdcPeas.search"), "javascript:onClick=sendQuery()", false);

int autocompletionMinChars = resource.getSetting("autocompletion.minChars", 3);
QueryParser.Operator defaultOperand = IndexSearcher.defaultOperand;
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"  "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title><%=resource.getString("GML.popupTitle")%></title>
<view:looknfeel withCheckFormScript="true"/>
<view:includePlugin name="datepicker"/>
<script type="text/javascript" src="javascript/formUtil.js"></script>
<!--[if IE 6]>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/jquery/jquery.bgiframe.min.js"></script>
<![endif]-->
<script type="text/javascript" src="<%=m_context%>/util/javaScript/jquery/thickbox-compressed.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/silverpeas-pdc-widgets.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/silverpeas-pdc.js"></script>
<script type="text/javascript">
var icWindow = window;

// opens popup for entering request name
function onLoadStart() {
 <% if ("yes".equals(requestSaved)) { %>
    if (window.opener) {
        window.opener.location.href = window.opener.location.href;
        window.close();
    }
  <%}%>
}

function saveAsInterestCenter() {

	setPositionIntoForm();

	if (document.AdvancedSearch.iCenterId.selectedIndex) {
		icName = document.AdvancedSearch.iCenterId.options[document.AdvancedSearch.iCenterId.selectedIndex].text;
	} else {
		icName = "";
	}
	url			= "<%=m_context%><%=URLUtil.getURL(URLUtil.CMP_INTERESTCENTERPEAS)%>newICenter.jsp?icName="+icName;
	windowName = "createICenter";
	width		= "600";
	height		= "200";
	windowParams = "directories=0,menubar=0,toolbar=0,alwaysRaised";
	icWindow = SP_openWindow(url, windowName, width, height, windowParams);
}

function loadICenter(){
	if (document.AdvancedSearch.iCenterId.selectedIndex != 0) {
		document.AdvancedSearch.action = "LoadAdvancedSearch";
	} else {
		document.AdvancedSearch.action = "GlobalView";
	}
	document.AdvancedSearch.submit();
}

function positionOfInput(inputName){
	var myForm = document.AdvancedSearch;
	var nbElementInForm = myForm.length;
	// pour chaque element, je recupere son nom et je le compare avec l'inputName
	// qd c'est egal (tjrs egal) je retourne la position du champ inputName
	var ret;
	for (var i=0;i<nbElementInForm ;i++ ){
		if (myForm.elements[i].name == inputName){
			ret = i;
			break;
		}
	}
	return ret;
}

function viewAdvancedSearch(){
	$.progressMessage();
	document.AdvancedSearch.submit();
}

// cette methode appelle le routeur avec le parametre ShowSndSearchAxis
function viewSecondaryAxis(show){
	if (show){
		document.AdvancedSearch.ShowSndSearchAxis.value = "YES";
	} else {
		document.AdvancedSearch.ShowSndSearchAxis.value = "NO";
	}
	$.progressMessage();
	document.AdvancedSearch.submit();
}

function addValue(selectItem, axisId)
{
	var valuePath = selectItem.value;
	if (valuePath.length > 0)
	{
		document.AdvancedSearch.AxisId.value = axisId;
		document.AdvancedSearch.ValueId.value = valuePath;
		document.AdvancedSearch.action = "GlobalAddCriteria";
	}
	else
	{
		document.AdvancedSearch.Ids.value = axisId;
		document.AdvancedSearch.action = "GlobalDeleteCriteria";
	}
	$.progressMessage();
	document.AdvancedSearch.submit();
}

function sendQuery() {
	setPositionIntoForm();
    if (document.AdvancedSearch.query.value == "*") {
		document.AdvancedSearch.query.value = "";
    }
	if (checkDates()) {
		try {
		  // clear global input search
		  top.topFrame.document.searchForm.query.value = "";
		} catch (e) {
		  // catch exceptions if this input does not exist
		}
		document.AdvancedSearch.action = "AdvancedSearch";

		$.progressMessage();
	setTimeout("document.AdvancedSearch.submit();", 500);
	}
}

function setPositionIntoForm() {
	<% if (activeSelection.booleanValue() || searchType == 2) { %>
	var values = $('#used_pdc').pdc('selectedValues');
	if (values.length > 0) {
      document.AdvancedSearch.AxisValueCouples.value = values.flatten();
    }
	<% } %>
}

function sendSelectionQuery() {
	document.AdvancedSearch.action = "AdvancedSearch";
	document.AdvancedSearch.submit();
}

var errorMsg;
var errorNb;
function areDatesOK(afterDate, beforeDate)
{
	var afterDateOK = true;

	if (!isWhitespace(afterDate)) {
		if (!isDateOK(afterDate, '<%=resource.getLanguage()%>')) {
		   errorMsg+="  - <%=resource.getString("pdcPeas.TheField")%> '<%=resource.getString("pdcPeas.AfterDate")%>' <%=resource.getString("pdcPeas.MustContainsCorrectDate")%>\n";
		   errorNb++;
		   afterDateOK = false;
		}
	 }
	 if (!isWhitespace(beforeDate)) {
		   if (!isDateOK(beforeDate, '<%=resource.getLanguage()%>')) {
				 errorMsg+="  - <%=resource.getString("pdcPeas.TheField")%> '<%=resource.getString("pdcPeas.BeforeDate")%>' <%=resource.getString("pdcPeas.MustContainsCorrectDate")%>\n";
				 errorNb++;
		   } else {
				 if (!isWhitespace(afterDate) && !isWhitespace(beforeDate)) {
					   if (afterDateOK && !isDate1AfterDate2(beforeDate, afterDate, '<%=resource.getLanguage()%>')) {
							  errorMsg+="  - <%=resource.getString("pdcPeas.TheField")%> '<%=resource.getString("pdcPeas.BeforeDate")%>' <%=resource.getString("pdcPeas.MustContainsPostDateToBeginDate")%>\n";
							  errorNb++;
					   }
				 }
		   }
	 }
}

function checkDates() {
	errorMsg = "";
	errorNb = 0;

	var afterDate	= document.AdvancedSearch.createafterdate.value;
	var beforeDate	= document.AdvancedSearch.createbeforedate.value;
	areDatesOK(afterDate, beforeDate);

	afterDate	= document.AdvancedSearch.updateafterdate.value;
	beforeDate	= document.AdvancedSearch.updatebeforedate.value;
	areDatesOK(afterDate, beforeDate);

	switch(errorNb) {
		case 0 :
			result = true;
			break;
		default :
			errorMsg = "<%=resource.getString("pdcPeas.Errors")%> :\n" + errorMsg;
			window.alert(errorMsg);
			result = false;
			break;
	}
	return result;
}

function editHelp()
{
	SP_openWindow('help.jsp', 'Aide', '700', '270','scrollbars=yes, resizable, alwaysRaised');
}

function callUserPanel()
{
	SP_openWindow('ToUserPanel','', '750', '550','scrollbars=yes, resizable, alwaysRaised');
}

//fonction pour force la soumission du formulaire par la touche entr�e
function checkEnter(e){
	var characterCode;
	if(e && e.which){
		e = e
		characterCode = e.which
	}
	else{
		e = event
		characterCode = e.keyCode
	}
	if(characterCode == 13){
		sendQuery();
		return false
	}
	return true
}

function setSortOrder(order){
	document.AdvancedSearch.sortOrder.value = order;
	document.AdvancedSearch.submit();
}

function deleteUser()
{
	var userName = document.getElementById("userName");
	userName.innerHTML = "<%=resource.getString("pdcPeas.AllAuthors")%>";

	var userId = document.getElementById("userId");
	userId.setAttribute("value", "");

	document.getElementById("deleteURL").style.visibility = "hidden";
}

function showExplorer() {
	var componentId = $("#componentSearch").val();
	SP_openWindow('<%=URLUtil.getApplicationURL()%>/explorer/jsp/explorer.jsp?elementHidden=<%=QueryParameters.PARAM_FOLDER %>&elementVisible=pathAsString&resultType=path&scope='+componentId,'explorer',400,600,'scrollbars=yes');
}

 $(document).ready(function(){
    <% if(resource.getSetting("enableAutocompletion", false)){ %>
		//used for keywords autocompletion
	    $("#query").autocomplete({
        source: "<%=m_context%>/AutocompleteServlet",
        minLength: <%=autocompletionMinChars%>
	    });
	<%}%>

	<% if (StringUtil.isDefined(itemType)) { %>
		$("#dataType option[value='<%=itemType%>']").attr("selected", "selected");
	<% } %>
 });

</script>
</head>
<body id="advanced-search" onload="onLoadStart();">
<%
	browseBar.setComponentName(resource.getString("pdcPeas.SearchPage"));
	if (searchType == 2) {
		// affichage de l'icone voir les axes secondaires ou les cacher
		if ("NO".equalsIgnoreCase(showSndSearchAxis)) {
			operationPane.addOperation(resource.getIcon("pdcPeas.icoDisplaySecondaryAxis"), resource.getString("pdcPeas.showSecondaryAxis"), "javascript:viewSecondaryAxis(true)");
		} else {
			operationPane.addOperation(resource.getIcon("pdcPeas.icoDisplayPrimaryAxis"), resource.getString("pdcPeas.hideSecondaryAxis"), "javascript:viewSecondaryAxis(false)");
		}
	}

	if (!activeSelection.booleanValue() && searchType >= 1) {
		operationPane.addOperation(resource.getIcon("pdcPeas.icoSaveAsInterestCenter"), resource.getString("pdcPeas.saveAsInterestCenter"), "javascript:saveAsInterestCenter()");
	}

	if (activeSelection.booleanValue() && !isEmptySearchContext) {
		operationPane.addOperation(resource.getIcon("pdcPeas.icoSearchPubli"), resource.getString("pdcPeas.searchResult"), "javascript:sendSelectionQuery()");
	}

	tabs = gef.getTabbedPane();
	tabs.addTab(resource.getString("pdcPeas.SearchResult"), "LastResults", searchType==0);
	if (expertSearchVisible) {
	tabs.addTab(resource.getString("pdcPeas.SearchSimple"), "ChangeSearchTypeToAdvanced", searchType==1);
	tabs.addTab(resource.getString("pdcPeas.SearchAdvanced"), "ChangeSearchTypeToExpert", searchType==2);
	} else {
		tabs.addTab(resource.getString("pdcPeas.SearchPage"), "ChangeSearchTypeToAdvanced", searchType==1);
	}
	if (xmlSearchVisible) {
		tabs.addTab(resource.getString("pdcPeas.SearchXml"), "ChangeSearchTypeToXml", searchType==3);
	}
out.println(window.printBefore());
%>
<center>
<form name="AdvancedSearch" action="ViewAdvancedSearch" method="post">
  <!-- champs cache pour voir ou non les axes secondaires -->
  <input type="hidden" name="ShowSndSearchAxis" value="<%=showSndSearchAxis%>"/>
  <input type="hidden" name="showNotOnlyPertinentAxisAndValues" value="<%=showNotOnlyPertinentAxisAndValues%>"/>
  <input type="hidden" name="AxisId"/>
  <input type="hidden" name="ValueId"/>
  <input type="hidden" name="Ids"/>
  <input type="hidden" name="requestName"/>
  <input type="hidden" name="mode"/>
  <input type="hidden" name="AxisValueCouples"/>
  <input type="hidden" name="sortOrder" value="<%=sortOrder%>"/>
<%
if (!activeSelection.booleanValue())
{
	if (!showAllAxis) {
		out.println(tabs.print());
	}
	out.println(frame.printBefore());
	out.println(board.printBefore());
%>
		<table border="0" cellspacing="0" cellpadding="5" width="100%">
        <tr align="center">
          <td valign="middle" align="left" class="txtlibform" width="30%"><%=resource.getString("pdcPeas.SearchFind")%></td>
          <td align="left" valign="middle">
		<table border="0" cellspacing="0" cellpadding="0"><tr valign="middle">
                        <td valign="middle"><input type="text" onkeypress="checkEnter(event)" name="query" size="60" value="<%=keywords%>" id="query"/></td>
			<td valign="middle">&nbsp;</td>
			<td align="left" valign="middle" width="100%">
				<%
					if (!showAllAxis) {
						out.println(searchButton.print());
					}
				%>
			</td>
		</tr></table>
          </td>
        </tr>
        </table>

	<%
	if ("All".equals(displayParamChoices) || "Req".equals(displayParamChoices))
	{
	%>
		<table border="0" cellspacing="0" cellpadding="5" width="100%">
		<tr align="center">
		  <td valign="middle" align="left" class="txtlibform" width="30%"><%=resource.getString("pdcPeas.NbResultSearch")%></td>
		  <td align="left"><select name="nbRes" size="1">
			<%
				if (choiceNbResToDisplay != null)
				{
					Iterator it = (Iterator) choiceNbResToDisplay.iterator();
					String choice;
					selected = "";
					while (it.hasNext())
					{
						selected = "";
						choice = (String) it.next();
						if(choice.equals(nbResToDisplay.toString())) {
							selected = " selected=\"selected\"";
						}
						out.println("<option value=\""+choice+"\""+selected+">"+choice+"</option>");
					}
				}
			 %>
		  </select>
		  <span class="txtlibform">&nbsp;&nbsp;&nbsp;<%=resource.getString("pdcPeas.SortResultSearch")%>&nbsp;&nbsp;&nbsp;</span>
		  <select name="sortRes" size="1">
			<%
				for (int i=1; i<=7; i++) {
					selected = "";
					if(sortValue.intValue() == i) {
						selected = " selected=\"selected\"";
					}
					out.println("<option value=\""+i+"\""+selected+">"+resource.getString("pdcPeas.SortValueSearch."+i)+"</option>");
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
		</table>
	<%
	}

	out.println(board.printAfter());
	if (searchType >= 1)
	{
		out.println("<br/>");
		out.println(board.printBefore());
%>
        <table border="0" cellspacing="0" cellpadding="5" width="100%">
        <tr align="center" id="space-filter">
          <td valign="top" align="left" class="txtlibform" width="30%"><%=resource.getString("pdcPeas.DomainSelect")%></td>
          <td align="left"><select name="spaces" size="1" onchange="javascript:viewAdvancedSearch()">
            <%
				out.println("<option value=\"\">"+resource.getString("pdcPeas.AllAuthors")+"</option>");
				String			incr	= "";
				SpaceInstLight 	space	= null;
				for (int i=0;i<allSpaces.size();i++) {
					selected	= "";
					incr		= "";
					space	= (SpaceInstLight) allSpaces.get(i);
					for (int j=0; j<space.getLevel(); j++) {
						incr += "&nbsp;&nbsp;&nbsp;&nbsp;";
					}

					if (space.getId().equals(spaceSelected)) {
						selected = " selected=\"selected\"";
					}

					out.println("<option value=\""+space.getId()+"\""+selected+">"+incr+EncodeHelper.javaStringToHtmlString(space.getName(language))+"</option>");
				}
             %>
             </select></td>
	    </tr>
        <!-- Affichage des composants -->
        <tr align="center" id="component-filter">
			<%
				out.println("<td valign=\"top\"  align=\"left\"><span class=\"txtlibform\">"+resource.getString("pdcPeas.ComponentSelect")+"</span></td>");
				out.println("<td align=\"left\">");
				out.println("<select id=\"componentSearch\" name=\"componentSearch\" size=\"1\" onchange=\"javascript:viewAdvancedSearch()\">");
				out.println("<option value=\"\">"+resource.getString("pdcPeas.AllAuthors")+"</option>");
				if (allComponents != null) {
					for(ComponentInstLight component : allComponents) {
							selected = "";
							if (component.getId().equals(componentSelected)){
								selected = " selected=\"selected\"";
							}
							out.println("<option value=\""+component.getId()+"\""+selected+">"+EncodeHelper.javaStringToHtmlString(component.getLabel(language)) +"</option>");
					}
				}
				out.println("</select>");
				out.println("</td>");
			%>
        </tr>
        <% if (StringUtil.isDefined(componentSelected) && componentSelected.startsWith("kmelia")) { %>
        <tr id="folder-filter">
		<td class="txtlibform"><%=resource.getString("GML.theme")%></td>
		<td>
			<input type="text" id="pathAsString" size="60" disabled="disabled"/>
			<input type="hidden" id="<%=QueryParameters.PARAM_FOLDER %>" name="<%=QueryParameters.PARAM_FOLDER %>" value="<%=folder %>" />
			<a href="#" onclick="javascript:showExplorer();" title="<%=resource.getString("GML.upload.choose.browse")%>"><img src="<%=resource.getIcon("pdcPeas.folder")%>" alt="<%=resource.getString("GML.upload.choose.browse")%>"/></a>
		</td>
        </tr>
        <% } %>
        <!-- Affichage du type des publications -->
        <tr align="center" id="contribution-type-filter">
          <td valign="top"  align="left"><span class="txtlibform"><%=resource.getString("pdcPeas.searchType")%></span></td>
          <td align="left">
		<select name="dataType" id="dataType">
			<option value="0"><%=resource.getString("pdcPeas.AllAuthors")%></option>
				<c:if test="${not empty dataTypes}">
			<c:forEach var="dataType" items="${dataTypes}">
				<option value="<c:out value="${dataType.configId}"/>"><c:out value="${dataType.name}"/></option>
			</c:forEach>
				</c:if>
		</select>
          </td>
        </tr>

          <tr align="center" id="publisher-filter">
          <td valign="top"  align="left" class="txtlibform"><%=resource.getString("pdcPeas.AuthorSelect")%></td>
				<td align="left">
            <%
				String selectedUserId = "";
				String selectedUserName = resource.getString("pdcPeas.AllAuthors");
				String deleteIconStyle = "visibility:hidden";
				if (userDetail != null)
				{
					selectedUserId = userDetail.getId();
					selectedUserName = userDetail.getDisplayedName();
					deleteIconStyle = "visibility:visible";
				}
			%>
					<input type="hidden" name="authorSearch" id="userId" value="<%=selectedUserId%>"/>
					<table><tr>
						<td id="userName"><%=selectedUserName%></td>
						<td width="100%">
							<a href="javascript:callUserPanel()" style="visibility:visible"><img src="<%=icoUser%>" alt="<%=resource.getString("pdcPeas.openUserPanelPeas")%>" title="<%=resource.getString("pdcPeas.openUserPanelPeas")%>" border="0" /></a>&nbsp;<a id="deleteURL" href="javascript:deleteUser()" style="<%=deleteIconStyle%>"><img src="<%=m_context + "/util/icons/delete.gif"%>" alt="<%=resource.getString("GML.delete")%>" title="<%=resource.getString("GML.delete")%>" border="0"/></a>
						</td>
					</tr></table>
			</td>
        </tr>
        <tr align="center" id="creation-date-filter">
          <td valign="top" align="left" class="txtlibform"><%=resource.getString("pdcPeas.CreateAfterDate")%></td>
          <td align="left"><input type="text" class="dateToPick" name="createafterdate" size="12" value="<%=createAfterDate%>"/>
            <span class="txtlibform"> <%=resource.getString("pdcPeas.BeforeDate")%></span><input type="text" class="dateToPick" name="createbeforedate" size="12" value="<%=createBeforeDate%>"/> <span class="txtnote"><%=resource.getString("GML.dateFormatExemple")%></span>
          </td>
        </tr>
        <tr align="center" id="update-date-filter">
          <td valign="top" align="left" class="txtlibform"><%=resource.getString("pdcPeas.UpdateAfterDate")%></td>
          <td align="left"><input type="text" class="dateToPick" name="updateafterdate" size="12" value="<%=updateAfterDate%>"/>
            <span class="txtlibform"> <%=resource.getString("pdcPeas.BeforeDate")%></span><input type="text" class="dateToPick" name="updatebeforedate" size="12" value="<%=updateBeforeDate%>"/> <span class="txtnote"><%=resource.getString("GML.dateFormatExemple")%></span>
          </td>
        </tr>
        <tr align="center" id="favorite-request-filter">
              <td valign="top" align="left" class="txtlibform"><%=resource.getString("pdcPeas.requestSelect")%>
              </td>
              <td align="left">
                <select name="iCenterId" size="1" onchange="javascript:loadICenter()">
                 <option value="-1"></option>
                 <%
                     String			requestId		= "";
					 Interests favoriteRequest = null;
                     for (int i=0;i<favoriteRequests.size() ;i++ ){
						selected		= "";
						favoriteRequest = (Interests) favoriteRequests.get(i);
						requestId		= new Integer(favoriteRequest.getId()).toString();
						if (requestId.equals(requestSelected)){
							 selected = " selected=\"selected\"";
						}
						out.println("<option value=" + requestId +selected+">"+ favoriteRequest.getName() + "</option>");
                     }
                %>
                </select>
            </td>
        </tr>
        </table>
<%
		out.println(board.printAfter());
	}
}
if (searchType == 2) {
	out.println("<br/>");
}

if (activeSelection.booleanValue() || searchType == 2) {%>
  <div class="tableFrame">
    <div class="tableBoard">
      <fieldset id="used_pdc" class="skinFieldset"></fieldset>
    </div>
  </div>
<% }
%>
<!-- fin de la recherche -->
<% if (!showAllAxis) { %>
	<div class="inlineMessage">
	<table width="100%" border="0"><tr><td valign="top" width="30%">
		<%=resource.getString("pdcPeas.helpCol1Header")%><br/><br/>
		<%=resource.getString("pdcPeas.helpCol1Content1")%><br/>
		<%=resource.getString("pdcPeas.helpCol1Content2")%><br/>
		<%=resource.getString("pdcPeas.helpCol1Content3")%><br/>
		</td>
		<td>&nbsp;</td>
		<td valign="top" width="30%">
		<%=resource.getString("pdcPeas.helpCol2Header")%><br/><br/>
		<%=resource.getStringWithParams("pdcPeas.helpCol2Content1", resource.getString("pdcPeas.help.operand."+defaultOperand.toString()))%><br/>
		<%=resource.getStringWithParams("pdcPeas.helpCol2Content2", defaultOperand.toString())%><br/>
		<%=resource.getString("pdcPeas.helpCol2Content3")%><br/>
		<%=resource.getString("pdcPeas.helpCol2Content4")%><br/>
		<%=resource.getString("pdcPeas.helpCol2Content5")%><br/>
		</td>
		<td>&nbsp;</td>
		<td valign="top" width="30%">
		<%=resource.getString("pdcPeas.helpCol3Header")%><br/><br/>
		<%=resource.getString("pdcPeas.helpCol3Content1")%><br/>
		<%=resource.getString("pdcPeas.helpCol3Content2")%><br/>
		<%=resource.getString("pdcPeas.helpCol3Content3")%><br/>
		<%=resource.getString("pdcPeas.helpCol3Content4")%><br/>
		</td>
		</tr></table>
	</div>
<%
}
out.println(frame.printAfter());
%>
</form>
</center>
<%
	out.println(window.printAfter());
%>
<view:progressMessage/>
<%
  if (activeSelection.booleanValue() || searchType == 2) {
%>
<script type="text/javascript">
    var showSecondaryAxis = <%= ("YES".equals(showSndSearchAxis) ? true: false)%>;
    var componentId = <%= componentSelected != null ? "'" + componentSelected + "'": null %>;
    var workspaceId = <%= spaceSelected != null ? "'" + spaceSelected + "'": null %>;
    <%
  List<SearchCriteria> criteriaOnAxisValues = searchContext.getCriterias();
  StringBuilder valuesInJs = new StringBuilder("[");
  if (criteriaOnAxisValues.size() > 0) {
    valuesInJs.append("{axisId: ").append(criteriaOnAxisValues.get(0).getAxisId()).
        append(", id: '" + criteriaOnAxisValues.get(0).getValue()).append("'}");
    for(int i = 1; i < criteriaOnAxisValues.size(); i++) {
      valuesInJs.append(", {axisId: ").append(criteriaOnAxisValues.get(i).getAxisId()).
        append(", id: '" + criteriaOnAxisValues.get(i).getValue()).append("'}");
    }
  }
  valuesInJs.append("]");
  %>
      $('#used_pdc').pdc('used', {
        component: componentId,
        workspace: workspaceId,
        withSecondaryAxis: showSecondaryAxis,
        values: <%= valuesInJs %>
      });
</script>
<%
  }
%>
</body>
</html>