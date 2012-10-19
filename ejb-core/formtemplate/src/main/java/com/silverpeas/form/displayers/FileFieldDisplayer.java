/**
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.form.displayers;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.fileupload.FileItem;
import org.silverpeas.viewer.ViewerFactory;

import com.silverpeas.form.Field;
import com.silverpeas.form.FieldDisplayer;
import com.silverpeas.form.FieldTemplate;
import com.silverpeas.form.Form;
import com.silverpeas.form.FormException;
import com.silverpeas.form.PagesContext;
import com.silverpeas.form.Util;
import com.silverpeas.form.fieldType.FileField;
import com.silverpeas.util.EncodeHelper;
import com.silverpeas.util.FileUtil;
import com.silverpeas.util.ForeignPK;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.web.servlet.FileUploadUtil;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.versioning.ejb.VersioningBm;
import com.stratelia.silverpeas.versioning.ejb.VersioningBmHome;
import com.stratelia.silverpeas.versioning.model.Document;
import com.stratelia.silverpeas.versioning.model.DocumentPK;
import com.stratelia.silverpeas.versioning.model.DocumentVersion;
import com.stratelia.silverpeas.versioning.model.Worker;
import com.stratelia.silverpeas.versioning.util.VersioningUtil;
import com.stratelia.webactiv.beans.admin.ComponentInst;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.ProfileInst;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.FileServerUtils;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.attachment.control.AttachmentController;
import com.stratelia.webactiv.util.attachment.ejb.AttachmentPK;
import com.stratelia.webactiv.util.attachment.model.AttachmentDetail;
import com.stratelia.webactiv.util.fileFolder.FileFolderManager;
import org.apache.commons.io.IOUtils;
import org.apache.jackrabbit.server.io.IOUtil;
import org.silverpeas.attachment.AttachmentServiceFactory;
import org.silverpeas.attachment.model.SimpleAttachment;
import org.silverpeas.attachment.model.SimpleDocument;
import org.silverpeas.attachment.model.SimpleDocumentPK;

/**
 * A FileFieldDisplayer is an object which can display a link to a file (attachment) in HTML and can
 * retrieve via HTTP any file.
 * @see Field
 * @see FieldTemplate
 * @see Form
 * @see FieldDisplayer
 */
public class FileFieldDisplayer extends AbstractFieldDisplayer<FileField> {

  public static final String CONTEXT_FORM_FILE = "Images";
  private VersioningBm versioningBm = null;

  /**
   * Returns the name of the managed types.
   */
  public String[] getManagedTypes() {
    return new String[] { FileField.TYPE };
  }

  /**
   * Prints the javascripts which will be used to control the new value given to the named field.
   * The error messages may be adapted to a local language. The FieldTemplate gives the field type
   * and constraints. The FieldTemplate gives the local labeld too. Never throws an Exception but
   * log a silvertrace and writes an empty string when :
   * <UL>
   * <LI>the fieldName is unknown by the template.
   * <LI>the field type is not a managed type.
   * </UL>
   */
  @Override
  public void displayScripts(PrintWriter out, FieldTemplate template, PagesContext pageContext)
      throws java.io.IOException {
    String language = pageContext.getLanguage();

    String fieldName = template.getFieldName();

    if (!FileField.TYPE.equals(template.getTypeName())) {
      SilverTrace.info("form", "FileFieldDisplayer.displayScripts", "form.INFO_NOT_CORRECT_TYPE",
          FileField.TYPE);
    }
    if (template.isMandatory() && pageContext.useMandatory()) {
      out.println("   if (isWhitespace(stripInitialWhitespace(field.value))) {");
      out.println("		var " + fieldName + "Value = document.getElementById('" + fieldName +
          FileField.PARAM_ID_SUFFIX + "').value;");
      out.println("   	if (" + fieldName + "Value=='' || " + fieldName +
          "Value.substring(0,7)==\"remove_\") {");
      out.println("      	errorMsg+=\"  - '" +
          EncodeHelper.javaStringToJsString(template.getLabel(language)) + "' " +
          Util.getString("GML.MustBeFilled", language) + "\\n \";");
      out.println("      	errorNb++;");
      out.println("   	}");
      out.println("   } ");
    }

    if (!template.isReadOnly()) {
      Util.includeFileNameLengthChecker(template, pageContext, out);
      Util.getJavascriptChecker(template.getFieldName(), pageContext, out);
    }
  }

