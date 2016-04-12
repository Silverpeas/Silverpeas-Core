/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.util;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.silverpeas.core.test.rule.CommonAPI4Test;
import org.silverpeas.core.util.VariableResolver;
import org.silverpeas.core.util.lang.SystemWrapper;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class VariableResolverTest {

  @Rule
  public CommonAPI4Test commonAPI4Test = new CommonAPI4Test();

  @Before
  public void setUpVariables() {
    SystemWrapper.get().setProperty("Foo", "Toto");
    SystemWrapper.get().getenv().put("Foo", "Titi");
  }

  @Test
  public void testResolveAStringValueWithPropertyVariables() throws Exception {
    String value = "${sys.Foo} is at home";
    String actual = VariableResolver.resolve(value);
    assertThat(actual, is("Toto is at home"));
  }

  @Test
  public void testResolveAStringValueWithEnvVariables() throws Exception {
    String value = "${env.Foo} is at home";
    String actual = VariableResolver.resolve(value);
    assertThat(actual, is("Titi is at home"));
  }

  @Test
  public void testResolveAStringValueWithoutAnyVariables() throws Exception {
    String value = "Lolo is at home";
    String actual = VariableResolver.resolve(value);
    assertThat(actual, is("Lolo is at home"));
  }

  @Test
  public void testResolveAStringValueWithAPropertyVariableAtTheMiddle() throws Exception {
    String value = "Once upon a time, ${sys.Foo} left his home to go at work";
    String actual = VariableResolver.resolve(value);
    assertThat(actual, is("Once upon a time, Toto left his home to go at work"));
  }

  @Test
  public void testResolveAStringValueWithAnEnvVariableAtTheMiddle() throws Exception {
    String value = "Once upon a time, ${env.Foo} left his home to go at work";
    String actual = VariableResolver.resolve(value);
    assertThat(actual, is("Once upon a time, Titi left his home to go at work"));
  }

  @Test
  public void testResolveAnObjectBeingAStringWithPropertyVariables() throws Exception {
    Object value = "${sys.Foo} is at home";
    Object actual = VariableResolver.resolve(value);
    assertThat(actual, is("Toto is at home"));
  }

  @Test
  public void testResolveAnObjectBeingAStringWithEnvVariables() throws Exception {
    Object value = "${env.Foo} is at home";
    Object actual = VariableResolver.resolve(value);
    assertThat(actual, is("Titi is at home"));
  }

  @Test
  public void testResolveAnObjectBeingAStringWithoutAnyVariables() throws Exception {
    Object value = "Lolo is at home";
    Object actual = VariableResolver.resolve(value);
    assertThat(actual, is("Lolo is at home"));
  }

  @Test
  public void testResolveAnObjectAsANonString() throws Exception {
    Integer value = 1;
    Object actual = VariableResolver.resolve(value);
    assertThat(actual, is(1));
  }
}