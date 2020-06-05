/*
 * Copyright (C) 2000 - 2020 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.security.authorization;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.internal.stubbing.answers.Returns;
import org.mockito.stubbing.Answer;
import org.silverpeas.core.admin.component.model.SilverpeasComponentInstance;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.admin.user.service.UserProvider;
import org.silverpeas.core.cache.service.CacheServiceProvider;
import org.silverpeas.core.cache.service.SessionCacheService;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.attachment.model.SimpleDocumentPK;
import org.silverpeas.core.contribution.publication.model.Location;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.contribution.publication.model.PublicationPK;
import org.silverpeas.core.contribution.publication.service.PublicationService;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.test.UnitTest;
import org.silverpeas.core.test.rule.LibCoreCommonAPI4Test;
import org.silverpeas.core.util.CollectionUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Optional;

import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

/**
 * @author ehugonnet
 */
@UnitTest
public class TestSimpleDocumentAccessController {

  private static final String USER_ID = "bart";
  private static final String GED_INSTANCE_ID = "kmelia26";

  private PublicationService publicationService;
  private OrganizationController organizationController;
  private ComponentAccessControl componentAccessController;
  private NodeAccessControl nodeAccessController;
  private SimpleDocumentAccessControl testInstance;
  private TestContext testContext;
  private User user;

  @Rule
  public LibCoreCommonAPI4Test commonAPI4Test = new LibCoreCommonAPI4Test();
  @Before
  public void setup() {
    user = mock(User.class);
    when(UserProvider.get().getUser(USER_ID)).thenReturn(user);
    organizationController = mock(OrganizationController.class);
    commonAPI4Test.injectIntoMockedBeanContainer(organizationController);
    publicationService = mock(PublicationService.class);
    commonAPI4Test.injectIntoMockedBeanContainer(publicationService);
    nodeAccessController = mock(NodeAccessControl.class);
    componentAccessController = mock(ComponentAccessControl.class);
    ((SessionCacheService) CacheServiceProvider.getSessionCacheService()).newSessionCache(user);
    testContext = new TestContext();
    testContext.clear();
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

    // User has PUBLISHER role on component
    // User rights are verified for sharing action on the document, but sharing is not enabled
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.publisher)
        .onOperationsOf(AccessControlOperation.sharing);
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(false);

