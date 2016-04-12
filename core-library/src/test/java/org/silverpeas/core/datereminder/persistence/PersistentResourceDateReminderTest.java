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
package org.silverpeas.core.datereminder.persistence;

import org.junit.Rule;
import org.junit.Test;
import org.silverpeas.core.datereminder.exception.DateReminderException;
import org.silverpeas.core.test.rule.CommonAPI4Test;
import org.silverpeas.core.exception.SilverpeasException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * @author Cécile Bonin
 */
public class PersistentResourceDateReminderTest {

  @Rule
  public CommonAPI4Test commonAPI4Test = new CommonAPI4Test();

  @Test
  public void testValidate() {
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
    dateReminder.setResource(new MyEntityReferenceForUnitTest("42"));
    assertValidate(dateReminder, true);

    dateReminder = initializeDateReminder();
    dateReminder.setResource(new MyEntityReferenceForUnitTest(null));
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
    dateReminder.setResource(new MyEntityReferenceForUnitTest("26"));
    dateReminder.setDateReminder(dateReminderDetail);

    return dateReminder;
  }
}