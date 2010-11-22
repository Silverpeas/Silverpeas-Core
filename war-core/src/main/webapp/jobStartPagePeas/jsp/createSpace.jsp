<%--

    Copyright (C) 2000 - 2009 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://repository.silverpeas.com/legal/licensing"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ include file="check.jsp" %>

<%
String 		m_SousEspace 		= (String) request.getAttribute("SousEspace");
Map     	m_SpaceTemplates 	= (Map) request.getAttribute("spaceTemplates");
SpaceInst[] brothers 			= (SpaceInst[]) request.getAttribute("brothers");
String 		spaceId				= (String) request.getAttribute("CurrentSpaceId");

	browseBar.setSpaceId(spaceId);
	browseBar.setClickable(false);
	if (m_SousEspace == null)
		browseBar.setComponentName(resource.getString("JSPP.creationSpace"));
	else
		browseBar.setPath(resource.getString("JSPP.creationSubSpace"));

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
		document.infoSpace.submit();
	}
}

/*****************************************************************************/

function isCorrectForm() {
     	var errorMsg = "";
     	var errorNb = 0;

		var name = stripInitialWhitespace(document.infoSpace.NameObject.value);
		var desc = document.infoSpace.Description;

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
<BODY marginheight="5" marginwidth="5" leftmargin="5" topmargin="5" onLoad="document.infoSpace.NameObject.focus();">
<FORM NAME="infoSpace" action = "SetSpaceTemplateProfile" METHOD="POST">
<input type="hidden" name="SousEspace" value="<%=m_SousEspace%>">

<%
out.println(window.printBefore());
out.println(frame.printBefore());
out.println(board.printBefore());
%>

		<table border="0" cellspacing="0" cellpadding="5" width="100%">
			<%=I18NHelper.getFormLine(resource)%>
			<tr>
				<td class="txtlibform"><%=resource.getString("GML.name")%> :</td>
				<td><input type="text" name="NameObject" size="60" maxlength="60" value="">&nbsp;<img src="<%=resource.getIcon("mandatoryField")%>" width="5" height="5" border="0"></td>
			</tr>
			<tr>
				<td class="txtlibform" valign="top"><%=resource.getString("GML.description")%> :</td>
				<td><textarea name="Description" rows="4" cols="49"></textarea></td>
			</tr>
			<tr>
				<td class="txtlibform"><%=resource.getString("JSPP.SpacePlace")%> :</td>
				<td valign="top">
                    <SELECT name="SpaceBefore" id="SpaceBefore">
                        <%
                            for (int i = 0; i < brothers.length; i++)
                            {
                                out.println("<OPTION value=\"" + brothers[i].getId() + "\">" + brothers[i].getName() + "</OPTION>");
                            }
                        %>
                        <OPTION value="-1" selected><%=resource.getString("JSPP.PlaceLast")%></OPTION>
                    </SELECT>
				</td>
			</tr>
            <tr>
				<td class="txtlibform"><%=resource.getString("JSPP.SpaceTemplate")%> :</td>
				<td>
                    <SELECT name="SpaceTemplate" id="SpaceTemplate">
                        <OPTION value=""><%=resource.getString("JSPP.NoTemplate")%></OPTION>
                        <%
                            Iterator it = m_SpaceTemplates.keySet().iterator();

                            while (it.hasNext())
                            {
                                String theKey = (String)it.next();
                                SpaceTemplate st = (SpaceTemplate)m_SpaceTemplates.get(theKey);
                                out.println("<OPTION value=\"" + theKey + "\">" + st.getTemplateName() + "</OPTION>");
                            }
                        %>
                    </SELECT>
				</td>
			</tr>
			<tr align=left>
				<td colspan="2">(<img border="0" src="<%=resource.getIcon("mandatoryField")%>" width="5" height="5"> : <%=resource.getString("GML.requiredField")%>)</td>
			</tr>
		</table>

<%
		out.println(board.printAfter());

		ButtonPane buttonPane = gef.getButtonPane();
		buttonPane.addButton((Button) gef.getFormButton(resource.getString("GML.validate"), "javascript:onClick=B_VALIDER_ONCLICK();", false));
		buttonPane.addButton((Button) gef.getFormButton(resource.getString("GML.cancel"), "javascript:onClick=B_ANNULER_ONCLICK();", false));
		out.println("<br/><center>"+buttonPane.print()+"</center>");

		out.println(frame.printAfter());
		out.println(window.printAfter());
%>
</FORM>
</BODY>
</HTML>