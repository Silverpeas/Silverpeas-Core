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

<%@ page import="com.stratelia.silverpeas.peasCore.URLManager"%>
<%@ page import="com.silverpeas.pdcSubscription.model.PDCSubscription"%>
<%@ page import="com.silverpeas.util.EncodeHelper"%>
<%@ include file="checkAdvancedSearch.jsp"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%!

String displaySynonymsAxis(Boolean activeThesaurus, Jargon jargon, int axisId) throws ThesaurusException {
	String synonyms = "";
	boolean first = true;
	if (jargon != null && activeThesaurus.booleanValue()) {//activ�
		//synonymes du terme
		String				idUser			= jargon.getIdUser();
		ThesaurusManager	thesaurus		= new ThesaurusManager();
		Collection			listSynonyms	= thesaurus.getSynonymsAxis(Integer.toString(axisId), idUser);
		Iterator			it				= listSynonyms.iterator();
		synonyms += "<i>";
		while (it.hasNext()) {
			String name = (String) it.next();
			if (first) {
				synonyms += "- "+name;
			}
			else
				synonyms += ", "+name;
			first = false;
		}
		synonyms += "</i>";
	}
	return synonyms;
}

String displaySynonymsValue(Boolean activeThesaurus, Jargon jargon, Value value) throws ThesaurusException {
	String	synonyms	= "";
	boolean first		= true;
	String	idTree		= value.getTreeId();
	String	idTerm		= value.getPK().getId();
	if (jargon != null && activeThesaurus.booleanValue()) {//activ�
		//synonymes du terme
		String				idUser			= jargon.getIdUser();
		ThesaurusManager	thesaurus		= new ThesaurusManager();
		Collection			listSynonyms	= thesaurus.getSynonyms(new Long(idTree).longValue(), new Long(idTerm).longValue(), idUser);
		Iterator			it				= listSynonyms.iterator();
		synonyms += "<i>";
		while (it.hasNext()) {
			String name = (String) it.next();
			if (first) {
				synonyms += "- "+name;
			}
			else
				synonyms += ", "+name;
			first = false;
		}
		synonyms += "</i>";
	}
	return synonyms;
}

String getValueIdFromPdcSearchContext(int axisId, SearchContext searchContext)
{
	SearchCriteria criteria = searchContext.getCriteriaOnAxis(axisId);
	if (criteria != null)
		return criteria.getValue();
	else
		return null;
}

