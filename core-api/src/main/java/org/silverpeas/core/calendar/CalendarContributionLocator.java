/*
 * Copyright (C) 2000 - 2022 Silverpeas
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

import org.silverpeas.core.contribution.ContributionLocatorByLocalIdAndType;
import org.silverpeas.core.contribution.model.Contribution;
import org.silverpeas.core.contribution.model.ContributionIdentifier;

import javax.inject.Named;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * This is an implementation of {@link ContributionLocatorByLocalIdAndType} which is able to locate
 * {@link Contribution} of following types:
 * <ul>
 * <li>{@link CalendarEvent}</li>
 * <li>{@link CalendarEventOccurrence}</li>
 * </ul>
 * @author silveryocha
 */
@Named
public class CalendarContributionLocator implements ContributionLocatorByLocalIdAndType {

  private static final List<String> HANDLED_TYPES =
      Arrays.asList(CalendarEvent.TYPE, CalendarEventOccurrence.TYPE);

  @Override
  public boolean isContributionLocatorOfType(final String type) {
    return HANDLED_TYPES.contains(type);
  }

  @Override
  public Optional<ContributionIdentifier> getContributionIdentifierFromLocalIdAndType(
      final String localId, final String type) {
    ContributionIdentifier contributionIdentifier = null;
    if (CalendarEvent.TYPE.equals(type)) {
      final CalendarEvent event = CalendarEvent.getById(localId);
      if (event != null) {
        contributionIdentifier = event.getIdentifier();
      }
    } else {
      final CalendarEventOccurrence occurrence =
          CalendarEventOccurrence.getById(localId).orElse(null);
      if (occurrence != null) {
        contributionIdentifier = occurrence.getIdentifier();
      }
    }
    return Optional.ofNullable(contributionIdentifier);
  }
}
