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

package org.silverpeas.core.calendar.notification.user;

import org.silverpeas.core.calendar.Calendar;
import org.silverpeas.core.calendar.ICalendarEventSynchronization.CalendarBatchSynchronizationErrorEvent;
import org.silverpeas.core.notification.user.SimpleUserNotification;
import org.silverpeas.core.util.Link;
import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.Pair;
import org.silverpeas.core.util.ResourceLocator;

import javax.enterprise.event.Observes;

import static org.silverpeas.core.util.URLUtil.getComponentInstanceURL;

/**
 * Listener of {@link CalendarBatchSynchronizationErrorEvent} events.
 * @author silveryocha
 */
public class CalendarBatchSynchronizationErrorUserNotification {

  private static final String TITLE_KEY = "calendar.notification.synchronization.error.title";
  private static final String LINK_LABEL_KEY = "calendar.link.label";
  private static final Pair<String, String> TEMPLATE_PATH = Pair.of("calendar", "calendarSynchronizationError");

  public void onEvent(@Observes final CalendarBatchSynchronizationErrorEvent event) {
    final Calendar calendar = event.getCalendar();
    SimpleUserNotification.fromSystem()
        .andComponentInstanceId(calendar.getComponentInstanceId())
        .toUsers(calendar.getCreator())
        .withTitle(l -> bundle(l).getStringWithParams(TITLE_KEY, calendar.getTitle()))
        .fillTemplate(TEMPLATE_PATH, (t, l) -> t.setAttribute("calendarTitle", calendar.getTitle()))
        .withLink(l -> new Link(getComponentInstanceURL(calendar.getComponentInstanceId()) + "Main",
                                bundle(l).getString(LINK_LABEL_KEY)))
        .send();
  }

  private LocalizationBundle bundle(final String locale) {
    return ResourceLocator.getLocalizationBundle("org.silverpeas.calendar.multilang.usernotification", locale);
  }
}
