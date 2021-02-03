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
package org.silverpeas.web.usercalendar.socialnetwork;

import org.silverpeas.core.admin.component.model.PersonalComponent;
import org.silverpeas.core.admin.component.model.PersonalComponentInstance;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.annotation.Provider;
import org.silverpeas.core.calendar.Calendar;
import org.silverpeas.core.calendar.CalendarEventOccurrence;
import org.silverpeas.core.calendar.VisibilityLevel;
import org.silverpeas.core.socialnetwork.model.SocialInformation;
import org.silverpeas.core.socialnetwork.model.SocialInformationType;
import org.silverpeas.core.socialnetwork.provider.SocialEventProvider;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.web.calendar.socialnetwork.CalendarEventOccurrenceSocialInformation;
import org.silverpeas.core.webapi.calendar.CalendarResourceURIs;
import org.silverpeas.core.webapi.calendar.CalendarWebManager;
import org.silverpeas.web.usercalendar.UserCalendarSettings;

import javax.annotation.Priority;
import javax.enterprise.inject.Alternative;
import javax.inject.Inject;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.time.OffsetDateTime.ofInstant;
import static java.time.ZoneId.systemDefault;
import static java.util.Collections.singleton;
import static java.util.stream.Stream.empty;
import static javax.interceptor.Interceptor.Priority.APPLICATION;
import static org.silverpeas.core.admin.component.model.PersonalComponentInstance.from;
import static org.silverpeas.core.util.StringUtil.isDefined;

/**
 * For now, this implementation is alternative to
 * {@link org.silverpeas.core.personalorganizer.socialnetwork.SocialEvent} one.
 * <p>
 * Indeed, {@link org.silverpeas.core.personalorganizer.socialnetwork.SocialEvent} implementation
 * will be soon deleted. But, while waiting for that to happen, it is kept intact.
 * </p>
 * @author silveryocha
 */
@Provider
@Alternative
@Priority(APPLICATION + 10)
public class DefaultUserCalendarEventOccurrenceSocialProvider implements SocialEventProvider {

  private static final String USER_CALENDAR_COMPONENT_NAME = UserCalendarSettings.COMPONENT_NAME;
  private static final String NEXT_EVENT_TYPE = SocialInformationType.EVENT.toString();
  private static final String LAST_EVENT_TYPE = SocialInformationType.LASTEVENT.toString();

  private final Comparator<SocialInformation> comparatorByDateAsc = Comparator
      .comparing(SocialInformation::getDate);

  private final Comparator<SocialInformation> comparatorByDateDesc = comparatorByDateAsc.reversed();

  @Inject
  private CalendarResourceURIs uri;

  protected DefaultUserCalendarEventOccurrenceSocialProvider() {
  }

  @Override
  public List<SocialInformation> getSocialInformationsList(String userId, String classification,
      Date begin, Date end) {
    return streamData(userId, singleton(userId), classification, begin, end, true)
        .collect(Collectors.toList());
  }

  @Override
  public List<SocialInformation> getSocialInformationList(final String userId, final Date begin,
      final Date end) {
    return getSocialInformationsList(userId, StringUtil.EMPTY, begin, end);
  }

  @Override
  public List<SocialInformation> getSocialInformationListOfMyContacts(String userId,
      List<String> myContactsIds, Date begin, Date end) {
    return streamData(userId, myContactsIds, StringUtil.EMPTY, begin, end, true)
        .collect(Collectors.toList());
  }

  @Override
  public List<SocialInformation> getLastSocialInformationsListOfMyContacts(String userId,
      List<String> myContactsIds, Date begin, Date end) {
    return streamData(userId, myContactsIds, StringUtil.EMPTY, begin, end, false)
        .collect(Collectors.toList());
  }

  @Override
  public List<SocialInformation> getMyLastSocialInformationsList(String userId, Date begin,
      Date end) {
    return streamData(userId, singleton(userId), StringUtil.EMPTY, begin, end, false)
        .collect(Collectors.toList());
  }

