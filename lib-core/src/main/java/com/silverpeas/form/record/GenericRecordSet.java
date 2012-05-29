/**
 * Copyright (C) 2000 - 2011 Silverpeas
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

package com.silverpeas.form.record;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

import com.silverpeas.form.DataRecord;
import com.silverpeas.form.Field;
import com.silverpeas.form.FieldDisplayer;
import com.silverpeas.form.FieldTemplate;
import com.silverpeas.form.FormException;
import com.silverpeas.form.RecordSet;
import com.silverpeas.form.RecordTemplate;
import com.silverpeas.form.TypeManager;
import com.silverpeas.form.displayers.ImageFieldDisplayer;
import com.silverpeas.form.displayers.VideoFieldDisplayer;
import com.silverpeas.form.displayers.WysiwygFCKFieldDisplayer;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.i18n.I18NHelper;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.attachment.control.AttachmentController;
import com.stratelia.webactiv.util.attachment.ejb.AttachmentException;
import com.stratelia.webactiv.util.attachment.ejb.AttachmentPK;
import com.stratelia.webactiv.util.indexEngine.model.FullIndexEntry;

/**
 * The GenericRecordSet manage DataRecord built on a RecordTemplate and saved by the
 * GenericRecordSetManager.
 * @see DataRecord
 */
public class GenericRecordSet implements RecordSet, Serializable {

  private static final long serialVersionUID = 1L;
  private IdentifiedRecordTemplate recordTemplate = null;

  /**
   * The generic record set is built upon a RecordTemplate.
   */
  public GenericRecordSet(IdentifiedRecordTemplate recordTemplate) {
    this.recordTemplate = recordTemplate;
  }

  /**
   * Returns the RecordTemplate shared by all the DataRecord of this RecordSet.
   */
  @Override
  public RecordTemplate getRecordTemplate() {
    return recordTemplate;
  }

  /**
   * Returns an empty DataRecord built on the RecordTemplate. This record is not yet managed by this
   * RecordSet. This is only an empty record which must be filled and saved in order to become a
   * DataRecord of this RecordSet.
   */
  @Override
  public DataRecord getEmptyRecord() throws FormException {
    return recordTemplate.getEmptyRecord();
  }

  /**
   * Returns the DataRecord with the given id.
   * @throw FormException when the id is unknown.
   */
  @Override
  public DataRecord getRecord(String recordId) throws FormException {
    return getGenericRecordSetManager().getRecord(recordTemplate, recordId);
  }

  /**
   * Returns the DataRecord with the given id.
   * @throw FormException when the id is unknown.
   */
  @Override
  public DataRecord getRecord(String recordId, String language)
      throws FormException {
    if (!I18NHelper.isI18N || I18NHelper.isDefaultLanguage(language)) {
      language = null;
    }
    return getGenericRecordSetManager().getRecord(recordTemplate, recordId, language);
  }

  /**
   * Inserts the given DataRecord and set its id.
   * @throw FormException when the record doesn't have the required template.
   * @throw FormException when the record has a not null id.
   * @throw FormException when the insert fail.
   */
  private void insert(DataRecord record) throws FormException {
    recordTemplate.checkDataRecord(record);
    getGenericRecordSetManager().insertRecord(recordTemplate, record);
  }

  /**
   * Updates the given DataRecord.
   * @throw FormException when the record doesn't have the required template.
   * @throw FormException when the record has a null or unknown id.
   * @throw FormException when the update fail.
   */
  private void update(DataRecord record) throws FormException {
    recordTemplate.checkDataRecord(record);
    getGenericRecordSetManager().updateRecord(recordTemplate, record);
  }

  /**
   * Save the given DataRecord. If the record id is null then the record is inserted in this
   * RecordSet. Else the record is updated.
   * @see insert
   * @see update
   * @throw FormException when the record doesn't have the required template.
   * @throw FormException when the record has an unknown id.
   * @throw FormException when the insert or update fail.
   */
  @Override
  public void save(DataRecord record) throws FormException {
    if (record.isNew()) {
      insert(record);
    } else {
      update(record);
    }
  }

  private void indexRecord(String recordId, String formName,
      FullIndexEntry indexEntry, String language) throws FormException {
    SilverTrace.info("form", "GenericRecordSet.index()",
        "root.MSG_GEN_ENTER_METHOD", "recordId = " + recordId + ", language = "
        + language);
    DataRecord data = getRecord(recordId, language);
    if (data != null) {
      String[] fieldNames = data.getFieldNames();
      Field field = null;
      for (String fieldName : fieldNames) {
        field = data.getField(fieldName);
        if (field != null) {
          FieldTemplate fieldTemplate = recordTemplate.getFieldTemplate(fieldName);
          if (fieldTemplate != null) {
            String fieldType = fieldTemplate.getTypeName();
            String fieldDisplayerName = fieldTemplate.getDisplayerName();
            try {
              if (!StringUtil.isDefined(fieldDisplayerName)) {
                fieldDisplayerName = TypeManager.getInstance().getDisplayerName(fieldType);
              }
              FieldDisplayer fieldDisplayer = TypeManager.getInstance().getDisplayer(
                  fieldType, fieldDisplayerName);
              if (fieldDisplayer != null) {
                String key = formName + "$$" + fieldName;
                fieldDisplayer.index(indexEntry, key, fieldName, field, language,
                    fieldTemplate.isUsedAsFacet());
              }
            } catch (FormException fe) {
              SilverTrace.error("form", "AbstractForm.update",
                  "form.EXP_UNKNOWN_FIELD", null, fe);
            } catch (Exception e) {
              SilverTrace.error("form", "AbstractForm.update",
                  "form.EXP_UNKNOWN_FIELD", null, e);
            }
          }
        }
      }
    }
  }

