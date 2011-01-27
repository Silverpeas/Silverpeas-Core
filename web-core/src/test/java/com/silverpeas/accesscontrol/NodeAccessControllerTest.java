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

import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.node.control.NodeBmHome;
import com.stratelia.webactiv.util.node.model.NodePK;
import com.stratelia.webactiv.util.EJBUtilitaire;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.webactiv.beans.admin.ObjectType;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.util.node.control.NodeBm;
import com.stratelia.webactiv.util.node.model.NodeDetail;
import org.junit.Test;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.hamcrest.Matchers.*;
/**
 *
 * @author ehugonnet
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({EJBUtilitaire.class})
public class NodeAccessControllerTest {

    public NodeAccessControllerTest() {
    }


  /**
   * Test of isUserAuthorized method, of class NodeAccessController.
   */
  @Test
  public void userIsAuthorizedWhenNodeHaveNoSpecificRights() throws Exception {
    NodePK nodPk = new NodePK("10");
    NodeDetail node = new NodeDetail();
    node.setNodePK(nodPk);
    mockStatic(EJBUtilitaire.class);
    NodeBmHome home= mock(NodeBmHome.class);
    NodeBm nodeBm = mock(NodeBm.class);
    when(home.create()).thenReturn(nodeBm);
    when(EJBUtilitaire.getEJBObjectRef(JNDINames.NODEBM_EJBHOME, NodeBmHome.class)).thenReturn(home);
    when(nodeBm.getHeader(nodPk, false)).thenReturn(node);
    MainSessionController controller = mock(MainSessionController.class);
    String componentId = "";
    NodeAccessController instance = new NodeAccessController();
    boolean result = instance.isUserAuthorized(controller, componentId, nodPk);
    assertThat(result, is(true));
  }
  
  /**
   * Test of isUserAuthorized method, of class NodeAccessController.
   */
  @Test
  public void userIsAuthorizedWhithRightsDependOn() throws Exception {
    NodePK nodPk = new NodePK("10");
     String componentId = "test";
    NodeDetail node = new NodeDetail();
    node.setNodePK(nodPk);
    node.setRightsDependsOn(5);
    mockStatic(EJBUtilitaire.class);
    NodeBmHome home= mock(NodeBmHome.class);
    NodeBm nodeBm = mock(NodeBm.class);
    when(home.create()).thenReturn(nodeBm);
    when(EJBUtilitaire.getEJBObjectRef(JNDINames.NODEBM_EJBHOME, NodeBmHome.class)).thenReturn(home);
    when(nodeBm.getHeader(nodPk, false)).thenReturn(node);
    MainSessionController controller = mock(MainSessionController.class);
    when(controller.getUserId()).thenReturn("5");
    OrganizationController orga = mock(OrganizationController.class);
    when(orga.isObjectAvailable(5, ObjectType.NODE, componentId, "5")).thenReturn(true);
    when(controller.getOrganizationController()).thenReturn(orga);
    NodeAccessController instance = new NodeAccessController();
    boolean result = instance.isUserAuthorized(controller, componentId, nodPk);
    assertThat(result, is(true));
    verify(orga, times(1)).isObjectAvailable(5, ObjectType.NODE, componentId, "5");
  }
  
  /**
   * Test of isUserAuthorized method, of class NodeAccessController.
   */
  @Test
  public void userIsNotAuthorizedWhithRightsDependOn() throws Exception {
    NodePK nodPk = new NodePK("10");
     String componentId = "test";
    NodeDetail node = new NodeDetail();
    node.setNodePK(nodPk);
    node.setRightsDependsOn(5);
    mockStatic(EJBUtilitaire.class);
    NodeBmHome home= mock(NodeBmHome.class);
    NodeBm nodeBm = mock(NodeBm.class);
    when(home.create()).thenReturn(nodeBm);
    when(EJBUtilitaire.getEJBObjectRef(JNDINames.NODEBM_EJBHOME, NodeBmHome.class)).thenReturn(home);
    when(nodeBm.getHeader(nodPk, false)).thenReturn(node);
    MainSessionController controller = mock(MainSessionController.class);
    when(controller.getUserId()).thenReturn("5");
    OrganizationController orga = mock(OrganizationController.class);
    when(orga.isObjectAvailable(5, ObjectType.NODE, componentId, "5")).thenReturn(false); 
    when(controller.getOrganizationController()).thenReturn(orga);
    NodeAccessController instance = new NodeAccessController();
    boolean result = instance.isUserAuthorized(controller, componentId, nodPk);
    assertThat(result, is(false));
    verify(orga, times(1)).isObjectAvailable(5, ObjectType.NODE, componentId, "5");

  }

  

}