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
<%@ page import="com.silverpeas.util.i18n.I18NHelper"%>
<%@ page import="com.silverpeas.util.StringUtil"%>
<%@ page import="java.util.StringTokenizer"%>

<%
      String id = (String) session.getAttribute("Silverpeas_Attachment_ObjectId");
      String componentId = (String) session.getAttribute("Silverpeas_Attachment_ComponentId");
      String context = (String) session.getAttribute("Silverpeas_Attachment_Context");
      boolean indexIt = ((Boolean) session.getAttribute("Silverpeas_Attachment_IndexIt")).booleanValue();

      String path = AttachmentController.createPath(componentId, context);

      String logicalName = null;
      String physicalName = null;
      String mimeType = null;
      Date creationDate = new Date();
      long size = 0;
      String type = null;
      String title = null;
      String info = null;
      boolean isExistFile = true; // par defaut, le fichier que l'utilisateur veut ajouter existe.

      ResourceLocator settings = new ResourceLocator("com.stratelia.webactiv.util.attachment.Attachment", "");
      boolean actifyPublisherEnable = settings.getBoolean("ActifyPublisherEnable", false);
      if (!StringUtil.isDefined(request.getCharacterEncoding())) {
        request.setCharacterEncoding("UTF-8");
      }
      List items = FileUploadUtil.parseRequest(request);

      title = getParameterValue(items, "Title", request.getCharacterEncoding());
      info = getParameterValue(items, "Description", request.getCharacterEncoding());

      FileItem file = getUploadedFile(items, "file_upload");
      if (file != null) {
        logicalName = file.getName();
        if (StringUtil.isDefined(logicalName)) {

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

          File dir = new File(path + physicalName);

          file.write(dir);

          SilverTrace.info("attachment", "SaveFile.jsp", "root.MSG_GEN_PARAM_VALUE", "mimetype=" + mimeType);
        }
      }

      //l'ajout du fichier joint ne se fait que si la taille du fichier (size) est >0
      //sinon cela indique que le fichier n'est pas valide (chemin non valide, fichier non accessible)
      if (size > 0) {
        //create AttachmentPK with componentId
        AttachmentPK atPK = new AttachmentPK(null, componentId);

        //create foreignKey with spaceId, componentId and id
        //use AttachmentPK to build the foreign key of customer object.
        AttachmentPK foreignKey = new AttachmentPK(id, componentId);

        //create AttachmentDetail Object
        AttachmentDetail ad = new AttachmentDetail(atPK, physicalName, logicalName, null, mimeType, size, context, creationDate, foreignKey);
        ad.setAuthor(m_MainSessionCtrl.getUserId());
        ad.setTitle(title);
        ad.setInfo(info);

        I18NHelper.setI18NInfo(ad, items);

        AttachmentController.createAttachment(ad, indexIt);

        //Specific case: 3d file to convert by Actify Publisher
        if (actifyPublisherEnable) {
          String extensions = settings.getString("Actify3dFiles");
          StringTokenizer tokenizer = new StringTokenizer(extensions, ",");
          //3d native file ?
          boolean fileForActify = false;
          while (tokenizer.hasMoreTokens() && !fileForActify) {
            String extension = tokenizer.nextToken();
            if (type.equalsIgnoreCase(extension)) {
              fileForActify = true;
            }
          }
          if (fileForActify) {
            String dirDestName = "a_" + componentId + "_" + id;
            String actifyWorkingPath = settings.getString("ActifyPathSource") + File.separator + dirDestName;

            String destPath = FileRepositoryManager.getTemporaryPath() + actifyWorkingPath;
            if (!new File(destPath).exists()) {
              FileRepositoryManager.createGlobalTempPath(actifyWorkingPath);
            }

            String destFile = FileRepositoryManager.getTemporaryPath() + actifyWorkingPath + File.separator + logicalName;
            FileRepositoryManager.copyFile(AttachmentController.createPath(componentId, "Images") + File.separator + physicalName, destFile);
          }
        }
      }
%>
<html>
  <head>
    <title>_________________/ Silverpeas - Corporate portal organizer \_________________/</title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <% out.println(gef.getLookStyleSheet());%>
  </head>
  <body>
    <%
          Frame frame = gef.getFrame();
          out.println(frame.printBefore());
    %>
    <center>
      <% if (!isExistFile) {%>
      <table width="98%" border="0" cellspacing="0" cellpadding="0" class=intfdcolor4><!--tablcontour-->
        <tr>
          <td valign="top" align="center"> <!-- SEPARATION NAVIGATION / CONTENU DU COMPOSANT -->
            <table border="0" cellspacing="0" cellpadding="5" width="100%" align="center" class="contourintfdcolor">
              <tr>
                <td align="center">
                  <b>
                    <%
                       if (!isExistFile) {
                         out.println(messages.getString("fichierInexistant"));
                       }
                    %></b>
                </td>
              </tr>
            </table>
          </td>
        </tr>
      </table><br/>
      <%
         ButtonPane buttonPane2 = gef.getButtonPane();
         buttonPane2.addButton((Button) gef.getFormButton(attResources.getString("GML.back"), "javascript:window.opener.location.reload();window.close();", false));
         out.println(buttonPane2.print());
         out.println(frame.printAfter());
      %>
      <% } else {%>
      <script language='javascript'>
      	try {
      		window.opener.reloadPage();
      	} catch (e) {
        	window.opener.location.reload();
      	}
        window.close();
      </script>
      <% }%>
    </center>
  </body>
  <script language='javascript'>
    window.focus();
  </script>
</html>