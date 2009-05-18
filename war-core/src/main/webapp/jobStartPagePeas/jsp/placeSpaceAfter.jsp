<%@ include file="check.jsp" %>
<%
    String m_SpaceName = (String) request.getAttribute("currentSpaceName");
    SpaceInst[] brothers = (SpaceInst[]) request.getAttribute("brothers");
			
    browseBar.setDomainName(resource.getString("JSPP.manageHomePage"));
    browseBar.setComponentName(m_SpaceName);	
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