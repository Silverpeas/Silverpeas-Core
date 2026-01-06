<%--

    Copyright (C) 2000 - 2024 Silverpeas

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
<%@ page import="org.silverpeas.core.util.WebEncodeHelper" %>
<%@ page import="java.util.*" %>
<%@ page import="org.apache.ecs.wml.U" %>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ include file="check.jsp" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%
    Board board = gef.getBoard();

	Domain 		domObject 	= (Domain)request.getAttribute("domainObject");
    String 		action 		= (String) request.getAttribute("action");
    String 		groupsPath 	= (String) request.getAttribute("groupsPath");
    Iterator<DomainProperty> 	properties	= (Iterator<DomainProperty>) request.getAttribute("properties");
    Map<String, String>	query		= (Map<String, String>) request.getAttribute("Query");
	List<UserDetail>		users		= (List<UserDetail>) request.getAttribute("Users");
	int 		nbUsersPerPage = 15;

    browseBar.setComponentName(getDomainLabel(domObject, resource), "domainContent?Iddomain="+domObject.getId());
    browseBar.setPath(groupsPath);

    if (users != null && !users.isEmpty()) {
	  operationPane.addOperation(resource.getIcon("JDP.importSelectedUsers"), resource.getString("JDP.importSelected"), "javaScript:importUsers();");
	  operationPane.addLine();
	  operationPane.addOperation(resource.getIcon("JDP.importAllUsers"), resource.getString("JDP.importAll"), "javaScript:importAll();");
	}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
<head>
<view:looknfeel withCheckFormScript="true"/>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/overlib.js"></script>
<script language="JavaScript">
function SubmitWithVerif(verifParams)
{
  let errorMsg = "";

  if (verifParams)
    {
      const loginfld = stripInitialWhitespace(document.userForm.userLogin.value);
      if (isWhitespace(loginfld))
            errorMsg = "<% out.print(resource.getString("JDP.missingFieldStart")+resource.getString("GML.login")+resource.getString("JDP.missingFieldEnd")); %>";
    }
    if (errorMsg === "")
    {
	$.progressMessage();
	setTimeout("document.userForm.submit();", 500);
    }
    else
    {
      jQuery.popup.error(errorMsg);
    }
}

function viewUser(specificId)
{
	SP_openWindow("userView?specificId="+specificId, "userWindow", "750", "500", "directories=0,menubar=0,toolbar=0,scrollbars=1,alwaysRaised");
}

function selectAll()
{
	const myForm = document.userForm;
	if (myForm.specificIds.length == null)
	{
		myForm.specificIds.click();
	}
	else
	{
		for (let i=0; i<myForm.specificIds.length; i++)
		{
			myForm.specificIds[i].click();
		}
	}
}

function doPagination(index)
{
	document.userForm.Pagination_SelectedIds.value 	= getObjects(true);
	document.userForm.Pagination_NotSelectedIds.value = getObjects(false);
	document.userForm.Pagination_Index.value 			= index;
	document.userForm.action				= "Pagination";
	document.userForm.submit();
}

function getObjects(selected)
{
  let items = "";
  const boxItems = document.userForm.specificIds;
  if (boxItems != null){
		// au moins une checkbox exist
    const nbBox = boxItems.length;
    if ( (nbBox == null) && (boxItems.checked === selected) ){
			// il n'y a qu'une checkbox non selectionn�e
			items += boxItems.value+",";
		} else{
			// search not checked boxes
			for (let i=0;i<boxItems.length ;i++ ){
				if (boxItems[i].checked === selected){
					items += boxItems[i].value+",";
				}
			}
		}
	}
	return items;
}

function importAll()
{
	document.userForm.action = "userImportAll";
	document.userForm.submit();
}

function importUsers()
{
	document.userForm.Pagination_SelectedIds.value 	= getObjects(true);
	document.userForm.Pagination_NotSelectedIds.value = getObjects(false);
	document.userForm.action = "userImport";
	document.userForm.submit();
}