  /**
   * Prints the HTML value of the field. The displayed value must be updatable by the end user. The
   * value format may be adapted to a local language. The fieldName must be used to name the html
   * form input. Never throws an Exception but log a silvertrace and writes an empty string when :
   * <UL>
   * <LI>the field type is not a managed type.
   * </UL>
   * @param out
   * @param field
   * @param pagesContext
   * @param template
   * @throws FormException
   */
  @Override
  public void display(PrintWriter out, FileField field, FieldTemplate template,
      PagesContext pagesContext) throws FormException {
    display(out, field, template, pagesContext, FileServerUtils.getApplicationContext());
  }

  public void display(PrintWriter out, Field field, FieldTemplate template,
      PagesContext pageContext, String webContext) throws FormException {
    SilverTrace.info("form", "FileFieldDisplayer.display", "root.MSG_GEN_ENTER_METHOD",
        "fieldName = " + template.getFieldName() + ", value = " + field.getValue() +
        ", fieldType = " + field.getTypeName());

    String html = "";

    String fieldName = template.getFieldName();

    if (!FileField.TYPE.equals(template.getTypeName())) {
      SilverTrace.info("form", "FileFieldDisplayer.display", "form.INFO_NOT_CORRECT_TYPE",
          FileField.TYPE);
    }

    String attachmentId = field.getValue();
    String componentId = pageContext.getComponentId();

    AttachmentDetail attachment = null;
    if (StringUtil.isDefined(attachmentId)) {
      attachment =
          AttachmentController.searchAttachmentByPK(new AttachmentPK(attachmentId, componentId));
    } else {
      attachmentId = "";
    }

    if (template.isReadOnly() && !template.isHidden()) {
      if (attachment != null) {
        html = "<img alt=\"\" src=\"" + attachment.getAttachmentIcon() + "\"/>&nbsp;";
        html +=
            "<a href=\"" + webContext + attachment.getAttachmentURL(pageContext.getContentLanguage()) + "\" target=\"_blank\">" +
            attachment.getLogicalName() + "</a>";
        if (ViewerFactory.getPreviewService().isPreviewable(
            new File(attachment.getAttachmentPath(pageContext.getContentLanguage())))) {
          html +=
              "<img onclick=\"javascript:previewFormFile(this, " + attachment.getPK().getId() +
                  ");\" class=\"preview-file\" src=\"" + webContext +
                  "/util/icons/preview.png\" alt=\"" +
                  Util.getString("GML.preview", pageContext.getLanguage()) + "\" title=\"" +
                  Util.getString("GML.preview", pageContext.getLanguage()) + "\"/>";
        }
        if (ViewerFactory.getViewService().isViewable(
            new File(attachment.getAttachmentPath(pageContext.getContentLanguage())))) {
          html +=
              "<img onclick=\"javascript:viewFormFile(this, " + attachment.getPK().getId() +
                  ");\" class=\"view-file\" src=\"" + webContext +
                  "/util/icons/view.png\" alt=\"" +
                  Util.getString("GML.view", pageContext.getLanguage()) + "\" title=\"" +
                  Util.getString("GML.view", pageContext.getLanguage()) + "\"/>";
        }
      }
    } else if (!template.isHidden() && !template.isDisabled() && !template.isReadOnly()) {
      html +=
          "<input type=\"file\" size=\"50\" id=\"" + fieldName + "\" name=\"" + fieldName + "\"/>";
      html +=
          "<input type=\"hidden\" id=\"" + fieldName + FileField.PARAM_ID_SUFFIX + "\" name=\"" +
          fieldName + FileField.PARAM_NAME_SUFFIX + "\" value=\"" + attachmentId + "\"/>";

      if (attachment != null) {
        String deleteImg = Util.getIcon("delete");
        String deleteLab = Util.getString("removeFile", pageContext.getLanguage());

        html += "&nbsp;<span id=\"div" + fieldName + "\">";
        html +=
            "<img alt=\"\" align=\"top\" src=\"" + attachment.getAttachmentIcon() +
            "\"/>&nbsp;";
        html +=
            "<a href=\"" + webContext + attachment.getAttachmentURL() + "\" target=\"_blank\">" +
            attachment.getLogicalName() + "</a>";

        html +=
            "&nbsp;<a href=\"#\" onclick=\"javascript:" + "document.getElementById('div" +
            fieldName + "').style.display='none';" + "document." + pageContext.getFormName() +
            "." + fieldName + FileField.PARAM_NAME_SUFFIX + ".value='remove_" + attachmentId +
            "';" + "\">";
        html +=
            "<img src=\"" + deleteImg + "\" width=\"15\" height=\"15\" border=\"0\" alt=\"" +
            deleteLab + "\" align=\"top\" title=\"" + deleteLab + "\"/></a>";
        html += "</span>";
      }

      if (template.isMandatory() && pageContext.useMandatory()) {
        html += Util.getMandatorySnippet();
      }
    }

    html += displayPreviewJavascript(pageContext);
    html += displayViewJavascript(pageContext);

    out.println(html);
  }

