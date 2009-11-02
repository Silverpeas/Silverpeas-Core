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
 * FLOSS exception.  You should have recieved a copy of the text describing
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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import com.silverpeas.form.DataRecord;
import com.silverpeas.form.Field;
import com.silverpeas.form.FieldTemplate;
import com.silverpeas.form.FormException;
import com.silverpeas.form.RecordSet;
import com.silverpeas.form.RecordTemplate;
import com.silverpeas.form.dummy.DummyRecordSet;
import com.silverpeas.form.fieldDisplayer.WysiwygFCKFieldDisplayer;
import com.silverpeas.publicationTemplate.PublicationTemplate;
import com.silverpeas.publicationTemplate.PublicationTemplateException;
import com.silverpeas.publicationTemplate.PublicationTemplateManager;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.i18n.I18NHelper;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.JNDINames;

/**
 * The GenericRecordSetManage all the GenericRecordSet
 */
public class GenericRecordSetManager {
  /**
   * Builds a new GenericRecordSet.
   */
  static public GenericRecordSet createRecordSet(String externalId,
      RecordTemplate template) throws FormException {
    return createRecordSet(externalId, template, null);
  }

  // public GenericRecordSet createRecordSet(String externalId, RecordTemplate
  // template, String templateName, boolean migration) throws FormException {

  static public GenericRecordSet createRecordSet(String externalId,
      RecordTemplate template, String templateName) throws FormException {
    Connection con = null;
    IdentifiedRecordTemplate identifiedTemplate = new IdentifiedRecordTemplate(
        template);
    identifiedTemplate.setExternalId(externalId);
    identifiedTemplate.setTemplateName(templateName);

    try {
      con = getConnection();

      IdentifiedRecordTemplate existingOne = selectTemplateRow(con, externalId);
      if (existingOne == null) {
        insertTemplateRow(con, identifiedTemplate);
        if (templateName == null) {
          insertTemplateFieldRows(con, identifiedTemplate);
        }
      }
      con.commit();

      if (existingOne == null) {
        GenericRecordSet newSet = new GenericRecordSet(identifiedTemplate);
        cacheRecordSet(externalId, newSet);
        return newSet;
      } else {
        return (GenericRecordSet) getRecordSet(externalId);
      }
    } catch (SQLException e) {
      rollbackConnection(con);
      throw new FormException("GenericRecordSetManager",
          "form.EXP_INSERT_FAILED", e);
    } finally {
      closeConnection(con);
    }
  }

  /**
   * Returns the record set known be its external id.
   * 
   * @throw FormException when the id is unknown.
   */
  static public RecordSet getRecordSet(String externalId) throws FormException {
    SilverTrace.debug("form", "GenericRecordSetManager.getRecordSet",
        "root.MSG_GEN_ENTER_METHOD", "externalId = " + externalId);
    GenericRecordSet cachedSet = getCachedRecordSet(externalId);
    if (cachedSet != null)
      return cachedSet;

    Connection con = null;
    IdentifiedRecordTemplate template = null;

    try {
      con = getConnection();
      template = selectTemplateRow(con, externalId);
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
      throw new FormException("GenericRecordSetManager",
          "form.EXP_SELECT_FAILED", e);
    } finally {
      closeConnection(con);
    }
  }

  /**
   * Removes the record set known by its external id.
   * 
   * @throw FormException when the id is unknown.
   */
  static public void removeRecordSet(String externalId) throws FormException {
    removeCachedRecordSet(externalId);

    Connection con = null;
    IdentifiedRecordTemplate template = null;

    try {
      con = getConnection();
      template = selectTemplateRow(con, externalId);
      if (template == null) {
        // throw new FormException("GenericRecordSetManager",
        // "form.EXP_UNKNOWN_TEMPLATE", externalId);
        SilverTrace.error("form", "GenericRecordSetManager.removeRecordSet",
            "form.EXP_UNKNOWN_TEMPLATE", "externalId = " + externalId);
      } else {
        deleteFieldRows(con, template);
        deleteRecordRows(con, template);
        deleteTemplateFieldRows(con, template);
        deleteTemplateRow(con, template);
      }
      con.commit();
    } catch (SQLException e) {
      rollbackConnection(con);
      throw new FormException("GenericRecordSetManager",
          "form.EXP_DELETE_FAILED", e);
    } finally {
      closeConnection(con);
    }
  }

