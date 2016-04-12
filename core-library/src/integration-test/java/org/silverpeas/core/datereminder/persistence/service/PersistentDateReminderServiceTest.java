/*
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
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package org.silverpeas.core.datereminder.persistence.service;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.datereminder.exception.DateReminderValidationException;
import org.silverpeas.core.datereminder.persistence.DateReminderDetail;
import org.silverpeas.core.datereminder.persistence.MyEntityReference;
import org.silverpeas.core.datereminder.persistence.MyUnknownEntityReference;
import org.silverpeas.core.datereminder.persistence.PersistentResourceDateReminder;
import org.silverpeas.core.persistence.EntityReference;
import org.silverpeas.core.test.WarBuilder4LibCore;
import org.silverpeas.core.test.rule.DbSetupRule;
import org.silverpeas.core.test.rule.MockByReflectionRule;

import javax.inject.Inject;
import java.util.Date;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * @author Cécile Bonin
 */
@RunWith(Arquillian.class)
public class PersistentDateReminderServiceTest {

  private static final String TABLES_CREATION = "create_dateReminder_table.sql";

  private static final String DATASET_CREATION = "dateReminder_dataset.sql";

  private final EntityReference dummyRef = new MyEntityReference("dummy");
  private final EntityReference existingRef = new MyEntityReference("38");
  private final EntityReference newRef = new MyEntityReference("26");

  @Rule
  public DbSetupRule dbSetupRule =
      DbSetupRule.createTablesFrom(TABLES_CREATION).loadInitialDataSetFrom(DATASET_CREATION);

