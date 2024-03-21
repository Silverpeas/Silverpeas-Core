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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.web.usercalendar.security.authorization;

import org.silverpeas.core.admin.component.model.SilverpeasPersonalComponentInstance;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.calendar.Attendee;
import org.silverpeas.core.calendar.CalendarEvent;
import org.silverpeas.core.security.authorization.AccessControlContext;
import org.silverpeas.core.security.authorization.AccessControlOperation;
import org.silverpeas.core.security.authorization.ComponentAccessController;
import org.silverpeas.core.security.authorization.DefaultInstanceAccessControlExtension;

import javax.inject.Named;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;

/**
 * This implementation is dedicated to {@link SilverpeasPersonalComponentInstance} of user
 * calendar type.
 * <p>
 *   The default verification process is performed first, then if no role has been computed,
 *   the operation is OK and a {@link CalendarEvent} provider has been set in the context,
 *   the process checks whether the user is a participant. If so, {@link SilverpeasRole#USER} is
 *   granted.
 * </p>
 * @author silveryocha
 */
@Named
@Service
public class UserCalendarInstanceAccessControlExtension
    extends DefaultInstanceAccessControlExtension {

  static final String CALENDAR_EVENT_INSTANCE_SUPPLIER = "UserCalendarCalendarEventInstanceSupplier";

  @Override
  public boolean fillUserRolesFromComponentInstance(
      final ComponentAccessController.DataManager dataManager, final User user,
      final String componentId, final AccessControlContext context,
      final Set<SilverpeasRole> userRoles) {
    boolean fillFlag = super.fillUserRolesFromComponentInstance(dataManager, user, componentId,
        context, userRoles);
    if (userRoles.isEmpty() && isOperationValidAsAttendee(context)) {
      ofNullable(context.get(CALENDAR_EVENT_INSTANCE_SUPPLIER, Supplier.class))
          .map(Supplier::get)
          .map(CalendarEvent.class::cast)
          .stream()
          .flatMap(e -> Stream.concat(e.getAttendees().stream(),
              e.getPersistedOccurrences().stream().flatMap(o -> o.getAttendees().stream())))
          .map(Attendee::getId)
          .filter(user.getId()::equals)
          .findAny()
          .ifPresent(e -> userRoles.add(SilverpeasRole.USER));
      fillFlag |= !userRoles.isEmpty();
    }
    return fillFlag;
  }

  @Override
  protected boolean canAnonymousAccessInstance(final AccessControlContext context) {
    return false;
  }

  private boolean isOperationValidAsAttendee(final AccessControlContext context) {
    final Set<AccessControlOperation> operations = context.getOperations();
    return !AccessControlOperation.isPersistActionFrom(operations);
  }
}
