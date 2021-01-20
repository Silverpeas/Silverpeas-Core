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
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.calendar.notification.CalendarEventLifeCycleEventNotifier;
import org.silverpeas.core.calendar.notification.CalendarEventOccurrenceLifeCycleEventNotifier;
import org.silverpeas.core.calendar.notification.LifeCycleEventSubType;
import org.silverpeas.core.calendar.repository.CalendarEventOccurrenceRepository;
import org.silverpeas.core.calendar.repository.CalendarEventRepository;
import org.silverpeas.core.contribution.model.Contribution;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.contribution.model.ContributionModel;
import org.silverpeas.core.contribution.model.LocalizedContribution;
import org.silverpeas.core.contribution.model.WithAttachment;
import org.silverpeas.core.contribution.model.WysiwygContent;
import org.silverpeas.core.date.Period;
import org.silverpeas.core.date.TemporalConverter;
import org.silverpeas.core.notification.system.ResourceEvent;
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.persistence.datasource.OperationContext;
import org.silverpeas.core.persistence.datasource.model.identifier.UuidIdentifier;
import org.silverpeas.core.persistence.datasource.model.jpa.BasicJpaEntity;
import org.silverpeas.core.reminder.WithReminder;
import org.silverpeas.core.security.Securable;
import org.silverpeas.core.security.SecurableRequestCache;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static org.silverpeas.core.calendar.CalendarComponentDiffDescriptor.diffBetween;
import static org.silverpeas.core.calendar.VisibilityLevel.PUBLIC;
import static org.silverpeas.core.persistence.datasource.OperationContext.State.IMPORT;

/**
 * An event planned in a calendar.
 *
 * An event in a calendar is a possibly {@link Recurrent} and a {@link Plannable} general business
 * component that can be planned on one and only one given existing {@link Calendar};
 * we ensures an event is unique in a per-calendar basis.
 * It occurs on a {@link Period} and as a such it must be well limited in the time (id est it must
 * have a start and an end dates/datetimes).
 * It can also be {@link Prioritized}, {@link Categorized}, and it can have some {@link Attendee}s.
 * In order to be customized for different kinds of use, some additional information can be set
 * through its {@link AttributeSet} property.
 *
 * An event in a calendar in Silverpeas can be originated from an external calendar. This comes
 * from an export process of the events planned in an external calendar (for example from a calendar
 * in Google Calendar). When such an event is in a Silverpeas's calendar, then the identifier of
 * this event on the external calendar can be get with the {@link CalendarEvent#getExternalId()}
 * method.
 *
 * When a list of events is retrieved from one or more calendars, they are all ordered by the
 * component instance that owns the calendar, then by the calendar on which they are planned, then
 * by the user who authored them, and finally by their starting  date in the timeline of the
 * calendar.
 */
