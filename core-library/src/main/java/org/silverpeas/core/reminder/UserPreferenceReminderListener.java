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

package org.silverpeas.core.reminder;

import org.silverpeas.core.annotation.Bean;
import org.silverpeas.core.notification.system.CDIResourceEventListener;
import org.silverpeas.core.personalization.UserPreferences;
import org.silverpeas.core.personalization.notification.UserPreferenceEvent;
import org.silverpeas.core.util.logging.SilverLogger;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Yohann Chastagnier
 */
@Bean
public class UserPreferenceReminderListener extends CDIResourceEventListener<UserPreferenceEvent> {

  @Override
  public void onUpdate(final UserPreferenceEvent event) {
    try {
      UserPreferences previous = event.getTransition().getBefore();
      UserPreferences current = event.getTransition().getAfter();
      if (!previous.getZoneId().equals(current.getZoneId())) {
        final List<Reminder> toUnschedule = new ArrayList<>();
        Reminder.getByUser(current.getUser()).stream()
            .filter(Reminder::isScheduled)
            .forEach(r -> {
              try {
                r.schedule();
              } catch (IllegalArgumentException | IllegalStateException e) {
                toUnschedule.add(r);
                SilverLogger.getLogger(this).warn(e);
              }
            });
        toUnschedule.forEach(r -> r.unschedule(false));
      }
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e);
    }
  }
}
