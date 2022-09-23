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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.silverpeas.core.admin.component;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.admin.component.model.PersonalComponent;
import org.silverpeas.core.test.extention.EnableSilverTestEnv;

import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Unit test on the services provided by the PersonalComponentRegistry.
 * @author Yohann Chastagnier
 */
@EnableSilverTestEnv
class PersonalComponentRegistryTest {

  private PersonalComponentRegistry registry;

  @BeforeEach
  public void setup() throws Exception {
    // Tested registry
    registry = new PersonalComponentRegistry();
    registry.init();
  }

  @Test
  void getAllPersonalComponentShouldWork() {
    assertThat(registry.getAllPersonalComponents().size(), is(2));
  }

  @SuppressWarnings("OptionalGetWithoutIsPresent")
  @Test
  void getUserCalendarPersonalComponentShouldWork() {
    Optional<PersonalComponent> result = registry.getPersonalComponent("userCalendar");
    assertThat(result.isPresent(), is(true));

    PersonalComponent userCalendar = result.get();
    assertThat(userCalendar.getName(), is("userCalendar"));
    assertThat(userCalendar.getLabel(), hasKey("fr"));
    assertThat(userCalendar.getLabel("fr"), is("Mes agendas"));
    assertThat(userCalendar.getLabel(), hasKey("en"));
    assertThat(userCalendar.getLabel("en"), is("My diaries"));
    assertThat(userCalendar.getLabel(), hasKey("de"));
    assertThat(userCalendar.getDescription(), hasKey("fr"));
    assertThat(userCalendar.getDescription("fr"), is("Vos agendas personnels."));
    assertThat(userCalendar.getDescription(), hasKey("en"));
    assertThat(userCalendar.getDescription("en"), is("Your personal diaries."));
    assertThat(userCalendar.getDescription(), hasKey("de"));
    assertThat(userCalendar.isVisible(), is(true));
    assertThat(userCalendar.getParameters(), is(empty()));
  }

  @Test
  void getUnknownPersonalComponentShouldReturnOptionalEmptyResult() {
    Optional<PersonalComponent> result = registry.getPersonalComponent("unknown");
    assertThat(result.isPresent(), is(false));
  }
}
