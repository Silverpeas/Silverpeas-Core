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

package org.silverpeas.core.webapi.reminder;

import org.apache.commons.lang3.tuple.Pair;
import org.silverpeas.core.ApplicationService;
import org.silverpeas.core.annotation.WebService;
import org.silverpeas.core.contribution.model.Contribution;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.contribution.model.ContributionLocalizationBundle;
import org.silverpeas.core.contribution.model.ContributionModel;
import org.silverpeas.core.date.TimeUnit;
import org.silverpeas.core.reminder.DateTimeReminder;
import org.silverpeas.core.reminder.DurationReminder;
import org.silverpeas.core.reminder.Reminder;
import org.silverpeas.core.reminder.ReminderProcessName;
import org.silverpeas.kernel.bundle.LocalizationBundle;
import org.silverpeas.kernel.util.Mutable;
import org.silverpeas.kernel.bundle.ResourceLocator;
import org.silverpeas.core.web.mvc.webcomponent.WebMessager;
import org.silverpeas.core.web.rs.RESTWebService;
import org.silverpeas.core.web.rs.annotation.Authenticated;

import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static javax.ws.rs.core.Response.Status.FORBIDDEN;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static org.silverpeas.core.SilverpeasExceptionMessages.failureOnGetting;
import static org.silverpeas.core.cache.service.VolatileCacheServiceProvider.getSessionVolatileResourceCacheService;
import static org.silverpeas.core.reminder.Reminder.getByContributionAndUser;
import static org.silverpeas.core.reminder.ReminderSettings.getMessagesIn;
import static org.silverpeas.core.reminder.ReminderSettings.getPossibleReminders;
import static org.silverpeas.kernel.util.StringUtil.isDefined;
import static org.silverpeas.kernel.util.StringUtil.isNotDefined;
import static org.silverpeas.core.webapi.reminder.ReminderEntity.fromReminder;
import static org.silverpeas.core.webapi.reminder.ReminderResourceURIs.REMINDER_BASE_URI;

/**
 * A REST Web resource giving reminder data.
 * @author silveryocha
 */
@WebService
@Path(REMINDER_BASE_URI + "/{componentInstanceId}/{type}/{localId}")
@Authenticated
public class ReminderResource extends RESTWebService {

  private static final Function<Pair<Integer, TimeUnit>, String> DURATION_IDS =
      r -> String.valueOf(r.getLeft()) + r.getRight();

  @Inject
  private ReminderResourceURIs uri;

  @PathParam("componentInstanceId")
  private String componentInstanceId;
  @PathParam("type")
  private String type;
  @PathParam("localId")
  private String localId;

  /**
   * Gets the identifier list of possible of durations.
   * <p>
   * An identifier of a duration is the concatenation about the duration value and the duration
   * unit ({@link TimeUnit}).<br>
   * {@code 15MINUTE} for example.
   * </p>
   * @return a filled list if any, or an empty one if no trigger can be scheduled.
   * @see WebProcess#execute()
   */
  @Path("possibledurations/{property}")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @SuppressWarnings("ConstantConditions")
  public List<String> getPossibleDurations(
      @PathParam("property") final String contributionProperty) {
    if (getSessionVolatileResourceCacheService().contains(localId, componentInstanceId)) {
      return getPossibleReminders().map(DURATION_IDS).collect(Collectors.toList());
    }
    final ContributionModel model = getContribution().getModel();
    final ZoneId userZoneId = getUserPreferences().getZoneId();
    final ZoneId platformZoneId = ZoneId.systemDefault();
    final Mutable<Boolean> lastMatchOk = Mutable.of(true);
    return getPossibleReminders()
        .filter(r -> {
          if (lastMatchOk.is(false)) {
            return false;
          }
          final ZonedDateTime from = ZonedDateTime.now(userZoneId).plus(r.getLeft(), r.getRight().toChronoUnit());
          final ZonedDateTime dateReference = model.filterByType(contributionProperty, from)
              .matchFirst(Date.class::isAssignableFrom, d -> ZonedDateTime.ofInstant(((Date) d).toInstant(), platformZoneId))
              .matchFirst(OffsetDateTime.class::equals, d -> ((OffsetDateTime) d).atZoneSameInstant(platformZoneId))
              .matchFirst(LocalDate.class::equals, d -> ((LocalDate) d).atStartOfDay(userZoneId).withZoneSameInstant(platformZoneId))
              .matchFirst(LocalDateTime.class::equals, d -> ((LocalDateTime) d).atZone(platformZoneId))
              .matchFirst(ZonedDateTime.class::equals, d -> ((ZonedDateTime) d).withZoneSameInstant(platformZoneId)).result().orElse(null);
          lastMatchOk.set(dateReference != null);
          return lastMatchOk.get();
        })
        .map(DURATION_IDS)
        .collect(Collectors.toList());
  }

