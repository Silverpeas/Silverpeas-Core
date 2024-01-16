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

package org.silverpeas.web.usercalendar;

import org.silverpeas.core.admin.component.model.PersonalComponent;
import org.silverpeas.core.admin.component.model.PersonalComponentInstance;
import org.silverpeas.core.admin.component.model.SilverpeasComponentInstance;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.web.calendar.AbstractCalendarInstanceRoutingMap;

import javax.inject.Named;

/**
 * @author silveryocha
 */
@Named
public class UserCalendarInstanceRoutingMap extends AbstractCalendarInstanceRoutingMap {

  private String baseForPages;

  @Override
  protected String getBaseForPages() {
    if (baseForPages == null) {
      final User user = User.getCurrentRequester();
      if (user != null) {
        // always redirecting the current user on its own instance
        SilverpeasComponentInstance instance = PersonalComponentInstance.from(user,
            PersonalComponent.getByName(UserCalendarSettings.COMPONENT_NAME).orElse(null));
        baseForPages = "/R" + instance.getName() + "/" + instance.getId();
      } else {
        baseForPages = super.getBaseForPages();
      }
    }
    return baseForPages;
  }
}
