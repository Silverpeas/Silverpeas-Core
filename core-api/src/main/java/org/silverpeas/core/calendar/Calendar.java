/*
 * Copyright (C) 2000 - 2020 Silverpeas
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

import org.silverpeas.core.admin.component.model.SilverpeasComponentInstance;
import org.silverpeas.core.admin.component.model.SilverpeasPersonalComponentInstance;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.cache.model.SimpleCache;
import org.silverpeas.core.cache.service.CacheServiceProvider;
import org.silverpeas.core.calendar.notification.CalendarLifeCycleEventNotifier;
import org.silverpeas.core.calendar.repository.CalendarEventRepository;
import org.silverpeas.core.calendar.repository.CalendarRepository;
import org.silverpeas.core.importexport.ImportException;
import org.silverpeas.core.notification.system.ResourceEvent;
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.persistence.datasource.model.identifier.UuidIdentifier;
import org.silverpeas.core.persistence.datasource.model.jpa.SilverpeasJpaEntity;
import org.silverpeas.core.security.Securable;
import org.silverpeas.core.security.SecurableRequestCache;
import org.silverpeas.core.security.authorization.AccessControlContext;
import org.silverpeas.core.security.authorization.AccessControlOperation;
import org.silverpeas.core.security.authorization.ComponentAccessControl;
import org.silverpeas.core.security.token.exception.TokenException;
import org.silverpeas.core.security.token.exception.TokenRuntimeException;
import org.silverpeas.core.security.token.persistent.PersistentResourceToken;
import org.silverpeas.core.ui.DisplayI18NHelper;
import org.silverpeas.core.util.Mutable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.text.MessageFormat.format;
import static java.time.Month.DECEMBER;
import static org.silverpeas.core.admin.user.model.SilverpeasRole.admin;

/**
 * A calendar is a particular system for scheduling and organizing events and activities that occur
 * at different times or on different dates throughout the years.
 *
 * Before adding any events or activities into a calendar, it requires to be persisted into the
 * Silverpeas data source (use the {@code save} method for doing). Once saved, a collection of
 * planned events is then set up for this calendar and through which the events of the calendar
 * can be managed.
 * @author mmoquillon
 */
@Entity
@NamedQueries({
@NamedQuery(
    name = "calendarsByComponentInstanceIds",
    query = "from Calendar c where c.componentInstanceId in :componentInstanceIds " +
            "order by c.componentInstanceId, c.title, c.id"),
@NamedQuery(
    name = "synchronizedCalendars",
    query = "from Calendar c where c.externalUrl is not null"
)})
@Table(name = "sb_cal_calendar")
public class Calendar extends SilverpeasJpaEntity<Calendar, UuidIdentifier> implements Securable {

  /**
   * The reference to identify the main calendar of an instance
   */
  private static final String MAIN_TITLE_REF = "###main###";

  @Column(name = "instanceId", nullable = false)
  private String componentInstanceId;

  @Column(name = "title", nullable = false)
  private String title;

  @Column(name = "zoneId", nullable = false)
  private String zoneId;

  @Column(name = "externalUrl")
  private String externalUrl;

  @Column(name = "synchroDate")
  private OffsetDateTime synchronizationDate;

  /**
   * Necessary for JPA management.
   */
  protected Calendar() {
  }

  /**
   * Creates in the specified component instance a new calendar with the given title. The timezone
   * identifier of the calendar is set to the default zone id of the platform on which Silverpeas
   * runs.
   * @param instanceId the identifier identifying an instance of a component in Silverpeas.
   * Usually, this identifier is the identifier of the component instance to which it belongs
   * (for example almanach32) or the identifier of the user personal calendar.
   * @param title the title of the calendar.
   */
  public Calendar(String instanceId, String title) {
    this(instanceId, title, DisplayI18NHelper.getDefaultZoneId());
  }

  /**
   * Creates in the specified component instance a new calendar with the given title and for the
   * specified zone ID.
   * @param instanceId the identifier of an instance of a component in Silverpeas.
   * Usually, this identifier is the identifier of the component instance to which it belongs
   * (for example almanach32) or the identifier of the user personal calendar.
   * @param title the title of the calendar.
   * @param zoneId the identifier of a timezone.
   */
  public Calendar(String instanceId, String title, ZoneId zoneId) {
    this.componentInstanceId = instanceId;
    this.title = title;
    this.zoneId = zoneId.toString();
  }

