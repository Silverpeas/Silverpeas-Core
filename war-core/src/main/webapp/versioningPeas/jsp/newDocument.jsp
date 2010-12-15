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
<%@ page import="com.stratelia.silverpeas.versioning.model.Document"%>
<%@ page import="com.silverpeas.publicationTemplate.*"%>
<%@ page import="com.silverpeas.form.*"%>
<%@ page errorPage="../../admin/jsp/errorpage.jsp"%>
<%@ include file="checkVersion.jsp" %>

<%
      ResourceLocator messages = new ResourceLocator("com.stratelia.silverpeas.versioningPeas.multilang.versioning", m_MainSessionCtrl.getFavoriteLanguage());
      String pleaseFill = messages.getString("pleaseFill");
      String okLabel = messages.getString("ok");
      String nokLabel = messages.getString("close");

      String name = "";
      String description = "";
      String versionType = new Integer(VersioningSessionController.WORK_VERSION).toString();
      String mimeType = "";
      File dir = null;
      int size = 0;

      String documentNameLabel = messages.getString("name");
      String descriptionLabel = messages.getString("description");
      String documentPathLabel = messages.getString("document");
      String versionTypeLabel = messages.getString("typeOfVersion");
      String[] radioButtonLabel = {messages.getString("public"), messages.getString("archive")};
      String requiredFieldLabel = messages.getString("required");

      String mandatoryField = m_context + "/util/icons/mandatoryField.gif";

      String pubId = (String) request.getAttribute("PubId");
      Form formUpdate = (Form) request.getAttribute("XMLForm");
      DataRecord data = (DataRecord) request.getAttribute("XMLData");
      String xmlFormName = (String) request.getAttribute("XMLFormName");
      PagesContext context = (PagesContext) request.getAttribute("PagesContext");
      if (context != null) {
        context.setBorderPrinted(false);
        context.setFormIndex("0");
        context.setCurrentFieldIndex("7");
      }

      Document document = null;
%>
<html>
  <head>
    <title><%=messages.getString("popupTitle")%></title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <script type="text/javascript">
      function addFile() {
    	var documentName = document.addForm.name.value;
        if ($.trim(documentName) != "" && isCorrectForm()) {
            if ($("#documentDesc").val().length > 255) {
            	var errorMsg =" <%=descriptionLabel%> : <%=attResources.getString("GML.nbCarMax")%> 255 <%= resources.getString("GML.caracteres")%>\n";
            	alert(errorMsg);
        	} else {
        		document.addForm.submit();
        	}
        } else {
          alert("<%=pleaseFill%>");
        }
      }
</script>
    <% if (formUpdate != null) {%>
    <% formUpdate.displayScripts(out, context);%>
    <% } else {%>
    <script type="text/javascript">
      function isCorrectForm()
      {
        return true;
      }
    </script>
    <% }
    out.println(gef.getLookStyleSheet());%>
  </head>
  <body class="yui-skin-sam">
    <%
          out.println(window.printBefore());
          out.println(frame.printBefore());

          Board board = gef.getBoard();
          out.println(board.printBefore());
    %>
    <form name="addForm" action="SaveNewDocument" method="POST" enctype="multipart/form-data" accept-charset="UTF-8">
      <input type="hidden" name="publicationId" value="<%=pubId%>">
      <table cellpadding="2" cellspacing="0" border="0" width="100%" >
        <tr>
          <td class="txtlibform"><%=documentNameLabel%> :</td>
          <td align="left" valign="baseline"><input type="text" id="documentName" name="name" size="50" maxlength="100" value="<%=name%>">&nbsp;<img border="0" src="<%=mandatoryField%>" width="5" height="5" alt="<%=requiredFieldLabel%>"></td>
        </tr>
        <tr>
          <td class="txtlibform"><%=descriptionLabel%> :</td>
          <td align="left" valign="baseline"><textarea id="documentDesc" name="description" rows="3" cols="50" ><%=description%></textarea></td>
        </tr>
        <tr>
          <td class="txtlibform"><%=documentPathLabel%> :</td>
          <td align="left" valign="baseline"><input type="file" name="file_upload"></td>
        </tr>
        <tr>
          <td class="txtlibform"><%=versionTypeLabel%> :</td>
          <td align="left" valign="baseline">
            <input value="0" type="radio" name="versionType"><%=radioButtonLabel[0]%><input type="radio" value="1" name="versionType" checked><%=radioButtonLabel[1]%>
          </td>
        </tr>
        <tr>
          <td colspan="2">(<img alt="<%=requiredFieldLabel%>"  border="0" src="<%=mandatoryField%>" width="5" height="5">: <%=requiredFieldLabel%>)</td>
        </tr>
      </table>
      <%
            if (formUpdate != null) {
      %>
      <br/>
      <table cellpadding="2" cellspacing="0" border="0" width="100%">
        <tr>
          <td class="intfdcolor6outline"><span class="txtlibform"><%=messages.getString("versioning.xmlForm.XtraData")%></span></td>
        </tr>
      </table>
      <%formUpdate.display(out, context, data);
            }
      %>
    </form>
    <%
          out.println(board.printAfter());
          ButtonPane buttonPane = gef.getButtonPane();
          buttonPane.addButton(gef.getFormButton(okLabel, "javascript:addFile()", false));
          buttonPane.addButton(gef.getFormButton(nokLabel, "javascript:window.close()", false));

          out.flush();
          out.println("<BR><center>");
          out.println(buttonPane.print());
          out.println("</center>");

          out.println(frame.printAfter());
          out.println(window.printAfter());
    %>

    <script type="text/javascript"  language="Javascript">
      document.addForm.name.focus();
    </script>
  </body>
</html>