/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.security.authorization;

import org.silverpeas.core.security.authorization.NodeAccessController;
import org.silverpeas.util.CollectionUtil;
import org.silverpeas.util.ForeignPK;
import com.stratelia.webactiv.node.model.NodePK;
import org.silverpeas.core.contribution.publication.control.PublicationService;
import org.silverpeas.core.contribution.publication.model.PublicationPK;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.silverpeas.importExport.versioning.Document;

import java.util.Collection;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 *
 * @author ehugonnet
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(EJBUtilitaire.class)
public class DocumentAccessControllerTest {

  private final String userId = "bart";
  private final String componentId = "yellowPages18";
  private final NodePK nodePk1 = new NodePK("5", componentId);
  private final NodePK nodePk2 = new NodePK("11", componentId);

  public DocumentAccessControllerTest() {
  }

  /**
   * Test of isUserAuthorized method, of class DocumentAccessController.
   *
   * @throws Exception
   */
  @Test
  public void UserIsAuthorized() throws Exception {
    Collection<NodePK> fathers = CollectionUtil.asList(nodePk1, nodePk2);
    PublicationPK pk = new PublicationPK("50");
    PowerMockito.mockStatic(EJBUtilitaire.class);
    PublicationService publicationService = mock(PublicationService.class);
        .thenReturn(publicationService);
    /* to replace with ServiceProvider
    when(EJBUtilitaire.getEJBObjectRef(JNDINames.PUBLICATIONBM_EJBHOME, PublicationService.class))
     */
    when(publicationService.getAllFatherPK(pk)).thenReturn(fathers);
    Document document = new Document();
    document.setForeignKey(new ForeignPK(pk));
    NodeAccessController accessController = mock(NodeAccessController.class);

    when(accessController
        .isUserAuthorized(anyString(), any(NodePK.class), any(AccessControlContext.class)))
        .thenAnswer(new Answer<Boolean>() {

          @Override
          public Boolean answer(final InvocationOnMock invocation) throws Throwable {
            String userIdArgs = (String) invocation.getArguments()[0];
            NodePK nodePkArgs = (NodePK) invocation.getArguments()[1];
            return userId.equals(userIdArgs) && nodePk2.equals(nodePkArgs);
          }
        });

    DocumentAccessController instance = new DocumentAccessController(accessController);
    boolean result = instance.isUserAuthorized(userId, document);
    assertThat(result, is(true));
  }

  /**
   * Test of isUserAuthorized method, of class DocumentAccessController.
   *
   * @throws Exception
   */
  @Test
  public void UserIsNotAuthorized() throws Exception {
    Collection<NodePK> fathers = CollectionUtil.asList(nodePk1, nodePk2);
    PublicationPK pk = new PublicationPK("50");
    PowerMockito.mockStatic(EJBUtilitaire.class);
    PublicationService publicationService = mock(PublicationService.class);
    /* to replace with ServiceProvider
    when(EJBUtilitaire.getEJBObjectRef(JNDINames.PUBLICATIONBM_EJBHOME, PublicationService.class))
        .thenReturn(publicationService);
        */
    when(publicationService.getAllFatherPK(pk)).thenReturn(fathers);
    Document document = new Document();
    document.setForeignKey(new ForeignPK(pk));
    NodeAccessController accessController = mock(NodeAccessController.class);
    DocumentAccessController instance = new DocumentAccessController(accessController);
    boolean result = instance.isUserAuthorized(userId, document);
    assertThat(result, is(false));
  }
}
