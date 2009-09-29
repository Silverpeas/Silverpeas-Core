package com.silverpeas.form.importExport;

import java.io.File;
import java.util.Date;
import java.util.List;

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
    if (StringUtil.isDefined(objectType))
      externalId = pk.getInstanceId() + ":" + objectType + ":"
          + xmlModel.getName();

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

    List xmlFields = xmlModel.getFields();
    XMLField xmlField = null;
    Field field = null;
    String xmlFieldName = null;
    String xmlFieldValue = null;
    String fieldValue = null;
    for (int f = 0; f < xmlFields.size(); f++) {
      xmlField = (XMLField) xmlFields.get(f);
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
            if (fieldTemplate.getDisplayerName().equals("image"))
              context = "XMLFormImages";
            else
              context = "Images";

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
              // le fichier à tout de même été créé sur le serveur avec une
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
    if (objectId != null)
      foreignKey.setId(objectId);

    // create AttachmentDetail Object
    AttachmentDetail ad = new AttachmentDetail(atPK, physicalName, logicalName,
        null, mimeType, size, context, new Date(), foreignKey);
    ad.setAuthor(userId);

    return ad;
  }
}
