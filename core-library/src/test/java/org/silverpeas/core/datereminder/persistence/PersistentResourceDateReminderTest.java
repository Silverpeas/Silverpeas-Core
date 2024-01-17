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
package org.silverpeas.core.datereminder.persistence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.admin.user.service.UserProvider;
import org.silverpeas.core.datereminder.exception.DateReminderException;
import org.silverpeas.core.exception.SilverpeasException;
import org.silverpeas.core.persistence.datasource.OperationContext;
import org.silverpeas.core.persistence.datasource.model.jpa.JpaPersistOperation;
import org.silverpeas.core.persistence.datasource.model.jpa.JpaUpdateOperation;
import org.silverpeas.core.test.extention.EnableSilverTestEnv;
import org.silverpeas.core.test.extention.TestManagedBeans;
import org.silverpeas.core.test.extention.TestManagedMock;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.when;

/**
 * @author Cécile Bonin
 */
@EnableSilverTestEnv
@TestManagedBeans({JpaPersistOperation.class, JpaUpdateOperation.class})
class PersistentResourceDateReminderTest {

  @BeforeEach
  public void prepareInjection(@TestManagedMock UserProvider userProvider) {
    when(userProvider.getUser(anyString())).thenAnswer(a -> {
      String id = a.getArgument(0);
      UserDetail user = new UserDetail();
      user.setId(id);
      return user;
    });
    OperationContext.fromUser("0");
  }

  @Test
  void testValidate() {
    PersistentResourceDateReminder dateReminder = initializeDateReminder();
    assertValidate(dateReminder, true);

    dateReminder = initializeDateReminder();
    dateReminder.setDateReminder(null);
    assertValidate(dateReminder, true);

    dateReminder = initializeDateReminder();
    DateReminderDetail dateReminderDetail =
        new DateReminderDetail(java.sql.Date.valueOf("2015-08-08"), "",
            DateReminderDetail.REMINDER_NOT_PROCESSED, "0", "0");
    dateReminder.setDateReminder(dateReminderDetail);
    assertValidate(dateReminder, true);

    dateReminder = initializeDateReminder();
    DateReminderDetail dateReminderDetail2 =
        new DateReminderDetail(java.sql.Date.valueOf("2015-08-08"), null,
            DateReminderDetail.REMINDER_NOT_PROCESSED, "0", "0");
    dateReminder.setDateReminder(dateReminderDetail2);
    assertValidate(dateReminder, true);

    dateReminder = initializeDateReminder();
    dateReminder.setResource(new MyUnitTestEntityReference("42"));
    assertValidate(dateReminder, true);

    dateReminder = initializeDateReminder();
    dateReminder.setResource(new MyUnitTestEntityReference(null));
    assertValidate(dateReminder, false);

    dateReminder = initializeDateReminder();
    dateReminder.setResource(null);
    assertValidate(dateReminder, true);
  }

  private <T extends SilverpeasException> void assertValidate(final PersistentResourceDateReminder dateReminder,
      final boolean isValid) {
    boolean isException = false;
    try {
      dateReminder.validate();
    } catch (final DateReminderException qe) {
      isException = true;
    }
    assertThat(isException, is(!isValid));
  }

  private PersistentResourceDateReminder initializeDateReminder() {

    DateReminderDetail dateReminderDetail =
        new DateReminderDetail(java.sql.Date.valueOf("2015-08-08"), "Modifier la publication",
            DateReminderDetail.REMINDER_NOT_PROCESSED, "0", "0");

    PersistentResourceDateReminder dateReminder = new PersistentResourceDateReminder();
    dateReminder.setResource(new MyUnitTestEntityReference("26"));
    dateReminder.setDateReminder(dateReminderDetail);

    return dateReminder;
  }
}