<%@ include file="checkThesaurus.jsp"%>
<%
	Vocabulary voca = (Vocabulary) request.getAttribute("Vocabulary");
	String nom = "";
	String desc = "";
	if (voca != null)
	{
		nom = Encode.javaStringToHtmlString(voca.getName());
		desc = Encode.javaStringToHtmlString(voca.getDescription());
	}
%>
<HTML>
<HEAD>
<TITLE><%=resource.getString("GML.popupTitle")%></TITLE>
<%
out.println(gef.getLookStyleSheet());
%>
<SCRIPT LANGUAGE="JavaScript">
<!--
function isCorrectForm() {
     	var errorMsg = "";
     	var errorNb = 0;
	var nom = document.forms[0].nom.value;
	var desc = document.forms[0].description;
	if (isWhitespace(nom)) {
           errorMsg+="  - '<%=resource.getString("thesaurus.vocabulaire")%>' <%=resource.getString("thesaurus.mustContainsText")%>\n";
           errorNb++; 
        }              
	if (isWhitespace(desc.value)) {
           errorMsg+="  - '<%=resource.getString("GML.description")%>' <%=resource.getString("thesaurus.mustContainsText")%>\n";
           errorNb++; 
        }              
     	if (!isValidTextArea(desc)) {
     		errorMsg+="  - '<%=resource.getString("GML.description")%>' <%=resource.getString("thesaurus.containsTooLargeText")+resource.getString("thesaurus.nbMaxTextArea")+resource.getString("thesaurus.characters")%>\n";
           	errorNb++; 
		}  	  			
     switch(errorNb)
     {
        case 0 :
            result = true;
            break;
        case 1 :
            errorMsg = "<%=resource.getString("thesaurus.thisFormContains")%> 1 <%=resource.getString("GML.error")%> : \n" + errorMsg;
            window.alert(errorMsg);
            result = false;
            break;
        default :
            errorMsg = "<%=resource.getString("thesaurus.thisFormContains")%> " + errorNb + " <%=resource.getString("GML.errors")%> :\n" + errorMsg;
            window.alert(errorMsg);
            result = false;
            break;
     }
     return result;	
}
function save()
{
	if (isCorrectForm())
		document.forms[0].submit();
}
//-->
</SCRIPT>
</HEAD>
<BODY marginheight="5" marginwidth="5" leftmargin="5" topmargin="5" bgcolor="#FFFFFF" onLoad="document.forms[0].nom.focus();">
<%
	browseBar.setComponentName(componentLabel, "Main");
	browseBar.setPath("<a href=\"Back\">"+resource.getString("thesaurus.thesaurus")+ "</a> > " + resource.getString("thesaurus.BBcreateVoc"));

	out.println(window.printBefore());
 
    
	out.println(frame.printBefore());
%>

<% // Ici debute le code de la page %>
<center>
<FORM METHOD=POST ACTION="CreateVoca">
<table width="98%" border="0" cellspacing="0" cellpadding="0" class=intfdcolor4><!--tablcontour-->
	<tr> 
		<td nowrap>
			<table border="0" cellspacing="0" cellpadding="5" class="contourintfdcolor" width="100%"><!--tabl1-->
				<tr align=center> 
					<td  class="intfdcolor4" valign="baseline" align=left>
						<span class="txtlibform"><%=resource.getString("thesaurus.vocabulaire")%> :</span>
					</td>
					<td  class="intfdcolor4" valign="baseline" align=left>
				<input type="text" name="nom" size="50" maxlength="50" value="<%=nom%>">&nbsp;<img src="<%=resource.getIcon("thesaurus.mandatory")%>" width="5" height="5">
					</td>
				</tr>
				<tr align=center> 

					<td  class="intfdcolor4" valign="top" align=left>
						<span class="txtlibform"><%=resource.getString("GML.description")%> :</span>
					</td>
					<td  class="intfdcolor4" valign="top" align=left>
					<textarea cols="49" rows="4" name="description"><%=desc%></textarea>&nbsp;<img src="<%=resource.getIcon("thesaurus.mandatory")%>" width="5" height="5">
					</td>
				</tr>
				<tr align=center>				 
					<td class="intfdcolor4" valign="baseline" align=left colspan=2><span class="txt">(<img src="<%=resource.getIcon("thesaurus.mandatory")%>" width="5" height="5"> : <%=resource.getString("GML.requiredField")%>)</span> 
					</td>
				</tr>
			</table>
		</td>
	</tr>
</table>
</FORM>
<%
    ButtonPane buttonPane = gef.getButtonPane();
    buttonPane.addButton((Button) gef.getFormButton(resource.getString("GML.validate"), "javascript:save();", false));
    buttonPane.addButton((Button) gef.getFormButton(resource.getString("GML.cancel"), "Back", false));
    out.println(buttonPane.print());
%>

</center>
<% // Ici se termine le code de la page %>

<%
out.println(frame.printAfter());
out.println(window.printAfter());
%>

</BODY>
</HTML>