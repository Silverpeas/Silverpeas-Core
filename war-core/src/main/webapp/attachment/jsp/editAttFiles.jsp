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

<%@ page isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ page errorPage="../../admin/jsp/errorpage.jsp"%>
<%@ page import="com.silverpeas.util.i18n.I18NHelper"%>
<%@ page import="com.stratelia.webactiv.util.ClientBrowserUtil"%>
<%@ include file="checkAttachment.jsp"%>
<%
      String sURI = request.getRequestURI();
      String sRequestURL = request.getRequestURL().toString();
      String m_sAbsolute = sRequestURL.substring(0, sRequestURL.length()
              - request.getRequestURI().length());

      ResourceLocator settings = new ResourceLocator(
              "com.stratelia.webactiv.util.attachment.Attachment", "");
      ResourceLocator generalSettings = GeneralPropertiesManager.getGeneralResourceLocator();

      ResourceLocator uploadSettings = new ResourceLocator("com.stratelia.webactiv.util.uploads.uploadSettings", "");
      String maximumFileSize = uploadSettings.getString("MaximumFileSize", "10000000");

      String httpServerBase = generalSettings.getString("httpServerBase", m_sAbsolute);

      boolean webdavEditingEnable = m_MainSessionCtrl.getPersonalization().getWebdavEditingStatus()
              && settings.getBoolean("OnlineEditingEnable", false);
      boolean dragAndDropEnable = m_MainSessionCtrl.getPersonalization().getDragAndDropStatus()
              && settings.getBoolean("DragAndDropEnable", false);

      //initialisation des variables
      String id = request.getParameter("Id");
      String componentId = request.getParameter("ComponentId");
      String context = request.getParameter("Context");
      String url = request.getParameter("Url");
      String sIndexIt = request.getParameter("IndexIt"); //indexIt can be 0 or 1 or notdefined (used only by kmelia actually)
      String checkOutStatus = request.getParameter("CheckOutStatus");
      String contentLanguage = request.getParameter("Language");
      String xmlForm = request.getParameter("XMLFormName");

      session.setAttribute("Silverpeas_Attachment_ObjectId", id);
      session.setAttribute("Silverpeas_Attachment_ComponentId", componentId);
      session.setAttribute("Silverpeas_Attachment_Context", context);

      if (!StringUtil.isDefined(contentLanguage)) {
        contentLanguage = null;
      }

      String profile = request.getParameter("Profile");
      if (!StringUtil.isDefined(profile)) {
        profile = "user";
      }

      session.setAttribute("Silverpeas_Attachment_Profile", profile);

      boolean originWysiwyg = false;
      if (request.getParameter("OriginWysiwyg") != null) {
        originWysiwyg = Boolean.parseBoolean(request.getParameter("OriginWysiwyg"));
      }

      boolean indexIt = !"0".equals(sIndexIt);
      session.setAttribute("Silverpeas_Attachment_IndexIt", new Boolean(indexIt));

      boolean openUrl = false;
      if (request.getParameter("OpenUrl") != null) {
        openUrl = Boolean.parseBoolean(request.getParameter("OpenUrl"));
      }

      String dNdVisible = request.getParameter("DNDVisible");

      //recuperation des fichiers attaches a un evenement
      //create foreignKey with componentId and customer id
      //use AttachmentPK to build the foreign key of customer object.
      AttachmentPK foreignKey = new AttachmentPK(id, componentId);

      Vector vectAttachment = AttachmentController.searchAttachmentByPKAndContext(foreignKey, context);
      Iterator itAttachment = vectAttachment.iterator();

      Window window = gef.getWindow();
      Board board = gef.getBoard();
%>

