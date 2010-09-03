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
    SpaceInst[] brothers = (SpaceInst[]) request.getAttribute("brothers");
    String spaceId = (String) request.getAttribute("CurrentSpaceId");

    browseBar.setSpaceId(spaceId);
    browseBar.setClickable(false);
    browseBar.setPath(resource.getString("JSPP.SpaceOrder"));
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
    document.spaceOrder.submit();
}
</script>
</HEAD>
<BODY marginheight=5 marginwidth=5 leftmargin=5 topmargin=5 onLoad="document.spaceOrder.SpaceBefore.focus();">
<FORM NAME="spaceOrder" action = "EffectivePlaceSpaceAfter" METHOD="POST">
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
					<td  class="intfdcolor4" valign="top" align=left>
						<span class="txtlibform"><%=resource.getString("JSPP.SpacePlace")%> :</span>
					</td>
					<td  class="intfdcolor4" valign="top" align=left>
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