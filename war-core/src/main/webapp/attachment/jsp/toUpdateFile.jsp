<%--

    Copyright (C) 2000 - 2012 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://www.silverpeas.org/legal/licensing"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>

<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<%@ include file="checkAttachment.jsp"%>

<% request.setAttribute("attachmentBundle", new ResourceLocator(
      "com.stratelia.webactiv.util.attachment.multilang.attachment", language).getResourceBundle());%>
<view:setBundle bundle="${requestScope.attachmentBundle}" />
<fmt:setLocale value="{sessionScope.SilverSessionController.favoriteLanguage}" />

<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
    <title><fmt:message key="GML.popupTitle"/></title>
    <view:looknfeel />
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <script type="text/javascript" src='<c:url value="/util/javaScript/animation.js" />'></script>
    <script type="text/javascript" src='<c:url value="/util/javaScript/checkForm.js" />'></script>
    <script type="text/javascript" src='<c:url value="/util/javaScript/i18n.js" />'></script>
    <script type="text/javascript">
      var attachmentMandatory = false;

      function update()
      {
        if (attachmentMandatory && isWhitespace(document.updateForm.file_upload.value))
        {
          alert("<%=messages.getString("nomVide")%>");
        }
        else
        {
          document.updateForm.submit();
        }
      }
      function showTranslation(lang)
      {
        try
        {
          document.getElementById('fileName').innerHTML = eval('name_'+lang);
        } catch (e) {
          document.getElementById('fileName').innerHTML = "";
          attachmentMandatory = true;
        }
        showFieldTranslation('fileTitle', 'title_'+lang);
        showFieldTranslation('fileDesc', 'desc_'+lang);
      }

      function removeTranslation()
      {
        document.updateForm.submit();
      }
    </script>
    <script type="text/javascript">
      function displayAttachment(attachment) {
        $('#fileName').html('');
        $('#fileName').html(attachment.fileName);
        $('#fileTitle').val(attachment.title);
        $('#fileDesc').val(attachment.description);
      }
      
      function loadAttachment(id) {
        translationsUrl = '<c:url value="/services/documents/${sessionScope.Silverpeas_Attachment_ComponentId}/document/"/>' + id + '/translations';
        $.ajax({
          url: translationsUrl,
          type: "GET",
          contentType: "application/json",
          dataType: "json",
          cache: false,
          success: function(data) {
            alert(data);
            $.each(data, function(index, attachment) {
              displayAttachment(attachment);
            });
          }
        });
      }
      
      $(document).ready(function() {
        loadAttachment('<c:out value="${param.IdAttachment}" />');       
      });
    </script>
    <%@ page errorPage="../../admin/jsp/errorpage.jsp"%>

    <%@ page import="org.silverpeas.attachment.AttachmentServiceFactory"%>
    <%@ page import="org.silverpeas.attachment.model.SimpleDocument" %>
    <%@ page import="org.silverpeas.attachment.model.SimpleDocumentPK" %>
  </head>
  <body>  
    <view:frame>
      <view:board>
        <div id="attachment">
          <fieldset>
            <form name="updateForm" action="<c:url value="/attachment/jsp/updateFile.jsp" />" method="post" enctype="multipart/form-data" accept-charset="UTF-8">
              <label for="fileName"><%=messages.getString("GML.file")%></label>
              <span id="fileName"></span>
              <input type="hidden" name="IdAttachment" id="attachmentId"/>
              <label for="file_upload"><fmt:message key="fichierJoint"/></label>
              <input type="file" name="file_upload" size="60" id="file_upload" />
              <label for="fileTitle"><fmt:message key="Title"/></label>
              <input type="text" name="Title" size="60" id="fileTitle" />
              <label for="fileDesc"><fmt:message key="GML.description" /></label>
              <textarea name="Description" cols="60" rows="3" id="fileDesc"></textarea>
            </form>
          </fieldset>
        </div>        
      </view:board>
    </view:frame>
  </body>
</html>