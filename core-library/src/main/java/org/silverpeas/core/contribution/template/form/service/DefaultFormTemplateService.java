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
package org.silverpeas.core.contribution.template.form.service;

import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.contribution.content.form.DataRecord;
import org.silverpeas.core.contribution.content.form.Field;
import org.silverpeas.core.contribution.content.form.FieldTemplate;
import org.silverpeas.core.contribution.content.form.RecordSet;
import org.silverpeas.core.contribution.content.form.displayers.WysiwygFCKFieldDisplayer;
import org.silverpeas.core.contribution.content.form.field.FileField;
import org.silverpeas.core.contribution.content.form.XMLField;
import org.silverpeas.core.contribution.template.publication.PublicationTemplate;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateImpl;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateManager;
import org.silverpeas.core.exception.SilverpeasException;
import org.silverpeas.core.exception.UtilException;

import javax.inject.Singleton;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
@Singleton
@Transactional(Transactional.TxType.SUPPORTS)
public class DefaultFormTemplateService implements FormTemplateService {

  public DefaultFormTemplateService() {
  }

  @Override
  public DataRecord getRecord(String externalId, String id) {

    PublicationTemplate pub = getPublicationTemplate(externalId);
    return getRecord(id, pub, null);
  }

  @Override
  public PublicationTemplate getPublicationTemplate(String externalId) {
    try {
      return PublicationTemplateManager.getInstance().getPublicationTemplate(externalId);
    } catch (Exception e) {
      throw new FormTemplateRuntimeException("DefaultFormTemplateService.getPublicationTemplate",
          SilverpeasException.ERROR, "Getting template '" + externalId + "' failed !", e);
    }
  }

  @Override
  public List<XMLField> getXMLFieldsForExport(String externalId, String id) {
    return getXMLFieldsForExport(externalId, id, null);
  }

  @Override
  public List<XMLField> getXMLFieldsForExport(String externalId, String id, String language) {
    PublicationTemplateImpl template = (PublicationTemplateImpl) getPublicationTemplate(externalId);
    DataRecord data = getRecord(id, template, language);
    List<XMLField> fields = new ArrayList<>();
    if (data != null) {
      try {
        String[] fieldNames = template.getRecordTemplate().getFieldNames();
        for (int f = 0; fieldNames != null && f < fieldNames.length; f++) {
          String fieldName = fieldNames[f];
          FieldTemplate fieldTemplate =
              template.getRecordTemplate().getFieldTemplate(fieldName);
          if (fieldTemplate != null) {
            if (!fieldTemplate.isRepeatable()) {
              Field field = data.getField(fieldName);
              XMLField xmlField = getXMLField(field, fieldTemplate);
              if (xmlField != null) {
                fields.add(xmlField);
              }
            } else {
              List<XMLField> xmlFields = getRepeatableXMLField(data, fieldTemplate);
              fields.addAll(xmlFields);
            }
          }
        }
      } catch (Exception e) {
        throw new FormTemplateRuntimeException("DefaultFormTemplateService.getXMLFields",
            SilverpeasException.ERROR, "Getting fields for externalId = " + externalId
            + " and id = " + id + " failed !", e);
      }
    }
    return fields;
  }

  private XMLField getXMLField(Field field, FieldTemplate fieldTemplate) {
    XMLField xmlField = null;
    if (field != null) {
      String fieldValue = field.getStringValue();
      if (field.getTypeName().equals(FileField.TYPE)) {
        if ("image".equals(fieldTemplate.getDisplayerName())) {
          fieldValue = "image_" + fieldValue;
        } else {
          fieldValue = "file_" + fieldValue;
        }
      }
      xmlField = new XMLField(field.getName(), fieldValue);
    }
    return xmlField;
  }

  private List<XMLField> getRepeatableXMLField(DataRecord dataRecord, FieldTemplate fieldTemplate) {
    int maxOccurrences = fieldTemplate.getMaximumNumberOfOccurrences();
    List<XMLField> xmlFields = new ArrayList<>();
    for (int occ = 0; occ < maxOccurrences; occ++) {
      Field fieldOcc = dataRecord.getField(fieldTemplate.getFieldName(), occ);
      if (fieldOcc != null && !fieldOcc.isNull()) {
        xmlFields.add(getXMLField(fieldOcc, fieldTemplate));
      }
    }
    return xmlFields;
  }

  @Override
  public String getWysiwygContent(String componentId, String objectId, String fieldName,
      String language) {
    try {
      return WysiwygFCKFieldDisplayer
          .getContentFromFile(componentId, objectId, fieldName, language);
    } catch (UtilException e) {
      throw new FormTemplateRuntimeException("DefaultFormTemplateService.getWysiwygContent",
          SilverpeasException.ERROR,
          "Getting Wysiwyg content for componentId = " + componentId + " and id = " + objectId +
              " failed !", e);
    }
  }

  private DataRecord getRecord(String id, PublicationTemplate pub, String language) {
    try {
      RecordSet set = pub.getRecordSet();
      return set.getRecord(id, language);
    } catch (Exception e) {
      throw new FormTemplateRuntimeException("DefaultFormTemplateService.getRecord",
          SilverpeasException.ERROR, "Getting record for id '" + id + " failed !", e);
    }
  }
}