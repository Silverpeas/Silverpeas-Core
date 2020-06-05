/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
package org.silverpeas.core.calendar.notification;

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.contribution.model.Contribution;
import org.silverpeas.core.contribution.model.LocalizedContribution;
import org.silverpeas.core.notification.user.UserSubscriptionNotificationBehavior;
import org.silverpeas.core.notification.user.client.constant.NotifAction;
import org.silverpeas.core.subscription.constant.SubscriberType;
import org.silverpeas.core.subscription.util.SubscriptionSubscriberMapBySubscriberType;
import org.silverpeas.core.template.SilverpeasTemplate;

import java.util.Collection;
import java.util.Collections;

import static org.silverpeas.core.subscription.service.ResourceSubscriptionProvider.getSubscribersOfComponent;

/**
 * A builder of notifications to subscribers with a subscription about a calendar component to
 * inform them about some changes in the component.
 * @author silveryocha
 */
class SubscriberNotificationBuilder extends AbstractCalendarEventUserNotificationBuilder
    implements UserSubscriptionNotificationBehavior {

  private final SubscriptionSubscriberMapBySubscriberType subscriberIdsByTypes;
  private Collection<String> userIdsToExclude = Collections.emptyList();

  /**
   * Constructs a new builder of user notification against the subscriber(s) of a calendar
   * component.
   * @param calendarComponent the calendar component concerned by the notification.
   * @param action the action that was performed onto the event.
   */
  SubscriberNotificationBuilder(final Contribution calendarComponent, final NotifAction action) {
    super(calendarComponent, action);
    subscriberIdsByTypes = getSubscribersOfComponent(getComponentInstanceId()).indexBySubscriberType();
  }

  @Override
  public SubscriberNotificationBuilder from(final User sender) {
    return (SubscriberNotificationBuilder) super.from(sender);
  }

  @Override
  public SubscriberNotificationBuilder immediately() {
    return (SubscriberNotificationBuilder) super.immediately();
  }

  @Override
  public SubscriberNotificationBuilder about(final CalendarOperation operation) {
    return (SubscriberNotificationBuilder) super.about(operation);
  }

  public SubscriberNotificationBuilder excludingUsersIds(final Collection<String> userIdsToExclude) {
    this.userIdsToExclude = userIdsToExclude;
    return this;
  }

  @Override
  protected String getBundleSubjectKey() {
    if (getOperation() == CalendarOperation.EVENT_CREATE) {
      return "subject.eventCreate";
    } else if (getOperation() == CalendarOperation.EVENT_UPDATE ||
               getOperation() == CalendarOperation.SINCE_EVENT_UPDATE) {
      return "subject.eventUpdate";
    } else  if (getOperation() == CalendarOperation.EVENT_DELETION ||
        getOperation() == CalendarOperation.SINCE_EVENT_DELETION) {
      return "subject.eventDelete";
    } else {
      return "subject.default";
    }
  }

  @Override
  protected void performTemplateData(final Contribution contribution,
      final SilverpeasTemplate template) {
    super.performTemplateData(contribution, template);
    final String language = ((LocalizedContribution) contribution).getLanguage();
  }

  @Override
  protected Collection<String> getUserIdsToNotify() {
    return subscriberIdsByTypes.get(SubscriberType.USER).getAllIds();
  }

  @Override
  protected Collection<String> getGroupIdsToNotify() {
    return subscriberIdsByTypes.get(SubscriberType.GROUP).getAllIds();
  }

  @Override
  protected Collection<String> getUserIdsToExcludeFromNotifying() {
    return this.userIdsToExclude;
  }
}
