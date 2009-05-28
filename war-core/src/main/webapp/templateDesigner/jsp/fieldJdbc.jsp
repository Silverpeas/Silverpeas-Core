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
	String driverName = "";
	String url = "";
	String login = "";
	String password = "";
	String query = "";
	String valueFieldType = "";
	
	if (field != null)
	{
		if (parameters.containsKey("driverName")) {
			driverName = (String) parameters.get("driverName");
		}
		
		if (parameters.containsKey("url")) {
			url = (String) parameters.get("url");
		}

		if (parameters.containsKey("login")) {
			login = (String) parameters.get("login");
		}

		if (parameters.containsKey("password")) {
			password = (String) parameters.get("password");
		}

		if (parameters.containsKey("query")) {
			query = (String) parameters.get("query");
		}

		if (parameters.containsKey("valueFieldType")) {
			valueFieldType = (String) parameters.get("valueFieldType");
		}
	}
%>
<%@ include file="includeTopField.jsp.inc" %>
<tr>
	<td class="txtlibform"><%=resource.getString("templateDesigner.driverName")%> :</td><td><input type="text" name="Param_driverName" value="<%=driverName%>" size="30"/></td>
</tr>
<tr>
	<td class="txtlibform"><%=resource.getString("templateDesigner.url")%> :</td><td><input type="text" name="Param_url" value="<%=url%>" size="30"/></td>
</tr>
<tr>
	<td class="txtlibform"><%=resource.getString("templateDesigner.login")%> :</td><td><input type="text" name="Param_login" value="<%=login%>" size="30"/></td>
</tr>
<tr>
	<td class="txtlibform"><%=resource.getString("templateDesigner.passwordDB")%> :</td><td><input type="text" name="Param_password" value="<%=password%>" size="30"/></td>
</tr>
<tr>
	<td class="txtlibform"><%=resource.getString("templateDesigner.query")%> :</td><td><input type="text" name="Param_query" value="<%=query%>" size="30"/></td>
</tr>
<tr>
	<td class="txtlibform"><%=resource.getString("templateDesigner.valueFieldType")%> :</td><td><input type="text" name="Param_valueFieldType" value="<%=valueFieldType%>" size="30"/></td>
</tr>
<%@ include file="includeBottomField.jsp.inc" %>