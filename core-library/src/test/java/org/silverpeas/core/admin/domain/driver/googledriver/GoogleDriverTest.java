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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.admin.domain.driver.googledriver;


import com.google.api.services.directory.model.User;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.admin.domain.DomainDriver;
import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.test.unit.extention.EnableSilverTestEnv;
import org.silverpeas.core.util.SettingBundle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.silverpeas.core.admin.domain.driver.googledriver.GoogleUserBuilder.aUser;

/**
 * @author silveryocha
 */
@EnableSilverTestEnv
class GoogleDriverTest {

  private static final List<User> allGoogleUsers = new ArrayList<>();

  @BeforeAll
  static void setup() {
    allGoogleUsers.add(aUser("A", "/").withEmail("a@a.a", "work").build());
    allGoogleUsers.add(aUser("B", "/SIEGE/DRH/ARH").build());
    allGoogleUsers.add(aUser("C", "/").withEmail("c@c.c", "").build());
    allGoogleUsers.add(aUser("D", "/").build());
    allGoogleUsers.add(aUser("E", "/SIEGE/DRH").build());
    allGoogleUsers.add(aUser("F", "/").build());
    allGoogleUsers.add(aUser("G", "/SIEGE/EXCLUSION").withCustomEmail("g@g.g", "work").suspended().build());
    allGoogleUsers.add(aUser("H", "/").withEmail("hh@hh.hh", "work").withCustomEmail("h@h.h", "perso").build());
    allGoogleUsers.add(aUser("I", "/DSI/AMOA").build());
    allGoogleUsers.add(aUser("J", "/DSI/ATOA").suspended().build());
    allGoogleUsers.add(aUser("K", "/").build());
  }

  @Test
  void getAllUsersWithDefaultSettings() throws AdminException {
    final SettingBundle settings = mock(SettingBundle.class);
    when(settings.getString(anyString(), anyString())).then(i -> i.getArguments()[1]);
    final UserDetail[] allUsers = getGoogleDriver(settings).getAllUsers();
    assertThat(allUsers, arrayWithSize(allGoogleUsers.size()));

    assertThat(allUsers[0].getLogin(), is(allGoogleUsers.get(0).getPrimaryEmail()));
    assertThat(allUsers[0].getLogin(), not(is("a@a.a")));
    assertThat(allUsers[0].getEmailAddress(), is("a@a.a"));
    assertThat(allUsers[0].isValidState(), is(true));

    assertThat(allUsers[2].getLogin(), is(allGoogleUsers.get(2).getPrimaryEmail()));
    assertThat(allUsers[2].getEmailAddress(), is(allGoogleUsers.get(2).getPrimaryEmail()));
    assertThat(allUsers[2].isValidState(), is(true));

    assertThat(allUsers[5].getLogin(), is(allGoogleUsers.get(5).getPrimaryEmail()));
    assertThat(allUsers[5].getEmailAddress(), is(allGoogleUsers.get(5).getPrimaryEmail()));
    assertThat(allUsers[5].isValidState(), is(true));

    assertThat(allUsers[6].getLogin(), is(allGoogleUsers.get(6).getPrimaryEmail()));
    assertThat(allUsers[6].getLogin(), not(is("g@g.g")));
    assertThat(allUsers[6].getEmailAddress(), is("g@g.g"));
    assertThat(allUsers[6].isDeactivatedState(), is(true));

    assertThat(allUsers[7].getLogin(), is(allGoogleUsers.get(7).getPrimaryEmail()));
    assertThat(allUsers[7].getLogin(), not(is("hh@hh.hh")));
    assertThat(allUsers[7].getEmailAddress(), is("hh@hh.hh"));
    assertThat(allUsers[7].isValidState(), is(true));

    assertThat(allUsers[9].getLogin(), is(allGoogleUsers.get(9).getPrimaryEmail()));
    assertThat(allUsers[9].getEmailAddress(), is(allGoogleUsers.get(9).getPrimaryEmail()));
    assertThat(allUsers[9].isDeactivatedState(), is(true));
  }

