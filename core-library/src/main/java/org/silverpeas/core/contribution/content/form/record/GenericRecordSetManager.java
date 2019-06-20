/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.contribution.content.form.record;

import org.silverpeas.core.contribution.content.form.DataRecord;
import org.silverpeas.core.contribution.content.form.Field;
import org.silverpeas.core.contribution.content.form.FieldTemplate;
import org.silverpeas.core.contribution.content.form.FormException;
import org.silverpeas.core.contribution.content.form.FormRuntimeException;
import org.silverpeas.core.contribution.content.form.RecordSet;
import org.silverpeas.core.contribution.content.form.RecordTemplate;
import org.silverpeas.core.contribution.content.form.dummy.DummyRecordSet;
import org.silverpeas.core.contribution.template.publication.PublicationTemplate;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateException;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateManager;
import org.silverpeas.core.i18n.I18NHelper;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.persistence.jdbc.sql.JdbcSqlQuery;
import org.silverpeas.core.security.encryption.ContentEncryptionService;
import org.silverpeas.core.security.encryption.ContentEncryptionServiceProvider;
import org.silverpeas.core.security.encryption.EncryptionContentIterator;
import org.silverpeas.core.security.encryption.cipher.CryptoException;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.logging.SilverLogger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * The GenericRecordSetManage all the GenericRecordSet. It is a singleton.
 */
public class GenericRecordSetManager {

  private static final GenericRecordSetManager instance = new GenericRecordSetManager();
  private static final String SEPARATOR = "|";
  private static final String INSERT_INTO = "insert into ";
  private static final String DELETE_FROM = "delete from ";
  private static final String GENERIC_RECORD_SET_MANAGER = "GenericRecordSetManager";
  private static final String FORM_EXP_SELECT_FAILED = "form.EXP_SELECT_FAILED";
  private static final String FORM_EXP_INSERT_FAILED = "form.EXP_INSERT_FAILED";
  private static final String FORM_EXP_UNKNOWN_TEMPLATE = "form.EXP_UNKNOWN_TEMPLATE";
  private static final String FIELD_VALUE = "fieldValue";

  private final Map<String, GenericRecordSet> cache = new HashMap<>();

  private GenericRecordSetManager() {

  }

  /**
   * Gets the single instance of this manager.
   * @return the single instance of GenericRecordSetManager.
   */
  public static GenericRecordSetManager getInstance() {
    return instance;
  }

  public List<String> getExternalIdOfComponentInstanceId(String componentInstanceId)
      throws FormException {
    try {
      return JdbcSqlQuery.createSelect("externalId from " + TEMPLATE_TABLE)
          .where("externalId like ?", componentInstanceId + ":%").execute(row -> row.getString(1));
    } catch (SQLException e) {
      throw new FormException(GENERIC_RECORD_SET_MANAGER, FORM_EXP_SELECT_FAILED, e);
    }
  }

  /**
   * Build and return a new record set.
   * @throws FormException
   */
  public GenericRecordSet createRecordSet(String externalId,
      RecordTemplate template) throws FormException {
    return createRecordSet(externalId, template, null, false);
  }

  public GenericRecordSet createRecordSet(String externalId,
      RecordTemplate template, String templateName, boolean encrypted) throws FormException {
    IdentifiedRecordTemplate identifiedTemplate = new IdentifiedRecordTemplate(
        template);
    identifiedTemplate.setExternalId(externalId);
    identifiedTemplate.setTemplateName(templateName);
    identifiedTemplate.setEncrypted(encrypted);
    try (final Connection con = getConnection()) {
      IdentifiedRecordTemplate existingOne = selectTemplateRow(con, externalId);
      if (existingOne == null) {
        insertTemplateRow(con, identifiedTemplate);
        if (templateName == null) {
          insertTemplateFieldRows(con, identifiedTemplate);
        }

        GenericRecordSet newSet = new GenericRecordSet(identifiedTemplate);
        cacheRecordSet(externalId, newSet);
        return newSet;
      } else {
        return (GenericRecordSet) getRecordSet(externalId);
      }
    } catch (SQLException e) {
      throw new FormException(GENERIC_RECORD_SET_MANAGER, FORM_EXP_INSERT_FAILED, e);
    }
  }

  /**
   * Get value of a field record directly from database.
   * @param templateExternalId template external id
   * @param recordExternalId record external id
   * @param fieldName field name
   * @return the field record value
   * @throws FormException
   */
  public String getRawValue(String templateExternalId,
      String recordExternalId, String fieldName) throws FormException {
    try (final Connection con = getConnection()) {
      return selectRecordFieldsRow(con, templateExternalId, recordExternalId, fieldName);
    } catch (SQLException e) {
      throw new FormException("GenericRecordSetManager.getRawValues", FORM_EXP_INSERT_FAILED,
          "templateExternalId : " + templateExternalId +
          ", recordExternalId : " + recordExternalId, e);
    }
  }

