/*
 * Copyright (C) 2000 - 2021 Silverpeas
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

package org.silverpeas.core.calendar;

import org.silverpeas.core.contribution.model.Contribution;
import org.silverpeas.core.util.ContributionPath;

import java.util.Optional;

import static org.silverpeas.core.util.StringUtil.EMPTY;

/**
 * Path of a {@link CalendarComponent} instance.
 * @author silveryocha
 */
public class CalendarComponentPath extends ContributionPath<Contribution> {
  private static final long serialVersionUID = -8183907269010658005L;

  private CalendarComponentPath(final Contribution contribution) {
    super(1);
    this.add(contribution);
  }

  /**
   * Gets a calendar component path without taking care about right accesses.
   * @param event a {@link CalendarEvent} instance.
   * @return an initialized {@link CalendarComponentPath}.
   */
  public static CalendarComponentPath getPath(final CalendarEvent event) {
    return new CalendarComponentPath(event);
  }

  /**
   * Gets a calendar component path without taking care about right accesses.
   * @param occurrence a {@link CalendarEventOccurrence} instance.
   * @return an initialized {@link CalendarComponentPath}.
   */
  public static CalendarComponentPath getPath(final CalendarEventOccurrence occurrence) {
    return new CalendarComponentPath(occurrence);
  }

  @Override
  protected boolean isRoot(final Contribution contribution) {
    return false;
  }

  @Override
  protected String getLabel(final Contribution contribution, final String language) {
    return contribution.getTitle();
  }

  @Override
  public String format(final String language, final boolean fullSpacePath, final String pathSep) {
    return Optional.ofNullable(get(0))
        .map(this::asCalendarComponent)
        .map(CalendarComponent::getCalendar)
        .map(CalendarPath::getPath)
        .map(p -> p.format(language, fullSpacePath, pathSep) + pathSep +
            super.format(language, fullSpacePath, pathSep))
        .orElse(EMPTY);
  }

  private CalendarComponent asCalendarComponent(Contribution contribution) {
    if (contribution instanceof CalendarEvent) {
      return ((CalendarEvent) contribution).asCalendarComponent();
    } else if (contribution instanceof CalendarEventOccurrence) {
      return ((CalendarEventOccurrence) contribution).asCalendarComponent();
    }
    return null;
  }
}
