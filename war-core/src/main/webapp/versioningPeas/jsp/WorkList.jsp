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

<%@ page errorPage="../../admin/jsp/errorpage.jsp"%>
<%@ include file="checkVersion.jsp" %>
<%@ page import="com.stratelia.webactiv.beans.admin.ProfileInst"%>
<%@ page import="com.stratelia.webactiv.beans.admin.Group"%>

<%!
    protected String styleToString(int typeWorkList, ResourceLocator rl)  {
       if (rl == null) {
           return Integer.toString(typeWorkList);
       }
       if (typeWorkList == 0) {
           return rl.getString("writerlist.simple");
       } else if (typeWorkList == 1) {
           return rl.getString("writerlist.approval");
       } else if (typeWorkList == 2) {
           return rl.getString("writerlist.ordered");
       }
       return "Unknown";
    }
%>
<%
		final String WRITERS_LIST_SIMPLE = "0";
		final String WRITERS_LIST_APPROUVAL = "1";
		final String WRITERS_LIST_ORDERED = "2";

		String profile = (String) request.getAttribute("Profile");
		List workers = (List) request.getAttribute("Workers");
    if (workers == null) {
      workers = new ArrayList();
    }
    
    Document document = versioningSC.getEditingDocument();
    String listType = new Integer(document.getCurrentWorkListOrder()).toString();
    
  	if (!StringUtil.isDefined(listType))
  		listType = String.valueOf(document.getTypeWorkList());
		boolean displayGroups = (!listType.equals(WRITERS_LIST_ORDERED)); 
	
		List groups = null;

		String message = "";
    if (request.getAttribute("Message") != null)
			message = (String) request.getAttribute("Message");

    ResourceLocator messages = new ResourceLocator("com.stratelia.silverpeas.versioningPeas.multilang.versioning", m_MainSessionCtrl.getFavoriteLanguage());
    String okLabel = messages.getString("ok");
    String nokLabel = messages.getString("close");

    
    int workersCount = workers.size();

    String disable_html = "";
    boolean disable_ctrl = false;
    
    List versions = versioningSC.getDocumentVersions(document.getPk());
    DocumentVersion currentVersion = (DocumentVersion)versions.get(versions.size()-1);
    DocumentVersion firstVersion = (DocumentVersion)versions.get(0);

    if (document.getStatus() == Document.STATUS_CHECKOUTED || (currentVersion.getMinorNumber() != 0 && versions.size() > 1))
    {
        disable_html = " disabled";
        disable_ctrl = true;
    }
	
		out.println(gef.getLookStyleSheet());
		if ("admin".equals(profile) || "publisher".equals(profile))
		{
			operationPane.addOperation(userPanelIcon, messages.getString("writerlist.ToUserPanel"), "javaScript:onClick=goToUserPanel()");
		  operationPane.addOperation(userPanelDeleteIcon, resources.getString("GML.delete"), "DeleteWriterProfile");
	    operationPane.addOperation(saveListIcon, messages.getString("versioning.SaveListWriters"), "SaveList?Role=writer&From=ViewWritersList");
		}
		out.println(window.printBefore());

		if (StringUtil.isDefined(message))
	    out.println(message);

    TabbedPane tabbedPane = gef.getTabbedPane();
    tabbedPane.addTab(messages.getString("versions.caption") , "ViewVersions", false, true);
    String fileRightsAccessMode = versioningSC.getFileRightsMode();
    if (versioningSC.tabWritersToDisplay())
        tabbedPane.addTab(messages.getString("writerlist.caption") , "ViewWritersList", true, true);
    
    if (versioningSC.tabReadersToDisplay())
		 	  tabbedPane.addTab(messages.getString("readerlist.caption") , "ViewReadersList", false, true);

    out.println(tabbedPane.print());
  	out.println(frame.printBefore());

		Board board = gef.getBoard();
    out.println(board.printBefore());
%>
<script language="Javascript" src="workList.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script language="javascript">
  function goToUserPanel()
	{
		windowName = "userPanelWindow";
		windowParams = "directories=0,menubar=0,toolbar=0,alwaysRaised,scrollbars,resizable";
		userPanelWindow = SP_openUserPanel("SelectUsersGroupsProfileInstance?Role=writer&ListType=<%=listType%>", windowName, windowParams);
	}

	function changeValidator(setTypeId, setType)
	{
		document.workForm.action = "ChangeValidator";
		document.workForm.VV.value = setTypeId;
		document.workForm.SetType.value = setType;
    document.workForm.submit();
	}
	
	function changeListType()
	{
			document.workForm.action = "ChangeListType";
	    document.workForm.ListType.value = document.workForm.List.options[document.workForm.List.selectedIndex].value;
	    document.workForm.submit();
	}

	function saveListType()
	{
    document.workForm.action = "SaveListType";
    document.workForm.submit();
	}
</script>
<%
	String validator_text = "";
	String user_text = messages.getString("user");
	if (!listType.equals(WRITERS_LIST_SIMPLE))
	    validator_text = messages.getString("validator");
