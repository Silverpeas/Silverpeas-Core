/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
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

package org.silverpeas.core.contribution.content.form.record;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.silverpeas.core.contribution.content.form.FormException;
import org.silverpeas.core.security.encryption.cipher.CryptoException;

import org.silverpeas.core.contribution.content.form.FormRuntimeException;
import org.silverpeas.core.contribution.template.publication.PublicationTemplate;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateException;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateManager;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.security.encryption.EncryptionContentIterator;
import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.exception.SilverpeasException;

public class FormEncryptionContentIterator implements EncryptionContentIterator {

  String formName;
  Iterator<Map<String, String>> contents;
  Connection con;

  public FormEncryptionContentIterator() {

  }

  public FormEncryptionContentIterator(String formName) {
    this.formName = formName;
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Map<String, String> next() {
    return contents.next();
  }

  @Override
  public boolean hasNext() {
    boolean hasNext = contents.hasNext();
    if (!hasNext) {
      commitConnection();
    }
    return hasNext;
  }

  @Override
  public void update(Map<String, String> updatedContent) {

    // generate structured data to store in database
    List<RecordRow> rowsToUpdate = new ArrayList<>();
    for (Entry<String, String> entry : updatedContent.entrySet()) {
      String key = entry.getKey();
      String[] recordIdAndFieldName = StringUtil.splitByWholeSeparator(key, "$SP$");
      RecordRow rowToUpdate =
          new RecordRow(Integer.parseInt(recordIdAndFieldName[0]), recordIdAndFieldName[1],
              entry.getValue());
      rowsToUpdate.add(rowToUpdate);
    }



    // update values in database
    try {
      getGenericRecordSetManager().updateFieldRows(getConnection(), rowsToUpdate);
    } catch (Exception e) {

      rollbackConnection();
      throw new FormRuntimeException("FormEncryptionContentIterator.update",
          SilverpeasException.ERROR, "form.EXP_UPDATE_FAILED", e);
    }


  }

  @Override
  public void onError(Map<String, String> content, CryptoException ex) {
    SilverTrace.error("form", this.getClass().getName() + ".onError()",
        "form.ERROR_DURING_ENCRYPTION", ex);
    rollbackConnection();
    throw new FormRuntimeException("FormEncryptionContentIterator", SilverpeasException.ERROR,
        "form.ERROR_DURING_ENCRYPTION", ex);
  }

  @Override
  public void init() {
    List<Map<String, String>>contents = new ArrayList<>();
    if (StringUtil.isDefined(formName)) {
      // encrypting/decrypting values...
      Map<String, String> toProcess = getFormData(formName);
      contents.add(toProcess);
    } else {
      // get all crypted forms
      List<PublicationTemplate> forms;
      try {
        forms = PublicationTemplateManager.getInstance().getCryptedPublicationTemplates();
      } catch (PublicationTemplateException e) {
        throw new FormRuntimeException("FormEncryptionContentIterator", SilverpeasException.ERROR,
            "form.EXP_SELECT_FAILED", e);
      }
      contents.addAll(
          forms.stream().map(form -> getFormData(form.getFileName())).collect(Collectors.toList()));
    }
    this.contents = contents.iterator();
    openConnection();
  }

  private Map<String, String> getFormData(String formName) {
    // get all data to process...
    List<RecordRow> rows;
    try {
      rows = getGenericRecordSetManager().getAllRecordsOfTemplate(formName);
    } catch (FormException e) {
      throw new FormRuntimeException("FormEncryptionContentIterator.getFormData",
          SilverpeasException.ERROR, "form.EXP_SELECT_FAILED", e);
    }

    // start to generate a compliant structure to encryption service
    Map<String, String> toProcess = new HashMap<>();
    for (RecordRow row : rows) {
      toProcess.put(row.getRecordId()+"$SP$"+row.getFieldName(), row.getFieldValue());
    }



    return toProcess;
  }

  private static GenericRecordSetManager getGenericRecordSetManager() {
    return GenericRecordSetManager.getInstance();
  }

  /**
   * Returns a connection.
   */
  private void openConnection() {
    try {
      con = DBUtil.openConnection();
      con.setAutoCommit(false);
    } catch (Exception e) {
      throw new FormRuntimeException("FormEncryptionContentIterator.openConnection",
          SilverpeasException.ERROR, "root.EX_CONNECTION_OPEN_FAILED", e);
    }
  }

  private Connection getConnection() {
    return con;
  }

  private void rollbackConnection() {
    DBUtil.rollback(con);
    DBUtil.close(con);
  }

  private void commitConnection() {
    try {
      con.commit();
    } catch (SQLException e) {
      throw new FormRuntimeException("FormEncryptionContentIterator.commitConnection",
          SilverpeasException.ERROR, "root.EX_ERR_COMMIT", e);
    } finally {
      DBUtil.close(con);
    }
  }
}