/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.chat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.user.UserRegistrationService;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.admin.user.service.UserProvider;
import org.silverpeas.core.chat.servers.ChatServer;
import org.silverpeas.core.socialnetwork.relationship.RelationShipService;
import org.silverpeas.core.test.extention.EnableSilverTestEnv;
import org.silverpeas.core.test.extention.TestManagedMock;
import org.silverpeas.core.test.extention.TestManagedMocks;
import org.silverpeas.core.test.extention.TestedBean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.*;

/**
 * Unit tests on the registration of users in the remote chat server.
 * @author mmoquillon
 */
@EnableSilverTestEnv
@TestManagedMocks(UserRegistrationService.class)
class ChatUserRegistrationTest {

  @TestManagedMock
  private OrganizationController controller;

  @TestManagedMock(stubbed = false)
  private ChatSettings settings;

  @TestManagedMock(stubbed = false)
  private ChatServer chatServer;

  @TestManagedMock
  private RelationShipService relationShip;

  @TestedBean
  private ChatUsersRegistration registration;

  private UserProvider userProvider;

  @BeforeEach
  void setUpMocks() {
    userProvider = UserProvider.get();
    assertThat(userProvider, notNullValue());
    assertThat(registration, notNullValue());
    assertThat(controller, notNullValue());
  }

  @Test
  @DisplayName("A user should be registered only if the chat is enabled for Silverpeas")
  void registerAUserWhereasTheChatIsNotEnabledShouldNotSucceed() {
    User aUser = aUser();
    when(settings.isChatEnabled()).thenReturn(false);
    assertThat(registration.isChatServiceEnabled(), is(false));

    registration.registerUser(aUser);
    verify(chatServer, times(0)).createUser(aUser);
  }

  @Test
  @DisplayName("A user should be registered only if this isn't already done")
  void registerAUserAlreadyRegisteredShouldNotSucceed() {
    User aUser = aUser();
    when(settings.isChatEnabled()).thenReturn(true);
    when(settings.getAllowedUserGroups()).thenReturn(Collections.emptyList());
    when(chatServer.isUserExisting(aUser)).thenReturn(true);
    assertThat(registration.isChatServiceEnabled(), is(true));

    registration.registerUser(aUser);
    verify(chatServer, times(0)).createUser(aUser);
  }

  @Test
  @DisplayName(
      "A user shouldn't be registered if the domain he belongs to isn't mapped to an XMPP domain.")
  void registerAUserInANonExplicitMappedDomainAndWithoutDefaultMappingShouldNotSucceed() {
    final User aUser = aUser();
    when(settings.isChatEnabled()).thenReturn(true);
    when(chatServer.isUserExisting(aUser)).thenReturn(false);
    when(settings.getExplicitMappedXmppDomain(aUser.getDomainId())).thenReturn("");
    when(settings.getDefaultXmppDomain()).thenReturn("");

    registration.registerUser(aUser);
    verify(chatServer, times(0)).createUser(aUser);
  }

  @Test
  @DisplayName(
      "A user shouldn't be registered if he isn't in a group allowed to use the chat service")
  void registerAUserInAMappedDomainButNotInAnAllowedGroupShouldNotSucceed() {
    final User aUser = aUser();
    when(settings.isChatEnabled()).thenReturn(true);
    when(chatServer.isUserExisting(aUser)).thenReturn(false);
    when(settings.getExplicitMappedXmppDomain(aUser.getDomainId())).thenReturn("im.silverpeas.net");
    when(settings.getAllowedUserGroups()).thenReturn(Collections.singletonList("42"));
    when(controller.getAllGroupIdsOfUser(aUser.getId())).thenReturn(new String[]{"12"});

    registration.registerUser(aUser);
    verify(chatServer, times(0)).createUser(aUser);
  }

  @Test
  @DisplayName(
      "A user should be registered if the domain he belongs to is mapped to an XMPP domain")
  void registerAUserInAnExplicitMappedDomainShouldSucceed() {
    final User aUser = aUser();
    when(settings.isChatEnabled()).thenReturn(true);
    when(chatServer.isUserExisting(aUser)).thenReturn(false);
    when(settings.getExplicitMappedXmppDomain(aUser.getDomainId())).thenReturn("im.silverpeas.net");
    when(settings.getAllowedUserGroups()).thenReturn(Collections.emptyList());
    when(relationShip.getMyContactsIds(Integer.parseInt(aUser.getId()))).thenReturn(
        Collections.emptyList());

    registration.registerUser(aUser);
    verify(chatServer, times(1)).createUser(aUser);
  }

