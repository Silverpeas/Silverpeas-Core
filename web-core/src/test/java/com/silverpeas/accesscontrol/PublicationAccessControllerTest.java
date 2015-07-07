/*
 * Copyright (C) 2000 - 2015 Silverpeas
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
import com.silverpeas.util.CollectionUtil;
import com.stratelia.webactiv.SilverpeasRole;
import com.stratelia.webactiv.util.node.model.NodePK;
import com.stratelia.webactiv.util.publication.control.PublicationBm;
import com.stratelia.webactiv.util.publication.model.Alias;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;
import com.stratelia.webactiv.util.publication.model.PublicationPK;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.internal.stubbing.answers.Returns;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.silverpeas.core.admin.OrganisationController;
import org.silverpeas.test.rule.MockByReflectionRule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * @author Yohann Chastagnier
 */
public class PublicationAccessControllerTest {


  private static final String userId = "bart";

  private OrganisationController controller;
  private PublicationBm publicationBm;

  private ComponentAccessController componentAccessController;
  private NodeAccessController nodeAccessController;
  private PublicationAccessControllerForTest testInstance;
  private TestContext testContext;

  @Rule
  public MockByReflectionRule reflectionRule = new MockByReflectionRule();

  @Before
  public void setup() {
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
    Map<String, WAComponent> waComponentMap = new HashMap<String, WAComponent>();
    waComponentMap.put("kmelia", kmeliaComponent);
    waComponentMap.put("yellowpages", yellowComponent);

    reflectionRule.setField(Instanciateur.class, waComponentMap, "componentsByName");

    controller = mock(OrganisationController.class);
    componentAccessController = mock(ComponentAccessController.class);
    nodeAccessController = mock(NodeAccessController.class);
    publicationBm = mock(PublicationBm.class);
    testInstance = new PublicationAccessControllerForTest();
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
        .onOperationsOf(AccessControlOperation.sharing).enablePublicationSharing();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(false);

    // User has ADMIN role on component
    // User rights are verified for sharing action on the document, sharing is enabled
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.admin)
        .onOperationsOf(AccessControlOperation.sharing).enablePublicationSharing();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(true);

