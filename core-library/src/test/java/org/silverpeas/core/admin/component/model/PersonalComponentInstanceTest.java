/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.admin.component.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.admin.component.PersonalComponentRegistry;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.admin.user.service.UserProvider;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateManager;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;
import org.silverpeas.kernel.test.annotations.TestManagedBean;
import org.silverpeas.kernel.test.annotations.TestManagedBeans;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * This class of unit tests deals with the resources (component xml descriptors) handled from
 * {@link PersonalComponentRegistry}.
 * @author Yohann Chastagnier
 */
@EnableSilverTestEnv(context = JEETestContext.class)
@TestManagedBeans(PublicationTemplateManager.class)
class PersonalComponentInstanceTest {

  private static final String USER_CALENDAR_PERSONAL_COMPONENT_NAME = "userCalendar";

  @BeforeEach
  public void setup(@TestManagedBean PersonalComponentRegistry registry) throws Exception {
    registry.init();
  }

  @Test
  void getInstanceFromExistingUserAndPersonalComponentShouldWork() {
    User user = putInContextMockedUserWithId("26");
    PersonalComponent personalComponent =
        PersonalComponent.getByName(USER_CALENDAR_PERSONAL_COMPONENT_NAME).orElse(null);
    String expectedInstanceId = USER_CALENDAR_PERSONAL_COMPONENT_NAME + user.getId() + "_PCI";
    PersonalComponentInstance instance = PersonalComponentInstance.from(user, personalComponent);
    assertThat(instance, notNullValue());
    assertThat(instance.getUser(), is(user));
    assertThat(instance.getId(), is(expectedInstanceId));
    assertThat(instance.getName(), is(USER_CALENDAR_PERSONAL_COMPONENT_NAME));
    assertThat(instance.getLabel(), is("Mes agendas"));
    assertThat(instance.getLabel("en"), is("My diaries"));
    assertThat(instance.getDescription(), is("Vos agendas personnels."));
    assertThat(instance.getDescription("en"), is("Your personal diaries."));
  }

  @Test
  void getInstanceFromUnknownUserButExistingPersonalComponentShouldThrowAnError() {
    assertThrows(IllegalArgumentException.class, () -> {
      User user = putInContextMockedUserWithId("26");
      PersonalComponentInstance.from(user, null);
    });
  }

  @Test
  void getInstanceFromExistingUserButUnknownPersonalComponentShouldThrowAnError() {
    assertThrows(IllegalArgumentException.class, () -> {
      PersonalComponent personalComponent =
          PersonalComponent.getByName(USER_CALENDAR_PERSONAL_COMPONENT_NAME).orElse(null);
      PersonalComponentInstance.from(null, personalComponent);
    });
  }

  @Test
  void getInstanceFromValidInstanceIdentifierShouldWork() {
    String validInstanceId = USER_CALENDAR_PERSONAL_COMPONENT_NAME + "26_PCI";
    User user = putInContextMockedUserWithId("26");
    Optional<PersonalComponentInstance> optionalInstance =
        PersonalComponentInstance.from(validInstanceId);
    assertThat(optionalInstance.isPresent(), is(true));
    PersonalComponentInstance instance = optionalInstance.get();
    assertThat(instance.getId(), is(validInstanceId));
    assertThat(instance.getUser(), is(user));
    assertThat(instance.getLabel("en"), is("My diaries"));
  }

  @Test
  void getInstanceFromNotDefinedInstanceIdentifierShouldThrowError() {
    assertThrows(IllegalArgumentException.class, () -> PersonalComponentInstance.from("  "));
  }

  @Test
  void getInstanceFromInstanceIdentifierLinkingNoPersonalComponentShouldReturnNoInstance() {
    String notValidInstanceId = "unknown_26_PCI";
    putInContextMockedUserWithId("26");
    Optional<PersonalComponentInstance> optionalInstance =
        PersonalComponentInstance.from(notValidInstanceId);
    assertThat(optionalInstance.isPresent(), is(false));
  }

  @Test
  void getInstanceFromInstanceIdentifierLinkingNoUserShouldReturnNoInstance() {
    String notValidInstanceId = USER_CALENDAR_PERSONAL_COMPONENT_NAME + "26_PCI";
    assertThat(PersonalComponentInstance.from(notValidInstanceId).isPresent(), is(false));
  }

  @Test
  void getInstanceFromInstanceIdentifierWithBadSuffixShouldWork() {
    String validInstanceId = USER_CALENDAR_PERSONAL_COMPONENT_NAME + "26_PCI";
    String notValidInstanceId1 = USER_CALENDAR_PERSONAL_COMPONENT_NAME + "26_PC";
    String notValidInstanceId2 = USER_CALENDAR_PERSONAL_COMPONENT_NAME + "26_PCA";
    String notValidInstanceId3 = USER_CALENDAR_PERSONAL_COMPONENT_NAME + "26_PCII";
    putInContextMockedUserWithId("26");
    assertThat(PersonalComponentInstance.from(validInstanceId).isPresent(), is(true));
    assertThat(PersonalComponentInstance.from(notValidInstanceId1).isPresent(), is(false));
    assertThat(PersonalComponentInstance.from(notValidInstanceId2).isPresent(), is(false));
    assertThat(PersonalComponentInstance.from(notValidInstanceId3).isPresent(), is(false));
  }

  private User putInContextMockedUserWithId(final String userId) {
    User user = mock(User.class);
    when(user.getId()).thenReturn(userId);
    when(UserProvider.get().getUser(userId)).thenReturn(user);
    return user;
  }
}