function checkSubmitToSearch(ev)
{
  const touche = ev.keyCode;
  if (touche === 13)
		SubmitWithVerif(false);
}
</script>
</head>
<body  class="page_content_admin">
<%
out.println(window.printBefore());
out.println(frame.printBefore());
%>
<%
out.println(board.printBefore());
%>
<form name="userForm" action="userSearchToImport" method="POST" onSubmit="SubmitWithVerif(false);">
    <input type="hidden" name="X-ATKN" value="${requestScope['X-ATKN']}"/>
	<input type="hidden" name="Pagination_Index">
	<input type="hidden" name="Pagination_SelectedIds">
	<input type="hidden" name="Pagination_NotSelectedIds">
    <table>
    <%
	while (properties.hasNext())
	{
        DomainProperty property = properties.next();
		String label = property.getLabel();
		String ldapAttribute = property.getMapParameter();
		String value = "";
		String name = property.getName();
		String description = property.getDescription();

		if ("lastName".equalsIgnoreCase(name))
			label = resource.getString("GML.lastName");
		else if ("firstName".equalsIgnoreCase(name))
			label = resource.getString("GML.firstName");
		else if ("email".equalsIgnoreCase(name))
			label = resource.getString("GML.eMail");
		else if ("login".equalsIgnoreCase(name))
			label = resource.getString("GML.login");
		if (query != null && StringUtil.isDefined(query.get(ldapAttribute)))
			value = query.get(ldapAttribute);
		%>
		<tr>
		<th scope="row" class="txtlibform"><%=label%></th>
		<td>
			<input type="text" name="<%=ldapAttribute%>" size="50" maxlength="50" value="<%=value%>" onkeydown="checkSubmitToSearch(event)">
			&nbsp;
			<img src="<%=resource.getIcon("JDP.info")%>" border="0" onmouseover="return overlib('<%=WebEncodeHelper.javaStringToJsString(description)%>', CAPTION, '<%=WebEncodeHelper.javaStringToJsString(resource.getString("JDP.LDAPField")+" : "+ldapAttribute)%>')" onmouseout="return nd();" align="absmiddle">
		</td>
        </tr>
		<%
	}
    %>
    <tr>
		<th scope="row"><%=resource.getString("JDP.searchSyntax")%>
		<img src="<%=resource.getIcon("JDP.info")%>" border=0 onmouseover="return overlib('<%=WebEncodeHelper.javaStringToJsString(resource.getString("JDP.fieldSyntaxContent"))%>', CAPTION, '<%=WebEncodeHelper.javaStringToJsString(resource.getString("JDP.fieldSyntax"))%>')" onmouseout="return nd();" align="absmiddle">
		</th>
	</tr>
    </table>
<%
	out.println(board.printAfter());

  ButtonPane bouton = gef.getButtonPane();
  bouton.addButton((Button) gef.getFormButton(resource.getString("GML.validate"), "javascript:SubmitWithVerif(false)", false));
  bouton.addButton((Button) gef.getFormButton(resource.getString("GML.cancel"), "domainContent", false));
  out.println("<BR/>");
  out.println(bouton.print());
	if (users != null)
	{
		int	firstUserIndex = 0;
		if (request.getAttribute("FirstUserIndex") != null)
			firstUserIndex = (Integer) request.getAttribute("FirstUserIndex");

		Collection<String> selectedIds = (Collection<String>) request.getAttribute("SelectedIds");
		// initialisation de la pagination
		Pagination 	pagination 	= gef.getPagination(users.size(), nbUsersPerPage, firstUserIndex);
		List<UserDetail> 		affUsers 	= users.subList(pagination.getFirstItemIndex(),
                pagination.getLastItemIndex());
		out.println("<BR/>");

		ArrayPane arrayPane = gef.getArrayPane("usersList", "userSearchToImport?FromArray=1", request, session);

        arrayPane.addArrayColumn(resource.getString("GML.lastName"));
        arrayPane.addArrayColumn(resource.getString("GML.firstName"));
        arrayPane.addArrayColumn(resource.getString("GML.eMail"));
        arrayPane.addArrayColumn(resource.getString("GML.login"));
        ArrayColumn colOperation = arrayPane.addArrayColumn("<input type=\"checkbox\" name=\"checkAll\" onClick=\"selectAll();\">"+"&nbsp;" + resource.getString("GML.operation"));
        colOperation.setSortable(false);
        arrayPane.setTitle(users.size()+"&nbsp;"+resource.getString("JDP.usersFound"));
        arrayPane.setVisibleLineNumber(nbUsersPerPage);

        for (UserDetail user : affUsers) {
            ArrayLine line = arrayPane.addArrayLine();
            String usedCheck = "";
            if (selectedIds != null && selectedIds.contains(user.getSpecificId())) {
                usedCheck = "checked";
            }

            line.addArrayCellLink(user.getLastName(), "javaScript:viewUser('" + user.getSpecificId() + "');");
            line.addArrayCellText(user.getFirstName());
            line.addArrayCellText(user.getEmailAddress());
            line.addArrayCellText(user.getLogin());
            line.addArrayCellText("<input type=\"checkbox\" " + usedCheck + " name=\"specificIds\" value=\"" + user.getSpecificId() + "\"/>");
        }

        out.println(arrayPane.print());
			if (users.size() > nbUsersPerPage)
				{
				%>
					<table>
						<tr class=intfdcolor4><th></th><td colspan=5><%=pagination.printIndex("doPagination")%></td></tr>
					</table>
					<%
				}
	}
%>
</form>
<%
out.println(frame.printAfter());
out.println(window.printAfter());
	%>
<view:progressMessage/>
</body>
<script language=javascript>
	myForm = document.userForm;
	if (myForm.specificIds != null)
	{
      let nbChecked = 0;
      for (let i=0; i<myForm.specificIds.length; i++)
		{
			if (myForm.specificIds[i].checked)
				nbChecked++;
		}
		if (nbChecked === myForm.specificIds.length)
			myForm.checkAll.checked = true;
	}
</script>
</html>