  private String displayPreviewJavascript(PagesContext context) {
    StringBuilder sb = new StringBuilder(50);
    sb.append("<script type=\"text/javascript\">\n");
    sb.append("function previewFormFile(target, attachmentId) {\n");
    sb.append("$(target).preview(\"previewAttachment\", {\n");
    sb.append("componentInstanceId: \"").append(context.getComponentId()).append("\",\n");
    sb.append("attachmentId: attachmentId\n");
    sb.append("});\n");
    sb.append("return false;");
    sb.append("}\n");
    sb.append("</script>\n");
    return sb.toString();
  }

  private String displayViewJavascript(PagesContext context) {
    StringBuilder sb = new StringBuilder(50);
    sb.append("<script type=\"text/javascript\">\n");
    sb.append("function viewFormFile(target, attachmentId) {\n");
    sb.append("$(target).view(\"viewAttachment\", {\n");
    sb.append("componentInstanceId: \"").append(context.getComponentId()).append("\",\n");
    sb.append("attachmentId: attachmentId\n");
    sb.append("});\n");
    sb.append("return false;");
    sb.append("}\n");
    sb.append("</script>\n");
    return sb.toString();
  }

  @Override
  public List<String> update(String attachmentId, FileField field, FieldTemplate template,
      PagesContext pagesContext) throws FormException {
    List<String> attachmentIds = new ArrayList<String>();
    if (FileField.TYPE.equals(field.getTypeName())) {
      if (attachmentId == null || attachmentId.trim().equals("")) {
        field.setNull();
      } else {
        ((FileField) field).setAttachmentId(attachmentId);
        attachmentIds.add(attachmentId);
      }
    } else {
      throw new FormException("FileFieldDisplayer.update", "form.EX_NOT_CORRECT_VALUE",
          FileField.TYPE);
    }
    return attachmentIds;
  }

  /**
   * Method declaration
   * @return
   */
  @Override
  public boolean isDisplayedMandatory() {
    return true;
  }

  /**
   * Method declaration
   * @return
   */
  @Override
  public int getNbHtmlObjectsDisplayed(FieldTemplate template, PagesContext pagesContext) {
    return 2;
  }

  @Override
  public List<String> update(List<FileItem> items, FileField field, FieldTemplate template,
      PagesContext pageContext) throws FormException {
    String itemName = template.getFieldName();
    try {
      String value = processUploadedFile(items, itemName, pageContext);
      String param = FileUploadUtil.getParameter(items, itemName + Field.FILE_PARAM_NAME_SUFFIX);
      if (param != null) {
        if (param.startsWith("remove_") && !pageContext.isCreation()) {
          // Il faut supprimer le fichier
          String attachmentId = param.substring("remove_".length());
          deleteAttachment(attachmentId, pageContext);
        } else if (value != null && StringUtil.isInteger(param)) {
          // Y'avait-il un déjà un fichier ?
          // Il faut remplacer le fichier donc supprimer l'ancien
          deleteAttachment(param, pageContext);
        } else if (value == null) {
          // pas de nouveau fichier, ni de suppression
          // le champ ne doit pas être mis à jour
          return new ArrayList<String>();
        }
      }
      if (pageContext.getUpdatePolicy() == PagesContext.ON_UPDATE_IGNORE_EMPTY_VALUES &&
          !StringUtil.isDefined(value)) {
        return new ArrayList<String>();
      }
      return update(value, field, template, pageContext);
    } catch (Exception e) {
      SilverTrace.error("form", "ImageFieldDisplayer.update", "form.EXP_UNKNOWN_FIELD", null, e);
    }
    return new ArrayList<String>();
  }