  @Override
  public void indexRecord(String recordId, String formName, FullIndexEntry indexEntry)
      throws FormException {
    if (!I18NHelper.isI18N) {
      indexRecord(recordId, formName, indexEntry, null);
    } else {
      List<String> languages =
          getGenericRecordSetManager().getLanguagesOfRecord(recordTemplate, recordId);
      for (String language : languages) {
        indexRecord(recordId, formName, indexEntry, language);
      }
    }
  }

  /**
   * Deletes the given DataRecord and set to null its id.
   * @throw FormException when the record doesn't have the required template.
   * @throw FormException when the record has an unknown id.
   * @throw FormException when the delete fail.
   */
  @Override
  public void delete(DataRecord record) throws FormException {
    if (record != null) {
      getGenericRecordSetManager().deleteRecord(recordTemplate, record);
    }
  }

  @Override
  public void clone(String originalExternalId, String originalComponentId, String cloneExternalId,
      String cloneComponentId) throws FormException {
    GenericDataRecord record = (GenericDataRecord) getRecord(originalExternalId);
    record.setInternalId(-1);
    record.setId(cloneExternalId);
    
    // clone wysiwyg fields content
    WysiwygFCKFieldDisplayer wysiwygDisplayer = new WysiwygFCKFieldDisplayer();
    try {
      wysiwygDisplayer.cloneContents(originalComponentId, originalExternalId, cloneComponentId,
          cloneExternalId);
    } catch (Exception e) {
      SilverTrace.error("form", "AbstractForm.clone", "form.EX_CLONE_FAILURE", null, e);
    }
    
    // clone images and videos
    AttachmentPK fromPK = new AttachmentPK(originalExternalId, originalComponentId);
    AttachmentPK toPK = new AttachmentPK(cloneExternalId, cloneComponentId);
    try {
      HashMap<String, String> ids =
          AttachmentController.cloneAttachments(fromPK, toPK,
              ImageFieldDisplayer.CONTEXT_FORM_IMAGE);

      HashMap<String, String> videoIds =
          AttachmentController.cloneAttachments(fromPK, toPK,
              VideoFieldDisplayer.CONTEXT_FORM_VIDEO);
      ids.putAll(videoIds);
      
      replaceIds(ids, record);
    } catch (AttachmentException e) {
      throw new FormException("form", "", e);
    }
    
    insert(record);
  }

  @Override
  public void merge(String fromExternalId, String fromComponentId, String toExternalId,
      String toComponentId) throws FormException {
    GenericDataRecord fromRecord = (GenericDataRecord) getRecord(fromExternalId);
    GenericDataRecord toRecord = (GenericDataRecord) getRecord(toExternalId);

    fromRecord.setInternalId(toRecord.getInternalId());
    fromRecord.setId(toExternalId);

    // merge wysiwyg fields content
    WysiwygFCKFieldDisplayer wysiwygDisplayer = new WysiwygFCKFieldDisplayer();
    try {
      wysiwygDisplayer.mergeContents(fromComponentId, fromExternalId, toComponentId, toExternalId);
    } catch (Exception e) {
      SilverTrace.error("form", "AbstractForm.clone", "form.EX_MERGE_FAILURE", null, e);
    }
    
    // merge images and videos
    AttachmentPK fromPK = new AttachmentPK(fromExternalId, fromComponentId);
    AttachmentPK toPK = new AttachmentPK(toExternalId, toComponentId);
    try {
      HashMap<String, String> ids =
          AttachmentController.mergeAttachments(toPK, fromPK,
              ImageFieldDisplayer.CONTEXT_FORM_IMAGE);
      
      HashMap<String, String> videoIds =
        AttachmentController.mergeAttachments(toPK, fromPK,
            VideoFieldDisplayer.CONTEXT_FORM_VIDEO);
      ids.putAll(videoIds);
    
      replaceIds(ids, fromRecord);
    } catch (AttachmentException e) {
      throw new FormException("form", "", e);
    }
    
    update(fromRecord);
  }
  
  private void replaceIds(HashMap<String, String> ids, GenericDataRecord record)
      throws FormException {
    String[] fieldNames = record.getFieldNames();
    Field field = null;
    for (String fieldName : fieldNames) {
      field = record.getField(fieldName);
      if (field != null) {
        FieldTemplate fieldTemplate = recordTemplate.getFieldTemplate(fieldName);
        if (fieldTemplate != null) {
          String fieldType = fieldTemplate.getTypeName();
          String fieldDisplayerName = fieldTemplate.getDisplayerName();
          try {
            if (!StringUtil.isDefined(fieldDisplayerName)) {
              fieldDisplayerName = TypeManager.getInstance().getDisplayerName(fieldType);
            }
            if ("image".equals(fieldDisplayerName) || "video".equals(fieldDisplayerName)) {
              if (ids.containsKey(field.getStringValue())) {
                field.setStringValue(ids.get(field.getStringValue()));
              }
            }
          } catch (Exception e) {
            SilverTrace.error("form", "AbstractForm.update",
                "form.EXP_UNKNOWN_FIELD", null, e);
          }
        }
      }
    }
  }
  
  /**
   * Gets an instance of a GenericRecordSet objects manager.
   * @return a GenericRecordSetManager instance.
   */
  protected GenericRecordSetManager getGenericRecordSetManager() {
    return GenericRecordSetManager.getInstance();
  }

}