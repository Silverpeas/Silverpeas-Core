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
 * FLOSS exception. You should have received a copy of the text describing
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

package org.silverpeas.cmis;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.stubbing.Answer;
import org.silverpeas.core.cmis.model.CmisFolder;
import org.silverpeas.core.cmis.model.CmisObjectFactory;
import org.silverpeas.cmis.security.AccessControllerRegister;
import org.silverpeas.cmis.walkers.TreeWalkerForComponentInst;
import org.silverpeas.cmis.walkers.TreeWalkerForSpaceInst;
import org.silverpeas.cmis.walkers.TreeWalkerSelector;
import org.silverpeas.core.Identifiable;
import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.space.SpaceInstLight;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.admin.user.service.UserProvider;
import org.silverpeas.core.i18n.AbstractI18NBean;
import org.silverpeas.core.personalization.UserMenuDisplay;
import org.silverpeas.core.personalization.UserPreferences;
import org.silverpeas.core.personalization.service.PersonalizationService;
import org.silverpeas.core.security.authorization.ComponentAccessControl;
import org.silverpeas.core.security.authorization.SpaceAccessControl;
import org.silverpeas.core.test.extention.EnableSilverTestEnv;
import org.silverpeas.core.test.extention.LoggerExtension;
import org.silverpeas.core.test.extention.LoggerLevel;
import org.silverpeas.core.test.extention.RequesterProvider;
import org.silverpeas.core.test.extention.TestManagedBean;
import org.silverpeas.core.test.extention.TestManagedBeans;
import org.silverpeas.core.test.extention.TestManagedMock;
import org.silverpeas.core.util.logging.Level;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * This class is to defines the whole environment required to run tests against the CMIS support of
 * Silverpeas.
 * @author mmoquillon
 */
@ExtendWith(LoggerExtension.class)
@LoggerLevel(Level.DEBUG)
@TestManagedBeans({CmisObjectFactory.class, SilverpeasCmisTypeManager.class})
@EnableSilverTestEnv
public abstract class CMISEnvForTests {

  // the organizational schema of Silverpeas
  protected static SilverpeasObjectsTree organization = new SilverpeasObjectsTree();

  // the personalization service is used through the User interface to get the preferences of
  // the current user behind the request (to get its default language)
  @TestManagedMock
  protected PersonalizationService personalizationService;

  // the organization controller is used by the tree walker. In the test, the organization
  //controller will provide the data by backing an excerpt of a organizational schema of Silverpeas.
  @TestManagedMock
  protected OrganizationController organizationController;

  // used to get the current user behind the request
  @TestManagedMock
  protected UserProvider userProvider;

  // controller to check a user has the rights to access a given space
  @TestManagedMock
  protected SpaceAccessControl spaceAccessControl;

  // controller to check a user has the rights to access a given component instance
  @TestManagedMock
  protected ComponentAccessControl componentAccessControl;

  // the AccessControllerRegister is used by the CmisObjectsTreeWalker to get the correct access
  // control according to the type of the Silverpeas object being accessed by the current user
  @TestManagedBean
  protected AccessControllerRegister accessControllerRegister;

  // the walker of a (sub)tree rooted to a space
  @TestManagedBean
  protected TreeWalkerForComponentInst treeWalkerForComponentInst;

  // the walker of a subtree rooted to a component instance
  @TestManagedBean
  protected TreeWalkerForSpaceInst treeWalkerForSpaceInst;

  // the selector of the correct tree walker according to the type of the Silverpeas object being
  // accessed by the current user. This selector is used by the CmisObjectsTreeWalker to delegate
  // the (sub)tree walking to the correct walker.
  @TestManagedBean
  protected TreeWalkerSelector treeWalkerSelector;

  /**
   * Gets the path of the specified node in the organizational schema of Silverpeas used in the
   * unit tests. The path is made up of the names of the identifiable objects wrapped by each node,
   * each of them separated by the {@link CmisFolder#PATH_SEPARATOR}
   * separator. This path is to be passed to the CMIS objects tree walker that is expected the
   * tokens in a path to be the name of a CMIS object.
   * @param node a node in an organizational schema of Silverpeas.
   * @param language the language in which the token of the path should be.
   * @return the path of the node in the organizational schema of Silverpeas.
   */
  protected String pathToNode(final TreeNode node, final String language) {
    return CmisFolder.PATH_SEPARATOR + node.getPath().stream()
        .map(n -> ((AbstractI18NBean<?>)n.getObject()).getName(language))
        .collect(Collectors.joining(CmisFolder.PATH_SEPARATOR));
  }

