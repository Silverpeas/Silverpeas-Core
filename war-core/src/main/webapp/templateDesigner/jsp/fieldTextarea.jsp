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
	String rows = "";
	String cols = "";
	
	if (field != null)
	{
		if (parameters.containsKey("rows")) {
			rows = (String) parameters.get("rows");
		}
		
		if (parameters.containsKey("cols")) {
			cols = (String) parameters.get("cols");
		}
	}
%>
<%@ include file="includeTopField.jsp.inc" %>
<tr>
<td class="txtlibform"><%=resource.getString("templateDesigner.rows")%> :</td><td><input type="text" name="Param_rows" value="<%=rows%>" size="5" maxLength="3"/></td>
</tr>
<tr>
<td class="txtlibform"><%=resource.getString("templateDesigner.cols")%> :</td><td><input type="text" name="Param_cols" value="<%=cols%>" size="5" maxLength="3"/></td>
</tr>
<%@ include file="includeBottomField.jsp.inc" %>