void displayAxisByType(boolean showAllAxis, String axisLabel, List axis, SearchContext searchContext, Boolean activeThesaurus, Jargon jargon, ResourcesWrapper resource, String axisTypeIcon, JspWriter out) throws ThesaurusException, IOException {
	SearchAxis	searchAxis			= null;
	int			axisId				= -1;
	String		axisName			= null;
	int			nbPositions			= -1;
	String		valueInContext		= null;
	Value		value				= null;
	String		increment			= "";
	String		selected			= "";
	String		sNbObjects			= "";
	String		language			= resource.getLanguage();
	
    out.println("<table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"5\">");
		// il peut y avoir aucun axe primaire dans un 1er temps
		if (axis != null && axis.size()>0){
            for (int i=0; i<axis.size(); i++){
				searchAxis		= (SearchAxis) axis.get(i);
                axisId			= searchAxis.getAxisId();
                axisName		= EncodeHelper.javaStringToHtmlString(searchAxis.getAxisName(language));
                nbPositions 	= searchAxis.getNbObjects();
                valueInContext 	= getValueIdFromPdcSearchContext(axisId, searchContext);
                if (nbPositions != 0)
                {
                    out.println("<tr>");
                    out.println("<td class=\"txtlibform\" width=\"30%\" nowrap><img src=\""+axisTypeIcon+"\" alt=\""+axisLabel+"\" align=\"absmiddle\">&nbsp;"+axisName+"&nbsp;"+displaySynonymsAxis(activeThesaurus, jargon, axisId)+":</td>");
                    if (showAllAxis)
                    	out.println("<td><select name=\"Axis"+axisId+"\" size=\"1\">");
                    else
                    	out.println("<td><select name=\"Axis"+axisId+"\" size=\"1\" onChange=\"javascript:addValue(this, '"+axisId+"');\">");
                    out.println("<option value=\"\"></option>");
                    List values = searchAxis.getValues();
                    for (int v=0; v<values.size(); v++)
                    {
                    	value = (Value) values.get(v);
                    	
                    	for (int inc=0; inc<value.getLevelNumber(); inc++)
                    	{
                    		increment += "&nbsp;&nbsp;&nbsp;&nbsp;";
                    	}
                    	
                    	if (searchContext.isEmpty())
                    	{
                    		sNbObjects = " ("+value.getNbObjects()+")";
                    	}
                    	else if (valueInContext == null)
                    	{
                    		sNbObjects = " ("+value.getNbObjects()+")";
                    	}
                    	else if (value.getFullPath().equals(valueInContext))
                    	{
                    		selected = " selected";
                    		sNbObjects = " ("+value.getNbObjects()+")";
                    	}
                    	else if (value.getFullPath().indexOf(valueInContext)!=-1)
                    		sNbObjects = " ("+value.getNbObjects()+")";
                    	
                    	out.println("<option value=\""+value.getFullPath()+"\""+selected+">"+increment+value.getName(language));
                    	if (!showAllAxis) {
	                    	out.println(sNbObjects);
	                    }
                    	out.println(displaySynonymsValue(activeThesaurus, jargon, value));
                    	out.println("</option>");
                    	
                    	increment 	= "";
                    	selected	= "";
                    	sNbObjects	= "";
                    }
                    out.println("</select></td>");
                    out.println("</tr>");                        
               }
			}// fin du for
		} else {
			out.println("<tr><td width=\"100%\" class=\"txtnav\" bgcolor=\"EDEDED\">&nbsp;</td></tr>");
		} // fin du else
    out.println("</table>");
}

%>
<%
String strpdcs = (String)request.getAttribute("isPDCSubscription");
boolean isPDCSubscription = false;
if (strpdcs != null && strpdcs.equalsIgnoreCase("true")) {
   isPDCSubscription = true;
}

boolean isNewPDCSubscription = false;
String strisNewPDC = (String)request.getAttribute("isNewPDCSubscription");
if (strisNewPDC != null && strisNewPDC.equalsIgnoreCase("true")) {
   isNewPDCSubscription = true;
}
PDCSubscription subscription 		= (PDCSubscription) request.getAttribute("PDCSubscription");

//recuperation des parametres pour le PDC
List			primaryAxis			= (List) request.getAttribute("ShowPrimaryAxis");
List			secondaryAxis		= (List) request.getAttribute("ShowSecondaryAxis");
String			showSndSearchAxis	= (String) request.getAttribute("ShowSndSearchAxis");
SearchContext	searchContext		= (SearchContext) request.getAttribute("SearchContext");
Jargon			jargon				= (Jargon) request.getAttribute("Jargon");
Boolean			activeThesaurus		= (Boolean) request.getAttribute("ActiveThesaurus");
Boolean 		activeSelection 	= (Boolean) request.getAttribute("ActiveSelection");
Boolean			XmlSearch			= (Boolean) request.getAttribute("XmlSearchVisible");

//CBO : ADD
String 			displayParamChoices = (String) request.getAttribute("DisplayParamChoices"); // All || Req || Res
List 			choiceNbResToDisplay = (List) request.getAttribute("ChoiceNbResToDisplay");
Integer			nbResToDisplay		= (Integer) request.getAttribute("NbResToDisplay");
Integer			sortValue			= (Integer) request.getAttribute("SortValue");
String			sortOrder			= (String) request.getAttribute("SortOrder");
List			webTabs				= (List) request.getAttribute("WebTabs");

boolean isXmlSearchVisible = false;
if (XmlSearch != null)
	isXmlSearchVisible = XmlSearch.booleanValue();

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

