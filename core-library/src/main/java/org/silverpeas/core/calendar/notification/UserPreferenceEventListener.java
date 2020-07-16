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
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
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

import org.silverpeas.core.admin.component.model.PersonalComponent;
import org.silverpeas.core.admin.component.model.PersonalComponentInstance;
import org.silverpeas.core.annotation.Bean;
import org.silverpeas.core.calendar.Calendar;
import org.silverpeas.core.notification.system.CDIResourceEventListener;
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.personalization.UserPreferences;
import org.silverpeas.core.personalization.notification.UserPreferenceEvent;

/**
 * @author Yohann Chastagnier
 */
@Bean
public class UserPreferenceEventListener extends CDIResourceEventListener<UserPreferenceEvent> {

  @Override
  public void onUpdate(final UserPreferenceEvent event) throws Exception {
    UserPreferences previous = event.getTransition().getBefore();
    UserPreferences current = event.getTransition().getAfter();
    if (!previous.getZoneId().equals(current.getZoneId())) {
      Transaction.performInOne(() -> {
        Calendar.getByComponentInstanceId(PersonalComponentInstance
            .from(current.getUser(), PersonalComponent.getByName("userCalendar").orElse(null)).getId())
            .forEach(calendar -> {
              calendar.setZoneId(current.getZoneId());
              calendar.save();
            });
        return null;
      });
    }
  }
}
