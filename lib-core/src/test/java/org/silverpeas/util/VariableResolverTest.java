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

package org.silverpeas.util;

import org.junit.*;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

public class VariableResolverTest {

  @Before
  public void setUpSystemProperties() {
    System.setProperty("Foo", "Toto");
  }

  @Test
  public void testResolveAStringValueWithVariables() throws Exception {
    String value = "${prop.Foo} is at home";
    String actual = VariableResolver.resolve(value);
    assertThat(actual, is("Toto is at home"));
  }

  @Test
  public void testResolveAStringValueWithoutAnyVariables() throws Exception {
    String value = "Lolo is at home";
    String actual = VariableResolver.resolve(value);
    assertThat(actual, is("Lolo is at home"));
  }

  @Test
  public void testResolveAStringValueWithAVariableAtTheMiddle() throws Exception {
    String value = "Once upon a time, ${prop.Foo} left his home to go at work";
    String actual = VariableResolver.resolve(value);
    assertThat(actual, is("Once upon a time, Toto left his home to go at work"));
  }

  @Test
  public void testResolveAnObjectBeingAStringWithVariables() throws Exception {
    Object value = "${prop.Foo} is at home";
    Object actual = VariableResolver.resolve(value);
    assertThat(actual, is("Toto is at home"));
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