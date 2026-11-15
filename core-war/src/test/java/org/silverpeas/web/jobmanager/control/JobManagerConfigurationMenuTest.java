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

package org.silverpeas.web.jobmanager.control;

import org.junit.jupiter.api.Test;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.admin.service.Administration;
import org.silverpeas.core.pdc.pdc.service.PdcManager;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.kernel.bundle.LocalizationBundle;
import org.silverpeas.kernel.bundle.ResourceLocator;
import org.silverpeas.kernel.test.annotations.TestManagedMock;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;
import org.silverpeas.web.jobmanager.JobManagerSettings;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@EnableSilverTestEnv(context = JEETestContext.class)
class JobManagerConfigurationMenuTest {

  @TestManagedMock
  private JobManagerSettings settings;
  @TestManagedMock
  private PdcManager pdc;
  @TestManagedMock
  private Administration administration;

  @Test
  void generalAdministratorSeesConfigurationEvenWhenOptionalToolsAreDisabled() {
    var controller = controller(true);
    assertNotNull(controller.getService("5"));
    assertEquals("CONFIGURATION", controller.getService("5").getLabel());
    assertEquals("55", controller.getService("5").getDefaultIdSubService());
    assertTrue(controller.getService("55").getUrl().endsWith("/jobManagerPeas/jsp/configuration.jsp"));
    assertEquals("CONFIGURATION_PROPERTIES", controller.getService("55").getLabel());
    assertNull(controller.getService("4"));
    assertTrue(Arrays.stream(controller.getSubServices("5")).anyMatch(s -> s.getId().equals("55")));
  }

  @Test
  void spaceManagerDoesNotSeeConfiguration() {
    var controller = controller(false);
    assertNull(controller.getService("5"));
    assertNull(controller.getService("55"));
  }

  private JobManagerPeasSessionController controller(boolean administrator) {
    var main = mock(MainSessionController.class);
    var user = mock(UserDetail.class);
    when(user.isAccessAdmin()).thenReturn(administrator);
    when(user.getId()).thenReturn("42");
    when(main.getCurrentUserDetail()).thenReturn(user);
    when(main.getFavoriteLanguage()).thenReturn("fr");
    when(main.getUserManageableSpaceIds()).thenReturn(administrator ? new String[0] : new String[]{"1"});
    var context = mock(ComponentContext.class);
    when(context.getCurrentComponentName()).thenReturn("jobManagerPeas");
    var bundle = mock(LocalizationBundle.class);
    when(bundle.getString(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
    try (var locator = mockStatic(ResourceLocator.class, CALLS_REAL_METHODS)) {
      locator.when(() -> ResourceLocator.getLocalizationBundle(
          "org.silverpeas.jobManagerPeas.multilang.jobManagerPeasBundle", "fr"))
          .thenReturn(bundle);
      return new JobManagerPeasSessionController(main, context);
    }
  }
}
