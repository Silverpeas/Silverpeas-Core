/*
 * Copyright (C) 2000 - 2026 Silverpeas
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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.silverpeas.core.contribution.content.form.FormException;
import org.silverpeas.core.contribution.content.form.field.TextFieldImpl;
import org.silverpeas.core.contribution.content.form.notification.FormRecordEventNotifier;
import org.silverpeas.core.contribution.content.form.notification.FormRecordReference;
import org.silverpeas.core.i18n.I18n;
import org.silverpeas.core.notification.system.ResourceEvent;
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.persistence.TransactionRuntimeException;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.core.util.Process;
import org.silverpeas.kernel.test.annotations.TestManagedMock;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** Exercises the persistence entry points against a real SQL database. */
@EnableSilverTestEnv(context = JEETestContext.class)
class GenericRecordSetManagerNotificationTest {

  private static final String URL = "jdbc:h2:mem:form-events;DB_CLOSE_DELAY=-1";
  private static final String TEMPLATE_ID = "webPages42:example";

  @TestManagedMock
  private FormRecordEventNotifier notifier;
  @TestManagedMock
  private Transaction transaction;

  private MockedStatic<DBUtil> database;
  private IdentifiedRecordTemplate template;
  private GenericRecordSetManager manager;

  @BeforeEach
  void setUp() throws Exception {
    var i18n = I18n.get();
    when(i18n.isEnabled()).thenReturn(true);
    when(i18n.isDefaultLanguage("fr")).thenReturn(true);
    doAnswer(invocation -> {
      try {
        return ((Process<?>) invocation.getArgument(0)).execute();
      } catch (Exception e) {
        throw new TransactionRuntimeException(e);
      }
    }).when(transaction).perform(any());
    try (Connection con = DriverManager.getConnection(URL); var sql = con.createStatement()) {
      sql.execute("DROP ALL OBJECTS");
      sql.execute("CREATE TABLE SB_FormTemplate_Template " +
          "(templateId INT PRIMARY KEY, externalId VARCHAR, templateName VARCHAR)");
      sql.execute("CREATE TABLE SB_FormTemplate_Record " +
          "(recordId INT PRIMARY KEY, templateId INT, externalId VARCHAR, lang VARCHAR)");
      sql.execute("CREATE TABLE SB_FormTemplate_TextField " +
          "(recordId INT, fieldName VARCHAR, fieldValue VARCHAR, fieldValueIndex INT)");
      sql.execute("CREATE TABLE SB_FormTemplate_TemplateField (templateId INT, fieldName VARCHAR, " +
          "fieldIndex INT, fieldType VARCHAR, isMandatory INT, isReadOnly INT, isHidden INT)");
      sql.execute("INSERT INTO SB_FormTemplate_Template VALUES (1, '" + TEMPLATE_ID + "', null)");
    }
    database = mockStatic(DBUtil.class);
    database.when(DBUtil::openConnection).thenAnswer(i -> DriverManager.getConnection(URL));
    AtomicInteger ids = new AtomicInteger(10);
    database.when(() -> DBUtil.getNextId(anyString(), anyString()))
        .thenAnswer(i -> ids.incrementAndGet());
    template = new IdentifiedRecordTemplate(new GenericRecordTemplate());
    template.setInternalId(1);
    template.setExternalId(TEMPLATE_ID);
    manager = GenericRecordSetManager.getInstance();
  }

  @AfterEach
  void closeDatabaseMock() {
    if (database != null) {
      database.close();
    }
  }

  @Test
  void insertAndUpdateNotifyExactlyOnceWithExternalIdentifiers() throws Exception {
    GenericDataRecord record = record("en");
    manager.insertRecord(template, record);
    var reference = new FormRecordReference(TEMPLATE_ID, "0", "en");
    verify(notifier).notifyEventOn(ResourceEvent.Type.CREATION, reference);
    record.getField("description").setStringValue("Updated value");
    manager.updateRecord(template, record);
    try (var con = DriverManager.getConnection(URL); var sql = con.createStatement();
        var rows = sql.executeQuery("SELECT fieldValue FROM SB_FormTemplate_TextField")) {
      assertTrue(rows.next());
      assertEquals("Updated value", rows.getString(1));
    }
    verify(notifier).notifyEventOn(ResourceEvent.Type.UPDATE, reference, reference);
    verifyNoMoreInteractions(notifier);
    verify(transaction, times(2)).perform(any());
    assertEquals(1, countRecords());
  }

