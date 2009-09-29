<%--

    Copyright (C) 2000 - 2009 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have recieved a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://repository.silverpeas.com/legal/licensing"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@ page errorPage="../../admin/jsp/errorpage.jsp"%>
<%@ include file="checkVersion.jsp"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.browseBars.BrowseBar" %>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.buttonPanes.ButtonPane" %>
<%@ page import="com.silverpeas.publicationTemplate.*"%>
<%@ page import="com.silverpeas.form.*"%>
<%@ taglib uri="/WEB-INF/c.tld" prefix="c"%>
<%@ taglib uri="/WEB-INF/fmt.tld" prefix="fmt"%>
<%@ taglib uri="/WEB-INF/viewGenerator.tld" prefix="view"%>
<%
  // declaration of labels !!!
  ResourceLocator messages = new ResourceLocator("com.stratelia.silverpeas.versioningPeas.multilang.versioning", m_MainSessionCtrl.getFavoriteLanguage());
  request.setAttribute("resources", messages);

  String versionTypeLabel = messages.getString("typeOfVersion");
  String commentsLabel = messages.getString("comments");
  String okLabel = messages.getString("ok");
  String nokLabel = messages.getString("cancel");
  String[] radioButtonLabel = { messages.getString("public"), messages.getString("archive") };
  pageContext.setAttribute("radios", radioButtonLabel);
  
  Form 				formUpdate 	= (Form) request.getAttribute("XMLForm");
  DataRecord 			data 		= (DataRecord) request.getAttribute("XMLData"); 
  String				xmlFormName = (String) request.getAttribute("XMLFormName");
  PagesContext		context		= (PagesContext) request.getAttribute("PagesContext");
  if (context != null)
  {
  	context.setBorderPrinted(false);
  	context.setFormIndex("0");
  	context.setCurrentFieldIndex("3");
  }
%>

<html>
<title></title>
<view:looknfeel />
<head>
<script type="text/javascript">
function addNewVersion()
{
	if (isCorrectForm())
		document.addForm.submit();
}
</script>
<% if (formUpdate != null) { %>
	<script type="text/javascript" src="<%=m_context%>/wysiwyg/jsp/FCKeditor/fckeditor.js"></script>
	<% formUpdate.displayScripts(out, context); %>
<% } else { %>
	<script type="text/javascript">
		function isCorrectForm()
		{
			return true;
		}
	</script>
<% } %>
</head>
<body class="yui-skin-sam">
<fmt:setLocale value="${sessionScope['SilverSessionController'].favoriteLanguage}" />
<view:setBundle bundle="${requestScope.resources.resourceBundle}" />
      <form name="addForm" action="<c:url value="/RVersioningPeas/jsp/saveOnline" />" method="POST" enctype="multipart/form-data">
      <input type="hidden" name="radio" value="0"/>
      <input type="hidden" name="action" value="checkin"/>
      <input type="hidden" name="publicationId" value="<c:out value="${param.Id}" />" />
      <input type="hidden" name="componentId" value="<c:out value="${param.ComponentId}" />" />
      <input type="hidden" name="spaceId" value="<c:out value="${param.SpaceId}" />" />
      <input type="hidden" name="documentId" value="<c:out value="${param.documentId}" />" />
      <table CELLPADDING="5" CELLSPACING="0" BORDER="0" WIDTH="100%">
        <tr>
          <td class="txtlibform"><%=versionTypeLabel%> :</td>
          <td align="left" valign="baseline">
            <c:forEach items="${pageScope.radios}" var="radio" varStatus="status">
              <input type="radio" name="versionType" <c:if test="${status.index == 0}">checked</c:if> onClick="javascript:document.addForm.radio.value=<c:out value="${status.index}" />;" /><c:out value="${radio}" />
            </c:forEach>
          </td>
        </tr>
        <tr>
          <td class="txtlibform" valign="top"><%=commentsLabel%> :</td>
          <td align=left valign="baseline"><textarea name="comments" rows="5" cols="60"></textarea></td>
        </tr>
      </table>
	<br/>
		<%if (formUpdate != null)
		{
			%>
			<table CELLPADDING="2" CELLSPACING="0" BORDER="0" WIDTH="100%">
				<tr><td class="intfdcolor6outline"><span class="txtlibform"><%=messages.getString("versioning.xmlForm.XtraData")%></span></td></tr>
			</table>
			<%formUpdate.display(out, context, data); 
		}%>
      </form>
    <%
      ButtonPane buttonPane = gef.getButtonPane();
      buttonPane.addButton(gef.getFormButton(okLabel, "javascript:addNewVersion();", false));
      buttonPane.addButton(gef.getFormButton(nokLabel,"javascript:parent.document.forms[0].action.value='checkin';parent.document.forms[0].submit();", false));
      out.println("<center>");
      out.println(buttonPane.print());
      out.println("</center>");
    %>
</body>
</html>