/**
 * Copyright (C) 2000 - 2013 Silverpeas
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
import org.junit.Before;
import org.silverpeas.core.admin.OrganisationController;

import com.stratelia.webactiv.beans.admin.ObjectType;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.node.control.NodeBm;
import com.stratelia.webactiv.util.node.model.NodeDetail;
import com.stratelia.webactiv.util.node.model.NodePK;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 *
 * @author ehugonnet
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(EJBUtilitaire.class)
public class NodeAccessControllerTest {

  private final String userId = "5";
  private final String componentId = "kmelia18";

  public NodeAccessControllerTest() {
  }

  @Before
  public void setUp() {
    WAComponent kmelia = Mockito.mock(WAComponent.class);
    Mockito.when(kmelia.getName()).thenReturn("kmelia");
    Instanciateur.addWAComponentForTest(kmelia);
  }

  /**
   * Test of isUserAuthorized method, of class NodeAccessController.
   * @throws Exception s
   */
  @Test
  public void userIsAuthorizedWhenNodeHaveNoSpecificRights() throws Exception {
    NodePK nodPk = new NodePK("10", componentId);
    NodeDetail node = new NodeDetail();
    node.setNodePK(nodPk);
    PowerMockito.mockStatic(EJBUtilitaire.class);
    NodeBm nodeBm = Mockito.mock(NodeBm.class);
    Mockito.when(EJBUtilitaire.getEJBObjectRef(JNDINames.NODEBM_EJBHOME, NodeBm.class)).
        thenReturn(nodeBm);
    Mockito.when(nodeBm.getHeader(nodPk, false)).thenReturn(node);
    NodeAccessController instance = new NodeAccessController();
    boolean result = instance.isUserAuthorized(userId, nodPk);
    Assert.assertThat(result, Matchers.is(true));
  }

  /**
   * Test of isUserAuthorized method, of class NodeAccessController.
   * @throws Exception
   */
  @Test
  public void userIsAuthorizedWhithRightsDependOn() throws Exception {
    NodePK nodPk = new NodePK("10", componentId);
    NodeDetail node = new NodeDetail();
    node.setNodePK(nodPk);
    node.setRightsDependsOn(5);
    PowerMockito.mockStatic(EJBUtilitaire.class);
    NodeBm nodeBm = Mockito.mock(NodeBm.class);
    Mockito.when(EJBUtilitaire.getEJBObjectRef(JNDINames.NODEBM_EJBHOME, NodeBm.class)).
        thenReturn(nodeBm);
    Mockito.when(nodeBm.getHeader(nodPk, false)).thenReturn(node);
    OrganisationController orga = Mockito.mock(OrganizationController.class);
    Mockito.when(orga.isObjectAvailable(5, ObjectType.NODE, componentId, userId)).thenReturn(true);
    NodeAccessController instance = new NodeAccessController(orga);
    boolean result = instance.isUserAuthorized(userId, nodPk);
    Assert.assertThat(result, Matchers.is(true));
    Mockito.verify(orga, Mockito.times(1)).isObjectAvailable(5, ObjectType.NODE, componentId, userId);
  }

  /**
   * Test of isUserAuthorized method, of class NodeAccessController.
   * @throws Exception
   */
  @Test
  public void userIsNotAuthorizedWhithRightsDependOn() throws Exception {
    NodePK nodPk = new NodePK("10", componentId);
    NodeDetail node = new NodeDetail();
    node.setNodePK(nodPk);
    node.setRightsDependsOn(5);
    PowerMockito.mockStatic(EJBUtilitaire.class);
    NodeBm nodeBm = Mockito.mock(NodeBm.class);
    Mockito.when(EJBUtilitaire.getEJBObjectRef(JNDINames.NODEBM_EJBHOME, NodeBm.class)).
        thenReturn(nodeBm);
    Mockito.when(nodeBm.getHeader(nodPk, false)).thenReturn(node);
    OrganisationController orga = Mockito.mock(OrganizationController.class);
    Mockito.when(orga.isObjectAvailable(5, ObjectType.NODE, componentId, "5")).thenReturn(false);
    NodeAccessController instance = new NodeAccessController(orga);
    boolean result = instance.isUserAuthorized(userId, nodPk);
    Assert.assertThat(result, Matchers.is(false));
    Mockito.verify(orga, Mockito.times(1)).isObjectAvailable(5, ObjectType.NODE, componentId, "5");

  }
}