  @Test
  void deleteResolvesThePersistedTemplateEvenForAnUnwrappedRecord() throws Exception {
    GenericDataRecord record = record("en");
    manager.insertRecord(template, record);
    clearInvocations(notifier);
    manager.deleteRecord(record);
    verify(notifier).notifyEventOn(ResourceEvent.Type.DELETION,
        new FormRecordReference(TEMPLATE_ID, "0", "en"));
    verifyNoMoreInteractions(notifier);
    assertEquals(0, countRecords());
  }

  @Test
  void deletingRecordSetNotifiesEveryStoredLanguageOnce() throws Exception {
    manager.insertRecord(template, record("fr"));
    manager.insertRecord(template, record("en"));
    clearInvocations(notifier);
    manager.removeRecordSet(TEMPLATE_ID);
    verify(notifier).notifyEventOn(ResourceEvent.Type.DELETION,
        new FormRecordReference(TEMPLATE_ID, "0", null));
    verify(notifier).notifyEventOn(ResourceEvent.Type.DELETION,
        new FormRecordReference(TEMPLATE_ID, "0", "en"));
    verifyNoMoreInteractions(notifier);
    assertEquals(0, countRecords());
  }

  @Test
  void templateFieldsKeepTheirFlagsAndTemplateIdentifier() throws Exception {
    var fields = new GenericRecordTemplate();
    var mandatory = new GenericFieldTemplate("required", TextFieldImpl.class);
    mandatory.setMandatory(true);
    var readOnly = new GenericFieldTemplate("fixed", TextFieldImpl.class);
    readOnly.setReadOnly(true);
    fields.addFieldTemplate(mandatory);
    fields.addFieldTemplate(readOnly);
    manager.createRecordSet("webPages43:example", fields);
    try (var con = DriverManager.getConnection(URL); var sql = con.createStatement();
        var rows = sql.executeQuery("SELECT templateId, isMandatory, isReadOnly, isHidden " +
            "FROM SB_FormTemplate_TemplateField ORDER BY fieldIndex")) {
      assertTrue(rows.next());
      int templateId = rows.getInt(1);
      assertEquals(1, rows.getInt(2));
      assertEquals(0, rows.getInt(3));
      assertEquals(1, rows.getInt(4));
      assertTrue(rows.next());
      assertEquals(templateId, rows.getInt(1));
      assertEquals(0, rows.getInt(2));
      assertEquals(1, rows.getInt(3));
      assertEquals(1, rows.getInt(4));
      assertFalse(rows.next());
    }
    verifyNoInteractions(notifier);
  }

  @Test
  void failedWriteDoesNotPublishAnEvent() throws FormException {
    var record = record("en");
    database.when(DBUtil::openConnection).thenThrow(new SQLException("Unavailable database"));
    assertThrows(FormException.class, () -> manager.insertRecord(template, record));
    assertThrows(FormException.class, () -> manager.updateRecord(template, record));
    assertThrows(FormException.class, () -> manager.deleteRecord(record));
    assertThrows(FormException.class, () -> manager.removeRecordSet(TEMPLATE_ID));
    verifyNoInteractions(notifier);
  }

  @Test
  void deletingAbsentRecordAndReadingLanguagesDoNotNotify() throws Exception {
    manager.deleteRecord(record("en"));
    assertTrue(manager.getLanguagesOfRecord(template, "0").isEmpty());
    verifyNoInteractions(notifier);
  }

  private GenericDataRecord record(String language) throws FormException {
    var fields = new GenericRecordTemplate();
    fields.addFieldTemplate(new GenericFieldTemplate("description", TextFieldImpl.class));
    var record = new GenericDataRecord(fields);
    record.getField("description").setStringValue("Initial value");
    record.setId("0");
    record.setLanguage(language);
    return record;
  }

  private int countRecords() throws SQLException {
    try (var con = DriverManager.getConnection(URL); var sql = con.createStatement();
        var rows = sql.executeQuery("SELECT COUNT(*) FROM SB_FormTemplate_Record")) {
      assertTrue(rows.next());
      return rows.getInt(1);
    }
  }
}