  /**
   * Return the record set known be its external id.
   * @throws FormException when the id is unknown.
   */
  public RecordSet getRecordSet(String externalId) throws FormException {
    GenericRecordSet cachedSet = getCachedRecordSet(externalId);
    if (cachedSet != null) {
      return cachedSet;
    }
    try (final Connection con = getConnection()) {
      final IdentifiedRecordTemplate template = selectTemplateRow(con, externalId);
      if (template == null) {
        return new DummyRecordSet();
      }
      String templateName = template.getTemplateName();
      if (templateName != null && templateName.length() > 0) {
        // get fields directly from xml
        selectTemplateFieldsFromXML(template);
      } else {
        // get fields directly from database
        selectTemplateFieldRows(con, template);
      }
      cachedSet = new GenericRecordSet(template);
      cacheRecordSet(externalId, cachedSet);
      return cachedSet;
    } catch (SQLException e) {
      throw new FormException(GENERIC_RECORD_SET_MANAGER, FORM_EXP_SELECT_FAILED, e);
    }
  }

  /**
   * Remove the record set known by its external id.
   * @throws FormException when the id is unknown.
   */
  public void removeRecordSet(String externalId) throws FormException {
    removeCachedRecordSet(externalId);
    try (final Connection con = getConnection()) {
      final IdentifiedRecordTemplate template = selectTemplateRow(con, externalId);
      if (template == null) {
        SilverLogger.getLogger(this).error("Unknown template: externalId = " + externalId);
      } else {
        deleteFieldRows(con, template);
        deleteRecordRows(con, template);
        deleteTemplateFieldRows(con, template);
        deleteTemplateRow(con, template);
      }
    } catch (SQLException e) {
      throw new FormException(GENERIC_RECORD_SET_MANAGER,
          "form.EXP_DELETE_FAILED", e);
    }
  }

  /*
   * Cache manipulation
   */
  private GenericRecordSet getCachedRecordSet(String externalId) {
    return cache.get(externalId);
  }

  private void cacheRecordSet(String externalId, GenericRecordSet set) {
    cache.put(externalId, set);
  }

  private void removeCachedRecordSet(String externalId) {
    cache.remove(externalId);
  }

  public void removeTemplateFromCache(String templateName) {
    // getting cached recordsets managed by given template
    List<String> ids = new ArrayList<>();
    for (Map.Entry<String, GenericRecordSet> recordSets : cache.entrySet()) {
      GenericRecordSet rs = recordSets.getValue();
      IdentifiedRecordTemplate template = (IdentifiedRecordTemplate) rs.getRecordTemplate();
      if (template != null && templateName.equalsIgnoreCase(template.getTemplateName())) {
        ids.add(recordSets.getKey());
      }
    }
    // removing recordsets from cache
    ids.forEach(this::removeCachedRecordSet);
  }

  /**
   * Return the DataRecord registered by the pair (templateId, recordId).
   * @param template the definition of the form template the record belongs to.
   * @param objectId the ID of the resource attached to form record.
   * @return the form record or <code>null</code> if not found.
   * @throws FormException if the (templateId, recordId) pair is unknown.
   */
  public DataRecord getRecord(IdentifiedRecordTemplate template,
      String objectId) throws FormException {
    return getRecord(template, objectId, null);
  }

  /**
   * Return the DataRecord registered by the tuple (templateId, objectId, language). If language
   * does not match, fallback to other languages is done.
   * @param template the definition of the form template the record belongs to.
   * @param objectId the ID of the resource attached to form record.
   * @return the form record in given language or in next language or <code>null</code> if not
   * found.
   * @throws FormException if the (templateId, recordId) pair is unknown.
   */
  public DataRecord getRecord(IdentifiedRecordTemplate template, String objectId, String language)
      throws FormException {
    try (final Connection con = getConnection()) {
      GenericDataRecord record = selectRecordRow(con, template, objectId, language);
      if (record != null) {
        record.setLanguage(language);
      } else if (I18NHelper.isI18nContentEnabled()) {
        List<String> languages = new ArrayList<>(I18NHelper.getAllSupportedLanguages());
        languages.remove(language);
        for (String lang : languages) {
          record = selectRecordRow(con, template, objectId, lang);
          if (record != null) {
            record.setLanguage(lang);
            break;
          }
        }
      }
      if (record != null) {
        selectFieldRows(con, template, record);
      }

      return record;

    } catch (SQLException e) {
      throw new FormException(GENERIC_RECORD_SET_MANAGER, FORM_EXP_SELECT_FAILED, e);
    }
  }

  public List<String> getLanguagesOfRecord(IdentifiedRecordTemplate template,
      String externalId) throws FormException {
    try (final Connection con = getConnection()) {
      return selectLanguagesOfRecord(con, template, externalId);
    } catch (SQLException e) {
      throw new FormException(GENERIC_RECORD_SET_MANAGER, FORM_EXP_SELECT_FAILED, e);
    }
  }

  /**
   * Register the DataRecord with the pair (templateId, recordId).
   * @param template
   * @param insertedRecord
   * @throws FormException if the (templateId, recordId) pair is already known or if the given
   * template is unknown.
   */
  public void insertRecord(IdentifiedRecordTemplate template,
      DataRecord insertedRecord) throws FormException {
    try (final Connection con = getConnection()) {
      GenericDataRecord record = (GenericDataRecord) insertedRecord;
      insertRecordRow(con, template, record);
      insertFieldRows(con, template, record);
    } catch (ClassCastException e) {
      throw new FormException(GENERIC_RECORD_SET_MANAGER, FORM_EXP_UNKNOWN_TEMPLATE, e);
    } catch (SQLException e) {
      throw new FormException(GENERIC_RECORD_SET_MANAGER, FORM_EXP_INSERT_FAILED, e);
    }
  }

