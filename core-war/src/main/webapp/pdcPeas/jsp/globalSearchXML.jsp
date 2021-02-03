<%--

    Copyright (C) 2000 - 2021 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "https://www.silverpeas.org/legal/floss_exception.html"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ page import="org.silverpeas.core.admin.component.model.SilverpeasComponentInstance"%>
<%@ page import="org.silverpeas.core.contribution.content.form.DataRecord"%>
<%@ page import="org.silverpeas.core.contribution.content.form.Form"%>
<%@ page import="org.silverpeas.core.contribution.content.form.PagesContext"%>
<%@ page import="org.silverpeas.core.contribution.template.publication.PublicationTemplate"%>
<%@ page import="org.silverpeas.core.util.StringUtil" %>

<%@ include file="checkAdvancedSearch.jsp"%>
<%
String sortOrder = request.getParameter("sortOrder");
String sortImp = request.getParameter("sortImp");
String SortResXForm = request.getParameter("SortResXForm");

boolean				expertSearchVisible  = (Boolean) request.getAttribute("ExpertSearchVisible");
List<PublicationTemplate> 	xmlForms 	= (List) request.getAttribute("XMLForms");
PublicationTemplate template 	= (PublicationTemplate) request.getAttribute("Template");
DataRecord			emptyData	= (DataRecord) request.getAttribute("Data");
PagesContext		context		= (PagesContext) request.getAttribute("context");

Form form = null;
String selectedTemplate = null;
if (template != null) {
	selectedTemplate 	= template.getFileName();
	form 				= template.getSearchForm();
}

List<SilverpeasComponentInstance>	allComponents		= (List) request.getAttribute("ComponentList");
List<SpaceInstLight>	allSpaces	= (List) request.getAttribute("SpaceList");
QueryParameters query	= (QueryParameters) request.getAttribute("QueryParameters");
String			spaceSelected		= null;
String			componentSelected	= null;
String			title				= "";
if (query != null) {
	spaceSelected		= query.getSpaceId();
	componentSelected	= query.getInstanceId();
	title				= query.getXmlTitle();
	if (title == null || "null".equals(title)) {
    title = "";
  }
}

String pageId = (String) request.getAttribute("PageId");
if (!StringUtil.isDefined(pageId)) {
  pageId = "globalSearchXML";
}
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<view:looknfeel/>
<view:includePlugin name="wysiwyg"/>
<script type="text/javascript">
function sendXMLRequest() {
	if(document.XMLSearchForm != null) {
		$.progressMessage();
		$("#TitleNotInXMLForm").val($("#plainText").val());
		document.XMLSearchForm.submit();
	} else {
    jQuery.popup.error("<%=resource.getString("pdcPeas.choiceForm")%>");
	}
}
function chooseTemplate() {
	var valuePath = document.XMLRestrictForm.xmlSearchSelectedForm.value;
	if (valuePath.length > 0) {
		$.progressMessage();
    document.XMLRestrictForm.action = "XMLSearchViewTemplate";
		document.XMLRestrictForm.submit();
	}
}
function viewXmlSearch(){
	$.progressMessage();
	document.XMLRestrictForm.submit();
}
</script>
</head>
<body class="yui-skin-sam" id="<%=pageId %>">
<%
	browseBar.setComponentName(resource.getString("pdcPeas.SearchPage"));

	Board board = gef.getBoard();
	ButtonPane buttonPane = gef.getButtonPane();

	out.println(window.printBefore());

	tabs = gef.getTabbedPane();
	tabs.addTab(resource.getString("pdcPeas.SearchResult"), "LastResults", false);
	if (expertSearchVisible) {
		tabs.addTab(resource.getString("pdcPeas.SearchSimple"), "ChangeSearchTypeToAdvanced", false);
		tabs.addTab(resource.getString("pdcPeas.SearchAdvanced"), "ChangeSearchTypeToExpert", false);
	} else {
		tabs.addTab(resource.getString("pdcPeas.SearchPage"), "ChangeSearchTypeToAdvanced", false);
	}
	tabs.addTab(resource.getString("pdcPeas.SearchXml"), "#", true);

	out.println("<div id=\"tabs\">" + tabs.print() + "</div>");
	out.println(frame.printBefore());

	%>