@Entity
@Table(name = "sb_cal_event")
@NamedQueries({
    @NamedQuery(name = "calendarEventCount", query =
        "SELECT COUNT(e) FROM CalendarEvent e WHERE e.component.calendar = :calendar"),
    @NamedQuery(name = "calendarEvents", query =
        "SELECT distinct e, c.componentInstanceId as ob_1, c.id as ob_2, cmp.period.startDateTime as ob_3 " +
            "FROM CalendarEvent e " +
            "JOIN e.component cmp " +
            "JOIN cmp.calendar c " +
            "ORDER BY ob_1, ob_2, ob_3"),
    @NamedQuery(name = "calendarEventByCalendarAndExternalId", query =
        "SELECT e FROM CalendarEvent e " +
            "WHERE e.component.calendar = :calendar " +
            "AND e.externalId = :externalId"),
    @NamedQuery(name = "calendarEventsByCalendar", query =
        "SELECT distinct e, c.componentInstanceId as ob_1, c.id as ob_2, cmp.period.startDateTime as ob_3 " +
            "FROM CalendarEvent e " +
            "JOIN e.component cmp " +
            "JOIN cmp.calendar c " +
            "WHERE c IN :calendars " +
            "ORDER BY ob_1, ob_2, ob_3"),
    @NamedQuery(name = "calendarEventsByParticipants", query =
        "SELECT distinct e, c.componentInstanceId as ob_1, c.id as ob_2, cmp.period.startDateTime as ob_3 " +
            "FROM CalendarEvent e " +
            "JOIN e.component cmp " +
            "JOIN cmp.calendar c " +
            "JOIN cmp.attendees.attendees a " +
            "WHERE a.attendeeId IN :participantIds " +
            "OR e.id IN (SELECT occ_e.id " +
            "            FROM CalendarEventOccurrence occ_o " +
            "            JOIN occ_o.event occ_e " +
            "            JOIN occ_o.component occ_cmp " +
            "            JOIN occ_cmp.attendees.attendees occ_a " +
            "            WHERE occ_a.attendeeId IN :participantIds" +
            "           )" +
            "ORDER BY ob_1, ob_2, ob_3"),
    @NamedQuery(name = "calendarEventsByCalendarByParticipants", query =
        "SELECT distinct e, c.componentInstanceId as ob_1, c.id as ob_2, cmp.period.startDateTime as ob_3 " +
            "FROM CalendarEvent e " +
            "JOIN e.component cmp " +
            "JOIN cmp.calendar c " +
            "JOIN cmp.attendees.attendees a " +
            "WHERE (c IN :calendars AND a.attendeeId IN :participantIds) " +
            "OR e.id IN (SELECT occ_e.id " +
            "            FROM CalendarEventOccurrence occ_o " +
            "            JOIN occ_o.event occ_e " +
            "            JOIN occ_o.component occ_cmp " +
            "            JOIN occ_cmp.calendar occ_c " +
            "            JOIN occ_cmp.attendees.attendees occ_a " +
            "            WHERE occ_c IN :calendars " +
            "            AND occ_a.attendeeId IN :participantIds" +
            "           )" +
            "ORDER BY ob_1, ob_2, ob_3"),
    @NamedQuery(name = "calendarEventsBeforeSynchronizationDate", query =
        "SELECT distinct e, c.componentInstanceId as ob_1, c.id as ob_2, cmp.period.startDateTime as ob_3 " +
            "FROM CalendarEvent e " +
            "JOIN e.component cmp " +
            "JOIN cmp.calendar c " +
            "WHERE e.synchronizationDate is not null " +
            "AND e.synchronizationDate < :synchronizationDateLimit " +
            "ORDER BY ob_1, ob_2, ob_3"),
    @NamedQuery(name = "calendarEventsByCalendarBeforeSynchronizationDate", query =
        "SELECT distinct e, c.componentInstanceId as ob_1, c.id as ob_2, cmp.period.startDateTime as ob_3 " +
            "FROM CalendarEvent e " +
            "JOIN e.component cmp " +
            "JOIN cmp.calendar c " +
            "WHERE c IN :calendars " +
            "AND e.synchronizationDate is not null " +
            "AND e.synchronizationDate < :synchronizationDateLimit " +
            "ORDER BY ob_1, ob_2, ob_3"),
    @NamedQuery(name = "calendarEventsByCalendarByPeriod", query =
        "SELECT distinct e, c.componentInstanceId as ob_1, c.id as ob_2, cmp.period.startDateTime as ob_3 " +
            "FROM CalendarEvent e " +
            "JOIN e.component cmp " +
            "JOIN cmp.calendar c " +
            "LEFT OUTER JOIN FETCH e.recurrence r " +
            "WHERE (c IN :calendars " +
            "       AND ((cmp.period.startDateTime < :endDateTime AND cmp.period.endDateTime > :startDateTime) " +
            "           OR (cmp.period.endDateTime <= :startDateTime AND e.recurrence IS NOT NULL AND (e.recurrence.endDateTime >= :startDateTime OR e.recurrence.endDateTime IS NULL)))) " +
            "OR e.id IN (SELECT occ_e.id " +
            "            FROM CalendarEventOccurrence occ_o " +
            "            JOIN occ_o.event occ_e " +
            "            JOIN occ_o.component occ_cmp " +
            "            JOIN occ_cmp.calendar occ_c " +
            "            LEFT OUTER JOIN occ_e.recurrence occ_r " +
            "            WHERE occ_c IN :calendars " +
            "            AND (occ_cmp.period.startDateTime < :endDateTime AND occ_cmp.period.endDateTime > :startDateTime)" +
            "           )" +
            "ORDER BY ob_1, ob_2, ob_3"),
    @NamedQuery(name = "calendarEventsByPeriod", query =
        "SELECT distinct e, c.componentInstanceId as ob_1, c.id as ob_2, cmp.period.startDateTime as ob_3 " +
            "FROM CalendarEvent e " +
            "JOIN e.component cmp " +
            "JOIN cmp.calendar c " +
            "LEFT OUTER JOIN FETCH e.recurrence r " +
            "WHERE ((cmp.period.startDateTime < :endDateTime AND cmp.period.endDateTime > :startDateTime) " +
            "       OR (cmp.period.endDateTime <= :startDateTime AND e.recurrence IS NOT NULL AND (e.recurrence.endDateTime >= :startDateTime OR e.recurrence.endDateTime IS NULL))) " +
            "OR e.id IN (SELECT occ_e.id " +
            "            FROM CalendarEventOccurrence occ_o " +
            "            JOIN occ_o.event occ_e " +
            "            JOIN occ_o.component occ_cmp " +
            "            LEFT OUTER JOIN occ_e.recurrence occ_r " +
            "            WHERE (occ_cmp.period.startDateTime < :endDateTime AND occ_cmp.period.endDateTime > :startDateTime)" +
            "           )" +
            "ORDER BY ob_1, ob_2, ob_3"),
    @NamedQuery(name = "calendarEventsByParticipantsByPeriod", query = 
        "SELECT distinct e, c.componentInstanceId as ob_1, c.id as ob_2, cmp.period.startDateTime as ob_3 " +
            "FROM CalendarEvent e " +
            "JOIN e.component cmp " +
            "JOIN cmp.calendar c " +
            "JOIN cmp.attendees.attendees a " +
            "LEFT OUTER JOIN FETCH e.recurrence r " +
            "WHERE (a.attendeeId IN :participantIds " +
            "       AND ((cmp.period.startDateTime < :endDateTime AND cmp.period.endDateTime > :startDateTime) " +
            "            OR" +
            "            (cmp.period.endDateTime <= :startDateTime AND e.recurrence IS NOT NULL AND (e.recurrence.endDateTime >= :startDateTime OR e.recurrence.endDateTime IS NULL)))) " +
            "OR e.id IN (SELECT occ_e.id " +
            "            FROM CalendarEventOccurrence occ_o " +
            "            JOIN occ_o.event occ_e " +
            "            JOIN occ_o.component occ_cmp " +
            "            JOIN occ_cmp.attendees.attendees occ_a " +
            "            LEFT OUTER JOIN occ_e.recurrence occ_r " +
            "            WHERE occ_a.attendeeId IN :participantIds " +
            "            AND (occ_cmp.period.startDateTime < :endDateTime AND occ_cmp.period.endDateTime > :startDateTime)" +
            "           ) " +
            "ORDER BY ob_1, ob_2, ob_3"),
    @NamedQuery(name = "calendarEventsByCalendarByParticipantsByPeriod", query =
        "SELECT distinct e, c.componentInstanceId as ob_1, c.id as ob_2, cmp.period.startDateTime as ob_3 " +
            "FROM CalendarEvent e " +
            "JOIN e.component cmp " +
            "JOIN cmp.calendar c " +
            "JOIN cmp.attendees.attendees a " +
            "LEFT OUTER JOIN FETCH e.recurrence r " +
            "WHERE (c IN :calendars " +
            "       AND a.attendeeId IN :participantIds " +
            "       AND ((cmp.period.startDateTime < :endDateTime AND cmp.period.endDateTime > :startDateTime) " +
            "            OR (cmp.period.endDateTime <= :startDateTime AND e.recurrence IS NOT NULL AND (e.recurrence.endDateTime >= :startDateTime OR e.recurrence.endDateTime IS NULL)))) " +
            "OR e.id IN (SELECT occ_e.id " +
            "            FROM CalendarEventOccurrence occ_o " +
            "            JOIN occ_o.event occ_e " +
            "            JOIN occ_o.component occ_cmp " +
            "            JOIN occ_cmp.calendar occ_c " +
            "            JOIN occ_cmp.attendees.attendees occ_a " +
            "            LEFT OUTER JOIN occ_e.recurrence occ_r " +
            "            WHERE occ_c IN :calendars " +
            "            AND occ_a.attendeeId IN :participantIds " +
            "            AND (occ_cmp.period.startDateTime < :endDateTime AND occ_cmp.period.endDateTime > :startDateTime)" +
            "           ) " +
            "ORDER BY ob_1, ob_2, ob_3")})