  public void moveRecord(int recordId, IdentifiedRecordTemplate templateTo)
      throws FormException {
    try (final Connection con = getConnection()) {
      updateTemplateId(con, templateTo.getInternalId(), recordId);
    } catch (SQLException e) {
      throw new FormException("GenericRecordSetManager.moveRecord",
          "form.CANT_MOVE_RECORD_FROM_TEMPLATE_TO_ANOTHER", e);
    }
  }

  /**
   * Save the DataRecord registered by the pair (templateId, recordId).
   * @param template
   * @param updatedRecord
   * @throws FormException when the (templateId, recordId) pair is unknown.
   */
  public void updateRecord(IdentifiedRecordTemplate template,
      DataRecord updatedRecord) throws FormException {
    try (final Connection con = getConnection()) {
      GenericDataRecord record = (GenericDataRecord) updatedRecord;
      updateFieldRows(con, template, record);
    } catch (ClassCastException e) {
      throw new FormException(GENERIC_RECORD_SET_MANAGER, FORM_EXP_UNKNOWN_TEMPLATE, e);
    } catch (SQLException e) {
      throw new FormException(GENERIC_RECORD_SET_MANAGER, "form.EXP_UPDATED_FAILED", e);
    }
  }

  /**
   * Delete the DataRecord registered by the pair (templateId, recordId).
   * @param deletedRecord
   * @throws FormException when the (templateId, recordId) pair is unknown.
   */
  public void deleteRecord(DataRecord deletedRecord) throws FormException {
    try (final Connection con = getConnection()) {
      GenericDataRecord record = (GenericDataRecord) deletedRecord;
      deleteFieldRows(con, record);
      deleteRecordRows(con, record);
    } catch (ClassCastException e) {
      throw new FormException(GENERIC_RECORD_SET_MANAGER, FORM_EXP_UNKNOWN_TEMPLATE, e);
    } catch (SQLException e) {
      throw new FormException(GENERIC_RECORD_SET_MANAGER,
          "form.EXP_DELETE_FAILED", e);
    }
  }

  public void encryptData(String templateName) throws CryptoException {
    encryptOrDecryptData(templateName, true);
  }

  public void decryptData(String templateName) throws CryptoException {
    encryptOrDecryptData(templateName, false);
  }

  public List<DataRecord> getRecords(IdentifiedRecordTemplate template, String fieldName,
      String fieldValue) throws FormException {
    try (final Connection con = getConnection()) {
      return selectRecords(con, template, fieldName, fieldValue);
    } catch (SQLException e) {
      throw new FormException("GenericRecordSetManager.getRecords", FORM_EXP_SELECT_FAILED, e);
    }
  }

  public Map<String, Integer> getNumberOfRecordsByTemplateAndComponents(String templateName)
      throws FormException {
    final Map<String, Integer> result = new HashMap<>();
    try (final Connection con = getConnection();
         final PreparedStatement select = con.prepareStatement(SELECT_NUMBER_OF_RECORDS_BY_TEMPLATE_AND_COMPONENTS)) {
      select.setString(1, templateName);
      try (final ResultSet rs = select.executeQuery()){
        while (rs.next()) {
          final int count = rs.getInt(2);
          result.put(extractComponentId(rs), count);
        }
      }
    } catch (SQLException e) {
      throw new FormException("GenericRecordSetManager.getNumberOfRecordsByTemplateAndComponents",
          FORM_EXP_SELECT_FAILED, e);
    }
    return result;
  }

  public Set<String> getAllComponentIdsOfRecords() throws FormException {
    final Set<String> result = new LinkedHashSet<>();
    try (final Connection con = getConnection();
         final PreparedStatement select = con.prepareStatement(SELECT_ALL_EXTERNAL_IDS_OF_RECORDS)) {
      try (final ResultSet rs = select.executeQuery()) {
        while (rs.next()) {
          result.add(extractComponentId(rs));
        }
      }
    } catch (SQLException e) {
      throw new FormException("GenericRecordSetManager.getAllComponentIdsOfRecords",
          FORM_EXP_SELECT_FAILED, e);
    }
    return result;
  }

  private String extractComponentId(final ResultSet rs) throws SQLException {
    final String externalId = rs.getString(1);
    return externalId.split(":")[0];
  }

  private List<DataRecord> selectRecords(Connection con, IdentifiedRecordTemplate template,
      String fieldName, String fieldValue) throws SQLException, FormException {
    final List<DataRecord> records = new ArrayList<>();
    try (final PreparedStatement select = con.prepareStatement(
        SELECT_TEMPLATE_RECORDS_BY_FIELDVALUE)) {
      select.setInt(1, template.getInternalId());
      select.setString(2, fieldName);
      select.setString(3, fieldValue);
      select.setString(4, "%##"+fieldValue);
      select.setString(5, "%##"+fieldValue+"##%");
      select.setString(6, fieldValue+"##%");
      try (final ResultSet rs = select.executeQuery()) {
        while (rs.next()) {
          int internalId = rs.getInt(1);
          String objectId = rs.getString(3);
          GenericDataRecord record = new GenericDataRecord(template);
          record.setInternalId(internalId);
          record.setId(objectId);
          records.add(record);
        }

      }
    }
    return records;
  }

