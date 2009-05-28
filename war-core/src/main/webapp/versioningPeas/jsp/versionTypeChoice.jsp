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