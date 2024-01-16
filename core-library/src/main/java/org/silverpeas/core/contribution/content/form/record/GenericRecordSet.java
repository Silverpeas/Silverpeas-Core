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
package org.silverpeas.core.contribution.content.form.record;

import org.apache.commons.lang3.tuple.Pair;
import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.contribution.attachment.AttachmentException;
import org.silverpeas.core.contribution.attachment.AttachmentServiceProvider;
import org.silverpeas.core.contribution.attachment.model.DocumentType;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.attachment.model.SimpleDocumentPK;
import org.silverpeas.core.contribution.content.form.DataRecord;
import org.silverpeas.core.contribution.content.form.Field;
import org.silverpeas.core.contribution.content.form.FieldDisplayer;
import org.silverpeas.core.contribution.content.form.FieldTemplate;
import org.silverpeas.core.contribution.content.form.FormException;
import org.silverpeas.core.contribution.content.form.RecordSet;
import org.silverpeas.core.contribution.content.form.RecordTemplate;
import org.silverpeas.core.contribution.content.form.TypeManager;
import org.silverpeas.core.contribution.content.form.displayers.WysiwygFCKFieldDisplayer;
import org.silverpeas.core.i18n.I18NHelper;
import org.silverpeas.core.index.indexing.model.FullIndexEntry;
import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.logging.SilverLogger;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


/**
 * The GenericRecordSet manage DataRecord built on a RecordTemplate and saved by the
 * GenericRecordSetManager.
 * @see DataRecord
 */
public class GenericRecordSet implements RecordSet, Serializable {

  private static final long serialVersionUID = 1L;
  private IdentifiedRecordTemplate recordTemplate;

  /**
   * The generic record set is built upon a RecordTemplate.
   */
  public GenericRecordSet(IdentifiedRecordTemplate recordTemplate) {
    this.recordTemplate = recordTemplate;
  }

  /**
   * Returns the RecordTemplate shared by all the DataRecord of this RecordSet.
   * @return the RecordTemplate shared by all the DataRecord of this RecordSet.
   */
  @Override
  public RecordTemplate getRecordTemplate() {
    return recordTemplate;
  }

  /**
   * Returns an empty DataRecord built on the RecordTemplate. This record is not yet managed by this
   * RecordSet. This is only an empty record which must be filled and saved in order to become a
   * DataRecord of this RecordSet.
   * @return an empty DataRecord.
   * @throws FormException
   */
  @Override
  public DataRecord getEmptyRecord() throws FormException {
    return recordTemplate.getEmptyRecord();
  }

  /**
   * Returns the DataRecord with the given id.
   * @return the DataRecord with the given id.
   * @throws FormException when the id is unknown.
   */
  @Override
  public DataRecord getRecord(String objectId) throws FormException {
    return getGenericRecordSetManager().getRecord(recordTemplate, objectId);
  }

  /**
   * Returns the DataRecord with the given id.
   * @return the DataRecord with the given id.
   * @throws FormException when the id is unknown.
   */
  @Override
  public DataRecord getRecord(String objectId, String language) throws FormException {
    return getGenericRecordSetManager().getRecord(recordTemplate, objectId, language);
  }

  public List<DataRecord> getRecords(String fieldName, String fieldValue) throws FormException {
    return getGenericRecordSetManager().getRecords(recordTemplate, fieldName, fieldValue);
  }

  /**
   * Inserts the given DataRecord and set its id.
   * @throws FormException when the record doesn't have the required template or when the record
   * has a not null id or when the insert fail.
   */
  private void insert(DataRecord record) throws FormException {
    recordTemplate.checkDataRecord(record);
    getGenericRecordSetManager().insertRecord(recordTemplate, record);
  }

  /**
   * Updates the given DataRecord.
   * @throws FormException when the record doesn't have the required template or when the record
   * has a null or unknown id or when the update fail.
   */
  private void update(DataRecord record) throws FormException {
    recordTemplate.checkDataRecord(record);
    getGenericRecordSetManager().updateRecord(recordTemplate, record);
  }

