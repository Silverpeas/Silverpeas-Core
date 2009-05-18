<%@ include file="check.jsp" %>

<%
    String m_SpaceName = (String) request.getAttribute("currentSpaceName");
    String m_SubSpace = (String) request.getAttribute("nameSubSpace");
    SpaceTemplateProfile[] m_SpaceTemplateProfiles = (SpaceTemplateProfile[]) request.getAttribute("SpaceTemplateProfiles");
    String[][] m_SpaceTemplateProfilesGroups = (String[][]) request.getAttribute("SpaceTemplateProfilesGroups");
    String[][] m_SpaceTemplateProfilesUsers = (String[][]) request.getAttribute("SpaceTemplateProfilesUsers");
			
    browseBar.setDomainName(resource.getString("JSPP.manageHomePage"));
    if ((m_SubSpace == null) || (m_SubSpace.length() <= 0)) { //je suis sur un espace
        browseBar.setComponentName(m_SpaceName);	
    }
    else {
        browseBar.setComponentName(m_SpaceName + " > " + m_SubSpace);
    }

    if ((m_SubSpace == null) || (m_SubSpace.length() <= 0)) //je suis sur un espace
        browseBar.setPath(resource.getString("JSPP.creationSpace"));
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
<script language="javascript">
function goToOperationInUserPanel(action) {
	url = action;
	windowName = "userPanelWindow";
	windowParams = "directories=0,menubar=0,toolbar=0,alwaysRaised,scrollbars,resizable";
	userPanelWindow = SP_openUserPanel(url, windowName, windowParams);
}    
</script>
</HEAD>

<BODY marginheight=5 marginwidth=5 leftmargin=5 topmargin=5>
<FORM NAME="infoSpace" action = "EffectiveCreateSpace" METHOD="POST">
<input type="hidden" name="SpaceBefore" value="<%=(String)request.getAttribute("SpaceBefore")%>">
<%
out.println(window.printBefore());
out.println(frame.printBefore());
%>
<center>
<table width="98%" border="0" cellspacing="0" cellpadding="0" class=intfdcolor4><!--tablcontour-->
	<tr> 
		<td nowrap>
			<table border="0" cellspacing="0" cellpadding="5" class="contourintfdcolor" width="100%"><!--tabl1-->
                <%
                    for (int i = 0; i < m_SpaceTemplateProfiles.length; i++)
                    {
                %>
                    <tr align=center> 
                        <td  class="intfdcolor4" valign="baseline" align=left>
                            <span class="txtlibform"><%=m_SpaceTemplateProfiles[i].getLabel()%> :</span>
                        </td>
                        <td  class="intfdcolor4" valign="baseline" align=left>
                            <input type="text" name="NameObject" size="60" maxlength="60" value="<% out.print("" + m_SpaceTemplateProfilesGroups[i].length + " " + resource.getString("GML.groupes") + " - " + m_SpaceTemplateProfilesUsers[i].length + " " + resource.getString("GML.users")); %>" disabled>
                            &nbsp;<a href="javaScript:onClick=goToOperationInUserPanel('InvokeUserPanelForTemplateProfile?profileIndex=<%=Integer.toString(i)%>')"><img src="<%=resource.getIcon("JSPP.UPSpaceTemplateProfile")%>" align="middle" alt="<%=resource.getString("JSPP.UPSpaceTemplateProfile")%>" border=0 title="<%=resource.getString("JSPP.UPSpaceTemplateProfile")%>"></a> 
                        </td>
                    </tr>
                <%
                    }
                %>
			</table>
		</td>
	</tr>

</table>


<br>
<%
		  ButtonPane buttonPane = gef.getButtonPane();
		  buttonPane.addButton((Button) gef.getFormButton(resource.getString("GML.validate"), "javascript:onClick=document.infoSpace.submit();", false));
		  buttonPane.addButton((Button) gef.getFormButton(resource.getString("GML.cancel"), "javascript:onClick=window.close();", false));
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