  /*
   * Cache manipulation
   */
  static private GenericRecordSet getCachedRecordSet(String externalId) {
    return (GenericRecordSet) cache.get(externalId);
  }

  static private void cacheRecordSet(String externalId, GenericRecordSet set) {
    cache.put(externalId, set);
  }

  static private void removeCachedRecordSet(String externalId) {
    cache.remove(externalId);
  }

  static private HashMap cache = new HashMap();

  /**
   * Returns the DataRecord registered by the pair (templateId, recordId)
   * 
   * @throw FormException when the (templateId, recordId) pair is unknown.
   */
  static public DataRecord getRecord(IdentifiedRecordTemplate template,
      String recordId) throws FormException {
    return getRecord(template, recordId, null);
  }

  static public DataRecord getRecord(IdentifiedRecordTemplate template,
      String recordId, String language) throws FormException {
    SilverTrace.debug("form", "GenericRecordSetManager.getRecord",
        "root.MSG_GEN_PARAM_VALUE", "recordId = " + recordId + ", language = "
            + language);
    Connection con = null;
    GenericDataRecord record = null;

    try {
      con = getConnection();
      record = selectRecordRow(con, template, recordId, language);
      if (record == null)
        return null;

      selectFieldRows(con, template, record);
      return record;
    } catch (SQLException e) {
      throw new FormException("GenericRecordSetManager",
          "form.EXP_SELECT_FAILED", e);
    } finally {
      closeConnection(con);
    }
  }

  static public List getLanguagesOfRecord(IdentifiedRecordTemplate template,
      String externalId) throws FormException {
    SilverTrace.debug("form", "GenericRecordSetManager.getLanguagesOfRecord",
        "root.MSG_GEN_PARAM_VALUE", "externalId = " + externalId);

    Connection con = null;
    try {
      con = getConnection();
      return selectLanguagesOfRecord(con, template, externalId);
    } catch (SQLException e) {
      throw new FormException("GenericRecordSetManager",
          "form.EXP_SELECT_FAILED", e);
    } finally {
      closeConnection(con);
    }
  }

  /**
   * Register the DataRecord with the pair (templateId, recordId)
   * 
   * @throw FormException when the (templateId, recordId) pair is already known.
   * @throw FormException when the templateId is unknown.
   */
  static public void insertRecord(IdentifiedRecordTemplate template,
      DataRecord insertedRecord) throws FormException {
    Connection con = null;

    try {
      GenericDataRecord record = (GenericDataRecord) insertedRecord;

      con = getConnection();
      insertRecordRow(con, template, record);
      insertFieldRows(con, template, record);
      con.commit();
    } catch (ClassCastException e) {
      throw new FormException("GenericRecordSetManager",
          "form.EXP_UNKNOWN_TEMPLATE", e);
    } catch (SQLException e) {
      rollbackConnection(con);
      throw new FormException("GenericRecordSetManager",
          "form.EXP_INSERT_FAILED", e);
    } finally {
      closeConnection(con);
    }
  }

