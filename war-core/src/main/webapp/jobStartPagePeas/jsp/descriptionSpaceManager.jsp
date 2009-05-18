<%@ include file="check.jsp" %>

<%
String m_SpaceName = (String) request.getAttribute("currentSpaceName");
String m_SpaceManagerName = (String) request.getAttribute("Name");
String m_SpaceManagerDescription = (String) request.getAttribute("Description");
String m_SubSpace = (String) request.getAttribute("nameSubSpace");
			
			browseBar.setDomainName(resource.getString("JSPP.manageHomePage"));
			if (m_SubSpace == null) //je suis sur un espace
 				browseBar.setComponentName(m_SpaceName);
 			else {
 				browseBar.setComponentName(m_SpaceName + " > " + m_SubSpace);
 			}
			browseBar.setPath(resource.getString("JSPP.roleDescription"));

%>

<HTML>
<HEAD>
<TITLE><%=resource.getString("GML.popupTitle")%></TITLE>
<%
out.println(gef.getLookStyleSheet());
%>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>

<script language="JavaScript">

function B_ANNULER_ONCLICK() {
	window.close();
}

/*****************************************************************************/
function B_VALIDER_ONCLICK() {
	if (isCorrectForm()) {
		document.infoSpaceManager.submit();
	}
}

/*****************************************************************************/

function isCorrectForm() {		
     	var errorMsg = "";
     	var errorNb = 0;
     	
     	var name = stripInitialWhitespace(document.infoSpaceManager.NameObject.value);
		var desc = document.infoSpaceManager.Description;
		
		if (isWhitespace(name)) {
			errorMsg+="  - '<%=resource.getString("GML.name")%>' <%=resource.getString("MustContainsText")%>\n";
			errorNb++; 
		}
	
     	var textAreaLength = 400;
		var s = desc.value;
		if (! (s.length <= textAreaLength)) {
     		errorMsg+="  - '<%=resource.getString("GML.description")%>' <%=resource.getString("ContainsTooLargeText")+"400 "+resource.getString("Characters")%>\n";
           	errorNb++; 
		}  	  	
		  	
		
     switch(errorNb)
     {
        case 0 :
            result = true;
            break;
        case 1 :
            errorMsg = "<%=resource.getString("ThisFormContains")%> 1 <%=resource.getString("GML.error")%> : \n" + errorMsg;
            window.alert(errorMsg);
            result = false;
            break;
        default :
            errorMsg = "<%=resource.getString("ThisFormContains")%> " + errorNb + " <%=resource.getString("GML.errors")%> :\n" + errorMsg;
            window.alert(errorMsg);
            result = false;
            break;
     }
     return result;	
     
}


</script>
</HEAD>


<BODY marginheight=5 marginwidth=5 leftmargin=5 topmargin=5 onLoad="document.infoSpaceManager.NameObject.focus();">
<FORM NAME="infoSpaceManager" action = "EffectiveUpdateSpaceManagerDescription" METHOD="POST">

<%
out.println(window.printBefore());
out.println(frame.printBefore());
%>
<center>
<table width="98%" border="0" cellspacing="0" cellpadding="0" class=intfdcolor4><!--tablcontour-->
	<tr> 
		<td nowrap>
			<table border="0" cellspacing="0" cellpadding="5" class="contourintfdcolor" width="100%"><!--tabl1-->
				<tr align=center> 
					<td  class="intfdcolor4" valign="baseline" align=left>
						<span class="txtlibform"><%=resource.getString("GML.name")%> :</span>
					</td>
					<td  class="intfdcolor4" valign="baseline" align=left>
					<input type="text" name="NameObject" size="60" maxlength="60" value="<%=Encode.javaStringToHtmlString(m_SpaceManagerName)%>">
					&nbsp;<img src="<%=resource.getIcon("mandatoryField")%>" width="5" height="5" border="0">
					</td>
				</tr>
				
				<tr align=center> 
					<td  class="intfdcolor4" valign="top" align=left>
						<span class="txtlibform"><%=resource.getString("GML.description")%> :</span>
					</td>
					<td  class="intfdcolor4" valign="top" align=left>
						<textarea name="Description" rows="4" cols="49"><%=Encode.javaStringToHtmlString(m_SpaceManagerDescription)%></textarea>
					</td>
				</tr>
				
				<tr align=left> 
					<td colspan="2">
					(<img border="0" src="<%=resource.getIcon("mandatoryField")%>" width="5" height="5"> 
              : <%=resource.getString("GML.requiredField")%>)
    				</td>
				</tr>				
				
			</table>
		</td>
	</tr>

</table>


<br>
<%
		  ButtonPane buttonPane = gef.getButtonPane();
		  buttonPane.addButton((Button) gef.getFormButton(resource.getString("GML.validate"), "javascript:onClick=B_VALIDER_ONCLICK();", false));
		  buttonPane.addButton((Button) gef.getFormButton(resource.getString("GML.cancel"), "javascript:onClick=B_ANNULER_ONCLICK();", false));
		  out.println(buttonPane.print());
%>
</center>

<%

			out.println(frame.printAfter());
      out.println(window.printAfter());
%>
</FORM>

</BODY>
</HTML>