  private void encryptOrDecryptData(String templateName, boolean encrypt) throws CryptoException {
    ContentEncryptionService encryptionService = getEncryptionService();
    EncryptionContentIterator contentIterator = new FormEncryptionContentIterator(templateName);
    // encrypt or decrypt values
    if (encrypt) {
      // encrypt values
      try {
        encryptionService.encryptContents(contentIterator);
      } catch (FormRuntimeException e) {
        throw new CryptoException(CryptoException.ENCRYPTION_FAILURE, e);
      }
    } else {
      // decrypt values
      try {
        encryptionService.decryptContents(contentIterator);
      } catch (Exception e) {
        throw new CryptoException(CryptoException.DECRYPTION_FAILURE, e);
      }
    }
  }

  private ContentEncryptionService getEncryptionService() {
    return ContentEncryptionServiceProvider.getContentEncryptionService();
  }

  protected void updateFieldRows(Connection con, List<RecordRow> rows) throws SQLException {
    try (final PreparedStatement update = con.prepareStatement(UPDATE_FIELD)) {
      for (RecordRow row : rows) {
        update.setString(1, row.getFieldValue());
        update.setInt(2, row.getRecordId());
        update.setString(3, row.getFieldName());

        int nbRowsUpdated = update.executeUpdate();
        if (nbRowsUpdated != 1) {
          SilverLogger.getLogger(this).error("Update failed for record " + row.getRecordId() +
                  " and field '" + row.getFieldName() + "' with value " + row.getFieldValue());
        }
      }
    }
  }

  protected List<RecordRow> getAllRecordsOfTemplate(String templateName) throws FormException {
    try (final Connection con = getConnection();
         final PreparedStatement select = con.prepareStatement(SELECT_TEMPLATE_RECORD_ENTRIES)) {
      final List<RecordRow> rows = new ArrayList<>();
      select.setString(1, templateName);
      try (final ResultSet rs = select.executeQuery()) {
        while (rs.next()) {
          RecordRow row = new RecordRow(rs.getInt("recordId"), rs.getString("fieldName"),
              rs.getString(FIELD_VALUE));
          rows.add(row);
        }
        return rows;
      }
    } catch (SQLException e) {
      throw new FormException(GENERIC_RECORD_SET_MANAGER, FORM_EXP_SELECT_FAILED, e);
    }
  }

  private String selectRecordFieldsRow(Connection con,
      String templateExternalId, String recordExternalId, String fieldName) throws SQLException {
    try (final PreparedStatement select = con.prepareStatement(SELECT_TEMPLATE_RECORD_VALUES)) {
      select.setString(1, fieldName);
      select.setString(2, recordExternalId);
      select.setString(3, templateExternalId);
      try (final ResultSet rs = select.executeQuery()) {
        if (!rs.next()) {
          return null;
        }
        return rs.getString(FIELD_VALUE);
      }
    }
  }

  /**
   * Get the template field declarations directly from the XML file.
   */
  private void selectTemplateFieldsFromXML(IdentifiedRecordTemplate template) throws FormException {
    GenericRecordTemplate genericRecordTemplate = null;
    try {
      PublicationTemplate publicationTemplateImpl = PublicationTemplateManager.getInstance()
          .loadPublicationTemplate(template.getTemplateName());
      genericRecordTemplate = (GenericRecordTemplate) publicationTemplateImpl
          .getRecordTemplate();
      template.setEncrypted(publicationTemplateImpl.isDataEncrypted());
    } catch (PublicationTemplateException e) {
      throw new FormException(GENERIC_RECORD_SET_MANAGER, FORM_EXP_UNKNOWN_TEMPLATE, e);
    }

    GenericRecordTemplate wrapped = (GenericRecordTemplate) template.
        getWrappedTemplate();
    FieldTemplate[] fields = genericRecordTemplate.getFieldTemplates();
    for (FieldTemplate field : fields) {
      String displayName = field.getDisplayerName();
      GenericFieldTemplate fieldTemplate = new GenericFieldTemplate(field.
          getFieldName(), field.getTypeName());
      fieldTemplate.setMandatory(field.isMandatory());
      fieldTemplate.setReadOnly(field.isReadOnly());
      fieldTemplate.setHidden(field.isHidden());
      fieldTemplate.setDisabled(field.isDisabled());
      fieldTemplate.setSearchable(field.isSearchable());
      fieldTemplate.setDisplayerName((displayName != null) ? displayName : "");
      fieldTemplate.setLabel(field.getLabel());
      fieldTemplate.setUsedAsFacet(field.isUsedAsFacet());
      fieldTemplate.setParametersObj(field.getParametersObj());
      fieldTemplate.setMaximumNumberOfOccurrences(field.getMaximumNumberOfOccurrences());

      wrapped.addFieldTemplate(fieldTemplate);
    }
  }

  /**
   * Returns a connection.
   */
  private Connection getConnection() throws FormException {
    try {
      return DBUtil.openConnection();
    } catch (Exception e) {
      throw new FormException("GenericRecordSetManager.getConnection()",
          "root.EX_CONNECTION_OPEN_FAILED", e);
    }
  }

