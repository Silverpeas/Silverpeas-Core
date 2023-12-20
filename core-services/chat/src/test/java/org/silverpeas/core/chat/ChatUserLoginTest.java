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

package org.silverpeas.core.chat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.test.extention.EnableSilverTestEnv;
import org.silverpeas.core.test.extention.TestManagedMock;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

@EnableSilverTestEnv
class ChatUserLoginTest {

  @TestManagedMock
  private ChatSettings chatSettings;

  @Test
  @DisplayName("When the login is an email address, the domain part should be removed from the " +
      "chat login with the REMOVED JID format policy")
  void domainPartShouldBeRemovedFromAnEmailAslogin() {
    when(chatSettings.getJidFormatPolicy()).thenReturn(ChatSettings.JidFormatPolicy.REMOVED);

    final ChatUser user = getChatUserWithLogin("miguel.moquillon@silverpeas.org");
    assertThat(user.getChatLogin(), is("miguel.moquillon"));
  }

  @Test
  @DisplayName("When the login is an email address, the @ character should be encoded in the chat" +
      " login with the SPECIFIC_CODE JID format policy")
  void specialCharactersShouldBeEncodedInAnEmailAslogin() {
    when(chatSettings.getJidFormatPolicy()).thenReturn(ChatSettings.JidFormatPolicy.SPECIFIC_CODE);

    final ChatUser user = getChatUserWithLogin("miguel.moquillon@silverpeas.org");
    assertThat(user.getChatLogin(), is("miguel.moquillon0x40silverpeas.org"));
  }

  @Test
  @DisplayName("The login of a Silverpeas user should be taken as such for the chat login")
  void chatLoginShouldBeUserLoginByDefault() {
    final ChatUser user = getChatUserWithLogin("miguel.moquillon");
    assertThat(user.getChatLogin(), is("miguel.moquillon"));
  }

  @Test
  @DisplayName("Any blanks in the login of a Silverpeas user should be removed")
  void chatLoginShouldBeWithoutAnySpaces() {
    final ChatUser user = getChatUserWithLogin("miguel  moquillon");
    assertThat(user.getChatLogin(), is("miguelmoquillon"));
  }

  @Test
  @DisplayName("The chat login of a user should be in lower case")
  void chatLoginShouldBeAlwaysInLowerCaseByDefault() {
    final ChatUser user = getChatUserWithLogin("Miguel.Moquillon");
    assertThat(user.getChatLogin(), is("miguel.moquillon"));
  }

  @Test
  @DisplayName("The chat login of a user must not contain any accented characters")
  void chatLoginMustNotContainAnyAccentedCharacters() {
    final ChatUser user = getChatUserWithLogin("Élodie.Léaûx-Fõntäiné");
    assertThat(user.getChatLogin(), is("elodie.leaux-fontaine"));
  }

  @Test
  @DisplayName("The chat login of a user should be in lower case even with an email-like address")
  void chatLoginShouldBeAlwaysInLowerCaseInAnyCircumstances() {
    when(chatSettings.getJidFormatPolicy()).thenReturn(ChatSettings.JidFormatPolicy.REMOVED);

    final ChatUser user = getChatUserWithLogin("Miguel.Moquillon@silverpeas.com");
    assertThat(user.getChatLogin(), is("miguel.moquillon"));
  }

  private ChatUser getChatUserWithLogin(final String login) {
    UserDetail user = new UserDetail();
    user.setId("42");
    user.setLogin(login);
    return ChatUser.fromUser(user);
  }
}