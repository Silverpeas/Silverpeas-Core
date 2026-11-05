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
package org.silverpeas.core.admin.service;

import org.junit.jupiter.api.Test;
import org.silverpeas.core.admin.component.ComponentInstanceLimitException;
import org.silverpeas.core.admin.component.model.ComponentInst;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.kernel.test.annotations.TestManagedMock;
import org.silverpeas.kernel.test.annotations.TestedBean;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@EnableSilverTestEnv(context = JEETestContext.class)
class AdminControllerCapacityTest {
  @TestManagedMock private Administration admin;
  @TestedBean private AdminController controller;

  @Test
  void aNestedCapacityFailureReachesTheCreationUiWithoutBeingSilentlySwallowed() throws Exception {
    var component = new ComponentInst(); component.setCreatorUserId("2");
    var limit = new ComponentInstanceLimitException(1, true);
    when(admin.addComponentInst("2",component)).thenThrow(new AdminException("Creation failed",
        new AdminException("Persistence failed",limit)));
    assertSame(limit,assertThrows(ComponentInstanceLimitException.class,()->controller.addComponentInst(component)));
  }

  @Test
  void unknownUsersDoNotCauseAnUncheckedDereferenceOrReceiveManageableSpaces() {
    assertEquals(0,controller.getUserManageableSpaceClientIds("missing").length);
  }
}
