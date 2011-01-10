/*
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.accesscontrol;

import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.instance.control.Instanciateur;
import com.stratelia.webactiv.beans.admin.instance.control.WAComponent;

import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 *
 * @author ehugonnet
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({Instanciateur.class})
public class ComponentAccessControllerTest {

  public ComponentAccessControllerTest() {
  }

  /**
   * Test of isRightOnTopicsEnabled method, of class ComponentAccessController.
   */
  @Test
  public void testIsRightOnTopicsEnabled() {
    MainSessionController controller = mock(MainSessionController.class);
    String componentIdWithRigths = "kmelia18";
    String componentIdWithoutRigths = "kmelia20";
    String componentId = "yellowpages154";
    mockStatic(Instanciateur.class);
    WAComponent kmeliaComponent = new WAComponent("kmelia", "kmelia", "kmelia", "kmelia", true, true,
        null, null, null);
    WAComponent yellowComponent = new WAComponent("yellowpages", "yellowpages", "yellowpages",
        "yellowpages", true, true, null, null, null);
    when(Instanciateur.getWAComponent("kmelia")).thenReturn(kmeliaComponent);
    when(Instanciateur.getWAComponent("yellowpages")).thenReturn(yellowComponent);

    when(controller.getComponentParameterValue(componentIdWithRigths, "rightsOnTopics")).thenReturn(
        "true");
    when(controller.getComponentParameterValue(componentIdWithoutRigths, "rightsOnTopics")).
        thenReturn("false");

    ComponentAccessController instance = new ComponentAccessController();
    boolean result = instance.isRightOnTopicsEnabled(controller, componentId);
    assertEquals(false, result);

    result = instance.isRightOnTopicsEnabled(controller, componentIdWithoutRigths);
    assertEquals(false, result);

    result = instance.isRightOnTopicsEnabled(controller, componentIdWithRigths);
    assertEquals(true, result);
  }

  /**
   * Test of isUserAuthorized method, of class ComponentAccessController.
   */
  @Test
  public void testIsUserAuthorized() throws Exception {
    String componentId = "kmelia18";
    String publicComponentId = "kmelia20";
    String forbiddenComponent = "yellowpages154";
    
    String userId = "bart";
    
    OrganizationController organizationController = mock(OrganizationController.class);
    when(organizationController.isComponentAvailable(componentId, userId)).thenReturn(Boolean.TRUE);
    when(organizationController.isComponentAvailable(forbiddenComponent, userId)).thenReturn(Boolean.FALSE);
    
    MainSessionController controller = mock(MainSessionController.class);
    when(controller.getOrganizationController()).thenReturn(organizationController);
    when(controller.getUserId()).thenReturn(userId);
    
    mockStatic(Instanciateur.class);
    WAComponent kmeliaComponent = new WAComponent("kmelia", "kmelia", "kmelia", "kmelia", true, true,
        null, null, null);
    WAComponent yellowComponent = new WAComponent("yellowpages", "yellowpages", "yellowpages",
        "yellowpages", true, true, null, null, null);
    when(Instanciateur.getWAComponent("kmelia")).thenReturn(kmeliaComponent);
    when(Instanciateur.getWAComponent("yellowpages")).thenReturn(yellowComponent);

    when(controller.getComponentParameterValue(publicComponentId, "publicFiles")).thenReturn(
        "true");
    when(controller.getComponentParameterValue(componentId, "rightsOnTopics")).
        thenReturn("false");
    when(controller.getComponentParameterValue(forbiddenComponent, "rightsOnTopics")).
        thenReturn("false");

    ComponentAccessController instance = new ComponentAccessController();
    boolean result = instance.isUserAuthorized(controller, null);
    assertEquals(true, result);

    result = instance.isUserAuthorized(controller, publicComponentId);
    assertEquals(true, result);

    result = instance.isUserAuthorized(controller, componentId);
    assertEquals(true, result);
    
    
    result = instance.isUserAuthorized(controller, forbiddenComponent);
    assertEquals(false, result);
  }
}