  /**
   * Gets the JSON representation of a list of reminder.
   * If it doesn't exist, a 404 HTTP code is returned.
   * @return the response to the HTTP GET request with the JSON representation of the asked
   * reminders.
   * @see WebProcess#execute()
   */
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public List<ReminderEntity> getReminders() {
    List<Reminder> reminders = getByContributionAndUser(getContributionIdentifier(), getUser());
    return asWebEntities(reminders);
  }

  /**
   * Creates the reminder from its JSON representation and returns it once created.<br>
   * If the user isn't authenticated, a 401 HTTP code is returned. If the user isn't authorized to
   * save the reminder, a 403 is returned. If a problem occurs when processing the request, a 503
   * HTTP code is returned.
   * @param reminderEntity the reminder data
   * @return the response to the HTTP POST request with the JSON representation of the created
   * reminder.
   */
  @POST
  @Produces(MediaType.APPLICATION_JSON)
  public ReminderEntity createReminder(ReminderEntity reminderEntity) {
    final Contribution contribution = getContribution();
    final ReminderProcessName processName = ReminderProcessName.getByName(reminderEntity.getProcessName());
    final Reminder reminder;
    if (isDefined(reminderEntity.getDateTime())) {
      reminder = new DateTimeReminder(getContributionIdentifier(), getUser(), processName);
    } else {
      reminder = new DurationReminder(getContributionIdentifier(), getUser(), processName);
    }
    reminderEntity.mergeInto(reminder);
    try {
      reminder.schedule();
    } catch (AssertionError e) {
      errorMessage("reminder.add.duration.error", getReminderLabel(reminderEntity),
          getUiMessageContributionLabel(reminderEntity, contribution));
      throw new WebApplicationException(e, Response.Status.PRECONDITION_FAILED);
    }
    if (reminder instanceof DurationReminder) {
      successMessage("reminder.add.duration.success", getReminderLabel(reminderEntity),
          getUiMessageContributionLabel(reminderEntity, contribution));
    }
    return asWebEntity(reminder);
  }

  /**
   * Updates the reminder from its JSON representation and returns it once updated.<br>
   * If the user isn't authenticated, a 401 HTTP code is returned. If the user isn't authorized to
   * save the reminder, a 403 is returned. If a problem occurs when processing the request, a 503
   * HTTP code is returned.
   * @param id a reminder identifier, the one of reminderEntity
   * @param reminderEntity the reminder data
   * @return the response to the HTTP POST request with the JSON representation of the updated
   * reminder.
   */
  @Path("{id}")
  @PUT
  @Produces(MediaType.APPLICATION_JSON)
  public ReminderEntity updateReminder(@PathParam("id") String id, ReminderEntity reminderEntity) {
    Contribution contribution = getContribution();
    final Reminder reminder = getByContributionAndUser(getContributionIdentifier(), getUser())
        .stream()
        .filter(r -> r.getId().equals(id))
        .findFirst()
        .orElseThrow(() -> new WebApplicationException(NOT_FOUND));
    reminderEntity.mergeInto(reminder);
    try {
      reminder.schedule();
    } catch (AssertionError e) {
      errorMessage("reminder.update.duration.error", getReminderLabel(reminderEntity),
          getUiMessageContributionLabel(reminderEntity, contribution));
      throw new WebApplicationException(e, Response.Status.PRECONDITION_FAILED);
    }

    if (reminder instanceof DurationReminder) {
      successMessage("reminder.update.duration.success", getReminderLabel(reminderEntity),
          getUiMessageContributionLabel(reminderEntity, contribution));
    }

    return asWebEntity(reminder);
  }

