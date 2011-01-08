/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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