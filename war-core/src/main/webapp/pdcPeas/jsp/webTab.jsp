<%--

    Copyright (C) 2000 - 2012 Silverpeas

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

<%@page import="com.silverpeas.util.StringUtil"%>
<%@page import="com.stratelia.silverpeas.pdcPeas.control.GoogleTabsUtil"%>
<%@ include file="checkAdvancedSearch.jsp"%>

<%
boolean	xmlSearchVisible	= (Boolean) request.getAttribute("XmlSearchVisible");
boolean	expertSearchVisible  = (Boolean) request.getAttribute("ExpertSearchVisible");

String  webTabId	= (String) request.getAttribute("WebTabId");
List	webTabs		= (List) request.getAttribute("WebTabs");
GoogleTab webTab 	= (GoogleTab) webTabs.get(Integer.parseInt(webTabId));
List	webSites	= (List) webTab.getSites();

// recuperation du choix de l'utilisateur
String keywords = (String) request.getAttribute("Keywords");
if (keywords == null)
	keywords = "";
	
Board board = gef.getBoard();
%>

<html>
<HEAD>
<TITLE><%=resource.getString("GML.popupTitle")%></TITLE>
<script src="http://www.google.com/jsapi?key=<%=GoogleTabsUtil.getKey()%>" type="text/javascript"></script>
<script language="Javascript" type="text/javascript">
//<![CDATA[    
google.load("search", "1", {"language" : "<%=language%>"});    
function OnLoad() {
	// Create a search control
	var searchControl = new google.search.SearchControl();
	searchControl.setResultSetSize(google.search.Search.LARGE_RESULTSET);

	// create a draw options object so that we
  	// can position the search form root
  	var options = new google.search.DrawOptions();
  	//options.setSearchFormRoot(document.getElementById("searchForm"));

  	//Switch to tabular mode (default is linear)
	options.setDrawMode(<%=GoogleTabsUtil.getDrawMode()%>);

	var searcherOptions = new google.search.SearcherOptions();
	searcherOptions.setExpandMode(<%=GoogleTabsUtil.getExpandMode()%>);

  	// populate with searchers
  	<%
  		GoogleSite site;
  		for (int i=0; i<webSites.size(); i++)
  		{
  			site = (GoogleSite) webSites.get(i);
  		%>
  			var siteSearch<%=i%> = new google.search.WebSearch();
  			siteSearch<%=i%>.setUserDefinedLabel("<%=site.getLabel()%>");
  			siteSearch<%=i%>.setSiteRestriction("<%=site.getUrl()%>");
  			
  			searchControl.addSearcher(siteSearch<%=i%>, searcherOptions);
  		<%
  		}
  	%>

  	searchControl.draw(document.getElementById("searchcontrol"), options);
	
	<% if (GoogleTabsUtil.isBranding()) { %>
		// attach "powered by Google" branding
		google.search.Search.getBranding(document.getElementById("branding"));
	<% } %>	
	
	searchControl.execute("<%=keywords%>");

}    
google.setOnLoadCallback(OnLoad);
//]]>
</script>
<%=gef.getLookStyleSheet()%>
<LINK REL="stylesheet" TYPE="text/css" HREF="<%=m_context%>/pdcPeas/jsp/css/google.css"/>
<% if (StringUtil.isDefined(GoogleTabsUtil.getCss())) { %>
	<LINK REL="stylesheet" TYPE="text/css" HREF="<%=GoogleTabsUtil.getCss()%>"/>
<% } %>
<style type="text/css">
.gsc-search-box .gsc-branding {
	display: none;
}
<% if (!GoogleTabsUtil.isSearchBox()) { %>
table.gsc-search-box {
	display: none;
}
<% } %>
</style>
</HEAD>
<BODY>
<%
	browseBar.setComponentName(resource.getString("pdcPeas.ResultPage"));

	out.println(window.printBefore());
	
	tabs = gef.getTabbedPane();
	tabs.addTab(resource.getString("pdcPeas.SearchResult"), "LastResults", false);
	if (webTabs != null) {
		boolean webTabActivate = false;
		for (int i=0; i<webTabs.size(); i++) {
			GoogleTab oneWebTab = (GoogleTab) webTabs.get(i);
			webTabActivate = (oneWebTab.getId() == webTab.getId());
			tabs.addTab(oneWebTab.getLabel(), "ViewWebTab?Id="+i, webTabActivate);
		}
	}
	if (expertSearchVisible) {
	  	tabs.addTab(resource.getString("pdcPeas.SearchSimple"), "ChangeSearchTypeToAdvanced", false);
		tabs.addTab(resource.getString("pdcPeas.SearchAdvanced"), "ChangeSearchTypeToExpert", false);
	} else {
	  	tabs.addTab(resource.getString("pdcPeas.SearchPage"), "ChangeSearchTypeToAdvanced", false);
	}
	if (xmlSearchVisible) {
		tabs.addTab(resource.getString("pdcPeas.SearchXml"), "ChangeSearchTypeToXml", false);
	}
	
	out.println(tabs.print());
    out.println(frame.printBefore());

    out.println(board.printBefore());
%>
	<div id="searchcontrol">
		<!-- <div id="searchForm" style="display:none">Loading...</div> -->
		<!-- <div id="searchResults">Loading...</div>-->
	</div>
	<div id="branding"></div>
<%
	out.println(board.printAfter());
	
	out.println(frame.printAfter());
	out.println(window.printAfter());
%>
</body>
</html>