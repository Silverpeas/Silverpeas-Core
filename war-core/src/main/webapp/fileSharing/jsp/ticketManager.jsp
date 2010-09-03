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
	// r�cup�ration des param�tres :
	TicketDetail 	ticket		= (TicketDetail) request.getAttribute("Ticket");
	
	String 		keyFile			= "";
	int 		fileId 			= 0;
	String 		fileName		= "";
	String 		id				= "";
	String 		componentId		= "";
	boolean 	versioning		= false;
	String 		creatorId		= "";
	String 		creatorName		= "";
	String 		creationDate	= resource.getOutputDate(new Date());
	String 		updateId		= "";
	String 		updateName		= "";
	String 		updateDate		= null;
	String 		endDate			= resource.getOutputDate(new Date());
	int 		nbAccessMax		= 1;
	int 		nbAccess		= 0;
	Collection 	downloads		= null;
	String		action 			= "CreateTicket";
	String		type			= null;		//versioning or not
	
	// dans le cas d'une mise � jour, r�cup�ration des donn�es :
	if (ticket != null)
	{
		keyFile			= ticket.getKeyFile();
		fileId 			= ticket.getFileId();
		if (ticket.getAttachmentDetail() != null)
			fileName		= ticket.getAttachmentDetail().getLogicalName();
		else
			fileName		= ticket.getDocument().getName();
		componentId		= ticket.getComponentId();
		versioning		= ticket.isVersioning();
		creatorId		= ticket.getCreatorId();
		creatorName		= ticket.getCreatorName();
		creationDate	= resource.getOutputDate(ticket.getCreationDate());
		updateId		= ticket.getUpdateId();
		updateName		= ticket.getUpdateName();
		updateDate		= resource.getOutputDate(ticket.getUpdateDate());
		endDate			= resource.getOutputDate(ticket.getEndDate());
		nbAccessMax		= ticket.getNbAccessMax();
		nbAccess		= ticket.getNbAccess();
		downloads 		= ticket.getDownloads();
		action 			= "UpdateTicket";
	}
	if (action.equals("CreateTicket"))
	{
		// en cr�ation, r�cup�ration des donn�es du fichier depuis attachment
		id				= (String) request.getAttribute("FileId");
		componentId		= (String) request.getAttribute("ComponentId");
		fileId 			= Integer.parseInt(id);
		fileName 		= (String) request.getAttribute("FileName");
		creatorName		= (String) request.getAttribute("CreatorName");
		versioning		= ((Boolean) request.getAttribute("Versioning")).booleanValue();
	}
	
	// d�claration des boutons
	Button validateButton;
	if (action.equals("CreateTicket"))
		validateButton = (Button) gef.getFormButton(resource.getString("GML.validate"), "javascript:onClick=sendDataCreate();", false);
	else
		validateButton = (Button) gef.getFormButton(resource.getString("GML.validate"), "javascript:onClick=sendData();", false);
    Button cancelButton = (Button) gef.getFormButton(resource.getString("GML.cancel"), "javascript:window.close()", false);
	
%>

<html>
<head>

<%
	out.println(gef.getLookStyleSheet());
%>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>	
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/dateUtils.js"></script>	
<script language="javascript">
function sendData() 
{
	if (isCorrectForm()) 
	{
   		window.opener.document.ticketForm.action = "<%=action%>";
   		window.opener.document.ticketForm.KeyFile.value = document.ticketForm.KeyFile.value;
   		window.opener.document.ticketForm.EndDate.value = document.ticketForm.EndDate.value;
   		window.opener.document.ticketForm.NbAccessMax.value = document.ticketForm.NbAccessMax.value;
   		
   		window.opener.document.ticketForm.submit();
  		window.close();		
	}
}

function sendDataCreate() 
{
	if (isCorrectForm()) 
	{
   		document.ticketForm.action = "<%=action%>";
   		document.ticketForm.submit();
	}
}
		
function isCorrectForm() {

     var errorMsg 			= "";
     var errorNb 			= 0;
     var nb 				= document.ticketForm.NbAccessMax.value;
     var endDate 			= document.ticketForm.EndDate.value;
     var re 				= /(\d\d\/\d\d\/\d\d\d\d)/i;

   	if (nb > 100 || nb < 1) {
     		errorMsg+="  - <%=resource.getString("GML.theField")%> '<%=resource.getString("fileSharing.nbAccessMax")%>' <%=resource.getString("fileSharing.maxValue")%>\n";
           	errorNb++;
     }
     if (isWhitespace(endDate)) {
           errorMsg +="  - <%=resource.getString("GML.theField")%> '<%=resource.getString("fileSharing.endDate")%>' <%=resource.getString("GML.MustBeFilled")%>\n";
           errorNb++;
     } else {
     	   if (isDateOK(document.ticketForm.EndDate)==false)
     	   {
   	   			errorMsg+="  - <%=resource.getString("GML.theField")%> '<%=resource.getString("fileSharing.endDate")%>' <%=resource.getString("GML.MustContainsCorrectDate")%>\n";
               	errorNb++;
     	   } 
     }
     switch(errorNb) {
        case 0 :
            result = true;
            break;
        case 1 :
            errorMsg = "<%=resource.getString("GML.ThisFormContains")%> 1 <%=resource.getString("GML.error")%> : \n" + errorMsg;
            window.alert(errorMsg);
            result = false;
            break;
        default :
            errorMsg = "<%=resource.getString("GML.ThisFormContains")%> " + errorNb + " <%=resource.getString("GML.errors")%> :\n" + errorMsg;
            window.alert(errorMsg);
            result = false;
            break;
     }
     return result;
}

