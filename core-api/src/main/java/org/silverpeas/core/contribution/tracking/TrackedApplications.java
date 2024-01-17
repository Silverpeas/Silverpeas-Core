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
 * FLOSS exception. You should have received a copy of the text describing
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

package org.silverpeas.core.contribution.tracking;

import org.silverpeas.core.admin.component.model.SilverpeasComponentInstance;

import java.util.Arrays;
import java.util.stream.Stream;

/**
 * It defines the applications in Silverpeas for which the tracking of modifications on
 * contributions are enabled.
 * @author mmoquillon
 */
public class TrackedApplications {

  private static final String ALL_APPS = "all";
  private static final String NO_APPS = "none";

  public static final TrackedApplications NONE = new TrackedApplications(NO_APPS);
  public static final TrackedApplications ALL = new TrackedApplications(ALL_APPS);

  public static TrackedApplications track(String... apps) {
    if (apps == null || apps.length == 0 ||
        (apps.length == 1 && apps[0].equalsIgnoreCase(NO_APPS))) {
      return NONE;
    }
    if (apps.length == 1 && apps[0].equalsIgnoreCase(ALL_APPS)) {
      return ALL;
    }
    return new TrackedApplications(apps);
  }

  private final String[] apps;

  private TrackedApplications(String... apps) {
    this.apps = Stream.of(apps)
        .distinct()
        .map(String::trim)
        .map(String::toLowerCase)
        .sorted(String::compareTo)
        .toArray(String[]::new);
    if (apps.length > 1 &&
        (contains(apps, ALL_APPS) || contains(apps, NO_APPS) || contains(apps, ""))) {
      throw new IllegalArgumentException("Either passes application names or NONE or ALL");
    }
  }

  public boolean isTracked(final String appId) {
    String appName = SilverpeasComponentInstance.getComponentName(appId);
    return apps != null && (this.apps[0].equals(ALL_APPS) || contains(apps, appName));
  }

  private boolean contains(String[] apps, String app) {
    return Arrays.binarySearch(apps, app, String::compareToIgnoreCase) >= 0;
  }
}
