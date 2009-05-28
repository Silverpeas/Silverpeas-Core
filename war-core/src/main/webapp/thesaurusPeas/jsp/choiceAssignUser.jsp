<%@ include file="checkThesaurus.jsp"%>

<%
	Vocabulary voca = (Vocabulary) request.getAttribute("Vocabulary");
	String nomVoca = Encode.javaStringToHtmlString(voca.getName());
%>

<HTML>
<HEAD>
<TITLE><%=resource.getString("GML.popupTitle")%></TITLE>
<%
out.println(gef.getLookStyleSheet());
%>
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