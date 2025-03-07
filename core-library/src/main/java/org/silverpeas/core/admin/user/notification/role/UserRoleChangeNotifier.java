/*
 * Copyright (C) 2000 - 2025 Silverpeas
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

package org.silverpeas.core.admin.user.notification.role;

import org.silverpeas.core.annotation.Bean;

import javax.enterprise.event.Event;
import javax.inject.Inject;

/**
 * Bean in charge to notify a user or a user group doesn't play anymore a role in one or several
 * component instances. This service is used by the more business services in charges to be
 * informed about role change of users in order to notify all others business services for which
 * this information is important.
 *
 * @author mmoquillon
 */
@Bean
class UserRoleChangeNotifier {

  @Inject
  private Event<UserRoleEvent> notifEvent;

  public void notify(final UserRoleEvent userRoleEvent) {
    notifEvent.fire(userRoleEvent);
  }

}
  