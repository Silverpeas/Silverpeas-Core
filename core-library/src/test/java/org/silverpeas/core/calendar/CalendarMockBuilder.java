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
 * FLOSS exception. You should have received a copy of the text describing
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

package org.silverpeas.core.calendar;

import java.time.ZoneId;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Yohann Chastagnier
 */
public class CalendarMockBuilder {

  private final Calendar calendar = mock(Calendar.class);

  public static CalendarMockBuilder from(String componentInstanceId) {
    CalendarMockBuilder builder = new CalendarMockBuilder();
    builder.withComponentInstanceId(componentInstanceId);
    return builder;
  }

  public CalendarMockBuilder withId(String id) {
    when(calendar.getId()).thenReturn(id);
    return this;
  }

  public CalendarMockBuilder atZoneId(final ZoneId zoneId) {
    when(calendar.getZoneId()).thenReturn(zoneId);
    return this;
  }

  public CalendarMockBuilder withTitle(String title) {
    when(calendar.getTitle()).thenReturn(title);
    return this;
  }

  public Calendar build() {
    return calendar;
  }

  private void withComponentInstanceId(final String componentInstanceId) {
    when(calendar.getComponentInstanceId()).thenReturn(componentInstanceId);
  }
}
