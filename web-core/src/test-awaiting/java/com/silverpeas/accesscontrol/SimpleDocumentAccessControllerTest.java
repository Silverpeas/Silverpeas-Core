/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
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
import org.silverpeas.util.CollectionUtil;
import com.stratelia.webactiv.SilverpeasRole;
import com.stratelia.webactiv.node.model.NodePK;
import com.stratelia.webactiv.publication.control.PublicationService;
import com.stratelia.webactiv.publication.model.PublicationDetail;
import com.stratelia.webactiv.publication.model.PublicationPK;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.internal.stubbing.answers.Returns;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.silverpeas.accesscontrol.SimpleDocumentAccessController;
import org.silverpeas.attachment.model.SimpleDocument;
import org.silverpeas.attachment.model.SimpleDocumentPK;
import org.silverpeas.core.admin.OrganizationController;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

/**
 * @author ehugonnet
 */

@RunWith(PowerMockRunner.class)
@PrepareForTest({Instanciateur.class})
public class SimpleDocumentAccessControllerTest {

  private static final String userId = "bart";

  private OrganizationController controller;
  private PublicationService publicationService;

  private ComponentAccessController componentAccessController;
  private NodeAccessController nodeAccessController;
  private SimpleDocumentAccessController testInstance;
  private TestContext testContext;

  @Before
  public void setup() {
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
    controller = mock(OrganizationController.class);
    componentAccessController = mock(ComponentAccessController.class);
    nodeAccessController = mock(NodeAccessController.class);
    publicationService = mock(PublicationService.class);
    testInstance = new SimpleDocumentAccessControllerForTest();
    testContext = new TestContext();
  }

  @Test
  public void testIsUserAuthorizedOnSomethingOtherThanGED() {
    // User has no right on component
    testContext.clear();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(false);

    // User has USER role on component
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user);
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(true);

