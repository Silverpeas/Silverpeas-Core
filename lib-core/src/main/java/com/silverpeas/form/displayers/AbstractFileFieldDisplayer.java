/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection withWriter Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/legal/licensing"
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

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.silverpeas.attachment.AttachmentServiceFactory;
import org.silverpeas.attachment.model.DocumentType;
import org.silverpeas.attachment.model.HistorisedDocument;
import org.silverpeas.attachment.model.SimpleAttachment;
import org.silverpeas.attachment.model.SimpleDocument;
import org.silverpeas.attachment.model.SimpleDocumentPK;

import com.silverpeas.form.Field;
import com.silverpeas.form.FieldTemplate;
import com.silverpeas.form.FormException;
import com.silverpeas.form.PagesContext;
import com.silverpeas.form.Util;
import com.silverpeas.form.fieldType.FileField;
import com.silverpeas.util.EncodeHelper;
import com.silverpeas.util.FileUtil;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.web.servlet.FileUploadUtil;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.FileServerUtils;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author ehugonnet
 */
public abstract class AbstractFileFieldDisplayer extends AbstractFieldDisplayer<FileField> {
  
  protected static final String OPERATION_KEY = "Operation";
  
  /**
   * The different kinds of operation that can be applied into an attached file.
   */
  protected enum Operation {
    ADD, UPDATE, DELETION;
  }

  protected SimpleDocument createSimpleDocument(String objectId, String componentId, FileItem item,
      String fileName, String userId, boolean versionned) throws IOException {
    SimpleDocumentPK documentPk = new SimpleDocumentPK(null, componentId);
    SimpleAttachment attachment = new SimpleAttachment(fileName, null, null, null, item.getSize(),
        FileUtil.getMimeType(fileName), userId, new Date(), null);
    SimpleDocument document;
    if (versionned) {
      document = new HistorisedDocument(documentPk, objectId, 0, attachment);
    } else {
      document = new SimpleDocument(documentPk, objectId, 0, false, null, attachment);
    }
    document.setDocumentType(DocumentType.form);
    InputStream in = item.getInputStream();
    try {
      return AttachmentServiceFactory.getAttachmentService().createAttachment(document, in, false);
    } finally {
      IOUtils.closeQuietly(in);
    }
  }

  /**
   * Deletes the specified attachment, identified by its unique identifier.?
   *
   * @param attachmentId the unique identifier of the attachment to delete.
   * @param pageContext the context of the page.
   */
  protected void deleteAttachment(String attachmentId, PagesContext pageContext) {
    SilverTrace.info("form", "AbstractFileFieldDisplayer.deleteAttachment",
        "root.MSG_GEN_ENTER_METHOD", "attachmentId = " + attachmentId + ", componentId = "
        + pageContext.getComponentId());
    SimpleDocumentPK pk = new SimpleDocumentPK(attachmentId, pageContext.getComponentId());
    SimpleDocument doc = AttachmentServiceFactory.getAttachmentService().searchDocumentById(pk,
        pageContext.getContentLanguage());
    if (doc != null) {
      AttachmentServiceFactory.getAttachmentService().deleteAttachment(doc);
    }
  }

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
      throws java.io.IOException {
    String language = pageContext.getLanguage();
    String fieldName = template.getFieldName();
    if (template.isMandatory() && pageContext.useMandatory()) {
      out.println(" if (isWhitespace(stripInitialWhitespace(field.value))) {");
      out.println(
          "   var " + fieldName + "Value = document.getElementById('" + fieldName
          + FileField.PARAM_ID_SUFFIX + "').value;");
      out.println("   if (" + fieldName + "Value=='' || " + fieldName
          + "Value.substring(0,7)==\"remove_\") {");
      out.println("     errorMsg+=\"  - '"
          + EncodeHelper.javaStringToJsString(template.getLabel(language)) + "' " + Util.
          getString("GML.MustBeFilled", language) + "\\n \";");
      out.println("     errorNb++;");
      out.println("   }");
      out.println(" }");
    }

    if (!template.isReadOnly()) {
      Util.includeFileNameLengthChecker(template, pageContext, out);
      Util.getJavascriptChecker(template.getFieldName(), pageContext, out);
    }
  }
  
  @Override
  public List<String> update(List<FileItem> items, FileField field, FieldTemplate template,
      PagesContext pageContext) throws FormException {
    List<String> attachmentIds = new ArrayList<String>();
    try {
      String fieldName = template.getFieldName();
      String attachmentId = processUploadedFile(items, fieldName, pageContext);
      Operation operation = Operation.valueOf(FileUploadUtil.getParameter(items, fieldName
          + OPERATION_KEY));
      String currentAttachmentId = field.getAttachmentId();
      if ((isDeletion(operation, currentAttachmentId) || isUpdate(operation, attachmentId))
          && !pageContext.isCreation()) {
        // delete previous attachment
        deleteAttachment(currentAttachmentId, pageContext);
        if (!StringUtil.isDefined(attachmentId)) {
          attachmentId = null;
        }
      }
      attachmentIds.addAll(update(attachmentId, field, template, pageContext));
    } catch (IOException ex) {
      SilverTrace.error("form", "VideoFieldDisplayer.update", "form.EXP_UNKNOWN_FIELD", null, ex);
    }

    return attachmentIds;
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
   * Is the specified operation is a deletion?
   *
   * @param operation the operation.
   * @param attachmentId the identifier of the attachment on which the operation is.
   * @return true if the operation is a deletion, false otherwise.
   */
  protected boolean isDeletion(final Operation operation, final String attachmentId) {
    return StringUtil.isDefined(attachmentId) && operation == Operation.DELETION;
  }

  /**
   * Is the specified operation is an update?
   *
   * @param operation the operation.
   * @param attachmentId the identifier of the attachment on which the operation is.
   * @return true if the operation is an update, false otherwise.
   */
  protected boolean isUpdate(final Operation operation, final String attachmentId) {
    return StringUtil.isDefined(attachmentId) && operation == Operation.UPDATE;
  }
  
  protected String processUploadedFile(List<FileItem> items, String parameterName,
      PagesContext pagesContext) throws IOException {
    String attachmentId = null;
    FileItem item = FileUploadUtil.getFile(items, parameterName);
    if (item != null && !item.isFormField()) {
      String componentId = pagesContext.getComponentId();
      String userId = pagesContext.getUserId();
      String objectId = pagesContext.getObjectId();
      if (StringUtil.isDefined(item.getName())) {
        String fileName = FileUtil.getFilename(item.getName());
        long size = item.getSize();
        if (size > 0L) {
          SimpleDocument document = createSimpleDocument(objectId, componentId, item, fileName,
              userId, false);
          attachmentId = document.getId();
        }
      }
    }
    return attachmentId;
  }
  
  @Override
  public boolean isDisplayedMandatory() {
    return true;
  }
  
  /**
   * Checks the type of the field is as expected. The field must be of type file.
   *
   * @param typeName the name of the type.
   * @param contextCall the context of the call: which is the caller of this method. This parameter
   * is used for trace purpose.
   */
  protected void checkFieldType(final String typeName, final String contextCall) {
    if (!Field.TYPE_FILE.equals(typeName)) {
      SilverTrace.info("form", contextCall, "form.INFO_NOT_CORRECT_TYPE", Field.TYPE_FILE);
    }
  }
}