<script type="text/javascript" src="<%=m_Context%>/util/javaScript/animation.js"></script>
<script type="text/javascript" src="<%=m_Context%>/attachment/jsp/javaScript/dragAndDrop.js"></script>
<script type="text/javascript" src="<%=m_Context%>/util/javaScript/upload_applet.js"></script>
<script type="text/javascript"  language='Javascript'>
  function checkoutOfficeFile(attachmentId)
  {
    document.attachmentForm.action = "<%=m_Context%>/attachment/jsp/checkOut.jsp";
    document.attachmentForm.IdAttachment.value = attachmentId;
    document.attachmentForm.submit();
  }

  function checkinOfficeFile(attachmentId, fileName)
  {
    document.attachmentForm.action = "<%=m_Context%>/attachment/jsp/checkIn.jsp";
    document.attachmentForm.IdAttachment.value = attachmentId;
    document.attachmentForm.submit();
  }

  function checkinOpenOfficeFile(attachmentId, fileName) {
    if(confirm('<%=messages.getString("confirm.checkin.message")%>')) {
      document.attachmentForm.update_attachment.value='true';
    }
    document.attachmentForm.action = "<%=m_Context%>/attachment/jsp/checkIn.jsp";
    document.attachmentForm.IdAttachment.value = attachmentId;
    document.attachmentForm.submit();
  }
  function AddAttachment()
  {
  <%
              String winAddHeight = "240";
              if (I18NHelper.isI18N) {
                winAddHeight = "270";
              }
  %>
            SP_openWindow("<%=m_Context%>/attachment/jsp/addAttFiles.jsp?Id=<%=id%>&ComponentId=<%=componentId%>&Context=<%=context%>&IndexIt=<%=indexIt%>&Url=<%=URLEncoder.encode(url)%>", "test", "600", "<%=winAddHeight%>","scrollbars=no, resizable, alwaysRaised");
          }

          function UpAttachment(id)
          {
            document.attachmentForm.action = "<%=m_Context%>/attachment/jsp/moveUp.jsp";
            document.attachmentForm.IdAttachment.value = id;
            document.attachmentForm.submit();;
          }

          function DownAttachment(id)
          {
            document.attachmentForm.action = "<%=m_Context%>/attachment/jsp/moveDown.jsp";
            document.attachmentForm.IdAttachment.value = id;
            document.attachmentForm.submit();;
          }

  <% if (StringUtil.isDefined(xmlForm)) {%>
      function EditXmlForm(id, lang)
      {
        SP_openWindow("<%=m_Context%>/RformTemplate/jsp/Edit?ObjectId="+id+"&ObjectLanguage="+lang+"&ComponentId=<%=componentId%>&IndexIt=<%=indexIt%>&ObjectType=Attachment&XMLFormName=<%=URLEncoder.encode(xmlForm)%>&Url=<%=URLEncoder.encode(url)%>", "test", "600", "400","scrollbars=yes, resizable, alwaysRaised");
      }
  <% }%>

      function updateAttachment(id)
      {
  <%
              String winHeight = "220";
              if (I18NHelper.isI18N) {
                winHeight = "240";
              }
  %>
            var url = "<%=m_Context%>/attachment/jsp/toUpdateFile.jsp?IdAttachment="+id+"&Id=<%=id%>&ComponentId=<%=componentId%>&Context=<%=context%>&IndexIt=<%=indexIt%>&Url=<%=URLEncoder.encode(url)%>";
            if (dNdVisible)
              url += "&DNDVisible=true";
            SP_openWindow(url, "test", "600", "<%=winHeight%>","scrollbars=no, resizable, alwaysRaised");
          }

          function uploadCompleted(s)
          {
            reloadPage();
          }

          function reloadPage()
		  {
			location.href="<%=m_Context%><%=url%>";
		  }

          function SelectFile( fileUrl )
          {
            window.opener.SetUrl( fileUrl ) ;
            window.close() ;
          }

          function closeMessage() {
			$("#attachmentModalDialog").dialog("close");
          }

          function closeMessage(force) {
          	document.attachmentForm.force_release.value=force;
          	$("#attachmentModalDialog").dialog("close");
          }

          var attachmentId 	= "-1";
          var attachmentName	= "";

          function deleteAttachment()
          {
            document.attachmentForm.IdAttachment.value = id;
            document.attachmentForm.submit();
          }

          function DeleteConfirmAttachment(id)
          {
            var url = '<%=m_Context%>/attachment/jsp/suppressionDialog.jsp?ComponentId=<%=componentId%>&Id=<%=id%>&Url=<%=url%>&IdAttachment='+id+'&IndexIt=<%=indexIt%>';
            $("#attachmentModalDialog").dialog("open").load(url);
          }

          function ShareAttachment(id)
          {
            var url = "<%=m_Context%>/RfileSharing/jsp/NewTicket?FileId="+id+"&ComponentId=<%=componentId%>";
            SP_openWindow(url, "NewTicket", "700", "300","scrollbars=no, resizable, alwaysRaised");
          }

          function displayWarning()
          {
        	  var url = "<%=m_Context%>/attachment/jsp/warning_locked.jsp?profile=<%=profile%>";
              $("#attachmentModalDialog").dialog("open").load(url);
          }

          function removeAttachment(attachmentId)
          {
            var sLanguages = "";
            var boxItems = document.removeForm.languagesToDelete;
            if (boxItems != null){
              //at least one checkbox exists
              var nbBox = boxItems.length;
              //alert("nbBox = "+nbBox);
              if ( (nbBox == null) && (boxItems.checked) ){
                //there's only once checkbox
                sLanguages += boxItems.value+",";
              } else{
                for (i=0;i<boxItems.length ;i++ ){
                  if (boxItems[i].checked){
                    sLanguages += boxItems[i].value+",";
                  }
                }
              }
            }

            //alert("sLanguages = "+sLanguages);

            $.get('<%=m_Context%>/Attachment', { id:attachmentId,Action:'Delete',languagesToDelete:sLanguages},
            function(data){
              data = data.replace(/^\s+/g,'').replace(/\s+$/g,'');
              if (data == "attachmentRemoved")
              {
                $('#attachment_'+attachmentId).remove();
              }
              else
              {
                if (data == "translationsRemoved")
                {
                  document.location.reload();
                }
              }
              closeMessage();
            });
          }

          $(document).ready(function(){
              $("#attachmentModalDialog").dialog({
            	  autoOpen: false,
                  modal: true,
                  title: "<%=attResources.getString("attachment.dialog.delete")%>",
                  height: 'auto',
                  width: 400});
            });
