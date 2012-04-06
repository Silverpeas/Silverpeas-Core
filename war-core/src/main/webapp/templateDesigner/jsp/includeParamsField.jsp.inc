<%@ include file="check.jsp" %>
<%
FieldTemplate 	field 		= (FieldTemplate) request.getAttribute("Field");
String			displayer 	= (String) request.getAttribute("Displayer");
List<String>	languages	= (List<String>) request.getAttribute("Languages");

String name = "";
String mandatoryChecked = "";
String searchable = "";
String usedAsFacet = "";
boolean nameDisabled = false;
String actionForm = "AddField";
Map<String, String> parameters = null;
boolean showFacetParam = "listbox".equals(displayer) || "radio".equals(displayer) || "checkbox".equals(displayer);

if (field != null) {
	name 			= field.getFieldName();
	actionForm 		= "UpdateField";
	
	String actionFormFromPdcUtilization = (String)request.getAttribute("actionForm");
	if (actionFormFromPdcUtilization != null) {
		actionForm = actionFormFromPdcUtilization;
	}
	
	if (actionForm.equals("UpdateField")) {
		nameDisabled = true;
	}
	
	parameters = field.getParameters("fr");
		
	if (field.isMandatory()) {
		mandatoryChecked = "checked=\"checked\"";
	}
		
	if (field.isSearchable()) {
		searchable = "checked=\"checked\"";
	}
	
	if (field.isUsedAsFacet()) {
		usedAsFacet = "checked=\"checked\"";
	}
}
%>
<html>
<head>
<title><%=resource.getString("GML.popupTitle")%></title>
<%
	out.println(gef.getLookStyleSheet());
%>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
<script type="text/javascript">
	
	var errorMsg = "";
    var errorNb = 0;
	
	function sendData() {
		if (isCorrectForm()) {
			<% if (field != null && !field.isUsedAsFacet()) { %>
				if ($("#UsedAsFacet").is(':checked')) {
					alert("<%=resource.getString("templateDesigner.facet.warning")%>");
				}
			<% } %>
			document.fieldForm.submit();
		} else {
			errorMsg = "";
    		errorNb = 0;
		}
	}
	
	function checkFieldName() {
		<% if (!nameDisabled) { %>
			var fieldName = stripInitialWhitespace(document.fieldForm.FieldName.value);
	     	if (isWhitespace(fieldName)) {
	           	errorMsg+="  - '<%=resource.getString("GML.name")%>' <%=resource.getString("GML.MustBeFilled")%>\n";
	           	errorNb++;
	     	} else {
	     		var regexp = /^[a-zA-Z0-9]*$/;
	      		if(!regexp.test(fieldName)) {
		        	errorMsg+="  - '<%=resource.getString("GML.name")%>' <%=resource.getString("GML.MustContainsLettersAndDigitsOnly")%>\n";
	           		errorNb++;
		      	} else {
		      		if (fieldName.toLowerCase() == "name") {
		      			errorMsg+="  - '<%=resource.getString("GML.name")%>' <%=resource.getString("templateDesigner.js.incorrectName")%>\n";
	           			errorNb++;
		      		}
		      	}
	     	}
     	<% } %>
	}
	
	function checkErrors() {
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
</script>