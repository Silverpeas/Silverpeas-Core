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
	Collection vocas = (Collection) request.getAttribute("listVoca");
	Iterator itVoca = vocas.iterator();
%>
<HTML>
<HEAD>
<TITLE><%=resource.getString("GML.popupTitle")%></TITLE>
<view:looknfeel/>
<SCRIPT LANGUAGE="JavaScript">
<!--
function ok () {
	index = document.forms[0].idVoca.selectedIndex;
	id = document.forms[0].idVoca.options[index].value;
	if (id == -1) {
		self.close();
	}
	else {
		document.forms[0].submit()
	}
}
-->
</SCRIPT>
</HEAD>
<BODY marginheight="5" marginwidth="5" leftmargin="5" topmargin="5" bgcolor="#FFFFFF">
<%
	browseBar.setComponentName(componentLabel);
	browseBar.setPath(resource.getString("thesaurus.BBaffectVoc_toSelected"));

	out.println(window.printBefore());

	out.println(frame.printBefore());
%>

<% // Ici debute le code de la page %>
<center>
<FORM METHOD=POST ACTION="SaveAssignVoca">
<table width="98%" border="0" cellspacing="0" cellpadding="0" class=intfdcolor4><!--tablcontour-->
	<tr>
		<td nowrap>
			<table border="0" cellspacing="0" cellpadding="5" class="contourintfdcolor" width="100%"><!--tabl1-->
				<tr align=center>
					<td  class="intfdcolor4" valign="baseline" align=left>
						<span class="txtlibform"><%=resource.getString("thesaurus.vocabulaire")%> :&nbsp;</span>
					</td>
					<td  class="intfdcolor4" valign="baseline" align=left>
                      <span class=selectNS>
                      <select name="idVoca">
					  <option value="-1" selected><%=resource.getString("GML.select")%></option>
				<%
				while (itVoca.hasNext())
				{
					Vocabulary voca = (Vocabulary) itVoca.next();
					String name = Encode.javaStringToHtmlString(voca.getName());
					String id = voca.getPK().getId();
				%>
					<option value="<%=id%>"><%=name%></option>
				<%
				}
				%>
			  <option value="0"><%=resource.getString("thesaurus.none")%></option>
			  </select></span>

					</td>
				</tr>
		</table>
		</td>
	</tr>
</table>



</FORM>
<%
    ButtonPane buttonPane = gef.getButtonPane();
    buttonPane.addButton((Button) gef.getFormButton(resource.getString("GML.validate"), "javascript:ok();", false));
    buttonPane.addButton((Button) gef.getFormButton(resource.getString("GML.cancel"), "javascript:self.close();", false));
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