  /**
   * Creates for the specified component instance a new main calendar with the given title. The
   * timezone identifier of the calendar is set to the default zone id of the platform on which
   * Silverpeas runs.
   * <p>A main calendar is a calendar that can not be deleted while the linked component instance is
   * existing. The title is also set automatically from the instance.</p>
   * @param instance an instance of a component in Silverpeas.
   * Usually, this identifier is the identifier of the component instance to which it belongs
   * (for example almanach32) or the identifier of the user personal calendar.
   * @return the initialized main calendar.
   */
  public static Calendar newMainCalendar(final SilverpeasComponentInstance instance) {
    return new Calendar(instance.getId(), MAIN_TITLE_REF);
  }

  /**
   * Gets a calendar by its identifier.
   * @param id the identifier of the aimed calendar.
   * @return the instance of the aimed calendar or null if it does not exist.
   */
  public static Calendar getById(final String id) {
    CalendarRepository calendarRepository = CalendarRepository.get();
    return calendarRepository.getById(id);
  }

  /**
   * Gets the calendars represented by the specified component instance.  For instance, the
   * component can be a collaborative application or a personal one.
   * @param instanceId the unique identifier identifying an instance of a Silverpeas
   * component.
   * @return a list containing the calendar instances which matched if any, empty list otherwise.
   */
  public static ComponentInstanceCalendars getByComponentInstanceId(String instanceId) {
    return ComponentInstanceCalendars.getByComponentInstanceId(instanceId);
  }

  /**
   * Gets the calendars represented by the specified component instances.  For instance, a
   * component can be a collaborative application or a personal one.
   * @param instanceIds the unique identifiers identifying instances of a Silverpeas
   * component.
   * @return a list containing the calendar instances which matched if any, empty list otherwise.
   */
  public static List<Calendar> getByComponentInstanceIds(final Collection<String> instanceIds) {
    final CalendarRepository calendarRepository = CalendarRepository.get();
    return calendarRepository.getByComponentInstanceIds(instanceIds);
  }

  /**
   * Gets a calendar window of time defined between the two specified dates and from which the
   * events occurring in the given period can be requested.
   * @param start the start date of the period.
   * @param end the end date of the period.
   * @return a window of time that includes all the calendar events occurring in its specified
   * period of time.
   */
  public static CalendarTimeWindow getTimeWindowBetween(final LocalDate start,
      final LocalDate end) {
    return new CalendarTimeWindow(start, end);
  }

  /**
   * Gets a calendar events instance which permits to get (as stream) events.<br>
   * @return a calendar events instance.
   */
  public static CalendarEvents getEvents() {
    return new CalendarEvents();
  }

  /**
   * Gets all the synchronized calendars in Silverpeas. A calendar is synchronized when it is
   * the counterpart of an external remote calendar and it is regularly updated from this external
   * calendar.
   *
   * @return a list of synchronized calendars.
   */
  public static List<Calendar> getSynchronizedCalendars() {
    return CalendarRepository.get().getAllSynchronized();
  }

  @Override
  protected void performBeforePersist() {
    super.performBeforePersist();
    SecurableRequestCache.clear(getId());
  }

  @Override
  protected void performBeforeUpdate() {
    super.performBeforeUpdate();
    SecurableRequestCache.clear(getId());
  }

  /**
   * Gets the identifier of the component instance which the calendar is attached.
   * @return the identifier of the component instance which the calendar is attached.
   */
  public String getComponentInstanceId() {
    return componentInstanceId;
  }

  /**
   * Gets the title of the calendar. If the calendar is the main one of a component instance, then
   * the title is taken from the component instance name.
   * @return the title of the calendar.
   */
  public String getTitle() {
    if (isMain()) {
      SimpleCache cache = CacheServiceProvider.getRequestCacheService().getCache();
      final String cacheKey = MAIN_TITLE_REF + getId();
      Mutable<String> mainTitle = Mutable.ofNullable(cache.get(cacheKey, String.class));
      if (!mainTitle.isPresent()) {
        SilverpeasComponentInstance.getById(getComponentInstanceId()).ifPresent(i -> {
          if (i.isPersonal()) {
            mainTitle.set(((SilverpeasPersonalComponentInstance) i).getUser().getDisplayedName());
          } else {
            mainTitle.set(i.getLabel());
          }
        });
        cache.put(cacheKey, mainTitle.get());
      }
      return mainTitle.get();
    }
    return this.title;
  }

  public void setTitle(String title) {
    if (!isMain()) {
      this.title = title;
    } else {
      throw new IllegalArgumentException(
          "not possible to set title on the main calendar of a component instance");
    }
  }

  /**
   * Gets the identifier of the location zone the calendar belongs to.
   * @return the zone id as {@link ZoneId} instance.
   */
  public ZoneId getZoneId() {
    return ZoneId.of(zoneId);
  }

  public void setZoneId(final ZoneId zoneId) {
    this.zoneId = zoneId.toString();
  }

