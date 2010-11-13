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

<%@page import="com.silverpeas.util.EncodeHelper"%>
<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ page import="com.stratelia.silverpeas.versioning.model.DocumentVersion"%>
<%@ page import="com.stratelia.silverpeas.versioning.util.VersioningUtil"%>
<%@ page import="com.stratelia.webactiv.util.ClientBrowserUtil"%>
<%@ include file="checkVersion.jsp" %>

<%@ page errorPage="../../admin/jsp/errorpage.jsp"%>

<script src="<%=m_context%>/versioningPeas/jsp/javaScript/dragAndDrop.js" type="text/javascript"></script>
<script src="<%=m_context%>/util/javaScript/upload_applet.js" type="text/javascript"></script>

<%
      ResourceLocator messages = new ResourceLocator("com.stratelia.silverpeas.versioningPeas.multilang.versioning", m_MainSessionCtrl.getFavoriteLanguage());

      boolean need_submit = false;
      String docId = request.getParameter("DocId");
      String flag = request.getParameter("profile");
      int user_id = Integer.parseInt(m_MainSessionCtrl.getUserId());
      if (docId == null) {
        docId = request.getParameter("DocId");
      }

      int versionType = DocumentVersion.TYPE_DEFAULT_VERSION;
      if (request.getParameter("VersionType") != null && !request.getParameter("VersionType").equals("")) {
        versionType = (new Integer(request.getParameter("VersionType"))).intValue();
      }

      String action = request.getParameter("Action");
      String comment = request.getParameter("comment");

      DocumentPK documentPK = new DocumentPK(Integer.parseInt(docId), versioningSC.getComponentId());
      Document document = versioningSC.getEditingDocument();
      if (document == null) {
        document = versioningSC.getDocument(documentPK);
        versioningSC.setEditingDocument(document);
      }
      if ("update".equals(action)) {
        versioningSC.updateDocument(document);
        document = versioningSC.getDocument(documentPK);
        versioningSC.setEditingDocument(document);
        out.println("<script>window.opener.location.href=window.opener.location.href;</script>");//window.close();
      } else if ("checkin".equals(action)) {
        document.setStatus(0);
        if (versioningSC.checkDocumentIn(documentPK, user_id, false)) {
          document = versioningSC.getDocument(documentPK);
          versioningSC.setEditingDocument(document);
          need_submit = true;
          out.println("<script>window.opener.location.href=window.opener.location.href;</script>");
        }

      } else if ("checkout".equals(action)) {
        document.setStatus(1);
        document.setLastCheckOutDate(new Date());
        versioningSC.checkDocumentOut(documentPK, user_id, new Date());
        document = versioningSC.getDocument(documentPK);
        versioningSC.setEditingDocument(document);
        need_submit = true;

        out.println("<script>window.opener.location.href=window.opener.location.href;</script>");
      } else if ("validate".equals(action)) {
        try {
          versioningSC.validateDocument(documentPK, user_id, comment, new Date());
        } catch (IllegalArgumentException e) {
          SilverTrace.error("versioning", "editDocumentJSP", "root.EX_WRONG_PARAMETERS", e);
        }
        document = versioningSC.getDocument(documentPK);
        versioningSC.setEditingDocument(document);
        out.println("<script>window.opener.location.href=window.opener.location.href;</script>");
        out.println("<script>window.close();</script>");
      } else if ("refuse".equals(action)) {
        try {
          versioningSC.refuseDocument(documentPK, user_id, comment, new Date());
        } catch (IllegalArgumentException e) {
          SilverTrace.error("versioning", "editDocumentJSP", "root.EX_WRONG_PARAMETERS", e);
        }
        document = versioningSC.getDocument(documentPK);
        versioningSC.setEditingDocument(document);
        out.println("<script>window.opener.location.href=window.opener.location.href;</script>");
        out.println("<script>window.close();</script>");
      }

      String statusLabel = messages.getString("status");//"Status";
      String mimeTypeLabel = messages.getString("type");//"Type"; // = resource.getString("XXX.mimeTypeLabel");
      String versionLabel = messages.getString("version");//"Version"; // = resource.getString("XXX.versionLabel");
      String documentNameLabel = messages.getString("name");//"Name"; // = resource.getString("XXX.documentNameLabel");
      String descriptionLabel = messages.getString("description");//"Description"; // = resource.getString("XXX.descriptionLabel");
      String creatorLabel = messages.getString("creator");//"Creator";
      String dateLabel = messages.getString("date");//"Date"; // = resource.getString("XXX.descriptionLabel");
      String commentsLabel = messages.getString("comments");//"Comments";
      String okLabel = messages.getString("ok");//"OK";
      String noOkLabel = messages.getString("close");//"OK";

      // getParameters !!!
      int iStatus = document.getStatus();
      String lock_message = "";

      List vVersions;
      List not_filtered_version = versioningSC.getDocumentVersions(document.getPk());

      if ("admin".equals(flag) || ((DocumentVersion) not_filtered_version.get(0)).getAuthorId() == user_id) {
        vVersions = not_filtered_version;
      } else {
        vVersions = versioningSC.getDocumentFilteredVersions(document.getPk(), user_id);
      }

      if (not_filtered_version.size() > 0 && ((DocumentVersion) not_filtered_version.get(not_filtered_version.size() - 1)).getStatus() == DocumentVersion.STATUS_VALIDATION_REQUIRED) {
        lock_message = messages.getString("waitingValidation");
      } else {
        lock_message = messages.getString("lockedBy");
      }

      String status = (iStatus == 0 ? messages.getString("free") : lock_message + " " + versioningSC.getUserNameByID(document.getOwnerId()) + " - " + resources.getOutputDate(document.getLastCheckOutDate()));
      String documentName = document.getName();//request.getParameter("name");
      String documentDescription = document.getDescription();//request.getParameter("name");
      String documentComments = document.getAdditionalInfo();

      // declaration of messages !!!
      String checkIn_msg = messages.getString("checkIn");//"Check In"; // = resource.getString("XXX.checkInMessage");
      String checkOut_msg = messages.getString("checkOut");//"Check Out"; // = resource.getString("XXX.checkOutMessage");
      String addVersion_msg = messages.getString("addNewVersion");//"Add a new Version"; // = resource.getString("XXX.addDocumentMessage");

      String validate_msg = messages.getString("operation.validate");
      String refuse_msg = messages.getString("operation.refuse");

      int maxLine = vVersions.size();//9;
      String[] iconsFullName = new String[maxLine];
      String[] iconsDocLabel = new String[maxLine];
      String[] versions = new String[maxLine];
      String[] dates = new String[maxLine];
      String[] filenames = new String[maxLine];
      String[] creators = new String[maxLine];
      String[] comments = new String[maxLine];
      String[] URLs = new String[maxLine];
      String[] permalinks = new String[maxLine];

      String iconsAddVersion = m_context + "/util/icons/versionAdd.gif";
      String iconsCheckIn = m_context + "/util/icons/versionUnlock.gif";
      String iconsCheckOut = m_context + "/util/icons/versionLock.gif";
      String iconValidate = m_context + "/util/icons/versionValidate.gif";
      String iconRefuse = m_context + "/util/icons/versionUnvalidate.gif";

      boolean is_user_writer = versioningSC.isWriter(document, user_id);
      boolean is_user_reader = versioningSC.isReader(document, new Integer(user_id).toString());
      boolean newVersionAllowed = false;

      int j = 0;

      DocumentVersion lastVersion = versioningSC.getLastVersion(documentPK);
      for (int i = 0; i < maxLine; i++) {
        DocumentVersion version = (DocumentVersion) vVersions.get(i);
        String versionURL = null;
        if (version.getSize() > 0 && !"dummy".equals(version.getLogicalName())) {
          iconsFullName[j] = versioningSC.getDocumentVersionIconPath(version.getPhysicalName());
          iconsDocLabel[j] = version.getMimeType();
          versions[j] = String.valueOf(version.getMajorNumber()) + "." + String.valueOf(version.getMinorNumber());//diff;
          filenames[j] = version.getLogicalName();
          creators[j] = versioningSC.getUserNameByID(version.getAuthorId());
          dates[j] = resources.getOutputDate(version.getCreationDate());
          permalinks[j] = " <a href=\"" + URLManager.getSimpleURL(URLManager.URL_VERSION, version.getPk().getId()) + "\"><img src=\"" + m_context + "/util/icons/link.gif\" border=\"0\" valign=\"absmiddle\" alt=\"" + messages.getString("versioning.CopyLink") + "\" title=\"" + messages.getString("versioning.CopyLink") + "\" target=\"_blank\"></a> ";

          String xtraData = "";
          if (StringUtil.isDefined(version.getXmlForm())) {
            String xmlURL = m_context + "/RformTemplate/jsp/View?ObjectId=" + version.getPk().getId() + "&ComponentId=" + versioningSC.getComponentId() + "&ObjectType=Versioning&XMLFormName=" + URLEncoder.encode(version.getXmlForm());
            xtraData = "<a rel=\"" + xmlURL + "\" href=\"#\" title=\"" + documentName + " " + versions[j] + "\"><img src=\"" + m_context + "/util/icons/info.gif\" border=\"0\"></a> ";
          }
          comments[j] = xtraData + version.getComments();
          versionURL = versioningSC.getDocumentVersionURL(version.getLogicalName(), version.getDocumentPK().getId(), version.getPk().getId());

          if (lastVersion.getPk().getId().equals(version.getPk().getId())) {
            if (lastVersion.isOpenOfficeCompatibleDocument() && webdavEditingEnable) {
              if (iStatus == Document.STATUS_CHECKINED || (document.getOwnerId() != user_id && lastVersion.getAuthorId() != user_id && !"admin".equals(flag))) {
                URLs[j++] = versionURL;
              } else {
                String ooUrl = java.net.URLEncoder.encode(httpServerBase + lastVersion.getWebdavUrl());
                String webdavUrl = httpServerBase + m_context + "/attachment/jsp/launch.jsp?documentUrl=" + ooUrl;
                URLs[j++] = webdavUrl; //response.encodeURL(request.getServerName() + ':' + request.getServerPort() + lastVersion.getWebdavUrl());
              }
            } else {
              URLs[j++] = versionURL;
            }
          } else {
            URLs[j++] = versionURL;
          }
        }
      }

      Board board = gef.getBoard();

      ArrayPane arrayPane = gef.getArrayPane("List", "?profile=" + flag, request, session);