%>
<form name="workForm" action="ViewWritersList" method="POST">
		<TABLE width="80%" align="center" border="0" cellPadding="0" cellSpacing="0">

	    <TR><TD align="left" width="180">
	    	<SELECT <%=(disable_ctrl)?"DISABLED ": "name=List" %> onchange="javascript:changeListType()">
		    <%
		    for (int i=0; i<3; i++)
		    {
		        if (String.valueOf(i).equals(listType))
		        {
		            out.println("<OPTION selected value=\""+ i+"\">" + styleToString(i, messages) + "</OPTION>");
		        }
		        else
		            out.println("<OPTION value=\""+i+"\">" + styleToString(i, messages) + "</OPTION>");
		    }
		    %>
	    	</SELECT>
	    	</TD><td></td>
	    </TR>
		<TR>
			<TD colspan="4" align="center"><BR/><BR/></TD>
		</TR>
		<TR>
			<TD colspan="4" align="center" class="intfdcolor" height="1"><img src="<%=hLineSrc%>"></TD>
		</TR>
		<TR>
			<TD align="center" class="txttitrecol"><%=resources.getString("GML.type")%></TD>
			<TD align="center" class="txttitrecol"><%=resources.getString("GML.name")%></TD>
    	<% if (WRITERS_LIST_ORDERED.equals(listType)) { %>
	        <TD class="txttitrecol"><INPUT type="checkbox" name="allV" onClick="selectAllValidators(this.checked);"><%=validator_text%></TD>
       <% } else { %>
          <TD class="txttitrecol"><b><%=validator_text%></b></TD>
       <% } %>
		</TR>
		<TR>
			<TD colspan="3" align="center" class="intfdcolor" height="1"><img src="<%=hLineSrc%>"></TD>
		</TR>
<%

    String script = "";
    String firstValidator = "";

    for ( int i=0; i<workersCount; i++ )
    {
        Worker worker = (Worker) workers.get(i);
        String v_role = (worker.isApproval())?" checked ":"";
				if (worker.getType().equals("G") && !WRITERS_LIST_ORDERED.equals(listType))
				{
	        String name =  versioningSC.getGroupNameById(worker.getId());
					out.println("<TR>");
					out.println("<TD align=\"center\"><IMG SRC=\""+groupSrc+"\"></TD>");
					out.println("<TD align=\"center\">"+name+"</TD>");
					if (listType.equals(WRITERS_LIST_APPROUVAL))
					{
						out.println("<TD align=\"left\"><input type=\"radio\" name=\"V\" onclick=\"changeValidator('"+worker.getId()+"','G')\"" + v_role+disable_html+"\"></TD>");
					}
					else
						out.println("<TD></td>");
					out.println("</TR>");
				}
				if (worker.getType().equals("U"))
				{
	        String name =  versioningSC.getUserNameByID(worker.getUserId());
	        if (WRITERS_LIST_SIMPLE.equals(listType)) { %>
	            <tr>
	              <TD align="center"><IMG SRC="<%=userSrc%>"></TD>
	              <TD align="center"><%=name%></TD>
								<td></td>
	            </tr> <%
	        }
	        else if (WRITERS_LIST_APPROUVAL.equals(listType))
	        {
	            if (!"".equals(firstValidator) )
	            {
	                v_role = "";
	            }
	            else if (!"".equals(v_role))
	            	firstValidator = String.valueOf(i);
							%>
	            <TR>
	            		<TD align="center"><IMG SRC="<%=userSrc%>"></TD>
	                <TD align="center"><%=name%></TD>
	                <TD><INPUT type="radio" name="V" onclick="changeValidator('<%=worker.getId()%>','U')" <%=v_role+disable_html%>></TD>
	            </TR>
	            <%
		      }
	        else if (WRITERS_LIST_ORDERED.equals(listType) )
	        { %>
	            <TR>
	           		<TD align="center"><img src="<%=m_context%>/util/icons/duplicate.gif" onclick ="addUser(<%=i%>)"><IMG SRC="<%=userSrc%>"></TD>
	            	<TD align="center">
										<table border=0 cellspacing="0" cellpadding="0" width="100%"><tr>
													<td height="8" width="15">
		                 			<% if (i!=0 && !disable_ctrl) { %>
		                 				<a href="javascript:moveUp(<%=i%>)"><img border=0 src="icons/arrowUp.gif"></a>
		                 			<% } %>
													</td>
													<td width="15">
			                 	<% if (i!=(workers.size()-1) && !disable_ctrl) { %>
			                 	 	<a href="javascript:moveDown(<%=i%>)"><img border=0 src="icons/arrowDown.gif"></a>
		                 			<% } %>
													</td>
												<td><%=name%></td>
											</tr>
										</table>
	              </TD>
								<TD><INPUT type="checkbox" name="chv<%=i%>" <%=v_role%> value="1"></TD>
	            </TR>
	            <%
	        }
				}
    }
    %>
    </table>
	<input type="hidden" name="lines" value="<%=workers.size()%>">
	<input type="hidden" name="VV" value="<%=firstValidator%>">
	<input type="hidden" name="SetType">
	<input type="hidden" name="up" value="-1">
	<input type="hidden" name="down" value="-1">
	<input type="hidden" name="add" value="-1">
	<input type="hidden" name="delete_index" value="-1">
	<input type="hidden" name="from_action" value="3">
	<input type="hidden" name="validate" value="">
	<input type="hidden" name="ListType" value="<%=listType%>">
</form>

<%
	out.println(board.printAfter());
	if (WRITERS_LIST_ORDERED.equals(listType))
	{
		ButtonPane buttonPane = gef.getButtonPane();
		buttonPane.addButton(gef.getFormButton(okLabel,"javascript:saveListType();", false));
		out.println("<br><center>"+buttonPane.print()+"</center>");
	}
	out.println(frame.printAfter());
	out.println(window.printAfter());
%>