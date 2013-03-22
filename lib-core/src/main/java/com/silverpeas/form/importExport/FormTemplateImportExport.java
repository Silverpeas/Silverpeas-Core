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
package com.silverpeas.form.importExport;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.silverpeas.attachment.AttachmentServiceFactory;
import org.silverpeas.attachment.model.DocumentType;
import org.silverpeas.attachment.model.SimpleAttachment;
import org.silverpeas.attachment.model.SimpleDocument;
import org.silverpeas.attachment.model.SimpleDocumentPK;

import com.silverpeas.form.DataRecord;
import com.silverpeas.form.Field;
import com.silverpeas.form.FieldDisplayer;
import com.silverpeas.form.FieldTemplate;
import com.silverpeas.form.PagesContext;
import com.silverpeas.form.RecordSet;
import com.silverpeas.form.TypeManager;
import com.silverpeas.publicationTemplate.PublicationTemplate;
import com.silverpeas.publicationTemplate.PublicationTemplateManager;
import com.silverpeas.util.FileUtil;
import com.silverpeas.util.ForeignPK;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.i18n.I18NHelper;

public class FormTemplateImportExport {

  public void importXMLModelContentType(ForeignPK pk, String objectType,
      XMLModelContentType xmlModel, String userId) throws Exception {
    String externalId = pk.getInstanceId() + ":" + xmlModel.getName();
    if (StringUtil.isDefined(objectType)) {
      externalId = pk.getInstanceId() + ":" + objectType + ":" + xmlModel.getName();
    }

    PublicationTemplateManager.getInstance().addDynamicPublicationTemplate(externalId, xmlModel.
        getName());
    PublicationTemplate pub = PublicationTemplateManager.getInstance().getPublicationTemplate(
        externalId);

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
          FieldDisplayer<Field> fieldDisplayer = TypeManager.getInstance().
              getDisplayer(field.getTypeName(), fieldTemplate.getDisplayerName());
          String fieldValue;
          if (Field.TYPE_FILE.equals(field.getTypeName())) {
            fieldValue = manageFileField(pk, userId, xmlFieldValue, fieldTemplate);
          } else {
            fieldValue = xmlFieldValue;
          }
          fieldDisplayer.update(fieldValue, field, fieldTemplate, new PagesContext());
        }
      }
    }
    set.save(data);
  }

  public String manageFileField(ForeignPK pk, String userId, String xmlFieldValue,
      FieldTemplate fieldTemplate) throws IOException {
    String fieldValue;
    DocumentType type;
    if ("image".equals(fieldTemplate.getDisplayerName())) {
      type = DocumentType.attachment;
    }
    else {
      type = DocumentType.form;
    }
    File image = new File(xmlFieldValue);
    if (image.length() > 0L) {
      String fileName = FileUtil.getFilename(xmlFieldValue);
      SimpleDocument document = new SimpleDocument(new SimpleDocumentPK(null, pk.getInstanceId()),
          pk.getId(), 0, false, new SimpleAttachment(fileName, I18NHelper.defaultLanguage, fileName,
          "", image.length(), FileUtil.getMimeType(fileName), userId, new Date(), null));
      document.setDocumentType(type);
      fieldValue = AttachmentServiceFactory.getAttachmentService().createAttachment(document, image,
          true).getId();
    }
    else {
      fieldValue = null;
    }
    return fieldValue;
  }
}