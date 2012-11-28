/**
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.form.displayers;


import org.silverpeas.process.ProcessFactory;
import org.silverpeas.process.io.file.FileHandler;
import org.silverpeas.process.management.AbstractFileProcess;
import org.silverpeas.process.management.ProcessExecutionContext;
import org.silverpeas.process.session.ProcessSession;

import com.silverpeas.form.Field;
import com.silverpeas.form.FieldDisplayer;
import com.silverpeas.form.FieldTemplate;
import com.silverpeas.form.Form;
import com.silverpeas.form.FormException;
import com.silverpeas.form.PagesContext;
import com.silverpeas.form.Util;
import com.silverpeas.form.fieldType.FileField;
import com.silverpeas.util.EncodeHelper;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.web.servlet.FileUploadUtil;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.FileServerUtils;

import org.apache.commons.fileupload.FileItem;
import org.silverpeas.attachment.AttachmentServiceFactory;
import org.silverpeas.attachment.model.SimpleDocument;
import org.silverpeas.attachment.model.SimpleDocumentPK;
import org.silverpeas.viewer.ViewerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.IOUtils;

import org.silverpeas.attachment.model.DocumentType;
import org.silverpeas.attachment.model.HistorisedDocument;
import org.silverpeas.attachment.model.SimpleAttachment;

import com.silverpeas.util.FileUtil;



/**
 * A FileFieldDisplayer is an object which can display a link to a file (attachment) in HTML and can
 * retrieve via HTTP any file.
 *
 * @see Field
 * @see FieldTemplate
 * @see Form
 * @see FieldDisplayer
 */
public class FileFieldDisplayer extends AbstractFieldDisplayer<FileField> {

  /**
   * Returns the name of the managed types.
   *
   * @return
   */
  public String[] getManagedTypes() {
    return new String[]{FileField.TYPE};
  }

