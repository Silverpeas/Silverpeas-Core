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
	String size = "";
	String maxLength = "";
	
	if (field != null)
	{
		if (parameters.containsKey("maxLength")) {
			maxLength = (String) parameters.get("maxLength");
		}
		
		if (parameters.containsKey("size")) {
			size = (String) parameters.get("size");
		}
	}
%>
<%@ include file="includeTopField.jsp.inc" %>
<tr>
<td class="txtlibform"><%=resource.getString("templateDesigner.size")%> :</td><td><input type="text" name="Param_size" value="<%=size%>" size="5" maxLength="3"/></td>
</tr>
<tr>
<td class="txtlibform"><%=resource.getString("templateDesigner.maxLength")%> :</td><td><input type="text" name="Param_maxLength" value="<%=maxLength%>" size="5" maxLength="3"/></td>
</tr>
<%@ include file="includeBottomField.jsp.inc" %>