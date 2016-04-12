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
			label = field.getLabel(lang);
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
<tr>
<td class="txtlibform"><%=resource.getString("templateDesigner.searchable")%> :</td><td><input type="checkbox" name="Searchable" value="true" <%=searchable%>/></td>
</tr>
<% if (showFacetParam) { %>
<tr>
<td class="txtlibform"><%=resource.getString("templateDesigner.facet")%> :</td><td><input type="checkbox" id="UsedAsFacet" name="UsedAsFacet" value="true" <%=usedAsFacet%>/></td>
</tr>
<% } %>