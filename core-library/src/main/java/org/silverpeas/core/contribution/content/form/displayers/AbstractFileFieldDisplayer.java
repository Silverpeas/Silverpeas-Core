/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
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
package org.silverpeas.core.contribution.content.form.displayers;

import org.silverpeas.core.contribution.content.form.Field;
import org.silverpeas.core.contribution.content.form.FieldTemplate;
import org.silverpeas.core.contribution.content.form.FormException;
import org.silverpeas.core.contribution.content.form.PagesContext;
import org.silverpeas.core.contribution.content.form.Util;
import org.silverpeas.core.contribution.content.form.field.FileField;
import org.silverpeas.core.silvertrace.SilverTrace;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.IOUtils;
import org.silverpeas.core.contribution.attachment.AttachmentServiceProvider;
import org.silverpeas.core.contribution.attachment.model.DocumentType;
import org.silverpeas.core.contribution.attachment.model.HistorisedDocument;
import org.silverpeas.core.contribution.attachment.model.SimpleAttachment;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.attachment.model.SimpleDocumentPK;
import org.silverpeas.core.util.file.FileUploadUtil;
import org.silverpeas.core.util.EncodeHelper;
import org.silverpeas.core.util.file.FileUtil;
import org.silverpeas.core.util.StringUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author ehugonnet
 */
public abstract class AbstractFileFieldDisplayer extends AbstractFieldDisplayer<FileField> {

  protected static final String OPERATION_KEY = "Operation";

  /**
   * The different kinds of operation that can be applied into an attached file.
   */
  protected enum Operation {
    ADD, UPDATE, DELETION
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
      return AttachmentServiceProvider.getAttachmentService().createAttachment(document, in, false);
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
    SimpleDocumentPK pk = new SimpleDocumentPK(attachmentId, pageContext.getComponentId());
    SimpleDocument doc = AttachmentServiceProvider.getAttachmentService().searchDocumentById(pk,
        pageContext.getContentLanguage());
    if (doc != null) {
      AttachmentServiceProvider.getAttachmentService().deleteAttachment(doc);
    }
  }

  /**
   * Returns the name of the managed types.
   */
  public String[] getManagedTypes() {
    return new String[]{FileField.TYPE};
  }

  /**
   * Prints the javascripts which will be used to control the new value given to the named field.
   * The error messages may be adapted to a local language. The FieldTemplate gives the field type
   * and constraints. The FieldTemplate gives the local labeld too. Never throws an Exception but
   * log a silvertrace and writes an empty string when :
   * <ul>
   * <li>the fieldName is unknown by the template.</li>
   * <li>the field type is not a managed type.</li>
   * </ul>
   */
  @Override
  public void displayScripts(final PrintWriter out, final FieldTemplate template,
      final PagesContext pageContext) throws IOException {
    checkFieldType(template.getTypeName(), "AbstractFileFieldDisplayer.displayScripts");
    String language = pageContext.getLanguage();
    String fieldName = template.getFieldName();
    if (template.isMandatory() && pageContext.useMandatory()) {
      out.append("  if (isWhitespace(stripInitialWhitespace(field.value))) {\n")
          .append("   var ").append(fieldName).append("Value = document.getElementById('")
          .append(fieldName).append(FileField.PARAM_ID_SUFFIX).append("').value;\n")
          .append("   var ").append(fieldName).append("Operation = document.")
          .append(pageContext.getFormName()).append(".")
          .append(fieldName).append(OPERATION_KEY).append(".value;\n")
          .append("   if (").append(fieldName).append("Value=='' || ")
          .append(fieldName).append("Operation=='").append(Operation.DELETION.name()).append(
              "') {\n")
          .append("     errorMsg+=\"  - '")
          .append(EncodeHelper.javaStringToJsString(template.getLabel(language))).append("' ")
          .append(Util.getString("GML.MustBeFilled", language)).append("\\n\";\n")
          .append("     errorNb++;\n")
          .append("   }\n")
          .append(" }\n");
    }

    if (!template.isReadOnly()) {
      Util.includeFileNameLengthChecker(template, pageContext, out);
      Util.getJavascriptChecker(template.getFieldName(), pageContext, out);
    }
  }

  @Override
  public List<String> update(List<FileItem> items, FileField field, FieldTemplate template,
      PagesContext pageContext) throws FormException {
    List<String> attachmentIds = new ArrayList<>();

    String attachmentId = processInput(items, field, pageContext);

    attachmentIds.addAll(update(attachmentId, field, template, pageContext));

    return attachmentIds;
  }

  protected String processInput(List<FileItem> items, FileField field, PagesContext pageContext) {
    try {
      String currentAttachmentId = field.getAttachmentId();
      String inputName = Util.getFieldOccurrenceName(field.getName(), field.getOccurrence());
      String attachmentId = processUploadedFile(items, inputName, pageContext);
      Operation operation = Operation.valueOf(FileUploadUtil.getParameter(items, inputName
          + OPERATION_KEY));
      if (!StringUtil.isDefined(attachmentId)) {
        // Trying to verify if a link is performed instead of uploading a real file
        String fileLinkOnApplication = FileUploadUtil.getParameter(items, inputName
            + Field.FILE_PARAM_NAME_SUFFIX);
        if (StringUtil.startsWith(fileLinkOnApplication, "/")) {
          // The identifier is a link to a file of an application.
          // The attachment identifier becomes the file link.
          if (isDeletion(operation, fileLinkOnApplication)) {
            attachmentId = null;
          } else {
            attachmentId = fileLinkOnApplication;
          }
        }
      }

      if (!pageContext.isCreation()) {
        boolean isDeletionOfCurrent = isDeletion(operation, currentAttachmentId);
        boolean isUpdate = StringUtil.isDefined(currentAttachmentId) && StringUtil.isDefined(
            attachmentId) && !currentAttachmentId.equals(attachmentId);
        boolean isAddOrUpdate = StringUtil.isDefined(attachmentId);
        if (isDeletionOfCurrent || isUpdate) {

          // Deletion of content
          if (!StringUtil.startsWith(currentAttachmentId, "/")) {
            // Current attachment identifier is a real one and is not a link to a resource of an
            // application. So, deleting the previous attachment.
            deleteAttachment(currentAttachmentId, pageContext);
          }

        } else if (!isAddOrUpdate) {

          // Same value
          return currentAttachmentId;
        }
      }

      // Add, update, delete or no value
      return attachmentId;
    } catch (IOException ex) {
      SilverTrace.error("form", "VideoFieldDisplayer.update", "form.EXP_UNKNOWN_FIELD", null, ex);
    }
    return null;
  }

  @Override
  public List<String> update(String attachmentId, FileField field, FieldTemplate template,
      PagesContext pagesContext) throws FormException {
    List<String> updated = new ArrayList<>();
    if (FileField.TYPE.equals(field.getTypeName())) {
      if (!StringUtil.isDefined(attachmentId)) {
        field.setNull();
      } else {
        field.setAttachmentId(attachmentId);
        updated.add(attachmentId);
      }
    } else {
      throw new FormException("FileFieldDisplayer.update", "form.EX_NOT_CORRECT_VALUE",
          FileField.TYPE);
    }
    return updated;
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

    }
  }
}
