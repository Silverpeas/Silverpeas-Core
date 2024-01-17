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
package org.silverpeas.core.web.calendar.socialnetwork;

import org.silverpeas.core.calendar.CalendarEventOccurrence;
import org.silverpeas.core.calendar.VisibilityLevel;
import org.silverpeas.core.date.Period;
import org.silverpeas.core.socialnetwork.model.AbstractSocialInformation;
import org.silverpeas.core.socialnetwork.model.SocialInformation;
import org.silverpeas.core.socialnetwork.model.SocialInformationType;
import org.silverpeas.core.util.StringUtil;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Objects;

import static java.time.ZoneOffset.UTC;
import static org.silverpeas.core.date.TemporalConverter.asLocalDate;
import static org.silverpeas.core.date.TemporalConverter.asOffsetDateTime;

/**
 * @author silveryocha
 */
public class CalendarEventOccurrenceSocialInformation extends AbstractSocialInformation {

  private final String requesterId;
  private final ZoneId userZoneId;
  private final String userOwnerId;
  private final Period period;
  private final CalendarEventOccurrence occurrence;

  /**
   * Creates a {@link SocialInformation} instance corresponding to a
   * {@link CalendarEventOccurrence} one.
   * @param occurrence an occurrence.
   * @param now the now temporal (date and time).
   * @param requesterId the identifier of the requester.
   * @param userZoneId a user zone id in order to handle properly dates according to user timezone.
   * @param userOwnerId the identifier of the user the social information belong to.
   */
  public CalendarEventOccurrenceSocialInformation(CalendarEventOccurrence occurrence,
      final OffsetDateTime now, final String requesterId, final ZoneId userZoneId,
      final String userOwnerId) {
    super(occurrence.getIdentifier().toReference());
    this.requesterId = requesterId;
    this.userZoneId = userZoneId;
    this.userOwnerId = userOwnerId;
    this.occurrence = occurrence;
    this.period = occurrence.getPeriod();
    if (period.endsAfter(now.atZoneSameInstant(UTC).toLocalDate().atStartOfDay(UTC))) {
      setType(SocialInformationType.EVENT.toString());
    } else {
      setType(SocialInformationType.LASTEVENT.toString());
    }
    setUpdated(true);
    setUrl(StringUtil.EMPTY);
  }

  @Override
  public String getIcon() {
    // Indeed, isUpdated means here isTheEventOneOfCurrentUser
    if (!isUpdated()
        && VisibilityLevel.PRIVATE == occurrence.getVisibilityLevel()
        && occurrence.getAttendees().stream().noneMatch(a -> a.getId().equals(requesterId))) {
      return getType() + "_private.gif";
    }
    return getType() + "_public.gif";
  }

  @Override
  public String getTitle() {
    return occurrence.getTitle();
  }

  @Override
  public String getDescription() {
    return occurrence.getDescription();
  }

  @Override
  public String getAuthor() {
    return userOwnerId;
  }

  @Override
  public Date getDate() {
    Date startDate = super.getDate();
    if (startDate == null) {
      final Instant instant = period.isInDays()
          ? asLocalDate(period.getStartDate()).atStartOfDay(userZoneId).toInstant()
          : asOffsetDateTime(period.getStartDate()).atZoneSameInstant(userZoneId).toInstant();
      startDate = Date.from(instant);
      setDate(startDate);
    }
    return startDate;
  }

  public CalendarEventOccurrence getOccurrence() {
    return occurrence;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    final CalendarEventOccurrenceSocialInformation that =
        (CalendarEventOccurrenceSocialInformation) o;
    return Objects.equals(userOwnerId, that.userOwnerId) &&
        Objects.equals(occurrence, that.occurrence);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), userOwnerId, occurrence);
  }
}
