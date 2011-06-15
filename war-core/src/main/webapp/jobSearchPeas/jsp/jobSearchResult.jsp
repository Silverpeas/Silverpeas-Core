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
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<view:looknfeel />
<title><%=resource.getString("GML.popupTitle")%></title>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
<script language="javascript">
function isCorrectForm()
{
	 var errorMsg = "";
     var errorNb = 0;
     var idOrName = stripInitialWhitespace(document.forms["SearchResultForm"].elements["SearchField"].value);

     if (isWhitespace(idOrName)) {
       errorMsg+="  - <%=resource.getString("GML.theField")%> '<%=resource.getString("JSP.searchField")%>' <%=resource.getString("GML.MustBeFilled")%>\n";
       errorNb++;
     }

     switch(errorNb)
     {
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
function validateSearch()
{
	if (isCorrectForm()) {
		document.forms["SearchResultForm"].submit();
	}
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

<center>
<table align="center" border="0" cellspacing="0" cellpadding="5" width="100%" class="intfdcolor4">
<FORM NAME="SearchResultForm" ACTION="SearchResult" METHOD="POST">
<tr>
	<td class="intfdcolor4"><span class="txtlibform"><fmt:message key="JSP.searchField"/>
    : </span></td>
    <td class="intfdcolor4"><input type="text" name="SearchField" size="60" maxlength="100" value="<%if(idOrName != null) { out.print(idOrName); }%>">
	<img border="0" src="<%=m_context+"/util/icons/mandatoryField.gif"%>" width="5" height="5"></td>
</tr>
<tr>
	<td valign="top">
	<input type="radio" name="Category" value="space" <%if("space".equals(category)) { out.print("checked"); }%>><fmt:message key="JSP.space"/>	
	<BR>
	<input type="radio" name="Category" value="service" <%if("service".equals(category)) { out.print("checked"); }%>><fmt:message key="JSP.service"/>
	<BR>
	<input type="radio" name="Category" value="publication" <%if("publication".equals(category)) { out.print("checked"); }%>><fmt:message key="JSP.publication"/>
	<BR>
	<input type="radio" name="Category" value="group" <%if("group".equals(category)) { out.print("checked"); }%>><fmt:message key="JSP.group"/>
	<BR>
	<input type="radio" name="Category" value="user" <%if("user".equals(category)) { out.print("checked"); }%>><fmt:message key="JSP.user"/>
	</td>
    <td valign="top">&nbsp;</td>
</tr>
</FORM>
</table>            
</center><br/>

<%
out.println(board.printAfter());
%>

<center>
<fmt:message key="JSP.search" var="search"/>
  <view:buttonPane>
	<view:button action="javascript:validateSearch();" label="${search}" disabled="false" />
  </view:buttonPane>
</center>
<br>

<%
if(listResult != null) {
	if(listResult.size() == 0) {
%>
	
<table border="0" cellspacing="0" cellpadding="0" width="100%">
<tr valign="middle" class="intfdcolor">
	<td align="center"><%=resource.getString("JSP.noResult")%></td>
</tr>
</table>
<%
	} else {
		ArrayPane arrayPane = gef.getArrayPane("searchResultList", "SearchResult", request, session);
		arrayPane.setVisibleLineNumber(20);
		arrayPane.setTitle(resource.getString("JSP.listResult"));
		arrayPane.addArrayColumn(resource.getString("JSP.name"));
		arrayPane.addArrayColumn(resource.getString("JSP.desc"));
		arrayPane.addArrayColumn(resource.getString("JSP.creaDate"));
		arrayPane.addArrayColumn(resource.getString("JSP.creaName"));
		arrayPane.addArrayColumn(resource.getString("JSP.path"));
    
		SearchResult searchResult = null;
		String name = null;
		String desc = null;
		String creaDate = null;
		String creaName = null;
		String path = null;
		String url = null;
		for(int nI=0; nI < listResult.size(); nI++) {
			searchResult	= (SearchResult) listResult.get(nI);
			
			name			= EncodeHelper.javaStringToHtmlString(searchResult.getName());
			url				= searchResult.getUrl();
			ArrayLine arrayLine = arrayPane.addArrayLine();
			arrayLine.addArrayCellLink(name, "javascript:onClick=linkElement('')");
			
			desc			= searchResult.getDesc();
			if (desc != null && desc.length() > 200) {
				desc = desc.substring(0, 200)+"...";
			}
			arrayLine.addArrayCellText(desc);
			
			try	{
				creaDate = resource.getOutputDate(searchResult.getCreaDate());
				ArrayCellText cellCreaDate = arrayLine.addArrayCellText(creaDate);
				cellCreaDate.setCompareOn(searchResult.getCreaDate());
			} catch (Exception e) {
				creaDate	= null;
			}
			
			creaName		= searchResult.getCreaName();
			arrayLine.addArrayCellText(creaName);
			
			path			= searchResult.getPath();
			arrayLine.addArrayCellText(path);
			
		}
		
		out.print(arrayPane.print());
            
%>

<%	
	}
}
%>
<%
out.println(frame.printAfter());
out.println(window.printAfter());
%>

</body>
</html>
