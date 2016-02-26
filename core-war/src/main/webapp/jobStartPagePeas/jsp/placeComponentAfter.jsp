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
<%@ include file="check.jsp" %>
<%
    String m_ComponentName = (String) request.getAttribute("currentComponentName");
    ComponentInst[] brothers = (ComponentInst[]) request.getAttribute("brothers");
	SpaceInst currentSpace = (SpaceInst) request.getAttribute("currentSpace");

	window.setPopup(true);
	browseBar.setSpaceId(currentSpace.getId());
    browseBar.setComponentName(m_ComponentName);
    browseBar.setPath(resource.getString("JSPP.ComponentOrder"));
%>
<HTML>
<HEAD>
<TITLE><%=resource.getString("GML.popupTitle")%></TITLE>
<view:looknfeel withCheckFormScript="true"/>
<script language="JavaScript">
function B_ANNULER_ONCLICK() {
	window.close();
}
/*****************************************************************************/
function B_VALIDER_ONCLICK() {
	document.componentOrder.action = "EffectivePlaceComponent";
	document.componentOrder.submit();
}
</script>
</HEAD>
<BODY>
<FORM NAME="componentOrder"  METHOD="POST">
<%
    out.println(window.printBefore());
    out.println(frame.printBefore());
%>
<table width="98%" border="0" cellspacing="0" cellpadding="0" class=intfdcolor4><!--tablcontour-->
	<tr>
		<td nowrap>
			<table border="0" cellspacing="0" cellpadding="5" class="contourintfdcolor" width="100%">
				<!-- Component choice-->
				<tr align=center>
					<td class="intfdcolor4" valign="top" align=left>
						<span class="txtlibform"><%=resource.getString("JSPP.ComponentPlace")%> :</span>
					</td>
					<td width=50% class="intfdcolor4" valign="top" align=left>
                        <SELECT name="ComponentBefore" id="ComponentBefore">
                            <%
                                for (int i = 0; i < brothers.length; i++)
                                {
					out.println("<OPTION value=\"" + brothers[i].getId() + "\">" +  EncodeHelper.javaStringToHtmlString(brothers[i].getLabel()) + "</OPTION>");
                                }
                            %>
                            <OPTION value="-1" selected><%=resource.getString("JSPP.PlaceLast")%></OPTION>
                        </SELECT>
					</td>
				</tr>
			</table>
		</td>
	</tr>
</table>
<br/>
<%
		  ButtonPane buttonPane = gef.getButtonPane();
		  buttonPane.addButton(gef.getFormButton(resource.getString("GML.validate"), "javascript:onClick=B_VALIDER_ONCLICK();", false));
		  buttonPane.addButton(gef.getFormButton(resource.getString("GML.cancel"), "javascript:onClick=B_ANNULER_ONCLICK();", false));
		  out.println(buttonPane.print());
		out.println(frame.printAfter());
        out.println(window.printAfter());
%>
</FORM>
</BODY>
</HTML>