public class CalendarEvent extends BasicJpaEntity<CalendarEvent, UuidIdentifier>
    implements Plannable, Recurrent, Categorized, Prioritized, Contribution, Securable,
    WithAttachment, WithReminder {

  public static final String TYPE = "CalendarEvent";
  public static final String NEXT_START_DATE_TIME_MODEL_PROPERTY = "NEXT_START_DATE_TIME";

  private static final long serialVersionUID = 1L;
  public static final String THE_EVENT = "The event ";

  @Column(name = "externalId")
  private String externalId;

  @OneToOne(optional = false, fetch = FetchType.EAGER, cascade = CascadeType.ALL)
  @JoinColumn(name = "componentId", referencedColumnName = "id", unique = true)
  private CalendarComponent component;

  @Column(name = "visibility")
  @Enumerated(EnumType.STRING)
  @NotNull
  private VisibilityLevel visibilityLevel = VisibilityLevel.PUBLIC;

  @Embedded
  private CategorySet categories = new CategorySet();

  @OneToOne(orphanRemoval = true, cascade = CascadeType.ALL)
  @JoinColumn(name = "recurrenceId", referencedColumnName = "id", unique = true)
  private Recurrence recurrence = Recurrence.NO_RECURRENCE;

  @Column(name = "synchroDate")
  private OffsetDateTime synchronizationDate;

  @Transient
  private WysiwygContent content;

  /**
   * Constructs a new calendar event that spawns to the specified period of time.
   * @param period a period of time in which this event occurs.
   */
  protected CalendarEvent(Period period) {
    this.component = new CalendarComponent(period);
  }

  /**
   * Constructs an empty calendar event. This constructor is dedicated to the persistence engine
   * when loading events from the data source.
   */
  protected CalendarEvent() {
    // this constructor is for the persistence engine.
  }

  /**
   * Gets a calendar event by its identifier.
   * @param id the identifier of the aimed calendar event.
   * @return the instance of the aimed calendar event or null if it does not exist.
   */
  public static CalendarEvent getById(final String id) {
    CalendarEventRepository calendarEventRepository = CalendarEventRepository.get();
    return calendarEventRepository.getById(id);
  }

  /**
   * Gets list of calendar event by their identifier.
   * @param ids the identifiers of the aimed calendar events.
   * @return the instance of the aimed calendar event or null if it does not exist.
   */
  public static List<CalendarEvent> getByIds(final List<String> ids) {
    CalendarEventRepository calendarEventRepository = CalendarEventRepository.get();
    return calendarEventRepository.getById(ids);
  }

  /**
   * Gets a calendar event by its external identifier and the calendar it belongs.
   * An external identifier is the identifier of an event that was imported from a external
   * calendar into a calendar in Silverpeas. This identifier is the one of the event in the
   * external calendar.
   * @param calendar the calendar repository.
   * @param externalId the identifier of the calendar event in the external calendar from which it
   * was imported.
   * @return the instance of the asked calendar event or null if it does not exist.
   */
  @SuppressWarnings("unused")
  public static CalendarEvent getByExternalId(final Calendar calendar, final String externalId) {
    CalendarEventRepository calendarEventRepository = CalendarEventRepository.get();
    return calendarEventRepository.getByExternalId(calendar, externalId);
  }

  /**
   * Creates a new calendar event that is spanning on the specified period of time.
   * @param period the period on which the event occurs.
   * @return a calendar event occurring on the specified period.
   */
  public static CalendarEvent on(Period period) {
    return new CalendarEvent(period);
  }

  /**
   * Creates a new calendar event that is on all the specified day.
   * @param day the day on which the event will occur.
   * @return a calendar event spanning on all the specified day.
   */
  public static CalendarEvent on(final LocalDate day) {
    return new CalendarEvent(Period.between(day, day));
  }

  /**
   * Creates a new calendar event from the properties of the specified occurrence of a calendar
   * event. This method is dedicated to the {@link CalendarEventOccurrence} class.
   * <p>
   * The new calendar event have the same attendees than those of the occurrence with their
   * participation status and their presence status unchanged. The new calendar event obtained
   * from the occurrence isn't planned in any calendar. The sequence number of the new event is
   * set at 0.
   * @return an unplanned calendar event with the properties of the specified occurrence.
   */
  static CalendarEvent from(final CalendarEventOccurrence occurrence) {
    CalendarEvent event = new CalendarEvent();
    event.component = occurrence.asCalendarComponent().copy();
    return event.withVisibilityLevel(occurrence.getVisibilityLevel())
        .withExternalId(occurrence.getCalendarEvent().getExternalId())
        .withCategories(occurrence.getCategories().asArray());
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

  @Override
  public ContributionIdentifier getContributionId() {
    return ContributionIdentifier
        .from(getCalendar().getComponentInstanceId(), getId(), getContributionType());
  }

  /**
   * Gets optionally the rich content of this event.
   * @return the rich content of this event as an {@link Optional} value.
   */
  public Optional<WysiwygContent> getContent() {
    if (content == null && isPlanned()) {
      try {
        content = WysiwygContent.getContent(LocalizedContribution.from(this));
      } catch (Exception e) {
        SilverLogger.getLogger(this)
            .error("Failure while loading the WYSIWYG content of event " + getId(), e);
      }
    }
    return Optional.ofNullable(content);
  }

  /**
   * Sets the rich content of this event.
   * @param content the content to set.
   */
  public void setContent(final WysiwygContent content) {
    this.content = content;
  }

  /**
   * This event is created by the specified user.
   * @param user the user to set as the creator of this event.
   * @return itself.
   */
  public CalendarEvent createdBy(final User user) {
    this.component.createdBy(user);
    return this;
  }

  /**
   * This event is created by the specified user.
   * @param userId the unique identifier of the user to set as the creator of this event.
   * @return itself.
   */
  public CalendarEvent createdBy(final String userId) {
    this.component.createdBy(userId);
    return this;
  }

  /**
   * Gets the user who created and planned this event.
   * @return the user that has authored this event.
   */
  @Override
  public User getCreator() {
    return this.component.getCreator();
  }

  /**
   * Gets the last user who updated this planned event.
   * @return the user who has last updated this event.
   */
  public User getLastUpdater() {
    return this.component.getLastUpdater();
  }

  @Override
  public Date getCreationDate() {
    return this.component.getCreationDate();
  }

  @Override
  public User getLastModifier() {
    return this.component.getLastUpdater();
  }

  @Override
  public Date getLastModificationDate() {
    return this.component.getLastUpdateDate();
  }

  public Date getLastUpdateDate() {
    return this.component.getLastUpdateDate();
  }

  /**
   * Gets the calendar to which this event is related. A calendar event can only be persisted into
   * a given existing calendar.
   * @return either the calendar to which this event belongs or null if this event isn't yet
   * saved into a given calendar.
   */
  @Override
  public Calendar getCalendar() {
    return this.component.getCalendar();
  }

  /**
   * Sets a new calendar to this event. This moves the event from its initial calendar to the
   * specified calendar. This will be effective once the {@link CalendarEvent#update()} method
   * invoked.
   * @param calendar the new calendar into which the event has to move.
   */
  protected void setCalendar(final Calendar calendar) {
    this.component.setCalendar(calendar);
  }

  @Override
  public String getTitle() {
    return component.getTitle();
  }

  @Override
  public void setTitle(String title) {
    this.component.setTitle(title);
  }

  /**
   * Gets the external identifier.
   * <p>
   *   Any events coming from a calendar external to Silverpeas are identified by an unique
   *   identifier for this external calendar. The external identifier is this identifier and it
   *   is null for events in a Silverpeas calendar. This identifier is typically processed by the
   *   calendar import/export mechanism of Silverpeas.
   * </p>
   * @return the external identifier as string.
   */
  public String getExternalId() {
    return externalId;
  }

  /**
   * Specifies an external identifier.
   * @param externalId an external identifier as string.
   * @return itself.
   */
  public CalendarEvent withExternalId(final String externalId) {
    if (externalId != null && externalId.equals(getId())) {
      throw new IllegalArgumentException("externalId must be different from the id");
    }
    this.externalId = StringUtil.isDefined(externalId) ? externalId : null;
    return this;
  }

  /**
   * Indicates if the event is coming from another platform than Silverpeas.
   * @return true if the event is coming from another platform, false otherwise.
   */
  public boolean isExternal() {
    return this.externalId != null;
  }

  /**
   * Gets the last date at which this event was synchronized from an external calendar.
   * @return a date and time or null if this event isn't a synchronized one.
   */
  public OffsetDateTime getLastSynchronizationDate() {
    return this.synchronizationDate;
  }

  /**
   * Is this event comes from the synchronization of an external calendar?
   * @return true if this event is a synchronized one, false otherwise.
   */
  public boolean isSynchronized() {
    return isExternal() && this.synchronizationDate != null;
  }

  /**
   * Sets the date time at which this event is lastly synchronized.
   * @param dateTime an {@link OffsetDateTime} value.
   */
  protected void setLastSynchronizationDate(final OffsetDateTime dateTime) {
    this.synchronizationDate = dateTime;
  }

  /**
   * Specifies a title to this event.
   * @param title the title of the event
   * @return itself.
   */
  public CalendarEvent withTitle(String title) {
    setTitle(title);
    return this;
  }

  /**
   * Specifies a location where this event will occur.
   * @param location a location: an address, a designation, a GPS coordinates, ...
   * @return itself.
   */
  @SuppressWarnings("WeakerAccess")
  public CalendarEvent inLocation(String location) {
    setLocation(location);
    return this;
  }

  /**
   * Specifies the visibility level to this event. In generally, it defines the intention of the
   * user about the visibility on the event he accepts to give. Usual values are PUBLIC, PRIVATE or
   * CONFIDENTIAL for example. By default, the visibility level is PUBLIC.
   * @param accessLevel the new visibility level to this event.
   * @return itself.
   */
  public CalendarEvent withVisibilityLevel(VisibilityLevel accessLevel) {
    this.visibilityLevel = accessLevel;
    return this;
  }

  /**
   * Sets a priority to this event.
   * @param priority an event priority.
   * @return itself.
   */
  @Override
  public CalendarEvent withPriority(Priority priority) {
    this.component.setPriority(priority);
    return this;
  }

  /**
   * Gets the categories to which this event belongs.
   * @return the categories of this event.
   */
  @Override
  public CategorySet getCategories() {
    return this.categories;
  }

  /**
   * Gets the visibility level of this event.
   * @return the visibility level of this event.
   */
  public VisibilityLevel getVisibilityLevel() {
    return visibilityLevel;
  }

  /**
   * Gets a description about this event.
   * @return a description about this event or an empty string if no description is attached to this
   * event.
   */
  @Override
  public String getDescription() {
    return component.getDescription();
  }

  /**
   * Sets a new description to this event.
   * @param description a new description of the event.
   */
  public void setDescription(final String description) {
    this.component.setDescription(description);
  }

  /**
   * Gets the priority of this event.
   * @return the priority of the event.
   */
  @Override
  public Priority getPriority() {
    return this.component.getPriority();
  }

  /**
   * Gets the different additional attributes set to this event.
   * @return the additional attributes of this event.
   */
  public AttributeSet getAttributes() {
    return this.component.getAttributes();
  }

  /**
   * Gets the revision sequence number of this calendar event within a sequence of revisions.
   * Any changes to some properties of a calendar event increment this sequence number. This
   * number is mainly dedicated with the synchronization or syndication mechanism of calendar
   * events with external calendars. Its meaning comes from the icalendar specification.
   * @return the sequence number of this event.
   * @see CalendarComponent#getSequence()
   */
  public long getSequence() {
    return this.component.getSequence();
  }

  /**
   * Gets the location where the event occurs. It can be an address, a designation or a GPS
   * coordinates.
   * @return the event's location.
   */
  public String getLocation() {
    return this.component.getLocation();
  }

  /**
   * Sets a new location for this event. It can be an address, a designation or a GPS coordinates.
   * @param location a location where the event occurs.
   */
  public void setLocation(String location) {
    this.component.setLocation(location);
  }

  /**
   * Recurs this event with the specified event recurrence.
   *
   * If the recurrence ends up at a given date or datetime, then this value is updated according to
   * the period of time of this event:
   *
   * <ul>
   *  <li>
   *    The event is on all the day: the recurrence rule is updated to end at the given recurrence
   *    ending date; the time part is removed.
   *  </li>
   *  <li>
   *    The event starts and ends at a given datetime: the recurrence rule is updated to end at
   *    a datetime with as date the given recurrence ending date and as time the time at which
   *    this event usually starts.
   *  </li>
   * </ul>
   * @param recurrence the recurrence defining the recurring property of this event.
   * @return itself.
   */
  @Override
  public CalendarEvent recur(final Recurrence recurrence) {
    if (isOnAllDay() && recurrence.getFrequency().isHourly()) {
      throw new IllegalArgumentException("Impossible to recur hourly an event on all day!");
    }
    this.recurrence = recurrence.startingAt(this.getStartDate());
    return this;
  }

  @Override
  public void unsetRecurrence() {
    this.recurrence = Recurrence.NO_RECURRENCE;
  }

  /**
   * Sets a description ot this event.
   * @param description the description to set.
   * @return itself.
   */
  public CalendarEvent withDescription(String description) {
    setDescription(description);
    return this;
  }

  /**
   * Adds the specified attribute among the attributes of this event. Same as
   * <pre>{@code
   * getAttributes().add(attrName, attrValue);
   * return this;}</pre>
   * @param attrName the name of the attribute to add.
   * @param attrValue the value of the attribute to add.
   * @return itself.
   */
  public CalendarEvent withAttribute(String attrName, String attrValue) {
    getAttributes().set(attrName, attrValue);
    return this;
  }

  /**
   * Adds the specified categories to the event. Same as
   * <pre>{@code
   * getCategories().addAll(categories);
   * return this;}</pre>
   * @param categories one or more categories with which this event will be categorized.
   * @return itself.
   */
  public CalendarEvent withCategories(String ...categories) {
    getCategories().addAll(categories);
    return this;
  }

  /**
   * Gets the recurrence of this recurring event. If the event isn't a recurring one, then returns
   * NO_RECURRENCE.
   * @return this event recurrence or NO_RECURRENCE.
   */
  @Override
  public Recurrence getRecurrence() {
    return this.recurrence;
  }

  /**
   * Is this event occurring on all the day(s)?
   * @return true if this event is occurring on all its day(s).
   */
  @Override
  public boolean isOnAllDay() {
    return getPeriod().isInDays();
  }

  /**
   * BE CAREFUL, it is not possible to use the persistence when the ID is set by this method or
   * {@link #setId(String)}.<br>
   * TODO this behavior will be removed...
   * @param appId the identifier of the app
   * @param eventId the identifier of the event
   * @return itself
   */
  public CalendarEvent identifiedBy(String appId, String eventId) {
    if (getId() != null) {
      throw new IllegalStateException("identifier should be null on this method call");
    }
    setId(appId + "-" + eventId);
    return this;
  }

  @Override
  public Temporal getStartDate() {
    return getPeriod().getStartDate();
  }

  @Override
  public Temporal getEndDate() {
    return getPeriod().getEndDate();
  }

  /**
   * Changes the planning of this event in the calendar.
   * The change will be effective only once the {@code update} method invoked.
   * @param newDay the new day at which this event will occur or has actually occurred.
   */
  public void setDay(final LocalDate newDay) {
    this.component.setPeriod(Period.between(newDay, newDay));
    if (this.recurrence != null) {
      this.recurrence = this.recurrence.startingAt(newDay);
    }
  }

  @Override
  public CalendarEvent planOn(final Calendar calendar) {
    CalendarEvent event = Transaction.getTransaction().perform(() -> {
      if (!isPersisted()) {
        CalendarEventRepository repository = CalendarEventRepository.get();
        setCalendar(calendar);
        normalize();
        CalendarEvent savedEvent = repository.save(this);
        savedEvent.getContent().ifPresent(WysiwygContent::save);
        return savedEvent;
      }
      return this;
    });
    notify(ResourceEvent.Type.CREATION, event);
    return event;
  }

  private void normalize() {
    if (isRecurrent()) {
      // in the case the start date of the event doesn't match its recurrence rule
      ZonedDateTime startDate = TemporalConverter.asZonedDateTime(getStartDate());
      Optional<CalendarEventOccurrence> firstOccurrence =
          CalendarEventOccurrence.getNextOccurrence(this, startDate.minusDays(1));
      firstOccurrence.filter(o -> !o.getStartDate().equals(this.getStartDate())).ifPresent(o -> {
        this.getRecurrence().startingAt(o.getStartDate());
        this.component.setPeriod(Period.between(o.getStartDate(), o.getEndDate()));
      });
    }
  }

  @Override
  public boolean isPlanned() {
    return isPersisted();
  }

  /**
   * Gets the attendees. The adding or the removing of an attendee should be done
   * only by the creator of this event. Nevertheless, there is actually no validation of this
   * rule and it is left to the services to perform such a rule validation according to their own
   * requirements.
   * @return a stream of attendees to this event.
   */
  public AttendeeSet getAttendees() {
    return this.component.getAttendees();
  }

  /**
   * Adds an attendee in this event and returns itself. It is a short write of
   * {@code event.getAttendees().add(InternalAttendee.fromUser(user).to(event))}
   * @param user the user in Silverpeas whose participation in this event is required.
   * @return the event itself.
   */
  public CalendarEvent withAttendee(User user) {
    getAttendees().add(user);
    return this;
  }

  /**
   * Adds an attendee in this event and returns itself. It is a short write of
   * {@code event.getAttendees().add(ExternalAttendee.withEmail(email).to(event))}
   * @param email the email of a user external to Silverpeas and whose the participation in this
   * event is required.
   * @return the event itself.
   */
  public CalendarEvent withAttendee(String email) {
    getAttendees().add(email);
    return this;
  }

  /**
   * Copies the specified event to another one. Only the specific business event attributes are
   * copied. Others business attributes as well as technical ones are set to null: creation date,
   * update date, creator, updater, identifier and version number.
   * @return a shallow copy of this event.
   */
  public CalendarEvent copy() {
    CalendarEvent copy = new CalendarEvent();
    copy.externalId = null;
    if (recurrence == Recurrence.NO_RECURRENCE) {
      copy.recurrence = Recurrence.NO_RECURRENCE;
    } else {
      copy.recurrence = recurrence.copy();
    }
    copy.categories = categories.copy();
    copy.component = component.copy();
    copy.visibilityLevel = visibilityLevel;
    copy.content = new WysiwygContent(content);
    copy.synchronizationDate = synchronizationDate;
    return copy;
  }

  /**
   * Deletes entirely this event and all of its occurrences. Does nothing if the event isn't
   * yet planned.
   * @return the result of the operation. It is empty as the event is deleted.
   */
  @Override
  public EventOperationResult delete() {
    return Transaction.performInOne(() -> {
      if (isPlanned()) {
        // Deletes all persisted occurrences belonging to this event
        deleteAllOccurrencesFromPersistence();
        // Deletes the event from persistence
        deleteFromPersistence();
        // notify about the deletion
        notify(ResourceEvent.Type.DELETION, this);
      }
      return new EventOperationResult();
    });
  }

  /**
   * Deletes only the specified occurrence of this event.
   *
   * If the event is non recurrent, then the event is itself deleted. Otherwise the original
   * starting date of the occurrence is added into the exception dates of its recurrence rule and
   * the event is updated. Keep in mind this last rule applies even if the occurrence is the single
   * one of the recurrence rule. If the occurrence is persisted, it is then removed from the
   * persistence context
   *
   * If this event isn't yet planned or it has no occurrences, then an {@link IllegalStateException}
   * exception is thrown.
   *
   * @param occurrence a reference to the occurrence to delete. If the occurrence doesn't come from
   * this event, then an {@link IllegalArgumentException} exception is thrown.
   * @return the result of the deletion. If the event is recurrent, it has the updated event
   * (its recurrence rule has been modified). Otherwise it is empty as the event was deleted.
   */
  public EventOperationResult deleteOnly(CalendarEventOccurrence occurrence) {
    checkOccurrence(occurrence);
    return doIfSingleOccurrence(() -> {
      this.deleteFromPersistence();
      notify(ResourceEvent.Type.DELETION, this);
      return new EventOperationResult();
    }).orElse(() -> {
      this.getRecurrence().excludeEventOccurrencesStartingAt(occurrence.getOriginalStartDate());
      occurrence.deleteFromPersistence();
      this.updateIntoPersistence();
      notify(ResourceEvent.Type.DELETION, LifeCycleEventSubType.SINGLE, occurrence);
      return new EventOperationResult().withUpdated(this);
    });
  }

  /**
   * Deletes for this event all the occurrences since and including the specified one.
   *
   * If the event is non recurrent, then the event is itself deleted. Otherwise the event is
   * updated with its recurrence ending at the original starting date of the given occurrence.
   *
   * If this event isn't yet planned or it has no occurrence, then an {@link IllegalStateException}
   * exception is thrown.
   *
   * @param occurrence the occurrence since which all the forthcoming occurrences (and including
   * the specified occurrence) have to be deleted. If the occurrence doesn't come from this event,
   * then an {@link IllegalArgumentException} exception is thrown.
   * @return the result of the deletion. If the event is recurrent, it has the updated event (its
   * recurrence rule has been modified). Otherwise it is empty as the event was deleted.
   */
  public EventOperationResult deleteSince(CalendarEventOccurrence occurrence) {
    checkOccurrence(occurrence);
    return doIfSingleOccurrence(() -> {
      this.deleteFromPersistence();
      notify(ResourceEvent.Type.DELETION, this);
      return new EventOperationResult();
    }).orElse(() -> {
      final Temporal endDate = occurrence.getOriginalStartDate().minus(1, ChronoUnit.DAYS);
      this.getRecurrence().until(endDate);
      occurrence.deleteAllSinceMeFromThePersistence();
      this.updateIntoPersistence();
      notify(ResourceEvent.Type.DELETION, LifeCycleEventSubType.SINCE, occurrence);
      return new EventOperationResult().withUpdated(this);
    });
  }

  /**
   * Updates this event. The modifications to this event are saved for all its occurrences.
   * Its sequence number is incremented by one. If the event isn't yet planned, an
   * {@link IllegalStateException} exception is thrown.
   *
   * @return the result of the update. It has the updated event.
   */
  @Override
  public EventOperationResult update() {
    if (!isPlanned()) {
      throw new IllegalStateException(THE_EVENT + this.getId() + " is not yet planned");
    }
    CalendarEvent previousState = getEventPreviousState();
    return Transaction.performInOne(() -> {
      applyChanges(previousState);
      final EventOperationResult result;
      if (!previousState.getCalendar().equals(this.getCalendar())) {
        result = moveToAnotherCalendar(previousState);
      } else {
        this.updateIntoPersistence();
        notify(ResourceEvent.Type.UPDATE, previousState, this);
        result = new EventOperationResult().withUpdated(this);
      }
      if (!OperationContext.statesOf(IMPORT)) {
        CalendarEvent updatedEvent =
            result.created().orElseGet(() -> result.updated().orElse(null));
        CalendarComponentDiffDescriptor diffDescriptor =
            diffBetween(updatedEvent.asCalendarComponent(), previousState.asCalendarComponent());
        applyToPersistedOccurrences(updatedEvent, diffDescriptor, result.created().isPresent());
      }
      return result;
    });
  }

  /**
   * Updates this event with the state of the specified calendar event.
   * @param event the event from which this event should be updated. It must represent this event
   * but with a new state, otherwise an {@link IllegalStateException} exception is thrown.
   * @return the result of the update. It has the updated event.
   */
  public EventOperationResult updateFrom(final CalendarEvent event) {
    if (!this.getId().equals(event.getId()) &&
        (this.externalId == null || !this.externalId.equals(event.externalId))) {
      throw new IllegalStateException(
          THE_EVENT + this.getId() + " cannot be updated from another event");
    }
    if (isRecurrent() && !event.isRecurrent()) {
      this.deleteAllOccurrencesFromPersistence();
    }
    event.component.copyTo(this.component);
    this.externalId = event.getExternalId();
    this.visibilityLevel = event.visibilityLevel;
    this.recurrence = event.recurrence;
    this.categories = event.categories;
    this.synchronizationDate = event.synchronizationDate;
    this.component.setSequence(event.component.getSequence());
    if (this.component.getLastUpdateDate().getTime() <
        event.component.getLastUpdateDate().getTime()) {
      this.component.updatedBy(event.component.getLastUpdater(),
          event.component.getLastUpdateDate());
    }
    return this.update();
  }

  /**
   * Updates all the occurrences of this event since and including the specified occurrence with
   * the modifications to this event. The modifications to this event are saved for all the
   * occurrences since and including the specified occurrence. The occurrences occurring before the
   * specified occurrence won't be updated.
   *
   * If the event isn't yet planned or it has no occurrence then an {@link IllegalStateException}
   * exception is thrown.
   *
   * If the specified occurrence is in fact the single one of this event, then this event is
   * itself updated (this is equivalent to the {@link CalendarEvent#update()} method. Otherwise a
   * new event is created from the modifications to this event.
   *
   * @param occurrence the occurrence of the event since which the changes to the event should be
   * applied. If the occurrence doesn't come from this event, then an
   * {@link IllegalArgumentException} exception is thrown.
   * @return the result of the update. If the event is recurrent, then it has the updated event
   * (with its modified recurrence rule) and the newly created event (for all of the forthcoming
   * occurrences, including the specified one). Otherwise it has only the updated event.
   */
  public EventOperationResult updateSince(CalendarEventOccurrence occurrence) {
    checkOccurrence(occurrence);
    return doIfSingleOccurrence(() -> {
      CalendarEvent previousState = getEventPreviousState();
      EventOperationResult result = updateFromOccurrence(occurrence);
      notify(ResourceEvent.Type.UPDATE, previousState, this);
      return result;
    }).orElse(() -> {
      final CalendarEventOccurrence previous = getEventOccurrencePreviousState(occurrence);
      final CalendarEvent createdEvent = createNewEventSince(occurrence);
      final Temporal endDate = occurrence.getOriginalStartDate().minus(1, ChronoUnit.DAYS);
      this.getRecurrence().until(endDate);
      occurrence.deleteAllSinceMeFromThePersistence();
      this.updateIntoPersistence();
      notify(ResourceEvent.Type.UPDATE, LifeCycleEventSubType.SINCE, previous, occurrence);
      return new EventOperationResult().withUpdated(this).withCreated(createdEvent);
    });
  }

  /**
   * Updates only the specified occurrence among the occurrences of this event.
   *
   * If the event is recurrent, even if the given occurrence is the single one obtained from the
   * recurrence rule, then only this occurrence is updated and the changes are persisted. The
   * sequence number of the occurrence is incremented by one.
   *
   * If the event is non recurrent then the changes are applied on this event itself from the
   * state of the occurrence. The sequence number of the event is incremented by one.
   *
   * In the case the date at which the occurrence starts is modified, the participation status
   * of all the attendees in this occurrence is cleared. If the occurrence comes from a non
   * recurrent event, then there are the participation status of all the attendees in this event
   * that are cleared.
   *
   * If this event isn't yet planned or it has no occurrences, then an {@link IllegalStateException}
   * exception is thrown.
   *
   * @param occurrence a reference to an occurrence of the event with the data modified.
   *  If the occurrence doesn't come from this event, then an
   * {@link IllegalArgumentException} exception is thrown.
   * @return the result of the update. For a recurrent event, it has only the updated and
   * persisted occurrence. Otherwise it has the updated event.
   */
  public EventOperationResult updateOnly(CalendarEventOccurrence occurrence) {
    checkOccurrence(occurrence);
    return doIfSingleOccurrence(() -> {
      CalendarEvent previousState = getEventPreviousState();
      EventOperationResult result = updateFromOccurrence(occurrence);
      notify(ResourceEvent.Type.UPDATE, previousState, this);
      return result;
    }).orElse(() -> {
      final CalendarEventOccurrence previous = getEventOccurrencePreviousState(occurrence);
      if (occurrence.isDateChanged()) {
        occurrence.getAttendees().forEach(Attendee::resetParticipation);
      }
      occurrence.saveIntoPersistence();
      notify(ResourceEvent.Type.UPDATE, LifeCycleEventSubType.SINGLE, previous, occurrence);
      return new EventOperationResult().withInstance(occurrence);
    });
  }

  @Override
  public boolean canBeAccessedBy(final User user) {
    return SecurableRequestCache.canBeAccessedBy(user, getId(), u -> {
      boolean canBeAccessed = getCalendar().canBeAccessedBy(u) || isUserParticipant(u);
      if (!canBeAccessed && PUBLIC == getVisibilityLevel()) {
        SilverpeasComponentInstance componentInstance =
            SilverpeasComponentInstance.getById(getCalendar().getComponentInstanceId())
                .orElse(null);
        if (componentInstance != null) {
          canBeAccessed = componentInstance.isPublic() || componentInstance.isPersonal();
        }
      }
      return canBeAccessed;
    });
  }

  @Override
  public String getContributionType() {
    return TYPE;
  }

  @Override
  public boolean canBeModifiedBy(final User user) {
    return SecurableRequestCache.canBeModifiedBy(user, getId(), u -> {
      boolean isCalendarSynchronized = getCalendar().getExternalCalendarUrl() != null;
      if (!isCalendarSynchronized && canBeAccessedBy(u)) {
        final SilverpeasRole highestUserSilverpeas =
            SilverpeasComponentInstance.getById(getCalendar().getComponentInstanceId()).get()
                .getHighestSilverpeasRolesFor(u);
        if (highestUserSilverpeas == SilverpeasRole.writer) {
          return u.getId().equals(getCreator().getId());
        }
        return highestUserSilverpeas != null &&
            highestUserSilverpeas.isGreaterThanOrEquals(SilverpeasRole.publisher);
      }
      return false;
    });
  }

  /**
   * Gets the {@link CalendarComponent} representation of this event. Any change to the returned
   * calendar component will change also the related event.
   * @return a {@link CalendarComponent} instance representing this event (without the specific
   * properties related to a calendar event).
   */
  public CalendarComponent asCalendarComponent() {
    return this.component;
  }

  @PostLoad
  protected void afterLoadingFromPersistenceContext() {
    if (this.recurrence != null) {
      this.recurrence = this.recurrence.startingAt(this.getStartDate());
    }
  }

  private void deleteFromPersistence() {
    if (isPersisted()) {
      Transaction.getTransaction().perform(() -> {
        CalendarEventRepository repository = CalendarEventRepository.get();
        repository.delete(this);
        WysiwygContent.deleteAllContents(this);
        return null;
      });
    }
  }

  private CalendarEvent updateIntoPersistence() {
    if (getNativeId() != null) {
      return Transaction.getTransaction().perform(() -> {
        CalendarEventRepository repository = CalendarEventRepository.get();
        getContent().filter(WysiwygContent::isModified).ifPresent(WysiwygContent::save);
        return repository.save(this);
      });
    }
    return this;
  }

  private long deleteAllOccurrencesFromPersistence() {
    return Transaction.performInOne(() -> {
      CalendarEventOccurrenceRepository repository = CalendarEventOccurrenceRepository.get();
      List<CalendarEventOccurrence> occurrences = repository.getAllByEvent(this);
      repository.delete(occurrences);
      return occurrences.size();
    });
  }

  private Period getPeriod() {
    return this.component.getPeriod();
  }

  /**
   * Changes the planning of this event in the calendar.
   * The change will be effective only once the {@code update} method invoked.
   * @param newPeriod a new period of time on which this event will occur or has actually occurred.
   */
  public void setPeriod(final Period newPeriod) {
    this.component.setPeriod(newPeriod);
    if (this.recurrence != null) {
      this.recurrence = this.recurrence.startingAt(newPeriod.getStartDate());
    }
  }

  /**
   * Gets all the occurrences linked to this event and explicitly persisted into persistence
   * context. So, an occurrence providing by a computation (instead of the persistence
   * context) is not included into result list.
   * <p> Please notice that the occurrences are retrieved on the demand and the returned list is not
   * coming from an attribute of this event entity</p>
   * @return a list of persisted occurrences linked to this event. If the event isn't recurrent or
   * the event isn't yet planned on a calendar, then an empty list is returned.
   */
  public List<CalendarEventOccurrence> getPersistedOccurrences() {
    return getPersistedOccurrences(this);
  }

  private void notify(ResourceEvent.Type type, CalendarEvent... events) {
    CalendarEventLifeCycleEventNotifier notifier = CalendarEventLifeCycleEventNotifier.get();
    notifier.notifyEventOn(type, events);
  }

  private void notify(ResourceEvent.Type type, LifeCycleEventSubType subType,
      CalendarEventOccurrence... occurrences) {
    CalendarEventOccurrenceLifeCycleEventNotifier notifier =
        CalendarEventOccurrenceLifeCycleEventNotifier.get();
    notifier.notifyEventOn(type, subType, occurrences);
  }

  private CalendarEvent createNewEventSince(final CalendarEventOccurrence occurrence) {
    CalendarEvent newEvent = occurrence.toRecurrentCalendarEvent();
    if (occurrence.isDateChanged()) {
      newEvent.getAttendees().forEach(Attendee::resetParticipation);
    }
    newEvent.component.incrementSequence();
    return newEvent.planOn(this.getCalendar());
  }

  private EventOperationResult updateFromOccurrence(final CalendarEventOccurrence occurrence) {
    Period previousPeriod = this.getPeriod().copy();
    this.component = occurrence.asCalendarComponent().copyTo(this.component);
    if (!this.getPeriod().equals(previousPeriod)) {
      this.getAttendees().forEach(Attendee::resetParticipation);
    }
    CalendarEvent updatedEvent = this.updateIntoPersistence();
    return new EventOperationResult().withUpdated(updatedEvent);
  }

  /**
   * Gets the previous state of an event from the persistence. If this event hasn't changed since
   * its last loading, then the returned event instance and this one will be the same.
   * @return an instance of this calendar event but with its states loaded from the persistence
   * context and without taking into account entity manager caches.
   */
  private CalendarEvent getEventPreviousState() {
    if (StringUtil.isNotDefined(this.getId())) {
      return null;
    }

    // Getting previous data from the persistence.
    final CalendarEvent event = Transaction.performInNew(() -> CalendarEvent.getById(this.getId()));

    // Checking that previous and new data concerns the same scope specified by the identifier of
    // a component instance.
    if (!event.getCalendar().getComponentInstanceId()
        .equals(this.getCalendar().getComponentInstanceId())) {
      throw new IllegalArgumentException("Two states of the event " + event.getId() +
          " doesn't refer the same component instance");
    }

    return event;
  }

  private CalendarEventOccurrence getEventOccurrencePreviousState(
      final CalendarEventOccurrence occurrence) {
    if (StringUtil.isNotDefined(this.getId())) {
      return null;
    }

    final CalendarEventOccurrence previous = Transaction.performInNew(
        () -> CalendarEventOccurrence.getById(occurrence.getId()).orElse(null));

    if (previous == null ||
        !previous.getCalendarEvent().getId().equals(occurrence.getCalendarEvent().getId()) ||
        !previous.getCalendarEvent()
            .getCalendar()
            .getComponentInstanceId()
            .equals(occurrence.getCalendarEvent().getCalendar().getComponentInstanceId())) {
      throw new IllegalArgumentException(
          "Two states of the event occurrence " + occurrence.getId() +
              " doesn't refer the same component instance");
    }

    return previous;
  }

  private void checkOccurrence(final CalendarEventOccurrence occurrence) {
    if (occurrence == null || !this.equals(occurrence.getCalendarEvent())) {
      throw new IllegalArgumentException(
          "The occurrence is either null or comes from a different event than '" + this.getId() +
              "'");
    }
  }

  private boolean isUserParticipant(final User user) {
    return !getAttendees().stream().filter(attendee -> attendee.getId().equals(user.getId()))
        .collect(Collectors.toList())
        .isEmpty();
  }

  private void applyChanges(final CalendarEvent previousState) {
    // Clears exception dates when switching on all day data
    if (getRecurrence() != null && previousState.isOnAllDay() != isOnAllDay()) {
      getRecurrence().clearsAllExceptionDates();
    }

    // If it exists date or recurrence changes, participation of attendees are reset.
    if (isDateOrRecurrenceChangedWith(previousState)) {
      this.getAttendees().forEach(Attendee::resetParticipation);

      // Deletes all persisted occurrences belonging to this event to reset any changes in some
      // of the event's occurrences
      deleteAllOccurrencesFromPersistence();
    }

    normalize();
    if (this.isModifiedSince(previousState)) {
      // force the update in the case of change(s) only in the event's component
      this.component.markAsModified();
    } else if (getAttendees().onlyAttendeePropertyChange(previousState.getAttendees())) {
      // we don't want update properties to be modified on participation answer or presence
      // status change in the attendees
      this.component
          .updatedBy(previousState.component.getLastUpdater(), previousState.getLastUpdateDate());
    }
  }

  private EventOperationResult moveToAnotherCalendar(final CalendarEvent previousState) {
    // Deletes all persisted occurrences belonging to this event (recreated after from
    // previousOccurrences list by applyToPersistedOccurrences method)
    deleteAllOccurrencesFromPersistence();
    // New event is created on other calendar
    CalendarEvent newEvent = this.copy();
    newEvent.component.setSequence(0);
    newEvent.planOn(this.getCalendar());
    // Deleting previous event
    previousState.deleteFromPersistence();
    return new EventOperationResult().withCreated(newEvent);
  }

  private boolean isDateOrRecurrenceChangedWith(final CalendarEvent previousState) {
    if (!previousState.getStartDate().equals(this.getStartDate()) ||
        !previousState.getEndDate().equals(this.getEndDate())) {
      return true;
    }
    if (previousState.isRecurrent() &&
        !previousState.getRecurrence().sameAs(this.getRecurrence())) {
      return true;
    }
    return this.isRecurrent() && !this.getRecurrence().sameAs(previousState.getRecurrence());
  }

  private void applyToPersistedOccurrences(final CalendarEvent updatedEvent,
      final CalendarComponentDiffDescriptor diff, boolean plannedIntoAnotherCalendar) {
    List<CalendarEventOccurrence> previousOccurrences = getPersistedOccurrences(this);
    if (!previousOccurrences.isEmpty() && updatedEvent != null) {
      if (plannedIntoAnotherCalendar) {
        previousOccurrences.stream()
            .map(o -> {
              CalendarEventOccurrence newOccurrence = o.copyWithEvent(updatedEvent);
              diff.mergeInto(newOccurrence.asCalendarComponent());
              return newOccurrence;
            })
            .forEach(CalendarEventOccurrence::saveIntoPersistence);
      } else if (diff.existsDiff()) {
        previousOccurrences.stream()
            .filter(o -> diff.mergeInto(o.asCalendarComponent()))
            .forEach(CalendarEventOccurrence::saveIntoPersistence);
      }
    }
  }

  private static List<CalendarEventOccurrence> getPersistedOccurrences(final CalendarEvent event) {
    if (!event.isPersisted() || !event.isRecurrent()) {
      return emptyList();
    }
    CalendarEventOccurrenceRepository occurrenceRepository =
        CalendarEventOccurrenceRepository.get();
    return occurrenceRepository.getAllByEvent(event);
  }

  /**
   * This method executes the given operation if, and only if following conditions are verified:
   * <ul>
   * <li>the event has recurrence set</li>
   * <li>it exists only one occurrence of the event without taking into account the exception
   * dates registered of the recurrence</li>
   * </ul>
   * If above conditions are not verified, then the operation given to {@link
   * OrElse#orElse(Supplier)} method of the returned {@link OrElse} instance is performed.
   * @param operation the operation to execute if it exists only one occurrence.
   * @return {@link OrElse} instance which will starts the process after the {@link
   * OrElse#orElse(Supplier)} call.
   */
  private OrElse doIfSingleOccurrence(Supplier<EventOperationResult> operation) {
    return new OrElse(operation);
  }

  public static class EventOperationResult
      extends OperationResult<CalendarEvent, CalendarEventOccurrence> {}

  private class OrElse {

    private Supplier<EventOperationResult> operationForSingleOccurrence;

    public OrElse(Supplier<EventOperationResult> operationForSingleOccurrence) {
      this.operationForSingleOccurrence = operationForSingleOccurrence;
    }

    private CalendarEventOccurrenceGenerator generator() {
      return CalendarEventOccurrenceGenerator.get();
    }

    public EventOperationResult orElse(
        Supplier<EventOperationResult> operationForSeveralOccurrences) {
      final CalendarEvent previousEvent = getEventPreviousState();
      return Transaction.performInOne(() -> {
        long occurrenceCount = generator().countOccurrencesOf(previousEvent, null);
        if (occurrenceCount > 1) {
          return operationForSeveralOccurrences.get();
        } else if (occurrenceCount == 1) {
          return operationForSingleOccurrence.get();
        }
        throw new IllegalStateException(THE_EVENT + previousEvent.getId() +
            " is either not planned or it doesn't occur in the calendar " +
            previousEvent.getCalendar().getId());
      });
    }
  }

  /**
   * Is the properties of this calendar event was modified since its last specified state?
   * The attendees in this event aren't taken into account as they aren't considered as a
   * property of a calendar event.
   * @param previous a previous state of this calendar event.
   * @return true if the state of this calendar event is different with the specified one.
   */
  public boolean isModifiedSince(final CalendarEvent previous) {
    if (!this.getId().equals(previous.getId())) {
      throw new IllegalArgumentException(
          "The calendar event of id " + previous.getId() + " isn't the expected one " +
              this.getId());
    }
    if (this.getVisibilityLevel() != previous.getVisibilityLevel() ||
        !this.getCategories().equals(previous.getCategories())) {
      return true;
    }

    if ((this.isRecurrent() && !this.getRecurrence().equals(previous.getRecurrence())) ||
        previous.isRecurrent() && !previous.getRecurrence().equals(this.getRecurrence())) {
      return true;
    }

    if (!this.getContent().equals(previous.getContent())) {
      return true;
    }

    return this.asCalendarComponent().isModifiedSince(previous.asCalendarComponent());
  }

  @Override
  public ContributionModel getModel() {
    return new CalendarEventModel(this);
  }

  @Override
  public boolean isIndexable() {
    return false;
  }
}