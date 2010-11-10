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
	Board board = gef.getBoard(); 

    Domain  domObject 			= (Domain)request.getAttribute("domainObject");
    UserDetail theUser 			= (UserDetail)request.getAttribute("theUser");
    boolean isDomainRW 			= ((Boolean)request.getAttribute("isDomainRW")).booleanValue();
    boolean isDomainSync 		= ((Boolean)request.getAttribute("isDomainSync")).booleanValue();
    boolean isUserRW 			= ((Boolean)request.getAttribute("isUserRW")).booleanValue();
    boolean isGroupManager		= ((Boolean)request.getAttribute("isOnlyGroupManager")).booleanValue();
    boolean isUserAddingAllowed = ((Boolean)request.getAttribute("isUserAddingAllowedForGroupManager")).booleanValue();

	boolean isDomainSql 	= "com.stratelia.silverpeas.domains.sqldriver.SQLDriver".equals(domObject.getDriverClassName());
		
    browseBar.setDomainName(resource.getString("JDP.jobDomain"));
    browseBar.setComponentName(getDomainLabel(domObject, resource), "domainContent?Iddomain="+domObject.getId());
	
    // Domain operations
	operationPane.addOperation(resource.getIcon("JDP.userPanelAccess"),resource.getString("JDP.userPanelAccess"),"displaySelectUserOrGroup");
	if (theUser.isAccessAdmin())
	{
	    if (!domObject.getId().equals("-1"))
	    {
		    operationPane.addLine();
	    	if(isDomainSql) {
		        operationPane.addOperation(resource.getIcon("JDP.domainSqlUpdate"),resource.getString("JDP.domainSQLUpdate"),"displayDomainSQLModify");
		        operationPane.addOperation(resource.getIcon("JDP.domainSqlDel"),resource.getString("JDP.domainSQLDel"),"javascript:ConfirmAndSend('"+resource.getString("JDP.domainDelConfirm")+"','domainSQLDelete')");
		    } else {
		        operationPane.addOperation(resource.getIcon("JDP.domainUpdate"),resource.getString("JDP.domainUpdate"),"displayDomainModify");
		        if (!domObject.getId().equals("0"))
		        	operationPane.addOperation(resource.getIcon("JDP.domainDel"),resource.getString("JDP.domainDel"),"javascript:ConfirmAndSend('"+resource.getString("JDP.domainDelConfirm")+"','domainDelete')");
		    }
	    }
	}
    if (isDomainRW)
    {        
        if (!isGroupManager)
        {
        	operationPane.addLine();
        	
        	operationPane.addOperation(resource.getIcon("JDP.groupAdd"),resource.getString("JDP.groupAdd"),"displayGroupCreate");
        	
        	if (isUserRW)
            {
                // User operations
                operationPane.addOperation(resource.getIcon("JDP.userCreate"),resource.getString("JDP.userCreate"),"displayUserCreate");
                operationPane.addOperation(resource.getIcon("JDP.importCsv"),resource.getString("JDP.csvImport"),"displayUsersCsvImport");
            }
        }
        else
        {
        	if (isUserRW && isUserAddingAllowed)
        	{
        		operationPane.addLine();
        		
                //User operations
                operationPane.addOperation(resource.getIcon("JDP.userCreate"),resource.getString("JDP.userCreate"),"displayUserCreate");
                operationPane.addOperation(resource.getIcon("JDP.importCsv"),resource.getString("JDP.csvImport"),"displayUsersCsvImport");
            }
        }
    }
    if (isDomainSync)
    {        
        if (!isGroupManager)
        {
        	operationPane.addLine();
        	
        	// Domain operations
        	operationPane.addOperation(resource.getIcon("JDP.domainSynchro"),resource.getString("JDP.domainSynchro"),"displayDomainSynchro");
        	
        	//User operations
            operationPane.addOperation(resource.getIcon("JDP.userImport"),resource.getString("JDP.userImport"),"displayUserImport");

            // Group operations
            operationPane.addOperation(resource.getIcon("JDP.groupImport"),resource.getString("JDP.groupImport"),"displayGroupImport");
        }
        else
        {
        	if (isUserAddingAllowed)
        	{
        		operationPane.addLine();
        		
        		//User operations
                operationPane.addOperation(resource.getIcon("JDP.userImport"),resource.getString("JDP.userImport"),"displayUserImport");
        	}
        }
    } else if(isDomainSql) {
    	ResourceLocator propDomain = new ResourceLocator(domObject.getPropFileName(), "");
    	boolean synchroUser = SilverpeasSettings.readBoolean(propDomain, "ExternalSynchro", false);
    	if(synchroUser) {
    	    operationPane.addLine();
		
			// Domain operations
    	    operationPane.addOperation(resource.getIcon("JDP.domainSqlSynchro"),resource.getString("JDP.domainSynchro"),"javascript:DomainSQLSynchro()");
    	}
    }
