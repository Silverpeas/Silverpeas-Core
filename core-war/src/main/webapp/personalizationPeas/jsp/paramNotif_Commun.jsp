<%--

    Copyright (C) 2000 - 2022 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "https://www.silverpeas.org/legal/floss_exception.html"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.

--%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.arraypanes.ArrayColumn" %>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.arraypanes.ArrayPane" %>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.icons.Icon" %>
<%@ page import="java.util.List" %>
<%  List notifPreferences = personalizationScc.getNotifPreferences() ; %>

<script>
  function editPref(id){
    SP_openWindow("editPreference.jsp?id=" + id,"addPrefs","600","250","scrollable=yes");
  }
  function deleteRegle(id) {
    jQuery.popup.confirm("<%=resource.getString("MessageSuppressionRegle")%>", function() {
      var $form = jQuery('#genericFormParamNotif');
      jQuery('#id', $form).val(id);
      jQuery('#Action', $form).val('delete');
      $form.attr('action', "paramNotif.jsp").submit();
    });
  }
</script>

<table cellpadding="5" cellspacing="2" border="0" width="98%"><tr><td>
	<span class="txttitrecol"><%=resource.getString("default")%> :</span>
     &#149;&nbsp;<%=Encode.forHtml(personalizationScc.getDefaultAddressProperties().getProperty("name"))%>&nbsp;&#149;
</td></tr></table>
<%
	// Arraypane notif
	ArrayPane notif = gef.getArrayPane("personalization", "paramNotif.jsp", request,session);
	notif.setVisibleLineNumber(10);
	ArrayColumn arrayColumn1 = notif.addArrayColumn(resource.getString("composant"));
	arrayColumn1.setSortable(true);
	ArrayColumn arrayColumn3 = notif.addArrayColumn(resource.getString("dest"));
	arrayColumn3.setSortable(true);
	ArrayColumn arrayColumn4 = notif.addArrayColumn(resource.getString("arrayPane_Operations"));
	arrayColumn4.setSortable(false);

	Properties p = null;
	ArrayLine arrayLine = null;
	IconPane actions = null;
	Icon modifier = null;
	Icon del = null;
	Icon tst = null;
	for (int i=0 ; i<notifPreferences.size() ; i++) {

	  p = (Properties) notifPreferences.get(i) ;
	  arrayLine = notif.addArrayLine();
	  arrayLine.addArrayCellText(Encode.forHtml(p.getProperty("component")));
	  arrayLine.addArrayCellText(Encode.forHtml(p.getProperty("notifAddress")));

      // Ajout des icones de modification et de suppression
      actions = gef.getIconPane();

      if (p.getProperty("canEdit").equalsIgnoreCase("true"))
      {
          modifier = actions.addIcon();
          modifier.setProperties(modif, resource.getString("GML.modify") , "javascript:editPref(" + p.getProperty("id") + ")");
      }
      else
      {
          modifier = actions.addIcon();
          modifier.setProperties(ArrayPnoColorPix, "" , "");
      }

      if (p.getProperty("canDelete").equalsIgnoreCase("true"))
      {
          del = actions.addIcon();
          del.setProperties(delete, resource.getString("GML.delete") , "javascript:deleteRegle('"+ p.getProperty("id") + "')");
      }
      else
      {
          del = actions.addIcon();
          del.setProperties(ArrayPnoColorPix, "" , "");
      }

      if (p.getProperty("canTest").equalsIgnoreCase("true"))
      {
          tst = actions.addIcon();
          tst.setProperties(test, resource.getString("iconPane_Test") , "paramNotif.jsp?id=" + p.getProperty("id") + "&Action=test");
      }
      else
      {
          tst = actions.addIcon();
          tst.setProperties(ArrayPnoColorPix, "" , "");
      }

	  arrayLine.addArrayCellIconPane(actions);
	}
%>

<form id="genericFormParamNotif" action="" method="post">
  <input id="id" name="id" type="hidden"/>
  <input id="Action" name="Action" type="hidden"/>
</form>

<%out.println(notif.print());%>