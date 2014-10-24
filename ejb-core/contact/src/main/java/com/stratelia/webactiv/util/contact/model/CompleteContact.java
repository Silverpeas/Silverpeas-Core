/**
 * Copyright (C) 2000 - 2013 Silverpeas
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

package com.stratelia.webactiv.util.contact.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FilenameUtils;
import org.silverpeas.search.indexEngine.model.FullIndexEntry;

import com.silverpeas.form.DataRecord;
import com.silverpeas.form.FieldTemplate;
import com.silverpeas.form.Form;
import com.silverpeas.form.FormException;
import com.silverpeas.form.PagesContext;
import com.silverpeas.form.RecordSet;
import com.silverpeas.publicationTemplate.PublicationTemplate;
import com.silverpeas.publicationTemplate.PublicationTemplateException;
import com.silverpeas.publicationTemplate.PublicationTemplateImpl;
import com.silverpeas.publicationTemplate.PublicationTemplateManager;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.UserFull;

/**
 * This object contains the description of a complete contact (contact parameter, model detail,
 * info)
 * @author Nicolas Eysseric
 * @version 1.0
 */
public class CompleteContact implements Contact, Serializable {

  private ContactDetail contactDetail;
  private String modelId;
  private List<FileItem> formItems;
  private List<String> formValues;

  /**
   * Create a new CompleteContact
   * @param pubDetail
   * @param modelId
   * @see com.stratelia.webactiv.util.contact.model.PulicationDetail
   * @since 1.0
   */
  public CompleteContact(ContactDetail pubDetail, String modelId) {
    this.contactDetail = pubDetail;
    this.modelId = modelId;
  }
  
  public CompleteContact(String instanceId, String modelId) {
    this(new ContactDetail(new ContactPK(null, null, instanceId)), modelId);
  }

  /**
   * Get the contact parameters
   * @return a ContactDetail - the contact parameters
   * @see com.stratelia.webactiv.util.contact.model.PulicationDetail
   * @since 1.0
   */
  public ContactDetail getContactDetail() {
    return contactDetail;
  }

  /**
   * @return
   */
  public String getModelId() {
    return modelId;
  }

  /**
   * @param modelId
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
  
  private boolean isFormDefined() {
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
  
  public void saveForm(String language) throws PublicationTemplateException, FormException {
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
      PagesContext context = new PagesContext("modelForm", "0", language, false, getPK().getInstanceId(), getCreatorId());
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
      }
      recordSet.save(data);
    }
  }
  
  public void removeForm() throws PublicationTemplateException, FormException {
    if (isFormDefined()) {
      // recuperation des donnees du formulaire (via le DataRecord)
      PublicationTemplate pubTemplate = PublicationTemplateManager.getInstance().getPublicationTemplate(
          getFullTemplateId());
      RecordSet recordSet = pubTemplate.getRecordSet();
      DataRecord data = recordSet.getRecord(getPK().getId());
      recordSet.delete(data);
    }
  }
  
  public void indexForm(FullIndexEntry indexEntry) {
    if (isFormDefined()) {
      try {
        PublicationTemplate pub = PublicationTemplateManager.getInstance().getPublicationTemplate(getFullTemplateId());
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

}