%>

<HTML>
<HEAD>
<%
out.println(gef.getLookStyleSheet());
%>
<script language="JavaScript">
function ConfirmAndSend(textToDisplay,targetURL)
{
    if (window.confirm(textToDisplay))
    {
        window.location.href = targetURL;
    }
}

function DomainSQLSynchro(){
	top.IdleFrame.SP_openWindow('<%=m_context %>/RjobDomainPeas/jsp/displayDynamicSynchroReport?IdTraceLevel=<%=Integer.toString(SynchroReport.TRACE_LEVEL_DEBUG)%>', 'SynchroReport', '750', '550', 'menubar=yes,scrollbars=yes,statusbar=yes,resizable=yes');
	window.location.href = "domainSQLSynchro";
}

</script>
</HEAD>
<BODY>
<% 
out.println(window.printBefore());
out.println(frame.printBefore());
%>
<center>
<%
out.println(board.printBefore());
%>
<table CELLPADDING="5" CELLSPACING="0" BORDER="0" WIDTH="100%">
	<tr valign="baseline">
		<td><img src="<% if(isDomainSql) {
							out.print(resource.getIcon("JDP.domainSqlIcone"));
						 } else {	
						 	out.print(resource.getIcon("JDP.domainicone"));
						 }
					%>" alt="<%=resource.getString("JDP.domaine")%>" title="<%=resource.getString("JDP.domaine")%>"></td>
		<td class="textePetitBold" nowrap><%=resource.getString("GML.nom") %> :</td>
		<td align=left valign="baseline" width="100%"><%=getDomainLabel(domObject, resource)%></td>
	</tr>
	<tr>
	    <td></td>
		<td class="textePetitBold" nowrap><%=resource.getString("GML.description") %> :</td>
		<td align=left valign="baseline" width="100%"><%=EncodeHelper.javaStringToHtmlString(domObject.getDescription())%></td>
	</tr>
	<% if (!"-1".equals(domObject.getId())) { %>
	<tr>
	    <td></td>
		<td class="textePetitBold" nowrap><%=resource.getString("JDP.class") %> :</td>
		<td align=left valign="baseline" width="100%"><%=EncodeHelper.javaStringToHtmlString(domObject.getDriverClassName())%></td>
	</tr>
	<tr>
		<td></td>
		<td class="textePetitBold" nowrap><%=resource.getString("JDP.properties") %> :</td>
		<td align=left valign="baseline" width="100%"><%=EncodeHelper.javaStringToHtmlString(domObject.getPropFileName())%></td>
	</tr>
	<tr>
		<td></td>
		<td class="textePetitBold" nowrap><%=resource.getString("JDP.serverAuthentification") %> :</td>
		<td align=left valign="baseline" width="100%"><%=EncodeHelper.javaStringToHtmlString(domObject.getAuthenticationServer())%></td>
	</tr>
	<tr>
		<td></td>
		<td class="textePetitBold" nowrap><%=resource.getString("JDP.silverpeasServerURL") %> :</td>
		<td align=left valign="baseline" width="100%"><%=EncodeHelper.javaStringToHtmlString(domObject.getSilverpeasServerURL())%></td>
	</tr>
	<% } %>
