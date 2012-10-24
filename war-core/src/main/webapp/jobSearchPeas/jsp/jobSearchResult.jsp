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

<%@ page import="java.util.List"%>
<%@ page import="com.silverpeas.util.EncodeHelper"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.window.Window"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.browseBars.BrowseBar"%>
<%@page import="com.stratelia.webactiv.util.viewGenerator.html.arrayPanes.ArrayCell"%>
<%@page import="com.stratelia.webactiv.util.viewGenerator.html.arrayPanes.ArrayCellLink"%>
<%@ page import="com.silverpeas.jobSearchPeas.SearchResult"%>
<%@ include file="check.jsp" %>
<fmt:setLocale value="${requestScope.resources.language}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />

<%
String idOrName = (String) request.getAttribute("IdOrName"); //null or filled
String category = (String) request.getAttribute("Category"); //null or filled
List<SearchResult> listResult = (List<SearchResult>) request.getAttribute("ListResult"); //null or empty or filled

if(category == null) {
	category = "space"; //default category
}

%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<view:looknfeel />
<title><%=resource.getString("GML.popupTitle")%></title>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script type="text/javascript">
function isCorrectForm() {
	 var errorMsg = "";
     var errorNb = 0;
     var idOrName = stripInitialWhitespace(document.forms["SearchResultForm"].elements["SearchField"].value);

     if (isWhitespace(idOrName)) {
       errorMsg+="  - <%=resource.getString("GML.theField")%> '<%=resource.getString("JSP.searchField")%>' <%=resource.getString("GML.MustBeFilled")%>\n";
       errorNb++;
     }

     switch(errorNb) {
        case 0 :
            result = true;
            break;
        case 1 :
            errorMsg = "<%=resource.getString("GML.ThisFormContains")%> 1 <%=resource.getString("GML.error")%> : \n" + errorMsg;
            window.alert(errorMsg);
            result = false;
            break;
        default :
            errorMsg = "<%=resource.getString("GML.ThisFormContains")%> " + errorNb + " <%=resource.getString("GML.errors")%> :\n" + errorMsg;
            window.alert(errorMsg);
            result = false;
            break;
     }
     return result;
}
function validateSearch() {
	if (isCorrectForm()) {
		document.forms["SearchResultForm"].submit();
	}
}

var spaceWindow = window;
function openSpace(spaceId) {
	url = '<%=m_context%>/RjobStartPagePeas/jsp/OpenSpace?Espace='+spaceId;
    windowName = "spaceWindow";
	larg = "800";
	haut = "800";
    windowParams = "directories=0,menubar=0,toolbar=0,alwaysRaised";
    if (!spaceWindow.closed && spaceWindow.name == "spaceWindow") {
        spaceWindow.close();
	}
    spaceWindow = SP_openWindow(url, windowName, larg, haut, windowParams, false);
}

var subSpaceWindow = window;
function openSubSpace(rootSpaceId, subSpaceId) {
	url = '<%=m_context%>/RjobStartPagePeas/jsp/OpenSubSpace?Espace='+rootSpaceId+'&SousEspace='+subSpaceId;
    windowName = "subSpaceWindow";
	larg = "800";
	haut = "800";
    windowParams = "directories=0,menubar=0,toolbar=0,alwaysRaised";
    if (!subSpaceWindow.closed && subSpaceWindow.name == "subSpaceWindow") {
        subSpaceWindow.close();
	}
    subSpaceWindow = SP_openWindow(url, windowName, larg, haut, windowParams, false);
}

var componentWindow = window;
function openComponent(componentId) {
	url = '<%=m_context%>/RjobStartPagePeas/jsp/OpenComponent?ComponentId='+componentId;
    windowName = "componentWindow";
	larg = "800";
	haut = "800";
    windowParams = "directories=0,menubar=0,toolbar=0,alwaysRaised";
    if (!componentWindow.closed && componentWindow.name == "componentWindow") {
        componentWindow.close();
	}
    componentWindow = SP_openWindow(url, windowName, larg, haut, windowParams, false);
}

function openPublication(permalink) {
	window.location.href = permalink;
}

var groupWindow = window;
function openGroup(groupId) {
	url = '<%=m_context%>/RjobDomainPeas/jsp/groupOpen?groupId='+groupId;
    windowName = "groupWindow";
	larg = "800";
	haut = "800";
    windowParams = "directories=0,menubar=0,toolbar=0,alwaysRaised";
    if (!groupWindow.closed && groupWindow.name == "componentWindow") {
        groupWindow.close();
	}
    groupWindow = SP_openWindow(url, windowName, larg, haut, windowParams, false);
}

