<%@ include file="check.jsp" %>
<%
FieldTemplate 	field 		= (FieldTemplate) request.getAttribute("Field");
String			displayer 	= (String) request.getAttribute("Displayer");
Iterator		languages	= ((List) request.getAttribute("Languages")).iterator();

String name = "";
String mandatoryChecked = "";
String searchable = "";
String nameDisabled = "";
String actionForm = "AddField";
Map parameters = null;

if (field != null)
{
	name 			= field.getFieldName();
	actionForm 		= "UpdateField";
	
	String actionFormFromPdcUtilization = (String)request.getAttribute("actionForm");
	if (actionFormFromPdcUtilization != null) {
		actionForm = actionFormFromPdcUtilization;
	}
	
	if (actionForm.equals("UpdateField")) {
		nameDisabled 	= "readOnly='readonly'";
	}
	
	parameters = field.getParameters("fr");
		
	if (field.isMandatory())
		mandatoryChecked = "checked";
		
	if (field.isSearchable())
		searchable = "checked";
}
%>
<html>
<head>
<TITLE><%=resource.getString("GML.popupTitle")%></TITLE>
<%
	out.println(gef.getLookStyleSheet());
%>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
<script language="javascript">
	
	var errorMsg = "";
    var errorNb = 0;
	
	function sendData() 
	{
		if (isCorrectForm()) 
		{
			document.fieldForm.submit();
			window.opener.location.href = "<%=m_context%>/RtemplateDesigner/jsp/ViewFields?Scope=0";
			window.close();
		}
		else
		{
			errorMsg = "";
    		errorNb = 0;
		}
	}
	
	function checkFieldName()
	{
		var fieldName = stripInitialWhitespace(document.fieldForm.FieldName.value);
     	if (isWhitespace(fieldName))
     	{
           	errorMsg+="  - '<%=resource.getString("GML.name")%>' <%=resource.getString("GML.MustBeFilled")%>\n";
           	errorNb++;
     	}
	}
	
	function checkErrors()
	{
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
</script>