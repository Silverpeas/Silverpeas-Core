/*
 * Copyright (C) 2000 - 2018 Silverpeas
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

import org.apache.commons.lang.reflect.FieldUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.admin.user.service.UserProvider;
import org.silverpeas.core.test.rule.CommonAPI4Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author silveryocha
 */
public class ChatUserTest {

  @Rule
  public CommonAPI4Test commonAPI4Test = new CommonAPI4Test();

  @Before
  public void setup() {
    final UserProvider mockedUserProvider = commonAPI4Test
        .injectIntoMockedBeanContainer(mock(UserProvider.class));
    when(mockedUserProvider.getUser(anyString())).then((a) -> {
      final UserDetail user = new UserDetail();
      user.setId((String) a.getArguments()[0]);
      return user;
    });
  }

  @Test
  public void emailLoginShouldWork() throws Exception {
    final ChatUser user = createChatUserWithLogin("yohann.chastagnier@silverpeas.org");
    assertThat(user.getChatLogin(), is("yohann.chastagnier"));
  }

  @Test
  public void chatLoginShouldAlwaysBeLowercase() throws Exception {
    final ChatUser user = createChatUserWithLogin("yohann26.cHastaGNier@silverpeas.org");
    assertThat(user.getChatLogin(), is("yohann26.chastagnier"));
  }

  @Test
  public void complexEmailLoginShouldWork() throws Exception {
    final ChatUser user = createChatUserWithLogin("yo.CHA.boule-de_boule@silverpeas.co.uk");
    assertThat(user.getChatLogin(), is("yo.cha.boule-de_boule"));
  }

  private ChatUser createChatUserWithLogin(final String login) throws IllegalAccessException {
    final ChatUser chatUser = ChatUser.getById("not the point");
    final UserDetail user = (UserDetail) FieldUtils.readDeclaredField(chatUser, "user", true);
    user.setLogin(login);
    return chatUser;
  }
}