  /**
   * Centralization of the getting of data.
   * @param requesterId the identifier of the user processing the request.
   * @param aimedUserIds the user identifiers for which the occurrences MUST be be searched for.
   * @param visibilityFilter an optional visibility filter.
   * @param begin the start date of the time window to request.
   * @param end the end date of the time window to request.
   * @param getNext true means that the next occurrences from now and into the time window are
   * requested, otherwise it the last occurrences into the time windows needed.
   * @return a stream of {@link SocialInformation}.
   */
  private Stream<CalendarEventOccurrenceSocialInformation> streamData(String requesterId,
      final Collection<String> aimedUserIds, String visibilityFilter, Date begin, Date end,
      boolean getNext) {
    final Optional<PersonalComponent> userCalendarComponent = PersonalComponent
        .getByName(USER_CALENDAR_COMPONENT_NAME);
    if (!userCalendarComponent.isPresent()) {
      return empty();
    }
    final CalendarWebManager calendarWebManager = getCalendarWebManager();
    final User requester = User.getById(requesterId);
    final ZoneId userZoneId = requester.getUserPreferences().getZoneId();
    final Date legacyNow = getLegacyNow();
    final OffsetDateTime now = ofInstant(legacyNow.toInstant(), systemDefault());
    final LocalDate startDate = toUserLocalDate(begin, userZoneId);
    final LocalDate endDate = toUserLocalDate(end, userZoneId).plusDays(1);
    final List<PersonalComponentInstance> componentIds = aimedUserIds.stream()
        .map(User::getById)
        .filter(Objects::nonNull)
        .map(u -> from(u, userCalendarComponent.get()))
        .collect(Collectors.toList());
    if (componentIds.isEmpty()) {
      return empty();
    }
    Stream<CalendarEventOccurrenceSocialInformation> streamResult = componentIds.stream()
        .flatMap(i -> {
          final List<Calendar> userCalendars = calendarWebManager.getCalendarsHandledBy(i.getId());
          if (userCalendars.isEmpty()) {
            return empty();
          }
          final User userOwner = i.getUser();
          final List<CalendarEventOccurrence> occurrences = calendarWebManager
              .getEventOccurrencesOf(startDate, endDate, userCalendars, userOwner);
          return asSocialInformationList(occurrences, now, requesterId, userZoneId, userOwner.getId());
        });
    if (getNext) {
      streamResult = streamResult.filter(i -> NEXT_EVENT_TYPE.equals(i.getType()));
    } else {
      streamResult = streamResult.filter(i -> LAST_EVENT_TYPE.equals(i.getType()));
    }
    if (isDefined(visibilityFilter)) {
      streamResult = streamResult.filter(onVisibility(visibilityFilter));
    }
    if (aimedUserIds.stream().anyMatch(i -> !String.valueOf(i).equals(requesterId))) {
      // Updated value is hacked. Not super good, but for now, no refactoring at this level.
      // Here, if true (default value) this data means that the calendar occurrence is a personal
      // one of requester.
      final String componentIdOfRequester = from(requester, userCalendarComponent.orElse(null)).getId();
      streamResult = streamResult.map(i -> {
        final boolean isOneOfRequester = i.getOccurrence().getCalendarEvent().getCalendar()
            .getComponentInstanceId().equals(componentIdOfRequester);
        i.setUpdated(isOneOfRequester);
        return i;
      });
    }
    if (getNext) {
      streamResult = streamResult.sorted(comparatorByDateAsc);
    } else {
      streamResult = streamResult.sorted(comparatorByDateDesc);
    }
    return streamResult.map(i -> {
      if (!i.getIcon().contains("private")) {
        i.setUrl(uri.ofOccurrencePermalink(i.getOccurrence()).toString());
      }
      return i;
    });
  }

  private LocalDate toUserLocalDate(final Date begin, final ZoneId userZoneId) {
    return ofInstant(begin.toInstant(), systemDefault()).atZoneSameInstant(userZoneId).toLocalDate();
  }

  protected Date getLegacyNow() {
    return DateUtil.getNow();
  }

  private Predicate<CalendarEventOccurrenceSocialInformation> onVisibility(final String visibility) {
    final VisibilityLevel visibilityLevel = VisibilityLevel.valueOf(visibility.toUpperCase());
    return o -> o.getOccurrence().getVisibilityLevel() == visibilityLevel;
  }

  private Stream<CalendarEventOccurrenceSocialInformation> asSocialInformationList(
      final Collection<CalendarEventOccurrence> occurrences, final OffsetDateTime now,
      final String requesterId, final ZoneId userZoneId, final String userOwnerId) {
    return occurrences.stream()
        .map(o -> asSocialInformation(o, now, requesterId, userZoneId, userOwnerId));
  }

  private CalendarEventOccurrenceSocialInformation asSocialInformation(
      final CalendarEventOccurrence occurrence, final OffsetDateTime now, final String requesterId,
      final ZoneId userZoneId, final String userOwnerId) {
    return new CalendarEventOccurrenceSocialInformation(occurrence, now, requesterId, userZoneId,
        userOwnerId);
  }

  private CalendarWebManager getCalendarWebManager() {
    return CalendarWebManager.get(USER_CALENDAR_COMPONENT_NAME);
  }
}
