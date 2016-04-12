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
	String nomVoca = Encode.javaStringToHtmlString(voca.getName());
%>

<HTML>
<HEAD>
<TITLE><%=resource.getString("GML.popupTitle")%></TITLE>
<view:looknfeel/>
</HEAD>
<BODY marginheight="5" marginwidth="5" leftmargin="5" topmargin="5" bgcolor="#FFFFFF">
<%
	browseBar.setComponentName(componentLabel, "Main");
	browseBar.setPath("<a href=\"Back\">"+resource.getString("thesaurus.thesaurus")+ "</a> > <a href=\"EditAssignments\">" + resource.getString("thesaurus.BBlistAffectations") + nomVoca + "</a> > " + resource.getString("thesaurus.BBmessageConflit"));

	out.println(window.printBefore());

	out.println(frame.printBefore());
%>

<% // Ici debute le code de la page %>
<center>

<table width="98%" border="0" cellspacing="0" cellpadding="0" class=intfdcolor4><!--tablcontour-->
	<tr>
		<td nowrap>
			<table border="0" cellspacing="0" cellpadding="5" class="contourintfdcolor" width="100%"><!--tabl1-->
				<tr align="center">
					<td  class="intfdcolor4" valign="baseline" align="center">
						<span class="txtnav"><%=resource.getString("thesaurus.dejaAffecte")%> &nbsp;</span>
					</td>
				</tr>
		</table>
		</td>
	</tr>
</table>
<br>

<%
    ButtonPane buttonPane = gef.getButtonPane();
    buttonPane.addButton((Button) gef.getFormButton(resource.getString("thesaurus.tousAffecte"), "CreateJargonsUser", false));
    buttonPane.addButton((Button) gef.getFormButton(resource.getString("thesaurus.onlyssAffcet_toAffect"), "CreateNewJargonsUser", false));
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