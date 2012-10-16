<%--

    Copyright (C) 2000 - 2012 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://www.silverpeas.org/docs/core/legal/floss_exception.html"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>

<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ page import="com.stratelia.webactiv.beans.admin.ProfileInst"%>

<%@ include file="check.jsp" %>

<%
String m_ComponentName = (String) request.getAttribute("compoName");
ProfileInst m_Profile = (ProfileInst) request.getAttribute("Profile");
String spaceId = (String) request.getAttribute("CurrentSpaceId");

browseBar.setSpaceId(spaceId);
			 			
		 	String profile = m_Profile.getLabel();
			String labelProfile = resource.getString(profile.replace(' ', '_'));
			if (labelProfile == null || labelProfile.equals("")) 
				labelProfile = profile;
					
			browseBar.setExtraInformation(m_ComponentName +" > "+ resource.getString("JSPP.roleDescription"));
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
		document.infoProfileInstance.submit();
	}
}

/*****************************************************************************/

function isCorrectForm() {		
     	var errorMsg = "";
     	var errorNb = 0;
     	
     	var name = stripInitialWhitespace(document.infoProfileInstance.NameObject.value);
		var desc = document.infoProfileInstance.Description;
		
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


<BODY marginheight=5 marginwidth=5 leftmargin=5 topmargin=5 onLoad="document.infoProfileInstance.NameObject.focus();">
<FORM NAME="infoProfileInstance" action = "EffectiveUpdateProfileInstanceDescription" METHOD="POST">

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
					<input type="text" name="NameObject" size="60" maxlength="60" value="<%=Encode.javaStringToHtmlString(labelProfile)%>">
					&nbsp;<img src="<%=resource.getIcon("mandatoryField")%>" width="5" height="5" border="0">
					</td>
				</tr>
				
				<tr align=center> 
					<td  class="intfdcolor4" valign="top" align=left>
						<span class="txtlibform"><%=resource.getString("GML.description")%> :</span>
					</td>
					<td  class="intfdcolor4" valign="top" align=left>
						<textarea name="Description" rows="4" cols="49"><%=Encode.javaStringToHtmlString(m_Profile.getDescription())%></textarea>
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