  static public void cloneRecord(IdentifiedRecordTemplate templateFrom,
      String recordIdFrom, IdentifiedRecordTemplate templateTo,
      String recordIdTo, Hashtable fileIds) throws FormException {
    SilverTrace.debug("form", "GenericRecordSetManager.cloneRecord",
        "root.MSG_GEN_ENTER_METHOD", "recordIdFrom = " + recordIdFrom
            + ", recordIdTo = " + recordIdTo);

    Iterator languages = I18NHelper.getLanguages();
    while (languages.hasNext()) {
      String language = (String) languages.next();

      GenericDataRecord record = (GenericDataRecord) getRecord(templateFrom,
          recordIdFrom, language);
      if (record != null) {
        record.setInternalId(-1);
        record.setId(recordIdTo);

        Field[] fields = record.getFields();
        Field field = null;
        for (int f = 0; f < fields.length; f++) {
          field = fields[f];
          if (Field.TYPE_FILE.equals(field.getTypeName())) {
            // le formulaire contient un champ de type File (fichier ou image)
            // Remplacement de l'ancien id par le nouveau
            String oldId = field.getStringValue();
            if (oldId != null) {
              String newId = (String) fileIds.get(oldId);
              field.setStringValue(newId);
            }
          } else {
            String oldValue = field.getStringValue();
            if (oldValue != null
                && oldValue.startsWith(WysiwygFCKFieldDisplayer.dbKey)) {
              // Wysiwyg case
              String newValue = oldValue.replaceAll(recordIdFrom, recordIdTo);
              field.setStringValue(newValue);
            }
          }
        }

        insertRecord(templateTo, record);
      }
    }
  }

  /**
   * Saves the DataRecord registered by the pair (templateId, recordId)
   * 
   * @throw FormException when the (templateId, recordId) pair is unknown.
   */
  static public void updateRecord(IdentifiedRecordTemplate template,
      DataRecord updatedRecord) throws FormException {
    Connection con = null;

    try {
      GenericDataRecord record = (GenericDataRecord) updatedRecord;

      con = getConnection();
      updateFieldRows(con, template, record);
      con.commit();
    } catch (ClassCastException e) {
      throw new FormException("GenericRecordSetManager",
          "form.EXP_UNKNOWN_TEMPLATE", e);
    } catch (SQLException e) {
      rollbackConnection(con);
      throw new FormException("GenericRecordSetManager",
          "form.EXP_UPDATED_FAILED", e);
    } finally {
      closeConnection(con);
    }
  }

  /**
   * Deletes the DataRecord registered by the pair (templateId, recordId)
   * 
   * @throw FormException when the (templateId, recordId) pair is unknown.
   */
  static public void deleteRecord(IdentifiedRecordTemplate template,
      DataRecord deletedRecord) throws FormException {
    Connection con = null;

    try {
      GenericDataRecord record = (GenericDataRecord) deletedRecord;

      con = getConnection();
      deleteFieldRows(con, template, record);
      deleteRecordRows(con, template, record);
      con.commit();
    } catch (ClassCastException e) {
      throw new FormException("GenericRecordSetManager",
          "form.EXP_UNKNOWN_TEMPLATE", e);
    } catch (SQLException e) {
      rollbackConnection(con);
      throw new FormException("GenericRecordSetManager",
          "form.EXP_DELETE_FAILED", e);
    } finally {
      closeConnection(con);
    }
  }

  /**
   * get the template field declarations directly from the xml file
   */
  static private void selectTemplateFieldsFromXML(
      IdentifiedRecordTemplate template) throws FormException {
    GenericRecordTemplate genericRecordTemplate = null;
    try {
      PublicationTemplate publicationTemplateImpl = PublicationTemplateManager
          .loadPublicationTemplate(template.getTemplateName());
      genericRecordTemplate = (GenericRecordTemplate) publicationTemplateImpl
          .getRecordTemplate();
    } catch (PublicationTemplateException e) {
      throw new FormException("GenericRecordSetManager",
          "form.EXP_UNKNOWN_TEMPLATE", e);
    }

    GenericRecordTemplate wrapped = (GenericRecordTemplate) template
        .getWrappedTemplate();

    GenericFieldTemplate fieldTemplate = null;
    String fieldName;
    String fieldType;
    boolean isMandatory;
    boolean isReadOnly;
    boolean isHidden;
    FieldTemplate[] fields = genericRecordTemplate.getFieldTemplates();

    for (int i = 0; i < fields.length; i++) {
      fieldName = fields[i].getFieldName();
      fieldType = fields[i].getTypeName();
      isMandatory = fields[i].isMandatory();
      isReadOnly = fields[i].isReadOnly();
      isHidden = fields[i].isHidden();

      fieldTemplate = new GenericFieldTemplate(fieldName, fieldType);
      fieldTemplate.setMandatory(isMandatory);
      fieldTemplate.setReadOnly(isReadOnly);
      fieldTemplate.setHidden(isHidden);

      wrapped.addFieldTemplate(fieldTemplate);
    }
  }