  @RequesterProvider
  User getCurrentUser() {
    final UserDetail currentUser = new UserDetail();
    currentUser.setId("42");
    currentUser.setFirstName("John");
    currentUser.setLastName("Doo");
    currentUser.setDomainId("0");
    when(personalizationService.getUserSettings("42")).thenReturn(
        new UserPreferences("42", "en", ZoneId.of("Europe/London"), "Aurora", "WA1", false, true,
            true, UserMenuDisplay.DEFAULT));
    return currentUser;
  }

  @BeforeEach
  public void prepareMock() {
    when(userProvider.getUser(anyString())).then((Answer<User>) invocation -> {
      String userId = invocation.getArgument(0);
      User theUser;
      if (userId.equals("42")) {
        theUser = getCurrentUser();
      } else {
        UserDetail user = new UserDetail();
        user.setId(userId);
        user.setFirstName(userId.equals("0") ? null : "Toto");
        user.setLastName(userId.equals("0") ? "Administrateur" : "Tartempion" + userId);
        user.setDomainId("0");
        user.setCreationDate(new Date());
        user.setSaveDate(new Date());
        theUser = user;
      }
      return theUser;
    });

    when(userProvider.getMainAdministrator()).then(
        (Answer<User>) invocation -> userProvider.getUser("0"));

    when(spaceAccessControl.isUserAuthorized(anyString(), anyString())).thenReturn(true);
    when(componentAccessControl.isUserAuthorized(anyString(), anyString())).thenReturn(true);

    when(organizationController.getSpaceInstLightById(anyString())).then(
        (Answer<SpaceInstLight>) invocation -> {
          String spaceId = invocation.getArgument(0);
          return getInTreeAndApply(spaceId, n -> {
            Identifiable object = n.getObject();
            return object instanceof SpaceInstLight ? (SpaceInstLight) object : null;
          });
        });

    when(organizationController.getComponentInstLight(anyString())).then(
        (Answer<ComponentInstLight>) invocation -> {
          String compInstId = invocation.getArgument(0);
          return getInTreeAndApply(compInstId, n -> {
            Identifiable object = organization.findTreeNodeById(compInstId).getObject();
            return object instanceof ComponentInstLight ? (ComponentInstLight) object : null;
          });
        });

    when(organizationController.getAvailCompoIds(anyString(), anyString())).then(
        (Answer<String[]>) invocation -> {
          String spaceId = invocation.getArgument(0);
          return getInTreeAndApply(spaceId, n -> n.getChildren()
              .stream()
              .map(TreeNode::getObject)
              .filter(o -> o instanceof ComponentInstLight)
              .map(Identifiable::getId)
              .toArray(String[]::new));
        });

    when(organizationController.getAllowedSubSpaceIds(anyString(), anyString())).then(
        (Answer<String[]>) invocation -> {
          String spaceId = invocation.getArgument(1);
          return getInTreeAndApply(spaceId, n -> n.getChildren()
              .stream()
              .map(TreeNode::getObject)
              .filter(o -> o instanceof SpaceInstLight)
              .map(Identifiable::getId)
              .toArray(String[]::new));
        });

    when(organizationController.getAllRootSpaceIds(anyString())).then(
        (Answer<String[]>) invocation -> applyOnRootNodes(
            rootNodes -> rootNodes.stream().map(TreeNode::getId).toArray(String[]::new)));

    when(organizationController.getPathToSpace(anyString())).then(
        (Answer<List<SpaceInstLight>>) invocation -> {
          String spaceId = invocation.getArgument(0);
          return getInTreeAndApply(spaceId, n -> {
            List<SpaceInstLight> path = new ArrayList<>();
            TreeNode current = n;
            do {
              path.add(0, (SpaceInstLight) current.getObject());
              current = current.getParent();
            } while (current != null);
            return path;
          });
        });

    when(organizationController.getPathToComponent(anyString())).then(
        (Answer<List<SpaceInstLight>>) invocation -> {
          String compInstId = invocation.getArgument(0);
          return getInTreeAndApply(compInstId, n -> {
            List<SpaceInstLight> path = new ArrayList<>();
            TreeNode current = n;
            while (current.getParent() != null) {
              current = current.getParent();
              path.add((SpaceInstLight) current.getObject());
            }
            return path;
          });
        });
  }

  private <T> T getInTreeAndApply(String id, Function<TreeNode, T> fun) {
    TreeNode node = organization.findTreeNodeById(id);
    if (node == null) {
      return null;
    }
    return fun.apply(node);
  }

  private <T> T applyOnRootNodes(Function<Set<TreeNode>, T> fun) {
    Set<TreeNode> nodes = organization.getRootNodes();
    return fun.apply(nodes);
  }
}
  