  @Rule
  public MockByReflectionRule reflectionRule = new MockByReflectionRule();

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4LibCore.onWarForTestClass(PersistentDateReminderServiceTest.class)
        .addSilverpeasExceptionBases()
        .addJpaPersistenceFeatures()
        .addAdministrationFeatures()
        .testFocusedOn(
            (warBuilder) -> warBuilder.addPackages(true, "org.silverpeas.core.datereminder"))
        .build();
  }

  @Inject
  private PersistentDateReminderService dateReminderService;

  @Test(expected = NullPointerException.class)
  public void testGetNullDateReminder() {

    // Testing getting a null resource
    PersistentResourceDateReminder dateReminder = dateReminderService.get(null);
  }

  @Test
  public void testGetDummyDateReminder() {

    // Testing from a dummy key (doesn't exist in database)
    PersistentResourceDateReminder dateReminder = dateReminderService.get(dummyRef);
    assertThat(dateReminder, notNullValue());
    assertThat(dateReminder.exists(), is(false));
    assertThat(dateReminder.notExists(), is(true));
    assertThat(dateReminder.getResource(MyEntityReference.class), nullValue());
  }

  @Test
  public void testGetExistingDateReminder() {

    // Testing from an existing key
    PersistentResourceDateReminder dateReminder = dateReminderService.get(existingRef);
    final Date currentDate = new Date();
    assertThat(dateReminder, notNullValue());
    assertThat(dateReminder.exists(), is(true));
    assertThat(dateReminder.notExists(), is(false));
    assertThat(dateReminder.getResource(MyEntityReference.class), is(existingRef));
    assertThat(dateReminder.getResource(MyUnknownEntityReference.class), nullValue());
    assertThat(dateReminder.getDateReminder(), notNullValue());
    assertThat(dateReminder.getDateReminder().getDateReminder().getTime(), lessThan(currentDate.getTime()));
    assertThat(dateReminder.getDateReminder().getMessage(), is("Modifier le contenu de la publication"));

  }

  @Test
  public void testCreateFromNewKey() throws Exception {
    createNewDateReminder();
  }

  /**
   * Centralization create new Date Reminder
   */
  private DateReminderDetail createNewDateReminderDetail() {
    final Date currentDate = new Date();
    DateReminderDetail dateReminderDetail =
        new DateReminderDetail(currentDate, "Rappel", DateReminderDetail.REMINDER_NOT_PROCESSED,
            "0", "0");
    return dateReminderDetail;
  }

  /**
   * Centralization test
   */
  private void createNewDateReminder() throws Exception {

    // Verifying dateReminder before set
    PersistentResourceDateReminder dateReminder = dateReminderService.get(newRef);
    assertThat(dateReminder, notNullValue());
    assertThat(dateReminder.exists(), is(false));
    assertThat(dateReminder.notExists(), is(true));
    assertThat(dateReminder.getResource(MyEntityReference.class), nullValue());

    // create
    DateReminderDetail dateReminderDetail = createNewDateReminderDetail();
    dateReminder = dateReminderService.create(newRef, dateReminderDetail);

    // Verifying dateReminder after initializing
    dateReminder = dateReminderService.get(newRef);
    assertThat(dateReminder, notNullValue());
    assertThat(dateReminder.exists(), is(true));
    assertThat(dateReminder.notExists(), is(false));
    assertThat(dateReminder.getResource(MyEntityReference.class), is(newRef));
    assertThat(dateReminder.getResource(MyUnknownEntityReference.class), nullValue());
    assertThat(dateReminder.getDateReminder(), notNullValue());
    assertThat(dateReminder.getDateReminder().getDateReminder().getTime(), greaterThanOrEqualTo(
        dateReminderDetail.getDateReminder().getTime()));
    assertThat(dateReminder.getDateReminder().getMessage(), is("Rappel"));
  }

  @Test(expected = DateReminderValidationException.class)
  public void testCreateNullNull() throws Exception {
    dateReminderService.create(null, null);
  }

  @Test(expected = DateReminderValidationException.class)
  public void testCreateNullResource() throws Exception {
    DateReminderDetail dateReminderDetail = createNewDateReminderDetail();
    dateReminderService.create(null, dateReminderDetail);
  }

  @Test(expected = DateReminderValidationException.class)
  public void testCreateNullDateReminder() throws Exception {
    dateReminderService.create(newRef, null);
  }

  @Test(expected = DateReminderValidationException.class)
  public void testCreateNotValidResourceAndDateReminder() throws Exception {
    DateReminderDetail dateReminderDetail = createNewDateReminderDetail();
    dateReminderDetail.setDateReminder(null);
    dateReminderService.create(new MyEntityReference(null), dateReminderDetail);
  }

  @Test(expected = DateReminderValidationException.class)
  public void testCreateNotValidResource() throws Exception {
    DateReminderDetail dateReminderDetail = createNewDateReminderDetail();
    dateReminderService.create(new MyEntityReference(null), dateReminderDetail);
  }

  @Test(expected = DateReminderValidationException.class)
  public void testCreateNotValidDateReminder() throws Exception {
    DateReminderDetail dateReminderDetail = createNewDateReminderDetail();
    dateReminderDetail.setDateReminder(null);
    dateReminderService.create(newRef, dateReminderDetail);
  }

  @Test(expected = NullPointerException.class)
  public void testSetNullNull() throws Exception {
    dateReminderService.set(null, null);
  }

  @Test(expected = NullPointerException.class)
  public void testSetNullResource() throws Exception {
    DateReminderDetail dateReminderDetail = createNewDateReminderDetail();
    dateReminderService.set(null, dateReminderDetail);
  }

  @Test(expected = DateReminderValidationException.class)
  public void testSetNullDateReminder() throws Exception {
    dateReminderService.set(newRef, null);
  }

  @Test(expected = DateReminderValidationException.class)
  public void testSetNotValidResourceAndDateReminder() throws Exception {
    DateReminderDetail dateReminderDetail = createNewDateReminderDetail();
    dateReminderDetail.setDateReminder(null);
    dateReminderService.set(new MyEntityReference(null), dateReminderDetail);
  }

  @Test(expected = DateReminderValidationException.class)
  public void testSetNotValidResource() throws Exception {
    DateReminderDetail dateReminderDetail = createNewDateReminderDetail();
    dateReminderService.set(new MyEntityReference(null), dateReminderDetail);
  }

  @Test(expected = DateReminderValidationException.class)
  public void testSetNotValidDateReminder() throws Exception {
    DateReminderDetail dateReminderDetail = createNewDateReminderDetail();
    dateReminderDetail.setDateReminder(null);
    dateReminderService.set(newRef, dateReminderDetail);
  }

  @Test
  public void testSetFromExistingKey() throws Exception {

    // Verifying DateReminder before updating
    final Date currentDate = new Date();
    PersistentResourceDateReminder dateReminder = dateReminderService.get(existingRef);
    assertThat(dateReminder, notNullValue());
    assertThat(dateReminder.exists(), is(true));
    assertThat(dateReminder.notExists(), is(false));
    assertThat(dateReminder.getResource(MyEntityReference.class), is(existingRef));
    assertThat(dateReminder.getResource(MyUnknownEntityReference.class), nullValue());
    assertThat(dateReminder.getResource(MyEntityReference.class).getId(), is("38"));
    assertThat(dateReminder.getResource(MyEntityReference.class).getType(), is("STRING"));
    assertThat(dateReminder.getDateReminder(), notNullValue());
    assertThat(dateReminder.getDateReminder().getDateReminder().getTime(), lessThan(
        currentDate.getTime()));
    assertThat(dateReminder.getDateReminder().getMessage(), is("Modifier le contenu de la publication"));

    // Update
    DateReminderDetail dateReminderDetail = createNewDateReminderDetail();
    PersistentResourceDateReminder updatedDateReminder = dateReminderService.set(
        dateReminder.getResource(MyEntityReference.class), dateReminderDetail);

    // Verifying dateReminder after set
    assertThat(updatedDateReminder, notNullValue());
    assertThat(updatedDateReminder.exists(), is(true));
    assertThat(updatedDateReminder.notExists(), is(false));
    assertThat(updatedDateReminder.getResource(MyEntityReference.class), is(existingRef));
    assertThat(updatedDateReminder.getResource(MyUnknownEntityReference.class), nullValue());
    assertThat(updatedDateReminder.getDateReminder(), notNullValue());
    assertThat(updatedDateReminder.getDateReminder().getDateReminder().getTime(),
        is(dateReminderDetail.getDateReminder().getTime()));
    assertThat(updatedDateReminder.getDateReminder().getMessage(), is(
        "Rappel"));
  }

  @Test
  public void testSetProcessStatus() throws Exception {

    // Verifying DateReminder before updating
    final Date currentDate = new Date();
    PersistentResourceDateReminder dateReminder = dateReminderService.get(existingRef);
    assertThat(dateReminder, notNullValue());
    assertThat(dateReminder.exists(), is(true));
    assertThat(dateReminder.notExists(), is(false));
    assertThat(dateReminder.getResource(MyEntityReference.class), is(existingRef));
    assertThat(dateReminder.getResource(MyUnknownEntityReference.class), nullValue());
    assertThat(dateReminder.getResource(MyEntityReference.class).getId(), is("38"));
    assertThat(dateReminder.getResource(MyEntityReference.class).getType(), is("STRING"));
    assertThat(dateReminder.getDateReminder(), notNullValue());
    assertThat(dateReminder.getDateReminder().getDateReminder().getTime(),
        lessThan(currentDate.getTime()));
    assertThat(dateReminder.getDateReminder().getMessage(),
        is("Modifier le contenu de la publication"));
    assertThat(Integer.toString(dateReminder.getDateReminder().getProcessStatus()), is("0"));

    // Update
    DateReminderDetail dateReminderDetail = createNewDateReminderDetail();
    dateReminderDetail.setProcessStatus(DateReminderDetail.REMINDER_PROCESSED);
    PersistentResourceDateReminder updatedDateReminder = dateReminderService
        .set(dateReminder.getResource(MyEntityReference.class), dateReminderDetail);

    // Verifying dateReminder after set
    assertThat(updatedDateReminder, notNullValue());
    assertThat(updatedDateReminder.exists(), is(true));
    assertThat(updatedDateReminder.notExists(), is(false));
    assertThat(updatedDateReminder.getResource(MyEntityReference.class), is(existingRef));
    assertThat(updatedDateReminder.getResource(MyUnknownEntityReference.class), nullValue());
    assertThat(updatedDateReminder.getDateReminder(), notNullValue());
    assertThat(updatedDateReminder.getDateReminder().getDateReminder().getTime(),
        is(dateReminderDetail.getDateReminder().getTime()));
    assertThat(updatedDateReminder.getDateReminder().getMessage(), is("Rappel"));
    assertThat(Integer.toString(updatedDateReminder.getDateReminder().getProcessStatus()), is("1"));

  }

  @Test
  public void testRemoveFromExistingKey() throws Exception {

    // Verifying DateReminder before updating
    final Date currentDate = new Date();
    PersistentResourceDateReminder dateReminder = dateReminderService.get(existingRef);
    assertThat(dateReminder, notNullValue());
    assertThat(dateReminder.exists(), is(true));
    assertThat(dateReminder.notExists(), is(false));
    assertThat(dateReminder.getResource(MyEntityReference.class), is(existingRef));
    assertThat(dateReminder.getResource(MyUnknownEntityReference.class), nullValue());
    assertThat(dateReminder.getResource(MyEntityReference.class).getId(), is("38"));
    assertThat(dateReminder.getResource(MyEntityReference.class).getType(), is("STRING"));
    assertThat(dateReminder.getDateReminder(), notNullValue());
    assertThat(dateReminder.getDateReminder().getDateReminder().getTime(), lessThan(
        currentDate.getTime()));
    assertThat(dateReminder.getDateReminder().getMessage(), is("Modifier le contenu de la publication"));

    //Remove
    dateReminderService.remove(existingRef);

    //Verifying dateReminder after remove
    PersistentResourceDateReminder removedDateReminder = dateReminderService.get(existingRef);
    assertThat(removedDateReminder, notNullValue());
    assertThat(removedDateReminder.exists(), is(false));
    assertThat(removedDateReminder.notExists(), is(true));
  }
}