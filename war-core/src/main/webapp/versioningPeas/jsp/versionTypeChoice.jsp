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

<%@ page import="com.stratelia.silverpeas.versioning.model.DocumentVersion"%>
<%@ include file="checkVersion.jsp" %>

<%@ page errorPage="../../admin/jsp/errorpage.jsp"%>
<%
				ResourceLocator messages = new ResourceLocator("com.stratelia.silverpeas.versioningPeas.multilang.versioning", m_MainSessionCtrl.getFavoriteLanguage());
				out.println(gef.getLookStyleSheet());
%>
<html>
<head>
<TITLE><%=messages.getString("popupTitle")%></TITLE>
<script language="javascript">
				function validateForm()
				{
								window.opener.document.forms[0].VersionType.value = document.frm_versiontype.VersionType[0].value;
								if (document.frm_versiontype.VersionType[1].checked)
												window.opener.document.forms[0].VersionType.value = document.frm_versiontype.VersionType[1].value;

								window.opener.document.forms[0].submit();
								window.close();
				}
</script>
</head>
<body>
<%
				browseBar.setExtraInformation(messages.getString("typeOfVersion"));
				out.println(window.printBefore());
				out.println(frame.printBefore());
				Button validateButton = (Button) gef.getFormButton(messages.getString("ok"), "javascript:onClick=validateForm();", false);
%>
<FORM NAME="frm_versiontype" Method="POST">
				<table width="100%">
								<tr>
												<td align="center>"<input type="radio" checked name="VersionType" value="<%=DocumentVersion.TYPE_PUBLIC_VERSION%>">&nbsp;<%=messages.getString("public")%></td>
												<td align="center"><input type="radio" name="VersionType" value="<%=DocumentVersion.TYPE_DEFAULT_VERSION%>">&nbsp;<%=messages.getString("archive")%></td>
								</tr>
				</table>
				<div align="center"><br>
				<%
				    ButtonPane buttonPane = gef.getButtonPane();
				    buttonPane.addButton(validateButton);
				    out.println(buttonPane.print());
				%>
				</div>
</form>
<%
				out.println(frame.printAfter());
				out.println(window.printAfter());
%>
</body>
</html>