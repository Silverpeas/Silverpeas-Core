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
package com.silverpeas.formTemplate.ejb;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import com.silverpeas.form.DataRecord;
import com.silverpeas.form.Field;
import com.silverpeas.form.FieldTemplate;
import com.silverpeas.form.RecordSet;
import com.silverpeas.form.displayers.WysiwygFCKFieldDisplayer;
import com.silverpeas.form.fieldType.FileField;
import com.silverpeas.form.importExport.XMLField;
import com.silverpeas.publicationTemplate.PublicationTemplate;
import com.silverpeas.publicationTemplate.PublicationTemplateImpl;
import com.silverpeas.publicationTemplate.PublicationTemplateManager;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import com.stratelia.webactiv.util.exception.UtilException;

@Stateless(name = "FormTemplate", description =
    "Stateless EJB to manage access to record and content of forms")
@TransactionAttribute(TransactionAttributeType.SUPPORTS)
public class FormTemplateBmEJB implements FormTemplateBm {

  private static final long serialVersionUID = 339452185858865016L;

  public FormTemplateBmEJB() {
  }

  @Override
  public DataRecord getRecord(String externalId, String id) {
    SilverTrace.info("form", "FormTemplateBmEJB.getRecord",
        "root.MSG_GEN_ENTER_METHOD", "externalId = " + externalId + ", id = " + id);
    PublicationTemplate pub = getPublicationTemplate(externalId);
    return getRecord(id, pub, null);
  }

  @Override
  public PublicationTemplate getPublicationTemplate(String externalId) {
    try {
      return PublicationTemplateManager.getInstance().getPublicationTemplate(externalId);
    } catch (Exception e) {
      throw new FormTemplateBmRuntimeException("FormTemplateBmEJB.getPublicationTemplate",
          SilverpeasException.ERROR, "Getting template '" + externalId + "' failed !", e);
    }
  }

  @Override
  public List<XMLField> getXMLFieldsForExport(String externalId, String id) {
    return getXMLFieldsForExport(externalId, id, null);
  }

  @Override
  public List<XMLField> getXMLFieldsForExport(String externalId, String id, String language) {
    SilverTrace.info("form", "FormTemplateBmEJB.getXMLFields", "root.MSG_GEN_ENTER_METHOD",
        "externalId = " + externalId + ", id = " + id + ", language = " + language);

    PublicationTemplateImpl template = (PublicationTemplateImpl) getPublicationTemplate(externalId);
    DataRecord data = getRecord(id, template, language);
    List<XMLField> fields = new ArrayList<XMLField>();
    if (data != null) {
      SilverTrace.debug("form", "FormTemplateBmEJB.getXMLFields", "root.MSG_GEN_PARAM_VALUE",
          "data != null");
      try {
        String[] fieldNames = template.getRecordTemplate().getFieldNames();
        for (int f = 0; fieldNames != null && f < fieldNames.length; f++) {
          String fieldName = fieldNames[f];
          SilverTrace.debug("form", "FormTemplateBmEJB.getXMLFields", "root.MSG_GEN_PARAM_VALUE",
              "fieldName = " + fieldName);
          Field field = data.getField(fieldName);
          if (field != null) {
            SilverTrace.debug("form", "FormTemplateBmEJB.getXMLFields", "root.MSG_GEN_PARAM_VALUE",
                "field != null");
            String fieldValue = field.getStringValue();
            if (field.getTypeName().equals(FileField.TYPE)) {
              FieldTemplate fieldTemplate =
                  template.getRecordTemplate().getFieldTemplate(fieldName);
              if (fieldTemplate != null) {
                if ("image".equals(fieldTemplate.getDisplayerName())) {
                  fieldValue = "image_" + fieldValue;
                } else {
                  fieldValue = "file_" + fieldValue;
                }
              }
            }
            XMLField xmlField = new XMLField(fieldName, fieldValue);
            fields.add(xmlField);
          }
        }
      } catch (Exception e) {
        throw new FormTemplateBmRuntimeException("FormTemplateBmEJB.getXMLFields",
            SilverpeasException.ERROR, "Getting fields for externalId = " + externalId
            + " and id = " + id + " failed !", e);
      }
    }
    return fields;
  }

  @Override
  public String getWysiwygContent(String componentId, String objectId, String fieldName,
      String language) {
    String wysiwygContent = null;
    try {
      wysiwygContent = WysiwygFCKFieldDisplayer.getContentFromFile(componentId, objectId, fieldName,
          language);
    } catch (UtilException e) {
      throw new FormTemplateBmRuntimeException(
          "FormTemplateBmEJB.getWysiwigContent", SilverpeasException.ERROR,
          "Getting Wysiwig content for componentId = " + componentId + " and id = " + objectId
          + " failed !", e);
    }
    return wysiwygContent;
  }

  private DataRecord getRecord(String id, PublicationTemplate pub, String language) {
    try {
      RecordSet set = pub.getRecordSet();
      return set.getRecord(id, language);
    } catch (Exception e) {
      throw new FormTemplateBmRuntimeException("FormTemplateBmEJB.getRecord",
          SilverpeasException.ERROR, "Getting record for id '" + id + " failed !", e);
    }
  }
}