  @Test
  void getAllUsersWithOneOuInclusion() throws AdminException {
    final SettingBundle settings = mock(SettingBundle.class);
    when(settings.getString(anyString(), anyString())).then(i -> {
      Object value = i.getArguments()[1];
      if ("google.user.filter.rule".equals(i.getArguments()[0])) {
        value = "orgUnitPath=/SIEGE%";
      }
      return value;
    });
    final UserDetail[] allUsers = getGoogleDriver(settings).getAllUsers();
    assertThat(Arrays.stream(allUsers).map(UserDetail::getSpecificId).collect(Collectors.joining()), is("BEG"));
  }

  @Test
  void getAllUsersWithSeveralOuInclusions() throws AdminException {
    final SettingBundle settings = mock(SettingBundle.class);
    when(settings.getString(anyString(), anyString())).then(i -> {
      Object value = i.getArguments()[1];
      if ("google.user.filter.rule".equals(i.getArguments()[0])) {
        value = "|(orgUnitPath=/SIEGE%)(orgUnitPath=/DSI/ATOA%)";
      }
      return value;
    });
    final UserDetail[] allUsers = getGoogleDriver(settings).getAllUsers();
    assertThat(Arrays.stream(allUsers).map(UserDetail::getSpecificId).collect(Collectors.joining()), is("BEGJ"));
  }

  @Test
  void getAllUsersWithOneOuExclusion() throws AdminException {
    final SettingBundle settings = mock(SettingBundle.class);
    when(settings.getString(anyString(), anyString())).then(i -> {
      Object value = i.getArguments()[1];
      if ("google.user.filter.rule".equals(i.getArguments()[0])) {
        value = "!(|(orgUnitPath=/SIEGE%)(orgUnitPath=/DSI/A%))";
      }
      return value;
    });
    final UserDetail[] allUsers = getGoogleDriver(settings).getAllUsers();
    assertThat(Arrays.stream(allUsers).map(UserDetail::getSpecificId).collect(Collectors.joining()), is("ACDFHK"));
  }

  @Test
  void getAllUsersWithSeveralOuExclusions() throws AdminException {
    final SettingBundle settings = mock(SettingBundle.class);
    when(settings.getString(anyString(), anyString())).then(i -> {
      Object value = i.getArguments()[1];
      if ("google.user.filter.rule".equals(i.getArguments()[0])) {
        value = "!(orgUnitPath=/SIEGE%)";
      }
      return value;
    });
    final UserDetail[] allUsers = getGoogleDriver(settings).getAllUsers();
    assertThat(Arrays.stream(allUsers).map(UserDetail::getSpecificId).collect(Collectors.joining()), is("ACDFHIJK"));
  }

  @Test
  void getAllUsersWithSeveralOuInclusionsAndExclusions() throws AdminException {
    final SettingBundle settings = mock(SettingBundle.class);
    when(settings.getString(anyString(), anyString())).then(i -> {
      Object value = i.getArguments()[1];
      if ("google.user.filter.rule".equals(i.getArguments()[0])) {
        value = "&(!(|(orgUnitPath=/SIEGE/EXCLUSION%)(orgUnitPath=/DSI/ATOA%)))(|(orgUnitPath=/SIEGE%)(orgUnitPath=/DSI%))";
      }
      return value;
    });
    final UserDetail[] allUsers = getGoogleDriver(settings).getAllUsers();
    assertThat(Arrays.stream(allUsers).map(UserDetail::getSpecificId).collect(Collectors.joining()), is("BEI"));
  }

  private DomainDriver getGoogleDriver(SettingBundle settings) throws AdminException {
    final GoogleDriver driver = new GoogleDriver4Test();
    driver.initFromProperties(settings);
    return driver;
  }

  private static class GoogleDriver4Test extends GoogleDriver {
    private final GoogleDirectoryRequester requester;

    private GoogleDriver4Test() throws AdminException {
      this.requester = mock(GoogleDirectoryRequester.class);
      when(requester.users()).then(i -> new GoogleUserFilter<>(allGoogleUsers,
          settings.getString("google.user.filter.rule", "")).apply());
    }

    @Override
    GoogleDirectoryRequester request() {
      return requester;
    }
  }
}