List			allComponents		= (List) request.getAttribute("ComponentList");
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
if (query != null)
{
	spaceSelected		= query.getSpaceId();
	componentSelected	= query.getInstanceId();
	keywords			= EncodeHelper.javaStringToHtmlString(query.getKeywords());
	createAfterDate		= query.getAfterDate();
	createBeforeDate	= query.getBeforeDate();
	updateAfterDate		= query.getAfterUpdateDate();
	updateBeforeDate	= query.getBeforeUpdateDate();
	userDetail			= query.getCreatorDetail();
}

if (keywords == null)
	keywords = "";
if (createAfterDate == null)
  	createAfterDate = "";
if (createBeforeDate == null)
  	createBeforeDate = "";
if (updateAfterDate == null)
  	updateAfterDate = "";
if (updateBeforeDate == null)
  	updateBeforeDate = "";


//r�cup�ration des donn�es pour l'espace de recherche
Vector searchDomains			= (Vector) request.getAttribute("searchDomains");
String currentSearchDomainId	= (String) request.getAttribute("currentSearchDomainId");
currentSearchDomainId = (currentSearchDomainId==null) ? "SILVERPEAS" : currentSearchDomainId;

boolean			isEmptySearchContext = true;
SearchCriteria	searchCriteria		= null;

if (showSndSearchAxis == null)
    showSndSearchAxis = "NO";

// l'objet SearchContext n'est pas vide
if (searchContext != null && searchContext.getCriterias().size() > 0){
	isEmptySearchContext = false;
}

String selected = "";

String icoHelp	= m_context + "/util/icons/info.gif";
String icoUser	= m_context + "/util/icons/user.gif";

Board board = gef.getBoard();

ButtonPane buttonPane = gef.getButtonPane();
Button searchButton = (Button) gef.getFormButton(resource.getString("pdcPeas.search"), "javascript:onClick=sendQuery()", false);


ResourceLocator resourceSearchEngine = new ResourceLocator(
        "com.stratelia.silverpeas.pdcPeas.settings.pdcPeasSettings", "");
        int autocompletionMinChars = SilverpeasSettings.readInt(resourceSearchEngine, "autocompletion.minChars", 3);

%>


<html>
<HEAD>
<TITLE><%=resource.getString("GML.popupTitle")%></TITLE>
<%
   out.println(gef.getLookStyleSheet());
%>
<link rel="stylesheet" type="text/css" href="<%=m_context%>/util/styleSheets/jquery.autocomplete.css" media="screen">
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/dateUtils.js"></script>
<script type="text/javascript" src="javascript/formUtil.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/jquery/jquery.bgiframe.min.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/jquery/jquery.autocomplete.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/jquery/thickbox-compressed.js"></script>

<script language="JavaScript1.2">
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

function getPdcContextAsString()
{
	var axisValueCouples = "";
	var myForm = document.AdvancedSearch;
	var myElement;
	for (var i=0;i<myForm.length;i++ ){
		myElement = myForm.elements[i];
		if (myElement.name.substring(0, 4) == "Axis"){
			if (myElement.value.length>0)
				axisValueCouples += myElement.name.substring(4, myElement.name.length)+"-"+myElement.value+",";
		}
	}
	
	return axisValueCouples;
}

function saveAsInterestCenter() {
	
	document.AdvancedSearch.AxisValueCouples.value = getPdcContextAsString();
	
	if (document.AdvancedSearch.iCenterId.selectedIndex)
		icName = document.AdvancedSearch.iCenterId.options[document.AdvancedSearch.iCenterId.selectedIndex].text;
	else
		icName = "";
	url			= "<%=m_context%><%=URLManager.getURL(URLManager.CMP_INTERESTCENTERPEAS)%>newICenter.jsp?icName="+icName;
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
	document.AdvancedSearch.submit();
}

// cette methode appelle le routeur avec le parametre ShowSndSearchAxis
function viewSecondaryAxis(show){
	if (show){
		document.AdvancedSearch.ShowSndSearchAxis.value = "YES";
	} else {
		document.AdvancedSearch.ShowSndSearchAxis.value = "NO";
	}
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
	document.AdvancedSearch.submit();
}

