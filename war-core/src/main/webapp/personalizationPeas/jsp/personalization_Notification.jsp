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

<%@ include file="checkPersonalization.jsp"%>

<%
  //R�cup�ration des param�tres
			String action = (String) request.getParameter("Action");
			String id = (String) request.getParameter("id");
			String testExplanation = "";

			boolean isMultiChannelNotif = personalizationScc.isMultiChannelNotification();

			// Liste des adresses de notification pour ce user.
			ArrayList notifAddresses = null;

			//Mise a jour de l'espace
			if (action != null) {
				if (action.equals("test")) {
					personalizationScc.testNotifAddress(id);
					if (id.equals("-10")) {
						testExplanation = resource
								.getString("TestPopUpExplanation");
					} else if (id.equals("-12")) {
						testExplanation = resource
								.getString("TestSilverMailExplanation");
					} else {
						testExplanation = resource
								.getString("TestSMTPExplanation");
					}
					action = "NotificationView";
				}
				if (action.equals("setDefault")) {
					personalizationScc.setDefaultAddress(id);  
					action = "NotificationView";
				}
				if (action.equals("delete")) {
					personalizationScc.deleteNotifAddress(id);
					action = "NotificationView";
				}
			} else
				action = "NotificationView";
			notifAddresses = personalizationScc.getNotificationAddresses();
%>

<HTML>
<HEAD>
<TITLE><%=resource.getString("GML.popupTitle")%></TITLE>
<%
  out.println(gef.getLookStyleSheet());
%>
<script type="text/javascript"
	src="<%=m_context%>/util/javaScript/animation.js"></script>
<script>
function editNotif(id){
	SP_openWindow("editNotification.jsp?id=" + id,"addNotif","600","250","scrollbars=yes");
}
function paramNotif(){
	SP_openWindow("paramNotif.jsp","paramNotif","750","400","scrollbars=yes");
}
function deleteCanal(id){
	if (window.confirm("<%=resource.getString("MessageSuppressionCanal")%>")) {
	   location.href = "personalization_Notification.jsp?id=" + id + "&Action=delete";
	}
}

function sendChoiceChannel() {
    document.channelForm.SelectedChannels.value  = getChannels();
    document.channelForm.action       = "SaveChannels";
    document.channelForm.submit();
}

function getChannels()
{
  var  items = "";
  try
  {
    var boxItems = document.channelForm.SelectChannel;
    if (boxItems != null){
      // au moins une checkbox exist
      var nbBox = boxItems.length;
      if ( (nbBox == null) && (boxItems.checked == true) ){
        items += boxItems.value+",";
      } else{
        for (i=0;i<boxItems.length ;i++ ){
          if (boxItems[i].checked == true){
            items += boxItems[i].value+",";
          }
        }
      }
    }
  }
  catch (e)
  {
    //Checkboxes are not displayed 
  }
  return items;
}

</script>
</HEAD>
<BODY marginwidth=5 marginheight=5 leftmargin=5 topmargin=5
	bgcolor="#FFFFFF">
<%
  browseBar.setComponentName(resource.getString("MesNotifications"));
			browseBar.setPath(resource.getString("ParametrerNotification"));

			OperationPane operationPane = window.getOperationPane();
			operationPane.addOperation(addProtocol, resource
					.getString("operationPane_addadress"),
					"javascript:editNotif(-1)");
			operationPane.addLine();
			operationPane.addOperation(paramNotif, resource
					.getString("operationPane_paramnotif"),
					"javascript:paramNotif()");
			out.println(window.printBefore());

			//Onglets
			TabbedPane tabbedPane = gef.getTabbedPane();
			tabbedPane.addTab(resource.getString("LireNotification"), m_context
					+ URLManager.getURL(URLManager.CMP_SILVERMAIL) + "Main",
					false);
			tabbedPane.addTab(resource.getString("SendedUserNotifications"),
					m_context + URLManager.getURL(URLManager.CMP_SILVERMAIL)
							+ "SendedUserNotifications", false);
			tabbedPane.addTab(resource.getString("ParametrerNotification"),
					"personalization_Notification.jsp?Action=LanguageView",
					true);
			out.println(tabbedPane.print());

			out.println(frame.printBefore());