  /**
   * Returns the next id in the named table.
   */
  private int getNextId(String tableName, String idColumn) {
    int nextId = DBUtil.getNextId(tableName, idColumn);
    if (nextId == 0) {
      return 1;
    } else {
      return nextId;
    }
  }

  /**
   * Creates the template declaration row in SB_FormTemplate_Template.
   */
  private void insertTemplateRow(Connection con,
      IdentifiedRecordTemplate template) throws SQLException {
    int internalId = getNextId(TEMPLATE_TABLE, "templateId");
    template.setInternalId(internalId);
    String externalId = template.getExternalId();
    String templateName = template.getTemplateName();
    try (final PreparedStatement insert = con.prepareStatement(INSERT_TEMPLATE)) {
      insert.setInt(1, internalId);
      insert.setString(2, externalId);
      insert.setString(3, templateName);
      insert.execute();
    }
  }

  /**
   * Creates a row for each template_fields.
   */
  private void insertTemplateFieldRows(Connection con,
      IdentifiedRecordTemplate template) throws SQLException, FormException {
    try (final PreparedStatement insert = con.prepareStatement(INSERT_TEMPLATE_FIELD)) {
      int internalId = template.getInternalId();
      FieldTemplate[] fields = template.getFieldTemplates();
      for (int i = 0; i < fields.length; i++) {
        insert.setInt(1, internalId);
        insert.setString(2, fields[i].getFieldName());
        insert.setInt(3, i);
        insert.setString(4, fields[i].getTypeName());
        if (fields[i].isMandatory()) {
          insert.setInt(5, 1);
        } else {
          insert.setInt(5, 0);
        }
        if (fields[i].isReadOnly()) {
          insert.setInt(6, 1);
        } else {
          insert.setInt(6, 0);
        }
        insert.setInt(7, 1);
        insert.execute();
      }
    }
  }

  /**
   * Select the template header.
   */
  private IdentifiedRecordTemplate selectTemplateRow(Connection con,
      String externalId) throws SQLException {
    try (final PreparedStatement select = con.prepareStatement(SELECT_TEMPLATE)) {
      select.setString(1, externalId);
      try (final ResultSet rs = select.executeQuery()) {
        if (!rs.next()) {
          return null;
        }
        int internalId = rs.getInt(1);
        String templateName = rs.getString(3);
        IdentifiedRecordTemplate template =
            new IdentifiedRecordTemplate(new GenericRecordTemplate());
        template.setInternalId(internalId);
        template.setExternalId(externalId);
        template.setTemplateName(templateName);
        return template;
      }
    }
  }

  /**
   * Select the template field declarations.
   */
  private void selectTemplateFieldRows(Connection con,
      IdentifiedRecordTemplate template) throws SQLException, FormException {
    GenericRecordTemplate wrapped = (GenericRecordTemplate) template
        .getWrappedTemplate();

    try (final PreparedStatement select = con.prepareStatement(SELECT_TEMPLATE_FIELDS)) {
      select.setInt(1, template.getInternalId());
      try (final ResultSet rs = select.executeQuery()) {

        GenericFieldTemplate fieldTemplate = null;
        String fieldName;
        String fieldType;
        boolean isMandatory;
        boolean isReadOnly;
        boolean isHidden;
        while (rs.next()) {
          fieldName = rs.getString(2);
          fieldType = rs.getString(4);
          isMandatory = rs.getBoolean(5);
          isReadOnly = rs.getBoolean(6);
          isHidden = rs.getBoolean(7);

          fieldTemplate = new GenericFieldTemplate(fieldName, fieldType);
          fieldTemplate.setMandatory(isMandatory);
          fieldTemplate.setReadOnly(isReadOnly);
          fieldTemplate.setHidden(isHidden);

          wrapped.addFieldTemplate(fieldTemplate);
        }
      }
    }
  }

  /**
   * Deletes all the fields of the records built on this template.
   */
  private void deleteFieldRows(Connection con,
      IdentifiedRecordTemplate template) throws SQLException {
    try (final PreparedStatement delete = con.prepareStatement(DELETE_TEMPLATE_RECORDS_FIELDS)) {
      int internalId = template.getInternalId();
      delete.setInt(1, internalId);
      delete.execute();
    }
  }

  /**
   * Deletes all the records built on this template.
   */
  private void deleteRecordRows(Connection con,
      IdentifiedRecordTemplate template) throws SQLException {
    try (final PreparedStatement delete = con.prepareStatement(DELETE_TEMPLATE_RECORDS)) {
      int internalId = template.getInternalId();
      delete.setInt(1, internalId);
      delete.execute();
    }
  }

  /**
   * Deletes the templatefields.
   */
  private void deleteTemplateFieldRows(Connection con,
      IdentifiedRecordTemplate template) throws SQLException {
    try (final PreparedStatement delete = con.prepareStatement(DELETE_TEMPLATE_FIELDS)) {
      int internalId = template.getInternalId();
      delete.setInt(1, internalId);
      delete.execute();
    }
  }

  /**
   * Deletes the template.
   */
  private void deleteTemplateRow(Connection con,
      IdentifiedRecordTemplate template) throws SQLException {
    try (final PreparedStatement delete = con.prepareStatement(DELETE_TEMPLATE)) {
      int internalId = template.getInternalId();
      delete.setInt(1, internalId);
      delete.execute();
    }
  }

