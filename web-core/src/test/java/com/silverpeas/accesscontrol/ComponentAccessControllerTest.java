/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
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

import com.silverpeas.admin.components.Instanciateur;
import com.silverpeas.admin.components.WAComponent;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import java.util.HashMap;

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
    OrganizationController controller = mock(OrganizationController.class);
    String componentIdWithRigths = "kmelia18";
    String componentIdWithoutRigths = "kmelia20";
    String componentId = "yellowpages154";
    mockStatic(Instanciateur.class);
    WAComponent kmeliaComponent = new WAComponent();
    kmeliaComponent.setName("kmelia");
    HashMap<String, String> label = new HashMap<String, String>();
    label.put("en", "kmelia");
    label.put("fr", "kmelia");
    kmeliaComponent.setLabel(label);
    kmeliaComponent.setVisible(true);
    kmeliaComponent.setPortlet(true);
    WAComponent yellowComponent = new WAComponent();
    yellowComponent.setName("yellowpages");
    HashMap<String, String> label2 = new HashMap<String, String>();
    label2.put("en", "yellowpages");
    label2.put("fr", "yellowpages");
    yellowComponent.setLabel(label2);
    yellowComponent.setVisible(true);
    yellowComponent.setPortlet(true);
    when(Instanciateur.getWAComponent("kmelia")).thenReturn(kmeliaComponent);
    when(Instanciateur.getWAComponent("yellowpages")).thenReturn(yellowComponent);

    when(controller.getComponentParameterValue(componentIdWithRigths, "rightsOnTopics")).thenReturn(
        "true");
    when(controller.getComponentParameterValue(componentIdWithoutRigths, "rightsOnTopics")).
        thenReturn("false");

    ComponentAccessController instance = new ComponentAccessController();
    instance.setOrganizationController(controller);
    boolean result = instance.isRightOnTopicsEnabled("", componentId);
    assertEquals(false, result);

    result = instance.isRightOnTopicsEnabled("", componentIdWithoutRigths);
    assertEquals(false, result);

    result = instance.isRightOnTopicsEnabled("", componentIdWithRigths);
    assertEquals(true, result);
  }

  /**
   * Test of isUserAuthorized method, of class ComponentAccessController.
   * @throws Exception
   */
  @Test
  public void testIsUserAuthorized() throws Exception {
    String componentId = "kmelia18";
    String publicComponentId = "kmelia20";
    String forbiddenComponent = "yellowpages154";

    String userId = "bart";

    OrganizationController controller = mock(OrganizationController.class);
    when(controller.isComponentAvailable(componentId, userId)).thenReturn(Boolean.TRUE);
    when(controller.isComponentAvailable(forbiddenComponent, userId)).thenReturn(Boolean.FALSE);

    mockStatic(Instanciateur.class);

    WAComponent kmeliaComponent = new WAComponent();
    kmeliaComponent.setName("kmelia");
    HashMap<String, String> label = new HashMap<String, String>();
    label.put("en", "kmelia");
    label.put("fr", "kmelia");
    kmeliaComponent.setLabel(label);
    kmeliaComponent.setVisible(true);
    kmeliaComponent.setPortlet(true);
    WAComponent yellowComponent = new WAComponent();
    yellowComponent.setName("yellowpages");
    HashMap<String, String> label2 = new HashMap<String, String>();
    label2.put("en", "yellowpages");
    label2.put("fr", "yellowpages");
    yellowComponent.setLabel(label2);
    yellowComponent.setVisible(true);
    yellowComponent.setPortlet(true);
    when(Instanciateur.getWAComponent("kmelia")).thenReturn(kmeliaComponent);
    when(Instanciateur.getWAComponent("yellowpages")).thenReturn(yellowComponent);

    when(controller.getComponentParameterValue(publicComponentId, "publicFiles")).thenReturn(
        "true");
    when(controller.getComponentParameterValue(componentId, "rightsOnTopics")).
        thenReturn("false");
    when(controller.getComponentParameterValue(forbiddenComponent, "rightsOnTopics")).
        thenReturn("false");

    ComponentAccessController instance = new ComponentAccessController();
    instance.setOrganizationController(controller);
    boolean result = instance.isUserAuthorized(userId, null);
    assertEquals(true, result);

    result = instance.isUserAuthorized(userId, publicComponentId);
    assertEquals(true, result);

    result = instance.isUserAuthorized(userId, componentId);
    assertEquals(true, result);


    result = instance.isUserAuthorized(userId, forbiddenComponent);
    assertEquals(false, result);
  }
}