    // User has ADMIN role on component
    // User rights are verified for sharing action on the document, but sharing is not enabled
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.admin)
        .onOperationsOf(AccessControlOperation.sharing);
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(false);

    // User has PUBLISHER role on component
    // User rights are verified for sharing action on the document, sharing is enabled
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.publisher)
        .onOperationsOf(AccessControlOperation.sharing).enableFileSharingRole(SilverpeasRole.admin);
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(false);

    // User has ADMIN role on component
    // User rights are verified for sharing action on the document, sharing is enabled
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.admin)
        .onOperationsOf(AccessControlOperation.sharing).enableFileSharingRole(SilverpeasRole.admin);
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(false);

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
        .verifyTwoCallsOfComponentAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(false);

    // User has USER role on component
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user).onGEDComponent();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyTwoCallsOfComponentAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(true);

    // User has PUBLISHER role on component
    // User rights are verified for sharing action on the document, but sharing is not enabled
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.publisher).onGEDComponent()
        .onOperationsOf(AccessControlOperation.sharing);
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyTwoCallsOfComponentAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(false);

    // User has ADMIN role on component
    // User rights are verified for sharing action on the document, but sharing is not enabled
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.admin).onGEDComponent()
        .onOperationsOf(AccessControlOperation.sharing);
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyTwoCallsOfComponentAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(false);

    // User has PUBLISHER role on component
    // User rights are verified for sharing action on the document, sharing is enabled
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.publisher).onGEDComponent()
        .onOperationsOf(AccessControlOperation.sharing).enableFileSharingRole(SilverpeasRole.admin);
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyTwoCallsOfComponentAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(false);

    // User has ADMIN role on component
    // User rights are verified for sharing action on the document, sharing is enabled
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.admin).onGEDComponent()
        .onOperationsOf(AccessControlOperation.sharing).enableFileSharingRole(SilverpeasRole.admin);
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyTwoCallsOfComponentAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(true);

    // User has USER role on component
    // User is going to download the document (but no download restriction on the document)
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user).onGEDComponent()
        .onOperationsOf(AccessControlOperation.download);
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyTwoCallsOfComponentAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(true);

    // User has PUBLISHER role on component
    // User is going to download the document (but no download restriction on the document)
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.publisher).onGEDComponent()
        .onOperationsOf(AccessControlOperation.download);
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyTwoCallsOfComponentAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(true);

    // User has WRITER role on component
    // User is going to download the document (but no download restriction on the document)
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.writer).onGEDComponent()
        .onOperationsOf(AccessControlOperation.download);
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyTwoCallsOfComponentAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(true);

    // User has USER role on component
    // User is going to download the document (download is forbidden for readers)
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user).onGEDComponent()
        .onOperationsOf(AccessControlOperation.download).forbidDownloadForReaders();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyTwoCallsOfComponentAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(false);

    // User has PUBLISHER role on component
    // User is going to download the document (download is forbidden for readers)
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.publisher).onGEDComponent()
        .onOperationsOf(AccessControlOperation.download).forbidDownloadForReaders();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyTwoCallsOfComponentAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(true);

    // User has WRITER role on component
    // User is going to download the document (download is forbidden for readers)
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.writer).onGEDComponent()
        .onOperationsOf(AccessControlOperation.download).forbidDownloadForReaders();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyTwoCallsOfComponentAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(true);

    // User has USER role on component
    // User is going to modify the document
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user).onGEDComponent()
        .onOperationsOf(AccessControlOperation.PERSIST_ACTIONS.iterator().next());
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyTwoCallsOfComponentAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(false);

    // User has PUBLISHER role on component
    // User is going to modify the document
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.publisher).onGEDComponent()
        .onOperationsOf(AccessControlOperation.PERSIST_ACTIONS.iterator().next());
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyTwoCallsOfComponentAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(true);

    // User has WRITER role on component
    // User is going to modify the document
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.writer).onGEDComponent()
        .onOperationsOf(AccessControlOperation.PERSIST_ACTIONS.iterator().next());
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyTwoCallsOfComponentAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(true);
  }

  @Test
  public void testIsUserAuthorizedOnGEDAndForeignIdIsDirectory() {
    // User has no right on component
    testContext.clear();
    testContext.onGEDComponent().documentAttachedToDirectory();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyTwoCallsOfComponentAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(false);

    // User has USER role on component
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user).onGEDComponent()
        .documentAttachedToDirectory();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyTwoCallsOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfNodeAccessControllerGetUserRoles()
        .verifyCallOfNodeAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(true);

    // User has PUBLISHER role on component
    // User rights are verified for sharing action on the document, but sharing is not enabled
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.publisher).onGEDComponent()
        .documentAttachedToDirectory().onOperationsOf(AccessControlOperation.sharing);
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyTwoCallsOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfNodeAccessControllerGetUserRoles()
        .verifyCallOfNodeAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(false);

    // User has ADMIN role on component
    // User rights are verified for sharing action on the document, but sharing is not enabled
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.admin).onGEDComponent()
        .documentAttachedToDirectory().onOperationsOf(AccessControlOperation.sharing);
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyTwoCallsOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfNodeAccessControllerGetUserRoles()
        .verifyCallOfNodeAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(false);

    // User has PUBLISHER role on component
    // User rights are verified for sharing action on the document, sharing is enabled
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.publisher).onGEDComponent()
        .documentAttachedToDirectory().onOperationsOf(AccessControlOperation.sharing)
        .enableFileSharingRole(SilverpeasRole.admin);
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyTwoCallsOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfNodeAccessControllerGetUserRoles()
        .verifyCallOfNodeAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(false);

    // User has ADMIN role on component
    // User rights are verified for sharing action on the document, sharing is enabled
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.admin).onGEDComponent()
        .documentAttachedToDirectory().onOperationsOf(AccessControlOperation.sharing)
        .enableFileSharingRole(SilverpeasRole.admin);
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyTwoCallsOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfNodeAccessControllerGetUserRoles()
        .verifyCallOfNodeAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(true);

    // User has USER role on component
    // User is going to download the document (but no download restriction on the document)
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user).onGEDComponent()
        .documentAttachedToDirectory().onOperationsOf(AccessControlOperation.download);
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyTwoCallsOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfNodeAccessControllerGetUserRoles()
        .verifyCallOfNodeAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(true);

    // User has PUBLISHER role on component
    // User is going to download the document (but no download restriction on the document)
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.publisher).onGEDComponent()
        .documentAttachedToDirectory().onOperationsOf(AccessControlOperation.download);
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyTwoCallsOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfNodeAccessControllerGetUserRoles()
        .verifyCallOfNodeAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(true);

    // User has WRITER role on component
    // User is going to download the document (but no download restriction on the document)
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.writer).onGEDComponent()
        .documentAttachedToDirectory().onOperationsOf(AccessControlOperation.download);
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyTwoCallsOfComponentAccessControllerIsUserAuthorized()
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
        .verifyTwoCallsOfComponentAccessControllerIsUserAuthorized()
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
        .verifyTwoCallsOfComponentAccessControllerIsUserAuthorized()
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
        .verifyTwoCallsOfComponentAccessControllerIsUserAuthorized()
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
        .verifyTwoCallsOfComponentAccessControllerIsUserAuthorized()
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
        .verifyTwoCallsOfComponentAccessControllerIsUserAuthorized()
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
        .verifyTwoCallsOfComponentAccessControllerIsUserAuthorized()
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
        .verifyTwoCallsOfComponentAccessControllerIsUserAuthorized()
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
        .verifyCallOfPublicationBmGetDetailAndGetAllAliases();
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

    // User has PUBLISHER role on component
    // User rights are verified for sharing action on the document, but sharing is not enabled
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.publisher).onGEDComponent()
        .documentAttachedToPublication().onOperationsOf(AccessControlOperation.sharing);
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled();
    assertIsUserAuthorized(false);

    // User has ADMIN role on component
    // User rights are verified for sharing action on the document, but sharing is not enabled
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.admin).onGEDComponent()
        .documentAttachedToPublication().onOperationsOf(AccessControlOperation.sharing);
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled();
    assertIsUserAuthorized(false);

    // User has PUBLISHER role on component
    // User rights are verified for sharing action on the document, sharing is enabled
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.publisher).onGEDComponent()
        .documentAttachedToPublication().onOperationsOf(AccessControlOperation.sharing)
        .enableFileSharingRole(SilverpeasRole.admin);
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled();
    assertIsUserAuthorized(false);

    // User has ADMIN role on component
    // User rights are verified for sharing action on the document, sharing is enabled
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.admin).onGEDComponent()
        .documentAttachedToPublication().onOperationsOf(AccessControlOperation.sharing)
        .enableFileSharingRole(SilverpeasRole.admin);
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
        .verifyCallOfPublicationBmGetDetailAndGetAllAliases();
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

    // User has PUBLISHER role on component
    // User rights are verified for sharing action on the document, but sharing is not enabled
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.publisher).onGEDComponent()
        .documentAttachedToPublication().userIsThePublicationAuthor()
        .onOperationsOf(AccessControlOperation.sharing);
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled();
    assertIsUserAuthorized(false);

    // User has ADMIN role on component
    // User rights are verified for sharing action on the document, but sharing is not enabled
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.admin).onGEDComponent()
        .documentAttachedToPublication().userIsThePublicationAuthor()
        .onOperationsOf(AccessControlOperation.sharing);
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled();
    assertIsUserAuthorized(false);

    // User has PUBLISHER role on component
    // User rights are verified for sharing action on the document, sharing is enabled
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.publisher).onGEDComponent()
        .documentAttachedToPublication().userIsThePublicationAuthor()
        .onOperationsOf(AccessControlOperation.sharing).enableFileSharingRole(SilverpeasRole.admin);
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled();
    assertIsUserAuthorized(false);

    // User has ADMIN role on component
    // User rights are verified for sharing action on the document, sharing is enabled
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.admin).onGEDComponent()
        .documentAttachedToPublication().userIsThePublicationAuthor()
        .onOperationsOf(AccessControlOperation.sharing).enableFileSharingRole(SilverpeasRole.admin);
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
        .verifyCallOfPublicationBmGetDetailAndGetAllAliases();
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
        .verifyCallOfPublicationBmGetMainLocation();
    assertIsUserAuthorized(true);

    // User has USER role on component
    // User rights are verified for sharing action on the document, sharing is enabled
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user).onGEDComponent()
        .documentAttachedToPublication().publicationOnRootDirectory()
        .withRightsActivatedOnDirectory().userIsThePublicationAuthor()
        .onOperationsOf(AccessControlOperation.sharing)
        .enableFileSharingRole(SilverpeasRole.reader);
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetMainLocation();
    assertIsUserAuthorized(true);

    // User has USER role on component but it is anonymous
    // User rights are verified for sharing action on the document, sharing is enabled
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user).onGEDComponent()
        .userIsAnonymous()
        .documentAttachedToPublication().publicationOnRootDirectory()
        .withRightsActivatedOnDirectory().userIsThePublicationAuthor()
        .onOperationsOf(AccessControlOperation.sharing)
        .enableFileSharingRole(SilverpeasRole.reader);
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetMainLocation();
    assertIsUserAuthorized(false);

    // User has PUBLISHER role on component
    // User rights are verified for sharing action on the document, but sharing is not enabled
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.publisher).onGEDComponent()
        .documentAttachedToPublication().publicationOnRootDirectory()
        .withRightsActivatedOnDirectory().userIsThePublicationAuthor()
        .onOperationsOf(AccessControlOperation.sharing);
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetMainLocation();
    assertIsUserAuthorized(false);

    // User has ADMIN role on component
    // User rights are verified for sharing action on the document, but sharing is not enabled
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.admin).onGEDComponent()
        .documentAttachedToPublication().publicationOnRootDirectory()
        .withRightsActivatedOnDirectory().userIsThePublicationAuthor()
        .onOperationsOf(AccessControlOperation.sharing);
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetMainLocation();
    assertIsUserAuthorized(false);

    // User has PUBLISHER role on component
    // User rights are verified for sharing action on the document, sharing is enabled
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.publisher).onGEDComponent()
        .documentAttachedToPublication().publicationOnRootDirectory()
        .withRightsActivatedOnDirectory().userIsThePublicationAuthor()
        .onOperationsOf(AccessControlOperation.sharing).enableFileSharingRole(SilverpeasRole.admin);
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetMainLocation();
    assertIsUserAuthorized(false);

    // User has PUBLISHER role on component
    // User rights are verified for sharing action on the document, sharing is enabled
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.publisher).onGEDComponent()
        .documentAttachedToPublication().publicationOnRootDirectory()
        .withRightsActivatedOnDirectory().userIsThePublicationAuthor()
        .onOperationsOf(AccessControlOperation.sharing).enableFileSharingRole(SilverpeasRole.reader);
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetMainLocation();
    assertIsUserAuthorized(true);

    // User has ADMIN role on component
    // User rights are verified for sharing action on the document, sharing is enabled
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.admin).onGEDComponent()
        .documentAttachedToPublication().publicationOnRootDirectory()
        .withRightsActivatedOnDirectory().userIsThePublicationAuthor()
        .onOperationsOf(AccessControlOperation.sharing).enableFileSharingRole(SilverpeasRole.admin);
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetMainLocation();
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
        .verifyCallOfPublicationBmGetMainLocation();
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
        .verifyCallOfPublicationBmGetMainLocation();
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
        .verifyCallOfPublicationBmGetMainLocation();
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
        .verifyCallOfPublicationBmGetMainLocation();
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
        .verifyCallOfPublicationBmGetMainLocation();
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
        .verifyCallOfPublicationBmGetMainLocation();
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
        .verifyCallOfPublicationBmGetMainLocation();
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
        .verifyCallOfPublicationBmGetMainLocation();
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
        .verifyCallOfPublicationBmGetMainLocation();
    assertIsUserAuthorized(true);
  }

  @Test
  public void testIsUserAuthorizedOnGEDAndForeignIdIsPublicationAndUserHasRightsOnDirectories() {
    // User has no right on component
    testContext.clear();
    testContext.onGEDComponent().documentAttachedToPublication().withRightsActivatedOnDirectory();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetailAndGetAllAliases();
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
        .verifyCallOfPublicationBmGetMainLocation()
        .verifyCallOfNodeAccessControllerGetUserRoles()
        .verifyCallOfNodeAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetailAndGetAllAliases();
    assertIsUserAuthorized(false);

    // User has ADMIN role on component
    // User has no role on directory
    // User rights are verified for sharing action on the document, sharing is enabled
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.admin).onGEDComponent()
        .documentAttachedToPublication().withRightsActivatedOnDirectory().withNodeUserRoles()
        .onOperationsOf(AccessControlOperation.sharing).enableFileSharingRole(SilverpeasRole.admin);
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetMainLocation()
        .verifyCallOfNodeAccessControllerGetUserRoles()
        .verifyCallOfNodeAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetailAndGetAllAliases();
    assertIsUserAuthorized(false);

    // User has USER role on component
    // User has PUBLISHER role on directory
    // User rights are verified for sharing action on the document, but sharing is not enabled
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user).onGEDComponent()
        .documentAttachedToPublication().withRightsActivatedOnDirectory()
        .withNodeUserRoles(SilverpeasRole.publisher).onOperationsOf(AccessControlOperation.sharing);
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetMainLocation().verifyCallOfNodeAccessControllerGetUserRoles()
        .verifyCallOfNodeAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(false);

    // User has USER role on component
    // User has ADMIN role on directory
    // User rights are verified for sharing action on the document, but sharing is not enabled
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user).onGEDComponent()
        .documentAttachedToPublication().withRightsActivatedOnDirectory()
        .withNodeUserRoles(SilverpeasRole.admin).onOperationsOf(AccessControlOperation.sharing);
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetMainLocation().verifyCallOfNodeAccessControllerGetUserRoles()
        .verifyCallOfNodeAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(false);

    // User has USER role on component
    // User has PUBLISHER role on directory
    // User rights are verified for sharing action on the document, sharing is enabled
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user).onGEDComponent()
        .documentAttachedToPublication().withRightsActivatedOnDirectory()
        .withNodeUserRoles(SilverpeasRole.publisher).onOperationsOf(AccessControlOperation.sharing)
        .enableFileSharingRole(SilverpeasRole.admin);
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetMainLocation().verifyCallOfNodeAccessControllerGetUserRoles()
        .verifyCallOfNodeAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(false);

    // User has USER role on component
    // User has ADMIN role on directory
    // User rights are verified for sharing action on the document, sharing is enabled
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user).onGEDComponent()
        .documentAttachedToPublication().withRightsActivatedOnDirectory()
        .withNodeUserRoles(SilverpeasRole.admin).onOperationsOf(AccessControlOperation.sharing)
        .enableFileSharingRole(SilverpeasRole.admin);
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetMainLocation().verifyCallOfNodeAccessControllerGetUserRoles()
        .verifyCallOfNodeAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(true);

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
        .verifyCallOfPublicationBmGetMainLocation().verifyCallOfNodeAccessControllerGetUserRoles()
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
        .verifyCallOfPublicationBmGetMainLocation().verifyCallOfNodeAccessControllerGetUserRoles()
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
        .verifyCallOfPublicationBmGetMainLocation().verifyCallOfNodeAccessControllerGetUserRoles()
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
        .verifyCallOfPublicationBmGetMainLocation().verifyCallOfNodeAccessControllerGetUserRoles()
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
        .verifyCallOfPublicationBmGetMainLocation().verifyCallOfNodeAccessControllerGetUserRoles()
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
        .verifyCallOfPublicationBmGetMainLocation().verifyCallOfNodeAccessControllerGetUserRoles()
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
        .verifyCallOfPublicationBmGetMainLocation().verifyCallOfNodeAccessControllerGetUserRoles()
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
        .verifyCallOfPublicationBmGetMainLocation().verifyCallOfNodeAccessControllerGetUserRoles()
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
        .verifyCallOfPublicationBmGetMainLocation().verifyCallOfNodeAccessControllerGetUserRoles()
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
        .verifyCallOfPublicationBmGetMainLocation().verifyCallOfNodeAccessControllerGetUserRoles()
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
        .verifyCallOfPublicationBmGetMainLocation().verifyCallOfNodeAccessControllerGetUserRoles()
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
        .verifyCallOfPublicationBmGetDetailAndGetAllAliases();
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
        .verifyCallOfPublicationBmGetMainLocation().verifyCallOfNodeAccessControllerGetUserRoles()
        .verifyCallOfNodeAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetailAndGetAllAliases();
    assertIsUserAuthorized(false);

    // User has ADMIN role on component
    // User has no role on directory
    // User rights are verified for sharing action on the document, sharing is enabled
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.admin).onGEDComponent()
        .documentAttachedToPublication().withRightsActivatedOnDirectory().withNodeUserRoles()
        .userIsThePublicationAuthor().onOperationsOf(AccessControlOperation.sharing)
        .enableFileSharingRole(SilverpeasRole.admin);
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetMainLocation().verifyCallOfNodeAccessControllerGetUserRoles()
        .verifyCallOfNodeAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetailAndGetAllAliases();
    assertIsUserAuthorized(false);

    // User has USER role on component
    // User has PUBLISHER role on directory
    // User rights are verified for sharing action on the document, but sharing is not enabled
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user).onGEDComponent()
        .documentAttachedToPublication().withRightsActivatedOnDirectory()
        .withNodeUserRoles(SilverpeasRole.publisher).userIsThePublicationAuthor()
        .onOperationsOf(AccessControlOperation.sharing);
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetMainLocation().verifyCallOfNodeAccessControllerGetUserRoles()
        .verifyCallOfNodeAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(false);

    // User has USER role on component
    // User has ADMIN role on directory
    // User rights are verified for sharing action on the document, but sharing is not enabled
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user).onGEDComponent()
        .documentAttachedToPublication().withRightsActivatedOnDirectory()
        .withNodeUserRoles(SilverpeasRole.admin).userIsThePublicationAuthor()
        .onOperationsOf(AccessControlOperation.sharing);
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetMainLocation().verifyCallOfNodeAccessControllerGetUserRoles()
        .verifyCallOfNodeAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(false);

    // User has USER role on component
    // User has PUBLISHER role on directory
    // User rights are verified for sharing action on the document, sharing is enabled
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user).onGEDComponent()
        .documentAttachedToPublication().withRightsActivatedOnDirectory()
        .withNodeUserRoles(SilverpeasRole.publisher).userIsThePublicationAuthor()
        .onOperationsOf(AccessControlOperation.sharing).enableFileSharingRole(SilverpeasRole.admin);
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetMainLocation().verifyCallOfNodeAccessControllerGetUserRoles()
        .verifyCallOfNodeAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(false);

    // User has USER role on component
    // User has ADMIN role on directory
    // User rights are verified for sharing action on the document, sharing is enabled
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user).onGEDComponent()
        .documentAttachedToPublication().withRightsActivatedOnDirectory()
        .withNodeUserRoles(SilverpeasRole.admin).userIsThePublicationAuthor()
        .onOperationsOf(AccessControlOperation.sharing).enableFileSharingRole(SilverpeasRole.admin);
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetMainLocation().verifyCallOfNodeAccessControllerGetUserRoles()
        .verifyCallOfNodeAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(true);

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
        .verifyCallOfPublicationBmGetMainLocation().verifyCallOfNodeAccessControllerGetUserRoles()
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
        .verifyCallOfPublicationBmGetMainLocation().verifyCallOfNodeAccessControllerGetUserRoles()
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
        .verifyCallOfPublicationBmGetMainLocation().verifyCallOfNodeAccessControllerGetUserRoles()
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
        .verifyCallOfPublicationBmGetMainLocation().verifyCallOfNodeAccessControllerGetUserRoles()
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
        .verifyCallOfPublicationBmGetMainLocation().verifyCallOfNodeAccessControllerGetUserRoles()
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
        .verifyCallOfPublicationBmGetMainLocation().verifyCallOfNodeAccessControllerGetUserRoles()
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
        .verifyCallOfPublicationBmGetMainLocation().verifyCallOfNodeAccessControllerGetUserRoles()
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
        .verifyCallOfPublicationBmGetMainLocation().verifyCallOfNodeAccessControllerGetUserRoles()
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
        .verifyCallOfPublicationBmGetMainLocation().verifyCallOfNodeAccessControllerGetUserRoles()
        .verifyCallOfNodeAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(true);
  }

  /**
   * Centralization.
   * @param expectedUserAuthorization the expected user authorization to verify.
   */
  private void assertIsUserAuthorized(boolean expectedUserAuthorization) {
    testContext.setup();
    SimpleDocument document = new SimpleDocument();
    document
        .setPK(new SimpleDocumentPK("dummyUuid", testContext.isGED ? GED_INSTANCE_ID : "yellowpages38"));
    document.setForeignId(testContext.isDocumentAttachedToDirectory ? "Node_26" :
        (testContext.isDocumentAttachedToPublication ? "26" : "dummyId"));
    if (!testContext.isDownloadAllowedForReaders) {
      document.addRolesForWhichDownloadIsForbidden(SilverpeasRole.READER_ROLES);
    }
    boolean result =
        testInstance.isUserAuthorized(USER_ID, document, testContext.accessControlContext);
    assertThat(result, is(expectedUserAuthorization));
    testContext.results().verifyMethodCalls();
  }

  private class TestContext {
    private boolean userIsAnonymous;
    private boolean isGED;
    private boolean isCoWriting;
    private SilverpeasRole fileSharingRole;
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
      CacheServiceProvider.clearAllThreadCaches();
      reset(user, componentAccessController, organizationController, nodeAccessController, publicationService);
      final PublicationAccessControl publicationAccessController = new PublicationAccessController(componentAccessController, nodeAccessController);
      testInstance = new SimpleDocumentAccessController(componentAccessController, nodeAccessController, publicationAccessController);
      userIsAnonymous = false;
      isGED = false;
      isCoWriting = false;
      fileSharingRole = null;
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

    public TestContext userIsAnonymous() {
      userIsAnonymous = true;
      return this;
    }

    public TestContext onGEDComponent() {
      isGED = true;
      return this;
    }

    public TestContext enableCoWriting() {
      isCoWriting = true;
      return this;
    }

    public TestContext enableFileSharingRole(SilverpeasRole role) {
      fileSharingRole = role;
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

    @SuppressWarnings("unchecked")
    public void setup() {
      when(user.getId()).thenReturn(USER_ID);
      when(user.isAnonymous()).thenReturn(userIsAnonymous);
      when(componentAccessController
          .getUserRoles(anyString(), anyString(), any(AccessControlContext.class)))
          .then(new Returns(componentUserRoles));
      when(componentAccessController.isUserAuthorized(any(EnumSet.class)))
          .then(new Returns(!componentUserRoles.isEmpty()));
      when(organizationController.getComponentInstance(anyString()))
          .thenAnswer(a -> {
            final String i = a.getArgument(0);
            final SilverpeasComponentInstance instance = mock(SilverpeasComponentInstance.class);
            when(instance.isTopicTracker()).then(new Returns(i.startsWith("kmelia") || i.startsWith("kmax") || i.startsWith("toolbox")));
            return Optional.of(instance);
          });
      when(organizationController.getComponentParameterValue(anyString(), eq("rightsOnTopics")))
          .then(new Returns(Boolean.toString(isRightsOnDirectories)));
      when(organizationController.getComponentParameterValue(anyString(), eq("coWriting")))
          .then(new Returns(Boolean.toString(isCoWriting)));
      when(organizationController.getComponentParameterValue(anyString(), eq("useFileSharing")))
          .thenAnswer((Answer<String>) invocationOnMock -> {
            if (fileSharingRole != null) {
              if (fileSharingRole.equals(SilverpeasRole.admin)) {
                return "1";
              } else if (fileSharingRole.equals(SilverpeasRole.writer)) {
                return "2";
              } else {
                return "3";
              }
            }
            return null;
          });
      when(nodeAccessController
          .getUserRoles(anyString(), any(NodePK.class), any(AccessControlContext.class))).then(
          new Returns(isRightsOnDirectories ?
              (nodeNotInheritedUserRoles == null ? componentUserRoles : nodeNotInheritedUserRoles) :
              componentUserRoles));
      when(nodeAccessController.isUserAuthorized(any(EnumSet.class)))
          .then(invocation -> CollectionUtil.isNotEmpty((EnumSet) invocation.getArguments()[0]));
      when(publicationService.getMinimalDataByIds(any(Collection.class))).then(invocation -> {
        PublicationDetail publi = new PublicationDetail();
        publi.setPk(((Collection<PublicationPK>) invocation.getArguments()[0]).iterator().next());
        publi.setStatus(PublicationDetail.VALID_STATUS);
        publi.setCreatorId(testContext.isUserThePublicationAuthor ? USER_ID : "otherUserId");
        return singletonList(publi);
      });
      when(publicationService.getAllLocations(any(PublicationPK.class))).then(invocation -> {
        final Collection<Location> allLocations = new ArrayList<>();
        if (!testContext.isPublicationOnRootDirectory) {
          allLocations.add(new Location("nodeId", GED_INSTANCE_ID));
        }
        return allLocations;
      });
      ((SessionCacheService) CacheServiceProvider.getSessionCacheService()).newSessionCache(user);
    }
  }

  private class TestVerifyResults {
    private int nbCallOfComponentAccessControllerGetUserRoles = 0;
    private int nbCallOfComponentAccessControllerIsUserAuthorized = 0;
    private int nbCallOfComponentAccessControllerIsRightOnTopicsEnabled = 0;
    private int nbCallOfNodeAccessControllerGetUserRoles = 0;
    private int nbCallOfNodeAccessControllerIsUserAuthorized = 0;
    private int nbCallOfPublicationBmGetDetail = 0;
    private int nbCallOfPublicationBmGetAllAliases = 0;
    private int nbCallOfPublicationBmGetMainLocation = 0;


    public TestVerifyResults verifyCallOfComponentAccessControllerGetUserRoles() {
      nbCallOfComponentAccessControllerGetUserRoles = 1;
      return this;
    }

    public TestVerifyResults verifyCallOfComponentAccessControllerIsUserAuthorized() {
      nbCallOfComponentAccessControllerIsUserAuthorized = 1;
      return this;
    }

    public TestVerifyResults verifyTwoCallsOfComponentAccessControllerIsUserAuthorized() {
      nbCallOfComponentAccessControllerIsUserAuthorized = 2;
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

    public TestVerifyResults verifyCallOfPublicationBmGetMainLocation() {
      nbCallOfPublicationBmGetMainLocation = 1;
      return this;
    }

    public TestVerifyResults verifyCallOfPublicationBmGetDetailAndGetAllAliases() {
      verifyCallOfPublicationBmGetDetail();
      nbCallOfPublicationBmGetAllAliases = 1;
      return this;
    }

    @SuppressWarnings("unchecked")
    public void verifyMethodCalls() {
      verify(componentAccessController, times(nbCallOfComponentAccessControllerGetUserRoles))
          .getUserRoles(anyString(), anyString(), any(AccessControlContext.class));
      verify(componentAccessController, times(0))
          .isUserAuthorized(anyString(), anyString(), any(AccessControlContext.class));
      verify(componentAccessController, times(nbCallOfComponentAccessControllerIsUserAuthorized))
          .isUserAuthorized(any(EnumSet.class));
      final ComponentAccessController.DataManager dataManager = ComponentAccessController.getDataManager(testContext.accessControlContext);
      assertThat(dataManager.isRightOnTopicsEnabledCache.size(), is(nbCallOfComponentAccessControllerIsRightOnTopicsEnabled));
      verify(nodeAccessController, times(nbCallOfNodeAccessControllerGetUserRoles))
          .getUserRoles(anyString(), any(NodePK.class), any(AccessControlContext.class));
      verify(nodeAccessController, times(nbCallOfNodeAccessControllerIsUserAuthorized))
          .isUserAuthorized(any(EnumSet.class));
      verify(publicationService, times(nbCallOfPublicationBmGetDetail))
          .getMinimalDataByIds(any(Collection.class));
      verify(publicationService, times(0))
          .getMainLocation(any(PublicationPK.class));
      verify(publicationService, times(0))
          .getAllAliases(any(PublicationPK.class));
      verify(publicationService, times(Math.max(nbCallOfPublicationBmGetMainLocation, nbCallOfPublicationBmGetAllAliases)))
          .getAllLocations(any(PublicationPK.class));
    }
  }
}