  /**
   * Creates the record declaration row in SB_FormTemplate_Record.
   */
  private void insertRecordRow(Connection con,
      IdentifiedRecordTemplate template, GenericDataRecord record)
      throws SQLException {
    try (final PreparedStatement insert = con.prepareStatement(INSERT_RECORD)) {
      int internalId = getNextId(RECORD_TABLE, "recordId");
      record.setInternalId(internalId);
      int templateId = template.getInternalId();
      String externalId = record.getId();
      insert.setInt(1, internalId);
      insert.setInt(2, templateId);
      insert.setString(3, externalId);
      if (!I18NHelper.isI18nContentActivated
          || I18NHelper.isDefaultLanguage(record.getLanguage())) {
        insert.setNull(4, Types.VARCHAR);
      } else {
        insert.setString(4, record.getLanguage());
      }
      insert.execute();
    }
  }

  /**
   * Creates a row for each template_fields.
   * @throws CryptoException
   */
  private void insertFieldRows(Connection con, IdentifiedRecordTemplate template,
      GenericDataRecord record) throws SQLException, FormException {
    int recordId = record.getInternalId();
    try {
      Map<String, String> rows = getRowsToStore(record, template.isEncrypted());
      for (Map.Entry<String, String> fieldNameIndexed : rows.entrySet()) {
        String[] fieldNameAndIndex = StringUtil.split(fieldNameIndexed.getKey(), SEPARATOR);
        String fieldName = fieldNameAndIndex[0];
        int fieldValueIndex = Integer.parseInt(fieldNameAndIndex[1]);
        String fieldValue = fieldNameIndexed.getValue();
        executeInsertQuery(con, recordId, fieldValueIndex, fieldName, fieldValue);
      }
    } catch (CryptoException e) {
      throw new FormException(GENERIC_RECORD_SET_MANAGER, "form.ENCRYPTING_DATA_FAILED", e);
    }
  }

  private void executeInsertQuery(final Connection con, final int recordId,
      final int fieldValueIndex, final String fieldName, final String fieldValue)
      throws SQLException {
    try (final PreparedStatement insert = con.prepareStatement(INSERT_FIELD)) {
      insert.setInt(1, recordId);
      insert.setString(2, fieldName);
      insert.setString(3, fieldValue);
      insert.setInt(4, fieldValueIndex);
      insert.execute();
    }
  }

  /**
   * Select the template header.
   */
  private GenericDataRecord selectRecordRow(Connection con, IdentifiedRecordTemplate template,
      String externalId, String language) throws SQLException, FormException {
    if (!I18NHelper.isI18nContentActivated || I18NHelper.isDefaultLanguage(language)) {
      language = null;
    }

    if (language != null) {
      final String query = SELECT_RECORD + " AND lang = ? ";
      return executeSelectQuery(con, query, template, externalId, language);
    } else {
      final String query = SELECT_RECORD + " AND lang is null";
      return executeSelectQuery(con, query, template, externalId);
    }
  }

  private GenericDataRecord executeSelectQuery(final Connection con, final String query,
      final IdentifiedRecordTemplate template, final String externalId)
      throws SQLException, FormException {
    try (final PreparedStatement select = con.prepareStatement(query)) {
      select.setInt(1, template.getInternalId());
      select.setString(2, externalId);
      try (final ResultSet rs = select.executeQuery()) {
        return fetchGenericDataRecord(rs, template, externalId);
      }
    }
  }

  private GenericDataRecord executeSelectQuery(final Connection con, final String query,
      final IdentifiedRecordTemplate template, final String externalId, final String language)
      throws SQLException, FormException {
    try (final PreparedStatement select = con.prepareStatement(query)) {
      select.setInt(1, template.getInternalId());
      select.setString(2, externalId);
      select.setString(3, language);
      try (final ResultSet rs = select.executeQuery()) {
        GenericDataRecord record = fetchGenericDataRecord(rs, template, externalId);
        if (record != null) {
          record.setLanguage(language);
        }
        return record;
      }
    }
  }

  private GenericDataRecord fetchGenericDataRecord(final ResultSet rs,
      final IdentifiedRecordTemplate template, final String externalId)
      throws SQLException, FormException {
    if (!rs.next()) {
      return null;
    }
    int internalId = rs.getInt(1);

    GenericDataRecord record = new GenericDataRecord(template);
    record.setInternalId(internalId);
    record.setId(externalId);
    return record;
  }

  /**
   * Select the template field declarations.
   * @throws CryptoException
   */
  private void selectFieldRows(Connection con,
      IdentifiedRecordTemplate template, GenericDataRecord record)
      throws SQLException, FormException {
    try (final PreparedStatement select = con.prepareStatement(SELECT_FIELDS)) {
      select.setInt(1, record.getInternalId());
      try (final ResultSet rs = select.executeQuery()) {
        Map<String, String> rows = new TreeMap<>();
        while (rs.next()) {
          String fieldName = rs.getString("fieldName");
          String fieldValue = rs.getString(FIELD_VALUE);
          int fieldValueIndex = rs.getInt("fieldvalueindex");

          rows.put(fieldName + SEPARATOR + fieldValueIndex, fieldValue);
        }

        if (template.isEncrypted()) {
          rows = getEncryptionService().decryptContent(rows);
        }

        for (Map.Entry<String, String> fieldNameIndexed : rows.entrySet()) {
          String[] fieldNameAndIndex = StringUtil.split(fieldNameIndexed.getKey(), SEPARATOR);
          String fieldName = fieldNameAndIndex[0];
          int fieldValueIndex = Integer.parseInt(fieldNameAndIndex[1]);
          Field field = record.getField(fieldName, fieldValueIndex);
          String fieldValue = fieldNameIndexed.getValue();
          if (field != null) {// We found a field corresponding to the fieldName
            field.setStringValue(fieldValue);
          }
        }
      }
    } catch (CryptoException e) {
      throw new FormException(GENERIC_RECORD_SET_MANAGER, "form.DECRYPTING_DATA_FAILED", e);
    }
  }

