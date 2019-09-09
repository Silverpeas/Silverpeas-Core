/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
import org.mockito.Mockito;
import org.mockito.internal.stubbing.answers.Returns;
import org.mockito.stubbing.Answer;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.admin.user.service.UserProvider;
import org.silverpeas.core.cache.service.CacheServiceProvider;
import org.silverpeas.core.cache.service.SessionCacheService;
import org.silverpeas.core.contribution.publication.model.Location;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.contribution.publication.model.PublicationPK;
import org.silverpeas.core.contribution.publication.model.Visibility;
import org.silverpeas.core.contribution.publication.service.PublicationService;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.test.UnitTest;
import org.silverpeas.core.test.rule.LibCoreCommonAPI4Test;
import org.silverpeas.core.test.rule.MockByReflectionRule;
import org.silverpeas.core.util.CollectionUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

/**
 * @author Yohann Chastagnier
 */
@UnitTest
public class TestPublicationAccessController {


  private static final String userId = "bart";

  private PublicationService publicationService;

  private ComponentAccessControl componentAccessController;
  private NodeAccessControl nodeAccessController;
  private PublicationAccessControl testInstance;
  private TestContext testContext;
  private User user;

  @Rule
  public LibCoreCommonAPI4Test commonAPI4Test = new LibCoreCommonAPI4Test();

  @Rule
  public MockByReflectionRule reflectionRule = new MockByReflectionRule();

  @Before
  public void setup() {
    testContext = new TestContext();
    testInstance = new PublicationAccessController4Test();

    user = mock(User.class);
    when(UserProvider.get().getUser(userId)).thenReturn(user);
    componentAccessController = reflectionRule
        .mockField(testInstance, ComponentAccessControl.class, "componentAccessController");
    nodeAccessController =
        reflectionRule.mockField(testInstance, NodeAccessControl.class, "nodeAccessController");
    publicationService =
        reflectionRule.mockField(testInstance, PublicationService.class, "publicationService");
  }

  @Test
  public void testIsUserAuthorizedOnSomethingOtherThanGED() {
    // User has no right on component
    testContext.clear();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail();
    assertIsUserAuthorized(false);

    // User has no right on component, creation case
    testContext.clear();
    testContext.publicationIdIsNull();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(false);

    // User has USER role on component
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user);
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail();
    assertIsUserAuthorized(true);