  /**
   * Returns a connection.
   */
  static synchronized private Connection getConnection() throws FormException {
    Connection con = null;
    try {
      Context ctx = new InitialContext();
      DataSource src = (DataSource) ctx
          .lookup(JNDINames.FORMTEMPLATE_DATASOURCE);
      con = src.getConnection();
      con.setAutoCommit(false);
    } catch (NamingException e) {
      // throw new FormException("GenericRecordSetManager",
      // "root.EX_DATASOURCE_NOT_FOUND", e);

      // the JNDI name have not been found in the current context
      // The caller is not takes place in any context (web application nor ejb
      // container)
      // So lookup operation cannot find JNDI properties !
      // This is absolutly normal according to the j2ee specification
      // Unfortunately, only BES takes care about this spec. This exception
      // doesn't appear with orion or BEA !

      try {
        // Get the initial Context
        Context ctx = new InitialContext();
        // Look up the datasource directly without JNDI access
        DataSource dataSource = (DataSource) ctx
            .lookup(JNDINames.DIRECT_DATASOURCE);
        // Create a connection object
        con = dataSource.getConnection();
        con.setAutoCommit(false);
      } catch (NamingException ne) {
        throw new FormException("GenericRecordSetManager.getConnection()",
            "Data source " + JNDINames.DIRECT_DATASOURCE + " not found", ne);
      } catch (SQLException se) {
        throw new FormException("GenericRecordSetManager.getConnection()",
            "root.EX_DATASOURCE_INVALID", JNDINames.DIRECT_DATASOURCE, se);
      }
    } catch (SQLException e) {
      rollbackConnection(con);
      throw new FormException("GenericRecordSetManager",
          "root.EX_DATASOURCE_INVALID", e);
    }
    return con;
  }

  /**
   * Returns the next id in the named table.
   */
  static private int getNextId(String tableName, String idColumn)
      throws SQLException {
    int nextId = 0;

    try {
      nextId = com.stratelia.webactiv.util.DBUtil
          .getNextId(tableName, idColumn);
    } catch (Exception e) {
      throw new SQLException(e.toString());
    }

    if (nextId == 0)
      return 1;
    else
      return nextId;
  }

  /**
   * Creates the template declaration row in SB_FormTemplate_Template.
   */
  static private void insertTemplateRow(Connection con,
      IdentifiedRecordTemplate template) throws SQLException {
    PreparedStatement insert = null;

    try {
      int internalId = getNextId(TEMPLATE_TABLE, "templateId");
      template.setInternalId(internalId);
      String externalId = template.getExternalId();
      String templateName = template.getTemplateName();

      insert = con.prepareStatement(INSERT_TEMPLATE);
      insert.setInt(1, internalId);
      insert.setString(2, externalId);
      insert.setString(3, templateName);
      insert.execute();
    } finally {
      DBUtil.close(insert);
    }
  }

