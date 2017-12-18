/*
 * Copyright (C) 2000 - 2017 Silverpeas
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

package org.silverpeas.core.webapi.reminder;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.silverpeas.core.SilverpeasRuntimeException;
import org.silverpeas.core.annotation.RequestScoped;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.cache.service.CacheServiceProvider;
import org.silverpeas.core.contribution.ContributionManager;
import org.silverpeas.core.contribution.model.Contribution;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.contribution.model.ContributionLocalizationBundle;
import org.silverpeas.core.persistence.datasource.model.identifier.UuidIdentifier;
import org.silverpeas.core.reminder.DateTimeReminder;
import org.silverpeas.core.reminder.DurationReminder;
import org.silverpeas.core.reminder.Reminder;
import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SilverpeasArrayList;
import org.silverpeas.core.util.SilverpeasList;
import org.silverpeas.core.web.mvc.webcomponent.WebMessager;
import org.silverpeas.core.webapi.base.RESTWebService;
import org.silverpeas.core.webapi.base.annotation.Authorized;

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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.silverpeas.core.SilverpeasExceptionMessages.failureOnGetting;
import static org.silverpeas.core.reminder.ReminderSettings.getMessagesIn;
import static org.silverpeas.core.util.StringUtil.isDefined;
import static org.silverpeas.core.util.StringUtil.isNotDefined;
import static org.silverpeas.core.webapi.reminder.ReminderEntity.fromReminder;
import static org.silverpeas.core.webapi.reminder.ReminderResourceURIs.REMINDER_BASE_URI;

/**
 * A REST Web resource giving reminder data.
 * @author silveryocha
 */
@Service
@RequestScoped
@Path(REMINDER_BASE_URI + "/{componentInstanceId}/{type}/{localId}")
@Authorized
public class ReminderResource extends RESTWebService {

  @Inject
  private ReminderResourceURIs uri;

  @PathParam("componentInstanceId")
  private String componentInstanceId;
  @PathParam("type")
  private String type;
  @PathParam("localId")
  private String localId;

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
    List<Reminder> reminders =
        process(() -> getByContributionId(getContributionIdentifier())).execute();
    return asWebEntities(reminders);
  }

  /**
   * Creates the reminder from its JSON representation and returns it once created.<br/>
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
    Contribution contribution = getContribution();
    final Reminder reminder;
    if (isDefined(reminderEntity.getDateTime())) {
      reminder = new DateTimeReminder(getContributionIdentifier(), getUser());
    } else {
      reminder = new DurationReminder(getContributionIdentifier(), getUser());
    }
    reminderEntity.mergeInto(reminder);
    final Reminder createdReminder = process(() -> {
      try {
        return save(reminder);
      } catch (IllegalArgumentException e) {
        errorMessage("reminder.add.duration.error", getReminderLabel(reminderEntity),
            getUiMessageContributionLabel(reminderEntity, contribution));
        throw new WebApplicationException(e, Response.Status.PRECONDITION_FAILED);
      }
    }).execute();

    if (createdReminder instanceof DurationReminder) {
      successMessage("reminder.add.duration.success", getReminderLabel(reminderEntity),
          getUiMessageContributionLabel(reminderEntity, contribution));
    }

    return asWebEntity(createdReminder);
  }

  /**
   * Updates the reminder from its JSON representation and returns it once updated.<br/>
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
    final Reminder reminder =
        getByContributionId(getContributionIdentifier()).stream().filter(r -> r.getId().equals(id))
            .findFirst().orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
    reminderEntity.mergeInto(reminder);
    final Reminder updatedReminder = process(() -> {
      try {
        return save(reminder);
      } catch (IllegalArgumentException e) {
        errorMessage("reminder.update.duration.error", getReminderLabel(reminderEntity),
            getUiMessageContributionLabel(reminderEntity, contribution));
        throw new WebApplicationException(e, Response.Status.PRECONDITION_FAILED);
      }
    }).execute();

    if (updatedReminder instanceof DurationReminder) {
      successMessage("reminder.update.duration.success", getReminderLabel(reminderEntity),
          getUiMessageContributionLabel(reminderEntity, contribution));
    }

    return asWebEntity(updatedReminder);
  }

  /**
   * Deletes the given reminder from its JSON representation.<br/>
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
    final Reminder reminder =
        getByContributionId(getContributionIdentifier()).stream().filter(r -> r.getId().equals(id))
            .findFirst().orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
    process(() -> {
      delete(reminder);
      return null;
    }).execute();

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

  protected Contribution getContribution() {
    return ContributionManager.get().getById(getContributionIdentifier()).orElseThrow(
        () -> new WebApplicationException(
            failureOnGetting("contribution", getContributionIdentifier().asString()),
            Response.Status.NOT_FOUND));
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

  // TODO to delete as soon as possible

  private static SilverpeasList<Reminder> getByContributionId(
      final ContributionIdentifier contributionIdentifier) {
    return (SilverpeasList<Reminder>) getDevCache()
        .computeIfAbsent(contributionIdentifier, i -> new SilverpeasArrayList<>());
  }

  private static Reminder save(Reminder reminder) {
    try {
      reminder.schedule();
      if (isNotDefined(reminder.getId())) {
        FieldUtils
            .writeField(reminder, "id", UuidIdentifier.from(UUID.randomUUID().toString()), true);
      }
      final SilverpeasList<Reminder> byContributionId = getByContributionId(reminder.getContributionId());
      final int i = byContributionId.indexOf(reminder);
      if (i >= 0) {
        byContributionId.set(i, reminder);
      } else {
        byContributionId.add(reminder);
      }
      return reminder;
    } catch (IllegalAccessException e) {
      throw new SilverpeasRuntimeException(e);
    }
  }

  private static void delete(Reminder reminder) {
    final SilverpeasList<Reminder> byContributionId =
        getByContributionId(reminder.getContributionId());
    byContributionId.clear();
  }

  @SuppressWarnings("unchecked")
  private static Map<ContributionIdentifier, List<Reminder>> getDevCache() {
    return CacheServiceProvider.getApplicationCacheService().getCache()
        .computeIfAbsent("TEMPORARY_REMINDER_CACHE_KEY", Map.class, HashMap::new);
  }
}
