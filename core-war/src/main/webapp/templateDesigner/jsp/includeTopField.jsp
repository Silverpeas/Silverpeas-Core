<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.Encode" %>
<%@ page import="org.silverpeas.core.util.WebEncodeHelper" %><%--

    Copyright (C) 2000 - 2024 Silverpeas

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
    along with this program.  If not, see <https://www.gnu.org/licenses/>.

--%>
<% if (shownInNewWindow) {
	browseBar.setDomainName(resource.getString("templateDesigner.toolName"));
	browseBar.setComponentName(resource.getString("templateDesigner.field"));

	out.println(window.printBefore());
	out.println(frame.printBefore());
	out.println(board.printBefore());
} %>
<form name="fieldForm" action="<%=m_context%>/RtemplateDesigner/jsp/<%=actionForm%>" method="post">
<table cellspacing="5" width="100%">
<tr>
<td class="txtlibform" width="10" nowrap="nowrap"><%=resource.getString("GML.name")%> :</td>
<td>
	<% if (!nameDisabled) { %>
		<input type="text" name="FieldName" size="30" value="<%=name%>"/>&nbsp;<img src="<%=resource.getIcon("templateDesigner.mandatory")%>" width="5" height="5"/>
	<% } else { %>
		<%=name%><input type="hidden" name="FieldName" value="<%=name%>"/>
	<% } %>
	<input type="hidden" name="Displayer" value="<%=displayer%>"/>
</td>
</tr>
<%
	for (String lang : languages) {
		String label = "";
		if (field != null) {
			label = WebEncodeHelper.javaStringToHtmlString(field.getLabel(lang));
		}
%>
		<tr>
			<td class="txtlibform" nowrap="nowrap"><%=resource.getString("GML.label")%> (<%=lang%>) :</td><td><input type="text" name="Label_<%=lang%>" size="30" value="<%=label%>"/></td>
		</tr>
<% } %>
<tr>
<td class="txtlibform"><%=resource.getString("GML.requiredField")%> :</td><td><input type="checkbox" name="Mandatory" value="true" <%=mandatoryChecked%>/></td>
</tr>
<% if (showMultiValuesParam) { %>
<tr>
<td class="txtlibform"><%=resource.getString("templateDesigner.field.multivalued")%> :</td><td><input type="checkbox" id="multivaluableChk" <%=multivaluableChecked%>/> <span id="multivaluableNb"><input type="text" name="NbMaxValues" value="<%=nbMaxValues%>" size="3"/> <%=resource.getString("templateDesigner.field.multivalued.max")%></span></td>
</tr>
<% } %>
<% if (showSearchParam) { %>
<tr>
<td class="txtlibform"><%=resource.getString("templateDesigner.searchable")%> :</td><td><input type="checkbox" name="Searchable" value="true" <%=searchable%>/></td>
</tr>
<% } %>
<% if (showFacetParam) { %>
<tr>
<td class="txtlibform"><%=resource.getString("templateDesigner.facet")%> :</td><td><input type="checkbox" id="UsedAsFacet" name="UsedAsFacet" value="true" <%=usedAsFacet%>/></td>
</tr>
<% } %>
