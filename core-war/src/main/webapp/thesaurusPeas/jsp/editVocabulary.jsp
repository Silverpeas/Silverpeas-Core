<%--

    Copyright (C) 2000 - 2013 Silverpeas

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
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ include file="checkThesaurus.jsp"%>
<%
	Vocabulary voca = (Vocabulary) request.getAttribute("Vocabulary");
	String nom = Encode.javaStringToHtmlString(voca.getName());
	String desc = Encode.javaStringToHtmlString(voca.getDescription());
%>
<HTML>
<HEAD>
<TITLE><%=resource.getString("GML.popupTitle")%></TITLE>
<view:looknfeel/>
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
function Deletes()
{
	if (window.confirm("<%=resource.getString("thesaurus.MessageSuppressionVoca")%>"))
    jQuery('#genericForm').attr('action', "DeleteVoca").submit();

}
//-->
</SCRIPT>
</HEAD>
<BODY marginheight="5" marginwidth="5" leftmargin="5" topmargin="5" bgcolor="#FFFFFF" onLoad="document.forms[0].nom.focus();">
<%
	browseBar.setComponentName(componentLabel, "Main");
	browseBar.setPath("<a href=\"Back\">"+resource.getString("thesaurus.thesaurus")+ "</a> > " + resource.getString("thesaurus.BBeditVoc"));




	operationPane.addOperation(resource.getIcon("thesaurus.OPdeleteVoc"),
		resource.getString("thesaurus.OPdeleteVoc2"), "javascript:Deletes();");



	out.println(window.printBefore());


	out.println(frame.printBefore());
%>

<% // Ici debute le code de la page %>
<center>
<FORM METHOD=POST ACTION="UpdateVoca">
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
<form id="genericForm" action="" method="POST"></form>
</BODY>
</HTML>