  /**
   * Save the given DataRecord. If the record id is null then the record is inserted in this
   * RecordSet. Else the record is updated.
   * @throws FormException when the record doesn't have the required , when the record has an
   * unknown id, when the insert or update fail.
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
    DataRecord data = getRecord(recordId, language);
    if (data != null) {
      String[] fieldNames = data.getFieldNames();
      Field field;
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
                if (fieldTemplate.isRepeatable()) {
                  for (int i=0; i<fieldTemplate.getMaximumNumberOfOccurrences(); i++) {
                    field.setStringValue(data.getField(fieldName, i).getStringValue());
                    fieldDisplayer.index(indexEntry, key, fieldName, field, language,
                        fieldTemplate.isUsedAsFacet());
                  }
                } else {
                  fieldDisplayer.index(indexEntry, key, fieldName, field, language,
                      fieldTemplate.isUsedAsFacet());
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
  }

  @Override
  public void indexRecord(String recordId, String formName, FullIndexEntry indexEntry)
      throws FormException {
    if (!I18NHelper.isI18nContentActivated) {
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
   * Deletes the given DataRecord and its associated data in all languages.
   * @deprecated use delete(String objectId) instead.
   * @param record the record to delete entirely.
   * @throws FormException when the record doesn't have the required template., when the record has
   * an unknown id, when the delete fail.
   */
  @Override
  public void delete(DataRecord record) throws FormException {
    if (record != null) {
      delete(record.getId());
    }
  }

  /**
   * Deletes the given DataRecord and its associated data in the given language.
   */
  private void delete(DataRecord record, String language) throws FormException {
    if (record != null) {
      ResourceReference resourceReference = new ResourceReference(record.getId(), recordTemplate.getInstanceId());

      // remove files managed by WYSIWYG fields
      WysiwygFCKFieldDisplayer.removeContents(resourceReference, getWYSIWYGFieldNames(record), language);

      // remove form documents registered into record but stored into JCR
      List<SimpleDocument> docs = AttachmentServiceProvider.getAttachmentService()
          .listDocumentsByForeignKeyAndType(resourceReference, DocumentType.form, language);
      for (SimpleDocument doc : docs) {
        AttachmentServiceProvider.getAttachmentService().deleteAttachment(doc, false);
      }

      // remove data in database
      // the record is associated to one language
      getGenericRecordSetManager().deleteRecord(record);
    }
  }

  /**
   * Deletes all form data for the given objectId in all languages
   */
  public void delete(String objectId) throws FormException {
    List<String> languages =
        getGenericRecordSetManager().getLanguagesOfRecord(recordTemplate, objectId);
    for (String lang : languages) {
      delete(objectId, lang);
    }
  }

  /**
   * Deletes form data for the given objectId in the given language only
   */
  public void delete(String objectId, String language) throws FormException {
    DataRecord data = getRecord(objectId, language);
    if (data != null && data.getLanguage().equals(language)) {
      // do not fallback on others languages
      delete(data, language);
    }
  }

  private List<String> getWYSIWYGFieldNames(DataRecord data) {
    List<String> wysiwygFieldNames = new ArrayList<String>();
    if (data != null) {
      String[] fieldNames = data.getFieldNames();
      for (String fieldName : fieldNames) {
        try {
          Field field = data.getField(fieldName);
          if (field != null) {
            FieldTemplate fieldTemplate = recordTemplate.getFieldTemplate(fieldName);
            if (fieldTemplate != null) {
              String fieldDisplayerName = fieldTemplate.getDisplayerName();
              if ("wysiwyg".equals(fieldDisplayerName)) {
                wysiwygFieldNames.add(fieldName);
              }
            }
          }
        } catch (FormException fe) {
          SilverTrace
              .error("form", "GenericRecordSet.getWYSIWYGFieldNames", "form.EXP_UNKNOWN_FIELD", fe);
        }
      }
    }
    return wysiwygFieldNames;
  }
  