function sendQuery() {
	if (document.AdvancedSearch.query.value == "*")
		document.AdvancedSearch.query.value = "";
	if (checkDates())
	{
		top.topFrame.document.searchForm.query.value = "";
		document.AdvancedSearch.action = "AdvancedSearch";
		
		$.progressMessage();
    	setTimeout("document.AdvancedSearch.submit();", 500);
	}
}

function sendSelectionQuery() {
	document.AdvancedSearch.action = "AdvancedSearch";
	document.AdvancedSearch.submit();
}

function addSubscription() {
	if (document.all["scName"].value == '') {
		alert('<%=EncodeHelper.javaStringToJsString(resource.getString("pdcSubscription.Name.NotEmpty"))%>');
		return;
	}
	
	var axisValueCouples = getPdcContextAsString();
	
	if (axisValueCouples.length==0) {
		alert('<%=resource.getString("pdcSubscription.Values.NotEmpty")%>');
		return;
	}
	
	document.AdvancedSearch.AxisValueCouples.value = axisValueCouples;
	
	<% if (isNewPDCSubscription) { %>
		document.AdvancedSearch.action = "addSubscription";
	<% } else {%>
		document.AdvancedSearch.action = "updateSubscription";
	<% } %>
	
	document.AdvancedSearch.submit();
}

var errorMsg;
var errorNb;
function areDatesOK(afterDate, beforeDate)
{
	var re = /(\d\d\/\d\d\/\d\d\d\d)/i;
	
	if (!isWhitespace(afterDate))
	{
		var yearBegin = extractYear(afterDate, '<%=resource.getLanguage()%>');
		var monthBegin = extractMonth(afterDate, '<%=resource.getLanguage()%>');
		var dayBegin = extractDay(afterDate, '<%=resource.getLanguage()%>');
	}

	if (!isWhitespace(beforeDate))
	{
		var yearEnd = extractYear(beforeDate, '<%=resource.getLanguage()%>');
		var monthEnd = extractMonth(beforeDate, '<%=resource.getLanguage()%>');
		var dayEnd = extractDay(beforeDate, '<%=resource.getLanguage()%>');
	}

	var afterDateOK = true;

	if (isWhitespace(afterDate)) {
		//do nothing
	} else {
		if (afterDate.replace(re, "OK") != "OK") {
		   errorMsg+="  - <%=resource.getString("pdcPeas.TheField")%> '<%=resource.getString("pdcPeas.AfterDate")%>' <%=resource.getString("pdcPeas.MustContainsCorrectDate")%>\n";
		   errorNb++;
		   afterDateOK = false;
		} else {
		   if (isCorrectDate(yearBegin, monthBegin, dayBegin)==false) {
			 errorMsg+="  - <%=resource.getString("pdcPeas.TheField")%> '<%=resource.getString("pdcPeas.AfterDate")%>' <%=resource.getString("pdcPeas.MustContainsCorrectDate")%>\n";
			 errorNb++;
			 afterDateOK = false;
		   }
		}
	 }
	 if (isWhitespace(beforeDate)) {
		//do nothing
	 } else {
		   if (beforeDate.replace(re, "OK") != "OK") {
				 errorMsg+="  - <%=resource.getString("pdcPeas.TheField")%> '<%=resource.getString("pdcPeas.BeforeDate")%>' <%=resource.getString("pdcPeas.MustContainsCorrectDate")%>\n";
				 errorNb++;
		   } else {
				 if (isCorrectDate(yearEnd, monthEnd, dayEnd)==false) {
					 errorMsg+="  - <%=resource.getString("pdcPeas.TheField")%> '<%=resource.getString("pdcPeas.BeforeDate")%>' <%=resource.getString("pdcPeas.MustContainsCorrectDate")%>\n";
					 errorNb++;
				 } else {
					 if ((isWhitespace(afterDate) == false) && (isWhitespace(beforeDate) == false)) {
						   if (afterDateOK && isD1AfterD2(yearEnd, monthEnd, dayEnd, yearBegin, monthBegin, dayBegin) == false) {
								  errorMsg+="  - <%=resource.getString("pdcPeas.TheField")%> '<%=resource.getString("pdcPeas.BeforeDate")%>' <%=resource.getString("pdcPeas.MustContainsPostDateToBeginDate")%>\n";
								  errorNb++;
						   }
					 }
				 }
		   }
	 }
}

