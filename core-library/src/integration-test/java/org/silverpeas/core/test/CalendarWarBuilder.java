/*
 * Copyright (C) 2000 - 2018 Silverpeas
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
package org.silverpeas.core.test;

import org.silverpeas.core.calendar.CalendarEventOccurrenceBuilder;
import org.silverpeas.core.calendar.ical4j.HtmlProperty;
import org.silverpeas.core.calendar.notification.CalendarContributionReminderUserNotification;
import org.silverpeas.core.test.stub.StubbedWysiwygContentRepository;

/**
 * @author Yohann Chastagnier
 */
public class CalendarWarBuilder extends WarBuilder4LibCore {

  /**
   * Constructs a war builder for the specified test class. It will load all the resources in the
   * same packages of the specified test class.
   * @param test the class of the test for which a war archive will be build.
   */
  private <T> CalendarWarBuilder(final Class<T> test) {
    super(test);
    addProcessFeatures();
    addAdministrationFeatures();
    addSilverpeasExceptionBases();
    addJpaPersistenceFeatures();
    addNotificationFeatures();
    addPublicationTemplateFeatures();
    addPackages(true, "org.silverpeas.core.notification.user.delayed.model");
    addClasses(CalendarEventOccurrenceBuilder.class, StubbedWysiwygContentRepository.class,
        HtmlProperty.class);
    addClasses(CalendarContributionReminderUserNotification.class);
  }

  /**
   * Constructs an instance of the calendar war archive builder for the specified test class.
   * @param test the test class for which a war will be built. Any resources located in the same
   * package of the test will be loaded into the war.
   * @param <T> the type of the test.
   * @return a calendar builder of the war archive.
   */
  public static <T> CalendarWarBuilder onWarForTestClass(Class<T> test) {
    return new CalendarWarBuilder(test);
  }

}