  /**
   * Creates a row for each template_fields.
   */
  static private void insertTemplateFieldRows(Connection con,
      IdentifiedRecordTemplate template) throws SQLException, FormException {
    PreparedStatement insert = null;

    try {
      insert = con.prepareStatement(INSERT_TEMPLATE_FIELD);

      int internalId = template.getInternalId();
      String fieldName;
      int fieldIndex;
      String fieldType;
      boolean isMandatory;
      boolean isReadOnly;
      boolean isHidden;

      FieldTemplate[] fields = template.getFieldTemplates();
      for (int i = 0; i < fields.length; i++) {
        fieldName = fields[i].getFieldName();
        fieldIndex = i;
        fieldType = fields[i].getTypeName();
        isMandatory = fields[i].isMandatory();
        isReadOnly = fields[i].isReadOnly();
        isHidden = fields[i].isHidden();

        insert.setInt(1, internalId);
        insert.setString(2, fieldName);
        insert.setInt(3, fieldIndex);
        insert.setString(4, fieldType);
        if (isMandatory)
          insert.setInt(5, 1);
        else
          insert.setInt(5, 0);

        if (isReadOnly)
          insert.setInt(6, 1);
        else
          insert.setInt(6, 0);

        if (isHidden)
          insert.setInt(7, 1);
        else
          insert.setInt(7, 1);

        insert.execute();
      }
    } finally {
      DBUtil.close(insert);
    }
  }

  /**
   * Select the template header.
   */
  static private IdentifiedRecordTemplate selectTemplateRow(Connection con,
      String externalId) throws SQLException {
    PreparedStatement select = null;
    ResultSet rs = null;

    try {
      select = con.prepareStatement(SELECT_TEMPLATE);
      select.setString(1, externalId);
      rs = select.executeQuery();

      if (!rs.next())
        return null;
      int internalId = rs.getInt(1);
      String templateName = rs.getString(3);

      IdentifiedRecordTemplate template = new IdentifiedRecordTemplate(
          new GenericRecordTemplate());
      template.setInternalId(internalId);
      template.setExternalId(externalId);
      template.setTemplateName(templateName);
      return template;
    } finally {
      DBUtil.close(rs, select);
    }
  }

