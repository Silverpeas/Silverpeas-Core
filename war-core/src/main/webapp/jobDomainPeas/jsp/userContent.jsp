<%@ include file="check.jsp" %>
<%
    Board board = gef.getBoard();

    UserFull	userObject 		= (UserFull)request.getAttribute("userObject");
    String  	groupsPath 		= (String)request.getAttribute("groupsPath");
    boolean 	isDomainRW 		= ((Boolean)request.getAttribute("isDomainRW")).booleanValue();
    boolean 	isDomainSync 	= ((Boolean)request.getAttribute("isDomainSync")).booleanValue();
    boolean 	isUserRW 		= ((Boolean)request.getAttribute("isUserRW")).booleanValue();
    boolean 	isX509Enabled 	= ((Boolean)request.getAttribute("isX509Enabled")).booleanValue();
    boolean		isGroupManager	= ((Boolean)request.getAttribute("isOnlyGroupManager")).booleanValue();
    
    System.out.println("isUserRW = "+isUserRW);
    
    String     	thisUserId 		= userObject.getId();

    browseBar.setDomainName(resource.getString("JDP.jobDomain"));
    browseBar.setComponentName("nom du user");
   
    if (request.getAttribute("domainName") != null && request.getAttribute("domainURL") != null)
        browseBar.setComponentName(Encode.javaStringToHtmlString((String)request.getAttribute("domainName")), (String)request.getAttribute("domainURL"));

    if (groupsPath != null && groupsPath.length() > 0)
        browseBar.setPath(groupsPath);

    if (isDomainRW && isUserRW && !isGroupManager)
    {
   		operationPane.addOperation(resource.getIcon("JDP.userUpdate"),resource.getString("JDP.userUpdate"),"displayUserModify?Iduser="+thisUserId);
   		operationPane.addOperation(resource.getIcon("JDP.userDel"),resource.getString("JDP.userDel"),"javascript:ConfirmAndSend('"+resource.getString("JDP.userDelConfirm")+"','userDelete?Iduser="+thisUserId+"')");
    }
    if (isDomainSync && !isGroupManager)
    {
        operationPane.addOperation(resource.getIcon("JDP.userUpdate"),resource.getString("JDP.userUpdate"),"displayUserMS?Iduser="+thisUserId);
        operationPane.addOperation(resource.getIcon("JDP.userSynchro"),resource.getString("JDP.userSynchro"),"userSynchro?Iduser="+thisUserId);
        operationPane.addOperation(resource.getIcon("JDP.userUnsynchro"),resource.getString("JDP.userUnsynchro"),"userUnSynchro?Iduser="+thisUserId);
    }
    if (isX509Enabled && !isGroupManager)
    {
   		operationPane.addLine();
   		operationPane.addOperation(resource.getIcon("JDP.x509"), resource.getString("JDP.getX509"), "userGetP12?Iduser="+thisUserId);
    }

%>
<html>
<head>
<% out.println(gef.getLookStyleSheet()); %>
<script language="JavaScript">
function ConfirmAndSend(textToDisplay,targetURL)
{
    if (window.confirm(textToDisplay))
    {
        window.location.href = targetURL;
    }
}
</script>
</head>
<body marginheight="5" marginwidth="5" leftmargin="5" topmargin="5" bgcolor="#FFFFFF">
<%
out.println(window.printBefore());
out.println(frame.printBefore());
%>
<center>
<%
out.println(board.printBefore());
%>
<table CELLPADDING=5 CELLSPACING=0 BORDER=0 WIDTH="100%">
	<tr>
		<td class="textePetitBold"><%=resource.getString("GML.lastName") %> :</td>
		<td align=left valign="baseline"><%=Encode.javaStringToHtmlString(userObject.getLastName())%></td>
	</tr>
	<tr>
		<td class="textePetitBold"><%=resource.getString("GML.surname") %> :</td>
		<td align=left valign="baseline"><%=Encode.javaStringToHtmlString(userObject.getFirstName())%></td>
	</tr>
	<tr>
		<td class="textePetitBold"><%=resource.getString("GML.eMail") %> :</td>
		<td align=left valign="baseline"><%=Encode.javaStringToHtmlString(userObject.geteMail())%></td>
	</tr>
	<tr>
		<td class="textePetitBold"><%=resource.getString("JDP.userRights") %> :</td>
		<td align=left valign="baseline">
			<%
                if ((userObject.getAccessLevel() != null) && (userObject.getAccessLevel().equalsIgnoreCase("A")))
                    out.print(resource.getString("GML.administrateur"));
                else if ((userObject.getAccessLevel() != null) && (userObject.getAccessLevel().equalsIgnoreCase("G")))
                    out.print(resource.getString("GML.guest"));
                else if ((userObject.getAccessLevel() != null) && (userObject.getAccessLevel().equalsIgnoreCase("K")))
                    out.print(resource.getString("GML.kmmanager"));
                else if ((userObject.getAccessLevel() != null) && (userObject.getAccessLevel().equalsIgnoreCase("D")))
                    out.print(resource.getString("GML.domainManager"));
                else
                    out.print(resource.getString("GML.user"));
            %>
		</td>
	</tr>
	<tr>
		<td class="textePetitBold"><%=resource.getString("GML.login") %> :</td>
		<td align="left" valign="baseline"><%=Encode.javaStringToHtmlString(userObject.getLogin())%></td>
	</tr>
	<tr>
		<td class="textePetitBold"><%=resource.getString("JDP.silverPassword") %> :</td>
		<td align="left" valign="baseline">
			<%
               if (userObject.isPasswordAvailable() && userObject.isPasswordValid()) 
                  out.print(resource.getString("GML.yes")); 
               else 
                  out.print(resource.getString("GML.no"));
            %>
		</td>
	</tr>
	
	<% 
        	//if (isUserRW) 
        	//{
        		String[] properties = userObject.getPropertiesNames();
				String property = null;
				for (int p=0; p<properties.length; p++)
				{
					property = (String) properties[p];
					if (!property.startsWith("password"))
					{
		%>
				<tr>			
					<td class="textePetitBold">
					<%=userObject.getSpecificLabel(resource.getLanguage(), property)%> :
					</td>
					
					<td align="left" valign="baseline">
					<%
				if("STRING".equals(userObject.getPropertyType(property)) ||
					"USERID".equals(userObject.getPropertyType(property))) {
					
					out.print(Encode.javaStringToHtmlString(userObject.getValue(property)));
		
				} else if("BOOLEAN".equals(userObject.getPropertyType(property))) {
					 
					 if (userObject.getValue(property) != null && "1".equals(userObject.getValue(property))) {
					 	out.print(resource.getString("GML.yes"));
					 } else if (userObject.getValue(property) == null || "".equals(userObject.getValue(property)) || "0".equals(userObject.getValue(property))) { 
					 	out.print(resource.getString("GML.no")); 
					 }
				}
		%>
					</td>
				</tr>
		<%
					}
				}
        	//}
        %>
        
</table>
<%
out.println(board.printAfter());
%>
<br>	
</center>
<%
out.println(frame.printAfter());
out.println(window.printAfter());
%>
</body>
</html>