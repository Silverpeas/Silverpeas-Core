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

import com.stratelia.webactiv.beans.admin.OrganizationController;
import org.hamcrest.Matchers;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import com.google.common.collect.Lists;
import com.silverpeas.util.ForeignPK;
import java.util.Collection;
import com.stratelia.silverpeas.versioning.model.Document;
import com.stratelia.webactiv.util.publication.control.PublicationBm;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.webactiv.util.node.model.NodePK;
import com.stratelia.webactiv.util.publication.control.PublicationBmHome;
import com.stratelia.webactiv.util.publication.model.PublicationPK;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author ehugonnet
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(EJBUtilitaire.class)
public class DocumentAccessControllerTest {

  private final String userId = "bart";

  public DocumentAccessControllerTest() {
  }

  /**
   * Test of isUserAuthorized method, of class DocumentAccessController.
   * @throws Exception 
   */
  @Test
  public void UserIsAuthorized() throws Exception {
    String componentId = "yellowPages18";
    NodePK nodePk1 = new NodePK("5", componentId);
    NodePK nodePk2 = new NodePK("11", componentId);
    Collection<NodePK> fathers = Lists.asList(nodePk1, new NodePK[]{nodePk2});
    PublicationPK pk = new PublicationPK("50");
    PowerMockito.mockStatic(EJBUtilitaire.class);
    PublicationBmHome home = Mockito.mock(PublicationBmHome.class);
    PublicationBm publicationBm = Mockito.mock(PublicationBm.class);
    Mockito.when(home.create()).thenReturn(publicationBm);
    Mockito.when(EJBUtilitaire.getEJBObjectRef(JNDINames.PUBLICATIONBM_EJBHOME,
        PublicationBmHome.class)).thenReturn(home);
    Mockito.when(publicationBm.getAllFatherPK(pk)).thenReturn(fathers);
    Document document = new Document();
    document.setForeignKey(new ForeignPK(pk));
    NodeAccessController accessController = Mockito.mock(NodeAccessController.class);

    Mockito.when(accessController.isUserAuthorized(userId, nodePk1)).thenReturn(false);
    Mockito.when(accessController.isUserAuthorized(userId, nodePk2)).thenReturn(
        true);
    DocumentAccessController instance = new DocumentAccessController(accessController);
    boolean result = instance.isUserAuthorized(userId, document);
    assertThat(result, Matchers.is(true));
  }

  /**
   * Test of isUserAuthorized method, of class DocumentAccessController.
   * @throws Exception 
   */
  @Test
  public void UserIsNotAuthorized() throws Exception {
    String componentId = "yellowPages18";
    NodePK nodePk1 = new NodePK("5", componentId);
    NodePK nodePk2 = new NodePK("11", componentId);
    Collection<NodePK> fathers = Lists.asList(nodePk1, new NodePK[]{nodePk2});
    PublicationPK pk = new PublicationPK("50");
    PowerMockito.mockStatic(EJBUtilitaire.class);
    PublicationBmHome home = Mockito.mock(PublicationBmHome.class);
    PublicationBm publicationBm = Mockito.mock(PublicationBm.class);
    Mockito.when(home.create()).thenReturn(publicationBm);
    Mockito.when(EJBUtilitaire.getEJBObjectRef(JNDINames.PUBLICATIONBM_EJBHOME,
        PublicationBmHome.class)).
        thenReturn(home);
    Mockito.when(publicationBm.getAllFatherPK(pk)).thenReturn(fathers);
    Document document = new Document();
    document.setForeignKey(new ForeignPK(pk));
    NodeAccessController accessController = Mockito.mock(NodeAccessController.class);
    Mockito.when(accessController.isUserAuthorized(userId, nodePk1)).thenReturn(false);
    Mockito.when(accessController.isUserAuthorized(userId, nodePk2)).thenReturn(false);
    DocumentAccessController instance = new DocumentAccessController(accessController);
    boolean result = instance.isUserAuthorized(userId, document);
    assertThat(result, Matchers.is(false));
  }
}