  /**
   * Select the template field declarations.
   */
  static private void selectTemplateFieldRows(Connection con,
      IdentifiedRecordTemplate template) throws SQLException, FormException {
    PreparedStatement select = null;
    ResultSet rs = null;
    GenericRecordTemplate wrapped = (GenericRecordTemplate) template
        .getWrappedTemplate();

    try {
      select = con.prepareStatement(SELECT_TEMPLATE_FIELDS);
      select.setInt(1, template.getInternalId());
      rs = select.executeQuery();

      GenericFieldTemplate fieldTemplate = null;
      String fieldName;
      String fieldType;
      boolean isMandatory;
      boolean isReadOnly;
      boolean isHidden;
      while (rs.next()) {
        // templateId = rs.getInt(1);
        fieldName = rs.getString(2);
        // fieldIndex = rs.getInt(3);
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
    } finally {
      DBUtil.close(rs, select);
    }
  }

  /**
   * Deletes all the fields of the records built on this template.
   */
  static private void deleteFieldRows(Connection con,
      IdentifiedRecordTemplate template) throws SQLException {
    PreparedStatement delete = null;

    try {
      int internalId = template.getInternalId();

      delete = con.prepareStatement(DELETE_TEMPLATE_RECORDS_FIELDS);
      delete.setInt(1, internalId);
      delete.execute();
    } finally {
      DBUtil.close(delete);
    }
  }

  /**
   * Deletes all the records built on this template.
   */
  static private void deleteRecordRows(Connection con,
      IdentifiedRecordTemplate template) throws SQLException {
    PreparedStatement delete = null;

    try {
      int internalId = template.getInternalId();

      delete = con.prepareStatement(DELETE_TEMPLATE_RECORDS);
      delete.setInt(1, internalId);
      delete.execute();
    } finally {
      DBUtil.close(delete);
    }
  }

  /**
   * Deletes the templatefields.
   */
  static private void deleteTemplateFieldRows(Connection con,
      IdentifiedRecordTemplate template) throws SQLException {
    PreparedStatement delete = null;

    try {
      int internalId = template.getInternalId();

      delete = con.prepareStatement(DELETE_TEMPLATE_FIELDS);
      delete.setInt(1, internalId);
      delete.execute();
    } finally {
      DBUtil.close(delete);
    }
  }

  /**
   * Deletes the template.
   */
  static private void deleteTemplateRow(Connection con,
      IdentifiedRecordTemplate template) throws SQLException {
    PreparedStatement delete = null;

    try {
      int internalId = template.getInternalId();

      delete = con.prepareStatement(DELETE_TEMPLATE);
      delete.setInt(1, internalId);
      delete.execute();
    } finally {
      DBUtil.close(delete);
    }
  }

  /**
   * Creates the record declaration row in SB_FormTemplate_Record.
   */
  static private void insertRecordRow(Connection con,
      IdentifiedRecordTemplate template, GenericDataRecord record)
      throws SQLException {
    PreparedStatement insert = null;

    try {
      int internalId = getNextId(RECORD_TABLE, "recordId");
      record.setInternalId(internalId);
      int templateId = template.getInternalId();
      String externalId = record.getId();

      SilverTrace.debug("form", "GenericRecordSetManager.insertRecordRow",
          "root.MSG_GEN_PARAM_VALUE", "internalId = " + internalId
              + ", templateId = " + templateId + ", externalId = " + externalId
              + ", language = " + record.getLanguage());

      insert = con.prepareStatement(INSERT_RECORD);
      insert.setInt(1, internalId);
      insert.setInt(2, templateId);
      insert.setString(3, externalId);
      if (!I18NHelper.isI18N
          || I18NHelper.isDefaultLanguage(record.getLanguage()))
        insert.setNull(4, Types.VARCHAR);
      else
        insert.setString(4, record.getLanguage());
      insert.execute();
    } finally {
      DBUtil.close(insert);
    }
  }

  /**
   * Creates a row for each template_fields.
   */
  static private void insertFieldRows(Connection con,
      IdentifiedRecordTemplate template, GenericDataRecord record)
      throws SQLException, FormException {
    PreparedStatement insert = null;

    try {
      insert = con.prepareStatement(INSERT_FIELD);

      Field field = null;
      int recordId = record.getInternalId();
      // int fieldIndex ;
      String fieldName;
      String fieldValue;

      String[] fieldNames = record.getFieldNames();
      for (int i = 0; i < fieldNames.length; i++) {
        // fieldIndex = i;
        fieldName = (String) fieldNames[i];
        field = record.getField(fieldName);
        fieldValue = (String) field.getStringValue();

        insert.setInt(1, recordId);
        // insert.setInt(2, fieldIndex);
        insert.setString(2, fieldName);
        insert.setString(3, fieldValue);

        insert.execute();
      }
    } finally {
      DBUtil.close(insert);
    }
  }

  /**
   * Select the template header.
   */
  static private GenericDataRecord selectRecordRow(Connection con,
      IdentifiedRecordTemplate template, String externalId, String language)
      throws SQLException, FormException {
    SilverTrace.debug("form", "GenericRecordSetManager.selectRecordRow",
        "root.MSG_GEN_ENTER_METHOD", "templateId = " + template.getInternalId()
            + ", externalId = " + externalId + ", language = " + language);
    PreparedStatement select = null;
    ResultSet rs = null;

    try {
      if (!I18NHelper.isI18N || I18NHelper.isDefaultLanguage(language))
        language = null;

      if (language != null)
        select = con.prepareStatement(SELECT_RECORD + " and lang = ? ");
      else
        select = con.prepareStatement(SELECT_RECORD + " and lang is null");

      select.setInt(1, template.getInternalId());
      select.setString(2, externalId);

      if (language != null)
        select.setString(3, language);

      rs = select.executeQuery();

      if (!rs.next())
        return null;

      int internalId = rs.getInt(1);

      GenericDataRecord record = new GenericDataRecord(template);
      record.setInternalId(internalId);
      record.setId(externalId);
      record.setLanguage(language);

      return record;
    } finally {
      DBUtil.close(rs, select);
    }
  }

  /**
   * Select the template field declarations.
   */
  static private void selectFieldRows(Connection con,
      IdentifiedRecordTemplate template, GenericDataRecord record)
      throws SQLException, FormException {
    PreparedStatement select = null;
    ResultSet rs = null;

    try {
      select = con.prepareStatement(SELECT_FIELDS);
      select.setInt(1, record.getInternalId());
      rs = select.executeQuery();

      Field field = null;
      // int fieldIndex ;
      String fieldName;
      String fieldValue;
      while (rs.next()) {
        // fieldIndex = rs.getInt(2);
        fieldName = rs.getString(2);
        fieldValue = rs.getString(3);

        field = record.getField(fieldName);
        if (field != null) {
          // We have found a field corresponding to the fieldName
          SilverTrace.debug("form", "GenericRecordSetManager.selectFieldRows",
              "root.MSG_GEN_PARAM_VALUE", "fieldName = " + fieldName
                  + ", fieldValue = " + fieldValue);
          field.setStringValue(fieldValue);
        } else {
          // We have not found a field corresponding to the fieldName
          // Maybe the form has changed and the field has been deleted
        }
      }
    } finally {
      DBUtil.close(rs, select);
    }
  }

  static private List selectLanguagesOfRecord(Connection con,
      IdentifiedRecordTemplate template, String externalId)
      throws SQLException, FormException {
    PreparedStatement select = null;
    ResultSet rs = null;

    List languages = new ArrayList();
    try {
      select = con.prepareStatement(SELECT_RECORD);

      select.setInt(1, template.getInternalId());
      select.setString(2, externalId);

      rs = select.executeQuery();

      String language = null;
      while (rs.next()) {
        language = rs.getString(4);
        if (!StringUtil.isDefined(language))
          language = I18NHelper.defaultLanguage;
        languages.add(language);
      }
      return languages;
    } finally {
      DBUtil.close(rs, select);
    }
  }

  /**
   * Updates the records fields.
   */
  static private void updateFieldRows(Connection con,
      IdentifiedRecordTemplate template, GenericDataRecord record)
      throws SQLException, FormException {
    PreparedStatement update = null;
    PreparedStatement insert = null;

    try {
      update = con.prepareStatement(UPDATE_FIELD);

      Field field = null;
      int recordId = record.getInternalId();
      String fieldName;
      String fieldValue;
      int nbRowsCount = 0;

      String[] fieldNames = record.getFieldNames();
      for (int i = 0; i < fieldNames.length; i++) {
        fieldName = (String) fieldNames[i];
        field = record.getField(fieldName);
        fieldValue = (String) field.getStringValue();

        update.setString(1, fieldValue);
        update.setInt(2, recordId);
        update.setString(3, fieldName);

        nbRowsCount = update.executeUpdate();
        if (nbRowsCount == 0) {
          // no row has been updated because the field fieldName doesn't exist
          // in database.
          // The form has changed since the last modification of the record.
          // So we must insert this new field.
          insert = con.prepareStatement(INSERT_FIELD);

          insert.setInt(1, recordId);
          insert.setString(2, fieldName);
          insert.setString(3, fieldValue);

          insert.execute();
        }
      }
    } finally {
      DBUtil.close(update);
      DBUtil.close(insert);
    }
  }

  /**
   * Deletes the record fields.
   */
  static private void deleteFieldRows(Connection con,
      IdentifiedRecordTemplate template, GenericDataRecord record)
      throws SQLException {
    PreparedStatement delete = null;

    try {
      int internalId = record.getInternalId();

      delete = con.prepareStatement(DELETE_RECORD_FIELDS);
      delete.setInt(1, internalId);
      delete.execute();
    } finally {
      DBUtil.close(delete);
    }
  }

  /**
   * Deletes the record.
   */
  static private void deleteRecordRows(Connection con,
      IdentifiedRecordTemplate template, GenericDataRecord record)
      throws SQLException {
    PreparedStatement delete = null;

    try {
      int internalId = record.getInternalId();

      delete = con.prepareStatement(DELETE_RECORD);
      delete.setInt(1, internalId);
      delete.execute();
    } finally {
      DBUtil.close(delete);
    }
  }

  static private void closeConnection(Connection con) throws FormException {
    if (con != null) {
      try {
        con.close();
      } catch (SQLException e) {
        throw new FormException("GenericRecordSetManager",
            "form.EXP_CLOSE_FAILED", e);
      }
    }
  }

  static private void rollbackConnection(Connection con) {
    if (con != null) {
      try {
        con.rollback();
      } catch (SQLException ignored) {
      }
    }
  }

  /* Template table */

  static final private String TEMPLATE_TABLE = "SB_FormTemplate_Template";

  static final private String TEMPLATE_COLUMNS = "templateId,externalId,templateName";

  static final private String SELECT_TEMPLATE = "select " + TEMPLATE_COLUMNS
      + " from " + TEMPLATE_TABLE + " where externalId=?";

  static final private String INSERT_TEMPLATE = "insert into " + TEMPLATE_TABLE
      + "(" + TEMPLATE_COLUMNS + ")" + " values (?,?,?)";

  static final private String DELETE_TEMPLATE = "delete from " + TEMPLATE_TABLE
      + " where templateId=?";

  /* Template fields table */

  static final private String TEMPLATE_FIELDS_TABLE = "SB_FormTemplate_TemplateField";

  static final private String TEMPLATE_FIELDS_COLUMNS = "templateId,fieldName,fieldIndex,fieldType,isMandatory,isReadOnly,isHidden";

  static final private String SELECT_TEMPLATE_FIELDS = "select "
      + TEMPLATE_FIELDS_COLUMNS + " from " + TEMPLATE_FIELDS_TABLE
      + " where templateId=?" + " order by fieldIndex";

  static final private String INSERT_TEMPLATE_FIELD = "insert into "
      + TEMPLATE_FIELDS_TABLE + "(" + TEMPLATE_FIELDS_COLUMNS + ")"
      + " values (?,?,?,?,?,?,?)";

  static final private String DELETE_TEMPLATE_FIELDS = "delete from "
      + TEMPLATE_FIELDS_TABLE + " where templateId=?";

  /* Record table */

  static final private String RECORD_TABLE = "SB_FormTemplate_Record";

  static final private String RECORD_COLUMNS = "recordId,templateId,externalId,lang";

  static final private String SELECT_RECORD = "select " + RECORD_COLUMNS
      + " from " + RECORD_TABLE + " where templateId=? and externalId=?";

  static final private String INSERT_RECORD = "insert into " + RECORD_TABLE
      + "(" + RECORD_COLUMNS + ")" + " values (?,?,?,?)";

  static final private String DELETE_TEMPLATE_RECORDS = "delete from "
      + RECORD_TABLE + " where templateId=?";

  static final private String DELETE_RECORD = "delete from " + RECORD_TABLE
      + " where recordId=?";

  /* Record fields table */

  static final private String FIELDS_TABLE = "SB_FormTemplate_TextField";

  static final private String FIELDS_COLUMNS = "recordId,fieldName,fieldValue";

  static final private String SELECT_FIELDS = "select " + FIELDS_COLUMNS
      + " from " + FIELDS_TABLE + " where recordId=?";
  // + " order by fieldIndex";

  static final private String INSERT_FIELD = "insert into " + FIELDS_TABLE
      + "(" + FIELDS_COLUMNS + ")" + " values (?,?,?)";

  static final private String UPDATE_FIELD = "update " + FIELDS_TABLE
      + " set fieldValue=? where recordId=? and fieldName=?";

  static final private String DELETE_TEMPLATE_RECORDS_FIELDS = "delete from "
      + FIELDS_TABLE + " where recordId in"
      + " (select recordId from SB_FormTemplate_Record where templateId=?)";

  static final private String DELETE_RECORD_FIELDS = "delete from "
      + FIELDS_TABLE + " where recordId=?";
}
