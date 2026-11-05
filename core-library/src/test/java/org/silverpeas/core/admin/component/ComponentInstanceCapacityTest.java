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

import java.sql.Connection;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.admin.component.dao.ComponentInstanceCapacityDAO;
import org.silverpeas.core.admin.component.model.WAComponent;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.kernel.test.annotations.TestManagedMock;
import org.silverpeas.kernel.test.annotations.TestedBean;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@EnableSilverTestEnv(context = JEETestContext.class)
class ComponentInstanceCapacityTest {
  @TestManagedMock private WAComponentRegistry registry;
  @TestManagedMock private ComponentInstanceCapacityDAO dao;
  @TestedBean private ComponentInstanceCapacity capacity;

  @Test
  void unlimitedAndLegacyDescriptorsDoNotAcquireLocksOrChangeExistingCreationBehaviour() {
    when(registry.getWAComponent("legacy")).thenReturn(Optional.of(new WAComponent()));
    try (var database = mockStatic(DBUtil.class)) {
      capacity.verifyAddition("legacy", 1, -1);
      database.verifyNoInteractions();
      verifyNoInteractions(dao);
    }
  }

  @Test
  void capacityUsesTheTargetSpaceAndExcludesTheInstanceBeingMovedOrRestored() throws Exception {
    var descriptor = new WAComponent();
    descriptor.setMaxInstancesPerSpace(2);
    when(registry.getWAComponent("app")).thenReturn(Optional.of(descriptor));
    var connection = mock(Connection.class);
    try (var database = mockStatic(DBUtil.class)) {
      database.when(DBUtil::openConnection).thenReturn(connection);
      when(dao.lockAndCount(connection, 12, "app", 33)).thenReturn(1);
      capacity.verifyAddition("app", 12, 33);
      when(dao.lockAndCount(connection, 12, "app", 33)).thenReturn(2);
      assertEquals(2, assertThrows(ComponentInstanceLimitException.class,
          () -> capacity.verifyAddition("app", 12, 33)).getMaximum());
      verify(connection, times(2)).close();
    }
  }
  @Test
  void aPlatformLimitIsSharedAcrossSpacesButDoesNotPreventMovingAnExistingInstance() throws Exception {
    var descriptor = new WAComponent(); descriptor.setMaxInstancesPerPlatform(1);
    when(registry.getWAComponent("global")).thenReturn(Optional.of(descriptor));
    var connection = mock(Connection.class);
    try (var database = mockStatic(DBUtil.class)) {
      database.when(DBUtil::openConnection).thenReturn(connection);
      when(dao.lockAndCountPlatform(connection, "global", -1)).thenReturn(1);
      assertTrue(assertThrows(ComponentInstanceLimitException.class,
          () -> capacity.verifyAddition("global", 10, -1)).isPlatform());
      assertTrue(assertThrows(ComponentInstanceLimitException.class,
          () -> capacity.verifyAddition("global", 20, -1)).isPlatform());
      capacity.verifyMove("global", 20, 12);
      verify(dao, never()).lockAndCountPlatform(connection,"global",12);
    }
  }

}