  private List<String> selectLanguagesOfRecord(Connection con,
      IdentifiedRecordTemplate template, String externalId)
      throws SQLException {
    List<String> languages = new ArrayList<>();
    try (final PreparedStatement select = con.prepareStatement(SELECT_RECORD)) {
      select.setInt(1, template.getInternalId());
      select.setString(2, externalId);
      try (final ResultSet rs = select.executeQuery()) {
        while (rs.next()) {
          String language = rs.getString("lang");
          if (!StringUtil.isDefined(language)) {
            language = I18NHelper.defaultLanguage;
          }
          languages.add(language);
        }
      }
      return languages;
    }
  }

  /**
   * Updates the records fields.
   * @throws CryptoException
   */
  private void updateFieldRows(Connection con,
      IdentifiedRecordTemplate template, GenericDataRecord record)
      throws SQLException, FormException {
    try (final PreparedStatement update = con.prepareStatement(UPDATE_FIELD)) {
      int recordId = record.getInternalId();
      Map<String, String> rows = getRowsToStore(record, template.isEncrypted());

      for (Map.Entry<String, String> fieldNameIndexed : rows.entrySet()) {
        String[] fieldNameAndIndex = StringUtil.split(fieldNameIndexed.getKey(), SEPARATOR);
        String fieldName = fieldNameAndIndex[0];
        int fieldValueIndex = Integer.parseInt(fieldNameAndIndex[1]);
        String fieldValue = fieldNameIndexed.getValue();
        update.setString(1, fieldValue);
        update.setInt(2, recordId);
        update.setString(3, fieldName);
        update.setInt(4, fieldValueIndex);

        int nbRowsCount = update.executeUpdate();
        if (nbRowsCount == 0) {
          // no row has been updated because the field fieldName doesn't exist in database.
          // The form has changed since the last modification of the record.
          // So we must insert this new field.
          executeInsertQuery(con, recordId, fieldValueIndex, fieldName, fieldValue);
        }
      }
    } catch (CryptoException e) {
      throw new FormException(GENERIC_RECORD_SET_MANAGER, "form.ENCRYPTING_DATA_FAILED", e);
    }
  }

  private Map<String, String> getRowsToStore(GenericDataRecord record, boolean crypt)
      throws CryptoException {
    Map<String, String> rows = new HashMap<>();
    for (Field field : record.getFields()) {
      String fieldNameIndexed = field.getName()+SEPARATOR+field.getOccurrence();
      rows.put(fieldNameIndexed, field.getStringValue());
    }
    if (crypt) {
      rows = getEncryptionService().encryptContent(rows);
    }
    return rows;
  }

  /**
   * Deletes the record fields.
   */
  private void deleteFieldRows(Connection con, GenericDataRecord record)
      throws SQLException {
    try (final PreparedStatement delete = con.prepareStatement(DELETE_RECORD_FIELDS)) {
      int internalId = record.getInternalId();
      delete.setInt(1, internalId);
      delete.execute();
    }
  }

  /**
   * Deletes the record.
   */
  private void deleteRecordRows(Connection con, GenericDataRecord record)
      throws SQLException {
    try (final PreparedStatement delete = con.prepareStatement(DELETE_RECORD)) {
      int internalId = record.getInternalId();
      delete.setInt(1, internalId);
      delete.execute();
    }
  }

  private void updateTemplateId(Connection con, int newTemplateId, int recordId)
      throws SQLException {
    try (final PreparedStatement update = con.prepareStatement(MOVE_RECORD)) {
      update.setInt(1, newTemplateId);
      update.setInt(2, recordId);
      update.execute();
    }
  }

  /* Template table */

  private static final String TEMPLATE_TABLE = "SB_FormTemplate_Template";

  private static final String TEMPLATE_COLUMNS = "templateId,externalId,templateName";

  private static final String SELECT_TEMPLATE = "select " + TEMPLATE_COLUMNS
      + " from " + TEMPLATE_TABLE + " where externalId=?";

  private static final String INSERT_TEMPLATE = INSERT_INTO + TEMPLATE_TABLE
      + "(" + TEMPLATE_COLUMNS + ")" + " values (?,?,?)";

  private static final String WHERE_TEMPLATE_ID_EQUAL_GIVEN_VALUE = " where templateId=?";
  private static final String DELETE_TEMPLATE =
      DELETE_FROM + TEMPLATE_TABLE + WHERE_TEMPLATE_ID_EQUAL_GIVEN_VALUE;

  /* Template fields table */

  private static final String TEMPLATE_FIELDS_TABLE = "SB_FormTemplate_TemplateField";