    // User has USER role on component
    // User is going to modify the publication
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user)
        .onOperationsOf(AccessControlOperation.PERSIST_ACTIONS.iterator().next());
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(false);

    // User has PUBLISHER role on component
    // User is going to modify the publication
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.publisher)
        .onOperationsOf(AccessControlOperation.PERSIST_ACTIONS.iterator().next());
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(true);

    // User has WRITER role on component
    // User is going to modify the publication
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.writer)
        .onOperationsOf(AccessControlOperation.PERSIST_ACTIONS.iterator().next());
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized();
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
    testContext.withComponentUserRoles(SilverpeasRole.user).onGEDComponent().publicationIdIsNull();
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
        .enablePublicationSharing();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(false);

    // User has ADMIN role on component
    // User rights are verified for sharing action on the document, sharing is enabled
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.admin).onGEDComponent().publicationIdIsNull()
        .onOperationsOf(AccessControlOperation.sharing).enablePublicationSharing();
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
        .enablePublicationSharing();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(false);

    // User has ADMIN role on component
    // User rights are verified for sharing action on the document, sharing is enabled
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.admin).onGEDComponent()
        .publicationIdHasWrongFormat().onOperationsOf(AccessControlOperation.sharing)
        .enablePublicationSharing();
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
        .enablePublicationSharing();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail();
    assertIsUserAuthorized(false);

    // User has ADMIN role on component
    // User rights are verified for sharing action on the document, sharing is enabled
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.admin).onGEDComponent()
        .publicationIsNotExisting().onOperationsOf(AccessControlOperation.sharing)
        .enablePublicationSharing();
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
        .onOperationsOf(AccessControlOperation.sharing).enablePublicationSharing();
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
        .onOperationsOf(AccessControlOperation.sharing).enablePublicationSharing();
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
        .onOperationsOf(AccessControlOperation.sharing).enablePublicationSharing();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled();
    assertIsUserAuthorized(false);

    // User has ADMIN role on component
    // User rights are verified for sharing action on the document, sharing is enabled
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.admin).onGEDComponent()
        .onOperationsOf(AccessControlOperation.sharing).enablePublicationSharing();
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
    // User is going to modify the publication (cowriting enabled)
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.writer).onGEDComponent().enableCoWriting()
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
        .enablePublicationSharing();
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
        .enablePublicationSharing();
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
        .onOperationsOf(AccessControlOperation.sharing).enablePublicationSharing();
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
        .onOperationsOf(AccessControlOperation.sharing).enablePublicationSharing();
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
        .verifyCallOfNodeAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(false);

    // User has ADMIN role on component
    // User has no role on directory
    // User rights are verified for sharing action on the document, sharing is enabled
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.admin).onGEDComponent()
        .withRightsActivatedOnDirectory().withNodeUserRoles()
        .onOperationsOf(AccessControlOperation.sharing).enablePublicationSharing();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetAllFatherPK().verifyCallOfNodeAccessControllerGetUserRoles()
        .verifyCallOfNodeAccessControllerIsUserAuthorized();
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
        .verifyCallOfPublicationBmGetAllFatherPK().verifyCallOfNodeAccessControllerGetUserRoles()
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
        .verifyCallOfPublicationBmGetAllFatherPK().verifyCallOfNodeAccessControllerGetUserRoles()
        .verifyCallOfNodeAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(false);

    // User has USER role on component
    // User has PUBLISHER role on directory
    // User rights are verified for sharing action on the document, sharing is enabled
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user).onGEDComponent()
        .withRightsActivatedOnDirectory().withNodeUserRoles(SilverpeasRole.publisher)
        .onOperationsOf(AccessControlOperation.sharing).enablePublicationSharing();
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
        .onOperationsOf(AccessControlOperation.sharing).enablePublicationSharing();
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

    // User has USER role on component (cowriting enabled)
    // User has WRITER role on directory
    // User is going to modify the publication
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.user).onGEDComponent().enableCoWriting()
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
        .verifyCallOfNodeAccessControllerIsUserAuthorized();
    assertIsUserAuthorized(false);

    // User has ADMIN role on component
    // User has no role on directory
    // User rights are verified for sharing action on the document, sharing is enabled
    testContext.clear();
    testContext.withComponentUserRoles(SilverpeasRole.admin).onGEDComponent()
        .withRightsActivatedOnDirectory().withNodeUserRoles().userIsThePublicationAuthor()
        .onOperationsOf(AccessControlOperation.sharing).enablePublicationSharing();
    testContext.results().verifyCallOfComponentAccessControllerGetUserRoles()
        .verifyCallOfComponentAccessControllerIsUserAuthorized()
        .verifyCallOfPublicationBmGetDetail()
        .verifyCallOfComponentAccessControllerIsRightOnTopicsEnabled()
        .verifyCallOfPublicationBmGetAllFatherPK().verifyCallOfNodeAccessControllerGetUserRoles()
        .verifyCallOfNodeAccessControllerIsUserAuthorized();
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
        .enablePublicationSharing();
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
        .enablePublicationSharing();
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
   * @param expectedUserAuthorization
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
    private boolean isGED;
    private boolean isCoWriting;
    private boolean isPublicationSharing;
    private boolean isPubIdNull;
    private boolean isPublicationNotExisting;
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
      reset(controller, componentAccessController, nodeAccessController, publicationBm);
      isGED = false;
      isCoWriting = false;
      isPublicationSharing = false;
      isPubIdNull = false;
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

    public TestContext onGEDComponent() {
      isGED = true;
      return this;
    }

    public TestContext enableCoWriting() {
      isCoWriting = true;
      return this;
    }

    public TestContext enablePublicationSharing() {
      isPublicationSharing = true;
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
      when(componentAccessController.isPublicationSharingEnabled(anyString()))
          .then(new Returns(isPublicationSharing));
      when(nodeAccessController
          .getUserRoles(any(AccessControlContext.class), anyString(), any(NodePK.class))).then(
          new Returns(isRightsOnDirectories ?
              (nodeNotInheritedUserRoles == null ? componentUserRoles : nodeNotInheritedUserRoles) :
              (aliasUserRoles == null ? componentUserRoles : aliasUserRoles)));
      when(nodeAccessController.isUserAuthorized(any(EnumSet.class))).then(new Answer<Boolean>() {
        @Override
        public Boolean answer(final InvocationOnMock invocation) throws Throwable {
          return CollectionUtil.isNotEmpty((EnumSet) invocation.getArguments()[0]);
        }
      });
      when(publicationBm.getDetail(any(PublicationPK.class))).then(new Answer<PublicationDetail>() {
        @Override
        public PublicationDetail answer(final InvocationOnMock invocation) throws Throwable {
          if (isPublicationNotExisting) {
            return null;
          }
          PublicationDetail publi = new PublicationDetail();
          publi.setPk((PublicationPK) invocation.getArguments()[0]);
          publi.setStatus(PublicationDetail.VALID);
          publi.setCreatorId(testContext.isUserThePublicationAuthor ? userId : "otherUserId");
          return publi;
        }
      });
      when(publicationBm.getAllFatherPK(any(PublicationPK.class)))
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
      when(publicationBm.getAlias(any(PublicationPK.class))).then(new Answer<Collection<Alias>>() {
        @Override
        public Collection<Alias> answer(final InvocationOnMock invocation) throws Throwable {
          Collection<Alias> alias = new ArrayList<Alias>();
          if (testContext.aliasUserRoles != null) {
            alias.add(new Alias("nodeId",
                ((PublicationPK) invocation.getArguments()[0]).getInstanceId()));
          }
          return alias;
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
      verify(publicationBm, times(nbCallOfPublicationBmGetDetail))
          .getDetail(any(PublicationPK.class));
      verify(publicationBm, times(nbCallOfPublicationBmGetAllFatherPK))
          .getAllFatherPK(any(PublicationPK.class));
      verify(publicationBm, times(nbCallOfPublicationBmGetAlias))
          .getAlias(any(PublicationPK.class));
    }
  }

  private class PublicationAccessControllerForTest extends PublicationAccessController {
    @Override
    protected PublicationBm getPublicationBm() throws Exception {
      return publicationBm;
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