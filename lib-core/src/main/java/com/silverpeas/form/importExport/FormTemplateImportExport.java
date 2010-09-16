/**
 * Copyright (C) 2000 - 2009 Silverpeas
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
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.form.importExport;

import java.io.File;
import java.util.Date;
import java.util.List;

import com.silverpeas.form.AbstractForm;
import com.silverpeas.form.DataRecord;
import com.silverpeas.form.Field;
import com.silverpeas.form.FieldDisplayer;
import com.silverpeas.form.FieldTemplate;
import com.silverpeas.form.PagesContext;
import com.silverpeas.form.RecordSet;
import com.silverpeas.form.TypeManager;
import com.silverpeas.publicationTemplate.PublicationTemplate;
import com.silverpeas.publicationTemplate.PublicationTemplateManager;
import com.silverpeas.util.ForeignPK;
import com.silverpeas.util.StringUtil;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.attachment.control.AttachmentController;
import com.stratelia.webactiv.util.attachment.ejb.AttachmentPK;
import com.stratelia.webactiv.util.attachment.model.AttachmentDetail;
import com.stratelia.webactiv.util.fileFolder.FileFolderManager;

public class FormTemplateImportExport {

  public void importXMLModelContentType(ForeignPK pk, String objectType,
      XMLModelContentType xmlModel, String userId) throws Exception {
    String externalId = pk.getInstanceId() + ":" + xmlModel.getName();
    if (StringUtil.isDefined(objectType)) {
      externalId = pk.getInstanceId() + ":" + objectType + ":"
          + xmlModel.getName();
    }

    PublicationTemplateManager.addDynamicPublicationTemplate(externalId,
        xmlModel.getName());

    PublicationTemplate pub = PublicationTemplateManager
        .getPublicationTemplate(externalId);

    RecordSet set = pub.getRecordSet();

    DataRecord data = set.getRecord(pk.getId());
    if (data == null) {
      data = set.getEmptyRecord();
      data.setId(pk.getId());
    }

    List<XMLField> xmlFields = xmlModel.getFields();
    XMLField xmlField = null;
    Field field = null;
    String xmlFieldName = null;
    String xmlFieldValue = null;
    String fieldValue = null;
    for (int f = 0; f < xmlFields.size(); f++) {
      xmlField = xmlFields.get(f);
      xmlFieldName = xmlField.getName();
      xmlFieldValue = xmlField.getValue();
      field = data.getField(xmlFieldName);
      if (field != null) {
        FieldTemplate fieldTemplate = pub.getRecordTemplate().getFieldTemplate(
            xmlFieldName);
        if (fieldTemplate != null) {
          FieldDisplayer fieldDisplayer = TypeManager.getDisplayer(field
              .getTypeName(), fieldTemplate.getDisplayerName());
          if (Field.TYPE_FILE.equals(field.getTypeName())) {
            String context = null;
            if (fieldTemplate.getDisplayerName().equals("image")) {
              context = AbstractForm.CONTEXT_FORM_IMAGE;
            }
            else {
              context = AbstractForm.CONTEXT_FORM_FILE;
            }

            String imagePath = xmlFieldValue;
            String imageName = imagePath.substring(imagePath
                .lastIndexOf(File.separator) + 1, imagePath.length());
            String imageExtension = FileRepositoryManager
                .getFileExtension(imagePath);
            String imageMimeType = AttachmentController.getMimeType(imagePath);

            String physicalName = new Long(new Date().getTime()).toString()
                + "." + imageExtension;

            String path = AttachmentController.createPath(pk.getInstanceId(),
                context);
            FileRepositoryManager.copyFile(imagePath, path + physicalName);
            File file = new File(path + physicalName);
            long size = file.length();

            if (size > 0) {
              AttachmentDetail ad = createAttachmentDetail(pk.getId(), pk
                  .getInstanceId(), physicalName, imageName, imageMimeType,
                  size, context, userId);
              ad = AttachmentController.createAttachment(ad, true);
              fieldValue = ad.getPK().getId();
            } else {
              // le fichier à tout de même été créé sur le serveur avec
              // une
              // taille 0!, il faut le supprimer
              FileFolderManager.deleteFolder(path + physicalName);
            }
          } else {
            fieldValue = xmlFieldValue;
          }

          fieldDisplayer.update(fieldValue, field, fieldTemplate,
              new PagesContext());
        }
      }
    }
    set.save(data);
  }

  private AttachmentDetail createAttachmentDetail(String objectId,
      String componentId, String physicalName, String logicalName,
      String mimeType, long size, String context, String userId) {
    // create AttachmentPK with spaceId and componentId
    AttachmentPK atPK = new AttachmentPK(null, "useless", componentId);

    // create foreignKey with spaceId, componentId and id
    // use AttachmentPK to build the foreign key of customer object.
    AttachmentPK foreignKey = new AttachmentPK("-1", "useless", componentId);
    if (objectId != null) {
      foreignKey.setId(objectId);
    }

    // create AttachmentDetail Object
    AttachmentDetail ad = new AttachmentDetail(atPK, physicalName, logicalName,
        null, mimeType, size, context, new Date(), foreignKey);
    ad.setAuthor(userId);

    return ad;
  }
}