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
Collection 	tickets 		= (Collection) request.getAttribute("Tickets");

%>

<html>

<head>
<%
	out.println(gef.getLookStyleSheet());
%>

<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>

<script type="text/javascript">

var ticketWindow = window;

function editTicket(keyFile) 
{
    urlWindows = "EditTicket?KeyFile="+keyFile;
	windowName = "ticketWindow";
	larg = "700";
	haut = "400";
    windowParams = "directories=0,menubar=0,toolbar=0, alwaysRaised";
    if (!ticketWindow.closed && ticketWindow.name== "ticketWindow")
        ticketWindow.close();
    ticketWindow = SP_openWindow(urlWindows, windowName, larg, haut, windowParams);
}
	
function deleteTicket(keyFile) 
{
	if(window.confirm("<%=resource.getString("fileSharing.confirmDeleteTicket")%> ?"))
	{
 		document.deleteForm.KeyFile.value = keyFile;
  		document.deleteForm.submit();
	}
}

</script>
</head>
<body>
<form name="readForm" action="" method="post">
<input type="hidden" name="mode"/>
<%
	browseBar.setComponentName(resource.getString("fileSharing.tickets"));
	
	out.println(window.printBefore());
	out.println(frame.printBefore());
	
	ArrayPane arrayPane = gef.getArrayPane("ticketList", "ViewTickets", request, session);
	arrayPane.addArrayColumn(resource.getString("GML.nom"));
	ArrayColumn columnTicket = arrayPane.addArrayColumn(resource.getString("fileSharing.ticket"));
	columnTicket.setSortable(false);
	arrayPane.addArrayColumn(resource.getString("fileSharing.endDate"));
	arrayPane.addArrayColumn(resource.getString("fileSharing.nbAccess"));
	ArrayColumn columnOp = arrayPane.addArrayColumn(resource.getString("fileSharing.operation"));
	columnOp.setSortable(false);
	
		// remplissage de l'ArrayPane avec les tickets
		if ((tickets != null) && (tickets.size() != 0)) 
		{
			Iterator it = (Iterator) tickets.iterator();
			while (it.hasNext()) 
			{
				ArrayLine line = arrayPane.addArrayLine();
				TicketDetail ticket = (TicketDetail) it.next();
				
				if (ticket.getAttachmentDetail() != null || ticket.getDocument() != null)
				{
				
					String fileId = Integer.toString(ticket.getFileId());
					String lien = "/File/"+ fileId;
					// ajouter le context devant le lien si n�c�ssaire
					if (lien.indexOf("://") == -1) {
						lien = m_context + lien;
					}
						
					if (ticket.getAttachmentDetail() != null)
						line.addArrayCellText(ticket.getAttachmentDetail().getLogicalName());
					else
						line.addArrayCellText(ticket.getDocument().getName());
	
					IconPane iconPane = gef.getIconPane();
		         	Icon keyIcon = iconPane.addIcon();
		          	keyIcon.setProperties(resource.getIcon("fileSharing.ticket"), resource.getString("fileSharing.ticket") , ticket.getUrl());
					line.addArrayCellText(keyIcon.print());
				
					String valideDate = resource.getOutputDate(ticket.getEndDate());
					ArrayCellText cell = line.addArrayCellText(valideDate);
					cell.setCompareOn(ticket.getEndDate());
					ArrayCellText cellNb = line.addArrayCellText(ticket.getNbAccess()+"/"+ticket.getNbAccessMax());
					cellNb.setCompareOn(Integer.valueOf(ticket.getNbAccess()));
					
					iconPane = gef.getIconPane();
		         	Icon updateIcon = iconPane.addIcon();
		         	Icon deleteIcon = iconPane.addIcon();
		         	String keyFile = ticket.getKeyFile();
		          	updateIcon.setProperties(resource.getIcon("fileSharing.update"), resource.getString("fileSharing.updateTicket") , "javaScript:onClick=editTicket('"+ keyFile + "')");
		         	deleteIcon.setProperties(resource.getIcon("fileSharing.delete"), resource.getString("fileSharing.deleteTicket") , "javaScript:onClick=deleteTicket('"+ keyFile + "')");
	
		        	line.addArrayCellText(updateIcon.print()+ "&nbsp;&nbsp;&nbsp;&nbsp;" + deleteIcon.print());
				}
			}
		}

			
	out.println(arrayPane.print());
  	out.println(frame.printAfter());
	out.println(window.printAfter());
  %>

</form>
  
<form name="ticketForm" action="" method="post">
	<input type="hidden" name ="KeyFile"/>
	<input type="hidden" name="FileId"/>
	<input type="hidden" name="ComponentId"/>
	<input type="hidden" name="Versioning"/>
	<input type="hidden" name="EndDate"/>
	<input type="hidden" name="NbAccessMax"/>
</form>

<form name="deleteForm" action="DeleteTicket" method="post">
	<input type="hidden" name="KeyFile"/>
</form>

</body>
</html>