%>
<!-- AFFICHAGE HEADER -->
<CENTER>
<FORM NAME="channelForm"><input type="hidden" name="SelectedChannels">
<%

  if (testExplanation.length() > 0) {
				out.println("<font color=red><B>" + testExplanation
						+ "<B></font><BR><BR>");
			}

			IconPane actions;

			// Arraypane notif
			ArrayPane notif = gef.getArrayPane("personalization",
					"personalization_Notification.jsp", request, session);
			ArrayColumn arrayColumn00 = notif.addArrayColumn(resource
					.getString("arrayPane_Default"));
			arrayColumn00.setSortable(false);
			ArrayColumn arrayColumn0 = notif.addArrayColumn(resource
					.getString("arrayPane_Nom"));
			arrayColumn0.setSortable(true);
			ArrayColumn arrayColumn3 = notif.addArrayColumn(resource
					.getString("arrayPane_Adresse"));
			arrayColumn3.setSortable(true);
			ArrayColumn arrayColumn4 = notif.addArrayColumn(resource
					.getString("arrayPane_Operations"));
			arrayColumn4.setSortable(false);

			Properties p = null;
			ArrayLine arrayLine = null;
			Icon def = null;
			for (int i = 0; i < notifAddresses.size(); i++) {

				p = (Properties) notifAddresses.get(i);
				arrayLine = notif.addArrayLine();

				// Ajout l'icone de default
				actions = gef.getIconPane();

				if (!isMultiChannelNotif) {
					def = actions.addIcon();
					if (p.getProperty("isDefault").equalsIgnoreCase("true")) {
						def.setProperties(on_default, "", "");
					} else {
						def.setProperties(off_default, resource
								.getString("iconPane_Default"),
								"personalization_Notification.jsp?id="
										+ p.getProperty("id")
										+ "&Action=setDefault");
					}
					arrayLine.addArrayCellIconPane(actions);
				} else {
					// afficher les choix en case � cocher pour une selection multiple
					String usedCheck = "";
					if (p.getProperty("isDefault").equalsIgnoreCase("true")) {
						usedCheck = "checked";
					}
						arrayLine.addArrayCellText("<input type=\"checkbox\" name=\"SelectChannel\" value=\""
										+ Encode.javaStringToHtmlString(p.getProperty("id")) + "\" "
										+ usedCheck
										+ ">");
				}
				arrayLine.addArrayCellText(Encode.javaStringToHtmlString(p
						.getProperty("name")));
				arrayLine.addArrayCellText(Encode.javaStringToHtmlString(p
						.getProperty("address")));

				// Ajout des icones de modification et de suppression
				actions = gef.getIconPane();

				if (p.getProperty("canEdit").equalsIgnoreCase("true")) {
					Icon modifier = actions.addIcon();
					modifier.setProperties(modif, resource
							.getString("GML.modify"), "javascript:editNotif("
							+ p.getProperty("id") + ")");
				} else {
					Icon modifier = actions.addIcon();
					modifier.setProperties(ArrayPnoColorPix, "", "");
				}

				if (p.getProperty("canDelete").equalsIgnoreCase("true")) {
					Icon del = actions.addIcon();
					del.setProperties(delete, resource.getString("GML.delete"),
							"javascript:deleteCanal('" + p.getProperty("id")
									+ "')");
				} else {
					Icon del = actions.addIcon();
					del.setProperties(ArrayPnoColorPix, "", "");
				}

				if (p.getProperty("canTest").equalsIgnoreCase("true")) {
					Icon tst = actions.addIcon();
					tst.setProperties(test,
							resource.getString("iconPane_Test"),
							"personalization_Notification.jsp?id="
									+ p.getProperty("id") + "&Action=test");
				} else {
					Icon tst = actions.addIcon();
					tst.setProperties(ArrayPnoColorPix, "", "");
				}

				arrayLine.addArrayCellIconPane(actions);
			}

			out.println(notif.print());
			if (isMultiChannelNotif) {
			  // ajout bouton de validation des choix des canaux
			  Button validateButton = (Button) gef.getFormButton(resource.getString("GML.validate"), "javascript:onClick=sendChoiceChannel();", false);
			  ButtonPane buttonPane = gef.getButtonPane();
		    buttonPane.addButton(validateButton);
		    out.println("<BR><center>"+buttonPane.print()+"</center><BR>");
			}
			out.println(frame.printAfter());
			out.println(window.printAfter());
%>
</FORM>
</BODY>
</HTML>