  @Test
  @DisplayName(
      "A user should be registered if the domain he belongs to isn't mapped to an XMPP domain but" +
          " a default mapping to an XMPP domain exists")
  void registerAUserInANonExplicitMappedDomainButWithADefaultMappingShouldSucceed() {
    final User aUser = aUser();
    when(settings.isChatEnabled()).thenReturn(true);
    when(chatServer.isUserExisting(aUser)).thenReturn(false);
    when(settings.getExplicitMappedXmppDomain(aUser.getDomainId())).thenReturn("");
    when(settings.getDefaultXmppDomain()).thenReturn("im.silverpeas.net");
    when(settings.getAllowedUserGroups()).thenReturn(Collections.emptyList());
    when(relationShip.getMyContactsIds(Integer.parseInt(aUser.getId()))).thenReturn(
        Collections.emptyList());

    registration.registerUser(aUser);
    verify(chatServer, times(1)).createUser(aUser);
  }

  @Test
  @DisplayName(
      "A user should be registered if his domain is mapped to an XMPP domain and if he's in a " +
          "group allowed to use the chat service")
  void registerAUserInAMappedDomainAndInAnAllowedGroupShouldSucceed() {
    final User aUser = aUser();
    when(settings.isChatEnabled()).thenReturn(true);
    when(chatServer.isUserExisting(aUser)).thenReturn(false);
    when(settings.getExplicitMappedXmppDomain(aUser.getDomainId())).thenReturn("im.silverpeas.net");
    when(settings.getAllowedUserGroups()).thenReturn(Arrays.asList("2", "42"));
    when(controller.getAllGroupIdsOfUser(aUser.getId())).thenReturn(new String[]{"12", "42"});

    registration.registerUser(aUser);
    verify(chatServer, times(1)).createUser(aUser);
  }

  @Test
  @DisplayName("The contacts of a user in the same domain should be registered with him")
  void registerAUserInMappedDomainWithItsContactsInSameDomainShouldSucceed() {
    final User aUser = aUser();
    final List<String> theConnections = connections(1, aUser);
    when(settings.isChatEnabled()).thenReturn(true);
    when(chatServer.isUserExisting(aUser)).thenReturn(false);
    when(settings.getExplicitMappedXmppDomain(aUser.getDomainId())).thenReturn("im.silverpeas.net");
    when(settings.getAllowedUserGroups()).thenReturn(Collections.emptyList());
    when(relationShip.getMyContactsIds(Integer.parseInt(aUser.getId()))).thenReturn(theConnections);

    registration.registerUser(aUser);

    verify(chatServer, times(1)).createUser(aUser);
    theConnections.stream().map(c -> userProvider.getUser(c)).forEach(c -> {
      verify(chatServer, times(1)).createUser(c);
      verify(chatServer, times(1)).createRelationShip(aUser, c);
    });
  }

  @Test
  @DisplayName("The contacts of a user in a mapped domain should be registered with him")
  void registerAUserInMappedDomainWithItsContactsInMappedDomainShouldSucceed() {
    final User aUser = aUser();
    final List<String> theConnections = connections(3, aUser);
    when(settings.isChatEnabled()).thenReturn(true);
    when(chatServer.isUserExisting(aUser)).thenReturn(false);
    when(settings.getExplicitMappedXmppDomain(aUser.getDomainId())).thenReturn("im.silverpeas.net");
    when(settings.getDefaultXmppDomain()).thenReturn("foo.silverpeas.net");
    when(settings.getAllowedUserGroups()).thenReturn(Collections.emptyList());
    when(relationShip.getMyContactsIds(Integer.parseInt(aUser.getId()))).thenReturn(theConnections);

    registration.registerUser(aUser);

    verify(chatServer, times(1)).createUser(aUser);
    theConnections.stream().map(c -> userProvider.getUser(c)).forEach(c -> {
      verify(chatServer, times(1)).createUser(c);
      verify(chatServer, times(1)).createRelationShip(aUser, c);
    });
  }