  /**
   * Prints the javascripts which will be used to control the new value given to the named field.
   * The error messages may be adapted to a local language. The FieldTemplate gives the field type
   * and constraints. The FieldTemplate gives the local labeld too. Never throws an Exception but
   * log a silvertrace and writes an empty string when : <UL> <LI>the fieldName is unknown by the
   * template. <LI>the field type is not a managed type. </UL>
   *
   * @param pageContext
   */
  @Override
  public void displayScripts(PrintWriter out, FieldTemplate template, PagesContext pageContext)
      throws IOException {
    String language = pageContext.getLanguage();
    String fieldName = template.getFieldName();
    if (!FileField.TYPE.equals(template.getTypeName())) {
      SilverTrace.info("form", "FileFieldDisplayer.displayScripts", "form.INFO_NOT_CORRECT_TYPE",
          FileField.TYPE);
    }
    if (template.isMandatory() && pageContext.useMandatory()) {
      out.println("   if (isWhitespace(stripInitialWhitespace(field.value))) {");
      out.println("		var " + fieldName + "Value = document.getElementById('" + fieldName
          + FileField.PARAM_ID_SUFFIX + "').value;");
      out.println("   	if (" + fieldName + "Value=='' || " + fieldName
          + "Value.substring(0,7)==\"remove_\") {");
      out.println("      	errorMsg+=\"  - '" + EncodeHelper.javaStringToJsString(template.getLabel(
          language)) + "' " + Util.getString("GML.MustBeFilled", language) + "\\n \";");
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
   * <UL> <LI>the field type is not a managed type. </UL>
   *
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
        "fieldName = " + template.getFieldName() + ", value = " + field.getValue()
        + ", fieldType = " + field.getTypeName());
    String language = pageContext.getContentLanguage();
    StringBuilder html = new StringBuilder(1024);

    String fieldName = template.getFieldName();

    if (!FileField.TYPE.equals(template.getTypeName())) {
      SilverTrace.info("form", "FileFieldDisplayer.display", "form.INFO_NOT_CORRECT_TYPE",
          FileField.TYPE);
    }

    String attachmentId = field.getValue();
    String componentId = pageContext.getComponentId();

    SimpleDocument attachment = null;
    if (StringUtil.isDefined(attachmentId)) {
      attachment = AttachmentServiceFactory.getAttachmentService().
          searchDocumentById(new SimpleDocumentPK(attachmentId, componentId), language);
    } else {
      attachmentId = "";
    }

    if (template.isReadOnly() && !template.isHidden()) {
      if (attachment != null) {
        html.append("<img alt=\"\" src=\"").append(attachment.getDisplayIcon()).append("\"/>&nbsp;");
        html.append("<a href=\"").append(webContext).append(attachment.getAttachmentURL()).
            append("\" target=\"_blank\">").append(attachment.getFilename()).append("</a>");
        File attachmentFile = new File(attachment.getAttachmentPath());
        if (ViewerFactory.isPreviewable(attachmentFile)) {
          html.append("<img onclick=\"javascript:previewFormFile(this, '").
              append(attachment.getId()).append("');\" class=\"preview-file\" src=\"").
              append(webContext).append("/util/icons/preview.png\" alt=\"").
              append(Util.getString("GML.preview", language)).append("\" title=\"").
              append(Util.getString("GML.preview", language)).append("\"/>");
        }
        if (ViewerFactory.isViewable(attachmentFile)) {
          html.append("<img onclick=\"javascript:viewFormFile(this, '").append(attachment.getId()).
              append("');\" class=\"view-file\" src=\"").append(webContext).append(
              "/util/icons/view.png\" alt=\"").append(Util.getString("GML.view", language)).append(
              "\" title=\"").append(Util.getString("GML.view", language)).append("\"/>");
        }
      }
    } else if (!template.isHidden() && !template.isDisabled() && !template.isReadOnly()) {
      html.append("<input type=\"file\" size=\"50\" id=\"").append(fieldName).append("\" name=\"").
          append(fieldName).append("\"/>");
      html.append("<input type=\"hidden\" id=\"").append(fieldName).
          append(FileField.PARAM_ID_SUFFIX + "\" name=\"").append(fieldName).
          append(FileField.PARAM_NAME_SUFFIX + "\" value=\"").append(attachmentId).append("\"/>");

      if (attachment != null) {
        String deleteImg = Util.getIcon("delete");
        String deleteLab = Util.getString("removeFile", language);

        html.append("&nbsp;<span id=\"div").append(fieldName).append("\">");
        html.append("<img alt=\"\" align=\"top\" src=\"").append(attachment.getDisplayIcon()).
            append("\"/>&nbsp;");
        html.append("<a href=\"").append(webContext).append(attachment.getAttachmentURL()).append(
            "\" target=\"_blank\">").append(attachment.getFilename()).append("</a>");

        html.append("&nbsp;<a href=\"#\" onclick=\"javascript:" + "document.getElementById('div").
            append(fieldName).append("').style.display='none';" + "document.").
            append(pageContext.getFormName()).append(".").append(fieldName).
            append(FileField.PARAM_NAME_SUFFIX + ".value='remove_").append(attachmentId).append(
            "';" + "\">");
        html.append("<img src=\"").append(deleteImg).
            append("\" width=\"15\" height=\"15\" border=\"0\" alt=\"").append(deleteLab).
            append("\" align=\"top\" title=\"").append(deleteLab).append(
            "\"/></a>");
        html.append("</span>");
      }

      if (template.isMandatory() && pageContext.useMandatory()) {
        html.append(Util.getMandatorySnippet());
      }
    }

    html.append(displayPreviewJavascript(pageContext));
    html.append(displayViewJavascript(pageContext));

    out.println(html.toString());
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
    if (FileField.TYPE.equals(field.getTypeName())) {
      if (!StringUtil.isDefined(attachmentId)) {
        field.setNull();
      } else {
        field.setAttachmentId(attachmentId);
      }
    } else {
      throw new FormException("FileFieldDisplayer.update", "form.EX_NOT_CORRECT_VALUE",
          FileField.TYPE);
    }
    return Collections.singletonList(attachmentId);
  }

  /**
   *
   * @return
   */
  @Override
  public boolean isDisplayedMandatory() {
    return true;
  }