<div id="scope">
<view:board>
    <form name="XMLRestrictForm" action="XMLRestrictSearch" method="post">
      <table border="0" cellspacing="0" cellpadding="5" width="100%">
        <tr>
          <td class="txtlibform" width="200"><%=resource.getString("pdcPeas.Template")%></td>
          <td>
            <select name="xmlSearchSelectedForm" size="1" onchange="chooseTemplate();return;">
              <option value=""><%=resource.getString("GML.select")%></option>
              <%
                String selected = "";
                for (PublicationTemplate oneTemplate : xmlForms) {
                  selected	= "";
                  if (oneTemplate.getFileName().equals(selectedTemplate)) {
                    selected = " selected";
                  }

                  out.println("<option value=\""+oneTemplate.getFileName()+"\""+selected+">"+oneTemplate.getName()+"</option>");
                }
              %>
            </select>
          </td>
        </tr>
        <tr id="spaceList">
          <td class="txtlibform" width="200"><%=resource.getString("pdcPeas.DomainSelect")%></td>
          <td><select name="spaces" size="1" onchange="javascript:viewXmlSearch()">
            <%
				out.println("<option value=\"\">"+resource.getString("pdcPeas.AllAuthors")+"</option>");
				String			incr	= "";
				for (SpaceInstLight 	space : allSpaces) {
						selected	= "";
						incr		= "";
						if (space.getLevel() == 1) {
              incr = "&nbsp;&nbsp;";
            }

						if (space.getId().equals(spaceSelected)) {
              selected = " selected";
            }

						out.println("<option value=\""+space.getId()+"\""+selected+">"+incr+WebEncodeHelper.javaStringToHtmlString(space.getName(language))+"</option>");
				}
             %>
             </select></td>
	    </tr>
    <% if (allComponents != null) {%>
		<tr>
			<td class="txtlibform" width="200"><%=resource.getString("pdcPeas.ComponentSelect")%></td>
			<td>
			<select name="componentSearch" size="1" onchange="javascript:viewXmlSearch()">
			<option value=""><%=resource.getString("pdcPeas.AllAuthors")%></option>
			<%
				for(SilverpeasComponentInstance component : allComponents) {
						selected	= "";
						if (component.getId().equals(componentSelected)){
							selected = " selected";
						}
						out.println("<option value=\""+component.getId()+"\""+selected+">"+WebEncodeHelper.javaStringToHtmlString(component.getLabel(language))+"</option>");
				}
			%>
			</select>
			</td>
		</tr>
    <% } %>
        <tr>
          <td width="200" class="txtlibform"><%=resource.getString("GML.search")%></td>
          <td><input type="text" id="plainText" size="50" value="<%=title%>"/></td>
        </tr>

		<input type="hidden" name="SearchPageId" value="<%=pageId %>"/>
		<input type="hidden" name="sortOrder" value="<%=sortOrder %>"/>
		<input type="hidden" name="sortImp" value="<%=sortImp %>"/>
		<input type="hidden" name="SortResXForm" value="<%=SortResXForm %>"/>
		    </table>
    </form>
</view:board>
</div>

	<% if (form != null) { %>
		<div id="template">
      <form name="XMLSearchForm" method="post" action="XMLSearch" enctype="multipart/form-data">
		  <input type="hidden" id="TitleNotInXMLForm" name="TitleNotInXMLForm" value="<%=title%>"/>
      <%
  		  form.display(out, context, emptyData);
 	    %>
		  </form>
      </div>
		  <br/>
	<% } %>
<%
	buttonPane.addButton(gef.getFormButton(resource.getString("pdcPeas.search"), "javascript:sendXMLRequest();", false));
	out.println(buttonPane.print());
	out.println(frame.printAfter());
	out.println(window.printAfter());
%>
<view:progressMessage/>
</body>
</html>