    // User has USER role on component
    // User is going to download the document (but no download restriction on the document)
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user)
        .onOperationsOf(AccessControlOperation.download);
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(true);

    // User has PUBLISHER role on component
    // User is going to download the document (but no download restriction on the document)
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.publisher)
        .onOperationsOf(AccessControlOperation.download);
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(true);

    // User has WRITER role on component
    // User is going to download the document (but no download restriction on the document)
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.writer)
        .onOperationsOf(AccessControlOperation.download);
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(true);

    // User has USER role on component
    // User is going to download the document (download is forbidden for readers)
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user)
        .onOperationsOf(AccessControlOperation.download).forbidDownloadForReaders();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(false);

    // User has PUBLISHER role on component
    // User is going to download the document (download is forbidden for readers)
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.publisher)
        .onOperationsOf(AccessControlOperation.download).forbidDownloadForReaders();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(true);

    // User has WRITER role on component
    // User is going to download the document (download is forbidden for readers)
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.writer)
        .onOperationsOf(AccessControlOperation.download).forbidDownloadForReaders();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(true);

    // User has USER role on component
    // User is going to modify the document
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user)
        .onOperationsOf(AccessControlOperation.PERSIST_ACTIONS.iterator().next());
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(false);

    // User has PUBLISHER role on component
    // User is going to modify the document
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.publisher)
        .onOperationsOf(AccessControlOperation.PERSIST_ACTIONS.iterator().next());
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(true);

    // User has WRITER role on component
    // User is going to modify the document
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.writer)
        .onOperationsOf(AccessControlOperation.PERSIST_ACTIONS.iterator().next());
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(true);
  }

  @Test
  public void testIsUserAuthorizedOnGEDButForeignIdIsNotPublicationOrDirectory() {
    // User has no right on component
    testContext.clear();
    testContext.onGEDComponent();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(false);

    // User has USER role on component
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user).onGEDComponent();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(true);

    // User has USER role on component
    // User is going to download the document (but no download restriction on the document)
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user).onGEDComponent()
        .onOperationsOf(AccessControlOperation.download);
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(true);

    // User has PUBLISHER role on component
    // User is going to download the document (but no download restriction on the document)
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.publisher).onGEDComponent()
        .onOperationsOf(AccessControlOperation.download);
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(true);

    // User has WRITER role on component
    // User is going to download the document (but no download restriction on the document)
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.writer).onGEDComponent()
        .onOperationsOf(AccessControlOperation.download);
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(true);

    // User has USER role on component
    // User is going to download the document (download is forbidden for readers)
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user).onGEDComponent()
        .onOperationsOf(AccessControlOperation.download).forbidDownloadForReaders();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(false);

    // User has PUBLISHER role on component
    // User is going to download the document (download is forbidden for readers)
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.publisher).onGEDComponent()
        .onOperationsOf(AccessControlOperation.download).forbidDownloadForReaders();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(true);

    // User has WRITER role on component
    // User is going to download the document (download is forbidden for readers)
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.writer).onGEDComponent()
        .onOperationsOf(AccessControlOperation.download).forbidDownloadForReaders();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(true);

    // User has USER role on component
    // User is going to modify the document
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user).onGEDComponent()
        .onOperationsOf(AccessControlOperation.PERSIST_ACTIONS.iterator().next());
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(false);

    // User has PUBLISHER role on component
    // User is going to modify the document
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.publisher).onGEDComponent()
        .onOperationsOf(AccessControlOperation.PERSIST_ACTIONS.iterator().next());
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(true);

    // User has WRITER role on component
    // User is going to modify the document
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.writer).onGEDComponent()
        .onOperationsOf(AccessControlOperation.PERSIST_ACTIONS.iterator().next());
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(true);
  }

  @Test
  public void testIsUserAuthorizedOnGEDAndForeignIdIsDirectory() {
    // User has no right on component
    testContext.clear();
    testContext.onGEDComponent().documentAttachedToDirectory();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(false);

    // User has USER role on component
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user).onGEDComponent()
        .documentAttachedToDirectory();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfNodeAccessControllerGetUserRoles()
        .verifyCallOfNodeAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(true);

    // User has USER role on component
    // User is going to download the document (but no download restriction on the document)
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user).onGEDComponent()
        .documentAttachedToDirectory().onOperationsOf(AccessControlOperation.download);
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfNodeAccessControllerGetUserRoles()
        .verifyCallOfNodeAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(true);

    // User has PUBLISHER role on component
    // User is going to download the document (but no download restriction on the document)
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.publisher).onGEDComponent()
        .documentAttachedToDirectory().onOperationsOf(AccessControlOperation.download);
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfNodeAccessControllerGetUserRoles()
        .verifyCallOfNodeAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(true);

    // User has WRITER role on component
    // User is going to download the document (but no download restriction on the document)
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.writer).onGEDComponent()
        .documentAttachedToDirectory().onOperationsOf(AccessControlOperation.download);
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfNodeAccessControllerGetUserRoles()
        .verifyCallOfNodeAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(true);

    // User has USER role on component
    // User is going to download the document (download is forbidden for readers)
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user).onGEDComponent()
        .documentAttachedToDirectory().onOperationsOf(AccessControlOperation.download)
        .forbidDownloadForReaders();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfNodeAccessControllerGetUserRoles()
        .verifyCallOfNodeAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(false);

    // User has PUBLISHER role on component
    // User is going to download the document (download is forbidden for readers)
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.publisher).onGEDComponent()
        .documentAttachedToDirectory().onOperationsOf(AccessControlOperation.download)
        .forbidDownloadForReaders();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfNodeAccessControllerGetUserRoles()
        .verifyCallOfNodeAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(true);

    // User has WRITER role on component
    // User is going to download the document (download is forbidden for readers)
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.writer).onGEDComponent()
        .documentAttachedToDirectory().onOperationsOf(AccessControlOperation.download)
        .forbidDownloadForReaders();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfNodeAccessControllerGetUserRoles()
        .verifyCallOfNodeAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(false);

    // User has USER role on component
    // User is going to modify the document
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user).onGEDComponent()
        .documentAttachedToDirectory()
        .onOperationsOf(AccessControlOperation.PERSIST_ACTIONS.iterator().next());
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfNodeAccessControllerGetUserRoles()
        .verifyCallOfNodeAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(false);

    // User has PUBLISHER role on component
    // User is going to modify the document
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.publisher).onGEDComponent()
        .documentAttachedToDirectory()
        .onOperationsOf(AccessControlOperation.PERSIST_ACTIONS.iterator().next());
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfNodeAccessControllerGetUserRoles()
        .verifyCallOfNodeAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(false);

    // User has WRITER role on component
    // User is going to modify the document
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.writer).onGEDComponent()
        .documentAttachedToDirectory()
        .onOperationsOf(AccessControlOperation.PERSIST_ACTIONS.iterator().next());
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfNodeAccessControllerGetUserRoles()
        .verifyCallOfNodeAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(false);

    // User has ADMIN role on component
    // User is going to modify the document
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.admin).onGEDComponent()
        .documentAttachedToDirectory()
        .onOperationsOf(AccessControlOperation.PERSIST_ACTIONS.iterator().next());
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfNodeAccessControllerGetUserRoles()
        .verifyCallOfNodeAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(true);
  }

  @Test
  public void testIsUserAuthorizedOnGEDAndForeignIdIsPublicationAndNoRightOnDirectories() {
    // User has no right on component
    testContext.clear();
    testContext.onGEDComponent().documentAttachedToPublication();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetailAndGetAlias();
    assertIsUserAuthorized(false);

    // User has USER role on component
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user).onGEDComponent()
        .documentAttachedToPublication();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled();
    assertIsUserAuthorized(true);

    // User has USER role on component
    // User is going to download the document (but no download restriction on the document)
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user).onGEDComponent()
        .documentAttachedToPublication().onOperationsOf(AccessControlOperation.download);
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled();
    assertIsUserAuthorized(true);

    // User has PUBLISHER role on component
    // User is going to download the document (but no download restriction on the document)
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.publisher).onGEDComponent()
        .documentAttachedToPublication().onOperationsOf(AccessControlOperation.download);
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled();
    assertIsUserAuthorized(true);

    // User has WRITER role on component
    // User is going to download the document (but no download restriction on the document)
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.writer).onGEDComponent()
        .documentAttachedToPublication().onOperationsOf(AccessControlOperation.download);
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled();
    assertIsUserAuthorized(true);

    // User has USER role on component
    // User is going to download the document (download is forbidden for readers)
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user).onGEDComponent()
        .documentAttachedToPublication().onOperationsOf(AccessControlOperation.download)
        .forbidDownloadForReaders();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled();
    assertIsUserAuthorized(false);

    // User has PUBLISHER role on component
    // User is going to download the document (download is forbidden for readers)
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.publisher).onGEDComponent()
        .documentAttachedToPublication().onOperationsOf(AccessControlOperation.download)
        .forbidDownloadForReaders();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled();
    assertIsUserAuthorized(true);

    // User has WRITER role on component
    // User is going to download the document (download is forbidden for readers)
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.writer).onGEDComponent()
        .documentAttachedToPublication().onOperationsOf(AccessControlOperation.download)
        .forbidDownloadForReaders();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled();
    assertIsUserAuthorized(false);

    // User has WRITER role on component (cowriting enabled)
    // User is going to download the document (download is forbidden for readers)
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.writer).onGEDComponent().enableCoWriting()
        .documentAttachedToPublication().onOperationsOf(AccessControlOperation.download)
        .forbidDownloadForReaders();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled();
    assertIsUserAuthorized(true);

    // User has USER role on component
    // User is going to modify the document
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user).onGEDComponent()
        .documentAttachedToPublication()
        .onOperationsOf(AccessControlOperation.PERSIST_ACTIONS.iterator().next());
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled();
    assertIsUserAuthorized(false);

    // User has PUBLISHER role on component
    // User is going to modify the document
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.publisher).onGEDComponent()
        .documentAttachedToPublication()
        .onOperationsOf(AccessControlOperation.PERSIST_ACTIONS.iterator().next());
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled();
    assertIsUserAuthorized(true);

    // User has WRITER role on component
    // User is going to modify the document
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.writer).onGEDComponent()
        .documentAttachedToPublication()
        .onOperationsOf(AccessControlOperation.PERSIST_ACTIONS.iterator().next());
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled();
    assertIsUserAuthorized(false);

    // User has WRITER role on component
    // User is going to modify the document (cowriting enabled)
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.writer).onGEDComponent().enableCoWriting()
        .documentAttachedToPublication()
        .onOperationsOf(AccessControlOperation.PERSIST_ACTIONS.iterator().next());
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled();
    assertIsUserAuthorized(true);
  }

  @Test
  public void
  testIsUserAuthorizedOnGEDAndForeignIdIsPublicationAndUserIsThePublicationAuthorAndNoRightOnDirectories() {
    // User has no right on component
    testContext.clear();
    testContext.onGEDComponent().documentAttachedToPublication().userIsThePublicationAuthor();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetailAndGetAlias();
    assertIsUserAuthorized(false);

    // User has USER role on component
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user).onGEDComponent()
        .documentAttachedToPublication().userIsThePublicationAuthor();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled();
    assertIsUserAuthorized(true);

    // User has USER role on component
    // User is going to download the document (but no download restriction on the document)
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user).onGEDComponent()
        .documentAttachedToPublication().userIsThePublicationAuthor()
        .onOperationsOf(AccessControlOperation.download);
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled();
    assertIsUserAuthorized(true);

    // User has PUBLISHER role on component
    // User is going to download the document (but no download restriction on the document)
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.publisher).onGEDComponent()
        .documentAttachedToPublication().userIsThePublicationAuthor()
        .onOperationsOf(AccessControlOperation.download);
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled();
    assertIsUserAuthorized(true);

    // User has WRITER role on component
    // User is going to download the document (but no download restriction on the document)
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.writer).onGEDComponent()
        .documentAttachedToPublication().userIsThePublicationAuthor()
        .onOperationsOf(AccessControlOperation.download);
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled();
    assertIsUserAuthorized(true);

    // User has USER role on component
    // User is going to download the document (download is forbidden for readers)
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user).onGEDComponent()
        .documentAttachedToPublication().userIsThePublicationAuthor()
        .onOperationsOf(AccessControlOperation.download).forbidDownloadForReaders();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled();
    assertIsUserAuthorized(false);

    // User has PUBLISHER role on component
    // User is going to download the document (download is forbidden for readers)
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.publisher).onGEDComponent()
        .documentAttachedToPublication().userIsThePublicationAuthor()
        .onOperationsOf(AccessControlOperation.download).forbidDownloadForReaders();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled();
    assertIsUserAuthorized(true);

    // User has WRITER role on component
    // User is going to download the document (download is forbidden for readers)
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.writer).onGEDComponent()
        .documentAttachedToPublication().userIsThePublicationAuthor()
        .onOperationsOf(AccessControlOperation.download).forbidDownloadForReaders();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled();
    assertIsUserAuthorized(true);

    // User has USER role on component
    // User is going to modify the document
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user).onGEDComponent()
        .documentAttachedToPublication().userIsThePublicationAuthor()
        .onOperationsOf(AccessControlOperation.PERSIST_ACTIONS.iterator().next());
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled();
    assertIsUserAuthorized(false);

    // User has PUBLISHER role on component
    // User is going to modify the document
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.publisher).onGEDComponent()
        .documentAttachedToPublication().userIsThePublicationAuthor()
        .onOperationsOf(AccessControlOperation.PERSIST_ACTIONS.iterator().next());
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled();
    assertIsUserAuthorized(true);

    // User has WRITER role on component
    // User is going to modify the document
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.writer).onGEDComponent()
        .documentAttachedToPublication().userIsThePublicationAuthor()
        .onOperationsOf(AccessControlOperation.PERSIST_ACTIONS.iterator().next());
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled();
    assertIsUserAuthorized(true);
  }

  @Test
  public void
  testIsUserAuthorizedOnGEDAndForeignIdIsPublicationOnRootDirectoryAndUserIsThePublicationAuthorAndRightsOnDirectories() {
    // User has no right on component
    testContext.clear();
    testContext.onGEDComponent().documentAttachedToPublication().publicationOnRootDirectory()
        .withRightsActivatedOnDirectory().userIsThePublicationAuthor();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetailAndGetAlias();
    assertIsUserAuthorized(false);

    // User has USER role on component
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user).onGEDComponent()
        .documentAttachedToPublication().publicationOnRootDirectory()
        .withRightsActivatedOnDirectory().userIsThePublicationAuthor();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetAllFatherPK();
    assertIsUserAuthorized(true);

    // User has USER role on component
    // User is going to download the document (but no download restriction on the document)
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user).onGEDComponent()
        .documentAttachedToPublication().publicationOnRootDirectory()
        .withRightsActivatedOnDirectory().userIsThePublicationAuthor()
        .onOperationsOf(AccessControlOperation.download);
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetAllFatherPK();
    assertIsUserAuthorized(true);

    // User has PUBLISHER role on component
    // User is going to download the document (but no download restriction on the document)
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.publisher).onGEDComponent()
        .documentAttachedToPublication().publicationOnRootDirectory()
        .withRightsActivatedOnDirectory().userIsThePublicationAuthor()
        .onOperationsOf(AccessControlOperation.download);
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetAllFatherPK();
    assertIsUserAuthorized(true);

    // User has WRITER role on component
    // User is going to download the document (but no download restriction on the document)
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.writer).onGEDComponent()
        .documentAttachedToPublication().publicationOnRootDirectory()
        .withRightsActivatedOnDirectory().userIsThePublicationAuthor()
        .onOperationsOf(AccessControlOperation.download);
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetAllFatherPK();
    assertIsUserAuthorized(true);

    // User has USER role on component
    // User is going to download the document (download is forbidden for readers)
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user).onGEDComponent()
        .documentAttachedToPublication().publicationOnRootDirectory()
        .withRightsActivatedOnDirectory().userIsThePublicationAuthor()
        .onOperationsOf(AccessControlOperation.download).forbidDownloadForReaders();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetAllFatherPK();
    assertIsUserAuthorized(false);

    // User has PUBLISHER role on component
    // User is going to download the document (download is forbidden for readers)
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.publisher).onGEDComponent()
        .documentAttachedToPublication().publicationOnRootDirectory()
        .withRightsActivatedOnDirectory().userIsThePublicationAuthor()
        .onOperationsOf(AccessControlOperation.download).forbidDownloadForReaders();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetAllFatherPK();
    assertIsUserAuthorized(true);

    // User has WRITER role on component
    // User is going to download the document (download is forbidden for readers)
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.writer).onGEDComponent()
        .documentAttachedToPublication().publicationOnRootDirectory()
        .withRightsActivatedOnDirectory().userIsThePublicationAuthor()
        .onOperationsOf(AccessControlOperation.download).forbidDownloadForReaders();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetAllFatherPK();
    assertIsUserAuthorized(true);

    // User has USER role on component
    // User is going to modify the document
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user).onGEDComponent()
        .documentAttachedToPublication().publicationOnRootDirectory()
        .withRightsActivatedOnDirectory().userIsThePublicationAuthor()
        .onOperationsOf(AccessControlOperation.PERSIST_ACTIONS.iterator().next());
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetAllFatherPK();
    assertIsUserAuthorized(false);

    // User has PUBLISHER role on component
    // User is going to modify the document
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.publisher).onGEDComponent()
        .documentAttachedToPublication().publicationOnRootDirectory()
        .withRightsActivatedOnDirectory().userIsThePublicationAuthor()
        .onOperationsOf(AccessControlOperation.PERSIST_ACTIONS.iterator().next());
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetAllFatherPK();
    assertIsUserAuthorized(true);

    // User has WRITER role on component
    // User is going to modify the document
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.writer).onGEDComponent()
        .documentAttachedToPublication().publicationOnRootDirectory()
        .withRightsActivatedOnDirectory().userIsThePublicationAuthor()
        .onOperationsOf(AccessControlOperation.PERSIST_ACTIONS.iterator().next());
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetAllFatherPK();
    assertIsUserAuthorized(true);
  }

  @Test
  public void testIsUserAuthorizedOnGEDAndForeignIdIsPublicationAndUserHasRightsOnDirectories() {
    // User has no right on component
    testContext.clear();
    testContext.onGEDComponent().documentAttachedToPublication().withRightsActivatedOnDirectory();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetailAndGetAlias();
    assertIsUserAuthorized(false);

    // User has USER role on component
    // User has no role on directory
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user).onGEDComponent()
        .documentAttachedToPublication().withRightsActivatedOnDirectory().withNodeUserRoles();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetAllFatherPK().verifyCallOfNodeAccessControllerGetUserRoles()
        .verifyCallOfNodeAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(false);

    // User has USER role on component
    // User has USER role on directory
    // User is going to download the document (but no download restriction on the document)
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user).onGEDComponent()
        .documentAttachedToPublication().withRightsActivatedOnDirectory()
        .withNodeUserRoles(SilverpeasRole.user).onOperationsOf(AccessControlOperation.download);
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetAllFatherPK().verifyCallOfNodeAccessControllerGetUserRoles()
        .verifyCallOfNodeAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(true);

    // User has USER role on component
    // User has PUBLISHER role on directory
    // User is going to download the document (but no download restriction on the document)
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user).onGEDComponent()
        .documentAttachedToPublication().withRightsActivatedOnDirectory()
        .withNodeUserRoles(SilverpeasRole.publisher)
        .onOperationsOf(AccessControlOperation.download);
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetAllFatherPK().verifyCallOfNodeAccessControllerGetUserRoles()
        .verifyCallOfNodeAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(true);

    // User has USER role on component
    // User has WRITER role on directory
    // User is going to download the document (but no download restriction on the document)
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user).onGEDComponent()
        .documentAttachedToPublication().withRightsActivatedOnDirectory()
        .withNodeUserRoles(SilverpeasRole.writer).onOperationsOf(AccessControlOperation.download);
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetAllFatherPK().verifyCallOfNodeAccessControllerGetUserRoles()
        .verifyCallOfNodeAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(true);

    // User has USER role on component
    // User has USER role on directory
    // User is going to download the document (download is forbidden for readers)
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user).onGEDComponent()
        .documentAttachedToPublication().withRightsActivatedOnDirectory()
        .withNodeUserRoles(SilverpeasRole.user).onOperationsOf(AccessControlOperation.download)
        .forbidDownloadForReaders();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetAllFatherPK().verifyCallOfNodeAccessControllerGetUserRoles()
        .verifyCallOfNodeAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(false);

    // User has USER role on component
    // User has PUBLISHER role on directory
    // User is going to download the document (download is forbidden for readers)
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user).onGEDComponent()
        .documentAttachedToPublication().withRightsActivatedOnDirectory()
        .withNodeUserRoles(SilverpeasRole.publisher).onOperationsOf(AccessControlOperation.download)
        .forbidDownloadForReaders();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetAllFatherPK().verifyCallOfNodeAccessControllerGetUserRoles()
        .verifyCallOfNodeAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(true);

    // User has USER role on component
    // User has WRITER role on directory
    // User is going to download the document (download is forbidden for readers)
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user).onGEDComponent()
        .documentAttachedToPublication().withRightsActivatedOnDirectory()
        .withNodeUserRoles(SilverpeasRole.writer).onOperationsOf(AccessControlOperation.download)
        .forbidDownloadForReaders();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetAllFatherPK().verifyCallOfNodeAccessControllerGetUserRoles()
        .verifyCallOfNodeAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(false);

    // User has USER role on component (cowriting enabled)
    // User has WRITER role on directory
    // User is going to download the document (download is forbidden for readers)
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user).onGEDComponent().enableCoWriting()
        .documentAttachedToPublication().withRightsActivatedOnDirectory()
        .withNodeUserRoles(SilverpeasRole.writer).onOperationsOf(AccessControlOperation.download)
        .forbidDownloadForReaders();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetAllFatherPK().verifyCallOfNodeAccessControllerGetUserRoles()
        .verifyCallOfNodeAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(true);

    // User has USER role on component
    // User has USER role on directory
    // User is going to modify the document
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user).onGEDComponent()
        .documentAttachedToPublication().withRightsActivatedOnDirectory()
        .withNodeUserRoles(SilverpeasRole.user)
        .onOperationsOf(AccessControlOperation.PERSIST_ACTIONS.iterator().next());
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetAllFatherPK().verifyCallOfNodeAccessControllerGetUserRoles()
        .verifyCallOfNodeAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(false);

    // User has USER role on component
    // User has PUBLISHER role on directory
    // User is going to modify the document
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user).onGEDComponent()
        .documentAttachedToPublication().withRightsActivatedOnDirectory()
        .withNodeUserRoles(SilverpeasRole.publisher)
        .onOperationsOf(AccessControlOperation.PERSIST_ACTIONS.iterator().next());
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetAllFatherPK().verifyCallOfNodeAccessControllerGetUserRoles()
        .verifyCallOfNodeAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(true);

    // User has USER role on component
    // User has WRITER role on directory
    // User is going to modify the document
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user).onGEDComponent()
        .documentAttachedToPublication().withRightsActivatedOnDirectory()
        .withNodeUserRoles(SilverpeasRole.writer)
        .onOperationsOf(AccessControlOperation.PERSIST_ACTIONS.iterator().next());
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetAllFatherPK().verifyCallOfNodeAccessControllerGetUserRoles()
        .verifyCallOfNodeAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(false);

    // User has USER role on component (cowriting enabled)
    // User has WRITER role on directory
    // User is going to modify the document
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user).onGEDComponent().enableCoWriting()
        .documentAttachedToPublication().withRightsActivatedOnDirectory()
        .withNodeUserRoles(SilverpeasRole.writer)
        .onOperationsOf(AccessControlOperation.PERSIST_ACTIONS.iterator().next());
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetAllFatherPK().verifyCallOfNodeAccessControllerGetUserRoles()
        .verifyCallOfNodeAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(true);
  }

  @Test
  public void
  testIsUserAuthorizedOnGEDAndForeignIdIsPublicationAndUserIsThePublicationAuthorAndUserHasRightsOnDirectories() {
    // User has no right on component
    testContext.clear();
    testContext.onGEDComponent().documentAttachedToPublication().withRightsActivatedOnDirectory()
        .userIsThePublicationAuthor();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetailAndGetAlias();
    assertIsUserAuthorized(false);

    // User has USER role on component
    // User has no role on directory
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user).onGEDComponent()
        .documentAttachedToPublication().withRightsActivatedOnDirectory().withNodeUserRoles()
        .userIsThePublicationAuthor();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetAllFatherPK().verifyCallOfNodeAccessControllerGetUserRoles()
        .verifyCallOfNodeAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(false);

    // User has USER role on component
    // User has USER role on directory
    // User is going to download the document (but no download restriction on the document)
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user).onGEDComponent()
        .documentAttachedToPublication().withRightsActivatedOnDirectory()
        .withNodeUserRoles(SilverpeasRole.user).userIsThePublicationAuthor()
        .onOperationsOf(AccessControlOperation.download);
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetAllFatherPK().verifyCallOfNodeAccessControllerGetUserRoles()
        .verifyCallOfNodeAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(true);

    // User has USER role on component
    // User has PUBLISHER role on directory
    // User is going to download the document (but no download restriction on the document)
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user).onGEDComponent()
        .documentAttachedToPublication().withRightsActivatedOnDirectory()
        .withNodeUserRoles(SilverpeasRole.publisher).userIsThePublicationAuthor()
        .onOperationsOf(AccessControlOperation.download);
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetAllFatherPK().verifyCallOfNodeAccessControllerGetUserRoles()
        .verifyCallOfNodeAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(true);

    // User has USER role on component
    // User has WRITER role on directory
    // User is going to download the document (but no download restriction on the document)
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user).onGEDComponent()
        .documentAttachedToPublication().withRightsActivatedOnDirectory()
        .withNodeUserRoles(SilverpeasRole.writer).userIsThePublicationAuthor()
        .onOperationsOf(AccessControlOperation.download);
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetAllFatherPK().verifyCallOfNodeAccessControllerGetUserRoles()
        .verifyCallOfNodeAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(true);

    // User has USER role on component
    // User has USER role on directory
    // User is going to download the document (download is forbidden for readers)
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user).onGEDComponent()
        .documentAttachedToPublication().withRightsActivatedOnDirectory()
        .withNodeUserRoles(SilverpeasRole.user).userIsThePublicationAuthor()
        .onOperationsOf(AccessControlOperation.download).forbidDownloadForReaders();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetAllFatherPK().verifyCallOfNodeAccessControllerGetUserRoles()
        .verifyCallOfNodeAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(false);

    // User has USER role on component
    // User has PUBLISHER role on directory
    // User is going to download the document (download is forbidden for readers)
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user).onGEDComponent()
        .documentAttachedToPublication().withRightsActivatedOnDirectory()
        .withNodeUserRoles(SilverpeasRole.publisher).userIsThePublicationAuthor()
        .onOperationsOf(AccessControlOperation.download).forbidDownloadForReaders();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetAllFatherPK().verifyCallOfNodeAccessControllerGetUserRoles()
        .verifyCallOfNodeAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(true);

    // User has USER role on component
    // User has WRITER role on directory
    // User is going to download the document (download is forbidden for readers)
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user).onGEDComponent()
        .documentAttachedToPublication().withRightsActivatedOnDirectory()
        .withNodeUserRoles(SilverpeasRole.writer).userIsThePublicationAuthor()
        .onOperationsOf(AccessControlOperation.download).forbidDownloadForReaders();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetAllFatherPK().verifyCallOfNodeAccessControllerGetUserRoles()
        .verifyCallOfNodeAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(true);

    // User has USER role on component
    // User has USER role on directory
    // User is going to modify the document
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user).onGEDComponent()
        .documentAttachedToPublication().withRightsActivatedOnDirectory()
        .withNodeUserRoles(SilverpeasRole.user).userIsThePublicationAuthor()
        .onOperationsOf(AccessControlOperation.PERSIST_ACTIONS.iterator().next());
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetAllFatherPK().verifyCallOfNodeAccessControllerGetUserRoles()
        .verifyCallOfNodeAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(false);

    // User has USER role on component
    // User has PUBLISHER role on directory
    // User is going to modify the document
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user).onGEDComponent()
        .documentAttachedToPublication().withRightsActivatedOnDirectory()
        .withNodeUserRoles(SilverpeasRole.publisher).userIsThePublicationAuthor()
        .onOperationsOf(AccessControlOperation.PERSIST_ACTIONS.iterator().next());
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetAllFatherPK().verifyCallOfNodeAccessControllerGetUserRoles()
        .verifyCallOfNodeAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(true);

    // User has USER role on component
    // User has WRITER role on directory
    // User is going to modify the document
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user).onGEDComponent()
        .documentAttachedToPublication().withRightsActivatedOnDirectory()
        .withNodeUserRoles(SilverpeasRole.writer).userIsThePublicationAuthor()
        .onOperationsOf(AccessControlOperation.PERSIST_ACTIONS.iterator().next());
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetAllFatherPK().verifyCallOfNodeAccessControllerGetUserRoles()
        .verifyCallOfNodeAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(true);
  }

  /**
   * Centralization.
   * @param expectedUserAuthorization
   */
  private void assertIsUserAuthorized(boolean expectedUserAuthorization) {
    testContext.setup();
    SimpleDocument document = new SimpleDocument();
    document
        .setPK(new SimpleDocumentPK("dummyUuid", testContext.isGED ? "kmelia26" : "yellowpages38"));
    document.setForeignId(testContext.isDocumentAttachedToDirectory ? "Node_26" :
        (testContext.isDocumentAttachedToPublication ? "26" : "dummyId"));
    if (!testContext.isDownloadAllowedForReaders) {
      document.addRolesForWhichDownloadIsForbidden(SilverpeasRole.READER_ROLES);
    }
    boolean result =
        testInstance.isUserAuthorized(userId, document, testContext.accessControlContext);
    assertThat(result, is(expectedUserAuthorization));
    testContext.results().verifyMethodCalls();
  }

  private class TestContext {
    private boolean isGED;
    private boolean isCoWriting;
    private boolean isDocumentAttachedToDirectory;
    private boolean isDocumentAttachedToPublication;
    private AccessControlContext accessControlContext;
    private EnumSet<SilverpeasRole> componentUserRoles;
    private boolean isRightsOnDirectories;
    private EnumSet<SilverpeasRole> nodeNotInheritedUserRoles;
    private boolean isPublicationOnRootDirectory;
    private TestVerifyResults testVerifyResults;
    private boolean isDownloadAllowedForReaders;
    private boolean isUserThePublicationAuthor;

    public void clear() {
      reset(controller, componentAccessController, nodeAccessController, publicationService);
      isGED = false;
      isCoWriting = false;
      isDocumentAttachedToDirectory = false;
      isDocumentAttachedToPublication = false;
      accessControlContext = AccessControlContext.init();
      componentUserRoles = EnumSet.noneOf(SilverpeasRole.class);
      isRightsOnDirectories = false;
      nodeNotInheritedUserRoles = null;
      isPublicationOnRootDirectory = false;
      testVerifyResults = new TestVerifyResults();
      isDownloadAllowedForReaders = true;
      isUserThePublicationAuthor = false;
    }

    public TestContext onGEDComponent() {
      isGED = true;
      return this;
    }

    public TestContext enableCoWriting() {
      isCoWriting = true;
      return this;
    }

    public TestContext onOperationsOf(AccessControlOperation... operations) {
      accessControlContext.onOperationsOf(operations);
      return this;
    }

    public TestContext withComponentUserRoles(SilverpeasRole... roles) {
      Collections.addAll(componentUserRoles, roles);
      return this;
    }

    public TestContext documentAttachedToDirectory() {
      isDocumentAttachedToDirectory = true;
      return this;
    }

    public TestContext documentAttachedToPublication() {
      isDocumentAttachedToPublication = true;
      return this;
    }

    public TestContext publicationOnRootDirectory() {
      isPublicationOnRootDirectory = true;
      return this;
    }

    public TestContext withRightsActivatedOnDirectory() {
      isRightsOnDirectories = true;
      return this;
    }

    public TestContext withNodeUserRoles(SilverpeasRole... roles) {
      if (nodeNotInheritedUserRoles == null) {
        nodeNotInheritedUserRoles = EnumSet.noneOf(SilverpeasRole.class);
      }
      Collections.addAll(nodeNotInheritedUserRoles, roles);
      return this;
    }

    public TestContext forbidDownloadForReaders() {
      isDownloadAllowedForReaders = false;
      return this;
    }

    public TestContext userIsThePublicationAuthor() {
      isUserThePublicationAuthor = true;
      return this;
    }

    public TestVerifyResults results() {
      return testVerifyResults;
    }

    public void setup() {
      when(componentAccessController
          .getUserRoles(any(AccessControlContext.class), anyString(), anyString()))
          .then(new Returns(componentUserRoles));
      when(componentAccessController.isUserAuthorized(any(EnumSet.class)))
          .then(new Returns(!componentUserRoles.isEmpty()));
      when(componentAccessController.isRightOnTopicsEnabled(anyString()))
          .then(new Returns(isRightsOnDirectories));
      when(componentAccessController.isCoWritingEnabled(anyString()))
          .then(new Returns(isCoWriting));
      when(nodeAccessController
          .getUserRoles(any(AccessControlContext.class), anyString(), any(NodePK.class))).then(
          new Returns(isRightsOnDirectories ?
              (nodeNotInheritedUserRoles == null ? componentUserRoles : nodeNotInheritedUserRoles) :
              componentUserRoles));
      when(nodeAccessController.isUserAuthorized(any(EnumSet.class))).then(new Answer<Boolean>() {
        @Override
        public Boolean answer(final InvocationOnMock invocation) throws Throwable {
          return CollectionUtil.isNotEmpty((EnumSet) invocation.getArguments()[0]);
        }
      });
      when(publicationService.getDetail(any(PublicationPK.class))).then(new Answer<PublicationDetail>() {
        @Override
        public PublicationDetail answer(final InvocationOnMock invocation) throws Throwable {
          PublicationDetail publi = new PublicationDetail();
          publi.setPk((PublicationPK) invocation.getArguments()[0]);
          publi.setStatus(PublicationDetail.VALID);
          publi.setCreatorId(testContext.isUserThePublicationAuthor ? userId : "otherUserId");
          return publi;
        }
      });
      when(publicationService.getAllFatherPK(any(PublicationPK.class)))
          .then(new Answer<Collection<NodePK>>() {
            @Override
            public Collection<NodePK> answer(final InvocationOnMock invocation) throws Throwable {
              Collection<NodePK> nodes = new ArrayList<NodePK>();
              if (!testContext.isPublicationOnRootDirectory) {
                nodes.add(new NodePK("nodeId"));
              }
              return nodes;
            }
          });
    }
  }

  private class TestVerifyResults {
    private int nbCallOfComponentAccessControllerGetUserRoles = 0;
    private int nbCallOfComponentAccessControllerIsUserAuthorized = 0;
    private int nbCallOfComponentAccessControllerIsRightOnTopicsEnabled = 0;
    private int nbCallOfNodeAccessControllerGetUserRoles = 0;
    private int nbCallOfNodeAccessControllerIsUserAuthorized = 0;
    private int nbCallOfPublicationBmGetDetail = 0;
    private int nbCallOfPublicationBmGetAlias = 0;
    private int nbCallOfPublicationBmGetAllFatherPK = 0;


    public TestVerifyResults verifyCallOfComponentAccessControllerGetUserRoles() {
      nbCallOfComponentAccessControllerGetUserRoles = 1;
      return this;
    }

    public TestVerifyResults verifyCallOfComponentAccessControllerIsUserAuthorized() {
      nbCallOfComponentAccessControllerIsUserAuthorized = 1;
      return this;
    }

    public TestVerifyResults verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled() {
      nbCallOfComponentAccessControllerIsRightOnTopicsEnabled = 1;
      return this;
    }

    public TestVerifyResults verifyCallOfNodeAccessControllerGetUserRoles() {
      nbCallOfNodeAccessControllerGetUserRoles = 1;
      return this;
    }

    public TestVerifyResults verifyCallOfNodeAccessControllerIsUserAuthorized() {
      nbCallOfNodeAccessControllerIsUserAuthorized = 1;
      return this;
    }

    public TestVerifyResults verifyCallOfPublicationBmGetDetail() {
      nbCallOfPublicationBmGetDetail = 1;
      return this;
    }

    public TestVerifyResults verifyCallOfPublicationBmGetAllFatherPK() {
      nbCallOfPublicationBmGetAllFatherPK = 1;
      return this;
    }

    public TestVerifyResults verifyCallOfPublicationBmGetDetailAndGetAlias() {
      verifyCallOfPublicationBmGetDetail();
      nbCallOfPublicationBmGetAlias = 1;
      return this;
    }

    public void verifyMethodCalls() {
      verify(componentAccessController, times(nbCallOfComponentAccessControllerGetUserRoles))
          .getUserRoles(any(AccessControlContext.class), anyString(), anyString());
      verify(componentAccessController, times(0))
          .isUserAuthorized(anyString(), anyString(), any(AccessControlContext.class));
      verify(componentAccessController, times(nbCallOfComponentAccessControllerIsUserAuthorized))
          .isUserAuthorized(any(EnumSet.class));
      verify(componentAccessController,
          times(nbCallOfComponentAccessControllerIsRightOnTopicsEnabled))
          .isRightOnTopicsEnabled(anyString());
      verify(nodeAccessController, times(nbCallOfNodeAccessControllerGetUserRoles))
          .getUserRoles(any(AccessControlContext.class), anyString(), any(NodePK.class));
      verify(nodeAccessController, times(nbCallOfNodeAccessControllerIsUserAuthorized))
          .isUserAuthorized(any(EnumSet.class));
      verify(publicationService, times(nbCallOfPublicationBmGetDetail))
          .getDetail(any(PublicationPK.class));
      verify(publicationService, times(nbCallOfPublicationBmGetAllFatherPK))
          .getAllFatherPK(any(PublicationPK.class));
      verify(publicationBm, times(nbCallOfPublicationBmGetAlias))
          .getAlias(any(PublicationPK.class));
    }
  }

  private class SimpleDocumentAccessControllerForTest extends SimpleDocumentAccessController {
    @Override
    protected PublicationService getPublicationBm() throws Exception {
      return publicationService;
    }

    @Override
    protected ComponentAccessController getComponentAccessController() {
      return componentAccessController;
    }

    @Override
    protected NodeAccessController getNodeAccessController() {
      return nodeAccessController;
    }
  }
}