/*
 * Copyright (C) 2000 - 2016 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.admin.component.model;

import org.jglue.cdiunit.CdiRunner;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.admin.component.PersonalComponentRegistry;
import org.silverpeas.core.admin.component.PersonalComponentRegistryTest;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.admin.user.service.UserProvider;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateManager;
import org.silverpeas.core.test.rule.CommonAPI4Test;
import org.silverpeas.core.util.lang.SystemWrapper;

import javax.inject.Inject;
import java.io.File;
import java.util.Optional;

import static org.apache.commons.io.FileUtils.getFile;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * This class of unit tests deals with the resources (component xml descriptors) handled from
 * {@link PersonalComponentRegistryTest}.
 * @author Yohann Chastagnier
 */
@RunWith(CdiRunner.class)
public class PersonalComponentInstanceTest {

  private static File TEMPLATES_PATH;
  private static File TARGET_DIR;
  private static final String USER_CALENDAR_PERSONAL_COMPONENT_NAME = "userCalendar";

  private CommonAPI4Test commonAPI4Test = new CommonAPI4Test();

  @Inject
  private PersonalComponentRegistry registry;

  @BeforeClass
  public static void generalSetup() {
    TARGET_DIR = getFile(
        PersonalComponentRegistryTest.class.getProtectionDomain().getCodeSource().getLocation()
            .getFile());
    TEMPLATES_PATH = getFile(TARGET_DIR, "templateRepository");
  }

  @Rule
  public CommonAPI4Test getCommonAPI4Test() {
    return commonAPI4Test;
  }

  @Before
  public void setup() throws Exception {
    PublicationTemplateManager.templateDir = TEMPLATES_PATH.getPath();
    SystemWrapper.get().getenv().put("SILVERPEAS_HOME", TARGET_DIR.getPath());
    commonAPI4Test.injectIntoMockedBeanContainer(registry);
    registry.init();
  }

  @Test
  public void getInstanceFromExistingUserAndPersonalComponentShouldWork() {
    User user = putInContextMockedUserWithId("26");
    PersonalComponent personalComponent =
        PersonalComponent.get(USER_CALENDAR_PERSONAL_COMPONENT_NAME).get();
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

  @Test(expected = IllegalArgumentException.class)
  public void getInstanceFromUnknownUserButExistingPersonalComponentShouldThrowAnError() {
    User user = putInContextMockedUserWithId("26");
    PersonalComponentInstance.from(user, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void getInstanceFromExistingUserButUnknownPersonalComponentShouldThrowAnError() {
    PersonalComponent personalComponent =
        PersonalComponent.get(USER_CALENDAR_PERSONAL_COMPONENT_NAME).get();
    PersonalComponentInstance.from(null, personalComponent);
  }

  @Test
  public void getInstanceFromValidInstanceIdentifierShouldWork() {
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

  @Test(expected = IllegalArgumentException.class)
  public void getInstanceFromNotDefinedInstanceIdentifierShouldThrowError() {
    PersonalComponentInstance.from("  ");
  }

  @Test
  public void getInstanceFromInstanceIdentifierLinkingNoPersonalComponentShouldReturnNoInstance() {
    String notValidInstanceId = "unknown_26_PCI";
    putInContextMockedUserWithId("26");
    Optional<PersonalComponentInstance> optionalInstance =
        PersonalComponentInstance.from(notValidInstanceId);
    assertThat(optionalInstance.isPresent(), is(false));
  }

  @Test
  public void getInstanceFromInstanceIdentifierLinkingNoUserShouldReturnNoInstance() {
    String notValidInstanceId = USER_CALENDAR_PERSONAL_COMPONENT_NAME + "26_PCI";
    assertThat(PersonalComponentInstance.from(notValidInstanceId).isPresent(), is(false));
  }

  @Test
  public void getInstanceFromInstanceIdentifierWithBadSuffixShouldWork() {
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