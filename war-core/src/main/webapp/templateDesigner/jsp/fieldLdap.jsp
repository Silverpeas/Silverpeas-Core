<%@ include file="includeParamsField.jsp.inc" %>
<script language="javascript">
	function isCorrectForm() 
	{
     	checkFieldName();
     	return checkErrors();
	}
</script>
</head>
<body>
<%
	String host = "";
	String port = "";
	String version = "";
	String baseDN = "";
	String password = "";
	String searchBase = "";
	String searchScope = "";
	String searchFilter = "";
	String searchAttribute = "";
	String searchTypeOnly = "";
	String maxResultDisplayed = "";
	String valueFieldType = "";
	
	if (field != null)
	{
		if (parameters.containsKey("host")) {
			host = (String) parameters.get("host");
		}
		
		if (parameters.containsKey("port")) {
			port = (String) parameters.get("port");
		}

		if (parameters.containsKey("version")) {
			version = (String) parameters.get("version");
		}

		if (parameters.containsKey("baseDN")) {
			baseDN = (String) parameters.get("baseDN");
		}

		if (parameters.containsKey("password")) {
			password = (String) parameters.get("password");
		}

		if (parameters.containsKey("searchBase")) {
			searchBase = (String) parameters.get("searchBase");
		}

		if (parameters.containsKey("searchScope")) {
			searchScope = (String) parameters.get("searchScope");
		}

		if (parameters.containsKey("searchFilter")) {
			searchFilter = (String) parameters.get("searchFilter");
		}

		if (parameters.containsKey("searchAttribute")) {
			searchAttribute = (String) parameters.get("searchAttribute");
		}

		if (parameters.containsKey("searchTypeOnly")) {
			searchTypeOnly = (String) parameters.get("searchTypeOnly");
		}

		if (parameters.containsKey("maxResultDisplayed")) {
			maxResultDisplayed = (String) parameters.get("maxResultDisplayed");
		}

		if (parameters.containsKey("valueFieldType")) {
			valueFieldType = (String) parameters.get("valueFieldType");
		}
	}
%>
<%@ include file="includeTopField.jsp.inc" %>
<tr>
	<td class="txtlibform"><%=resource.getString("templateDesigner.host")%> :</td><td><input type="text" name="Param_host" value="<%=host%>" size="30"/></td>
</tr>
<tr>
	<td class="txtlibform"><%=resource.getString("templateDesigner.port")%> :</td><td><input type="text" name="Param_port" value="<%=port%>" size="30"/></td>
</tr>
<tr>
	<td class="txtlibform"><%=resource.getString("templateDesigner.version")%> :</td><td><input type="text" name="Param_version" value="<%=version%>" size="30"/></td>
</tr>
<tr>
	<td class="txtlibform"><%=resource.getString("templateDesigner.baseDN")%> :</td><td><input type="text" name="Param_baseDN" value="<%=baseDN%>" size="30"/></td>
</tr>
<tr>
	<td class="txtlibform"><%=resource.getString("templateDesigner.password")%> :</td><td><input type="text" name="Param_password" value="<%=password%>" size="30"/></td>
</tr>
<tr>
	<td class="txtlibform"><%=resource.getString("templateDesigner.searchBase")%> :</td><td><input type="text" name="Param_searchBase" value="<%=searchBase%>" size="30"/></td>
</tr>
<tr>
	<td class="txtlibform"><%=resource.getString("templateDesigner.searchScope")%> :</td><td><input type="text" name="Param_searchScope" value="<%=searchScope%>" size="30"/></td>
</tr>
<tr>
	<td class="txtlibform"><%=resource.getString("templateDesigner.searchFilter")%> :</td><td><input type="text" name="Param_searchFilter" value="<%=searchFilter%>" size="30"/></td>
</tr>
<tr>
	<td class="txtlibform"><%=resource.getString("templateDesigner.searchAttribute")%> :</td><td><input type="text" name="Param_searchAttribute" value="<%=searchAttribute%>" size="30"/></td>
</tr>
<tr>
	<td class="txtlibform"><%=resource.getString("templateDesigner.searchTypeOnly")%> :</td><td><input type="text" name="Param_searchTypeOnly" value="<%=searchTypeOnly%>" size="30"/></td>
</tr>
<tr>
	<td class="txtlibform"><%=resource.getString("templateDesigner.maxResultDisplayed")%> :</td><td><input type="text" name="Param_maxResultDisplayed" value="<%=maxResultDisplayed%>" size="30"/></td>
</tr>
<tr>
	<td class="txtlibform"><%=resource.getString("templateDesigner.valueFieldType")%> :</td><td><input type="text" name="Param_valueFieldType" value="<%=valueFieldType%>" size="30"/></td>
</tr>
<%@ include file="includeBottomField.jsp.inc" %>