/*
 * Copyright (C) 2000 - 2026 Silverpeas
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
package org.silverpeas.core.admin.component.model;

import java.util.List;

/** Read-only catalogue information; authoritative enforcement always happens at write time. */
public record ComponentInstanceAvailability(boolean available, int maximum, String existingInstanceId, boolean platform) {
  public boolean isPlatform() { return platform; }
  public boolean isAvailable() { return available; }
  public int getMaximum() { return maximum; }
  public String getExistingInstanceId() { return existingInstanceId; }

  public static ComponentInstanceAvailability from(WAComponent descriptor, List<ComponentInst> directInstances) {
    return from(descriptor, directInstances, List.of());
  }

  public static ComponentInstanceAvailability from(WAComponent descriptor, List<ComponentInst> directInstances,
      List<String> platformInstances) {
    int platformMaximum = descriptor.getMaxInstancesPerPlatform();
    if (platformMaximum > 0 && platformInstances.size() >= platformMaximum) {
      return new ComponentInstanceAvailability(false, platformMaximum,
          platformMaximum == 1 && platformInstances.size() == 1 ? platformInstances.getFirst() : "", true);
    }
    int maximum = descriptor.getMaxInstancesPerSpace();
    if (maximum == 0) { return new ComponentInstanceAvailability(true, platformMaximum, "", platformMaximum > 0); }
    var matches = directInstances.stream().filter(instance -> instance.getStatus() == null
        && descriptor.getName().equals(instance.getName())).toList();
    return new ComponentInstanceAvailability(matches.size() < maximum, maximum,
        maximum == 1 && matches.size() == 1 ? matches.getFirst().getId() : "", false);
  }
}