</table>
<%
out.println(board.printAfter());
%>
<br>
<%
  ArrayPane arrayPane = gef.getArrayPane("groupe", "domainContent.jsp", request, session);
  Group[] subGroups = (Group[])request.getAttribute("subGroups");

  arrayPane.setVisibleLineNumber(JobDomainSettings.m_GroupsByPage);
  //arrayPane.setTitle(resource.getString("JDP.groups"));

  arrayPane.addArrayColumn("&nbsp;");
  arrayPane.addArrayColumn(resource.getString("GML.name"));
  arrayPane.addArrayColumn(resource.getString("GML.users"));
  arrayPane.addArrayColumn(resource.getString("GML.description"));
  arrayPane.setSortable(false);

  if (subGroups != null)
  {
	  Group group = null;
      for(int i=0; i<subGroups.length; i++){
          //cr�ation des ligne de l'arrayPane
    	  group = subGroups[i];
    	  if (group != null)
    	  {
	          ArrayLine arrayLine = arrayPane.addArrayLine();
	          IconPane iconPane1 = gef.getIconPane();
	          Icon groupIcon = iconPane1.addIcon();
	          if (group.isSynchronized())
	        	  groupIcon.setProperties(resource.getIcon("JDP.groupSynchronized"), resource.getString("GML.groupe"), "");
	          else
	        	  groupIcon.setProperties(resource.getIcon("JDP.group"), resource.getString("GML.groupe"), "");
	          arrayLine.addArrayCellIconPane(iconPane1);
	          arrayLine.addArrayCellLink(EncodeHelper.javaStringToHtmlString(group.getName()), (String)request.getAttribute("myComponentURL")+"groupContent?Idgroup="+group.getId());
	          arrayLine.addArrayCellText(group.getNbUsers());
	          arrayLine.addArrayCellText(EncodeHelper.javaStringToHtmlString(group.getDescription()));
    	  }
      }
  } 	
  out.println(arrayPane.print());
%>
<br>

<%
  ArrayPane arrayPaneUser = gef.getArrayPane("users", "domainContent.jsp", request, session);
  String[][] subUsers = (String[][])request.getAttribute("subUsers");

  arrayPaneUser.setVisibleLineNumber(JobDomainSettings.m_UsersByPage);
  //arrayPaneUser.setTitle(resource.getString("GML.users"));

  arrayPaneUser.addArrayColumn("&nbsp;");
  arrayPaneUser.addArrayColumn(resource.getString("GML.lastName"));
  arrayPaneUser.addArrayColumn(resource.getString("GML.surname"));
  arrayPaneUser.setSortable(false);

  if (subUsers != null)
  {
      for(int i=0; i<subUsers.length; i++){
          //cr�ation des ligne de l'arrayPane
          ArrayLine arrayLineUser = arrayPaneUser.addArrayLine();
          IconPane iconPane1User = gef.getIconPane();
          Icon userIcon = iconPane1User.addIcon();
          userIcon.setProperties(resource.getIcon("JDP.user"), resource.getString("GML.user"), "");
          arrayLineUser.addArrayCellIconPane(iconPane1User);
          arrayLineUser.addArrayCellLink(subUsers[i][1], (String)request.getAttribute("myComponentURL") + "userContent?Iduser=" + subUsers[i][0]);
          arrayLineUser.addArrayCellText(subUsers[i][2]);
        }
  } 	
  out.println(arrayPaneUser.print());
%>

</center>
<% 
out.println(frame.printAfter());
out.println(window.printAfter());
%>
</BODY>
</HTML>