// header of the array
      ArrayColumn arrayColumn_mimeType = arrayPane.addArrayColumn(mimeTypeLabel);
      arrayColumn_mimeType.setSortable(false);
      ArrayColumn arrayColumn_version = arrayPane.addArrayColumn(versionLabel);
      arrayColumn_version.setSortable(false);
      ArrayColumn arrayColumn_docName = arrayPane.addArrayColumn(documentNameLabel);
      arrayColumn_docName.setSortable(false);
      ArrayColumn arrayColumn_creatorLabel = arrayPane.addArrayColumn(creatorLabel);
      arrayColumn_creatorLabel.setSortable(false);
      ArrayColumn arrayColumn_date = arrayPane.addArrayColumn(dateLabel);
      arrayColumn_date.setSortable(false);
      ArrayColumn arrayColumn_status = arrayPane.addArrayColumn(commentsLabel);
      arrayColumn_status.setSortable(false);

      ArrayLine arrayLine = null; // declare line object of the array

// icon to add a new Document
      ArrayList users = document.getWorkList();
      boolean is_buttons_visible = false;
      DocumentVersion version = (DocumentVersion) not_filtered_version.get(not_filtered_version.size() - 1);
      boolean isOfficeDocument = version.isOfficeDocument();
      DocumentVersion first_version = (DocumentVersion) not_filtered_version.get(0);

      if (document.getTypeWorkList() == 0) {
        is_buttons_visible = true;
        if (document.getStatus() == 0) /* has writes to checkout */ {
//            if (isExist(users, user_id) || "publisher".equals(flag) || "admin".equals(flag) )
          if (is_user_writer || "publisher".equals(flag) || "admin".equals(flag)) {
            operationPane.addOperation(iconsCheckOut, checkOut_msg, "Checkout?DocId=" + docId);
          }
        } else {
          if (document.getOwnerId() == user_id || first_version.getAuthorId() == user_id || "admin".equals(flag)) {
            operationPane.addOperation(iconsCheckIn, checkIn_msg, "javascript:perfAction('checkin','" + EncodeHelper.javaStringToJsString(first_version.getLogicalName()) + "');");
            if (document.getOwnerId() == user_id) {
              operationPane.addOperation(iconsAddVersion, addVersion_msg, "AddNewVersion?documentId=" + docId);
              newVersionAllowed = true;
            }
          }
        }
      } else if (document.getTypeWorkList() == 1) {
        if (document.getStatus() == 0) /* has writes to checkout */ {
          if (is_user_writer) {
            operationPane.addOperation(iconsCheckOut, checkOut_msg, "javascript:perfAction('checkout','');");
            is_buttons_visible = true;
          }
        } else {
          if (document.getOwnerId() == user_id || first_version.getAuthorId() == user_id || "admin".equals(flag)) {
            if (is_user_writer && version.getStatus() != DocumentVersion.STATUS_VALIDATION_REQUIRED || first_version.getAuthorId() == user_id || "admin".equals(flag)) {
              operationPane.addOperation(iconsCheckIn, checkIn_msg, "javascript:perfAction('checkin','" + isOfficeDocument + "');");
              if (document.getOwnerId() == user_id && isWriter(users, user_id) && version.getStatus() != DocumentVersion.STATUS_VALIDATION_REQUIRED) {
                operationPane.addOperation(iconsAddVersion, addVersion_msg, "AddNewVersion?documentId=" + docId);
                newVersionAllowed = true;
              }
              is_buttons_visible = true;
            }

            if (document.getOwnerId() == user_id && (isValidator(users, user_id) && version.getStatus() == DocumentVersion.STATUS_VALIDATION_REQUIRED)) {
              operationPane.addOperation(iconValidate, validate_msg, "javascript:perfAction('validate','');");
              operationPane.addOperation(iconRefuse, refuse_msg, "javascript:perfAction('refuse','');");
            }
          }
        }
      } else if (document.getTypeWorkList() == 2) {
        if (document.getStatus() == 0) /* has writes to checkout */ {
          Worker user = (Worker) document.getWorkList().get(document.getCurrentWorkListOrder());
          if (user.isWriter() && user.getUserId() == user_id /*|| "admin".equals(flag)*/) {
            operationPane.addOperation(iconsCheckOut, checkOut_msg, "javascript:perfAction('checkout','');");
            is_buttons_visible = true;
          }
        } else {
          Worker user = (Worker) document.getWorkList().get(document.getCurrentWorkListOrder());
          if (user_id == user.getUserId() || first_version.getAuthorId() == user_id || "admin".equals(flag)) {
            if (user_id == user.getUserId() && (user.isWriter() /*&& version.getStatus() != DocumentVersion.STATUS_VALIDATION_REQUIRED*/) || "admin".equals(flag) || first_version.getAuthorId() == user_id) {
              operationPane.addOperation(iconsCheckIn, checkIn_msg, "javascript:perfAction('checkin','" + isOfficeDocument + "');");
              if (user_id == user.getUserId() && user.isWriter() /*&& version.getStatus() != DocumentVersion.STATUS_VALIDATION_REQUIRED*/) {
                operationPane.addOperation(iconsAddVersion, addVersion_msg, "AddNewVersion?documentId=" + docId + "&hide_radio=true");
                newVersionAllowed = true;
              }

              is_buttons_visible = true;
            }

            if (user_id == user.getUserId() && (user.isApproval())) {
              operationPane.addOperation(iconValidate, validate_msg, "javascript:perfAction('validate','');");
              operationPane.addOperation(iconRefuse, refuse_msg, "javascript:perfAction('refuse','');");
            }
          }
        }
      }

      if (request.getParameter("buttons") != null) {
        is_buttons_visible = true;
      }

      out.println(window.printBefore());

      if (flag.equals("admin") || flag.equals("publisher")) {
        TabbedPane tabbedPane = gef.getTabbedPane();

        tabbedPane.addTab(messages.getString("versions.caption"), "ViewVersions", true, true);
        if (versioningSC.tabWritersToDisplay()) {
          tabbedPane.addTab(messages.getString("writerlist.caption"), "ViewWritersList", false, true);
        }

        if (versioningSC.tabReadersToDisplay()) {
          tabbedPane.addTab(messages.getString("readerlist.caption"), "ViewReadersList", false, true);
        }

        out.println(tabbedPane.print());
      }

      out.println(frame.printBefore());
      out.println(board.printBefore());

