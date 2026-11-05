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
package org.silverpeas.core.admin.component;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.sql.SQLException;
import org.silverpeas.core.admin.component.dao.ComponentInstanceCapacityDAO;
import org.silverpeas.core.admin.component.model.WAComponent;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.kernel.SilverpeasRuntimeException;

/** Generic descriptor-driven limit; all writers must call this before changing active membership. */
@Service
@Transactional(Transactional.TxType.MANDATORY)
public class ComponentInstanceCapacity {
  @Inject
  private WAComponentRegistry registry;
  @Inject
  private ComponentInstanceCapacityDAO dao;

  public void verifyAddition(String component, int spaceId, int excludedId) {
    verify(component, spaceId, excludedId, true);
  }

  public void verifyMove(String component, int spaceId, int instanceId) {
    // An active instance moving between spaces does not consume an additional platform slot.
    verify(component, spaceId, instanceId, false);
  }

  private void verify(String component, int spaceId, int excludedId, boolean addingToPlatform) {
    var descriptor = registry.getWAComponent(component).orElse(null);
    int maximum = descriptor == null ? 0 : descriptor.getMaxInstancesPerSpace();
    int platformMaximum = descriptor == null || !addingToPlatform ? 0 : descriptor.getMaxInstancesPerPlatform();
    if (maximum == 0 && platformMaximum == 0) { return; }
    try (var connection = DBUtil.openConnection()) {
      // All writers acquire the platform lock before any destination space lock.
      if (platformMaximum > 0 && dao.lockAndCountPlatform(connection, component, excludedId) >= platformMaximum) {
        throw new ComponentInstanceLimitException(platformMaximum, true);
      }
      if (maximum > 0 && dao.lockAndCount(connection, spaceId, component, excludedId) >= maximum) {
        throw new ComponentInstanceLimitException(maximum);
      }
    } catch (SQLException error) {
      throw new SilverpeasRuntimeException("Cannot verify application instance capacity", error);
    }
  }

  @Transactional(Transactional.TxType.SUPPORTS)
  public java.util.List<String> platformInstanceIds(String component) {
    try (var connection = DBUtil.openConnection()) {
      return dao.platformInstanceIds(connection, component);
    } catch (SQLException error) {
      throw new SilverpeasRuntimeException("Cannot read application instance capacity", error);
    }
  }
}
