/*
 * Copyright (C) 2000 - 2016 Silverpeas
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

package org.silverpeas.core.contact.model;

import org.silverpeas.core.contribution.content.form.DataRecord;
import org.silverpeas.core.contribution.content.form.Field;
import org.silverpeas.core.contribution.content.form.FieldDisplayer;
import org.silverpeas.core.contribution.content.form.FieldTemplate;
import org.silverpeas.core.contribution.content.form.Form;
import org.silverpeas.core.contribution.content.form.FormException;
import org.silverpeas.core.contribution.content.form.PagesContext;
import org.silverpeas.core.contribution.content.form.RecordSet;
import org.silverpeas.core.contribution.content.form.TypeManager;
import org.silverpeas.core.contribution.content.form.record.GenericFieldTemplate;
import org.silverpeas.core.contribution.template.publication.PublicationTemplate;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateException;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateImpl;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateManager;
import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.core.admin.user.model.UserFull;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FilenameUtils;
import org.silverpeas.core.index.indexing.model.FullIndexEntry;
import org.silverpeas.core.util.StringUtil;

import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This object contains the description of a complete contact (contact parameter, model detail,
 * info)
 * @author Nicolas Eysseric
 * @version 1.0
 */
public class CompleteContact implements Contact, Serializable {
  private static final long serialVersionUID = 6098500884583430615L;

  private ContactDetail contactDetail;
  private String modelId;
  private List<FileItem> formItems;
  private List<String> formValues;
  private Map<String, String> formNamedValues;

  private String creatorLanguage;

  /**
   * Create a new CompleteContact
   * @param contactDetail the contact detail
   * @param modelId the modeil identifier
   * @see ContactDetail
   * @since 1.0
   */
  public CompleteContact(ContactDetail contactDetail, String modelId) {
    this.contactDetail = contactDetail;
    this.modelId = modelId;
  }

  public CompleteContact(String instanceId, String modelId) {
    this(new ContactDetail(new ContactPK(null, instanceId)), modelId);
  }

  /**
   * Get the contact parameters
   * @return a ContactDetail - the contact parameters
   * @see ContactDetail
   * @since 1.0
   */
  public ContactDetail getContactDetail() {
    return contactDetail;
  }

  /**
   * @return the model identifier
   */
  public String getModelId() {
    return modelId;
  }

  /**
   * @param modelId the model identifier to set
   */
  public void setModelId(String modelId) {
    this.modelId = modelId;
  }

  @Override
  public ContactPK getPK() {
    return contactDetail.getPK();
  }

  @Override
  public String getFirstName() {
    return contactDetail.getFirstName();
  }

  @Override
  public String getLastName() {
    return contactDetail.getLastName();
  }

  @Override
  public String getEmail() {
    return contactDetail.getEmail();
  }

  @Override
  public String getPhone() {
    return contactDetail.getPhone();
  }

  @Override
  public String getFax() {
    return contactDetail.getFax();
  }

  @Override
  public Date getCreationDate() {
    return contactDetail.getCreationDate();
  }

  public void setCreationDate(Date date) {
    contactDetail.setCreationDate(date);
  }

  @Override
  public String getCreatorId() {
    return contactDetail.getCreatorId();
  }

  public void setCreatorId(String id) {
    contactDetail.setCreatorId(id);
  }

  @Override
  public String getUserId() {
    return contactDetail.getUserId();
  }

  @Override
  public UserFull getUserFull() {
    return contactDetail.getUserFull();
  }

  public Form getUpdateForm() {
    return getForm(true);
  }

  public Form getViewForm() {
    return getForm(false);
  }

  public boolean isFormDefined() {
    return StringUtil.isDefined(modelId) && modelId.endsWith(".xml");
  }

  private String getFullTemplateId() {
    return getPK().getInstanceId() + ":" + getFormName();
  }

  private String getFormName() {
    return FilenameUtils.getBaseName(modelId);
  }

  private Form getForm(boolean updateMode) {
    Form form = null;
    try {
      if (isFormDefined()) {
        // création du PublicationTemplate
        PublicationTemplateManager templateManager = PublicationTemplateManager.getInstance();
        PublicationTemplateImpl pubTemplate = (PublicationTemplateImpl) templateManager.
            getPublicationTemplate(getFullTemplateId());

        // création du formulaire et du DataRecord
        if (updateMode) {
          form = pubTemplate.getUpdateForm();
        } else {
          form = pubTemplate.getViewForm();
        }
        RecordSet recordSet = pubTemplate.getRecordSet();
        DataRecord data = recordSet.getRecord(getPK().getId());
        if (data == null) {
          data = recordSet.getEmptyRecord();
          data.setId(getPK().getId());
        }
        form.setData(data);
      }
    } catch (Exception e) {
      SilverTrace.
          error("yellowpages", getClass().getSimpleName() + ".setForm()", "root.NO_EX_MESSAGE", e);
    }
    return form;
  }