  @Test
  @DisplayName(
      "The contacts of a user in a mapped domain and in a group allowed to access the chat " +
          "service should be registered with him")
  void registerAUserAndHisContactsInTheAllowedGroupsShouldBeRegistered() {
    final User aUser = aUser();
    final List<String> theConnections = connections(3, aUser);
    when(settings.isChatEnabled()).thenReturn(true);
    when(chatServer.isUserExisting(aUser)).thenReturn(false);
    when(settings.getExplicitMappedXmppDomain(aUser.getDomainId())).thenReturn("im.silverpeas.net");
    when(settings.getDefaultXmppDomain()).thenReturn("foo.silverpeas.net");
    when(settings.getAllowedUserGroups()).thenReturn(Arrays.asList("42", "12"));
    when(controller.getAllGroupIdsOfUser(anyString())).thenReturn(new String[]{"42"});
    when(relationShip.getMyContactsIds(Integer.parseInt(aUser.getId()))).thenReturn(theConnections);

    registration.registerUser(aUser);

    verify(chatServer, times(1)).createUser(aUser);
    theConnections.stream().map(c -> userProvider.getUser(c)).forEach(c -> {
      verify(chatServer, times(1)).createUser(c);
      verify(chatServer, times(1)).createRelationShip(aUser, c);
    });
  }

  @Test
  @DisplayName(
      "Some contacts of a user in a mapped same domain should be registered with him; Not his " +
          "others contacts")
  void registerAUserInMappedDomainWithItsContactsNotAllInMappedDomainsShouldSucceed() {
    final User aUser = aUser();
    final List<String> theConnections = connections(3, aUser);
    when(settings.isChatEnabled()).thenReturn(true);
    when(chatServer.isUserExisting(aUser)).thenReturn(false);
    when(settings.getExplicitMappedXmppDomain(aUser.getDomainId())).thenReturn("im.silverpeas.net");
    when(settings.getAllowedUserGroups()).thenReturn(Collections.emptyList());
    when(relationShip.getMyContactsIds(Integer.parseInt(aUser.getId()))).thenReturn(theConnections);

    registration.registerUser(aUser);

    verify(chatServer, times(1)).createUser(aUser);
    theConnections.stream().map(c -> userProvider.getUser(c)).forEach(c -> {
      final int invocationCount = c.getDomainId().equals(aUser.getDomainId()) ? 1 : 0;
      verify(chatServer, times(invocationCount)).createUser(c);
      verify(chatServer, times(invocationCount)).createRelationShip(aUser, c);
    });
  }

  @Test
  @DisplayName(
      "Some contacts of a user in a mapped domain and in a group allowed to use the chat service " +
          "should be registered with him; Not his others contacts")
  void registerAUserAndHisContactsInAMappedDomainAndSomeInAnAllowedGroupsShouldSucceed() {
    final User aUser = aUser();
    final List<String> theConnections = connections(5, aUser);
    final List<String> allowed =
        Arrays.asList(theConnections.get(0), theConnections.get(2), theConnections.get(4));
    when(settings.isChatEnabled()).thenReturn(true);
    when(chatServer.isUserExisting(aUser)).thenReturn(false);
    when(settings.getExplicitMappedXmppDomain(aUser.getDomainId())).thenReturn("im.silverpeas.net");
    when(settings.getAllowedUserGroups()).thenReturn(Arrays.asList("42", "12"));
    when(controller.getAllGroupIdsOfUser(anyString())).thenAnswer((Answer<String[]>) i -> {
      String userId = i.getArgument(0);
      return allowed.contains(userId) || userId.equals(aUser.getId()) ? new String[]{"42"} :
          new String[0];
    });
    when(relationShip.getMyContactsIds(Integer.parseInt(aUser.getId()))).thenReturn(theConnections);

    registration.registerUser(aUser);

    verify(chatServer, times(1)).createUser(aUser);
    theConnections.stream().map(c -> userProvider.getUser(c)).forEach(c -> {
      final int invocationCount =
          c.getDomainId().equals(aUser.getDomainId()) && allowed.contains(c.getId()) ? 1 : 0;
      verify(chatServer, times(invocationCount)).createUser(c);
      verify(chatServer, times(invocationCount)).createRelationShip(aUser, c);
    });
  }

  private User injectUser(final String id, final String domainId, final String firstName,
      final String lastName) {
    final UserDetail user = new UserDetail();
    user.setId(id);
    user.setDomainId(domainId);
    user.setLogin(firstName + "." + lastName);
    user.setFirstName(firstName);
    user.setLastName(lastName);
    user.setEmailAddress(user.getLogin() + "@silverpeas.org");
    when(userProvider.getUser(id)).thenReturn(user);
    return user;
  }

  final User aUser() {
    return injectUser("32", "0", "Bart", "Simpson");
  }

  final List<String> connections(final int count, final User user) {
    final List<String> connections = new ArrayList<>();
    for (int i = 0; i < count; i++) {
      final String connectionId = String.valueOf(i + 50);
      final String connectionDomainId = String.valueOf(i);
      final User connection = injectUser(connectionId, connectionDomainId, "John", "Doe" + i);
      connections.add(connection.getId());
    }
    return connections;
  }
}
  