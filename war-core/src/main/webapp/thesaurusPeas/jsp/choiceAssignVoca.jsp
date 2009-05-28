<%@ include file="checkThesaurus.jsp"%>
<%
	String idVoca = (String) request.getAttribute("idVoca");
%>
<HTML>
<HEAD>
<TITLE><%=resource.getString("GML.popupTitle")%></TITLE>
<%
out.println(gef.getLookStyleSheet());
%>
<SCRIPT LANGUAGE="JavaScript">
<!--
function CreateJargonsVoca()
{
	document.forms[0].action = "CreateJargonsVoca";
	document.forms[0].submit();
}
function CreateNewJargonsVoca()
{
	document.forms[0].action = "CreateNewJargonsVoca";
	document.forms[0].submit();
}
//-->
</SCRIPT>
</HEAD>
<BODY marginheight="5" marginwidth="5" leftmargin="5" topmargin="5" bgcolor="#FFFFFF">
<%
	browseBar.setComponentName(componentLabel);
	browseBar.setPath(resource.getString("thesaurus.BBmessageConflit"));

	out.println(window.printBefore());
    
	out.println(frame.printBefore());
%>

<% // Ici debute le code de la page %>
<center>
<FORM METHOD=POST ACTION="">
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
<INPUT TYPE="hidden" name="idVoca" value="<%=idVoca%>">
</FORM>
<%
    ButtonPane buttonPane = gef.getButtonPane();
    buttonPane.addButton((Button) gef.getFormButton(resource.getString("thesaurus.tousAffecte"), "javascript:CreateJargonsVoca();", false));
    buttonPane.addButton((Button) gef.getFormButton(resource.getString("thesaurus.onlyssAffcet_toAffect"), "javascript:CreateNewJargonsVoca();", false));
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