<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ include file="check.jsp" %>
<%
FieldTemplate 	field 		= (FieldTemplate) request.getAttribute("Field");
String			displayer 	= (String) request.getAttribute("Displayer");
List<String>	languages	= (List<String>) request.getAttribute("Languages");
Iterator<FieldTemplate> existingFields = (Iterator<FieldTemplate>) request.getAttribute("Fields");

String name = "";
String mandatoryChecked = "";
int nbMaxValues = 1;
String multivaluableChecked = "";
String searchable = "";
String usedAsFacet = "";
boolean nameDisabled = false;
String actionForm = "AddField";
Map<String, String> parameters = null;
boolean showFacetParam = "listbox".equals(displayer) || "radio".equals(displayer) || "checkbox".equals(displayer);
boolean showMultiValuesParam = "text".equals(displayer) || "textarea".equals(displayer) || "url".equals(displayer) || "file".equals(displayer) || "image".equals(displayer) || "video".equals(displayer);
boolean shownInNewWindow = "pdc".equals(displayer);
String checked = "checked=\"checked\"";

if (field != null) {
	name 			= field.getFieldName();
	if (field.isRepeatable()) {
		nbMaxValues = field.getMaximumNumberOfOccurrences();
		multivaluableChecked = checked;
	}
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
		mandatoryChecked = checked;
	}

	if (field.isSearchable()) {
		searchable = checked;
	}

	if (field.isUsedAsFacet()) {
		usedAsFacet = checked;
	}
}
%>

<% if (shownInNewWindow) { %>
<html>
<head>
<title><%=resource.getString("GML.popupTitle")%></title>
<view:looknfeel withCheckFormScript="true"/>
<% } else { %>
<view:includePlugin name="tkn"/>
<% } %>

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
		return false;
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

		<%
			out.print("var existingFieldNames = [");
			while (existingFields.hasNext()) {
				FieldTemplate existingField = existingFields.next();
				if (existingField != null) {
					out.print("'"+existingField.getFieldName()+"'");
					if (existingFields.hasNext()) {
						out.print(",");
					}
				}
			}
			out.println("];");
		%>
		if ($.inArray(fieldName, existingFieldNames) != -1) {
			errorMsg+="  - <%=resource.getString("templateDesigner.js.nameAlreadyExist")%>\n";
			errorNb++;
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

	function checkMultivaluedVisibility() {
	  if ($("#multivaluableChk").is(":checked")) {
		$("#multivaluableNb").show();
	  } else {
		$("#multivaluableNb").hide();
		$("#multivaluableNb input").val('1');
	  }
	}

	$(document).ready(
		function () {
			$( "#multivaluableChk" ).change(function() {
			  checkMultivaluedVisibility();
			});

			checkMultivaluedVisibility();
		}
	);
</script>