  private String processUploadedFile(List<FileItem> items, String parameterName,
      PagesContext pagesContext)
      throws Exception {
    String attachmentId = null;
    FileItem item = FileUploadUtil.getFile(items, parameterName);
    if (!item.isFormField()) {
      String componentId = pagesContext.getComponentId();
      String userId = pagesContext.getUserId();
      String objectId = pagesContext.getObjectId();
      String logicalName = item.getName();
      String physicalName = null;
      String mimeType = null;
      File dir = null;
      long size = 0;
      VersioningUtil versioningUtil = new VersioningUtil();
      if (StringUtil.isDefined(logicalName)) {
        if (!FileUtil.isWindows()) {
          logicalName = logicalName.replace('\\', File.separatorChar);
          SilverTrace.info("form", "AbstractForm.processUploadedFile", "root.MSG_GEN_PARAM_VALUE",
              "fullFileName on Unix = " + logicalName);
        }
        logicalName =
            logicalName
            .substring(logicalName.lastIndexOf(File.separator) + 1, logicalName.length());
        String type = FileRepositoryManager.getFileExtension(logicalName);
        mimeType = item.getContentType();
        if (mimeType.equals("application/x-zip-compressed")) {
          if (type.equalsIgnoreCase("jar") || type.equalsIgnoreCase("ear") ||
              type.equalsIgnoreCase("war")) {
            mimeType = "application/java-archive";
          } else if (type.equalsIgnoreCase("3D")) {
            mimeType = "application/xview3d-3d";
          }
        }
        physicalName = System.currentTimeMillis() + "." + type;
        String path = "";
        if (pagesContext.isVersioningUsed()) {
          path = versioningUtil.createPath("useless", componentId, "useless");
        } else {
          path = AttachmentController.createPath(componentId, FileFieldDisplayer.CONTEXT_FORM_FILE);
        }
        dir = new File(path + physicalName);
        size = item.getSize();
        item.write(dir);

        // l'ajout du fichier joint ne se fait que si la taille du fichier (size) est >0
        // sinon cela indique que le fichier n'est pas valide (chemin non valide, fichier non
        // accessible)
        if (size > 0) {
          AttachmentDetail ad =
              createAttachmentDetail(objectId, componentId, physicalName, logicalName, mimeType,
              size, FileFieldDisplayer.CONTEXT_FORM_FILE, userId);

          if (pagesContext.isVersioningUsed()) {
            // mode versioning
            attachmentId = createDocument(objectId, ad);
          } else {
            // mode classique
            SimpleDocument document = createSimpleDocument(objectId, componentId, item,
                logicalName, userId);
            attachmentId = document.getId();
          }
        } else {
          // le fichier à tout de même été créé sur le serveur avec une taille 0!, il faut le
          // supprimer
          if (dir != null) {
            FileFolderManager.deleteFolder(dir.getPath());
          }
        }
      }
    }
    return attachmentId;
  }


  private SimpleDocument createSimpleDocument(String objectId, String componentId,FileItem item,
                                              String logicalName, String userId)
      throws IOException {
    SimpleDocumentPK documentPk = new SimpleDocumentPK(null, componentId);
    SimpleDocument document = new SimpleDocument(documentPk, objectId, 0,false, userId,
        new SimpleAttachment(logicalName, null, null, null, item.getSize(),
            FileUtil.getMimeType(logicalName),userId, new Date(), null));
    InputStream in = item.getInputStream();
    try {
    return AttachmentServiceFactory.getAttachmentService().createAttachment(document, in, false);
    }finally {
      IOUtils.closeQuietly(in);
    }

  }

