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

<%@page import="com.silverpeas.util.web.servlet.FileUploadUtil"%>
<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ page errorPage="../../admin/jsp/errorpage.jsp"%>
<%@ include file="checkAttachment.jsp"%>
<%@ page import="com.stratelia.webactiv.util.attachment.model.AttachmentDetailI18N"%>
<%@ page import="com.silverpeas.util.i18n.I18NHelper"%>

<%
      String componentId = (String) session.getAttribute("Silverpeas_Attachment_ComponentId");
      String context = (String) session.getAttribute("Silverpeas_Attachment_Context");
      long size = 0;
      String type = "";
      String mimeType = "";
      String logicalName = "";
      String physicalName = "";

      boolean indexIt = ((Boolean) session.getAttribute("Silverpeas_Attachment_IndexIt")).booleanValue();

      if (!StringUtil.isDefined(request.getCharacterEncoding())) {
        request.setCharacterEncoding("UTF-8");
      }
      List items = FileUploadUtil.parseRequest(request);

      String title = getParameterValue(items, "Title", request.getCharacterEncoding());
      String description = getParameterValue(items, "Description", request.getCharacterEncoding());
      String attachmentId = getParameterValue(items, "IdAttachment", request.getCharacterEncoding());


      AttachmentPK atPK = new AttachmentPK(attachmentId, componentId);
      AttachmentDetail ad = AttachmentController.searchAttachmentByPK(atPK);
      //Just to retrieve i18n infos
      AttachmentDetail dummy = new AttachmentDetail();
      I18NHelper.setI18NInfo(dummy, items);

      FileItem file = getUploadedFile(items, "file_upload");
      if (file != null && StringUtil.isDefined(file.getName())) {
        logicalName = file.getName();

        if (runOnUnix()) {
          logicalName = logicalName.replace('\\', File.separatorChar);
        }

        logicalName = logicalName.substring(logicalName.lastIndexOf(File.separator) + 1, logicalName.length());
        type = FileRepositoryManager.getFileExtension(logicalName);

        physicalName = new Long(new Date().getTime()).toString() + "." + type;

        mimeType = file.getContentType();
        size = file.getSize();

        if (mimeType.equals("application/x-zip-compressed")) {
          if (type.equalsIgnoreCase("jar") || type.equalsIgnoreCase("ear") || type.equalsIgnoreCase("war")) {
            mimeType = "application/java-archive";
          } else if (type.equalsIgnoreCase("3D")) {
            mimeType = "application/xview3d-3d";
          }
        } else if (type.equalsIgnoreCase("rtf")) {
          mimeType = AttachmentController.getMimeType(logicalName);
        }


        if (I18NHelper.isI18N) {
          AttachmentDetail toDelete = new AttachmentDetail();

          AttachmentDetailI18N translation = (AttachmentDetailI18N) ad.getTranslation(dummy.getLanguage());
          if (translation == null) {
            //case of a translation creation
          } else {
            //case of a translation update
            String fileToDelete = translation.getPhysicalName();

            toDelete.setContext(ad.getContext());
            toDelete.setPK(ad.getPK());
            toDelete.setPhysicalName(fileToDelete);

            System.out.println("fileToDelete = " + fileToDelete + ", ad.getContext() = " + ad.getContext() + ", ad.getPK() = " + ad.getPK().toString() + ", toDelete.getAttachmentGroup() = " + toDelete.getAttachmentGroup());

            AttachmentController.deleteFileAndIndex(toDelete);
          }
        } else {
          AttachmentController.deleteFileAndIndex(ad);
        }

        String path = AttachmentController.createPath(componentId, context);
        File dir = new File(path + physicalName);
        file.write(dir);

        // Mise a jour des valeurs
        ad.setSize(size);
        ad.setPhysicalName(physicalName);
        ad.setLogicalName(logicalName);
        ad.setType(mimeType);
        ad.setCreationDate(null);

        SilverTrace.info("attachment", "updateFile.jsp", "root.MSG_GEN_PARAM_VALUE", "mimetype=" + mimeType);
      } else {
        if (I18NHelper.isI18N) {
          ad.setPhysicalName(null);
        }
      }

      ad.setAuthor(m_MainSessionCtrl.getUserId());
      ad.setTitle(title);
      ad.setInfo(description);

      I18NHelper.setI18NInfo(ad, items);

      AttachmentController.updateAttachment(ad, indexIt);
%>
<HTML>
  <BODY>
    <script type="text/javascript">
    	try {
  			window.opener.reloadPage();
  		} catch (e) {
    		window.opener.location.reload();
  		}
      	window.close();
    </script>
  </BODY>
</HTML>