function isDateOK(input)
{
	var re 		= /(\d\d\/\d\d\/\d\d\d\d)/i;
    var date	= input.value;
    
    if (!isWhitespace(date)) {
           if (date.replace(re, "OK") != "OK") {
               return false;
           } else {
           		var year 	= extractYear(date, '<%=resource.getLanguage()%>'); 
    			var month	= extractMonth(date, '<%=resource.getLanguage()%>');
    			var day 	= extractDay(date, '<%=resource.getLanguage()%>');
                if (isCorrectDate(year, month, day)==false) {
                 return false;
               }
           }
     }
     return true;
}
</script>		
</head>
<body>
<%
	if (action.equals("CreateTicket"))
		browseBar.setComponentName(resource.getString("fileSharing.tickets") + " > " + resource.getString("fileSharing.createTicket"));
	else
		browseBar.setComponentName(resource.getString("fileSharing.tickets") + " > " + resource.getString("fileSharing.updateTicket"));
		
	Board board	= gef.getBoard();
	
	out.println(window.printBefore());
    out.println(frame.printBefore());
    out.println(board.printBefore());
%>

<FORM Name="ticketForm" method="post" action="">
<table CELLPADDING=5 WIDTH="100%">
	<tr>
		<td class="txtlibform"><%=resource.getString("fileSharing.nameFile")%> :</td>
		<td><%=fileName%></td>
	</tr>
	<tr>
		<% if (action.equals("UpdateTicket")) 
  		{ %>	
			<td class="txtlibform"><%=resource.getString("fileSharing.keyFile")%> :</td>
			<td><a href="<%=ticket.getUrl()%>"><%=keyFile%></a></td>
		<%} %>
	</tr>
	<input type="hidden" name="ComponentId" value="<%=componentId%>">
	<input type="hidden" name="KeyFile" value="<%=keyFile%>">
	<input type="hidden" name="FileId" size="60" maxlength="150" value="<%=fileId%>">
	<input type="hidden" name="Versioning" value="<%=versioning%>">
		
	<tr>
		<td class="txtlibform"><%=resource.getString("fileSharing.creationDate")%> :</td>
		<TD><%=creationDate%>&nbsp;<span class="txtlibform"><%=resource.getString("fileSharing.by")%></span>&nbsp;<%=creatorName%></TD>
	</tr>
	<% if (updateDate != null && updateId != null) { %>
		<tr>
			<td class="txtlibform"><%=resource.getString("fileSharing.updateDate")%> :</td>
			<TD><%=updateDate%>&nbsp;<span class="txtlibform"><%=resource.getString("fileSharing.by")%></span>&nbsp;<%=updateName%></TD>
		</tr>
	<% } %>
	<tr>
		<td class="txtlibform"><%=resource.getString("fileSharing.endDate")%> :</td>
		<TD><input type="text" class="dateToPick" name="EndDate" size="12" maxlength="10" value="<%=endDate%>"/><span class="txtnote">(<%=resource.getString("GML.dateFormatExemple")%>)
			<img border="0" src=<%=resource.getIcon("fileSharing.obligatoire")%> width="5" height="5">
		</TD>
	</tr>
	<tr>
		<td class="txtlibform"><%=resource.getString("fileSharing.nbAccessMax")%> :</td>
		<TD>
			<input type="text" name="NbAccessMax" size="3" maxlength="3" value="<%=nbAccessMax%>" >
			<img border="0" src=<%=resource.getIcon("fileSharing.obligatoire")%> width="5" height="5">
		</TD>
	</tr>
  	<tr>
  		<td colspan="2">( <img border="0" src=<%=resource.getIcon("fileSharing.obligatoire")%> width="5" height="5"> : Obligatoire )</td>
  	</tr>
  	
  	<% if (action.equals("UpdateTicket")) 
  	{ %>
	  	<tr><td colspan="2">
	  	<%
		// liste des t�l�chargements d�j� effectu�s
		
		ArrayPane arrayPane = gef.getArrayPane("downloadList", "EditTicket?KeyFile="+keyFile, request, session);
		arrayPane.addArrayColumn(resource.getString("fileSharing.downloadDate"));
		arrayPane.addArrayColumn(resource.getString("fileSharing.IP"));
		
		if ((downloads != null) && (downloads.size() != 0)) 
		{
			Iterator it = (Iterator) downloads.iterator();
			while (it.hasNext()) 
			{
				ArrayLine line = arrayPane.addArrayLine();
				DownloadDetail download = (DownloadDetail) it.next();
						
				String downloadDate = resource.getOutputDateAndHour(download.getDownloadDate());
				line.addArrayCellText(downloadDate);
				line.addArrayCellText(download.getUserIP());		
			}
		}
				
		out.println(arrayPane.print()); 
		%>
		</td></tr>
	<% } %>
</table>	
</form>

<% 
	out.println(board.printAfter());
	ButtonPane buttonPane = gef.getButtonPane();
    buttonPane.addButton(validateButton);
    buttonPane.addButton(cancelButton);
	out.println("<BR><center>"+buttonPane.print()+"</center><BR>");
 	out.println(frame.printAfter());
	out.println(window.printAfter());
%>
</body>
</html>