%>
<form name="essai" method="post" action="<%=versioningSC.getDocumentVersionShowVersionsURL()%>">
  <table cellpadding="5" cellspacing="0" border="0" width="100%">
    <tr>
      <td class="txtlibform"><%=statusLabel%> :</td>
      <td align="left" valign="baseline"><%=status%></td>
    </tr>
    <tr>
      <td class="txtlibform"><%=documentNameLabel%> :</td>
      <td align="left" valign="baseline">
        <input type="text" name="name" size="80" maxlength="100" value="<%=documentName%>"/>
        <input type="hidden" name="DocId" value="<%=docId%>"/>
        <input type="hidden" name="VersionType" value="<%=versionType%>"/>
        <input type="hidden" name="Action" value=""/>
        <input type="hidden" name="from_action" value="1"/>
        <input type="hidden" name="comment"/>
        <input type="hidden" name="profile" value="<%=flag%>"/>
      </td>
    </tr>
    <tr>
      <td class="txtlibform" valign="top"><%=descriptionLabel%> :</td>
      <td align="left" valign="baseline"><textarea name="description" rows="4" cols="80"><%=documentDescription + "\n" + documentComments%></textarea></td>
    </tr>
    <!-- <tr>
        	<td class="txtlibform" valign="top"><%=commentsLabel%> :</td>
        <td align="left" valign="baseline"><textarea name="comments" rows="2" cols="80"><%=documentComments%></textarea></td>
    </tr> -->
  </table>