    // User has USER role on component, but publication is in draft mode
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user)
        .withDraftStatus();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail();
    assertIsUserAuthorized(false);

    // User has USER role on component, but publication has to be validated
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user)
        .withToValidateStatus();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail();
    assertIsUserAuthorized(false);

    // User has USER role on component, but publication has been refused
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user)
        .withRefusedStatus();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail();
    assertIsUserAuthorized(false);

    // User has USER role on component, but publication is not visible
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user)
        .notVisible();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail();
    assertIsUserAuthorized(false);

    // User has WRITER role on component
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.writer);
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail();
    assertIsUserAuthorized(true);

    // User has WRITER role on component, but publication is in draft mode
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.writer)
        .withDraftStatus();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail();
    assertIsUserAuthorized(false);

    // User has WRITER role on component, but publication has to be validated
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.writer)
        .withToValidateStatus();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail();
    assertIsUserAuthorized(false);

    // User has WRITER role on component, but publication has been refused
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.writer)
        .withRefusedStatus();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail();
    assertIsUserAuthorized(false);

    // User has WRITER role on component, but publication is not visible
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.writer)
        .notVisible();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail();
    assertIsUserAuthorized(false);

    // User has USER role on component
    // User rights are verified for sharing action on the document, sharing is enabled
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user)
        .onOperationsOf(AccessControlOperation.sharing).enablePublicationSharingRole(SilverpeasRole.admin);
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail();
    assertIsUserAuthorized(false);

    // User has USER role on component
    // User rights are verified for sharing action on the document, sharing is enabled
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user)
        .onOperationsOf(AccessControlOperation.sharing)
        .enablePublicationSharingRole(SilverpeasRole.reader);
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail();
    assertIsUserAuthorized(true);

    // User has USER role on component
    // User rights are verified for sharing action on the document, sharing is enabled
    // But publication is in draft mode
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user)
        .withDraftStatus()
        .onOperationsOf(AccessControlOperation.sharing)
        .enablePublicationSharingRole(SilverpeasRole.reader);
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail();
    assertIsUserAuthorized(false);

    // User has USER role on component
    // User rights are verified for sharing action on the document, sharing is enabled
    // But publication has to be validated
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user)
        .withToValidateStatus()
        .onOperationsOf(AccessControlOperation.sharing)
        .enablePublicationSharingRole(SilverpeasRole.reader);
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail();
    assertIsUserAuthorized(false);

    // User has USER role on component
    // User rights are verified for sharing action on the document, sharing is enabled
    // But publication has been refused
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user)
        .withRefusedStatus()
        .onOperationsOf(AccessControlOperation.sharing)
        .enablePublicationSharingRole(SilverpeasRole.reader);
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail();
    assertIsUserAuthorized(false);

    // User has USER role on component
    // User rights are verified for sharing action on the document, sharing is enabled
    // But publication is not visible
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user)
        .notVisible()
        .onOperationsOf(AccessControlOperation.sharing)
        .enablePublicationSharingRole(SilverpeasRole.reader);
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail();
    assertIsUserAuthorized(false);

    // User has USER role on component but it is anonymous
    // User rights are verified for sharing action on the document, sharing is enabled
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user)
        .userIsAnonymous()
        .onOperationsOf(AccessControlOperation.sharing)
        .enablePublicationSharingRole(SilverpeasRole.reader);
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail();
    assertIsUserAuthorized(false);

    // User has PUBLISHER role on component
    // User rights are verified for sharing action on the document, but sharing is not enabled
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.publisher)
        .onOperationsOf(AccessControlOperation.sharing);
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail();
    assertIsUserAuthorized(false);

    // User has ADMIN role on component
    // User rights are verified for sharing action on the document, but sharing is not enabled
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.admin)
        .onOperationsOf(AccessControlOperation.sharing);
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail();
    assertIsUserAuthorized(false);

    // User has PUBLISHER role on component
    // User rights are verified for sharing action on the document, sharing is enabled
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.publisher)
        .onOperationsOf(AccessControlOperation.sharing).enablePublicationSharingRole(SilverpeasRole.admin);
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail();
    assertIsUserAuthorized(false);

    // User has PUBLISHER role on component
    // User rights are verified for sharing action on the document, sharing is enabled
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.publisher)
        .onOperationsOf(AccessControlOperation.sharing).enablePublicationSharingRole(SilverpeasRole.reader);
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail();
    assertIsUserAuthorized(true);

    // User has PUBLISHER role on component
    // User rights are verified for sharing action on the document, sharing is enabled
    // And publication is in draft mode
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.publisher)
        .withDraftStatus()
        .onOperationsOf(AccessControlOperation.sharing)
        .enablePublicationSharingRole(SilverpeasRole.reader);
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail();
    assertIsUserAuthorized(true);

    // User has PUBLISHER role on component
    // User rights are verified for sharing action on the document, sharing is enabled
    // And publication has to be validated
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.publisher)
        .withToValidateStatus()
        .onOperationsOf(AccessControlOperation.sharing)
        .enablePublicationSharingRole(SilverpeasRole.reader);
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail();
    assertIsUserAuthorized(true);

    // User has PUBLISHER role on component
    // User rights are verified for sharing action on the document, sharing is enabled
    // And publication has been refused
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.publisher)
        .withRefusedStatus()
        .onOperationsOf(AccessControlOperation.sharing)
        .enablePublicationSharingRole(SilverpeasRole.reader);
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail();
    assertIsUserAuthorized(true);

    // User has PUBLISHER role on component
    // User rights are verified for sharing action on the document, sharing is enabled
    // And publication is not visible
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.publisher)
        .notVisible()
        .onOperationsOf(AccessControlOperation.sharing)
        .enablePublicationSharingRole(SilverpeasRole.reader);
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail();
    assertIsUserAuthorized(true);

    // User has ADMIN role on component
    // User rights are verified for sharing action on the document, sharing is enabled
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.admin)
        .onOperationsOf(AccessControlOperation.sharing).enablePublicationSharingRole(SilverpeasRole.admin);
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail();
    assertIsUserAuthorized(true);

    // User has ADMIN role on component
    // User rights are verified for sharing action on the document, sharing is enabled
    // And publication is in draft mode
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.admin)
        .withDraftStatus()
        .onOperationsOf(AccessControlOperation.sharing)
        .enablePublicationSharingRole(SilverpeasRole.admin);
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail();
    assertIsUserAuthorized(true);

    // User has ADMIN role on component
    // User rights are verified for sharing action on the document, sharing is enabled
    // And publication has to be validated
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.admin)
        .withToValidateStatus()
        .onOperationsOf(AccessControlOperation.sharing)
        .enablePublicationSharingRole(SilverpeasRole.admin);
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail();
    assertIsUserAuthorized(true);

    // User has ADMIN role on component
    // User rights are verified for sharing action on the document, sharing is enabled
    // And publication has been refused
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.admin)
        .withRefusedStatus()
        .onOperationsOf(AccessControlOperation.sharing)
        .enablePublicationSharingRole(SilverpeasRole.admin);
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail();
    assertIsUserAuthorized(true);

    // User has ADMIN role on component
    // User rights are verified for sharing action on the document, sharing is enabled
    // And publication is not visible
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.admin)
        .notVisible()
        .onOperationsOf(AccessControlOperation.sharing)
        .enablePublicationSharingRole(SilverpeasRole.admin);
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail();
    assertIsUserAuthorized(true);

    // User has USER role on component
    // User is going to modify the publication
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user)
        .onOperationsOf(AccessControlOperation.PERSIST_ACTIONS.iterator().next());
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail();
    assertIsUserAuthorized(false);

    // User has PUBLISHER role on component
    // User is going to modify the publication
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.publisher)
        .onOperationsOf(AccessControlOperation.PERSIST_ACTIONS.iterator().next());
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail();
    assertIsUserAuthorized(true);

    // User has WRITER role on component
    // User is going to modify the publication
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.writer)
        .onOperationsOf(AccessControlOperation.PERSIST_ACTIONS.iterator().next());
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail();
    assertIsUserAuthorized(false);
  }

  @Test
  public void testIsUserAuthorizedOnGEDButPublicationIdIsNull() {
    // User has no right on component
    testContext.clear();
    testContext.onGEDComponent().publicationIdIsNull();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(false);

    // User has no right on component, but has alias right
    testContext.clear();
    testContext.onGEDComponent().publicationIdIsNull().withAliasUserRoles(SilverpeasRole.admin);
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(false);

    // User has USER role on component
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user).onGEDComponent()
        .publicationIdIsNull();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(true);

    // User has USER role on component,
    // And publication is in draft mode
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user).onGEDComponent()
        .publicationIdIsNull().withDraftStatus();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(true);

    // User has USER role on component,
    // And publication is not visible
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user).onGEDComponent()
        .publicationIdIsNull().notVisible();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(true);

    // User has PUBLISHER role on component
    // User rights are verified for sharing action on the document, but sharing is not enabled
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.publisher).onGEDComponent()
        .publicationIdIsNull().onOperationsOf(AccessControlOperation.sharing);
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(false);

    // User has ADMIN role on component
    // User rights are verified for sharing action on the document, but sharing is not enabled
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.admin).onGEDComponent().publicationIdIsNull()
        .onOperationsOf(AccessControlOperation.sharing);
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(false);

    // User has PUBLISHER role on component
    // User rights are verified for sharing action on the document, sharing is enabled
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.publisher).onGEDComponent()
        .publicationIdIsNull().onOperationsOf(AccessControlOperation.sharing)
        .enablePublicationSharingRole(SilverpeasRole.admin);
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(false);

    // User has ADMIN role on component
    // User rights are verified for sharing action on the document, sharing is enabled
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.admin).onGEDComponent().publicationIdIsNull()
        .onOperationsOf(AccessControlOperation.sharing).enablePublicationSharingRole(SilverpeasRole.admin);
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(true);

    // User has USER role on component
    // User is going to modify the publication
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user).onGEDComponent().publicationIdIsNull()
        .onOperationsOf(AccessControlOperation.PERSIST_ACTIONS.iterator().next());
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(false);

    // User has PUBLISHER role on component
    // User is going to modify the publication
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.publisher).onGEDComponent()
        .publicationIdIsNull()
        .onOperationsOf(AccessControlOperation.PERSIST_ACTIONS.iterator().next());
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(true);

    // User has WRITER role on component
    // User is going to modify the publication
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.writer).onGEDComponent().publicationIdIsNull()
        .onOperationsOf(AccessControlOperation.PERSIST_ACTIONS.iterator().next());
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(false);

    // User has ADMIN role on component
    // User is going to modify the publication
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.admin).onGEDComponent().publicationIdIsNull()
        .onOperationsOf(AccessControlOperation.PERSIST_ACTIONS.iterator().next());
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(true);
  }

  @Test
  public void testIsUserAuthorizedOnGEDButPublicationIdHasWrongFormat() {
    // User has no right on component
    testContext.clear();
    testContext.onGEDComponent().publicationIdHasWrongFormat();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(false);

    // User has no right on component, but has alias right
    testContext.clear();
    testContext.onGEDComponent().publicationIdHasWrongFormat()
        .withAliasUserRoles(SilverpeasRole.admin);
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(false);

    // User has USER role on component
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user).onGEDComponent()
        .publicationIdHasWrongFormat();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(true);

    // User has USER role on component
    // And publication is in draft mode
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user).onGEDComponent()
        .publicationIdHasWrongFormat().withDraftStatus();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(true);

    // User has USER role on component
    // And publication is not visible
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user).onGEDComponent()
        .publicationIdHasWrongFormat().notVisible();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(true);

    // User has PUBLISHER role on component
    // User rights are verified for sharing action on the document, but sharing is not enabled
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.publisher).onGEDComponent()
        .publicationIdHasWrongFormat().onOperationsOf(AccessControlOperation.sharing);
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(false);

    // User has ADMIN role on component
    // User rights are verified for sharing action on the document, but sharing is not enabled
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.admin).onGEDComponent()
        .publicationIdHasWrongFormat().onOperationsOf(AccessControlOperation.sharing);
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(false);

    // User has PUBLISHER role on component
    // User rights are verified for sharing action on the document, sharing is enabled
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.publisher).onGEDComponent()
        .publicationIdHasWrongFormat().onOperationsOf(AccessControlOperation.sharing)
        .enablePublicationSharingRole(SilverpeasRole.admin);
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(false);

    // User has ADMIN role on component
    // User rights are verified for sharing action on the document, sharing is enabled
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.admin).onGEDComponent()
        .publicationIdHasWrongFormat().onOperationsOf(AccessControlOperation.sharing)
        .enablePublicationSharingRole(SilverpeasRole.admin);
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(true);

    // User has USER role on component
    // User is going to modify the publication
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user).onGEDComponent()
        .publicationIdHasWrongFormat()
        .onOperationsOf(AccessControlOperation.PERSIST_ACTIONS.iterator().next());
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(false);

    // User has PUBLISHER role on component
    // User is going to modify the publication
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.publisher).onGEDComponent()
        .publicationIdHasWrongFormat()
        .onOperationsOf(AccessControlOperation.PERSIST_ACTIONS.iterator().next());
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(true);

    // User has WRITER role on component
    // User is going to modify the publication
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.writer).onGEDComponent()
        .publicationIdHasWrongFormat()
        .onOperationsOf(AccessControlOperation.PERSIST_ACTIONS.iterator().next());
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(false);

    // User has ADMIN role on component
    // User is going to modify the publication
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.admin).onGEDComponent()
        .publicationIdHasWrongFormat()
        .onOperationsOf(AccessControlOperation.PERSIST_ACTIONS.iterator().next());
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(true);
  }

  @Test
  public void testIsUserAuthorizedOnGEDButPublicationIsNotExisting() {
    // User has no right on component
    testContext.clear();
    testContext.onGEDComponent().publicationIsNotExisting();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail();
    assertIsUserAuthorized(false);

    // User has no right on component, but has alias right
    testContext.clear();
    testContext.onGEDComponent().publicationIsNotExisting()
        .withAliasUserRoles(SilverpeasRole.admin);
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail();
    assertIsUserAuthorized(false);

    // User has USER role on component
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user).onGEDComponent()
        .publicationIsNotExisting();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail();
    assertIsUserAuthorized(false);

    // User has PUBLISHER role on component
    // User rights are verified for sharing action on the document, but sharing is not enabled
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.publisher).onGEDComponent()
        .publicationIsNotExisting().onOperationsOf(AccessControlOperation.sharing);
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail();
    assertIsUserAuthorized(false);

    // User has ADMIN role on component
    // User rights are verified for sharing action on the document, but sharing is not enabled
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.admin).onGEDComponent()
        .publicationIsNotExisting().onOperationsOf(AccessControlOperation.sharing);
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail();
    assertIsUserAuthorized(false);

    // User has PUBLISHER role on component
    // User rights are verified for sharing action on the document, sharing is enabled
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.publisher).onGEDComponent()
        .publicationIsNotExisting().onOperationsOf(AccessControlOperation.sharing)
        .enablePublicationSharingRole(SilverpeasRole.admin);
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail();
    assertIsUserAuthorized(false);

    // User has ADMIN role on component
    // User rights are verified for sharing action on the document, sharing is enabled
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.admin).onGEDComponent()
        .publicationIsNotExisting().onOperationsOf(AccessControlOperation.sharing)
        .enablePublicationSharingRole(SilverpeasRole.admin);
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail();
    assertIsUserAuthorized(false);

    // User has USER role on component
    // User is going to modify the publication
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user).onGEDComponent()
        .publicationIsNotExisting()
        .onOperationsOf(AccessControlOperation.PERSIST_ACTIONS.iterator().next());
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail();
    assertIsUserAuthorized(false);

    // User has PUBLISHER role on component
    // User is going to modify the publication
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.publisher).onGEDComponent()
        .publicationIsNotExisting()
        .onOperationsOf(AccessControlOperation.PERSIST_ACTIONS.iterator().next());
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail();
    assertIsUserAuthorized(false);

    // User has WRITER role on component
    // User is going to modify the publication
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.writer).onGEDComponent()
        .publicationIsNotExisting()
        .onOperationsOf(AccessControlOperation.PERSIST_ACTIONS.iterator().next());
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail();
    assertIsUserAuthorized(false);

    // User has ADMIN role on component
    // User is going to modify the publication
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.admin).onGEDComponent()
        .publicationIsNotExisting()
        .onOperationsOf(AccessControlOperation.PERSIST_ACTIONS.iterator().next());
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail();
    assertIsUserAuthorized(false);
  }

  @Test
  public void testIsUserAuthorizedOnGEDAndNoRightOnDirectories() {
    // User has no right on component
    testContext.clear();
    testContext.onGEDComponent();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetailAndGetAlias();
    assertIsUserAuthorized(false);

    // User has no right on component, but has alias right
    testContext.clear();
    testContext.onGEDComponent().withAliasUserRoles(SilverpeasRole.publisher);
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetailAndGetAlias()
        .verifyCallOfNodeAccessControllerGetUserRoles()
        .verifyCallOfNodeAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(true);

    // User has no right on component, but has alias right (PUBLISHER)
    // User rights are verified for sharing action on the document, sharing is enabled
    testContext.clear();
    testContext.onGEDComponent().withAliasUserRoles(SilverpeasRole.publisher)
        .onOperationsOf(AccessControlOperation.sharing).enablePublicationSharingRole(SilverpeasRole.admin);
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetailAndGetAlias()
        .verifyCallOfNodeAccessControllerGetUserRoles()
        .verifyCallOfNodeAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(false);

    // User has no right on component, but has alias right (ADMIN)
    // User rights are verified for sharing action on the document, sharing is enabled
    testContext.clear();
    testContext.onGEDComponent().withAliasUserRoles(SilverpeasRole.admin)
        .onOperationsOf(AccessControlOperation.sharing).enablePublicationSharingRole(SilverpeasRole.admin);
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetailAndGetAlias()
        .verifyCallOfNodeAccessControllerGetUserRoles()
        .verifyCallOfNodeAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(true);

    // User has USER role on component
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user).onGEDComponent();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled();
    assertIsUserAuthorized(true);

    // User has USER role on component
    // And publication is in draft mode
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user).onGEDComponent().withDraftStatus();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled();
    assertIsUserAuthorized(false);

    // User has USER role on component
    // And publication has to be validated
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user).onGEDComponent().withToValidateStatus();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled();
    assertIsUserAuthorized(false);

    // User has USER role on component
    // And publication has been refused
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user).onGEDComponent().withRefusedStatus();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled();
    assertIsUserAuthorized(false);

    // User has USER role on component
    // And publication is not visible
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user).onGEDComponent().notVisible();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled();
    assertIsUserAuthorized(false);

    // User has WRITER role on component
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.writer).onGEDComponent();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled();
    assertIsUserAuthorized(true);

    // User has WRITER role on component
    // And publication is in draft mode
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.writer).onGEDComponent().withDraftStatus();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled();
    assertIsUserAuthorized(false);

    // User has WRITER role on component
    // And publication has to be validated
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.writer).onGEDComponent().withToValidateStatus();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled();
    assertIsUserAuthorized(false);

    // User has WRITER role on component
    // And publication has been refused
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.writer).onGEDComponent().withRefusedStatus();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled();
    assertIsUserAuthorized(false);

    // User has WRITER role on component
    // And publication is not visible
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.writer).onGEDComponent().notVisible();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled();
    assertIsUserAuthorized(false);

    // User has WRITER role on component, co-writing enabled
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.writer).onGEDComponent().enableCoWriting();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled();
    assertIsUserAuthorized(true);

    // User has WRITER role on component, co-writing enabled
    // And publication is in draft mode
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.writer).onGEDComponent()
        .withDraftStatus().enableCoWriting();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled();
    assertIsUserAuthorized(false);

    // User has WRITER role on component, co-writing enabled
    // And publication has to be validated
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.writer).onGEDComponent()
        .withToValidateStatus().enableCoWriting();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled();
    assertIsUserAuthorized(true);

    // User has WRITER role on component, co-writing enabled
    // And publication has been refused
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.writer).onGEDComponent()
        .withRefusedStatus().enableCoWriting();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled();
    assertIsUserAuthorized(true);

    // User has WRITER role on component, co-writing enabled
    // And publication is not visible
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.writer).onGEDComponent()
        .notVisible().enableCoWriting();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled();
    assertIsUserAuthorized(true);

    // User has WRITER role on component, co-writing enabled, draft visible with co-writing
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.writer).onGEDComponent()
        .enableCoWriting().draftVisibleWithCoWriting();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled();
    assertIsUserAuthorized(true);

    // User has WRITER role on component, co-writing enabled, draft visible with co-writing
    // And publication is in draft mode
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.writer).onGEDComponent()
        .withDraftStatus().enableCoWriting().draftVisibleWithCoWriting();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled();
    assertIsUserAuthorized(true);

    // User has WRITER role on component, co-writing enabled, draft visible with co-writing
    // And publication has to be validated
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.writer).onGEDComponent()
        .withToValidateStatus().enableCoWriting().draftVisibleWithCoWriting();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled();
    assertIsUserAuthorized(true);

    // User has WRITER role on component, co-writing enabled, draft visible with co-writing
    // And publication has been refused
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.writer).onGEDComponent()
        .withRefusedStatus().enableCoWriting().draftVisibleWithCoWriting();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled();
    assertIsUserAuthorized(true);

    // User has WRITER role on component, co-writing enabled, draft visible with co-writing
    // And publication is not visible
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.writer).onGEDComponent()
        .notVisible().enableCoWriting().draftVisibleWithCoWriting();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled();
    assertIsUserAuthorized(true);

    // User has PUBLISHER role on component
    // User rights are verified for sharing action on the document, but sharing is not enabled
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.publisher).onGEDComponent()
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
        .onOperationsOf(AccessControlOperation.sharing).enablePublicationSharingRole(SilverpeasRole.admin);
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled();
    assertIsUserAuthorized(false);

    // User has ADMIN role on component
    // User rights are verified for sharing action on the document, sharing is enabled
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.admin).onGEDComponent()
        .onOperationsOf(AccessControlOperation.sharing).enablePublicationSharingRole(SilverpeasRole.admin);
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled();
    assertIsUserAuthorized(true);

    // User has USER role on component
    // User is going to modify the publication
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user).onGEDComponent()
        .onOperationsOf(AccessControlOperation.PERSIST_ACTIONS.iterator().next());
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled();
    assertIsUserAuthorized(false);

    // User has PUBLISHER role on component
    // User is going to modify the publication
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.publisher).onGEDComponent()
        .onOperationsOf(AccessControlOperation.PERSIST_ACTIONS.iterator().next());
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled();
    assertIsUserAuthorized(true);

    // User has WRITER role on component
    // User is going to modify the publication
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.writer).onGEDComponent()
        .onOperationsOf(AccessControlOperation.PERSIST_ACTIONS.iterator().next());
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled();
    assertIsUserAuthorized(false);

    // User has WRITER role on component
    // User is going to modify the publication, co-writing enabled
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.writer).onGEDComponent()
        .enableCoWriting()
        .onOperationsOf(AccessControlOperation.PERSIST_ACTIONS.iterator().next());
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled();
    assertIsUserAuthorized(true);

    // User has WRITER role on component
    // User is going to modify the publication, co-writing enabled, draft visible with co-writing
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.writer).onGEDComponent()
        .enableCoWriting().draftVisibleWithCoWriting()
        .onOperationsOf(AccessControlOperation.PERSIST_ACTIONS.iterator().next());
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled();
    assertIsUserAuthorized(true);
  }

  @Test
  public void testIsUserAuthorizedOnGEDAndUserIsThePublicationAuthorAndNoRightOnDirectories() {
    // User has no right on component
    testContext.clear();
    testContext.onGEDComponent().userIsThePublicationAuthor();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetailAndGetAlias();
    assertIsUserAuthorized(false);

    // User has USER role on component
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user).onGEDComponent()
        .userIsThePublicationAuthor();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled();
    assertIsUserAuthorized(true);

    // User has USER role on component
    // And publication is in draft mode
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user).onGEDComponent()
        .userIsThePublicationAuthor()
        .withDraftStatus();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled();
    assertIsUserAuthorized(false);

    // User has USER role on component
    // And publication has to be validated
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user).onGEDComponent()
        .userIsThePublicationAuthor()
        .withToValidateStatus();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled();
    assertIsUserAuthorized(false);

    // User has USER role on component
    // And publication has been refused
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user).onGEDComponent()
        .userIsThePublicationAuthor()
        .withRefusedStatus();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled();
    assertIsUserAuthorized(false);

    // User has USER role on component
    // And publication is not visible
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user).onGEDComponent()
        .userIsThePublicationAuthor()
        .notVisible();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled();
    assertIsUserAuthorized(false);

    // User has WRITER role on component
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.writer).onGEDComponent()
        .userIsThePublicationAuthor();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled();
    assertIsUserAuthorized(true);

    // User has WRITER role on component
    // And publication is in draft mode
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.writer).onGEDComponent()
        .userIsThePublicationAuthor()
        .withDraftStatus();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled();
    assertIsUserAuthorized(true);

    // User has WRITER role on component
    // And publication has to be validated
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.writer).onGEDComponent()
        .userIsThePublicationAuthor()
        .withToValidateStatus();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled();
    assertIsUserAuthorized(true);

    // User has WRITER role on component
    // And publication has been refused
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.writer).onGEDComponent()
        .userIsThePublicationAuthor()
        .withRefusedStatus();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled();
    assertIsUserAuthorized(true);

    // User has WRITER role on component
    // And publication is not visible
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.writer).onGEDComponent()
        .userIsThePublicationAuthor()
        .notVisible();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled();
    assertIsUserAuthorized(true);

    // User has WRITER role on component, co-writing enabled
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.writer).onGEDComponent()
        .userIsThePublicationAuthor().enableCoWriting();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled();
    assertIsUserAuthorized(true);

    // User has WRITER role on component, co-writing enabled
    // And publication is in draft mode
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.writer).onGEDComponent()
        .userIsThePublicationAuthor().enableCoWriting()
        .withDraftStatus();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled();
    assertIsUserAuthorized(true);

    // User has WRITER role on component, co-writing enabled
    // And publication has to be validated
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.writer).onGEDComponent()
        .userIsThePublicationAuthor().enableCoWriting()
        .withToValidateStatus();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled();
    assertIsUserAuthorized(true);

    // User has WRITER role on component, co-writing enabled
    // And publication has been refused
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.writer).onGEDComponent()
        .userIsThePublicationAuthor().enableCoWriting()
        .withRefusedStatus();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled();
    assertIsUserAuthorized(true);

    // User has WRITER role on component, co-writing enabled
    // And publication is not visible
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.writer).onGEDComponent()
        .userIsThePublicationAuthor().enableCoWriting()
        .notVisible();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled();
    assertIsUserAuthorized(true);

    // User has WRITER role on component, co-writing enabled, draft visible with co-writing
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.writer).onGEDComponent()
        .userIsThePublicationAuthor().enableCoWriting().draftVisibleWithCoWriting();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled();
    assertIsUserAuthorized(true);

    // User has WRITER role on component, co-writing enabled, draft visible with co-writing
    // And publication is in draft mode
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.writer).onGEDComponent()
        .userIsThePublicationAuthor().enableCoWriting().draftVisibleWithCoWriting()
        .withDraftStatus();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled();
    assertIsUserAuthorized(true);

    // User has WRITER role on component, co-writing enabled, draft visible with co-writing
    // And publication has to be validated
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.writer).onGEDComponent()
        .userIsThePublicationAuthor().enableCoWriting().draftVisibleWithCoWriting()
        .withToValidateStatus();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled();
    assertIsUserAuthorized(true);

    // User has WRITER role on component, co-writing enabled, draft visible with co-writing
    // And publication has been refused
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.writer).onGEDComponent()
        .userIsThePublicationAuthor().enableCoWriting().draftVisibleWithCoWriting()
        .withRefusedStatus();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled();
    assertIsUserAuthorized(true);

    // User has WRITER role on component, co-writing enabled, draft visible with co-writing
    // And publication is not visible
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.writer).onGEDComponent()
        .userIsThePublicationAuthor().enableCoWriting().draftVisibleWithCoWriting()
        .notVisible();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled();
    assertIsUserAuthorized(true);

    // User has PUBLISHER role on component
    // User rights are verified for sharing action on the document, but sharing is not enabled
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.publisher).onGEDComponent()
        .userIsThePublicationAuthor().onOperationsOf(AccessControlOperation.sharing);
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled();
    assertIsUserAuthorized(false);

    // User has ADMIN role on component
    // User rights are verified for sharing action on the document, but sharing is not enabled
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.admin).onGEDComponent()
        .userIsThePublicationAuthor().onOperationsOf(AccessControlOperation.sharing);
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled();
    assertIsUserAuthorized(false);

    // User has PUBLISHER role on component
    // User rights are verified for sharing action on the document, sharing is enabled
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.publisher).onGEDComponent()
        .userIsThePublicationAuthor().onOperationsOf(AccessControlOperation.sharing)
        .enablePublicationSharingRole(SilverpeasRole.admin);
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled();
    assertIsUserAuthorized(false);

    // User has ADMIN role on component
    // User rights are verified for sharing action on the document, sharing is enabled
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.admin).onGEDComponent()
        .userIsThePublicationAuthor().onOperationsOf(AccessControlOperation.sharing)
        .enablePublicationSharingRole(SilverpeasRole.admin);
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled();
    assertIsUserAuthorized(true);

    // User has USER role on component
    // User is going to modify the publication
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user).onGEDComponent()
        .userIsThePublicationAuthor()
        .onOperationsOf(AccessControlOperation.PERSIST_ACTIONS.iterator().next());
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled();
    assertIsUserAuthorized(false);

    // User has PUBLISHER role on component
    // User is going to modify the publication
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.publisher).onGEDComponent()
        .userIsThePublicationAuthor()
        .onOperationsOf(AccessControlOperation.PERSIST_ACTIONS.iterator().next());
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled();
    assertIsUserAuthorized(true);

    // User has WRITER role on component
    // User is going to modify the publication
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.writer).onGEDComponent()
        .userIsThePublicationAuthor()
        .onOperationsOf(AccessControlOperation.PERSIST_ACTIONS.iterator().next());
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled();
    assertIsUserAuthorized(true);
  }

  @Test
  public void
  testIsUserAuthorizedOnGEDAndPublicationOnRootDirectoryAndUserIsThePublicationAuthorAndRightsOnDirectories() {
    // User has no right on component
    testContext.clear();
    testContext.onGEDComponent().publicationOnRootDirectory().withRightsActivatedOnDirectory()
        .userIsThePublicationAuthor();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetailAndGetAlias();
    assertIsUserAuthorized(false);

    // User has USER role on component
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user).onGEDComponent()
        .publicationOnRootDirectory().withRightsActivatedOnDirectory().userIsThePublicationAuthor();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetAllFatherPK();
    assertIsUserAuthorized(true);

    // User has USER role on component
    // And publication is in draft mode
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user).onGEDComponent()
        .publicationOnRootDirectory().withRightsActivatedOnDirectory().userIsThePublicationAuthor()
        .withDraftStatus();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetAllFatherPK();
    assertIsUserAuthorized(false);

    // User has USER role on component
    // And publication has to be validated
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user).onGEDComponent()
        .publicationOnRootDirectory().withRightsActivatedOnDirectory().userIsThePublicationAuthor()
        .withToValidateStatus();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetAllFatherPK();
    assertIsUserAuthorized(false);

    // User has USER role on component
    // And publication has been refused
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user).onGEDComponent()
        .publicationOnRootDirectory().withRightsActivatedOnDirectory().userIsThePublicationAuthor()
        .withRefusedStatus();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetAllFatherPK();
    assertIsUserAuthorized(false);

    // User has USER role on component
    // And publication is not visible
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user).onGEDComponent()
        .publicationOnRootDirectory().withRightsActivatedOnDirectory().userIsThePublicationAuthor()
        .notVisible();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetAllFatherPK();
    assertIsUserAuthorized(false);

    // User has WRITER role on component
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.writer).onGEDComponent()
        .publicationOnRootDirectory().withRightsActivatedOnDirectory().userIsThePublicationAuthor();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetAllFatherPK();
    assertIsUserAuthorized(true);

    // User has WRITER role on component
    // And publication is in draft mode
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.writer).onGEDComponent()
        .publicationOnRootDirectory().withRightsActivatedOnDirectory().userIsThePublicationAuthor()
        .withDraftStatus();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetAllFatherPK();
    assertIsUserAuthorized(true);

    // User has WRITER role on component
    // And publication has to be validated
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.writer).onGEDComponent()
        .publicationOnRootDirectory().withRightsActivatedOnDirectory().userIsThePublicationAuthor()
        .withToValidateStatus();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetAllFatherPK();
    assertIsUserAuthorized(true);

    // User has WRITER role on component
    // And publication has been refused
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.writer).onGEDComponent()
        .publicationOnRootDirectory().withRightsActivatedOnDirectory().userIsThePublicationAuthor()
        .withRefusedStatus();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetAllFatherPK();
    assertIsUserAuthorized(true);

    // User has WRITER role on component
    // And publication is not visible
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.writer).onGEDComponent()
        .publicationOnRootDirectory().withRightsActivatedOnDirectory().userIsThePublicationAuthor()
        .notVisible();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetAllFatherPK();
    assertIsUserAuthorized(true);

    // User has WRITER role on component, co-writing enabled
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.writer).onGEDComponent()
        .publicationOnRootDirectory().withRightsActivatedOnDirectory().userIsThePublicationAuthor()
        .enableCoWriting();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetAllFatherPK();
    assertIsUserAuthorized(true);

    // User has WRITER role on component, co-writing enabled
    // And publication is in draft mode
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.writer).onGEDComponent()
        .publicationOnRootDirectory().withRightsActivatedOnDirectory().userIsThePublicationAuthor()
        .withDraftStatus().enableCoWriting();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetAllFatherPK();
    assertIsUserAuthorized(true);

    // User has WRITER role on component, co-writing enabled
    // And publication has to be validated
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.writer).onGEDComponent()
        .publicationOnRootDirectory().withRightsActivatedOnDirectory().userIsThePublicationAuthor()
        .withToValidateStatus().enableCoWriting();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetAllFatherPK();
    assertIsUserAuthorized(true);

    // User has WRITER role on component, co-writing enabled
    // And publication has been refused
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.writer).onGEDComponent()
        .publicationOnRootDirectory().withRightsActivatedOnDirectory().userIsThePublicationAuthor()
        .withRefusedStatus().enableCoWriting();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetAllFatherPK();
    assertIsUserAuthorized(true);

    // User has WRITER role on component, co-writing enabled
    // And publication is not visible
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.writer).onGEDComponent()
        .publicationOnRootDirectory().withRightsActivatedOnDirectory().userIsThePublicationAuthor()
        .notVisible().enableCoWriting();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetAllFatherPK();
    assertIsUserAuthorized(true);

    // User has WRITER role on component, co-writing enabled, draft visible with co-writing
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.writer).onGEDComponent()
        .publicationOnRootDirectory().withRightsActivatedOnDirectory().userIsThePublicationAuthor()
        .enableCoWriting().draftVisibleWithCoWriting();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetAllFatherPK();
    assertIsUserAuthorized(true);

    // User has WRITER role on component, co-writing enabled, draft visible with co-writing
    // And publication is in draft mode
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.writer).onGEDComponent()
        .publicationOnRootDirectory().withRightsActivatedOnDirectory().userIsThePublicationAuthor()
        .withDraftStatus().enableCoWriting().draftVisibleWithCoWriting();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetAllFatherPK();
    assertIsUserAuthorized(true);

    // User has WRITER role on component, co-writing enabled, draft visible with co-writing
    // And publication has to be validated
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.writer).onGEDComponent()
        .publicationOnRootDirectory().withRightsActivatedOnDirectory().userIsThePublicationAuthor()
        .withToValidateStatus().enableCoWriting().draftVisibleWithCoWriting();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetAllFatherPK();
    assertIsUserAuthorized(true);

    // User has WRITER role on component, co-writing enabled, draft visible with co-writing
    // And publication has been refused
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.writer).onGEDComponent()
        .publicationOnRootDirectory().withRightsActivatedOnDirectory().userIsThePublicationAuthor()
        .withRefusedStatus().enableCoWriting().draftVisibleWithCoWriting();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetAllFatherPK();
    assertIsUserAuthorized(true);

    // User has WRITER role on component, co-writing enabled, draft visible with co-writing
    // And publication is not visible
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.writer).onGEDComponent()
        .publicationOnRootDirectory().withRightsActivatedOnDirectory().userIsThePublicationAuthor()
        .notVisible().enableCoWriting().draftVisibleWithCoWriting();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetAllFatherPK();
    assertIsUserAuthorized(true);

    // User has PUBLISHER role on component
    // User rights are verified for sharing action on the document, but sharing is not enabled
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.publisher).onGEDComponent()
        .publicationOnRootDirectory().withRightsActivatedOnDirectory().userIsThePublicationAuthor()
        .onOperationsOf(AccessControlOperation.sharing);
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetAllFatherPK();
    assertIsUserAuthorized(false);

    // User has ADMIN role on component
    // User rights are verified for sharing action on the document, but sharing is not enabled
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.admin).onGEDComponent()
        .publicationOnRootDirectory().withRightsActivatedOnDirectory().userIsThePublicationAuthor()
        .onOperationsOf(AccessControlOperation.sharing);
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetAllFatherPK();
    assertIsUserAuthorized(false);

    // User has PUBLISHER role on component
    // User rights are verified for sharing action on the document, sharing is enabled
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.publisher).onGEDComponent()
        .publicationOnRootDirectory().withRightsActivatedOnDirectory().userIsThePublicationAuthor()
        .onOperationsOf(AccessControlOperation.sharing).enablePublicationSharingRole(SilverpeasRole.admin);
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetAllFatherPK();
    assertIsUserAuthorized(false);

    // User has ADMIN role on component
    // User rights are verified for sharing action on the document, sharing is enabled
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.admin).onGEDComponent()
        .publicationOnRootDirectory().withRightsActivatedOnDirectory().userIsThePublicationAuthor()
        .onOperationsOf(AccessControlOperation.sharing).enablePublicationSharingRole(SilverpeasRole.admin);
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetAllFatherPK();
    assertIsUserAuthorized(true);

    // User has USER role on component
    // User is going to modify the publication
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user).onGEDComponent()
        .publicationOnRootDirectory().withRightsActivatedOnDirectory().userIsThePublicationAuthor()
        .onOperationsOf(AccessControlOperation.PERSIST_ACTIONS.iterator().next());
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetAllFatherPK();
    assertIsUserAuthorized(false);

    // User has PUBLISHER role on component
    // User is going to modify the publication
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.publisher).onGEDComponent()
        .publicationOnRootDirectory().withRightsActivatedOnDirectory().userIsThePublicationAuthor()
        .onOperationsOf(AccessControlOperation.PERSIST_ACTIONS.iterator().next());
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetAllFatherPK();
    assertIsUserAuthorized(true);

    // User has WRITER role on component
    // User is going to modify the publication
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.writer).onGEDComponent()
        .publicationOnRootDirectory().withRightsActivatedOnDirectory().userIsThePublicationAuthor()
        .onOperationsOf(AccessControlOperation.PERSIST_ACTIONS.iterator().next());
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetAllFatherPK();
    assertIsUserAuthorized(true);
  }

  @Test
  public void testIsUserAuthorizedOnGEDAndUserHasRightsOnDirectories() {
    // User has no right on component
    testContext.clear();
    testContext.onGEDComponent().withRightsActivatedOnDirectory();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetailAndGetAlias();
    assertIsUserAuthorized(false);

    // User has USER role on component
    // User has no role on directory
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user).onGEDComponent()
        .withRightsActivatedOnDirectory().withNodeUserRoles();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetAllFatherPK().verifyCallOfNodeAccessControllerGetUserRoles()
        .verifyCallOfNodeAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetailAndGetAlias();
    assertIsUserAuthorized(false);

    // User has USER role on component
    // User has no role on directory
    // And publication is in draft mode
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user).onGEDComponent()
        .withRightsActivatedOnDirectory().withNodeUserRoles().withDraftStatus();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetAllFatherPK().verifyCallOfNodeAccessControllerGetUserRoles()
        .verifyCallOfNodeAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetailAndGetAlias();
    assertIsUserAuthorized(false);

    // User has USER role on component
    // User has no role on directory
    // And publication has to be validated
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user).onGEDComponent()
        .withRightsActivatedOnDirectory().withNodeUserRoles().withToValidateStatus();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetAllFatherPK().verifyCallOfNodeAccessControllerGetUserRoles()
        .verifyCallOfNodeAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetailAndGetAlias();
    assertIsUserAuthorized(false);

    // User has USER role on component
    // User has no role on directory
    // And publication has been refused
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user).onGEDComponent()
        .withRightsActivatedOnDirectory().withNodeUserRoles().withRefusedStatus();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetAllFatherPK().verifyCallOfNodeAccessControllerGetUserRoles()
        .verifyCallOfNodeAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetailAndGetAlias();
    assertIsUserAuthorized(false);

    // User has USER role on component
    // User has no role on directory
    // And publication is not visible
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user).onGEDComponent()
        .withRightsActivatedOnDirectory().withNodeUserRoles().notVisible();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetAllFatherPK().verifyCallOfNodeAccessControllerGetUserRoles()
        .verifyCallOfNodeAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetailAndGetAlias();
    assertIsUserAuthorized(false);

    // User has ADMIN role on component
    // User has USER on directory
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.admin).onGEDComponent()
        .withRightsActivatedOnDirectory().withNodeUserRoles(SilverpeasRole.user);
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetAllFatherPK().verifyCallOfNodeAccessControllerGetUserRoles()
        .verifyCallOfNodeAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(true);

    // User has ADMIN role on component
    // User has USER on directory
    // And publication is in draft mode
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.admin).onGEDComponent()
        .withRightsActivatedOnDirectory().withNodeUserRoles(SilverpeasRole.user).withDraftStatus();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetAllFatherPK().verifyCallOfNodeAccessControllerGetUserRoles()
        .verifyCallOfNodeAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(false);

    // User has ADMIN role on component
    // User has USER on directory
    // And publication has to be validated
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.admin).onGEDComponent()
        .withRightsActivatedOnDirectory().withNodeUserRoles(SilverpeasRole.user).withToValidateStatus();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetAllFatherPK().verifyCallOfNodeAccessControllerGetUserRoles()
        .verifyCallOfNodeAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(false);

    // User has ADMIN role on component
    // User has USER on directory
    // And publication has been refused
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.admin).onGEDComponent()
        .withRightsActivatedOnDirectory().withNodeUserRoles(SilverpeasRole.user).withRefusedStatus();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetAllFatherPK().verifyCallOfNodeAccessControllerGetUserRoles()
        .verifyCallOfNodeAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(false);

    // User has ADMIN role on component
    // User has USER on directory
    // And publication is not visible
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.admin).onGEDComponent()
        .withRightsActivatedOnDirectory().withNodeUserRoles(SilverpeasRole.user).notVisible();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetAllFatherPK().verifyCallOfNodeAccessControllerGetUserRoles()
        .verifyCallOfNodeAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(false);

    // User has ADMIN role on component
    // User has WRITER on directory
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.admin).onGEDComponent()
        .withRightsActivatedOnDirectory().withNodeUserRoles(SilverpeasRole.writer);
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetAllFatherPK().verifyCallOfNodeAccessControllerGetUserRoles()
        .verifyCallOfNodeAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(true);

    // User has ADMIN role on component
    // User has WRITER on directory
    // And publication is in draft mode
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.admin).onGEDComponent()
        .withRightsActivatedOnDirectory().withNodeUserRoles(SilverpeasRole.writer).withDraftStatus();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetAllFatherPK().verifyCallOfNodeAccessControllerGetUserRoles()
        .verifyCallOfNodeAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(false);

    // User has ADMIN role on component
    // User has WRITER on directory
    // And publication has to be validated
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.admin).onGEDComponent()
        .withRightsActivatedOnDirectory().withNodeUserRoles(SilverpeasRole.writer).withToValidateStatus();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetAllFatherPK().verifyCallOfNodeAccessControllerGetUserRoles()
        .verifyCallOfNodeAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(false);

    // User has ADMIN role on component
    // User has WRITER on directory
    // And publication has been refused
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.admin).onGEDComponent()
        .withRightsActivatedOnDirectory().withNodeUserRoles(SilverpeasRole.writer).withRefusedStatus();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetAllFatherPK().verifyCallOfNodeAccessControllerGetUserRoles()
        .verifyCallOfNodeAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(false);

    // User has ADMIN role on component
    // User has WRITER on directory
    // And publication is not visible
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.admin).onGEDComponent()
        .withRightsActivatedOnDirectory().withNodeUserRoles(SilverpeasRole.writer).notVisible();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetAllFatherPK().verifyCallOfNodeAccessControllerGetUserRoles()
        .verifyCallOfNodeAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(false);

    // User has ADMIN role on component, co-writing enabled
    // User has WRITER on directory
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.admin).onGEDComponent()
        .withRightsActivatedOnDirectory().withNodeUserRoles(SilverpeasRole.writer)
        .enableCoWriting();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetAllFatherPK().verifyCallOfNodeAccessControllerGetUserRoles()
        .verifyCallOfNodeAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(true);

    // User has ADMIN role on component, co-writing enabled
    // User has WRITER on directory
    // And publication is in draft mode
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.admin).onGEDComponent()
        .withRightsActivatedOnDirectory().withNodeUserRoles(SilverpeasRole.writer)
        .withDraftStatus().enableCoWriting();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetAllFatherPK().verifyCallOfNodeAccessControllerGetUserRoles()
        .verifyCallOfNodeAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(false);

    // User has ADMIN role on component, co-writing enabled
    // User has WRITER on directory
    // And publication has to be validated
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.admin).onGEDComponent()
        .withRightsActivatedOnDirectory().withNodeUserRoles(SilverpeasRole.writer)
        .withToValidateStatus().enableCoWriting();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetAllFatherPK().verifyCallOfNodeAccessControllerGetUserRoles()
        .verifyCallOfNodeAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(true);

    // User has ADMIN role on component, co-writing enabled
    // User has WRITER on directory
    // And publication has been refused
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.admin).onGEDComponent()
        .withRightsActivatedOnDirectory().withNodeUserRoles(SilverpeasRole.writer)
        .withRefusedStatus().enableCoWriting();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetAllFatherPK().verifyCallOfNodeAccessControllerGetUserRoles()
        .verifyCallOfNodeAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(true);

    // User has ADMIN role on component, co-writing enabled
    // User has WRITER on directory
    // And publication is not visible
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.admin).onGEDComponent()
        .withRightsActivatedOnDirectory().withNodeUserRoles(SilverpeasRole.writer)
        .notVisible().enableCoWriting();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetAllFatherPK().verifyCallOfNodeAccessControllerGetUserRoles()
        .verifyCallOfNodeAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(true);

    // User has ADMIN role on component, co-writing enabled, draft visible with co-writing
    // User has WRITER on directory
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.admin).onGEDComponent()
        .withRightsActivatedOnDirectory().withNodeUserRoles(SilverpeasRole.writer)
        .enableCoWriting().draftVisibleWithCoWriting();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetAllFatherPK().verifyCallOfNodeAccessControllerGetUserRoles()
        .verifyCallOfNodeAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(true);

    // User has ADMIN role on component, co-writing enabled, draft visible with co-writing
    // User has WRITER on directory
    // And publication is in draft mode
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.admin).onGEDComponent()
        .withRightsActivatedOnDirectory().withNodeUserRoles(SilverpeasRole.writer)
        .withDraftStatus().enableCoWriting().draftVisibleWithCoWriting();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetAllFatherPK().verifyCallOfNodeAccessControllerGetUserRoles()
        .verifyCallOfNodeAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(true);

    // User has ADMIN role on component, co-writing enabled, draft visible with co-writing
    // User has WRITER on directory
    // And publication has to be validated
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.admin).onGEDComponent()
        .withRightsActivatedOnDirectory().withNodeUserRoles(SilverpeasRole.writer)
        .withToValidateStatus().enableCoWriting().draftVisibleWithCoWriting();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetAllFatherPK().verifyCallOfNodeAccessControllerGetUserRoles()
        .verifyCallOfNodeAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(true);

    // User has ADMIN role on component, co-writing enabled, draft visible with co-writing
    // User has WRITER on directory
    // And publication has been refused
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.admin).onGEDComponent()
        .withRightsActivatedOnDirectory().withNodeUserRoles(SilverpeasRole.writer)
        .withRefusedStatus().enableCoWriting().draftVisibleWithCoWriting();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetAllFatherPK().verifyCallOfNodeAccessControllerGetUserRoles()
        .verifyCallOfNodeAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(true);

    // User has ADMIN role on component, co-writing enabled, draft visible with co-writing
    // User has WRITER on directory
    // And publication is not visible
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.admin).onGEDComponent()
        .withRightsActivatedOnDirectory().withNodeUserRoles(SilverpeasRole.writer)
        .notVisible().enableCoWriting().draftVisibleWithCoWriting();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetAllFatherPK().verifyCallOfNodeAccessControllerGetUserRoles()
        .verifyCallOfNodeAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(true);

    // User has ADMIN role on component
    // User has PUBLISHER on directory
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.admin).onGEDComponent()
        .withRightsActivatedOnDirectory().withNodeUserRoles(SilverpeasRole.publisher);
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetAllFatherPK().verifyCallOfNodeAccessControllerGetUserRoles()
        .verifyCallOfNodeAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(true);

    // User has ADMIN role on component
    // User has PUBLISHER on directory
    // And publication is in draft mode
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.admin).onGEDComponent()
        .withRightsActivatedOnDirectory().withNodeUserRoles(SilverpeasRole.publisher).withDraftStatus();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetAllFatherPK().verifyCallOfNodeAccessControllerGetUserRoles()
        .verifyCallOfNodeAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(true);

    // User has ADMIN role on component
    // User has PUBLISHER on directory
    // And publication has to be validated
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.admin).onGEDComponent()
        .withRightsActivatedOnDirectory().withNodeUserRoles(SilverpeasRole.publisher).withToValidateStatus();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetAllFatherPK().verifyCallOfNodeAccessControllerGetUserRoles()
        .verifyCallOfNodeAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(true);

    // User has ADMIN role on component
    // User has PUBLISHER on directory
    // And publication has been refused
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.admin).onGEDComponent()
        .withRightsActivatedOnDirectory().withNodeUserRoles(SilverpeasRole.publisher).withRefusedStatus();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetAllFatherPK().verifyCallOfNodeAccessControllerGetUserRoles()
        .verifyCallOfNodeAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(true);

    // User has ADMIN role on component
    // User has PUBLISHER on directory
    // And publication is not visible
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.admin).onGEDComponent()
        .withRightsActivatedOnDirectory().withNodeUserRoles(SilverpeasRole.publisher).notVisible();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetAllFatherPK().verifyCallOfNodeAccessControllerGetUserRoles()
        .verifyCallOfNodeAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(true);

    // User has ADMIN role on component
    // User has no role on directory
    // User rights are verified for sharing action on the document, sharing is enabled
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.admin).onGEDComponent()
        .withRightsActivatedOnDirectory().withNodeUserRoles()
        .onOperationsOf(AccessControlOperation.sharing).enablePublicationSharingRole(SilverpeasRole.admin);
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetAllFatherPK()
        .verifyCallOfNodeAccessControllerGetUserRoles()
        .verifyCallOfNodeAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetailAndGetAlias();
    assertIsUserAuthorized(false);

    // User has USER role on component
    // User has PUBLISHER role on directory
    // User rights are verified for sharing action on the document, but sharing is not enabled
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user).onGEDComponent()
        .withRightsActivatedOnDirectory().withNodeUserRoles(SilverpeasRole.publisher)
        .onOperationsOf(AccessControlOperation.sharing);
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetAllFatherPK()
        .verifyCallOfNodeAccessControllerGetUserRoles()
        .verifyCallOfNodeAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(false);

    // User has USER role on component
    // User has ADMIN role on directory
    // User rights are verified for sharing action on the document, but sharing is not enabled
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user).onGEDComponent()
        .withRightsActivatedOnDirectory().withNodeUserRoles(SilverpeasRole.admin)
        .onOperationsOf(AccessControlOperation.sharing);
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetAllFatherPK()
        .verifyCallOfNodeAccessControllerGetUserRoles()
        .verifyCallOfNodeAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(false);

    // User has USER role on component
    // User has PUBLISHER role on directory
    // User rights are verified for sharing action on the document, sharing is enabled
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user).onGEDComponent()
        .withRightsActivatedOnDirectory().withNodeUserRoles(SilverpeasRole.publisher)
        .onOperationsOf(AccessControlOperation.sharing).enablePublicationSharingRole(SilverpeasRole.admin);
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetAllFatherPK().verifyCallOfNodeAccessControllerGetUserRoles()
        .verifyCallOfNodeAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(false);

    // User has USER role on component
    // User has ADMIN role on directory
    // User rights are verified for sharing action on the document, sharing is enabled
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user).onGEDComponent()
        .withRightsActivatedOnDirectory().withNodeUserRoles(SilverpeasRole.admin)
        .onOperationsOf(AccessControlOperation.sharing).enablePublicationSharingRole(SilverpeasRole.admin);
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetAllFatherPK().verifyCallOfNodeAccessControllerGetUserRoles()
        .verifyCallOfNodeAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(true);

    // User has USER role on component
    // User has USER role on directory
    // User is going to modify the publication
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user).onGEDComponent()
        .withRightsActivatedOnDirectory().withNodeUserRoles(SilverpeasRole.user)
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
    // User is going to modify the publication
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user).onGEDComponent()
        .withRightsActivatedOnDirectory().withNodeUserRoles(SilverpeasRole.publisher)
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
    // User is going to modify the publication
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user).onGEDComponent()
        .withRightsActivatedOnDirectory().withNodeUserRoles(SilverpeasRole.writer)
        .onOperationsOf(AccessControlOperation.PERSIST_ACTIONS.iterator().next());
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetAllFatherPK().verifyCallOfNodeAccessControllerGetUserRoles()
        .verifyCallOfNodeAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(false);

    // User has USER role on component, co-writing enabled, draft visible with co-writing
    // User has WRITER role on directory
    // User is going to modify the publication
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user).onGEDComponent()
        .enableCoWriting()
        .withRightsActivatedOnDirectory().withNodeUserRoles(SilverpeasRole.writer)
        .onOperationsOf(AccessControlOperation.PERSIST_ACTIONS.iterator().next());
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetAllFatherPK().verifyCallOfNodeAccessControllerGetUserRoles()
        .verifyCallOfNodeAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(true);

    // User has USER role on component, co-writing enabled, draft visible with co-writing
    // User has WRITER role on directory
    // User is going to modify the publication
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user).onGEDComponent()
        .enableCoWriting().draftVisibleWithCoWriting()
        .withRightsActivatedOnDirectory().withNodeUserRoles(SilverpeasRole.writer)
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
  public void testIsUserAuthorizedOnGEDAndUserIsThePublicationAuthorAndUserHasRightsOnDirectories
      () {
    // User has no right on component
    testContext.clear();
    testContext.onGEDComponent().withRightsActivatedOnDirectory().userIsThePublicationAuthor();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetailAndGetAlias();
    assertIsUserAuthorized(false);

    // User has USER role on component
    // User has no role on directory
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user).onGEDComponent()
        .withRightsActivatedOnDirectory().withNodeUserRoles().userIsThePublicationAuthor();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetAllFatherPK().verifyCallOfNodeAccessControllerGetUserRoles()
        .verifyCallOfNodeAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetailAndGetAlias();
    assertIsUserAuthorized(false);

    // User has USER role on component
    // User has USER on directory
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user).onGEDComponent()
        .withRightsActivatedOnDirectory().withNodeUserRoles(SilverpeasRole.user)
        .userIsThePublicationAuthor();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetAllFatherPK().verifyCallOfNodeAccessControllerGetUserRoles()
        .verifyCallOfNodeAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(true);

    // User has USER role on component
    // User has USER on directory
    // And publication is in draft mode
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user).onGEDComponent()
        .withRightsActivatedOnDirectory().withNodeUserRoles(SilverpeasRole.user)
        .userIsThePublicationAuthor().withDraftStatus();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetAllFatherPK().verifyCallOfNodeAccessControllerGetUserRoles()
        .verifyCallOfNodeAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(false);

    // User has USER role on component
    // User has USER on directory
    // And publication has to be validated
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user).onGEDComponent()
        .withRightsActivatedOnDirectory().withNodeUserRoles(SilverpeasRole.user)
        .userIsThePublicationAuthor().withToValidateStatus();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetAllFatherPK().verifyCallOfNodeAccessControllerGetUserRoles()
        .verifyCallOfNodeAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(false);

    // User has USER role on component
    // User has USER on directory
    // And publication has been refused
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user).onGEDComponent()
        .withRightsActivatedOnDirectory().withNodeUserRoles(SilverpeasRole.user)
        .userIsThePublicationAuthor().withRefusedStatus();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetAllFatherPK().verifyCallOfNodeAccessControllerGetUserRoles()
        .verifyCallOfNodeAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(false);

    // User has USER role on component
    // User has USER on directory
    // And publication is not visible
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user).onGEDComponent()
        .withRightsActivatedOnDirectory().withNodeUserRoles(SilverpeasRole.user)
        .userIsThePublicationAuthor().notVisible();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetAllFatherPK().verifyCallOfNodeAccessControllerGetUserRoles()
        .verifyCallOfNodeAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(false);

    // User has USER role on component
    // User has WRITER on directory
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user).onGEDComponent()
        .withRightsActivatedOnDirectory().withNodeUserRoles(SilverpeasRole.writer)
        .userIsThePublicationAuthor();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetAllFatherPK().verifyCallOfNodeAccessControllerGetUserRoles()
        .verifyCallOfNodeAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(true);

    // User has USER role on component
    // User has WRITER on directory
    // And publication is in draft mode
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user).onGEDComponent()
        .withRightsActivatedOnDirectory().withNodeUserRoles(SilverpeasRole.writer)
        .userIsThePublicationAuthor().withDraftStatus();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetAllFatherPK().verifyCallOfNodeAccessControllerGetUserRoles()
        .verifyCallOfNodeAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(true);

    // User has USER role on component
    // User has WRITER on directory
    // And publication has to be validated
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user).onGEDComponent()
        .withRightsActivatedOnDirectory().withNodeUserRoles(SilverpeasRole.writer)
        .userIsThePublicationAuthor().withToValidateStatus();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetAllFatherPK().verifyCallOfNodeAccessControllerGetUserRoles()
        .verifyCallOfNodeAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(true);

    // User has USER role on component
    // User has WRITER on directory
    // And publication has been refused
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user).onGEDComponent()
        .withRightsActivatedOnDirectory().withNodeUserRoles(SilverpeasRole.writer)
        .userIsThePublicationAuthor().withRefusedStatus();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetAllFatherPK().verifyCallOfNodeAccessControllerGetUserRoles()
        .verifyCallOfNodeAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(true);

    // User has USER role on component
    // User has WRITER on directory
    // And publication is not visible
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user).onGEDComponent()
        .withRightsActivatedOnDirectory().withNodeUserRoles(SilverpeasRole.writer)
        .userIsThePublicationAuthor().notVisible();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetAllFatherPK().verifyCallOfNodeAccessControllerGetUserRoles()
        .verifyCallOfNodeAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(true);

    // User has USER role on component, co-writing enabled
    // User has WRITER on directory
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user).onGEDComponent()
        .withRightsActivatedOnDirectory().withNodeUserRoles(SilverpeasRole.writer)
        .userIsThePublicationAuthor().enableCoWriting();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetAllFatherPK().verifyCallOfNodeAccessControllerGetUserRoles()
        .verifyCallOfNodeAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(true);

    // User has USER role on component, co-writing enabled
    // User has WRITER on directory
    // And publication is in draft mode
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user).onGEDComponent()
        .withRightsActivatedOnDirectory().withNodeUserRoles(SilverpeasRole.writer)
        .userIsThePublicationAuthor().withDraftStatus().enableCoWriting();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetAllFatherPK().verifyCallOfNodeAccessControllerGetUserRoles()
        .verifyCallOfNodeAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(true);

    // User has USER role on component, co-writing enabled
    // User has WRITER on directory
    // And publication has to be validated
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user).onGEDComponent()
        .withRightsActivatedOnDirectory().withNodeUserRoles(SilverpeasRole.writer)
        .userIsThePublicationAuthor().withToValidateStatus().enableCoWriting();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetAllFatherPK().verifyCallOfNodeAccessControllerGetUserRoles()
        .verifyCallOfNodeAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(true);

    // User has USER role on component, co-writing enabled
    // User has WRITER on directory
    // And publication has been refused
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user).onGEDComponent()
        .withRightsActivatedOnDirectory().withNodeUserRoles(SilverpeasRole.writer)
        .userIsThePublicationAuthor().withRefusedStatus().enableCoWriting();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetAllFatherPK().verifyCallOfNodeAccessControllerGetUserRoles()
        .verifyCallOfNodeAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(true);

    // User has USER role on component, co-writing enabled
    // User has WRITER on directory
    // And publication is not visible
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user).onGEDComponent()
        .withRightsActivatedOnDirectory().withNodeUserRoles(SilverpeasRole.writer)
        .userIsThePublicationAuthor().notVisible().enableCoWriting();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetAllFatherPK().verifyCallOfNodeAccessControllerGetUserRoles()
        .verifyCallOfNodeAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(true);

    // User has USER role on component, co-writing enabled, draft visible with co-writing
    // User has WRITER on directory
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user).onGEDComponent()
        .withRightsActivatedOnDirectory().withNodeUserRoles(SilverpeasRole.writer)
        .userIsThePublicationAuthor()
        .enableCoWriting().draftVisibleWithCoWriting();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetAllFatherPK().verifyCallOfNodeAccessControllerGetUserRoles()
        .verifyCallOfNodeAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(true);

    // User has USER role on component, co-writing enabled, draft visible with co-writing
    // User has WRITER on directory
    // And publication is in draft mode
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user).onGEDComponent()
        .withRightsActivatedOnDirectory().withNodeUserRoles(SilverpeasRole.writer)
        .userIsThePublicationAuthor().withDraftStatus()
        .enableCoWriting().draftVisibleWithCoWriting();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetAllFatherPK().verifyCallOfNodeAccessControllerGetUserRoles()
        .verifyCallOfNodeAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(true);

    // User has USER role on component, co-writing enabled, draft visible with co-writing
    // User has WRITER on directory
    // And publication has to be validated
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user).onGEDComponent()
        .withRightsActivatedOnDirectory().withNodeUserRoles(SilverpeasRole.writer)
        .userIsThePublicationAuthor().withToValidateStatus()
        .enableCoWriting().draftVisibleWithCoWriting();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetAllFatherPK().verifyCallOfNodeAccessControllerGetUserRoles()
        .verifyCallOfNodeAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(true);

    // User has USER role on component, co-writing enabled, draft visible with co-writing
    // User has WRITER on directory
    // And publication has been refused
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user).onGEDComponent()
        .withRightsActivatedOnDirectory().withNodeUserRoles(SilverpeasRole.writer)
        .userIsThePublicationAuthor().withRefusedStatus()
        .enableCoWriting().draftVisibleWithCoWriting();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetAllFatherPK().verifyCallOfNodeAccessControllerGetUserRoles()
        .verifyCallOfNodeAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(true);

    // User has USER role on component, co-writing enabled, draft visible with co-writing
    // User has WRITER on directory
    // And publication is not visible
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user).onGEDComponent()
        .withRightsActivatedOnDirectory().withNodeUserRoles(SilverpeasRole.writer)
        .userIsThePublicationAuthor().notVisible()
        .enableCoWriting().draftVisibleWithCoWriting();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetAllFatherPK().verifyCallOfNodeAccessControllerGetUserRoles()
        .verifyCallOfNodeAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(true);

    // User has ADMIN role on component
    // User has no role on directory
    // User rights are verified for sharing action on the document, sharing is enabled
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.admin).onGEDComponent()
        .withRightsActivatedOnDirectory().withNodeUserRoles().userIsThePublicationAuthor()
        .onOperationsOf(AccessControlOperation.sharing).enablePublicationSharingRole(SilverpeasRole.admin);
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetAllFatherPK()
        .verifyCallOfNodeAccessControllerGetUserRoles()
        .verifyCallOfNodeAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetailAndGetAlias();
    assertIsUserAuthorized(false);

    // User has USER role on component
    // User has PUBLISHER role on directory
    // User rights are verified for sharing action on the document, but sharing is not enabled
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user).onGEDComponent()
        .withRightsActivatedOnDirectory().withNodeUserRoles(SilverpeasRole.publisher)
        .userIsThePublicationAuthor().onOperationsOf(AccessControlOperation.sharing);
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetAllFatherPK().verifyCallOfNodeAccessControllerGetUserRoles()
        .verifyCallOfNodeAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(false);

    // User has USER role on component
    // User has ADMIN role on directory
    // User rights are verified for sharing action on the document, but sharing is not enabled
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user).onGEDComponent()
        .withRightsActivatedOnDirectory().withNodeUserRoles(SilverpeasRole.admin)
        .userIsThePublicationAuthor().onOperationsOf(AccessControlOperation.sharing);
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetAllFatherPK().verifyCallOfNodeAccessControllerGetUserRoles()
        .verifyCallOfNodeAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(false);

    // User has USER role on component
    // User has PUBLISHER role on directory
    // User rights are verified for sharing action on the document, sharing is enabled
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user).onGEDComponent()
        .withRightsActivatedOnDirectory().withNodeUserRoles(SilverpeasRole.publisher)
        .userIsThePublicationAuthor().onOperationsOf(AccessControlOperation.sharing)
        .enablePublicationSharingRole(SilverpeasRole.admin);
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetAllFatherPK().verifyCallOfNodeAccessControllerGetUserRoles()
        .verifyCallOfNodeAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(false);

    // User has USER role on component
    // User has ADMIN role on directory
    // User rights are verified for sharing action on the document, sharing is enabled
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user).onGEDComponent()
        .withRightsActivatedOnDirectory().withNodeUserRoles(SilverpeasRole.admin)
        .userIsThePublicationAuthor().onOperationsOf(AccessControlOperation.sharing)
        .enablePublicationSharingRole(SilverpeasRole.admin);
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetAllFatherPK().verifyCallOfNodeAccessControllerGetUserRoles()
        .verifyCallOfNodeAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(true);

    // User has USER role on component
    // User has USER role on directory
    // User is going to modify the publication
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user).onGEDComponent()
        .withRightsActivatedOnDirectory().withNodeUserRoles(SilverpeasRole.user)
        .userIsThePublicationAuthor()
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
    // User is going to modify the publication
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user).onGEDComponent()
        .withRightsActivatedOnDirectory().withNodeUserRoles(SilverpeasRole.publisher)
        .userIsThePublicationAuthor()
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
    // User is going to modify the publication
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user).onGEDComponent()
        .withRightsActivatedOnDirectory().withNodeUserRoles(SilverpeasRole.writer)
        .userIsThePublicationAuthor()
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
   * @param expectedUserAuthorization the expected user authorization to verify
   */
  private void assertIsUserAuthorized(boolean expectedUserAuthorization) {
    testContext.setup();
    PublicationPK publicationPK =
        new PublicationPK("124", testContext.isGED ? "kmelia26" : "yellowpages38");
    publicationPK.setId(
        testContext.isPubIdNull ? null : (testContext.isWrongIdentifierFormat ? "dummyId" : "124"));
    boolean result =
        testInstance.isUserAuthorized(userId, publicationPK, testContext.accessControlContext);
    assertThat(result, is(expectedUserAuthorization));
    testContext.results().verifyMethodCalls();
  }

  private class TestContext {
    private boolean userIsAnonymous;
    private boolean isGED;
    private boolean isCoWriting;
    private boolean isDraftVisibleWithCoWriting;
    private SilverpeasRole publicationSharingRole;
    private boolean isPubIdNull;
    private boolean isPublicationNotExisting;
    private String pubStatus;
    private boolean isPubVisible;
    private boolean isWrongIdentifierFormat;
    private AccessControlContext accessControlContext;
    private EnumSet<SilverpeasRole> componentUserRoles;
    private boolean isRightsOnDirectories;
    private EnumSet<SilverpeasRole> aliasUserRoles;
    private EnumSet<SilverpeasRole> nodeNotInheritedUserRoles;
    private boolean isPublicationOnRootDirectory;
    private TestVerifyResults testVerifyResults;
    private boolean isUserThePublicationAuthor;

    public void clear() {
      Mockito.reset(user, componentAccessController, nodeAccessController, publicationService);
      when(componentAccessController.isTopicTrackerSupported(anyString())).thenAnswer(
          invocation -> {
            String instanceId = (String) invocation.getArguments()[0];
            return instanceId.startsWith("kmelia") || instanceId.startsWith("kmax") ||
                instanceId.startsWith("toolbox");
          });
      userIsAnonymous = false;
      isGED = false;
      isCoWriting = false;
      isDraftVisibleWithCoWriting = false;
      publicationSharingRole = null;
      isPubIdNull = false;
      pubStatus = PublicationDetail.VALID_STATUS;
      isPubVisible = true;
      isPublicationNotExisting = false;
      isWrongIdentifierFormat = false;
      accessControlContext = AccessControlContext.init();
      componentUserRoles = EnumSet.noneOf(SilverpeasRole.class);
      isRightsOnDirectories = false;
      isPublicationOnRootDirectory = false;
      aliasUserRoles = null;
      nodeNotInheritedUserRoles = null;
      testVerifyResults = new TestVerifyResults();
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

    public TestContext draftVisibleWithCoWriting() {
      isDraftVisibleWithCoWriting = true;
      return this;
    }

    public TestContext enablePublicationSharingRole(SilverpeasRole role) {
      publicationSharingRole = role;
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

    public TestContext publicationIdIsNull() {
      isPubIdNull = true;
      return this;
    }

    public TestContext withDraftStatus() {
      pubStatus = PublicationDetail.DRAFT_STATUS;
      return this;
    }

    public TestContext withToValidateStatus() {
      pubStatus = PublicationDetail.TO_VALIDATE_STATUS;
      return this;
    }

    public TestContext withRefusedStatus() {
      pubStatus = PublicationDetail.REFUSED_STATUS;
      return this;
    }

    public TestContext notVisible() {
      isPubVisible = false;
      return this;
    }

    public TestContext publicationIdHasWrongFormat() {
      isWrongIdentifierFormat = true;
      return this;
    }

    public TestContext publicationIsNotExisting() {
      isPublicationNotExisting = true;
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

    public TestContext withAliasUserRoles(SilverpeasRole... roles) {
      if (aliasUserRoles == null) {
        aliasUserRoles = EnumSet.noneOf(SilverpeasRole.class);
      }
      Collections.addAll(aliasUserRoles, roles);
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
      when(user.getId()).thenReturn(userId);
      when(user.isAnonymous()).thenReturn(userIsAnonymous);
      when(componentAccessController.isTopicTrackerSupported(anyString())).thenAnswer(
          invocation -> {
            String instanceId = (String) invocation.getArguments()[0];
            return instanceId.startsWith("kmelia") || instanceId.startsWith("kmax") ||
                instanceId.startsWith("toolbox");
          });
      when(componentAccessController
          .getUserRoles(anyString(), anyString(), any(AccessControlContext.class)))
          .then(new Returns(componentUserRoles));
      when(componentAccessController.isUserAuthorized(any(EnumSet.class)))
          .then(new Returns(!componentUserRoles.isEmpty()));
      when(componentAccessController.isRightOnTopicsEnabled(anyString()))
          .then(new Returns(isRightsOnDirectories));
      when(componentAccessController.isCoWritingEnabled(anyString()))
          .then(new Returns(isCoWriting));
      when(componentAccessController.isPublicationSharingEnabledForRole(anyString(), anyObject()))
          .thenAnswer((Answer<Boolean>) i -> {
            SilverpeasRole role = (SilverpeasRole) i.getArguments()[1];
            return publicationSharingRole != null && role.isGreaterThanOrEquals(publicationSharingRole);
          });
      when(nodeAccessController
          .getUserRoles(anyString(), any(NodePK.class), any(AccessControlContext.class))).then(
          new Returns(isRightsOnDirectories ?
              (nodeNotInheritedUserRoles == null ? componentUserRoles : nodeNotInheritedUserRoles) :
              (aliasUserRoles == null ? componentUserRoles : aliasUserRoles)));
      when(nodeAccessController.isUserAuthorized(any(EnumSet.class)))
          .then(invocation -> CollectionUtil.isNotEmpty((EnumSet) invocation.getArguments()[0]));
      when(publicationService.getDetail(any(PublicationPK.class))).then(invocation -> {
        if (isPublicationNotExisting) {
          return null;
        }
        PublicationDetail publi = new PublicationDetail();
        publi.setPk((PublicationPK) invocation.getArguments()[0]);
        publi.setStatus(testContext.pubStatus);
        publi.setCreatorId(testContext.isUserThePublicationAuthor ? userId : "otherUserId");
        if (!testContext.isPubVisible) {
          final Visibility visibility = mock(Visibility.class);
          when(visibility.isVisible()).thenReturn(false);
          reflectionRule.setField(publi, visibility, "visibility");
        }
        return publi;
      });
      when(publicationService.getAllFatherPK(any(PublicationPK.class))).then(invocation -> {
        Collection<NodePK> nodes = new ArrayList<>();
        if (!testContext.isPublicationOnRootDirectory) {
          nodes.add(new NodePK("nodeId"));
        }
        return nodes;
      });
      when(publicationService.getAllLocations(any(PublicationPK.class))).then(invocation -> {
        Collection<Location> locations = new ArrayList<>();
        if (testContext.aliasUserRoles != null) {
          locations.add(
              new Location("nodeId", ((PublicationPK) invocation.getArguments()[0]).getInstanceId()));
        }
        return locations;
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

    @SuppressWarnings("unchecked")
    public void verifyMethodCalls() {
      verify(componentAccessController, times(nbCallOfComponentAccessControllerGetUserRoles))
          .getUserRoles(anyString(), anyString(), any(AccessControlContext.class));
      verify(componentAccessController, times(0))
          .isUserAuthorized(anyString(), anyString(), any(AccessControlContext.class));
      verify(componentAccessController, times(nbCallOfComponentAccessControllerIsUserAuthorized))
          .isUserAuthorized(any(EnumSet.class));
      verify(componentAccessController,
          times(nbCallOfComponentAccessControllerIsRightOnTopicsEnabled))
          .isRightOnTopicsEnabled(anyString());
      verify(nodeAccessController, times(nbCallOfNodeAccessControllerGetUserRoles))
          .getUserRoles(anyString(), any(NodePK.class), any(AccessControlContext.class));
      verify(nodeAccessController, times(nbCallOfNodeAccessControllerIsUserAuthorized))
          .isUserAuthorized(any(EnumSet.class));
      verify(publicationService, times(nbCallOfPublicationBmGetDetail))
          .getDetail(any(PublicationPK.class));
      verify(publicationService, times(nbCallOfPublicationBmGetAllFatherPK))
          .getAllFatherPK(any(PublicationPK.class));
      verify(publicationService, times(nbCallOfPublicationBmGetAlias))
          .getAllLocations(any(PublicationPK.class));
    }
  }

  private class PublicationAccessController4Test extends PublicationAccessController {

    @Override
    ComponentInstancePublicationAccessControlExtension getComponentExtension(
        final String instanceId) {
      final DefaultInstancePublicationAccessControlExtension4Test extension =
          new DefaultInstancePublicationAccessControlExtension4Test(testContext);
      reflectionRule.setField(extension, componentAccessController, "componentAccessController");
      return extension;
    }
  }

  private static class DefaultInstancePublicationAccessControlExtension4Test
      extends DefaultInstancePublicationAccessControlExtension {
    private TestContext testContext;

    DefaultInstancePublicationAccessControlExtension4Test(final TestContext testContext) {
      this.testContext = testContext;
    }

    @Override
    protected boolean isDraftVisibleWithCoWriting() {
      return testContext.isDraftVisibleWithCoWriting;
    }
  }
}