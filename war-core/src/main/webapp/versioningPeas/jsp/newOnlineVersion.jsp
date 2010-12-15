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

<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ page errorPage="../../admin/jsp/errorpage.jsp"%>
<%@ include file="checkVersion.jsp"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.browseBars.BrowseBar" %>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.buttonPanes.ButtonPane" %>
<%@ page import="com.silverpeas.publicationTemplate.*"%>
<%@ page import="com.silverpeas.form.*"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
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
  
  String documentId = (String) request.getAttribute("DocumentId");
  Form formUpdate = (Form) request.getAttribute("XMLForm");
  DataRecord data = (DataRecord) request.getAttribute("XMLData");
  String xmlFormName = (String) request.getAttribute("XMLFormName");
  PagesContext context = (PagesContext) request.getAttribute("PagesContext");
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
function checkIn()
{
	$.get('<%=m_context%>/AjaxVersioning', {DocId:'<%=documentId%>',Action:'Checkin',force_release:'<%=request.getParameter("force_release")%>'},
    function(data) {
      window.opener.menuCheckin('<%=documentId%>');
      window.close();
    }, "html");
}
</script>
<% if (formUpdate != null) { %>
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
      <form name="addForm" action="<c:url value="/RVersioningPeas/jsp/saveOnline" />" method="POST" enctype="multipart/form-data" accept-charset="UTF-8">
      <input type="hidden" name="radio" value="0"/>
      <input type="hidden" name="action" value="checkin"/>
      <input type="hidden" name="publicationId" value="<c:out value="${param.Id}" />" />
      <input type="hidden" name="componentId" value="<c:out value="${param.ComponentId}" />" />
      <input type="hidden" name="spaceId" value="<c:out value="${param.SpaceId}" />" />
      <input type="hidden" name="documentId" value="<c:out value="${param.documentId}" />" />
	  <input type="hidden" name="Callback" value="<c:out value="${param.Callback}" />" />
      <table cellpadding="5" cellspacing="0" border="0" width="100%">
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
      buttonPane.addButton(gef.getFormButton(nokLabel, "javascript:window.close();", false));
      buttonPane.addButton(gef.getFormButton(messages.getString("versioning.checkin.abort"),"javascript:checkIn();", false));
      out.println("<center>");
      out.println(buttonPane.print());
      out.println("</center>");
    %>
</body>
</html>