  /**
   * Deletes the given reminder from its JSON representation.<br>
   * If the user isn't authenticated, a 401 HTTP code is returned. If the user isn't authorized to
   * delete the reminder, a 403 is returned. If a problem occurs when processing the request, a 503
   * HTTP code is returned.
   * @param id a reminder identifier
   */
  @DELETE
  @Path("{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public void deleteReminder(@PathParam("id") String id) {
    Contribution contribution = getContribution();
    final Reminder reminder = getByContributionAndUser(getContributionIdentifier(), getUser())
        .stream()
        .filter(r -> r.getId().equals(id))
        .findFirst()
        .orElseThrow(() -> new WebApplicationException(NOT_FOUND));
      reminder.unschedule();

    if (reminder instanceof DurationReminder) {
      successMessage("reminder.delete.success",
          getUiMessageContributionLabel(asWebEntity(reminder), contribution));
    }
  }

  @Override
  protected String getResourceBasePath() {
    return REMINDER_BASE_URI;
  }

  @Override
  public String getComponentId() {
    return componentInstanceId;
  }

  /**
   * Converts the list of reminder into list of linked web entity.
   * @param reminders the reminders to convert.
   * @return the reminder web entities.
   */
  private List<ReminderEntity> asWebEntities(Collection<Reminder> reminders) {
    return reminders.stream().map(this::asWebEntity).collect(Collectors.toList());
  }

  /**
   * Converts the reminder into its corresponding web entity.
   * @param reminder the reminder to convert.
   * @return the corresponding reminder entity.
   */
  private ReminderEntity asWebEntity(Reminder reminder) {
    return fromReminder(reminder).withURI(uri.ofReminder(reminder));
  }

  private ContributionIdentifier getContributionIdentifier() {
    return ContributionIdentifier.from(getComponentId(), localId, type);
  }

  private Contribution getContribution() {
    ContributionIdentifier id = getContributionIdentifier();
    final Contribution contribution = ApplicationService.getInstance(id.getComponentInstanceId())
        .getContributionById(id)
        .orElseThrow(() -> new WebApplicationException(
            failureOnGetting("contribution", getContributionIdentifier().asString()), NOT_FOUND));
    if (!contribution.canBeAccessedBy(getUser())) {
      throw new WebApplicationException(FORBIDDEN);
    }
    return contribution;
  }

  private String getUiMessageContributionLabel(final ReminderEntity reminder,
      final Contribution contribution) {
    ContributionLocalizationBundle bundle = ContributionLocalizationBundle
        .getByInstanceAndLanguage(contribution, getUserPreferences().getLanguage());
    if (isNotDefined(reminder.getDateTime())) {
      return bundle.getUiMessageTitleByTypeAndProperty(reminder.getcProperty());
    }
    return bundle.getUiMessageTitleByType();
  }

  private String getReminderLabel(final ReminderEntity reminder) {
    if (isNotDefined(reminder.getDateTime())) {
      LocalizationBundle localizedUnits = ResourceLocator
          .getLocalizationBundle("org.silverpeas.util.multilang.util",
              getUserPreferences().getLanguage());
      return localizedUnits
          .getStringWithParams(reminder.getTimeUnit() + ".precise", reminder.getDuration());
    }
    return null;
  }

  /**
   * Push a success message to the current user.
   * @param messageKey the key of the message.
   * @param params the message parameters.
   */
  private void successMessage(String messageKey, Object... params) {
    getMessager().addSuccess(
        getMessagesIn(getUserPreferences().getLanguage()).getStringWithParams(messageKey, params));
  }

  /**
   * Push a error message to the current user.
   * @param messageKey the key of the message.
   * @param params the message parameters.
   */
  private void errorMessage(String messageKey, Object... params) {
    getMessager().addError(
        getMessagesIn(getUserPreferences().getLanguage()).getStringWithParams(messageKey, params));
  }

  private WebMessager getMessager() {
    return WebMessager.getInstance();
  }
}