  private AttachmentDetail createAttachmentDetail(String objectId, String componentId,
      String physicalName, String logicalName, String mimeType, long size, String context,
      String userId) {
    // create AttachmentPK with spaceId and componentId
    AttachmentPK atPK = new AttachmentPK(null, "useless", componentId);

    // create foreignKey with spaceId, componentId and id
    // use AttachmentPK to build the foreign key of customer object.
    AttachmentPK foreignKey = new AttachmentPK("-1", "useless", componentId);
    if (objectId != null) {
      foreignKey.setId(objectId);
    }

    // create AttachmentDetail Object
    AttachmentDetail ad =
        new AttachmentDetail(atPK, physicalName, logicalName, null, mimeType, size, context,
        new Date(), foreignKey);
    ad.setAuthor(userId);

    return ad;
  }

  private void deleteAttachment(String attachmentId, PagesContext pageContext) {
    SilverTrace.info("form", "AbstractForm.deleteAttachment", "root.MSG_GEN_ENTER_METHOD",
        "attachmentId = " + attachmentId + ", componentId = " + pageContext.getComponentId());
    AttachmentPK pk = new AttachmentPK(attachmentId, pageContext.getComponentId());
    AttachmentController.deleteAttachment(pk);
  }

  private String createDocument(String objectId, AttachmentDetail attachment)
      throws RemoteException {
    String componentId = attachment.getPK().getInstanceId();
    int userId = Integer.parseInt(attachment.getAuthor());
    ForeignPK pubPK = new ForeignPK("-1", componentId);
    if (objectId != null) {
      pubPK.setId(objectId);
    }

    // Création d'un nouveau document
    DocumentPK docPK = new DocumentPK(-1, "useless", componentId);
    Document document =
        new Document(docPK, pubPK, attachment.getLogicalName(), "", -1, userId, new Date(), null,
        null, null, null, 0, 0);

    document.setWorkList((ArrayList<Worker>) getWorkers(componentId, userId));

    DocumentVersion version = new DocumentVersion(attachment);
    version.setAuthorId(userId);

    // et on y ajoute la première version
    version.setMajorNumber(1);
    version.setMinorNumber(0);
    version.setType(DocumentVersion.TYPE_PUBLIC_VERSION);
    version.setStatus(DocumentVersion.STATUS_VALIDATION_NOT_REQ);
    version.setCreationDate(new Date());

    docPK = getVersioningBm().createDocument(document, version);
    document.setPk(docPK);

    return docPK.getId();
  }

  private List<Worker> getWorkers(String componentId, int creatorId) {
    List<Worker> workers = new ArrayList<Worker>();

    OrganizationController orga = new OrganizationController();
    ComponentInst component = orga.getComponentInst(componentId);

    List<ProfileInst> profilesInst = component.getAllProfilesInst();
    List<String> profiles = new ArrayList<String>();
    for (ProfileInst profileInst : profilesInst) {
      profiles.add(profileInst.getName());
    }

    String[] userIds = orga.getUsersIdsByRoleNames(componentId, profiles);

    int iUserId = -1;
    Worker worker = null;
    boolean find = false;
    for (String userId : userIds) {
      iUserId = Integer.parseInt(userId);
      if (!find && (iUserId == creatorId)) {
        find = true;
      }

      worker = new Worker(iUserId, 0, 0, true, true, componentId);
      workers.add(worker);
    }
    if (!find) {
      worker = new Worker(creatorId, 0, 0, true, true, componentId);
      workers.add(worker);
    }

    Worker lastWorker = workers.get(workers.size() - 1);
    lastWorker.setApproval(true);

    return workers;
  }

  private VersioningBm getVersioningBm() {
    if (versioningBm == null) {
      try {
        VersioningBmHome vscEjbHome = EJBUtilitaire.getEJBObjectRef(JNDINames.VERSIONING_EJBHOME,
            VersioningBmHome.class);
        versioningBm = vscEjbHome.create();
      } catch (Exception e) {
        // NEED
        // throw new
        // ...RuntimeException("VersioningSessionController.initEJB()",SilverpeasRuntimeException.
        // ERROR,"root.EX_CANT_GET_REMOTE_OBJECT",e);
      }
    }
    return versioningBm;
  }
}