  /**
   * Gets the URL of the external calendar with which this calendar is synchronized. This URL is
   * used in the synchronization to fetch the content of the external calendar.
   * @return either the URL of an external calendar or null if this calendar isn't synchronized with
   * such an external calendar.
   */
  public URL getExternalCalendarUrl() {
    try {
      return new URL(this.externalUrl);
    } catch (MalformedURLException e) {
      return null;
    }
  }

  /**
   * Sets the URL of an external calendar with which this calendar will be synchronized. The URL
   * will be used to get the content of the external calendar.
   * @param calendarUrl the URL of a calendar external to Silverpeas.
   */
  public void setExternalCalendarUrl(final URL calendarUrl) {
    this.externalUrl = calendarUrl.toString();
  }

  /**
   * Gets the last synchronization date of this calendar in the case this calendar is a synchronized
   * one. If it is a synchronized calendar and its last synchronization date is empty, this means
   * no synchronization was yet operated.
   * @return optionally the date of its last synchronization date.
   */
  public Optional<OffsetDateTime> getLastSynchronizationDate() {
    return Optional.ofNullable(this.synchronizationDate);
  }

  /**
   * Is this calendar synchronized with the events from an external calendar.
   * @return true if this calendar is synchronized with an external calendar.
   */
  public boolean isSynchronized() {
    return this.externalUrl != null;
  }

  /**
   * Sets the date time at which this calendar is lastly synchronized.
   * @param dateTime an {@link OffsetDateTime} value.
   */
  protected void setLastSynchronizationDate(final OffsetDateTime dateTime) {
    this.synchronizationDate = dateTime;
  }

  /**
   * Saves the calendar into the Silverpeas data source and set up for it a persistence collection
   * of planned event. Once saved, the calendar will be then ready to be used to plan events and
   * activities.
   */
  public void save() {
    if(!isPersisted() && isMain()) {
      getByComponentInstanceId(getComponentInstanceId()).forEach(c -> {
        if (c.isMain()) {
          throw new IllegalStateException(
              format("instance {0} has already a main calendar", getComponentInstanceId()));
        }
      });
    }
    Transaction.performInOne(() -> {
      CalendarRepository calendarRepository = CalendarRepository.get();
      calendarRepository.save(this);
      return null;
    });
  }

  /**
   * Deletes the calendar in the Silverpeas data source. By deleting it, the persistence collection
   * of planned events is then tear down, causing the deletion of all of the events planned in this
   * calendar.
   */
  public void delete() {
    Transaction.performInOne(() -> {
      PersistentResourceToken.removeToken(CalendarReference.fromCalendar(this));
      this.clear();
      CalendarRepository calendarRepository = CalendarRepository.get();
      calendarRepository.delete(this);
      // notify about the deletion
      notify(ResourceEvent.Type.DELETION, this);
      return null;
    });
  }

  /**
   * Gets a window of time on this calendar defined by the specified period. The window of time
   * will include only the events in this calendar that occur in the specified period.
   * @param year the year during which the events in this calendar occur.
   * @return the window of time including the events in this calendar occurring in the given period.
   */
  public CalendarTimeWindow in(final Year year) {
    return between(year.atDay(1), year.atMonth(DECEMBER).atEndOfMonth());
  }

  /**
   * Gets a window of time on this calendar defined by the specified period. The window of time
   * will include only the events in this calendar that occur in the specified period.
   * @param yearMonth the month and year during which the events in this calendar occur.
   * @return the window of time including the events in this calendar occurring in the given period.
   */
  public CalendarTimeWindow in(final YearMonth yearMonth) {
    return between(yearMonth.atDay(1), yearMonth.atEndOfMonth());
  }

  /**
   * Gets a window of time on this calendar defined by the specified period. The window of time
   * will include only the events in this calendar that occur in the specified period.
   * @param day day during which the events in this calendar occur.
   * @return the window of time including the events in this calendar occurring in the given period.
   */
  public CalendarTimeWindow in(final LocalDate day) {
    return between(day, day);
  }

  /**
   * Gets a window of time on this calendar defined by the specified period. The window of time
   * will include only the events in this calendar that occur in the specified period.
   * @param start the start date of the period.
   * @param end the end date of the period.
   * @return the window of time including the events in this calendar occurring in the given period.
   */
  public CalendarTimeWindow between(final LocalDate start, final LocalDate end) {
    verifyCalendarIsPersisted();
    return Calendar.getTimeWindowBetween(start, end).filter(f -> f.onCalendar(this));
  }

  private void verifyCalendarIsPersisted() {
    if (!isPersisted()) {
      throw new IllegalStateException(
          "The calendar isn't persisted and then no action is available");
    }
  }