  private static final String TEMPLATE_FIELDS_COLUMNS =
      "templateId,fieldName,fieldIndex,fieldType,isMandatory,isReadOnly,isHidden";

  private static final String SELECT_TEMPLATE_FIELDS =
      "select " + TEMPLATE_FIELDS_COLUMNS + " from " + TEMPLATE_FIELDS_TABLE +
          WHERE_TEMPLATE_ID_EQUAL_GIVEN_VALUE + " order by fieldIndex";

  private static final String INSERT_TEMPLATE_FIELD = INSERT_INTO
      + TEMPLATE_FIELDS_TABLE + "(" + TEMPLATE_FIELDS_COLUMNS + ")"
      + " values (?,?,?,?,?,?,?)";

  private static final String DELETE_TEMPLATE_FIELDS =
      DELETE_FROM + TEMPLATE_FIELDS_TABLE + WHERE_TEMPLATE_ID_EQUAL_GIVEN_VALUE;

  /* Record table */

  private static final String RECORD_TABLE = "SB_FormTemplate_Record";

  private static final String RECORD_COLUMNS = "recordId,templateId,externalId,lang";

  private static final String SELECT_RECORD =
      "SELECT recordId, templateId, externalId, lang FROM " +
      "sb_formtemplate_record WHERE templateId=? AND externalId=?";

  private static final String INSERT_RECORD = INSERT_INTO + RECORD_TABLE
      + "(" + RECORD_COLUMNS + ")" + " values (?,?,?,?)";

  private static final String DELETE_TEMPLATE_RECORDS =
      DELETE_FROM + RECORD_TABLE + WHERE_TEMPLATE_ID_EQUAL_GIVEN_VALUE;

  private static final String DELETE_RECORD = DELETE_FROM + RECORD_TABLE
      + " where recordId=?";

  private static final String MOVE_RECORD =
      "update " + RECORD_TABLE + " set templateId = ? where recordId = ? ";

  /* Record fields table */

  private static final String FIELDS_TABLE = "SB_FormTemplate_TextField";

  private static final String FIELDS_COLUMNS = "recordId,fieldName,fieldValue,fieldValueIndex";

  private static final String SELECT_FIELDS = "SELECT recordId, fieldName, fieldValue, fieldValueIndex FROM " +
      "sb_formtemplate_textfield WHERE recordId=? order by fieldName, fieldValueIndex";

  private static final String INSERT_FIELD = INSERT_INTO + FIELDS_TABLE
      + "(" + FIELDS_COLUMNS + ")" + " values (?,?,?,?)";

  private static final String UPDATE_FIELD = "update " + FIELDS_TABLE
      + " set fieldValue=? where recordId=? and fieldName=? and fieldValueIndex=?";

  private static final String DELETE_TEMPLATE_RECORDS_FIELDS = DELETE_FROM
      + FIELDS_TABLE + " where recordId in"
      + " (select recordId from SB_FormTemplate_Record where templateId=?)";

  private static final String DELETE_RECORD_FIELDS = DELETE_FROM
      + FIELDS_TABLE + " where recordId=?";

  private static final String TF_ALIAS = " tf, ";
  private static final String REC_ALIAS = " rec, ";
  private static final String SELECT_TEMPLATE_RECORD_VALUES =
      "select fieldValue from " + FIELDS_TABLE + TF_ALIAS + RECORD_TABLE + REC_ALIAS
          +
          TEMPLATE_TABLE +
          " tpl where tf.fieldName= ? and tf.recordId = rec.recordId and rec.externalId = ? and rec.templateId = tpl.templateId and tpl.externalId = ?";

  private static final String SELECT_TEMPLATE_RECORD_ENTRIES =
      "SELECT * FROM " + FIELDS_TABLE + TF_ALIAS + RECORD_TABLE + REC_ALIAS +
          TEMPLATE_TABLE +
          " tpl WHERE tpl.templatename = ? AND rec.templateId = tpl.templateId AND tf.recordId = rec.recordId";

  private static final String SELECT_TEMPLATE_RECORDS_BY_FIELDVALUE =
      "SELECT rec.recordId,rec.templateId,rec.externalId,rec.lang FROM " + FIELDS_TABLE + TF_ALIAS +
          RECORD_TABLE + REC_ALIAS +
          TEMPLATE_TABLE +
          " tpl WHERE tpl.templateid = ? AND rec.templateId = tpl.templateId AND tf.recordId = rec.recordId" +
          " AND tf.fieldName = ? " +
          " AND (lower(tf.fieldvalue) like lower(?)" +
          "  OR lower(tf.fieldvalue) like lower(?)" +
          "  OR lower(tf.fieldvalue) like lower(?)" +
          "  OR lower(tf.fieldvalue) like lower(?))";

  private static final String SELECT_NUMBER_OF_RECORDS_BY_TEMPLATE_AND_COMPONENTS =
      "select t.externalid, count(r.recordid) from " +
          TEMPLATE_TABLE + " t, "+ RECORD_TABLE +" r " +
          "where r.templateid = t.templateid and t.templatename = ? " +
          "GROUP BY t.externalid";

  private static final String SELECT_ALL_EXTERNAL_IDS_OF_RECORDS =
      "select distinct externalid from " + TEMPLATE_TABLE;
}