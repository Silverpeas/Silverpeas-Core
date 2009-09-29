<%--

    Copyright (C) 2000 - 2009 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have recieved a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://repository.silverpeas.com/legal/licensing"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@ include file="checkPersonalization.jsp" %>

<%
boolean isThesaurusEnabled	= new Boolean((String) request.getAttribute("thesaurusStatus")).booleanValue();
boolean isDragDropEnabled	= new Boolean((String) request.getAttribute("dragDropStatus")).booleanValue();
boolean isOnlineEditingEnabled = new Boolean((String) request.getAttribute("onlineEditingStatus")).booleanValue();
boolean isWebdavEditingEnabled = new Boolean((String) request.getAttribute("webdavEditingStatus")).booleanValue();
String 	selectedLanguage	= (String) request.getAttribute("selectedLanguage");
String 	selectedLook		= (String) request.getAttribute("selectedLook");
String 	favoriteSpace		= (String) request.getAttribute("FavoriteSpace");
List	spaceTreeview		= (List) request.getAttribute("SpaceTreeview");
List 	allLanguages		= (List) request.getAttribute("AllLanguages");

Boolean framesetMustBeReloaded = (Boolean) request.getAttribute("FramesetMustBeReloaded");
boolean reloadFrameset = false;
if (framesetMustBeReloaded != null)
{
	reloadFrameset = framesetMustBeReloaded.booleanValue();
}

String favoriteFrame		= gef.getLookFrame();
Vector availableLooks		= gef.getAvailableLooks();

if (reloadFrameset)
{
%>
	<script language="Javascript">
			window.top.location.replace("<%=m_context%><%=(favoriteFrame.startsWith("/")) ? ""  : "/admin/jsp/"%><%=favoriteFrame%>");
	</script>
<%
}

String checkedThesaurus_activate 		= "";
String checkedDragDrop_activate 		= "";
String checkedOnlineEditing_activate 	= "";
String checkedWebdavEditing_activate    = "";

if (isThesaurusEnabled)
	checkedThesaurus_activate = "checked";

if (isDragDropEnabled)
	checkedDragDrop_activate = "checked";

if (isOnlineEditingEnabled)
	checkedOnlineEditing_activate = "checked";
if (isWebdavEditingEnabled)
  checkedWebdavEditing_activate = "checked";
%>
<HTML>
<HEAD>
<TITLE><%=resource.getString("GML.popupTitle")%></TITLE>
<%
out.println(gef.getLookStyleSheet());
%>
<script language="JavaScript">
function checkForm()
{
	var currentLook = '<%=selectedLook%>';
	if (document.languageForm.SelectedLook.value != currentLook)
		alert("<%=resource.getString("PP.ChangeLookAlert")%>");

	document.languageForm.submit();
}
</script>
</HEAD>
<BODY MARGINHEIGHT="5" MARGINWIDTH="5" TOPMARGIN="5" LEFTMARGIN="5">
<%
	browseBar.setComponentName(resource.getString("PersonalizationTitleTab1"));
	out.println(window.printBefore());

	TabbedPane tabbedPane = gef.getTabbedPane();
    tabbedPane.addTab(resource.getString("Preferences"), "#", true);
    tabbedPane.addTab(resource.getString("Identity"), "ChangePassword", false);
    out.println(tabbedPane.print());

	out.println(frame.printBefore());
	out.println(board.printBefore());