  @Override
  public void move(ResourceReference fromPK, ResourceReference toPK, RecordTemplate toRecordTemplate)
      throws FormException {

    // move WYSIWYG fields
    WysiwygFCKFieldDisplayer wysiwygDisplayer = new WysiwygFCKFieldDisplayer();
    try {
      wysiwygDisplayer.move(fromPK, toPK);
    } catch (IOException e) {
      SilverTrace.error("form", "GenericRecordSet.move", "form.CANT_MOVE_WYSIWYG_FIELD_CONTENT", null, e);
    }

    // move files, images and video of form
    List<SimpleDocument> documents = AttachmentServiceProvider.getAttachmentService().
        listDocumentsByForeignKeyAndType(fromPK, DocumentType.form, null);
    for (SimpleDocument doc : documents) {
      AttachmentServiceProvider.getAttachmentService().moveDocument(doc, toPK);
    }

    // update data stored in database
    List<String> languages = getGenericRecordSetManager().getLanguagesOfRecord(recordTemplate, fromPK.getId());
    for (String lang : languages) {
      GenericDataRecord record = (GenericDataRecord) getRecord(fromPK.getId(), lang);
      if (record != null) {
        // move record itself in database
        getGenericRecordSetManager().moveRecord(record.getInternalId(),
            (IdentifiedRecordTemplate) toRecordTemplate);
      }
    }
  }

  @Override
  public void copy(ResourceReference fromPK, ResourceReference toPK, RecordTemplate toRecordTemplate,
      Map<String, String> oldAndNewFileIds) throws FormException {

    // clone WYSIWYG fields content
    WysiwygFCKFieldDisplayer wysiwygDisplayer = new WysiwygFCKFieldDisplayer();
    try {
      wysiwygDisplayer.cloneContents(fromPK, toPK, oldAndNewFileIds);
    } catch (Exception e) {
      SilverTrace.error("form", "GenericRecordSet.copy", "form.EX_CLONE_FAILURE", null, e);
    }

    // copy files, images and videos
    Map<String, String> ids = new HashMap<>();
    try {
      List<SimpleDocument> originals = AttachmentServiceProvider.getAttachmentService()
          .listDocumentsByForeignKeyAndType(fromPK, DocumentType.form, null);
      for (SimpleDocument original : originals) {
        SimpleDocumentPK clonePk =
            AttachmentServiceProvider.getAttachmentService().copyDocument(original, toPK);
        ids.put(original.getId(), clonePk.getId());
      }
    } catch (AttachmentException e) {
      throw new FormException("form", "", e);
    }

    // copy data stored in database
    List<String> languages = getGenericRecordSetManager().getLanguagesOfRecord(recordTemplate, fromPK.getId());
    for (String lang : languages) {
      GenericDataRecord record = (GenericDataRecord) getRecord(fromPK.getId(), lang);
      if (record != null) {
        record.setInternalId(-1);
        record.setId(toPK.getId());

        // replace files reference
        replaceIds(ids, record);

        // insert record itself in database
        getGenericRecordSetManager()
            .insertRecord((IdentifiedRecordTemplate) toRecordTemplate, record);
      }
    }
  }

