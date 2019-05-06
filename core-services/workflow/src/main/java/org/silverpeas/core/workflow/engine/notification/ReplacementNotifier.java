/*
 * Copyright (C) 2000 - 2018 Silverpeas
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

package org.silverpeas.core.workflow.engine.notification;

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.date.TemporalConverter;
import org.silverpeas.core.notification.system.CDIAfterSuccessfulTransactionResourceEventListener;
import org.silverpeas.core.notification.user.client.constant.NotifAction;
import org.silverpeas.core.workflow.api.user.Replacement;
import org.silverpeas.core.workflow.engine.user.ReplacementEvent;

import java.time.LocalDate;

/**
 * Notifier to the concerned users about an event occurring on a replacement. For example, about
 * the creation of a replacement between them or the deletion of an existing replacement.
 * @author mmoquillon
 */
public class ReplacementNotifier
    extends CDIAfterSuccessfulTransactionResourceEventListener<ReplacementEvent> {

  @Override
  public void onDeletion(final ReplacementEvent event) {
    Replacement replacement = event.getTransition().getBefore();
    notifyUsersIfEndInFuture(NotifAction.DELETE, replacement);
  }

  @Override
  public void onUpdate(final ReplacementEvent event) {
    Replacement previous = event.getTransition().getBefore();
    Replacement replacement = event.getTransition().getAfter();
    if (!previous.isSameAs(replacement)) {
      if (!replacement.getSubstitute().getUserId().equals(previous.getSubstitute().getUserId())) {
        notifyUsersIfEndInFuture(NotifAction.DELETE, previous);
        notifyUsersIfEndInFuture(NotifAction.CREATE, replacement);
      } else {
        notifyUsers(NotifAction.UPDATE, replacement);
      }
    }
  }

  @Override
  public void onCreation(final ReplacementEvent event) {
    Replacement replacement = event.getTransition().getAfter();
    notifyUsersIfEndInFuture(NotifAction.CREATE, replacement);
  }

  private void notifyUsersIfEndInFuture(final NotifAction action, final Replacement replacement) {
    final LocalDate now = LocalDate.now();
    final LocalDate endDate = TemporalConverter
        .asLocalDate(replacement.getPeriod().getEndDate())
        .minusDays(1);
    if (now.isBefore(endDate) || now.equals(endDate)) {
      notifyUsers(action, replacement);
    }
  }

  private void notifyUsers(final NotifAction action, final Replacement replacement) {
    final User currentRequester = User.getCurrentRequester();
    if (!currentRequester.getId().equals(replacement.getIncumbent().getUserId())) {
      new ToIncumbentReplacementNotificationBuilder(replacement, action).build().send();
    }
    if (!currentRequester.getId().equals(replacement.getSubstitute().getUserId())) {
      new ToSubstituteReplacementNotificationBuilder(replacement, action).build().send();
    }
  }
}
  