var userWindow = window;
function openUser(userId) {
	url = '<%=m_context%>/RjobDomainPeas/jsp/userOpen?userId='+userId;
    windowName = "userWindow";
	larg = "800";
	haut = "800";
    windowParams = "directories=0,menubar=0,toolbar=0,alwaysRaised";
    if (!userWindow.closed && userWindow.name == "componentWindow") {
        userWindow.close();
	}
    userWindow = SP_openWindow(url, windowName, larg, haut, windowParams, false);
}
</script>

</head>
<body onload="document.SearchResultForm.SearchField.focus()">

<%
browseBar.setDomainName(resource.getString("JSP.administrationTab"));
browseBar.setComponentName(resource.getString("JSP.searchTab"));

out.println(window.printBefore());
out.println(frame.printBefore());
out.println(board.printBefore());
%>

<div>
<form name="SearchResultForm" action="SearchResult" method="post">

	<span class="txtlibform"><fmt:message key="JSP.searchField"/> : </span>
    <input type="text" name="SearchField" size="60" maxlength="100" value="<%if(idOrName != null) { out.print(idOrName); }%>" />
	<img border="0" src="<%=m_context+"/util/icons/mandatoryField.gif"%>" width="5" height="5"/>
<br/>
	<input type="radio" name="Category" value="space" <%if("space".equals(category)) { out.print("checked"); }%> /><fmt:message key="JSP.space"/>	
<br/>
	<input type="radio" name="Category" value="service" <%if("service".equals(category)) { out.print("checked"); }%> /><fmt:message key="JSP.service"/>
<br/>
	<input type="radio" name="Category" value="publication" <%if("publication".equals(category)) { out.print("checked"); }%> /><fmt:message key="JSP.publication"/>
<br/>
	<input type="radio" name="Category" value="group" <%if("group".equals(category)) { out.print("checked"); }%> /><fmt:message key="JSP.group"/>
<br/>
	<input type="radio" name="Category" value="user" <%if("user".equals(category)) { out.print("checked"); }%> /><fmt:message key="JSP.user"/>
</form>
</div>
<%
out.println(board.printAfter());
%>
<div class="center">
<fmt:message key="JSP.search" var="search"/>
  <view:buttonPane>
	<view:button action="javascript:validateSearch();" label="${search}" disabled="false" />
  </view:buttonPane>
</div>
<br />

<%
if(listResult != null) {
	if(listResult.size() == 0) {
%>
<div class="inlineMessage"><%=resource.getString("JSP.noResult")%></div>
<%
	} else {
		ArrayPane arrayPane = gef.getArrayPane("searchResultList", "Main", request, session);
		arrayPane.setVisibleLineNumber(20);
		arrayPane.setTitle(resource.getString("JSP.listResult"));
		arrayPane.addArrayColumn(resource.getString("JSP.name"));
		arrayPane.addArrayColumn(resource.getString("JSP.desc"));
		if("space".equals(category) || "service".equals(category) || "publication".equals(category)) { 
			arrayPane.addArrayColumn(resource.getString("JSP.creaDate"));
			arrayPane.addArrayColumn(resource.getString("JSP.creaName"));
		}
		arrayPane.addArrayColumn(resource.getString("JSP.path"));

		for(SearchResult searchResult : listResult) {
			String name = EncodeHelper.javaStringToHtmlString(searchResult.getName());
			String url	= searchResult.getUrl();
			ArrayLine arrayLine = arrayPane.addArrayLine();
			arrayLine.addArrayCellText("<a href=\"#\" onclick=\""+url+"\">"+name+"</a>");
			
			String desc = searchResult.getDesc();
			if (desc != null && desc.length() > 200) {
				desc = desc.substring(0, 200)+"...";
			}
			arrayLine.addArrayCellText(desc);
			
			if("space".equals(category) || "service".equals(category) || "publication".equals(category)) { 
				try	{
					String creaDate = resource.getOutputDate(searchResult.getCreaDate());
					ArrayCellText cellCreaDate = arrayLine.addArrayCellText(creaDate);
					cellCreaDate.setCompareOn(searchResult.getCreaDate());
				} catch (Exception e) {
				  	ArrayCellText cellCreaDate = arrayLine.addArrayCellText("");
				}
				
				String creaName = searchResult.getCreaName();
				arrayLine.addArrayCellText(creaName);
			}
			
			List<String> listPath = searchResult.getPath();
			String path = "";
			boolean first = true;
			for(String item : listPath) {
				if(!first) {
					path += "<br/>";
				}
				path += item;
				first = false;
			}
			arrayLine.addArrayCellText(path);
			
		}
		out.print(arrayPane.print());
	}
}

out.println(frame.printAfter());
out.println(window.printAfter());
%>

</body>
</html>