  @Override
  public void clone(String originalExternalId, String originalComponentId, String cloneExternalId,
      String cloneComponentId, Map<String, String> attachmentIds) throws FormException {
    GenericDataRecord record = (GenericDataRecord) getRecord(originalExternalId);
    record.setInternalId(-1);
    record.setId(cloneExternalId);

    ResourceReference fromPK = new ResourceReference(originalExternalId, originalComponentId);
    ResourceReference toPK = new ResourceReference(cloneExternalId, cloneComponentId);

    // clone wysiwyg fields content
    WysiwygFCKFieldDisplayer wysiwygDisplayer = new WysiwygFCKFieldDisplayer();
    try {
      wysiwygDisplayer.cloneContents(fromPK, toPK, attachmentIds);
    } catch (Exception e) {
      SilverTrace.error("form", "AbstractForm.clone", "form.EX_CLONE_FAILURE", null, e);
    }

    // clone images and videos
    try {
      List<SimpleDocument> originals = AttachmentServiceProvider.getAttachmentService()
          .listDocumentsByForeignKeyAndType(fromPK, DocumentType.form, null);
      originals.addAll(AttachmentServiceProvider.getAttachmentService()
          .listDocumentsByForeignKeyAndType(fromPK, DocumentType.video, null));
      Map<String, String> ids = new HashMap<>(originals.size());
      for (SimpleDocument original : originals) {
        SimpleDocumentPK clonePk = AttachmentServiceProvider.getAttachmentService().cloneDocument(
            original, cloneExternalId);
        ids.put(original.getId(), clonePk.getId());
      }
      replaceIds(ids, record);
    } catch (AttachmentException e) {
      throw new FormException("form", "", e);
    }

    insert(record);
  }

  @Override
  public void merge(String fromExternalId, String fromComponentId, String toExternalId,
      String toComponentId, Map<String, String> attachmentIds) throws FormException {
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
    ResourceReference fromPK = new ResourceReference(fromExternalId, fromComponentId);
    ResourceReference toPK = new ResourceReference(toExternalId, toComponentId);
    try {
      Map<String, String> ids = AttachmentServiceProvider.getAttachmentService().mergeDocuments(toPK,
          fromPK, DocumentType.form);
      Map<String, String> videoIds = AttachmentServiceProvider.getAttachmentService().mergeDocuments(
          toPK, fromPK, DocumentType.video);
      ids.putAll(videoIds);
      replaceIds(ids, fromRecord);
    } catch (AttachmentException e) {
      throw new FormException("form", "", e);
    }

    update(fromRecord);
  }

  private void replaceIds(Map<String, String> ids, GenericDataRecord record)
      throws FormException {
    final String[] fieldNames = record.getFieldNames();
    for (String fieldName : fieldNames) {
      getRecordFieldAndTemplateByFieldName(record, fieldName).ifPresent(p -> {
        final Field field = p.getKey();
        final FieldTemplate fieldTemplate = p.getValue();
        final String fieldType = fieldTemplate.getTypeName();
        final String currentValue = field.getStringValue();
        if (Field.TYPE_FILE.equals(fieldType)) {
          if (ids.containsKey(currentValue)) {
            setStringValueQuietly(field, ids.get(currentValue));
          }
        } else {
          Optional.ofNullable(currentValue)
              .filter(o -> o.startsWith(WysiwygFCKFieldDisplayer.DB_KEY))
              .ifPresent(o -> {
                // Wysiwyg case
                final String newValue = o.replaceFirst("[0-9]+", record.getId());
                setStringValueQuietly(field, newValue);
              });
        }
      });
    }
  }

  private void setStringValueQuietly(final Field field, final String value) {
    try {
      field.setStringValue(value);
    } catch (FormException e) {
      SilverLogger.getLogger(this).error(e);
    }
  }

  private Optional<Pair<Field, FieldTemplate>> getRecordFieldAndTemplateByFieldName(
      final GenericDataRecord record, final String fieldName) throws FormException {
    final Field field = record.getField(fieldName);
    if (field != null) {
      final FieldTemplate fieldTemplate = recordTemplate.getFieldTemplate(fieldName);
      if (fieldTemplate != null) {
        return Optional.of(Pair.of(field, fieldTemplate));
      }
    }
    return Optional.empty();
  }

  /**
   * Gets an instance of a GenericRecordSet objects manager.
   * @return a GenericRecordSetManager instance.
   */
  private GenericRecordSetManager getGenericRecordSetManager() {
    return GenericRecordSetManager.getInstance();
  }

}