</form>
<center>
  <%
        ButtonPane buttonPane = gef.getButtonPane();

        if (is_buttons_visible) {
          buttonPane.addButton(gef.getFormButton(okLabel, "javascript:update();", false));
        }

        buttonPane.addButton(gef.getFormButton(noOkLabel, "javascript:window.close();", false));
        out.print(buttonPane.print());
  %>
</center>

<script type="text/javascript"  language="javacript">
  document.essai.name.focus();
</script>
<%
  if (need_submit) {
%>
<script type="text/javascript"  language="javacript">
    submitForm(1);
</script>
<%
  }

// main loop
      for (int i = 0; i < j; i++) {
        arrayLine = arrayPane.addArrayLine(); // set a new line
        // build the icon of the document mimetype
        IconPane iconPane1 = gef.getIconPane();
        Icon debIcon = iconPane1.addIcon(); // create a new icon
        debIcon.setProperties(iconsFullName[i], iconsDocLabel[i], URLs[i]);
        arrayLine.addArrayCellIconPane(iconPane1);

        String target = "_blank";
        if (URLs[i].startsWith("javaScript:")) {
          target = "_self";
        }
        arrayLine.addArrayCellLink(versions[i], URLs[i], target);
        arrayLine.addArrayCellLink(filenames[i] + permalinks[i], URLs[i], target);

        arrayLine.addArrayCellText(creators[i]);
        arrayLine.addArrayCellText(dates[i]);
        arrayLine.addArrayCellText(comments[i]);
      }

      out.println(board.printAfter());
      out.print("<br/>");
      if (dragAndDropEnable && newVersionAllowed) {
%><table width="100%" border="0" id="DropZone">
  <tr><td colspan="3" align="right">
      <a href="javascript:showDnD()" id="dNdActionLabel"><%=resources.getString("GML.DragNDropExpand")%></a>
    </td>
  </tr>
  <tr>
    <td>
      <div id="DragAndDrop" style="background-color: #CDCDCD; border: 1px solid #CDCDCD; paddding:0px; width:100%" valign="top"><img alt=""border" src="<%=m_context%>/util/icons/colorPix/1px.gif" height="2"/></div>
    </td>
    <td width="2%"><img alt="border"  src="<%=m_context%>/util/icons/colorPix/1px.gif" width="10px"/></td>
    <td>
      <div id="DragAndDropDraft" style="background-color: #CDCDCD; border: 1px solid #CDCDCD; paddding: 0px; width: 100%" valign="top"><img alt="border" src="<%=m_context%>/util/icons/colorPix/1px.gif" height="2"/></div>
    </td>
  </tr>
</table>
<%
      } //end if dragAndDropEnable

      out.println(arrayPane.print());
      out.println(frame.printAfter());
      out.println(window.printAfter());