function checkDates() {
	errorMsg = "";
	errorNb = 0;
	var re = /(\d\d\/\d\d\/\d\d\d\d)/i;
	
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
	SP_openWindow('help.jsp', 'Aide', '700', '220','scrollbars=yes, resizable, alwaysRaised');
}

function callUserPanel()
{
	SP_openWindow('ToUserPanel','', '750', '550','scrollbars=yes, resizable, alwaysRaised');
}

function calculateAction()
{
	var index = document.AdvancedSearch.searchDomainId.selectedIndex;
	var value = document.AdvancedSearch.searchDomainId.options[index].value;
	if (value!="SILVERPEAS")
	{
		document.AdvancedSearch.action = "SpecificDomainView";
	}
	else
	{
		document.AdvancedSearch.action = "GlobalView";
	}
	document.AdvancedSearch.submit();
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

 $(document).ready(function(){
		//used for keywords autocompletion
	    <%  if(SilverpeasSettings.readBoolean(resourceSearchEngine, "enableAutocompletion", false)){ %>
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
<BODY onLoad="onLoadStart();">
<%
if (!isPDCSubscription) {
	browseBar.setComponentName(resource.getString("pdcPeas.SearchPage"));
	if (searchType == 2)
	{
		// affichage de l'icone voir les axes secondaires ou les cacher
		if (secondaryAxis == null) {
			operationPane.addOperation(resource.getIcon("pdcPeas.icoDisplaySecondaryAxis"), resource.getString("pdcPeas.showSecondaryAxis"), "javascript:viewSecondaryAxis(true)");
		} else {
			operationPane.addOperation(resource.getIcon("pdcPeas.icoDisplayPrimaryAxis"), resource.getString("pdcPeas.hideSecondaryAxis"), "javascript:viewSecondaryAxis(false)");
		}
	}

	if (!activeSelection.booleanValue() && searchType >= 1)
		operationPane.addOperation(resource.getIcon("pdcPeas.icoSaveAsInterestCenter"), resource.getString("pdcPeas.saveAsInterestCenter"), "javascript:saveAsInterestCenter()");

	if (activeSelection.booleanValue() && !isEmptySearchContext)
		operationPane.addOperation(resource.getIcon("pdcPeas.icoSearchPubli"), resource.getString("pdcPeas.searchResult"), "javascript:sendSelectionQuery()");
	
	tabs = gef.getTabbedPane();
	tabs.addTab(resource.getString("pdcPeas.SearchResult"), "LastResults", searchType==0);
	if (webTabs != null)
	{
		for (int i=0; i<webTabs.size(); i++)
		{
			GoogleTab webTab = (GoogleTab) webTabs.get(i);
			tabs.addTab(webTab.getLabel(), "ViewWebTab?Id="+i, false);
		}
	}
	tabs.addTab(resource.getString("pdcPeas.SearchSimple"), "ChangeSearchTypeToAdvanced", searchType==1);
	tabs.addTab(resource.getString("pdcPeas.SearchAdvanced"), "ChangeSearchTypeToExpert", searchType==2);
	if ( isXmlSearchVisible )
		tabs.addTab(resource.getString("pdcPeas.SearchXml"), "ChangeSearchTypeToXml", searchType==3);	
} else {
	browseBar.setComponentName(resource.getString("pdcSubscription.path"));
	if (isNewPDCSubscription) {
    	browseBar.setExtraInformation(resource.getString("pdcSubscription.newSubsription"));
	}
}
out.println(window.printBefore());
%>
<CENTER>
<form name="AdvancedSearch" action="ViewAdvancedSearch" method="post">
  <!-- champs cach� pour voir ou non les axes secondaires -->
  <input type="hidden" name="ShowSndSearchAxis" value="<%=showSndSearchAxis%>">
  <input type="hidden" name="showNotOnlyPertinentAxisAndValues" value="<%=showNotOnlyPertinentAxisAndValues%>">
  <input type="hidden" name="AxisId">
  <input type="hidden" name="ValueId">
  <input type="hidden" name="Ids">
  <input type="hidden" name="requestName">
  <input type="hidden" name="mode">
  <input type="hidden" name="AxisValueCouples">
  <input type="hidden" name="sortOrder" value=<%=sortOrder%>>

  <% if (isNewPDCSubscription) { %>
     <input type="hidden" name="isNewPDCSubscription" value="true">
  <% } 

	if (isPDCSubscription) { 
    	String initialName = (subscription != null)? subscription.getName(): "";
      	String paramName = request.getParameter("scName");
      	String scResName = (paramName != null )? paramName : initialName;
      
      	out.println(frame.printBefore());
      	out.println(board.printBefore());
  %>
        <TABLE CELLPADDING="5" CELLSPACING="0" BORDER="0" width="100%">
		<tr>
        	<td valign="top" nowrap align="left" class="txtlibform" width="30%"><%=resource.getString("pdcSubscription.Name")%> :</td>
            <td align="left"><input type="text" name="scName" size="50" maxlength="100" value="<%=scResName%>"><input type="hidden" name="isPDCSubscription" value="true"></td>
        </tr>
        </TABLE>
  <%    
  		out.println(board.printAfter());
  		out.println("<br>");
        out.println("<center>");
        buttonPane.addButton((Button) gef.getFormButton(resource.getString("pdcSubscription.ok"), "javascript:addSubscription()", false));
        out.println(buttonPane.print());
        out.println("</center>");
		out.println(frame.printAfter());
		out.println("<br>");
		out.println(frame.printBefore());
 	} %>
<%
if (!activeSelection.booleanValue() && !isPDCSubscription)
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
                        <td valign="middle"><input type="text" onkeypress="checkEnter(event)" name="query" size="60" value="<%=keywords%>" id="query"></td>
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
							selected = "selected";
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
						selected = "selected";
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
		out.println("<br>");
		out.println(board.printBefore());
%>
        <table border="0" cellspacing="0" cellpadding="5" width="100%">
        <tr align="center">
          <td valign="top" nowrap align="left" class="txtlibform" width="30%"><%=resource.getString("pdcPeas.DomainSelect")%></td>
          <td align="left"><select name="spaces" size="1" onChange="javascript:viewAdvancedSearch()">
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

					if (space.getFullId().equals(spaceSelected)) {
						selected = " selected";
					}

					out.println("<option value=\""+space.getFullId()+"\""+selected+">"+incr+space.getName(language)+"</option>");
				}
             %>
             </select></td>
	    </tr>
			<%
				// Affichage des composants
				out.println("<tr align=\"center\">");
				out.println("<td valign=\"top\" nowrap align=\"left\"><span class=\"txtlibform\">"+resource.getString("pdcPeas.ComponentSelect")+"</span></td>");
				out.println("<td align=\"left\">");
				out.println("<select name=\"componentSearch\" size=1 onChange=\"javascript:viewAdvancedSearch()\">");
				out.println("<option value=\"\">"+resource.getString("pdcPeas.AllAuthors")+"</option>");
				ComponentInstLight component = null;
				for(int nI = 0; allComponents!=null && nI < allComponents.size(); nI++) {
						selected	= "";
						component	= (ComponentInstLight) allComponents.get(nI);
						if (component.getId().equals(componentSelected)){
							selected = " selected";
						}
						out.println("<option value=\""+component.getId()+"\""+selected+">"+component.getLabel(language)+"</option>");
				}
				out.println("</select>");
				out.println("</td>");
				out.println("</tr>");
			%>
          <tr align="center">
          <td valign="top" nowrap align="left" class="txtlibform"><%=resource.getString("pdcPeas.AuthorSelect")%></td>
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
						<td id="userName" nowrap="nowrap"><%=selectedUserName%></td>
						<td width="100%" nowrap="nowrap">
							<a href="javascript:callUserPanel()" style="visibility:visible"><img src="<%=icoUser%>" alt="<%=resource.getString("pdcPeas.openUserPanelPeas")%>" title="<%=resource.getString("pdcPeas.openUserPanelPeas")%>" border="0" align="absmiddle"/></a>&nbsp;<a id="deleteURL" href="javascript:deleteUser()" style="<%=deleteIconStyle%>"><img src="<%=m_context + "/util/icons/delete.gif"%>" alt="<%=resource.getString("GML.delete")%>" title="<%=resource.getString("GML.delete")%>" border="0" align="absmiddle"/></a>
						</td>
					</tr></table>
			</td>            
        </tr>
        <tr align="center">
          <td valign="top" nowrap align="left" class="txtlibform"><%=resource.getString("pdcPeas.CreateAfterDate")%></td>
          <td align="left"><input type="text" class="dateToPick" name="createafterdate" size="12" value="<%=createAfterDate%>"/>
            <span class="txtlibform"> <%=resource.getString("pdcPeas.BeforeDate")%></span><input type="text" class="dateToPick" name="createbeforedate" size="12" value="<%=createBeforeDate%>"/> <span class="txtnote"><%=resource.getString("GML.dateFormatExemple")%></span>
          </td>
        </tr>
        <tr align="center">
          <td valign="top" nowrap align="left" class="txtlibform"><%=resource.getString("pdcPeas.UpdateAfterDate")%></td>
          <td align="left"><input type="text" class="dateToPick" name="updateafterdate" size="12" value="<%=updateAfterDate%>">
            <span class="txtlibform"> <%=resource.getString("pdcPeas.BeforeDate")%></span><input type="text" class="dateToPick" name="updatebeforedate" size="12" value="<%=updateBeforeDate%>"/> <span class="txtnote"><%=resource.getString("GML.dateFormatExemple")%></span>
          </td>
        </tr>
        <tr align="center">
              <td valign="top" nowrap align="left" class="txtlibform"><%=resource.getString("pdcPeas.requestSelect")%></span>
              </td>
              <td align="left">
                <select name="iCenterId" size="1" onChange="javascript:loadICenter()">
                 <option value="-1"></option>
                 <%
                     String			requestId		= "";
					 InterestCenter favoriteRequest = null;
                     for (int i=0;i<favoriteRequests.size() ;i++ ){
						selected		= "";
						favoriteRequest = (InterestCenter) favoriteRequests.get(i);
						requestId		= new Integer(favoriteRequest.getId()).toString();
						if (requestId.equals(requestSelected)){
							 selected = " selected";
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
if (searchType == 2 && !isPDCSubscription)
	out.println("<br>");
	
if (activeSelection.booleanValue() || searchType == 2 || isPDCSubscription) {
	out.println(board.printBefore());
	String axisIcon = resource.getIcon("pdcPeas.icoPrimaryAxis");
	displayAxisByType(showAllAxis, resource.getString("pdcPeas.primaryAxis"), primaryAxis, searchContext, activeThesaurus, jargon, resource, axisIcon, out);
	if (secondaryAxis != null){
		axisIcon = resource.getIcon("pdcPeas.icoSecondaryAxis");
		displayAxisByType(showAllAxis, resource.getString("pdcPeas.secondaryAxis"), secondaryAxis, searchContext, activeThesaurus, jargon, resource, axisIcon, out);
	}
	out.println(board.printAfter());
} 
%>
<!-- fin de la recherche -->
<%
 if (!showAllAxis) {
    out.println("<br>");
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
}
out.println(frame.printAfter());
%>
</CENTER>
</form>
<%
	out.println(window.printAfter());
%>
<view:progressMessage/>
</BODY>
</HTML>