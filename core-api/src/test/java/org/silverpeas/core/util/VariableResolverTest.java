/*
 * Copyright (C) 2000 - 2021 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General License as
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
 * GNU Affero General License for more details.
 *
 * You should have received a copy of the GNU Affero General License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.test.extension.EnableSilverTestEnv;
import org.silverpeas.core.util.lang.SystemWrapper;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@EnableSilverTestEnv
class VariableResolverTest {

  @BeforeEach
  void setUpVariables() {
    SystemWrapper.get().setProperty("Foo", "Toto");
    SystemWrapper.get().getenv().put("Foo", "Titi");
    SystemWrapper.get().setProperty("Home", "D:\\users\\toto");
    SystemWrapper.get().getenv().put("Home", "D:\\users\\titi");
  }

  @Test
  void testResolveAStringValueWithPropertyVariables() {
    String value = "${sys.Foo} is at home";
    String actual = VariableResolver.resolve(value);
    assertThat(actual, is("Toto is at home"));
  }

  @Test
  void testResolveAStringValueWithEnvVariables() {
    String value = "${env.Foo} is at home";
    String actual = VariableResolver.resolve(value);
    assertThat(actual, is("Titi is at home"));
  }

  @Test
  void testResolveAPathValueWithPropertyVariables() {
    String path = "${sys.Home}/Applications/Silverpeas";
    String actual = VariableResolver.resolve(path);
    assertThat(actual, is("D:/users/toto/Applications/Silverpeas"));
  }

  @Test
  void testResolveAPathValueWithEnvVariables() {
    String path = "${env.Home}/Applications/Silverpeas";
    String actual = VariableResolver.resolve(path);
    assertThat(actual, is("D:/users/titi/Applications/Silverpeas"));
  }

  @Test
  void testResolveAStringValueWithoutAnyVariables() {
    String value = "Lolo is at home";
    String actual = VariableResolver.resolve(value);
    assertThat(actual, is("Lolo is at home"));
  }

  @Test
  void testResolveAStringValueWithAPropertyVariableAtTheMiddle() {
    String value = "Once upon a time, ${sys.Foo} left his home to go at work";
    String actual = VariableResolver.resolve(value);
    assertThat(actual, is("Once upon a time, Toto left his home to go at work"));
  }

  @Test
  void testResolveAStringValueWithAnEnvVariableAtTheMiddle() {
    String value = "Once upon a time, ${env.Foo} left his home to go at work";
    String actual = VariableResolver.resolve(value);
    assertThat(actual, is("Once upon a time, Titi left his home to go at work"));
  }

  @Test
  void testResolveAnObjectBeingAStringWithPropertyVariables() {
    Object value = "${sys.Foo} is at home";
    Object actual = VariableResolver.resolve(value);
    assertThat(actual, is("Toto is at home"));
  }

  @Test
  void testResolveAnObjectBeingAStringWithEnvVariables() {
    Object value = "${env.Foo} is at home";
    Object actual = VariableResolver.resolve(value);
    assertThat(actual, is("Titi is at home"));
  }

  @Test
  void testResolveAnObjectBeingAPathVStringWithPropertyVariables() {
    Object path = "${sys.Home}/Applications/Silverpeas";
    Object actual = VariableResolver.resolve(path);
    assertThat(actual, is("D:/users/toto/Applications/Silverpeas"));
  }

  @Test
  void testResolveAnObjectBeingAPathVStringWithEnvVariables() {
    Object path = "${env.Home}/Applications/Silverpeas";
    Object actual = VariableResolver.resolve(path);
    assertThat(actual, is("D:/users/titi/Applications/Silverpeas"));
  }

  @Test
  void testResolveAnObjectBeingAStringWithoutAnyVariables() {
    Object value = "Lolo is at home";
    Object actual = VariableResolver.resolve(value);
    assertThat(actual, is("Lolo is at home"));
  }

  @Test
  void testResolveAnObjectAsANonString() {
    Integer value = 1;
    Object actual = VariableResolver.resolve(value);
    assertThat(actual, is(1));
  }
}