%>
<script type="text/javascript">
  // Create the tooltips only on document load
  $(document).ready(function()
  {
    // Use the each() method to gain access to each elements attributes
    $('a[rel]').each(function()
    {
      $(this).qtip(
      {
        content: {
          // Set the text to an image HTML string with the correct src URL to the loading image you want to use
          text: '<img class="throbber" src="<%=m_context%>/util/icons/inProgress.gif" alt="Loading..." />',
          url: $(this).attr('rel'), // Use the rel attribute of each element for the url to load
          title: {
            text: '<%=messages.getString("versioning.xmlForm.ToolTip")%> \"' + $(this).attr('title') + "\"", // Give the tooltip a title using each elements text
            button: '<%=resources.getString("GML.close")%>' // Show a close link in the title
          }
        },
        position: {
          corner: {
            target: 'leftMiddle', // Position the tooltip above the link
            tooltip: 'rightMiddle'
          },
          adjust: {
            screen: true // Keep the tooltip on-screen at all times
          }
        },
        show: {
          when: 'click',
          solo: true // Only show one tooltip at a time
        },
        hide: 'unfocus',
        style: {
          tip: true, // Apply a speech bubble tip to the tooltip at the designated tooltip corner
          border: {
            width: 0,
            radius: 4
          },
          name: 'light', // Use the default light style
          width: 350 // Set the tooltip width
        }
      })
    });
  });
</script>
