/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.importexport.form;

import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.contribution.attachment.AttachmentServiceProvider;
import org.silverpeas.core.contribution.attachment.model.DocumentType;
import org.silverpeas.core.contribution.attachment.model.SimpleAttachment;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.attachment.model.SimpleDocumentPK;
import org.silverpeas.core.contribution.content.form.DataRecord;
import org.silverpeas.core.contribution.content.form.Field;
import org.silverpeas.core.contribution.content.form.FieldDisplayer;
import org.silverpeas.core.contribution.content.form.FieldTemplate;
import org.silverpeas.core.contribution.content.form.FormException;
import org.silverpeas.core.contribution.content.form.PagesContext;
import org.silverpeas.core.contribution.content.form.RecordSet;
import org.silverpeas.core.contribution.content.form.TypeManager;
import org.silverpeas.core.contribution.content.form.XMLField;
import org.silverpeas.core.contribution.template.publication.PublicationTemplate;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateException;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateManager;
import org.silverpeas.core.i18n.I18NHelper;
import org.silverpeas.kernel.util.StringUtil;
import org.silverpeas.core.util.file.FileUtil;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

public class FormTemplateImportExport {

  public void importXMLModelContentType(ResourceReference pk, String objectType,
      XMLModelContentType xmlModel, String userId)
      throws PublicationTemplateException, FormException, IOException {
    String externalId = pk.getInstanceId() + ":" + xmlModel.getName();
    if (StringUtil.isDefined(objectType)) {
      externalId = pk.getInstanceId() + ":" + objectType + ":" + xmlModel.getName();
    }

    PublicationTemplateManager.getInstance()
        .addDynamicPublicationTemplate(externalId, xmlModel.getName());
    PublicationTemplate pub = PublicationTemplateManager.getInstance()
        .getPublicationTemplate(externalId);

    RecordSet set = pub.getRecordSet();

    DataRecord data = set.getRecord(pk.getId());
    if (data == null) {
      data = set.getEmptyRecord();
      data.setId(pk.getId());
    }

    List<XMLField> xmlFields = xmlModel.getFields();
    for (XMLField xmlField : xmlFields) {
      String xmlFieldName = xmlField.getName();
      String xmlFieldValue = xmlField.getValue();
      Field field = data.getField(xmlFieldName);
      if (field != null) {
        FieldTemplate fieldTemplate = pub.getRecordTemplate().getFieldTemplate(xmlFieldName);
        if (fieldTemplate != null) {
          FieldDisplayer<Field> fieldDisplayer = TypeManager.getInstance()
              .getDisplayer(field.getTypeName(), fieldTemplate.getDisplayerName());
          String fieldValue = xmlFieldValue;
          if (Field.TYPE_FILE.equals(field.getTypeName())) {
            fieldValue = manageFileField(pk, userId, xmlFieldValue);
          }
          fieldDisplayer.update(fieldValue, field, fieldTemplate, new PagesContext());
        }
      }
    }
    set.save(data);
  }

  public String manageFileField(ResourceReference pk, String userId, String xmlFieldValue)
      throws IOException {
    String fieldValue = null;
    DocumentType type = DocumentType.form;
    File image = new File(xmlFieldValue);
    if (image.length() > 0L) {
      String fileName = FileUtil.getFilename(xmlFieldValue);
      SimpleAttachment attachment = SimpleAttachment.builder(I18NHelper.DEFAULT_LANGUAGE)
          .setFilename(fileName)
          .setTitle(fileName)
          .setDescription("")
          .setSize(image.length())
          .setContentType(FileUtil.getMimeType(fileName))
          .setCreationData(userId, new Date())
          .build();
      SimpleDocument document =
          new SimpleDocument(new SimpleDocumentPK(null, pk.getInstanceId()), pk.getId(), 0, false,
              attachment);
      document.setDocumentType(type);
      fieldValue = AttachmentServiceProvider.getAttachmentService()
          .createAttachment(document, image, true)
          .getId();
    }
    return fieldValue;
  }
}