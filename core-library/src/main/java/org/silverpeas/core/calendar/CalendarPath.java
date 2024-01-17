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

package org.silverpeas.core.calendar;

import org.silverpeas.core.NotFoundException;
import org.silverpeas.core.admin.component.model.ComponentInstPath;
import org.silverpeas.core.admin.component.model.SilverpeasComponentInstance;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.space.model.SpacePath;
import org.silverpeas.core.util.ResourcePath;

import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * Path of a {@link CalendarComponent} instance.
 * @author silveryocha
 */
public class CalendarPath extends ArrayList<Calendar> {
  private static final long serialVersionUID = 6013424875452769996L;

  /**
   * Gets the path of the specified calendar instance.
   * @param calendar the calendar instance.
   * @return a {@link CalendarPath} instance.
   */
  public static CalendarPath getPath(final Calendar calendar) {
    CalendarPath path = new CalendarPath();
    path.add(calendar);
    return path;
  }

  private CalendarPath() {
    super(1);
  }

  public String format(final String language) {
    return format(language, false);
  }

  public String format(final String language, final boolean absolutePath) {
    return format(language, absolutePath, ResourcePath.Constants.DEFAULT_SEPARATOR);
  }

  public String format(final String language, final boolean absolutePath, final String pathSep) {
    if (isEmpty()) {
      return "";
    }
    Calendar calendar = get(0);
    if (calendar.isMain()) {
      ComponentInstPath instancePath = ComponentInstPath.getPath(calendar.getComponentInstanceId());
      return instancePath.format(language, absolutePath, pathSep);
    } else {
      final String spaceId = OrganizationController.get()
          .getComponentInstance(calendar.getComponentInstanceId())
          .map(SilverpeasComponentInstance::getSpaceId)
          .orElseThrow(() -> new NotFoundException(
              "space not found from instance id " + calendar.getComponentInstanceId()));
      SpacePath spacePath = SpacePath.getPath(spaceId);
      return spacePath.format(language, absolutePath, pathSep) + pathSep +
          stream().map(Calendar::getTitle)
              .collect(Collectors.joining(pathSep));
    }
  }
}
