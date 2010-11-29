<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<%@page import="com.stratelia.webactiv.util.ResourceLocator"%>
<%@page import="com.stratelia.silverpeas.util.ResourcesWrapper"%>
<%@page import="com.silverpeas.util.EncodeHelper"%>
<%@page import="com.stratelia.webactiv.util.viewGenerator.html.buttonPanes.ButtonPane"%>
<%@page import="com.stratelia.webactiv.util.viewGenerator.html.buttons.Button"%>
<%@page import="com.silverpeas.util.StringUtil"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Iterator"%>
<%@page import="com.stratelia.webactiv.beans.admin.SpaceInstLight"%>
    
<%
ResourceLocator rs = new ResourceLocator("com.stratelia.silverpeas.personalizationPeas.settings.personalizationPeasSettings", "");
ResourcesWrapper resource = (ResourcesWrapper) request.getAttribute("resources");
ResourceLocator general = new ResourceLocator("com.stratelia.silverpeas.lookAndFeel.generalLook", "");

boolean isThesaurusEnabled	= ((Boolean) request.getAttribute("thesaurusStatus")).booleanValue();
boolean isDragDropEnabled	= ((Boolean) request.getAttribute("dragDropStatus")).booleanValue();
boolean isWebdavEnabled 	= ((Boolean) request.getAttribute("webdavEditingStatus")).booleanValue();
String 	selectedLanguage	= (String) request.getAttribute("selectedLanguage");
String 	selectedLook		= (String) request.getAttribute("selectedLook");
String 	favoriteSpace		= (String) request.getAttribute("FavoriteSpace");
List	spaceTreeview		= (List) request.getAttribute("SpaceTreeview");
List 	allLanguages		= (List) request.getAttribute("AllLanguages");

String message = null;

List availableLooks		= gef.getAvailableLooks();

String checkedThesaurus_activate 		= "";
String checkedDragDrop_activate 		= "";
String checkedWebdavEditing_activate    = "";
if (isThesaurusEnabled) {
	checkedThesaurus_activate = "checked=\"checked\"";
}
if (isDragDropEnabled) {
	checkedDragDrop_activate = "checked=\"checked\"";
}
if (isWebdavEnabled) {
  checkedWebdavEditing_activate = "checked=\"checked\"";
}

%>

<% if (StringUtil.isDefined(message)) { %>
<div class="inline_message_ok">
	<%=message %>
</div>
<% } %>

<form name="UserForm" action="UpdateMySettings" method="post">
<table border="0" cellspacing="0" cellpadding="5" width="100%">
	<!-- Language -->
    <tr>
        <td class="txtlibform"><%=resource.getString("myProfile.settings.FavoriteLanguage")%> :</td>
        <td>
	        <select name="SelectedLanguage" size="1">
				<%
				if (allLanguages != null) {
					String selected = "";
		  			Iterator it = allLanguages.iterator();
		  			while (it.hasNext()) {
		  				String langue = (String) it.next();
		  				selected = "";
		  				if (selectedLanguage.equals(langue)) {
		  					selected = "selected=\"selected\"";
		  				}
		  				%>
		  				<option value="<%=langue%>" <%=selected%>><%=resource.getString("myProfile.settings.language_" + langue)%></option>
		  				<%
			  		}
		  		}
				%>
		</select>
       </td>
    </tr>
	<!-- Look -->
	<% if (availableLooks.size() > 1) { %>
	<tr>
        <td class="txtlibform"><%=resource.getString("myProfile.settings.FavoriteLook")%> :</td>
        <td><select name="SelectedLook" size="1">
            <% for (int i = 0; i < availableLooks.size(); i++) {
                String lookName = (String) availableLooks.get(i);
                if (selectedLook.equals(lookName)) {
                  out.println("<option value=\""+lookName+"\" selected=\"selected\">"+lookName+"</option>");
                } else {
                  out.println("<option value=\""+lookName+"\">"+lookName+"</option>");
                }
            } %>
            </select>
        </td>
    </tr>
    <% } else { %>
    	<input type="hidden" name="SelectedLook" value="<%=selectedLook%>"/>
    <% } %>
	<!-- defaultWorkSpace -->
    <tr>
        <td class="txtlibform"><%=resource.getString("myProfile.settings.DefaultWorkSpace")%> :</td>
        <td>
        	<select name="SelectedWorkSpace" size="1">
                <%
                	if (favoriteSpace == null || favoriteSpace.equals("null")) {
						out.println("<option value=\"null\" selected=\"selected\">"+resource.getString("UndefinedFavoriteSpace")+"</option>");
                	}

                	SpaceInstLight 	sil 		= null;
                	String			indent		= null;
                	String			selected	= null;
                	for(int t = 0; t < spaceTreeview.size(); t++) {
                		sil = (SpaceInstLight) spaceTreeview.get(t);

                		//gestion de l'indentation
                		indent = "";
                		for (int l=0; l<sil.getLevel(); l++) {
                			indent += "&nbsp;&nbsp;";
                		}

                		selected = "";
                		if (sil.getFullId().equals(favoriteSpace)) {
                			selected = "selected=\"selected\"";
                		}

                		out.println("<option value=\""+sil.getFullId()+"\" "+selected+">"+indent+sil.getName()+"</option>");
                	}
                %>
            </select>
        </td>
    </tr>
	<!-- ThesaurusState -->
    <tr>
        <td class="txtlibform"><%=resource.getString("myProfile.settings.Thesaurus")%> :</td>
        <td>
			<input name="opt_thesaurusStatus" type="checkbox" value="true" <%=checkedThesaurus_activate%>/>
        </td>
    </tr>
    <!-- Drag&Drop -->
    <tr>
        <td class="txtlibform"><%=resource.getString("myProfile.settings.DragDrop")%> :</td>
        <td>
			<input name="opt_dragDropStatus" type="checkbox" value="true" <%=checkedDragDrop_activate%>/>
        </td>
    </tr>
    <!-- Webdav Editing -->
    <tr>
        <td class="txtlibform"><%=resource.getString("myProfile.settings.WebdavEditing")%> :</td>
        <td>
            <input name="opt_webdavEditingStatus" type="checkbox" value="true" <%=checkedWebdavEditing_activate%>/>
        </td>
    </tr>    
</table>
</form>
 <%
		ButtonPane buttonPane = gef.getButtonPane();
		Button validateButton = gef.getFormButton(resource.getString("GML.validate"), "javascript:onClick=submitForm();", false);
		buttonPane.addButton(validateButton);
		Button cancelButton = gef.getFormButton(resource.getString("GML.cancel"), "javascript:onClick=history.back();", false);
		buttonPane.addButton(cancelButton);
		out.println("<br/><center>"+buttonPane.print()+"</center>");
%>

<script type="text/javascript">
	function submitForm() {
		var currentLook = '<%=selectedLook%>';
		if (document.UserForm.SelectedLook.value != currentLook) {
			alert("<%=resource.getString("myProfile.settings.ChangeLookAlert")%>");
		}
	
		document.UserForm.submit();
	}
</script>