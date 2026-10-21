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
package org.silverpeas.core.contribution.content.form.notification;

import jakarta.transaction.Status;
import jakarta.transaction.UserTransaction;
import javax.naming.InitialContext;
import java.util.Set;
import java.util.stream.Collectors;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.contribution.content.form.FormException;
import org.silverpeas.core.contribution.content.form.field.TextFieldImpl;
import org.silverpeas.core.contribution.content.form.record.GenericFieldTemplate;
import org.silverpeas.core.contribution.content.form.record.GenericDataRecord;
import org.silverpeas.core.contribution.content.form.record.GenericRecordSetManager;
import org.silverpeas.core.contribution.content.form.record.GenericRecordTemplate;
import org.silverpeas.core.contribution.content.form.record.IdentifiedRecordTemplate;
import org.silverpeas.core.notification.system.ResourceEvent;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.security.encryption.cipher.CryptoException;
import org.silverpeas.core.security.encryption.ContentEncryptionService;
import org.silverpeas.core.security.encryption.ContentEncryptionServiceProvider;
import org.silverpeas.core.security.encryption.EncryptionContentIterator;
import org.silverpeas.core.test.LibCoreWarBuilder;
import org.silverpeas.core.test.integration.rule.DbSetupRule;
import org.silverpeas.core.util.ServiceProvider;

import static org.junit.Assert.*;

/** Verifies SQL persistence and AFTER_SUCCESS notification in a real JTA transaction. */
@RunWith(Arquillian.class)
public class FormRecordNotificationIT {

  private static final String TEMPLATE_ID = "webPages42:example";

  @Rule
  public DbSetupRule database = DbSetupRule.createTablesFrom(
      "/org/silverpeas/core/contribution/content/form/notification/create-database.sql");

  private GenericRecordSetManager manager;
  private IdentifiedRecordTemplate template;
  private CommittedFormRecordListener listener;

  @Deployment
  public static Archive<?> deployment() {
    return LibCoreWarBuilder.onWarForTestClass(FormRecordNotificationIT.class)
        .addI18n()
        .addClasses(CryptoException.class, ContentEncryptionService.class,
            ContentEncryptionServiceProvider.class, EncryptionContentIterator.class)
        .addPackages(true, "org.silverpeas.core.contribution.content.form",
            "org.silverpeas.core.contribution.template.publication")
        .build();
  }

  @Before
  public void prepare() {
    manager = GenericRecordSetManager.getInstance();
    template = new IdentifiedRecordTemplate(new GenericRecordTemplate());
    template.setExternalId(TEMPLATE_ID);
    template.setInternalId(1);
    listener = ServiceProvider.getService(CommittedFormRecordListener.class);
    listener.reset();
  }

  @Test
  public void creationIsCommittedBeforeDeliveryWithoutAnExistingTransaction() throws Exception {
    manager.insertRecord(template, record());
    assertEquals(1, countRecords());
    assertEquals(1, listener.events().size());
    assertEquals(ResourceEvent.Type.CREATION, listener.events().getFirst().getType());
    assertEquals(TEMPLATE_ID,
        listener.events().getFirst().getTransition().getAfter().templateId());
  }

  @Test
  public void explicitCommitDeliversAllChangesOnlyOnceAfterCommit() throws Exception {
    UserTransaction transaction = transaction();
    transaction.begin();
    try {
      var record = record();
      manager.insertRecord(template, record);
      manager.updateRecord(template, record);
      manager.deleteRecord(record);
      assertTrue(listener.events().isEmpty());
      transaction.commit();
    } finally {
      rollbackIfActive(transaction);
    }
    assertEquals(3, listener.events().size());
    assertEquals(Set.of(ResourceEvent.Type.CREATION, ResourceEvent.Type.UPDATE,
            ResourceEvent.Type.DELETION),
        listener.events().stream().map(FormRecordEvent::getType).collect(Collectors.toSet()));
    assertEquals(0, countRecords());
  }

  @Test
  public void rolledBackCreationDoesNotPersistOrNotify() throws Exception {
    UserTransaction transaction = transaction();
    transaction.begin();
    try {
      manager.insertRecord(template, record());
      assertTrue(listener.events().isEmpty());
    } finally {
      transaction.rollback();
    }
    assertTrue(listener.events().isEmpty());
    assertEquals(0, countRecords());
  }

  @Test
  public void rolledBackDeletionPreservesTheRecordAndDoesNotNotify() throws Exception {
    var record = record();
    manager.insertRecord(template, record);
    listener.reset();
    UserTransaction transaction = transaction();
    transaction.begin();
    try {
      manager.updateRecord(template, record);
      manager.deleteRecord(record);
      assertTrue(listener.events().isEmpty());
    } finally {
      transaction.rollback();
    }
    assertTrue(listener.events().isEmpty());
    assertEquals(1, countRecords());
  }

  @Test
  public void failedFieldInsertRollsBackTheRecordHeaderAndDoesNotNotify() throws Exception {
    var record = record();
    record.getField("description").setStringValue("FAIL");
    try {
      manager.insertRecord(template, record);
      fail("The field constraint should reject the insertion");
    } catch (FormException expected) {
      assertTrue(listener.events().isEmpty());
      assertEquals(0, countRecords());
    }
  }

  private GenericDataRecord record() throws FormException {
    var fields = new GenericRecordTemplate();
    fields.addFieldTemplate(new GenericFieldTemplate("description", TextFieldImpl.class));
    var record = new GenericDataRecord(fields);
    record.getField("description").setStringValue("Initial value");
    record.setId("0");
    record.setLanguage("en");
    return record;
  }

  private int countRecords() throws Exception {
    try (var con = DBUtil.openConnection(); var sql = con.createStatement();
        var rows = sql.executeQuery("SELECT COUNT(*) FROM SB_FormTemplate_Record")) {
      assertTrue(rows.next());
      return rows.getInt(1);
    }
  }

  private UserTransaction transaction() throws Exception {
    return InitialContext.doLookup("java:comp/UserTransaction");
  }

  private void rollbackIfActive(UserTransaction transaction) throws Exception {
    if (transaction.getStatus() == Status.STATUS_ACTIVE) {
      transaction.rollback();
    }
  }
}
