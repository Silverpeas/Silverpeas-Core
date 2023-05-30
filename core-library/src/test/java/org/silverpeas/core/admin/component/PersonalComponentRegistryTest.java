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
import org.silverpeas.core.admin.component.model.LocalizedComponent;
import org.silverpeas.core.admin.component.model.PersonalComponent;
import org.silverpeas.core.test.extention.EnableSilverTestEnv;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;

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

  @Test
  void getUserCalendarPersonalComponentShouldWork() {
    Optional<PersonalComponent> result = registry.getPersonalComponent("userCalendar");
    assertThat(result.isPresent(), is(true));

    PersonalComponent userCalendar = result.get();
    LocalizedComponent frUserCalendar = new LocalizedComponent(userCalendar, "fr");
    LocalizedComponent enUserCalendar = new LocalizedComponent(userCalendar, "en");
    LocalizedComponent deUserCalendar = new LocalizedComponent(userCalendar, "de");
    assertThat(userCalendar.getName(), is("userCalendar"));
    assertThat(frUserCalendar.getLabel(), is("Mes agendas"));
    assertThat(enUserCalendar.getLabel(), is("My diaries"));
    assertThat(deUserCalendar.getLabel(), is("My diaries"));
    assertThat(frUserCalendar.getDescription(), is("Vos agendas personnels."));
    assertThat(enUserCalendar.getDescription(), is("Your personal diaries."));
    assertThat(deUserCalendar.getDescription(), is("Your personal diaries."));
    assertThat(userCalendar.isVisible(), is(true));
    assertThat(userCalendar.getParameters(), is(empty()));
  }

  @Test
  void getUnknownPersonalComponentShouldReturnOptionalEmptyResult() {
    Optional<PersonalComponent> result = registry.getPersonalComponent("unknown");
    assertThat(result.isPresent(), is(false));
  }
}
