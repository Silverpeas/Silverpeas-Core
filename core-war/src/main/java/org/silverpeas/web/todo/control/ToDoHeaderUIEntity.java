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
package org.silverpeas.web.todo.control;

import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.space.SpaceInstLight;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.cache.service.CacheServiceProvider;
import org.silverpeas.core.contribution.model.Contribution;
import org.silverpeas.core.notification.user.server.channel.silvermail.SILVERMAILMessage;
import org.silverpeas.core.personalorganizer.model.Attendee;
import org.silverpeas.core.personalorganizer.model.Schedulable;
import org.silverpeas.core.personalorganizer.model.ToDoHeader;
import org.silverpeas.core.util.SilverpeasList;
import org.silverpeas.core.web.util.SelectableUIEntity;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.silverpeas.core.util.StringUtil.isDefined;

/**
 * UI item for a {@link SILVERMAILMessage} instance.
 * @author Yohann Chastagnier
 */
public class ToDoHeaderUIEntity extends SelectableUIEntity<ToDoHeader> {

  private static final String PATH_SEPARATOR = " > ";
  private static final int ATTENDEES_THRESHOLD = 2;

  private final ToDoSessionController ctrl;
  private Map<String, List<Attendee>> attendeeCaches;

  ToDoHeaderUIEntity(final ToDoSessionController ctrl, final ToDoHeader data,
      final Map<String, List<Attendee>> attendeeCaches, final Set<String> selectedIds) {
    super(data, selectedIds);
    this.ctrl = ctrl;
    this.attendeeCaches = attendeeCaches;
  }

  @Override
  public String getId() {
    return String.valueOf(getData().getId());
  }

  /**
   * Converts the given data list into a {@link SilverpeasList} of item wrapping the {@link
   * Contribution}.
   * @param requests the list of {@link ToDoHeader}.
   * @return the {@link SilverpeasList} of {@link ToDoHeaderUIEntity}.
   */
  public static SilverpeasList<ToDoHeaderUIEntity> convertList(
      final ToDoSessionController controller, final SilverpeasList<ToDoHeader> requests,
      final Set<String> selectedIds) {
    final List<String> todoIds =
        requests.stream().map(Schedulable::getId).collect(Collectors.toList());
    final Map<String, List<Attendee>> attendeeCaches = controller.getToDoAttendees(todoIds);
    final Function<ToDoHeader, ToDoHeaderUIEntity> converter =
        c -> new ToDoHeaderUIEntity(controller, c, attendeeCaches, selectedIds);
    return requests.stream().map(converter).collect(SilverpeasList.collector(requests));
  }

  /**
   * Gets the path of the to do.
   * @return a functional path.
   */
  public String getPath() {
    String path = getData().getName();
    if (isDefined(getData().getExternalId())) {
      final String cacheKey =
          getClass().getSimpleName() + "###comp###" + getData().getComponentId();
      final String componentPath = CacheServiceProvider.getRequestCacheService().getCache()
          .computeIfAbsent(cacheKey, String.class, () -> {
            final StringBuilder subSb = new StringBuilder();
            subSb.append(getData().getComponentId());
            final OrganizationController orgCtrl = OrganizationController.get();
            orgCtrl.getComponentInstance(getData().getComponentId()).ifPresent(i -> {
              final SpaceInstLight space = orgCtrl.getSpaceInstLightById(i.getSpaceId());
              subSb.setLength(0);
              if (space != null) {
                subSb.append(space.getName(ctrl.getLanguage())).append(PATH_SEPARATOR);
              }
              subSb.append(i.getLabel(ctrl.getLanguage()));
            });
            return subSb.toString() + PATH_SEPARATOR;
          });
      path = componentPath + path;
    }
    return path;
  }

  /**
   * Gets the label of the priority level.
   * @return the label of priority level.
   */
  public String getPriorityLabel() {
    return ctrl.getString("priorite" + getData().getPriority().getValue());
  }

  /**
   * Gets the attendee label according to the current view type.
   * @return the attendee label.
   */
  public String getAttendeeLabel() {
    final StringBuilder text = new StringBuilder();
    if (ctrl.getViewType() == ToDoSessionController.ORGANIZER_TODO_VIEW) {
      final Collection<Attendee> attendees = getAttendees();
      int countAttendees = 0;
      for (Attendee attendee : attendees) {
        if (countAttendees > 0) {
          text.append("<br/>");
        }
        if (countAttendees > ATTENDEES_THRESHOLD) {
          text.append("...");
          break;
        }
        text.append(getUserLabelById(attendee.getUserId()));
        countAttendees++;
      }
    } else if (getData().getDelegatorId() != null) {
      text.append(getUserLabelById(getData().getDelegatorId()));
    }
    return text.toString();
  }

  /**
   * Gets the list of attendees of the to do.
   * @return a list of {@link Attendee} instances.
   */
  public List<Attendee> getAttendees() {
    return attendeeCaches.computeIfAbsent(getId(), s -> ctrl.getToDoAttendees(getId()));
  }

  /**
   * Gets the label of the priority level.
   * @return the label of priority level.
   */
  public String getPercentCompletedLabel() {
    return getData().getPercentCompleted() == ToDoHeader.PERCENT_UNDEFINED ?
        ctrl.getString("percentUndefined") : getData().getPercentCompleted() + "%";
  }

  /**
   * Gets from cache user label.
   * @param id identifier of a user.
   * @return the label corresponding to the given user identifier.
   */
  private String getUserLabelById(final String id) {
    final String cacheKey = getClass().getSimpleName() + "###user-label###" + id;
    return CacheServiceProvider.getRequestCacheService().getCache()
        .computeIfAbsent(cacheKey, String.class, () -> {
          User user = getUserByIdFromCache(getData().getDelegatorId());
          if (user != null) {
            return user.getLastName() + " " + user.getFirstName();
          }
          return ctrl.getString("utilisateurInconnu");
        });
  }
}