  public void saveForm() throws PublicationTemplateException, FormException {
    if (isFormDefined()) {
      String xmlFormName = modelId;
      // création du PublicationTemplate
      String key = getFullTemplateId();
      PublicationTemplateManager templateManager = PublicationTemplateManager.getInstance();
      templateManager.addDynamicPublicationTemplate(key, xmlFormName);
      PublicationTemplateImpl pubTemplate = (PublicationTemplateImpl) templateManager.
          getPublicationTemplate(key, xmlFormName);

      Form formUpdate = pubTemplate.getUpdateForm();
      RecordSet recordSet = pubTemplate.getRecordSet();
      DataRecord data = recordSet.getRecord(getPK().getId());
      if (data == null) {
        data = recordSet.getEmptyRecord();
        data.setId(getPK().getId());// id contact
      }

      // sauvegarde des données du formulaire
      PagesContext context =
          new PagesContext("useless", "0", getCreatorLanguage(), false, getPK().getInstanceId(), getCreatorId());
      context.setObjectId(getPK().getId());

      if (getFormItems() != null && !getFormItems().isEmpty()) {
        formUpdate.update(getFormItems(), data, context);
      } else if (getFormValues() != null && !getFormValues().isEmpty()) {
        // getting fields using data.xml ordering
        FieldTemplate[] fieldTemplates = pubTemplate.getRecordTemplate().getFieldTemplates();

        int fieldIndex = 0;
        for (String value : getFormValues()) {
          if (StringUtil.isDefined(value)) {
            FieldTemplate fieldTemplate = fieldTemplates[fieldIndex];
            if (fieldTemplate != null) {
              String fieldName = fieldTemplate.getFieldName();
              data.getField(fieldName).setObjectValue(value);
            }
          }
          fieldIndex++;
        }
      } else if (getFormNamedValues() != null) {
        FieldTemplate[] fieldTemplates = pubTemplate.getRecordTemplate().getFieldTemplates();
        for (FieldTemplate fieldTemplate : fieldTemplates) {
          String fieldName = fieldTemplate.getFieldName();
          data.getField(fieldName).setObjectValue(getFormNamedValues().get(fieldName));
        }
      }
      recordSet.save(data);
    }
  }

  public void removeForm() throws PublicationTemplateException, FormException {
    if (isFormDefined()) {
      // recuperation des donnees du formulaire (via le DataRecord)
      PublicationTemplate pubTemplate =
          PublicationTemplateManager.getInstance().getPublicationTemplate(
              getFullTemplateId());
      RecordSet recordSet = pubTemplate.getRecordSet();
      DataRecord data = recordSet.getRecord(getPK().getId());
      recordSet.delete(data);
    }
  }

  public void indexForm(FullIndexEntry indexEntry) {
    if (isFormDefined()) {
      try {
        PublicationTemplate pub =
            PublicationTemplateManager.getInstance().getPublicationTemplate(getFullTemplateId());
        RecordSet set = pub.getRecordSet();
        set.indexRecord(contactDetail.getPK().getId(), getFormName(), indexEntry);
      } catch (Exception e) {
        SilverTrace.error("contact", "CompleteContact.indexForm", "", e);
      }
    }
  }

  public void setFormItems(List<FileItem> formItems) {
    this.formItems = formItems;
  }

  public List<FileItem> getFormItems() {
    return formItems;
  }

  public void setFormValues(List<String> formValues) {
    this.formValues = formValues;
  }

  public List<String> getFormValues() {
    return formValues;
  }

  public Map<String, String> getFormValues(String language, boolean onlyDefinedValues) {
    HashMap<String, String> formValues = new HashMap<String, String>();
    if (isFormDefined()) {
      DataRecord data = null;
      PublicationTemplate pub = null;
      try {
        pub = PublicationTemplateManager.getInstance().getPublicationTemplate(getFullTemplateId());
        data = pub.getRecordSet().getRecord(getPK().getId());
      } catch (Exception e) {
        SilverTrace.warn("contact", "CompleteContact.getFormValues", "CANT_GET_FORM_RECORD",
            "id = " + getPK().getId() + "infoId = " + getModelId());
      }

      if (data != null) {
        String fieldNames[] = data.getFieldNames();
        PagesContext pageContext = new PagesContext();
        pageContext.setLanguage(language);
        for (String fieldName : fieldNames) {
          try {
            Field field = data.getField(fieldName);
            GenericFieldTemplate fieldTemplate = (GenericFieldTemplate) pub.getRecordTemplate()
                .getFieldTemplate(fieldName);
            FieldDisplayer fieldDisplayer = TypeManager.getInstance().getDisplayer(fieldTemplate
                .getTypeName(), "simpletext");
            StringWriter sw = new StringWriter();
            PrintWriter out = new PrintWriter(sw);
            fieldDisplayer.display(out, field, fieldTemplate, pageContext);
            String value = sw.toString();
            if (!onlyDefinedValues || (onlyDefinedValues && StringUtil.isDefined(value))) {
              formValues.put(fieldName, sw.toString());
            }
          } catch (Exception e) {
            SilverTrace.warn("contact", "CompleteContact.getFormValues", "CANT_GET_FIELD_VALUE",
                "id = " + getPK().getId() + "fieldName = " + fieldName, e);
          }
        }
      }
    }
    return formValues;
  }

  public void setFormNamedValues(Map<String, String> formNamedValues) {
    this.formNamedValues = formNamedValues;
  }

  public Map<String, String> getFormNamedValues() {
    return formNamedValues;
  }

  public void setCreatorLanguage(String creatorLanguage) {
    this.creatorLanguage = creatorLanguage;
  }

  public String getCreatorLanguage() {
    return creatorLanguage;
  }

}