%>
<form name="languageForm" Action="SavePreferences" Method="POST">
	<table border="0" cellspacing="0" cellpadding="5" width="100%">
	<!-- Language -->
    <tr>
        <td class="txtlibform" align="left" valign="baseline" width="200px"><%=resource.getString("FavoriteLanguage")%> :</td>
        <td align="left" valign="baseline">
	        <select name="SelectedLanguage" size="1">

				<%
				if (allLanguages != null)
		  		{
					String selected = "";
		  			Iterator it = (Iterator) allLanguages.iterator();
		  			while (it.hasNext())
			  		{
		  				String langue = (String) it.next();
		  				if (selectedLanguage.equals(langue))
		  				{
		  					selected = "selected";
		  				}
		  				%>
		  				<option value=<%=langue%> <%=selected%>><%=resource.getString("language_" + langue)%></option>
		  				<%
		  				selected = "";
			  		}
		  		}
				%>
		</select>
       </td>
    </tr>
	<!-- Look -->
	<% if (availableLooks.size() > 1) { %>
	<tr>
        <td class="txtlibform" align="left" valign="baseline"><%=resource.getString("FavoriteLook")%> :</td>
        <td align="left" valign="baseline"><select name="SelectedLook" size="1">
            <% for (int i = 0; i < availableLooks.size(); i++) {
                String lookName = (String) availableLooks.get(i);
                if (selectedLook.equals(lookName))
                  out.println("<option value=\""+lookName+"\" selected>"+lookName+"</option>");
                else
                  out.println("<option value=\""+lookName+"\">"+lookName+"</option>");
            } %>
            </select>
        </td>
    </tr>
    <% } else { %>
    	<input type="hidden" name="SelectedLook" value="<%=selectedLook%>"/>
    <% } %>
	<!-- defaultWorkSpace -->
    <tr>
        <td class="txtlibform" align="left" valign="baseline"><%=resource.getString("DefaultWorkSpace")%> :</td>
        <td align="left" valign="baseline">
        	<select name="SelectedWorkSpace" size="1">
                <%
                	if (favoriteSpace == null || favoriteSpace.equals("null"))
						out.println("<option value=null selected>"+resource.getString("UndefinedFavoriteSpace")+"</option>");

                	SpaceInstLight 	sil 		= null;
                	String			indent		= null;
                	String			selected	= null;
                	for(int t = 0; t < spaceTreeview.size(); t++) {
                		sil = (SpaceInstLight) spaceTreeview.get(t);

                		//gestion de l'indentation
                		indent = "";
                		for (int l=0; l<sil.getLevel(); l++)
                		{
                			indent += "&nbsp;&nbsp;";
                		}

                		selected = "";
                		if (sil.getFullId().equals(favoriteSpace))
                			selected = "selected";

                		out.println("<option value=\""+sil.getFullId()+"\" "+selected+">"+indent+sil.getName()+"</option>");
                	}
                %>
            </select>
        </td>
    </tr>
	<!-- ThesaurusState -->
    <tr>
        <td class="txtlibform" align="left" valign="baseline"><%=resource.getString("Thesaurus")%> :</td>
        <td align="left" valign="middle">
			<input name="opt_thesaurusStatus" type="checkbox" value="true" <%=checkedThesaurus_activate%>>
        </td>
    </tr>
    <!-- Drag&Drop -->
    <tr>
        <td class="txtlibform" align="left" valign="baseline"><%=resource.getString("DragDrop")%> :</td>
        <td align="left" valign="middle">
			<input name="opt_dragDropStatus" type="checkbox" value="true" <%=checkedDragDrop_activate%>>
        </td>
    </tr>
    <!-- Online Editing -->
    <tr>
        <td class="txtlibform" align="left" valign="baseline"><%=resource.getString("OnlineEditing")%> :</td>
        <td align="left" valign="middle">
			<input name="opt_onlineEditingStatus" type="checkbox" value="true" <%=checkedOnlineEditing_activate%>>
        </td>
    </tr>
    <!-- Webdav Editing -->
    <tr>
        <td class="txtlibform" align="left" valign="baseline"><%=resource.getString("WebdavEditing")%> :</td>
        <td align="left" valign="middle">
            <input name="opt_webdavEditingStatus" type="checkbox" value="true" <%=checkedWebdavEditing_activate%>>
            <!-- <span style="vertical-align: middle;">(<a href="<%= request.getScheme()%>://<%=request.getServerName()%><%= request.getServerPort() > 0 ? (":" + request.getServerPort()) : ("") %><%=request.getContextPath()%>/personalizationPeas/jsp/launch.jsp"><%=resource.getString("WebdavEditing.installation")%></a>)</span> -->
        </td>
    </tr>
  </table>
</form>
<center>
<%
		out.println(board.printAfter());

		ButtonPane buttonPane = gef.getButtonPane();
		Button validateButton = gef.getFormButton(resource.getString("GML.validate"), "javascript:onClick=checkForm();", false);
		buttonPane.addButton(validateButton);
		Button cancelButton = gef.getFormButton(resource.getString("GML.cancel"), "javascript:onClick=history.back();", false);
		buttonPane.addButton(cancelButton);
		out.println("<BR><center>"+buttonPane.print()+"</center>");
		out.println(frame.printAfter());
		out.println(window.printAfter());
%>
</center>
</BODY>
</HTML>