  /**
   * Method declaration
   *
   * @return
   */
  @Override
  public int getNbHtmlObjectsDisplayed(FieldTemplate template, PagesContext pagesContext) {
    return 2;
  }

  @Override
  public List<String> update(final List<FileItem> items, final FileField field, final FieldTemplate template,
      final PagesContext pageContext) throws FormException {
    final List<String> result = new ArrayList<String>();
    final String itemName = template.getFieldName();
    try {
      // TODO - MODIFYING THIS PROCESSES EXECUTION AFTER NEW ATTACHMENT HANDLING INTEGRATION
      ProcessFactory.getProcessManagement().execute(
          new AbstractFileProcess<ProcessExecutionContext>() {
            @Override
            public void processFiles(ProcessExecutionContext processExecutionProcess,
                ProcessSession session, FileHandler fileHandler) throws Exception {

              String value = processUploadedFile(items, itemName, pageContext, fileHandler);
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
                }
              }
              if (pageContext.getUpdatePolicy() != PagesContext.ON_UPDATE_IGNORE_EMPTY_VALUES ||
                  StringUtil.isDefined(value)) {
                result.addAll(update(value, field, template, pageContext));
              }
            }
          }, new ProcessExecutionContext(pageContext.getComponentId()));
    } catch (Exception e) {
      SilverTrace.error("form", "ImageFieldDisplayer.update", "form.EXP_UNKNOWN_FIELD", null, e);
    }
    return result;
  }

  private String processUploadedFile(List<FileItem> items, String parameterName,
      PagesContext pagesContext, FileHandler fileHandler)
      throws Exception {
    String attachmentId = null;
    FileItem item = FileUploadUtil.getFile(items, parameterName);
    if (!item.isFormField()) {
      String componentId = pagesContext.getComponentId();
      String userId = pagesContext.getUserId();
      String objectId = pagesContext.getObjectId();
      if (StringUtil.isDefined(item.getName())) {
        String fileName = FileUtil.getFilename(item.getName());
        SilverTrace.info("form", "AbstractForm.processUploadedFile", "root.MSG_GEN_PARAM_VALUE",
            "fullFileName on Unix = " + fileName);
        long size = item.getSize();
        if (size > 0) {
          SimpleDocument document = createSimpleDocument(objectId, componentId, item,
              fileName, userId, pagesContext.isVersioningUsed());
          return document.getId();
        }
          }
        }
    return attachmentId;
  }

  private SimpleDocument createSimpleDocument(String objectId, String componentId, FileItem item,
      String logicalName, String userId, boolean versionned) throws IOException {
    SimpleDocumentPK documentPk = new SimpleDocumentPK(null, componentId);
    SimpleDocument document;
    if (versionned) {
      document = new HistorisedDocument(documentPk, objectId, 0, new SimpleAttachment(logicalName,
          null, null, null, item.getSize(), FileUtil.getMimeType(logicalName), userId, new Date(),
          null));
    } else {
      document = new SimpleDocument(documentPk, objectId, 0, false, userId, new SimpleAttachment(
          logicalName, null, null, null, item.getSize(), FileUtil.getMimeType(logicalName), userId,
          new Date(), null));
    }
    document.setDocumentType(DocumentType.form);
    InputStream in = item.getInputStream();
    try {
      return AttachmentServiceFactory.getAttachmentService().createAttachment(document, in, false);
    } finally {
      IOUtils.closeQuietly(in);
    }

  }

  private void deleteAttachment(String attachmentId, PagesContext pageContext) {
    SilverTrace.info("form", "AbstractForm.deleteAttachment", "root.MSG_GEN_ENTER_METHOD",
        "attachmentId = " + attachmentId + ", componentId = " + pageContext.getComponentId());
    SimpleDocumentPK pk = new SimpleDocumentPK(attachmentId, pageContext.getComponentId());
    SimpleDocument doc = AttachmentServiceFactory.getAttachmentService().searchDocumentById(pk,
        pageContext.getContentLanguage());
    if (doc != null) {
      AttachmentServiceFactory.getAttachmentService().deleteAttachment(doc);
    }
  }
}