  /**
   * Synchronizes this calendar. An {@link IllegalArgumentException} is thrown if this calendar
   * isn't a synchronized one.
   * <p>
   * The synchronization is a peculiar and regular import process to update the calendar
   * with its external counterpart in such a way this calendar is a mirror of the external one at a
   * given time.
   * </p>
   * @return the result of the synchronization with the number of events added, updated and
   * deleted in this calendar.
   * @throws ImportException if the synchronization fails.
   */
  public ICalendarImportResult synchronize() throws ImportException {
    return ICalendarEventSynchronization.get().synchronize(this);
  }

  /**
   * Gets either the calendar event with the specified identifier or nothing if no
   * such event exists with the given identifier.
   * @param eventId the unique identifier of the event to get.
   * @return optionally an event with the specified identifier.
   */
  public Optional<CalendarEvent> event(String eventId) {
    verifyCalendarIsPersisted();
    CalendarEventRepository repository = CalendarEventRepository.get();
    CalendarEvent event = repository.getById(eventId);
    if (event != null && !event.getCalendar().getId().equals(getId())) {
      event = null;
    }
    return Optional.ofNullable(event);
  }

  /**
   * Gets either the calendar external event with the specified external identifier or nothing if no
   * such event exists with the given external identifier. Such events come from either an external
   * calendar with which this calendar is synchronized or from an import of an ics file (iCalendar
   * document containing a collection of calendar component definitions).
   * @param externalEventId the unique external identifier of the event to get.
   * @return optionally an event with the specified identifier.
   */
  public Optional<CalendarEvent> externalEvent(String externalEventId) {
    verifyCalendarIsPersisted();
    CalendarEventRepository repository = CalendarEventRepository.get();
    CalendarEvent event = repository.getByExternalId(this, externalEventId);
    return Optional.ofNullable(event);
  }

  /**
   * Clears this calendar of all of the planned events.
   */
  public void clear() {
    verifyCalendarIsPersisted();
    Transaction.performInOne(() -> {
      CalendarEventRepository repository = CalendarEventRepository.get();
      repository.deleteAll(Calendar.this);
      return null;
    });
  }

  /**
   * Is this calendar empty of event?
   * @return true if there is no events planned in the calendar. Otherwise returns false.
   */
  public boolean isEmpty() {
    verifyCalendarIsPersisted();
    CalendarEventRepository repository = CalendarEventRepository.get();
    return repository.size(this) == 0;
  }

  /**
   * Is this calendar the main one of the linked component instance.
   * @return true if it is, false otherwise.
   */
  public boolean isMain() {
    return MAIN_TITLE_REF.equals(this.title);
  }

  /**
   * Is this calendar the personal one of the given user which is not modifiable?
   * @param user the user to verify.
   * @return true if it is, false otherwise.
   */
  public boolean isMainPersonalOf(final User user) {
    return isMain() && isPersonalOf(user);
  }

  /**
   * Is this calendar a personal one of the given user?
   * @param user the user to verify.
   * @return true if it is, false otherwise.
   */
  public boolean isPersonalOf(final User user) {
    Optional<SilverpeasPersonalComponentInstance> instance =
        SilverpeasPersonalComponentInstance.getById(getComponentInstanceId());
    return instance.isPresent() && instance.get().getUser().getId().equals(user.getId());
  }

  /**
   * Gets the token associated to the calendar.
   * @return the token as string.
   */
  public String getToken() {
    try {
      CalendarReference ref = CalendarReference.fromCalendar(this);
      return PersistentResourceToken.getOrCreateToken(ref).getValue();
    } catch (TokenException e) {
      throw new TokenRuntimeException(e.getMessage(), e);
    }
  }

  @Override
  public boolean canBeAccessedBy(final User user) {
    return SecurableRequestCache.canBeAccessedBy(user, getId(),
        u -> ComponentAccessControl.get().isUserAuthorized(u.getId(), getComponentInstanceId()));
  }

  @Override
  public boolean canBeModifiedBy(final User user) {
    return SecurableRequestCache.canBeModifiedBy(user, getId(), u -> {
      if (isMain()) {
        return false;
      }
      final ComponentAccessControl componentAccessControl = ComponentAccessControl.get();
      final Set<SilverpeasRole> roles = componentAccessControl
          .getUserRoles(u.getId(), getComponentInstanceId(),
              AccessControlContext.init().onOperationsOf(AccessControlOperation.modification));
      return componentAccessControl.isUserAuthorized(roles) &&
          Optional.ofNullable(SilverpeasRole.getHighestFrom(roles))
              .filter(r -> r.isGreaterThanOrEquals(admin)).isPresent();
    });
  }

  private void notify(ResourceEvent.Type type, Calendar... events) {
    CalendarLifeCycleEventNotifier notifier = CalendarLifeCycleEventNotifier.get();
    notifier.notifyEventOn(type, events);
  }
}