</script>
<CENTER>
  <%
        if ("1".equals(checkOutStatus)) {
          out.println(board.printBefore());
          out.println("<BR><table border=\"0\" width=\"100%\"><tr><td align=\"center\"><b>");
          out.println(messages.getString("attachment.SameFileNameAlreadyCheckout"));
          out.println("</b></td></tr></table><BR>");
          out.println(board.printAfter());
          out.println("<BR>");
        }
  %> <%
        out.println(board.printBefore());
  %>
  <table border="0" width="100%">
    <% if (dragAndDropEnable) {%>
    <tr>
      <td align="right">
        <a href="javascript:showHideDragDrop('<%=httpServerBase + m_Context%>/DragAndDrop/drop?UserId=<%=userId%>&ComponentId=<%=componentId%>&PubId=<%=id%>&IndexIt=<%=indexIt%>&Context=<%=context%>','<%=httpServerBase + m_Context%>/upload/explanation_<%=language%>.html','<%=attResources.getString("GML.applet.dnd.alt")%>','<%=maximumFileSize%>','<%=m_Context%>','<%=attResources.getString("GML.DragNDropExpand")%>','<%=attResources.getString("GML.DragNDropCollapse")%>')" id="dNdActionLabel"><%=attResources.getString("GML.DragNDropExpand")%></a>
        <div id="DragAndDrop" style="background-color: #CDCDCD; border: 1px solid #CDCDCD; paddding: 0px" align="top"></div>
      </td>
    </tr>
    <% } //end if dragAndDropEnable %>
    <tr>
      <td><!--formulaire de gestion des fichiers joints -->
      	<form name="attachmentForm" action="<%=m_Context%>/attachment/jsp/removeFile.jsp" method="post">
      		<input type="hidden" name="Id" value="<%=id%>" />
            <input type="hidden" name="ComponentId" value="<%=componentId%>" />
            <input type="hidden" name="Context" value="<%=context%>" />
            <input type="hidden" name="Url" value="<%=url%>" />
            <input type="hidden" name="IndexIt" value="<%=indexIt%>" />
            <input type="hidden" name="IdAttachment" value="" />
            <input type="hidden" name="DocumentId"/>
            <input type="hidden" name="PubId" value="<%=id%>" />
            <input type="hidden" name="UserId" value="<%=userId%>" />
            <input type="hidden" name="FileLanguage" value="<%=contentLanguage%>" />
            <input type="hidden" name="update_attachment" value="false" />
            <input type="hidden" name="force_release" value="false" />
        <table border="0" cellspacing="3" cellpadding="0" width="100%">
            <tr>
              <td colspan="8" align="center" class="intfdcolor" height="1"><img src="<%=noColorPix%>" alt=""/></td>
            </tr>
            <tr>
              <td align="center"><b><%=messages.getString("type")%></b></td>
              <td align="left"><b><%=attResources.getString("GML.file")%></b></td>
              <td align="left"><b><%=messages.getString("Title")%></b></td>
              <td align="left"><b><%=attResources.getString("GML.description")%></b></td>
              <td align="left"><b><%=attResources.getString("GML.size")%></b></td>
              <td align="left"><b><%=attResources.getString("uploadDate")%></b></td>
              <td align="center"><b><%=attResources.getString("GML.operations")%></b></td>
            </tr>
            <tr>
              <td colspan="8" align="center" class="intfdcolor" height="1"><img src="<%=noColorPix%>" alt=""/></td>
            </tr>
            <%
                  String nameWritten;
                  String lastDirContext = "";
                  String htmlisation = "true";
                  String zip = "true";
                  String zipIcone = URLManager.getApplicationURL()
                          + "/util/icons/fileType/gif.gif";
                  boolean isFirst = true;
                  AttachmentDetail attachmentDetail = null;

                  while (itAttachment.hasNext()) {
                    attachmentDetail = (AttachmentDetail) (itAttachment.next());
                    String urlAttachment = request.getContextPath() + attachmentDetail.getAttachmentURL(contentLanguage);
                    String onlineURL = attachmentDetail.getOnlineURL(contentLanguage);
                    String logicalName = attachmentDetail.getLogicalName(contentLanguage);
                    String attachmentId = attachmentDetail.getPK().getId();
%>
            <tr id="attachment_<%=attachmentDetail.getPK().getId()%>">
              <td class="odd" align="center">
                <%
                            if (attachmentDetail.isReadOnly()
                                    && attachmentDetail.isOpenOfficeCompatible(contentLanguage)
                                    && webdavEditingEnable
                                    && (userId.equals(attachmentDetail.getWorkerId()) || profile.equals("admin"))) {
                              String ooUrl = httpServerBase;
                              pageContext.setAttribute("httpServerBase", httpServerBase + m_Context);
                              pageContext.setAttribute("ooo_url", ooUrl
                                      + attachmentDetail.getWebdavUrl(contentLanguage));
                %>
                <c:url var="webdavUrl" value="${pageScope.httpServerBase}/attachment/jsp/launch.jsp">
                  <c:param name="documentUrl" value="${pageScope.ooo_url}" />
                </c:url>
                <a href="<c:out value="${webdavUrl}"/>" id="webdav"><img src="<%=attachmentDetail.getAttachmentIcon(contentLanguage)%>" border="0" alt=""/></a>
              </td>
              <%
                        } else {
              %>
            <a id="other" href="<%=urlAttachment%>" target="_blank"><img src="<%=attachmentDetail.getAttachmentIcon(contentLanguage)%>" border="0" alt=""/></a>
            </td>
            <%
                        }
            %>
            <td class="odd" align="left">
              <%
                          if (attachmentDetail.isAttachmentOffset(lastDirContext)) {
                            out.println("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
                          } else if (attachmentDetail.getAttachmentGroup() == AttachmentDetail.GROUP_DIR) {
                            lastDirContext = attachmentDetail.getContext();
                          } else {
                            lastDirContext = "";
                          }
                          if (originWysiwyg) {
              %> <a href="javascript:SelectFile('<%=Encode.javaStringToJsString(urlAttachment)%>');"><%=attachmentDetail.getLogicalName(contentLanguage)%></a> <%
                          } else {
                            if (attachmentDetail.isReadOnly()
                                    && attachmentDetail.isOpenOfficeCompatible(contentLanguage)
                                    && webdavEditingEnable
                                    && (userId.equals(attachmentDetail.getWorkerId()) || profile.equals("admin"))) {
              %>
              <a href="<c:out value="${webdavUrl}"/>" id="webdav_name"><%=logicalName%></a> <%
                       } else {
              %> <a href="<%=urlAttachment%>" target="_blank"><%=logicalName%></a> <%
                            }
                          }
                          if (attachmentDetail.isReadOnly()) {
                            out.println("<br>(" + messages.getString("readOnly"));
                            String displayedName = "?????";
                            if (attachmentDetail.getWorkerId() != null) {
                              UserDetail worker = AttachmentController.getUserDetail(attachmentDetail.getWorkerId());
                              if (worker != null) {
                                displayedName = worker.getDisplayedName();
                              }
                            }

                            out.print(displayedName + " ");
                            out.print(messages.getString("at") + " "
                                    + attResources.getOutputDate(attachmentDetail.getReservationDate()));
                            if (StringUtil.isDefined(attResources.getOutputDate(attachmentDetail.getExpiryDate()))) {
                              out.print(" " + messages.getString("until") + " "
                                      + attResources.getOutputDate(attachmentDetail.getExpiryDate()));
                            }
                            out.println(")");
                            out.print("<br>");
                          }
              %>
            </td>
            <td class="odd" align="left">
              <%
                          String title = attachmentDetail.getTitle(contentLanguage);
                          if (title != null && title.length() > 0) {
                            out.println(title);
                          } else {
                            out.println("&nbsp;");
                          }
              %>
            </td>
            <td class="odd" align="center">
              <%
                          String info = attachmentDetail.getInfo(contentLanguage);
                          if (info != null && info.length() > 0) {
                            IconPane descriptionIP = gef.getIconPane();
                            Icon descIcon = descriptionIP.addIcon();
                            descIcon.setProperties(m_Context + "/util/icons/info.gif", info);
                            out.println(descriptionIP.print());
                          } else {
                            out.println("&nbsp;");
                          }
              %>
            </td>
            <td class="odd" align="left"><%=attachmentDetail.getAttachmentFileSize(contentLanguage)%></td>
              <td class="odd" align="left"><%=DateUtil.getOutputDate(attachmentDetail.getCreationDate(contentLanguage), language)%></td>
            <td class="odd" align="right">
              <%
                          IconPane iconPane = gef.getIconPane();
                          if (!attachmentDetail.isReadOnly()) {
                            //Checkout allowed
                            Icon checkoutIcon = iconPane.addIcon();
                            checkoutIcon.setProperties(
                                    m_Context + "/util/icons/checkoutFile.gif", messages.getString("checkOut"),
                                    "javascript:onClick=checkoutOfficeFile(" + attachmentId + ")");
                          } else if (attachmentDetail.isReadOnly()
                                  && attachmentDetail.isOpenOfficeCompatible(contentLanguage)
                                  && webdavEditingEnable
                                  && (userId.equals(attachmentDetail.getWorkerId()) || profile.equals("admin"))) {
                            Icon checkinIcon = iconPane.addIcon();
                            checkinIcon.setProperties(m_Context + "/util/icons/checkinFile.gif",
                                    messages.getString("checkIn"),
                                    "javascript:onClick=checkinOpenOfficeFile('" + attachmentId
                                    + "','" + EncodeHelper.javaStringToJsString(logicalName) + "');");
                          } else if (attachmentDetail.isReadOnly()
                                  && (userId.equals(attachmentDetail.getWorkerId()) || profile.equals("admin"))) {
                            Icon checkinIcon = iconPane.addIcon();
                            checkinIcon.setProperties(m_Context + "/util/icons/checkinFile.gif",
                                    messages.getString("checkIn"),
                                    "javascript:onClick=checkinOfficeFile('" + attachmentId + "');");
                          }

                          Icon updateIcon = iconPane.addIcon();
                          Icon deleteIcon = iconPane.addIcon();
                          Icon shareIcon = iconPane.addIcon();

                          if (attachmentDetail.isReadOnly()) {
                            if (userId.equals(attachmentDetail.getWorkerId())) {
                              updateIcon.setProperties(m_Context + "/util/icons/update.gif",
                                      attResources.getString("GML.modify"),
                                      "javascript:onClick=updateAttachment('" + attachmentId + "');");
                              deleteIcon.setProperties(ArrayPnoColorPix, "", "");
                              shareIcon.setProperties(ArrayPnoColorPix, "", "");
                            } else {
                              updateIcon.setProperties(ArrayPnoColorPix, "", "");
                              deleteIcon.setProperties(ArrayPnoColorPix, "", "");
                              shareIcon.setProperties(ArrayPnoColorPix, "", "");
                            }
                          } else {
                            updateIcon.setProperties(m_Context + "/util/icons/update.gif",
                                    attResources.getString("GML.modify"),
                                    "javascript:onClick=updateAttachment('" + attachmentId + "')");
                            deleteIcon.setProperties(m_Context + "/util/icons/delete.gif", attResources.getString("GML.delete"),
                                    "javascript:onClick=DeleteConfirmAttachment('"+ attachmentId + "')");

                            if (isFileSharingEnable(m_MainSessionCtrl, componentId) && "admin".equalsIgnoreCase(profile)) {
                              shareIcon.setProperties(m_Context + "/util/icons/webLink.gif", messages.getString("attachment.share"),
                                      "javascript:onClick=ShareAttachment('" + attachmentId + "')");
                            } else {
                              shareIcon.setProperties(ArrayPnoColorPix, "", "");
                            }
                          }

                          if (StringUtil.isDefined(xmlForm)) {
                            Icon xmlIcon = iconPane.addIcon();
                            xmlIcon.setProperties(m_Context + "/util/icons/add.gif",
                                    messages.getString("attachment.xmlForm.Edit"), "javascript:onClick=EditXmlForm('"
                                    + attachmentId + "','" + contentLanguage + "')");
                          }

                          Icon downIcon = iconPane.addIcon();
                          if (itAttachment.hasNext()) {
                            downIcon.setProperties(m_Context + "/util/icons/arrow/arrowDown.gif",
                                    messages.getString("Down"), "javascript:onClick=DownAttachment('" + attachmentId + "')");
                          } else {
                            downIcon.setProperties(ArrayPnoColorPix, "", "");
                          }

                          Icon upIcon = iconPane.addIcon();
                          if (isFirst) {
                            upIcon.setProperties(ArrayPnoColorPix, "", "");
                          } else {
                            upIcon.setProperties(m_Context + "/util/icons/arrow/arrowUp.gif",
                                    messages.getString("Up"), "javascript:onClick=UpAttachment('"
                                    + attachmentId + "')");
                          }
                          out.println(iconPane.print());
                          isFirst = false;
              %>
            </td>
            </tr>
            <%
                  }
            %>
          <tr>
            <td colspan="8" align="center" class="intfdcolor" height="1"><img src="<%=noColorPix%>" alt=""/></td>
          </tr>
        </table>
        </form>
      </td>
    </tr>
  </table>
  <%
        out.println(board.printAfter());
  %> <br>
  <%
        ButtonPane buttonPane2 = gef.getButtonPane();
        buttonPane2.addButton((Button) gef.getFormButton(attResources.getString("GML.add"), "javascript:AddAttachment()", false));
        out.println(buttonPane2.print());
  %>
</CENTER>
<%
      if ("true".equalsIgnoreCase(dNdVisible)) {
%>
<script language="JavaScript">
  showHideDragDrop();
</script>
<%              }
%>
<%
      String warning = request.getParameter("warning");
      if ("locked".equalsIgnoreCase(warning)) {%>
<script type="text/javascript" >
  setTimeout("displayWarning();", 500);
</script>
<% }%>
<div id="attachmentModalDialog" style="display: none"/>